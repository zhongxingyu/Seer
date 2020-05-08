 package org.businessmanager.web.jsf.helper;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 
 import javax.faces.application.Application;
 import javax.faces.application.FacesMessage.Severity;
 import javax.faces.context.ExternalContext;
 import javax.faces.context.FacesContext;
 
 import org.easymock.EasyMock;
 import org.primefaces.component.panel.Panel;
 
 public class MockFacesContextHelper implements FacesContextHelper {
 
 	private Map<String, String> params = new HashMap<String, String>();
 	private List<String> messages = new ArrayList<String>();
 	
 	@Override
 	public FacesContext getCurrentFacesContext() {
 		FacesContext mockFacesContext = EasyMock.createMock(FacesContext.class);
 		ExternalContext externalContext = EasyMock.createMock(ExternalContext.class);
 		Application application = EasyMock.createMock(Application.class);
 		Panel panel = EasyMock.createMock(Panel.class);
 		
 		EasyMock.expect(mockFacesContext.getExternalContext()).andReturn(externalContext);
 		EasyMock.expect(mockFacesContext.getApplication()).andReturn(application);
 		
 		EasyMock.expect(application.createComponent(Panel.COMPONENT_TYPE)).andReturn(panel);
 		EasyMock.replay(mockFacesContext, application);
 		
 		return mockFacesContext;
 	}
 
 	@Override
 	public String getSessionParam(String key) {
 		return params.get(key);
 	}
 
 	@Override
 	public void setSessionParam(String key, String value) {
 		params.put(key, value);
 	}
 
 	@Override
 	public Locale getLocale() {
 		return Locale.GERMAN;
 	}
 
 	@Override
 	public void addErrorMessage(String msgKey) {
 		messages.add(msgKey);
 	}
 	
 	public List<String> getMessages() {
 		return messages;
 	}
 	
 	@Override
 	public String getRequestParam(String key) {
 		return params.get(key);
 	}
 
 	@Override
 	public void setRequestParam(String key, String value) {
 		params.put(key, value);		
 	}
 
 	@Override
 	public void addErrorMessage(String clientId, String msgKey) {
 		messages.add(msgKey);
 	}
 
 	@Override
 	public void addMessage(Severity severity, String msgKey) {
 		messages.add(msgKey);		
 	}
 
 	@Override
 	public void addMessage(String clientId, Severity severity, String msgKey) {
 		messages.add(msgKey);		
 	}
 
 }
