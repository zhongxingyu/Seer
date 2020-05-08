 package server;
 
 import java.util.LinkedList;
 import java.util.List;
 
 import javax.faces.application.FacesMessage;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.SessionScoped;
 import javax.faces.context.FacesContext;
 import javax.faces.event.ActionEvent;
 
 import ctrl.DBModAccess;
 import ctrl.DBModule;
 import ctrl.DBUser;
 
 import data.ModAccess;
 import data.User;
 
 @ManagedBean(name = "AccessBean")
 @SessionScoped
 public class AccessBean {
 	//Display users in SelectOneMenus
 	private List<String> userList;
	
 	private List<ModAccess> modAccList;
 	private List<String> modTitleList;
 
 	private String selectedUserAccept;
 	private String selectedUserFilter;
 	private String selectedAccess;
	
 	private ModAccess deleteMod;
 	/**
 	 * preload all lists that are being used to display stuff
 	 */
 	public AccessBean() {
 		// TODO Remove link between adam.admin and the testmodule in the
 		// database.
 		userList = DBUser.loadAllUserEmailsByRole("Modulverantwortlicher");
 		new LinkedList<User>();
 
 		// TODO evtl abgrenzen noch vorher handbuch aussuchen?
 		modTitleList = DBModule.loadAllModuleTitles();
 
 		System.out.println(userList.toString());
 
 		modAccList=DBModAccess.loadAccess();
 
 		System.out.println(modAccList.toString());
 
 	}
 	/**
 	 * Adds another module to the users responsibility
 	 * @param e
 	 */
 	public void acceptChanges(ActionEvent e) {
 
 		boolean alreadySet = false;
 		System.out.println(selectedUserAccept + "\t" + selectedAccess
 				+ " is selected");
 		if (!userList.contains(selectedUserAccept)
 				|| !modTitleList.contains(selectedAccess)) {
 			addErrorMessage("messages-access", "Failed",
 					"you need to select both - a user and a module. ");
 			return;
 		}
 		List<ModAccess> modAccessSelectedUser = DBModAccess
 				.loadAccess(selectedUserAccept);
 		for (ModAccess m : modAccessSelectedUser) {
 			if (m.getModTitle().equals(selectedAccess)) {
 				System.out.println(m.getModTitle()+" "+selectedAccess);
 				System.out.println("already selected");
 				alreadySet = true;
 			}
 		}
 		if (!alreadySet) {
 			if (DBModAccess.saveModAccess(new ModAccess(selectedAccess,
					selectedUserAccept)))
 				addMessage("messages-access", "Success", selectedUserAccept
 						+ " is now able to edit things in " + selectedAccess);
			else 
 				addErrorMessage("messages-access", "Failed",
					"Error with the database - try again.");
 		}
 		else 
 			addErrorMessage("messages-access", "Failed",
 					"The selected User is already able to edit the selected Module.");
 	}
	
 	/**
 	 * Filters the List by only displaying the selectedUserFilter's module access rights
 	 * @param e
 	 */
 	public void filterSelectionList(ActionEvent e){
 		if(selectedUserFilter.equals("-1")){
 			System.out.println("none selected - display all");
 			modAccList=DBModAccess.loadAccess();
 		}
 		else{
 			System.out.println("filter-else use email:"+selectedUserFilter);
 			modAccList=DBModAccess.loadAccess(selectedUserFilter);
 		}
			
 	}
	
 	/**
 	 * deletes the row that got selected - is confirmed by a dialog
 	 */
 	public void deleteRow(){
 		System.out.println("delete \t "+deleteMod);
 		DBModAccess.deleteModAccess(deleteMod.getModTitle(), deleteMod.getEmail());
 	}
 
 	/**
 	 * if anything goes wrong - display an Error
 	 * 
 	 * @param title
 	 * @param msg
 	 */
 	public void addErrorMessage(String toFor, String title, String msg) {
 		FacesContext.getCurrentInstance().addMessage(toFor,
 				new FacesMessage(FacesMessage.SEVERITY_FATAL, title, msg));
 	}
 
 	/**
 	 * Informs the User via message.
 	 * 
 	 * @param title
 	 * @param msg
 	 */
 	public void addMessage(String toFor, String title, String msg) {
 		FacesContext.getCurrentInstance().addMessage(toFor,
 				new FacesMessage(FacesMessage.SEVERITY_INFO, title, msg));
 	}
 
 	/**
 	 * @return the modAccList
 	 */
 	public List<ModAccess> getModAccList() {
 		return modAccList;
 	}
 
 	/**
 	 * @param modAccList
 	 *            the modAccList to set
 	 */
 	public void setModAccList(List<ModAccess> modAccList) {
 		this.modAccList = modAccList;
 	}
 
 	/**
 	 * @return the modAcc
 	 */
 	public List<String> getModTitleList() {
 		return modTitleList;
 	}
 
 	/**
 	 * @param modAcc
 	 *            the modAcc to set
 	 */
 	public void setModTitleList(List<String> modAcc) {
 		this.modTitleList = modAcc;
 	}
 
 	/**
 	 * @return the selectedAccess
 	 */
 	public String getSelectedAccess() {
 		return selectedAccess;
 	}
 
 	/**
 	 * @param selectedAccess
 	 *            the selectedAccess to set
 	 */
 	public void setSelectedAccess(String selectedAccess) {
 		this.selectedAccess = selectedAccess;
 	}
 
 	/**
 	 * @return the userList
 	 */
 	public List<String> getUserList() {
 		return userList;
 	}
 
 	/**
 	 * @param userList
 	 *            the userList to set
 	 */
 	public void setUserList(List<String> userList) {
 		this.userList = userList;
 	}
 
 
 
 	/**
 	 * @return the selectedUserAccept
 	 */
 	public String getSelectedUserAccept() {
 		return selectedUserAccept;
 	}
 
 	/**
 	 * @param selectedUserAccept the selectedUserAccept to set
 	 */
 	public void setSelectedUserAccept(String selectedUserAccept) {
 		this.selectedUserAccept = selectedUserAccept;
 	}
 
 	/**
 	 * @return the deleteMod
 	 */
 	public ModAccess getDeleteMod() {
 		return deleteMod;
 	}
 
 	/**
 	 * @param deleteMod the deleteMod to set
 	 */
 	public void setDeleteMod(ModAccess deleteMod) {
 		System.out.println(deleteMod.toString());
 		this.deleteMod = deleteMod;
 	}
 	/**
 	 * @return the selectedUserFilter
 	 */
 	public String getSelectedUserFilter() {
 		return selectedUserFilter;
 	}
 	/**
 	 * @param selectedUserFilter the selectedUserFilter to set
 	 */
 	public void setSelectedUserFilter(String selectedUserFilter) {
 		this.selectedUserFilter = selectedUserFilter;
 	}
 
 }
