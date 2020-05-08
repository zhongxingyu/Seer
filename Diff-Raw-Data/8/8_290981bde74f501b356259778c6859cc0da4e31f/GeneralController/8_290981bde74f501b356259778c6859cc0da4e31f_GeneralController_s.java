 package ca.ualberta.cs.c301f13t13.backend;
 
 import java.util.ArrayList;
 import java.util.UUID;
 
 import android.content.Context;
 
 public class GeneralController {
 	public static final int CACHED = 0;
 	public static final int CREATED = 1;
 	public static final int PUBLISHED = 2;
 	
 	public static final int STORY = 0;
 	public static final int CHAPTER = 1;
 	public static final int CHOICE = 2;
 
 	
 	/** 
 	 * Gets all the stories that are either cached, created by the author, or published.
 	 * 
 	 * @param type
 	 * 			Will either be CACHED (0), CREATED (1), or PUBLISHED (2). 
 	 * @param context
 	 * @return ArrayList<Story>
 	 */
 	public ArrayList<Story> getAllStories(int type, Context context){
 		StoryManager sm = new StoryManager(context);
 		DBHelper helper = DBHelper.getInstance(context);
 		ArrayList<Story> stories = new ArrayList<Story>();
 		ArrayList<Object> objects;
 		Story criteria;
 		
 		switch(type) {
 		case CACHED:
 			criteria = new Story("", "", "", false);
 			objects = sm.retrieve(criteria, helper);
 			stories = Utilities.objectsToStories(objects);
 			break;
 		case CREATED:
 			criteria = new Story("", "", "", true);
 			objects = sm.retrieve(criteria, helper);
 			stories = Utilities.objectsToStories(objects);
 			break;
 		case PUBLISHED:
 			stories = sm.getPublishedStories();
 			break;
 		default:		
 			break;
 			
 		}
 		
 		return stories;
 	}
 	
 	/**
 	 *  Gets all the chapters that are in story.
 	 * 
 	 * @param storyId
 	 * @param context
 	 * 
 	 * @return ArrayList<Chapter>
 	 */
 	public ArrayList<Chapter> getAllChapters(UUID storyId, Context context){
 		ChapterManager cm = new ChapterManager(context);
 		DBHelper helper = DBHelper.getInstance(context);
 		ArrayList<Chapter> chapters = new ArrayList<Chapter>();
 		ArrayList<Object> objects;
 		Chapter criteria = new Chapter(storyId, "");
 		
 		objects = cm.retrieve(criteria, helper);
 		chapters = Utilities.objectsToChapters(objects);
 		
 		return chapters;
 	}
 	
 	/** 
 	 * Gets all the choices that are in a chapter.
 	 * 
 	 * @return
 	 */
 	public ArrayList <Choice> getAllChoices(UUID chapterId, Context context){
 		ChoiceManager cm = new ChoiceManager(context);
 		DBHelper helper = DBHelper.getInstance(context);
 		ArrayList<Choice> choices = new ArrayList<Choice>();
 		ArrayList<Object> objects;
 //		Choice criteria = new Choice(chapterId, "");
 //		
 //		objects = cm.retrieve(criteria, helper);
 //		choices = Utilities.objectsToChoices(objects);
 		return choices;
 	}
 	
 	/**
 	 * Adds either a story, chapter, or choice to the database.
 	 * 
 	 * @param object
 	 * @param helper
 	 * @param type
 	 * 			Will either be STORY(0), CHAPTER(1), CHOICE(2).
 	 * @param context
 	 */
	public void addObject(Object object, DBHelper helper, int type, Context context) {
 		switch (type) {
 		case STORY:
 			Story story = (Story) object;
 			StoryManager sm = new StoryManager(context);
 			sm.insert(story, helper);
 			break;
 		case CHAPTER:
 			Chapter chapter = (Chapter) object;
 			ChapterManager cm = new ChapterManager(context);
 			cm.insert(chapter, helper);
 			break;
 		case CHOICE:
 			Choice choice = (Choice) object;
 			ChoiceManager chm = new ChoiceManager(context);
 			chm.insert(choice, helper);
 			break;
 		}
 	}
 	
 	/** have to figure out.... this will be used to search by keyword
 	 * 
 	 * @return
 	 */
 	public ArrayList<Story> searchStory(String string){
 		//TODO everything
 		
 		return null;
 	}
 	/** load all photo/illustration/choice as a chapter
 	 * 
 	 * @return
 	 */
 	public Chapter getCompleteChapter(UUID id){
 		//TODO everything
 		
 		return null;
 	}
 	/** load all photo/illustration/choice/chapters as a story
 	 * 
 	 * @return
 	 */
 	public Story getCompleteStory(UUID id){
 		//TODO everything
 		
 		return null;
 	}
 	public void updateObject(Object object, int type) {
 		// TODO SWITCH STATEMENT
 				
 	}
 
 }
