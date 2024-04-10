package afafafaf;

import com.jcraft.jsch.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class SSHCommandExecutor {

    public static void main(String[] args) throws InterruptedException {
        try {
            String[] connectionDetails = readConnectionDetails("connection.txt");
            String[] commands = readCommands("commands.txt");

            if (connectionDetails != null && commands != null) {
                System.out.println("Connecting to " + connectionDetails[1] + "...");
                Thread.sleep(2000);
                System.out.println("");
                // Connect once outside the loop
                Session session = connectSSH(connectionDetails[0], connectionDetails[1], connectionDetails[2], connectionDetails[3]);
                System.out.println("**Connection successful**");
                System.out.println("");
                System.out.println("Start execution of commands");
                System.out.println("==========================");
                int successCount = 0;
                int failureCount = 0;
                System.out.println();

                // Send commands in the same session
                for (String command : commands) {
                    Thread.sleep(2000);
                    boolean success = executeCommand(session, command);
                    if (success) {
                        successCount++;
                    } else {
                        failureCount++;
                    }
                }
                
                System.out.println("");
                System.out.println("===========================");
                System.out.println("Total input commands count: " + commands.length);
                System.out.println("Commands executed successfully: " + successCount);
                System.out.println("Commands failed: " + failureCount);

                // Disconnect the session after executing all commands
                session.disconnect();
            } else {
                System.out.println("Failed to read connection details or commands. Please check the input files.");
            }
        } catch (IOException e) {
            System.out.println("Error reading file: " + e.getMessage());
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("Invalid format in file. Expected format: key:value");
        } catch (NumberFormatException e) {
            System.out.println("Invalid port number in connection details.");
        } catch (JSchException e) {
            System.out.println("Error executing command on the server: " + e.getMessage());
        }
    }

    public static Session connectSSH(String password, String host, String username, String port) throws JSchException {
        JSch jsch = new JSch();
        Session session = jsch.getSession(username, host, Integer.parseInt(port));
        session.setPassword(password);
        session.setConfig("StrictHostKeyChecking", "no");
        session.connect();
        return session;
    }

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
                    System.out.println("###### Error executing command ###### -->'" + command + "'");
                    return false;
                } else {
                    System.out.println("Success executing command ---->'" + command + "'");
                    return true;
                }
            }
        } finally {
            // Do not disconnect the session here, as we're keeping the session open for multiple commands
        }
    }

    public static String[] readConnectionDetails(String fileName) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String[] connectionDetails = new String[4];
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(":");
                if (parts.length == 2) {
                    switch (parts[0].trim()) {
                        case "Password":
                            connectionDetails[0] = parts[1].trim();
                            break;
                        case "ServerIPAddress":
                            connectionDetails[1] = parts[1].trim();
                            break;
                        case "userName":
                            connectionDetails[2] = parts[1].trim();
                            break;
                        case "PortNo":
                            connectionDetails[3] = parts[1].trim();
                            break;
                        default:
                            throw new IOException("Invalid connection parameter: " + parts[0]);
                    }
                } else {
                    throw new IOException("Invalid format in file: " + fileName);
                }
            }
            return connectionDetails;
        }
    }

    public static String[] readCommands(String fileName) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            return br.lines().toArray(String[]::new);
        }
    }
}


