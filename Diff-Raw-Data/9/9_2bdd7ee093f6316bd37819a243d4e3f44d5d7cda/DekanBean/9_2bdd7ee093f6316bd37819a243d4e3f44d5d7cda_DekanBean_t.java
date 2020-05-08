 package server;
 
 
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.LinkedList;
 import java.util.List;
 
 import javax.annotation.PostConstruct;
 import javax.faces.application.FacesMessage;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.SessionScoped;
 import javax.faces.context.FacesContext;
 import javax.faces.event.ActionEvent;
 
 import org.primefaces.event.RowEditEvent;
import org.primefaces.event.TabChangeEvent;
 
 import ctrl.DBExRules;
 import ctrl.DBModManual;
 import ctrl.DBUser;
 import data.ExRules;
 import data.ModManual;
 import data.User;
 
 
 
 //Changed to Sessionscope - if in doubt revert to RequestScoped
 @ManagedBean(name = "DekanBean")
 @SessionScoped
 public class DekanBean {
 	
 	private List<ModManual> modManualList;
 	private boolean success = true;
 	private String modManTitle, description;
 	private int exRulesTitle;
 	private Date deadline;
 	private ModManual currentMM;
 	
 	private List<ExRules> exRulesList;
 	private String exRules;
 	private ExRules currentER;
 	
 	SimpleDateFormat sdf;
 	
 	/**
 	 * Equivalent to a constructor
 	 */
 	@PostConstruct
 	public void init() {
 		// is used to parse date
 		sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
 		modManualList = new LinkedList<ModManual>();
 		modManualList = DBModManual.loadAllModManuals();
 		exRulesList = new LinkedList<ExRules>();
 		exRulesList = DBExRules.loadAllExRules();
 	}
 	
	public void onTabChange(TabChangeEvent event) {  
        FacesMessage msg = new FacesMessage("Tab Changed", "Active Tab: " + event.getTab().getTitle());  
  
        FacesContext.getCurrentInstance().addMessage(null, msg);  
        System.out.println("Tab Changed!");
    }
	
 	/**
 	 * reset modManTitle, description, deadline and exRulesTitle
 	 */
 	public void resetFields() {
 		modManTitle = description = null;
 		deadline = null;
 		exRulesTitle = -1;
 	}
 	
 	/**
 	 * reset exRules to null
 	 */
 	public void resetField() {
 		exRules = null;
 	}
 	
 	/**
 	 * saves the ModManual to the database
 	 * 
 	 */
 	public void saveModManual(ActionEvent action) {
 		success = true;
 		if (modManTitle.isEmpty() || description.isEmpty() || exRulesTitle == -1 || deadline == null) {
 			success = false;
 			addErrorMessage("Empty Field error: ", "Fields may not be empty - be sure to edit every field.");
 			System.out.println(success);			
 			return;
 		}	
 
 		if (DBModManual.loadModManual(modManTitle) == null) {
 			String exRules = decodeExRulesTitle();
 			DBModManual.saveModManual(new ModManual(modManTitle, description, exRules, deadline));
 			System.out.println(success);
 			addMessage("Module manual created: ", "" + modManTitle + ", " + exRules + ", " + deadline);
 			exRulesTitle = -1;
 			actualizeModManualList();
 			return;
 		}
 		
 		success = false;
 		System.out.println("error - modMantitle already exists: " + modManTitle + " " + success);
 		addErrorMessage("Module manual title already exists: ", "'" + modManTitle + "' is already in the database - please doublecheck.");
 	}
 	
 	/**
 	 * saves the examination rules to the database
 	 * 
 	 */
 	public void saveExRules(ActionEvent action) {
 		success = true;
 		System.out.println("**** " + exRules);
 		if (exRules == "") {
 			success = false;
 			addErrorMessage("Empty Field error: ", "Field may not be empty.");
 			System.out.println(success);			
 			return;
 		}	
 
 		if (DBExRules.loadExRules(exRules) == null) {
 			DBExRules.saveExRule(new ExRules(exRules));
 			System.out.println(success);
 			addMessage("Examinatino rules created: ", exRules);
 			return;
 		}
 		
 		success = false;
 		System.out.println("error - exRules already exists: " + exRules + " " + success);
 		addErrorMessage("Examination rules title already exists: ", "'" + exRules + "' is already in the database - please doublecheck.");
 	}
 	
 	
 	/**
 	 * function that refreshes the ModManualsList for delete and edit mode
 	 */
 	public void actualizeModManualList() {
 		String exRulesTitle = decodeExRulesTitle();
 		if (exRulesTitle == null)
 			setModManualList(DBModManual.loadAllModManuals());
 		else
 			setModManualList(DBModManual.loadModManuals(exRulesTitle));
 	}
 	
 	/**
 	 * Event Triggered when the accept icon is clicked on edit module manual tab -
 	 * updates with new Values
 	 * 
 	 * @param event
 	 */
 	public void onEdit(RowEditEvent event) {
 		
 		ModManual m = (ModManual) event.getObject();
 		FacesMessage msg = new FacesMessage("User Edited", ((ModManual) event.getObject()).toString());
 		if (DBModManual.loadModManual(m.getModManTitle()) != null) {
 			System.out.println(m.toString());
 			DBModManual.updateModManual(m, m.getModManTitle());
 		}
 		FacesContext.getCurrentInstance().addMessage("edit-messages", msg);
 		actualizeModManualList();
 	}
 
 	/**
 	 * Event triggered when editing user is cancelled
 	 * 
 	 * @param event
 	 */
 	public void onCancel(RowEditEvent event) {
 		FacesMessage msg = new FacesMessage("User edit cancelled", ((ModManual) event.getObject()).getModManTitle());
 		FacesContext.getCurrentInstance().addMessage("edit-messages", msg);
 	}
 	
 	/**
 	 * Translates numbers into Strings that are way more readable
 	 * 
 	 * @return String
 	 */
 	private String decodeExRulesTitle() {
 		switch (exRulesTitle) {
 		case 1:
 			return "PO2010";
 		case 2:
 			return "PO2012";
 		default:
 			return null;
 		}
 	}
 	
 	/**
 	 * function that refreshes the exRulesList for delete edit mode
 	 */
 	public void actualizeExRulesList() {
 		String exRulesTitle = decodeExRulesTitle();
 		if (exRulesTitle == null)
 			setExRulesList(DBExRules.loadAllExRules());
 		else
 			setExRulesList(DBExRules.loadAllExRules(exRulesTitle));
 	}
 	
 	/**
 	 * deletes the exRule from the database, which was selected in the data table
 	 */
 	public void deleteModManual(){
 		DBModManual.deleteModManual(currentMM.getModManTitle());
 		exRulesTitle = -1;
 		actualizeModManualList();
 	}
 	
 	/**
 	 * deletes the exRule from the database, which was selected in the data table
 	 */
 	public void deleteExRules(){
 		DBExRules.deleteExRule(currentER.getExRulesTitle());
 		actualizeExRulesList();
 	}
 	
 	/**
 	 * if anything goes wrong - display an Error
 	 * 
 	 * @param title
 	 * @param msg
 	 */
 	public void addErrorMessage(String title, String msg) {
 		FacesContext.getCurrentInstance().addMessage("dekan-messages",
 				new FacesMessage(FacesMessage.SEVERITY_FATAL, title, msg));
 	}
 
 	/**
 	 * Informs the User via message.
 	 * 
 	 * @param title
 	 * @param msg
 	 */
 	public void addMessage(String title, String msg) {
 		FacesContext.getCurrentInstance().addMessage("dekan-messages",
 				new FacesMessage(FacesMessage.SEVERITY_INFO, title, msg));
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
 	 * @return the deadline
 	 */
 	public Date getDeadline() {
 		return deadline;
 	}
 
 	/**
 	 * @param deadline the deadline to set
 	 */
 	public void setDeadline(Date deadline) {
 		this.deadline = deadline;
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
 	 * @return the exRulesTitle
 	 */
 	public int getExRulesTitle() {
 		return exRulesTitle;
 	}
 
 	/**
 	 * @param exRulesTitle the exRulesTitle to set
 	 */
 	public void setExRulesTitle(int exRulesTitle) {
 		this.exRulesTitle = exRulesTitle;
 	}
 
 
 	/**
 	 * @return the exRules
 	 */
 	public String getExRules() {
 		return exRules;
 	}
 
 
 	/**
 	 * @param exRules the exRules to set
 	 */
 	public void setExRules(String exRules) {
 		this.exRules = exRules;
 	}
 
 	/**
 	 * @return the exRulesList
 	 */
 	public List<ExRules> getExRulesList() {
 		return exRulesList;
 	}
 
 	/**
 	 * @param exRulesList the exRulesList to set
 	 */
 	public void setExRulesList(List<ExRules> exRulesList) {
 		this.exRulesList = exRulesList;
 	}
 
 	/**
 	 * @return the currentER
 	 */
 	public ExRules getCurrentER() {
 		return currentER;
 	}
 
 	/**
 	 * @param currentER the currentER to set
 	 */
 	public void setCurrentER(ExRules currentER) {
 		this.currentER = currentER;
 	}
 
 	/**
 	 * @return the currentMM
 	 */
 	public ModManual getCurrentMM() {
 		return currentMM;
 	}
 
 	/**
 	 * @param currentMM the currentMM to set
 	 */
 	public void setCurrentMM(ModManual currentMM) {
 		this.currentMM = currentMM;
 	}
 	
 	
 
 }
