 package chatlobby;
 
 import java.io.Serializable;
 import java.util.Locale;
 
 import javax.faces.component.UIViewRoot;
 import javax.faces.context.FacesContext;
 
 public class LanguageBean implements Serializable {
 
 	private static final long serialVersionUID = 1L;
 
	public String setLocaleToEn() {
 		UIViewRoot viewRoot = FacesContext.getCurrentInstance().getViewRoot();
 		viewRoot.setLocale(new Locale("en"));
		return "ChatLobby.xhtml?faces-redirect=true";
 	}
 
	public String setLocaleToDe() {
 		UIViewRoot viewRoot = FacesContext.getCurrentInstance().getViewRoot();
 		viewRoot.setLocale(new Locale("de"));
		return "ChatLobby.xhtml?faces-redirect=true";
 	}
 
 }
