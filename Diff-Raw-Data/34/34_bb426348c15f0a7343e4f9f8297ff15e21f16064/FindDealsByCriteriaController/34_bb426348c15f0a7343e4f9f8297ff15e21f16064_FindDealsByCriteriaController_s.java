 /**
  * 
  */
 package org.diveintojee.poc.ui.unittesting.deals;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.swing.DefaultListModel;
 
 import org.apache.commons.collections.CollectionUtils;
 import org.apache.commons.lang.ArrayUtils;
 import org.apache.commons.lang.StringUtils;
 import org.diveintojee.poc.ui.unittesting.deals.model.FindDealByCriteriaResultsTableModel;
 import org.diveintojee.poc.ui.unittesting.deals.model.FindDealsByCriteriaModel;
 import org.diveintojee.poc.ui.unittesting.deals.view.FindDealsByCriteriaInternalFrame;
 import org.diveintojee.poc.ui.unittesting.domain.Deal;
 import org.diveintojee.poc.ui.unittesting.domain.Entity;
 import org.diveintojee.poc.ui.unittesting.domain.ProductType;
 import org.diveintojee.poc.ui.unittesting.domain.services.DealService;
 import org.diveintojee.poc.ui.unittesting.domain.services.DealsSearchCriteria;
 import org.diveintojee.poc.ui.unittesting.domain.services.ReferentialService;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Qualifier;
 import org.springframework.stereotype.Component;
 
 /**
  * @author louis.gueye@gmail.com
  * 
  */
 @Component(FindDealsByCriteriaController.BEAN_ID)
 public class FindDealsByCriteriaController implements ActionListener {
 
 	public static final String BEAN_ID = "findDealsByCriteriaController";
 
 	@Autowired
 	@Qualifier(ReferentialService.BEAN_ID)
 	private ReferentialService referentialService;
 
 	@Autowired
 	@Qualifier(DealService.BEAN_ID)
 	private DealService dealService;
 
 	private FindDealsByCriteriaInternalFrame view;
 
 	private FindDealsByCriteriaModel model;
 
 	@Override
 	public void actionPerformed(final ActionEvent e) {
 		if (getView().getOkButton().equals(e.getSource()))
 			onOkButtonClick();
 		else if (getView().getClearButton().equals(e.getSource()))
 			onClearButtonClick();
 		else if (getView().getEntityComboBox().equals(e.getSource()))
			try {
				onEntitySelectionChange();
			} catch (Throwable th) {
				th.printStackTrace();
			}
 	}
 
 	protected void addListeners() {
 		getView().getOkButton().addActionListener(this);
 		getView().getClearButton().addActionListener(this);
 		getView().getEntityComboBox().addActionListener(this);
 	}
 
 	protected void createView() {
 		setView(new FindDealsByCriteriaInternalFrame());
		getView().getResultsTable().setModel(
				new FindDealByCriteriaResultsTableModel());
 		String[] columns = { "Date", "Entity", "Product type", "Amount",
 				"Currency" };
		((FindDealByCriteriaResultsTableModel) getView().getResultsTable()
				.getModel()).setColumns(columns);
 	}
 
 	/**
 	 * @return the model
 	 */
 	public FindDealsByCriteriaModel getModel() {
 		return model;
 	}
 
 	/**
 	 * @return the view
 	 */
 	public FindDealsByCriteriaInternalFrame getView() {
 		return view;
 	}
 
 	public void initialize() {
 
 		createView();
 
 		loadReferentialData();
 
 		setModel(null);
 
 		showView(true);
 
 	}
 
 	/**
 	 * 
 	 */
 	protected void loadReferentialData() {
 
 		List<Entity> entities = referentialService.loadAllEntities();
 
 		if (getModel() == null)
 			model = new FindDealsByCriteriaModel();
 
 		getModel().setEntities(entities);
 
 		if (CollectionUtils.isEmpty(getModel().getEntities()))
 			getView().getEntityComboBox().removeAllItems();
 		else {
 			getView().getEntityComboBox().addItem(Entity.EMPTY);
 			for (Entity entity : getModel().getEntities())
 				if (entity != null)
 					getView().getEntityComboBox().addItem(entity);
 		}
 
 		List<ProductType> productTypes = referentialService
 				.loadAllProductTypes();
 
 		getModel().setProductTypes(productTypes);
 
 		if (CollectionUtils.isEmpty(getModel().getProductTypes()))
 			getView().getProductTypeComboBox().removeAllItems();
 		else {
 			getView().getProductTypeComboBox().addItem(ProductType.EMPTY);
 			for (ProductType productType : getModel().getProductTypes())
 				getView().getProductTypeComboBox().addItem(productType);
 		}
 
 	}
 
 	protected void onClearButtonClick() {
 		setModel(null);
 	}
 
 	protected void onEntitySelectionChange() {
 		Entity entity = (Entity) getView().getEntityComboBox()
 				.getSelectedItem();
 
 		if (getModel() == null)
 			model = new FindDealsByCriteriaModel();
 
 		getModel().getCriteria().setEntity(entity);
 
 		// New entity, clear currency selection
 		getModel().getCriteria().setCurrencyCodes(null);
 
 		updateViewFromModel();
 	}
 
 	protected void onOkButtonClick() {
 		updateModelFromView();
 		List<Deal> results = dealService.findByCriteria(getModel()
 				.getCriteria());
 		getModel().setResults(results);
 		updateViewFromModel();
 	}
 
 	protected void removeListeners() {
 		getView().getOkButton().removeActionListener(this);
 		getView().getClearButton().removeActionListener(this);
 		getView().getEntityComboBox().removeActionListener(this);
 	}
 
 	/**
 	 * @param model
 	 *            the model to set
 	 */
 	public void setModel(final FindDealsByCriteriaModel model) {
 		this.model = model;
 		updateViewFromModel();
 	}
 
 	/**
 	 * @param view
 	 *            the view to set
 	 */
 	public void setView(final FindDealsByCriteriaInternalFrame view) {
 		this.view = view;
 	}
 
 	public void showView(final boolean visible) {
 		getView().setVisible(visible);
 	}
 
 	protected void updateModelFromView() {
 
 		removeListeners();
 
 		if (getModel() == null)
 			model = new FindDealsByCriteriaModel();
 		if (getModel().getCriteria() == null)
 			getModel().setCriteria(new DealsSearchCriteria());
 
 		getModel().getCriteria().setStartDate(
 				getView().getStartDatePicker().getDate());
 		getModel().getCriteria().setEndDate(
 				getView().getEndDatePicker().getDate());
 		getModel().getCriteria().setEntity(
 				(Entity) getView().getEntityComboBox().getSelectedItem());
 		getModel().getCriteria().setProductType(
 				(ProductType) getView().getProductTypeComboBox()
 						.getSelectedItem());
 
 		Object[] currencies = getView().getCurrencyList().getSelectedValues();
 
 		if (!ArrayUtils.isEmpty(currencies)) {
 			List<String> currencyCodes = new ArrayList<String>();
 			for (Object object : currencies)
 				currencyCodes.add((String) object);
 			getModel().getCriteria().setCurrencyCodes(currencyCodes);
 		}
 
 		getModel().setResults(
 				((FindDealByCriteriaResultsTableModel) getView()
 						.getResultsTable().getModel()).getRows());
 
 		addListeners();
 
 	}
 
 	protected void updateViewFromModel() {
 
 		removeListeners();
 
 		if (getModel() == null || getModel().getCriteria() == null) {
 			getView().getStartDatePicker().setDate(null);
 			getView().getEndDatePicker().setDate(null);
 			getView().getEntityComboBox().setSelectedIndex(-1);
 			getView().getCurrencyList().setModel(new DefaultListModel());
 			getView().getProductTypeComboBox().setSelectedIndex(-1);
 			getView().getCurrencyList().setSelectionInterval(-1, -1);
 			((FindDealByCriteriaResultsTableModel) getView().getResultsTable()
 					.getModel()).setRows(null);
 			getView().updateResultsTitle(null);
 
 		} else {
 			getView().getStartDatePicker().setDate(
 					getModel().getCriteria().getStartDate());
 			getView().getStartDatePicker().revalidate();
 
 			getView().getEndDatePicker().setDate(
 					getModel().getCriteria().getEndDate());
 			getView().getEndDatePicker().revalidate();
 
 			getView().getEntityComboBox().setSelectedItem(
 					getModel().getCriteria().getEntity());
 
 			Entity entity = getModel().getCriteria().getEntity();
 			getView().getCurrencyList().setModel(new DefaultListModel());
 			if (entity != null
 					&& !CollectionUtils.isEmpty(entity.getCurrencyCodes())) {
 				List<String> copy = new ArrayList<String>();
 				for (String string : entity.getCurrencyCodes())
 					copy.add(string);
 				copy.add(0, StringUtils.EMPTY);
 				getView().getCurrencyList().setListData(copy.toArray());
 			}
 
 			if (entity != null
 					&& !CollectionUtils.isEmpty(entity.getCurrencyCodes())
 					&& !CollectionUtils.isEmpty(getModel().getCriteria()
 							.getCurrencyCodes()))
 				for (String currency : getModel().getCriteria()
 						.getCurrencyCodes())
 					getView().getCurrencyList().setSelectedValue(currency,
 							false);
 
 			getView().getProductTypeComboBox().setSelectedItem(
 					getModel().getCriteria().getProductType());
 
 			((FindDealByCriteriaResultsTableModel) getView().getResultsTable()
 					.getModel()).setRows(getModel().getResults());
 
 			getView().updateResultsTitle(getModel().getResults());
 		}
 
 		getView().getSplitPane().repaint();
		getView().getResultsScrollPane().repaint();
		getView().getResultsTable().repaint();
		getView().getResultsTable().updateUI();
 		getView().getResultsTable().revalidate();
 
 		addListeners();
 
 	}
 }
