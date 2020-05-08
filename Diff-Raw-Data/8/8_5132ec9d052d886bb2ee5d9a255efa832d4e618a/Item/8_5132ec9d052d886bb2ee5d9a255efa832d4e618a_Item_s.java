 package model.item;
 
 import model.common.IModel;
 import model.common.Barcode;
 import model.common.Model;
 import model.product.Product;
 import model.product.ProductVault;
 import model.reports.Ivisitor;
 import common.Result;
 
 import org.joda.time.DateTime;
 
 /**
  * The Item class encapsulates all the funtions and data associated with a "Item".
  * It extends the {@link gui.item.ItemData ItemData} 
  * 	class which contains getters and setters for the various datas.
  */
 public class Item extends Model{
 
 		/**
 	 * A unique ID is associated with every Item once it is presisted to the vault.
 	 * _id is not set by the user, but by the vault when it is saved.
 	 * _id can be -1 if it is new and has not been saved
 	 */
     private int _productId;
 
     private Barcode _barcode;
 
    private DateTime _entryDate;
 
     private DateTime _exitDate;
     
     /* This is the earliest date that the entry date can be. */
     private static DateTime _entryDateLowerLimit = new DateTime(1999, 12, 31, 23, 59, 59);
     
 
 	/**
 	 * Constructor
 	 */
 	public Item(){
 		//super();
 		_id = -1;
 		_valid = false;
 		_saved = false;
 	}
 
     /**
      *  Copy Constructor
      */
     public Item(Item i){
         assert i != null;
         _id = i.getId();
         _valid = false;
         _saved = false;
         _deleted = i.isDeleted();
         _productId = i.getProductId();
         _barcode = i.getBarcode();
         _entryDate = i.getEntryDate();
         _exitDate = i.getExitDate();
     }
 
     /**
      *  Stores in the Item the ID of the Product to which the Item belongs.
      */
     public Result setProductId(int id){
         _productId = id;
         invalidate();
         return new Result(true);
     }
 
     /**
      *  Returns a copy of the Product to which this Item belongs.
      */
     public Product getProduct(){
         return _productVault.get(_productId);
     }
 
     /**
      *  Returns the ID of the Product to which this Item belongs.
      */
     public int getProductId(){
         return _productId;
     }
 
     /**
      *  Sets the Barcode instance belonging to this Item.
      */
     protected Result setBarcode(Barcode b){
         assert b != null;
         _barcode = b;
         return new Result(true, "Barcode set successfully.");
     }
 
     /**
      *  Returns the barcode belonging to this Item.
      */
     public Barcode getBarcode(){
         return _barcode;
     }
 
     /**
      *  Sets the entry date for this Item.
      */
     public Result setEntryDate(DateTime d){
         _entryDate = d;
         invalidate();
         return new Result(true);
     }
 
     /**
      *  Returns a reference to the entry date for this Item.
      */
     public DateTime getEntryDate(){
         return _entryDate;
     }
     
     /**
      * Return the entry date in a shortened string format.
      */
     public String getShortEntryDateString(){
     		return _entryDate.toLocalDate().toString("MM-dd-yy");
     }
 
     /**
      *  Set the exit date for this Item.
      */
     public Result setExitDate(DateTime d){
         assert d != null;
         _exitDate = d;
         invalidate();
         return new Result(true);
     }
 
     /**
      *  Return the exit date of this Item.
      */
     public DateTime getExitDate(){
         return _exitDate;
     }
 
     /**
      *  Return the expiration date for the Item.
      */
     public DateTime getExpirationDate(){
     	assert this.getProduct() != null;
         return getEntryDate().plusMonths(getProduct().getShelfLife());
     }
     
     /**
      * Return the expiration date in a shortened string format.
      */
     public String getShortExpirationDateString(){
     		return getExpirationDate().toLocalDate().toString("MM-dd-yy");
     }
 
 	/**
 	 * If the Item is valid it is saved into the vault.
 	 */
 	public Result save(){
 		if(!this._valid && !this.validate().getStatus())
             return new Result(false, "Item must be valid before saving.");
         if(getId() == -1)
             return _itemVault.saveNew(this);
         else
             return _itemVault.saveModified(this);
 	}
 
 	/**
 	 * Validate that the Item is able to be saved into the vault.
 	 */
 	public Result validate(){
         if (!isValidEntryDate()){
         	return new Result(false);
         }
         if(getId() == -1)
             return _itemVault.validateNew(this);
         else
             return _itemVault.validateModified(this);
 	}
 	
 	private boolean isValidEntryDate() {
 		DateTime d = getEntryDate();
 		if (d.isBefore(_entryDateLowerLimit)){
 			return false;
 		}else if(d.isAfter(new DateTime())){
 			return false;
 		}else{
 			return true;
 		}
 	}
 	
     @Override
 	public Result isDeleteable() {
 		return new Result(true);
 	}
 
 	/**
      * Return the string of the Barcode for the Product that this Item belongs
      * to.
      */
     public String getProductBarcode(){
     	return this.getProduct().getBarcodeString();
     }
 
     /**
      * Return the description of the Item.
      */
     public String getProductDescription(){
     	return this.getProduct().getDescription();
     }
 
     /**
      * Return the ID of the StorageUnit that holds this Item.
      */
     public String getProductStorageUnitId(){
     	return Integer.toString(this.getProduct().getStorageUnitId());
     }
     
     /**
      * Return the ID of the StorageUnit that holds this Item.
      */
     public int getProductStorageUnitIdInt(){
     	return this.getProduct().getStorageUnitId();
     }
     
     /**
      * Return the name of the name of the storage unit that holds this item.
      */
     public String getProductStorageUnitName() {
     		return this.getProduct().getStorageUnit().getName();
     }
     
     /**
      * Return the name of the product group that holds this item.
      */
     public String getProductProductGroupName() {
     		return this.getProduct().getProductContainerName();
     }
 
 	public String getBarcodeString() {
 		return this.getBarcode().toString();
 	}
 	
 	/**
 	 * Fills this item with some test data to prepare it for JUnit tests.
 	 * @return 
 	 */
 	public Item generateTestData(){
 		/* Make a product to put this in */
         setBarcode(new Barcode());
         setEntryDate(new DateTime());
         setExitDate(new DateTime());
         return this;
 	}
 
 	/**
 	 * Associates this item with a product.
 	 * @param product the product to which this item should be associated.
 	 */
 	public void setProduct(Product product) {
 		this.setProductId(product.getId());
 	}
 
 	/**
 	 * Removes this item from its vault completely, and sets it to an unsaved state.
 	 */
 	public void obliterate() {
 		_itemVault.obliterate(this);
 		this._saved = false;
 		this._id = -1;
 	}
 	
 	public void accept(Ivisitor visitor){
 		visitor.visit(this);
 	}
 }
