 import java.io.File;
 import java.io.IOException;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 
 
 // On my honor:
 //
 // - I have not used source code obtained from another student,
 // or any other unauthorized source, either modified or
 // unmodified.
 //
 // - All source code and documentation used in my program is
 // either my original work, or was derived by me from the
//source code published in the textbook for this course.
 //
 //- I have not discussed coding details about this project with
 // anyone other than my partner (in the case of a joint
 // submission), instructor, ACM/UPE tutors or the TAs assigned
 // to this course. I understand that I may discuss the concepts
 // of this program with other students, and that another student
 // may help me debug my program so long as neither of us writes
 // anything during the discussion or modifies any computer file
 // during the discussion. I have violated neither the spirit nor
 // letter of this restriction.
 
 /**
  * Main class for the program.
  * 
  * @author Tyler Kahn
  * @author Reese Moore
  * @version 12.09.2011
  */
 public class memman {
 	
 	/**
 	 * Main entry point for the program.
 	 * Reads the command line arguments
 	 * Allocates the underlying memory manager
 	 * Parses the command file and executes commands.
 	 * 
 	 * @param argv The command line arguments
 	 * @throws IOException
 	 * @throws IllegalArgumentException
 	 * @throws IllegalAccessException
 	 * @throws InvocationTargetException
 	 */
 	public static void main(String argv[]) 
 		throws IOException, IllegalArgumentException, IllegalAccessException, 
 		       InvocationTargetException 
 	{
 		// Make sure we get the proper number of command line arguments,
		// Otherwise print a useage statement.
 		if (argv.length != 3) {
 			System.out.println("memman - Memory Manager for Locational Records");
 			System.out.println("Usage:");
 			System.out.println("\tjava memman <pool-size> <num-recs> <command-file>");
 			System.exit(2);
 		}
 		
 		// Parse the command line arguments
 		int poolSize = Integer.parseInt(argv[0], 10);
 		int numRecs = Integer.parseInt(argv[1], 10);
 		File f = new File(argv[2]);
 		
 		// Allocate the memory manager and the object that is going to be
 		// calling methods on the memory manager.
 		MemoryManager mm = new MemoryManager(poolSize);
 		Executor ex = new Executor(mm, numRecs);
 		
 		// Parse the command file
 		Parser<Executor> parser = new Parser<Executor>(f, Executor.class);
 		for (Pair<Method, Object[]> p : parser) {
 			Method m = p.getLeft();
 			Object[] args = p.getRight();
 			
 			m.invoke(ex, args);
 		}
 	}
 }
