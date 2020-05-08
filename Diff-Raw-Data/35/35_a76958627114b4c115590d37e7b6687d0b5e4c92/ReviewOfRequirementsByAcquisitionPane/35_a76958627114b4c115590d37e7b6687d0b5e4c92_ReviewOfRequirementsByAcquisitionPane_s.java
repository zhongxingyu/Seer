 package edu.cmu.square.client.ui.reviewOfRequirementsByAcquisitionOrganization;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.event.dom.client.KeyUpEvent;
 import com.google.gwt.event.dom.client.KeyUpHandler;
 import com.google.gwt.user.client.History;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.rpc.ServiceDefTarget;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.DisclosurePanel;
 import com.google.gwt.user.client.ui.FlexTable;
 import com.google.gwt.user.client.ui.HasHorizontalAlignment;
 import com.google.gwt.user.client.ui.HasVerticalAlignment;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import com.google.gwt.user.client.ui.Widget;
 
 import edu.cmu.square.client.exceptions.ExceptionHelper;
 import edu.cmu.square.client.model.GwtModesType;
 import edu.cmu.square.client.model.GwtProject;
 import edu.cmu.square.client.model.GwtRequirement;
 import edu.cmu.square.client.navigation.State;
 import edu.cmu.square.client.remoteService.step.interfaces.ReviewOfRequirementsByAcquisitionService;
 import edu.cmu.square.client.remoteService.step.interfaces.ReviewOfRequirementsByAcquisitionServiceAsync;
 import edu.cmu.square.client.ui.ChooseStep.ChooseStepPilot;
 import edu.cmu.square.client.ui.reviewOfRequirementsByAcquisitionOrganization.ReviewOfRequirementsByAcquisitionMessages;
 import edu.cmu.square.client.ui.reviewOfRequirementsByAcquisitionOrganization.ReviewOfRequirementsByAcquisitionPane;
 import edu.cmu.square.client.ui.reviewOfRequirementsByAcquisitionOrganization.ReviewOfRequirementsByAcquisitionPilot;
 //import edu.cmu.square.client.ui.reviewOfRequirementsByAcquisitionOrganization.ViewDetailDialog;
 //import edu.cmu.square.client.ui.reviewOfRequirementsByAcquisitionOrganization.ReviewOfRequirementsByAcquisitionPane.SummaryElementHyperLinkElement;
 import edu.cmu.square.client.ui.core.BasePane;
 import edu.cmu.square.client.ui.core.SquareHyperlink;
 import edu.cmu.square.client.ui.core.SquareWaterMarkTextBox;
 //import edu.cmu.square.client.ui.elicitSecurityRequirements.ElicitSecurityRequirementsPilot;
 //import edu.cmu.square.client.ui.elicitSecurityRequirements.ElicitSecurityRequirementSummaryPane.SummaryElementHyperLinkElement;
 
 public class ReviewOfRequirementsByAcquisitionPane extends BasePane
 {
 	private int projectID;
 	private List<GwtRequirement> listOfRequirements = new ArrayList<GwtRequirement>();
 	private List<GwtRequirement> listOfFilteredRequirements = new ArrayList<GwtRequirement>();
 
 	
 
 	private VerticalPanel vPane = new VerticalPanel();
 	private VerticalPanel vPaneData = new VerticalPanel();
 	private int modifyRequirementId = -1;
 
 	//private CreateRequirementDialog createRequirementDialog;
 	private GwtRequirement newRequirement;
 	private String lastSearch="";
 	
 	private int pageSize=10;
 	private int currentPage=1;
 	private int pageCount=0;
 	
 	
 	final ReviewOfRequirementsByAcquisitionMessages messages = (ReviewOfRequirementsByAcquisitionMessages) GWT.create(ReviewOfRequirementsByAcquisitionMessages.class);
 
 	public ReviewOfRequirementsByAcquisitionPane(State stateInfo)
 	{
 			super(stateInfo);
 			
 			this.projectID = this.getCurrentState().getProjectID();
 			SetQueryString();
 			this.showLoadingStatusBar();
 			loadRequirements();
 
 	}
 
 	public void SetQueryString()
 	{
 		
 			if(this.getQueryString().get("PageId")!=null)
 			{
 				
 					try
 					{
 					this.currentPage=Integer.parseInt(this.getQueryString().get("PageId"));
 					}
 					catch(Exception ex)
 					{
 						this.currentPage=1;
 					}
 				
 			}
 			
 			if(this.getQueryString().get("search")!=null)
 			{
 			 this.lastSearch=this.getQueryString().get("search");
 			}
 			
 	}
 	
 	// ------------------------------------------------------
 	// RPC Asynchronous calls section
 	// ------------------------------------------------------
 
 	public void loadRequirements()
 	{
 
 		ReviewOfRequirementsByAcquisitionServiceAsync service1 = GWT.create(ReviewOfRequirementsByAcquisitionService.class);
 		ServiceDefTarget endpoint = (ServiceDefTarget) service1;
 		//System.out.println("at loadRequirement()");
 		endpoint.setServiceEntryPoint(GWT.getModuleBaseURL() + "reviewOfRequirementsByAcquisitionService.rpc");
 
 		GwtProject project = new GwtProject();
 		project.setId(this.getCurrentState().getProjectID());
 
 		service1.getRequirements(project, new AsyncCallback<List<GwtRequirement>>()
 		{
 
 				public void onFailure(Throwable caught)
 				{
 					ExceptionHelper.SquareRootRPCExceptionHandler(caught, "Retriving Requirements");
 				}
 
 				public void onSuccess(List<GwtRequirement> result)
 				{
 					listOfRequirements = result;
 
 					initializePane();
 				}
 			});
 
 	}
 	
 /*0617
 	public void addRequirement(GwtRequirement gwtRequirement, GwtProject gwtProject)
 	{
 		this.showStatusBar("adding...");
 
 		ReviewOfRequirementsByAcquisitionServiceAsync service1 = GWT.create(ReviewOfRequirementsByAcquisitionService.class);
 		ServiceDefTarget endpoint = (ServiceDefTarget) service1;
 		endpoint.setServiceEntryPoint(GWT.getModuleBaseURL() + "reviewOfRequirementsByAcquisitionService.rpc");
 
 		GwtProject project = new GwtProject();
 		project.setId(this.getCurrentState().getProjectID());
 
 		service1.addRequirement(gwtProject, gwtRequirement, new AsyncCallback<GwtRequirement>()
 		{
 
 				public void onFailure(Throwable caught)
 				{
 					ExceptionHelper.SquareRootRPCExceptionHandler(caught, "Adding Requirements");
 
 				}
 
 				public void onSuccess(GwtRequirement result)
 				{
 					newRequirement = result;
 					modifyRequirementId = newRequirement.getId();
 					loadRequirements();
 
 				}
 			});
 
 	}
 */	
 
 	
 	public void removeRequirement(final GwtRequirement gwtRequirement)
 	{
 
 		this.showStatusBar("removing...");
 		ReviewOfRequirementsByAcquisitionServiceAsync service1 = GWT.create(ReviewOfRequirementsByAcquisitionService.class);
 		ServiceDefTarget endpoint = (ServiceDefTarget) service1;
 		endpoint.setServiceEntryPoint(GWT.getModuleBaseURL() + "reviewOfRequirementsByAcquisitionService.rpc");
 
 		GwtProject project = new GwtProject();
 		project.setId(this.getCurrentState().getProjectID());
 
 		service1.removeRequirement(gwtRequirement, new AsyncCallback<Void>()
 			{
 
 				public void onFailure(Throwable caught)
 				{
 					ExceptionHelper.SquareRootRPCExceptionHandler(caught, "Retriving Requirements");
 
 				}
 
 				@Override
 				public void onSuccess(Void result)
 				{
 
 					loadRequirements();
 				}
 
 			});
 
 	}
 
 	
 	public void updateRequirement(final GwtRequirement gwtRequirement, GwtProject gwtProject)
 	{
 
 		this.showStatusBar("updating...");
 
 		ReviewOfRequirementsByAcquisitionServiceAsync service1 = GWT.create(ReviewOfRequirementsByAcquisitionService.class);
 		ServiceDefTarget endpoint = (ServiceDefTarget) service1;
 		endpoint.setServiceEntryPoint(GWT.getModuleBaseURL() + "reviewOfRequirementsByAcquisitionService.rpc");
 
 		GwtProject project = new GwtProject();
 		project.setId(this.getCurrentState().getProjectID());
 
 		service1.updateRequirement(gwtProject, gwtRequirement, new AsyncCallback<Void>()
 			{
 
 				public void onFailure(Throwable caught)
 				{
 					ExceptionHelper.SquareRootRPCExceptionHandler(caught, "Retriving Requirements");
 				}
 
 				@Override
 				public void onSuccess(Void result)
 				{
 					modifyRequirementId = gwtRequirement.getId();
 					loadRequirements();
 				}
 			});
 	}
 
 	public void initializePane()
 
 	{
 		this.hideStatusBar();
 
 		this.getContent().add(vPane);
 		loadRequirementsTable();
 
 	}
 
 	public void loadRequirementsTable()
 	{
 
 		filterRequirements(lastSearch);
 
 		vPane.clear();
 		vPane.setSpacing(0);
 		vPane.setWidth("90%");
 		vPane.setHeight("5%");
 
 		vPane.add(getHeaderRow());
 		vPane.add(vPaneData);
 		loadRequirementTableData();
 		//addDoneButton();
 		
 		
 		
 
 	}
 
 	public void loadRequirementTableData()
 	{
 		vPaneData.clear();
 		vPaneData.setWidth("100%");
 
 		vPaneData.setSpacing(5);
 		int rowCount = 1;
 		for (GwtRequirement requirement : listOfFilteredRequirements)
 		{
 			vPaneData.add(getDataRow(rowCount, requirement));
 			rowCount++;
 		}
 
 		
 		if (rowCount == 1)
 		{
 
 			DisclosurePanel diclosure = new DisclosurePanel();
 
 			Label noRequirement = new Label(messages.noelementsFound());
 			noRequirement.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
 			diclosure.add(noRequirement);
 			vPaneData.add(diclosure);
 			diclosure.setAnimationEnabled(true);
 			diclosure.setOpen(true);
 
 		}
 		
 		else
 		{
 			//loadPaginBar(); TODO if we want to do paging.
 		}
 		
 		
 		
 
 	}
 
 	public void loadPaginBar()
 	{
 		if(pageCount>1)
 		{
 			
 		HorizontalPanel pagingPanel = new HorizontalPanel();
 		
 		SquareHyperlink previousPage = new SquareHyperlink("<<");
 		SquareHyperlink nextPage = new SquareHyperlink(">>");
 		
 		if(currentPage>1)
 		{
 			pagingPanel.add(previousPage);
 			pagingPanel.add(new Label("  "));
 		}
 		for(int i=1;i<=pageCount;i++)
 		{
 			final SquareHyperlink pageLink = new SquareHyperlink(i+"");
 			pageLink.addClickHandler(new ClickHandler(){
 
 			
 				public void onClick(ClickEvent event)
 				{
 					currentPage=Integer.parseInt(pageLink.getText());
 					//navigatePaging();
 				filterRequirements(lastSearch);
 				loadRequirementTableData();
 					
 				}});
 		
 			if(currentPage==i)
 			{ 
 				pagingPanel.add(new Label(" "+i+" "));
 			}
 			else
 			{
 				pagingPanel.add(new Label(" "));
 				pagingPanel.add(pageLink);
 				pagingPanel.add(new Label(" "));
 			}
 			if(i<pageCount)
 			{
 				pagingPanel.add(new Label("|"));
 			}
 			
 			
 		}
 		
 		previousPage.addClickHandler(new ClickHandler(){
 
 			
 			public void onClick(ClickEvent event)
 			{
 				
 				moveToPreviousPage();
 				//navigatePaging();  This will force to use the history manager
 			
 				filterRequirements(lastSearch);
 				loadRequirementTableData();
 				
 			}});
 		
 		nextPage.addClickHandler(new ClickHandler(){
 
 			@Override
 			public void onClick(ClickEvent event)
 			{
 				moveToNextPage();
 				//navigatePaging();
 				filterRequirements(lastSearch);
 				loadRequirementTableData();
 				
 			}});
 		
 		if(currentPage<pageCount)
 		{
 			pagingPanel.add(new Label("  "));
 			pagingPanel.add(nextPage);
 		}
 		
 		pagingPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
 		vPaneData.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
 		vPaneData.add(pagingPanel);
 		}
 	}
 	/*
 	public void navigatePaging()
 	{
 		String searchQuery="";
 		if(lastSearch.trim().length()!=0)
 		{
 			searchQuery="&search="+lastSearch;
 		}
 		
 		String queryString ="?PageId="+currentPage+searchQuery;
 		History.newItem(ReviewOfRequirementsByAcquisitionPilot.generateNavigationId(ReviewOfRequirementsByAcquisitionPilot.PageId.start)+queryString);
 		
 	}
 	*/
 	public int getIndexById(int requirementId)
 	{
 		int count = 0;
 		for (GwtRequirement t : listOfFilteredRequirements)
 		{
 
 			if (t.getId().intValue() == requirementId)
 			{
 				return count;
 			}
 			count++;
 		}
 		return -1;
 	}
 	
 	public void filterRequirements(String searchRequirement)
 	{
 		
 		lastSearch=searchRequirement;
 		listOfFilteredRequirements.clear();
 		if (searchRequirement.trim().length() > 0)
 		{
 			for (GwtRequirement t : listOfRequirements)
 			{
 				if (t.getTitle().trim().toLowerCase().startsWith(searchRequirement.toLowerCase().trim()))
 				{
 					GwtRequirement filterRequirement = new GwtRequirement();
 					filterRequirement.setTitle(t.getTitle());
 					filterRequirement.setSecurity(t.isSecurity());
 					filterRequirement.setPrivacy(t.isPrivacy());
 											
 					filterRequirement.setDescription(t.getDescription());
 					filterRequirement.setPriority(t.getPriority());
 					filterRequirement.setStatus(t.getStatus());
 					filterRequirement.setId(t.getId());
 					
 					listOfFilteredRequirements.add(filterRequirement);
 				}
 			}
 		}
 		else
 		{
 			for (GwtRequirement t : listOfRequirements)
 			{
 				GwtRequirement filterRequirement = new GwtRequirement();
 
 				filterRequirement.setTitle(t.getTitle());
 				filterRequirement.setSecurity(t.isSecurity());
 				filterRequirement.setPrivacy(t.isPrivacy());
 										
 				filterRequirement.setDescription(t.getDescription());
 				filterRequirement.setPriority(t.getPriority());
 				filterRequirement.setStatus(t.getStatus());
 				filterRequirement.setId(t.getId());
 				
 				listOfFilteredRequirements.add(filterRequirement);
 
 			}
 
 		}
 		Collections.sort(listOfFilteredRequirements);
 		
 		//filterForPagin(currentPage);
 
 	}
 
 
 	public void filterForPagin(int currentPage)
 	{
 		
 		List<GwtRequirement> pageFilterRequirements= new ArrayList<GwtRequirement>();
 		pageCount=  listOfFilteredRequirements.size()/pageSize;
 		int minRequirementIndex=((currentPage-1)*pageSize);
 		int maxRequirementIndex=(((currentPage-1)*pageSize)+(pageSize-1));
 		
 		if((pageCount*pageSize)< listOfFilteredRequirements.size())
 		{
 		
 			pageCount=pageCount+1;
 			
 		}
 		
 		if(currentPage==pageCount)
 		{
 			int extraRequirements =listOfFilteredRequirements.size()-(pageCount*pageSize);
 			maxRequirementIndex=maxRequirementIndex+extraRequirements;
 		}
 		
 		if(currentPage>pageCount)
 		{
 			currentPage=1;
 		}
 		
 		
 		
 		
 		for(int i=0; i<listOfFilteredRequirements.size();i++ )
 		{
 			if(i>= minRequirementIndex && i<=maxRequirementIndex)
 			{
 				GwtRequirement req= new GwtRequirement();
 				//req.setRequirement(listOfFilteredRequirements.get(i).getRequirement());
 				req.setTitle(listOfFilteredRequirements.get(i).getTitle());
 				req.setSecurity(listOfFilteredRequirements.get(i).isSecurity());
 				req.setPrivacy(listOfFilteredRequirements.get(i).isPrivacy());
 				req.setDescription(listOfFilteredRequirements.get(i).getDescription());
 				req.setPriority(listOfFilteredRequirements.get(i).getPriority());
 				req.setStatus(listOfFilteredRequirements.get(i).getStatus());
 				req.setId(listOfFilteredRequirements.get(i).getId());
 				
 				
 				pageFilterRequirements.add(req);
 				
 			}
 			
 		}
 		
 		listOfFilteredRequirements.clear();
 		listOfFilteredRequirements=pageFilterRequirements;
 	
 		
 	}
 	
 	public void moveToNextPage()
 	{
 		if(currentPage<pageCount)
 		{
 			currentPage++;
 		}
 		
 	}
 	
 	public void moveToPreviousPage()
 	{
 		if(currentPage>1)
 		{
 			currentPage--;
 		}
 		
 	}
 	
 	public Widget getHeaderRow()
 	{
 
 		FlexTable searchTable = new FlexTable();
 		searchTable.setWidth("100%");
 		searchTable.setCellSpacing(0);
 		HorizontalPanel searchBox = new HorizontalPanel();
 		searchBox.setSpacing(0);
 		Button clearButton = new Button("X");
 		// clearButton.setStyleName("square-clear-label");
 		clearButton.setSize("25px", "26px");
 
 	/*
 		final SquareWaterMarkTextBox searchTextBox = new SquareWaterMarkTextBox(messages.search());
 		
 		if(lastSearch.trim().length()!=0)
 		{
 			searchTextBox.setText(lastSearch);
 			searchTextBox.setStyleName("textInput");
 		}
 		// searchTextBox.setHeight("25px");
 
 		searchBox.add(searchTextBox);
 		searchBox.add(clearButton);
 		searchBox.setStyleName("square-agree-on-definition-inner-table"); //<<<-!!!! ASQUARE 
 
 		searchTextBox.addKeyUpHandler(new KeyUpHandler()
 			{
 
 				public void onKeyUp(KeyUpEvent event)
 				{   currentPage=1;
 					filterRequirements(searchTextBox.getText());
 					loadRequirementTableData();
 					searchTextBox.setFocus(true);
 					searchTextBox.setSelectionRange(searchTextBox.getText().length(), 0);
 
 				}
 			});
 
 		clearButton.addClickHandler(new ClickHandler()
 			{
 
 				@Override
 				public void onClick(ClickEvent event)
 				{
 					currentPage=1;
 					searchTextBox.setText("");
 					filterRequirements(searchTextBox.getText());
 					loadRequirementTableData();
 					searchTextBox.setFocus(true);
 					searchTextBox.setSelectionRange(searchTextBox.getText().length(), 0);
 
 				}
 			});
 
 		//Button addRequirementButton = new Button("Add Requirement");
 		//final ReviewOfRequirementsByAcquisitionPane reviewOfRequirementsByAcquisitionObject = this;
 		
 		addRequirementButton.addClickHandler(new ClickHandler()
 			{
 
 				@Override
 				public void onClick(ClickEvent event)
 				{
 					createRequirementDialog = new CreateRequirementDialog(listOfRequirements, agreeOnDefinitionsObject);
 					createRequirementDialog.center();
 					createRequirementDialog.setModal(true);
 					createRequirementDialog.show();
 
 				}
 			});
 */
 		/*
 		if (this.getCurrentState().getMode().equals(GwtModesType.ReadWrite))
 		{
 			searchTable.setWidget(0, 0, addRequirementButton);
 		}
 		else
 		{
 			searchTable.setWidget(0, 0, new Label(messages.termsAndDefinition()));
 		}
 		*/
 	//	searchTable.setWidget(0, 1, new Label(" "));
 	//	searchTable.setWidget(0, 2, searchBox);
 		
 	//	searchTable.setWidget(0, 0, new Label(messages.requirementTableTitle()));
 		
 		searchTable.setWidget(0, 0, new Label(messages.requirementTableBarTitle()));
 		searchTable.setWidget(0, 1, new Label(messages.requirementTableBarDescription()));
 		searchTable.setWidget(0, 2, new Label(messages.requirementTableBarStatus()));
 		searchTable.setWidget(0, 3, new Label("      "));
 
 		searchTable.getCellFormatter().setWidth(0, 0, "20%");
 		searchTable.getCellFormatter().setWidth(0, 1, "60%");
 		searchTable.getCellFormatter().setWidth(0, 2, "15%");
 		searchTable.getCellFormatter().setWidth(0, 3, "20%");
 		
 		//searchTable.getCellFormatter().setHorizontalAlignment(0, 0, HasHorizontalAlignment.ALIGN_LEFT);
 		//searchTable.getCellFormatter().setHorizontalAlignment(0, 2, HasHorizontalAlignment.ALIGN_RIGHT);
 		
 		searchTable.getCellFormatter().setHorizontalAlignment(0, 0, HasHorizontalAlignment.ALIGN_CENTER);
 		searchTable.getCellFormatter().setHorizontalAlignment(0, 1, HasHorizontalAlignment.ALIGN_CENTER);
 		searchTable.getCellFormatter().setHorizontalAlignment(0, 2, HasHorizontalAlignment.ALIGN_CENTER);
 		searchTable.getCellFormatter().setHorizontalAlignment(0, 3, HasHorizontalAlignment.ALIGN_CENTER);
 
 		searchTable.getCellFormatter().setStyleName(0, 0, "square-Categorize-TableHeader");
 		searchTable.getCellFormatter().setStyleName(0, 1, "square-Categorize-TableHeader");
 		searchTable.getCellFormatter().setStyleName(0, 2, "square-Categorize-TableHeader");
 		searchTable.getCellFormatter().setStyleName(0, 3, "square-Categorize-TableHeader");
 
 		
 		return searchTable;
 	}
 
 	public Widget getDataRow(int rowCount, GwtRequirement req)
 	{
 		FlexTable rowTable = new FlexTable();
 		rowTable.setStyleName("square-agree-on-definition-flex"); //ASQUARE change!!!!
 		rowTable.setCellSpacing(4);
 		rowTable.setSize("100%", "50px");
 
 		Label reqTitleLabel = new Label(req.getTitle());
 		Label reqDescriptionLabel = new Label(req.getDescription());
 		Label reqStatusLabel = new Label(req.getStatus());
 		
 		
 		final SummaryElementHyperLinkElement viewDetailLink = new SummaryElementHyperLinkElement(req.getId(), "View Detail");
 		//final SummaryElementHyperLinkElement hyper2 = new SummaryElementHyperLinkElement(req.getId(), "Remove");
 
 		/*
 		hyper2.addClickHandler(new ClickHandler()
 			{
 
 				@Override
 				public void onClick(ClickEvent event)
 				{
 					boolean response = Window.confirm(messages.confirmDelete() + "?");
 					if (response)
 					{
 						GwtRequirement requirement = new GwtRequirement();
 						requirement.setId(hyper2.getrequirementirementId());
 						removeRequirement(requirement);
 					}
 				}
 			});
 			*/
 
		if (this.getCurrentState().getMode().equals(GwtModesType.ReadWrite))
 		{
 			HorizontalPanel links = new HorizontalPanel();
 			links.setStyleName("flex-link-bar");
 			links.add(viewDetailLink);
 			// links.add(new Label(" "));
 			//links.add(hyper2);
 			rowTable.setWidget(0, 3, links);
 		}
 		else
 		{
 			rowTable.setWidget(0, 2, new Label(" "));
 		}
 
 		final ReviewOfRequirementsByAcquisitionPane reviewOfRequirementsByAcquisitionObject = this;
 
 //view detail hyperlink
 		viewDetailLink.addClickHandler(new ClickHandler()
 			{
 
 				@Override
 				public void onClick(ClickEvent event)
 				{
 
 					currentState.setCurrentRisk(viewDetailLink.getRequirementId());
 					currentState.setRiskCommand(1);
 					History.newItem(ReviewOfRequirementsByAcquisitionPilot.generateNavigationId(ReviewOfRequirementsByAcquisitionPilot.PageId.requirementDetail));
 
 				}
 	
 			});
 
 		int addedItemIndex = getIndexById(modifyRequirementId);
 
 		if (addedItemIndex != -1 && (addedItemIndex + 1) == rowCount)
 		{
 			yellowFadeHandler.add(rowTable);
 			modifyRequirementId = -1;
 
 		}
 
 		rowTable.setWidget(0, 0, reqTitleLabel);
 		rowTable.setWidget(0, 1, reqDescriptionLabel);
 		rowTable.setWidget(0, 2, reqStatusLabel);
 
 		rowTable.getCellFormatter().setWidth(0, 0, "20%");
 		rowTable.getCellFormatter().setWidth(0, 1, "60%");
 		rowTable.getCellFormatter().setWidth(0, 2, "15%");
 		rowTable.getCellFormatter().setWidth(0, 3, "20%");
 		
 		rowTable.getCellFormatter().setHorizontalAlignment(0, 2, HasHorizontalAlignment.ALIGN_CENTER);
 		rowTable.getCellFormatter().setVerticalAlignment(0, 0, HasVerticalAlignment.ALIGN_TOP);
 
 		rowTable.getCellFormatter().setHorizontalAlignment(0, 0, HasHorizontalAlignment.ALIGN_LEFT);
 		rowTable.getCellFormatter().setHorizontalAlignment(0, 1, HasHorizontalAlignment.ALIGN_LEFT);
 		rowTable.getCellFormatter().setHorizontalAlignment(0, 2, HasHorizontalAlignment.ALIGN_CENTER);
 
 		rowTable.getCellFormatter().setVerticalAlignment(0, 0, HasVerticalAlignment.ALIGN_TOP);
 		rowTable.getCellFormatter().setVerticalAlignment(0, 1, HasVerticalAlignment.ALIGN_TOP);
 		rowTable.getCellFormatter().setVerticalAlignment(0, 2, HasVerticalAlignment.ALIGN_TOP);
 
 		return rowTable;
 
 	}
 
 	public GwtRequirement getRequirementById(int id)
 	{
 		for (GwtRequirement t : listOfRequirements)
 		{
 			if (t.getId().intValue() == id)
 			{
 				return t;
 			}
 		}
 		return null;
 	}
 
 
 	// This commands are triggered from the dialog boxes
 	public void createCommand(GwtRequirement req)
 	{
 		GwtProject gwtProject = new GwtProject();
 		gwtProject.setId(projectID);
 		//addRequirement(requirement, gwtProject);
 
 	}
 
 	public void updateCommand(GwtRequirement requirement)
 	{
 
 		GwtProject gwtProject = new GwtProject();
 		gwtProject.setId(projectID);
 		updateRequirement(requirement, gwtProject);;
 	}
 	/*
 	private void addDoneButton()
 	{
 		FlexTable buttonPanel = new FlexTable();
 		Button done =new Button(messages.done());
 		done.addStyleName("square-button");
 		done.addClickHandler(new ClickHandler(){
 
 			public void onClick(ClickEvent event)
 			{
 				History.newItem(ChooseStepPilot.generateNavigationId(ChooseStepPilot.PageId.home));
 				
 			}});
 		buttonPanel.setWidget(0,0,new Label(" "));
 		buttonPanel.setWidget(1, 0, done);
 		buttonPanel.setWidth("100%");
 		buttonPanel.getCellFormatter().setHorizontalAlignment(1,0,HasHorizontalAlignment.ALIGN_RIGHT);
 		this.vPane.add(buttonPanel);
 	}
 */
 	class SummaryElementHyperLinkElement extends SquareHyperlink
 	{
 
 		private int requirementID;
 		public SummaryElementHyperLinkElement(int requirementID, String text)
 			{
 				super(text);
 				this.requirementID = requirementID;
 			}
 
 		public void setRequirementID(int requirementID)
 		{
 			this.requirementID = requirementID;
 		}
 
 		public int getRequirementId()
 		{
 			return requirementID;
 		}
 
 	}
 	
 	
 
 }
 
 
 
