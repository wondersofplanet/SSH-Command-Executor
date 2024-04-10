This Java program allows you to execute multiple commands on a remote server via SSH. It provides functionality to read connection details and commands from text files, execute the commands on the server, and report the success and failure counts along with the total input commands count.

Features:
SSH Connectivity: Utilizes the JSch library to establish an SSH connection to the remote server securely.
Command Execution: Executes a list of commands sequentially on the remote server.
Error Handling: Catches and handles various exceptions such as file reading errors, connection failures, and command execution errors.
Output Reporting: Prints the output of successful commands and reports any errors encountered during command execution.
Usage:
Input Files: Provide connection details (such as username, password, server IP address, and port number) in the connection.txt file, and the list of commands to execute in the commands.txt file.

Example connection.txt:

Password:your_password
ServerIPAddress:remote_server_ip
userName:your_username
PortNo:22
 
Example commands.txt:

echo Hello
ls -l
pwd


Execution: Compile and run the SSHCommandExecutor.java file. The program will establish an SSH connection, execute the commands, and display the output along with success and failure counts.

Dependencies:
JSch Library: Used for SSH connectivity and command execution. Ensure the JSch library is included in the project dependencies.

https://mvnrepository.com/artifact/com.jcraft/jsch/0.1.55



run jar using bat command:

java -jar test.jar

in the same folder where jar is, keep your commands.txt and connection.txt.

pause

You should place the connection.txt and commands.txt files in the same directory as the Java program (SSHCommandExecutor.java). When you run the program, it will look for these files in the current directory by default. If you want to place them elsewhere, you can specify the full path to the files when calling the program.
Contributions:
Contributions and feedback are welcome! If you encounter any issues or have suggestions for improvements, please open an issue or submit a pull request.
