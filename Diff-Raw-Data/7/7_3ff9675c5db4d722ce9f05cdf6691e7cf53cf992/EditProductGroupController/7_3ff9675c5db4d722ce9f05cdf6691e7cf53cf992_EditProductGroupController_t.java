 package gui.productgroup;
 
 import gui.common.Controller;
 import gui.common.IView;
 import gui.common.SizeUnits;
 import gui.inventory.ProductContainerData;
 import model.ProductContainerManager;
 import model.ProductGroup;
 import model.ProductQuantity;
 import model.StorageUnit;
 import model.Unit;
 
 /**
  * Controller class for the edit product group view.
  */
 public class EditProductGroupController extends Controller implements
 		IEditProductGroupController {
 	protected ProductContainerData originalData;
 
 	/**
 	 * Constructor.
 	 * 
 	 * @param view
 	 *            Reference to edit product group view
 	 * @param target
 	 *            Product group being edited
 	 */
 	public EditProductGroupController(IView view, ProductContainerData target) {
 		super(view);
 		originalData = target;
 
 		construct();
 	}
 
 	//
 	// Controller overrides
 	//
 
 	/**
 	 * This method is called when the user clicks the "OK" button in the edit product group
 	 * view.
 	 */
 	@Override
 	public void editProductGroup() {
 		ProductGroup pg = (ProductGroup) originalData.getTag();
 		StorageUnit root;
 		while (pg.getContainer() instanceof ProductGroup) {
 			pg = (ProductGroup) pg.getContainer();
 		}
 		root = (StorageUnit) pg.getContainer();
 		String newName = getView().getProductGroupName();
 		float tmsQuantity = Float.parseFloat(getView().getSupplyValue());
 		Unit tmsUnit = Unit.convertToUnit(getView().getSupplyUnit().toString());
 		ProductQuantity newTMS = new ProductQuantity(tmsQuantity, tmsUnit);
 		ProductContainerManager manager = getView().getProductContainerManager();
 		manager.editProductGroup(root, originalData.getName(), newName, newTMS);
 	}
 
 	/**
 	 * This method is called when any of the fields in the edit product group view is changed
 	 * by the user.
 	 */
 	@Override
 	public void valuesChanged() {
 		enableComponents();
 	}
 
 	/**
 	 * Sets the enable/disable state of all components in the controller's view. A component
 	 * should be enabled only if the user is currently allowed to interact with that component.
 	 * 
 	 * {@pre None}
 	 * 
 	 * {@post The enable/disable state of all components in the controller's view have been set
 	 * appropriately.}
 	 */
 	@Override
 	protected void enableComponents() {
 		getView().enableProductGroupName(true);
 		getView().enableSupplyUnit(true);
 		getView().enableSupplyValue(true);
 		boolean enableOk = true;
 		float supplyValue = 0;
 		try {
 			supplyValue = Float.parseFloat(getView().getSupplyValue());
 			if (!ProductQuantity.isValidProductQuantity(supplyValue,
 					Unit.convertToUnit(getView().getSupplyUnit().toString()))) {
 				enableOk = false;
 			}
 		} catch (NumberFormatException e) {
 			enableOk = false;
 		}
 
 		ProductGroup target = (ProductGroup) originalData.getTag();
 		if (!target.getContainer().canAddProductGroup(getView().getProductGroupName())
 				&& !originalData.getName().equals(getView().getProductGroupName())) {
 			enableOk = false;
 		}
 
 		getView().enableOK(enableOk);
 	}
 
 	//
 	// IEditProductGroupController overrides
 	//
 
 	/**
 	 * Returns a reference to the view for this controller.
 	 * 
 	 * {@pre None}
 	 * 
 	 * {@post Returns a reference to the view for this controller.}
 	 */
 	@Override
 	protected IEditProductGroupView getView() {
 		return (IEditProductGroupView) super.getView();
 	}
 
 	/**
 	 * Loads data into the controller's view.
 	 * 
 	 * {@pre None}
 	 * 
 	 * {@post The controller has loaded data into its view}
 	 */
 	@Override
 	protected void loadValues() {
 		ProductGroup original = (ProductGroup) originalData.getTag();
 		getView().setSupplyValue(
 				String.valueOf((original.getThreeMonthSupply().getQuantity())));
 
 		String unitString = original.getThreeMonthSupply().getUnits().toString();
 		if (unitString.contains(" "))
 			unitString = unitString.replace(" ", "");
 
 		getView().setSupplyUnit(SizeUnits.valueOf(unitString));
 
 		getView().setProductGroupName(originalData.getName());
 	}
 }
