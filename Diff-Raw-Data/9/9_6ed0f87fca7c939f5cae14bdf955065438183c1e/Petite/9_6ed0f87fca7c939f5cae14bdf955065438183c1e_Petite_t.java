 /* 
  * License: source-license.txt
  * If this code is used independently, copy the license here.
  */
 
 package wombat.scheme;
 
 import java.io.*;
 import java.net.URISyntaxException;
 import java.util.*;
 import java.util.concurrent.locks.Lock;
 import java.util.concurrent.locks.ReentrantLock;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipFile;
 
 import wombat.scheme.util.InteropAPI;
 
 /**
  * Class to wrap Petite bindings.
  */
 public class Petite {
	static final boolean DEBUG_INTEROP = false;
 
 	/**
 	 * Run from the command line, providing a REPL.
 	 * 
 	 * @param args
 	 *            Ignored.
 	 * @throws URISyntaxException
 	 *             If we botched the files from the JAR.
 	 */
 	public static void main(String[] args) throws URISyntaxException {
 		try {
 			final Petite p = new Petite();
 			final Scanner s = new Scanner(System.in);
 
 			p.addPetiteListener(new PetiteListener() {
 				@Override public void onReady() {
 					System.out.print(">> ");
 				}
 				
 				@Override public void onOutput(String output) {
 					System.out.print(output);
 				}
 				
 				@Override
 				public void onInteropReturn() {}
 			});
 			
 			while (true) {
 				if (s.hasNextLine()) {
 					String cmd = s.nextLine();
 					p.sendCommand(cmd);
 				}
 
 				try {
 					Thread.sleep(10);
 				} catch (InterruptedException e) {
 				}
 			}
 
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	static final String os = System.getProperty("os.name").toLowerCase();
 	static final boolean IsWindows = os.indexOf("win") != -1;
 	static final boolean IsOSX = os.indexOf("mac") != -1;
 	static final boolean IsLinux = (os.indexOf("nux") != -1) || (os.indexOf("nix") != -1);
 	static final boolean Is64Bit = System.getProperty("os.arch").indexOf("64") != -1;        
 
 	static final char Prompt1 = '|';
 	static final char Prompt2 = '`';
 	static final char Interop = '!';
 
 	boolean RestartOnCollapse;
 	boolean InStartup;
 	boolean SeenPrompt1;
 	boolean InInterop;
 	
 	List<PetiteListener> Listeners;
 
 	StringBuffer Buffer;
 	StringBuffer InteropBuffer;
 	Lock BufferLock;
 
 	Writer ToPetite;
 	Reader FromPetite;
 	Process NativeProcess;
 
 	Thread FromPetiteThread;
 
 	// The root is either this directory or a nested 'lib' directory.
 	static File[] searchDirs;
 
 	static {
 		try {
 			searchDirs = new File[] {
 				new File("").getCanonicalFile(),
 				new File(new File("").getCanonicalFile(), "lib").getCanonicalFile(),
 				new File(Petite.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getCanonicalFile(),
 				new File(new File(Petite.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()),"lib").getCanonicalFile(),
 				new File(Petite.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getParentFile().getCanonicalFile(),
 				new File(new File(Petite.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getParentFile(), "lib").getCanonicalFile(), 
 			};
 		} catch (IOException ex) {
 		} catch (URISyntaxException e) {
 		}
 	};
 
 	/**
 	 * Create a new Petite thread.
 	 * 
 	 * @throws IOException
 	 *             If we fail to access the Petite process.
 	 * @throws URISyntaxException
 	 *             If we have problems getting the path from a JAR file.
 	 */
 	public Petite() throws IOException, URISyntaxException {
 		// Connect to an initial Petite session.
 		connect();
 	}
 
 	/**
 	 * (Re)connect the Petite process.
 	 * 
 	 * @throws IOException
 	 *             If we couldn't connect.
 	 * @throws URISyntaxException
 	 *             If we have problems getting the path from a JAR file.
 	 */
 	private void connect() throws IOException, URISyntaxException {
 		System.err.println("Petite connecting");
 
 		// Reset the wrapper state (necessary in the case of a reconnect).
 		SeenPrompt1 = false;
 		InStartup = true;
 		InInterop = false;
 		RestartOnCollapse = true;
 		
 		Listeners = new ArrayList<PetiteListener>();
 		
 		if (Buffer == null)
 			Buffer = new StringBuffer();
 		else
 			Buffer.delete(0, Buffer.length());
 
 		if (InteropBuffer == null)
 			InteropBuffer = new StringBuffer();
 		else
 			InteropBuffer.delete(0, InteropBuffer.length());
 
 		BufferLock = new ReentrantLock();
 
 		// Find the correct Petite directory.
 		File pdir = null;
 		for (File dir : searchDirs) {
 			System.out.println("Looking in " + dir + " for petite.");
 			if (dir != null && dir.exists() && dir.isDirectory()) {
 				for (String path : dir.list()) {
 					if (path.startsWith("petite") && path.endsWith(IsWindows ? "win" : IsOSX ? "osx" : IsLinux ? "linux" : "unknown")) {
 						pdir = new File(dir, path);
 						System.out.println("Found: " + pdir);
 						break;
 					}
 				}
 			}
 			if (pdir != null) 
 				break;
 		}
 
 		// If it didn't we may have to look for zip files.
 		if (pdir == null) {
 			System.out.println("Petite not find, looking for an archive.");
 			for (File dir : searchDirs) {
 				System.out
 						.println("Looking in " + dir + " for petite archive.");
 				if (dir != null && dir.exists() && dir.isDirectory()) {
 					for (String path : dir.list()) {
 						if (path.startsWith("petite")
 								&& path.endsWith((IsWindows ? "win"
 										: IsOSX ? "osx" : IsLinux ? "linux"
 												: "unknown")
 										+ ".zip")) {
 							ZipFile zip = new ZipFile(new File(dir, path));
 							System.out.println("Found: " + new File(dir, path).getCanonicalPath());
 
 							@SuppressWarnings("unchecked")
 							Enumeration<ZipEntry> entries = (Enumeration<ZipEntry>) zip
 									.entries();
 
 							while (entries.hasMoreElements()) {
 								ZipEntry entry = entries.nextElement();
 
 								if (entry.isDirectory()) {
 									System.out.println("\tunzipping: " + entry.getName());
 									new File(dir, entry.getName()).getCanonicalFile().mkdirs();
 								} else {
 							    	System.out.println("\tunzipping: " + entry.getName());
 							    	new File(dir, entry.getName()).getCanonicalFile().getParentFile().mkdirs();
 							    
 									File targetFile = new File(dir, entry.getName());
 									InputStream zipin = zip.getInputStream(entry);
 									OutputStream zipout = new BufferedOutputStream(new FileOutputStream(targetFile));
 												    
 									byte[] buffer = new byte[1024];
 									int len;
 												    
 									while((len = zipin.read(buffer)) >= 0)
 										zipout.write(buffer, 0, len);
 											    
 									zipin.close();
 									zipout.close();
 									
 									if (targetFile.getName().toLowerCase().startsWith("petite")) {
 										targetFile.setExecutable(true);
 								    }
 							    }
 							}
 
 							zip.close();
 
 							// Try again.
 							connect();
 							return;
 						}
 					}
 				}
 			}
 		}
 
 		// If we made it this far without a directory, something broke. :(
 		if (pdir == null)
 			throw new IOException("Unable to find Petite directory.");
 
 		String petiteBinary = null;
 		String petiteBoot = null;
 		if (IsWindows) {
 		    petiteBinary = "petite.exe";
 		    petiteBoot = "petite.boot";
 		} else {
 		    if (Is64Bit) {
 			petiteBinary = "petite64";
 			petiteBoot = "petite64.boot";
 		    } else {
 			petiteBinary = "petite";
 			petiteBoot = "petite.boot";
 		    }
 		}
 
 		System.out.println("Binary: " + petiteBinary + ", Boot: " + petiteBoot);
 
 		// Create the process builder.
 		ProcessBuilder pb = new ProcessBuilder(
 		    new File(pdir, petiteBinary).getCanonicalPath(), 
 		    "-b", 
 		    new File(pdir, petiteBoot).getCanonicalPath()
 		);
 		pb.directory(pdir.getParentFile().getParentFile());
 		pb.redirectErrorStream(true);
 
 		// Start the process.
 		NativeProcess = pb.start();
 
 		// Set up the print writer.
 		ToPetite = new PrintWriter(NativeProcess.getOutputStream());
 		FromPetite = new InputStreamReader(NativeProcess.getInputStream());
 
 		// Immediately send the command to reset the prompt.
 		reset();
 
 		// Create a listener thread.
 		if (FromPetiteThread == null) {
 			FromPetiteThread = new PetiteIOThread(); 
 			FromPetiteThread.setDaemon(true);
 			FromPetiteThread.start();
 		}
 
 		// Show down the thread when done.
 		Runtime.getRuntime().addShutdownHook(new Thread("Petite Shutdown") {
 			public void run() {
 				NativeProcess.destroy();
 			}
 		});
 	}
 
 	/**
 	 * Reset Petite's environment.
 	 */
 	public void reset() {
 		// Actually clear the environment
 		sendCommand("(interaction-environment (copy-environment (scheme-environment) #t))");
 
 		// So that (eq? 'A 'a) => #t
 		sendCommand("(case-sensitive #f)");
 
 		// So that gensyms look at least semi-sane (it's not like anyone will
 		// need them)
 		sendCommand("(print-gensym #f)");
 
 		// To test infinite loops
 		sendCommand("(define (omega) ((lambda (x) (x x)) (lambda (x) (x x))))");
 
 		// Fix error message that give define/lambda names
 		sendCommand("(import (wombat define))");
 		
 		// Reset the library directories.
 		sendCommand("(library-directories '((\"lib\" . \"lib\") (\".\" . \".\") (\"..\" . \"..\") (\"dist\" . \"dist\") (\"dist/lib\" . \"dist/lib\")))");
 
 		// Make sure that the prompt is set as we want it
 		// Set this last so all of the startup commands have time to run
 		sendCommand("(waiter-prompt-string \"|`\")");
 	}
 
 	/**
 	 * Stop the running process.
 	 * 
 	 * @throws IOException
 	 *             If we cannot connect.
 	 * @throws URISyntaxException
 	 *             Botched file from JAR.
 	 */
 	public void stop() throws IOException, URISyntaxException {
 		System.err.println("Petite stopping");
 
 		// Shut down the old connection.
 		RestartOnCollapse = false;
 		BufferLock.lock();
 		Buffer.delete(0, Buffer.length());
 		BufferLock.unlock();
 		NativeProcess.destroy();
 
 		try {
 			NativeProcess.waitFor();
 		} catch (InterruptedException e) {
 		}
 	}
 
 	/**
 	 * Stop and restart.
 	 * 
 	 * @throws IOException
 	 *             If we cannot connect.
 	 * @throws URISyntaxException
 	 *             Botched file from JAR.
 	 */
 	public void restart() throws IOException, URISyntaxException {
 		System.err.println("Petite restarting");
 
 		stop();
 		connect();
 	}
 
 	/**
 	 * Listen for state changes in the Petite binding.
 	 * @param pl A listener
 	 */
 	public void addPetiteListener(PetiteListener pl) {
 		Listeners.add(pl);
 	}
 	
 	/**
 	 * Stop a certain Petite listener.
 	 * @param pl The listener that we are watching.
 	 */
 	public void removePetiteListener(PetiteListener pl) {
 		Listeners.remove(pl);
 	}
 	
 	/**
 	 * If the output buffer has any content.
 	 * 
 	 * @return True or false.
 	 */
 	public boolean hasOutput() {
 		return !InStartup && (Buffer.length() > 0);
 	}
 
 	/**
 	 * Send a command to the Petite process.
 	 * 
 	 * @param cmd
 	 *            The command to send.
 	 */
 	public void sendCommand(String cmd) {
 		try {
 			// Swap out lambda character for string
 			cmd = cmd.replace("\u03BB", "lambda");
 
 			// Send it, make sure there's a newline.
 			// Use flush to force it.
 			ToPetite.write(cmd);
 			if (!cmd.endsWith("\n"))
 				ToPetite.write("\n");
 			ToPetite.flush();
 
 		} catch (Exception e) {
 			System.err.println("Unable to execute command");
 			Buffer.append("\nException: Unable to execute command\n");
 			e.printStackTrace();
 		}
 	}
 	
 	private final class PetiteIOThread extends Thread {
 		PetiteIOThread() { super("Petite IO"); }
 		
 		public void run() {
 			char c;
 			try {
 				while (true) {
 					c = (char) FromPetite.read();
 					BufferLock.lock();
 
 					// Ignore end of file characters.
 					if (c == (char) 65535) {
 					}
 
 					// Potential start of a prompt.
 					else if (c == Prompt1) {
 						SeenPrompt1 = true;
 					}
 
 					// The whole prompt.
 					else if (SeenPrompt1 && c == Prompt2) {
 						SeenPrompt1 = false;
 
 						if (InStartup) {
 							Buffer.delete(0, Buffer.length());
 							InStartup = false;
 						}
 
 						for (PetiteListener pl : Listeners) {
 							BufferLock.lock();
 							pl.onOutput(Buffer.toString());
 							Buffer.delete(0, Buffer.length());
 							BufferLock.unlock();
 							pl.onReady();
 						}
 					}
 
 					// Switch to interop mode on Prompt1 + Interop
 					else if (SeenPrompt1 && c == Interop) {
 						SeenPrompt1 = false;
 
 						if (InInterop) {
 							String[] parts = InteropBuffer.toString()
 									.split(" ", 2);
 							String key = parts[0];
 							String val = (parts.length > 1 ? parts[1]
 									: null);
 
 							if (DEBUG_INTEROP) System.out.println("calling interop: " + key + " with " + val); // debug
 							String result = InteropAPI
 									.interop(key, val);
 							if (DEBUG_INTEROP) System.out.println("interop returns: " + (result.length() > 10 ? result .subSequence(0, 10) + "..." : result)); // debug
 							if (result != null) {
 								ToPetite.write(result + " ");
 								ToPetite.flush();
 							}
 
 							if (DEBUG_INTEROP) System.out.println("exiting interop");
 							for (PetiteListener pl : Listeners) {
 								pl.onInteropReturn();
 							}
 
 							InteropBuffer.delete(0, InteropBuffer.length());
 							InInterop = false;
 						} else {
 							if (DEBUG_INTEROP) System.out.println("entering interop"); // debug
 
 							InInterop = true;
 						}
 
 					}
 
 					// Thought it was a prompt, but we were wrong.
 					// Remember to store the first half of the prompt.
 					else if (SeenPrompt1) {
 						SeenPrompt1 = false;
 
 						if (InInterop) {
 							InteropBuffer.append(Prompt1);
 							InteropBuffer.append(c);
 						} else {
 							Buffer.append(Prompt1);
 							Buffer.append(c);
 						}
 					}
 
 					// Normal case, no new characters.
 					else {
 						if (InInterop) {
 							InteropBuffer.append(c);
 						} else {
 							Buffer.append(c);
 						}
 					}
 
 					BufferLock.unlock();
 				}
 
 			} catch (Exception e) {
 				if (RestartOnCollapse) { 
 					System.err.println("Petite buffer is broken");
 					Buffer.append("\nException: Petite buffer is broken\n");
 					e.printStackTrace();
 					if (BufferLock.tryLock())
 						BufferLock.unlock();
 
 					try {
 						restart();
 					} catch (Exception e2) {
 					}
 				}
 			}
 		}
 	}
 }
 
