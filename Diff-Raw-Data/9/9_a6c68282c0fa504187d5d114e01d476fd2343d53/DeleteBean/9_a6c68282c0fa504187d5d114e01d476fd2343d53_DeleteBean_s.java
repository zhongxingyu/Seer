 package dk.statsbiblioteket.doms.gui;
 
 import dk.statsbiblioteket.doms.client.exceptions.ServerOperationFailed;
 import dk.statsbiblioteket.doms.client.exceptions.XMLParseException;
 import dk.statsbiblioteket.doms.client.objects.DigitalObject;
 import dk.statsbiblioteket.doms.client.relations.ObjectRelation;
 import dk.statsbiblioteket.doms.client.utils.Constants;
 import org.jboss.seam.ScopeType;
 import org.jboss.seam.annotations.In;
 import org.jboss.seam.annotations.Logger;
 import org.jboss.seam.annotations.Name;
 import org.jboss.seam.annotations.Scope;
 import org.jboss.seam.faces.FacesMessages;
 import org.jboss.seam.log.Log;
 
 import javax.faces.application.FacesMessage;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Set;
 
 /**
  * Created with IntelliJ IDEA.
  * User: abr
  * Date: 3/20/13
  * Time: 11:11 AM
  * To change this template use File | Settings | File Templates.
  */
 @Name(value = "deleteBean")
 @Scope(ScopeType.PAGE)
 public class DeleteBean {
 
     @In(create = true)
     private DomsManagerBean domsManager;
 
     @Logger
     private Log logger;
 
 
     public DeleteBean() {
 
     }
 
 
 
     public String deleteSelectedObject() throws ServerOperationFailed, XMLParseException {
         DigitalObject deleteObject = domsManager.getSelectedDataObject();
        Set<DigitalObject> viewKasse = getViewList(domsManager.getRootDataObject());
 
         boolean deletedAnything = recursiveDelete(deleteObject,Collections.unmodifiableSet(viewKasse));
         if (deletedAnything){
             domsManager.reload();
            if (domsManager.getSelectedDataObject().equals(domsManager.getRootDataObject())){//we deleted root
                 domsManager.setRootDataObject(null);
                 domsManager.setSelectedDataObject(null);
                 FacesMessages.instance().add(new FacesMessage("Record deleted successfully"));
                 return "/search.xhtml";
             } else {
                domsManager.setSelectedDataObject(domsManager.getRootDataObject());
                 FacesMessages.instance().add(new FacesMessage("Element(s) deleted successfully"));
                 return "/editRecord.xhtml";
             }
         } else {
             FacesMessages.instance().add(new FacesMessage("Cannot delete object(s) because another record refers to the" +
                     " root object"));
             return "/editRecord.xhtml";
         }
     }
 
     private boolean recursiveDelete(DigitalObject deleteObject, final Set<DigitalObject> viewKasse)
             throws ServerOperationFailed, XMLParseException {
         boolean deletedAnything = false;
 
         List<ObjectRelation> inverseRels = deleteObject.getInverseRelations();
         boolean hasExtRel = false;
         for (ObjectRelation inverseRel : inverseRels) {
             //TODO check lige om getSubject er initialised og om den vil svare korrekt på initialised
             if (viewKasse.contains(inverseRel.getSubject())) {
                 if (inverseRel.getSubject().getState() != Constants.FedoraState.Deleted) {
                     inverseRel.remove();
                     inverseRel.getSubject().save();
                     deletedAnything = true;
                 }
             } else {
                 hasExtRel = true;
             }
         }
         if (!hasExtRel) {
             deleteObject.setState(Constants.FedoraState.Deleted);
             deleteObject.save();
             deletedAnything = true;
             for (DigitalObject child : deleteObject.getChildObjects(Constants.VIEW_GUI)) {
                 recursiveDelete(child, viewKasse);
             }
         }
         return deletedAnything;
     }
 
 
 
     private Set<DigitalObject> getViewList(DigitalObject dataObject) throws ServerOperationFailed {
         List<DigitalObject> found = new LinkedList<DigitalObject>();
         Set<DigitalObject> result = new HashSet<DigitalObject>();
         found.add(dataObject);
         result.add(dataObject);
         for (int i = 0; i < found.size(); i++) {
             Set<DigitalObject> childObjects = found.get(i).getChildObjects(Constants.VIEW_GUI);
             for (DigitalObject childObject : childObjects) {
                 if (result.add(childObject)) {
                     found.add(childObject);
                 }
             }
         }
         return result;
     }
 }
