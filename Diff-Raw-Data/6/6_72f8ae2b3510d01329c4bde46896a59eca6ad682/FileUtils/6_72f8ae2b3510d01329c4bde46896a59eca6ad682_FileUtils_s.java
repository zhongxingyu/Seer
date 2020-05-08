 package nl.nikhef.jgridstart.util;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileFilter;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.commons.lang.StringUtils;
 
 /** Some file-related utilities */
 public class FileUtils {
     static protected Logger logger = Logger.getLogger("nl.nikhef.jgridstart.util");
     
     /** cache file copy method (Windows only) */
     static private boolean hasRobocopy = false;
     static private boolean copyDetected = false;
    static private String xcopyInput = null;
     
     /** Copy a file from one place to another, retaining permisions.
      * <p>
      * This calls an external copy program on the operating system to
      * make sure it is properly copied and permissions are retained.
      * <p>
      * It is strongly advised to make sure the destination file does
      * not exist already.
      * 
      * @return {@code true} if file was copied, {@code false} if no files was copied
      * @throws IOException on error, as far as it can be detected from the utilities
      */
     public static boolean CopyFile(File in, File out) throws IOException {
 	String[] cmd;
 	
 	if (!in.canRead())
 	    throw new IOException("source file unreadable: "+in);
 	
 	if (System.getProperty("os.name").startsWith("Windows")) {
 	    // detect copy method once
 	    if (!copyDetected) {
 		// windows: use special copy program to retain permissions.
 		//   on Vista, "xcopy /O" requires administrative rights, so we
 		//   have to resort to using robocopy there.
 		try {
 		    int ret = Exec(new String[]{"robocopy.exe"});
 		    if (ret==0 || ret==16) hasRobocopy = true;
 		} catch (Exception e) { }
 		copyDetected = true;
 	    }
 	    
 	    if (hasRobocopy) {
 		// we have robocopy. But ... its destination filename
 		//   needs to be equal to the source filename :(
 		// So we make a temporary subdir, create a copy of the
 		// file in there, and rename+move the file to the destination.
 		// All this is required to copy a file retaining its permissions.
 		
 		// create new temp dir as subdir of new destination directory
 		File tmpdir = createTempDir(out.getName(), out.getParentFile());
 		// temporary copy
 		File tmpfile = new File(tmpdir, in.getName());
 
 		boolean copied = false;
 		try {
 		    // copy file to tmpdir
 		    cmd = new String[]{"robocopy.exe",
 			    in.getParent(), tmpdir.getPath(),
 			    tmpfile.getName(),
 			    "/SEC", "/NP", "/NS", "/NC", "/NFL", "/NDL"};
 		    int ret = Exec(cmd);
 		    // http://support.microsoft.com/kb/954404
 		    copied = (ret&1) != 0;
 		    if (ret >= 8)
 			throw new IOException("robocopy error #"+ret);
 		    // rename new file
 		    if (!tmpfile.renameTo(out))
 			throw new IOException("Could not copy\n  "+in+"\nto\n  "+out+"\n(robocopy, phase rename)");
 		} finally {
 		    // cleanup
 		    tmpfile.delete();
 		    tmpdir.delete();
 		}
 		return copied;
 	    } else {
 		// use xcopy instead
 		cmd = new String[]{"xcopy.exe",
 		    in.getAbsolutePath(),
 		    out.getAbsolutePath(),
 		    "/O", // copy file ownership and ACL information
 		    "/Q", // be quiet
 		    "/H", // copy hidden and system files also
 		    "/K", // copy attributes, normally xcopy will reset readonly attr
 		    "/Y"};// suppress confirm prompts
 		// If the file doesn't exist on copying, xcopy will ask whether you want
 		// to create it as a directory or just copy a file. The input required is
 		// locale-dependant (sigh), so we need to detect that first if aborted.
 		StringBuffer output = new StringBuffer();
 		int ret = Exec(cmd, xcopyInput, output);
 		// check if aborted due to missing xcopyInput
		if (ret==2 && xcopyInput==null) {
 		    Matcher m = Pattern.compile("\\(\\s*(.)\\s*=.*?,\\s*(.)\\s*=.*?\\)").matcher(output);
 		    if (m.find()) {
 			// store and try again; first match means file copy
 			xcopyInput = m.group(1);
 			return CopyFile(in, out);
 		    }
 		}
 		if (ret==0) return true;
 		if (ret==1) return false;
 		// Catch xcopy bug; if the user has no access to one of the parent
 		//  directories, xcopy will fail using "File not found - [source filename]".
 		//  We'll have to resort to copying without keeping permissions ... we want
 		//  the software to work, right.
 		//  TODO ask user or find another way
 		if (ret==4 && output.toString().contains("File not found")) {
 		    logger.warning("xcopy bug triggered, copying without retaining permissions (!) - "+in);
 		    cmd = new String[]{"cmd", "/C",
 			    "copy /B /Y \""+in.getAbsolutePath()+"\" \""+out.getAbsolutePath()+"\""};
 		    ret = Exec(cmd);
 		    if (ret==0) return true;
 		    throw new IOException("copy failed, return code "+ret+"\n(after xcopy bug was triggered)");
 		}
 		// handle return value
 		if (ret==2) throw new IOException("xcopy aborted\n"+output.toString());
 		if (ret==4) throw new IOException("xcopy initialization error\n"+output.toString());
 		if (ret==5) throw new IOException("xcopy disk write error\n"+output.toString());
 		throw new IOException("unknown xcopy return code "+ret);
 	    }
 	    
 	} else {
 	    // other, assume unix-like
 	    cmd = new String[]{"cp",
 		    "-f", "-p",
 		    in.getAbsolutePath(), out.getAbsolutePath()};
 	    int ret = Exec(cmd);
 	    if (ret!=0)
 		throw new IOException("cp failed, return code "+ret);
 	    return true;
 	}
     }
     
     /** List ordinary files in a directory.
      * <p>
      * 
      * @see File#listFiles
      * @param path Directory to list files in
      */
     public static File[] listFilesOnly(File path) {
 	return path.listFiles(new FileFilter() {
 	    public boolean accept(File pathname) {
 		return pathname.isFile();
 	    }
 	});
     }
     
     /** Copies a list of files to a directory.
      * <p>
      * When an error occurs, files already copied are removed again and
      * an {@linkplain IOException} is thrown.
      * <p>
      * It is discouraged to have files with the same name existing
      * already in the destination path.
      */
     public static void CopyFiles(File[] fromFiles, final File toPath) throws IOException {
 	EachFile(fromFiles, new FileCallback() {
 	    public void action(File f) throws IOException {
 		File toFile = new File(toPath, f.getName());
 		if (!CopyFile(f, toFile))
 		    throw new IOException("Copy failed: "+f+" -> "+toFile);
 	    }
 	    public void reverseAction(File f) throws IOException {
 		File toFile = new File(toPath, f.getName());
 		if (!toFile.delete())
 		    throw new IOException("Delete failed: "+toFile);
 	    }
 	});
     }
     
     /** Moves a list of files to a directory.
      * <p>
      * When an error occurs, files already moved are put back and an
      * {@linkplain IOException} is thrown.
      * <p>
      * It is discouraged to have files with the same name
      * existing already in the destination path.
      */
     public static void MoveFiles(File[] fromFiles, final File toPath) throws IOException {
 	EachFile(fromFiles, new FileCallback() {
 	    public void action(File f) throws IOException {
 		File toFile = new File(toPath, f.getName());
 		if (!f.renameTo(toFile))
 		    throw new IOException("Move failed: "+f+" -> "+toFile);
 	    }
 	    public void reverseAction(File f) throws IOException {
 		File toFile = new File(toPath, f.getName());
 		if (!toFile.renameTo(f))
 		    throw new IOException("Move back failed: "+toFile+" -> "+f);
 	    }
 	});
     }
 
     /** Helper method: remove directory recursively.
      * <p>
      * Beware, symlinks might traverse into unwanted territory!
      */
     public static void recursiveDelete(File what) {
 	File[] children = what.listFiles();
 	for (int i=0; i<children.length; i++) {
 	    File c = children[i];
 	    // make sure we can read/write it so traversal and deletion are possible
 	    chmod(c, true, true, c.isDirectory(), true);
 	    if (c.isDirectory())
 		recursiveDelete(c);
 	    c.delete();
 	}
 	what.delete();
     }
     
     /** Runs a callback on each file specified.
      * <p>
      * Implements a rollback mechanism: when the callback throws an {@linkplain IOException},
      * the previous operations are undone by means of the {@link FileCallback#reverseAction}
      * method.
      */
     protected static void EachFile(File[] fromFiles, FileCallback callback) throws IOException {
 	// then copy or move
 	int i=0;
 	try {
 	    for (i=0; i<fromFiles.length; i++) {
 		callback.action(fromFiles[i]);
 	    }
 	} catch (IOException e1) {
 	    String extra = "";
 	    // move already moved files back
 	    for (int j=0; j<i; j++) {
 		try {
 		    callback.reverseAction(fromFiles[j]);
 		} catch (IOException e2) {
 		    extra += "\n" + e2.getLocalizedMessage();
 		}
 	    }
 	    // and propagate exception
 	    if (extra=="")
 		throw e1;
 	    else
 		throw new IOException(e1.getLocalizedMessage() +
 			"\n\nNote that the following errors occured on rollback(!):\n" +
 			extra);
 	}
     }
     /** Callback handler for {@link #EachFile} */
     protected interface FileCallback {
 	/** Does an action on a File */
 	public void action(File f) throws IOException;
 	/** Reverses the action of {@linkplain #action} on a File */
 	public void reverseAction(File f) throws IOException;
     }
     
     /**
      * Return the contents of a text file as String
      */
     public static String readFile(File file) throws IOException {
 	String s = System.getProperty("line.separator");
 	BufferedReader r = new BufferedReader(new FileReader(file));
 	StringBuffer buf = new StringBuffer();
 	String line;
 	while ( (line = r.readLine() ) != null) {
 	    buf.append(line);
 	    buf.append(s);
 	}
 	r.close();
 	return buf.toString();
     }
     
     /**
      * Write a String to a text file, possibly overwriting it.
      */
     public static void writeFile(File file, String data) throws IOException {
 	FileWriter writer = new FileWriter(file);
 	writer.write(data);
 	writer.close();
     }
 
     /**
      * Change file permissions
      * <p>
      * Not supported natively until java 1.6. Bad Java.
      * Note that the ownerOnly argument differs from Java. When {@code ownerOnly} is
      * true for Java's {@link File#setReadable}, {@link File#setWritable} or
      * File.setExecutable(), the other/group permissions are left as they are.
      * This method resets them instead. When ownerOnly is false, behaviour is
      * as Java's.
      * 
      * @param file File to set permissions on
      * @param read  if reading should be allowed
      * @param write if writing should be allowed
      * @param exec if executing should be allowed
      * @param ownerOnly true to set group&world permissions to none,
      *                  false to set them all alike
      */
     static public boolean chmod(File file, boolean read, boolean write,
 	    boolean exec, boolean ownerOnly) {
 	try {
 	    // Try Java 1.6 method first.
 	    // This is also compilable on lower java versions
 	    boolean ret = true;
 	    Method setReadable = File.class.getDeclaredMethod("setReadable",
 		    new Class[] { boolean.class, boolean.class });
 	    Method setWritable = File.class.getDeclaredMethod("setWritable",
 		    new Class[] { boolean.class, boolean.class });
 	    Method setExecutable = File.class.getDeclaredMethod("setExecutable",
 		    new Class[] { boolean.class, boolean.class });
 
 	    // first remove all permissions if ownerOnly is wanted, because File.set*
 	    // doesn't touch other/group permissions when ownerOnly is true.
 	    if (ownerOnly) {
 		ret &= (Boolean)setReadable.invoke(file, new Object[]{ false, false });
 		ret &= (Boolean)setWritable.invoke(file, new Object[]{ false, false });
 		ret &= (Boolean)setExecutable.invoke(file, new Object[]{ false, false });
 	    }
 	    // then set owner/all permissions
 	    ret &= (Boolean)setReadable.invoke(file, new Object[] { read, ownerOnly });
 	    ret &= (Boolean)setWritable.invoke(file, new Object[] { write, ownerOnly });
 	    ret &= (Boolean)setExecutable.invoke(file, new Object[] { exec, ownerOnly });
 	    
 	    if (logger.isLoggable(Level.FINEST)) {
 		String perms = new String(new char[] {
 			read?  'r' : '-',
 			write? 'w' : '-',
 			exec?  'x' : '-'
 		}) + (ownerOnly ? "user" : "all");
 		logger.finest("Java 1.6 chmod "+perms+" of "+file+" returns "+ret);
 	    }
 
 	    return ret;
 	} catch (InvocationTargetException e) {
 	    // throw exceptions caused by set* methods
 	    throw (SecurityException)e.getTargetException();
 	    // return false; // (would be unreachable code)
 
 	} catch (NoSuchMethodException e) {
 	} catch (IllegalAccessException e) {
 	} catch (IllegalArgumentException e) {
 	}
 	
 	try {
 	    // fallback to unix command
 	    int perm = 0;
 	    if (read)  perm |= 4;
 	    if (write) perm |= 2;
 	    if (exec)  perm |= 1;
 	    String[] chmodcmd = { "chmod", null, file.getPath() };
 	    if (ownerOnly) {
 		Object args[] = { new Integer(perm) };
 		chmodcmd[1] = String.format("0%1d00", args);
 	    } else {
 		Object args[] = {new Integer(perm),new Integer(perm),new Integer(perm)};
 		chmodcmd[1] = String.format("0%1d%1d%1d", args);
 	    }
 	    return Exec(chmodcmd) == 0;
 	} catch (Exception e2) {
 	    return false;
 	}
     }
     
     /** Create a temporary directory with read-only permissions.
      * <p>
      * TODO Current code contains a race-condition. Please see Sun
      * bug <a href="http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4735419">4735419</a>
      * for a better solution.
      */
     public static File createTempDir(String prefix, File directory) throws IOException {
 	File d = File.createTempFile(prefix, null, directory);
 	d.delete();
 	d.mkdirs();
 	chmod(d, true, true, true, true);
 	return d;
     }
     public static File createTempDir(String prefix) throws IOException {
 	return createTempDir(prefix, new File(System.getProperty("java.io.tmpdir")));
     }
     
     /** Exec invocation index, to keep track of logging lines */
     private static int globalExecIndex = 0;
     
     /** Execute a command, enter input on stdin, and return the exit code while storing stdout and stderr.
      * <p>
      * The process must stop waiting for input by itself, and not rely on its stdin
      * being closed. This doesn't work on Windows, so the process will never terminate.
      * 
      * @param cmd command to run
      * @param input String to feed to process's stdin
      * @param output String to which stdout and stderr is appended, or null
      * @return process exit code
      * 
      * @throws IOException */
     public static int Exec(String[] cmd, String input, StringBuffer output) throws IOException {
 	// get current exec invocation number for stdout/stderr tracking
 	int index = globalExecIndex++;	
 	// log
 	logger.finer("[  exec #"+index+"] cmd: "+StringUtils.join(cmd, ' '));
 	// run
 	Process p = Runtime.getRuntime().exec(cmd);
 	if (input!=null) {
 	    p.getOutputStream().write(input.getBytes());
 	    p.getOutputStream().close();
 	}
 	// retrieve output
 	String s = System.getProperty("line.separator");
 	String lineout = null, lineerr = null;
 	BufferedReader stdout = new BufferedReader(
 		new InputStreamReader(p.getInputStream()));
 	BufferedReader stderr = new BufferedReader(
 		new InputStreamReader(p.getErrorStream()));
 	while ( (lineout=stdout.readLine()) != null || (lineerr=stderr.readLine()) != null) {
 	    if (lineout!=null) logger.finest("[stdout #"+index+"] "+lineout);
 	    if (lineerr!=null) logger.finest("[stderr #"+index+"] "+lineerr);
 	    if (lineout!=null && output!=null) output.append(lineout + s);
 	    if (lineerr!=null && output!=null) output.append(lineerr + s);
 	}
 	stdout.close();
 	stderr.close();
 	// log and return
 	int ret = -1;
 	try {
 	    ret = p.waitFor();
 	} catch (InterruptedException e) {
 	    // TODO verify this is the right thing to do
 	    throw new IOException(e.getMessage());
 	}
 	logger.finest("[  exec #"+index+"] returns "+ret);
 	return ret;
     }
     
     /** Execute a command and return the exit code while storing stdout and stderr.
      * 
      * @param cmd command to run
      * @param output String to which stdin and stdout is appended.
      * @return process exit code
      * 
      * @throws IOException
      */
     public static int Exec(String[] cmd, String output) throws IOException {
 	return Exec(cmd, null, null);
     }
     
     /** Execute a command and return the exit code 
      * @throws IOException */
     public static int Exec(String[] cmd) throws IOException {
 	return Exec(cmd, null);
     }
 }
