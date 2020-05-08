 package cl.votainteligente.inspector.client.presenters;
 
 import cl.votainteligente.inspector.client.i18n.ApplicationMessages;
 import cl.votainteligente.inspector.client.services.BillServiceAsync;
 import cl.votainteligente.inspector.client.services.ParlamentarianServiceAsync;
 import cl.votainteligente.inspector.client.services.SocietyServiceAsync;
 import cl.votainteligente.inspector.client.uihandlers.BillUiHandlers;
 import cl.votainteligente.inspector.model.*;
 
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
 import com.google.gwt.user.cellview.client.*;
 import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.view.client.*;
 import com.google.inject.Inject;
 
 import java.util.*;
 
 public class BillPresenter extends Presenter<BillPresenter.MyView, BillPresenter.MyProxy> implements BillUiHandlers {
 	public static final String PLACE = "bill";
 	public static final String PARAM_BILL_ID = "billId";
 	public static final String PARAM_PARLAMENTARIAN_ID = "parlamentarianId";
 
 	public interface MyView extends View, HasUiHandlers<BillUiHandlers> {
 		void setBillBulletinNumber(String billBulletinNumber);
 		void setBillTitle(String billTitle);
 		void setBillDescription(String billContent);
 		void addBillAuthors(Parlamentarian parlamentarian, String href, Boolean hasNext);
 		void clearBillAuthors();
 		void setBillEntryDate(Date billEntryDate);
 		void setBillInitiativeType(String billInitiativeType);
 		void setBillType(String billType);
 		void setBillOriginChamber(String billOriginChamber);
 		void setBillUrgency(String billUrgency);
 		void setBillStage(String billStage);
 		void addBillCategories(Category category, String href, Boolean hasNext);
 		void clearBillCategories();
 		void setParlamentarianImage(String parlamentarianImageUrl);
 		void setParlamentarianDisplay(String parlamentarianName);
 		CellTable<Parlamentarian> getParlamentarianTable();
 		void setParlamentarianTable(CellTable<Parlamentarian> parlamentarianTable);
 		CellTable<Society> getSocietyTable();
 		void setSocietyTable(CellTable<Society> societyTable);
 	}
 
 	@ProxyStandard
 	@NameToken(PLACE)
 	public interface MyProxy extends ProxyPlace<BillPresenter> {
 	}
 
 	@Inject
 	private ApplicationMessages applicationMessages;
 	@Inject
 	private PlaceManager placeManager;
 	@Inject
 	private BillServiceAsync billService;
 	@Inject
 	private ParlamentarianServiceAsync parlamentarianService;
 	@Inject
 	private SocietyServiceAsync societyService;
 
 	private Long billId;
 	private Long parlamentarianId;
 	private Bill selectedBill;
 	private Parlamentarian selectedParlamentarian;
 	private AbstractDataProvider<Parlamentarian> parlamentarianData;
 	private AbstractDataProvider<Society> societyData;
 
 	@Inject
 	public BillPresenter(EventBus eventBus, MyView view, MyProxy proxy) {
 		super(eventBus, view, proxy);
 		getView().setUiHandlers(this);
 	}
 
 	@Override
 	protected void onReset() {
 	}
 
 	@Override
 	protected void onReveal() {
 		selectedBill = null;
 		parlamentarianData = new ListDataProvider<Parlamentarian>();
 		societyData = new ListDataProvider<Society>();
 		initParlamentarianTable();
 		initSocietyTable();
 
 		if (billId != null) {
 			showBill();
 		}
 
 		getView().setParlamentarianDisplay(applicationMessages.getGeneralParlamentarian());
 		getView().setParlamentarianImage("images/parlamentarian/large/avatar.png");
 	}
 
 	@Override
 	protected void revealInParent() {
 		fireEvent(new RevealContentEvent(MainPresenter.SLOT_MAIN_CONTENT, this));
 	}
 
 	@Override
 	public void prepareFromRequest(PlaceRequest placeRequest) {
 		super.prepareFromRequest(placeRequest);
 
 		try {
 			billId = Long.parseLong(placeRequest.getParameter(PARAM_BILL_ID, null));
 			parlamentarianId = Long.parseLong(placeRequest.getParameter(PARAM_PARLAMENTARIAN_ID, null));
 		} catch (NumberFormatException nfe) {
 			billId = null;
 			parlamentarianId = null;
 		}
 	}
 
 	public Long getBillId() {
 		return billId;
 	}
 
 	public void setBillId(Long billId) {
 		this.billId = billId;
 	}
 
 	public Long getParlamentarianId() {
 		return parlamentarianId;
 	}
 
 	public void setParlamentarianId(Long parlamentarianId) {
 		this.parlamentarianId = parlamentarianId;
 	}
 
 	public void setSelectedParlamentarian(Parlamentarian parlamentarian) {
 		this.selectedParlamentarian = parlamentarian;
 	}
 
 	public Parlamentarian getSelectedParlamentarian() {
 		return selectedParlamentarian;
 	}
 
 	public void showBill() {
 		billService.getBill(billId, new AsyncCallback<Bill>() {
 
 			@Override
 			public void onFailure(Throwable caught) {
 				Window.alert(applicationMessages.getErrorBill());
 			}
 
 			@Override
 			public void onSuccess(Bill result) {
 				if (result != null) {
 					selectedBill = result;
 					getView().setBillBulletinNumber(selectedBill.getBulletinNumber());
 					getView().setBillTitle(selectedBill.getTitle());
 					getView().setBillDescription(selectedBill.getDescription());
 					getView().setBillEntryDate(selectedBill.getEntryDate());
 					getView().setBillInitiativeType(selectedBill.getInitiativeType().getName());
 					getView().setBillType(selectedBill.getBillType().getName());
 					getView().setBillOriginChamber(selectedBill.getOriginChamber().getName());
 					getView().setBillUrgency(selectedBill.getUrgency().getName());
 					getView().setBillStage(selectedBill.getStage().getName());
 
 					Iterator<Category> iterator = selectedBill.getCategories().iterator();
 					getView().clearBillCategories();
 					Category category = null;
 					String href = null;
 
 					while (iterator.hasNext()) {
 						category = iterator.next();
 						PlaceRequest placeRequest = new PlaceRequest(HomePresenter.PLACE).with(HomePresenter.PARAM_CATEGORY_ID, category.getId().toString());
 						href = placeManager.buildHistoryToken(placeRequest);
 						getView().addBillCategories(category, href, iterator.hasNext());
 					}
 
 					if (parlamentarianId != null) {
 						loadSelectedParlamentarian();
 					} else {
 						setSelectedParlamentarian(new Parlamentarian());
 					}
 					getBillAuthors(result);
 					getParlamentarians(result);
 				}
 			}
 		});
 	}
 
 	public void loadSelectedParlamentarian() {
 		parlamentarianService.getParlamentarian(parlamentarianId, new AsyncCallback<Parlamentarian>() {
 			@Override
 			public void onFailure(Throwable caught) {
 				Window.alert(applicationMessages.getErrorParlamentarian());
 			}
 
 			@Override
 			public void onSuccess(Parlamentarian result) {
 				setSelectedParlamentarian(result);
 				showSelectedParlamentarian();
 			}
 		});
 	}
 
 	public void showSelectedParlamentarian() {
 		if (selectedParlamentarian != null) {
 			getView().setParlamentarianDisplay(selectedParlamentarian.toString());
 			if (selectedParlamentarian.getImage() != null) {
 				getView().setParlamentarianImage("images/parlamentarian/large/" + selectedParlamentarian.getImage());
 			} else {
 				getView().setParlamentarianImage("images/parlamentarian/large/avatar.png");
 			}
 			List<Society> societies = new ArrayList<Society>(selectedParlamentarian.getSocieties().keySet());
 			Set<Society> resultSet = new TreeSet<Society>();
 			Set<Category> intersection = new HashSet<Category>();
 
 			for (Society society : societies) {
 				intersection = new HashSet<Category>(society.getCategories());
 				intersection.retainAll(selectedBill.getCategories());
 				if (intersection.size() > 0) {
 					resultSet.add(society);
 				}
 			}
 
 			societies = new ArrayList<Society>(resultSet);
 
 			AbstractDataProvider<Society> data = new ListDataProvider<Society>(societies);
 			setSocietyData(data);
 		}
 	}
 
 	public void getParlamentarians(Bill bill) {
 		parlamentarianService.getParlamentariansByBill(bill, new AsyncCallback<List<Parlamentarian>>() {
 
 			@Override
 			public void onFailure(Throwable caught) {
 				Window.alert(applicationMessages.getErrorParlamentarianBillSearch());
 			}
 
 			@Override
 			public void onSuccess(List<Parlamentarian> result) {
 				if (result != null) {
 					ListDataProvider<Parlamentarian> data = new ListDataProvider<Parlamentarian>(result);
 					setParlamentarianData(data);
 				}
 			}
 		});
 	}
 
 	public void getBillAuthors(Bill bill) {
 		parlamentarianService.getBillAuthors(bill, new AsyncCallback<List<Parlamentarian>>() {
 
 			@Override
 			public void onFailure(Throwable caught) {
 				Window.alert(applicationMessages.getErrorBillAuthors());
 			}
 
 			@Override
 			public void onSuccess(List<Parlamentarian> result) {
 				if (result != null) {
 					Iterator<Parlamentarian> iterator = result.iterator();
 					getView().clearBillAuthors();
 					Parlamentarian parlamentarian = null;
 					String href = null;
 
 					while (iterator.hasNext()) {
 						parlamentarian = iterator.next();
 						PlaceRequest placeRequest = new PlaceRequest(ParlamentarianPresenter.PLACE).with(HomePresenter.PARAM_PARLAMENTARIAN_ID, parlamentarian.getId().toString());
 						href = placeManager.buildHistoryToken(placeRequest);
 						getView().addBillAuthors(parlamentarian, href, iterator.hasNext());
 					}
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
 
 	public AbstractDataProvider<Society> getSocietyData() {
 		return societyData;
 	}
 
 	public void setSocietyData(AbstractDataProvider<Society> data) {
 		societyData = data;
 		societyData.addDataDisplay(getView().getSocietyTable());
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
 
 		// Adds image column to table
 		getView().getParlamentarianTable().addColumn(imageColumn, "");
 
 		// Creates name column
 		TextColumn<Parlamentarian> nameColumn = new TextColumn<Parlamentarian>() {
 
 			@Override
 			public String getValue(Parlamentarian parlamentarian) {
 				return parlamentarian.toString();
 			}
 		};
 
 		// Sets sortable name column
 		nameColumn.setSortable(true);
 		ListHandler<Parlamentarian> nameSortHandler = new ListHandler<Parlamentarian>(((ListDataProvider<Parlamentarian>) parlamentarianData).getList());
 		getView().getParlamentarianTable().addColumnSortHandler(nameSortHandler);
 		nameSortHandler.setComparator(nameColumn, new Comparator<Parlamentarian>() {
 
 			@Override
 			public int compare(Parlamentarian o1, Parlamentarian o2) {
 				return o1.getLastName().compareTo(o2.getLastName());
 			}
 		});
 
 		// Adds name column to table
 		getView().getParlamentarianTable().addColumn(nameColumn, applicationMessages.getGeneralParlamentarian());
 
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
 					parlamentarianId = selectionModel.getSelectedObject().getId();
 					loadSelectedParlamentarian();
 				}
 			}
 		});
 	}
 
 	public void initSocietyTable() {
 		while (getView().getSocietyTable().getColumnCount() > 0) {
 			getView().getSocietyTable().removeColumn(0);
 		}
 
 		// Creates categories column
 		TextColumn<Society> categoriesColumn = new TextColumn<Society>() {
 
 			@Override
 			public String getValue(Society society) {
 				StringBuilder sb = new StringBuilder();
 				Iterator<Category> iterator = society.getCategories().iterator();
 
 				while (iterator.hasNext()) {
 					sb.append(iterator.next().getName());
 
 					if (iterator.hasNext()) {
 						sb.append(", ");
 					} else {
 						sb.append('.');
 					}
 				}
 
 				return sb.toString();
 			}
 		};
 
 		// Adds name column to table
 		getView().getSocietyTable().addColumn(categoriesColumn, applicationMessages.getGeneralCategory());
 
 		// Creates name column
 		TextColumn<Society> nameColumn = new TextColumn<Society>() {
 
 			@Override
 			public String getValue(Society society) {
 				return society.getName();
 			}
 		};
 
 		// Sets sortable name column
 		nameColumn.setSortable(true);
 		ListHandler<Society> nameSortHandler = new ListHandler<Society>(((ListDataProvider<Society>) societyData).getList());
 		getView().getParlamentarianTable().addColumnSortHandler(nameSortHandler);
 		nameSortHandler.setComparator(nameColumn, new Comparator<Society>() {
 
 			@Override
 			public int compare(Society o1, Society o2) {
 				return o1.getName().compareTo(o2.getName());
 			}
 		});
 
 		// Adds name column to table
 		getView().getSocietyTable().addColumn(nameColumn, applicationMessages.getGeneralSocietiesInConflict());
 
 		// Creates reported column
 		Column<Society, String> reportedColumn = new Column<Society, String>(new ImageCell()) {
 
 			@Override
 			public String getValue(Society society) {
 				String reported = "images/declare_no.png";
 
 				if (selectedParlamentarian != null && selectedParlamentarian.getSocieties().containsKey(society)) {
 					if (selectedParlamentarian.getSocieties().get(society) == true) {
 						reported = "images/declare_yes.png";
 					}
 				}
 				return reported;
 			}
 		};
 
 		// Adds name reported to table
 		getView().getSocietyTable().addColumn(reportedColumn, applicationMessages.getSocietyReported());
 
 		// Creates action profile column
 		Column<Society, Society> profileColumn = new Column<Society, Society>(new ActionCell<Society>("", new ActionCell.Delegate<Society>() {
 
 			@Override
 			public void execute(Society society) {
 				PlaceRequest placeRequest = new PlaceRequest(SocietyPresenter.PLACE);
 				placeManager.revealPlace(placeRequest.with(SocietyPresenter.PARAM_SOCIETY_ID, society.getId().toString()));
 			}
 		}) {
 			@Override
 			public void render(Cell.Context context, Society value, SafeHtmlBuilder sb) {
 				sb.append(new SafeHtml() {
 
 					@Override
 					public String asString() {
 						return "<div class=\"glassButton\"></div>";
 					}
 				});
 			}
 		}) {
 
 			@Override
 			public Society getValue(Society society) {
 				return society;
 			}
 		};
 
 		// Adds action profile column to table
 		getView().getSocietyTable().addColumn(profileColumn, applicationMessages.getGeneralViewMore());
 	}
 
 	@Override
 	public void showParlamentarianProfile() {
 		if (selectedParlamentarian != null) {
 			PlaceRequest placeRequest = new PlaceRequest(ParlamentarianPresenter.PLACE);
 			placeManager.revealPlace(placeRequest.with(ParlamentarianPresenter.PARAM_PARLAMENTARIAN_ID, selectedParlamentarian.getId().toString()));
 		}
 	}
 }
