 package gui.product;
 
 import gui.common.Controller;
 import gui.common.IView;
 import gui.common.SizeUnits;
 import model.Product;
 import model.ProductQuantity;
 import model.Unit;
 
 /**
  * Controller class for the edit product view.
  */
 public class EditProductController extends Controller implements IEditProductController {
 
 	private ProductData target;
 	private Unit currentUnit;
 
 	/**
 	 * Constructor.
 	 * 
 	 * @param view
 	 *            Reference to the edit product view
 	 * @param target
 	 *            Product being edited
 	 */
 	public EditProductController(IView view, ProductData target) {
 		super(view);
 		this.target = target;
 		currentUnit = null;
 		construct();
 	}
 
 	//
 	// Controller overrides
 	//
 
 	/**
 	 * This method is called when the user clicks the "OK" button in the edit product view.
 	 */
 	@Override
 	public void editProduct() {
 		Product oldProduct = (Product) target.getTag();
 		String newDescription = getView().getDescription();
 		Unit newUnit = Unit.convertToUnit(getView().getSizeUnit().toString());
 		float quantity = Float.parseFloat(getView().getSizeValue());
 		ProductQuantity newQuantity = new ProductQuantity(quantity, newUnit);
 		int newShelfLife = Integer.parseInt(getView().getShelfLife());
 		int newTms = Integer.parseInt(getView().getSupply());
		
		oldProduct.edit(newDescription, newQuantity, newShelfLife, newTms);
 	}
 
 	/**
 	 * This method is called when any of the fields in the edit product view is changed by the
 	 * user.
 	 */
 	@Override
 	public void valuesChanged() {
 		if ((currentUnit == Unit.COUNT) && (getView().getSizeUnit() != SizeUnits.Count)) {
 			getView().setSizeValue("0");
 			setCurrentUnit(Unit.convertFromSizeUnits(getView().getSizeUnit()));
 		} else if ((currentUnit != Unit.COUNT) && (getView().getSizeUnit() == SizeUnits.Count)) {
 			getView().setSizeValue("1");
 			setCurrentUnit(Unit.convertFromSizeUnits(getView().getSizeUnit()));
 		}
 
 		enableComponents();
 	}
 
 	private boolean isAllDataValid() {
 		int shelfLife = 0;
 		try {
 			shelfLife = Integer.parseInt(getView().getShelfLife());
 			if (!Product.isValidShelfLife(shelfLife))
 				return false;
 		} catch (NumberFormatException e) {
 			return false;
 		}
 		double sizeValue = 1;
 		try {
 			sizeValue = Double.parseDouble(getView().getSizeValue());
 			Unit unit = Unit.convertFromSizeUnits(getView().getSizeUnit());
 			if (!ProductQuantity.isValidProductQuantity((float) sizeValue, unit))
 				return false;
 			ProductQuantity pq = new ProductQuantity((float) sizeValue, unit);
 			if (!Product.isValidProductQuantity(pq))
 				return false;
 		} catch (NumberFormatException e) {
 			return false;
 		}
 		int tms = 0;
 		try {
 			tms = Integer.parseInt(getView().getSupply());
 			if (!Product.isValidThreeMonthSupply(tms))
 				return false;
 		} catch (NumberFormatException e) {
 			return false;
 		}
 		return (!getView().getBarcode().equals("") && !getView().getDescription().equals(""));
 	}
 
 	private void setCurrentUnit(Unit unit) {
 		assert (unit != null);
 
 		currentUnit = unit;
 	}
 
 	//
 	// IEditProductController overrides
 	//
 
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
 		getView().enableBarcode(false);
 		getView().enableDescription(true);
 		getView().enableOK(isAllDataValid());
 		getView().enableShelfLife(true);
 		getView().enableSizeUnit(true);
 		getView().enableSizeValue(!getView().getSizeUnit().equals(SizeUnits.Count));
 		getView().enableSupply(true);
 	}
 
 	/**
 	 * Returns a reference to the view for this controller.
 	 * 
 	 * {@pre None}
 	 * 
 	 * {@post Returns a reference to the view for this controller.}
 	 */
 	@Override
 	protected IEditProductView getView() {
 		return (IEditProductView) super.getView();
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
 		getView().setBarcode(target.getBarcode());
 		getView().setDescription(target.getDescription());
 		getView().setShelfLife(target.getShelfLife());
 
 		Product productTag = (Product) target.getTag();
 		ProductQuantity productQuantity = productTag.getProductQuantity();
 		currentUnit = productQuantity.getUnits();
 		String unitString = productQuantity.getUnits().toString();
 		if (unitString.contains(" "))
 			unitString = unitString.replace(" ", "");
 		getView().setSizeUnit(SizeUnits.valueOf(unitString));
 
 		String sizeString = "";
 		float quantity = productQuantity.getQuantity();
 		if (productQuantity.getUnits() == Unit.COUNT)
 			sizeString += ((int) quantity);
 		else
 			sizeString += quantity;
 		getView().setSizeValue(sizeString);
 
 		getView().setSupply(target.getSupply());
 	}
 
 }
