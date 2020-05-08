 package com.scholastic.sbam.client.uiobjects.fields;
 
 import com.extjs.gxt.ui.client.Style.SortDir;
 import com.extjs.gxt.ui.client.data.BasePagingLoader;
 import com.extjs.gxt.ui.client.data.BeanModel;
 import com.extjs.gxt.ui.client.data.BeanModelReader;
 import com.extjs.gxt.ui.client.data.ModelData;
 import com.extjs.gxt.ui.client.data.PagingLoadConfig;
 import com.extjs.gxt.ui.client.data.PagingLoadResult;
 import com.extjs.gxt.ui.client.data.PagingLoader;
 import com.extjs.gxt.ui.client.data.RpcProxy;
 import com.extjs.gxt.ui.client.event.ComponentEvent;
 import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
 import com.extjs.gxt.ui.client.event.SelectionChangedListener;
 import com.extjs.gxt.ui.client.store.ListStore;
 import com.extjs.gxt.ui.client.widget.LayoutContainer;
 import com.extjs.gxt.ui.client.widget.MessageBox;
 import com.extjs.gxt.ui.client.widget.form.ComboBox;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.scholastic.sbam.client.services.ProxySearchService;
 import com.scholastic.sbam.client.services.ProxySearchServiceAsync;
 import com.scholastic.sbam.client.uiobjects.uiapp.CreateProxyDialog;
 import com.scholastic.sbam.client.uiobjects.uiapp.CreateProxyDialog.CreateProxyDialogSaver;
 import com.scholastic.sbam.shared.exceptions.ServiceNotReadyException;
 import com.scholastic.sbam.shared.objects.ProxyInstance;
 import com.scholastic.sbam.shared.objects.SimpleKeyProvider;
 import com.scholastic.sbam.shared.objects.SynchronizedPagingLoadResult;
 
 public class ProxySearchField extends ComboBox<BeanModel> implements CreateProxyDialogSaver {
 	
 	protected final ProxySearchServiceAsync proxySearchService = GWT.create(ProxySearchService.class);
 	
	protected final ProxyInstance		NO_PROXY_INSTANCE	= ProxyInstance.getEmptyInstance();
	protected final BeanModel			NO_PROXY			= ProxyInstance.obtainModel(NO_PROXY_INSTANCE);
	
 	private long						searchSyncId		=	0;
 
 	private boolean						includeNoneOption	=	true;
 	private boolean						includeAddOption	=	true;
 	private ProxyInstance				addInstance			=	null;
 	private ProxyInstance				noneInstance		=	null;
 
 	private String						sortField			=	"description";
 	private SortDir						sortDir				=	SortDir.ASC;
 	
 	protected LayoutContainer			createDialogContainer	= null;
 	
 	public ProxySearchField() {
 		super();
 		
 		PagingLoader<PagingLoadResult<ProxyInstance>> loader = getProxyLoader(); 
 		
 		ListStore<BeanModel> proxyStore = new ListStore<BeanModel>(loader);
 		proxyStore.setKeyProvider(new SimpleKeyProvider("uniqueKey"));
 		
 	//	this.setWidth(300);
 		this.setValueField("uniqueKey");
 		this.setDisplayField("descriptionAndId");
 		this.setEmptyText("Enter proxy search criteria here...");
 		this.setStore(proxyStore);
 		this.setMinChars(2);
 		this.setHideTrigger(false); 
 		this.setTriggerAction(TriggerAction.QUERY);
 		this.setTriggerStyle("trigger-square");
 		this.setPageSize(200);
 		this.setAllowBlank(true);
 		this.setEditable(true);
 		this.setSimpleTemplate(getResultTemplate());
 		this.setSelectOnFocus(true);
 		
 		this.addSelectionChangedListener(new SelectionChangedListener<BeanModel>() {
 
 			@Override
 			public void selectionChanged(SelectionChangedEvent<BeanModel> se) {
 				if (se.getSelectedItem() == null) {
					onSelectionChange(NO_PROXY_INSTANCE);
 				} else
 					onSelectionChange((ProxyInstance) se.getSelectedItem().getBean());
 			}
 			
 		});
 	}
 	
 	public ProxySearchField(LayoutContainer createDialogContainer) {
 		this();
 		this.createDialogContainer = createDialogContainer;
 	}
 	
 	public void onSelectionChange(ProxyInstance selected) {
 		if (selected == null) {
			this.setValue(NO_PROXY);
 			return;
 		}
 		if (selected.isAddNew()) {
 			this.setRawValue("");
 			return;
 		}
 		//	For add let the object using the field handle it
 		//	For an actual proxy, let the object using the field handle it
 	}
 	
 	
 	@Override
 	public boolean isDirty() {
 		if (disabled || !rendered) {
 	    	return false;
 	    }
 	    return !equalWithNull();
 	}
 
 	/**
 	 * Like Util.equalWithNull, but if the values are ModelData with a Store and a KeyProvider, use the key values provided.
 	 * @param obj1
 	 * @param obj2
 	 * @return
 	 */
 	public boolean equalWithNull() {
 		if (getSelectedValue() == originalValue) {
 			return true;
 		} else if (getSelectedValue() == null) {
 			return false;
 		} else if (getSelectedValue() instanceof ModelData && originalValue instanceof ModelData && this.getStore() != null && this.getStore().getKeyProvider() != null) {
 	    	if (originalValue == null)
 	    		return true;
 	    	String key1 = this.getStore().getKeyProvider().getKey(getSelectedValue());
 	    	String key2 = this.getStore().getKeyProvider().getKey(originalValue);
 	    	return (key1.equals(key2));
 	    } else {
 	    	return getSelectedValue().equals(originalValue);
 	    }
 	}
 	
 	public BeanModel getSelectedValue() {
 		return value;
 	}
 	
 	public ProxyInstance getSelectedProxy() {
 		if (value != null)
 			return (ProxyInstance) value.getBean();
 		return null;
 	}
 	
 	public void onBlur(ComponentEvent ce) {
 		super.onBlur(ce);
 		if (this.value == null) {
 			this.value = this.originalValue;
 			if (this.value == null)
 				setRawValue("");
 			else
 				setRawValue(this.value.get(this.getDisplayField()).toString());
 		}
 	}
 	
 	@Override
 	public void onSelect(BeanModel model, int index) {
 		if (model.getBean() != null) {
 			ProxyInstance proxy = (ProxyInstance) model.getBean();
 			if (proxy.isAddNew()) {
 				openCreateProxyDialog();
 				lastQuery = null;
 			}
 		}
 		
 		super.onSelect(model, index);
 	}
 	
 	protected void openCreateProxyDialog() {
 		new CreateProxyDialog(createDialogContainer, this).show();	
 	}
 
 	@Override
 	public void onCreateProxySave(ProxyInstance instance) {
 		//	Add the model to the field store and select it
 		BeanModel model = ProxyInstance.obtainModel(instance);
 		this.getStore().add(model);
 //		this.select(model);
 		this.setValue(model);
 //		this.setRawValue(instance.getDescriptionAndCode());
 		lastQuery = null;		//	So next trigger click reloads/sorts from database
 	}
 	
 	@Override
 	public void lockTrigger() {
 		this.disable();
 	}
 	
 	public void unlockTrigger() {
 		this.enable();
 	}
 	
 	protected String getResultTemplate() {
 		return "<div class=\"{listStyle}\"><b>{descriptionAndId}</div>";
 		//	This isn't working... it's dying on institution.institutionName and institution.htmlAddress (for today, it is okay to say GXT sucks).
 		//	return "<b>{linkIdCheckDigit} {institution.institutionName}</b><br/>  <span style=\"color:gray\">{institution.htmlAddress}</span>"; // {address1}<br/>{city}, {state} &nbsp;&nbsp;&nbsp; {zip}";
 	}
 	
 	/**
 	 * Construct and return a loader to handle returning a list of proxys.
 	 * @return
 	 */
 	protected PagingLoader<PagingLoadResult<ProxyInstance>> getProxyLoader() {
 		// proxy and reader  
 		RpcProxy<PagingLoadResult<ProxyInstance>> proxy = new RpcProxy<PagingLoadResult<ProxyInstance>>() {  
 			@Override  
 			public void load(Object loadConfig, final AsyncCallback<PagingLoadResult<ProxyInstance>> callback) {
 		    	
 				// This could be as simple as calling userListService.getUsers and passing the callback
 				// Instead, here the callback is overridden so that it can catch errors and alert the users.  Then the original callback is told of the failure.
 				// On success, the original callback is just passed the onSuccess message, and the response (the list).
 				
 				AsyncCallback<SynchronizedPagingLoadResult<ProxyInstance>> myCallback = new AsyncCallback<SynchronizedPagingLoadResult<ProxyInstance>>() {
 					public void onFailure(Throwable caught) {
 						// Show the RPC error message to the user
 						if (caught instanceof IllegalArgumentException)
 							MessageBox.alert("Alert", caught.getMessage(), null);
 						else if (caught instanceof ServiceNotReadyException)
 								MessageBox.alert("Alert", "The " + caught.getMessage() + " is not available at this time.  Please try again in a few minutes.", null);
 						else {
 							MessageBox.alert("Alert", "Proxy search failed unexpectedly.", null);
 							System.out.println(caught.getClass().getName());
 							System.out.println(caught.getMessage());
 						}
 						callback.onFailure(caught);
 					}
 
 					public void onSuccess(SynchronizedPagingLoadResult<ProxyInstance> syncResult) {
 						if(syncResult.getSyncId() != searchSyncId)
 							return;
 						
 						PagingLoadResult<ProxyInstance> result = syncResult.getResult();
 						if (includeAddOption) {
 							if (addInstance == null) {
 								addInstance= ProxyInstance.getAddNewInstance();
 							}
 							result.getData().add(0, addInstance);
 							result.setTotalLength(result.getTotalLength() + 1);
 						}
 						if (includeNoneOption) {
 							if (noneInstance == null) {
 								noneInstance= ProxyInstance.getNoneInstance();
 							}
 							result.getData().add(0, noneInstance);
 							result.setTotalLength(result.getTotalLength() + 1);
 						}
 
 						callback.onSuccess(result);
 					}
 				};
 				
 				( (PagingLoadConfig) loadConfig).set("sortField",	sortField);
 				( (PagingLoadConfig) loadConfig).set("sortDir",		sortDir);
 				
 				searchSyncId = System.currentTimeMillis();
 				proxySearchService.searchProxies((PagingLoadConfig) loadConfig, getQueryValue(loadConfig), searchSyncId, myCallback);
 				
 		    }  
 		};
 		BeanModelReader reader = new BeanModelReader();
 		
 		// loader and store  
 		PagingLoader<PagingLoadResult<ProxyInstance>> loader = new BasePagingLoader<PagingLoadResult<ProxyInstance>>(proxy, reader);
 		return loader;
 	}
 	
 	public String getQueryValue(Object loadConfig) {
 		String query = (loadConfig != null && loadConfig instanceof PagingLoadConfig) ? ((PagingLoadConfig) loadConfig).get("query").toString() : null;
 		if (query == null)
 			query = getRawValue();
 		return query;
 	}
 
 	public boolean isIncludeAddOption() {
 		return includeAddOption;
 	}
 
 	public void setIncludeAddOption(boolean includeAddOption) {
 		this.includeAddOption = includeAddOption;
 	}
 
 	public String getSortField() {
 		return sortField;
 	}
 
 	public void setSortField(String sortField) {
 		this.sortField = sortField;
 	}
 
 	public SortDir getSortDir() {
 		return sortDir;
 	}
 
 	public void setSortDir(SortDir sortDir) {
 		this.sortDir = sortDir;
 	}
 
 	public LayoutContainer getCreateDialogContainer() {
 		return createDialogContainer;
 	}
 
 	public void setCreateDialogContainer(LayoutContainer createDialogContainer) {
 		this.createDialogContainer = createDialogContainer;
 	}
 }
