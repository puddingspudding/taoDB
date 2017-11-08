package com.github.puddingspudding.taodb;

import org.apache.commons.lang3.tuple.Pair;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.Properties;
import java.util.stream.Stream;

/**
 * Created by pudding on 01.11.17.
 */
public class CLI {

    public static void main(String[] args) throws Exception {
        Properties config = new Properties();
        config.load(new FileReader(new File(System.getProperty("config"))));

        String[] serviceNames = config.getProperty("taodb.services").split(",");
        String[] replicationServiceNames = config.getProperty("taodb.replication.services").split(",");

        if (args.length == 0) {
            printHelp();
            return;
        }

        String action = args[0];
        String name = null;

        switch (action) {
            case "status":

                System.out.format("+----------------------------------+------------------------+\n");
                System.out.format("| \033[1m%1$-32s\033[21m | \033[1m%2$22s\033[21m |\n", "Service", "Status");
                System.out.format("+----------------------------------+------------------------+\n");
                Stream
                    .of(serviceNames)
                    .map(n -> Pair.of(n, Paths.get("/tmp/taodb-" + n + ".pid")))
                    .map(name_PidFile -> Pair.of(name_PidFile.getLeft(), getPidFromPath(name_PidFile.getRight()).orElse(0))).forEach(name_pid -> {
                        System.out.format("| %1$-32s | %2$32s |\n", name_pid.getLeft(), (name_pid.getRight() > 0 ? "\033[32mrunning\033[39m" : "\033[31mstopped\033[39m"));
                    });
                Stream
                    .of(replicationServiceNames)
                    .map(n -> Pair.of(n, Paths.get("/tmp/taodb-" + n + ".pid")))
                    .map(name_PidFile -> Pair.of(name_PidFile.getLeft(), getPidFromPath(name_PidFile.getRight()).orElse(0))).forEach(name_pid -> {
                        System.out.format("| \033[90m%1$-32s\033[21m | %2$32s |\n", name_pid.getLeft(), (name_pid.getRight() > 0 ? "\033[32mrunning\033[39m" : "\033[31mstopped\033[39m"));
                    });
                System.out.format("+----------------------------------+------------------------+\n");
                break;
            case "stop":
                name = args[1];
                if (name == null || name.isEmpty()) {
                    printHelp();
                    return;
                }
                Path tmpPidFile = Paths.get("/tmp/taodb-" + name + ".pid");
                if (tmpPidFile.toFile().exists()) {
                    getPidFromPath(tmpPidFile).ifPresent(pid ->
                        ProcessHandle.of(pid).ifPresent(processHandle -> {
                           processHandle.destroy();
                           long timeout = 1000;
                           sleepWhileAlive(processHandle, timeout);
                           System.out.println("stopped");
                        })
                    );
                }
                break;
            case "start":
                name = args[1];
                if (name == null || name.isEmpty()) {
                    printHelp();
                    return;
                }
                Optional<Process> processBuilder = createProcess(serviceNames, replicationServiceNames, args, config);
                if (!processBuilder.isPresent()) {
                    System.out.println("service not defined in config");
                    return;
                }
                Process process = processBuilder.get();
                Thread.sleep(1000);
                if (process.isAlive()) {
                    System.out.println("started");
                } else {
                    System.out.println("failed");
                }

                break;
            default:
                printHelp();
                break;
        }

    }

    private static Optional<Process> createProcess(String[] serviceNames, String[] replicationServiceNames, String[] args, Properties config) throws IOException {
        String name = args[1];
        boolean isService = Arrays.asList(serviceNames).contains(name);
        boolean isReplicationService = Arrays.asList(replicationServiceNames).contains(name);

        if (isService) {
            return Optional.of(Runtime.getRuntime().exec(
                "/usr/bin/java"
                + " -Dport=" + config.getProperty(name + ".network.port")
                + " -Dfile=" + config.getProperty(name + ".storage.path")
                + " -Dname=" + name
                + " -cp"
                + " /usr/lib/taodb/taodb.jar"
                + " com.github.puddingspudding.taodb.TaoService"
            ));
        }
        if (isReplicationService) {
            return Optional.of(Runtime.getRuntime().exec(
                "java"
                + " -Dport=" + config.getProperty(name + ".network.port")
                + " -Dfile=" + config.getProperty(name + ".storage.path")
                + " -DmasterHost=" + config.getProperty(name + ".master.host")
                + " -DmasterPort=" + config.getProperty(name + ".master.port")
                + " -Dname=" + name
                + " -cp"
                + " /usr/lib/taodb/taodb.jar"
                + " com.github.puddingspudding.taodb.TaoReplicationService"
            ));
        }
        return Optional.empty();
    }

    private static void sleepWhileAlive(ProcessHandle processHandle, long timeout) {
        while (processHandle.isAlive()) {
            try {
                System.out.print(".");
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private static void printHelp() {
        System.out.println("taodb status|start|stop");
    }

    private static OptionalLong getPidFromPath(final Path path) {
        try {
            return OptionalLong.of(Long.valueOf(new String(Files.readAllBytes(path))));
        } catch (IOException e) {
            return OptionalLong.empty();
        }
    }

}
