 package gui.productgroup;
 
import common.util.StringUtils;
 import gui.common.*;
 import gui.inventory.*;
 import model.common.Size;
 import model.productcontainer.ProductGroup;
 import model.productcontainer.ProductGroupVault;
 
 /**
  * Controller class for the edit product group view.
  */
 public class EditProductGroupController extends Controller 
 										implements IEditProductGroupController {
 
     private ProductContainerData target;
 	
 	/**
 	 * Constructor.
 	 * 
 	 * @param view Reference to edit product group view
 	 * @param target Product group being edited
 	 */
 	public EditProductGroupController(IView view, ProductContainerData target) {
 		super(view);
         this.target = target;
 		getView().setProductGroupName(target.getName());
         ProductGroup pg = ProductGroupVault.getInstance().get((Integer)target.getTag());
         getView().setSupplyValue(String.valueOf(pg.get3MonthSupply().getAmount()));
        getView().setSupplyUnit(SizeUnits.valueOf(StringUtils.capitalize(pg.get3MonthSupply().getUnit())));
         construct();
 
 	}
 
 	//
 	// Controller overrides
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
 		return (IEditProductGroupView)super.getView();
 	}
 
 	/**
 	 * Sets the enable/disable state of all components in the controller's view.
 	 * A component should be enabled only if the user is currently
 	 * allowed to interact with that component.
 	 * 
 	 * {@pre None}
 	 * 
 	 * {@post The enable/disable state of all components in the controller's view
 	 * have been set appropriately.}
 	 */
 	@Override
 	protected void enableComponents() {
         ProductGroup pg = ProductGroupVault.getInstance().get(((Number)target.getTag()).intValue());
         float size = 0;
         try {
             size = Float.parseFloat(getView().getSupplyValue());
         } catch(Exception e ){
             getView().enableOK(false);
             return;
         }
         pg.set3MonthSupply(new Size(size, getView().getSupplyUnit().toString()));
         pg.setName(getView().getProductGroupName());
         getView().enableOK(pg.validate().getStatus());
 	}
 
 	/**
 	 * Loads data into the controller's view.
 	 * 
 	 *  {@pre None}
 	 *  
 	 *  {@post The controller has loaded data into its view}
 	 */
 	@Override
 	protected void loadValues() {
 	}
 
 	//
 	// IEditProductGroupController overrides
 	//
 
 	/**
 	 * This method is called when any of the fields in the
 	 * edit product group view is changed by the user.
 	 */
 	@Override
 	public void valuesChanged() {
         enableComponents();
 	}
 	
 	/**
 	 * This method is called when the user clicks the "OK"
 	 * button in the edit product group view.
 	 */
 	@Override
 	public void editProductGroup() {
         ProductGroup pg = ProductGroupVault.getInstance().get(((Number)target.getTag()).intValue());
         float size = Float.parseFloat(getView().getSupplyValue());
         pg.set3MonthSupply(new Size(size, getView().getSupplyUnit().toString()));
         pg.setName(getView().getProductGroupName());
         pg.validate();
         pg.save();
 	}
 
 }
 
