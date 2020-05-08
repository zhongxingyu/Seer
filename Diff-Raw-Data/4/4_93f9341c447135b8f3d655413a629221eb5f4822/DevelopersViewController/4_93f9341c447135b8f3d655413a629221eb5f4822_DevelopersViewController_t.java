 package controller.view;
 
 import model.Developer;
 import persistency.Database;
 import utils.ActionUtils;
 import utils.Dialog;
 import utils.DialogChoice;
 import view.ViewContainer;
 import view.state.AbstractViewState;
 import view.state.DevelopersViewState;
 import controller.ControllerCollection;
 import exceptions.DeleteNonExistingException;
 
 public class DevelopersViewController extends AbstractViewController {
 
 	private DevelopersViewState viewState;
 	
 	public DevelopersViewController(Database database, ViewContainer viewContainer, ControllerCollection controllers) {
 		super(database, viewContainer, controllers);
 	}
 
 	@Override
 	public AbstractViewState getViewState() {
 		return viewState;
 	}
 
 	@Override
 	public void initialize() {
 		this.viewState = new DevelopersViewState();
 
 		ActionUtils.addListener(this.viewState.getDeleteButton(), this, "deleteSelectedDeveloper");
 		ActionUtils.addListener(this.viewState.getCreateButton(), this, "createNewDeveloper");
 		
 		this.fillDeveloperList();
 	}
 
 	private void fillDeveloperList() {
 		this.viewState.setDevelopers(this.database.developer().readAll());
 	}
 	
 	public void createNewDeveloper() {
 		String initialsInput = this.viewState.getInitialsInput().trim();
 		String nameInput = this.viewState.getNameInput().trim();
 		
 		if (initialsInput.length() == 0 || nameInput.length() == 0) {
 			Dialog.message("You must fill out both initials and name");
 			return;
 		}else if(initialsInput.length() > 4){
 			Dialog.message("The length of initials can at most be 4.");
 			return;
 		}
 		
 		if (this.database.developer().create(initialsInput, nameInput) == null) {
 			Dialog.message("Could not create developer");
 		}
 		this.fillDeveloperList();
 	}
 	
 	public void setViewState(DevelopersViewState viewState) {
 		this.viewState = viewState;
 	}
 	
 	public void deleteSelectedDeveloper() {
 		Developer sel = this.viewState.getSelectedDeveloper();
 		if (sel == null) {
 			Dialog.message("You must select a developer to delete");
 			return;
 		}
 		
 		if (sel.getId() == this.controllers.getLoginController().getUser().getId())
 		{
 			Dialog.message("You can't delete yourself. Lol.");
 			return;
 		}
 		
 		DialogChoice confirm = Dialog.confirm(String.format("Really delete %s?", sel.getName()));
 		if (confirm == DialogChoice.Yes) {
 			try {
 				sel.delete();
 			} catch (DeleteNonExistingException e) {
 				e.printStackTrace();
 			}
 		}
		
		this.fillDeveloperList();
 	}
 }
