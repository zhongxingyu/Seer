 package edu.rit.asksg.domain;
 
 import org.joda.time.LocalDateTime;
 import org.springframework.roo.addon.javabean.RooJavaBean;
 import org.springframework.roo.addon.jpa.entity.RooJpaEntity;
 import org.springframework.roo.addon.json.RooJson;
 import org.springframework.roo.addon.tostring.RooToString;
 
 import java.util.List;
 
 @RooJavaBean
 @RooToString
 @RooJpaEntity
 @RooJson
 public class Facebook extends Service {
 

	@Override
	public List<Conversation> fetchNewContent() {
		return null;
	}

	@Override
	public List<Conversation> fetchContentSince(LocalDateTime datetime) {
		return null;
	}
 }
