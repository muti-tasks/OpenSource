package org.igorgames;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Stream;

public class Main {
    public static boolean ifisexit = false;

    public static final Map<String, Process> servers = new ConcurrentHashMap<>();
    public static final Map<String, BufferedWriter> serverInputs = new ConcurrentHashMap<>();

    public static void startJar(String name, Path path, int maxmem) {
        try {
            ProcessBuilder pb = new ProcessBuilder(
                    "java", "-Xms10M", "-Xmx" + maxmem + "M", "-jar", "server.jar", "nogui"
            );
            pb.directory(path.toFile());
            Process process = pb.start();

            // Store references
            servers.put(name, process);
            serverInputs.put(name, new BufferedWriter(new OutputStreamWriter(process.getOutputStream())));

            // Start output reader threads
            startOutputReader(name, process.getInputStream(), false);
            startOutputReader(name, process.getErrorStream(), true);

            System.out.println("[" + infoname.NameCmd + "]: Started server '" + name + "'");

        } catch (IOException e) {
            System.err.println("[" + infoname.NameCmd + "]: Failed to start server '" + name + "': " + e.getMessage());
        }
    }

    private static void startOutputReader(String name, InputStream stream, boolean isError) {
        new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    synchronized (System.out) { // prevent prompt/output overlap
                        if (!ifisexit) {
                            System.out.print("\r"); // return to start of line
                            System.out.println("[" + name + "]: " + line);
                            System.out.print("> ");
                        } else {
                            System.out.println("[" + name + "]: " + line);
                        }
                    }
                }
            } catch (IOException e) {
                System.err.println("[" + infoname.NameCmd + "]: Error reading output from " + name + ": " + e.getMessage());
            }
        }, name + (isError ? "-stderr" : "-stdout")).start();
    }

    public static boolean runcommand(String name, String cmd) {
        BufferedWriter writer = serverInputs.get(name);
        if (writer == null) {
            return false;
        }

        try {
            writer.write(cmd);
            writer.newLine(); // Sends Enter
            writer.flush();   // Pushes the command into the process
            return true;
        } catch (IOException e) {
            return false;
        }
    }
    public static void exit() {
        servers.forEach((name, process) -> {
            runcommand(name,"stop");
            runcommand(name,"exit");
            runcommand(name,"bye");
            //runcommand(name,"end");
        });
        new Thread(() -> {
            final int timeoutMs = 20_000;
            final int checkIntervalMs = 1000;
            int waited = 0;

            try {
                while (waited < timeoutMs) {
                    // Check if all processes are dead
                    boolean allStopped = servers.values().stream().noneMatch(Process::isAlive);
                    if (allStopped) {
                        return;
                    }

                    Thread.sleep(checkIntervalMs);
                    waited += checkIntervalMs;
                }

                servers.forEach((name, process) -> {
                    if (process.isAlive()) {
                        System.out.println("Force-killing server: " + name);
                        process.destroyForcibly();
                    }
                });

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }
    public static boolean startserver(String name) {
        try (Stream<Path> paths = Files.list(Path.of("servers"))) {
            Optional<Path> serverFolder = paths
                    .filter(Files::isDirectory)
                    .filter(folder -> folder.getFileName().toString().equalsIgnoreCase(name))
                    .findFirst();

            if (serverFolder.isPresent()) {
                Path folder = serverFolder.get();
                Path jarFile = folder.resolve("server.jar");
                if (Files.exists(jarFile)) {
                    Runtime runtime = Runtime.getRuntime();
                    long maxMemory = runtime.maxMemory();
                    int maxMemoryMB = (int) Math.min(Integer.MAX_VALUE, maxMemory / (1024 * 1024));
                    startJar(folder.getFileName().toString(), folder, maxMemoryMB);
                    return true;
                } else {
                    System.out.println("No server.jar in: " + folder);
                    return false;
                }
            }
        } catch (IOException e) {
            System.err.println("Error listing servers: " + e.getMessage());
        }
        return false;
    }


    public static void main(String[] args) {
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory();
        int maxMemoryMB = (int) Math.min(Integer.MAX_VALUE, maxMemory / (1024 * 1024));
        new Thread(() -> console.main(), "ConsoleThread").start();
        makefolder.makefolder("", "servers");
        makefolder.makefolder("servers/", "lobby");

        System.out.println("Downloading Jar File");
        downloadfile.downloadFile(
                "servers/lobby/",
                "https://fill-data.papermc.io/v1/objects/4b011f5adb5f6c72007686a223174fce82f31aeb4b34faf4652abc840b47e640/paper-1.20.6-151.jar",
                "server.jar"
        );

        Path serversFolder = Path.of("servers");

        try (Stream<Path> paths = Files.list(serversFolder)) {
            paths.filter(Files::isDirectory)
                    .forEach(folder -> {
                        Path jarFile = folder.resolve("server.jar");
                        if (Files.exists(jarFile)) {
                            startJar(folder.getFileName().toString(), folder, maxMemoryMB);
                        } else {
                            System.out.println("No server.jar in: " + folder);
                        }
                    });
        } catch (IOException e) {
            System.err.println("Error listing servers: " + e.getMessage());
        }
    }
}
