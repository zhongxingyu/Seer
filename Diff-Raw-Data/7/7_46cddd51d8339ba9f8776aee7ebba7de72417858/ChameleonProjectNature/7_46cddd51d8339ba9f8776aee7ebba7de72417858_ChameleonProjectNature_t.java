 package chameleon.eclipse.project;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.concurrent.Semaphore;
 
 import org.eclipse.core.resources.ICommand;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IMarker;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IProjectDescription;
 import org.eclipse.core.resources.IProjectNature;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IResourceChangeEvent;
 import org.eclipse.core.resources.IResourceChangeListener;
 import org.eclipse.core.resources.IWorkspace;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.jobs.Job;
 
 import chameleon.core.document.Document;
 import chameleon.core.element.Element;
 import chameleon.core.language.Language;
 import chameleon.core.namespace.Namespace;
 import chameleon.eclipse.ChameleonEditorPlugin;
 import chameleon.eclipse.LanguageMgt;
 import chameleon.eclipse.builder.ChameleonBuilder;
 import chameleon.eclipse.connector.EclipseEditorExtension;
 import chameleon.eclipse.connector.EclipseEditorInputProcessor;
 import chameleon.eclipse.connector.EclipseSourceManager;
 import chameleon.eclipse.editors.ChameleonDocument;
 import chameleon.eclipse.editors.ChameleonEditor;
 import chameleon.eclipse.presentation.PresentationModel;
 import chameleon.input.InputProcessor;
 import chameleon.input.ModelFactory;
 import chameleon.input.ParseException;
 import chameleon.input.SourceManager;
 import chameleon.workspace.BootstrapProjectConfig;
 import chameleon.workspace.ConfigException;
 import chameleon.workspace.DocumentLoader;
 import chameleon.workspace.FileInputSource;
 import chameleon.workspace.IFileInputSource;
 import chameleon.workspace.InputSource;
 import chameleon.workspace.InputSourceListener;
 import chameleon.workspace.Project;
 import chameleon.workspace.ProjectInitialisationListener;
 import chameleon.workspace.View;
 import chameleon.workspace.ViewListener;
 import chameleon.workspace.Workspace;
 
 /**
  * @author Manuel Van Wesemael 
  * @author Joeri Hendrickx
  * @author Tim Vermeiren
  * @author Marko van Dooren
  * 
  * Defines the nature for ChameleonProjects, it contains the elements in the model 
  * of the nature, the language of the project, and knows the project it is created for.
  */
 public class ChameleonProjectNature implements IProjectNature {
 	/**
 	 * This listener synchronizes the list of ChameleonDocuments with the FileInputSources in the sourceLoaders
 	 * of the project.
 	 * @author Marko van Dooren
 	 *
 	 */
 	public class EclipseInputSourceListener implements InputSourceListener {
 		
 		/**
 		 * Explicit empty default constructor so we can see who invokes it.
 		 */
 		public EclipseInputSourceListener() {
 			
 		}
 		
 		@Override
 		public void notifyInputSourceRemoved(InputSource source) {
 			if(source instanceof IFileInputSource) {
 				IFileInputSource fileSource = (IFileInputSource) source;
 				ChameleonDocument doc = documentOfPath(toPath(fileSource));
 				_documents.remove(doc);
 				doc.destroy();
 			}
 		}
 
 		protected IPath toPath(IFileInputSource fileSource) {
 			IFile ifile = toFile(fileSource);
 			return ifile.getFullPath();
 		}
 
 		protected IFile toFile(IFileInputSource fileSource) {
 			File file = fileSource.file();
 			IWorkspace workspace= ResourcesPlugin.getWorkspace();
 			IPath location= Path.fromOSString(file.getAbsolutePath());
 			IFile ifile= workspace.getRoot().getFileForLocation(location);
 			return ifile;
 		}
 
 		@Override
 		public void notifyInputSourceAdded(InputSource source) {
 			if(source instanceof IFileInputSource) {
 				IFileInputSource fileSource = (IFileInputSource) source;
 				//FIXME Let the document listen to "the" input source
 				//      fileSource.document() is null when lazy loading is used and the document hasn't been needed yet.
 				addToModel(new ChameleonDocument(ChameleonProjectNature.this,fileSource,toFile(fileSource),toPath(fileSource)));
 			}
 		}
 	}
 
 	public ChameleonProjectNature() {
 		_documents=new ArrayList<ChameleonDocument>() {
 			@Override
 			public boolean add(ChameleonDocument e) {
 				if(e == null) {
 					throw new IllegalArgumentException();
 				}
 				return super.add(e);
 			}
 		};
 	}
 	
 	//the project this natures resides
 	private IProject _project;
 	
 	//the language of this nature
 //	private Language _language;
 	
 	public static final String CHAMELEON_PROJECT_FILE_EXTENSION = "CHAMPROJECT";
 
 	public static final String CHAMELEON_PROJECT_FILE = "project.xml";
 
 	//The elements in the model of this nature
 	private ArrayList<ChameleonDocument> _documents;
 	
 	public static final String NATURE = ChameleonEditorPlugin.PLUGIN_ID+".ChameleonNature";
 		
 	public PresentationModel presentationModel() {
 		return LanguageMgt.getInstance().getPresentationModel(view().language().name());
 	}
 //
 //	/**
 //	 * Set the language, and attach the source manager and the input processor.
 //	 * @param language
 //	 */
 //	public void init(Project project){
 //		if(project == null) {
 //			throw new ChameleonProgrammerException("Cannot set the project of a Chameleon project nature to null.");
 //		}
 //		this._chameleonProject = project;
 ////		for(View view: project.views()) {
 ////		}
 //	}
 	
 	public Project chameleonProject() {
 		return _chameleonProject;
 	}
 	
 	private Project _chameleonProject;
 	
 	/**
 	 * Configure this nature. This method is called by Eclipse after initialisation.
 	 * We use it to create the Chameleon builder. FIXME: this also starts a new build job. I'm not
 	 * sure if that should happen.
 	 */
 	public void configure() throws CoreException {
 		System.out.println("Configuring Chameleon project nature.");
 		//later om de builders in te steken (compiler?)
 		addBuilder(getProject(), ChameleonBuilder.BUILDER_ID);
 		new Job("Chameleon Project Build"){
 		
 			@Override
 			protected IStatus run(IProgressMonitor monitor) {
 				try {
 					getProject().build(ChameleonBuilder.FULL_BUILD, ChameleonBuilder.BUILDER_ID, null, monitor);
 					return Status.OK_STATUS;
 				}catch(CoreException exc) {
 					return Status.CANCEL_STATUS;
 				}
 			}
 		}.schedule();
 	}
 	
   private void addBuilder(IProject project, String id) throws CoreException {
     IProjectDescription desc = project.getDescription();
     ICommand[] commands = desc.getBuildSpec();
     for (int i = 0; i < commands.length; ++i) {
        if (commands[i].getBuilderName().equals(id)) {
           return;
        }
     }
     //add builder to project
     ICommand command = desc.newCommand();
     command.setBuilderName(id);
     ICommand[] nc = new ICommand[commands.length + 1];
     // Add it before other builders.
     System.arraycopy(commands, 0, nc, 1, commands.length);
     nc[0] = command;
     desc.setBuildSpec(nc);
     project.setDescription(desc, null);
  }
 
 
 	/**
 	 * Deconfigures this nature. To be called before object destruction.
 	 */
 	public void deconfigure() throws CoreException {
 		getProject().getWorkspace().removeResourceChangeListener(_projectListener);
 	}
 
 	/**
 	 * returns the project where this nature is linked with
 	 */
 	public IProject getProject() {
 		return _project;
 	}
 
 	/**
 	 * A new project for this nature is set. Based on the chameleon project description file,
 	 * the project is initialized.
 	 */
 	public void setProject(IProject project) {
 		if(project != _project) {
 			IProject old = _project;
 			this._project = project;
 			if(project != null) {
 				try {
 					IPath location = project.getLocation();
 					File file = location.toFile();
 					final EclipseInputSourceListener listener = new EclipseInputSourceListener();
 					BootstrapProjectConfig bootstrapProjectConfig = new BootstrapProjectConfig(file, workspace());
 					_chameleonProject = bootstrapProjectConfig.project(new File(location+"/"+CHAMELEON_PROJECT_FILE), new ProjectInitialisationListener(){
 						@Override
 						public void viewAdded(View view) {
 							// Attach listeners for document loaders which attaches
 							// the listeners for the input sources.
 							view.addListener(new ViewListener(){
 								@Override
 								public void sourceLoaderAdded(DocumentLoader loader) {
 									loader.addAndSynchronizeListener(listener);
 								}
 							});
 							view.setPlugin(SourceManager.class, new EclipseSourceManager(ChameleonProjectNature.this));
 							//FIXME This should not be attached to a language, but to a view.
 							view.addProcessor(InputProcessor.class, new EclipseEditorInputProcessor(ChameleonProjectNature.this));
 							// Let the editor extension initialize the view.
 							//FIXME this should not be done by IDE code. Need further improvements
 							view.language().plugin(EclipseEditorExtension.class).initialize(view);
 							//FIXME This will NOT work with language stacking, but for now it will do. Got more important things to do now.
 //							_language = language;
 							_view = view;
 						}
 					});
 					_projectListener = new ProjectChangeListener(this);
 					getProject().getWorkspace().addResourceChangeListener(_projectListener, IResourceChangeEvent.POST_CHANGE);
 					// It should be sufficient to register the listener once for the entire project. Now we need a more
 					// complicated setup to deal with adding document loaders to a view.
 				} catch (ConfigException e) {
 					e.printStackTrace();
 					System.out.println("Error while loading the project");
 				}
 			} else {
 				old.getWorkspace().removeResourceChangeListener(_projectListener);
 			}
 		}
 	}
 	
 	/*
 	 * TODO get rid of this static nonsense.
 	 */
 	protected Workspace workspace() {
 		return LanguageMgt.getInstance().workspace();
 	}
 	
 	private IResourceChangeListener _projectListener;
 	
 	/*
 	 *  (non-Javadoc)
 	 * @see chameleonEditor.editors.IChameleonDocument#getMetaModelFactory()
 	 */
 	public ModelFactory modelFactory() {
 		return view().language().plugin(ModelFactory.class);
 	}
 
 	/**
 	 * Removes the old file markers and document positions, and asks the model factory
 	 * to replace the contents of the compilation unit associated with the document.
 	 * @param doc
 	 */
 	public void updateModel(ChameleonDocument doc) {
 		
 		// Removing the file markers is already done someplace else, and it gives a ResourceException
 		// when this method is ran via the project listener of this nature.
 		
 //		try {
 //			IFile file = doc.getFile();
 //			if(documentOfFile(file) != null) {
 //			  file.deleteMarkers(IMarker.PROBLEM, true, IResource.DEPTH_ZERO);
 //			}
 //			// getDocument().removeParseErrors();
 //		} catch (CoreException e) {
 //			e.printStackTrace();
 //		}
 		if(doc != null) {
 			doc.dumpPositions();
 			try {
 				modelFactory().parse(doc.get(), doc.chameleonDocument());
 			} catch (ParseException e) {
 				// FIXME Can we ignore this exception? Normally, the parse error markers should have been set.
 				e.printStackTrace();
 			} finally {
 				flushSourceCache();
 			}
 		}
 	}
 
 //	/**
 //	 * Clear the list of documents, and loads all the project files with a project extension into the model.
 //	 */
 //	public void loadDocuments(){
 //		_documents.clear();
 //		IResource[] resources;
 //		try {
 //			resources = getProject().members();
 //			for (int i = 0; i < resources.length; i++) {
 //				IResource resource = resources[i];
 //				addResourceToModel(resource);
 //			}
 //		
 //		} catch (CoreException e) {
 //			e.printStackTrace();
 //		}
 //		
 //	}
 
 	/**
 	 * Check whether the given resource is the Chameleon project description file.
 	 */
 	protected boolean isChameleonProjectFile(IResource resource) {
 		String ext = extension(resource);
 		return ext != null && ext.equals(CHAMELEON_PROJECT_FILE_EXTENSION);
 	}
 
 	/**
 	 * Check whether the given resource is the Eclipse project description file.
 	 */
 	protected boolean isEclipseProjectFile(IResource resource) {
 		String ext = extension(resource);
 		return ext != null && ext.equals("project");
 	}
 
 	protected String extension(IResource resource) {
 		return resource.getFullPath().getFileExtension();
 	}
 
 //	/**
 //	 * Adds another resource to the model. This can be either a file or a folder
 //	 * @param resource
 //	 */
 //	public void addResourceToModel(IResource resource) {
 ////		List<String> extensions = LanguageMgt.getInstance().extensions(language());
 //		if (resource instanceof IFile)  {
 //			if(!(isEclipseProjectFile(resource) || (isChameleonProjectFile(resource)))) {
 //				IPath fullPath = resource.getFullPath();
 //				String fileExtension = fullPath.getFileExtension();
 //				if(extensions.contains(fileExtension)) {
 //					System.out.println("ADDING :: "+resource.getName());
 //					addToModel(new ChameleonDocument(this,(IFile)resource,resource.getFullPath()));
 //				}
 //			}
 //		}
 //		if (resource instanceof IFolder) {
 //			IFolder folder = (IFolder) resource ;
 //			IResource[] resources = null;
 //			try {
 //				resources = folder.members();
 //			} catch (CoreException e) {
 //				e.printStackTrace();
 //			}
 //			for (int i = 0; i < resources.length; i++) {
 //				IResource folderResource = resources[i];
 //				addResourceToModel(folderResource);
 //			}
 //			
 //		}
 //	}
 
 
 	
 
 
 	/**
 	 * @see chameleonEditor.editors.IChameleonDocument#getModel()
 	 * @deprecated
 	 */
 	public Namespace getModel(){
 		//FIXME This should be removed for multi-view support
 		return chameleonProject().views().get(0).namespace();
 	}
 
 	/**
 	 * Adds an extra chameleonDocument to this nature
 	 * @param document
	 * @deprecated
 	 */
 	public void addToModel(ChameleonDocument document) {
 		//FIXME: why do we remove the 'same' document and add the new one if
 		//       the document was already in the model?
 		ChameleonDocument same = null;
 		for (ChameleonDocument element: _documents) {
 			if (element.isSameDocument(document)) {
 				same = element;
 			}
 		}
 		if (same!=null) {
 			removeDocument(same);
 		}
		_documents.add(document);
		
//		addDocument(document);
 		//FIXME why update? I think this can go because now the project nature
 		// sends an event to the appropriate document loader, which in turn
 		// sends an event back to add a ChameleonDocument for the document
 //		updateModel(document);
 	}
 
 	public List<ChameleonDocument> documents(){
 		return new ArrayList<ChameleonDocument>(_documents);
 	}
 	
 	/**
 	 * Return the Chameleon/Eclipse document for the compilation unit of the
 	 * given element.
 	 * 
 	 * @param element
 	 * @return
 	 */
 	public ChameleonDocument document(Element element) {
 		if(element != null) {
 			Document cu = element.nearestAncestorOrSelf(Document.class);
 			for(ChameleonDocument doc : _documents) {
 //				if(doc.chameleonDocument().equals(cu)) {
 				if(doc.inputSource().equals(cu.inputSource())) {
 					return doc;
 				}
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Remove the given document from the project. The document is removed from the
 	 * list of documents in the project, and the compilation unit of the document
 	 * is disconnected from the project.
 	 * 
 	 * If the given document is null, nothing happens.
 	 */
  /*@
    @ post ! documents().contains(document);
    @ post document != null ==> document.compilationUnit().disconnected();
    @*/
 	public void removeDocument(ChameleonDocument document){
 		if(document != null) {
 			_documents.remove(document);
 			document.chameleonDocument().disconnect();
 		}
 	}
 
 	public View view() {
 		return _view;
 	}
 	
 	private View _view;
 
 	public void addDocument(ChameleonDocument document) {
 		if(document == null) {
 			throw new IllegalArgumentException();
 		}
 		_documents.add(document);
 		try {
 			Document chameleonDocument = document.chameleonDocument();
 			Language language = chameleonDocument.view().language();
 			ModelFactory plugin = language.plugin(ModelFactory.class);
 			plugin.parse(document.get(), chameleonDocument);
 			chameleonDocument.activate();
 			//modelFactory().parse(document.get(), chameleonDocument);
 		} catch (ParseException e) {
 			e.printStackTrace();
 		} 
 	}
 	
 	public ChameleonDocument documentOfPath(IPath path) {
 		ChameleonDocument result = null;
 		for(ChameleonDocument doc:_documents) {
 			if(doc.path().equals(path)) {
 				result = doc;
 				break;
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * Returns the document with the given file
 	 * @param file must be effective
 	 * @return returns null if no appropriate document found.
 	 */
 	public ChameleonDocument documentOfFile(IFile file){
 		if(file == null) {
 			return null;
 		}
 		for(ChameleonDocument doc : _documents){
 			if(file.equals(doc.getFile())){
 				return doc;
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Static method to retrieve the language of the current editor. This
 	 * is required because the views are global, and not connected to a particular
 	 * project.
 	 * @return
 	 */
 	public static Language getCurrentLanguage(){
 		ChameleonEditor editor = ChameleonEditor.getActiveEditor();
 		if(editor!=null){
 			ChameleonDocument doc = editor.getDocument();
 			if(doc!= null) {
 				return doc.language();
 			}
 		}
 		return null;
 	}
 
 	public List<Document> compilationUnits() {
 		ArrayList<Document> result = new ArrayList<Document>();
 		for(ChameleonDocument document: documents()) {
 			result.add(document.chameleonDocument());
 		}
 		return result;
 	}
 
 	public void flushSourceCache() {
 		view().flushSourceCache();
 	}
 	
 	public void acquire() throws InterruptedException {
 		_semaphore.acquire();
 	}
 
 	public void release() {
 		_semaphore.release();
 	}
 	
 	private Semaphore _semaphore = new Semaphore(1);
 	
 	public void clearMarkers() throws CoreException {
 		for(ChameleonDocument document: _documents) {
 			IFile file = document.getFile();
 			file.deleteMarkers(IMarker.PROBLEM, true, IResource.DEPTH_ZERO);
 		}
 	}
 }
