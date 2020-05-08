 package org.jboss.tools.portlet.operations;
 
 import java.net.URL;
 
 import org.eclipse.core.runtime.FileLocator;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.emf.codegen.jet.JETException;
 import org.eclipse.jdt.core.IPackageFragment;
 import org.eclipse.jst.j2ee.internal.common.operations.CreateJavaEEArtifactTemplateModel;
 import org.eclipse.jst.j2ee.internal.plugin.J2EEPlugin;
 import org.eclipse.jst.j2ee.internal.project.WTPJETEmitter;
 import org.eclipse.jst.j2ee.internal.web.operations.NewWebClassOperation;
 import org.eclipse.wst.common.componentcore.internal.operation.ArtifactEditProviderOperation;
 import org.eclipse.wst.common.frameworks.datamodel.IDataModel;
 import org.eclipse.wst.common.frameworks.internal.WTPPlugin;
 import org.jboss.tools.portlet.ui.PortletUIActivator;
 
 /**
  * 
  * @see org.eclipse.jst.j2ee.internal.web.operations.NewServletClassOperation
  * 
  */
 public class NewPortletClassOperation extends NewWebClassOperation {
 
 	protected static final String TEMPLATE_DIR = "/templates/"; //$NON-NLS-1$
 	
 	protected static final String TEMPLATE_FILE = "/templates/portlet.javajet"; //$NON-NLS-1$
 
 	
 	public NewPortletClassOperation(IDataModel dataModel) {
 		super(dataModel);
 	}
 
 	@Override
 	protected CreatePortletTemplateModel createTemplateModel() {
 		return new CreatePortletTemplateModel(model);
 	}
 	
 	@Override
 	protected String getTemplateFile() {
 		return TEMPLATE_FILE;
 	}
 	
 	@Override
 	protected String generateTemplateSource(WTPPlugin plugin, CreateJavaEEArtifactTemplateModel tempModel, String template_file, IProgressMonitor monitor) throws JETException {
 		URL templateURL = FileLocator.find(PortletUIActivator.getDefault().getBundle(), new Path(template_file), null);
 		cleanUpOldEmitterProject();
 		WTPJETEmitter emitter = new WTPJETEmitter(templateURL.toString(), this.getClass().getClassLoader());
 		emitter.setIntelligentLinkingEnabled(true);
 		emitter.addVariable(J2EEPlugin.getPlugin().getPluginID(), J2EEPlugin.getPlugin().getPluginID());
 		emitter.addVariable(plugin.getPluginID(), plugin.getPluginID());
 		emitter.addVariable(PortletUIActivator.PLUGIN_ID, PortletUIActivator.PLUGIN_ID);
 		return emitter.generate(monitor, new Object[] { tempModel });
 	}

	@Override
	protected Object getTemplateImplementation() {
		return null;
	}
 }
