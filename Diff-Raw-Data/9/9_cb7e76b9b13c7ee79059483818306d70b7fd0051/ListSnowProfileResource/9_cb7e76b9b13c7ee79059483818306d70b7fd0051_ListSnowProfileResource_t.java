 package at.ac.dbisinformatik.snowprofile.web;
 
 /**
  * Copyright (c) 2012 Aleksandar Stojakovic
  * 
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  * 
  * The above copyright notice and this permission notice shall be included in all
  * copies or substantial portions of the Software.
  * 
  * The Software shall be used for Good, not Evil.
  * 
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  * SOFTWARE.
  * 
  */
 
 import java.io.IOException;
 import java.util.Iterator;
 import java.util.List;
 
 import org.apache.commons.fileupload.FileItem;
 import org.apache.commons.fileupload.disk.DiskFileItemFactory;
 import org.apache.commons.io.IOUtils;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.json.XML;
 import org.restlet.data.MediaType;
 import org.restlet.ext.fileupload.RestletFileUpload;
 import org.restlet.representation.Representation;
 import org.restlet.representation.StringRepresentation;
 import org.restlet.resource.Get;
 import org.restlet.resource.Post;
 import org.restlet.resource.ServerResource;
 
 import at.ac.dbisinformatik.snowprofile.data.DB;
 import at.ac.dbisinformatik.snowprofile.data.SchichtprofilDAO;
 
 public class ListSnowProfileResource extends ServerResource {
 
 	private DB db;
 
 	public ListSnowProfileResource(DB db) {
 		setNegotiated(true);
 		this.db = db;
 	}
 
 	/**
 	 * gives a list of all Snowprofiles which are saved in the Database
 	 * 
 	 * @return
 	 * @throws JSONException
 	 * @throws IOException
 	 */
 	@Get()
 	public String getJson() throws JSONException, IOException {
		return "{\"SnowprofileList\": " + SchichtprofilDAO.getAllSnowprofiles(db).toString() + "}";
 	}
 
 	/**
 	 * store new Snowprofile in Database
 	 * 
 	 * @param entity
 	 * @return
 	 * @throws Exception
 	 */
 	@Post
 	public String storeJson(Representation entity) throws Exception {
 		String rep = "";
         if (entity != null) {
             if (MediaType.MULTIPART_FORM_DATA.equals(entity.getMediaType(), true)) {
                 DiskFileItemFactory factory = new DiskFileItemFactory();
                 factory.setSizeThreshold(1000240);
 
                 RestletFileUpload upload = new RestletFileUpload(factory);
                 List<FileItem> items;
 
                 items = upload.parseRequest(getRequest());
 
                 boolean found = false;
                 for (final Iterator<FileItem> it = items.iterator(); it
                         .hasNext()
                         && !found;) {
                     FileItem fi = it.next();
                     String snowprofile = "";
                     if (fi.getFieldName().equals("import")) {
                     	snowprofile = IOUtils.toString(fi.getInputStream());
                     	JSONObject snowprofileJSON = XML.toJSONObject(snowprofile);
                     	
                     	snowprofile = snowprofileJSON.toString().replace("caaml:", "");
                     	snowprofile = snowprofile.replace("gml:", "gml_");
                     	snowprofile = snowprofile.replace("xmlns:", "xmlns_");
                     	snowprofile = snowprofile.replace("xsi:", "xsi_");
                     	
                     	snowprofileJSON = new JSONObject(snowprofile);
                     	
                     	snowprofile = db.store("SnowProfile", new JSONObject(snowprofileJSON.get("SnowProfile").toString()));
                     	rep = "{\"success\": \"true\", \"id\": \""+snowprofile+"\"}";
                     } else {
                         rep = new StringRepresentation("no file uploaded", MediaType.TEXT_PLAIN).toString();
                     }
                 }
             } else {
             	rep = db.store("SnowProfile", new JSONObject(entity.getText()));
     		}
         }
 
         return rep;
 	}
 }
