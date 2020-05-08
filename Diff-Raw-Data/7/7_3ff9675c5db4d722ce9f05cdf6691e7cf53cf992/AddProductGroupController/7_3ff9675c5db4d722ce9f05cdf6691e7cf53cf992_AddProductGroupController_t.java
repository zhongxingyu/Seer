 package gui.productgroup;
 
 import gui.common.Controller;
 import gui.common.IView;
 import gui.inventory.ProductContainerData;
 import model.ProductContainer;
 import model.ProductContainerManager;
 import model.ProductGroup;
 import model.ProductQuantity;
 import model.Unit;
 
 /**
  * Controller class for the add product group view.
  */
 public class AddProductGroupController extends Controller implements
 		IAddProductGroupController {
 
 	ProductContainerData parentContainerData;
 
 	/**
 	 * Constructor.
 	 * 
 	 * @param view
 	 *            Reference to add product group view
 	 * @param container
 	 *            Product container to which the new product group is being added
 	 */
 	public AddProductGroupController(IView view, ProductContainerData container) {
 		super(view);
 		parentContainerData = container;
 		construct();
 	}
 
 	//
 	// Controller overrides
 	//
 
 	/**
 	 * This method is called when the user clicks the "OK" button in the add product group
 	 * view.
 	 * 
 	 * @post The ProductGroup specified by the AddProductGroupView now exists in the selected
 	 *       ProductContainer
 	 */
 	@Override
 	public void addProductGroup() {
 		String pgName = getView().getProductGroupName();
 		float tmsQuantity = Float.parseFloat(getView().getSupplyValue());
 		Unit tmsUnit = Unit.convertToUnit(getView().getSupplyUnit().toString());
 		ProductQuantity threeMonthSupply = new ProductQuantity(tmsQuantity, tmsUnit);
 		ProductContainerManager manager = getProductContainerManager();
 		ProductContainer parent = (ProductContainer) parentContainerData.getTag();
 
 		ProductGroup productGroup = new ProductGroup(pgName, threeMonthSupply, tmsUnit,
 				parent, manager);
 	}
 
 	/**
 	 * This method is called when any of the fields in the add product group view is changed by
 	 * the user.
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
 
 		ProductContainer parent = (ProductContainer) parentContainerData.getTag();
 		if (!parent.canAddProductGroup(getView().getProductGroupName())) {
 			enableOk = false;
 		}
 
 		getView().enableOK(enableOk);
 	}
 
 	//
 	// IAddProductGroupController overrides
 	//
 
 	/**
 	 * Returns a reference to the view for this controller.
 	 * 
 	 * {@pre None}
 	 * 
 	 * {@post Returns a reference to the view for this controller.}
 	 */
 	@Override
 	protected IAddProductGroupView getView() {
 		return (IAddProductGroupView) super.getView();
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
 		getView().setProductGroupName("");
 		getView().setSupplyValue("0");
 	}
 }
