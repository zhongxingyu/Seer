 /*===========================================================================
   Copyright (C) 2011 by the Okapi Framework contributors
 -----------------------------------------------------------------------------
   This library is free software; you can redistribute it and/or modify it 
   under the terms of the GNU Lesser General Public License as published by 
   the Free Software Foundation; either version 2.1 of the License, or (at 
   your option) any later version.
 
   This library is distributed in the hope that it will be useful, but 
   WITHOUT ANY WARRANTY; without even the implied warranty of 
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser 
   General Public License for more details.
 
   You should have received a copy of the GNU Lesser General Public License 
   along with this library; if not, write to the Free Software Foundation, 
   Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 
   See also the full LGPL text here: http://www.gnu.org/copyleft/lesser.html
 ===========================================================================*/
 
 package net.sf.okapi.lib.transifex;
 
 import java.io.BufferedReader;
 import java.io.DataOutputStream;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.io.UnsupportedEncodingException;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 
 import org.json.simple.JSONArray;
 import org.json.simple.JSONObject;
 import org.json.simple.parser.JSONParser;
 import org.json.simple.parser.ParseException;
 
 import net.sf.okapi.common.Base64;
 import net.sf.okapi.common.LocaleId;
 import net.sf.okapi.common.Util;
 import net.sf.okapi.common.exceptions.OkapiIOException;
 
 /**
  * Basic Transifex client allowing to create and maintain Transifex project from a java application.
  */
 public class TransifexClient {
 
 	private static final String HYPHENS = "--";
 	private static final String BOUNDARY = "oIkPaApKiO";
 	private static final String LINEBREAK = System.getProperty("line.separator");
 	private static final int RESCODE_OK = 200; 
 	private static final int RESCODE_CREATED = 201; 
 	private static final int MAXBUFFERSIZE = 1024*8; 
 
 	private String host;
 	private String project;
 	private String credentials;
 	private String username;
 	private JSONParser parser;
 
 	public TransifexClient (String host) {
 		setHost(host);
 		parser = new JSONParser();
 	}
 	
 	public void setHost (String host) {
 		this.host = Util.ensureSeparator(host, true);
 	}
 	
 	public String getHost () {
 		return host;
 	}
 	
 	public void setProject (String project) {
 		this.project = project;
 	}
 	
 	public String getProject () {
 		return project;
 	}
 	
 	public void setCredentials (String username,
 		String password)
 	{
 		this.username = username;
 		credentials = "Basic " + Base64.encodeString(username+":"+password);
 	}
 
 	/**
 	 * Creates a new project if one does not exists already.
 	 * If the project exists already it is updated.
 	 * @param projectId the project Id.
 	 * @param name the name of the project.
 	 * @param shortDescription a short description.
 	 * @param longDescription a longer description.
 	 * @return an array of strings: On success 0=the project id.
 	 * On error 0=null, 1=Error code and message.
 	 */
 	public String[] createProject (String projectId,
 		String name,
 		String shortDescription,
 		String longDescription)
 	{
 		String[] res = new String[2];
 		try {
 			URL url = new URL(host + "api/project/"+projectId+"/");
 			HttpURLConnection conn = createConnection(url, "POST");
 			String data = String.format("{"
 				+ "\"slug\": \"%s\", "
 				+ "\"name\": \"%s\", "
 				+ "\"maintainers\": \"%s\" "
 				+ "}",
 				projectId, name, username);
 			writeData(conn, data);
 			
 			// Execute
 			int code = conn.getResponseCode();
 			if ( code == RESCODE_CREATED ) {
 //				conn.disconnect();
 //				url = new URL(host + "api/project/"+projectId+"/");
 //				conn = createConnection(url, "PUT");
 //				data = String.format("{\"description\": \"%s\","
 //					+ "\"created\": \"2011-02-18 12:53:51\","
 //					+ "\"slug\": \"%s\","
 //					+ "\"owner\": {\"username\": \"%s\", \"email\": \"%s\"},"
 //					+ "\"anyone_submit\": false,"
 //					+ "\"long_description\": \"%s\","
 //					+ "\"resources\": [],"
 //					+ "\"name\": \"%s\"}, " 
 //					+ "\"maintainer\": \"%s\"}",
 //					shortDescription, projectId, username, email, longDescription, name, username);
 //				writeData(conn, data);
 //				res = conn.getResponseCode();
 				project = projectId;
 				res[0] = projectId;
 			}
 			else {
 				res[1] = String.format("Error %d ", code) + conn.getResponseMessage();
 			}
 		}
 		catch ( IOException e ) {
 			res[1] = e.getMessage();
 		}
 		return res;
 	}
 	
 	/**
 	 * Add a resource to the current project.
 	 * If the resource exists already it is updated.
 	 * @param poPath the full path of the PO file to add.
 	 * @param srcLoc the locale of the source text.
 	 * @param resourceFile filename of the resource (must be the same for all languages)
 	 * or null to use the filename of the path.
 	 * @return An array of strings: On success 0=redirect path, 1=resource Id.
 	 * On error: 0=null, 1=Error code and message.
 	 */
 	public String[] putSourceResource (String poPath,
 		LocaleId srcLoc,
 		String resourceFile)
 	{
 		String[] res = uploadFile(poPath, srcLoc.toPOSIXLocaleId(), resourceFile);
 		if ( res[0] == null ) {
 			return res; // Could not upload the file
 		}
 		return extractSourceFromStoredFile(res[0], srcLoc.toPOSIXLocaleId());
 	}
 	
 	public String[] putTargetResource (String poPath,
 		LocaleId srcLoc,
 		String resourceId,
 		String resourceFile)
 	{
 		String[] res = uploadFile(poPath, srcLoc.toPOSIXLocaleId(), resourceFile);
 		if ( res[0] == null ) {
 			return res; // Could not upload the file
 		}
 		return extractTargetFromStoredFile(res[0], srcLoc.toPOSIXLocaleId(), resourceId);
 	}
 	
 	
 	/**
 	 * Pulls a resource from the current project.
 	 * @param resourceId the id of the resource to pull.
 	 * @param trgLoc the target locale of the resource to pull.
 	 * @param outputPath the output path of the resulting PO file.
 	 * @return an array of strings: On success 0=the output path, 1=the resource id.
 	 * On error 0=null, 1=the code and error message.
 	 */
 	public String[] getResource (String resourceId,
 		LocaleId trgLoc,
 		String outputPath)
 	{
 		String[] res = null;
 		try {
 			res = retrieveFile(resourceId, trgLoc.toPOSIXLocaleId());
 			if ( res[0] == null ) {
 				return res;
 			}
 			// Else: save the PO file
 			Util.createDirectories(outputPath);
 			OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(outputPath), "UTF-8");
 			osw.write(res[0]);
 			osw.close();
 			res[0] = outputPath;
 			res[1] = resourceId;
 		}
 		catch ( IOException e ) {
 			res[0] = null;
 			res[1] = e.getMessage();
 		}
 		return res;
 	}
 	
 	/**
 	 * Deletes a stored file from the project's storage.
 	 * @param uuid the UUID of the file to delete.
 	 * @return response code.
 	 */
 	public int deleteStoredFile (String uuid) {
 		try {
 			URL url = new URL(host + "api/storage/"+uuid);
 			HttpURLConnection conn = (HttpURLConnection)url.openConnection();
 			conn.setRequestMethod("DELETE");
 			conn.setRequestProperty("Authorization", credentials);
 			// Execute
 			return conn.getResponseCode();
 		}
 		catch ( MalformedURLException e ) {
 			throw new OkapiIOException("Error deleting file.", e);
 		}
 		catch ( IOException e ) {
 			throw new OkapiIOException("Error deleting file.", e);
 		}
 	}
 	
 	/**
 	 * Retrieves a file.
 	 * @param resId the resource ID of the file to retrieve.
 	 * @param lang the language code of the file to retrieve.
 	 * @return an array of strings: On success 0=the content of the file, 1=the resource id.
 	 * On error 0=null, 1=the code and error messge.
 	 */
 	public String[] retrieveFile (String resId,
 		String lang)
 	{
 		String[] res = new String[2];
 		try {
 			URL url = new URL(host + String.format("api/project/%s/resource/%s/%s/file/",
 				project, resId, lang));
 			HttpURLConnection conn = (HttpURLConnection)url.openConnection();
 			conn.setRequestProperty("Authorization", credentials);
 
 			int code = conn.getResponseCode();
 			if ( code == RESCODE_OK ) {
 				res[0] = readResponse(conn);
 				res[1] = resId;
 			}
 			else {
 				res[1] = String.format("Error %d ", code) + conn.getResponseMessage(); 
 			}
 		}
 		catch ( MalformedURLException e ) {
 			throw new OkapiIOException("Error retrieving file.", e);
 		}
 		catch ( IOException e ) {
 			throw new OkapiIOException("Error retrieving file.", e);
 		}
 		
 		return res;
 	}
 
 	/**
 	 * Extracts a source file from the the storage into the repository.
 	 * <p>The file in storage is removed from storage by Transifex.
 	 * @param uuid the UUID of the file to extract.
 	 * @return String array: On success 0=redirect path to the extracted file, 1=id.
 	 * On error: 0=null, 1=Error code and message.
 	 */
 	public String[] extractSourceFromStoredFile (String uuid,
 		String language)
 	{
 		String[] res = new String[2];
 		try {
 			URL url = new URL(host + "api/project/" + project + "/files/");
 			HttpURLConnection conn = createConnection(url, "POST");
 			String data = String.format("{\"uuid\": \"%s\"}", uuid); //, slug=\"%s\"}", uuid, slug));
 			writeData(conn, data);
 
 			// {"strings_added": 0, "strings_updated": 0, "redirect": "/projects/p/Icaria/resource/test03pot/"}
 			int code = conn.getResponseCode();
 			if ( code == RESCODE_OK ) {
 			    JSONObject object = (JSONObject)parser.parse(new InputStreamReader(conn.getInputStream(), "UTF-8"));
 			    res[0] = res[1] = (String)object.get("redirect");
 			    if ( res[1].endsWith("/") ) res[1] = res[1].substring(0, res[1].length()-1);
 			    res[1] = Util.getFilename(res[1], false);
 			}
 			else {
 				res[1] = String.format("Error %d ", code) + conn.getResponseMessage();
 			}
 		}
 		catch ( MalformedURLException e ) {
 			res[1] = e.getMessage();
 		}
 		catch ( IOException e ) {
 			res[1] = e.getMessage();
 		}
 		catch ( ParseException e ) {
 			res[1] = e.getMessage();
 		}
 		
 		return res;
 	}
 
 	public String[] extractTargetFromStoredFile (String uuid,
 		String language,
 		String resourceId)
 	{
 		String[] res = new String[2];
 		try {
 			// %(hostname)s/api/project/%(project)s/resource/%(resource)s/%(language)s/
 			URL url = new URL(host + "api/project/" + project + "/resource/" + resourceId + "/" + language);
 			HttpURLConnection conn = createConnection(url, "PUT");
 			
 			String data = String.format("{\"uuid\": \"%s\"}", uuid);
 			writeData(conn, data);
 
 			int code = conn.getResponseCode();
 			if ( code == RESCODE_OK ) {
 			    JSONObject object = (JSONObject)parser.parse(new InputStreamReader(conn.getInputStream(), "UTF-8"));
 			    res[0] = res[1] = (String)object.get("redirect");
 			    if ( res[1].endsWith("/") ) res[1] = res[1].substring(0, res[1].length()-1);
 			    res[1] = Util.getFilename(res[1], false);
 			}
 			else {
 				res[1] = String.format("Error %d ", code) + conn.getResponseMessage();
 			}
 		}
 		catch ( MalformedURLException e ) {
 			res[1] = e.getMessage();
 		}
 		catch ( IOException e ) {
 			res[1] = e.getMessage();
 		}
 		catch ( ParseException e ) {
 			res[1] = e.getMessage();
 		}
 		
 		return res;
 	}
 
 	private HttpURLConnection createConnection (URL url,
 		String requestType)
 		throws IOException
 	{
 		HttpURLConnection conn = null;
 
 		conn = (HttpURLConnection)url.openConnection();
 		conn.setRequestMethod(requestType);
 		conn.setDoOutput(true);
 	    conn.setDoInput(true);
 	    conn.setAllowUserInteraction(false);
 		conn.setRequestProperty("Authorization", credentials);
 		conn.setRequestProperty("Content-Type", "application/json");
 
 		return conn;
 	}
 	
 	/**
 	 * Upload a file to the storage.
 	 * <p>The file must be a POT file, in UTF-8 without BOM.
 	 * @param path the path of the POT file to upload. 
 	 * @param resourceFile filename of the resource (must be the same for all languages)
 	 * or null to use the filename of the path.
 	 * @return an array of strings: On success 0=UUID, 1=name, 2=Resource id.
 	 * On error 0=null, 1=error code and message, 2=null.
 	 */
 	public String[] uploadFile (String path,
 		String language,
 		String resourceFile)
 	{
 		FileInputStream fis = null;
 		DataOutputStream dos = null;
 		String[] res = new String[3];
 		try {
 			// Set the default value for the resource file if needed
 			if ( resourceFile == null ) {
 				resourceFile = Util.getFilename(path, true);
 			}
 			
 			// Start by opening the input, making sure it exists
 			fis = new FileInputStream(path);
 			
 			URL url = new URL(host + "api/storage/");
 			HttpURLConnection conn = (HttpURLConnection)url.openConnection();
 			conn.setRequestMethod("POST");
 			conn.setDoOutput(true);
 		    conn.setDoInput(true);
 		    conn.setAllowUserInteraction(false);
 			conn.setRequestProperty("Authorization", credentials);
 			conn.setRequestProperty("Content-Type", "multipart/form-data; boundary="+BOUNDARY);
 
 			dos = new DataOutputStream(conn.getOutputStream());
 
 			addFormDataPart("resource", resourceFile, dos);
 			addFormDataPart("language", language, dos);
 			
 			dos.writeBytes(HYPHENS + BOUNDARY + LINEBREAK);
 			dos.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";"
 				+ " filename=\"" + resourceFile + "\"" + LINEBREAK);
 			dos.writeBytes("Content-Type: application/octet-stream"
 				+ LINEBREAK + LINEBREAK);
 			
 			int bytesAvailable = fis.available();
 			int bufferSize = Math.min(bytesAvailable, MAXBUFFERSIZE);
 			byte[] buffer = new byte[bufferSize];
 
 			// Read and write the file
 			int bytesRead = fis.read(buffer, 0, bufferSize);
 			while ( bytesRead > 0 ) {
 				dos.write(buffer, 0, bufferSize);
 				bytesAvailable = fis.available();
 			    bufferSize = Math.min(bytesAvailable, MAXBUFFERSIZE);
 			    bytesRead = fis.read(buffer, 0, bufferSize);
 			}
 
 			// Close form data
 			dos.writeBytes(LINEBREAK);
 			dos.writeBytes(HYPHENS + BOUNDARY + HYPHENS + LINEBREAK);
 			fis.close(); fis = null;
 			dos.flush();
 			dos.close();
 
 			int code =  conn.getResponseCode();
 			if ( code == RESCODE_OK ) {
				String str = readResponse(conn);
			    JSONObject object = (JSONObject)parser.parse(str);
 			    JSONArray files = (JSONArray)object.get("files");
 			    if ( files.size() == 1 ) {
 			    	JSONObject file = (JSONObject)files.get(0);
 			    	res[0] = (String)file.get("uuid");
 			    	res[1] = (String)file.get("name");
 			    	res[2] = (String)file.get("id");
 			    }
			    else {
			    	res[1] = String.format("Success returned, but no file description returned. Response='%s'", str);
			    }
 			}
 			else {
 				res[1] = String.format("Error %d ", code) + conn.getResponseMessage();
 			}
 		}
 		catch ( MalformedURLException e ) {
 			res[1] = e.getMessage();
 		}
 		catch ( IOException e ) {
 			res[1] = e.getMessage();
 		}
 		catch ( ParseException e ) {
 			res[1] = e.getMessage();
 		}
 		return res;
 	}
 
 	private void addFormDataPart (String name,
 		String value,
 		DataOutputStream dos)
 		throws IOException
 	{
 		dos.writeBytes(HYPHENS + BOUNDARY + LINEBREAK);
 		dos.writeBytes("Content-Disposition: form-data; name=\"" + name + "\""
 			+ LINEBREAK + LINEBREAK);
 		dos.writeBytes(value + LINEBREAK);
 	}
 
 	private String readResponse (HttpURLConnection conn)
 		throws UnsupportedEncodingException, IOException
 	{
 		StringBuilder tmp = new StringBuilder();
 		BufferedReader reader = null;
 		try {
 			reader = new BufferedReader(
 				new InputStreamReader(conn.getInputStream(), "UTF-8"));
 			String line;
 			while ( (line = reader.readLine()) != null ) {
 				tmp.append(line+"\n");
 			}
 		}
 		finally {
 			if ( reader != null ) {
 				reader.close();
 			}
 		}
 		return tmp.toString();
 	}
 	
 	private void writeData (HttpURLConnection conn,
 		String data)
 		throws IOException
 	{
 		DataOutputStream dos = null;
 		try {
 			dos = new DataOutputStream(conn.getOutputStream());
 			dos.writeBytes(data);
 			dos.flush();
 		}
 		finally {
 			dos.close();
 		}
 	}
 	
 }
 
 //StringBuilder tmp = new StringBuilder();
 //String line;
 //BufferedReader reader = new BufferedReader(
 //	new InputStreamReader(conn.getInputStream(), "UTF-8"));
 //while ( (line = reader.readLine()) != null ) {
 //	tmp.append(line);
 //}
 //reader.close();
