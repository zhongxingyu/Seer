 package node.storage;
 
 import java.io.BufferedReader;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import node.rpc.IStorageServer;
 import node.rpc.RPCException;
 import node.rpc.RPCNode;
 import edu.washington.cs.cse490h.lib.PersistentStorageReader;
 import edu.washington.cs.cse490h.lib.PersistentStorageWriter;
 import edu.washington.cs.cse490h.lib.Utility;
 
 
 public class StorageSystemServer implements IStorageServer
 {
 	private static final String TEMP_FILE = ".temp";
 
 	private static final int FILE_DOES_NOT_EXIST = 10;	
 	private static final int FILE_ALREADY_EXISTS = 11;
 	private static final int FILE_TOO_LARGE = 30;		
 	private static final int IO_EXCEPTION = 40;
 	
 	private static final int MAX_MESSAGE_SIZE = 1500;
 	
 	private RPCNode m_node;
 	
 	
 	public StorageSystemServer(RPCNode node)
 	{
 		m_node = node;
 	}
 	
 	
 	/**
 	 * API: createFile()
 	 * @param fileName
 	 */
 	public String createFile(String fileName) throws RPCException
 	{		
 		if (Utility.fileExists(m_node, fileName))
 		{				
 			throw new RPCException(FILE_ALREADY_EXISTS, null);
 		}
 
 		try 
 		{			
 			m_node.getWriter(fileName, false);
 			return null;
 		} 
 		catch (IOException e) 
 		{
 			throw new RPCException(IO_EXCEPTION, null);
 		}
 	}
 	
 	
 	/**
 	 * API: getFile()
 	 * @param fileName
 	 */
 	public String getFile(String fileName) throws RPCException
 	{
 		if (!Utility.fileExists(m_node, fileName))
 		{
 			throw new RPCException(FILE_DOES_NOT_EXIST, null);
 		}
 
 		try
 		{
 			String contents = readFileContents(fileName);
 			return contents;
 		}
 		catch (IOException e) 
 		{
 			throw new RPCException(IO_EXCEPTION, null);
 		}
 	}	
 	
 	
 	/**
 	 * putFile()
 	 * @param fileName
 	 * @param contents
 	 */
 	public String putFile(String fileName, String contents) throws RPCException
 	{
 		if (!Utility.fileExists(m_node, fileName))
 		{
 			throw new RPCException(FILE_DOES_NOT_EXIST, null);
 		}
 		
 		if (contents.getBytes().length > MAX_MESSAGE_SIZE)
 		{
 			throw new RPCException(FILE_TOO_LARGE, null);
 		}
 		
 		try {
 			updateFileContents(fileName, contents, false);
 			return null;
 		} catch (IOException e) {
 			throw new RPCException(IO_EXCEPTION, null);
 		}
 	}
 	
 	
 	/**
 	 * API: appendToFile()
 	 * @param fileName
 	 * @param contents
 	 */
 	public String appendToFile(String fileName, String contents) throws RPCException
 	{
 		try {						
 			if (!Utility.fileExists(m_node, fileName))
 			{
 				throw new RPCException(FILE_DOES_NOT_EXIST, null);
 			}
 			
 			if (readFileContents(fileName).getBytes().length > MAX_MESSAGE_SIZE)
 			{
 				throw new RPCException(FILE_TOO_LARGE, null);
 			}
 			
 			updateFileContents(fileName, contents, true);			
 			return null;
 		} catch (IOException e) {
 			throw new RPCException(IO_EXCEPTION, null);
 		}
 	}
 
 	
 	/**
 	 * API: deleteFile()
 	 * @param fileName
 	 */
 	public String deleteFile(String fileName) throws RPCException
 	{
 		if (!Utility.fileExists(m_node, fileName))
 		{
 			throw new RPCException(FILE_DOES_NOT_EXIST, null);
 		}
 		
 		try {
 			PersistentStorageWriter writer = m_node.getWriter(fileName, false);
 			writer.delete();
 			return null;
 		} catch (IOException e) {
 			throw new RPCException(IO_EXCEPTION, null);
 		}
 	}
 
 
     /**
      * Executes commands directed to the server version of the Storage System.
      *
      * @param from - who invoked the server.
      * @param methodName - the method being invoked.
      * @param params - any parameters to the method.
      */
     public void executeCommand(String command) throws RPCException {
     	String[] parts = command.split(" ");
     	String methodName = parts[0];
 
     	String[] params = new String[parts.length-1];
     	System.arraycopy(parts, 1, params, 0, params.length);
 
     	if (params.length < 1)
     	{
     		return;
     	}
 
     	String fileName = params[0];
 
     	if (methodName.equals("create"))
         {
     		createFile(fileName);
         }
     	else if (methodName.equals("get"))
     	{
     		getFile(fileName);
     	}
     	else if (methodName.equals("put") || methodName.equals("append"))
     	{
     		if (params.length < 2)
     		{
     			return;
     		}
 
     		String contents = command.substring(methodName.length()+1);
 			contents += "\n";
 			
			int idx = contents.indexOf(" ");
			if (idx == -1) {
				contents = "\n";
			} else {
				contents = contents.substring(idx+1);
			}
			
     		if (methodName.equals("put"))
     		{
     			putFile(fileName, contents);
     		}
     		else
     		{
     			appendToFile(fileName, contents);
     		}
     	}
     	else if (methodName.equals("delete"))
     	{
     		deleteFile(fileName);
     	}
     }	
 
 	// some helpers for transaction
 	private String readAllLines(String filename)  {
 		try {
 			PersistentStorageReader psr = m_node.getReader(filename);
 			return readAll(psr);
 		} catch (FileNotFoundException e) {
 			return null;
 		}
 
 	}
 
 	
 	private String readAll(PersistentStorageReader psr) {
 		StringBuffer sb = new StringBuffer();
 
 		String line;
 		try {
 			while ((line=psr.readLine()) != null) {
 				sb.append(line);
 				sb.append('\n');
 			}
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 		return sb.toString();
 	}
 	
 
 	/**
 	 * Sub classes will call this method in order to store files on disk. it already takes care of crashes
 	 * @param filename
 	 * @param contents
 	 * @throws IOException
 	 */
 	public void updateFileContents(String filename, String contents, boolean append) throws IOException {
 		try {
 			// read old file
 			String oldFile = readAllLines(filename);
 
 			// write old to backup
 			PersistentStorageWriter psw_bck = m_node.getWriter(".temp", false);
 			psw_bck.write(filename + "\n" + oldFile);
 			psw_bck.close();
 
 			// update new
 			PersistentStorageWriter psw = m_node.getWriter(filename, append);
 			psw.write(contents);
 			psw.close();
 
 			// delete temporary file
 			PersistentStorageWriter f = m_node.getWriter(TEMP_FILE, false);
 			f.delete();
 		} catch (IOException e) {
 			throw e;
 		}
 	}
 	
 
 	public void appendFileContents(String filename, String contents) throws IOException {
 		try {
 			// update new
 			PersistentStorageWriter psw = m_node.getWriter(filename, true);
 			psw.write(contents);
 			psw.close();
 		} catch (IOException e) {
 			throw e;
 		}
 	}
 
 	
 	private String readFileContents(String fileName)
 			throws FileNotFoundException, IOException {
 		BufferedReader bufferedReader = m_node.getReader(fileName);
 		
 		StringBuffer contents = new StringBuffer();
 		
 		String line = bufferedReader.readLine();  
 		while (line != null)
 		{
 			contents.append(line);
 			
 			line = bufferedReader.readLine();
 		}
 		
 		bufferedReader.close();
 		return contents.toString();
 	}
 	
 	
 	private void recoverTempFileFromCrash() {
 		try {
 			if (Utility.fileExists(m_node, TEMP_FILE)) {
 				PersistentStorageReader psw_bck = m_node.getReader(TEMP_FILE);
 				if (!psw_bck.ready()) {
 					PersistentStorageWriter f = m_node.getWriter(TEMP_FILE, false);
 					f.delete();
 				} else {
 					String filename = psw_bck.readLine();
 					psw_bck.close();
 
 					String oldContents = readAll(psw_bck);
 					PersistentStorageWriter psw = m_node.getWriter(filename, false);
 					psw.write(oldContents);
 					psw.close();
 				}
 			}
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 }
