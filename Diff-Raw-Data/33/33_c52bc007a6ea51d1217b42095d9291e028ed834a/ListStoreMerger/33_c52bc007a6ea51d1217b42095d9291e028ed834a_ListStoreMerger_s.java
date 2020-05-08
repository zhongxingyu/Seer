 package org.mule.galaxy.web.client.ui.store;
 
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Map;
 
 import com.extjs.gxt.ui.client.data.ModelData;
 import com.extjs.gxt.ui.client.store.ListStore;
 
 /**
  *
  * Base class implementing {@link ListStore} merging logic.
  *
  * @param <M>
  * @param <T>
  */
 public abstract class ListStoreMerger<M extends ModelData, T> {
 
     private final ListStore<M> store;
 
     public ListStoreMerger(final ListStore<M> store) {
         this.store = store;
     }
 
     public void merge(final Collection<T> objects) {
         final Map<String, T> filteredObjects = new HashMap<String, T>();
         for (final T object : objects) {
             if (!filter(object)) {
                 final String id = extractIdentifier(object);
                 final T previousValue = filteredObjects.put(id, object);
                 if (previousValue != null) {
                     throw new IllegalStateException("Found several values for id <"+id+">");
                 }
             }
         }
         for (final M model : this.store.getModels()) {
             final T object = filteredObjects.remove(extractIdentifier(model));
             if (object != null && isValid(object)) {
                if (hasBeenUpdated(object)) {
                    this.store.update(model);
                 }
             } else {
                 this.store.remove(model);
             }
         }
         for (final T object : filteredObjects.values()) {
             if (isValid(object)) {
                 this.store.add(createModel(object));
             }
         }
         this.store.commitChanges();
     }
 
     protected abstract M createModel(final T object);
 
     /**
      * @param object
      * @return true to not consider specified object. Called first things once per object.
      */
     protected boolean filter(final T object) {
         return false;
     }
 
     protected abstract String extractIdentifier(final T object);
 
     protected abstract String extractIdentifier(final M model);
 
     /**
      * @param object
      * @return true if specified object should be store in {@link ListStore}. Usefull when an updated object must be removed from {@link ListStore}.
      */
     protected boolean isValid(final T object) {
         return true;
     }
 
     /**
      * @param object
      * @return true if updated object has changed and must trigger a {@link ListStore#update(ModelData)} operation.
      */
    protected boolean hasBeenUpdated(final T object) {
         return false;
     }
 
 }
