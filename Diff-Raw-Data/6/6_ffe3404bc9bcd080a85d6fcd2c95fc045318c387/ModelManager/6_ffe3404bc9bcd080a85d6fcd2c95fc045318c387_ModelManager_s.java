 package org.integratedmodelling.thinklab.client.modelling;
 
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.PrintStream;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 
 import org.integratedmodelling.exceptions.ThinklabException;
 import org.integratedmodelling.exceptions.ThinklabIOException;
 import org.integratedmodelling.exceptions.ThinklabValidationException;
 import org.integratedmodelling.lang.model.ModelObject;
 import org.integratedmodelling.lang.model.Namespace;
 import org.integratedmodelling.thinklab.api.knowledge.storage.IKBox;
 import org.integratedmodelling.thinklab.api.lang.IResolver;
 import org.integratedmodelling.thinklab.api.lang.IModelParser;
 import org.integratedmodelling.thinklab.api.modelling.IAgentModel;
 import org.integratedmodelling.thinklab.api.modelling.IModel;
 import org.integratedmodelling.thinklab.api.modelling.IModelObject;
 import org.integratedmodelling.thinklab.api.modelling.INamespace;
 import org.integratedmodelling.thinklab.api.modelling.IScenario;
 import org.integratedmodelling.thinklab.api.modelling.factories.IModelManager;
 import org.integratedmodelling.thinklab.api.modelling.observation.IContext;
 import org.integratedmodelling.thinklab.api.project.IProject;
 import org.integratedmodelling.thinklab.api.runtime.ISession;
 import org.integratedmodelling.thinklab.client.lang.ClientNamespace;
 import org.integratedmodelling.thinklab.client.utils.MiscUtilities;
 
 /**
  * A model manager that can parse the Thinklab language and build a model map, without actually being 
  * capable of running the model objects.
  * 
  * @author Ferd
  *
  */
 public class ModelManager implements IModelManager {
 
 	private static ModelManager _this = null;
 	
 	HashMap<String, IModelParser> interpreters = new HashMap<String, IModelParser>();
 	HashMap<String, INamespace> namespaces = new HashMap<String, INamespace>();
 	
 	private CContext _ccontext = null;
 	
 	class CContext implements IResolver {
 
 		@Override
		public String getDefaultNamespaceId() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
 		public boolean onException(Throwable e, int lineNumber)
 				throws ThinklabException {
 			// TODO Auto-generated method stub
 			return false;
 		}
 
 		@Override
 		public boolean onWarning(String warning, int lineNumber) {
 			// TODO Auto-generated method stub
 			return false;
 		}
 
 		@Override
 		public boolean onInfo(String info, int lineNumber) {
 			// TODO Auto-generated method stub
 			return false;
 		}
 
 		@Override
 		public InputStream resolveNamespace(String namespace, String reference)
 				throws ThinklabException {
 			// TODO Auto-generated method stub
 			return null;
 		}
 
 		@Override
 		public void onNamespaceDeclared(String namespaceId, String resourceId,
 				Namespace namespace) {
 			// TODO Auto-generated method stub
 			
 		}
 
 		@Override
 		public void onNamespaceDefined(Namespace namespace) {
 			// TODO Auto-generated method stub
 			
 		}
 		
 	}
 	
 	public static ModelManager get() {
 		if (_this == null) {
 			_this = new ModelManager();
 		}
 		return _this;
 	}
 	
 	public void addInterpreter(String extension, IModelParser modelParser) {
 		interpreters.put(extension, modelParser);
 	}
 	
 	@Override
 	public IAgentModel getAgentModel(String arg0) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public Collection<IScenario> getApplicableScenarios(IModel arg0,
 			IContext arg1, boolean arg2) throws ThinklabException {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public IContext getContext(String arg0) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public IContext getCoverage(IModel arg0, IKBox arg1, ISession arg2) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public Collection<IModelObject> getDependencies(String arg0) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public IModel getModel(String arg0) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public IModelObject getModelObject(String arg0) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public INamespace getNamespace(String arg0) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public Collection<INamespace> getNamespaces() {
 
 		ArrayList<INamespace> ret = new ArrayList<INamespace>();
 		for (INamespace n : namespaces.values()) {
 			ret.add(n);
 		}
 		
 		/*
 		 * sort namespace list alphabetically
 		 */
 		Collections.sort(ret, new Comparator<INamespace>() {
 			@Override
 			public int compare(INamespace arg0, INamespace arg1) {
 				return arg0.getNamespace().compareTo(arg1.getNamespace());
 			}
 		});
 		
 		return ret;
 		
 	}
 
 	@Override
 	public IScenario getScenario(String arg0) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public String getSource(String arg0) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public INamespace loadFile(String file) throws ThinklabException {
 		
 		String extension = MiscUtilities.getFileExtension(file);
 		IModelParser parser = interpreters.get(extension);
 		INamespace ret = null;
 		
 		if (parser == null) {
 			throw new ThinklabValidationException("don't know how to parse a " + extension + " model file");
 		}
 
 		ret = new ClientNamespace(parser.parse(file, getCompilationContext()));
 		
 		namespaces.put(ret.getNamespace(), ret);
 		
 		return ret;
 	}
 
 	private IResolver getCompilationContext() {
 		if (_ccontext == null) {
 			_ccontext = new CContext();
 		}
 		return _ccontext;
 	}
 
 	@Override
 	public void releaseNamespace(String arg0) {
 		// TODO Auto-generated method stub
 	}
 
 	@Override
 	public IContext run(IModel arg0, IKBox arg1, ISession arg2, IContext arg3)
 			throws ThinklabException {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public Collection<INamespace> load(IProject project)
 			throws ThinklabException {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 }
