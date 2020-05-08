 package cl.votainteligente.inspector.client.views;
 
 import cl.votainteligente.inspector.client.i18n.ApplicationMessages;
 import cl.votainteligente.inspector.client.presenters.HomePresenter;
 import cl.votainteligente.inspector.client.presenters.HomePresenter.SelectionType;
 import cl.votainteligente.inspector.client.resources.DisplayCellTableResource;
 import cl.votainteligente.inspector.client.resources.SearchCellTableResource;
 import cl.votainteligente.inspector.client.uihandlers.HomeUiHandlers;
 import cl.votainteligente.inspector.model.Bill;
 import cl.votainteligente.inspector.model.Category;
 import cl.votainteligente.inspector.model.Parlamentarian;
 
 import com.gwtplatform.mvp.client.ViewWithUiHandlers;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.KeyCodes;
 import com.google.gwt.event.dom.client.KeyUpEvent;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.uibinder.client.UiHandler;
 import com.google.gwt.user.cellview.client.CellTable;
 import com.google.gwt.user.client.ui.*;
 
 import javax.inject.Inject;
 
 public class HomeView extends ViewWithUiHandlers<HomeUiHandlers> implements HomePresenter.MyView {
 	private static HomeViewUiBinder uiBinder = GWT.create(HomeViewUiBinder.class);
 	interface HomeViewUiBinder extends UiBinder<Widget, HomeView> {}
 	private final Widget widget;
 
 	@Inject
 	private ApplicationMessages applicationMessages;
 
 	@UiField HTMLPanel parlamentarianPanel;
 	@UiField HTMLPanel parlamentarianContent;
 	@UiField HTMLPanel parlamentarianTableContainer;
 	@UiField Label parliamentarianStatusLight;
 	@UiField TextBox parlamentarianSearch;
 	@UiField Label parlamentarianSearchClear;
 	@UiField Label parliamentarianMessage;
 	@UiField HTMLPanel parliamentarianMessageContent;
 	@UiField Label selectionType;
 	@UiField Label notificationSelectedType;
 	@UiField Label permalink;
 	@UiField HTMLPanel categoryPanel;
 	@UiField HTMLPanel categoryContent;
 	@UiField HTMLPanel categoryTableContainer;
 	@UiField Label categoryStatusLight;
 	@UiField TextBox categorySearch;
 	@UiField Label categorySearchClear;
 	@UiField Label categoryMessage;
 	@UiField HTMLPanel categoryMessageContent;
 	@UiField Image parlamentarianImage;
 	@UiField Label parlamentarianDisplay;
 	@UiField Label parlamentarianProfileLink;
 	@UiField Label categoryDisplay;
 	CellTable<Parlamentarian> parlamentarianTable;
 	CellTable<Category> categoryTable;
 	@UiField HTMLPanel billPanel;
 	@UiField Label billMessage;
 	CellTable<Bill> billTable;
 	@UiField Image parliamentarianConflict;
 	@UiField Image categoryConflict;
 
 	public HomeView() {
 		widget = uiBinder.createAndBindUi(this);
 		ResourceBundle.INSTANCE.HomeView().ensureInjected();
 		SearchCellTableResource searchResource = GWT.create(SearchCellTableResource.class);
 		DisplayCellTableResource displayResource = GWT.create(DisplayCellTableResource.class);
		parlamentarianTable = new CellTable<Parlamentarian>(15, searchResource);
		categoryTable = new CellTable<Category>(15, searchResource);
		billTable = new CellTable<Bill>(15, displayResource);
 		parlamentarianTableContainer.add(parlamentarianTable);
 		categoryTableContainer.add(categoryTable);
 		billPanel.add(billTable);
 	}
 
 	@Override
 	public Widget asWidget() {
 		return widget;
 	}
 
 	@Override
 	public String getParlamentarianSearch() {
 		return parlamentarianSearch.getText();
 	}
 
 	@Override
 	public void setParlamentarianSearch(String parlamentarianSearch) {
 		this.parlamentarianSearch.setText(parlamentarianSearch);
 	}
 
 	@Override
 	public String getCategorySearch() {
 		return categorySearch.getText();
 	}
 
 	@Override
 	public void setCategorySearch(String categorySearch) {
 		this.categorySearch.setText(categorySearch);
 	}
 
 	@Override
 	public CellTable<Parlamentarian> getParlamentarianTable() {
 		return parlamentarianTable;
 	}
 
 	@Override
 	public CellTable<Category> getCategoryTable() {
 		return categoryTable;
 	}
 
 	@Override
 	public CellTable<Bill> getBillTable() {
 		return billTable;
 	}
 
 	@Override
 	public void setParlamentarianDisplay(String parlamentarianName) {
 		parlamentarianDisplay.setText(parlamentarianName);
 	}
 
 	@Override
 	public void setParlamentarianImage(String parlamentarianImage) {
 		this.parlamentarianImage.setUrl(parlamentarianImage);
 	}
 
 	@Override
 	public void setCategoryDisplay(String categoryName) {
 		categoryDisplay.setText(categoryName);
 	}
 
 	@Override
 	public void setSelectedType(SelectionType selectedType) {
 		switch (selectedType) {
 		case SELECTED_PARLAMENTARIAN:
 			selectionType.setStyleName(ResourceBundle.INSTANCE.HomeView().lockedParlamentarian());
 			parliamentarianStatusLight.setStyleName(ResourceBundle.INSTANCE.HomeView().parliamentarianLightOn());
 			categoryStatusLight.setStyleName(ResourceBundle.INSTANCE.HomeView().categoryLightOff());
 			break;
 		case SELECTED_CATEGORY:
 			selectionType.setStyleName(ResourceBundle.INSTANCE.HomeView().lockedCategory());
 			parliamentarianStatusLight.setStyleName(ResourceBundle.INSTANCE.HomeView().parliamentarianLightOff());
 			categoryStatusLight.setStyleName(ResourceBundle.INSTANCE.HomeView().categoryLightOn());
 			break;
 		}
 	}
 
 	@Override
 	public void setParlamentarianMessage(String message) {
 		parliamentarianMessage.setText(message);
 		parliamentarianMessageContent.setVisible(true);
 		parlamentarianContent.setVisible(false);
 	}
 
 	@Override
 	public void hideParlamentarianMessage() {
 		parliamentarianMessageContent.setVisible(false);
 		parlamentarianContent.setVisible(true);
 	}
 
 	@Override
 	public void setCategoryMessage(String message) {
 		categoryMessage.setText(message);
 		categoryMessageContent.setVisible(true);
 		categoryContent.setVisible(false);
 	}
 
 	@Override
 	public void hideCategoryMessage() {
 		categoryMessageContent.setVisible(false);
 		categoryContent.setVisible(true);
 	}
 
 	@Override
 	public void setBillMessage(String message) {
 		billMessage.setText(message);
 		billMessage.setVisible(true);
 	}
 
 	@Override
 	public void hideBillMessage() {
 		billMessage.setVisible(false);
 	}
 
 	@Override
 	public void showBillTable() {
 		billPanel.setVisible(true);
 	}
 
 	@Override
 	public void notificationSelectCategory() {
 		notificationSelectedType.setVisible(true);
 		notificationSelectedType.setStyleName(ResourceBundle.INSTANCE.HomeView().selectionDisplayCategory());
 		notificationSelectedType.setText(applicationMessages.getCategoryNotificationSelectParliamentarian());
 	}
 
 	@Override
 	public void notificationSelectParliamentarian() {
 		notificationSelectedType.setVisible(true);
 		notificationSelectedType.setStyleName(ResourceBundle.INSTANCE.HomeView().selectionDisplayParliamentary());
 		notificationSelectedType.setText(applicationMessages.getParliamentarianNotificationSelectCategory());
 	}
 
 	@Override
 	public void notificationSelectHidden() {
 		notificationSelectedType.setVisible(false);
 	}
 
 	@Override
 	public void displaySelectionNone() {
 		billPanel.setVisible(false);
 		parlamentarianProfileLink.setVisible(false);
 		parlamentarianDisplay.setStyleName(ResourceBundle.INSTANCE.HomeView().selectionDisplayNone());
 		categoryDisplay.setStyleName(ResourceBundle.INSTANCE.HomeView().selectionDisplayNone());
 	}
 
 	@Override
 	public void displaySelectionParliamentarian() {
 		parlamentarianProfileLink.setVisible(true);
 		parlamentarianDisplay.setStyleName(ResourceBundle.INSTANCE.HomeView().selectedParlamentarian());
 	}
 
 	@Override
 	public void displaySelectionCategory() {
 		categoryDisplay.setStyleName(ResourceBundle.INSTANCE.HomeView().selectedCategory());
 	}
 
 	@Override
 	public void showPermalink() {
 		permalink.setStyleName(ResourceBundle.INSTANCE.HomeView().permalinkLabelOn());
 	}
 
 	@Override
 	public void hidePermalink() {
 		permalink.setStyleName(ResourceBundle.INSTANCE.HomeView().permalinkLabelOff());
 	}
 
 	@Override
 	public void showParliamentarianConflictImage() {
 		parliamentarianConflict.setVisible(true);
 		parliamentarianConflict.setUrl("images/without_conflict.png");
 	}
 
 	@Override
 	public void showCategoryConflictImage() {
 		categoryConflict.setVisible(true);
 		categoryConflict.setUrl("images/without_conflict.png");
 	}
 
 	@Override
 	public void hideConflictImage() {
 		parliamentarianConflict.setVisible(false);
 		categoryConflict.setVisible(false);
 	}
 
 	@UiHandler("parlamentarianSearch")
 	public void onParlamentarianSearchKeyUp(KeyUpEvent event) {
 		if ((event.getNativeKeyCode() >= 48 && event.getNativeKeyCode() <= 57) ||
 			(event.getNativeKeyCode() >= 65 && event.getNativeKeyCode() <= 90) ||
 			(event.getNativeKeyCode() >= 97 && event.getNativeKeyCode() <= 122)||
 			event.getNativeKeyCode() == KeyCodes.KEY_BACKSPACE) {
 			getUiHandlers().searchParlamentarian(parlamentarianSearch.getText());
 			categorySearch.setText(applicationMessages.getCategorySearchMessage());
 		}
 	}
 
 	@UiHandler("parlamentarianSearch")
 	public void onParlamentarianSearchClick(ClickEvent event) {
 		if (parlamentarianSearch.getText().equals(applicationMessages.getParlamentarianSearchMessage())) {
 			parlamentarianSearch.setText("");
 		}
 	}
 
 	@UiHandler("parlamentarianSearchClear")
 	public void onParlamentarianSearchClearClick(ClickEvent event) {
 		parlamentarianSearch.setText("");
 		getUiHandlers().searchCleaner();
 	}
 
 	@UiHandler("categorySearch")
 	public void onCategorySearchKeyUp(KeyUpEvent event) {
 		if ((event.getNativeKeyCode() >= 48 && event.getNativeKeyCode() <= 57) ||
 			(event.getNativeKeyCode() >= 65 && event.getNativeKeyCode() <= 90) ||
 			(event.getNativeKeyCode() >= 97 && event.getNativeKeyCode() <= 122)||
 			event.getNativeKeyCode() == KeyCodes.KEY_BACKSPACE) {
 			getUiHandlers().searchCategory(categorySearch.getText());
 			parlamentarianSearch.setText(applicationMessages.getParlamentarianSearchMessage());
 		}
 	}
 
 	@UiHandler("categorySearch")
 	public void onCategorySearchClick(ClickEvent event) {
 		if (categorySearch.getText().equals(applicationMessages.getCategorySearchMessage())) {
 			categorySearch.setText("");
 		}
 	}
 
 	@UiHandler("categorySearchClear")
 	public void onCategorySearchClearClick(ClickEvent event) {
 		categorySearch.setText("");
 		getUiHandlers().searchCleaner();
 	}
 
 	@UiHandler("parlamentarianProfileLink")
 	public void onClickParlamentarianProfileLink(ClickEvent event) {
 		getUiHandlers().showParlamentarianProfile();
 	}
 
 	@UiHandler("selectionType")
 	public void onSelectionTypeClick(ClickEvent event) {
 		getUiHandlers().switchSelectionType();
 	}
 
 	@UiHandler("permalink")
 	public void onPermalinkClick(ClickEvent event) {
 		getUiHandlers().getPermalink();
 	}
 }
