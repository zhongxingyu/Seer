 package com.duggan.workflow.client.ui.home;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 
 import com.duggan.workflow.client.model.MODE;
 import com.duggan.workflow.client.model.TaskType;
 import com.duggan.workflow.client.place.NameTokens;
 import com.duggan.workflow.client.service.ServiceCallback;
 import com.duggan.workflow.client.service.TaskServiceCallback;
 import com.duggan.workflow.client.ui.MainPagePresenter;
 import com.duggan.workflow.client.ui.activityfeed.ActivitiesPresenter;
 import com.duggan.workflow.client.ui.addDoc.DocumentPopupPresenter;
 import com.duggan.workflow.client.ui.document.GenericDocumentPresenter;
 import com.duggan.workflow.client.ui.events.ActivitiesSelectedEvent;
 import com.duggan.workflow.client.ui.events.ActivitiesSelectedEvent.ActivitiesSelectedHandler;
 import com.duggan.workflow.client.ui.events.AfterSaveEvent;
 import com.duggan.workflow.client.ui.events.AfterSaveEvent.AfterSaveHandler;
 import com.duggan.workflow.client.ui.events.AfterSearchEvent;
 import com.duggan.workflow.client.ui.events.AlertLoadEvent;
 import com.duggan.workflow.client.ui.events.AlertLoadEvent.AlertLoadHandler;
 import com.duggan.workflow.client.ui.events.CreateDocumentEvent;
 import com.duggan.workflow.client.ui.events.CreateDocumentEvent.CreateDocumentHandler;
 import com.duggan.workflow.client.ui.events.DocumentSelectionEvent;
 import com.duggan.workflow.client.ui.events.DocumentSelectionEvent.DocumentSelectionHandler;
 import com.duggan.workflow.client.ui.events.LoadAlertsEvent;
 import com.duggan.workflow.client.ui.events.PresentTaskEvent;
 import com.duggan.workflow.client.ui.events.ProcessingCompletedEvent;
 import com.duggan.workflow.client.ui.events.ProcessingCompletedEvent.ProcessingCompletedHandler;
 import com.duggan.workflow.client.ui.events.ProcessingEvent;
 import com.duggan.workflow.client.ui.events.ProcessingEvent.ProcessingHandler;
 import com.duggan.workflow.client.ui.events.ReloadEvent;
 import com.duggan.workflow.client.ui.events.ReloadEvent.ReloadHandler;
 import com.duggan.workflow.client.ui.events.SearchEvent;
 import com.duggan.workflow.client.ui.events.SearchEvent.SearchHandler;
 import com.duggan.workflow.client.ui.filter.FilterPresenter;
 import com.duggan.workflow.client.ui.login.LoginGateKeeper;
 import com.duggan.workflow.client.ui.save.CreateDocPresenter;
 import com.duggan.workflow.client.ui.save.form.GenericFormPresenter;
 import com.duggan.workflow.client.ui.tasklistitem.DateGroupPresenter;
 import com.duggan.workflow.client.ui.util.DateUtils;
 import com.duggan.workflow.client.ui.util.DocMode;
 import com.duggan.workflow.client.util.AppContext;
 import com.duggan.workflow.shared.model.Doc;
 import com.duggan.workflow.shared.model.DocStatus;
 import com.duggan.workflow.shared.model.Document;
 import com.duggan.workflow.shared.model.DocumentType;
 import com.duggan.workflow.shared.model.HTSummary;
 import com.duggan.workflow.shared.model.SearchFilter;
 import com.duggan.workflow.shared.requests.GetTaskList;
 import com.duggan.workflow.shared.responses.GetTaskListResult;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.event.dom.client.HasClickHandlers;
 import com.google.gwt.event.dom.client.KeyCodes;
 import com.google.gwt.event.dom.client.KeyUpEvent;
 import com.google.gwt.event.dom.client.KeyUpHandler;
 import com.google.gwt.event.shared.EventBus;
 import com.google.gwt.event.shared.GwtEvent.Type;
 import com.google.gwt.user.client.History;
 import com.google.gwt.user.client.Timer;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.ui.Anchor;
 import com.google.gwt.user.client.ui.TextBox;
 import com.google.inject.Inject;
 import com.google.inject.Provider;
 import com.gwtplatform.common.client.IndirectProvider;
 import com.gwtplatform.common.client.StandardProvider;
 import com.gwtplatform.dispatch.shared.DispatchAsync;
 import com.gwtplatform.mvp.client.Presenter;
 import com.gwtplatform.mvp.client.View;
 import com.gwtplatform.mvp.client.annotations.ContentSlot;
 import com.gwtplatform.mvp.client.annotations.NameToken;
 import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
 import com.gwtplatform.mvp.client.annotations.UseGatekeeper;
 import com.gwtplatform.mvp.client.proxy.PlaceManager;
 import com.gwtplatform.mvp.client.proxy.PlaceRequest;
 import com.gwtplatform.mvp.client.proxy.ProxyPlace;
 import com.gwtplatform.mvp.client.proxy.RevealContentEvent;
 import com.gwtplatform.mvp.client.proxy.RevealContentHandler;
 
 public class HomePresenter extends
 		Presenter<HomePresenter.MyView, HomePresenter.MyProxy> implements AfterSaveHandler,
 		DocumentSelectionHandler, ReloadHandler, AlertLoadHandler, ActivitiesSelectedHandler,
 		ProcessingHandler, ProcessingCompletedHandler, SearchHandler,CreateDocumentHandler{
 
 	public interface MyView extends View {
 		HasClickHandlers getAddButton();	
 		void setHeading(String heading);
 		void bindAlerts(HashMap<TaskType, Integer> alerts);
 		HasClickHandlers getRefreshButton();
 		public void setHasItems(boolean hasItems);
 		void setTaskType(TaskType currentTaskType);
 		void showActivitiesPanel(boolean b);
 		public Anchor getaDrafts();
 		public Anchor getaProgress();
 		public Anchor getaApproved();
 		public Anchor getaRejected();
 		public Anchor getaNewReq();
 		public Anchor getaRecentApprovals();
 		public Anchor getaFlagged();
 		public Anchor getaRefresh() ;
 		TextBox getSearchBox();
 		public void hideFilterDialog();
 		public void setSearchBox(String text);
 		void showmask(boolean b);
 	}
 
 	@ProxyCodeSplit
 	@NameToken(NameTokens.home)
 	@UseGatekeeper(LoginGateKeeper.class)
 	public interface MyProxy extends ProxyPlace<HomePresenter> {
 	}
 
 	public static final Object DATEGROUP_SLOT = new Object();
 	@ContentSlot
 	public static final Type<RevealContentHandler<?>> DOCPOPUP_SLOT = new Type<RevealContentHandler<?>>();
 	@ContentSlot
 	public static final Type<RevealContentHandler<?>> DOCUMENT_SLOT = new Type<RevealContentHandler<?>>();
 	@ContentSlot
 	public static final Type<RevealContentHandler<?>> FILTER_SLOT = new Type<RevealContentHandler<?>>();
 	@ContentSlot
 	public static final Type<RevealContentHandler<?>> ACTIVITIES_SLOT = new Type<RevealContentHandler<?>>();
 	@ContentSlot
 	public static final Type<RevealContentHandler<?>> ADMIN_SLOT = new Type<RevealContentHandler<?>>();
 	
 	@Inject DispatchAsync dispatcher;
 	@Inject PlaceManager placeManager;
 	@Inject DocumentPopupPresenter docPopup;
 	
 	private IndirectProvider<CreateDocPresenter> createDocProvider;
 	private IndirectProvider<GenericFormPresenter> genericFormProvider;
 	private IndirectProvider<GenericDocumentPresenter> docViewFactory;
 	private IndirectProvider<DateGroupPresenter> dateGroupFactory;
 	private IndirectProvider<ActivitiesPresenter> activitiesFactory;
 	
 	private TaskType currentTaskType;
 	
 	/**
 	 * on select documentId
 	 */
 	private Long selectedDocumentId;
 	
 	/**
 	 * Url processInstanceId (pid) - required incase the use hits refresh
 	 */
 	private Long processInstanceId=null;
 	
 	/**
 	 * Url documentId (did) - required incase the use hits refresh
 	 */
 	private Long documentId=null;
 	
 	
 	@Inject FilterPresenter filterPresenter;
 	Timer timer = new Timer() {
 		
 		@Override
 		public void run() {
 			search();
 		}
 	};
 	
 	@Inject
 	public HomePresenter(final EventBus eventBus, final MyView view,
 			final MyProxy proxy,
 			Provider<CreateDocPresenter> docProvider,
 			Provider<GenericFormPresenter> formProvider,
 			Provider<GenericDocumentPresenter> docViewProvider,
 			Provider<DateGroupPresenter> dateGroupProvider,
 			Provider<ActivitiesPresenter> activitiesProvider) {
 		super(eventBus, view, proxy);
 		
 		createDocProvider = new StandardProvider<CreateDocPresenter>(docProvider);
 		docViewFactory  = new StandardProvider<GenericDocumentPresenter>(docViewProvider);
 		dateGroupFactory = new StandardProvider<DateGroupPresenter>(dateGroupProvider);
 		genericFormProvider = new StandardProvider<GenericFormPresenter>(formProvider);
 		activitiesFactory = new StandardProvider<ActivitiesPresenter>(activitiesProvider);
 	}
 
 	protected void search() {
 		timer.cancel();
 		if(searchTerm.isEmpty()){
 			loadTasks();
 			return;
 		}
 		
 		//fireEvent(new ProcessingEvent());
 		SearchFilter filter = new SearchFilter();
 		filter.setSubject(searchTerm);
 		//filter.setPhrase(searchTerm);
 		search(filter);
 	}
 	
 	public void search(final SearchFilter filter){
 			
 		GetTaskList request = new GetTaskList(AppContext.getUserId(), filter);
 		fireEvent(new ProcessingEvent());
 		dispatcher.execute(request, new TaskServiceCallback<GetTaskListResult>(){
 			@Override
 			public void processResult(GetTaskListResult result) {		
 				
 				GetTaskListResult rst = (GetTaskListResult)result;
 				List<Doc> tasks = rst.getTasks();
 				loadLines(tasks);
 				if(tasks.isEmpty())
 					getView().setHasItems(false);
 				else
 					getView().setHasItems(true);
 				
 				fireEvent(new AfterSearchEvent(filter.getSubject(), filter.getPhrase()));
 				fireEvent(new ProcessingCompletedEvent());
 			}
 		});		
 	}
 
 	@Override
 	protected void revealInParent() {
 		RevealContentEvent.fire(this, MainPagePresenter.CONTENT_SLOT, this);
 	}
 	
 	String searchTerm="";
 	
 	@Override
 	protected void onBind() {
 		super.onBind();
 		addRegisteredHandler(AfterSaveEvent.TYPE, this);
 		addRegisteredHandler(DocumentSelectionEvent.TYPE, this);
 		addRegisteredHandler(ReloadEvent.TYPE, this);
 		addRegisteredHandler(AlertLoadEvent.TYPE, this);
 		addRegisteredHandler(ActivitiesSelectedEvent.TYPE, this);
 		addRegisteredHandler(ProcessingEvent.TYPE, this);
 		addRegisteredHandler(ProcessingCompletedEvent.TYPE, this);
 		addRegisteredHandler(SearchEvent.TYPE, this);
 		addRegisteredHandler(CreateDocumentEvent.TYPE, this);
 		
 		getView().getSearchBox().addKeyUpHandler(new KeyUpHandler() {
 			
 			@Override
 			public void onKeyUp(KeyUpEvent event) {
 				String txt = getView().getSearchBox().getValue().trim();
 				
 				if(!txt.equals(searchTerm) || event.getNativeKeyCode()==KeyCodes.KEY_ENTER){
 					searchTerm = txt;
 					timer.cancel();
 					timer.schedule(400);
 				}
 				
 			}
 		});
 		
 		/*getView().getAddButton().addClickHandler(new ClickHandler() {
 			@Override
 			public void onClick(ClickEvent event) {
 				//showEditForm(MODE.CREATE);
 				//showEditForm();
 				
 				
 				getView().setDocPopupVisible();
 			}
 			
 		});*/
 		
 		getView().getRefreshButton().addClickHandler(new ClickHandler() {
 			@Override
 			public void onClick(ClickEvent event) {				
 				loadTasks();
 			}
 		});
 		
 		
 		getView().getaDrafts().addClickHandler(new ClickHandler() {		
 			@Override
 			public void onClick(ClickEvent event) {
 				History.newItem("home;type=drafts");
 			}
 		});
 		
 
 		getView().getaProgress().addClickHandler(new ClickHandler() {		
 			@Override
 			public void onClick(ClickEvent event) {
 				History.newItem("home;type=inprog");
 			}
 		});
 		
 		getView().getaApproved().addClickHandler(new ClickHandler() {		
 			@Override
 			public void onClick(ClickEvent event) {
 				History.newItem("home;type=approved");
 			}
 		});
 		
 		getView().getaRejected().addClickHandler(new ClickHandler() {		
 			@Override
 			public void onClick(ClickEvent event) {
 				History.newItem("home;type=rejected");
 			}
 		});
 		
 		getView().getaNewReq().addClickHandler(new ClickHandler() {
 			@Override
 			public void onClick(ClickEvent event) {
 				History.newItem("home;type=appreqnew");
 			}
 		});
 		
 		
 		getView().getaRecentApprovals().addClickHandler(new ClickHandler() {
 			@Override
 			public void onClick(ClickEvent event) {				
 				History.newItem("home;type=appredone");
 			}
 		});
 		
 		getView().getaFlagged().addClickHandler(new ClickHandler() {		
 			@Override
 			public void onClick(ClickEvent event) {
 				History.newItem("home;type=flagged");				
 			}
 		});
 	}
 
 	/**
 	 * 
 	 */
 	@Override
 	public void prepareFromRequest(PlaceRequest request) {
 		super.prepareFromRequest(request);
 		
 		fireEvent(new LoadAlertsEvent());
 		clear();		
 		processInstanceId=null;
 		documentId=null;
 		
 		//String name = request.getParameter("type", TaskType.DRAFT.getURL());
 		String name = request.getParameter("type", null);
 		String processInstID = request.getParameter("pid", null);
 		String documentSearchID = request.getParameter("did", null);
 		if(processInstID!=null){
 			processInstanceId = Long.parseLong(processInstID);
 		}
 		
 		if(documentSearchID!=null){
 			documentId = Long.parseLong(documentSearchID);
 		}
 		
 		if(name==null){
 			getView().setTaskType(null);
 			activitiesFactory.get(new ServiceCallback<ActivitiesPresenter>() {
 				@Override
 				public void processResult(ActivitiesPresenter presenter) {
 
 					setInSlot(ACTIVITIES_SLOT, presenter);
 					presenter.loadActivities();
 				}
 			});
 		}else{
 			TaskType type = TaskType.getTaskType(name);
 			this.currentTaskType=type;
 			
 			getView().setTaskType(currentTaskType);
 			loadTasks(type);
 		}
 					
 		
 	}	
 
 	private void clear() {		
 		//clear document slot
 		setInSlot(DATEGROUP_SLOT, null);
 		setInSlot(DOCUMENT_SLOT, null);
 	}
 	
 	private void loadTasks() {
 		loadTasks(currentTaskType);
 	}
 
 	/**
 	 * Load JBPM records
 	 * @param type
 	 */
 	private void loadTasks(final TaskType type) {
 		clear();
 		getView().setHeading(type.getTitle());
 		
 		String userId = AppContext.getUserId();
 		
 		GetTaskList request = new GetTaskList(userId,currentTaskType);
 		request.setProcessInstanceId(processInstanceId);
 		request.setDocumentId(documentId);
 		
 		//System.err.println("###### Search:: did="+documentId+"; PID="+processInstanceId+"; TaskType="+type);
 		
 		fireEvent(new ProcessingEvent());
 		dispatcher.execute(request, new TaskServiceCallback<GetTaskListResult>(){
 			@Override
 			public void processResult(GetTaskListResult result) {		
 				
 				GetTaskListResult rst = (GetTaskListResult)result;
 				List<Doc> tasks = rst.getTasks();
 				loadLines(tasks);
 				
 				if(tasks.size()>0){
 					getView().setHasItems(true);
 					
 					Doc doc = tasks.get(0);
 					Long docId=null;
 					DocMode docMode = DocMode.READ;
 					
 					if(doc instanceof Document){
 						docId = (Long)doc.getId();
 						if(((Document)doc).getStatus()==DocStatus.DRAFTED){
 							docMode = DocMode.READWRITE;
 						}
 						//Load document
 						fireEvent(new DocumentSelectionEvent(docId,null,docMode));
 					}else{
 						docId = ((HTSummary)doc).getDocumentRef();
 						long taskId = ((HTSummary)doc).getId(); 
 						//Load Task
 						fireEvent(new DocumentSelectionEvent(docId,taskId,docMode));
 					}
 					
 				}else{
 					getView().setHasItems(false);
 				}
 				
 				fireEvent(new ProcessingCompletedEvent());
 			}
 			
 		});
 	}
 	
 	/**
 	 * 
 	 * @param tasks
 	 */
 	protected void loadLines(final List<Doc> tasks) {
 		setInSlot(DATEGROUP_SLOT, null);
 		final List<Date> dates=new ArrayList<Date>();
 		
 		for(int i=0; i< tasks.size(); i++){
 			//final String dt = DateUtils.FULLDATEFORMAT.format(tasks.get(i).getCreated());
 			final Doc doc = tasks.get(i);
 			final String dt = DateUtils.DATEFORMAT.format(doc.getCreated());
 			final Date date = DateUtils.DATEFORMAT.parse(dt);
 			
 			if(dates.contains(date)){
 				fireEvent(new PresentTaskEvent(doc));
 			}else{
 				dateGroupFactory.get(new ServiceCallback<DateGroupPresenter>() {
 					@Override
 					public void processResult(DateGroupPresenter result) {
 						result.setDate(doc.getCreated());
 						HomePresenter.this.addToSlot(DATEGROUP_SLOT, result);						
 						fireEvent(new PresentTaskEvent(doc));						
 						dates.add(date);
 					}
 				});
 				
 			}
 		}
 	
 	}
 	
 	protected void showEditForm(final MODE mode) {
 		createDocProvider.get(new ServiceCallback<CreateDocPresenter>() {
 			@Override
 			public void processResult(CreateDocPresenter result) {
 				if(mode.equals(MODE.EDIT) && selectedDocumentId!=null){
 					result.setDocumentId(selectedDocumentId);
 				}
 					
 				addToPopupSlot(result, false);
 			}
 		});
 	}
 	
 	protected void showEditForm(final DocumentType type){
 		genericFormProvider.get(new ServiceCallback<GenericFormPresenter>() {
 			@Override
 			public void processResult(GenericFormPresenter result) {
 				result.setDocumentType(type);
 				addToPopupSlot(result, false);
 			}
 		});
 	}
 
 	@Override
 	protected void onReset() {
 		super.onReset();
 		//System.err.println("HomePresenter - OnReset :: "+this);
		setInSlot(FILTER_SLOT, filterPresenter);
		setInSlot(DOCPOPUP_SLOT, docPopup);
 	}
 
 	@Override
 	public void onAfterSave(AfterSaveEvent event) {
 		loadTasks();
 	}
 
 	@Override
 	public void onDocumentSelection(DocumentSelectionEvent event) {
 		this.selectedDocumentId=event.getDocumentId();
 		
 		displayDocument(event.getDocumentId(), event.getTaskId());
 	}
 	
 	private void displayDocument(final Long documentId, final Long taskId) {
 		if(documentId==null && taskId==null){
 			setInSlot(DOCUMENT_SLOT, null);
 			return;
 		}
 		
 		docViewFactory.get(new ServiceCallback<GenericDocumentPresenter>() {
 			@Override
 			public void processResult(GenericDocumentPresenter result) {
 				result.setDocId(documentId, taskId);
 				setInSlot(DOCUMENT_SLOT, result);
 			}
 		});
 	}
 	
 	@Override
 	public void onReload(ReloadEvent event) {
 		loadTasks();
 	}
 
 	@Override
 	public void onAlertLoad(AlertLoadEvent event) {
 		//event.getAlerts();
 		getView().bindAlerts(event.getAlerts());
 		Integer count = event.getAlerts().get(currentTaskType);
 		if(count==null) count=0;
 		if(currentTaskType!=null)
 		Window.setTitle(currentTaskType.getTitle()+ (count==0? "" : " ("+count+")"));
 	}
 
 	@Override
 	public void onActivitiesSelected(ActivitiesSelectedEvent event) {
 		getView().showActivitiesPanel(true);
 	}
 
 	@Override
 	public void onProcessingCompleted(ProcessingCompletedEvent event) {
 		getView().showmask(false);
 		
 	}
 
 	@Override
 	public void onProcessing(ProcessingEvent event) {		
 		getView().showmask(true);
 	}
 
 	@Override
 	public void onSearch(SearchEvent event) {
 		SearchFilter filter= event.getFilter();
 		search(filter);
 	}
 
 	@Override
 	public void onCreateDocument(CreateDocumentEvent event) {
 		DocumentType type = event.getDocType();	
 		
 		if(type.getFormId()!=null){
 			showEditForm(type);
 		}else{
 			showEditForm(MODE.CREATE);
 		}
 	}
 
 }
