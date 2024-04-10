package afafafaf;
import com.jcraft.jsch.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class SSHCommandExecutor {
	 public static void main(String[] args) throws InterruptedException {
		 Set<String> failedServers = new HashSet<>();

	        try {
	            // Read connection details and commands from files
	            String[][] servers = readServerList("connection.txt");
	            String[] commands = readCommands("commands.txt");

	            // Iterate through each server
	            for (String[] server : servers) {
	                if (server != null && server.length == 4) {
	                    String username = server[0];
	                    String password = server[1];
	                    String host = server[2];
	                    String port = server[3];

	                    // Display connection log
	                    System.out.println("--------------------------------------------------------------------------------");
	                    System.out.println("|                              Server Connection Log                             |");
	                    System.out.println("--------------------------------------------------------------------------------");
	                    System.out.println("Connecting to Server:  " + host + "...");
	                    Thread.sleep(2000);
	                    System.out.println("");
	                    System.out.println("");

	                    // Connect to the server
	                    Session session = connectSSH(password, host, username, port);
	                    System.out.println("**Connection successful**");
	                    System.out.println("--------------------------------------------------------------------------------");
	                    System.out.println();
	                    System.out.println("--------------------------------------------------------------------------------");
	                    System.out.println("|                            Execution of Commands Log                           |");
	                    System.out.println("--------------------------------------------------------------------------------");
	                    System.out.println();
	                    int successCount = 0;
	                    int failureCount = 0;
	                    System.out.println();

	                    // Execute commands on the server
	                    for (String command : commands) {
	                        Thread.sleep(2000);
	                        boolean success = executeCommand(session, command);
	                        if (success) {
	                            successCount++;
	                        } else {
	                            failureCount++;
	                            failedServers.add(host);
	                        }
	                    }

	                    System.out.println("");
	                    // Summary Log
	                    System.out.println("--------------------------------------------------------------------------------");
	                    System.out.println("|                            Summary Log                                        |");
	                    System.out.println("--------------------------------------------------------------------------------");
	                    System.out.println("| Total input commands count" + "(" + host + "):   " + commands.length);
	                    System.out.println("| Commands executed successfully" + "(" + host + "):   " + successCount);
	                    System.out.println("| Commands failed" + "(" + host + "):   " + failureCount);
	                    System.out.println("--------------------------------------------------------------------------------");

	                    // Disconnect the session after executing all commands
	                    session.disconnect();
	                }
	            }

	            // Print combined summary
	            System.out.println("");
                System.out.println("-------all server executions completed ---");
                System.out.println("");
                System.out.println("");
	            System.out.println("--------------------------------------------------------------------------------");
	            System.out.println("|                            Combined Summary Log                               |");
	            System.out.println("--------------------------------------------------------------------------------");
	            System.out.println("| Total input servers count: " + servers.length);
	            String output = failedServers.stream().collect(Collectors.joining(", ", "[", "]"));
	  
	            System.out.println("| Error executing command observed in: " + output);
	            System.out.println("--------------------------------------------------------------------------------");

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

    // Read commands from a file
    public static String[] readCommands(String fileName) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            return br.lines().toArray(String[]::new);
        }
    }
}

