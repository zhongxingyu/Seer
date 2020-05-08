 package com.wedlum.styleprofile.domain.survey;
 
 import java.util.ArrayList;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 
 import com.wedlum.styleprofile.domain.photo.Photo;
 import com.wedlum.styleprofile.domain.photo.PhotoSource;
 
 public class Profile {
 
 	public List<String> photos;
 	private List<String> likedPhotos;
 
 	PhotoSource photoSource;
 
 	private Map<String, List<String>> sessionsByName;
 
     public Profile() {
		this.sessionsByName = new LinkedHashMap<String, List<String>>();
 	}
 
 	@SuppressWarnings("unchecked")
 	public Profile(Map<?, ?> $sessionsByName) {
 		this.sessionsByName = (Map<String, List<String>>) $sessionsByName;
 	}
 
 	public void addSession(String name, List<String> session) {
 		sessionsByName.put(name, session);
 	}
 
 	public List<String> getSession(String name) {
 		return sessionsByName.get(name);
 	}
 
 	public boolean hasSession(String name) {
 		return sessionsByName.containsKey(name);
 	}
 
 	public boolean isEmpty() {
 		return sessionsByName.isEmpty();
 	}
 
     public Iterable<String> allSessions() {
         return sessionsByName.keySet();
     }
 
     public List<Photo> getLikedPhotos() {
         return null;
     }
 
 	public List<Photo> getPhotos() {
 		List<Photo> result = new ArrayList<Photo>();
 		for (String $photo : photos)
 			result.add(new Photo($photo, photoSource.getMetadata($photo)));
 
 		return result;
 	}
 }
