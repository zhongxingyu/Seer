 /*
 Adventure App - Allows you to create an Adventure Book, or Download
 	books from other authors.
 Copyright (C) Fall 2013 Team 5 CMPUT 301 University of Alberta
 
 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.
 
 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.
 
 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
 package com.uofa.adventure_app.controller.test;
 
 import java.util.ArrayList;
 
 import android.test.ActivityInstrumentationTestCase2;
 
 import com.uofa.adventure_app.activity.BrowserActivity;
 import com.uofa.adventure_app.controller.StoryParser;
 import com.uofa.adventure_app.controller.WebServiceController;
 import com.uofa.adventure_app.controller.http.HttpObjectStory;
 import com.uofa.adventure_app.model.Story;
 
 /**
  * @author Joel
  * See documentation for WebServiceController 
  */
 public class WebServiceControllerTest extends
 		ActivityInstrumentationTestCase2<BrowserActivity>
 {
 	
 	HttpObjectStory httpStory = null;
 	WebServiceController webServiceController = new WebServiceController();
 	StoryParser parser = null;
 	
 	public WebServiceControllerTest()
 	{
 		super(BrowserActivity.class);
 	}
 
 	protected void setUp() throws Exception
 	{
 		super.setUp();
 		httpStory = new HttpObjectStory();
 		parser = new StoryParser();
 	}
 	
 	/**
 	 * Tests all functions
 	 */
 	public void testPublishFetchAllDelete() {
 		Story s = new Story();
 		webServiceController.httpWithType(httpStory.publishObject(s));
 		String parseString = webServiceController.httpWithType(httpStory.fetchAll());
 		ArrayList<Story> stories = parser.parse(parseString);
 		assertNotNull(stories);
 		
 		assertFalse(stories.contains(s));
 		
 		// Delete Story after publish
 		webServiceController.httpWithType(httpStory.deleteObject(s.id().toString()));
 		
 	}
 	
 } // class end
