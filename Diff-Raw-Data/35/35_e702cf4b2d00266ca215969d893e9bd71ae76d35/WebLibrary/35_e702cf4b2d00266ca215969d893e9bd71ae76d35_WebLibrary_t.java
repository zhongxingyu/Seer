 package edu.weblibrary.client;
 
 import java.util.Date;
 
 import com.google.gwt.core.client.EntryPoint;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.logical.shared.ValueChangeEvent;
 import com.google.gwt.event.logical.shared.ValueChangeHandler;
 import com.smartgwt.client.data.DSCallback;
 import com.smartgwt.client.data.DSRequest;
 import com.smartgwt.client.data.DSResponse;
 import com.smartgwt.client.rpc.RPCResponse;
 import com.smartgwt.client.types.Side;
 import com.smartgwt.client.util.DateDisplayFormatter;
 import com.smartgwt.client.util.DateInputFormatter;
 import com.smartgwt.client.util.DateUtil;
 import com.smartgwt.client.util.SC;
 import com.smartgwt.client.widgets.IButton;
 import com.smartgwt.client.widgets.events.ClickEvent;
 import com.smartgwt.client.widgets.events.ClickHandler;
 import com.smartgwt.client.widgets.grid.ListGrid;
 import com.smartgwt.client.widgets.layout.HLayout;
 import com.smartgwt.client.widgets.layout.VLayout;
 import com.smartgwt.client.widgets.tab.Tab;
 import com.smartgwt.client.widgets.tab.TabSet;
 import com.google.gwt.i18n.client.DateTimeFormat;
 import com.google.gwt.user.client.ui.CheckBox;
 
 import edu.weblibrary.shared.ResponseStatusEx;
  
 public class WebLibrary implements EntryPoint { 
 	public void onModuleLoad()	{
		//SC.showConsole();
 		DateUtil.setShortDateDisplayFormatter(new DateDisplayFormatter() {			
 			public String format(Date date) {
 				if(date == null) return null;
 				
 				DateTimeFormat dateFormatter = DateTimeFormat.getFormat("dd.MM.yyyy");
 				String format = dateFormatter.format(date);
 				return format;
 			}
 		});
 		
 		DateUtil.setDateInputFormatter(new DateInputFormatter() {
 			public Date parse(String dateString) {
 			       final DateTimeFormat dateFormatter = DateTimeFormat.getFormat("dd.MM.yyyy");
 			       Date date = dateFormatter.parse(dateString);
 			       return date;
 			}
 		});
 		
 		final WindowAdd windowAdd = new WindowAdd(new FormAuthor());
 		windowAdd.setWidth(300);
 		windowAdd.setHeight(200);
 		
 		final WindowUpdate authorUpdateWindow = new WindowUpdate(new FormAuthor());
 		authorUpdateWindow.setWidth(300);
 		authorUpdateWindow.setHeight(200);
 		
 		final ListGrid authorGrid = new AuthorGrid();
 		final ListGrid bookGrid = new BookGrid();
 		
 		authorGrid.setWidth(500);
 		authorGrid.setHeight100();
 		
 		IButton authorAddButton = new IButton("Добавить");
 		authorAddButton.setIcon("[SKIN]/actions/plus.png");
 		authorAddButton.addClickHandler(new ClickHandler() {			
 			public void onClick(ClickEvent event) {
 				windowAdd.addNewRecord();
 			}
 		});
 		
 		final IButton authorRemoveButton = new IButton("Удалить");
 		authorRemoveButton.setIcon("[SKIN]/actions/remove.png");
 		authorRemoveButton.addClickHandler(new ClickHandler() {			
 			public void onClick(ClickEvent event) {
 				DSCallback callback = new DSCallback() {
 					public void execute(DSResponse response, Object rawData, DSRequest request) {
 						GWT.log("Remove status: " + String.valueOf(response.getStatus()));
 						if (response.getStatus() == ResponseStatusEx.STATUS_DELETE_DENY_ERROR) {
 							SC.say("Ошибка", "Невозможно удалить автора, пока в библиотеке есть "+
 									"хотя бы одна его книга.");																
 						} else	if(response.getStatus() != RPCResponse.STATUS_SUCCESS) {
 							SC.say("Ошибка", "Не удалось удалить запись");
 						}											
 					}
 				};
 				
 				DSRequest request = new DSRequest();
 				request.setWillHandleError(true);
 				
 				authorGrid.removeSelectedData(callback, request);		
 			}
 		});	
 		
 		final IButton authorUpdateButton = new IButton("Изменить данные");
 		authorUpdateButton.setIcon("[SKIN]/actions/edit.png");
 		authorUpdateButton.setAutoFit(true);
 		authorUpdateButton.addClickHandler(new ClickHandler() {
 			public void onClick(ClickEvent event) {
 				if (authorGrid.getSelectedRecord() != null)	{
 					authorUpdateWindow.editSelectedData(authorGrid);
 					authorUpdateWindow.show();
 				}
 			}
 		});
 		
 		CheckBox showFilterCheckBox = new CheckBox("фильтр");
 		showFilterCheckBox.setWordWrap(false);
 		
 		showFilterCheckBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
 			public void onValueChange(ValueChangeEvent<Boolean> event) {
 				authorGrid.setShowFilterEditor(event.getValue());				
 			}
 		});
         
 		VLayout authorLayout = new VLayout();
 		
 		HLayout authorTopLayout = new HLayout();
 		authorTopLayout.setMargin(3);
 		authorTopLayout.setMembersMargin(5);
 		
 		authorLayout.addMember(authorTopLayout);
 		authorLayout.addMember(authorGrid);
 		
 		authorTopLayout.addMember(authorAddButton);
 		authorTopLayout.addMember(authorUpdateButton);
 		authorTopLayout.addMember(authorRemoveButton);
 		authorTopLayout.addMember(showFilterCheckBox);
 		
 		final WindowAdd bookAddWindow = new WindowAdd(new FormBook());
 		bookAddWindow.setWidth(330);
 		bookAddWindow.setHeight(400);
 		final WindowUpdate bookUpdateWindow = new WindowUpdate(new FormBook());
 		bookUpdateWindow.setWidth(330);
 		bookUpdateWindow.setHeight(400);
 		final WindowReport bookReportWindow = new WindowReport();
 		bookReportWindow.setWidth(680);
 		bookReportWindow.setHeight(600);	
 		
 		bookGrid.setWidth100();
 		bookGrid.setHeight100();
 
 		
 		IButton bookAddButton = new IButton("Добавить книгу", new ClickHandler() {			
 			public void onClick(ClickEvent event) {
 				bookAddWindow.addNewRecord();
 			}
 		});
 		bookAddButton.setIcon("[SKIN]/actions/plus.png");
 		bookAddButton.setAutoFit(true);
 		
 		IButton bookUpdateButton = new IButton("Изменить данные", new ClickHandler() {
 			public void onClick(ClickEvent event) {				
 				if (bookGrid.getSelectedRecord() != null)	{
 					bookUpdateWindow.editSelectedData(bookGrid);
 					bookUpdateWindow.show();
 				}				
 			}
 		});
 		bookUpdateButton.setIcon("[SKIN]/actions/edit.png");
 		bookUpdateButton.setAutoFit(true);
 		
 		IButton bookReportButton = new IButton("Отчет", new ClickHandler() {			
 			public void onClick(ClickEvent event) {
 				if (bookGrid.getSelectedRecord() != null) {
 					bookReportWindow.setBookId(bookGrid.getSelectedRecord().getAttributeAsInt("id"));
 					bookReportWindow.show();	
 				}
 			}
 		});
 		
 		IButton bookRemoveButton = new IButton("Удалить", new ClickHandler() {
 			public void onClick(ClickEvent event) {			
 					DSCallback callback = new DSCallback() {
 						public void execute(DSResponse response, Object rawData, DSRequest request) {
 							GWT.log("Remove status: " + String.valueOf(response.getStatus()));
 							if (response.getStatus() == ResponseStatusEx.STATUS_DELETE_DENY_ERROR) {
 								SC.say("Ошибка", "DELETE_DENY_ERROR");																
 							} else	if(response.getStatus() != RPCResponse.STATUS_SUCCESS) {
 								SC.say("Ошибка", "Не удалось удалить запись");
 							}
 						}
 					};
 					
 					DSRequest request = new DSRequest();
 					request.setWillHandleError(true);					
 					bookGrid.removeSelectedData(callback, request);		
 				}
 		});
 		bookRemoveButton.setIcon("[SKIN]/actions/remove.png");
 		
 		VLayout bookLayout = new VLayout();
 		HLayout bookTopLayout = new HLayout();
 		
 		bookTopLayout.addMember(bookAddButton);
 		bookTopLayout.addMember(bookUpdateButton);
 		bookTopLayout.addMember(bookRemoveButton);
 		bookTopLayout.addMember(bookReportButton);
 		bookTopLayout.setMargin(3);
 		bookTopLayout.setMembersMargin(5);
 		
 		bookLayout.addMember(bookTopLayout);
 		bookLayout.addMember(bookGrid);
 		
 		final TabSet tabSet = new TabSet();
 		tabSet.setTabBarPosition(Side.TOP);
 		tabSet.setWidth100();
 		tabSet.setHeight100();		
 		Tab authorTab = new Tab("Авторы");
 		Tab bookTab = new Tab("Книги");	
 		tabSet.addTab(authorTab);
 		tabSet.addTab(bookTab);
 
 		authorTab.setPane(authorLayout);	
 		bookTab.setPane(bookLayout);
 		
 		tabSet.show();		
 	}
 }
