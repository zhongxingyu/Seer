 package newsrack.web;
 
 import java.io.File;
 import java.io.Reader;
 import java.io.FileInputStream;
 import java.io.InputStream;
 import java.io.PrintWriter;
 import java.util.Map;
 
 import javax.servlet.http.HttpServletResponse;
 
 import com.opensymphony.xwork2.Action;
 import com.opensymphony.xwork2.ActionSupport;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import org.apache.struts2.interceptor.ServletResponseAware;
 
 import newsrack.GlobalConstants;
 import newsrack.user.User;
 import newsrack.util.IOUtils;
 
 /**
  * class <code>FileActions</code> supports various file-related actions: 
  * - upload, edit, save, create-new, display, rename, download, copy
  */
 public class FileAction extends BaseAction implements ServletResponseAware
 {
    private static final Log _log = LogFactory.getLog(FileAction.class); /* Logger for this action class */
 
 	private String _fileContent;
 	private String _file;
 	private File   _uploadedFile;
 	private String _uploadedContentType;
 
    private HttpServletResponse _response;   // The response object
 
    public void setServletResponse(HttpServletResponse r) { _response = r; }
 
 	public String getFile() { return _file; }
 	public String getFileContent() { return _fileContent; }
 
 		// The FileUpload Interceptor will set up these values below!
	public void setUploadedFile(File f) { _log.info("Setting uploaded file to: " + f); _uploadedFile = f; }
	public void setUploadedContentType(String ct) { _log.info("Setting uploaded content type to: " + ct); _uploadedContentType = ct; }
	public void setUploadedFileName(String n) { _log.info("Seting uploaded file name to: " + n); _file = n; }
	public void setSubmit(Object o) { _log.info("Hmm !!! " + o); }
 
 	private boolean haveValidParams()
 	{
 		_file = getParam("file");
 		if ((_file == null) || _file.equals("")) {
 			addActionError(getText("error.missing.filename"));
 			return false;
 		}
 
 		return true;
 	}
 
 	public String edit()
 	{
 		if (!haveValidParams())
 			return Action.ERROR;
 
 		try {
 				// Get the input stream for the file and read in the entire content
 			Reader       fr   = _user.getFileReader(_file);
 			StringBuffer csb  = new StringBuffer();
 			char[]       cbuf = new char[256];
 			int          n    = 0;
 			while ((n = fr.read(cbuf)) != -1) {
 				csb.append(cbuf, 0, n);
 			}
 			fr.close();
 
 			_fileContent = csb.toString();
 			return Action.SUCCESS;
 		}
 		catch (Exception e) {
 			_log.error("Error reading file " + _file + " for user " + _user.getUid(), e);
 			addActionError(getText("error.file.read", new String[]{_file, _user.getUid()}));
 			return Action.ERROR;
 		}
 	}
 
 	public String upload()
 	{
 		if (!haveValidParams())
 			return Action.ERROR;
 
 		try {
 			// FIXME: How do I get the file input stream??
 			_user.uploadFile(new FileInputStream(_uploadedFile), _file);
 			addActionMessage(getText("msg.file.uploaded"));
 			return Action.SUCCESS;
 		}
 		catch (java.lang.Exception e) {
 			addActionError(getText("error.file.upload", new String[]{_file}));
 			return Action.ERROR;
 		}
 	}
 
 	public String display()
 	{
 		if (!haveValidParams())
 			return Action.ERROR;
 
 			/* Set output content type to 'text/plain' to prevent the
 			 * browser from interpreting the xml content */
 		_response.setContentType("text/plain; charset=UTF-8");
 
 			/* Get the input and output streams and copy input to output */
 		String owner = getParam("owner");
 		try {
 			if (owner == null)
 				IOUtils.copyInputToOutput(_user.getInputStream(_file), _response.getOutputStream());
 			else
 				IOUtils.copyInputToOutput(_user.getInputStream(owner, _file), _response.getOutputStream());
 		}
 		catch (Exception e) {
 			_log.error("Exception", e);
 			addActionError(getText("error.file.display", new String[]{_file}));
 			return Action.ERROR;
 		}
 
 			/* No need to return any mapping here */
 		return null;
 	}
 
 	private String storeFileContent(String content)
 	{
 		try {
 				// Store content
 			PrintWriter pw = IOUtils.getUTF8Writer(_user.getOutputStream(_file));
 			pw.print(content);
 			pw.close();
 
 				// Invalidate issues -- they need to be re-validated after content changes!
 			_user.invalidateAllIssues();
 
 			addActionMessage(getText("msg.file.saved"));
 			return Action.SUCCESS;
 		}
 		catch (Exception e) {
 			addActionError(getText("error.file.save", new String[]{_file}));
 			return Action.ERROR;
 		}
 	}
 
 	public String save()
 	{
 		if (!haveValidParams())
 			return Action.ERROR;
 
 		return storeFileContent(getParam("fileContent"));
 	}
 
 	public String createNew()
 	{
 		_fileContent = getParam("fileContent");
 		if (!haveValidParams())
 			return Action.ERROR;
 
 		try {
 			_user.addFile(_file);
 			return storeFileContent(_fileContent);
 		}
 		catch (EditProfileException e) {
 			_log.error(e);
 			addActionError(getText("error.file.create", new String[]{_file}));
 			return Action.ERROR;
 		}
 	}
 
 	public String rename()
 	{
 		String name    = getParam("name");
 		String newName = getParam("newname");
 		if ((name == null) || name.equals("") || (newName == null) || newName.equals("")) {
 			_log.error("RenameFile: File name(s) not specified");
 			addActionError(getText("missing.filename"));
 			return Action.ERROR;
 		}
 
 		try {
 			_user.renameFile(name, newName);
 			addActionMessage(getText("msg.file.renamed"));
 			return Action.SUCCESS;
 		}
 		catch (Exception e) {
 			_log.error("Error renaming file!" + e.toString(), e);
 			addActionError(getText("internal.app.error"));
 			return Action.ERROR;
 		}
 	}
 	
 	public String download()
 	{
 		if (!haveValidParams())
 			return Action.ERROR;
 
 		String owner = getParam("owner");
 		if (_user.canAccessFile(owner, _file)) {
 			try {
 				InputStream inStr = _user.getInputStream(owner, _file);
 					/* Set output content type to 'octet-stream' to force a
 					 * download dialog on the user's browser */
 				_response.setContentType("application/octet-stream");
 				_response.setHeader("Content-Disposition","attachment; filename=\"" + _file + "\"");
 				IOUtils.copyInputToOutput(inStr, _response.getOutputStream());
 
 					/* Don't return anything!  Stay on the same mapping page since
 					 * a response has already been sent back */
 				return null;
 			}
 			catch(Exception e) {
 				_log.error("Exception downloading file.", e);
 				addActionError(getText("internal.app.error"));
 				return Action.ERROR;
 			}
 		}
 		else {
 			_log.error("Permission denied: " + _user.getUid() + " cannot access file " + _file + " owned by user " + owner);
 			addActionError(getText("error.file.permission.denied", new String[]{_file, owner}));
 			return Action.ERROR;
 		}
 	}
 
 	public String copy()
 	{
 		if (!haveValidParams())
 			return Action.ERROR;
 
 		String owner = getParam("owner");
 		if (_user.canAccessFile(owner, _file)) {
 			try {
 				InputStream inStr = _user.getInputStream(owner, _file);
 				IOUtils.copyInputToOutput(inStr, _user.getOutputStream(_file));
 				_user.addFile(_file);
 
 					// Invalidate issues -- they need to be re-validated with addition of new content
 				_user.invalidateAllIssues();
 
 				addActionMessage(getText("msg.file.copied"));
 				return Action.SUCCESS;
 			}
 			catch(Exception e) {
 				_log.error("Exception copying file.", e);
 				addActionError(getText("internal.app.error"));
 				return Action.ERROR;
 			}
 		}
 		else {
 			_log.error("Permission denied: " + _user.getUid() + " cannot access file " + _file + " owned by user " + owner);
 			addActionError(getText("error.file.permission.denied", new String[]{_file, owner}));
 			return Action.ERROR;
 		}
 	}
 }
