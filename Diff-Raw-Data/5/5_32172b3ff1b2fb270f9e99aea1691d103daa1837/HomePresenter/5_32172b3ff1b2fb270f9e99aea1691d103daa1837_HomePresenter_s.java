 package cl.votainteligente.inspector.client.presenters;
 
 import cl.votainteligente.inspector.client.i18n.ApplicationMessages;
 import cl.votainteligente.inspector.client.services.BillServiceAsync;
 import cl.votainteligente.inspector.client.services.CategoryServiceAsync;
 import cl.votainteligente.inspector.client.services.ParlamentarianServiceAsync;
 import cl.votainteligente.inspector.client.uihandlers.HomeUiHandlers;
 import cl.votainteligente.inspector.model.Bill;
 import cl.votainteligente.inspector.model.Category;
 import cl.votainteligente.inspector.model.Parlamentarian;
 import cl.votainteligente.inspector.shared.NotificationEvent;
 import cl.votainteligente.inspector.shared.NotificationEventParams;
 import cl.votainteligente.inspector.shared.NotificationEventType;
 
 import com.gwtplatform.mvp.client.HasUiHandlers;
 import com.gwtplatform.mvp.client.Presenter;
 import com.gwtplatform.mvp.client.View;
 import com.gwtplatform.mvp.client.annotations.NameToken;
 import com.gwtplatform.mvp.client.annotations.ProxyStandard;
 import com.gwtplatform.mvp.client.proxy.*;
 
 import com.google.gwt.cell.client.ActionCell;
 import com.google.gwt.cell.client.Cell;
 import com.google.gwt.cell.client.ImageCell;
 import com.google.gwt.event.shared.EventBus;
 import com.google.gwt.safehtml.shared.SafeHtml;
 import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
 import com.google.gwt.user.cellview.client.CellTable;
 import com.google.gwt.user.cellview.client.Column;
 import com.google.gwt.user.cellview.client.TextColumn;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.view.client.*;
 import com.google.inject.Inject;
 
 import java.util.*;
 
 public class HomePresenter extends Presenter<HomePresenter.MyView, HomePresenter.MyProxy> implements HomeUiHandlers {
 	public static final String PLACE = "home";
 	public static final String PARAM_PARLAMENTARIAN_ID = "parlamentarianId";
 	public static final String PARAM_CATEGORY_ID = "categoryId";
 
 	public interface MyView extends View, HasUiHandlers<HomeUiHandlers> {
 		String getParlamentarianSearch();
 		void setParlamentarianSearch(String parlamentarianSearch);
 		String getCategorySearch();
 		void setCategorySearch(String categorySearch);
 		CellTable<Parlamentarian> getParlamentarianTable();
 		CellTable<Category> getCategoryTable();
 		CellTable<Bill> getBillTable();
 		void setParlamentarianDisplay(String parlamentarianName);
 		void setCategoryDisplay(String categoryName);
 		void setParlamentarianImage(String parlamentarianImage);
 		void setSelectedType(SelectionType selectedType);
 		void setParlamentarianMessage(String message);
 		void hideParlamentarianMessage();
 		void setCategoryMessage(String message);
 		void hideCategoryMessage();
 		void setBillMessage(String message);
 		void hideBillMessage();
 		void showBillTable();
 		void notificationSelectCategory();
 		void notificationSelectParliamentarian();
 		void notificationSelectHidden();
 		void displaySelectionNone();
 		void displaySelectionParliamentarian();
 		void displaySelectionCategory();
 		void showPermalink();
 		void hidePermalink();
 		void showParliamentarianConflictImage();
 		void showCategoryConflictImage();
 		void hideConflictImage();
 	}
 
 	public enum SelectionType {
 		SELECTED_PARLAMENTARIAN,
 		SELECTED_CATEGORY
 	}
 
 	@ProxyStandard
 	@NameToken(PLACE)
 	public interface MyProxy extends ProxyPlace<HomePresenter> {
 	}
 
 	@Inject
 	private ApplicationMessages applicationMessages;
 	@Inject
 	private PlaceManager placeManager;
 	@Inject
 	private ParlamentarianServiceAsync parlamentarianService;
 	@Inject
 	private CategoryServiceAsync categoryService;
 	@Inject
 	private BillServiceAsync billService;
 
 	private AbstractDataProvider<Parlamentarian> parlamentarianData;
 	private List<Parlamentarian> parlamentarianFetchedData;
 	private AbstractDataProvider<Category> categoryData;
 	private List<Category> categoryFetchedData;
 	private AbstractDataProvider<Bill> billData;
 	private Parlamentarian selectedParlamentarian;
 	private Category selectedCategory;
 	private SelectionType selectedType;
 	private Long parlamentarianId;
 	private Long categoryId;
 
 	@Inject
 	public HomePresenter(EventBus eventBus, MyView view, MyProxy proxy) {
 		super(eventBus, view, proxy);
 		getView().setUiHandlers(this);
 	}
 
 	@Override
 	protected void onReset() {
 	}
 
 	@Override
 	protected void onReveal() {
 		parlamentarianData = new ListDataProvider<Parlamentarian>();
 		categoryData = new ListDataProvider<Category>();
 		billData = new ListDataProvider<Bill>();
 		cleanerSelection();
 		resetSelection();
 		setupSelection(SelectionType.SELECTED_PARLAMENTARIAN);
 		initParlamentarianTable();
 		initCategoryTable();
 		initBillTable();
 		initDataLoad();
 		getView().displaySelectionNone();
 	}
 
 	@Override
 	protected void revealInParent() {
 		fireEvent(new RevealContentEvent(MainPresenter.SLOT_MAIN_CONTENT, this));
 	}
 
 	@Override
 	public void prepareFromRequest(PlaceRequest placeRequest) {
 		super.prepareFromRequest(placeRequest);
 
 		try {
 			parlamentarianId = Long.parseLong(placeRequest.getParameter(PARAM_PARLAMENTARIAN_ID, null));
 		} catch (NumberFormatException nfe) {
 			parlamentarianId = null;
 		}
 
 		try {
 			categoryId = Long.parseLong(placeRequest.getParameter(PARAM_CATEGORY_ID, null));
 		} catch (NumberFormatException nfe) {
 			categoryId = null;
 		}
 	}
 
 	public void initDataLoad() {
 		parlamentarianService.getAllParlamentarians(new AsyncCallback<List<Parlamentarian>>() {
 
 			@Override
 			public void onFailure(Throwable caught) {
 				Window.alert(applicationMessages.getErrorParlamentarianList());
 			}
 
 			@Override
 			public void onSuccess(List<Parlamentarian> result) {
 				if (result != null) {
 					parlamentarianFetchedData = result;
 					ListDataProvider<Parlamentarian> data = new ListDataProvider<Parlamentarian>(parlamentarianFetchedData);
 					setParlamentarianData(data);
 					if (parlamentarianId != null) {
 						for (Parlamentarian parlamentarian : result) {
 							if (parlamentarian.getId().equals(parlamentarianId)) {
 								selectedParlamentarian = parlamentarian;
 								getView().getParlamentarianTable().getSelectionModel().setSelected(selectedParlamentarian, true);
 								break;
 							}
 						}
 						setupSelection(SelectionType.SELECTED_PARLAMENTARIAN);
 						if (selectedParlamentarian != null) {
 							List<Parlamentarian> parlamentarians = new ArrayList<Parlamentarian>();
 							parlamentarians.add(selectedParlamentarian);
 							data.setList(parlamentarians);
 							getView().getParlamentarianTable().getSelectionModel().setSelected(selectedParlamentarian, true);
 						}
 						// TODO: add else and send invalid parlamentarian notice
 					}
 				}
 			}
 		});
 
 		categoryService.getAllCategories(new AsyncCallback<List<Category>>() {
 
 			@Override
 			public void onFailure(Throwable caught) {
 				Window.alert(applicationMessages.getErrorCategoryList());
 			}
 
 			@Override
 			public void onSuccess(List<Category> result) {
 				if (result != null) {
 					categoryFetchedData = result;
 					ListDataProvider<Category> data = new ListDataProvider<Category>(categoryFetchedData);
 					setCategoryData(data);
 					if (categoryId != null && parlamentarianId == null) {
 						for (Category category : result) {
 							if (category.getId().equals(categoryId)) {
 								selectedCategory = category;
 								break;
 							}
 						}
 						setupSelection(SelectionType.SELECTED_CATEGORY);
 						if (selectedCategory != null) {
 							List<Category> categories = new ArrayList<Category>();
 							categories.add(selectedCategory);
 							data.setList(categories);
 							getView().getCategoryTable().getSelectionModel().setSelected(selectedCategory, true);
 						}
 						// TODO: add else and send invalid category notice
 					}
 				}
 			}
 		});
 		setBillTable();
 		getView().hideParlamentarianMessage();
 		getView().hideCategoryMessage();
 		getView().hideBillMessage();
 		getView().displaySelectionNone();
 		getView().notificationSelectHidden();
 	}
 
 	@Override
 	public void searchParlamentarian(String keyWord) {
 		getView().hideParlamentarianMessage();
 		getView().setParlamentarianDisplay(applicationMessages.getGeneralParlamentarian());
 		getView().setParlamentarianImage("images/parlamentarian/large/avatar.png");
 		if (selectedParlamentarian != null) {
 			getView().getParlamentarianTable().getSelectionModel().setSelected(selectedParlamentarian, false);
 			selectedParlamentarian = null;
 		}
 
 		if (keyWord == null || keyWord.length() == 0 || keyWord.equals("")) {
 			if (selectedType.equals(SelectionType.SELECTED_PARLAMENTARIAN)) {
 				ListDataProvider<Parlamentarian> data = new ListDataProvider<Parlamentarian>(parlamentarianFetchedData);
 				setParlamentarianData(data);
 			} else if (selectedType.equals(SelectionType.SELECTED_CATEGORY)) {
 				searchParlamentarian(selectedCategory);
 			}
 		} else if (keyWord.length() < 2) {
 			return;
 		} else {
 			String[] keyWordArray = keyWord.split("[ ]");
 
 			for (int i = 0; i < keyWordArray.length; i++) {
				keyWordArray[i] = keyWordArray[i].replaceAll("[^\\p{L}\\p{N}]", "");
 				keyWordArray[i] = keyWordArray[i].replaceAll("[ÄÁÀAäáàa]","[äáàa]");
 				keyWordArray[i] = keyWordArray[i].replaceAll("[ËÉÈEëéèe]","[ëéèe]");
 				keyWordArray[i] = keyWordArray[i].replaceAll("[ÏÍÌIïíìi]","[ïíìi]");
 				keyWordArray[i] = keyWordArray[i].replaceAll("[ÖÓÒOöóòo]","[öóòo]");
 				keyWordArray[i] = keyWordArray[i].replaceAll("[ÜÚÙUüúùu]","[üúùu]");
 				keyWordArray[i] = keyWordArray[i].replaceAll("[ÑNñn]", "[ñn]");
 				keyWordArray[i] = keyWordArray[i].toLowerCase();
 			}
 
 			List<String> keyWordList = new ArrayList<String>(Arrays.asList(keyWordArray));
 			String keyWordPattern = "(";
 			Iterator<String> keyWordIterator = keyWordList.iterator();
 
 			while (keyWordIterator.hasNext()) {
 				keyWordPattern += keyWordIterator.next();
 				if (keyWordIterator.hasNext()) {
 					keyWordPattern += "|";
 				}
 			}
 			keyWordPattern += ")";
 
 			List<Parlamentarian> result = new ArrayList<Parlamentarian>();
 
 			for (Parlamentarian parlamentarian : parlamentarianFetchedData) {
 				if (parlamentarian.getFirstName().toLowerCase().matches(".*"+ keyWordPattern + ".*")) {
 					result.add(parlamentarian);
 				} else {
 					if (parlamentarian.getLastName().toLowerCase().matches(".*"+ keyWordPattern + ".*")) {
 						result.add(parlamentarian);
 					}
 				}
 			}
 
 			Collections.sort(result, new Comparator<Parlamentarian>() {
 
 				@Override
 				public int compare(Parlamentarian o1, Parlamentarian o2) {
 					return o1.compareTo(o2);
 				}
 			});
 
 			ListDataProvider<Parlamentarian> data = new ListDataProvider<Parlamentarian>(result);
 			setParlamentarianData(data);
 
 			if (result.size() == 0) {
 				resetSelection();
 				resetNoConflicts();
 				getView().setParlamentarianMessage(applicationMessages.getGeneralNoMatches());
 			}
 		}
 	}
 
 	@Override
 	public void searchParlamentarian(Category category) {
 		getView().hideParlamentarianMessage();
 		getView().setParlamentarianDisplay(applicationMessages.getGeneralParlamentarian());
 		getView().setParlamentarianImage("images/parlamentarian/large/avatar.png");
 		if (selectedParlamentarian != null) {
 			getView().getParlamentarianTable().getSelectionModel().setSelected(selectedParlamentarian, false);
 			selectedParlamentarian = null;
 		}
 
 		parlamentarianService.searchParlamentarian(category, new AsyncCallback<List<Parlamentarian>>() {
 
 			@Override
 			public void onFailure(Throwable caught) {
 				Window.alert(applicationMessages.getErrorParlamentarianCategorySearch());
 			}
 
 			@Override
 			public void onSuccess(List<Parlamentarian> result) {
 				if (result != null) {
 					if (result.size() > 0) {
 						parlamentarianFetchedData = result;
 						ListDataProvider<Parlamentarian> data = new ListDataProvider<Parlamentarian>(parlamentarianFetchedData);
 						setParlamentarianData(data);
 					}
 					if (selectedCategory != null && result.size() == 0) {
 						getView().showParliamentarianConflictImage();
 						getView().setParlamentarianMessage(applicationMessages.getGeneralNoConflictCategory());
 					}
 				}
 			}
 		});
 	}
 
 	@Override
 	public void searchCategory(String keyWord) {
 		getView().hideCategoryMessage();
 		getView().setCategoryDisplay(applicationMessages.getGeneralCategory());
 		if (selectedCategory != null) {
 			getView().getCategoryTable().getSelectionModel().setSelected(selectedCategory, false);
 			selectedCategory = null;
 		}
 
 		if (keyWord == null || keyWord.length() == 0 || keyWord.equals("")) {
 			if (selectedType.equals(SelectionType.SELECTED_CATEGORY)) {
 				ListDataProvider<Category> data = new ListDataProvider<Category>(categoryFetchedData);
 				setCategoryData(data);
 			} else if (selectedType.equals(SelectionType.SELECTED_PARLAMENTARIAN)) {
 				searchCategory(selectedParlamentarian);
 			}
 		} else if (keyWord.length() < 2) {
 			return;
 		} else {
 			String[] keyWordArray = keyWord.split("[ ]");
 
 			for (int i = 0; i < keyWordArray.length; i++) {
				keyWordArray[i] = keyWordArray[i].replaceAll("[^\\p{L}\\p{N}]", "");
 				keyWordArray[i] = keyWordArray[i].replaceAll("[ÄÁÀAäáàa]","[äáàa]");
 				keyWordArray[i] = keyWordArray[i].replaceAll("[ËÉÈEëéèe]","[ëéèe]");
 				keyWordArray[i] = keyWordArray[i].replaceAll("[ÏÍÌIïíìi]","[ïíìi]");
 				keyWordArray[i] = keyWordArray[i].replaceAll("[ÖÓÒOöóòo]","[öóòo]");
 				keyWordArray[i] = keyWordArray[i].replaceAll("[ÜÚÙUüúùu]","[üúùu]");
 				keyWordArray[i] = keyWordArray[i].replaceAll("[ÑNñn]", "[ñn]");
 				keyWordArray[i] = keyWordArray[i].toLowerCase();
 			}
 
 			List<String> keyWordList = new ArrayList<String>(Arrays.asList(keyWordArray));
 			String keyWordPattern = "(";
 			Iterator<String> keyWordIterator = keyWordList.iterator();
 
 			while (keyWordIterator.hasNext()) {
 				keyWordPattern += keyWordIterator.next();
 				if (keyWordIterator.hasNext()) {
 					keyWordPattern += "|";
 				}
 			}
 			keyWordPattern += ")";
 
 			List<Category> result = new ArrayList<Category>();
 
 			for (Category category : categoryFetchedData) {
 				if (category.getName().toLowerCase().matches(".*"+ keyWordPattern + ".*")) {
 					result.add(category);
 				}
 			}
 
 			Collections.sort(result, new Comparator<Category>() {
 
 				@Override
 				public int compare(Category o1, Category o2) {
 					return o1.compareTo(o2);
 				}
 			});
 
 			ListDataProvider<Category> data = new ListDataProvider<Category>(result);
 			setCategoryData(data);
 
 			if (result.size() == 0) {
 				resetSelection();
 				resetNoConflicts();
 				getView().setCategoryMessage(applicationMessages.getGeneralNoMatches());
 			}
 		}
 	}
 
 	@Override
 	public void searchCategory(Parlamentarian parlamentarian) {
 		getView().hideCategoryMessage();
 		getView().setCategoryDisplay(applicationMessages.getGeneralCategory());
 		if (selectedCategory != null) {
 			getView().getCategoryTable().getSelectionModel().setSelected(selectedCategory, false);
 			selectedCategory = null;
 		}
 
 		categoryService.searchCategory(parlamentarian, new AsyncCallback<List<Category>>() {
 
 			@Override
 			public void onFailure(Throwable caught) {
 				Window.alert(applicationMessages.getErrorCategoryParlamentarianSearch());
 			}
 
 			@Override
 			public void onSuccess(List<Category> result) {
 				if (result != null) {
 					categoryFetchedData = result;
 					ListDataProvider<Category> data = new ListDataProvider<Category>(categoryFetchedData);
 					setCategoryData(data);
 					if (categoryId != null && parlamentarianId != null) {
 						for (Category category : result) {
 							if (category.getId().equals(categoryId)) {
 								selectedCategory = category;
 								break;
 							}
 						}
 						if (selectedCategory != null && result.size() > 0) {
 							List<Category> categories = new ArrayList<Category>();
 							categories.add(selectedCategory);
 							data.setList(categories);
 							getView().getCategoryTable().getSelectionModel().setSelected(selectedCategory, true);
 						}
 						// TODO: add else and send invalid category notice
 					}
 					if (result.size() == 0) {
 						getView().showCategoryConflictImage();
 						getView().setCategoryMessage(applicationMessages.getGeneralNoConflictParliamentarian());
 					}
 				}
 			}
 		});
 	}
 
 	@Override
 	public void searchBill(Long parlamentarianId, Long categoryId) {
 		getView().hideBillMessage();
 		billService.searchBills(parlamentarianId, categoryId, new AsyncCallback<List<Bill>>() {
 
 			@Override
 			public void onFailure(Throwable caught) {
 				Window.alert(applicationMessages.getErrorBillList());
 			}
 
 			@Override
 			public void onSuccess(List<Bill> result) {
 				if (result != null) {
 					ListDataProvider<Bill> data = new ListDataProvider<Bill>(result);
 					setBillData(data);
 					if (result.size() == 0) {
 						getView().setBillMessage(applicationMessages.getGeneralNoMatches());
 					}
 				}
 			}
 		});
 	}
 
 	public void getParlamentarian(Parlamentarian parlamentarian) {
 		parlamentarianService.getParlamentarian(selectedParlamentarian.getId(), new AsyncCallback<Parlamentarian>() {
 
 			@Override
 			public void onFailure(Throwable caught) {
 				Window.alert(applicationMessages.getGeneralParlamentarian());
 			}
 
 			@Override
 			public void onSuccess(Parlamentarian result) {
 				if (result != null) {
 					selectedParlamentarian = result;
 					setBillTable();
 				}
 			}
 
 		});
 	}
 
 	public AbstractDataProvider<Parlamentarian> getParlamentarianData() {
 		return parlamentarianData;
 	}
 
 	public void setParlamentarianData(AbstractDataProvider<Parlamentarian> data) {
 		parlamentarianData = data;
 		parlamentarianData.addDataDisplay(getView().getParlamentarianTable());
 	}
 
 	public AbstractDataProvider<Category> getCategoryData() {
 		return categoryData;
 	}
 
 	public void setCategoryData(AbstractDataProvider<Category> data) {
 		categoryData = data;
 		categoryData.addDataDisplay(getView().getCategoryTable());
 	}
 
 	public AbstractDataProvider<Bill> getBillData() {
 		return billData;
 	}
 
 	public void setBillData(AbstractDataProvider<Bill> data) {
 		billData = data;
 		billData.addDataDisplay(getView().getBillTable());
 	}
 
 	public void initParlamentarianTable() {
 		while (getView().getParlamentarianTable().getColumnCount() > 0) {
 			getView().getParlamentarianTable().removeColumn(0);
 		}
 
 		// Creates image column
 		Column<Parlamentarian, String> imageColumn = new Column<Parlamentarian, String>(new ImageCell()){
 			@Override
 			public String getValue(Parlamentarian parlamentarian) {
 				if (parlamentarian.getImage() != null) {
 					return "images/parlamentarian/small/" + parlamentarian.getImage();
 				} else {
 					return "images/parlamentarian/small/avatar.png";
 				}
 			}
 		};
 
 		// Adds name column to table
 		getView().getParlamentarianTable().addColumn(imageColumn, "");
 
 		// Creates name column
 		TextColumn<Parlamentarian> nameColumn = new TextColumn<Parlamentarian>() {
 			@Override
 			public String getValue(Parlamentarian parlamentarian) {
 				return parlamentarian.toString();
 			}
 		};
 
 		// Adds name column to table
 		getView().getParlamentarianTable().addColumn(nameColumn, applicationMessages.getGeneralParlamentarian());
 
 		// Creates party column
 		TextColumn<Parlamentarian> partyColumn = new TextColumn<Parlamentarian>() {
 			@Override
 			public String getValue(Parlamentarian parlamentarian) {
 				return parlamentarian.getParty().getName();
 			}
 		};
 
 		// Adds party column to table
 		getView().getParlamentarianTable().addColumn(partyColumn, applicationMessages.getGeneralParty());
 
 		// Creates action profile column
 		Column<Parlamentarian, Parlamentarian> profileColumn = new Column<Parlamentarian, Parlamentarian>(new ActionCell<Parlamentarian>("", new ActionCell.Delegate<Parlamentarian>() {
 
 			@Override
 			public void execute(Parlamentarian parlamentarian) {
 				PlaceRequest placeRequest = new PlaceRequest(ParlamentarianPresenter.PLACE);
 				placeManager.revealPlace(placeRequest.with(ParlamentarianPresenter.PARAM_PARLAMENTARIAN_ID, parlamentarian.getId().toString()));
 			}
 		}) {
 			@Override
 			public void render(Cell.Context context, Parlamentarian value, SafeHtmlBuilder sb) {
 				sb.append(new SafeHtml() {
 
 					@Override
 					public String asString() {
 						return "<div class=\"profileButton\"></div>";
 					}
 				});
 			}
 		}) {
 
 			@Override
 			public Parlamentarian getValue(Parlamentarian parlamentarian) {
 				return parlamentarian;
 			}
 		};
 
 		// Adds action profile column to table
 		getView().getParlamentarianTable().addColumn(profileColumn, applicationMessages.getGeneralProfile());
 
 		// Sets selection model for each row
 		final SingleSelectionModel<Parlamentarian> selectionModel = new SingleSelectionModel<Parlamentarian>(Parlamentarian.KEY_PROVIDER);
 		getView().getParlamentarianTable().setSelectionModel(selectionModel);
 		selectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
 			public void onSelectionChange(SelectionChangeEvent event) {
 
 				if (selectionModel.getSelectedObject() != null) {
 					if (selectedType.equals(SelectionType.SELECTED_PARLAMENTARIAN)) {
 						selectedParlamentarian = selectionModel.getSelectedObject();
 						searchCategory(selectedParlamentarian);
 						getView().notificationSelectParliamentarian();
 						getView().displaySelectionNone();
 						getView().displaySelectionParliamentarian();
 					}
 					if (selectedType.equals(SelectionType.SELECTED_CATEGORY) && selectedCategory == null) {
 						getView().getParlamentarianTable().getSelectionModel().setSelected(selectionModel.getSelectedObject(), false);
 						selectedCategory = null;
 					} else {
 						selectedParlamentarian = selectionModel.getSelectedObject();
 						getView().setParlamentarianDisplay(selectedParlamentarian.toString());
 						getParlamentarian(selectedParlamentarian);
 					}
 					if (selectedParlamentarian == null || selectedParlamentarian.getImage() == null) {
 						getView().setParlamentarianImage("images/parlamentarian/large/avatar.png");
 					} else {
 						getView().setParlamentarianImage("images/parlamentarian/large/" + selectedParlamentarian.getImage());
 					}
 					showPermalink();
 				}
 			}
 		});
 	}
 
 	public void initCategoryTable() {
 		while (getView().getCategoryTable().getColumnCount() > 0) {
 			getView().getCategoryTable().removeColumn(0);
 		}
 
 		// Creates name column
 		TextColumn<Category> nameColumn = new TextColumn<Category>() {
 			@Override
 			public String getValue(Category category) {
 				return category.getName();
 			}
 		};
 
 		// Adds name column to table
 		getView().getCategoryTable().addColumn(nameColumn, applicationMessages.getGeneralCategory());
 
 		// Creates action suscription column
 		Column<Category, Category> suscriptionColumn = new Column<Category, Category>(new ActionCell<Category>("", new ActionCell.Delegate<Category>() {
 
 			@Override
 			public void execute(Category category) {
 				// TODO: add category suscription servlet
 				PlaceRequest placeRequest = new PlaceRequest(SubscriptionPresenter.PLACE);
 				placeManager.revealPlace(placeRequest.with(SubscriptionPresenter.PARAM_CATEGORY_ID, category.getId().toString()));
 			}
 		}) {
 			@Override
 			public void render(Cell.Context context, Category value, SafeHtmlBuilder sb) {
 				sb.append(new SafeHtml() {
 
 					@Override
 					public String asString() {
 						return "<div class=\"suscribeButtonCategory\"></div>";
 					}
 				});
 			}
 		}) {
 
 			@Override
 			public Category getValue(Category category) {
 				return category;
 			}
 		};
 
 		// Adds action suscription column to table
 		getView().getCategoryTable().addColumn(suscriptionColumn, applicationMessages.getGeneralSubscribe());
 
 		// Sets selection model for each row
 		final SingleSelectionModel<Category> selectionModel = new SingleSelectionModel<Category>(Category.KEY_PROVIDER);
 		getView().getCategoryTable().setSelectionModel(selectionModel);
 		selectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
 			public void onSelectionChange(SelectionChangeEvent event) {
 
 				if (selectionModel.getSelectedObject() != null){
 					if (selectedType.equals(SelectionType.SELECTED_CATEGORY)) {
 						selectedCategory = selectionModel.getSelectedObject();
 						searchParlamentarian(selectedCategory);
 						getView().notificationSelectCategory();
 						getView().displaySelectionNone();
 						getView().displaySelectionCategory();
 					}
 					if (selectedType.equals(SelectionType.SELECTED_PARLAMENTARIAN) && selectedParlamentarian == null) {
 						getView().getCategoryTable().getSelectionModel().setSelected(selectionModel.getSelectedObject(), false);
 						selectedCategory = null;
 					} else {
 						selectedCategory = selectionModel.getSelectedObject();
 						getView().setCategoryDisplay(selectedCategory.getName());
 						setBillTable();
 					}
 					showPermalink();
 				}
 			}
 		});
 	}
 
 	public void initBillTable() {
 		while (getView().getBillTable().getColumnCount() > 0) {
 			getView().getBillTable().removeColumn(0);
 		}
 
 		// Creates action view bill column
 		Column<Bill, Bill> viewBillColumn = new Column<Bill, Bill>(new ActionCell<Bill>("", new ActionCell.Delegate<Bill>() {
 
 			@Override
 			public void execute(Bill bill) {
 				PlaceRequest placeRequest = new PlaceRequest(BillPresenter.PLACE);
 				placeRequest = placeRequest.with(BillPresenter.PARAM_BILL_ID, bill.getId().toString());
 				placeRequest = placeRequest.with(BillPresenter.PARAM_PARLAMENTARIAN_ID, selectedParlamentarian.getId().toString());
 				placeManager.revealPlace(placeRequest);
 			}
 		}) {
 			@Override
 			public void render(Cell.Context context, Bill value, SafeHtmlBuilder sb) {
 				sb.append(new SafeHtml() {
 
 					@Override
 					public String asString() {
 						return "<div class=\"glassButton\"></div>";
 					}
 				});
 			}
 		}) {
 
 			@Override
 			public Bill getValue(Bill bill) {
 				return bill;
 			}
 		};
 
 		// Adds action view bill column to table
 		getView().getBillTable().addColumn(viewBillColumn, applicationMessages.getGeneralViewMore());
 
 		// Creates bulletin column
 		TextColumn<Bill> bulletinColumn = new TextColumn<Bill>() {
 			@Override
 			public String getValue(Bill bill) {
 				return bill.getBulletinNumber();
 			}
 		};
 
 		// Adds bulletin column to table
 		getView().getBillTable().addColumn(bulletinColumn, applicationMessages.getBillBulletin());
 
 		// Creates title column
 		Column<Bill, Bill> titleColumn = new Column<Bill, Bill>(new ActionCell<Bill>("", new ActionCell.Delegate<Bill>() {
 
 			@Override
 			public void execute(Bill bill) {
 				PlaceRequest placeRequest = new PlaceRequest(BillPresenter.PLACE);
 				placeRequest = placeRequest.with(BillPresenter.PARAM_BILL_ID, bill.getId().toString());
 				placeRequest = placeRequest.with(BillPresenter.PARAM_PARLAMENTARIAN_ID, selectedParlamentarian.getId().toString());
 				placeManager.revealPlace(placeRequest);
 			}
 		}) {
 			@Override
 			public void render(Cell.Context context, final Bill value, SafeHtmlBuilder sb) {
 				sb.append(new SafeHtml() {
 
 					@Override
 					public String asString() {
 						return "<div class=\"clickableCell\">" + value.getTitle() + "</div>";
 					}
 				});
 			}
 		}) {
 
 			@Override
 			public Bill getValue(Bill bill) {
 				return bill;
 			}
 		};
 
 		// Adds title column to table
 		getView().getBillTable().addColumn(titleColumn, applicationMessages.getBillConflictedBill());
 
 		// Creates isAuthor column
 		Column<Bill, String> isAuthorColumn = new Column<Bill, String>(new ImageCell()){
 			@Override
 			public String getValue(Bill bill) {
 				if (selectedParlamentarian != null && selectedParlamentarian.getAuthoredBills() != null) {
 					if (selectedParlamentarian.getAuthoredBills().contains(bill)) {
 						return "images/shoeprints.png";
 					}
 				}
 				return "images/shoeprints_hidden.png";
 			}
 		};
 
 		// Adds isAuthor column to table
 		getView().getBillTable().addColumn(isAuthorColumn, applicationMessages.getBillIsAuthoredBill());
 
 		// Creates isVoted column
 		Column<Bill, String> isVotedColumn = new Column<Bill, String>(new ImageCell()){
 			@Override
 			public String getValue(Bill bill) {
 				if (selectedParlamentarian != null && selectedParlamentarian.getVotedBills() != null) {
 					if (selectedParlamentarian.getVotedBills().contains(bill)) {
 						return "images/shoeprints.png";
 					}
 				}
 				return "images/shoeprints_hidden.png";
 			}
 		};
 
 		// Adds isVoted column to table
 		getView().getBillTable().addColumn(isVotedColumn, applicationMessages.getBillVotedInChamber());
 
 		// Creates action suscription column
 		Column<Bill, Bill> suscriptionColumn = new Column<Bill, Bill>(new ActionCell<Bill>("", new ActionCell.Delegate<Bill>() {
 
 			@Override
 			public void execute(Bill bill) {
 				// TODO: add bill suscription servlet
 				PlaceRequest placeRequest = new PlaceRequest(SubscriptionPresenter.PLACE);
 				placeManager.revealPlace(placeRequest.with(SubscriptionPresenter.PARAM_BILL_ID, bill.getId().toString()));
 			}
 		}) {
 			@Override
 			public void render(Cell.Context context, Bill value, SafeHtmlBuilder sb) {
 				sb.append(new SafeHtml() {
 
 					@Override
 					public String asString() {
 						return "<div class=\"suscribeButtonBill\"></div>";
 					}
 				});
 			}
 		}) {
 
 			@Override
 			public Bill getValue(Bill bill) {
 				return bill;
 			}
 		};
 
 		// Adds action subscription column to table
 		getView().getBillTable().addColumn(suscriptionColumn, applicationMessages.getGeneralSubscribe());
 	}
 
 	public void setBillTable() {
 		if (selectedParlamentarian != null && selectedCategory != null) {
 			if (selectedType.equals(SelectionType.SELECTED_PARLAMENTARIAN)) {
 				getView().displaySelectionCategory();
 			}
 			else if (selectedType.equals(SelectionType.SELECTED_CATEGORY)) {
 				getView().displaySelectionParliamentarian();
 			}
 			searchBill(selectedParlamentarian.getId(), selectedCategory.getId());
 			getView().showBillTable();
 		} else {
 			setBillData(new ListDataProvider<Bill>());
 		}
 	}
 
 	@Override
 	public void showParlamentarianProfile(){
 		if (selectedParlamentarian != null) {
 			PlaceRequest placeRequest = new PlaceRequest(ParlamentarianPresenter.PLACE);
 			placeManager.revealPlace(placeRequest.with(ParlamentarianPresenter.PARAM_PARLAMENTARIAN_ID, selectedParlamentarian.getId().toString()));
 		}
 	}
 
 	@Override
 	public void switchSelectionType() {
 		if (selectedType.equals(SelectionType.SELECTED_PARLAMENTARIAN)) {
 			setupSelection(SelectionType.SELECTED_CATEGORY);
 		}
 		else if (selectedType.equals(SelectionType.SELECTED_CATEGORY)) {
 			setupSelection(SelectionType.SELECTED_PARLAMENTARIAN);
 		}
 		searchCleaner();
 	}
 
 	@Override
 	public void searchCleaner() {
 		getView().getParlamentarianTable().getSelectionModel().setSelected(selectedParlamentarian, false);
 		getView().getCategoryTable().getSelectionModel().setSelected(selectedCategory, false);
 		getView().notificationSelectHidden();
 		cleanerSelection();
 		resetSelection();
 		parlamentarianId = null;
 		categoryId = null;
 		initDataLoad();
 	}
 
 	@Override
 	public void setupSelection(SelectionType changeType) {
 		selectedType = changeType;
 		getView().setSelectedType(selectedType);
 	}
 
 	public void showPermalink() {
 		if (selectedParlamentarian != null && selectedCategory != null) {
 			getView().showPermalink();
 		} else {
 			getView().hidePermalink();
 		}
 	}
 
 	@Override
 	public void getPermalink() {
 		PlaceRequest placeRequest = new PlaceRequest(PermalinkPresenter.PLACE)
 			.with(PermalinkPresenter.PARAM_PLACE_TOKEN, PLACE)
 			.with(PermalinkPresenter.PARAM_PARLAMENTARIAN_ID, selectedParlamentarian.getId().toString())
 			.with(PermalinkPresenter.PARAM_CATEGORY_ID, selectedCategory.getId().toString());
 		placeManager.revealPlace(placeRequest);
 	}
 
 	@Override
 	public void showNotification(String message, NotificationEventType type) {
 		NotificationEventParams params = new NotificationEventParams();
 		params.setMessage(message);
 		params.setType(type);
 		params.setDuration(NotificationEventParams.DURATION_NORMAL);
 		fireEvent(new NotificationEvent(params));
 	}
 
 	public void cleanerSelection() {
 		getView().setCategorySearch(applicationMessages.getCategorySearchMessage());
 		getView().setParlamentarianSearch(applicationMessages.getParlamentarianSearchMessage());
 	}
 
 	public void resetSelection() {
 		selectedParlamentarian = null;
 		selectedCategory = null;
 		getView().setParlamentarianDisplay(applicationMessages.getGeneralParlamentarian());
 		getView().setParlamentarianImage("images/parlamentarian/large/avatar.png");
 		getView().setCategoryDisplay(applicationMessages.getGeneralCategory());
 		getView().hideParlamentarianMessage();
 		getView().hideCategoryMessage();
 		getView().hideBillMessage();
 		getView().hidePermalink();
 	}
 
 	public void resetNoConflicts() {
 		getView().getParlamentarianTable().getSelectionModel().setSelected(selectedParlamentarian, false);
 		getView().getCategoryTable().getSelectionModel().setSelected(selectedCategory, false);
 		getView().notificationSelectHidden();
 		getView().hideConflictImage();
 		initDataLoad();
 	}
 }
