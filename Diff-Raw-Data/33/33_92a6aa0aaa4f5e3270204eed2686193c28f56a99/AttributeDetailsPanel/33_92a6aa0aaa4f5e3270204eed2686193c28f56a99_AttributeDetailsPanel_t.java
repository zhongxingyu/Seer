 /**
  * 
  */
 package org.cotrix.web.manage.client.codelist.common.attribute;
 
 import java.util.Collection;
 
 import org.cotrix.web.common.client.util.ValueUtils;
 import org.cotrix.web.common.client.widgets.AdvancedTextBox;
 import org.cotrix.web.common.client.widgets.LanguageListBox;
 import org.cotrix.web.common.client.widgets.table.CellContainer;
 import org.cotrix.web.common.shared.Language;
 import org.cotrix.web.common.shared.codelist.attributetype.UIAttributeType;
 import org.cotrix.web.common.shared.codelist.linktype.CodeNameValue;
 import org.cotrix.web.manage.client.codelist.cache.AttributeTypesCache;
 import org.cotrix.web.manage.client.codelist.common.DetailsPanelStyle;
 import org.cotrix.web.manage.client.codelist.common.SuggestListBox;
 import org.cotrix.web.manage.client.codelist.common.attribute.AttributeTypeSuggestOracle.AttributeTypeSuggestion;
 import org.cotrix.web.manage.client.resources.CotrixManagerResources;
 
 import com.allen_sauer.gwt.log.client.Log;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.dom.client.Document;
 import com.google.gwt.event.dom.client.KeyUpEvent;
 import com.google.gwt.event.dom.client.KeyUpHandler;
 import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
 import com.google.gwt.event.logical.shared.SelectionEvent;
 import com.google.gwt.event.logical.shared.SelectionHandler;
 import com.google.gwt.event.logical.shared.ValueChangeEvent;
 import com.google.gwt.event.logical.shared.ValueChangeHandler;
 import com.google.gwt.event.shared.HandlerRegistration;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.Image;
 import com.google.gwt.user.client.ui.SuggestOracle;
 import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;
 import com.google.gwt.user.client.ui.TextBox;
 import com.google.gwt.user.client.ui.Widget;
 
 /**
  * @author "Federico De Faveri federico.defaveri@fao.org"
  *
  */
 public class AttributeDetailsPanel extends Composite implements HasValueChangeHandlers<Void>{
 
 	public static final String CODE_NAME_VALUE_TYPE = Document.get().createUniqueId();
 	public static final CodeNameValue CODE_NAME_TYPE = new CodeNameValue();
 
 	private static AttributeDetailsPanelUiBinder uiBinder = GWT.create(AttributeDetailsPanelUiBinder.class);
 
 	interface AttributeDetailsPanelUiBinder extends UiBinder<Widget, AttributeDetailsPanel> {}
 	
 	@UiField(provided=true) SuggestListBox definitionBox;
 	@UiField Image definitionBoxLoader;
 	private UIAttributeType selectedDefinition;
 	@UiField CellContainer definitionRow;
 
 	@UiField AdvancedTextBox nameBox;
 	private boolean nameBoxReadOnly;
 	
 	@UiField(provided=true) SuggestListBox typeBox;
	private boolean typeBoxReadOnly;
 	
 	@UiField AdvancedTextBox descriptionBox;
 	
 	@UiField LanguageListBox languageBox;
 	private boolean languageBoxReadOnly;
 	
 	@UiField TextBox valueBox;
 	
 	private DetailsPanelStyle style = CotrixManagerResources.INSTANCE.detailsPanelStyle();
 	
 	private AttributeTypesCache attributeTypesCache;
 	private AttributeTypeSuggestOracle attributeTypeSuggestOracle;
 	private AttributeDescriptionSuggestOracle attributeDescriptionSuggestOracle;
 	
 	private boolean readOnly = false;
 
 	public AttributeDetailsPanel(AttributeDescriptionSuggestOracle attributeDescriptionSuggestOracle, AttributeTypesCache attributeTypesCache) {
 
 		this.attributeDescriptionSuggestOracle = attributeDescriptionSuggestOracle;
 		typeBox = new SuggestListBox(attributeDescriptionSuggestOracle);
 		
 		this.attributeTypesCache = attributeTypesCache;
 		this.attributeTypeSuggestOracle = new AttributeTypeSuggestOracle();
 		definitionBox = new SuggestListBox(attributeTypeSuggestOracle);
 		
 		initWidget(uiBinder.createAndBindUi(this));
 		
 		setupDefinitionField();
 		setupNameField();
 		setupDescriptionField();
 		setupTypeField();
 		setupLanguageField();
 		setupValueField();
 	}
 	
 	private void setupDefinitionField() {
 		
 		definitionBox.addSelectionHandler(new SelectionHandler<SuggestOracle.Suggestion>() {
 
 			@Override
 			public void onSelection(SelectionEvent<Suggestion> event) {
 				AttributeTypeSuggestion suggestion = (AttributeTypeSuggestion) event.getSelectedItem();
 				if (suggestion == AttributeTypeSuggestOracle.NONE) setDefinitionNone();
 				else setDefinition(suggestion.getAttributeType());
 				fireChange();
 			}
 		});
 	}
 	
 	private void setDefinitionLoader(boolean visible) {
 		definitionBoxLoader.setVisible(visible);
 		definitionBox.setVisible(!visible);
 	}
 	
 	private void loadDefinitions(final String definitionId) {
 		setDefinitionLoader(true);
 		attributeTypesCache.getItems(new AsyncCallback<Collection<UIAttributeType>>() {
 			
 			@Override
 			public void onSuccess(Collection<UIAttributeType> result) {
 				attributeTypeSuggestOracle.loadCache(result);
 				selectDefinition(definitionId);
 				setDefinitionLoader(false);
 			}
 			
 			@Override
 			public void onFailure(Throwable caught) {
 			}
 		});
 	}
 	
 	private void setDefinitionNone() {
 		definitionBox.setValue(AttributeTypeSuggestOracle.NONE.getDisplayString());
 		selectedDefinition = null;
 		
 		setName("");
 		nameBoxReadOnly = false;
 		
 		setLanguage(Language.NONE);
 		languageBoxReadOnly = false;
		
		setType("");
		typeBoxReadOnly = false;
 
		syncDefinibleFields();
 	}
 	
	private void syncDefinibleFields() {
 		setNameReadOnly(readOnly || nameBoxReadOnly);
 		setLanguageReadOnly(readOnly || languageBoxReadOnly);
		setTypeReadOnly(readOnly || typeBoxReadOnly);
 	}
 	
 	private void selectDefinition(String definitionId) {
 		UIAttributeType definition = attributeTypesCache.getItem(definitionId);
 		if (definition == null) setDefinitionNone();
 		else {
 			definitionBox.setValue(AttributeTypeSuggestion.toDisplayString(definition));
 			setDefinition(definition);
 		}
 	}
 	
 	private void setDefinition(UIAttributeType definition) {
 		Log.trace("setting definition to "+definition);
 		
 		selectedDefinition = definition;
 		
 		setName(ValueUtils.getLocalPart(definition.getName()));
 		nameBoxReadOnly = true;
 		
 		setLanguage(definition.getLanguage());
 		languageBoxReadOnly = true;
 		
		setType(ValueUtils.getLocalPart(definition.getType()));
		typeBoxReadOnly = true;
		
 		String currentValue = getValue();
 		String defaultValue = definition.getDefaultValue();
 		
 		if ((currentValue == null || currentValue.isEmpty()) 
 				&& (defaultValue !=null && !defaultValue.isEmpty())) 
 			setValue(defaultValue);
 		
		syncDefinibleFields();
 	}
 	
 	private void setupNameField() {
 
 		nameBox.addValueChangeHandler(new ValueChangeHandler<String>() {
 
 			@Override
 			public void onValueChange(ValueChangeEvent<String> event) {
 				fireChange();
 			}
 		});
 		
 		nameBox.addKeyUpHandler(new KeyUpHandler() {
 			
 			@Override
 			public void onKeyUp(KeyUpEvent event) {
 				fireChange();
 			}
 		});
 	}
 	
 	private void setupTypeField() {
 		typeBox.addSelectionHandler(new SelectionHandler<SuggestOracle.Suggestion>() {
 			
 			@Override
 			public void onSelection(SelectionEvent<Suggestion> event) {
 				fireChange();
 			}
 		});
 		typeBox.addValueChangeHandler(new ValueChangeHandler<String>() {
 
 			@Override
 			public void onValueChange(ValueChangeEvent<String> event) {
 				fireChange();
 			}
 		});
 		
 		typeBox.getValueBox().addKeyUpHandler(new KeyUpHandler() {
 			
 			@Override
 			public void onKeyUp(KeyUpEvent event) {
 				fireChange();
 			}
 		});
 		
 		attributeDescriptionSuggestOracle.setOnlyDefaults(true);
 	}
 	
 	private void setupDescriptionField() {
 
 		descriptionBox.addValueChangeHandler(new ValueChangeHandler<String>() {
 
 			@Override
 			public void onValueChange(ValueChangeEvent<String> event) {
 				fireChange();
 			}
 		});
 		
 		descriptionBox.addKeyUpHandler(new KeyUpHandler() {
 			
 			@Override
 			public void onKeyUp(KeyUpEvent event) {
 				fireChange();
 			}
 		});
 	}
 	
 	private void setupLanguageField() {
 		languageBox.addValueChangeHandler(new ValueChangeHandler<Language>() {
 
 			@Override
 			public void onValueChange(ValueChangeEvent<Language> event) {
 				fireChange();
 			}
 		});
 		
 	}
 	
 	private void setupValueField() {
 		valueBox.addValueChangeHandler(new ValueChangeHandler<String>() {
 
 			@Override
 			public void onValueChange(ValueChangeEvent<String> event) {
 				fireChange();
 			}
 		});
 		
 		valueBox.addKeyUpHandler(new KeyUpHandler() {
 			
 			@Override
 			public void onKeyUp(KeyUpEvent event) {
 				fireChange();
 			}
 		});
 	}
 	
 	public void setDefinitionVisible(boolean visible) {
 		definitionRow.setVisible(visible);
 	}
 	
 	public UIAttributeType getDefinition() {
 		return selectedDefinition;
 	}
 	
 	public void setDefinitionId(String definitionId) {
 		loadDefinitions(definitionId);
 	}
 	
 	public String getName() {
 		return nameBox.getValue();
 	}
 	
 	public void setName(String name) {
 		nameBox.setValue(name, false);
 	}
 	
 	public void setNameFieldValid(boolean valid) {
 		nameBox.setStyleName(style.textboxError(), !valid);
 	}
 	
 	public void setNameReadOnly(boolean readOnly) {
 		nameBox.setEnabled(!readOnly);
 		if (readOnly) nameBox.setStyleName(style.textboxError(), false);
 	}
 	
 	public String getType() {
 		return typeBox.getValue();
 	}
 	
 	public void setType(String type) {
 		typeBox.setValue(type);
 	}
 	
 	public void setTypeFieldValid(boolean valid) {
 		typeBox.setStyleName(style.textboxError(), !valid);
 	}
 	
	public void setTypeReadOnly(boolean readOnly) {
		typeBox.setEnabled(!readOnly);
		if (readOnly) typeBox.setStyleName(style.textboxError(), false);
	}
	
 	public String getDescription() {
 		return descriptionBox.getValue();
 	}
 	
 	public void setDescription(String description) {
 		descriptionBox.setValue(description, false);
 	}
 	
 	public void setDescriptionFieldValid(boolean valid) {
 		descriptionBox.setStyleName(style.textboxError(), !valid);
 	}
 	
 	public Language getLanguage() {
 		return languageBox.getValue();
 	}
 	
 	public void setLanguage(Language language) {
 		languageBox.setValue(language);
 	}
 	
 	public void setLanguageFieldValid(boolean valid) {
 		languageBox.setStyleName(style.textboxError(), !valid);
 	}
 	
 	public String getValue() {
 		return valueBox.getValue();
 	}
 	
 	public void setValue(String value) {
 		valueBox.setValue(value, false);
 	}
 	
 	public void setValueFieldValid(boolean valid) {
 		valueBox.setStyleName(style.textboxError(), !valid);
 	}
 	
 	public void setLanguageReadOnly(boolean readOnly) {
 		languageBox.setEnabled(!readOnly);
 		if (readOnly) languageBox.setStyleName(style.textboxError(), false);
 	}
 
 	public void setReadOnly(boolean readOnly) {
 		
 		this.readOnly = readOnly;
 		
 		definitionBox.setEnabled(!readOnly);
 		if (readOnly) definitionBox.setStyleName(style.textboxError(), false);
 		
 		setNameReadOnly(readOnly);
 		
		setTypeReadOnly(readOnly);
 		
 		descriptionBox.setEnabled(!readOnly);
 		if (readOnly) descriptionBox.setStyleName(style.textboxError(), false);
 		
 		setLanguageReadOnly(readOnly);
 
 		valueBox.setEnabled(!readOnly);
 		if (readOnly) valueBox.setStyleName(style.textboxError(), false);
 		
		if (!readOnly) syncDefinibleFields();
 	}
 
 	private void fireChange() {
 		ValueChangeEvent.fire(this, null);
 	}
 
 	@Override
 	public HandlerRegistration addValueChangeHandler(ValueChangeHandler<Void> handler) {
 		return addHandler(handler, ValueChangeEvent.getType());
 	}
 }
