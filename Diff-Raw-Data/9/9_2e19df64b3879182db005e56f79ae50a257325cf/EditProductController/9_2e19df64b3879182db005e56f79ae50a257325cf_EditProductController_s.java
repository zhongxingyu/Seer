 package gui.product;
 
 import common.Result;
 import gui.common.*;
 import model.common.Size;
 import model.product.Product;
 import model.product.ProductVault;
 import org.joda.time.DateTime;
 
 /**
  * Controller class for the edit product view.
  */
 public class EditProductController extends Controller 
 										implements IEditProductController {
 
     ProductData target;
 
 	/**
 	 * Constructor.
 	 * 
 	 * @param view Reference to the edit product view
 	 * @param target Product being edited
 	 */
 	public EditProductController(IView view, ProductData target) {
 		super(view);
         getView().setBarcode(target.getBarcode());
         getView().enableBarcode(false);
         getView().setDescription(target.getDescription());
         getView().setShelfLife(target.getShelfLife());
        //getView().setSizeUnit();
        //getView().setSizeValue();
         getView().setSupply(target.getSupply());
         this.target = target;
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
 	protected IEditProductView getView() {
 		return (IEditProductView)super.getView();
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
         getView().enableSizeValue(true);
         if(getView().getSizeUnit().name().equals( "Count" )){
             getView().enableSizeValue(false);
             getView().setSizeValue("1");
         }
         try{
             if(Float.parseFloat(getView().getSizeValue()) < 0){
                 getView().enableOK(false);
                 return;
             }
 
             if(Integer.parseInt(getView().getShelfLife()) < 0){
                 getView().enableOK(false);
                 return;
             }
 
             if(Integer.parseInt(getView().getSupply()) < 0){
                 getView().enableOK(false);
                 return;
             }
         } catch (Exception e){
             getView().enableOK(false);
             return;
         }
         getView().enableOK(!getView().getDescription().isEmpty());
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
 	// IEditProductController overrides
 	//
 
 	/**
 	 * This method is called when any of the fields in the
 	 * edit product view is changed by the user.
 	 */
 	@Override
 	public void valuesChanged() {
         enableComponents();
 	}
 	
 	/**
 	 * This method is called when the user clicks the "OK"
 	 * button in the edit product view.
 	 */
 	@Override
 	public void editProduct() {
         Product product = ProductVault.getInstance().get((Integer) target.getTag());
         product.set3MonthSupply(Integer.parseInt(getView().getSupply()));
         product.setDescription(getView().getDescription());
         product.setShelfLife(Integer.parseInt(getView().getShelfLife()));
         product.setSize(new Size(Float.parseFloat(getView().getSizeValue()),getView().getSizeUnit().toString()));
         Result r = product.validate();
         if(!r.getStatus())
             getView().displayErrorMessage(r.getMessage());
         r = product.save();
         if(!r.getStatus())
             getView().displayErrorMessage(r.getMessage());
 
     }
 
 }
 
