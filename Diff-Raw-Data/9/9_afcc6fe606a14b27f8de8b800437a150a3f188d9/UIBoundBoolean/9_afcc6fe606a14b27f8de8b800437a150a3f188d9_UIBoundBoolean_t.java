 /*
  * Created on Jul 28, 2005
  */
 package uk.org.ponder.rsf.components;
 
 import uk.org.ponder.rsf.uitype.BooleanUIType;
 
 /**
  * Component holding a single boolean value, which will peer with a component
  * such as a checkbox or radio button.
  * 
  * @author Antranig Basman (antranig@caret.cam.ac.uk)
  * 
  */
 public class UIBoundBoolean extends UIBound {
 
   public boolean getValue() {
     return ((Boolean) value).booleanValue();
   }
 
   public void setValue(boolean value) {
    this.value = value ? Boolean.TRUE : Boolean.FALSE;
   }
 
   public UIBoundBoolean() {
     value = BooleanUIType.instance.getPlaceholder();
     fossilize = true;
     willinput = true;
   }
 
   /**
    * Construct a new UIBoundBoolean component with the specified container as parent.
    * 
    * @param parent Parent container to which the component is to be added.
    * @param ID (RSF) ID of this component.
    * @param binding An EL expression to be used as the value binding for the
    *          contained Boolean value. May be <code>null</code>.
    * @param initvalue An initial value for the bound value. May be left
    *          <code>null</code>. If neither this field nor
    *          <code>binding</code> is set. the value present in the template
    *          will be used.
    * @return The constructed UIBoundBoolean component.
    */
   
   public static UIBoundBoolean make(UIContainer parent, String ID,
       String binding, Boolean initvalue) {
     UIBoundBoolean togo = new UIBoundBoolean();
     togo.valuebinding = ELReference.make(binding);
     if (initvalue != null) {
       togo.setValue(initvalue.booleanValue());
     }
     togo.ID = ID;
     parent.addComponent(togo);
     return togo;
   }
 
   /** @see #make(UIContainer, String, String, Boolean) **/
   
   public static UIBoundBoolean make(UIBranchContainer parent,
       String ID, String binding, boolean initvalue) {
     return make(parent, ID, binding, initvalue? Boolean.TRUE : Boolean.FALSE);
   }
 
   /** @see #make(UIContainer, String, String, Boolean) **/
   
   public static UIBoundBoolean make(UIContainer parent, String ID,
       String binding) {
     return make(parent, ID, binding, null);
   }
 
   /** @see #make(UIContainer, String, String, Boolean) **/
   
   public static UIBoundBoolean make(UIContainer parent, String ID,
       Boolean initvalue) {
     return make(parent, ID, null, initvalue);
   }
 
   /** @see #make(UIContainer, String, String, Boolean) **/
   
   public static UIBoundBoolean make(UIContainer parent, String ID,
       boolean initvalue) {
     return make(parent, ID, null, initvalue ? Boolean.TRUE
         : Boolean.FALSE);
   }
 
  /** Suitable for a non-bound control such as in a GET form. 
    *  @param parent The parent to which this component is to be added
    *  @param ID The (RSF) ID to be given to this component
    *  @return The constructed component, if it is required.
    **/
   public static UIBoundBoolean make(UIContainer parent, String ID) {
     return make(parent, ID, null, (Boolean) null);
   }
 
 }
