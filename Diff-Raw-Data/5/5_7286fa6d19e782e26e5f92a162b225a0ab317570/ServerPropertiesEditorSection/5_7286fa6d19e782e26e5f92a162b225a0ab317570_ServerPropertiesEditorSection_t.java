 package org.eclipse.jst.server.generic.ui.internal.editor;
 
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.StringTokenizer;
 
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.jst.server.generic.core.internal.GenericServer;
 import org.eclipse.jst.server.generic.core.internal.GenericServerRuntime;
 import org.eclipse.jst.server.generic.servertype.definition.Property;
 import org.eclipse.jst.server.generic.ui.internal.GenericServerUIMessages;
 import org.eclipse.jst.server.generic.ui.internal.SWTUtil;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.IEditorSite;
 import org.eclipse.ui.forms.widgets.ExpandableComposite;
 import org.eclipse.ui.forms.widgets.FormToolkit;
 import org.eclipse.ui.forms.widgets.Section;
 import org.eclipse.wst.server.ui.editor.ServerEditorSection;
 
 public class ServerPropertiesEditorSection extends ServerEditorSection{
 	private GenericServer fServer;
 	private PropertyChangeListener fPropertyChangeListener;
 	private Map fControls = new HashMap();
     private boolean fUpdating = false;
     
 	public void init(IEditorSite site, IEditorInput input) {
 		super.init(site, input);
 		if(server!=null){
 			fServer = (GenericServer)server.loadAdapter(GenericServer.class, new NullProgressMonitor());
 		}
 		fPropertyChangeListener = new PropertyChangeListener(){
 
 			public void propertyChange( PropertyChangeEvent evt ) {
 				if(evt.getPropertyName().equals( GenericServerRuntime.SERVER_INSTANCE_PROPERTIES )) 
                 {
                     if ( !fUpdating ){
                         fUpdating = true;
                         updateControls();
                         fUpdating = false;
                     }
                 }
 			}
 		};
 		server.addPropertyChangeListener( fPropertyChangeListener );
 	}
     
 	protected void updateControls() {
         List props = fServer.getServerDefinition().getProperty(); 
         for (Iterator iter = props.iterator(); iter.hasNext();) {
             Property property = (Property) iter.next();
             if(property.getContext().equals(Property.CONTEXT_SERVER))
             {
                 if( Property.TYPE_BOOLEAN.equals(property.getType()) ){
                     Button b = (Button)fControls.get( property.getId() );
                     b.setSelection( "true".equals(  getPropertyValue( property ) ) ); //$NON-NLS-1$
                 }
                 else if( Property.TYPE_SELECT.equals( property.getType() )){
                     Combo c = (Combo)fControls.get( property.getId() );
                     String value = getPropertyValue( property )==null ? "": getPropertyValue( property ); //$NON-NLS-1$
                    //c.setText( getPropertyValue( property ) );
                    // responding to "value not used" msg, I'm assuming value
                    // should be used as in following block.
                    c.setText( value );
                 }
                 else{
                     Text t = (Text)fControls.get( property.getId() );
                     String value = getPropertyValue( property )==null ? "": getPropertyValue( property ); //$NON-NLS-1$
                     t.setText( value );
                 }
             }
         }  
     }
 
     public void createSection(Composite parent) {
 		super.createSection(parent);
 		FormToolkit formToolkit = getFormToolkit(parent.getDisplay());
 		Section section = formToolkit.createSection(parent, ExpandableComposite.TWISTIE | ExpandableComposite.EXPANDED
 				| ExpandableComposite.TITLE_BAR | Section.DESCRIPTION | ExpandableComposite.FOCUS_TITLE);
 			section.setText(GenericServerUIMessages.ServerEditorSectionTitle);
 			section.setDescription(GenericServerUIMessages.ServerEditorSectionDescription);
 			section.setLayoutData(new GridData(SWT.FILL,SWT.NONE,true,false));
 			
 		Composite composite = formToolkit.createComposite(section);
 		GridLayout layout = new GridLayout();
 		layout.numColumns=3;
 		layout.marginHeight = 5;
 		layout.marginWidth = 10;
 		layout.verticalSpacing = 5;
 		layout.horizontalSpacing = 15;
 		composite.setLayout(layout);
 		composite.setLayoutData(new GridData(SWT.FILL,SWT.NONE,true,false));
 		
 		List props = fServer.getServerDefinition().getProperty();
 		for (Iterator iter = props.iterator(); iter.hasNext();) {
 			Property property = (Property) iter.next();
 			if(property.getContext().equals(Property.CONTEXT_SERVER))
 				createPropertyControl(composite, property,formToolkit);
 		}
 
 		formToolkit.paintBordersFor(composite);
 		section.setClient(composite);
 	}
 	
 	private void executeUpdateOperation(String propertyName, String propertyValue)
 	{
         if( !fUpdating )
         {
             fUpdating = true;
             execute( new UpdateServerPropertyOperation( server,
                     GenericServerUIMessages.UpdateOperationDescription, propertyName,
                     propertyValue ) );
             fUpdating = false;
         }
 	}
     
     private void createPropertyControl(Composite parent, final Property property, FormToolkit toolkit){
     	
     	if( Property.TYPE_DIRECTORY.equals(property.getType())) {
     		final Text path = SWTUtil.createLabeledPath(property.getLabel(),getPropertyValue(property),parent,toolkit);
             fControls.put( property.getId(), path );
     		path.addModifyListener(new ModifyListener() {			
 				public void modifyText(ModifyEvent e) {
 					executeUpdateOperation(property.getId(),path.getText());
 				}
 			});
      	} else if( Property.TYPE_FILE.equals(property.getType())) {
             
     	    final Text file = SWTUtil.createLabeledFile(property.getLabel(),getPropertyValue(property),parent,toolkit);
     		fControls.put( property.getId(), file );
             file.addModifyListener(new ModifyListener() {
 				public void modifyText(ModifyEvent e) {
 					executeUpdateOperation(property.getId(),file.getText());
 				}
 			});	
        	}else if( Property.TYPE_BOOLEAN.equals(property.getType())) {
     	    final Button bool = SWTUtil.createLabeledCheck(property.getLabel(),("true".equals( getPropertyValue(property))),parent,toolkit); //$NON-NLS-1$
     	    fControls.put( property.getId(), bool );
             bool.addSelectionListener(new SelectionListener() {			
 				public void widgetSelected(SelectionEvent e) {
 					executeUpdateOperation(property.getId(),  Boolean.toString(bool.getSelection()));
 				}
 				public void widgetDefaultSelected(SelectionEvent e) {
 					// Do Nothing
 				}
 			});	
        	}else if(Property.TYPE_SELECT.equals(property.getType())) {
     		StringTokenizer tokenizer = new StringTokenizer(property.getDefault(),","); //$NON-NLS-1$
     		int tokenCount = tokenizer.countTokens();
     		String[] values = new String[tokenCount];
     		int i =0;
     		while(tokenizer.hasMoreTokens() && i<tokenCount){
     			values[i]=tokenizer.nextToken();
     			i++;
     		}
        		final Combo combo = SWTUtil.createLabeledCombo(property.getLabel(), values, parent,toolkit);
        		fControls.put( property.getId(), combo );
             combo.addModifyListener(new ModifyListener() {
 				public void modifyText(ModifyEvent e) {
 					executeUpdateOperation(property.getId(),combo.getText());
 				}
 			});
        		combo.addSelectionListener(new SelectionListener() {		
 				public void widgetSelected(SelectionEvent e) {
 					executeUpdateOperation(property.getId(),combo.getText());
 				}			
 				public void widgetDefaultSelected(SelectionEvent e) {
 					// nothing to do
 				}			
 			});	
        	}
        	else  {// Property.TYPE_TEXT
     	    final Text defaultText= SWTUtil.createLabeledText(property.getLabel(),getPropertyValue(property),parent,toolkit);
             fControls.put( property.getId(), defaultText );
     		defaultText.addModifyListener(new ModifyListener() {
 				public void modifyText(ModifyEvent e) {
 					executeUpdateOperation(property.getId(), defaultText.getText());
 				}
 			});
     	}
     }
 
 	private String getPropertyValue(Property property) {
 		 return(String) fServer.getServerInstanceProperties().get(property.getId());
 	}
 
 	public void dispose() {
 	    super.dispose();
         if( server!= null )
             server.removePropertyChangeListener( fPropertyChangeListener );
 	}
 	
 }
