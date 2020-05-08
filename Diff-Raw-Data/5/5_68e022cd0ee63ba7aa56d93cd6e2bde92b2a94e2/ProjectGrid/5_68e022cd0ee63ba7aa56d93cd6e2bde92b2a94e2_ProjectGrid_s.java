 package edu.cmu.square.client.ui.ManageSite;
 
 import java.util.List;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.user.client.Command;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.rpc.ServiceDefTarget;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.FlexTable;
 import com.google.gwt.user.client.ui.HasHorizontalAlignment;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.Label;
 
 import edu.cmu.square.client.exceptions.ExceptionHelper;
 import edu.cmu.square.client.model.GwtProject;
 import edu.cmu.square.client.model.GwtUser;
 import edu.cmu.square.client.remoteService.interfaces.ManageProjectService;
 import edu.cmu.square.client.remoteService.interfaces.ManageProjectServiceAsync;
 import edu.cmu.square.client.remoteService.interfaces.ManageSiteService;
 import edu.cmu.square.client.remoteService.interfaces.ManageSiteServiceAsync;
 import edu.cmu.square.client.ui.core.BasePane;
 import edu.cmu.square.client.ui.core.SquareConfirmDialog;
 import edu.cmu.square.client.ui.core.SquareHyperlink;
 
 public class ProjectGrid extends Composite implements Command
 {
 	private BasePane caller = null;
 
 	private FlexTable projectTable = new FlexTable();
 
 	final ManageSitePaneMessages messages = (ManageSitePaneMessages) GWT.create(ManageSitePaneMessages.class);
 	private ManageProjectServiceAsync manageProjectService = GWT.create(ManageProjectService.class);
 	private ManageSiteServiceAsync manageSiteService = GWT.create(ManageSiteService.class);
 
 	private SquareConfirmDialog confirmDialog = null;
 	private SquareConfirmDialog copyConfirmDialog = null;
 	
 	
 	private int lastRowClicked = -1;
 	private int lastProjectIdClicked = -1;
 
 	protected List<GwtProject> listOfProjects = null;
 
 	public ProjectGrid(BasePane caller)
 		{
 			this.caller = caller;
 			this.caller.showLoadingStatusBar();
 
 			this.projectTable.setWidth("100%");
 			this.projectTable.setCellSpacing(0);
 			this.projectTable.setStyleName("square-flex");
 
 			this.setHeaderInTable();
 			this.getAllProjects();
 
 			initWidget(this.projectTable);
 		}
 
 	private void setHeaderInTable()
 	{
 		projectTable.getRowFormatter().setStyleName(0, "square-TableHeader");
 
 		Button createButton = new Button(messages.createProject());
 
 		projectTable.setWidget(0, 0, createButton);
 		projectTable.setWidget(0, 1, new Label(messages.projectCase()));
 		projectTable.setWidget(0, 2, new Label(messages.projectType()));
 		projectTable.setWidget(0, 3, new Label(messages.leadRequirementsEngineer()));
		projectTable.getFlexCellFormatter().setColSpan(0, 3, 3);
 
 		createButton.addClickHandler(new ClickHandler()
 			{
 				@Override
 				public void onClick(ClickEvent event)
 				{
 					lastRowClicked = -1;
 					loadCreateDialog(new GwtProject());
 				}
 			});
 		
 		this.projectTable.getCellFormatter().setHorizontalAlignment(0,1,	HasHorizontalAlignment.ALIGN_CENTER);
 		this.projectTable.getCellFormatter().setHorizontalAlignment(0,2,	HasHorizontalAlignment.ALIGN_CENTER);
 		
		this.projectTable.getCellFormatter().setHorizontalAlignment(0,3,	HasHorizontalAlignment.ALIGN_LEFT);
 		
 	}
 
 	public void createProject(GwtProject newProject)
 	{
 		this.caller.showStatusBar(messages.creatingProject());
 		ServiceDefTarget endpoint = (ServiceDefTarget) manageProjectService;
 		endpoint.setServiceEntryPoint(GWT.getModuleBaseURL() + "manageProject.rpc");
 		manageProjectService.createProject(newProject, new AsyncCallback<GwtProject>()
 			{
 				public void onFailure(Throwable caught)
 				{
 					ExceptionHelper.SquareRootRPCExceptionHandler(caught, messages.creatingProject());
 					caller.hideStatusBar();
 				}
 
 				public void onSuccess(GwtProject result)
 				{
 					listOfProjects.add(result);
 					addProjectToTable(result);
 					caller.yellowFadeHandler.add(projectTable, projectTable.getRowCount() - 1);
 					caller.hideStatusBar();
 				}
 			});
 	}
 	
 //Copy project
 	public void copyProject(GwtProject originalProject)
 	{
 		//System.out.println("projectgrid copyproject");
 		this.caller.showStatusBar(messages.copyProject());
 		ServiceDefTarget endpoint = (ServiceDefTarget) manageProjectService;
 		endpoint.setServiceEntryPoint(GWT.getModuleBaseURL() + "manageProject.rpc");
 		manageProjectService.copyProject(originalProject, new AsyncCallback<GwtProject>()
 			{
 				public void onFailure(Throwable caught)
 				{
 					ExceptionHelper.SquareRootRPCExceptionHandler(caught, messages.copyProject());
 					caller.hideStatusBar();
 				}
 
 				public void onSuccess(GwtProject result)
 				{
 					//System.out.println("On success copying project");
 					listOfProjects.add(result);
 					addProjectToTable(result);
 					caller.yellowFadeHandler.add(projectTable, projectTable.getRowCount() - 1);
 					
 					caller.hideStatusBar();
 				}
 			});
 	}
 
 	public void updateProject(int projectId, int newLeadRequirementEngineerId, String projectName)
 	{
 		this.caller.showStatusBar(messages.creatingProject());
 		ServiceDefTarget endpoint = (ServiceDefTarget) manageProjectService;
 		endpoint.setServiceEntryPoint(GWT.getModuleBaseURL() + "manageProject.rpc");
 		manageProjectService.updateProject(projectId, newLeadRequirementEngineerId, projectName, new AsyncCallback<GwtProject>()
 			{
 				public void onFailure(Throwable caught)
 				{
 					ExceptionHelper.SquareRootRPCExceptionHandler(caught, messages.creatingProject());
 					caller.hideStatusBar();
 				}
 
 				@Override
 				public void onSuccess(GwtProject result)
 				{
 					listOfProjects.set(lastRowClicked - 1, result);
 					updateProjectToTable(lastRowClicked, result);
 					caller.yellowFadeHandler.add(projectTable, lastRowClicked);
 					caller.hideStatusBar();
 				}
 			});
 	}
 
 	private void deleteProject(int projectId)
 	{
 		ServiceDefTarget endpoint = (ServiceDefTarget) manageProjectService;
 		endpoint.setServiceEntryPoint(GWT.getModuleBaseURL() + "manageProject.rpc");
 
 		manageProjectService.deleteProject(projectId, new AsyncCallback<Void>()
 			{
 				public void onFailure(Throwable caught)
 				{
 					ExceptionHelper.SquareRootRPCExceptionHandler(caught, messages.deletingProject());
 					caller.hideStatusBar();
 
 				}
 				public void onSuccess(Void result)
 				{
 					projectTable.removeRow(lastRowClicked);
 					listOfProjects.remove(lastRowClicked - 1);
 					caller.hideStatusBar();
 
 				}
 			});
 	}
 
 	private void loadCreateDialog(final GwtProject project)
 	{
 		final ProjectGrid caller = this;
 
 		ServiceDefTarget endpoint = (ServiceDefTarget) manageSiteService;
 		endpoint.setServiceEntryPoint(GWT.getModuleBaseURL() + "manageSite.rpc");
 
 		manageSiteService.getAllUsers(new AsyncCallback<List<GwtUser>>()
 			{
 				public void onFailure(Throwable caught)
 				{
 					ExceptionHelper.SquareRootRPCExceptionHandler(caught, messages.retrievingUsers());
 				}
 
 				public void onSuccess(List<GwtUser> result)
 				{
 					if (project.isInDatabase())
 					{
 						CreateProjectDialog dialog = new CreateProjectDialog(project.getId(), project.getName(),project.getAcquisitionOrganizationEngineer().getUserId(), listOfProjects, result, caller, project.getCases().getId());
 						dialog.center();
 						dialog.setModal(true);
 						dialog.show();
 						
 					}
 					else
 					{
 						CreateProjectDialog dialog = new CreateProjectDialog( listOfProjects, result, caller);
 						dialog.center();
 						dialog.setModal(true);
 						dialog.show();
 					}
 
 				}
 			});
 	}
 
 	private void getAllProjects()
 	{
 		ServiceDefTarget endpoint = (ServiceDefTarget) manageProjectService;
 		endpoint.setServiceEntryPoint(GWT.getModuleBaseURL() + "manageProject.rpc");
 
 		manageProjectService.getAllProjects(new AsyncCallback<List<GwtProject>>()
 			{
 				public void onFailure(Throwable caught)
 				{
 					ExceptionHelper.SquareRootRPCExceptionHandler(caught, messages.retrievingAllProjects());
 				}
 
 				public void onSuccess(List<GwtProject> result)
 				{
 					listOfProjects = result;
 					for (GwtProject currentProject : result)
 					{
 						addProjectToTable(currentProject);
 					}
 
 					caller.hideStatusBar();
 				}
 			});
 	}
 
 	private void addProjectToTable(final GwtProject project)
 	{
 		int row = this.projectTable.getRowCount();
 
 		updateProjectToTable(row, project);
 	}
 
 	private void updateProjectToTable(int row, final GwtProject project)
 	{
 
 		this.projectTable.setWidget(row, 0, new Label(project.getName()));
 
 		Label type = new Label();
 		if (project.isPrivacy() && !project.isSecurity())
 		{
 			type.setText(messages.privacyOnly());
 		}
 		else if (!project.isPrivacy() && project.isSecurity())
 		{
 			type.setText(messages.securityOnly());
 		}
 		else
 		{
 			type.setText(messages.securityAndPrivacy());
 		}
 
 		//Label projectase = new Label();
 		String caseString = Integer.toString(project.getCases().getId());
 		this.projectTable.setWidget(row, 1, new Label(caseString));
 		this.projectTable.setWidget(row, 2, type);
 		this.projectTable.setWidget(row, 3, new Label(project.getAcquisitionOrganizationEngineer().getFullName()));
 
 		final Command caller = this;
 		
 
 		SquareHyperlink deleteProjectLink = new SquareHyperlink(messages.permanentlyDelete());
 		deleteProjectLink.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
 		deleteProjectLink.addClickHandler(new ClickHandler()
 			{
 				public void onClick(ClickEvent event)
 				{
 					lastRowClicked = projectTable.getCellForEvent(event).getRowIndex();
 					lastProjectIdClicked = project.getId();
 
 					confirmDialog = new SquareConfirmDialog(
 							caller, 
 							messages.confirmDeleteProject(project.getName()), 
 							messages.deleteForever(),
 							messages.cancelDeleteProject());
 					confirmDialog.setText(messages.confirmDeleteDialogTitle());
 					confirmDialog.center();
 					confirmDialog.show();
 
 				}
 			});
 
 		SquareHyperlink editProjectLink = new SquareHyperlink(messages.edit());
 		editProjectLink.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
 		editProjectLink.addClickHandler(new ClickHandler()
 			{
 				public void onClick(ClickEvent event)
 				{
 					lastRowClicked = projectTable.getCellForEvent(event).getRowIndex();
 					lastProjectIdClicked = project.getId();
 					loadCreateDialog(project);
 				}
 			});
 		
 		SquareHyperlink copyProjectLink = new SquareHyperlink(messages.copyProject());
 		copyProjectLink.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
 		copyProjectLink.addClickHandler(new ClickHandler()
 			{
 				public void onClick(ClickEvent event)
 				{
 					System.out.println("done0");
 					
 					lastRowClicked = projectTable.getCellForEvent(event).getRowIndex();
 					lastProjectIdClicked = project.getId();
 					
 					System.out.println("done1");
 					
 					copyConfirmDialog = new SquareConfirmDialog(
 							caller, 
 							messages.confirmCopyProject(project.getName()), 
 							messages.confirmCopy(),
 							messages.cancelCopy()
 					);
 					copyConfirmDialog.setText(messages.confirmCopyDialogTitle());
 					copyConfirmDialog.center();
 					copyConfirmDialog.show();
 
 				}
 			});
 		
 
 		HorizontalPanel links = new HorizontalPanel();
 		links.setStyleName("flex-link-bar");
 		links.add(editProjectLink);
 		links.add(copyProjectLink);
 		links.add(deleteProjectLink);
 
 		this.projectTable.setWidget(row, 4, links);
 
 		this.projectTable.getCellFormatter().setHorizontalAlignment(row,1,	HasHorizontalAlignment.ALIGN_CENTER);
 		this.projectTable.getCellFormatter().setHorizontalAlignment(row,2,	HasHorizontalAlignment.ALIGN_CENTER);
 		this.projectTable.getCellFormatter().setHorizontalAlignment(row,3,	HasHorizontalAlignment.ALIGN_RIGHT);
 
 		this.projectTable.getCellFormatter().setHorizontalAlignment(row,4,	HasHorizontalAlignment.ALIGN_RIGHT);
 
 	}
 	
 
 	@Override
 	public void execute()
 	{
 		
 		if(copyConfirmDialog != null && copyConfirmDialog.isConfirmed())
 		{
 			caller.showStatusBar(messages.copying());
 			copyProject(listOfProjects.get(lastRowClicked-1));
 			copyConfirmDialog.setConfirmed(false);
 			
 		}
 		else if (confirmDialog != null && confirmDialog.isConfirmed())
 		{
 			caller.showStatusBar(messages.removing());
 			deleteProject(lastProjectIdClicked);
 			confirmDialog.setConfirmed(false);
 		}
 	
 		
 
 	}
 
 }
