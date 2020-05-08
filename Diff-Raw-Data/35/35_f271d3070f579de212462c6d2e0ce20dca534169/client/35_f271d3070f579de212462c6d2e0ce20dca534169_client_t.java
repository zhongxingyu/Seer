 
 import  java.io.*;
 import java.util.*;
 public class client{
   
    String lwd;
    String rwd;
    String username;
    String password;
    messaging[] servers;
    String[]       server_list;
    int 		  port_num;
    int 		  qur_size;
    public static void main (String[] args) {
 	client cl = new client(1,10001);
 	cl.lwd =  new File(".").getAbsolutePath();
 	//lwd = System.getProperty("user.dir");
 	cl.rwd = "/";
       	while(true){
 		//  prompt the user to enter their name
 		System.out.print("quarac> ");
 		//  open up standard input
 		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
 		String ccmd = null;
 		//  read the username from the command-line; need to use try/catch with the
 		//  readLine() method
 		try {
 		 ccmd = br.readLine();
 		} catch (IOException ioe) {
 		 System.out.println("IO error trying to read your name!");
 		 System.exit(1);
 		}
 		// System.out.println(ccmd);
 		StringTokenizer Tok = new StringTokenizer(ccmd);
 		if(!Tok.hasMoreElements()) continue;
 		String cmdstr = (String)Tok.nextElement();
 		int cmdargc =Tok.countTokens();
 		String[] cmdargs = new String[cmdargc];
 		for(int i = 0; i < cmdargc; i++) cmdargs[i] = (String)Tok.nextElement(); 
 		// System.out.println(cmdstr);
 		if(cmdstr.equals("quit") || cmdstr.equals("exit")) return;
 		// Transactions
 		else if(cmdstr.equals("login")) cl.login(cmdargs);
 		else if(cmdstr.equals("get")) cl.get(cmdargs);
 		else if(cmdstr.equals("put")) cl.put(cmdargs);
 		// Local Commands
 		else if(cmdstr.equals("help")) cl.help(cmdargs);
 		else if(cmdstr.equals("lls")) cl.lls(cmdargs);
 		else if(cmdstr.equals("lpwd")) cl.lpwd(cmdargs);
 		else if(cmdstr.equals("lcd")) cl.lcd(cmdargs);
 		// Remote Commands
 		else if(cmdstr.equals("cd")) cl.cd(cmdargs);
 		else if(cmdstr.equals("ls")) cl.ls(cmdargs);
 		else if(cmdstr.equals("pwd")) cl.pwd(cmdargs);
 		else System.out.println("Invalid Command");
 	}
    }	
    client(int qs,int pn){
 	qur_size = qs;
 	port_num = pn;
 	servers = new messaging[qur_size];
 	String ip_addr = "127.0.0.1";
 	for(int i = 0;i < qur_size; i++){
 		servers[i] = new messaging(ip_addr,port_num);
 	}
    }
 	// Commands
 	// Transcations
 	void login(String[] argv){
 		System.out.println("Username: ");
                 BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
                 try {
                  username = br.readLine();
                 } catch (IOException ioe) {
                  System.out.println("IO error trying to read your name!");
                  System.exit(1);
                 } 
 		System.out.println("Password: ");
                 try {
                  password = br.readLine();
                 } catch (IOException ioe) {
                  System.out.println("IO error trying to read your password!");
                  System.exit(1);
                 } 
 		System.out.println("login as:  " + username + " with: " + password);
                 //  read the username from the command-line; need to use try/catch with the
                 //  readLine() method
 		String username_hash = username;//+ password; // TODO: CHANGE THIS TO INCLUDE VINAY's stuff	
 		servers = new messaging[qur_size];
 		for(int i = 0; i< qur_size; i++){
 			servers[i] = new messaging("127.0.0.1",port_num);
 			if(!servers[i].clientLogin(username_hash)){
 				System.out.println("Login failed!");
 				return;
 			}
 		}
 	}
 	void get(String[] argv){
 		// File not found is -1
 		// Incorrect command string sent -2
 		String[] local_path = new String[qur_size];// = lwd + ".temp_qurac/";
 		 for(int i = 0; i< qur_size; i++){ local_path[i] = lwd + "/.temp_qurac/" + 'a'+i; }
 		 for(int i = 0; i< qur_size; i++){
                         servers[i].get(local_path[i] + "/" + argv[0], rwd + "/" + argv[1]);
 		}
 	}
 	void put(String[] argv){
 		 String local_path;
		 if(argv.length != 2) return;
                  for(int i = 0; i< qur_size; i++){
                	 int opcode = servers[i].put(lwd + "/" +  argv[0], rwd + "/" + argv[1],"tempstamp"); //TODO: Add correct timestamp names
                	 if(opcode == -1) System.out.println("Failed to communicate with the server!");
                 }
 	}
 	// Local Commands
 	void help(String[] argv){	
 		System.out.println(	 "This program allows for read and write to a remote file repository.");
 		System.out.println(	 "List of Commands: ");
 		System.out.println(	 "help: prints this help message.");
 		System.out.println(	 "login [username]@[domain]: reloads the login prompt for username and will " );
 		System.out.println(	 "                           attempt to connect to the domain");
 		System.out.println(	 "quit: Exits this prompt with code 0");
 		System.out.println(	 "Local System Commands:");
 		System.out.println(	 "lls -alF [path]: Displays the local directory path relative to");
 		System.out.println(	 "                 the current working directory.");
 		System.out.println(	 "lcd [path]: Changes the local current working directory to path relative to lpwd.");
 		System.out.println(	 "lpwd: Displays the present working directory on the local computer.");
 		System.out.println(	 "Remote System Commands:");
 		System.out.println(         "ls -alF [path]: Displays the directory path  on the remote system relative to"); 
 		System.out.println(         "                the current working directory.");
 		System.out.println(         "cd [path]: Changes the current working directory on the remote system to path.");
 		System.out.println(         "pwd: Displays the present working directory on the remote system.");
 		System.out.println(	 "File System Interaction: ");
 		System.out.println(	 "mkdir [name]: creates the directory name on the remote system.");
 		System.out.println(	 "rmdir [name]: Removes the directory name on the remote system fails if name is not empty.");
 		System.out.println(	 "rm -rf [name]: Removes the file name. If name is a directory -r will delete recursively.");
 		System.out.println(	 "put -r [name]: Puts the file name on the remote system.");
 		System.out.println(	 "               If name is a directory -r will add recursively");
 		System.out.println(	 "get -r [name]: Gets the file name from the remote system.");
 		System.out.println(	 "               If name is a directory -r will recursively get the directory structure");
 	}
 	void lls(String[] argv){
 		String path;
                 if(argv.length == 0) path = lwd;
                 else if(argv.length > 1) {System.out.println("This does not accept that many arguments"); return;}
                 else {
 			if(argv[0].charAt(0) == '/') path = argv[0];
                 	else path = lwd + "/" + argv[0];
 		}
                 File check = new File(path);
                 if(check.exists() && check.isDirectory())
 		 	list(check,false,false,true);
                 else System.out.println("Invalid path to a directory");
 
 	}
 	void lpwd(String[] argv){System.out.println(lwd);};
 	void lcd(String[] argv){
 		String path;
 		if(argv.length == 0) return;
 		if(argv.length > 1) {System.out.println("This does not accept that many arguments"); return;}
 		if(argv[0].charAt(0) == '/') path = argv[0];
 		else path = lwd + "/" + argv[0];
 		File check = new File(path);
 		if(check.exists() && check.isDirectory()) lwd = check.getAbsolutePath();
 		else System.out.println("Invalid path to a directory");	
 	}
 	// Remote Commands
 	void cd(String[] argv){
 		
 	}
 	void ls(String[] argv){
 
 	}
 	void pwd(String[] argv){System.out.println(rwd);};	
 	private static void list(File dir, boolean recur, boolean size, boolean hidden) {
 	    if ((dir != null) && dir.exists() && dir.isDirectory()) {
 		File[] filelist = dir.listFiles();
 		for (int index = 0; index < filelist.length; index++) {
 		    File file = filelist[index];
 		    // Print hidden Files
 		    if (hidden || !file.isHidden()) {
 			    String name = file.getName();
 			    // Print Size
 			    if (file.isDirectory()) System.out.println(name+'/');
 			    else System.out.println(name);
 
 		    }
 		}
 	    } else {
 		System.err.println("Error: " + dir + " is not a Directory!");
 	    }
 	}
 }
