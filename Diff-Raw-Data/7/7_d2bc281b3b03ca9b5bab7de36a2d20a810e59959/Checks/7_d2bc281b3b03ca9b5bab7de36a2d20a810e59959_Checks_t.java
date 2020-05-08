 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.PrintStream;
 import java.io.PrintWriter;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.Scanner;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.atomic.AtomicBoolean;
 
 public class Checks
 {
 	// Making a HashSet<String> to hold all bad commits:
 	static HashSet<String>	badCommits	= new HashSet<String>();
 	static {
 		badCommits.add("first");
 		badCommits.add("second");
 		badCommits.add("third");
 		badCommits.add("fourth");
 		badCommits.add("fifth");
 		badCommits.add("sixth");
 		badCommits.add("seventh");
 		badCommits.add("eighth");
 		badCommits.add("ninth");
 		badCommits.add("tenth");
 		badCommits.add("eleventh");
 	}
 	
 	// The runtime for executing commands
 	static Runtime	       runtime	   = Runtime.getRuntime();
 	
 	// The thread executor used for printing the stdout and stderr of the run commands
 	ExecutorService	       exec	       = Executors.newFixedThreadPool(2);
 	
 	// The stdout and stderr for this class
 	final PrintStream	   out;
 	final PrintStream	   err;
 	
 	public boolean checkMakeClean(File partDir, String makeName)
 	{
 		out.println("Checking make clean:");
 		// Holding the return value. We still need to clean up the directory
 		boolean cleanWorked = true;
 		// Checking to make sure they did part 1.
 		if (partDir == null || partDir.listFiles() == null)
 			return false;
 		final AtomicBoolean makeErr = new AtomicBoolean(false);
 		// Run make clean in the current directory.
 		try {
 			Process makeClean = runtime.exec("make clean", null, partDir);
 			final Scanner stdout = new Scanner(makeClean.getInputStream());
 			final Scanner stderr = new Scanner(makeClean.getErrorStream());
 			// Print the stdout of this process
 			exec.execute(new Runnable() {
 				@Override
 				public void run()
 				{
 					while (stdout.hasNextLine()) {
 						err.flush();
 						out.println(stdout.nextLine());
 					}
 					stdout.close();
 				}
 			});
 			// If there's anything here, then an error occurred and it did not clean correctly
 			exec.execute(new Runnable() {
 				@Override
 				public void run()
 				{
 					while (stderr.hasNextLine()) {
 						makeErr.set(true);
 						out.flush();
 						err.println(stderr.nextLine());
 					}
 					stderr.close();
 				}
 			});
 			// Wait for the command to finish
 			int success = makeClean.waitFor();
 			if (success != 0)
 				cleanWorked = false;
 		}
 		catch (IOException e) {
 			System.err.println("Unable to run make clean in directory: " + partDir);
 			e.printStackTrace();
 		}
 		catch (InterruptedException e) {
 			System.err.println("Interrupted while waiting for make clean to finish.");
 			e.printStackTrace();
 		}
 		// Checking to make sure that make clean removed everything it should
 		for (File f : partDir.listFiles())
 			if (f.getName().endsWith(".o") || f.getName().compareTo(makeName) == 0
 			        || f.getName().compareTo("core") == 0 || f.getName().compareTo("a.out") == 0
 			        || f.getName().endsWith(".gch")) {
 				f.delete();
 				cleanWorked = false;
 			}
 		err.flush();
 		out.println();
 		if (makeErr.get())
 			return false;
 		return cleanWorked;
 	}
 	
 	public boolean[] bufferCommand(File partDir, String commandName, File inputFile)
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
 			out.println("Testing " + commandName + " with input file: " + inputFile);
 			Process partProc =
 			        runtime.exec("valgrind --leak-check=yes ./" + commandName, null, partDir);
 			final Scanner stdout = new Scanner(partProc.getInputStream());
 			final Scanner stderr = new Scanner(partProc.getErrorStream());
 			// Print the stdout of this process
 			exec.execute(new Runnable() {
 				@Override
 				public void run()
 				{
 					while (stdout.hasNextLine()) {
 						err.flush();
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
 						out.flush();
 						err.println(line);
 					}
 					stderr.close();
 				}
 			});
 			// Pipe the input line to the process
 			Scanner in = new Scanner(inputFile);
 			PrintWriter stdin = new PrintWriter(partProc.getOutputStream());
 			while (in.hasNextLine()) {
 				String line = in.nextLine();
 				stdin.println(line);
 				stdin.flush();
 			}
 			stdin.close();
 			success = partProc.waitFor();
 			out.println("Return code: " + success + "\n");
 			in.close();
 		}
 		catch (FileNotFoundException e) {
 			System.err.println(inputFile.getPath() + " does not exist.");
 			e.printStackTrace();
 			return new boolean[] {true, true};
 		}
 		catch (IOException e) {
 			System.err.println("Could not run the specified command.");
 			return new boolean[] {true, true};
 		}
 		catch (InterruptedException e) {
 			System.err.println("Interrupted while waiting for process to termingate.");
 			e.printStackTrace();
 		}
 		// return code 126 for valgrind means it cannot find the file specified
 		if (success == 126)
 			return new boolean[] {true, true};
 		return new boolean[] {memErr.get(), leakErr.get()};
 	}
 	
 	/**
 	 * @param partDir
 	 * @param commandName
 	 * @param inputFile
 	 * @return <code>{memory error, leak error}</code>
 	 */
 	public boolean[] testCommand(File partDir, String commandName, File inputFile)
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
 		if (inputFile != null) {
 			try {
 				Scanner in = new Scanner(inputFile);
 				while (in.hasNextLine()) {
 					String line = in.nextLine();
 					out.println("Testing " + commandName + " with input: " + line);
 					Process partProc =
 					        runtime.exec("valgrind --leak-check=yes ./" + commandName, null,
 					                partDir);
 					final Scanner stdout = new Scanner(partProc.getInputStream());
 					final Scanner stderr = new Scanner(partProc.getErrorStream());
 					// Print the stdout of this process
 					exec.execute(new Runnable() {
 						@Override
 						public void run()
 						{
 							while (stdout.hasNextLine()) {
 								err.flush();
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
 								out.flush();
 								err.println(line);
 							}
 							stderr.close();
 						}
 					});
 					// Pipe the input line to the process
 					PrintWriter stdin = new PrintWriter(partProc.getOutputStream());
 					stdin.println(line);
 					stdin.flush();
 					stdin.close();
 					success = partProc.waitFor();
 					out.println("Return code: " + success + "\n");
 				}
 				in.close();
 			}
 			catch (FileNotFoundException e) {
 				System.err.println(inputFile.getPath() + " does not exist.");
 				e.printStackTrace();
 				return new boolean[] {true, true};
 			}
 			catch (IOException e) {
 				System.err.println("Could not run the specified command.");
 				return new boolean[] {true, true};
 			}
 			catch (InterruptedException e) {
 				System.err.println("Interrupted while waiting for process to termingate.");
 				e.printStackTrace();
 			}
 		}
 		// Otherwise, simply run the specified command
 		else {
 			try {
 				out.println("Testing " + commandName);
 				Process partProc =
 				        runtime.exec("valgrind --leak-check=yes ./" + commandName, null, partDir);
 				final Scanner stdout = new Scanner(partProc.getInputStream());
 				final Scanner stderr = new Scanner(partProc.getErrorStream());
 				// Print the stdout of this process
 				exec.execute(new Runnable() {
 					@Override
 					public void run()
 					{
 						while (stdout.hasNextLine()) {
 							err.flush();
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
 							out.flush();
 							err.println(line);
 						}
 						stderr.close();
 					}
 				});
 				// Check the return of the valgrind process
 				success = partProc.waitFor();
 				out.println("Return code: " + success + "\n");
 			}
 			catch (IOException e) {
 				System.err.println("An error occured while trying to run: " + commandName);
 				return new boolean[] {true, true};
 			}
 			catch (InterruptedException e) {
 				System.err.println("Interrupted while waiting for process to complete.");
 				e.printStackTrace();
 			}
 		}
 		// return code 126 for valgrind means it cannot find the file specified
 		if (success == 126)
 			return new boolean[] {true, true};
 		return new boolean[] {memErr.get(), leakErr.get()};
 	}
 	
 	public boolean checkMake(File partDir, String makeName)
 	{
 		out.println("Make results:");
 		Process partProc = null;
 		if (!partDir.isDirectory())
 			return false;
 		final AtomicBoolean makeErr = new AtomicBoolean(false);
 		try {
 			partProc = runtime.exec("make", null, partDir);
 			final Scanner stdout = new Scanner(partProc.getInputStream());
 			final Scanner stderr = new Scanner(partProc.getErrorStream());
 			// Print the stdout of this process
 			exec.execute(new Runnable() {
 				@Override
 				public void run()
 				{
 					while (stdout.hasNextLine()) {
 						err.flush();
 						out.println(stdout.nextLine());
 					}
 					stdout.close();
 				}
 			});
 			// If anything prints to here on make, then either gcc or make had an error, in which
 			// case, this test is a fail
 			exec.execute(new Runnable() {
 				@Override
 				public void run()
 				{
 					while (stderr.hasNextLine()) {
						// First, check to make sure the line isn't all whitespace or empty
						String line = stderr.nextLine();
						if (line.trim().isEmpty())
							continue;
 						makeErr.set(true);
 						out.flush();
						err.println(line);
 					}
 					stderr.close();
 				}
 			});
 		}
 		catch (IOException e) {
 			System.err.println("Could not run \"make\" in: " + partDir.getPath());
 			e.printStackTrace();
 		}
 		int goodMake = 1;
 		if (partProc != null)
 			try {
 				goodMake = partProc.waitFor();
 			}
 			catch (InterruptedException e) {
 				System.err.println("Interrupted while waiting for make to compile.");
 				e.printStackTrace();
 			}
 		// Finally, check that the executable exists:
 		boolean foundExec = false;
 		for (File f : partDir.listFiles()) {
 			if (f.getName().compareTo(makeName) == 0) {
 				foundExec = true;
 				break;
 			}
 		}
 		out.println();
 		if (goodMake == 0 && foundExec && !makeErr.get())
 			return true;
 		return false;
 	}
 	
 	public void shutdown()
 	{
 		out.flush();
 		err.flush();
 		exec.shutdownNow();
 		out.close(); // The underlying stream is closed, so err is closed by our concern.
 	}
 	
 	public boolean checkGitCommits(File gitNotes)
 	{
 		out.println("Checking git commits:");
 		int goodCommits = 0;
 		LinkedList<String> commitList = new LinkedList<String>();
 		try {
 			Scanner in = new Scanner(gitNotes);
 			while (in.hasNextLine()) {
 				String line = in.nextLine();
 				commitList.add(line);
 				// We're rejecting any commit of the form: "ORDINAL COMMIT" (case insensitive)
 				boolean commonCommit = false;
 				for (String s : badCommits) {
 					if (line.toLowerCase().contains(s + " commit")) {
 						commonCommit = true;
 						break;
 					}
 				}
 				if (commonCommit)
 					continue;
 				// We're rejecting any commit with only one word
 				if (line.split("\\ ").length == 2)
 					continue;
 				goodCommits++;
 			}
 			in.close();
 		}
 		catch (FileNotFoundException e) {
 			System.err.println("Failed to read in GIT_PATCH.txt!");
 			e.printStackTrace();
 		}
 		if (goodCommits >= 5) {
 			for (String s : commitList)
 				out.println(s);
 			out.println();
 			return true;
 		}
 		for (String s : commitList)
 			err.println(s);
 		err.println();
 		return false;
 	}
 	
 	public void printMessage(String message, int stream)
 	{
 		if (stream == 0)
 			out.println(message);
 		else if (stream == 1)
 			err.print(message);
 	}
 	
 	public Checks(File target)
 	{
 		FileOutputStream fileStream = null;
 		try {
 			fileStream = new FileOutputStream(target);
 		}
 		catch (FileNotFoundException e) {
 			e.printStackTrace();
 		}
 		out = new PrintStream(fileStream);
 		err = new PrintStream(fileStream);
 	}
 }
