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
 import java.text.ParseException;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.restlet.representation.Representation;
 import org.restlet.resource.Post;
 import org.restlet.resource.ServerResource;
 
 import at.ac.dbisinformatik.snowprofile.data.DB;
 import at.ac.dbisinformatik.snowprofile.data.SchichtprofilDAO;
 
 public class SearchSnowProfileResource extends ServerResource {
 	
 	private DB db;
 
 	public SearchSnowProfileResource(DB db) {
 		setNegotiated(true);
 		this.db = db;
 	}
 	
 	/**
 	 * returns a list of all Snow Profiles filtered by search values
 	 * 
 	 * @param value
 	 * @return
 	 * @throws JSONException
 	 * @throws IOException
 	 * @throws ParseException
 	 */
 	@Post()
 	public String getJsonPost(Representation value) throws JSONException, IOException, ParseException {
		return "{\"SnowprofileList\": " + SchichtprofilDAO.getSnowProfilesBySearch(db, new JSONObject(value.getText())).toString() + "}";
 	}
 }
