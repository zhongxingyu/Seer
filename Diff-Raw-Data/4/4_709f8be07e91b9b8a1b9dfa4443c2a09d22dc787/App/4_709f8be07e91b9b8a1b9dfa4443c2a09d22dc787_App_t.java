 package com.cefn.filesystem;
 
 import java.lang.annotation.ElementType;
 import java.lang.annotation.Retention;
 import java.lang.annotation.RetentionPolicy;
 import java.lang.annotation.Target;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.Properties;
 
 import javax.inject.Inject;
 import javax.persistence.EntityManager;
 
 import com.cefn.filesystem.factory.FileFactory;
 import com.cefn.filesystem.factory.FilesystemFactory;
 import com.cefn.filesystem.factory.FolderFactory;
 import com.cefn.filesystem.impl.FileImpl;
 import com.cefn.filesystem.impl.FilesystemImpl;
 import com.cefn.filesystem.impl.FolderImpl;
 import com.cefn.filesystem.traversal.DepthFirstFileVisitor;
 import com.cefn.filesystem.traversal.LiveTraversal;
 import com.cefn.filesystem.traversal.CachedTraversal;
 import com.cefn.filesystem.traversal.Traversal;
 import com.google.inject.AbstractModule;
 import com.google.inject.BindingAnnotation;
 import com.google.inject.Guice;
 import com.google.inject.Injector;
 import com.google.inject.assistedinject.FactoryModuleBuilder;
 import com.google.inject.persist.PersistService;
 import com.google.inject.persist.jpa.JpaPersistModule;
 
 /** Accesses objects implementing interfaces with routines backed by a real file system. 
  * Stores the data accessed in this way through JPA annotations on POJO domain objects. 
  * Retrieves the file system data through objects backed by JPA Database retrieval.
 */
 public class App {
 		
 	public static void main(String[] args){
 				
 		//start up dependency injection
 		Injector injector = Guice.createInjector(
 				new JpaPersistModule("openjpa"),
 				new FilesystemModule(args)
 		);
 
 		//start up persistence
 		injector.getInstance(PersistService.class).start();
 		
 		//load and run app
 		App app = injector.getInstance(App.class);
 		app.run();
 		
 	}
 		
 	static class FilesystemModule extends AbstractModule{
 				
 		private final String[] args;
 		
 		public FilesystemModule(String[] args){
 			this.args = args;
 		}
 		
 		protected void configure() {
 			
 			//App relies on defs below
 			bind(App.class);
 
 			//make arguments globally available to constructors carrying Args annotation
 			bind(String[].class).annotatedWith(Args.class).toInstance(args);
 		
 			FactoryModuleBuilder factoryModuleBuilder = new FactoryModuleBuilder();
 			install(factoryModuleBuilder.implement(Filesystem.class, FilesystemImpl.class).build(FilesystemFactory.class));
 			install(factoryModuleBuilder.implement(Folder.class, FolderImpl.class).build(FolderFactory.class));
 			install(factoryModuleBuilder.implement(File.class, FileImpl.class).build(FileFactory.class));
 			
 			/** Constructs filesystem object on the fly by traversing file system. */
 			bind(Traversal.class).annotatedWith(Real.class).to(LiveTraversal.class);
 			
 			/** Constructs filesystem objects on the fly by loading from database. */
 			bind(Traversal.class).annotatedWith(Fake.class).to(CachedTraversal.class);
 		}
 											
 	}
 	
 	@BindingAnnotation @Retention(RetentionPolicy.RUNTIME) @Target({ElementType.PARAMETER}) 
 	public @interface Args {}
 
 	@BindingAnnotation @Retention(RetentionPolicy.RUNTIME) @Target({ElementType.PARAMETER}) 
 	public @interface Real {}
 
 	@BindingAnnotation @Retention(RetentionPolicy.RUNTIME) @Target({ElementType.PARAMETER}) 
 	public @interface Fake {}
 
 	private final String[] args;
 	private final EntityManager entityManager;
 	private Traversal toRecord, toPlayback;
 	private FilesystemFactory filesystemFactory;
 	
 	@Inject
 	App(@Args String[] args, 
 		@Real Traversal toRecord, @Fake Traversal toPlayback, 
 		EntityManager entityManager,
 		FilesystemFactory filesystemFactory){
 		this.args = args;
 		this.toRecord = toRecord;
 		this.toPlayback = toPlayback;
 		this.entityManager = entityManager;
 		this.filesystemFactory = filesystemFactory;		
 	}
 	
 	public void run(){
 		
 		//ask it to configure this app instance (in particular inject an EntityManager)
 		try{			
 
 			Filesystem filesystemInput = filesystemFactory.create(new URL("file:///home/cefn/"));
 			
 			/* Traverse live file hierarchy depth first, storing data */
 			new DepthFirstFileVisitor(toRecord) {
 				public void visit(File f) {
 					entityManager.getTransaction().begin();
					f = entityManager.merge(f);
					//entityManager.persist(f);
 					entityManager.getTransaction().commit();
 				}
 			}.visit(filesystemInput);
 			
 			
 			/** Retrieve file system object from database */
 			Filesystem filesystemOutput = (Filesystem)entityManager.createQuery("SELECT fs FROM filesystem AS fs").getSingleResult();
 			
 			/* Traverse stored file hierarchy depth first, printing out data */
 			new DepthFirstFileVisitor(toPlayback) {
 				public void visit(File f) {
 					System.out.println("Retrieved file : " + f.getLocation());
 				}
 			}.visit(filesystemOutput);
 			
 		}
 		catch(MalformedURLException mue){
 			throw new RuntimeException(mue);
 		}
 		
 	}
 		
 }
