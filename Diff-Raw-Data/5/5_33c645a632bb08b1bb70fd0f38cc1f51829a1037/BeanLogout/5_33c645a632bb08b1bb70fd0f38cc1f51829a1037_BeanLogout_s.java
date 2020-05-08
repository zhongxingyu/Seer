 package com.metal.managed;
 
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.SessionScoped;
 import javax.faces.context.FacesContext;
 import javax.servlet.http.HttpSession;
 
 @ManagedBean
 @SessionScoped
 public class BeanLogout {
 
	public void doLogout() {
 		FacesContext context = FacesContext.getCurrentInstance();
 		HttpSession session = (HttpSession) context.getExternalContext().getSession(false);
 		session.invalidate();
 	}
 }
