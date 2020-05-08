 package com.isb.bks.ivr.voice.m2e.builder.internal;
 
 import java.io.File;
 import java.util.Set;
 
 import org.apache.maven.plugin.MojoExecution;
 import org.eclipse.core.resources.IFolder;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IResourceDelta;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.m2e.core.MavenPlugin;
 import org.eclipse.m2e.core.embedder.IMaven;
 import org.eclipse.m2e.core.project.configurator.MojoExecutionBuildParticipant;
 import org.sonatype.plexus.build.incremental.BuildContext;
 
 import com.vectorsf.jvoice.base.model.service.BaseModel;
 import com.vectorsf.jvoice.model.base.JVBean;
 
 public class JVoiceModuleBuildParticipant extends MojoExecutionBuildParticipant {
 
 	private IPath pkgPath = new Path(BaseModel.JV_PATH);
 
 	public JVoiceModuleBuildParticipant(MojoExecution execution) {
 		super(execution, true);
 	}
 
 	@Override
 	public Set<IProject> build(int kind, IProgressMonitor monitor)
 			throws Exception {
 		IMaven maven = MavenPlugin.getMaven();
 		BuildContext buildContext = getBuildContext();
 
 		IResourceDelta delta = getDelta(getMavenProjectFacade().getProject());
		if (delta != null && delta.getKind() == IResourceDelta.CHANGED) {
 			for (IResourceDelta delt : delta.getAffectedChildren()) {
 				IResource resource = delt.getResource();
 				if (resource instanceof IFolder) {
 					IFolder folder = (IFolder) resource;
 					DeleteFileXML delete = new DeleteFileXML(folder);
 					try {
 						delta.accept(delete);
 					} catch (CoreException e) {
 					}
 				}
 
 			}
 		}
 
 		// execute mojo
 		Set<IProject> result = super.build(kind, monitor);
 
 		// tell m2e builder to refresh generated files
 		File generated = maven.getMojoParameterValue(getSession()
 				.getCurrentProject(), getMojoExecution(), "outputDirectory",
 				File.class, monitor);
 		if (generated != null) {
 			buildContext.refresh(generated);
 		}
 
 		return result;
 	}
 
 	private IPath getRelativePath(IResource resource) {
 		if (pkgPath.isPrefixOf(resource.getProjectRelativePath())
 				&& !pkgPath.equals(resource.getProjectRelativePath())) {
 			return resource.getProjectRelativePath().makeRelativeTo(pkgPath);
 		} else {
 			return null;
 		}
 	}
 
 	private JVBean getBeanFromResource(Resource eResource) {
 		for (EObject e : eResource.getContents()) {
 			if (e instanceof JVBean) {
 				return (JVBean) e;
 			}
 		}
 		return null;
 	}
 }
