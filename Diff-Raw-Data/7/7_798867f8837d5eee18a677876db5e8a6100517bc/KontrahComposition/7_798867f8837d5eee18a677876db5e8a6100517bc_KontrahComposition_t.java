 package com.example.helloworld;
 
 import java.sql.SQLException;
 
 import org.tepi.filtertable.FilterTable;
 import com.vaadin.data.Item;
 import com.vaadin.data.Property;
 import com.vaadin.data.Property.ValueChangeEvent;
 import com.vaadin.event.ShortcutAction.KeyCode;
 import com.vaadin.event.ShortcutAction.ModifierKey;
 import com.vaadin.event.ShortcutListener;
 import com.vaadin.ui.Button;
 import com.vaadin.ui.Button.ClickEvent;
 import com.vaadin.ui.CustomComponent;
 import com.vaadin.ui.CustomTable;
 import com.vaadin.ui.CustomTable.ColumnGenerator;
 import com.vaadin.ui.HorizontalLayout;
 import com.vaadin.ui.Label;
 import com.vaadin.ui.NativeButton;
 import com.vaadin.ui.VerticalLayout;
 import com.vaadin.ui.Window.CloseEvent;
 import com.vaadin.ui.Window.CloseListener;
 import com.vaadin.ui.Window.Notification;
 
 public class KontrahComposition extends CustomComponent {
 	public final VerticalLayout kontrahContainer = new VerticalLayout();
 	public final FilterTable KontrahTable = new FilterTable();
 	public final NativeButton btnShowComplexForm = new NativeButton();
 	public final NativeButton btnRemove = new NativeButton();
 	public final NativeButton btnAdd = new NativeButton();
 	public final NativeButton btnShowNormalForm = new NativeButton();
 	public final ComplexForm complexForm = new ComplexForm();
 	
 	public KontrahComposition() {
 		setSizeFull();
 		
 		kontrahContainer.setSizeFull();
 		kontrahContainer.setSpacing(false);
 		kontrahContainer.setMargin(false);
 		
 		setupKontrahTable();
 		setupButtons();
 		
 		HorizontalLayout buttonsContainer = new HorizontalLayout();
 		buttonsContainer.addComponent(btnShowComplexForm);
 		buttonsContainer.addComponent(btnRemove);
 		buttonsContainer.addComponent(btnAdd);
 		buttonsContainer.addComponent(btnShowNormalForm);
 		
 		kontrahContainer.addComponent(KontrahTable);
 		kontrahContainer.setExpandRatio(KontrahTable, 1);
 		kontrahContainer.addComponent(buttonsContainer);
 		
 		setCompositionRoot(kontrahContainer);
 	}
 
 	public void setupKontrahTable() {
 		AppData.getContextHelp().addHelpForComponent(KontrahTable, "Shortcuts:</br>Add: <b>INS</b> Remove: <b>DELETE</b> Edit: <b>CTRL+ENTER</b>");
 		
 		KontrahTable.setSizeFull();
 		KontrahTable.setFilterGenerator(new KontrahFilterGenerator());
 		KontrahTable.setFilterDecorator(new KontrahFilterDecorator());
 		KontrahTable.setFilterBarVisible(true);
 		KontrahTable.setColumnCollapsingAllowed(true);
 		KontrahTable.setColumnReorderingAllowed(true);
 		KontrahTable.setContainerDataSource(AppData.getDataSource().getKontrahContainer());
 		KontrahTable.setSelectable(true);
 		KontrahTable.setImmediate(true);
 		
 		KontrahTable.addGeneratedColumn("ID_FIRMA", new ColumnGenerator() {
 
 			@Override
 			public Object generateCell(CustomTable source, Object itemId,
 					Object columnId) {
 				if (source.getItem(itemId).getItemProperty("ID_FIRMA").getValue() != null) {
 					Label l = new Label();
 					int firmaId = (Integer)source.getItem(itemId).getItemProperty("ID_FIRMA").getValue();
 					l.setValue(AppData.getDataSource().getFirmaNazwa(firmaId));
 					l.setSizeUndefined();
 					return l;
 				}
 				return null;
 			}
 			
 		});
 		
 		//KontrahTable.setFilterFieldVisible("ID_FIRMA", false);
 
 		KontrahTable.addListener(new Property.ValueChangeListener() {
 			
 			@Override
 			public void valueChange(ValueChangeEvent event) {
 				try {
 					AppData.getDataSource().getKontrahContainer().rollback();
 				} catch (UnsupportedOperationException e) {
 					e.printStackTrace();
 				} catch (SQLException e) {
 					e.printStackTrace();
 				}
 			}
 		});
 		
 		KontrahTable.addShortcutListener(new ShortcutListener("Edit kontrah", KeyCode.ENTER, new int[] {ModifierKey.CTRL}) {
 			
 			@Override
 			public void handleAction(Object sender, Object target) {
 				btnShowComplexForm.click();				
 			}
 		});
 		
 		KontrahTable.addShortcutListener(new ShortcutListener("Remove kontrah", KeyCode.DELETE, new int[0]) {
 			
 			@Override
 			public void handleAction(Object sender, Object target) {
 				btnRemove.click();	
 			}
 		});
 		
 		KontrahTable.addShortcutListener(new ShortcutListener("Insert kontrah", KeyCode.INSERT, new int[0]) {
 			
 			@Override
 			public void handleAction(Object sender, Object target) {
 				btnAdd.click();	
 			}
 		});
 	}
 	
 	public void setupButtons() {
 		btnShowComplexForm.setCaption("Edit");
 		btnRemove.setCaption("Remove");
 		btnAdd.setCaption("Add");
 		btnShowNormalForm.setCaption("Edit (normal form)");
 		
 		complexForm.addListener(new CloseListener() {
 
 			@Override
 			public void windowClose(CloseEvent e) {
 				AppData.getContextHelp().hideHelp();
 				KontrahTable.focus();
 				complexForm.removeIconsTabs();
				complexForm.tabs.setSelectedTab(0);
 			}
 		});
 				
 		btnShowComplexForm.addListener(new Button.ClickListener() {
 			@Override
 			public void buttonClick(ClickEvent event) {
 				Object id = KontrahTable.getValue();
 				if (id != null) {					
 					try {
 						AppData.getDataSource().getKontrahContainer().rollback();
 					} catch (UnsupportedOperationException e) {
 						e.printStackTrace();
 					} catch (SQLException e) {
 						e.printStackTrace();
 					}
 					
 					Item selectedKontrah = AppData.getDataSource().getKontrahContainer().getItem(id);
 					complexForm.form.setItemDataSource(selectedKontrah);
 					
 					AppData.getAppWindow().addWindow(complexForm);
 					complexForm.focus();
 				} else {
 					getWindow().showNotification("Notification", "Please select customer from table", Notification.TYPE_TRAY_NOTIFICATION);
 				}
 			}
 		});
 		
 		btnRemove.addListener(new Button.ClickListener() {
 			
 			@Override
 			public void buttonClick(ClickEvent event) {
 				Object id = KontrahTable.getValue();
 				if (id != null) {
 					KontrahTable.removeItem(id);
 					AppData.getDataSource().getKontrahContainer().removeItem(id);
 					try {
 						AppData.getDataSource().getKontrahContainer().commit();
 					} catch (UnsupportedOperationException e) {
 						e.printStackTrace();
 					} catch (SQLException e) {
 						e.printStackTrace();
 					}
 					KontrahTable.select(null);
 				} else { 
 					getWindow().showNotification("Notification", "Please select customer from table", Notification.TYPE_TRAY_NOTIFICATION);
 				}
 			}
 		});
 		
 		btnAdd.addListener(new Button.ClickListener() {
 			
 			@Override
 			public void buttonClick(ClickEvent event) {				
 				try {
 					AppData.getDataSource().getKontrahContainer().commit();
 				} catch (SQLException ignored) {
 				}
 				
 				Object tempItemId = AppData.getDataSource().getKontrahContainer().addItem();
 				complexForm.form.setItemDataSource(AppData.getDataSource().getKontrahContainer().getItem(tempItemId));
 				AppData.getAppWindow().addWindow(complexForm);
 				complexForm.focus();
 			}
 		});
 		
 		btnShowNormalForm.addListener(new Button.ClickListener() {
 			
 			@Override
 			public void buttonClick(ClickEvent event) {
 				Object id = KontrahTable.getValue();
 				if (id != null) {					
 					try {
 						AppData.getDataSource().getKontrahContainer().rollback();
 					} catch (UnsupportedOperationException e) {
 						e.printStackTrace();
 					} catch (SQLException e) {
 						e.printStackTrace();
 					}
 					
 					Item selectedKontrah = AppData.getDataSource().getKontrahContainer().getItem(id);
 					
 					NormalForm normalForm = new NormalForm();
 					normalForm.form.setItemDataSource(selectedKontrah);
 					
 					AppData.getAppWindow().addWindow(normalForm);
 					normalForm.focus();
 				} else {
 					getWindow().showNotification("Notification", "Please select customer from table", Notification.TYPE_TRAY_NOTIFICATION);
 				}
 			}
 		});
 	}
 }
