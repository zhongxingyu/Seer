 package dataContainers;
 
 import java.util.ArrayList;
 
 public class MediaLibrary extends ArrayList<MediaFile>{
 
 	private static final long serialVersionUID = -7147719263849293488L;
 	
 	private ArrayList<MediaLibrary> m_subLibrary = null;
 	
 	public MediaLibrary(){
 		// Do Nothing
 	}
 
 	public MediaLibrary addLibrary(){
 		MediaLibrary newLib = new MediaLibrary();
 		m_subLibrary.add(newLib);
 		return newLib;
 	}
 	
 	public MediaLibrary addLibrary(MediaLibrary library){
 		m_subLibrary.add(library);
 		return library;
 	}
 	
 
 	
 	
 }
