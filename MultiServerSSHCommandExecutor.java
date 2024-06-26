package afafafaf;
import com.jcraft.jsch.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class MultiServerSSHCommandExecutor {
    private static final boolean failOutputPrintEnableFlag = true;
    private static final boolean successOutputPrintEnableFlag = true;
    private static final int CONNECTION_TIMEOUT = 2000;
    private static final String CONNECTION_FILE_NAME = "connection.txt";
    private static final String COMMANDS_FILE_NAME = "commands.txt";

        public static void main(String[] args) {
            Set<String> failedServers = new HashSet<>();
            Set<String> connectionErrorServers = new HashSet<>();
            Set<String> commandExecutionErrorServers = new HashSet<>();

            try {
                // Display header
                System.out.println("--------------------------------------------------------------------------------");
                System.out.println("|                        MultiServerSSHCommandExecutor                           |");
                System.out.println("--------------------------------------------------------------------------------");

                // Read connection details from file
                String[][] servers = readServerList(CONNECTION_FILE_NAME);

                // Read servers from commands file
                List<String> serversInCommands = readServersFromCommands(COMMANDS_FILE_NAME);

                // Validate if servers in commands file are present in connection file
                List<String> serversNotInConnection = serversInCommands.stream()
                        .filter(server -> Arrays.stream(servers).noneMatch(arr -> arr[2].equals(server)))
                        .collect(Collectors.toList());

                if (!serversNotInConnection.isEmpty()) {
                    System.out.println("Error: The following servers listed in " + COMMANDS_FILE_NAME + " are not present in " + CONNECTION_FILE_NAME + ":");
                    serversNotInConnection.forEach(System.out::println);
                    return;
                }

                // Iterate through each server
                for (String[] server : servers) {
                    if (server != null && server.length == 4 && serversInCommands.contains(server[2])) {
                        String username = server[0];
                        String password = server[1];
                        String host = server[2];
                        String port = server[3];

                        try {
                            // Display connection log
                            System.out.println("--------------------------------------------------------------------------------");
                            System.out.println("|                              Server Connection Log                             |");
                            System.out.println("--------------------------------------------------------------------------------");
                            System.out.println("Connecting to Server:  " + host + "...");
                            Thread.sleep(CONNECTION_TIMEOUT);
                            System.out.println("");
                            System.out.println("");

                            // Connect to the server
                            Session session = connectSSH(password, host, username, port);
                            System.out.println("\u001B[32m**Connection successful**\u001B[0m");
                            System.out.println("--------------------------------------------------------------------------------");
                            System.out.println();
                            System.out.println("--------------------------------------------------------------------------------");
                            System.out.println("|                            Execution of Commands Log                           |");
                            System.out.println("--------------------------------------------------------------------------------");
                            System.out.println();
                            int successCount = 0;
                            int failureCount = 0;
                            System.out.println();

                            // Read commands from file
                            List<String> commands = readCommands(COMMANDS_FILE_NAME);

                            // Filter commands for the current server
                            List<String> serverCommands = commands.stream()
                                    .filter(line -> line.startsWith(host))
                                    .collect(Collectors.toList());

                            // Execute filtered commands
                            for (String command : serverCommands) {
                                Thread.sleep(CONNECTION_TIMEOUT);
                                try {
                                    String[] parts = command.split(" ", 2);
                                    if (parts.length == 2) {
                                        boolean success = executeCommand(session, parts[1]);
                                        if (success) {
                                            successCount++;
                                        } else {
                                            failureCount++;
                                            commandExecutionErrorServers.add(host);
                                        }
                                    } else {
                                        System.out.println("Invalid command format: " + command);
                                        failureCount++;
                                        commandExecutionErrorServers.add(host);
                                    }
                                } catch (JSchException | IOException e) {
                                    System.out.println("Error executing command on server: " + host + ". Skipping to the next command.");
                                    failureCount++;
                                    commandExecutionErrorServers.add(host);
                                }
                            }

                            // Display summary log
                            System.out.println("");
                            System.out.println("--------------------------------------------------------------------------------");
                            System.out.println("|                            Summary Log                                        |");
                            System.out.println("--------------------------------------------------------------------------------");
                            System.out.println("| Total input commands count" + "(" + host + "):   " + serverCommands.size());
                            System.out.println("| Commands executed successfully" + "(" + host + "):   " + successCount);
                            System.out.println("| Commands failed" + "(" + host + "):   " + failureCount);
                            System.out.println("--------------------------------------------------------------------------------");

                            // Disconnect session
                            session.disconnect();
                        } catch (JSchException e) {
                            System.out.println("\u001B[31mError connecting to server: " + host + ". Reason: " + e.getMessage() + "\u001B[0m");
                            System.out.println("Skipping to the next server.");
                            connectionErrorServers.add(host);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            System.out.println("Thread interrupted while sleeping. Exiting.");
                            return;
                        }
                    }
                }

                // Extract unique servers from commands file
                Set<String> uniqueServersInCommands = new HashSet<>(serversInCommands);

                // Print combined summary
                System.out.println("");
                System.out.println("");
                System.out.println("");
                System.out.println("");
                System.out.println("-------all server executions completed ------");
                System.out.println("");
                System.out.println("");
                System.out.println("");
                System.out.println("--------------------------------------------------------------------------------");
                System.out.println("|                            Combined Summary Log                               |");
                System.out.println("--------------------------------------------------------------------------------");
                if (connectionErrorServers.size() == 0 && commandExecutionErrorServers.size() == 0) {
                    System.out.println("\u001B[32m| Total input servers count: " + servers.length + "\u001B[0m");
                    System.out.println("\u001B[32m| Connection issues found in: " + connectionErrorServers + " (Total: " + connectionErrorServers.size() + ")\u001B[0m");
                    System.out.println("\u001B[32m| Error executing command observed in: " + commandExecutionErrorServers + " (Total: " + commandExecutionErrorServers.size() + ")\u001B[0m");
                    // Print total unique servers present in commands.txt
                    System.out.println("| Total unique servers present in " + COMMANDS_FILE_NAME + ": " + uniqueServersInCommands.size());
                    System.out.println("| List of unique servers in " + COMMANDS_FILE_NAME + ": ");
                    
                    uniqueServersInCommands.forEach(server -> System.out.println("| - " + server));
                    System.out.println("--------------------------------------------------------------------------------");

                    System.out.println("");
                    System.out.println("\u001B[32m ------ NO ISSUES OBSERVED!! -----\u001B[0m");
                    System.out.println("\u001B[32m ------ Success!! -----\u001B[0m");
                } else {

                    System.out.println("| Total input servers count: " + servers.length);

                    // Print total unique servers present in commands.txt
                    System.out.println("| Total unique servers present in " + COMMANDS_FILE_NAME + ": " + uniqueServersInCommands.size());
                    System.out.println("| List of unique servers in " + COMMANDS_FILE_NAME + ": ");
                    uniqueServersInCommands.forEach(server -> System.out.println("| - " + server));
                    System.out.println("--------------------------------------------------------------------------------");

                    // Check and print connection issues
                    if (connectionErrorServers.size() > 0) {
                        System.out.println("\u001B[31m" + "| Connection issues found in: " + connectionErrorServers + " (Total: " + connectionErrorServers.size() + ")" + "\u001B[0m");
                    } else {
                        System.out.println("| Connection issues found in: " + connectionErrorServers + " (Total: " + connectionErrorServers.size() + ")");
                    }

                    // Check and print command execution errors
                    if (commandExecutionErrorServers.size() > 0) {
                        System.out.println("\u001B[31m" + "| Error executing command observed in: " + commandExecutionErrorServers + " (Total: " + commandExecutionErrorServers.size() + ")" + "\u001B[0m");
                    } else {
                        System.out.println("| Error executing command observed in: " + commandExecutionErrorServers + " (Total: " + commandExecutionErrorServers.size() + ")");
                    }
                    System.out.println("");
                    System.out.println("\u001B[31m ------ FEW ISSUES OBSERVED!! PLS CHECK-----\u001B[0m");

                }

                System.out.println("--------------------------------------------------------------------------------");
            } catch (IOException e) {
                System.out.println("Error reading file: " + e.getMessage());
            } catch (ArrayIndexOutOfBoundsException e) {
                System.out.println("Invalid format in file. Expected format: key:value");
            } catch (NumberFormatException e) {
                System.out.println("Invalid port number in connection details.");
            }
        }
    // Read the list of servers from a file
    public static String[][] readServerList(String fileName) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            // Skipping the first line as it contains headers
            br.readLine();

            // Counting the lines in the file to determine the number of servers
            long count = br.lines().count();
            String[][] servers = new String[(int) count][4];

            // Resetting the reader to read the file from the beginning
            br.close();
            BufferedReader br2 = new BufferedReader(new FileReader(fileName));
            br2.readLine(); // Skip the first line again

            // Reading server details into the array
            String line;
            int index = 0;
            while ((line = br2.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 4) {
                    servers[index] = parts;
                    index++;
                } else {
                    throw new IOException("Invalid format in file: " + fileName);
                }
            }
            return servers;
        }
    }


    // Establish SSH connection to the server
    public static Session connectSSH(String password, String host, String username, String port) throws JSchException {
        JSch jsch = new JSch();
        Session session = jsch.getSession(username, host, Integer.parseInt(port));
        session.setPassword(password);
        session.setConfig("StrictHostKeyChecking", "no");
        session.connect();
        return session;
    }

    // Execute a command on the SSH session
    public static boolean executeCommand(Session session, String command) throws JSchException, IOException {
        try {
            ChannelExec channelExec = (ChannelExec) session.openChannel("exec");
            channelExec.setCommand(command);
            channelExec.connect();

            try (InputStream in = channelExec.getInputStream();
                 InputStream err = channelExec.getErrStream();
                 BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                 BufferedReader errReader = new BufferedReader(new InputStreamReader(err))) {
                // Read and print command output
                List<String> outputLines = reader.lines().collect(Collectors.toList());

                // Read and print error messages
                List<String> errorLines = errReader.lines().collect(Collectors.toList());
                if (!errorLines.isEmpty()) {
                    System.out.println("Error executing command ---> '" + command + "':\n");
                    if (failOutputPrintEnableFlag) {
                        errorLines.forEach(line -> System.out.println(line));
                        System.out.println("\n");
                    }
                    return false;
                } else {
                    if (successOutputPrintEnableFlag) {
                        // Print the whole output at once
                        System.out.println("Success executing command --->'" + command + "'");
                        outputLines.forEach(line -> System.out.println(line));
                        System.out.println("\n");
                    }
                    return true;
                }
            }
        } finally {
            // Do not disconnect the session here, as we're keeping the session open for multiple commands
        }
    }
    // Read commands from a file
    public static List<String> readCommands(String fileName) throws IOException {
        List<String> commands = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = br.readLine()) != null) {
                commands.add(line);
            }
        }
        return commands;
    }

    // Read servers from commands file
    public static List<String> readServersFromCommands(String fileName) throws IOException {
        List<String> servers = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(" ", 2);
                if (parts.length == 2) {
                    servers.add(parts[0]);
                } else {
                    System.out.println("Invalid command format: " + line);
                }
            }
        }
        return servers;
    }
}
