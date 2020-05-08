 package org.palaso.languageforge.client.lex.configure.presenter;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.SortedMap;
 import java.util.TreeMap;
 
 import org.palaso.languageforge.client.lex.common.CheckableItem;
 import org.palaso.languageforge.client.lex.common.ConsoleLog;
 import org.palaso.languageforge.client.lex.common.IPersistable;
 import org.palaso.languageforge.client.lex.common.SettingInputSystemItem;
 import org.palaso.languageforge.client.lex.common.SettingInputSystemItemHelper;
 import org.palaso.languageforge.client.lex.common.enums.EntryFieldType;
 import org.palaso.languageforge.client.lex.common.enums.SettingFieldClassNameType;
 import org.palaso.languageforge.client.lex.common.enums.SettingFieldDataType;
 import org.palaso.languageforge.client.lex.common.enums.SettingFieldVisibilityType;
 import org.palaso.languageforge.client.lex.configure.ConfigureEventBus;
 import org.palaso.languageforge.client.lex.configure.view.ConfigureSettingFieldsView;
 import org.palaso.languageforge.client.lex.controls.ExtendedCheckBox;
 import org.palaso.languageforge.client.lex.controls.ExtendedTextBox;
 import org.palaso.languageforge.client.lex.controls.FastTree;
 import org.palaso.languageforge.client.lex.controls.FastTreeItem;
 import org.palaso.languageforge.client.lex.controls.TextChangeEvent;
 import org.palaso.languageforge.client.lex.controls.TextChangeEventHandler;
 import org.palaso.languageforge.client.lex.controls.TreeItemSelectedHandler;
 import org.palaso.languageforge.client.lex.main.service.ILexService;
 import org.palaso.languageforge.client.lex.model.CurrentEnvironmentDto;
 import org.palaso.languageforge.client.lex.model.FieldSettings;
 import org.palaso.languageforge.client.lex.model.FieldSettingsEntry;
 import org.palaso.languageforge.client.lex.model.IanaBaseDataDto;
 import org.palaso.languageforge.client.lex.model.UserDto;
 import org.palaso.languageforge.client.lex.model.settings.fields.SettingFieldsDto;
 import org.palaso.languageforge.client.lex.model.settings.fields.SettingFieldsFieldElementDto;
 import org.palaso.languageforge.client.lex.model.settings.fields.SettingFieldsFieldWritingSystemDto;
 import org.palaso.languageforge.client.lex.model.settings.inputsystems.SettingInputSystemElementDto;
 import org.palaso.languageforge.client.lex.model.settings.inputsystems.SettingInputSystemsDto;
 
 import com.google.gwt.core.client.JsArray;
 import com.google.gwt.core.client.JsArrayString;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.event.dom.client.HasClickHandlers;
 import com.google.gwt.event.logical.shared.ValueChangeEvent;
 import com.google.gwt.event.logical.shared.ValueChangeHandler;
 import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.CheckBox;
 import com.google.gwt.user.client.ui.SimplePanel;
 import com.google.gwt.user.client.ui.TabLayoutPanel;
 import com.google.gwt.user.client.ui.Widget;
 import com.google.inject.Inject;
 import com.mvp4g.client.annotation.Presenter;
 import com.mvp4g.client.presenter.BasePresenter;
 
 @Presenter(view = ConfigureSettingFieldsView.class)
 public class ConfigureSettingFieldsPresenter
 		extends
 		BasePresenter<ConfigureSettingFieldsPresenter.IConfigureSettingFieldsView, ConfigureEventBus>
 		implements IPersistable {
 	@Inject
 	public ILexService lexService;
 
 	// only a copy of Fields setting, and all in referenced, so we can easy to
 	// persist to server.
 	private SettingFieldsDto fieldDto = null;
 	private ArrayList<CheckableItem> fieldsListDs;
 	// private HashMap<String, CheckableItem> fieldsListDs;
 	private SettingInputSystemItemHelper sisHelper;
 
 	public void bind() {
 
 		view.getInputSystemMoveDownClickHandlers().addClickHandler(
 				new ClickHandler() {
 
 					@Override
 					public void onClick(ClickEvent event) {
 						FastTree tree = view.getInputSystemsTree();
 						if (tree.getSelectedItem() != null) {
 							tree.getSelectedItem().moveItemDown();
 							updateInputSystems();
 						}
 					}
 				});
 
 		view.getInputSystemMoveUpClickHandlers().addClickHandler(
 				new ClickHandler() {
 
 					@Override
 					public void onClick(ClickEvent event) {
 						FastTree tree = view.getInputSystemsTree();
 						if (tree.getSelectedItem() != null) {
 							tree.getSelectedItem().moveItemUp();
 							updateInputSystems();
 						}
 					}
 				});
 
 		view.getFieldsTree().setItemSelectedCallbackHandler(
 				new TreeItemSelectedHandler() {
 
 					@Override
 					public void onTreeItemSelected(FastTreeItem item) {
 						if (((CheckableItem) item.getData() != null)
 								&& (SettingFieldsFieldElementDto) (((CheckableItem) item
 										.getData()).getData()) != null) {
 							view.getFieldDetailTabPanel().setVisible(true);
 							SettingFieldsFieldElementDto fieldElementDto = (SettingFieldsFieldElementDto) (((CheckableItem) item
 									.getData()).getData());
 
 							fillInputSystems(fieldElementDto);
 							fillSetupTab(fieldElementDto);
 						} else {
 							// no data or not a entry, hide right tab
 							view.getFieldDetailTabPanel().setVisible(false);
 						}
 					}
 				});
 
 		view.getSetupNameTextBox().addTextChangeEventHandler(
 				new TextChangeEventHandler() {
 
 					@Override
 					public void onTextChange(TextChangeEvent event) {
 						if (view.getFieldsTree().getSelectedItem() != null) {
 							FastTreeItem item = view.getFieldsTree()
 									.getSelectedItem();
 
 							CheckableItem checkableItem = (CheckableItem) item
 									.getData();
 
 							SettingFieldsFieldElementDto fieldElementDto = (SettingFieldsFieldElementDto) (checkableItem
 									.getData());
 
 							String newDisplayName = view.getSetupNameTextBox()
 									.getText();
 
 							fieldElementDto.setDisplayName(newDisplayName);
 							checkableItem.setDisplayText(newDisplayName);
							((CheckBox) item.getWidget())
 									.setText(newDisplayName);
 						}
 
 					}
 				});
 
 		view.getSetupHideIfEmptyToggleCheckbox().addValueChangeHandler( new ValueChangeHandler<Boolean>() {
 			
 			@Override
 			public void onValueChange(ValueChangeEvent<Boolean> event) {
 				if (view.getFieldsTree().getSelectedItem() != null) {
 					FastTreeItem item = view.getFieldsTree()
 							.getSelectedItem();
 
 					CheckableItem checkableItem = (CheckableItem) item
 							.getData();
 
 					SettingFieldsFieldElementDto fieldElementDto = (SettingFieldsFieldElementDto) (checkableItem
 							.getData());
 					fieldElementDto.setVisibility(event.getValue().booleanValue());
 
 				}
 			}
 		});
 	}
 
 	public void onAttachFieldsView(SimplePanel simplePanel) {
 		simplePanel.clear();
 		simplePanel.add(view.getWidget());
 		// fieldsInputSystems = new LinkedList<CheckableItem>();
 		eventBus.addSubControl(simplePanel, this);
 
 		SortedMap<String, IanaBaseDataDto> ianaRegions = new TreeMap<String, IanaBaseDataDto>();
 		eventBus.getIanaRegions(ianaRegions);
 
 		SortedMap<String, IanaBaseDataDto> ianaScripts = new TreeMap<String, IanaBaseDataDto>();
 		eventBus.getIanaScripts(ianaScripts);
 
 		SortedMap<String, IanaBaseDataDto> ianaLanguages = new TreeMap<String, IanaBaseDataDto>();
 		eventBus.getIanaLanguages(ianaLanguages);
 
 		sisHelper = new SettingInputSystemItemHelper(ianaRegions, ianaScripts,
 				ianaLanguages);
 	}
 
 	private void populateTreeFromDataSource() {
 		FastTree tree = view.getFieldsTree();
 		tree.setSelectedItem(null);
 		tree.clear();
 
 		for (CheckableItem chkItem : fieldsListDs) {
 			FastTreeItem topTreeItem = createTreeItem(chkItem);
 			topTreeItem.setState(true);
 			tree.addItem(topTreeItem);
 		}
 
 	}
 
 	private void updateInputSystems() {
 		// here will loop through the tree list to get all selected node
 		if (view.getInputSystemsTree().getChildCount() > 0) {
 			LinkedList<FastTreeItem> itemList = view.getInputSystemsTree()
 					.getItems();
 
 			FastTreeItem treeItem = view.getFieldsTree().getSelectedItem();
 
 			if (treeItem != null) {
 
 				if (((CheckableItem) treeItem.getData() != null)
 						&& (SettingFieldsFieldElementDto) (((CheckableItem) treeItem
 								.getData()).getData()) != null) {
 
 					// get ElementDto which attached to the tree item.
 					SettingFieldsFieldElementDto fieldElementDto = (SettingFieldsFieldElementDto) (((CheckableItem) treeItem
 							.getData()).getData());
 
 					JsArray<SettingFieldsFieldWritingSystemDto> newWritingSystemDtoList = fieldElementDto
 							.getNewWritingSystems();
 
 					JsArray<SettingFieldsFieldWritingSystemDto> newAvailableWritingSystemDtoList = fieldElementDto
 							.getNewAvailableWritingSystems();
 
 					if (newWritingSystemDtoList.length() > 0) {
 						throw new RuntimeException(
 								"at this time list should be empty!");
 					}
 
 					for (FastTreeItem fastTreeItem : itemList) {
 						// we need checked only
						if (((CheckBox) fastTreeItem.getWidget()).getValue() == true) {
 							SettingFieldsFieldWritingSystemDto newWritingSystemDto = SettingFieldsFieldWritingSystemDto
 									.getNew();
 							newWritingSystemDto
 									.setId((String) ((CheckableItem) fastTreeItem
 											.getData()).getData());
 							newWritingSystemDtoList.push(newWritingSystemDto);
 						}
 
 						SettingFieldsFieldWritingSystemDto availableWritingSystemDto = SettingFieldsFieldWritingSystemDto
 								.getNew();
 						availableWritingSystemDto
 								.setId((String) ((CheckableItem) fastTreeItem
 										.getData()).getData());
 						newAvailableWritingSystemDtoList
 								.push(availableWritingSystemDto);
 					}
 
 				}
 
 			}
 		}
 
 	}
 
 	private void fieldsDependenceChecker(
 			SettingFieldsFieldElementDto relatedData) {
 		// Rule-1: when "Definition" unckecked, all other in "LexSense" should
 		// unchecked.
 
 		// Rule-2: when "Example Sentence" unckecked, all other in
 		// "LexExampleSentence" should unchecked.
 
 		// Rule-3: when any in "LexSense" checked, "Definition" should also
 		// checked.
 
 		// Rule-4: when any in "LexExampleSentence" checked, "Example Sentence"
 		// should also checked.
 
 		ConsoleLog.log("Dependece check: " + relatedData.getFieldName() + " / "
 				+ relatedData.getEnabled());
 		FastTree tree = view.getFieldsTree();
 		if (relatedData.getClassName() == SettingFieldClassNameType.LEXSENSE) {
 
 			if (relatedData.getFieldName().equalsIgnoreCase("definition")
 					&& relatedData.getEnabled() == false) {
 
 				for (FastTreeItem treeItem : tree.getItems()) {
 					for (FastTreeItem treeChildItem : treeItem.getChildren()) {
 						CheckableItem chkItem = (CheckableItem) treeChildItem
 								.getData();
 
 						if (chkItem.getData() != null
 								&& chkItem.getData() instanceof SettingFieldsFieldElementDto) {
 							SettingFieldsFieldElementDto data = (SettingFieldsFieldElementDto) chkItem
 									.getData();
 							if (data.getClassName() == SettingFieldClassNameType.LEXSENSE
 									&& !data.getFieldName().equalsIgnoreCase(
 											"definition")
 									&& !isInIgnoreList(data)) {
 
 								ExtendedCheckBox chkBox = (ExtendedCheckBox) treeChildItem
 										.getWidget();
 
 								data.setEnabled(false);
 								chkBox.setValue(false);
 
 
 							}
 						}
 
 					}
 				}
 			} else {
 				// rule 3
 				if (!isInIgnoreList(relatedData)) {
 					for (FastTreeItem treeItem : tree.getItems()) {
 						for (FastTreeItem treeChildItem : treeItem
 								.getChildren()) {
 							CheckableItem chkItem = (CheckableItem) treeChildItem
 									.getData();
 							if (chkItem.getData() != null
 									&& chkItem.getData() instanceof SettingFieldsFieldElementDto) {
 								SettingFieldsFieldElementDto data = (SettingFieldsFieldElementDto) chkItem
 										.getData();
 								if (data.getFieldName().equalsIgnoreCase(
 										"definition")) {
 									ExtendedCheckBox chkBox = (ExtendedCheckBox) treeChildItem
 											.getWidget();
 									chkBox.setValue(true);
 									data.setEnabled(true);
 								}
 							}
 						}
 					}
 				}
 			}
 
 		} else if (relatedData.getClassName() == SettingFieldClassNameType.LEXEXAMPLESENTENCE) {
 			if (relatedData.getFieldName().equalsIgnoreCase("ExampleSentence")
 					&& relatedData.getEnabled() == false) {
 				// rule 2
 				for (FastTreeItem treeItem : tree.getItems()) {
 					for (FastTreeItem treeChildItem : treeItem.getChildren()) {
 						CheckableItem chkItem = (CheckableItem) treeChildItem
 								.getData();
 
 						if (chkItem.getData() != null
 								&& chkItem.getData() instanceof SettingFieldsFieldElementDto) {
 							SettingFieldsFieldElementDto data = (SettingFieldsFieldElementDto) chkItem
 									.getData();
 							if (data.getClassName() == SettingFieldClassNameType.LEXEXAMPLESENTENCE
 									&& !data.getFieldName().equalsIgnoreCase(
 											"ExampleSentence")
 									&& !isInIgnoreList(data)) {
 
 								ExtendedCheckBox chkBox = (ExtendedCheckBox) treeChildItem
 										.getWidget();
 
 								data.setEnabled(false);
 								chkBox.setValue(false);
 
 							}
 						}
 
 					}
 				}
 			} else {
 				// rule 4
 				if (!isInIgnoreList(relatedData)) {
 					for (FastTreeItem treeItem : tree.getItems()) {
 						for (FastTreeItem treeChildItem : treeItem
 								.getChildren()) {
 							CheckableItem chkItem = (CheckableItem) treeChildItem
 									.getData();
 							if (chkItem.getData() != null
 									&& chkItem.getData() instanceof SettingFieldsFieldElementDto) {
 								SettingFieldsFieldElementDto data = (SettingFieldsFieldElementDto) chkItem
 										.getData();
 								if (data.getFieldName().equalsIgnoreCase(
 										"ExampleSentence")) {
 									ExtendedCheckBox chkBox = (ExtendedCheckBox) treeChildItem
 											.getWidget();
 									chkBox.setValue(true);
 									data.setEnabled(true);
 								}
 							}
 						}
 					}
 				}
 			}
 		}
 	}
 
 	private FastTreeItem createTreeItem(CheckableItem item) {
 		FastTreeItem topTreeItem = new FastTreeItem();
 		topTreeItem.setData(item);
 		if (item.isCheckable()) {
 			final ExtendedCheckBox chkBox = new ExtendedCheckBox(
 					item.getDisplayText());
 			if (item.getData() != null) {
 				if (item.getData() instanceof SettingFieldsFieldElementDto) {
 					final SettingFieldsFieldElementDto relatedData = (SettingFieldsFieldElementDto) item
 							.getData();
 					chkBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
 						@Override
 						public void onValueChange(
 								ValueChangeEvent<Boolean> event) {
 							// the change is makes on DS directly, so easy for
 							// saving operation.
 							relatedData.setEnabled(chkBox.getValue()
 									.booleanValue());
 							fieldsDependenceChecker(relatedData);
 						}
 					});
 				} else if (item.getData() instanceof String) {
 					chkBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
 						@Override
 						public void onValueChange(
 								ValueChangeEvent<Boolean> event) {
 							updateInputSystems();
 						}
 					});
 				}
 			}
 
 			chkBox.setValue(item.isChecked());
 			if (!item.getToolTip().isEmpty()) {
 				chkBox.setTitle(item.getToolTip());
 			}
 			topTreeItem.setWidget(chkBox);
 		} else {
 			topTreeItem.setText(item.getDisplayText());
 		}
 		if (item.getChilds().size() > 0) {
 			for (CheckableItem chkItem : item.getChilds()) {
 				topTreeItem.addItem(createTreeItem(chkItem));
 			}
 		}
 		return topTreeItem;
 	}
 
 	private void fillSetupTab(SettingFieldsFieldElementDto fieldElementDto) {
 		view.getSetupNameTextBox().setValue(fieldElementDto.getDisplayName());
 		if (fieldElementDto.getVisibility() == SettingFieldVisibilityType.NORMALLYHIDDEN) {
 			view.getSetupHideIfEmptyToggleCheckbox().setValue(true);
 
 		} else {
 			view.getSetupHideIfEmptyToggleCheckbox().setValue(false);
 		}
 	}
 
 	private void fillInputSystems(SettingFieldsFieldElementDto fieldElementDto) {
 
 		List<SettingInputSystemsDto> dtoList = new ArrayList<SettingInputSystemsDto>();
 		eventBus.getSettingInputSystems(dtoList);
 		JsArray<SettingInputSystemElementDto> arr = dtoList.get(0).getEntries();
 
 		Map<String, SettingInputSystemItem> availableInputSystems = new HashMap<String, SettingInputSystemItem>();
 		for (int i = 0; i < arr.length(); i++) {
 			SettingInputSystemElementDto sisElement = arr.get(i);
 			SettingInputSystemItem sisItem = sisHelper
 					.getSettingInputSystemItemFromDto(sisElement);
 			availableInputSystems.put(sisItem.getIdLangCode(), sisItem);
 		}
 
 		FastTree tree = view.getInputSystemsTree();
 		tree.setSelectedItem(null);
 		tree.clear();
 
 		Map<String, String> checkedInputSystems = new HashMap<String, String>();
 
 		for (int i = 0; i < fieldElementDto.getWritingSystems().length(); i++) {
 			String inputSystemsId = fieldElementDto.getWritingSystems().get(i)
 					.getId();
 			if (availableInputSystems.containsKey(inputSystemsId)) {
 				checkedInputSystems.put(inputSystemsId, inputSystemsId);
 			}
 		}
 
 		Map<String, String> addedInputSystems = new HashMap<String, String>();
 
 		// fill it as saved sequence
 		for (int i = 0; i < fieldElementDto.getAvailableWritingSystems()
 				.length(); i++) {
 			String inputSystemsId = fieldElementDto
 					.getAvailableWritingSystems().get(i).getId();
 			if (availableInputSystems.containsKey(inputSystemsId)) {
 				addedInputSystems.put(inputSystemsId, inputSystemsId);
 
 				CheckableItem checkableItem = new CheckableItem(true);
 				checkableItem.setDisplayText(inputSystemsId);
 
 				if (checkedInputSystems.containsKey(inputSystemsId)) {
 					checkableItem.setChecked(true);
 				} else {
 					checkableItem.setChecked(false);
 				}
 				checkableItem.setData(inputSystemsId);
 				FastTreeItem treeItem = createTreeItem(checkableItem);
 				treeItem.setState(true);
 				tree.addItem(treeItem);
 			}
 		}
 		// fill newly added
 		Iterator<SettingInputSystemItem> itr = availableInputSystems.values()
 				.iterator();
 
 		while (itr.hasNext()) {
 			SettingInputSystemItem sisItem = itr.next();
 			if (addedInputSystems.containsKey(sisItem.getIdLangCode())) {
 				continue;
 			}
 			CheckableItem checkableItem = new CheckableItem(true);
 			checkableItem.setDisplayText(sisItem.getIdLangCode());
 			checkableItem.setData(sisItem.getIdLangCode());
 			FastTreeItem treeItem = createTreeItem(checkableItem);
 			treeItem.setState(true);
 			tree.addItem(treeItem);
 		}
 	}
 
 	private boolean isInIgnoreList(SettingFieldsFieldElementDto fieldElementDto) {
 		if (fieldElementDto.getClassName() == SettingFieldClassNameType.LEXENTRY) {
 			if (fieldElementDto.getDataType() == SettingFieldDataType.MULTITEXT) {
 				if (fieldElementDto.getFieldName().equalsIgnoreCase("citation")) {
 					return true;
 				}
 
 				if (fieldElementDto.getFieldName().equalsIgnoreCase(
 						"literal-meaning")) {
 					return true;
 				}
 
 			} else if (fieldElementDto.getDataType() == SettingFieldDataType.RELATIONTOONEENTRY) {
 				if (fieldElementDto.getFieldName().equalsIgnoreCase("BaseForm")) {
 					return true;
 				}
 				if (fieldElementDto.getFieldName().equalsIgnoreCase("confer")) {
 					return true;
 				}
 
 			}
 		} else if (fieldElementDto.getClassName() == SettingFieldClassNameType.LEXSENSE) {
 			if (fieldElementDto.getDataType() == SettingFieldDataType.MULTITEXT) {
 				if (fieldElementDto.getFieldName().equalsIgnoreCase("gloss")) {
 					return true;
 				}
 			} else if (fieldElementDto.getDataType() == SettingFieldDataType.PICTURE) {
 				if (fieldElementDto.getFieldName().equalsIgnoreCase("Picture")) {
 					return true;
 				}
 			} else if (fieldElementDto.getDataType() == SettingFieldDataType.OPTIONCOLLECTION) {
 				if (fieldElementDto.getFieldName().equalsIgnoreCase(
 						"semantic-domain-ddp4")) {
 					return true;
 				}
 			}
 
 		} else if (fieldElementDto.getClassName() == SettingFieldClassNameType.PALASODATAOBJECT) {
 			if (fieldElementDto.getDataType() == SettingFieldDataType.MULTITEXT) {
 				if (fieldElementDto.getFieldName().equalsIgnoreCase("note")) {
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 
 	public void onFillFields(JsArray<SettingFieldsFieldElementDto> fieldsDto) {
 		fieldsListDs = new ArrayList<CheckableItem>();
 		fieldDto = SettingFieldsDto.getNew();
 		if (fieldsDto.length() > 0) {
 
 			Map<SettingFieldClassNameType, CheckableItem> existRootList = new HashMap<SettingFieldClassNameType, CheckableItem>();
 
 			for (int i = 0; i < fieldsDto.length(); i++) {
 				SettingFieldsFieldElementDto fieldElementDto = fieldsDto.get(i);
 				if (isInIgnoreList(fieldElementDto)) {
 					// check do those fields needed in WebApp or not.
 					continue;
 				}
 				fieldDto.addEntry(fieldElementDto);
 
 				// Build datasource
 				CheckableItem chkRoot = null;
 
 				if (existRootList.containsKey(fieldElementDto.getClassName())) {
 					chkRoot = existRootList.get(fieldElementDto.getClassName());
 				} else {
 					chkRoot = new CheckableItem(false);
 					chkRoot.setDisplayText(fieldElementDto
 							.getClassNameAsString());
 					existRootList.put(fieldElementDto.getClassName(), chkRoot);
 					chkRoot.setChecked(fieldElementDto.getEnabled());
 					fieldsListDs.add(chkRoot);
 				}
 
 				CheckableItem chkChild = new CheckableItem(true);
 				chkChild.setDisplayText((fieldElementDto.getDisplayName()
 						.isEmpty() ? fieldElementDto.getClassNameAsString()
 						: fieldElementDto.getDisplayName()));
 				// chkChild.setToolTip(fieldElementDto.getDescription());
 				chkChild.setData(fieldElementDto);
 				chkChild.setChecked(fieldElementDto.getEnabled());
 				chkRoot.addChild(chkChild);
 
 			}
 		} else {
 			eventBus.displayMessage("User Setting is missing!");
 		}
 		populateTreeFromDataSource();
 		view.getFieldDetailTabPanel().setVisible(false);
 	}
 
 	@Override
 	public boolean persistData(final UserDto user) {
 		AsyncCallback<SettingFieldsDto> asyncCallback = new AsyncCallback<SettingFieldsDto>() {
 
 			@Override
 			public void onSuccess(SettingFieldsDto result) {
 				if (user != null
 						&& user.getId() == CurrentEnvironmentDto
 								.getCurrentUser().getId()) {
 					// apply user is current user
 					updateCurrentFieldSetting(result);
 				} else if (user == null) {
 					// to all? update self too.
 					updateCurrentFieldSetting(result);
 				} else {
 					// nothing to do.
 				}
 			}
 
 			@Override
 			public void onFailure(Throwable caught) {
 				eventBus.handleError(caught);
 			}
 		};
 
 		if (user != null) {
 			lexService.updateSettingUserFieldsSetting(
 					String.valueOf(user.getId()), fieldDto, asyncCallback);
 		} else {
 			// apply to all user
 			Map<String, UserDto> list = new HashMap<String, UserDto>();
 			eventBus.getUserInProjectList(list);
 
 			Iterator<String> itr = list.keySet().iterator();
 			String userIds = "";
 			while (itr.hasNext()) {
 
 				if (!userIds.isEmpty()) {
 					userIds += "|";
 				}
 				userIds += itr.next().toString();
 			}
 
 			lexService.updateSettingUserFieldsSetting(userIds, fieldDto,
 					asyncCallback);
 		}
 		return true;
 	}
 
 	private void updateCurrentFieldSetting(SettingFieldsDto fields) {
 		FieldSettings currentFS = FieldSettings.fromWindow();
 
 		for (int i = 0; i < fields.getEntries().length(); i++) {
 			SettingFieldsFieldElementDto sffElement = fields.getEntries()
 					.get(i);
 			EntryFieldType fieldType = EntryFieldType.valueOf(sffElement
 					.getFieldName().toUpperCase());
 			switch (fieldType) {
 			case ENTRYLEXICALFORM:
 				updateFiledSetting(currentFS.value("Word"), sffElement);
 				break;
 			case DEFINITION:
 				updateFiledSetting(currentFS.value("Definition"), sffElement);
 				break;
 			case POS:
 				updateFiledSetting(currentFS.value("POS"), sffElement);
 				break;
 			case EXAMPLESENTENCE:
 				updateFiledSetting(currentFS.value("Example"), sffElement);
 				break;
 			case EXAMPLETRANSLATION:
 				updateFiledSetting(currentFS.value("Translation"), sffElement);
 				break;
 			case NEWDEFINITION:
 				updateFiledSetting(currentFS.value("NewMeaning"), sffElement);
 				break;
 			case NEWEXAMPLESENTENCE:
 				updateFiledSetting(currentFS.value("NewExample"), sffElement);
 				break;
 			default:
 				break;
 			}
 		}
 
 		FieldSettings.applyToCurrentUserForWindow(currentFS);
 	}
 
 	private void updateFiledSetting(FieldSettingsEntry fieldSetting,
 			SettingFieldsFieldElementDto sffElement) {
 
 		fieldSetting.setLabel(sffElement.getDisplayName());
 		fieldSetting
 				.setVisiblity(sffElement.getVisibility() == SettingFieldVisibilityType.VISIBLE);
 		fieldSetting.setEnabled(sffElement.getEnabled());
 		JsArrayString jsStringLanguages = (JsArrayString) JsArrayString
 				.createArray();
 		JsArrayString jsStringAbbreviations = (JsArrayString) JsArrayString
 				.createArray();
 
 		SortedMap<String, IanaBaseDataDto> ianaLanguages = new TreeMap<String, IanaBaseDataDto>();
 		eventBus.getIanaLanguages(ianaLanguages);
 		for (int j = 0; j < sffElement.getWritingSystems().length(); j++) {
 			SettingFieldsFieldWritingSystemDto writeSystemDto = sffElement
 					.getWritingSystems().get(j);
 			jsStringLanguages.push(writeSystemDto.getId());
 			if (ianaLanguages.containsKey(writeSystemDto.getId())) {
 				jsStringAbbreviations.push(ianaLanguages.get(
 						writeSystemDto.getId()).getSubtag());
 			} else {
 				jsStringAbbreviations.push(writeSystemDto.getId());
 			}
 		}
 
 		fieldSetting.setAbbreviations(jsStringAbbreviations);
 		fieldSetting.setLanguages(jsStringLanguages);
 	}
 
 	public void onFieldSettingsReloadIana() {
 
 		if (view.getFieldsTree().getSelectedItem() != null) {
 			SettingFieldsFieldElementDto fieldElementDto = (SettingFieldsFieldElementDto) (((CheckableItem) view
 					.getFieldsTree().getSelectedItem().getData()).getData());
 
 			fillInputSystems(fieldElementDto);
 		}
 
 	}
 
 	public interface IConfigureSettingFieldsView {
 		public Widget getWidget();
 
 		public FastTree getInputSystemsTree();
 
 		public FastTree getFieldsTree();
 
 		public HasClickHandlers getInputSystemMoveUpClickHandlers();
 
 		public HasClickHandlers getInputSystemMoveDownClickHandlers();
 
 		public ExtendedTextBox getSetupNameTextBox();
 
 		public TabLayoutPanel getFieldDetailTabPanel();
 
 		public ExtendedCheckBox getSetupHideIfEmptyToggleCheckbox();
 	}
 
 	@Override
 	public boolean isMultiUserSupported() {
 		return true;
 	}
 
 	@Override
 	public boolean isPersistDataByParentSupported() {
 		return true;
 	}
 }
