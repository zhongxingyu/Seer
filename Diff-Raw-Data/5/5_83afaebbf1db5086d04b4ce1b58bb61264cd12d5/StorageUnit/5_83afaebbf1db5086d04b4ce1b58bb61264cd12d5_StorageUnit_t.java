 package model.productcontainer;
 
 import common.Result;
 import model.product.Product;
 
 import java.util.ArrayList;
 
 /**
  * The StorageUnit class encapsulates all the funtions and data associated with a "StorageUnit".
  * It extends the {@link model.productcontainer.ProductContainer ProductContainer} 
  * implements the general functions for StorageUnit and for 
  * {@link ProductGroup ProductGroup}.
  */
 public class StorageUnit extends ProductContainer{
 
 
     /**
      * Constructor.
      */
     public StorageUnit(){
         super();
 
     }
 
     /**
      * Copy Constructor.
      */
     public StorageUnit(StorageUnit su){
         super(su);
         assert su != null;
     }
 
     /**
      * Uses the {@link model.productcontainer.StorageVault StorageVault} to make
      * sure that the proper data constraints for a StorageUnit instance are met.
      * This function must be called and complete successfully before this
      * instance can be saved into the vault. 
      */
     public Result validate(){
         if(getName().isEmpty())
             return new Result(false,"Name cannot be empty");
 
         if(getId() == -1)
             return storageUnitVault.validateNew(this);
         else
             return storageUnitVault.validateModified(this);
     }
 
     /**
      * Puts a copy of this instance into the {@link
      *  model.productcontainer.StorageVault StorageVault} and links it to all of
      *  its relatives. Before saving, an instance must be validated using the
      *  validate() call. */
     public Result save(){
         if(!isValid())
             return new Result(false, "Item must be valid before saving.");
         if(getId() == -1)
             return storageUnitVault.saveNew(this);
         else
             return storageUnitVault.saveModified(this);
     }
 
     /**
      * Returns a copy of the storage unit that this ProductGroup is contained
      * in.
      */
     public ProductContainer getRootParent(){
         return this;
     }
 
     /**
      * Set this to the ID of the StorageUnit that this ProductGroup belongs to.
      */
     public Result setRootParentId(int id){
         return new Result(false, "Root Parent of a Storage Unit is immutable");
    }
 }
