 package ca.cmput301f13t03.adventure_datetime.model;/*
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
 import junit.framework.Assert;
 
 import java.util.UUID;
 
 /**
  * @author Andrew Fontaine
  * @version 1.0
  * @since 31/10/13
  */
 public class StoryDBTest extends AndroidTestCase {
 
 	private StoryDB database;
 
 	@Override
 	public void setUp() throws Exception {
 		super.setUp();    //TODO Implement
 		RenamingDelegatingContext context = new RenamingDelegatingContext(getContext(), "test_");
 		database = new StoryDB(context);
 	}
 
 	public void testSetStoryFragment() throws Exception {
 
 		String uuid = UUID.randomUUID().toString();
 		Choice choice = new Choice("test", uuid);
 		StoryFragment frag = new StoryFragment(uuid, "testing", choice);
 
 		String fragUuid = frag.getFragmentID();
 
 		Assert.assertTrue("Error inserting fragment", database.setStoryFragment(frag));
 		StoryFragment frag2 = database.getStoryFragment(frag.getFragmentID());
 
 		Assert.assertEquals("Not equivalent fragment ids", frag.getFragmentID(), frag2.getFragmentID());
 		Assert.assertEquals("Not equivalent story ids", frag.getStoryID(), frag2.getStoryID());
 
 		Assert.assertEquals("Not equivalent UUIDs", fragUuid, frag.getFragmentID());
 
 	}
 
 	public void testSetStory() throws Exception {
 		Story story = new Story("TestAuthor", "TestTitle", "TestSynop");
 		String uuid = story.getId();
         story.setHeadFragmentId(UUID.randomUUID().toString());
 		Assert.assertTrue("Error inserting story", database.setStory(story));
 		Story story2 = database.getStory(story.getId());
 
 		Assert.assertEquals("Not equivalent story ids", story.getId(), story2.getId());
 		Assert.assertEquals("Not equivalent uuids", uuid, story.getId());
 	}
 
 	public void testSetBookmark() throws Exception {
 		String sUuid, sFUuid;
 		sUuid = UUID.randomUUID().toString();
 		sFUuid = UUID.randomUUID().toString();
 		Bookmark bookmark = new Bookmark(sUuid, sFUuid);
 		Assert.assertTrue("Error inserting bookmark", database.setBookmark(bookmark));
 		Bookmark bookmark2 = database.getBookmark(sUuid);
 
 		Assert.assertEquals("Not equivalent story ids", bookmark.getStoryID(), bookmark2.getStoryID());
 		Assert.assertEquals("Not equivalent uuids", sUuid, bookmark.getStoryID());
 
         database.deleteBookmarkByStory(sUuid);
         bookmark2 = database.getBookmark(sUuid);
        Assert.assertNull("Bookmark not null", bookmark2);
 
         Assert.assertTrue("Error inserting bookmark", database.setBookmark(bookmark));
 
         database.deleteStoryFragment(sFUuid);
 
         bookmark2 = database.getBookmark(sUuid);
 
        Assert.assertNull("Bookmark not null", bookmark2);
 	}
 
 
 	@Override
 	public void tearDown() throws Exception {
 		super.tearDown();    //TODO Implement
 	}
 
 
 }
