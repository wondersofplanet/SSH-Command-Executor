//package afafafaf; // This line indicates the package name

// Importing necessary libraries for SSH communication and file input/output
import com.jcraft.jsch.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

// Class definition
public class MultiServerSSHCommandExecutor {
    
    // Main method, the entry point of the program
    public static void main(String[] args) {
        // Sets to keep track of failed connections and command execution errors
        Set<String> connectionErrorServers = new HashSet<>();
        Set<String> commandExecutionErrorServers = new HashSet<>();

        try {
            // Read server connection details and commands from files
            String[][] servers = readServerList("connection.txt");
            String[] commands = readCommands("commands.txt");

            // Iterate through each server
            for (String[] server : servers) {
                if (server != null && server.length == 4) { // Checking if server details are valid
                    String username = server[0];
                    String password = server[1];
                    String host = server[2];
                    String port = server[3];

                    try {
                        // Connect to the server
                        Session session = connectSSH(password, host, username, port);

                        // Execute commands on the server
                        for (String command : commands) {
                            boolean success = executeCommand(session, command);
                            if (!success) {
                                commandExecutionErrorServers.add(host);
                            }
                        }

                        // Disconnect the session after executing all commands
                        session.disconnect();
                    } catch (JSchException e) {
                        // Handle connection errors
                        System.out.println("Error connecting to server: " + host);
                        connectionErrorServers.add(host);
                    }
                }
            }

            // Print summary of all executions
            if (connectionErrorServers.isEmpty() && commandExecutionErrorServers.isEmpty()) {
                System.out.println("No issues observed!");
            } else {
                System.out.println("Some issues observed. Please check!");
            }
        } catch (IOException e) {
            System.out.println("Error reading file: " + e.getMessage());
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("Invalid format in file.");
        } catch (NumberFormatException e) {
            System.out.println("Invalid port number.");
        }
    }

    // Method to read server details from a file
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


    // Method to establish SSH connection to a server
    public static Session connectSSH(String password, String host, String username, String port) throws JSchException {
        JSch jsch = new JSch();
        Session session = jsch.getSession(username, host, Integer.parseInt(port));
        session.setPassword(password);
        session.setConfig("StrictHostKeyChecking", "no");
        session.connect();
        return session;
    }

    // Method to execute a command on the SSH session
    public static boolean executeCommand(Session session, String command) throws JSchException, IOException {
        try {
            ChannelExec channelExec = (ChannelExec) session.openChannel("exec");
            channelExec.setCommand(command);
            channelExec.connect();

            try (InputStream in = channelExec.getInputStream();
                 InputStream err = channelExec.getErrStream();
                 BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                 BufferedReader errReader = new BufferedReader(new InputStreamReader(err))) {
                // Check if the command execution produced any error
                boolean isError = errReader.lines().anyMatch(line -> line != null && !line.isEmpty());
                if (isError) {
                    System.out.println("Error executing command: " + command);
                    return false;
                } else {
                    System.out.println("Command executed successfully: " + command);
                    return true;
                }
            }
        } finally {
            // Do not disconnect the session here, as we're keeping the session open for multiple commands
        }
    }

    // Method to read commands from a file
    public static String[] readCommands(String fileName) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            return br.lines().toArray(String[]::new);
        }
    }
}
