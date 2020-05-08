 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.PrintStream;
 import java.util.concurrent.ConcurrentLinkedQueue;
 
 public class GraderMT
 {
 	// The input file for isort
	/**
	 * This input file contains the array sizes I use for testing isort. Change it to whatever you
	 * named yours, or add more
	 */
 	File	         isin	   = new File("src/isort.in");
 	static GitFilter	filter	= new GitFilter();
 	
	/** This constructor isn't all that interesting */
 	public GraderMT(String root, int threads)
 	{
 		// We'll be single threaded to populate our list with all folders to check.
 		File rootDir = new File(root);
 		ConcurrentLinkedQueue<File> uniDirs = new ConcurrentLinkedQueue<File>();
 		for (File f : rootDir.listFiles())
 			if (f.isDirectory() && !f.getName().startsWith("."))
 				uniDirs.add(f);
 		// Then, spawn the requested number of threads
 		Thread[] workers = new Thread[threads];
 		for (int i = 0; i < threads; i++) {
 			workers[i] = new Thread(new GraderWorker(uniDirs));
 			workers[i].start();
 		}
 		// Wait for them to all finish
 		for (Thread worker : workers)
 			try {
 				worker.join();
 			}
 			catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 		// That's it
 	}
 	
 	class GraderWorker implements Runnable
 	{
 		// The output and error stream for messages produced by this class.
 		PrintStream		            out;
 		PrintStream		            err;
 		
 		// The folder queue
 		ConcurrentLinkedQueue<File>	uniDirs;
 		
 		public GraderWorker(ConcurrentLinkedQueue<File> queue)
 		{
 			uniDirs = queue;
 		}
 		
 		@Override
 		public void run()
 		{
 			File f = null;
 			while ((f = uniDirs.poll()) != null) {
 				// The Checker Object for this student. Each student gets their own
 				Checks check = null;
 				// If grader_mod was run to produce the directories, then f.getName() returns the
 				// UNI/username of the student we are currently checking
 				System.out.println(Thread.currentThread() + ": Verifying " + f.getName() + "...");
 				
 				// Redirect System.err and System.out to the results file
 				File results = new File(f, "GRADE_RESULTS.txt");
 				try {
 					if (results.isFile())
 						results.delete();
 					results.createNewFile();
 					check = new Checks(results);
 				}
 				catch (IOException e) {
 					System.err.println("Unable to redirect output to file");
 					e.printStackTrace();
 				}
 				
 				// This class uses its own streams for reading and writing.
 				File summary = new File(f, "SUMMARY.txt");
 				try {
 					if (summary.isFile())
 						summary.delete();
 					summary.createNewFile();
 					FileOutputStream summaryStream = new FileOutputStream(summary);
 					out = new PrintStream(summaryStream);
 					err = new PrintStream(summaryStream);
 				}
 				catch (IOException e) {
 					System.err.println("Unable to redirect output to file.");
 					e.printStackTrace();
 				}
 				
 				// GIT commit verification //
 				// Get the file called GIT_PATCH.txt from the current project directory
 				File gitNotes = f.listFiles(filter)[0];
 				boolean goodCommit = check.checkGitCommits(gitNotes);
 				if (goodCommit)
 					out.println(f.getName() + " GIT+");
 				else
 					err.println(f.getName() + " GIT-");
 				// End GIT commit verification //
 				
 				check.printMessage("\nIsort verification:", 0);
 				// isort make verification //
 				File isortDir = new File(f, "part1");
 				boolean goodMake = check.checkMake(isortDir, "isort");
 				if (goodMake)
 					out.println(f.getName() + " isort: make+");
 				else
 					err.println(f.getName() + " isort: make-");
 				// end isort make verification //
 				
 				// isort verification //
 				boolean[] badIsort = check.testCommand(isortDir, "isort", isin);
 				if (badIsort[0])
 					err.println(f.getName() + " isort: memory error-");
 				else
 					out.println(f.getName() + " isort: memory error+");
 				if (badIsort[1])
 					err.println(f.getName() + " isort: leak error-");
 				else
 					out.println(f.getName() + " isort: leak error+");
 				// end isort verification //
 				
 				// isort make clean verification //
 				boolean cleanWorked = check.checkMakeClean(isortDir, "isort");
 				if (cleanWorked)
 					out.println(f.getName() + " isort: make clean+");
 				else
 					err.println(f.getName() + " isort: make clean-");
 				// end isort make clean verification //
 				
 				check.printMessage("\nTwecho verification:", 0);
 				
 				// twecho make verification //
 				File twDir = new File(f, "part2");
 				goodMake = check.checkMake(twDir, "twecho");
 				if (goodMake)
 					out.println(f.getName() + " twecho: make+");
 				else
 					err.println(f.getName() + " twecho: make-");
 				// end twecho make verification //
 				
 				// twecho verification //
 				boolean[][] twechoSuccess = new boolean[4][2];
 				twechoSuccess[0] = check.testCommand(twDir, "twecho hello world dude", null);
 				twechoSuccess[1] =
 				        check.testCommand(twDir, "twecho 129!oihd as923!#0 njkdas54%()", null);
 				twechoSuccess[2] = check.testCommand(twDir, "twecho I AM IN ALL CAPS 1", null);
 				twechoSuccess[3] = check.testCommand(twDir, "twecho", null);
 				boolean memErr = false;
 				boolean leakErr = false;
 				for (boolean[] errSum : twechoSuccess) {
 					if (errSum[0])
 						memErr = true;
 					if (errSum[1])
 						leakErr = true;
 				}
 				if (memErr)
 					err.println(f.getName() + " twecho: memory error-");
 				else
 					out.println(f.getName() + " twecho: memory error+");
 				if (leakErr)
 					err.println(f.getName() + " twecho: leak error-");
 				else
 					out.println(f.getName() + " twecho: leak error+");
 				// end twecho verification //
 				
 				// twecho make clean verification //
 				cleanWorked = check.checkMakeClean(twDir, "twecho");
 				if (cleanWorked)
 					out.println(f.getName() + " twecho: make clean+");
 				else
 					err.println(f.getName() + " twecho: make clean-");
 				// end twecho make clean verification //
 				
 				// Clean up
 				check.shutdown();
 			}
 		}
 	}
 	
 	public static void main(String[] args)
 	{
 		int threads = (int) Math.round(Runtime.getRuntime().availableProcessors());
 		if (args.length == 1)
 			if (Integer.parseInt(args[0]) < threads)
 				threads = Integer.parseInt(args[0]);
 		try {
 			new GraderMT("dat", threads);
 		}
 		catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 }
