 package de.aidger.model;
 
 import static de.aidger.utils.Translation._;
 
 import java.text.MessageFormat;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Observable;
 import java.util.ArrayList;
 
 import de.aidger.model.validators.DateRangeValidator;
 import de.aidger.model.validators.ExistanceValidator;
 import de.aidger.model.validators.FormatValidator;
 import de.aidger.model.validators.InclusionValidator;
 import de.aidger.model.validators.PresenceValidator;
 import de.aidger.model.validators.Validator;
 import de.aidger.utils.Logger;
 import de.aidger.utils.history.HistoryEvent;
 import de.aidger.utils.history.HistoryManager;
 import de.unistuttgart.iste.se.adohive.controller.AdoHiveController;
 import de.unistuttgart.iste.se.adohive.controller.IAdoHiveManager;
 import de.unistuttgart.iste.se.adohive.exceptions.AdoHiveException;
 import de.unistuttgart.iste.se.adohive.model.IAdoHiveModel;
 import java.util.Calendar;
 
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
     protected Integer id = 0;
 
     /**
      * Determines if the model has been saved in the db yet.
      */
     private boolean isNew = true;
 
     /**
      * Used to cache the AdoHiveManagers after getting them the first time.
      */
     protected static Map<String, IAdoHiveManager> managers = new HashMap<String, IAdoHiveManager>();
 
     /**
      * Array containing all validators for that specific model.
      */
     protected List<Validator> validators = new ArrayList<Validator>();
 
     /**
      * Array containing errors if a validator fails.
      */
     protected List<String> errors = new ArrayList<String>();
 
     /**
      * Map of errors for specific fields.
      */
     protected Map<String, List<String>> fieldErrors = new HashMap<String, List<String>>();
 
     /**
      * Should the model first be removed before saveing. Needed for example for
      * HourlyWage which has several Primary Keys and needs to be removed when
      * edited.
      */
     protected boolean updatePKs = false;
 
     /**
      * The old model before any changes.
      */
     private AbstractModel<T> pkModel = null;
 
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
     public List getAll() throws AdoHiveException {
         return getManager().getAll();
     }
 
     /**
      * Get a specific model by specifying its unique id.
      *
      * @param id
      *            The unique id of the model
      * @return The model if one was found or null
      */
     @SuppressWarnings("unchecked")
     public T getById(int id) throws AdoHiveException {
         return (T) getManager().getById(id);
     }
 
     /**
      * Get a specific model by specifying a set of keys.
      *
      * @param o
      *            The set of keys specific to this model
      * @return The model if one was found or null
      */
     @SuppressWarnings("unchecked")
     public T getByKeys(Object... o) throws AdoHiveException {
         return (T) getManager().getByKeys(o);
     }
 
     /**
      * Get the number of models in the database.
      *
      * @return The number of models
      * @throws AdoHiveException
      */
     public int size() throws AdoHiveException {
         return getManager().size();
     }
 
     /**
      * Returns true if no model has been saved into the database.
      *
      * @return True if no model is in the database
      * @throws AdoHiveException
      */
     public boolean isEmpty() throws AdoHiveException {
         return getManager().isEmpty();
     }
 
     /**
      * Checks if the current instance exists in the database.
      *
      * @return True if the instance exists
      * @throws AdoHiveException
      */
     public boolean isInDatabase() throws AdoHiveException {
         return getManager().contains(this);
     }
 
     /**
      * Deletes everything from the associated table.
      *
      * @throws AdoHiveException
      */
     public void clearTable() throws AdoHiveException {
         getManager().clear();
         id = 0; // Reset
     }
 
     // TODO: Add get(index) method?
 
     /**
      * Save the current model to the database.
      *
      * @return True if validation succeeds
      * @throws AdoHiveException
      */
     @SuppressWarnings("unchecked")
     public boolean save() throws AdoHiveException {
         if (!doValidate()) {
             return false;
         } else if (!errors.isEmpty()) {
             Logger
                 .debug(_("The model was not saved because the error list is not empty."));
             return false;
         }
 
         /* Add or update model */
         IAdoHiveManager mgr = getManager();
        boolean wasNew = isNew;
         if (isNew) {
             Logger.info(MessageFormat.format(_("Adding model: {0}"),
                 new Object[] { toString() }));
 
             mgr.add(this);
             setNew(false);
         } else if (updatePKs) {
             Logger.info(MessageFormat.format(_("Updating PKs for model: {0}"),
                 new Object[] { toString() }));
 
             mgr.remove(pkModel);
             mgr.add(this);
             pkModel = (AbstractModel<T>) clone();
         } else {
             Logger.info(MessageFormat.format(_("Updating model: {0}"),
                 new Object[] { toString() }));
 
             mgr.update(this);
         }
 
         /* Add event to the HistoryManager */
         HistoryEvent evt = new HistoryEvent();
         evt.id = getId();
        evt.status = wasNew ? HistoryEvent.Status.Added : HistoryEvent.Status.Changed;
         evt.type = getClass().getSimpleName();
         evt.date = new java.sql.Date(Calendar.getInstance().getTime().getTime());
         HistoryManager.getInstance().addEvent(evt);
 
         setChanged();
         notifyObservers(true);
 
         return true;
     }
 
     /**
      * Remove the current model from the database.
      *
      * @return False if the model is new or doesn't validate
      * @throws AdoHiveException
      */
     @SuppressWarnings("unchecked")
     public boolean remove() throws AdoHiveException {
         if (isNew) {
             return false;
         }
 
         /* Check if there is a custom validation function */
         try {
             java.lang.reflect.Method m = getClass().getDeclaredMethod(
                 "validateOnRemove");
             if (!(Boolean) m.invoke(this, new Object[0])) {
                 return false;
             }
         } catch (Exception ex) {
         }
 
         Logger.info(MessageFormat.format(_("Removing model: {0}"),
             new Object[] { toString() }));
 
         getManager().remove(this);
         setChanged();
         notifyObservers(false);
 
         /* Add event to the HistoryManager */
         HistoryEvent evt = new HistoryEvent();
         evt.id = getId();
         evt.status = HistoryEvent.Status.Removed;
         evt.type = getClass().getSimpleName();
         evt.date = new java.sql.Date(Calendar.getInstance().getTime().getTime());
         HistoryManager.getInstance().addEvent(evt);
 
         setNew(true);
 
         return true;
     }
 
     /**
      * Get a list of all errors.
      *
      * @return A list of errors
      */
     public List<String> getErrors() {
         return errors;
     }
 
     /**
      * Get a list of errors for a specific field.
      *
      * @param field
      *            The field to get the errors for
      * @return A list of errors
      */
     public List<String> getErrorsFor(String field) {
         return fieldErrors.get(field);
     }
 
     /**
      * Add an error to the list,
      *
      * @param error
      *            The error to add
      */
     public void addError(String error) {
         errors.add(error);
     }
 
     /**
      * Add an error for a specific field to the list.
      *
      * @param field
      *            The field on which the error occured
      * @param trans
      *            The translated field name
      * @param error
      *            The error to add
      */
     public void addError(String field, String trans, String error) {
         error = trans + " " + error;
         errors.add(error);
         if (fieldErrors.containsKey(field)) {
             fieldErrors.get(field).add(error);
         } else {
             List<String> list = new ArrayList<String>();
             list.add(error);
             fieldErrors.put(field, list);
         }
     }
 
     /**
      * Clear the error lists.
      */
     public void resetErrors() {
         errors.clear();
         fieldErrors.clear();
     }
 
     /**
      * Add a presence validator to the model.
      *
      * @param members
      *            The name of the member variables to validate
      * @param trans
      *            The translated names
      */
     public void validatePresenceOf(String[] members, String[] trans) {
         validators.add(new PresenceValidator(this, members, trans));
     }
 
     /**
      * Add an email validator to the model.
      *
      * @param member
      *            The name of the member variable to validate
      * @param trans
      *            The translated name
      */
     public void validateEmailAddress(String member, String trans) {
         validators.add(new FormatValidator(this, new String[] { member },
                 new String[] { trans },
                 "^[\\w\\-]([\\.\\w])+[\\w]+@([\\wüäöÜÄÖ\\-]+\\.)+[A-Z]{2,4}$", false));
     }
 
     /**
      * Add an date range validator to the model.
      *
      * @param from
      *            The starting date
      * @param to
      *            The end date
      * @param transFrom
      *            Translated from date
      * @param transTo
      *            Translated to date
      */
     public void validateDateRange(String from, String to, String transFrom,
             String transTo) {
         validators.add(new DateRangeValidator(this, from, to, transFrom, transTo));
     }
 
     /**
      * Add an inclusion validator to the model.
      *
      * @param members
      *            The name of the member variables to validate
      * @param trans
      *            The translated names
      * @param inc
      *            The list to check for inclusion
      */
     public void validateInclusionOf(String[] members, String[] trans,
             String[] inc) {
         validators.add(new InclusionValidator(this, members, trans, inc));
     }
 
     /**
      * Add an existance validator to the model.
      *
      * @param members
      *            The name of the member variables to validate
      * @param type
      *            The type to check for
      * @param trans
      *            The translated names
      */
     public void validateExistanceOf(String[] members, String[] trans,
             AbstractModel type) {
         validators.add(new ExistanceValidator(this, members, trans, type));
     }
 
     /**
      * Add an format validator to the model.
      *
      * @param members
      *            The name of the member variables to validate
      * @param trans
      *            The translated names
      * @param format
      *            THe format to check
      */
     public void validateFormatOf(String[] members, String[] trans, String format) {
         validators.add(new FormatValidator(this, members, trans, format));
     }
 
     /**
      * Returns the unique id of the activity.
      *
      * @return The unique id of the activity
      */
     @Override
     public Integer getId() {
         return id;
     }
 
     /**
      * Set the unique id of the assistant.
      *
      * <b>!!! THIS IS FOR INTERNAL ADOHIVE USAGE ONLY !!!</b>
      *
      * @param id
      *            The unique id of the assistant
      */
     @Override
     public void setId(Integer id) {
         this.id = id;
     }
 
     /**
      * Set if the model is new and should be added to the database.
      *
      * @param isnew
      *            Is the model new?
      */
     public void setNew(boolean isnew) {
         isNew = isnew;
         if (isNew) {
             setId(0);
             if (updatePKs) {
                 pkModel = null;
             }
         } else if (updatePKs) {
             pkModel = (AbstractModel<T>) clone();
         }
     }
 
     /**
      * Get the state of the model in the db.
      *
      * @return True if the model is new
      */
     protected boolean isNew() {
         return isNew;
     }
 
     /**
      * Returns a string containing all informations stored in the model.
      *
      * @return A string containing informations on the model
      */
     @Override
     public String toString() {
         String ret = getClass().getSimpleName() + " [" + "ID: " + getId()
                 + ", ";
         try {
             for (java.lang.reflect.Method m : getClass().getDeclaredMethods()) {
                 if (m.getName().startsWith("get")
                         && m.getParameterTypes().length == 0) {
                     ret += m.getName().substring(3) + ": ";
                     ret += m.invoke(this, new Object[0]) + ", ";
                 }
             }
             if (ret.endsWith(", ")) {
                 ret = ret.substring(0, ret.length() - 2);
             }
         } catch (Exception ex) {
             System.err.println(ex.getMessage());
         }
         return ret + "]";
     }
 
     /**
      * Extract the name of the class and return the correct manager.
      *
      * @return The name of the model class
      */
     @SuppressWarnings("unchecked")
     protected IAdoHiveManager getManager() {
         String classname = getClass().getSimpleName();
         if (!managers.containsKey(classname) || managers.get(classname) == null) {
             /* Try to get the correct manager from the AdoHiveController */
             try {
                 java.lang.reflect.Method m = AdoHiveController.class
                     .getMethod("get" + classname + "Manager");
                 managers.put(classname, (IAdoHiveManager) m.invoke(
                     AdoHiveController.getInstance(), new Object[0]));
             } catch (Exception ex) {
                 Logger.error(MessageFormat.format(
                     _("Could not get manager for class \"{0}\". Error: {1}"),
                     new Object[] { classname, ex.getMessage() }));
             }
         }
 
         return managers.get(classname);
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
             java.lang.reflect.Method m = getClass().getDeclaredMethod(
                 "validate");
             if (!(Boolean) m.invoke(this, new Object[0])) {
                 ret = false;
             }
         } catch (Exception ex) {
         }
 
         return ret;
     }
 
     /**
      * Protected helper method to correctly clone models with updatePKs = true.
      *
      * @param toClone
      *              The model that gets cloned
      */
     protected void doClone(AbstractModel toClone) {
         isNew = toClone.isNew;
         if (isNew) {
             pkModel = null;
         } else if (updatePKs) {
             pkModel = toClone.pkModel;
         }
         setId(toClone.getId());
     }
 
 }
