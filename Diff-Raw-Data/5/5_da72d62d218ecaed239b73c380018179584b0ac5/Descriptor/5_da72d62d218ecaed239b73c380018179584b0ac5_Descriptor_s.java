 package org.seventyeight.web.model;
 
 import org.apache.log4j.Logger;
 import org.apache.velocity.VelocityContext;
 import org.seventyeight.database.mongodb.MongoDBCollection;
 import org.seventyeight.database.mongodb.MongoDBQuery;
 import org.seventyeight.database.mongodb.MongoDocument;
 import org.seventyeight.database.orm.SimpleORM;
 import org.seventyeight.web.Core;
 import org.seventyeight.web.CoreException;
 import org.seventyeight.web.handlers.template.TemplateException;
 import org.seventyeight.web.servlet.Request;
 import org.seventyeight.web.servlet.Response;
 
 import java.io.IOException;
 import java.lang.reflect.Constructor;
 import java.util.Collections;
 import java.util.Date;
 import java.util.List;
 
 public abstract class Descriptor<T extends Describable<T>> {
 	
 	private static Logger logger = Logger.getLogger( Descriptor.class );
 	
 	protected transient Class<T> clazz;
 	
 	protected Descriptor() {
 		clazz = (Class<T>) getClass().getEnclosingClass();
 		logger.debug( "Descriptor class is " + clazz );
 	}
 
     public List<String> getRequiredJavascripts() {
         return Collections.EMPTY_LIST;
     }
 	
 	public abstract String getDisplayName();
 
     /*
 	public T newInstance( String title ) throws ItemInstantiationException {
 		logger.debug( "New instance for " + clazz );
 		return createSubDocument( title, null );
 	}
 	*/
 
     //public abstract T newInstance( String title ) throws ItemInstantiationException;
 
     public T newInstance( String title, Node parent ) throws ItemInstantiationException {
         logger.debug( "New instance for " + clazz );
         return createSubDocument( title, parent );
     }
 
     protected T createSubDocument( String title, Node parent ) throws ItemInstantiationException {
         logger.debug( "Creating sub document " + clazz.getName() );
 
         MongoDocument document = new MongoDocument();
 
         T instance = null;
         try {
             Constructor<T> c = clazz.getConstructor( Node.class, MongoDocument.class );
             instance = c.newInstance( parent, document );
         } catch( Exception e ) {
             throw new ItemInstantiationException( "Unable to instantiate " + clazz.getName(), e );
         }
 
         document.set( "title", title );
         document.set( "class", clazz.getName() );
 
         return instance;
     }
 
     /**
      * Get the descriptors for
      * @return
      */
     public List<Class> getExtensionClasses() {
         return Collections.emptyList();
     }
 
     /**
      * Get the class of the {@link Descriptor}s {@link Describable}.
      * @return
      */
 	public Class<? extends Describable> getClazz() {
 		return clazz;
 	}
 
     public String getId() {
         return getClazz().getName();
     }
 
     public String getJsonId() {
         return getId().replace( '.', '-' );
     }
 
     /**
      * When instantiated the descriptor can configure an index
      */
     public void configureIndex() {
         /* Default implementation is a no op */
     }
 
     public boolean enabledByDefault() {
         return false;
     }
 
     public List<Searchable> getSearchables() {
         return Collections.EMPTY_LIST;
     }
 
     public String getConfigurationPage( Request request ) throws TemplateException, NotFoundException {
         return getConfigurationPage( request, null );
     }
 
     public String getConfigurationPage( Request request, AbstractExtension extension ) throws TemplateException, NotFoundException {
         VelocityContext c = new VelocityContext();
         c.put( "class", getClazz().getName() );
         c.put( "descriptor", this );
 
         if( extension != null ) {
             logger.debug( "Extension is " + extension );
             c.put( "enabled", true );
            c.put( "content", Core.getInstance().getTemplateManager().getRenderer( request ).renderObject( extension, "config.vm" ) );
         } else {
             logger.debug( "Preparing EMPTY " + getClazz() );
             c.put( "enabled", false );
            c.put( "content", Core.getInstance().getTemplateManager().getRenderer( request ).renderClass( getClazz(), "config.vm" ) );
         }
 
         return Core.getInstance().getTemplateManager().getRenderer( request ).setContext( c ).render( "org/seventyeight/web/model/descriptorpage.vm" );
     }
 
     public String getRelationType() {
         return Core.Relations.EXTENSIONS;
     }
 
     /**
      * Determine whether to remove data items on configure.
      * @return
      */
     public boolean doRemoveDataItemOnConfigure() {
         return false;
     }
 
     public String getEnctype() {
         return "application/x-www-form-urlencoded";
     }
 
     public boolean hasGlobalConfiguration() {
         //Core.getInstance().getTemplateManager().getTemplateFromClass(  )
         return true;
     }
 
     /*
     public String getGlobalConfigurationPage() {
 
     }
     */
 
     public void doSubmit( Request request, Response response ) throws IOException {
         save( request, response );
         MongoDocument doc = getDescriptorDocument();
         try {
             SimpleORM.storeFromObject( this, doc );
         } catch( IllegalAccessException e ) {
             throw new IOException( e );
         }
 
         logger.debug( "SAVING " + doc );
         MongoDBCollection.get( Core.DESCRIPTOR_COLLECTION_NAME ).save( doc );
 
         /* TODO get a better URL */
         response.sendRedirect( "/" );
     }
 
     public void loadFromDisk() throws CoreException {
         MongoDocument doc = getDescriptorDocument();
         logger.debug( "Configuration for " + this.getDisplayName() + ": " + doc );
         try {
             SimpleORM.bindToObject( this, doc );
         } catch( IllegalAccessException e ) {
             throw new CoreException( "Unable to load " + this, e );
         }
     }
 
     /**
      * Get the {@link MongoDocument} for the {@link Descriptor}. Will never return null.
      * @return
      */
     public MongoDocument getDescriptorDocument() {
         MongoDocument doc = MongoDBCollection.get( Core.DESCRIPTOR_COLLECTION_NAME ).findOne( new MongoDBQuery().is( "_id", this.getId() ) );
         if( !doc.isNull() ) {
             logger.debug( "Had a configuration" );
             return doc;
         } else {
             logger.debug( "New empty document" );
             MongoDocument newdoc = new MongoDocument();
             newdoc.set( "_id", getId() );
             return newdoc;
         }
     }
 
     /**
      * Base class that does nothing
      * @param request
      * @param response
      */
     public void save( Request request, Response response ) {
         logger.debug( "Saving " + getClass() );
     }
 }
