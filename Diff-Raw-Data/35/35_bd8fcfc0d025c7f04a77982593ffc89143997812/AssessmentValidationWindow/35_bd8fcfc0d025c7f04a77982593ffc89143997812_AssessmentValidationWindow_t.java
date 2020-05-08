 package org.iucn.sis.shared.api.integrity;
 
 import org.iucn.sis.client.api.utils.UriBase;
 import org.iucn.sis.client.container.SimpleSISClient;
 import org.iucn.sis.client.panels.integrity.ValidationResultsWindow;
 
 import com.extjs.gxt.ui.client.data.BaseModelData;
 import com.extjs.gxt.ui.client.event.ButtonEvent;
 import com.extjs.gxt.ui.client.event.SelectionListener;
 import com.extjs.gxt.ui.client.store.ListStore;
 import com.extjs.gxt.ui.client.widget.Window;
 import com.extjs.gxt.ui.client.widget.button.Button;
 import com.extjs.gxt.ui.client.widget.form.ComboBox;
 import com.extjs.gxt.ui.client.widget.form.FormPanel;
 import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
 import com.extjs.gxt.ui.client.widget.layout.FillLayout;
 import com.solertium.lwxml.shared.GenericCallback;
 import com.solertium.lwxml.shared.NativeDocument;
 import com.solertium.lwxml.shared.NativeElement;
 import com.solertium.lwxml.shared.NativeNode;
 import com.solertium.lwxml.shared.NativeNodeList;
 import com.solertium.util.extjs.client.WindowUtils;
 import com.solertium.util.gwt.ui.DrawsLazily;
 
 /**
  * AssessmentValidationWindow.java
  * 
  * Pop this up to validate a single assessment against a single ruleset. It will
  * automatically pop the results window after the user chooses which validator
  * to run.
  * 
  * @author <a href="mailto:carl.scott@solertium.com">Carl Scott</a>, <a
  *         href="http://www.solertium.com">Solertium Corporation</a>
  * 
  */
 public class AssessmentValidationWindow extends Window implements DrawsLazily {
 
 	private final Integer assessmentID;
 	private final String assessmentType;
 
 	public AssessmentValidationWindow(Integer assessmentID, String assessmentType) {
 		super();
 		this.assessmentID = assessmentID;
 		this.assessmentType = assessmentType;
 
 		setIconStyle("icon-integrity");
 		setClosable(true);
 		setModal(true);
 		setHeading("Integrity Validator");
 		setLayout(new FillLayout());
 // setAlignment(HorizontalAlignment.CENTER);
 		setSize(450, 200);
 	}
 
 	public void draw(final DoneDrawingCallback callback) {
 		final NativeDocument document = SimpleSISClient.getHttpBasicNativeDocument();
 		document.get(UriBase.getInstance().getIntegrityBase() + "/ruleset",
 				new GenericCallback<String>() {
 					public void onSuccess(String result) {
 						final ListStore<BaseModelData> store = new ListStore<BaseModelData>();
 						final NativeNodeList nodes = document
 								.getDocumentElement().getChildNodes();
 						for (int i = 0; i < nodes.getLength(); i++) {
 							final NativeNode current = nodes.item(i);
 							if ("uri".equals(current.getNodeName())) {
 								final String name = ((NativeElement) current)
 										.getAttribute("name");
 								String display = name;
 								int index;
 								if ((index = display.indexOf('.')) != -1)
 									display = display.substring(0, index);
 
 								final BaseModelData model = new BaseModelData();
 								model.set("text", display);
 								model.set("value", name);
 
 								store.add(model);
 							}
 						}
 
 						final ComboBox<BaseModelData> box = new ComboBox<BaseModelData>();
						box.setTriggerAction(TriggerAction.ALL);
 						box.setStore(store);
 						box.setFieldLabel("Select Validator");
 						box.setEditable(false);
 						box.setForceSelection(true);
 						box.setAllowBlank(false);
 
 						final TextField<Integer> field = new TextField<Integer>();
 						field.setFieldLabel("Validate");
 						field.setReadOnly(true);
 						field.setValue(assessmentID);
 
 						final FormPanel panel = new FormPanel();
 						panel.setLabelWidth(150);
 						panel.setHeaderVisible(false);
 						panel.setBodyBorder(false);
 						panel.add(field);
 						panel.add(box);
 
 						add(panel);
 						addButton(new Button("Submit",
 								new SelectionListener<ButtonEvent>() {
 									public void componentSelected(ButtonEvent ce) {
 										if (panel.isValid()) {
 											String file = box.getValue().get(
 													"text");
 											validate(file);
 										}
 									}
 								}));
 						addButton(new Button("Close",
 								new SelectionListener<ButtonEvent>() {
 									public void componentSelected(ButtonEvent ce) {
 										hide();
 									}
 								}));
 
 						callback.isDrawn();
 					}
 
 					public void onFailure(Throwable caught) {
 						WindowUtils.errorAlert("No validators available.");
 					}
 				});
 	}
 
 	public void open() {
 		draw(new DoneDrawingCallback() {
 			public void isDrawn() {
 				show();
 			}
 		});
 	}
 
 	/**
 	 * @deprecated call open instead!
 	 */
 	public void show() {
 		super.show();
 	}
 
 	private void validate(String file) {
 		ClientAssessmentValidator.validate(assessmentID, assessmentType, file,
 				new GenericCallback<NativeDocument>() {
 					public void onSuccess(NativeDocument result) {
 						hide();
 						ValidationResultsWindow window = new ValidationResultsWindow(
 								assessmentID, result.getText());
 						window.show();
 					}
 
 					public void onFailure(Throwable caught) {
 						// Nothing else to do...
 						hide();
 					}
 				});
 	}
 
 }
