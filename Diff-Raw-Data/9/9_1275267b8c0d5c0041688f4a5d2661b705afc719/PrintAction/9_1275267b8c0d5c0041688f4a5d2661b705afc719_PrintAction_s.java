 package actions;
 import java.io.File;
 
 import java.io.IOException;
 
 /**
  * 
  * print file's details action
  *
  */
 public class PrintAction extends Action {
 
 	
 	private static final String _readPermission = "r";
 	private static final String _writePermission = "w";
 	private static final String _execPermission = "x";
 	private static final String _noPermission = "-";
 	public PrintAction(String param){
 	
 	}
 	
 	/**
 	 * print the file's details
 	 */
 	public void performAction(File f)
 	{
 		System.out.println(getReadPermission(f) +
 						   getWritePermission(f) + 
 						   getExecPermission(f) + " " +
 						   getFileSize(f) + " " + 
 						   getFullPath(f));
 	}
 	
 	/**
 	 * get whether the file is readable
 	 * @param f the file
 	 * @return a string representing the readability
 	 */
 	
 	private String getReadPermission(File f)
 	{
 		if (f.canRead())
 		{
 			return _readPermission;
 		}
 		else
 		{
 			return _noPermission;
 		}
 	}
 	
 	/**
 	 * get whether the file is writable
 	 * @param f the file
 	 * @return a string representing whether the file is writable
 	 */
 	
 	private String getWritePermission(File f)
 	{
		if (f.canExecute())
 		{
 			return _writePermission;
 		}
 		else
 		{
 			return _noPermission;
 		}
 	}
 	
 	/**
 	 * get whether the file is writable
 	 * @param f the file
 	 * @return a string representing whether the file is executable
 	 */
 	private String getExecPermission(File f)
 	{
 		if (f.canExecute())
 		{
 			return _execPermission;
 		}
 		else
 		{
 			return _noPermission;
 		}
 	}
 	
 	/**
 	 * check the file size
 	 * @param f the file
 	 * @return the size
 	 */
 	private long getFileSize(File f)
 	{
 		return f.length();
 	}
 	
 	/**
 	 * get the full path of a file
 	 * @param f the file
 	 * @return the path
 	 */
 	private String getFullPath(File f)
 	{
 		try
 		{
 			return f.getCanonicalPath();
 		
 		}
 		catch (IOException e)
 		{
 			return "";
 		}
 	}
 	
 	
 
 }
