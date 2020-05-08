 package edu.cmu.square.client.ui.assetsAndGoals;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.dom.client.ChangeEvent;
 import com.google.gwt.event.dom.client.ChangeHandler;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.user.client.Command;
 import com.google.gwt.user.client.History;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.rpc.ServiceDefTarget;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.FlexTable;
 import com.google.gwt.user.client.ui.HasHorizontalAlignment;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.ListBox;
 import com.google.gwt.user.client.ui.Panel;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import com.google.gwt.user.client.ui.FlexTable.FlexCellFormatter;
 
 import edu.cmu.square.client.exceptions.SquareException;
 import edu.cmu.square.client.model.GwtAsset;
 import edu.cmu.square.client.model.GwtBusinessGoal;
 import edu.cmu.square.client.model.GwtSubGoal;
 import edu.cmu.square.client.navigation.State;
 import edu.cmu.square.client.remoteService.step.interfaces.IdentifyGoalsAssetsService;
 import edu.cmu.square.client.remoteService.step.interfaces.IdentifyGoalsAssetsServiceAsync;
 import edu.cmu.square.client.ui.ChooseStep.ChooseStepPilot;
import edu.cmu.square.client.ui.ChooseStepCase3.ChooseStepCase3Pilot;
 import edu.cmu.square.client.ui.core.BasePane;
 import edu.cmu.square.client.ui.core.SquareHyperlink;
 import edu.cmu.square.client.ui.risksAssessment.RiskAssessmentPaneMessages;
 
 public class AssociateAssetsWithGoalsPane extends BasePane  
 {
 	private IdentifyGoalsAssetsServiceAsync service = GWT.create(IdentifyGoalsAssetsService.class);
 	private GwtBusinessGoal businessGoal;
 	private boolean duplicatePriorities,traceabilityViolation;
 	private VerticalPanel baseLayout = new VerticalPanel();
 	private VerticalPanel associateAssetsPanel = new VerticalPanel();
 	final RiskAssessmentPaneMessages messages1 = (RiskAssessmentPaneMessages) GWT.create(RiskAssessmentPaneMessages.class);
 	private IdentifyAssetsAndGoalsPaneMessages messages = (IdentifyAssetsAndGoalsPaneMessages)GWT.create(IdentifyAssetsAndGoalsPaneMessages.class);
 	
 	private FlexTable assetAssociationTable;
 	private boolean updating = false;
 	
     private int projectID;
     private List<CustomizedVerticalPanel> listSubGoalCells = new ArrayList<CustomizedVerticalPanel>();
     private FetchBusinessGoalFromDB commandClass;
 
 
     
 	public AssociateAssetsWithGoalsPane(State stateInfo) 
 	{
 		super(stateInfo);
 		this.projectID = getCurrentState().getProjectID();
 	
 		commandClass = new FetchBusinessGoalFromDB(this, businessGoal, updating);
 		
 		commandClass.selectCommandToExecute("showAssetAssociationGoalPage");
 		updateBusinessGoalFromDB(commandClass);
 	}
 	
 	
 	void showAssetAssociationGoalPage()
 	{
 		duplicatePriorities = false;
 		this.getContent().clear();
 		baseLayout.setWidth("500px");
 		associateAssetsPanel.setWidth("100%");
 		
 		baseLayout.add(renderColumnHeading());
 		baseLayout.add(associateAssetsPanel);
 		
 		associateAssetsPanel.setStyleName("square-flex");
 		
 		
 		for(int i=0; i<businessGoal.getSubGoals().size();i++)
 		{
 		    renderRowForEachSubGoal(associateAssetsPanel, businessGoal.getSubGoals().get(i));
 		}
 		
 		baseLayout.add(NavigationButtons(baseLayout));
 	
 		this.getContent().add(baseLayout);
 	}
 
 
 	
 	private HorizontalPanel renderColumnHeading()
 	{
 		HorizontalPanel columnHeadingPanel = new HorizontalPanel();
 		FlexTable columnTable = new FlexTable();
 		
 		Label subGoalTitle = new Label(messages.securityGoal());
 		subGoalTitle.setStyleName("square-TableHeader");
 		
 		Label priorityTitle = new Label(messages.priority());
 		priorityTitle.setStyleName("square-TableHeader");
 		
 		Label associationTitle = new Label(messages.association());
 		associationTitle.setStyleName("square-TableHeader");
 		
 		columnTable.setWidget(0, 0, subGoalTitle);
 		columnTable.setWidget(0, 1, priorityTitle);
 		columnTable.setWidget(0, 2, associationTitle);
 		columnHeadingPanel.setWidth("100%");
 		columnTable.setWidth("100%");
 		
 		FlexCellFormatter formatter = columnTable.getFlexCellFormatter();
 		formatter.setWidth(0, 0, "70%");
 		formatter.setWidth(0, 1, "10%");
 		formatter.setWidth(0, 2, "20%");
 		columnHeadingPanel.add(columnTable);
 		columnHeadingPanel.setStyleName("summary-column-heading");
 		return columnHeadingPanel;
 
 	}
 
 
 	void renderRowForEachSubGoal(VerticalPanel base, GwtSubGoal gwtSubGoalTemp)
 	{
 		CustomizedVerticalPanel subGoalCell = new CustomizedVerticalPanel();
 		subGoalCell.setCellBusinessGoal(businessGoal);
 		subGoalCell.setCellSubGoal(gwtSubGoalTemp);
 		subGoalCell.setPriorityListBox();
 		subGoalCell.initializeLink();
 		subGoalCell.setStyleName("inner-table");
 		
 		FlexTable goalPriorityAssociationLink = new FlexTable();
 		Label subGoalDesc = new Label(subGoalCell.getCellSubGoal().getDescription());
 		subGoalDesc.setStyleName("section-heading");
 		
 		goalPriorityAssociationLink.setWidget(0,0,subGoalDesc);
 		goalPriorityAssociationLink.setWidget(0,1,subGoalCell.getPriorityListBox());
 		goalPriorityAssociationLink.setWidget(0,2,subGoalCell.getLink());
 		subGoalCell.setWidth("100%");
 		listSubGoalCells.add(subGoalCell);
 		goalPriorityAssociationLink.setWidth("100%");
 		
 		FlexCellFormatter formatter = goalPriorityAssociationLink.getFlexCellFormatter();
 		formatter.setWidth(0, 0, "70%");
 		formatter.setWidth(0, 1, "10%");
 		formatter.setWidth(0, 2, "20%");
 		
 		VerticalPanel associatedAssetsDisplayPanel = new VerticalPanel();
 		associatedAssetsDisplayPanel.setWidth("100%");
 		renderSubGoalsAndAssets(associatedAssetsDisplayPanel, subGoalCell,goalPriorityAssociationLink);
 		base.add(subGoalCell);
 	}
 
 	/**
 	 * Ensure that every subgoal must be associated with AT LEAST one asset and vice-versa
 	 */
 	private void checkAssetGoalTraceability()
 	{
 		traceabilityViolation = false;
 		List<GwtSubGoal> subGoalsList = businessGoal.getSubGoals();
 		List<GwtAsset> assetsList = businessGoal.getAssets();
 		for(int i=0;i<subGoalsList.size();i++)
 		{
 			if(subGoalsList.get(i).getAssets().size()==0)
 			{
 				Window.alert(messages.subgoalAssociationError(subGoalsList.get(i).getDescription()));
 				traceabilityViolation = true;
 				return;
 			}
 		}
 		
 		for(int i=0;i<assetsList.size();i++)
 		{
 		    GwtAsset referenceAsset = assetsList.get(i);
 		    boolean assetAssociatedWithSubGoal = false;
 			for(int j=0;j<subGoalsList.size();j++)
 			{
 				List<GwtAsset> assetListForSubGoal =  subGoalsList.get(j).getAssets();
 				for(int k=0;k<assetListForSubGoal.size();k++)
 				{
 					if(referenceAsset.getId().intValue() == assetListForSubGoal.get(k).getId().intValue())
 					{
 						assetAssociatedWithSubGoal = true;
 						break;
 					}
 				}
 				if(assetAssociatedWithSubGoal)
 				{
 					break;
 				}
 			}
 			if(!assetAssociatedWithSubGoal)
 			{
 				Window.alert(messages.assetAssociationError(referenceAsset.getDescription()));
 				traceabilityViolation = true;
 				return;
 			}		
 		}
 	}
 	
 	/**
 	 * Navigation Buttons to browse other pages
 	 * @param layout 
 	 * 
 	 * @return a table containing navigation buttons
 	 */
 	private FlexTable NavigationButtons(VerticalPanel layout)
 	{
 		FlexTable navigatorLayout = new FlexTable();
 		FlexCellFormatter formatterNavigator = navigatorLayout.getFlexCellFormatter();
 		navigatorLayout.setWidth("100%");
 
 		Button subGoalButton = new Button(messages.subGoalButton());
 		Button summaryButton = new Button(messages.done());
 		subGoalButton.setWidth("170px");
 		summaryButton.setWidth("170px");
 
 		navigatorLayout.setWidget(0, 0, subGoalButton);
 		navigatorLayout.setWidget(0, 1, summaryButton);
 		formatterNavigator.setHorizontalAlignment(0, 0, HasHorizontalAlignment.ALIGN_LEFT);
 		formatterNavigator.setHorizontalAlignment(0, 1, HasHorizontalAlignment.ALIGN_RIGHT);
 
 		subGoalButton.addClickHandler(new ClickHandler() 
 		{
 			public void onClick(ClickEvent event) 
 			{
 				if(updating == true)
 				{
 					Window.alert("Updating values from Database...");
 					return;
 				}
 				if( commandClass!=null)
 				{
 					if(commandClass.getupdatedParentBusinessGoal()!=null)
 					{
 						businessGoal = commandClass.getupdatedParentBusinessGoal();
 					}
 				}
 				savePriorities();
 				checkAssetGoalTraceability();
 				if (duplicatePriorities || traceabilityViolation)
 				{
 					return;
 				}
 				History.newItem(AssetsAndGoalsPilot.generateNavigationId(AssetsAndGoalsPilot.PageId.addSubGoalsAndAssets));
 			}
 		});
 		
		final int caseid = this.getCurrentState().getCaseID();
 		summaryButton.addClickHandler(new ClickHandler()
 		{
 			public void onClick(ClickEvent event)
 			{
 				if (commandClass != null)
 				{
 					if (commandClass.getupdatedParentBusinessGoal() != null)
 					{
 						businessGoal = commandClass.getupdatedParentBusinessGoal();
 					}
 				}
 				
 /*				Window.alert((commandClass == null)+"");
 
 				Window.alert((businessGoal == null)+"");*/
 				savePriorities();
 				checkAssetGoalTraceability();
 				if (duplicatePriorities || traceabilityViolation)
 				{
 					return;
 				}
				
				if(caseid==1)
					History.newItem(ChooseStepPilot.generateNavigationId(ChooseStepPilot.PageId.home));
				if(caseid==3)
					History.newItem(ChooseStepCase3Pilot.generateNavigationId(ChooseStepCase3Pilot.PageId.home));
 			}
 		});
 		
 		return navigatorLayout;
 	}
 
 	private void renderSubGoalsAndAssets(VerticalPanel associatedAssetsDisplayPanel, CustomizedVerticalPanel subGoalCell, FlexTable goalPriorityAssociationLink)
 	{
 		assetAssociationTable = new FlexTable();
 		assetAssociationTable.setWidth("70%");
 		GwtSubGoal subGoalRef = null;
 
 		for(int j=0; j<businessGoal.getSubGoals().size(); j++)
 		{
 			if(businessGoal.getSubGoals().get(j).getId().intValue() == subGoalCell.getCellSubGoal().getId().intValue())
 			{
 				subGoalRef = businessGoal.getSubGoals().get(j);
 			}
 		}
 		if(subGoalRef == null)
 		{
 			Window.alert(messages.subGoalsCommitError());
 		}
 		Label temporary;
 		Collections.sort(subGoalRef.getAssets());
 		for(int k=0; k<subGoalRef.getAssets().size(); k++)
 		{
 			temporary = new Label(subGoalRef.getAssets().get(k).getDescription());
 			assetAssociationTable.setWidget(k,0,temporary);
 		}
 		
 		subGoalCell.add(goalPriorityAssociationLink);
 		subGoalCell.add(associatedAssetsDisplayPanel);
 		subGoalCell.setAssetAssociationTable(assetAssociationTable);
 		
 		associatedAssetsDisplayPanel.clear();
 		associatedAssetsDisplayPanel.add(assetAssociationTable);
 	}
 	
 	private void savePriorities()
 	{
 		duplicatePriorities = false;
 		for (int i = 0; i < listSubGoalCells.size(); i++)
 		{
 			for (int j = i + 1; j < listSubGoalCells.size(); j++)
 			{
 				if (listSubGoalCells.get(i).getCellSubGoal().getPriority().intValue() == listSubGoalCells.get(j).getCellSubGoal().getPriority().intValue())
 				{
 					if(listSubGoalCells.get(i).getCellSubGoal().getPriority()!=-1)
 					{
 						Window.alert(messages.duplicatePrioritiesError());
 					    duplicatePriorities = true;
 					    return;
 					}
 				}
 			}
 		}
 				
 		ServiceDefTarget endpoint = (ServiceDefTarget)service;
 		endpoint.setServiceEntryPoint(GWT.getModuleBaseURL() + "identifyGoalAssetService.rpc");	
 		
 		for (int i = 0; i < listSubGoalCells.size(); i++)
 		{
 			//listOfListBox.get(i).updateSubGoalPriority();
 			service.updateSubGoal(this.getCurrentState().getProjectID(), listSubGoalCells.get(i).getCellSubGoal(),new AsyncCallback<Void>()
 			{
 				
 				public void onFailure(Throwable caught)
 				{		
 					if (caught instanceof SquareException) {
 						SquareException se = (SquareException) caught;
 						switch (se.getType()) {
 						case authorization:
 							Window.alert(messages.errorAuthorization());
 							break;
 					
 						default:
 							Window.alert(messages.errorUpdatingSubGoal());
 							break;
 						}
 
 					} else {
 						Window.alert(messages.errorUpdatingSubGoal());
 					}
 				}
 
 				
 				public void onSuccess(Void result) 
 				{
 					// data is already in the GUI, nothing to update.					
 				}		
 			});		
 		}
 	}
 	
 	private class CustomizedVerticalPanel extends VerticalPanel
 	{
 		GwtSubGoal cellSubGoal;
 		ListBox priorityListBox = new ListBox();
 		SquareHyperlink link = new SquareHyperlink(messages.associateAssetsLink());
 		GwtBusinessGoal cellBusinessGoal;	
 		FetchBusinessGoalFromDB refresher;
 		FlexTable assetAssociationTable;
 				
 		SquareHyperlink getLink()
 		{
 			return this.link;
 		}
 		
 		public void setAssetAssociationTable(FlexTable assetAssociationTable)
 		{
 			this.assetAssociationTable = assetAssociationTable;
 		}
 		
 		public FlexTable setAssetAssociationTable()
 		{
 			return this.assetAssociationTable;
 		}
 
 		public void setRefreshDisplayCommand(FetchBusinessGoalFromDB refresher) 
 		{
 			this.refresher = refresher;
 		}
 
 		void setCellSubGoal(GwtSubGoal tempGoal)
 		{
 			this.cellSubGoal = tempGoal;
 		}
 		
 		void setCellBusinessGoal(GwtBusinessGoal businessGoalTemp) 
 		{
 			this.cellBusinessGoal = businessGoalTemp;
 		}
 		
 		GwtBusinessGoal getCellBusinessGoal(GwtBusinessGoal businessGoalTemp) 
 		{
 			return this.cellBusinessGoal;
 		}
 
 		GwtSubGoal getCellSubGoal()
 		{
 			return this.cellSubGoal;
 		}	
 		
 	    void setPriorityListBox()
 		{
 			this.priorityListBox.addItem(" ", "-1");
 			for (int k = 1; k <= this.cellBusinessGoal.getSubGoals().size(); k++)
 			{
 				this.priorityListBox.addItem(k+"",k+"");
 			}
 			
 			if(this.cellSubGoal.getPriority().intValue()!=-1)
 			{
 				this.priorityListBox.setSelectedIndex(this.cellSubGoal.getPriority().intValue());
 			}
 			else
 			{
 				this.priorityListBox.setSelectedIndex(0);
 			}
 			
 			this.priorityListBox.addChangeHandler(new ChangeHandler()
 			{
 				public void onChange(ChangeEvent event)
 				{
 					int valueSelected = priorityListBox.getSelectedIndex();
 					if(valueSelected == 0)
 					{
 						valueSelected =-1;
 					}
 					cellSubGoal.setPriority(valueSelected);
 				}
 			});
 		}
 	    
 	    ListBox getPriorityListBox()
 		{
 			return this.priorityListBox;
 		}
 	    
 	    
 	    void initializeLink()
 	    {
 	    	this.link.addClickHandler(new ClickHandler()
 	    	{
 				public void onClick(ClickEvent event)
 				{
 					commandClass.selectCommandToExecute("RefreshAssetsPane");
 					new AssociateAssetDialog(businessGoal, projectID, cellSubGoal, commandClass,assetAssociationTable); 
 				}
 	    	});
 	    }
 	}
 	
 	private void updateBusinessGoalFromDB(final Command command)
 	{
 		this.getContent().clear();
 		this.showLoadingStatusBar();
 		final Panel temp = this.getContent();
 		ServiceDefTarget endpoint = (ServiceDefTarget) service;
 		endpoint.setServiceEntryPoint(GWT.getModuleBaseURL() + "identifyGoalAssetService.rpc");	
 		service.loadBusinessGoalInfo(getCurrentState().getProjectID(), new AsyncCallback<GwtBusinessGoal>()
 		{
 			public void onFailure(Throwable caught)
             {
             	Window.alert(caught.getMessage());
             }
 			public void onSuccess(GwtBusinessGoal result)
 			{
 				businessGoal = result;
 				temp.clear();
 				if(command!=null)
 				{	hideStatusBar();
 					command.execute();
 				}
 			}
 		 });
 	}
 }
 
 class FetchBusinessGoalFromDB implements Command
 {
 	AssociateAssetsWithGoalsPane referenceClass;
 	String commandToExecute;
 	private VerticalPanel base;
 	private GwtSubGoal gwtSubGoalTemp,subGoalToBeUpdated;
 	private GwtBusinessGoal goal, parentReference;
 	private FlexTable assetAssociationTable;
 	@SuppressWarnings("unused")
 	private boolean updating;
 	
 	
 	
     public FetchBusinessGoalFromDB(AssociateAssetsWithGoalsPane pane, GwtBusinessGoal temp, boolean updating)
 	{
     	commandToExecute = "showAssetAssociationGoalPage";
     	this.referenceClass = pane;
     	this.parentReference = temp;
     	this.updating = updating;
 	}
     
     public FetchBusinessGoalFromDB(AssociateAssetsWithGoalsPane pane,VerticalPanel base, GwtSubGoal gwtSubGoalTemp)
 	{
     	commandToExecute = "RefreshAssetsPane";
     	this.referenceClass = pane;
     	this.base = base;
     	this.gwtSubGoalTemp = gwtSubGoalTemp;
     	
     	
 	}
     
     public void selectCommandToExecute(String cmd)
     {
     	commandToExecute = cmd;
     }
 	
 	public void execute()
 	{
 		this.updating = true;
 		this.parentReference = this.goal;
 		if(commandToExecute.trim().equalsIgnoreCase("showAssetAssociationGoalPage"))
 		{
 			this.referenceClass.showAssetAssociationGoalPage();
 		}
 		if(commandToExecute.trim().equalsIgnoreCase("RefreshAssetsPane"))
 		{
 			parentReference = this.goal;
 			for(int i=0; i<goal.getSubGoals().size();i++)
 			{
 				if(goal.getSubGoals().get(i).getId().intValue() == subGoalToBeUpdated.getId().intValue())
 				{
 					subGoalToBeUpdated = goal.getSubGoals().get(i);
 					}	
 				}
 				List<GwtAsset> assets = subGoalToBeUpdated.getAssets();
 				Collections.sort(assets);
 				assetAssociationTable.clear();
 				for(int k=0; k<assets.size();k++)
 				{
 					assetAssociationTable.setWidget(k, 0, new Label(assets.get(k).getDescription()));
 				}
 		}		
 		this.updating = false;
 	}
 	
 	
 	public GwtBusinessGoal getupdatedParentBusinessGoal()
 	{
 		return this.goal;
 	}
 
 	public void setUpTableRenderer(GwtBusinessGoal goal, FlexTable assetAssociationTable, GwtSubGoal subGoal)
 	{
 		this.goal = goal;
 		this.assetAssociationTable = assetAssociationTable;
 		this.subGoalToBeUpdated = subGoal;
 	}
 }
