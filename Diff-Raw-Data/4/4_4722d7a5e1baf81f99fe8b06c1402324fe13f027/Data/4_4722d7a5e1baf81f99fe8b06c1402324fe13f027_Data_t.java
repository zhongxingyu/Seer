 package controllers;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipOutputStream;
 
 import models.AppProps;
 import models.OutItem;
 import models.OutResult;
 import models.Repo;
 
 import org.apache.commons.io.FileUtils;
 
 import play.Logger;
 import play.cache.Cache;
 import play.data.Upload;
 import play.libs.IO;
import play.libs.MimeTypes;
 import play.mvc.Http.Request;
 import play.mvc.Scope.Session;
 import play.mvc.Util;
 import play.templates.JavaExtensions;
 import util.JsonHelper;
 import util.Utils;
 import exception.QuickException;
 
 
 /**
  * Controller that handles data download and upload 
  * 
  * @author Paolo Di Tommaso
  *
  */
 public class Data extends CommonController {
 
 	/** 
 	 * characters that have to be used into command line and file names
 	 */
 	public static final char[] INVALID_CHARS = { ';','&','`',':','*','?','$','(',')','{','}','[',']','<','>','|' };
 	
 	
 	/**
 	 * This method let to download any file placed in the application 
 	 * data folder 
 	 * 
 	 * @param path
 	 */
 	public static void resource(String path) {
 		assertNotEmpty(path, "Missing 'path' argument on #resource action");
 
 		renderStaticResponse();
		String content = MimeTypes.getMimeType(path);
		response.contentType = content;
 		renderFile(AppProps.WORKSPACE_FOLDER, path);
 	}
 	
 	/**
 	 * Create a temporary zip file with all generated content and download it
 	 * 
 	 * @param rid the request identifier
 	 * @throws IOException 
 	 */
 	public static void zip( String rid ) throws IOException {
 		assertNotEmpty(rid, "Missing 'rid' argument on #zip action");
 		
 		Repo repo = new Repo(rid);
 		if( !repo.hasResult() ) {
 			notFound(String.format("The requested download is not available (%s) ", rid));
 			return;
 		}
 		
 		OutResult result = repo.getResult();
 		File zip = File.createTempFile("download", ".zip", repo.getFile());
 		// get the list of files to download 
 		List<File> files = new ArrayList<File>( result.getItems().size() );
 		for( OutItem item : result.getItems() ) { 
 			files.add(item.file);
 		}
 		// zip them and download
 		zipThemAll(files, zip, null);
 
 		String attachName = String.format("tcoffee-all-files-%s.zip",rid);
 		response.setHeader("Content-Disposition", "attachment; filename=\"" + attachName+ "\"");
 		renderStaticResponse();
 		renderBinary(zip);
 	}
 	
 	/**
 	 * Handy method to zip all datafolder content and download it
 	 * 
 	 * @param rid request identifier
 	 */
 	public static void zipDataFolder( String rid ) throws IOException { 
 		assertNotEmpty(rid, "Missing 'rid' argument on #zipDataFolder action");
 
 		File folder = new File(AppProps.instance().getDataPath(), rid);
 		if( !folder.exists() ) { 
 			notFound("Data path '%s' does not exist on the server", folder);
 		}
 		
 		Collection allFiles = FileUtils.listFiles(folder, null, true);
 		File zip = File.createTempFile("folder", ".zip");
 		
 		String parent = folder.getAbsolutePath();
 		if( !parent.endsWith("/")) { 
 			parent += "/";
 		}
 		zipThemAll(allFiles, zip, parent);
 		
 		String attachName = String.format("all-data-files-%s.zip",rid);
 		response.setHeader("Content-Disposition", "attachment; filename=\"" + attachName+ "\"");
 		renderStaticResponse();
 		renderBinary(zip);
 		
 	}
 
 	/**
 	 * Zip the collections of files as a unique zip file
 	 * 
 	 * @param items a collections of files to be zipped 
 	 * @param targetZip the target zip file
 	 * @param basePath if <code>null</code> all files are zipped in a plain archive (only file name is used) 
 	 * otherwise this string value will be considered the prefix of the files absulte path to be removed 
 	 */
 	static void zipThemAll( Collection<File> items, File targetZip, String basePath ) {
 
 		try {
 			ZipOutputStream zip = new ZipOutputStream(new FileOutputStream(targetZip));
 			
 			for( File item : items ) { 
 				if( item==null || !item.exists() ) { continue; }
 				
 				// add a new zip entry
 				String entryName; 
 				if( basePath == null ) { 
 					/* use just the file name as entry name (w/o path information) */
 					entryName = item.getName();
 				}
 				else { 
 					/* make relative to the basePath */
 					entryName = item.getAbsolutePath();
 					if( entryName.startsWith(basePath)) { 
 						entryName = entryName.substring(basePath.length());
 					}
 				}
 				
 				
 				zip.putNextEntry( new ZipEntry(entryName) );
 				
 				// append the file content
 				FileInputStream in = new FileInputStream(item);
 				IO.copy(in, zip);
 	 
 				// Complete the entry 
 				zip.closeEntry(); 
 			}
 			
 			zip.close();					
 		}
 		catch (IOException e) {
 			throw new QuickException(e, "Unable to zip content to file: '%s'", targetZip);
 		}
 	} 
 		
 	/**
 	 * Manage upload of the input file 
 	 * 
 	 * NOTE: this use the legacy mechanism
 	 * 
 	 * @param name the file name that is being uploaded
 	 */
 	public static void upload(String name) {
 		assertNotEmpty(name, "Missing 'name' argument on #upload action");
 		
 		/* default error result */
 		String ERROR = "{success:false}";
 		
 		/* 
 		 * here it is the uploaded file 
 		 */
 		File file = params.get(name, File.class);
 		
 		/* uh oh something goes wrong .. */
 		if( file==null ) {
 			Logger.error("Ajax upload is null for field: '%s'", name);
 			renderText(ERROR);
 			return;
 		}
 		
 		/* error condition: wtf is the file ? */
 		if( !file.exists() ) {
 			Logger.error("Cannot find file for ajax upload field: '%s'", name);
 			renderText(ERROR);
 			return;
 		}
 
 		/* 
 		 * copy the uploaded content to a temporary file 
 		 * and return that name in the result to be stored in a hidden field
 		 */
 		try {
 			File temp = File.createTempFile("upload-", null);
 			// to create a temporary folder instead of a file delete and recreate it 
 			temp.delete();
 			temp.mkdir();
 			temp = new File(temp, file.getName());
 			
 			FileUtils.copyFile(file, temp);
 			String filename = temp.getAbsolutePath();
 			renderText(String.format("{success:true, name:'%s', path:'%s', size:'%s'}", 
 						file.getName(),
 						JavaExtensions.escapeJavaScript(filename),
 						FileUtils.byteCountToDisplaySize(temp.length())
 						));
 		}
 		catch( IOException e ) {
 			Logger.error(e, "Unable to copy temporary upload file: '%s'", file);
 			renderText(ERROR);
 		}
 		
 	}	
 	
 	
 	/* 
 	 * Upload mechanism for the 'advanced' T-coffee mode 
 	 */
 	static class AjaxUpload implements Serializable { 
 		String fileName;
 		File path;
 		
 		public AjaxUpload() { }
 		
 		public AjaxUpload( File file ) { 
 			fileName = file.getName();
 			path = file;
 		}
 		
 		public String getFileName() { return fileName; }
 		public File getPath() { return path; } 
 		
 		public String toString() {
 			return String.format("AjaxUpload[ %s -> %s ]", fileName, path );
 		} 
 		
 		public int hashCode() { 
 			int hash= Utils.hash();
 			hash = Utils.hash(hash, fileName);
 			return hash;
 		}
 		
 		public boolean equals( Object that ) { 
 			return 
 				Utils.isEqualsClass(this, that) && 
 				Utils.isEquals(this.fileName, ((AjaxUpload)that).fileName);
 		}
 	}
 	
 	
 	@Util
 	public static Set<AjaxUpload> getAjaxUploads() { 
 		String key = Session.current().getId() + "-ajaxuploads";
 		Set<AjaxUpload> result = (Set<AjaxUpload>) Cache.get(key);
 		if( result == null ) { 
 			result = new HashSet<AjaxUpload>();
 			Cache.set( key, result );
 		}
 		return result;
 	}
 	
 	@Util
 	public static void addAjaxUpload( String filename, File path ) { 
 		AjaxUpload item = new AjaxUpload();
 		item.fileName = filename;
 		item.path = path;
 		Set<AjaxUpload> set = getAjaxUploads();
 		if( set.contains(item) ) { 
 			set.remove(item);
 		}
 		set.add(item);
 	}
 	
 	public static void ajaxupload(String qqfile) { 
 
 		response.contentType = "text/html";  // <!-- also this reponse header is requried to make it work IE 
 		
 		/* 
 		 * Hack to handle upload from fucking IE7/8
 		 */
 		Upload __fileUpload = null;
 		if( "multipart/form-data".equals(request.contentType)) { 
 			List<Upload> __uploads = (List<Upload>) Request.current().args.get("__UPLOADS");
 			if( __uploads != null && __uploads.size()>0) { 
 				__fileUpload = __uploads.get(0);
 				qqfile = __fileUpload.getFileName();
 			}
 		}
 
 		
 		/* 
 		 * some integrity checks
 		 */
 		if( Utils.isEmpty(qqfile) ) { 
 			renderText(JsonHelper.error("The file name cannot be empty"));
 		}
 		
 		if( qqfile.startsWith("-") ) { 
 			renderText(JsonHelper.error("The file name cannot start with a minus (-) character."));
 		}
 		
 		for( char ch : INVALID_CHARS ) { 
 			if( qqfile.indexOf(ch) != -1 ) { 
 				String msg =  String.format("The file name cannot contain character '%s'",  ch);
 				renderText(JsonHelper.error(msg));
 			}
 		}
 		
 		/* check if already exists */
 		for( AjaxUpload upload : getAjaxUploads() ) { 
 			if( qqfile.equals( upload.fileName ) && upload.path.exists() ) { 
 				String msg =  String.format("A file with the same name '%s' already exist.",  qqfile);
 				renderText(JsonHelper.error(msg));
 			}
 		}
 	
 		
 		File tmpfile = null;
 		try  {
 			tmpfile = File.createTempFile("input_", null, AppProps.TEMP_PATH);
 			OutputStream out = new BufferedOutputStream(new FileOutputStream(tmpfile));
 			InputStream input = __fileUpload == null 
 							  ? request.body
 							  : __fileUpload.asStream();
 
 			IO.write(new BufferedInputStream(input), out);
 			// ^ Stream closed by the write method
 			
 			addAjaxUpload(qqfile, tmpfile);
 
 			renderText("{\"success\":true}");
 		}
 		catch( Exception e ) { 
 			Logger.error(e, "Unable to store ajax file upload to: '%s'; file name: '%s'", tmpfile, qqfile);
 			renderText( JsonHelper.error(e) );
 		}
 
 	}
 	
 
 }
