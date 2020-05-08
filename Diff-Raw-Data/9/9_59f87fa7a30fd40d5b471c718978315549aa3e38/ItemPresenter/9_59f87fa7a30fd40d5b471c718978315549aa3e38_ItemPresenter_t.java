 package com.tda.presentation.client.presenter;
 
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.event.dom.client.HasClickHandlers;
 import com.google.gwt.event.logical.shared.ValueChangeEvent;
 import com.google.gwt.event.logical.shared.ValueChangeHandler;
 import com.google.gwt.event.shared.HandlerManager;
 import com.google.gwt.user.client.History;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.HasWidgets;
 import com.google.gwt.user.client.ui.Panel;
 import com.google.gwt.user.client.ui.Widget;
 import com.smartgwt.client.data.Record;
 import com.smartgwt.client.util.BooleanCallback;
 import com.smartgwt.client.util.SC;
 import com.smartgwt.client.widgets.Canvas;
 import com.smartgwt.client.widgets.form.DynamicForm;
 import com.smartgwt.client.widgets.grid.ListGrid;
 import com.tda.model.item.Item;
 import com.tda.presentation.client.service.ItemServiceGWTWrapperAsync;
 
 public class ItemPresenter implements Presenter, ValueChangeHandler<String> {
 
 	public interface Display {
 		HasClickHandlers getAddButton();
 		HasClickHandlers getEditButton();
 		HasClickHandlers getDeleteButton();
 		HasClickHandlers getSubmitButton();
 		ListGrid getGrid();
 		Record getClickedRow();
 		Record[] getSelectedRows();
 		DynamicForm getForm();
 		Widget asWidget();
 		Panel getListContainer();
 		Panel getFormContainer();
		Panel getParentContainer();
 	}
 
 
 	private final Display display;
 
 	public Display getDisplay() {
 		return display;
 	}
 
 	private final ItemServiceGWTWrapperAsync rpc;
 	private final HandlerManager eventBus;
 	private Status status;
 	
 	public ItemPresenter(ItemServiceGWTWrapperAsync rpc, HandlerManager eventBus, Display view) {
 		this.display = view;
 		this.rpc = rpc;
 		this.eventBus = eventBus;
 		this.status = Status.LIST;
 		History.addValueChangeHandler(this);
 	}
 
 	public void go(HasWidgets container) {
 		bind();
 		container.clear();
 		container.add(display.asWidget());
 		display.getGrid().fetchData();
 	}
 
 	public void go(Canvas canvas) {
 		canvas.clear();
 		canvas.addChild(display.asWidget());
 		display.getGrid().fetchData();
 	}
 
 	private void showForm() {
 		display.getListContainer().setVisible(false);
 		display.getFormContainer().setVisible(true);
 	}
 
 	private void showList() {
 		display.getFormContainer().setVisible(false);
 		display.getListContainer().setVisible(true);
 		display.getGrid().fetchData();
 		display.getForm().clearValues();
 	}
 
 	private void bind() {
 		
 		display.getAddButton().addClickHandler(new ClickHandler() {
 			public void onClick(ClickEvent event) {
 				status = Status.ADD;
 				History.newItem("addForm");
 			}
 		});
 		
 		display.getEditButton().addClickHandler(new ClickHandler() {
 			public void onClick(ClickEvent event) {
 
 				/* No item selected */
 				if ( display.getClickedRow() == null )
 					return;
 
 				loadFormWithRecord(display.getClickedRow());
 				status = Status.EDIT;
 				History.newItem("editForm");
 			}
 
 
 		});
 
 		display.getSubmitButton().addClickHandler(new ClickHandler() {
 			public void onClick(ClickEvent event) {
 
 				if ( !display.getForm().validate() ) {
 					return;
 				}
 
 				switch (status) {
 				case ADD:
 					addItem();
 					break;
 
 				case EDIT:
 					editItem();
 					break;
 
 				default:
 					break;
 				}
 			}
 		});
 
 		display.getDeleteButton().addClickHandler(new ClickHandler() {
 			public void onClick(ClickEvent event) {
 
 				/* No item selected */
 				if ( display.getClickedRow() == null )
 					return;
 
 				final long id = Long.valueOf(display.getClickedRow().getAttribute("id"));
 
 				SC.ask("Desea borrar el item?", new BooleanCallback() {
 					public void execute(Boolean value) {
 						if (value) {
 							removeItem(id);
 						}
 					}
 				});
 			}
 		});
 	}
 
 	private void editItem() {
 		Item item = getItemFromForm();
 		rpc.update(item, new AsyncCallback<Void>() {
 
 			public void onFailure(Throwable caught) {
 				SC.say("Item no pudo ser editado. " + caught.getMessage());
 			}
 
 			public void onSuccess(Void result) {
 				History.newItem("itemsList");
 				SC.say("Item editado.");
 			}
 			
 		});
 	}
 
 	private void addItem() {
 		Item item = getItemFromForm();
 		rpc.save(item, new AsyncCallback<Void>() {
 
 			public void onFailure(Throwable caught) {
 				SC.say("Item no pudo ser agregado. " + caught.getMessage());
 				
 			}
 
 			public void onSuccess(Void result) {
 				History.newItem("itemsList");
 				SC.say("Item agregado.");
 			}
 		});
 		
 	}
 
 	private void loadFormWithRecord(Record record) {
 		DynamicForm form = display.getForm();
 		form.setValue("name", record.getAttribute("name"));
 		form.setValue("id", record.getAttribute("id"));
 	}
 
 	private Item getItemFromForm() {
 		Item item = new Item();
 
 		DynamicForm form = display.getForm();
 		item.setName(form.getValueAsString("name"));
 		String idString = form.getValueAsString("id");
 		if ( idString == null ) {
 			idString = "1";
 		}
 
 		item.setId(Long.valueOf(idString));
 
 		return item;
 	}
 
 	/*
 	 * TODO: MEJORAR!!!!!!!!!
 	 */
 	private void removeItem(long id){
 		/*
 		 * First got to fetch item(id)
 		 */
 		rpc.findById(id, new AsyncCallback<Item>() {
 			public void onFailure(Throwable arg0) {
 				SC.say("Error al retribuir el item");
 			}
 
 			public void onSuccess(Item item) {
 				rpc.delete(item, new AsyncCallback<Void>(){
 
 					public void onFailure(Throwable arg0) {
 						SC.say("Error al eliminar el item");
 					}
 
 					public void onSuccess(Void arg0) {
 						SC.say("Item removido");
 						/* TODO: This is not updating data */
 						display.getGrid().fetchData();
 						display.getGrid().redraw();
 					}
 					
 				});
 			}
 		});
 	}
 
 	public void onDestroy() {
 
 	}
 
 	public void onValueChange(ValueChangeEvent<String> event) {
 		String token = event.getValue();
 
 		if (token != null) {
 			if (token.equals("itemsList")) {
 				showList();
 			} else if ( token.equals("addForm") ) {
 				showForm();
 			} else if ( token.equals("editForm") ) {
 				showForm();
 			}
 		}
 	}
 }
