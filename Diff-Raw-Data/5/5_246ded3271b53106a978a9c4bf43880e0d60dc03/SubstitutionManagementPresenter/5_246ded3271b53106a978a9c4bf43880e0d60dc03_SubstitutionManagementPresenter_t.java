 package com.example.test.task.client.presenter;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Comparator;
 import java.util.LinkedHashMap;
 import java.util.List;
 
 import com.example.test.task.client.Messages;
 import com.example.test.task.client.SubstitutionManagementServiceAsync;
 import com.example.test.task.client.event.CreateSubstitutionEvent;
 import com.example.test.task.client.event.EditSubstitutionEvent;
 import com.example.test.task.client.event.UpdateDataEvent;
 import com.example.test.task.client.event.UpdateDataEventHandler;
 import com.example.test.task.client.view.StatusIndicator;
 import com.example.test.task.client.view.SubstitutionManagementView;
 import com.example.test.task.shared.SubstitutionDetails;
 import com.example.test.task.shared.Utils;
 import com.google.gwt.event.shared.EventBus;
 import com.google.gwt.user.cellview.client.Column;
 import com.google.gwt.user.cellview.client.TextColumn;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.view.client.ProvidesKey;
 
 /**
  * Substitution management view presenter.
  * 
  * @author Ilya Sviridov
  *
  */
 public class SubstitutionManagementPresenter implements Presenter,
 		SubstitutionManagementView.Presenter<SubstitutionDetails> {
 	/**
 	 * Messages
 	 */
 	private Messages messages;
 	/**
 	 * View
 	 */
 	SubstitutionManagementView<SubstitutionDetails> view;
 	/**
 	 * RPC stub
 	 */
 	SubstitutionManagementServiceAsync service;
 	/**
 	 * Status indicator. See {@link StatusIndicator}}
 	 */
 	StatusIndicator statusIndicator;
 
 	/**
 	 * Event bus
 	 */
 	EventBus eventBus;
 
 	/**
 	 * Constructor 
 	 * @param view
 	 * @param service
 	 * @param statusIndicator
 	 * @param eventBus
 	 * @param messages
 	 */
 	public SubstitutionManagementPresenter(
 			SubstitutionManagementView<SubstitutionDetails> view,
 			SubstitutionManagementServiceAsync service,StatusIndicator statusIndicator, EventBus eventBus, Messages messages) {
 		super();
 		this.view = view;
 		this.service = service;
 		this.eventBus = eventBus;
 		this.statusIndicator=statusIndicator;
 		this.messages=messages;
 		view.setPresenter(this);
 		initView();
 		fetchData();
 
 		eventBus.addHandler(UpdateDataEvent.TYPE, new UpdateDataEventHandler() {
 			public void onUpdateDataEvent(UpdateDataEvent event) {
 				fetchData();
 			}
 		});
 	}
 
 	/**
 	 * Go method
 	 */
 	public void go() {
 		view.go();
 	}
 	
 	/**
 	 * Called by view on create action 
 	 */
 	public void onCreateAction() {
 		eventBus.fireEvent(new CreateSubstitutionEvent());
 	}
 
 	/**
 	 * Called by view on update action 
 	 */
 	public void onUpdateAction() {
 		if(view.getSelectedItems().size()==1){
 			 eventBus.fireEvent(new EditSubstitutionEvent(view.getSelectedItems().iterator().next().getId()));
 		}else{
 			statusIndicator.setErrorStatus(messages.statusInternalError());
 		}
 	}
 	
 	/**
 	 * Called by view on delete action 
 	 */
 	@SuppressWarnings("rawtypes")
 	public void onDeleteAction() {
 		
 		Collection<SubstitutionDetails> selectedItems=view.getSelectedItems();
 		if(selectedItems.isEmpty()) return;
 		 
 		statusIndicator.setInfoStatus(messages.statusDeleting());
 		List<Integer> ids=new ArrayList<Integer>();
 		for(SubstitutionDetails item:selectedItems)
 			ids.add(item.getId());
 		
		service.deleteSubstitution(ids,new AsyncCallback<List<SubstitutionDetails>>() {
 			public void onFailure(Throwable caught) {
 				statusIndicator.setErrorStatus(messages.statusErrorDuringDeleting(caught.getMessage()));				
 			}
 
 			@SuppressWarnings("unchecked")
 			public void onSuccess(List result) {
 				view.setData(result);
 				statusIndicator.clear();
 				view.enableDeleteControl(false);
 				view.enableUpdateControl(false);
 			}
 		}); 
 	}
 	
 	/**
 	 * Loads data from server
 	 */
 	@SuppressWarnings("rawtypes")
 	protected void fetchData() {
 		
 		  statusIndicator.setInfoStatus(messages.statusLoadingData());
		  service.getSubstitutions(new AsyncCallback<List<SubstitutionDetails>>() {
 			
 			@SuppressWarnings("unchecked")
 			public void onSuccess(List result) {
 				view.setData(result);
 				statusIndicator.clear();				
 			}
 			
 			public void onFailure(Throwable caught) {
 				statusIndicator.setErrorStatus(messages.statusLoadingProblems(caught.getMessage()));				
 			}
 		});
 		 
 	}
 	
 	/**
 	 * Called on select event
 	 */
 	public void onSelect(Collection<SubstitutionDetails> selctedItems) {
 		if (selctedItems.size() == 1) {
 			view.enableDeleteControl(true);
 			view.enableUpdateControl(true);
 		} else {
 			// more than one selected
 			if (selctedItems.size() > 0) {
 				view.enableDeleteControl(true);
 				view.enableUpdateControl(false);
 			} else {
 				// nothing selected
 				view.enableDeleteControl(false);
 				view.enableUpdateControl(false);
 			}
 		}
 	}
 	
 	/**
 	 * Initalize view with table data structure and behaviour
 	 */
 	private void initView(){
 		ProvidesKey<SubstitutionDetails> providesKey=new ProvidesKey<SubstitutionDetails>() {
 			public Integer getKey(SubstitutionDetails item) {
 				return item.getId();
 			};
 		};
 		
 		LinkedHashMap<String, Column<SubstitutionDetails, ?>> columns=new LinkedHashMap<String, Column<SubstitutionDetails, ?>>();
 		LinkedHashMap<String, Comparator<SubstitutionDetails>> comparators=new LinkedHashMap<String, Comparator<SubstitutionDetails>>();
 		
 		//name column
 		TextColumn<SubstitutionDetails> nameColumn=new TextColumn<SubstitutionDetails>() {
 			@Override
 			public String getValue(SubstitutionDetails object) {
 				return object.getName();
 			}
 		};
 		nameColumn.setSortable(true);
 		
 		columns.put(messages.tableColumnNameSubstitute(), nameColumn);
 		comparators.put(messages.tableColumnNameSubstitute(), new Comparator<SubstitutionDetails>() {
 			public int compare(SubstitutionDetails o1, SubstitutionDetails o2) {
 				return Utils.compare(o1.getName(), o2.getName());
 			}
 		});
 		
 		
 		//role column
 		TextColumn<SubstitutionDetails> roleColumn=new TextColumn<SubstitutionDetails>() {
 			@Override
 			public String getValue(SubstitutionDetails object) {
 				return messages.roles(object.getRole());
 			}
 		};
 		roleColumn.setSortable(true);
 		
 		columns.put(messages.tableColumnNameRole(), roleColumn);
 		comparators.put(messages.tableColumnNameRole(), new Comparator<SubstitutionDetails>() {
 			public int compare(SubstitutionDetails o1, SubstitutionDetails o2) {
 				return Utils.compare(o1.getRole(), o2.getRole());
 			}
 		});
 		
 		
 
 		//ruletype column
 		TextColumn<SubstitutionDetails> ruleTypeColumn=new TextColumn<SubstitutionDetails>() {
 			@Override
 			public String getValue(SubstitutionDetails object) {
 				return messages.rules(object.getRuleType());
 			}
 		};
 		ruleTypeColumn.setSortable(true);
 		
 		columns.put(messages.tableColumnNameRuleType(), ruleTypeColumn);
 		comparators.put(messages.tableColumnNameRuleType(), new Comparator<SubstitutionDetails>() {
 			public int compare(SubstitutionDetails o1, SubstitutionDetails o2) {
 				return Utils.compare(o1.getRuleType(), o2.getRuleType());
 			}
 		});
 		
 		
 		//beginDate column
 		TextColumn<SubstitutionDetails> beginDateColumn=new TextColumn<SubstitutionDetails>() {
 			@Override
 			public String getValue(SubstitutionDetails object) {
 				return object.getEndDate()!=null?messages.dateFormat(object.getBeginDate()):"";
 			}
 		};
 		beginDateColumn.setSortable(true);
 		
 		columns.put(messages.tableColumnNameBegin(), beginDateColumn);
 		comparators.put(messages.tableColumnNameBegin(), new Comparator<SubstitutionDetails>() {
 			public int compare(SubstitutionDetails o1, SubstitutionDetails o2) {
 				return Utils.compare(o1.getBeginDate(), o2.getBeginDate());
 			}
 		});
 		
 		
 		//beginDate column
 		TextColumn<SubstitutionDetails> endDateColumn=new TextColumn<SubstitutionDetails>() {
 			@Override
 			public String getValue(SubstitutionDetails object) {
 				return object.getEndDate()!=null?messages.dateFormat(object.getEndDate()):"";
 			}
 		};
 		endDateColumn.setSortable(true);
 		
 		columns.put(messages.tableColumnNameEnd(), endDateColumn);
 		comparators.put(messages.tableColumnNameEnd(), new Comparator<SubstitutionDetails>() {
 			public int compare(SubstitutionDetails o1, SubstitutionDetails o2) {
 				return Utils.compare(o1.getEndDate(), o2.getEndDate());
 			}
 		});
 
 		view.init(providesKey, columns,comparators);
 	}
 	
 }
