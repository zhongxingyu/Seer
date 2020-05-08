 package interiores.business.controllers;
 
 import horarios.shared.ElementNotFoundException;
 import interiores.business.exceptions.DefaultCatalogOverwriteException;
 import interiores.business.exceptions.ElementNotFoundBusinessException;
 import interiores.business.exceptions.NoRoomCreatedException;
 import interiores.business.models.FurnitureType;
 import interiores.business.models.Room;
 import interiores.business.models.WantedFurniture;
 import interiores.business.models.WishList;
 import interiores.business.models.catalogs.NamedCatalog;
 import interiores.core.business.BusinessController;
 import interiores.core.business.BusinessException;
 import interiores.core.data.JAXBDataController;
 import interiores.utils.Range;
 import java.util.Collection;
 import java.util.List;
 
 /**
  *
  * @author hector
  */
 public class FurnitureTypeController
     extends CatalogElementController<FurnitureType>
 {
     public FurnitureTypeController(JAXBDataController data) {
         super(data, FurnitureTypesCatalogController.getCatalogTypeName());
     }
     
     public void add(String name, int minWidth, int maxWidth, int minDepth, int maxDepth)
             throws DefaultCatalogOverwriteException
     {
         Range widthRange = new Range(minWidth, maxWidth);
         Range depthRange = new Range(minDepth, maxDepth);
         
         FurnitureType toAdd = new FurnitureType(name, widthRange, depthRange);
         
         super.add(toAdd);
     }
     
    public void select(String name) throws ElementNotFoundException {
        WantedFurniture wf = new WantedFurniture(getActiveCatalog().getObject(name));
         getWishList().addWantedFurniture(wf);
     }
     
     public void unselect(String name) throws BusinessException, ElementNotFoundException {
         getWishList().removeWantedFurniture(name);
     }
     
     public String getNameActiveCatalog() {
         return getActiveCatalog().getName();
     }
     
     public Collection getRoomFurniture() {
         return getWishList().getFurnitureNames();
     }
     
     private WishList getWishList() {
         return (WishList) data.get("wishList");
     }
     
     private Room getRoom() throws NoRoomCreatedException {
         if(! data.has("room"))
             throw new NoRoomCreatedException();
         
         return (Room) data.get("room");
     }
 }
