 package edu.ucla.loni.server;
 
 //import gwtupload.server.UploadAction;
 //import gwtupload.server.exceptions.UploadActionException;
 
 import java.io.File;
 import java.util.Iterator;
 import java.util.List;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.apache.commons.fileupload.FileItem;
 import org.apache.commons.fileupload.FileItemFactory;
 import org.apache.commons.fileupload.disk.DiskFileItemFactory;
 import org.apache.commons.fileupload.servlet.ServletFileUpload;
 
 import edu.ucla.loni.shared.*;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletResponse;
 import java.io.IOException;
 import java.sql.Timestamp;
 
 @SuppressWarnings("serial")
 public class Upload extends HttpServlet//extends UploadAction
 {
 	/*@Override
 	  public String executeAction(HttpServletRequest request, List<FileItem> sessionFiles) throws UploadActionException {
 	    	String root = request.getParameter("root");
 		String response = "";
 	    	for (FileItem item : sessionFiles) {
 	    		if (false == item.isFormField()) {
 	    			try {
 	    				/// Create a new file based on the remote file name in the client
 	    				// String saveName = item.getName().replaceAll("[\\\\/><\\|\\s\"'{}()\\[\\]]+", "_");
 	    				// File file =new File("/tmp/" + saveName);
 	          
 	    				/// Create a temporary file placed in /tmp (only works in unix)
 	    				// File file = File.createTempFile("upload-", ".bin", new File("/tmp"));
 	          
 	    				/// Create a temporary file placed in the default system temp folder
 	    				String name = root + File.separatorChar + item.getName();
 	    				File file = new File(name);
 	    				item.write(file);
 	          
 	    				/// Send a customized message to the client.
 	    				response += "File saved as " + file.getAbsolutePath();
 
 	    			} catch (Exception e) {
 	    				throw new UploadActionException(e);
 	    			}
 	    		}
 	    	}
 	    
 	    	/// Remove files from session because we have a copy of them
 	    	removeSessionFileItems(request);
 	    
 	    	/// Send your customized message to the client.
 	    	return response;
 	  }*/
 	 /**
 	 * temporary folder where the pipefiles downloaded from client computer
 	 * will be stored at first
 	 */
 	private String temp_dir = "C:\\tmp";//"/tmp";
 	
 	/** 
 	 * JUST A COPY FROM FileServiceImpl
 	 * 
 	 * REASON WHY COPIED :: it was set as private inside FileServiceImpl,
 	 *                      so I decided not to change its visibility...
 	 * 
 	 * Gets the directoryID of the root directory by selecting it from the database,
 	 * inserts the directory into the database if needed
 	 * 
 	 * @param absolutePath absolute path of the root directory  
 	 * @return directoryID of the root directory
 	 */
 	private int getDirectoryId(String absolutePath) throws Exception{
 		int ret = Database.selectDirectory(absolutePath);
 		if(ret == -1){
 			Database.insertDirectory(absolutePath);
 			ret = Database.selectDirectory(absolutePath);
 		}
 		return ret;
 	}
 	
 	public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
 	{
 		String rootDir = "";
 		if ( ServletFileUpload.isMultipartContent( req ) )
 		{
 			// Create a factory for disk-based file items
 			FileItemFactory factory = new DiskFileItemFactory();
 			// Create a new file upload handler
 			ServletFileUpload upload = new ServletFileUpload( factory );
 			try
 			{
 				@SuppressWarnings("unchecked")
 				List<FileItem> items = upload.parseRequest( req );
 				for ( FileItem item : items )
 				{
 					if ( item.isFormField() )
 					{
 						//if it is not file, than process it separatelly
 						//in this case two types of parameters will be supplied before all
 						//files are supplied -- absolute path of library where all files need
 						//to be placed in (i.e. currently it is just abs path of CraniumLibrary on the server side)
 						//and the second parameter is a String that contains URLs
 						if( item.getFieldName().compareTo("specify name of library :") == 0 )
 						{
 							rootDir = item.getString();
 							res.getWriter().println("rootDIR = " + rootDir);
 						}
 						else if( item.getFieldName().compareTo("specify addresses of URLs :") == 0 )
 						{
 							res.getWriter().println(item.getString());
 						}
 						continue;
 					}
 					else
 					{
 						res.getWriter().println("FieldName = " + item.getFieldName() + " , FileName = " + item.getName() + " , size = " + item.getSize());
 					}
 					String fileName = item.getName();
 					File uploadedFile = new File( temp_dir, fileName );
 					if ( uploadedFile.createNewFile() )
 					{
 						item.write( uploadedFile );
 						res.setStatus( HttpServletResponse.SC_CREATED );
 					}
 					else
 					{
 						throw new IOException( "The file already exists" );
 					}
 					//analyze XML of this file
 					Pipefile pipe = ServerUtils.parseFile(uploadedFile);
 					// Get old and new absolute path directory
 					String oldAbsolutePath = pipe.absolutePath;
 					String newAbsolutePath = ServerUtils.newAbsolutePath(pipe.absolutePath, pipe.packageName, pipe.type);
 					//update actual name of file from temp_name to real_name stored inside pipe variable
 					newAbsolutePath = ServerUtils.extractDirName(newAbsolutePath) + File.separatorChar + pipe.name + ".pipe";
 					//
 					if( rootDir == "" )
 					{
 						//ideally separate parameter should arrive that tells exactly the path to the library
 						//if for some reason, it did not => just peek the first library in the table DIRECTORIES
 						//
 						//so if rootDir is empty string by the time first file or URL start getting processed
 						//that means parameter that sets library was lost or not specified by the client
 						rootDir = Database.getRootDir();
 						if( rootDir == "" )
 						{
 							throw new IOException("rootDir has not been found.");
 						}
 					}
 					newAbsolutePath = rootDir + newAbsolutePath;
 					//
 					//file descriptor of the actual file to be saved in appropriate package
 					File dest = new File(newAbsolutePath);
 					// If the destination directory does not exist, create it and necessary parent directories
 					File destDir = dest.getParentFile();
 					if (destDir.exists() == false)
 					{
 						boolean success = destDir.mkdirs();
 						if (!success)
 						{
 							throw new Exception("Destination folders could not be created");
 						}
 					}
 					//move file from temp dir to the actual dir
 					boolean success = uploadedFile.renameTo(dest);
 					if(success == false)
 					{
 						throw new Exception("File could not be moved :: ");
 					}
 					//update database
 					//get dir id
 					int dirId = getDirectoryId(rootDir);
 					//get timestamp
 					Timestamp fs_lastModified = new Timestamp(dest.lastModified());
 					//insert file
 					Database.insertPipefile(dirId, ServerUtils.parseFile(dest), fs_lastModified);
 				}
 			}
 			catch ( Exception e )
 			{
 				res.sendError( HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
 				"An error occurred while creating the file = " + e.getMessage() );
 			}
 		}
 		else
 		{
 		res.sendError( HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE,
		"Request contents type is not supported by the servlet = ");
 		}
 		// If uploading from a URL
 		// Get the file from the URL
 		// Add it to the filesystem
 		// Update the database
 		// If uploading a folder or files
 		// For each file
 		// Add it to the filesystem
 		// Update the database
 	}
 	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
 	{
 		doPost(req, resp);
 	}
 }
