 package server;
 
 
 import java.util.Date;
 import java.util.LinkedList;
 import java.util.List;
 
 import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
 import javax.faces.context.FacesContext;
 import javax.faces.event.ActionEvent;
 
 import ctrl.DBModManual;
 
 import data.ModManual;
 
 
 
 
//Changed to Sessionscope - if in doubt revert to RequestScoped
@ManagedBean(name = "DekanBean")
@SessionScoped
 public class DekanBean {
 	
 	private List<ModManual> modManualList;
 	private LoginBean login;
 	private String modManTitle, description;
 	private int exRules;
 	private Date deadLine;
 	
 	/**
 	 * Equivalent to a constructor
 	 */
 	@PostConstruct
 	public void init() {
 		modManualList = new LinkedList<ModManual>();
 		modManualList = DBModManual.loadAllModManuals();
 		login = findBean("LoginBean");
 	}
 	
 	/**
 	 * finds the bean with corresponding name
 	 * 
 	 * @param beanName
 	 * @return
 	 */
 	@SuppressWarnings("unchecked")
 	public static <T> T findBean(String beanName) {
 		FacesContext context = FacesContext.getCurrentInstance();
 		return (T) context.getApplication().evaluateExpressionGet(context,
 				"#{" + beanName + "}", Object.class);
 	}
 	
 	public void resetFields() {
 		modManTitle = description = null;
 		deadLine = null;
 		exRules = -1;
 	}
 	
 	/**
 	 * saves the user with the specified email from the database
 	 * 
 	 * @param email
 	 * @return boolean
 	 */
 	public void saveModManual(ActionEvent action) {
 		System.out.println("saveModManual");
 	}
 	
 	/**
 	 * @return the modManualList
 	 */
 	public List<ModManual> getModManualList() {
 		return modManualList;
 	}
 
 	/**
 	 * @param modManualList the modManualList to set
 	 */
 	public void setModManualList(List<ModManual> modManualList) {
 		this.modManualList = modManualList;
 	}
 
 	/**
 	 * @return the modManTitle
 	 */
 	public String getModManTitle() {
 		return modManTitle;
 	}
 
 	/**
 	 * @param modManTitle the modManTitle to set
 	 */
 	public void setModManTitle(String modManTitle) {
 		this.modManTitle = modManTitle;
 	}
 
 
 
 	/**
 	 * @return the deadLine
 	 */
 	public Date getDeadLine() {
 		return deadLine;
 	}
 
 	/**
 	 * @param deadLine the deadLine to set
 	 */
 	public void setDeadLine(Date deadLine) {
 		this.deadLine = deadLine;
 	}
 
 	/**
 	 * @return the description
 	 */
 	public String getDescription() {
 		return description;
 	}
 
 	/**
 	 * @param description the description to set
 	 */
 	public void setDescription(String description) {
 		this.description = description;
 	}
 
 	/**
 	 * @return the exRules
 	 */
 	public int getExRules() {
 		return exRules;
 	}
 
 	/**
 	 * @param exRules the exRules to set
 	 */
 	public void setExRules(int exRules) {
 		this.exRules = exRules;
 	}
 
 	
 	
 }
