 package story.book;
 
 import java.io.*;
 import java.util.ArrayList;
 
 import android.content.Context;
 import android.util.Log;
 
 /**
  * 
  * @author Anthony Ou
  * 
  */
 public class IOClient extends DataClient {
 
     private String story_dir;
 
     /**
      * Unbuffered IO for writing a serialized story. Buffered IO for read a
      * serialized story string.
      * 
      * @param c
      *            the Context of the main application since the package name
      *            directory can change
      */
     public IOClient(Context c) {
 	super();
 	this.story_dir = c.getFilesDir() + "/storys/";
 	new File(story_dir).mkdir(); // make the directory if it doesn't exist
     }
 
     /**
      * http://stackoverflow.com/questions/14376807/how-to-read-write-string-from
      * -a-file-in-android
      * 
      * @param SID
      *            A fileID
      * @param aStory
      *            The story to be saved
      */
     public void saveStory(Story aStory) {
 	FileOutputStream fos;
 	try {
 	    Log.d(String.valueOf(aStory.hashCode()), "hash Code");
 	    fos = new FileOutputStream(story_dir
 		    + String.valueOf(aStory.getStoryInfo().getSID()));
 	    fos.write(super.serialize(aStory).getBytes());
 	    fos.flush();
 	    fos.close();
 	} catch (IOException e) {
 	}
 
     }
 
     /**
      * 
      * @param SID
      *            the story to be deleted
      * @return true on success and false on failure
      */
     public boolean deleteStory(int SID) {
 	// I add a slash here since getFilesDir doesnt include it at the end
 	return new File(story_dir + String.valueOf(SID)).delete();
     }
 
     /**
      * 
      * @return a list of all the SIDs on the internal device.
      */
     public ArrayList<String> getStoryList() {
 	ArrayList<String> listOfFileNames = new ArrayList<String>();
 	File f = new File(story_dir);
 	for (String temp : f.list()) {
 	    listOfFileNames.add(temp);
 	}
 	return listOfFileNames;
     }
 
     /**
      * 
      * @return an array list of StoryInfos
      */
     public ArrayList<StoryInfo> getStoryInfoList() {
 	ArrayList<StoryInfo> listOfStoryInfo = new ArrayList<StoryInfo>();
 	for (String file : getStoryList()) {
 	    Log.d(file, "List Of Files");
 	    listOfStoryInfo.add(getStory(Integer.valueOf(file).intValue())
 		    .getStoryInfo());
 	}
 	return listOfStoryInfo;
     }
 
     public Boolean checkSID(int SID) {
	return getStoryList().contains(String.valueOf(SID)) ? false : true;
     }
 
     /**
      * 
      * @return a free SID or -1 on failure
      */
     public int getSID() {
 	ArrayList<StoryInfo> StoryInfo = getStoryInfoList();
 	for (int i = 1; i < Integer.MAX_VALUE; ++i) {
 	    if (!StoryInfo.contains(String.valueOf(i))) {
 		return i;
 	    }
 	}
 	return -1;
     }
 
     /**
      * http://stackoverflow.com/questions/14376807/how-to-read-write-string-from
      * -a-file-in-android
      * 
      * @param SID
      *            Likely to the SID of the story or any other string
      * @returns A string representing a serialized story;
      */
     public Story getStory(int SID) {
 
 	char[] inputBuffer = new char[5048];
 	StringBuilder sb = new StringBuilder(5048); // set the initial size
 	// of
 	try {
 	    FileInputStream fis = new FileInputStream(story_dir
 		    + String.valueOf(SID));
 	    InputStreamReader isr = new InputStreamReader(fis);
 
 	    // string builder to be
 	    // same size as the buffer
 	    int l;
 	    while ((l = isr.read(inputBuffer)) != -1) {
 		sb.append(inputBuffer, 0, l);
 	    }
 	    fis.close();
 	    isr.close();
 	} catch (Exception e) {
 	    // TODO Auto-generated catch block
 	    return null;
 	}
 
 	return (Story) super.unSerialize(sb.toString(), Story.class);
     }
 }
