 package edu.cmu.square.client.ui.performTradeoffAnalysis;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.dom.client.ChangeEvent;
 import com.google.gwt.event.dom.client.ChangeHandler;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.event.dom.client.FocusEvent;
 import com.google.gwt.event.dom.client.FocusHandler;
 import com.google.gwt.event.dom.client.KeyCodes;
 import com.google.gwt.event.dom.client.KeyPressEvent;
 import com.google.gwt.event.dom.client.KeyPressHandler;
 import com.google.gwt.event.dom.client.MouseOutEvent;
 import com.google.gwt.event.dom.client.MouseOutHandler;
 import com.google.gwt.event.dom.client.MouseOverEvent;
 import com.google.gwt.event.dom.client.MouseOverHandler;
 import com.google.gwt.user.client.History;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.rpc.ServiceDefTarget;
 import com.google.gwt.user.client.ui.DecoratedPopupPanel;
 import com.google.gwt.user.client.ui.FlexTable;
 import com.google.gwt.user.client.ui.FlexTable.FlexCellFormatter;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.DisclosurePanel;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.HasHorizontalAlignment;
 import com.google.gwt.user.client.ui.HasVerticalAlignment;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.ListBox;
 import com.google.gwt.user.client.ui.TextBox;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import com.google.gwt.user.client.ui.Widget;
 
 import edu.cmu.square.client.exceptions.ExceptionHelper;
 import edu.cmu.square.client.exceptions.SquareException;
 import edu.cmu.square.client.model.GwtModesType;
 import edu.cmu.square.client.model.GwtProject;
 import edu.cmu.square.client.model.GwtQualityAttribute;
 import edu.cmu.square.client.model.GwtRating;
 import edu.cmu.square.client.model.GwtRequirement;
 import edu.cmu.square.client.model.GwtRequirementRating;
 import edu.cmu.square.client.model.GwtSoftwarePackage;
 import edu.cmu.square.client.model.GwtTradeoffReason;
 import edu.cmu.square.client.model.StepStatus;
 import edu.cmu.square.client.navigation.State;
 import edu.cmu.square.client.remoteService.step.interfaces.PerformTradeoffAnalysisService;
 import edu.cmu.square.client.remoteService.step.interfaces.PerformTradeoffAnalysisServiceAsync;
 import edu.cmu.square.client.remoteService.step.interfaces.ReviewOfRequirementsByAcquisitionService;
 import edu.cmu.square.client.remoteService.step.interfaces.ReviewOfRequirementsByAcquisitionServiceAsync;
 import edu.cmu.square.client.remoteService.step.interfaces.ReviewPackagesService;
 import edu.cmu.square.client.remoteService.step.interfaces.ReviewPackagesServiceAsync;
 import edu.cmu.square.client.ui.core.BasePane;
 import edu.cmu.square.client.ui.core.SquareHyperlink;
 
 public class PerformTradeoffAnalysisPane extends BasePane
 {
 	final private PerformTradeoffAnalysisPaneMessages messages = (PerformTradeoffAnalysisPaneMessages)GWT.create(PerformTradeoffAnalysisPaneMessages.class);
 	
 	private VerticalPanel vPane = new VerticalPanel();
 	private VerticalPanel vPaneData = new VerticalPanel();
 	
 	private ReviewPackagesServiceAsync service = GWT.create(ReviewPackagesService.class);
 	private PerformTradeoffAnalysisServiceAsync performTradeoffService = GWT.create(PerformTradeoffAnalysisService.class);
 	
 	
 	private List<GwtSoftwarePackage> softwarePackages;
 	private List<GwtRequirement> listOfRequirements = new ArrayList<GwtRequirement>();
 	private List<GwtQualityAttribute> attributes;
 	private List<GwtRating> ratings;
 	private List<GwtRequirementRating> requirementRatings;
 	private List<GwtTradeoffReason> tradeoffReasons;
 	
 	protected EditTradeoffReasonDialog editTradeoffReasonDialog;
 	
 	GwtProject currentProject;
 	
  
 	private FlexTable matrix = new FlexTable();
 	private FlexTable matrixHeader = new FlexTable();
 	
 	SquareHyperlink editRatingsLink = new SquareHyperlink(messages.editRatingsLink());		
 	SquareHyperlink finishRatingsLink = new SquareHyperlink(messages.finishRatingsLink());
 	SquareHyperlink editPackagesLink = new SquareHyperlink(messages.editPackagesLink());
 	SquareHyperlink finishPackagesLink = new SquareHyperlink(messages.finishPackagesLink());
 	SquareHyperlink editAttributesLink = new SquareHyperlink(messages.editAttributesLink());		
 	SquareHyperlink finishAttributesLink = new SquareHyperlink(messages.finishAttributesLink());
 	
 	boolean isReadOnly=false;
 
 	public PerformTradeoffAnalysisPane(final State stateInfo)
 	{
 		super(stateInfo);
 		
 		this.showLoadingStatusBar();
 		ServiceDefTarget endpoint = (ServiceDefTarget) service;
 		endpoint.setServiceEntryPoint(GWT.getModuleBaseURL() + "reviewPackages.rpc");
 		
 		ServiceDefTarget endpoint2 = (ServiceDefTarget) service;
 		endpoint2.setServiceEntryPoint(GWT.getModuleBaseURL() + "performTradeoffAnalysis.rpc");
 		
 		currentProject = new GwtProject();
 		currentProject.setId(this.getCurrentState().getProjectID());
 		
 	
 		isReadOnly = true;
 		
 		loadAttributes();
 	}
 	
 	public void loadRequirements()
 	{
 		ReviewOfRequirementsByAcquisitionServiceAsync service1 = GWT.create(ReviewOfRequirementsByAcquisitionService.class);
 		ServiceDefTarget endpoint = (ServiceDefTarget) service1;
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
 					
 
 					loadTradeoffReasons();
 					
 				}
 			});
 	}
 	
 	public void initializePane()
 	{
 		this.hideStatusBar();
 		loadRequirementsTable();
 		this.getContent().add(vPane);	
 	}
 	
 	public void loadRequirementsTable()
 	{	
 		vPane.clear();
 		vPane.setSpacing(0);
 		vPane.setWidth("90%");
 		vPane.setHeight("5%");
 
 		vPane.add(getHeaderRow());
 		vPane.add(vPaneData);
 		loadRequirementTableData();
 		//addDoneButton();
 	}
 	
 	public Widget getHeaderRow()
 	{
 
 		//FlexTable requirementTitleTable = new FlexTable();
 		FlexTable searchTable = new FlexTable();
 		
 		//requirementTitleTable.setWidth("100%");
 		//requirementTitleTable.setCellSpacing(0);
 		searchTable.setWidth("100%");
 		searchTable.setCellSpacing(0);
 	
 		//requirementTitleTable.setWidget(0, 0, new Label(messages.requirementTableTitle()));
 		
 		//requirementTitleTable.getCellFormatter().setHorizontalAlignment(0, 0, HasHorizontalAlignment.ALIGN_CENTER);
 		//requirementTitleTable.getCellFormatter().setStyleName(0, 0, "square-Categorize-TableHeader");
 		
 		searchTable.setWidget(0, 0, new Label(messages.requirementTableTitle()));
 		
 		searchTable.setWidget(1, 0, new Label(messages.requirementTableBarTitle()));
 		searchTable.setWidget(1, 1, new Label(messages.requirementTableBarDescription()));
 		searchTable.setWidget(1, 2, new Label(messages.requirementTableBarStatus()));
 		searchTable.setWidget(1, 3, new Label("      "));
 		
 		searchTable.getCellFormatter().setWidth(0, 0, "20%");
 		searchTable.getCellFormatter().setWidth(0, 1, "55%");
 		searchTable.getCellFormatter().setWidth(0, 2, "15%");
 		searchTable.getCellFormatter().setWidth(0, 3, "10%");
 		
 		searchTable.getCellFormatter().setHorizontalAlignment(1, 0, HasHorizontalAlignment.ALIGN_CENTER);
 		searchTable.getCellFormatter().setHorizontalAlignment(1, 1, HasHorizontalAlignment.ALIGN_CENTER);
 		searchTable.getCellFormatter().setHorizontalAlignment(1, 2, HasHorizontalAlignment.ALIGN_CENTER);
 		searchTable.getCellFormatter().setHorizontalAlignment(1, 3, HasHorizontalAlignment.ALIGN_CENTER);
 
 		
 		searchTable.getCellFormatter().setStyleName(1, 0, "square-Categorize-TableHeader");
 		searchTable.getCellFormatter().setStyleName(1, 1, "square-Categorize-TableHeader");
 		searchTable.getCellFormatter().setStyleName(1, 2, "square-Categorize-TableHeader");
 		searchTable.getCellFormatter().setStyleName(1, 3, "square-Categorize-TableHeader");
 
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
 
 		if (this.getCurrentState().getMode().equals(GwtModesType.ReadWrite))
 		{
 			HorizontalPanel links = new HorizontalPanel();
 			links.setStyleName("flex-link-bar");
 			links.add(viewDetailLink);
 			rowTable.setWidget(0, 3, links);
 		}
 		
 		//view detail hyperlink
 		viewDetailLink.addClickHandler(new ClickHandler()
 			{
 				@Override
 				public void onClick(ClickEvent event)
 				{
 					System.out.println("detail......");
 					currentState.setCurrentRisk(viewDetailLink.getRequirementId());
 					currentState.setRiskCommand(1);
 					History.newItem(PerformTradeoffAnalysisPilot.generateNavigationId(PerformTradeoffAnalysisPilot.PageId.requirementDetail));
 				}
 			});
 
 		rowTable.setWidget(0, 0, reqTitleLabel);
 		rowTable.setWidget(0, 1, reqDescriptionLabel);
 		rowTable.setWidget(0, 2, reqStatusLabel);
 
 		rowTable.getCellFormatter().setWidth(0, 0, "20%");
 		rowTable.getCellFormatter().setWidth(0, 1, "55%");
 		rowTable.getCellFormatter().setWidth(0, 2, "15%");
 		rowTable.getCellFormatter().setWidth(0, 3, "10%");
 		
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
 
 	public void loadRequirementTableData()
 	{
 		vPaneData.clear();
 		vPaneData.setWidth("100%");
 
 		vPaneData.setSpacing(5);
 		int rowCount = 1;
 		for (GwtRequirement requirement : listOfRequirements)
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
 	}
 	
 	
 	
 	public void PaneInitialization()
 	{
 		this.hideStatusBar();
 		VerticalPanel layout = new VerticalPanel();
 		
 		editRatingsLink.addClickHandler(new ClickHandler(){		
 			public void onClick(ClickEvent event) {
 				isReadOnly=false;
 				//loadEvaluationsCriteria();
 				drawRateMatrix();
 				getTotalsFromMatrix();
 				
 				changeLink();		
 		}});
 		finishRatingsLink.addClickHandler(new ClickHandler(){	
 			public void onClick(ClickEvent event) {
 				isReadOnly=true;
 				//loadEvaluationsCriteria();
 				drawRateMatrix();
 				getTotalsFromMatrix();
 				
 				changeLink();		
 			}});
 		drawMatrixPage();
 	
 		//this.getContent().clear();
 		//layout.add(comparisonMatrixLabel);
 		layout.add(matrixHeader);
 		this.getContent().add(layout);
 		
 		//initWidget(layout);
 	}	
 	
 	protected void updateTradeoffReasoninDB(GwtTradeoffReason localTradeoffReason)
 	{
 		this.performTradeoffService.setTradeoffReason(currentProject.getId(), localTradeoffReason.getPackageId(), localTradeoffReason.getTradeoffreason(), new AsyncCallback<Void>()
 				{
 					public void onFailure(Throwable caught) 
 					{
 						if (caught instanceof SquareException) 
 						{
 							SquareException se = (SquareException) caught;
 							switch (se.getType()) {
 							case authorization:
 								Window.alert(messages.rateAuthorization());
 								break;
 						
 							default:
 								Window.alert(messages.error());
 								break;
 							}
 
 						} else {
 							Window.alert(messages.error());
 						}
 					}
 					
 					public void onSuccess(Void result) 
 					{
 						loadTradeoffReasons();
 					}
 				});	
 	}
 	
 	private void changeLink()
 	{
 		if(isReadOnly)
 		{
 			this.matrixHeader.setWidget(2, 1, editRatingsLink);
 			this.matrixHeader.setWidget(3, 1, matrix);		
 		}
 		else
 		{
 			this.matrixHeader.setWidget(2, 1, finishRatingsLink);
 			this.matrixHeader.setWidget(3, 1, matrix);
 		}		
 	}
 	
 	private void drawMatrixPage()
 	{
 		FlexCellFormatter formatter1 = this.matrixHeader.getFlexCellFormatter();
 		this.matrixHeader.setWidth("100%");
 
 		this.matrixHeader.setWidget(1, 1, new Label(messages.matrixLableX()));
 		this.matrixHeader.setWidget(3, 0, new Label(messages.matrixLableY()));
 		this.matrixHeader.setWidget(4, 1, new Label(messages.rateLegend()));
 		
 		
 		formatter1.setHorizontalAlignment(0, 0, HasHorizontalAlignment.ALIGN_LEFT);
 		formatter1.setHorizontalAlignment(1, 1, HasHorizontalAlignment.ALIGN_CENTER);
 		formatter1.setHorizontalAlignment(2, 1, HasHorizontalAlignment.ALIGN_RIGHT);
 		formatter1.setHorizontalAlignment(3, 1, HasHorizontalAlignment.ALIGN_LEFT);
 		formatter1.setVerticalAlignment(3, 0, HasVerticalAlignment.ALIGN_MIDDLE);
 		formatter1.setHorizontalAlignment(4, 1, HasHorizontalAlignment.ALIGN_LEFT);
 		formatter1.setHorizontalAlignment(5, 1, HasHorizontalAlignment.ALIGN_RIGHT);
 		
 		if(isReadOnly)
 		{
 			if(GwtModesType.ReadWrite==this.currentState.getMode())
 			{
 				this.matrixHeader.setWidget(2, 1, editRatingsLink);
 			}		
 			this.matrixHeader.setWidget(3, 1, matrix);		
 		}
 		else
 		{
 			this.matrixHeader.setWidget(2, 1, finishRatingsLink);
 			this.matrixHeader.setWidget(3, 1, matrix);
 		}
 		
 	}
 	
 	/**
 	 * Contains RPC Calls to retrieve the evaluation criteria for the project
 	 */
 	
 	private void loadAttributes()
 	{
 		final StepStatus stepStatus =  StepStatus.NotStarted;
 		service.getQualityAttributes(currentProject,stepStatus, new AsyncCallback<List<GwtQualityAttribute> >()
 				{
 					@Override
 					public void onSuccess(List<GwtQualityAttribute> result)
 					{
 						System.out.println("We got quality attribute: "+result.size());
 						attributes = result;
 						loadPackages();
 					}
 					
 					@Override
 					public void onFailure(Throwable caught)
 					{
 						Window.alert(messages.attributesRetrievalError());
 						ExceptionHelper.SquareRootRPCExceptionHandler(caught, "Retrieving Quality Attributes");		
 					}
 				});
 	}
 
 	private void loadPackages()
 	{
 		final StepStatus stepStatus =  StepStatus.NotStarted;
 		service.getSoftwarePackages(currentProject,stepStatus, new AsyncCallback<List<GwtSoftwarePackage>>()
 		{
 			@Override
 			public void onSuccess(List<GwtSoftwarePackage> result)
 			{
 				softwarePackages = result;
 				
 				loadRatings();		
 				
 				
 			}	
 			@Override
 			public void onFailure(Throwable caught)
 			{
 				Window.alert(messages.packagesRetrievalError());
 				ExceptionHelper.SquareRootRPCExceptionHandler(caught, "Retrieving Software Packages");		
 			}
 		});
 	}
 	
 	public void loadTradeoffReasons()
 	{
 		performTradeoffService.getTradeoffReasons(currentProject.getId(), new AsyncCallback<List<GwtTradeoffReason>>()		
 				{		
 					@Override
 					public void onSuccess(List<GwtTradeoffReason> result)
 					{
 						tradeoffReasons = result;
 						initializePane();
 						drawRateMatrix();
 						getTotalsFromMatrix();
 						PaneInitialization();
 					}
 					@Override
 					public void onFailure(Throwable caught)
 					{
 						Window.alert(messages.ratingsRetrievalError());
 						ExceptionHelper.SquareRootRPCExceptionHandler(caught, "Retrieving Tradeoff Reasons");
 					}	
 				});		
 	}
 
 	private void loadRatings()
 	{
 		service.getRateValues(currentProject.getId(), new AsyncCallback<List<GwtRating > >()
 				{		
 					@Override
 					public void onSuccess(List<GwtRating >  result)
 					{
 						
 						ratings = result;
 						
 						loadRequirementRatings();
 					}			
 					@Override
 					public void onFailure(Throwable caught)
 					{
 						Window.alert(messages.ratingsRetrievalError());
 						ExceptionHelper.SquareRootRPCExceptionHandler(caught, "Retrieving Ratings");
 					}
 				});		
 	}
 	
 	private void loadRequirementRatings()
 	{
 		performTradeoffService.getRequirementRateValues(currentProject.getId(), new AsyncCallback<List<GwtRequirementRating > >()
 				{		
 					@Override
 					public void onSuccess(List<GwtRequirementRating> result)
 					{
 						
 						requirementRatings = result;
 						
 						loadRequirements();
 					}
 					@Override
 					public void onFailure(Throwable caught)
 					{
 						Window.alert(messages.ratingsRetrievalError());
 						ExceptionHelper.SquareRootRPCExceptionHandler(caught, "Retrieving Requirement Ratings");
 					}	
 				});		
 	}
 
 	private void setRateValue(final int packageID, final int attributeID, final int value)
 	{
 		
 		
 		this.service.setRateValue(currentProject.getId(), packageID, attributeID, value, new AsyncCallback<Void>(){	
 			public void onFailure(Throwable caught) {
 				if (caught instanceof SquareException) {
 					SquareException se = (SquareException) caught;
 					switch (se.getType()) {
 					case authorization:
 						Window.alert(messages.rateAuthorization());
 						break;
 				
 					default:
 						Window.alert(messages.error());
 						break;
 					}
 				} else {
 					Window.alert(messages.error());
 				}		
 			}
 			
 			public void onSuccess(Void result) {
 				setValueFromlistOfRateValues(packageID, attributeID, value);	
 				loadRequirementRatings();
 			}});
 	}
 	
 	
 	private void setRequirementRateValue(final int packageID, final int requirementID, final int value)
 	{	
 		//the sequence of requirementID and packageID has problems 
 		this.performTradeoffService.setRequirementRateValue(currentProject.getId(), requirementID, packageID, value, new AsyncCallback<Void>(){
 		
 			public void onFailure(Throwable caught) {
 				if (caught instanceof SquareException) {
 					SquareException se = (SquareException) caught;
 					switch (se.getType()) {
 					case authorization:
 						Window.alert(messages.rateAuthorization());
 						break;				
 					default:
 						Window.alert(messages.error());
 						break;
 					}
 
 				} else {
 					Window.alert(messages.error());
 				}				
 			}		
 			public void onSuccess(Void result) {
 				
 				setValueFromlistOfRequirementRateValues(requirementID, packageID, value);	
 				loadRequirementRatings();
 			}});
 	}
 	
 	
 	private void setPackagePriority(final int packageID, final int priority)
 	{	
 		//the sequence of requirementID and packageID has problems 
		System.out.println("set priority........."+currentProject.getId()+"  "+packageID +"  "+priority);		
 		this.performTradeoffService.setPriority(currentProject.getId(), packageID, priority, new AsyncCallback<Void>(){
 		
 			public void onFailure(Throwable caught) {
 				if (caught instanceof SquareException) {
 					SquareException se = (SquareException) caught;
 					switch (se.getType()) {
 					case authorization:
 						Window.alert(messages.rateAuthorization());
 						break;				
 					default:
 						Window.alert(messages.error());
 						break;
 					}
 				} else {
 					Window.alert(messages.error());
 				}				
 			}		
 			public void onSuccess(Void result) {
 			
 			}});
 	}
 	
 	
 	public void drawRateMatrix()
 	{
 		matrix.clear();
 		//matrix.setBorderWidth(1);
 		matrix.setWidth("100%");
 		matrix.setStyleName("square-Matrix");
 		matrix.setCellSpacing(0);
 		drawAttributesAndRequirements();
 		drawSoftwarePackages();
 		drawRateMatrixValues();
 	}
 	
 	public void drawAttributesAndRequirements()
 	{
 		FlexCellFormatter formatter = this.matrix.getFlexCellFormatter();
 		matrix.setWidget(0, 0, new Label(" "));
 		formatter.setStyleName(0, 0, "square-Matrix");
 			
 		int i, j;
 		// Set the header rows  with requirements
 		for(i=1; i<=listOfRequirements.size();i++)
 		{
 			Label techniqueLabel = null;
 			final DecoratedPopupPanel simplePopup = new DecoratedPopupPanel(true);
 			   // simplePopup.ensureDebugId("cwBasicPopup-simplePopup");
 			simplePopup.setWidth("150px");
 			
 			techniqueLabel = new Label(listOfRequirements.get(i-1).getTitle());
 			simplePopup.setWidget(new HTML(listOfRequirements.get(i-1).getDescription()));
 			
 				//techniqueLabel = new Label(listOfRequirements.get(i-1).getTitle());
 				//simplePopup.setWidget(new HTML(listOfRequirements.get(i-1).getDescription()));		   
 			
 			techniqueLabel.addMouseOverHandler(new MouseOverHandler(){
 					public void onMouseOver(MouseOverEvent event) 
 					{
 						Widget source = (Widget) event.getSource();
 			            int left = source.getAbsoluteLeft() + 20;
 			            int top = source.getAbsoluteTop() + 20;
 			            simplePopup.setPopupPosition(left, top);
 						simplePopup.show();
 					}});
 			    
 			techniqueLabel.addMouseOutHandler(new MouseOutHandler(){
 					public void onMouseOut(MouseOutEvent event) {
 						simplePopup.hide();				
 					}});
 			    
 			matrix.setWidget(0, i, techniqueLabel);
 			formatter.setHorizontalAlignment(0, i, HasHorizontalAlignment.ALIGN_CENTER);
 			formatter.setStyleName(0, i, "square-Matrix");
 		}
 		
 		
 		// Set the header rows  with attributes
 		for(j=1; j<=attributes.size();j++)
 		{
 			Label techniqueLabel = null;
 			final DecoratedPopupPanel simplePopup = new DecoratedPopupPanel(true);
 			   // simplePopup.ensureDebugId("cwBasicPopup-simplePopup");
 			simplePopup.setWidth("150px");
 			
 			techniqueLabel = new Label(attributes.get(j-1).getName());
 			simplePopup.setWidget(new HTML(attributes.get(j-1).getDescription()));
 			
 			
 			techniqueLabel.addMouseOverHandler(new MouseOverHandler(){
 					public void onMouseOver(MouseOverEvent event) 
 					{
 						Widget source = (Widget) event.getSource();
 			            int left = source.getAbsoluteLeft() + 20;
 			            int top = source.getAbsoluteTop() + 20;
 			            simplePopup.setPopupPosition(left, top);
 						simplePopup.show();
 					}});
 			    
 			techniqueLabel.addMouseOutHandler(new MouseOutHandler(){
 					public void onMouseOut(MouseOutEvent event) {
 						simplePopup.hide();				
 					}});
 			    
 			matrix.setWidget(0, listOfRequirements.size()+j, techniqueLabel);
 			formatter.setHorizontalAlignment(0, listOfRequirements.size()+j, HasHorizontalAlignment.ALIGN_CENTER);
 			formatter.setStyleName(0, listOfRequirements.size()+j, "square-Matrix");
 		}
 		
 		matrix.setWidget(0, attributes.size()+listOfRequirements.size()+1, new Label("Total"));
 		matrix.setWidget(0, attributes.size()+listOfRequirements.size()+2, new Label("Tradeoff Reason"));
 		matrix.setWidget(0, attributes.size()+listOfRequirements.size()+3, new Label("Prioritize"));
 		formatter.setHorizontalAlignment(0, attributes.size()+listOfRequirements.size()+1, HasHorizontalAlignment.ALIGN_RIGHT);
 		formatter.setStyleName(0, attributes.size()+listOfRequirements.size()+1,"square-Matrix");	
 		formatter.setHorizontalAlignment(0, attributes.size()+listOfRequirements.size()+2, HasHorizontalAlignment.ALIGN_RIGHT);
 		formatter.setStyleName(0, attributes.size()+listOfRequirements.size()+2,"square-Matrix");	
 		formatter.setHorizontalAlignment(0, attributes.size()+listOfRequirements.size()+3, HasHorizontalAlignment.ALIGN_RIGHT);
 		formatter.setStyleName(0, attributes.size()+listOfRequirements.size()+3,"square-Matrix");	
 	}
 	
 	public void drawSoftwarePackages()
 	{
 		FlexCellFormatter formatter = this.matrix.getFlexCellFormatter();
 		// Set the left columns with the package names 
 		for(int j=0; j<softwarePackages.size();j++)
 		{		
 			Label packageLabel = new Label(softwarePackages.get(j).getName());
 			
			
 			final DecoratedPopupPanel simplePopup = new DecoratedPopupPanel(true);
 			simplePopup.setWidth("150px");
 			simplePopup.setWidget(new HTML(softwarePackages.get(j).getDescription()));
 			
 			packageLabel.addMouseOverHandler(new MouseOverHandler(){		
 				public void onMouseOver(MouseOverEvent event) {
 						Widget source = (Widget) event.getSource();
 			            int left = source.getAbsoluteLeft() + 40;
 			            int top = source.getAbsoluteTop() + 20;
 			            simplePopup.setPopupPosition(left, top);
 						simplePopup.show();
 				}});
 			packageLabel.addMouseOutHandler(new MouseOutHandler(){	
 				public void onMouseOut(MouseOutEvent event) {
 						simplePopup.hide();
 				}});
 			matrix.setWidget(j+1,0 , packageLabel);
 			
 			
 			final SummaryElementHyperLinkElement tradeoffReasonLink = new SummaryElementHyperLinkElement(softwarePackages.get(j).getId(), "Tradeoff Reason");
 			final int index = j;
			final int packageid = softwarePackages.get(j).getId();
			
 			tradeoffReasonLink.addClickHandler(new ClickHandler(){
 				public void onClick(ClickEvent event) {
 					editTradeoffReasonDialog = new EditTradeoffReasonDialog(tradeoffReasons.get(index),tradeoffReasons,PerformTradeoffAnalysisPane.this);
 					editTradeoffReasonDialog.center();
 					editTradeoffReasonDialog.setModal(true);
 					editTradeoffReasonDialog.show();
 					
 				}});
 			
 			final ListBox priorityListBox = new ListBox();
 			for(int k=0; k<softwarePackages.size(); k++)
 				priorityListBox.addItem(k+"", k+1+"");
 			
 			if(tradeoffReasons.get(j).getPriority().intValue()!=-1)
 			{
 				priorityListBox.setSelectedIndex(tradeoffReasons.get(j).getPriority().intValue());
 			}
 			else
 			{
 				System.out.println("here1.....");
 				priorityListBox.setSelectedIndex(0);
 			}
 			
 			priorityListBox.addChangeHandler(new ChangeHandler()
 			{
 				public void onChange(ChangeEvent event)
 				{
 					int valueSelected = priorityListBox.getSelectedIndex();
 					if(valueSelected == 0)
 					{
 						valueSelected =-1;
 					}
					System.out.println("here2....."+"  "+packageid+"   "+valueSelected);
					setPackagePriority(packageid,valueSelected);
 					tradeoffReasons.get(index).setPriority(valueSelected);
 					System.out.println("here3.....");
 				}
 			});
 			
 			
 			
 			
 			
 			
 			if(j>=1)
 			{
 				matrix.setWidget(j+1,attributes.size()+listOfRequirements.size()+2, tradeoffReasonLink);
 				formatter.setHorizontalAlignment(j+1, attributes.size()+listOfRequirements.size()+2, HasHorizontalAlignment.ALIGN_RIGHT);
 				formatter.setStyleName(j+1, attributes.size()+listOfRequirements.size()+2,  "square-Matrix");
 				
 				matrix.setWidget(j+1,attributes.size()+listOfRequirements.size()+3, priorityListBox);
 				formatter.setHorizontalAlignment(j+1, attributes.size()+listOfRequirements.size()+3, HasHorizontalAlignment.ALIGN_RIGHT);
 				formatter.setStyleName(j+1, attributes.size()+listOfRequirements.size()+3,  "square-Matrix");
 			}
 			formatter.setHorizontalAlignment(j+1,0 , HasHorizontalAlignment.ALIGN_RIGHT);
 			formatter.setStyleName(j+1,0, "square-Matrix");		
 		}	
 	}
 	
 	public void drawRateMatrixValues()
 	{
 		FlexCellFormatter formatter = this.matrix.getFlexCellFormatter();
 		for(int i=0;i<attributes.size()+listOfRequirements.size();i++)
 		{
 			for(int j=0; j<softwarePackages.size();j++)
 			{
 				
 				if(i<listOfRequirements.size())
 				{
 					int rID=listOfRequirements.get(i).getId();
 					int pID=softwarePackages.get(j).getId();			
 					int value = getValueFromlistOfRequirementRateValues(pID,rID);
 					
 					if(isReadOnly)
 					{
 						Label valueLabel = new Label(String.valueOf(value));
 						valueLabel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
 						matrix.setWidget(j+1, i+1, valueLabel);
 						formatter.setHorizontalAlignment(j+1, i+1, HasHorizontalAlignment.ALIGN_CENTER);
 						formatter.setStyleName(j+1, i+1,"square-Matrix");
 					}
 					else
 					{
 						//Allow only the digits 1,2 and 3 to be inputed in the rateValueTextBox
 						final RateValueTextbox rateValueTextbox =new RateValueTextbox(rID,pID, value);
 						rateValueTextbox.setTextAlignment(TextBox.ALIGN_CENTER);
 						rateValueTextbox.addKeyPressHandler(new KeyPressHandler(){	
 							public void onKeyPress(KeyPressEvent event) {
 								char keyCode= event.getCharCode();
 								 if (
 										 ( (keyCode< '0' || keyCode>'3')) && (keyCode != (char) KeyCodes.KEY_TAB)
 								            && (keyCode != (char) KeyCodes.KEY_BACKSPACE)
 								            && (keyCode != (char) KeyCodes.KEY_DELETE) && (keyCode != (char) KeyCodes.KEY_ENTER) 
 								            && (keyCode != (char) KeyCodes.KEY_HOME) && (keyCode != (char) KeyCodes.KEY_END)
 								            && (keyCode != (char) KeyCodes.KEY_LEFT) && (keyCode != (char) KeyCodes.KEY_UP)
 								            && (keyCode != (char) KeyCodes.KEY_RIGHT) && (keyCode != (char) KeyCodes.KEY_DOWN)) {	 	
 									 	// rateValueTextbox.cancelKey() suppresses the current keyboard event.
 									 	rateValueTextbox.cancelKey();
 								 }
 							}
 							});
 						rateValueTextbox.addFocusHandler(new FocusHandler(){
 							public void onFocus(FocusEvent event)
 							{
 								rateValueTextbox.setSelectionRange(0, rateValueTextbox.getText().length());			
 						}});
 						
 						rateValueTextbox.addChangeHandler(new ChangeHandler(){
 							public void onChange(ChangeEvent event) {
 							//	Window.alert("a changed happened");
 								
 								if(rateValueTextbox.getText().trim().length()==0)
 								 {
 									 rateValueTextbox.setText("0");
 								 }
 								char keyCode;
 								
 								// Validation of the textbox from Copy and Paste
 								if(rateValueTextbox.getText().trim().length()==1)
 								{
 									keyCode=rateValueTextbox.getText().trim().charAt(0);
 								   if(keyCode>= '0' && keyCode<='3')
 								   {
 									   getTotalsFromMatrix();
 									   setRequirementRateValue(rateValueTextbox.getTecniqueID(),rateValueTextbox.getEvaluationID(),Integer.parseInt(rateValueTextbox.getText()));
 										rateValueTextbox.setOldValue(rateValueTextbox.getText());
 								   }
 								   else
 								   {
 									   rateValueTextbox.setText(rateValueTextbox.getOldValue());
 								   }
 								}
 								else
 								{
 									rateValueTextbox.setText(rateValueTextbox.getOldValue());
 								}			
 						}});
 
 						matrix.setWidget(j+1, i+1, rateValueTextbox);
 						formatter.setHorizontalAlignment(j+1, i+1, HasHorizontalAlignment.ALIGN_CENTER);
 						formatter.setStyleName(j+1, i+1,"square-Matrix");
 					}
 									
 					RateValueLabel totalLabel= new RateValueLabel(listOfRequirements.get(i).getId());
 					totalLabel.setText("0");
 	
 					//SummaryElementHyperLinkElement tradeoffReasonLink = new SummaryElementHyperLinkElement(softwarePackages.get(i).getId(), "Tradeoff Reason");
 					if(j>=1)
 					{
 						matrix.setWidget(j+1, listOfRequirements.size()+1, totalLabel);
 						//matrix.setWidget(j+1, listOfRequirements.size()+2, tradeoffReasonLink);
 						formatter.setHorizontalAlignment(j+1, listOfRequirements.size()+1,  HasHorizontalAlignment.ALIGN_CENTER);
 						formatter.setStyleName(j+1, listOfRequirements.size()+1,"square-Matrix");
 					}
 				}
 				else
 				{
 					int aID=attributes.get(i-listOfRequirements.size()).getId();
 					int pID=softwarePackages.get(j).getId();				
 					int value = getValueFromlistOfRateValues(pID,aID);
 					
 					Label valueLabel = new Label(String.valueOf(value));
 					valueLabel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
 					matrix.setWidget(j+1, i+1, valueLabel);
 					formatter.setHorizontalAlignment(j+1, i+1, HasHorizontalAlignment.ALIGN_CENTER);
 					formatter.setStyleName(j+1, i+1,"square-Matrix");
 		
 					RateValueLabel totalLabel= new RateValueLabel(attributes.get(i-listOfRequirements.size()).getId());
 					totalLabel.setText("0");
 	
 					
 					if(j>=1)
 					{
 					matrix.setWidget(j+1, attributes.size()+listOfRequirements.size()+1, totalLabel);
 					
 					//System.out.println("here........"+softwarePackages.get(i).getId());
 					//SummaryElementHyperLinkElement tradeoffReasonLink = new SummaryElementHyperLinkElement(softwarePackages.get(i).getId(), "Tradeoff Reason");
 					//matrix.setWidget(j+1, listOfRequirements.size()+2, tradeoffReasonLink);
 					
 					formatter.setHorizontalAlignment(j+1, attributes.size()+listOfRequirements.size()+1,  HasHorizontalAlignment.ALIGN_CENTER);
 					formatter.setStyleName(j+1, attributes.size()+listOfRequirements.size()+1,"square-Matrix");	
 					}
 				}
 
 			}
 		}
 	}
 	/**
 	 * Get the totals traversing the current Matrix FlexTable
 	 */
 	public void getTotalsFromMatrix()
 	{
 		for(int i=2; i<=softwarePackages.size();i++)
 		{	int sum=0;
 			
 			for(int j=1; j<=attributes.size()+listOfRequirements.size();j++)
 			{
 				
 					Widget w =  matrix.getWidget(i, j);
 					Widget weight =  matrix.getWidget(1, j);
 					
 					if ( w instanceof RateValueTextbox) 
 					{
 						RateValueTextbox new_name = (RateValueTextbox) w;
 						RateValueTextbox weightVal = (RateValueTextbox) weight;
 						//Window.alert(new_name.getText());
 						//sum=sum+ Integer.parseInt(new_name.getText());
 						sum += Integer.parseInt(new_name.getText()) * Integer.parseInt(weightVal.getText());
 					}
 					else if( w instanceof Label)
 					{
 						Label new_name = (Label) w;
 						Label weightVal = (Label) weight;
 						//Window.alert(new_name.getText());
 						//sum=sum+ Integer.parseInt(new_name.getText());
 						sum += Integer.parseInt(new_name.getText()) * Integer.parseInt(weightVal.getText());
 					}
 					
 			}
 			
 //				Widget totalLabel =  matrix.getWidget(softwarePackages.size()+1, i);
 			Widget totalLabel =  matrix.getWidget(i, attributes.size()+listOfRequirements.size()+1);
 			RateValueLabel new_name = (RateValueLabel) totalLabel;
 			new_name.setText(String.valueOf(sum));
 				//new_name.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
 		}
 		
 		
 	}
 
 
 	/**
 	 * Traverse the total row in the Matrix and get the top 1 rated technique
 	 * including ties
 	 * @return List<GwtTechnique> top 1 rated technique(s) remember it could be ties in 1st place
 	 */
 	private List<GwtSoftwarePackage> getTopValues()
 	{
 		List<GwtSoftwarePackage> topPackages = new ArrayList<GwtSoftwarePackage>();
 		
 		int max=-1;
 		for(int i=1; i<=softwarePackages.size();i++)
 		{
 				Widget widget =  matrix.getWidget(attributes.size()+1, i);	
 				RateValueLabel totalLabel = (RateValueLabel) widget;
 			
 				if(max<Integer.parseInt(totalLabel.getText()) )
 					{
 						max=Integer.parseInt(totalLabel.getText());
 					}		
 		}
 		for(int i=1; i<=softwarePackages.size();i++)
 		{
 				Widget widget =  matrix.getWidget(attributes.size()+1, i);	
 				RateValueLabel totalLabel = (RateValueLabel) widget;
 			
 				if(max==Integer.parseInt(totalLabel.getText()) )
 					{
 						topPackages.add(getPackageFromListOfPackages(totalLabel.getTecniqueID()));
 					}		
 		}
 		
 		return topPackages;	
 	}
 	/**
 	 * This method search the techniques in the listloaded by RCP initially
 	 * @param tecniqueID
 	 * @return GwtTechnique for that ID
 	 */
 	private GwtSoftwarePackage getPackageFromListOfPackages(int tecniqueID)
 	{
 		for(int i=0; i<softwarePackages.size();i++)
 		{
 				if(softwarePackages.get(i).getId()==tecniqueID)
 				{
 					return 	softwarePackages.get(i);
 				}
 		}
 		return null;
 	}
 	/**
 	 * This method search the rate values in the loaded by RCP initially
 	 * @param techniqueID
 	 * @param evaluationID
 	 * @return rateValue
 	 */
 	private int getValueFromlistOfRateValues(int packageID, int attributeID)
 	{
 		for(int j=0; j<ratings.size();j++)
 		{
 			int aID=ratings.get(j).getAttributeId();
 			int pID =ratings.get(j).getPackageId();
 			
 			if(packageID==pID && attributeID==aID)
 			{
 				return ratings.get(j).getValue();
 			}			
 		}
 		return 0;
 	}
 	
 	private int getValueFromlistOfRequirementRateValues(int packageID, int requirementID)
 	{
 		for(int j=0; j<requirementRatings.size();j++)
 		{
 			int rID=requirementRatings.get(j).getRequirementId();
 			int pID=requirementRatings.get(j).getPackageId();
 			
 			if(requirementID==rID && packageID==pID)
 			{
 				return requirementRatings.get(j).getValue();
 			}			
 		}
 		return 0;
 	}
 	
 	private void setValueFromlistOfRateValues(int attributeID, int packageID, int value)
 	{
 		for(int j=0; j<ratings.size();j++)
 		{
 			int aID=ratings.get(j).getAttributeId();
 			int pID =ratings.get(j).getPackageId();
 			
 			if(attributeID==aID && packageID==pID)
 			{
 				ratings.get(j).setValue(value);
 			}	
 		}
 	}
 	
 	private void setValueFromlistOfRequirementRateValues(int requirementID, int packageID, int value)
 	{
 		for(int j=0; j<requirementRatings.size();j++)
 		{
 			int rID=requirementRatings.get(j).getRequirementId();
 			int pID =requirementRatings.get(j).getPackageId();
 			
 			if(requirementID==rID && packageID==pID)
 			{
 				requirementRatings.get(j).setValue(value);
 			}	
 		}
 	}
 	/**
 	 * This class extend the texbox to add 
 	 * the techniqueID and evaluationID as properties
 	 */
 	class RateValueTextbox extends TextBox
 	{
 		private int tecniqueID;
 		private int evaluationID;
 		private String oldValue= "-1";
 		public RateValueTextbox(int tecniqueID, int evaluationID, int value) {
 			this.tecniqueID=tecniqueID;
 			this.evaluationID=evaluationID;
 			
 			this.setWidth("35px");
 			this.setText(String.valueOf(value));;
 			this.setOldValue(String.valueOf(value));;
 		}
 		public void setTecniqueID(int tecniqueID) {
 			this.tecniqueID = tecniqueID;
 		}
 		public int getTecniqueID() {
 			return tecniqueID;
 		}
 		public void setEvaluationID(int evaluationID) {
 			this.evaluationID = evaluationID;
 		}
 		public int getEvaluationID() {
 			return evaluationID;
 		}
 		public void setOldValue(String oldValue) {
 			this.oldValue = oldValue;
 		}
 		public String getOldValue() {
 			return oldValue;
 		}
 	}
 
 	class RateValueLabel extends Label
 	{
 		private int tecniqueID;
 		
 		public RateValueLabel(int tecniqueID) {
 			this.tecniqueID=tecniqueID;		
 			this.setWidth("35px");
 		}
 		public void setTecniqueID(int tecniqueID) {
 			this.tecniqueID = tecniqueID;
 		}
 		public int getTecniqueID() {
 			return tecniqueID;
 		}
 	}
 	
 	
 	
 	
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
 
 	
 	
 	public void updateCommand(GwtTradeoffReason localTradeoffReason)
 	{
 		updateTradeoffReasoninDB(localTradeoffReason);
 	}
 }
