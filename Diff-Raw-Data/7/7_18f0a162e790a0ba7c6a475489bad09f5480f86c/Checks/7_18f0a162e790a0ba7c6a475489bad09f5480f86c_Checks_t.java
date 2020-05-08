 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.io.PrintStream;
 import java.io.PrintWriter;
 import java.lang.reflect.Field;
 import java.net.DatagramSocket;
 import java.net.ServerSocket;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.Scanner;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.LinkedBlockingQueue;
 import java.util.concurrent.atomic.AtomicBoolean;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 public class Checks
 {
 	// The worker number of the instantiating GradeWorker to be used for retrieving the correct
 	// timer.
 	int										number;
 
 	// The runtime for executing commands
 	static Runtime							runtime	= Runtime.getRuntime();
 
 	// The thread executor used for printing the stdout and stderr of the run commands
 	public static ExecutorService			exec;
 
 	// The stdout and stderr for this class
 	final PrintWriter						out;
 	final PrintWriter						err;
 
 	// The header and the footer to denote System errors that occur (and may or may not be due to
 	// the student)
 	static String							header	= "==========ERROR==========\n";
 	static String							footer	= "=========================";
 
 	// A field to contain the current process to be tested that is running.
 	Process									currProc;
 	// A timer to be used to cut down on thread creation
 	public static Timer						tmArr[];
 
 	// An array used to hold the "return" for StreamPrinter
 	private boolean[]						streamFindings;
 
 	// A linked list to hold the parsed git commits
 	LinkedList<String>						commitTitles;
 	LinkedList<String>						commitBodies;
 	public static HashSet<String>			invalidCommitRegex;
 	private LinkedBlockingQueue<Integer>	messanger;
 
 	public boolean checkMakeClean(File partDir, String makeName)
 	{
 		out.println("Checking make clean:");
 		// Holding the return value. We still need to clean up the directory (so we can't return
 		// directly)
 		boolean cleanWorked = true;
 		// Checking to make sure there are files and a directory to clean up.
 		if (partDir == null || !partDir.isDirectory() || partDir.listFiles().length == 0)
 			return false;
 		// Run make clean in the current directory.
 		try {
 			Process makeClean = runtime.exec("make clean", null, partDir);
 			currProc = makeClean;
 			final BufferedReader stdout =
 				new BufferedReader(new InputStreamReader(makeClean.getInputStream()),
 					2 * (int) Math.pow(1024, 2));
 			final BufferedReader stderr =
 				new BufferedReader(new InputStreamReader(makeClean.getErrorStream()),
 					2 * (int) Math.pow(1024, 2));
 			// Print the stdout of this process
 			exec.execute(new StreamPrinter(1, stdout, 1, 0));
 			// Print the stderr of this process
 			exec.execute(new StreamPrinter(2, stderr, 1, 0));
 			// Wait for the command to finish
 			int success = makeClean.waitFor();
 			messanger.take();
 			messanger.take();
 			if (success != 0)
 				cleanWorked = false;
 		} catch (IOException e) {
 			System.err.println("Grader " + number + ": Unable to run make clean in directory: "
 				+ partDir);
 			e.printStackTrace();
 		} catch (InterruptedException e) {
 			System.err.println("Grader " + number
 				+ ": Interrupted while waiting for make clean to finish.");
 			e.printStackTrace();
 		}
 		// Checking to make sure that make clean removed everything it should
 		String[] names = makeName.split(",\\ ");
 		HashSet<String> nameSet = new HashSet<String>();
 		for (String n : names)
 			nameSet.add(n);
 		for (File f : partDir.listFiles())
 			if (f.getName().endsWith(".o") || nameSet.contains(f.getName())
 				|| f.getName().compareTo("core") == 0 || f.getName().compareTo("a.out") == 0
				|| f.getName().endsWith(".gch") || f.getName().endsWith(".a")) {
 				f.delete();
 				cleanWorked = false;
 			}
 		out.println();
 		currProc = null;
 		return cleanWorked;
 	}
 
 	/** This command tests some arbitrary command by directly calling exec on the given command
 	 * string. It takes two optional arguments. <code>inputFile</code> refers to a file to be fed
 	 * directly into the program's stdin, and <code>limit</code> refers to how long in seconds to
 	 * run the program. If <code>inputFile</code> is null, then nothing will be piped to the
 	 * program. If <code>limit</code> is 0, then the program will run indefinetly.
 	 * @param workingDir
 	 *            - The initial working directory for this program (e.g. part1, part2,...)
 	 * @param command
 	 * @param inputFile
 	 * @return <code>{memory error, leak error}</code> */
 	public boolean[] testCommand(File workingDir, String command, File inputFile, int limit)
 	{
 		// Marking the command name
 		final String commandName = command.split("\\ ")[0];
 		out.println("Command results:");
 		// Check to make sure the directory exists (i.e. they did this part)
 		if (workingDir == null)
 			return new boolean[] {true, true};
 
 		int retVal = 1;
 		try {
 			if (inputFile == null)
 				out.println("\tTesting: " + command);
 			else
 				out.println("\tTesting: " + command + " with " + inputFile.getName());
 
 			Process proc = runtime.exec("valgrind --leak-check=yes ./" + command, null, workingDir);
 			// If we have an input file, feed that to the process
 			if (inputFile != null)
 				exec.execute(new LineFeeder(proc.getOutputStream(), inputFile));
 			System.out.println("Grader " + number + ": started " + commandName);
 			currProc = proc;
 
 			final BufferedReader stdout =
 				new BufferedReader(new InputStreamReader(proc.getInputStream()),
 					2 * (int) Math.pow(1024, 2));
 			final BufferedReader stderr =
 				new BufferedReader(new InputStreamReader(proc.getErrorStream()),
 					2 * (int) Math.pow(1024, 2));
 			// Print the stdout of this process
 			exec.execute(new StreamPrinter(1, stdout, 2, 0));
 			// This stderr stream contains the output of valgrind. We can easily check for
 			// errors because:
 			// (1) "ERROR SUMMARY: 0" not being found means there were memory errors
 			// (2) "LEAK SUMMARY:" being found means there were... leaks
 			exec.execute(new StreamPrinter(2, stderr, 2, 2));
 
 			// Wake us limit number of seconds later if we were given a non-zero value for limit
 			if (limit > 0) {
 				ProcessKiller pk = new ProcessKiller(commandName);
 				tmArr[number].schedule(pk, limit * 1000);
 				System.out.println("Grader " + number + ": timed waiting on " + commandName);
 				// Now we wait
 				retVal = proc.waitFor();
 				// If we reached here, then the process may have terminated already and we should
 				// not kill it again.
 				pk.cancel();
 			} else {
 				System.out.println("Grader " + number + ": waiting on " + commandName);
 				retVal = proc.waitFor();
 			}
 			messanger.take();
 			messanger.take();
 			out.println("\tReturn code: " + retVal + "\n");
 		} catch (IOException e) {
 			System.err.println("Grader " + number + ": An error occured while trying to run: "
 				+ commandName);
 			currProc = null;
 			return new boolean[] {true, true};
 		} catch (InterruptedException e) {
 			System.err.println("Grader " + number + ": Interrupted while waiting for "
 				+ commandName + " to complete.");
 			e.printStackTrace();
 		}
 		currProc = null;
 		// return code 126 for valgrind means it cannot find the file specified
 		if (retVal == 126)
 			return new boolean[] {true, true};
 		return streamFindings;
 	}
 
 	public int jockeyCommand(File workingDir, String command, File inputFile)
 	{
 		int retVal = -1;
 		try {
 			Process proc = runtime.exec(command, null, workingDir);
 			currProc = proc;
 			// Feed the file
 			if (inputFile != null)
 				exec.execute(new LineFeeder(proc.getOutputStream(), inputFile));
 			// Get the process streams
 			new StreamGobbler(proc.getInputStream());
 			new StreamGobbler(proc.getErrorStream());
 			retVal = proc.waitFor();
 		} catch (IOException e) {
 			e.printStackTrace();
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 		currProc = null;
 		return retVal;
 	}
 
 	public int runCommand(File workingDir, String command, File inputFile, int limit)
 	{
 		String commandName = command.split("\\ ")[0];
 		out.println("====Custom command:" + commandName + "====");
 		int retVal = -1;
 		try {
 			Process proc = runtime.exec(command, null, workingDir);
 			if (inputFile != null)
 				exec.execute(new LineFeeder(proc.getOutputStream(), inputFile));
 			currProc = proc;
 			// Get the process streams
 			final BufferedReader stdout =
 				new BufferedReader(new InputStreamReader(proc.getInputStream()),
 					2 * (int) Math.pow(1024, 2));
 			final BufferedReader stderr =
 				new BufferedReader(new InputStreamReader(proc.getErrorStream()),
 					2 * (int) Math.pow(1024, 2));
 			// Print them
 			exec.execute(new StreamPrinter(1, stdout, 0, 0));
 			exec.execute(new StreamPrinter(2, stderr, 0, 0));
 			// Wake us limit number of seconds later if we were given a non-zero value for limit
 			if (limit > 0) {
 				ProcessKiller pk = new ProcessKiller(commandName);
 				tmArr[number].schedule(pk, limit * 1000);
 				System.out.println("Grader " + number + ": timed waiting on " + commandName);
 				// Now we wait
 				retVal = proc.waitFor();
 				// If we reached here, then the process may have terminated already and we should
 				// not kill it again.
 				pk.cancel();
 			} else {
 				System.out.println("Grader " + number + ": waiting on " + commandName);
 				retVal = proc.waitFor();
 			}
 			messanger.take();
 			messanger.take();
 		} catch (IOException e) {
 			System.err.println("Grader " + number + ": An error occured while trying to run: "
 				+ commandName);
 			e.printStackTrace();
 		} catch (InterruptedException e) {
 			System.err.println("Grader " + number + ": Interrupted while waiting for "
 				+ commandName + " to complete.");
 			e.printStackTrace();
 		}
 		out.println("===================" + commandName.replaceAll(".", "=") + "====");
 		currProc = null;
 		return retVal;
 	}
 
 	public boolean checkMake(File partDir, String makeName)
 	{
 		out.println("Make results:");
 		Process makeProc = null;
 		if (!partDir.isDirectory())
 			return false;
 
 		// Run the make
 		try {
 			makeProc = runtime.exec("make", null, partDir);
 			final BufferedReader stdout =
 				new BufferedReader(new InputStreamReader(makeProc.getInputStream()),
 					2 * (int) Math.pow(1024, 2));
 			final BufferedReader stderr =
 				new BufferedReader(new InputStreamReader(makeProc.getErrorStream()),
 					2 * (int) Math.pow(1024, 2));
 			// Print the stdout of this process
 			exec.execute(new StreamPrinter(1, stdout, 1, 0));
 			// If anything prints to here on make, then either gcc or make had an error, in which
 			// case, this test is a fail
 			exec.execute(new StreamPrinter(2, stderr, 1, 1));
 		} catch (IOException e) {
 			System.err.println("Grader " + number + ": Could not run \"make\" in: "
 				+ partDir.getPath());
 			e.printStackTrace();
 		}
 
 		// Wait for the process to terminate
 		int goodMake = 1;
 		if (makeProc != null) {
 			try {
 				goodMake = makeProc.waitFor();
 				messanger.take();
 				messanger.take();
 			} catch (InterruptedException e) {
 				System.err.println("Grader " + number
 					+ ": Interrupted while waiting for make to compile.");
 				e.printStackTrace();
 			}
 		}
 
 		// Finally, check that the executable(s) exists:
 		String[] names = makeName.split(",\\ ");
 		HashSet<String> nameSet = new HashSet<String>();
 		for (String n : names)
 			nameSet.add(n);
 		boolean[] foundArr = new boolean[names.length];
 		int found = 0;
 		for (File f : partDir.listFiles())
 			if (f.getName().compareTo(makeName) == 0)
 				foundArr[found++] = true;
 		out.println();
 		boolean foundExec = true;
 		for (boolean f : foundArr) {
 			if (!f) {
 				foundExec = false;
 				break;
 			}
 		}
 		// Compile the results
		if (goodMake == 0 && foundExec && !streamFindings[0])
 			return true;
 		return false;
 	}
 
 	public void shutdown()
 	{
 		out.flush();
 		err.flush();
 		out.close();
 		err.close();
 	}
 
 	public boolean checkGitCommits(File partDir)
 	{
 		out.println("Checking git commits:");
 
 		// Get the git log of the given directory //
 		// Run git log
 		Process gitLog;
 		try {
 			gitLog =
 				runtime.exec("git log --abbrev-commit --format=format:\"%s:|:%b%n...\"", null,
 					partDir);
 			// Ignore stderr
 			new StreamGobbler(gitLog.getErrorStream());
 			// Read stdout
 			exec.execute(new LogReader(gitLog.getInputStream()));
 			// Wait for the reader to finish
 			gitLog.waitFor();
 			// And the LogReader to parse
 			messanger.take();
 		} catch (IOException e) {
 			System.err.println("Grader " + number + ": Could not run git log in directory:"
 				+ partDir.getAbsolutePath());
 			e.printStackTrace();
 		} catch (InterruptedException e) {
 			System.err.println(header + "Grader " + number
 				+ ": Interrupted while waiting for git log to finish.");
 			e.printStackTrace();
 			System.err.println(footer);
 		}
 
 		// Make sure they have at least 5 commits
 		if (commitBodies.size() < 5)
 			return false;
 
 		int count = 0;
 		// Make sure the commits are valid
 		for (int i = 0; i < commitBodies.size(); i++) {
 			String t = commitTitles.get(i);
 			String b = commitBodies.get(i);
 			// Print out the title and body
 			out.println("\t" + (i + 1) + ") Title:");
 			String[] tArr = t.split("(\r)?\n");
 			for (String s : tArr)
 				out.println("\t\t   " + s);
 			out.println("\t   Body:");
 			String[] bArr = b.split("(\r)?\n");
 			for (String s : bArr)
 				out.println("\t\t   " + s);
 			// Check to make sure the commits are valid
 			boolean badTitle = false;
 			boolean badBody = false;
 			for (String regex : invalidCommitRegex) {
 				Pattern p = Pattern.compile(regex);
 				Matcher title = p.matcher(t);
 				Matcher body = p.matcher(b);
 				badTitle = title.find();
 				badBody = body.find();
 			}
 			if (badTitle && badBody)
 				continue;
 			else
 				count++;
 		}
 		// We didn't have enough good commits
 		if (count < 5)
 			return false;
 		else
 			return true;
 	}
 
 	public void printMessage(String message, int stream)
 	{
 		if (stream == 1)
 			out.println(message);
 		else if (stream == 2)
 			err.println(message);
 	}
 
 	public Checks(File target, int number) throws FileNotFoundException
 	{
 		FileOutputStream fileStream = null;
 		fileStream = new FileOutputStream(target);
 		this.number = number;
 		out = new PrintWriter(fileStream, true);
 		err = new PrintWriter(fileStream, true);
 		invalidCommitRegex = new HashSet<String>();
 		messanger = new LinkedBlockingQueue<Integer>();
 	}
 
 	public boolean[] mdbTest(File partDir, String commandName, File inputFile, String portNum)
 	{
 		out.println("Command results:");
 		// Check to make sure the directory exists (i.e. they did this part)
 		if (partDir == null)
 			return new boolean[] {true, true};
 		// Open up the input file for reading, if it exists
 		// Valgrind error booleans
 		final AtomicBoolean memErr = new AtomicBoolean(true);
 		final AtomicBoolean leakErr = new AtomicBoolean(false);
 		int success = 1;
 		try {
 			out.println("Testing " + commandName);
 			Process partProc =
 				runtime.exec("valgrind --leak-check=yes ./" + commandName, null, partDir);
 			System.out.println(Thread.currentThread() + ": started mdb-lookup-server");
 			currProc = partProc;
 			final Scanner stdout = new Scanner(partProc.getInputStream());
 			final Scanner stderr = new Scanner(partProc.getErrorStream());
 			// Print the stdout of this process
 			exec.execute(new Runnable() {
 				@Override
 				public void run()
 				{
 					while (stdout.hasNextLine()) {
 						out.println(stdout.nextLine());
 					}
 					stdout.close();
 				}
 			});
 			// This stderr stream contains the output of valgrind. We can easily check for
 			// errors because:
 			// (1) "ERROR SUMMARY: 0" not being found means there were memory errors
 			// (2) "LEAK SUMMARY:" being found means there were... leaks
 			exec.execute(new Runnable() {
 				@Override
 				public void run()
 				{
 					while (stderr.hasNextLine()) {
 						String line = stderr.nextLine();
 						// There was no memory error
 						if (line.contains("ERROR SUMMARY: 0"))
 							memErr.set(false);
 						if (line.contains("LEAK SUMMARY:"))
 							leakErr.set(true);
 						err.println(line);
 					}
 					stderr.close();
 				}
 			});
 
 			// Make a file to hold the mdb server output
 			File mdbfile = new File(partDir.getParentFile(), "mdb.out.txt");
 			if (mdbfile.exists())
 				mdbfile.delete();
 			mdbfile.createNewFile();
 
 			// For each line of input, make a new nc process
 			Scanner in = new Scanner(inputFile);
 			// Pause until the socket is no longer available
 			System.out.println(Thread.currentThread() + ": Checking for port " + portNum);
 			int count = 0;
 			while (available(Integer.parseInt(portNum)) && count < 2000) {
 				count++;
 				Thread.sleep(25);
 			}
 			// mdb-lookup-server never took the port
 			if (!(count < 2000)) {
 				killProcess();
 				System.err.println(Thread.currentThread() + ": port not bound");
 				out.println("mdb-lookup-server port was never bound!\n"
 					+ "Rerun to see memory memory errors and heap summary.");
 				in.close();
 				return new boolean[] {true, true};
 			}
 
 			// Each netcat has 15 seconds to complete, at which point we kill the mdb-lookup-server
 			// which should free up the netcat
 			// make the command file
 			File ncfile = new File(partDir.getParentFile(), "nc.sh");
 			if (ncfile.exists())
 				ncfile.delete();
 			ncfile.createNewFile();
 			PrintStream ncFileOut = new PrintStream(ncfile);
 			System.out.println(Thread.currentThread() + ": Running nc on mdb-lookup-server");
 			while (in.hasNextLine()) {
 				String line = in.nextLine();
 				// Write out the command to file
 				ncFileOut.println("echo nc for phrase \\\"" + line + "\\\": >> mdb.out.txt");
 				ncFileOut.println("echo " + line + " | nc -q 5 localhost " + portNum
 					+ " >> mdb.out.txt && echo >> mdb.out.txt");
 			}
 			ncFileOut.flush();
 			ncFileOut.close();
 
 			Process ncproc = runtime.exec("bash nc.sh", null, partDir.getParentFile());
 			// Gobble this process's streams, as bash should output the nc stuff to file
 			new StreamGobbler(ncproc.getErrorStream());
 			new StreamGobbler(ncproc.getInputStream());
 			final AtomicBoolean killed = new AtomicBoolean(false);
 			final Thread ncWait = Thread.currentThread();
 			// Close the process
 			try {
 				TimerTask tt = new TimerTask() {
 					@Override
 					public void run()
 					{
 						killProcess();
 						System.err.println(ncWait + ": killed mdb-lookup-server");
 						out.println("mdb-lookup-server took more than 5 seconds to send a response.");
 						killed.set(true);
 					}
 				};
 				tmArr[number].schedule(tt, 15 * 1000);
 				ncproc.waitFor();
 				// If we reach this point, then we don't need to interrupt
 				if (!killed.get())
 					tt.cancel();
 			} catch (InterruptedException e) {
 				in.close();
 				killProcess();
 				success = partProc.waitFor();
 				out.println("Return code: " + success + "\n");
 				e.printStackTrace();
 				return new boolean[] {true, true};
 			}
 			in.close();
 			killProcess();
 			success = partProc.waitFor();
 			out.println("Return code: " + success + "\n");
 		} catch (IOException e) {
 			System.err.println("An error occured while trying to run: " + commandName);
 			killProcess();
 			e.printStackTrace();
 			return new boolean[] {true, true};
 		} catch (InterruptedException e) {
 			System.err.println("Interrupted while waiting for process to complete.");
 			killProcess();
 			e.printStackTrace();
 			return new boolean[] {true, true};
 		}
 		// return code 126 for valgrind means it cannot find the file specified
 		currProc = null;
 		if (success == 126)
 			return new boolean[] {true, true};
 		return new boolean[] {memErr.get(), leakErr.get()};
 	}
 
 	private void killProcess()
 	{
 		try {
 			// Get the PID of the process
 			Field f = currProc.getClass().getDeclaredField("pid");
 			f.setAccessible(true);
 			// Kill the process
 			Process kill = runtime.exec("kill -s SIGKILL " + f.get(currProc));
 			kill.waitFor();
 		} catch (NoSuchFieldException e) {
 			e.printStackTrace();
 		} catch (SecurityException e) {
 			e.printStackTrace();
 		} catch (IllegalArgumentException e) {
 			e.printStackTrace();
 		} catch (IllegalAccessException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public boolean checkFileEquiv(File user, File base)
 	{
 		final AtomicBoolean equal = new AtomicBoolean(true);
 		// First, make sure the user's file exists
 		if (!user.isFile())
 			return false;
 		// Then, sanitize the user file
 		File script = sanitize(user);
 		script.delete();
 		// Then run diff, and print the result to file
 		String command = "diff '" + user.getAbsolutePath() + "' '" + base.getAbsolutePath() + "'";
 		try {
 			script.createNewFile();
 			PrintStream diffWriter = new PrintStream(script);
 			diffWriter.println(command);
 			diffWriter.flush();
 			diffWriter.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 			return false;
 		}
 		try {
 			Process diffExec = runtime.exec("bash san.sh", null, user.getParentFile());
 			final Scanner stdout = new Scanner(diffExec.getInputStream());
 			final Scanner stderr = new Scanner(diffExec.getErrorStream());
 			// Print the stdout of this process
 			out.println("diff on " + user.getName() + ":");
 			exec.execute(new Runnable() {
 				@Override
 				public void run()
 				{
 					// If anything prints here, then they aren't the same
 					while (stdout.hasNextLine()) {
 						out.println(stdout.nextLine());
 						equal.set(false);
 					}
 					stdout.close();
 				}
 			});
 			// Print the stderr of this process
 			exec.execute(new Runnable() {
 				@Override
 				public void run()
 				{
 					while (stderr.hasNextLine()) {
 						err.println(stderr.nextLine());
 						equal.set(false);
 					}
 					stderr.close();
 				}
 			});
 
 			// Wait for it to finish
 			diffExec.waitFor();
 			out.println();
 		} catch (IOException e) {
 			e.printStackTrace();
 			return false;
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 			return false;
 		}
 		script.delete();
 		return equal.get();
 	}
 
 	private File sanitize(File user)
 	{
 		// Write the bash script to sanitize the file
 		File bash = new File(user.getParent(), "san.sh");
 		if (bash.isFile())
 			bash.delete();
 		PrintStream bashWriter = null;
 		try {
 			bash.createNewFile();
 			bashWriter = new PrintStream(bash);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		// Write the script to file
 		bashWriter.println("sed '/^[[:space:]]*$/{:a;$d;N;/\\n[[:space:]]*$/ba}' " + user.getName()
 			+ " > " + user.getName() + ".2");
 		bashWriter.println("mv " + user.getName() + ".2 " + user.getName());
 		bashWriter.flush();
 		bashWriter.close();
 
 		// Execute the script
 		try {
 			Process bashExec = runtime.exec("bash san.sh", null, user.getParentFile());
 			final Scanner stdout = new Scanner(bashExec.getInputStream());
 			final Scanner stderr = new Scanner(bashExec.getErrorStream());
 			// Print the stdout of this process
 			exec.execute(new Runnable() {
 				@Override
 				public void run()
 				{
 					while (stdout.hasNextLine()) {
 						System.err.flush();
 						System.out.println(stdout.nextLine());
 					}
 					stdout.close();
 				}
 			});
 			// Print the stderr of this process
 			exec.execute(new Runnable() {
 				@Override
 				public void run()
 				{
 					while (stderr.hasNextLine()) {
 						System.out.flush();
 						System.err.println(stderr.nextLine());
 					}
 					stderr.close();
 				}
 			});
 
 			// Wait for it to finish
 			bashExec.waitFor();
 		} catch (IOException e) {
 			e.printStackTrace();
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 
 		// We're done
 		return bash;
 	}
 
 	public static boolean available(int port)
 	{
 		if (port < 1 || port > 65535) { throw new IllegalArgumentException("Invalid start port: "
 			+ port); }
 
 		ServerSocket ss = null;
 		DatagramSocket ds = null;
 		try {
 			ss = new ServerSocket(port);
 			ss.setReuseAddress(true);
 			ds = new DatagramSocket(port);
 			ds.setReuseAddress(true);
 			return true;
 		} catch (IOException e) {} finally {
 			if (ds != null) {
 				ds.close();
 			}
 
 			if (ss != null) {
 				try {
 					ss.close();
 				} catch (IOException e) {
 					/* should not be thrown */
 				}
 			}
 		}
 
 		return false;
 	}
 
 	private class LogReader implements Runnable
 	{
 		BufferedReader	logStream;
 
 		public LogReader(InputStream stream)
 		{
 			logStream =
 				new BufferedReader(new InputStreamReader(stream), 2 * (int) Math.pow(1024, 2));
 		}
 
 		@Override
 		public void run()
 		{
 			commitTitles = new LinkedList<String>();
 			commitBodies = new LinkedList<String>();
 			try {
 				String line;
 				StringBuilder title = new StringBuilder();
 				boolean breakFound = false;
 				StringBuilder body = new StringBuilder();
 				while ((line = logStream.readLine()) != null) {
 					if (line.startsWith("...") && line.matches("[[^a-z][^A-Z][^0-9]]+")) {
 						commitTitles.add(title.toString().trim());
 						commitBodies.add(body.toString().trim());
 						title.delete(0, title.length());
 						body.delete(0, body.length());
 						breakFound = false;
 					} else {
 						if (line.contains(":|:")) {
 							String[] commentStart = line.split("\\:\\|\\:");
 							title.append(commentStart[0]);
 							if (commentStart.length == 2)
 								body.append(commentStart[1]);
 							breakFound = true;
 						} else {
 							if (breakFound)
 								body.append(line);
 							else
 								title.append(line);
 						}
 					}
 				}
 			} catch (IOException e) {
 				err.println("Failed to read git log.");
 			}
 			messanger.add(0);
 		}
 	}
 
 	private class StreamPrinter implements Runnable
 	{
 		PrintWriter		writer;
 		BufferedReader	stream;
 		int				errorLevel;
 		String			tabs;
 
 		public StreamPrinter(int streamType, BufferedReader stream, int numTabs, int errorType)
 		{
 			if (streamType == 1)
 				writer = out;
 			else if (streamType == 2)
 				writer = err;
 			this.stream = stream;
 			errorLevel = errorType;
 			tabs = "";
 			for (int i = 0; i < numTabs; i++)
 				tabs += "\t";
 		}
 
 		@Override
 		public void run()
 		{
 			String line;
 			try {
 				if (errorLevel == 0) {
 					while ((line = stream.readLine()) != null) {
 						writer.println(tabs + line);
 					}
 				} else if (errorLevel == 1) {
 					boolean streamWrote = false;
 					while ((line = stream.readLine()) != null) {
 						// If this line is empty or whitespace, continue
 						if (line.trim().isEmpty())
 							continue;
 						writer.println(tabs + line);
 						if (!streamWrote)
 							streamWrote = true;
 					}
 					streamFindings = new boolean[] {streamWrote};
 				} else {
 					boolean memErr = true;
 					boolean leakErr = false;
 					while ((line = stream.readLine()) != null) {
 						// There was no memory error
 						if (line.contains("ERROR SUMMARY: 0"))
 							memErr = false;
 						// There were leaks
 						if (line.contains("LEAK SUMMARY:"))
 							leakErr = true;
 						err.println(tabs + line);
 					}
 					streamFindings = new boolean[] {memErr, leakErr};
 				}
 				stream.close();
 				messanger.add(0);
 			} catch (IOException e) {
 				System.err.println(header + "Grader " + number + ":");
 				e.printStackTrace();
 				System.err.println(footer);
 			}
 		}
 	}
 
 	private class ProcessKiller extends TimerTask
 	{
 		String	commandName;
 
 		public ProcessKiller(String name)
 		{
 			commandName = name;
 		}
 
 		@Override
 		public void run()
 		{
 			killProcess();
 			System.err.println("Grader " + number + ": killed " + commandName);
 			out.println("\nKilled " + commandName + ". Rerun to see if it behaves itself.");
 		}
 	}
 
 	private class LineFeeder implements Runnable
 	{
 		PrintWriter	stdin;
 		Scanner		reader;
 
 		public LineFeeder(OutputStream processStdin, File inputFile) throws FileNotFoundException
 		{
 			stdin = new PrintWriter(processStdin, true);
 			reader = new Scanner(inputFile);
 		}
 
 		@Override
 		public void run()
 		{
 			while (reader.hasNextLine())
 				stdin.println(reader.nextLine());
 			reader.close();
 			stdin.close();
 		}
 	}
 }
