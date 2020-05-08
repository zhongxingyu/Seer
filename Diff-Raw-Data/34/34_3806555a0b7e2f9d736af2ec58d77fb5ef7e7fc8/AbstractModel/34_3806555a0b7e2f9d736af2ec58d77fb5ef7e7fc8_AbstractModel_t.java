 package de.aidger.model;
 
 import static de.aidger.utils.Translation._;
 
 import java.util.List;
 import java.util.Vector;
 import java.util.Observable;
 import java.text.MessageFormat;
 
 import de.unistuttgart.iste.se.adohive.controller.AdoHiveController;
 import de.unistuttgart.iste.se.adohive.controller.IAdoHiveManager;
 import de.unistuttgart.iste.se.adohive.exceptions.AdoHiveException;
 import de.unistuttgart.iste.se.adohive.model.IAdoHiveModel;
 
 import de.aidger.model.validators.*;
 import de.aidger.utils.Logger;
 
 /**
  * AbstractModel contains all important database related functions which all
  * models need to contain. This includes getting instances of models and saving
  * or removing them.
  * 
  * @author Philipp Gildein
  */
 public abstract class AbstractModel<T> extends Observable implements
         IAdoHiveModel<T> {
 
     /**
      * The unique id of the model in the database.
      */
     protected int id = -1;
 
     /**
      * Used to cache the AdoHiveManager after getting it the first time.
      */
     protected IAdoHiveManager manager = null;
 
     /**
      * Array containing all validators for that specific model.
      */
     protected List<Validator> validators = new Vector<Validator>();
 
     /**
      * Cloneable function inherited from IAdoHiveModel.
      *
      * @return Clone of the model
      */
     @Override
     abstract public T clone();
 
     /**
      * Get all models from the database.
      * 
      * @return An array containing all found models or null
      */
     @SuppressWarnings("unchecked")
     public List getAll() {
         try {
             return getManager().getAll();
         } catch (Exception ex) {
         }
         return null;
     }
 
     /**
      * Get a specific model by specifying its unique id.
      * 
      * @param id
      *            The unique id of the model
      * @return The model if one was found or null
      */
     @SuppressWarnings("unchecked")
     public T getById(int id) {
         try {
             return (T) getManager().getById(id);
         } catch (Exception ex) {
         }
         return null;
     }
 
     /**
      * Get a specific model by specifying a set of keys.
      * 
      * @param o
      *            The set of keys specific to this model
      * @return The model if one was found or null
      */
     @SuppressWarnings("unchecked")
     public T getByKeys(Object... o) {
         try {
             return (T) getManager().getByKeys(o);
         } catch (Exception ex) {
         }
         return null;
     }
 
     /**
      * Save the current model to the database.
      *
      * @return True if validation succeeds
      */
     @SuppressWarnings("unchecked")
     public boolean save() throws AdoHiveException {
         if (!doValidate()) {
             return false;
         }
 
         /* Add or update model */
         IAdoHiveManager mgr = getManager();
         if (id == -1) {
             mgr.add(this);
         } else {
             mgr.update(this);
         }
         setChanged();
         notifyObservers();
 
         return true;
     }
 
     /**
      * Remove the current model from the database.
      */
     @SuppressWarnings("unchecked")
     public void remove() throws AdoHiveException {
         if (id != -1) {
             getManager().remove(this);
             clearChanged();
             notifyObservers();
         }
     }
 
     /**
      * Add a validator to the model.
      *
      * @param valid
      *            The validator to add
      */
     public void validate(Validator valid) {
         validators.add(valid);
     }
 
     /**
      * Add a presence validator to the model.
      *
      * @param members
      *            The name of the member variables to validate
      */
     public void validatePresenceOf(String[] members) {
         validators.add(new PresenceValidator(this, members));
     }
 
     /**
      * Add an email validator to the model.
      *
      * @param member
      *            The name of the member variable to validate
      */
     public void validateEmailAddress(String member) {
         validators.add(new EmailValidator(this, new String[] { member }));
     }
 
     /**
      * Add an date range validator to the model.
      *
      * @param from
      *            The starting date
      * @param to
      *            The end date
      */
     public void validateDateRange(String from, String to) {
         validators.add(new DateRangeValidator(this, from, to));
     }
 
     /**
      * Extract the name of the class and return the correct manager.
      * 
      * @return The name of the model class
      */
     @SuppressWarnings("unchecked")
     protected IAdoHiveManager getManager() {
         if (manager == null) {
             /* Extract the name of the class */
             String classname = getClass().getName();
             int idx = classname.lastIndexOf('.');
            classname = classname.substring(idx + 1);
 
             /* Try to get the correct manager from the AdoHiveController */
             try {
                 java.lang.reflect.Method m = AdoHiveController.class.getMethod(
                         "get" + classname + "Manager");
                 manager = (IAdoHiveManager) m.invoke(AdoHiveController.getInstance(),
                         new Object[0]);
             } catch (Exception ex) {
                 manager = null;  // Make sure that manager is really null!
                 Logger.error(MessageFormat.format(
                         _("Could not get manager for class \"{0}\". Error: {1}"),
                         new Object[] { classname, ex.getMessage() }));
             }
         }
 
         return manager;
     }
 
     /**
      * Validate the input using the validators and a custom validate function.
      *
      * @return True if everything validates
      */
     protected boolean doValidate() {
         /* Try to validate before adding/updating */
         boolean ret = true;
         for (Validator v : validators) {
             if (!v.validate()) {
                 ret = false;
             }
         }
 
         /* Check if the model got a validate() function */
         try {
             java.lang.reflect.Method m = getClass().getDeclaredMethod("validate");
             if (!(Boolean)m.invoke(this, new Object[0]))
                 ret = false;
         } catch (Exception ex) { }
 
         return ret;
     }
 }
