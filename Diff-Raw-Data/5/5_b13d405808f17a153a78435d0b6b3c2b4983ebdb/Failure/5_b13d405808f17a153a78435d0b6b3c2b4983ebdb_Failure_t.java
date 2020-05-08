 package es.upm.dit.gsi.shanks.model.failure;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.logging.Logger;
 
 import es.upm.dit.gsi.shanks.model.element.NetworkElement;
 import es.upm.dit.gsi.shanks.model.element.exception.UnsupportedNetworkElementStatusException;
 import es.upm.dit.gsi.shanks.model.failure.exception.UnsupportedElementInFailureException;
 
 /**
  * DeviceErrors class
  * 
  * Make the possible errors of the devices
  * 
  * @author Daniel Lara
  * @author a.carrera
  * @version 0.1.1
  * 
  */
 
 public abstract class Failure {
 
     private Logger logger = Logger.getLogger(Failure.class.getName());
 
     private String id;
     private boolean active;
 
     private List<NetworkElement> affectedElements;
     private HashMap<Class<? extends NetworkElement>, HashMap<String, Object>> possibleAffectedElements;
 
     private HashMap<String, Object> properties;
     private HashMap<String, Object> hash = new HashMap<String, Object>();
     
     private double occurrenceProbability;
 
     /**
      * Constructor of the class
      * 
      * @param id
      * @param occurrenceProbability
      */
     public Failure(String id, double occurrenceProbability) {
         this.id = id+"_"+System.currentTimeMillis();
         this.occurrenceProbability = occurrenceProbability;
         this.affectedElements = new ArrayList<NetworkElement>();
         this.possibleAffectedElements = new HashMap<Class<? extends NetworkElement>, HashMap<String, Object>>();
         this.active = false;
         this.properties = new HashMap<String, Object>();
 
 
         this.addPossibleAffectedElements();
         logger.finer("New Failure: "+this);
     }
 
     /**
      * Add classes using addPossibleAffectedElements method
      */
     public abstract void addPossibleAffectedElements();
 
     /**
      * @return the id
      */
     public String getID() {
         return id;
     }
 
     /**
      * @return the occurrenceProbability
      */
     public double getOccurrenceProbability() {
         return occurrenceProbability;
     }
 
     /**
      * @param occurrenceProbability
      *            the occurrenceProbability to set
      */
     public void setOccurrenceProbability(double occurrenceProbability) {
         this.occurrenceProbability = occurrenceProbability;
     }
     
     
     
     public void activateFailure() throws UnsupportedNetworkElementStatusException{
         if(!this.active){
             List<? extends NetworkElement> elements = this.affectedElements;
             for(NetworkElement e : elements){
                 for(Class<?> c : possibleAffectedElements.keySet()){
                     if(e.getClass().equals(c)){
                        for(String s : possibleAffectedElements.get(c).keySet()){
                            if(e.getStatus().containsKey(s)){
                                e.updateStatusTo(s, (Boolean) possibleAffectedElements.get(c).get(s));
                            }else if(e.getProperties().containsKey(s)){
                                e.updatePropertyTo(s, possibleAffectedElements.get(c).get(s));
                            }
                        }
                     }
                 }
             e.checkStatus();
             }
             this.active = true;
       
         }
     }
 
 //    /**
 //     * Used to activate a failure. All elements will be set with the affected
 //     * status.
 //     * 
 //     */
 //    public void activateFailure(){
 //        if (!this.active) {
 //            List<? extends NetworkElement> elements = this.affectedElements;
 //            List<String> status = new ArrayList<String>();
 //            HashMap<String, Boolean> oldStatus = new HashMap<String, Boolean>();
 //            for (NetworkElement element : elements) {
 //                try{
 //                        for(String s : element.getStatus().keySet()){
 //                                status.add(s);
 //                            }
 //                        for(String state : status){
 //                           oldStatus.put(state, element.getStatus().get(state)); 
 //                        }
 //                    }catch (Exception e) {
 //                        logger.severe("Exception setting status: "
 //                                + affectedElements.toString() + " in element "
 //                                + element.getID() + ". Exception: "
 //                                + e.getMessage());
 //                    }
 //                }
 //            this.active = true;
 //        } else {
 //            logger.info("Failure " + this.getID() + " is already active.");
 //        }
 //
 //    }
 
 //    /**
 //     * Used to deactivate a failure. All current affected elements will restore
 //     * the old status.
 //     * 
 //     */
 //    public void deactivateFailure() {
 //        if (this.active) {
 //            Set<? extends NetworkElement> elements = this.affectedElements
 //                    .keySet();
 //            for (NetworkElement element : elements) {
 //                try {
 //                    String oldStatus = this.oldStatesOfAffectedElements
 //                            .get(element);
 //                    element.setCurrentStatus(oldStatus);
 //                } catch (UnsupportedNetworkElementStatusException e) {
 //                    logger.severe("Exception setting status: "
 //                            + affectedElements.get(element) + " in element "
 //                            + element.getID() + ". Exception: "
 //                            + e.getMessage());
 //                }
 //                this.oldStatesOfAffectedElements.remove(element);
 //            }
 //            this.active = false;
 //        } else {
 //            logger.info("Failure " + this.getID() + " is not active.");
 //        }
 //
 //    }
 
     /**
      * @return true if failure is active, false if not
      */
     public boolean isActive() {
         return active;
     }
 
     /**
      * @return the currentAffectedElements if the failure is active, null if not
      */
     public List<NetworkElement> getCurrentAffectedElements() {
         if (this.active) {
             return affectedElements;
         } else {
             return new ArrayList<NetworkElement>();
         }
     }
 
     /**
      * @return the affectedElements
      */
     public List<NetworkElement> getAffectedElements() {
         return affectedElements;
     }
 
     /**
      * @param element
      *            The element that will be affected by this failure when the
      *            failure will be active
      * @param status
      *            The element status that will be set when the failure will be
      *            active
      * @throws UnsupportedElementInFailureException
      */
     public void addAffectedElement(NetworkElement element, String status, boolean value)
             throws UnsupportedElementInFailureException {
         Class<? extends NetworkElement> c = element.getClass();
         if (this.possibleAffectedElements.containsKey(c)
                 && element.getPossibleStates().contains(status)){
             element.getStatus().put(status, value);
             if(!affectedElements.contains(element)){
                 affectedElements.add(element);
             }
 //        }else if(this.possibleAffectedElements.containsKey(c)
 //                && element.getProperties().keySet().contains(affected)){
 //            element.getProperties().put(affected, value);
 //            if(!affectedElements.contains(element)){
 //                affectedElements.add(element);
 //            }
         } else {
             throw new UnsupportedElementInFailureException(element);
         }
     }
     
     
     /**
      * This method change a property with the selected value 
      * when a failure affect that property
      * 
      * @param element
      *          The element which change its property
      * @param property
      *          The property that the failure will change
      * @param value
      *          The new value of the property
      * @throws UnsupportedElementInFailureException
      */
     public void addAffectedPropertiesOfElement(NetworkElement element, String property, Object value)
     throws UnsupportedElementInFailureException {
         Class<? extends NetworkElement> c = element.getClass();
             if (this.possibleAffectedElements.containsKey(c)
                     && element.getProperties().containsKey(property)){
                 element.getProperties().put(property, value);
                 if(!affectedElements.contains(element)){
                         affectedElements.add(element);
                     }
             } else {
                 throw new UnsupportedElementInFailureException(element);
             }     
     }
 
     /**
      * Remove this element, but not modify the status. When the failure will be
      * deactive, this removed element will keep the actual status
      * 
      * @param element
      */
     public void removeAffectedElement(NetworkElement element) {
         this.affectedElements.remove(element);
     }
 
     /**
      * @return the possibleAffectedElements
      */
     public HashMap<Class<? extends NetworkElement>, HashMap<String, Object>> getPossibleAffectedElements() {
         return possibleAffectedElements;
     }
 
     /**
      * @param c
      */
     public void addPossibleAffectedElements(Class<? extends NetworkElement> c, String status, boolean value) {
         hash.put(status, value);
         this.possibleAffectedElements.put(c, hash);
     }
     
     /**
      * @param c
      */
     public void addPossibleAffectedProperties(Class<? extends NetworkElement> c, String property, Object value) {
         this.properties.put(property, value);
         this.possibleAffectedElements.put(c, properties);
     }
 
     /**
      * @param elementClass
      */
     public void removePossibleAffectedElements(Class<? extends NetworkElement> elementClass) {
         if (this.possibleAffectedElements.containsKey(elementClass)) {
             this.possibleAffectedElements.remove(elementClass);
         }
     }
 
     /**
      * To know if a failure is resolved, the
      * 
      * @return true if the failure is resolved, false if not
      */
     abstract public boolean isResolved();
 //    public boolean isResolved() {
 //        // FIXME check this method, maybe... if two failures affect to one
 //        // element at the same time... the old status is a not valid check
 //        boolean resolved = false;
 //        for (NetworkElement element : this.affectedElements.keySet()) {
 //            if (element.getCurrentStatus().equals(
 //                    this.oldStatesOfAffectedElements.get(element))) {
 //                resolved = true;
 //            } else {
 //                resolved = false;
 //            }
 //        }
 //        return resolved;
 //    }
 
 }
