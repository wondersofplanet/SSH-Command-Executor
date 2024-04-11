
Please download this README file and then read it, this ensure its contents are accurately understood by the user, as GitHub rendering may sometimes cause formatting issues that could mislead the reader.


MultiServerSSHCommandExecutor


Overview:
This Java program allows you to execute commands on multiple remote servers via SSH. It reads server connection details and commands from text files, establishes SSH connections to each server, one by one , executes the provided commands, and provides a summary of the execution results.

Features:
Multiple Server Support: You can provide a list of servers along with their connection details (username, password, host, and port) in a text file.
Command Execution: Specify commands to be executed on each server in a separate text file.
Error Handling: The program handles connection errors and command execution errors gracefully, providing detailed logs for troubleshooting.
Summary Log: It generates a summary log at the end, indicating the total number of servers processed, any connection issues encountered, and any errors observed during command execution.
How to Use:
Prepare Input Files:

Create a text file (connection.txt) containing server connection details in the format: username,password,host,port.
Create another text file (commands.txt) containing the commands to be executed on each server, with each command on a new line.
Run the Program:

Compile and run the SSHCommandExecutor.java file.
The program will read the input files, establish SSH connections, execute commands, and provide a summary of the execution results.
Review Summary Log:

At the end of execution, review the combined summary log to identify any connection issues or command execution errors.
Note:
Ensure that you have SSH access to the remote servers and that the provided credentials are correct.
Make sure that the input files (connection.txt and commands.txt) are correctly formatted to avoid errors during execution.

Example connection.txt:

userName,Password,ServerIPAddress,PortNo
batman,batman,111.11.11.111,22
batman2,batman3,111.11.11.111,22
 
Example commands.txt:

echo Hello
ls -l
pwd


Execution: Compile and run the MultiServerSSHCommandExecutor.java file. The program will establish an SSH connection, execute the commands, and display the output along with success and failure counts.

Dependencies:
JSch Library: Used for SSH connectivity and command execution. Ensure the JSch library is included in the project dependencies.

https://mvnrepository.com/artifact/com.jcraft/jsch/0.1.55



run jar using bat command:

in the same folder where jar is, keep your commands.txt and connection.txt.
----------------------
java -jar test.jar

pause
---------------------

You should place the connection.txt and commands.txt files in the same directory as the Java program (SSHCommandExecutor.java). When you run the program, it will look for these files in the current directory by default. If you want to place them elsewhere, you can specify the full path to the files when calling the program.
Contributions:
Contributions and feedback are welcome! If you encounter any issues or have suggestions for improvements, please open an issue or submit a pull request.
