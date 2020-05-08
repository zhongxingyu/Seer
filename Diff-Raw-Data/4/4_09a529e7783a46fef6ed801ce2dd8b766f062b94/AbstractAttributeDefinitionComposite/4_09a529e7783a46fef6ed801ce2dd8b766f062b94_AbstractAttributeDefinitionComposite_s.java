 package era.foss.objecteditor;
 
 import org.eclipse.core.databinding.Binding;
 import org.eclipse.core.databinding.DataBindingContext;
 import org.eclipse.core.databinding.observable.ChangeEvent;
 import org.eclipse.core.databinding.observable.IChangeListener;
 import org.eclipse.emf.common.util.Diagnostic;
 import org.eclipse.emf.ecore.util.Diagnostician;
 import org.eclipse.emf.edit.domain.EditingDomain;
 import org.eclipse.jface.fieldassist.ControlDecoration;
 import org.eclipse.jface.fieldassist.FieldDecoration;
 import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 
 import era.foss.erf.AttributeValue;
 import era.foss.erf.SpecObject;
 import era.foss.erf.ViewElement;
 
 public abstract class AbstractAttributeDefinitionComposite extends Composite {
 
     /** color for default values */
     final static int COLOR_DEFAULT_VALUE = SWT.COLOR_INFO_BACKGROUND;
 
     /** The view element to show which refers to a AttributeDefifinition */
     final ViewElement viewElement;
 
     /** Create one data binding context for all Elements */
     static final DataBindingContext dbc = new DataBindingContext();
 
     /** binding of the GUI element to the model element */
     Binding binding;
 
     private Control control;
 
     private ControlDecoration errorDecoration;
 
     private AttributeValue attributeValue;
 
     public AbstractAttributeDefinitionComposite( Composite parent, ViewElement viewElement ) {
         super( parent, SWT.NONE );
         this.setLayout( new FillLayout() );
         this.viewElement = viewElement;
 
         this.control = createControl();
 
         // set up decoration for showing erroneous validation
         errorDecoration = new ControlDecoration( this.control, SWT.RIGHT | SWT.TOP );
         FieldDecoration fieldDecoration = FieldDecorationRegistry.getDefault()
                                                                  .getFieldDecoration( FieldDecorationRegistry.DEC_ERROR );
         errorDecoration.setImage( fieldDecoration.getImage() );
         errorDecoration.hide();
 
     }
 
     abstract public Control createControl();
 
     /**
      * Return the view element associated with this GUI element
      * 
      * @return the view element associated with this GUI element
      */
     public ViewElement getViewElement() {
         return viewElement;
     }
 
     /**
      * Bind the GUI element to the model element. This has to be implemented be subclasses.
      * 
      * <strong>Subclasses have to to call this method in case the ovveride it.</strong>
      */
     public void bind( SpecObject specObject, AttributeValue attributeValue, EditingDomain editingDomain ) {
         this.unbind();
         this.attributeValue = attributeValue;
         this.doBind( specObject, attributeValue, editingDomain );
         this.setupValidation();
     }
 
     /**
      * Unbind the GUI element from the model element.
      * 
      * <strong>Subclasses have to to call this method in case the ovveride it.</strong>
      */
     public void unbind() {
         if( this.binding != null ) {
             dbc.removeBinding( binding );
             binding.dispose();
             binding = null;
             attributeValue = null;
         }
     }
 
     /** Get the control actually representing the AttributeDefinition */
     public abstract Control getControl();
 
     public abstract void doBind( SpecObject specObject, AttributeValue attributeValue, EditingDomain editingDomain );
 
     private void setupValidation() {
         this.validate();
 
         if( binding != null && binding.isDisposed() == false ) {
             binding.getModel().addChangeListener( new IChangeListener() {
                 @Override
                 public void handleChange( ChangeEvent event ) {
                     AbstractAttributeDefinitionComposite.this.validate();
                 }
             } );
         }
     }
 
     private void validate() {
         if( attributeValue != null ) {
             Diagnostic diagnostic = Diagnostician.INSTANCE.validate( attributeValue );
             if( diagnostic.getSeverity() == Diagnostic.ERROR ) {
                 errorDecoration.show();
             } else {
                 errorDecoration.hide();
            };
         }
     }
 
 }
