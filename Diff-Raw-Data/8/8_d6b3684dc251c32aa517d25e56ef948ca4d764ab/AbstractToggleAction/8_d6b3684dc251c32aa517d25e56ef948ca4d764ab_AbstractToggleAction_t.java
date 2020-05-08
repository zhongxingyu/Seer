 package org.javakontor.sherlog.application.action;
 
 /**
  * <p>
  * </p>
  * <p>
  * This class <b>is intended<b> to be subclassed by clients.
  * </p>
  * 
  * @author Nils Hartmann (nils@nilshartmann.net)
  * @author Gerd W&uuml;therich (gerd@gerd-wuetherich.de)
  */
 public abstract class AbstractToggleAction extends AbstractAction implements ToggleAction {
 
   /** the 'active' property */
   private boolean _active = false;
 
   /**
    * @see org.javakontor.sherlog.application.action.ToggleAction#isActive()
    */
   public boolean isActive() {
     return this._active;
   }
 
   /**
    * @see org.javakontor.sherlog.application.action.ToggleAction#setActive(boolean)
    */
   public void setActive(final boolean active) {
     final boolean oldValue = this._active;
     this._active = active;
     firePropertyChangeEvent(ACTIVE_PROPERTY, oldValue, this._active);
   }
 
   /**
    * @see org.javakontor.sherlog.application.action.Action#execute()
    */
   public void execute() {
    // When execute() is called, that activate state has already been set to it's
    // new state.
    if (isActive()) {
       activate();
     } else {
       deactivate();
     }
   }
 
   /**
    * <p>
    * </p>
    */
   protected void activate() {
     // empty implementation
   }
 
   protected void deactivate() {
     // empty implementation
   }
 }
