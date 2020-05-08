 package org.iucn.sis.client.panels.integrity;
 
 import java.util.ArrayList;
 import java.util.Collection;
 
 import org.iucn.sis.client.api.utils.UriBase;
 import org.iucn.sis.client.container.SimpleSISClient;
 import org.iucn.sis.shared.api.integrity.AssessmentValidationDesigner;
 import org.iucn.sis.shared.api.integrity.ClientAssessmentValidator;
 import org.iucn.sis.shared.api.integrity.HelpWindow;
 import org.iucn.sis.shared.api.integrity.IntegrityRulesetPropertiesEditor;
 import org.iucn.sis.shared.api.integrity.SISQBQuery;
 import org.iucn.sis.shared.api.integrity.SISTableChooserFactory;
 import org.iucn.sis.shared.api.models.AssessmentType;
 
 import com.extjs.gxt.ui.client.Style.LayoutRegion;
 import com.extjs.gxt.ui.client.data.BaseModelData;
 import com.extjs.gxt.ui.client.event.ButtonEvent;
 import com.extjs.gxt.ui.client.event.DataListEvent;
 import com.extjs.gxt.ui.client.event.Events;
 import com.extjs.gxt.ui.client.event.Listener;
 import com.extjs.gxt.ui.client.event.SelectionListener;
 import com.extjs.gxt.ui.client.store.ListStore;
 import com.extjs.gxt.ui.client.widget.DataList;
 import com.extjs.gxt.ui.client.widget.DataListItem;
 import com.extjs.gxt.ui.client.widget.Html;
 import com.extjs.gxt.ui.client.widget.Info;
 import com.extjs.gxt.ui.client.widget.LayoutContainer;
 import com.extjs.gxt.ui.client.widget.Window;
 import com.extjs.gxt.ui.client.widget.button.Button;
 import com.extjs.gxt.ui.client.widget.form.ComboBox;
 import com.extjs.gxt.ui.client.widget.form.FormPanel;
 import com.extjs.gxt.ui.client.widget.form.TextField;
 import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
 import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
 import com.extjs.gxt.ui.client.widget.layout.FillLayout;
 import com.extjs.gxt.ui.client.widget.layout.FitLayout;
 import com.extjs.gxt.ui.client.widget.toolbar.FillToolItem;
 import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
 import com.solertium.lwxml.shared.GWTConflictException;
 import com.solertium.lwxml.shared.GenericCallback;
 import com.solertium.lwxml.shared.NativeDocument;
 import com.solertium.lwxml.shared.NativeElement;
 import com.solertium.lwxml.shared.NativeNode;
 import com.solertium.lwxml.shared.NativeNodeList;
 import com.solertium.util.extjs.client.WindowUtils;
 import com.solertium.util.gwt.ui.DrawsLazily;
 import com.solertium.util.portable.IDValidator;
 import com.solertium.util.querybuilder.gwt.client.chooser.TableChooserCreator;
 import com.solertium.util.querybuilder.query.SelectedField;
 import com.solertium.util.querybuilder.struct.DBStructure;
 
 /**
  * IntegrityApplicationPanel.java
  * 
  * Main panel that holds the view of the container. Create and edit rule sets. A
  * "ruleset" is a glorified QueryBuilder query that doesn't allow you to choose
  * columns, just constraints.
  * 
  * @author <a href="mailto:carl.scott@solertium.com">Carl Scott</a>, <a
  *         href="http://www.solertium.com">Solertium Corporation</a>
  * 
  */
 public class IntegrityApplicationPanel extends LayoutContainer implements DrawsLazily {
 
 	private static final Collection<SelectedField> defaultTables = loadDefaults();
 
 	private static Collection<SelectedField> loadDefaults() {
 		final Collection<SelectedField> fields = new ArrayList<SelectedField>();
 		fields.add(new SelectedField("assessment", "uid"));
 		/*
 		 * TODO: verify that this is correct...
 		 */
 		/*
 		 * fields.add(new SelectedField("assessment_reference", "asm_id"));
 		 * fields.add(new SelectedField("ConservationActions", "asm_id"));
 		 * fields.add(new SelectedField("CountryOccurrence", "asm_id"));
 		 * fields.add(new SelectedField("FAOOccurrence", "asm_id"));
 		 * fields.add(new SelectedField("GeneralHabitats", "asm_id"));
 		 * fields.add(new SelectedField("EcosystemServices", "asm_id"));
 		 * fields.add(new SelectedField("InPlaceEducation", "asm_id"));
 		 * fields.add(new SelectedField("InPlaceLandWaterProtection",
 		 * "asm_id")); fields.add(new SelectedField("InPlaceSpeciesManagement",
 		 * "asm_id")); fields.add(new SelectedField("LargeMarineEcosystems",
 		 * "asm_id")); fields.add(new SelectedField("LandCover", "asm_id"));
 		 * fields.add(new SelectedField("RedListCriteria", "asm_id"));
 		 * fields.add(new SelectedField("RedListCriteria_3_1", "asm_id"));
 		 * fields.add(new SelectedField("RedListCriteria_2_3", "asm_id"));
 		 * fields.add(new SelectedField("RedListReasonsForChange", "asm_id"));
 		 * fields.add(new SelectedField("RegionInformation", "asm_id"));
 		 * fields.add(new SelectedField("Research", "asm_id")); fields.add(new
 		 * SelectedField("Stresses", "asm_id")); fields.add(new
 		 * SelectedField("Threats", "asm_id"));
 		 */
 		return fields;
 	}
 
 	private final LayoutContainer center;
 	private final DataList list;
 
 	private final AssessmentValidationDesigner designer;
 	
 	private boolean isDrawn;
 
 	public IntegrityApplicationPanel() {
 		setLayout(new FillLayout());
 
 		list = new DataList();
 
 		center = new LayoutContainer();
 		center.setLayout(new FillLayout());
 		center.setLayoutOnChange(true);
 
 		designer = new AssessmentValidationDesigner();
 		
 		isDrawn = false;
 	}
 	
 	public boolean isDrawn() {
 		return isDrawn;
 	}
 
 	public void draw(final DoneDrawingCallback callback) {
 		TableChooserCreator.getInstance().register("SIS", new SISTableChooserFactory());
 		DBStructure.getInstance().setChooserType("SIS");
 		DBStructure.getInstance().setURL(UriBase.getInstance().getIntegrityBase() + "/struct");
 		DBStructure.getInstance().load(new GenericCallback<Object>() {
 			public void onSuccess(Object result) {
 				final NativeDocument lookups = SimpleSISClient.getHttpBasicNativeDocument();
 				lookups.get(UriBase.getInstance().getIntegrityBase() + "/lookup", new GenericCallback<String>() {
 					public void onSuccess(String result) {
 						DBStructure.getInstance().loadLookupTables(UriBase.getInstance().getIntegrityBase() + "/lookup", lookups);
 						finish(callback);
 					}
 
 					public void onFailure(Throwable caught) {
 						finish(callback);
 					}
 				});
 			}
 
 			public void onFailure(Throwable caught) {
 				callback.isDrawn();
 			}
 		});
 	}
 
 	private void finish(final DrawsLazily.DoneDrawingCallback callback) {
 		final NativeDocument document = SimpleSISClient.getHttpBasicNativeDocument();
 		document.get(UriBase.getInstance().getIntegrityBase() + "/ruleset", new GenericCallback<String>() {
 			public void onSuccess(String result) {
 				render(document, callback);
 			}
 
 			public void onFailure(Throwable caught) {
 				render(null, callback);
 			}
 		});
 	}
 
 	private void render(final NativeDocument document, final DrawsLazily.DoneDrawingCallback callback) {
 		final LayoutContainer container = new LayoutContainer();
 		container.setLayout(new BorderLayout());
 		container.add(createLeftPanel(document), new BorderLayoutData(LayoutRegion.WEST, 200, 200, 200));
 		container.add(center, new BorderLayoutData(LayoutRegion.CENTER));
 
 		add(container);
 
 		isDrawn = true;
 		callback.isDrawn();
 	}
 
 	private LayoutContainer createLeftPanel(final NativeDocument document) {
 		list.addListener(Events.BeforeSelect, new Listener<DataListEvent>() {
 			public void handleEvent(final DataListEvent be) {
 				if (designer.hasChanged()) {
 					be.setCancelled(true);
 					final DataListItem item = be.getItem();
 					WindowUtils.confirmAlert("Warning", "You have unsaved changes.  Save before switching?",
 							new WindowUtils.MessageBoxListener() {
 								public void onNo() {
 									designer.clearChanges();
 									be.getContainer().setSelectedItem(item);
 								}
 
 								public void onYes() {
 									final String ruleName = list.getSelectedItem().getText();
 									final String selectedUri = createUrl(ruleName);
 
 									saveQuery(selectedUri, new GenericCallback<Object>() {
 										public void onSuccess(Object result) {
 											be.getContainer().setSelectedItem(item);	
 										}
 										public void onFailure(Throwable caught) {
 											WindowUtils.errorAlert("Could not save, please try again later.");
 										}
 									});
 								}
 							}, "Yes", "No");
 				}
 			}
 		});
 		list.addListener(Events.SelectionChange, new Listener<DataListEvent>() {
 			public void handleEvent(final DataListEvent be) {
 				center.removeAll();
 				if (be.getSelected().isEmpty())
 					return;
 
 				final String ruleName = be.getSelected().get(0).getText();
 				final String selectedUri = createUrl(ruleName);
 				final NativeDocument document = SimpleSISClient.getHttpBasicNativeDocument();
 				document.get(selectedUri, new GenericCallback<String>() {
 					public void onSuccess(String result) {
 						final SISQBQuery query = new SISQBQuery();
 						query.load(document);
 
 						designer.draw(query);
 
 						final ToolBar bar = new ToolBar();
 						bar.add(new Button("Help", new SelectionListener<ButtonEvent>() {
 							public void componentSelected(ButtonEvent ce) {
 								final HelpWindow window = new HelpWindow();
 								window.show();
 							}
 						}));
 						bar.add(new FillToolItem());
 						bar.add(new Button("Validate an Assessment", new SelectionListener<ButtonEvent>() {
 							public void componentSelected(ButtonEvent ce) {
 								if (designer.hasChanged()) {
 									WindowUtils.confirmAlert("Warning", "You must save before validating.  Save changes?",
 											new WindowUtils.MessageBoxListener() {
 												public void onNo() {
 												}
 
 												public void onYes() {
 													final String ruleName = list.getSelectedItem().getText();
 													final String selectedUri = createUrl(ruleName);
 
 													saveQuery(selectedUri, new GenericCallback<Object>() {
 														public void onSuccess(Object result) {
 															ValidateAssessmentWindow window = new ValidateAssessmentWindow(
 																list.getSelectedItem().getText());
 															window.show();	
 														}
 														public void onFailure(Throwable caught) {
 															WindowUtils.errorAlert("Could not save, please try again later.");
 														}
 													});
 												}
 											}, "Yes", "No");
 								}
 								else {
 									ValidateAssessmentWindow window = new ValidateAssessmentWindow(be.getSelected().get(0)
 											.getText());
 									window.show();
 								}
 							}
 						}));
 						bar.add(new Button("Save", new SelectionListener<ButtonEvent>() {
 							public void componentSelected(ButtonEvent ce) {
 								saveQuery(selectedUri);
 							}
 						}));
 
 						final LayoutContainer container = new LayoutContainer();
 						container.setLayout(new BorderLayout());
 						container.add(bar, new BorderLayoutData(LayoutRegion.NORTH, 25, 25, 25));
 						container.add(designer, new BorderLayoutData(LayoutRegion.CENTER));
 
 						center.add(container);
 
 					}
 
 					public void onFailure(Throwable caught) {
 						WindowUtils.errorAlert("Could not load rule set, please try again later.");
 					}
 				});
 			}
 		});
 		if (document != null) {
 			final NativeNodeList nodes = document.getDocumentElement().getChildNodes();
 			for (int i = 0; i < nodes.getLength(); i++) {
 				final NativeNode current = nodes.item(i);
 				if ("uri".equals(current.getNodeName())) {
 					String name = ((NativeElement) current).getAttribute("name");
 					int index;
 					if ((index = name.indexOf('.')) != -1)
 						name = name.substring(0, index);
 					final DataListItem item = new DataListItem(name);
 					item.setData("uri", current.getTextContent());
 					list.add(item);
 				}
 			}
 		}
 
 		/*final ButtonBar bar = new ButtonBar();
 		bar.setAlignment(HorizontalAlignment.CENTER);*/
 		final ToolBar bar = new ToolBar();
 		bar.add(new ButtonToolItem("Add", new SelectionListener<ButtonEvent>() {
 			public void componentSelected(ButtonEvent ce) {
 				final Window window = new NewCustomConfigurationPanel() {
 					public void onSave(String name) {
 						final DataListItem newItem = new DataListItem(name);
 						list.add(newItem);
 						list.setSelectedItem(newItem);
 					}
 				};
 				window.show();
 			}
 		}));
 		bar.add(new ButtonToolItem("Remove", new SelectionListener<ButtonEvent>() {
 			public void componentSelected(ButtonEvent ce) {
 				final DataListItem item = list.getSelectedItem();
 				if (item == null)
 					WindowUtils.errorAlert("Please select an item to remove.");
 				else {
 					WindowUtils.confirmAlert("Confirm",
 							"Are you sure you want to permanently delete this configuration set?",
 							new WindowUtils.MessageBoxListener() {
 								public void onNo() {
 								}
 
 								public void onYes() {
 									final NativeDocument delete = SimpleSISClient.getHttpBasicNativeDocument();
 									delete.delete(createUrl(item.getText()), new GenericCallback<String>() {
 										public void onFailure(Throwable caught) {
 											WindowUtils.errorAlert("Failed to delete " + item.getText()
 													+ ", please try again later.");
 										}
 
 										public void onSuccess(String result) {
 											list.remove(item);
 											Info.display("Success", "{0} successfully deleted.", item.getText());
 										}
 									});
 								}
 							});
 				}
 			}
 		}));
 		bar.add(new FillToolItem());
 		bar.add(new ButtonToolItem("Properties", new SelectionListener<ButtonEvent>() {
 			public void componentSelected(ButtonEvent ce) {
 				final DataListItem item = list.getSelectedItem();
 				if (item == null)
 					WindowUtils.errorAlert("Please select an item first.");
 				final IntegrityRulesetPropertiesEditor editor = 
 					new IntegrityRulesetPropertiesEditor((SISQBQuery)designer.getQuery());
 				editor.show();
 			}
 		}));
 
 		final LayoutContainer wrapper = new LayoutContainer();
 		wrapper.setLayout(new FillLayout());
 		wrapper.add(list);
 
 		final ToolBar header = new ToolBar();
 		header.add(new Button("Assessment Rulesets"));
 
 		final LayoutContainer container = new LayoutContainer();
 		container.setLayout(new BorderLayout());
 		container.add(header, new BorderLayoutData(LayoutRegion.NORTH, 25, 25, 25));
 		container.add(bar, new BorderLayoutData(LayoutRegion.SOUTH, 25, 25, 25));
 		container.add(wrapper, new BorderLayoutData(LayoutRegion.CENTER));
 
 		return container;
 	}
 
 	private void saveQuery(String selectedUri) {
 		saveQuery(selectedUri, null);
 	}
 
 	private void saveQuery(String selectedUri, final GenericCallback<Object> callback) {
 		final NativeDocument document = SimpleSISClient.getHttpBasicNativeDocument();
 		document.post(selectedUri, designer.getQuery().toXML(), new GenericCallback<String>() {
 			public void onSuccess(String result) {
 				designer.updateSavedXML();
 				Info.display("Success", "Information Saved.");
 				if (callback != null)
 					callback.onSuccess(null);
 			}
 
 			public void onFailure(Throwable caught) {
 				if (callback != null)
 					callback.onFailure(caught);
 				else
 					WindowUtils.errorAlert("Could not save, please try again later.");
 			}
 		});
 	}
 
 	public static String createUrl(String ruleName) {
 		return createUrl(ruleName, "ruleset");
 	}
 
 	public static String createUrl(String ruleName, String service) {
 		return UriBase.getInstance().getIntegrityBase() + "/" + service + (ruleName == null ? "" : "/" + ruleName + ".xml");
 	}
 
 	public static class ValidateAssessmentWindow extends Window {
 
 		private final TextField<Integer> field;
 		private final ComboBox<BaseModelData> status;
 
 		public ValidateAssessmentWindow(final String rule) {
 			super();
 
 			setHeading("Validate an Assessment");
 			setLayout(new FitLayout());
 			setModal(true);
 			setClosable(true);
 			setSize(400, 150);
 
 			field = new TextField<Integer>();
 			field.setFieldLabel("Enter Assessment ID");
 			field.setMaxLength(16);
 			field.setAllowBlank(false);
 			
 			final BaseModelData draft = new BaseModelData();
 			draft.set("text", "Draft");
 			draft.set("value", AssessmentType.DRAFT_ASSESSMENT_TYPE);
 			
 			final BaseModelData published = new BaseModelData();
 			published.set("text", "Published");
 			published.set("value", AssessmentType.PUBLISHED_ASSESSMENT_TYPE);
 			
 			final ListStore<BaseModelData> store = new ListStore<BaseModelData>();
 			store.add(draft);
 			store.add(published);
 			
 			status = new ComboBox<BaseModelData>();
 			status.setStore(store);
 			status.setFieldLabel("Assessment Status");
 			status.setAllowBlank(false);
 			status.setForceSelection(true);
 			status.setValue(draft);
 
 			final FormPanel panel = new FormPanel();
 			panel.setHeaderVisible(false);
 			panel.setBodyBorder(false);
 			panel.setLabelWidth(120);
 			panel.add(field);
 			panel.add(status);
 
 			add(panel);
 
 			addButton(new Button("Validate", new SelectionListener<ButtonEvent>() {
 				public void componentSelected(ButtonEvent ce) {
 					if (!panel.isValid()) {
 						WindowUtils.errorAlert("Please fill in all fields.");
 						return;
 					}
 						
 					final Integer value = field.getValue();
 					final String assessmentStatus = (String)status.getValue().get("value");
 					ClientAssessmentValidator.validate(value, assessmentStatus, 
 							rule, new GenericCallback<NativeDocument>() {
 						public void onSuccess(NativeDocument result) {
 							close();
 							ValidationResultsWindow window = new ValidationResultsWindow(field.getValue(), result
 									.getText());
 							window.show();
 						}
 
 						public void onFailure(Throwable caught) {
 							close();
 						}
 					});
 				}
 			}));
 			addButton(new Button("Validate for All Rules", new SelectionListener<ButtonEvent>() {
 				public void componentSelected(ButtonEvent ce) {
 					final Integer value = field.getValue();
 					
 					ClientAssessmentValidator.validate(value, AssessmentType.PUBLISHED_ASSESSMENT_TYPE, 
 							null, new GenericCallback<NativeDocument>() {
 						public void onSuccess(NativeDocument result) {
 							close();
 							ValidationResultsWindow window = new ValidationResultsWindow(field.getValue(), result
 									.getText());
 							window.show();
 						}
 
 						public void onFailure(Throwable caught) {
 							close();
 						}
 					});
 				}
 			}));			
 			addButton(new Button("Cancel", new SelectionListener<ButtonEvent>() {
 				public void componentSelected(ButtonEvent ce) {
 					close();
 				}
 			}));
 // setAlignment(HorizontalAlignment.CENTER);
 		}
 
 	}
 
 	public static abstract class NewCustomConfigurationPanel extends Window {
 
 		private final TextField<String> field;
 
 		public NewCustomConfigurationPanel() {
 			super();
 			setHeading("Add Rule Set");
 			setLayout(new FitLayout());
 			setModal(true);
 			setClosable(true);
 			setSize(400, 200);
 
 			field = new TextField<String>();
 			field.setFieldLabel("Enter Name");
 			field.setMaxLength(16);
 			field.setAllowBlank(false);
 
 			final FormPanel panel = new FormPanel();
 			panel.setHeaderVisible(false);
 			panel.add(new Html("Note: The name can contain only letters and numbers."));
 			panel.add(field);
 
 			add(panel);
 
 			addButton(new Button("Save", new SelectionListener<ButtonEvent>() {
 				public void componentSelected(ButtonEvent ce) {
 					final String value = field.getValue();
 
 					final IDValidator validator = new IDValidator();
 					validator.setLowercaseRestriction(false);
 					validator.setMustBeAlphaNumeric(true);
 					validator.setMustBeginWithLetter(false);
 					validator.setNoWhiteSpace(true);
 
 					final IDValidator.ValidationInfo response = validator.validate(value);
 
 					if (!response.getErrors().isEmpty()) {
 						String errorMsg = validator.getStandardErrorMessage(response.getErrors().get(0));
 						if (response.getSuggestedID() != null) {
 							field.setValue(response.getSuggestedID());
 							errorMsg += " A suggested name has been provided.";
 						}
 						WindowUtils.errorAlert(errorMsg);
 						return;
 					}
 
 					final NativeDocument document = SimpleSISClient.getHttpBasicNativeDocument();
 					document.put(createUrl(field.getValue()), getDefaultXML(), new GenericCallback<String>() {
 						public void onFailure(Throwable caught) {
 							if (caught instanceof GWTConflictException) {
 								WindowUtils.errorAlert("A configuration by this name already exists.");
 								field.reset();
 							} else
 								WindowUtils.errorAlert("Could not save, please try again later.");
 						}
 
 						public void onSuccess(String result) {
 							Info.display("Success", "New ruleset created.");
 							close();
 							onSave(field.getValue());
 						}
 					});
 				}
 			}));
 			addButton(new Button("Cancel", new SelectionListener<ButtonEvent>() {
 				public void componentSelected(ButtonEvent ce) {
 					close();
 				}
 			}));
 // setAlignment(HorizontalAlignment.CENTER);
 		}
 
 		private String getDefaultXML() {
 			final StringBuilder builder = new StringBuilder();
 			builder.append("<query version=\"1.1\">");
 			for (SelectedField field : defaultTables)
 				builder.append(field.toXML());
 			builder.append("</query>");
 			return builder.toString();
 		}
 
 		public abstract void onSave(String name);
 
 	}
 	
 	private static class ButtonToolItem extends Button {
 		
 		public ButtonToolItem(final String text, final SelectionListener<ButtonEvent> listener) {
 			super(text, new SelectionListener<ButtonEvent>() {
 				public void componentSelected(ButtonEvent ce) {
 					listener.componentSelected(new ButtonEvent(null));
 				}
 			});
 			setIconStyle("icon_integrity_" + text.toLowerCase());
 		}
 		
 	}
 
 }
