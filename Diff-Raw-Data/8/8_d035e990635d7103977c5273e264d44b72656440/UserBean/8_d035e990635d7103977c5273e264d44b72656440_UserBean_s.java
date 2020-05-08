 package org.mdissjava.mdisscore.view.user;
 
 import java.io.IOException;
 import java.util.List;
 
 import javax.faces.application.FacesMessage;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.RequestScoped;
 import javax.faces.context.FacesContext;
 
 import org.mdissjava.commonutils.properties.PropertiesFacade;
 import org.mdissjava.mdisscore.controller.bll.UserManager;
 import org.mdissjava.mdisscore.controller.bll.impl.UserManagerImpl;
 import org.mdissjava.mdisscore.model.dao.factory.MorphiaDatastoreFactory;
 import org.mdissjava.mdisscore.model.pojo.User;
 import org.mdissjava.mdisscore.view.params.ParamsBean;
 import org.springframework.security.core.Authentication;
 import org.springframework.security.core.context.SecurityContextHolder;
 
 import com.google.code.morphia.Datastore;
 
 @RequestScoped
 @ManagedBean
 public class UserBean {
 	
 	private String userNickname;
 	private String userId;
 
 	private List<User> follows;
 	private List<User> followers;
 	
 	private UserManager userManager;	
 	private final String GLOBAL_PROPS_KEY = "globals";
 	private final String MORPHIA_DATABASE_KEY = "morphia.db";
 	private int MAX_NUMBER_CONNECTIONS = 10;
 	
 	private String thumbnailBucket = "square.75";
 	private String thumbnailDatabase = "images";
 	private Datastore datastore;
 	
 		
 	private User user;	
 	private int page;
 	
 	PropertiesFacade propertiesFacade;
 	String database;
 	
 	public UserBean() {
 		ParamsBean pb = getPrettyfacesParams();
 		this.page = pb.getPage();
 		if (this.page == 0){
 			this.page = 1;
 		}
 		this.userId = pb.getUserId();
 		this.userManager = new UserManagerImpl();			
 		this.userNickname = retrieveSessionUserNick();	
 		this.user = userManager.getUserByNick(this.userId);	
 		
 		try {
 			propertiesFacade = new PropertiesFacade();
 			database = propertiesFacade.getProperties(GLOBAL_PROPS_KEY).getProperty(MORPHIA_DATABASE_KEY);	
 			datastore = MorphiaDatastoreFactory.getDatastore(database);
 			database = propertiesFacade.getProperties("globals").getProperty("images.db");
 		} catch (IOException e) {
 			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,"Error while retrieveing connections from the database", "Connections"));
 		}					
 	}
 		
 	public String getUserId() {
 		return this.userId;
 	}
 
 	public void setUserId(String userId) {
 		this.userId = userId;
 	}
 		
 	public User getUser(){
 		return this.user;
 	}
 	
 	public void setUser(User user) {
 		this.user = user;
 	}
 	
 	public String getUserNickname() {
 		return userNickname;
 	}
 
 	public void setUserNickname(String userNickname) {
 		this.userNickname = userNickname;
 	}
 
 	public List<User> getFollows() {
 		this.follows = userManager.findFollows(user.getNick(), page, MAX_NUMBER_CONNECTIONS);		
 		return this.follows;
 	}
 	
 	public void setFollows(List<User> follows) {
 		this.follows = follows;
 	}
 	
 	public List<User> getFollowers() {
 		this.followers = userManager.findFollowers(user.getNick(), page, MAX_NUMBER_CONNECTIONS);														
 		return this.followers;
 	}
 	
 	public void setFollowers(List<User> followers) {
 		this.followers = followers;
 	}
 	
 	public void addFollow(User follow) {		
 		userManager.addFollow(this.userNickname, follow);
 	}
 	
 	public void deleteFollow(User follow) {		
 		userManager.deleteFollow(this.userNickname, follow);
 	}
 	
 	public boolean followsUser(User follow) {
 		return userManager.followsUser(this.userNickname, follow);
 	}
 			
 	public int getPage() {
 		return this.page;
 	}
 
 	public void setPage(int page) {
 		this.page = page;
 	}
 
 	public int getMaxNumberConnections(){
 		return this.MAX_NUMBER_CONNECTIONS;
 	}
 	
 	public void setMaxNumberConnections(int max){
 		this.MAX_NUMBER_CONNECTIONS = max;
 	}
 
 	public String getThumbnailBucket() {
 		return this.thumbnailBucket;
 	}
 
 	public void setThumbnailBucket(String thumbnailBucket) {
 		this.thumbnailBucket = thumbnailBucket;
 	}
 
 	public String getThumbnailDatabase() {
 		return this.thumbnailDatabase;
 	}
 
 	public void setThumbnailDatabase(String thumbnailDatabase) {
 		this.thumbnailDatabase = thumbnailDatabase;
 	}
 
 	private String retrieveSessionUserNick() {
 	  //Get the current logged user's username
 	  Authentication auth = SecurityContextHolder.getContext().getAuthentication();
 	  return auth.getName();		  
 	}
 	
 	private ParamsBean getPrettyfacesParams() {
 		FacesContext context = FacesContext.getCurrentInstance();
 		ParamsBean pb = (ParamsBean) context.getApplication().evaluateExpressionGet(context, "#{paramsBean}", ParamsBean.class);
 		return pb;
 	}	
 	
 }
