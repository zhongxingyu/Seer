 package burrito.client.crud.input;
 
 import burrito.client.crud.CrudServiceAsync;
 import burrito.client.crud.generic.CrudEntityDescription;
 import burrito.client.crud.generic.CrudField;
 import burrito.client.crud.generic.fields.ManyToOneRelationField;
 import burrito.client.crud.labels.CrudLabelHelper;
 import burrito.client.crud.labels.CrudMessages;
 import burrito.client.crud.widgets.SearchSelectWidget;
 import burrito.client.crud.widgets.SearchSelectWidget.SelectHandler;
 import burrito.client.widgets.validation.HasValidators;
 import burrito.client.widgets.validation.InputFieldValidator;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.Anchor;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.FlowPanel;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.Widget;
 
 public class SearchListField extends Composite implements CrudInputField, HasValidators {
 
 	private static CrudMessages labels = GWT.create(CrudMessages.class);
 	
 	private final ManyToOneRelationField relationField;
 	private final CrudServiceAsync service;
 	private FlowPanel wrapper;
 	private Long selectedId;
 	private Label selectedLabel;
 	private Anchor clearAnchor;
 	private Label errorLabel;
 	private Anchor changeAnchor;
 
 	private String entityDisplayName;
 	
 	public SearchListField(final ManyToOneRelationField relationField, final CrudServiceAsync service) {
 		this.relationField = relationField;
 		this.service = service;
 		this.selectedId = (Long) relationField.getValue();
 		
 		final String relatedEntityName = relationField.getRelatedEntityName();
 		entityDisplayName = CrudLabelHelper.getString(relatedEntityName.replace('.', '_')).toLowerCase();
 		
 		wrapper = new FlowPanel();
 		
 		selectedLabel = new Label("");
 		selectedLabel.addStyleName("selectedInfo");
 		
 		clearAnchor = new Anchor("X");
 		clearAnchor.addStyleName("clearButton");
 		clearAnchor.addClickHandler(new ClickHandler() {
 			@Override
 			public void onClick(ClickEvent event) {
 				selectedId = null;	
 				setDescribingLable(selectedId, entityDisplayName, relatedEntityName);
 			}
 		});
 		clearAnchor.setVisible(false);
 		
 		setDescribingLable(selectedId, entityDisplayName, relatedEntityName);
 		
 		
 		changeAnchor = new Anchor(labels.selectEntity(entityDisplayName));
 		changeAnchor.addStyleName("searchListItem");
 		changeAnchor.addClickHandler(new ClickHandler() {
 			@Override
 			public void onClick(ClickEvent event) {
 				final SearchSelectWidget popup = new SearchSelectWidget(service, relationField);
 				popup.setPopupPosition(Window.getClientWidth() / 2, Window.getClientHeight() /2);
 				popup.setSelectHandler(new SelectHandler() {
 					@Override
 					public void onSelect(Long id) {
 						selectedId = id;
 						setDescribingLable(selectedId, entityDisplayName, relatedEntityName);
 						
 						popup.hide();
 					}
 				});
 				popup.show();
 			}
 		});
 		
 		errorLabel = new Label(labels.embeddedItemAtLeastOne(entityDisplayName));
 		errorLabel.addStyleName("error searchListItem");
 		errorLabel.setVisible(false);		
 		
 		HorizontalPanel entityWrapper = new HorizontalPanel();
 		entityWrapper.addStyleName("entityInfo searchListItem");
 		entityWrapper.add(selectedLabel);
 		entityWrapper.add(clearAnchor);
 		wrapper.add(entityWrapper);
 		
 		
 		wrapper.add(changeAnchor);
 		wrapper.add(errorLabel);
 		
 		initWidget(wrapper);
 		addStyleName("searchList");
 	}
 	
 	private void setDescribingLable(Long entityId, String entityDisplayName, String relatedEntityName) {
 		if (entityId == null) {
 			selectedLabel.setText(labels.noEmbeddedItemsAdded(entityDisplayName));
 			clearAnchor.setVisible(false);
 			return;
 		}
 		
 		service.describe(relatedEntityName, entityId, null, new AsyncCallback<CrudEntityDescription>() {
 			@Override
 			public void onSuccess(CrudEntityDescription result) {
 				selectedLabel.setText(result.getDisplayString());
 				clearAnchor.setVisible(true);
 			}
 			
 			@Override
 			public void onFailure(Throwable caught) {
 				
 			}
 		});
 	}
 
 	
 	@Override
 	public Widget getDisplayWidget() {
 		return this;
 	}
 
 	@Override
 	public void load(Object value) {
 		selectedId = (Long) value;
 	}
 
 	@Override
 	public Object getValue() {
 		return selectedId;
 	}
 
 	@Override
 	public CrudField getCrudField() {
 		relationField.setValue(selectedId);
 		return relationField;
 	}
 
 	@Override
 	public void addInputFieldValidator(InputFieldValidator validator) {
 		//
 	}
 
 	@Override
 	public boolean validate() {
 		boolean valid = selectedId != null;
 		if (valid) {
 			errorLabel.setVisible(false);
 		} else {
 			errorLabel.setVisible(true);
 		}
 		
 		return valid;
 	}
 
 	@Override
 	public void setValidationError(String validationError) {
 		//
 	}
 }
