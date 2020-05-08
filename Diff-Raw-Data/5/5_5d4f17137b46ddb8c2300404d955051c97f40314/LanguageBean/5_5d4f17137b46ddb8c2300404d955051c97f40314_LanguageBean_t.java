 package chatlobby;
 
 import java.io.Serializable;
 import java.util.Locale;
 
 import javax.faces.component.UIViewRoot;
 import javax.faces.context.FacesContext;
 
 public class LanguageBean implements Serializable {
 
 	private static final long serialVersionUID = 1L;
 
 	public void setLocaleToEn() {
 		UIViewRoot viewRoot = FacesContext.getCurrentInstance().getViewRoot();
 		viewRoot.setLocale(new Locale("en"));
 	}
 
 	public void setLocaleToDe() {
 		UIViewRoot viewRoot = FacesContext.getCurrentInstance().getViewRoot();
 		viewRoot.setLocale(new Locale("de"));
 	}
	
	public String getLocal(){
		UIViewRoot viewRoot = FacesContext.getCurrentInstance().getViewRoot();
		return viewRoot.getLocale().toString();
	}
 
 }
