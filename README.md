MultiServerSSHCommandExecutor

Introduction
MultiServerSSHCommandExecutor is a Java program designed to execute commands on multiple servers via SSH concurrently. It reads server connection details and commands to execute from separate text files and performs the specified operations on each server.

Features
SSH Connection: Establishes SSH connections to multiple servers using username, password, host, and port details provided in a connection file.
Command Execution: Executes commands on each server concurrently, reading the commands from a separate file.
Error Handling: Handles connection errors, command execution errors, and invalid file formats gracefully, providing detailed logs for debugging.
Summary Log: Generates a summary log at the end of execution, indicating the total number of servers, connection issues, and command execution errors.

Usage
Prerequisites: Ensure that Java Development Kit (JDK) is installed on your system.
Setup Connection File: Create a text file (connection.txt) containing the connection details for each server in the following format:
username1,password1,host1,port1
username2,password2,host2,port2
...
Setup Commands File: Create another text file (commands.txt) containing the commands to execute on each server in the following format:
host1 command1
host2 command2
...
Run the Program: Compile and run the MultiServerSSHCommandExecutor class. Ensure that all required Java files are in the same directory.
View Output: The program will display detailed logs for each server's connection, command execution, and a combined summary log at the end.
Example
Suppose you have the following connection.txt file:

user1,password1,server1.example.com,22
user2,password2,server2.example.com,22
And the following commands.txt file:

bash
server1.example.com ls -l
server2.example.com ps aux
Running the program will connect to server1.example.com and execute ls -l, then connect to server2.example.com and execute ps aux. Finally, it will display a summary log of the execution.

Dependencies
This program uses the following external library:

JSch: A pure Java implementation of SSH2.

useful command - echo "Hello, the memory size of $(hostname -I) is $(awk '/MemTotal/ {print $2}' /proc/meminfo)"
