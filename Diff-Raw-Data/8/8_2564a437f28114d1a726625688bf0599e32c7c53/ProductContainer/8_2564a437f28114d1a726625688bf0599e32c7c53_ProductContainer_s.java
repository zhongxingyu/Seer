 package model.productcontainer;
 
 import java.util.ArrayList;
 
 import model.common.IModel;
 import model.common.Model;
 import model.product.Product;
 import common.Result;
 
 /**
  * The ProductContainer is the base class for 
  * {@link ProductGroup ProductGroup} and for
  * {@link StorageUnit StorageUnit}. It defines the shared
  * interface for these objects.
  */
 public class ProductContainer extends Model{
 
     /**
      * A string that is non-empty and must be unique among its vault context.
      */
     private String _name;
 
     /**
      * A unique ID is associated with every ProductContainer once it is presisted to the vault.
      * _id is not set by the user, but by the vault when it is saved.
      * _id can be -1 if it is new and has not been saved
      */
 
     /* A pointer to the StorageUnit that holds this group. Can be the same
 	 * as _parentId */
     protected int _rootParentId;
     
     
     /**
      * Constructor
      */
     public ProductContainer(){
     	super();
         _name = "";
         _id = -1;
         _valid = false;
         _saved = false;
         _deleted = false;
         _rootParentId = -1;
     }
 
     /**
      * Copy Constructor
      */
     public ProductContainer(ProductContainer p){
         assert p != null : "I wanted a ProductContainer, and I found a null!";
         _id = p.getId();
         _valid = p.isValid();
         _saved = p.isSaved();
         _deleted = p.isDeleted();
         _name = p.getName();
         _rootParentId = p.getRootParentId();
     }
 
 
     /**
      * Return the ID of the StorageUnit.
      */
     public int getRootParentId(){
         return _rootParentId;
     }
 
     /**
      * Returns a copy of the storage unit that this ProductGroup is contained
      * in.
      */
     public ProductContainer getRootParent(){
         return _storageUnitVault.get(_rootParentId);
     }
 
     /**
      * Set this to the ID of the StorageUnit that this ProductGroup belongs to.
      */
     public Result setRootParentId(int id){
         assert true;
         _rootParentId = id;
         invalidate();
         return new Result(true);
     }
 
     /**
      * Returns the String name of the ProductCointainer.
      */
     public String getName() {
         return _name;
     }
     
     public String getLowerCaseName(){
     	return _name.toLowerCase();
     }
     
     
 
     /**
      * Sets the name of the ProductContainer, invalidating it as well so that a 
      * subsequent save must be validated first.
      */
     public Result setName(String _name) {
         assert _name != null;
         this._name = _name;
         invalidate();
         return new Result(_name==this._name);
     }
 
 
     /**
      * If the ProductContainer is valid it is saved into the vault.
      */
     public Result save(){
         assert true;
         return new Result(false, "THIS METHOD SHOULD BE OVERRIDDEN");
     }
 
     /**
      * Validate that the ProductContainer is able to be saved into the vault.
      */
     public Result validate(){
         assert true;
         return new Result(false, "THIS METHOD SHOULD BE OVERRIDDEN");
     }
 
     
     public ArrayList<ProductGroup> getChildProductGroups(){
     	ArrayList<ProductGroup> childProductGroups = _productGroupVault.findAll("ParentIdString = "+this.getId());
     	return childProductGroups;
     }
     /**
      * Indicates if the object is able to be deleted
      */
     public Result isDeletable(){
     	//Product Container can not have items
     	ArrayList<Product> products;
     	products = _productVault.findAll("ContainerId = "+this.getId());
     	for(Product tempProduct : products){
     		if(_itemVault.find("ProductId = "+tempProduct.getId()) != null)
     			return new Result(false);
     	}
     	for(ProductGroup tempPG : this.getChildProductGroups()){
     		if(tempPG.isDeletable().getStatus() == false)
     			return new Result(false);;
     	}
     	
     	
     	
         return new Result(true);
     }
 }
