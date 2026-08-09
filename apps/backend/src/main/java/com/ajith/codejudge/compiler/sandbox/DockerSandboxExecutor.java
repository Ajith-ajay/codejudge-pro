package com.ajith.codejudge.compiler.sandbox;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Component;

import com.ajith.codejudge.submission.entity.SubmissionStatus;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class DockerSandboxExecutor {

    public ExecutionResult executeCode(
            String languageCode,
            String sourceCode,
            String input,
            int timeLimitMs,
            int memoryLimitMb
    ) {
        String containerName = "codejudge_run_" + UUID.randomUUID().toString().replace("-", "");
        Path tempDir = null;
        Process process = null;

        try {
            // 1. Create temp directory
            Path tempParent = Path.of("temp");
            if (!Files.exists(tempParent)) {
                Files.createDirectories(tempParent);
            }
            tempDir = Files.createTempDirectory(tempParent, "sandbox_");
            String hostPath = tempDir.toAbsolutePath().toString();

            // 2. Write source code file based on language
            String sourceFileName;
            String dockerImage;
            List<String> cmdArgs = new ArrayList<>();
            cmdArgs.add("docker");
            cmdArgs.add("run");
            cmdArgs.add("--rm");
            cmdArgs.add("-i");
            cmdArgs.add("--name=" + containerName);
            cmdArgs.add("--network=none");
            cmdArgs.add("--pids-limit=64");
            cmdArgs.add("--cap-drop=ALL");
            cmdArgs.add("--security-opt=no-new-privileges:true");
            cmdArgs.add("--memory=" + memoryLimitMb + "m");
            cmdArgs.add("--cpus=1.0");
            cmdArgs.add("-v");
            cmdArgs.add(hostPath + ":/app");
            cmdArgs.add("-w");
            cmdArgs.add("/app");

            if ("java".equalsIgnoreCase(languageCode)) {
                sourceFileName = "Solution.java";
                dockerImage = "eclipse-temurin:21-jdk-alpine";
                Files.writeString(tempDir.resolve(sourceFileName), sourceCode, StandardCharsets.UTF_8);

                cmdArgs.add(dockerImage);
                cmdArgs.add("sh");
                cmdArgs.add("-c");
                cmdArgs.add("javac Solution.java && java Solution");
            } else if ("cpp".equalsIgnoreCase(languageCode) || "c++".equalsIgnoreCase(languageCode)) {
                sourceFileName = "solution.cpp";
                dockerImage = "gcc:13-bookworm";
                Files.writeString(tempDir.resolve(sourceFileName), sourceCode, StandardCharsets.UTF_8);

                cmdArgs.add(dockerImage);
                cmdArgs.add("sh");
                cmdArgs.add("-c");
                cmdArgs.add("g++ -O3 solution.cpp -o solution && ./solution");
            } else { // Default to Python
                sourceFileName = "solution.py";
                dockerImage = "python:3.11-slim";
                Files.writeString(tempDir.resolve(sourceFileName), sourceCode, StandardCharsets.UTF_8);

                cmdArgs.add(dockerImage);
                cmdArgs.add("python");
                cmdArgs.add("solution.py");
            }

            // 3. Start execution process
            ProcessBuilder pb = new ProcessBuilder(cmdArgs);
            long startTime = System.nanoTime();
            process = pb.start();

            // 4. Pipe testcase input into standard input stream of the process
            if (input != null && !input.isEmpty()) {
                try (var writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream(), StandardCharsets.UTF_8))) {
                    writer.write(input);
                    writer.flush();
                }
            } else {
                process.getOutputStream().close();
            }

            // 5. Wait for the process to finish execution with time limit
            long sandboxTimeoutMs = Math.max(
                    (long) timeLimitMs + 5000L,
                    10000L
            );

            log.debug(
                    "Executing {} with timeLimitMs={} and sandboxTimeoutMs={}",
                    containerName,
                    timeLimitMs,
                    sandboxTimeoutMs
            );

            boolean finished = process.waitFor(
                    sandboxTimeoutMs,
                    TimeUnit.MILLISECONDS
            );
            long durationMs = (System.nanoTime() - startTime) / 1_000_000;

            if (!finished) {
                log.warn("Execution timed out for container {}", containerName);
                process.destroyForcibly();
                killContainer(containerName);
                return ExecutionResult.builder()
                        .status(SubmissionStatus.TIME_LIMIT_EXCEEDED)
                        .timeMs((int) durationMs)
                        .errorMessage("Time Limit Exceeded")
                        .build();
            }

            int exitCode = process.exitValue();
            String stdout = readStream(process.getInputStream());
            String stderr = readStream(process.getErrorStream());

            if (exitCode == 137) { // Docker Out Of Memory exit code
                return ExecutionResult.builder()
                        .status(SubmissionStatus.MEMORY_LIMIT_EXCEEDED)
                        .timeMs((int) durationMs)
                        .errorMessage("Memory Limit Exceeded")
                        .build();
            }

            if (exitCode != 0) {
                // Determine if compilation or runtime error
                SubmissionStatus errorStatus = SubmissionStatus.RUNTIME_ERROR;
                if ("java".equalsIgnoreCase(languageCode)) {
                    boolean classExists = Files.exists(tempDir.resolve("Solution.class"));
                    if (!classExists) {
                        errorStatus = SubmissionStatus.COMPILATION_ERROR;
                    }
                } else if ("cpp".equalsIgnoreCase(languageCode) || "c++".equalsIgnoreCase(languageCode)) {
                    boolean binaryExists = Files.exists(tempDir.resolve("solution"));
                    if (!binaryExists) {
                        errorStatus = SubmissionStatus.COMPILATION_ERROR;
                    }
                }

                return ExecutionResult.builder()
                        .status(errorStatus)
                        .timeMs((int) durationMs)
                        .errorMessage(stderr.isEmpty() ? "Runtime Error with exit code: " + exitCode : stderr)
                        .build();
            }

            // Successfully executed
            return ExecutionResult.builder()
                    .status(SubmissionStatus.ACCEPTED)
                    .timeMs((int) durationMs)
                    .output(stdout)
                    .build();

        } catch (Exception e) {
            log.error("Exception in Docker sandbox execution", e);
            if (process != null) {
                process.destroyForcibly();
            }
            killContainer(containerName);
            return ExecutionResult.builder()
                    .status(SubmissionStatus.RUNTIME_ERROR)
                    .errorMessage("Sandbox system error: " + e.getMessage())
                    .build();
        } finally {
            // Clean up files
            if (tempDir != null) {
                cleanupDir(tempDir);
            }
        }
    }

    private String readStream(InputStream is) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (var reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
        }
        return sb.toString().trim();
    }

    private void killContainer(String containerName) {
        try {
            new ProcessBuilder("docker", "rm", "-f", containerName).start().waitFor();
            log.info("Force removed container: {}", containerName);
        } catch (Exception e) {
            log.error("Failed to force remove container: {}", containerName, e);
        }
    }

    private void cleanupDir(Path dir) {
        try {
            try (var files = Files.walk(dir)) {
                files.sorted((a, b) -> b.compareTo(a)) // delete children first
                        .forEach(p -> {
                            try {
                                Files.delete(p);
                            } catch (IOException ignored) {
                            }
                        });
            }
        } catch (IOException e) {
            log.error("Failed to cleanup sandbox temp directory: {}", dir, e);
        }
    }
}
