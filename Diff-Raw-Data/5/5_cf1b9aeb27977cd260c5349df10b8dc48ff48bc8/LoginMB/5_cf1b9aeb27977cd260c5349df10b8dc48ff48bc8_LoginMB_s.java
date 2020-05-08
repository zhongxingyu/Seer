 package br.com.findplaces.mb;
 
 import java.io.Serializable;
 
 import javax.annotation.ManagedBean;
 import javax.faces.bean.ViewScoped;
 import javax.faces.context.ExternalContext;
 import javax.faces.context.FacesContext;
 import javax.servlet.http.HttpServletRequest;
 
 import com.restfb.DefaultFacebookClient;
 import com.restfb.types.User;
 
 @ManagedBean("loginMB")
 @ViewScoped
 public class LoginMB implements Serializable {
 	
 	private static final long serialVersionUID = 1L;
 
 	public void SingupFacebook() {
 		ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
 		HttpServletRequest request = (HttpServletRequest) context.getRequest();
 		DefaultFacebookClient facebook = new DefaultFacebookClient(request.getParameter("token"));
 		User user = facebook.fetchObject("me", User.class);
		user.getName();
 	}
 
 }
