 package com.example.helloworld;
 
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.List;
 
 import com.vaadin.data.Item;
 import com.vaadin.data.util.sqlcontainer.RowId;
 import com.vaadin.event.FieldEvents;
 import com.vaadin.event.FieldEvents.FocusEvent;
 import com.vaadin.event.ShortcutAction.KeyCode;
 import com.vaadin.terminal.ThemeResource;
 import com.vaadin.ui.Button;
 import com.vaadin.ui.Button.ClickEvent;
 import com.vaadin.ui.Component;
 import com.vaadin.ui.Field;
 import com.vaadin.ui.Form;
 import com.vaadin.ui.FormLayout;
 import com.vaadin.ui.HorizontalLayout;
 import com.vaadin.ui.Panel;
 import com.vaadin.ui.TabSheet;
 import com.vaadin.ui.TabSheet.Tab;
 import com.vaadin.ui.Window;
 import com.vaadin.ui.themes.Reindeer;
 
 public class ComplexForm extends Window {
 	public Form form = new Form();
 	public Button btnSave = new Button();
 	public Button btnCancel = new Button();
 	public KontrahFieldFactory kontrahFieldFactory = new KontrahFieldFactory();
 	public TabSheet tabs = new TabSheet();
 	private Window self = this;
 	private static final ThemeResource errorIcon = new ThemeResource("icons/error.png");
 	
 	public ComplexForm() {		
 		AppData.getContextHelp().addHelpForComponent(this, "Shortcuts:</br>close: <b>ESC</b></br>save: <b>ENTER</b>");
 		Panel p = new Panel();
 		
         tabs.setSizeFull();
         tabs.setStyleName(Reindeer.TABSHEET_SMALL);        
         final FormLayout personalInfo = new FormLayout();
         tabs.addTab(personalInfo, "Personal data");
         final FormLayout profile = new FormLayout();
         tabs.addTab(profile, "Company");
         final FormLayout other = new FormLayout();
         tabs.addTab(other, "Other");
         
         this.addListener(new FieldEvents.FocusListener() {
 
 			@Override
 			public void focus(FocusEvent event) {
 				AppData.getContextHelp().showHelpFor(self);
 				form.getField("IMIE").focus();
 			}
         });
                 
         form = new Form() {
             @Override
             protected void attachField(Object propertyId, Field field) {
                 if ("IMIE".equals(propertyId) || "NAZWISKO".equals(propertyId)) {
                     personalInfo.addComponent(field);
                 } else 
                 if ("ID_FIRMA".equals(propertyId)) {
                 	profile.addComponent(field);
                 } else
                 if ("WIEK".equals(propertyId) || "PESEL".equals(propertyId)) {
                 	other.addComponent(field);
                 }
             }
             
         	@Override
         	public void setItemDataSource(Item newDataSource) {
         		if (newDataSource != null) {
         			super.setItemDataSource(newDataSource);
         			if (newDataSource.getItemProperty("ID_FIRMA").getValue() != null) {
         				kontrahFieldFactory.cbFirmy.select(new RowId(new Object[] { newDataSource.getItemProperty("ID_FIRMA").getValue() }));
         			}
         		} else {
         			super.setItemDataSource(null);
         		}
         	}
         };
         
         btnSave.setCaption("Save");
         btnSave.setClickShortcut(KeyCode.ENTER, null);
         btnSave.setStyleName(Reindeer.BUTTON_SMALL);
         btnSave.addListener(new Button.ClickListener() {
 			
 			@Override
 			public void buttonClick(ClickEvent event) {				
 				try {
 					Field fieldWiek = form.getField("WIEK");
 					Object wiekValue = fieldWiek.getValue();
 					Field fieldImie = form.getField("IMIE");
 					Object imieValue = fieldImie.getValue();
 					
 					if ((imieValue == null || imieValue.equals("")) && (wiekValue == null || wiekValue.equals(""))) {
 						 getWindow().showNotification(
 	                             (String) "BLAD",
 	                             (String) "Imie and Wiek can not be empty at the same time",
 	                             Notification.TYPE_ERROR_MESSAGE);
 					} else {
 						form.commit();
 						try {
 							AppData.getDataSource().getKontrahContainer().commit();
 						} catch (UnsupportedOperationException e) {
 							e.printStackTrace();
 						} catch (SQLException e) {
 							e.printStackTrace();
 						}
 						((Window) getWindow().getParent()).removeWindow(getWindow());
 					}
 				} catch (Exception e) {
 					// Remove icon from all tabs
 					removeIconsTabs();
 					// Get tab where is invalid field set error icon for that tab and make it selected
 					List<Field> fields = getAllFields(form);
 					for (Field f : fields) {
 						
 						boolean isInvalid = false;
 						if (f.getValidators() != null) {
 							Iterator<com.vaadin.data.Validator> i = f.getValidators().iterator();
 							while (i.hasNext()) {
 								com.vaadin.data.validator.AbstractValidator v = (com.vaadin.data.validator.AbstractValidator)i.next();
 								if (v.getErrorMessage().equals(e.getMessage())) {
 									isInvalid = true;
 									break;
 								}
 							}
 						}
 						
 						if ((isInvalid || f.getRequiredError().equals(e.getMessage())) && !f.isValid()) {
 							FormLayout invalidTab = (FormLayout)f.getParent();
 							tabs.getTab(invalidTab).setIcon(errorIcon);
 							tabs.setSelectedTab(invalidTab);
							f.focus();
 							break;
 						}
 					}
 					//getWindow().showNotification("Save error", e.getMessage(), Notification.TYPE_ERROR_MESSAGE);
 				}
 			}
 		});
         
 		btnCancel.setCaption("Cancel");
 		btnCancel.setClickShortcut(KeyCode.ESCAPE, null);
 		btnCancel.setStyleName(Reindeer.BUTTON_SMALL);
 		btnCancel.addListener(new Button.ClickListener() {
 			
 			@Override
 			public void buttonClick(ClickEvent event) {
 				form.discard();
 				try {
 					AppData.getDataSource().getKontrahContainer().rollback();
 				} catch (UnsupportedOperationException e) {
 					e.printStackTrace();
 				} catch (SQLException e) {
 					e.printStackTrace();
 				}
 				((Window) getWindow().getParent()).removeWindow(getWindow());
 			}
 		});
 		
         form.setWriteThrough(false);
         form.setInvalidCommitted(false);
         form.setFormFieldFactory(kontrahFieldFactory);
 
         p.getContent().setSizeFull();
         p.setContent(tabs);
         this.addComponent(p);
         this.addComponent(form);
 		
 		HorizontalLayout footer = new HorizontalLayout();
 		footer.setSpacing(true);
 		footer.addComponent(btnCancel);
 		footer.addComponent(btnSave);
 		this.addComponent(footer);
         
         this.setWidth("350px");
         this.setResizable(false);
         this.center();
         this.setCaption("Customer edit");
         this.setClosable(false);
         this.setModal(true);
 	}
 	
 	public List<Field> getAllFields(Form form) {
 		  Collection<?> propertyIds = form.getItemPropertyIds();
 		  List<Field> fields = new ArrayList<Field>(propertyIds.size());
 		  for (Object itemPropertyId : propertyIds) {
 		    fields.add(form.getField(itemPropertyId));
 		  }
 		  return fields;
 	}
 	
 	public void removeIconsTabs() {
 		Iterator<Component> i = tabs.getComponentIterator();
 		while (i.hasNext()) {
 		    Component c = (Component) i.next();
 		    Tab tab = tabs.getTab(c);
 		    if (tab.getIcon() != null) {
 		    	tab.setIcon(null);
 		    }
 		}
 	}
 }
