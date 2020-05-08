 /*
 ] * Copyright (c) 2013 Andrew Fontaine, James Finlay, Jesse Tucker, Jacob Viau, and
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
 
 
 package ca.cmput301f13t03.adventure_datetime.controller;
 
 import java.util.UUID;
 
 import android.util.Log;
 import ca.cmput301f13t03.adventure_datetime.model.Bookmark;
 import ca.cmput301f13t03.adventure_datetime.model.Choice;
 import ca.cmput301f13t03.adventure_datetime.model.Comment;
 
 import ca.cmput301f13t03.adventure_datetime.model.Interfaces.ILocalStorage;
 import ca.cmput301f13t03.adventure_datetime.model.Interfaces.IStoryModelDirector;
 
 /**
  * Controller for aspects of playing through stories
  */
 public class UserController {
 	private IStoryModelDirector m_storyDirector = null;
 
 
 	public UserController(IStoryModelDirector director, ILocalStorage storage) {
 		m_storyDirector = director;
 	}
 
 	/**
 	 * @param storyId UUID of the story to start
 	 *
 	 * @return true if the story was successfully selected, false if it doesn't exist
 	 */
 	public boolean StartStory(UUID storyId) {
 		try {
 			m_storyDirector.selectStory(storyId);
 			m_storyDirector.selectFragment(m_storyDirector.getStory(storyId).getHeadFragmentId());
 			return true;
 		} catch (NullPointerException e) {
			Log.e("UserController", e.getMessage());
 			return false;
 		}
 	}
 
 	/**
 	 * @param id UUID of the story to start
 	 *
 	 * @return true if story was successfully selected, false if it doesn't exist
 	 */
 	public boolean ResumeStory(UUID id) {
 		Bookmark bookmark = m_storyDirector.getBookmark(id);
 		try {
 			m_storyDirector.selectStory(bookmark.getStoryID());
 			m_storyDirector.selectFragment(bookmark.getFragmentID());
 			return true;
 		} catch (NullPointerException e) {
			Log.e("UserController", e.getMessage());
 			return false;
 		}
 	}
 
     /**
      * Creates a bookmark at the current location
      */
 	public void SetBookmark() {
 		m_storyDirector.setBookmark();
 	}
 
     /**
      * Passes a comment to the fragment
      *
      * @param comment The comment to attach to a fragment
      */
 	public void AddComment(Comment comment) {
 		m_storyDirector.addComment(comment);
 	}
 
     /**
      * The user makes a choice at the current fragment
      *
      * @param choice The choice made
      */
 	public void MakeChoice(Choice choice) {
 		m_storyDirector.selectFragment(choice.getTarget());
 		m_storyDirector.setBookmark();
 	}
 	
 	public void download() 
 	{
 		m_storyDirector.download();
 	}
 
 	public void deleteBookmark() {
 		m_storyDirector.deleteBookmark();
 		
 	}
 
 }
