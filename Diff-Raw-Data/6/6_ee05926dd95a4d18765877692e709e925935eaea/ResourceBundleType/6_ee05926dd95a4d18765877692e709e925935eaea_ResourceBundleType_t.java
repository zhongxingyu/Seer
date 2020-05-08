 package org.eclipse.jst.jsf.taglibprocessing.attributevalues;
 
import java.text.MessageFormat;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IStorage;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.jst.jsf.context.resolver.structureddocument.IStructuredDocumentContextResolverFactory;
 import org.eclipse.jst.jsf.context.resolver.structureddocument.IWorkspaceContextResolver;
 import org.eclipse.jst.jsf.core.internal.tld.LoadBundleUtil;
 import org.eclipse.jst.jsf.metadataprocessors.AbstractRootTypeDescriptor;
 import org.eclipse.jst.jsf.metadataprocessors.features.IValidValues;
 import org.eclipse.jst.jsf.metadataprocessors.features.IValidationMessage;
 import org.eclipse.jst.jsf.metadataprocessors.features.ValidationMessage;
 
 /**
  * Meta-data processing type representing a path to resource bundle on classpath
  * Patch by Vadim Dmitriev.  See https://bugs.eclipse.org/bugs/show_bug.cgi?id=203307.
  * 
  * @author Vadim Dmitriev
  */
 public class ResourceBundleType extends AbstractRootTypeDescriptor implements IValidValues 
 {
 	private IProject _project 								= null;
 	private final List<IValidationMessage> _validationMsgs 	= new ArrayList<IValidationMessage>(1);
 	
 	private IProject getProject()
 	{
 		if( _project == null )
 		{
             final IWorkspaceContextResolver wkspaceResolver =
                 IStructuredDocumentContextResolverFactory.INSTANCE.getWorkspaceContextResolver( getStructuredDocumentContext() );
             _project = wkspaceResolver.getProject();
 		}
 		
 		return _project;
 	}
 
 	public boolean isValidValue( String value )
 	{
 		try
 		{
 			IProject project = getProject();
 			IStorage bundle = LoadBundleUtil.getLoadBundleResource( project , value );
 			if( bundle != null )
 			{
 				return true;
 			}
 		}
 		catch (CoreException e) 
 		{
 			//error message is generated later
 		}
 		
		final String message = 
			MessageFormat.format(Messages.Bundle_not_found_rb, value); 
		_validationMsgs.add(new ValidationMessage(message, "", IStatus.ERROR));
 		return false;
 	}
 	
 	public List getValidationMessages() 
 	{
 		return _validationMsgs;
 	}
 }
