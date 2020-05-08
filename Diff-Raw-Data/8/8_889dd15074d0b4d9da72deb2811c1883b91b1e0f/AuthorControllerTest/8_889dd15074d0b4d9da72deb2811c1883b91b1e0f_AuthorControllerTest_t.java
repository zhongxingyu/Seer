 package ca.cmput301f13t03.adventure_datetime.controller;
 /*
  * Copyright (c) 2013 Andrew Fontaine, James Finlay, Jesse Tucker, Jacob Viau, and
  * Evan DeGraff
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy of
  * this software and associated documentation files (the "Software"), to deal in
  * the Software without restriction, including without limitation the rights to
  * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
  * the Software, and to permit persons to whom the Software is furnished to do so,
  * subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in all
  * copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
  * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
  * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
  * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
  * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
  */
 
 import android.test.AndroidTestCase;
 import android.test.RenamingDelegatingContext;
 import ca.cmput301f13t03.adventure_datetime.model.Choice;
 import ca.cmput301f13t03.adventure_datetime.model.Story;
 import ca.cmput301f13t03.adventure_datetime.model.StoryFragment;
 import ca.cmput301f13t03.adventure_datetime.model.StoryManager;
import ca.cmput301f13t03.adventure_datetime.serviceLocator.Locator;
 import junit.framework.Assert;
 
 import java.util.UUID;
 
 /**
  * @author Evan DeGraff
  * @version 1.0
  * @since 31/10/13
  */
 public class AuthorControllerTest extends AndroidTestCase {
 
 	private AuthorController controller;
 	private StoryManager manager;
 
 	@Override
 	public void setUp() throws Exception {
 		super.setUp();    //TODO Implement
 		RenamingDelegatingContext context = new RenamingDelegatingContext(getContext(), "test_");
 		manager = new StoryManager(context);
 		controller = new AuthorController(manager);
 	}
 
 	public void testSaveStory() throws Exception {
		Story story = Locator.getAuthorController().CreateStory();
		
		story.setAuthor("TestAuthor");
		story.setTitle("TestTitle");
		story.setSynopsis("TestSynop");
 
 		Assert.assertTrue("Error inserting story", controller.saveStory(story));
 		Story story2 = controller.getStory(story.getId());
 
 		Assert.assertEquals("Not equivalent stories", story.getId(), story2.getId());
 	}
 
 	public void testSaveFragment() throws Exception {
 		String uuid = UUID.randomUUID().toString();
 		Choice choice = new Choice("test", uuid);
 		StoryFragment fragment = new StoryFragment(uuid, "testing", choice);
 
 		Assert.assertTrue("Error inserting fragment", controller.saveFragment(fragment));
 		StoryFragment fragment2 = manager.getFragment(uuid);
 
 		Assert.assertEquals("Not equivalent fragments", fragment.getFragmentID(), fragment2.getFragmentID());
 	}
 
 	@Override
 	public void tearDown() throws Exception {
 		super.tearDown();    //TODO Implement
 	}
 
 
 }
