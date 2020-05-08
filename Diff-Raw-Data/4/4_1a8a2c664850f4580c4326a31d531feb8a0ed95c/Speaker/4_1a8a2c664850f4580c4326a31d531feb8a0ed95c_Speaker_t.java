 package eu.margiel.domain;
 
 import static ch.lambdaj.Lambda.*;
 import static com.google.common.collect.Lists.*;
import static org.apache.commons.lang.StringUtils.*;
 
 import java.util.List;
 
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.OneToMany;
 
 @SuppressWarnings("serial")
 @Entity
 public class Speaker extends User {
 	private String webPage;
 	private String twitter;
 	@OneToMany(fetch = FetchType.EAGER, mappedBy = "speaker", orphanRemoval = true)
 	private List<Presentation> presentations = newArrayList();
 	@SuppressWarnings("unused")
 	private String token;
 
 	public String getWebPage() {
 		return webPage;
 	}
 
 	public String getTwitter() {
 		return twitter;
 	}
 
 	public String getTwitterUrl() {
		return isBlank(twitter) ? null : "http://www.twitter.com/" + twitter;
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public Speaker mail(String mail) {
 		return super.mail(mail);
 	}
 
 	public List<Presentation> getPresentations() {
 		return presentations;
 	}
 
 	public Speaker addPresentation(Presentation presentation) {
 		presentation.speaker(this);
 		this.presentations.add(presentation);
 		return this;
 	}
 
 	@Override
 	String getSubfolderName() {
 		return "speaker";
 	}
 
 	public void setToken(String token) {
 		this.token = token;
 	}
 
 	public boolean anyPresentationAccepted() {
 		return getAcceptedPresentations().size() > 0;
 	}
 
 	public List<Presentation> getAcceptedPresentations() {
 		return select(getPresentations(), having(on(Presentation.class).isAccepted()));
 	}
 
 }
