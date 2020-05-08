 package com.avricot.prediction.model.theme;
 
 import java.util.HashSet;
 import java.util.Set;
 
 import org.bson.types.ObjectId;
 import org.springframework.data.annotation.Id;
 import org.springframework.data.mongodb.core.mapping.Document;
 
 @Document(collection = "theme")
 public class Theme {
 	@Id
 	private ObjectId id;
 	private Set<String> words = new HashSet<String>();
 	private ThemeName themeName;
 
 	public ObjectId getId() {
 		return id;
 	}
 
 	public void setId(ObjectId id) {
 		this.id = id;
 	}
 
 	public Set<String> getWords() {
 		return words;
 	}
 
 	public void setWords(Set<String> words) {
 		this.words = words;
 	}
 
 	public ThemeName getThemeName() {
 		return themeName;
 	}
 
 	public void setThemeName(ThemeName themeName) {
 		this.themeName = themeName;
 	}
 
 	public static enum ThemeName {
 		SECURITY, EUROPE, ECONOMIC, GREEN, IMIGRATION, SOCIAL, ENERGY;
 	}
 }
