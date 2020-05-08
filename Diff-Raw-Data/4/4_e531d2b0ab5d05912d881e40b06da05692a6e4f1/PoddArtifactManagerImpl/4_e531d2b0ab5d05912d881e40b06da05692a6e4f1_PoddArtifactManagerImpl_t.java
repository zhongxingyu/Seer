 /**
  * 
  */
 package com.github.podd.impl;
 
 import info.aduna.iteration.Iterations;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.List;
 import java.util.Set;
 import java.util.UUID;
 
 import org.openrdf.OpenRDFException;
 import org.openrdf.model.Model;
 import org.openrdf.model.Resource;
 import org.openrdf.model.Statement;
 import org.openrdf.model.URI;
 import org.openrdf.model.Value;
 import org.openrdf.model.impl.ValueFactoryImpl;
 import org.openrdf.model.vocabulary.OWL;
 import org.openrdf.model.vocabulary.RDF;
 import org.openrdf.repository.Repository;
 import org.openrdf.repository.RepositoryConnection;
 import org.openrdf.repository.RepositoryException;
 import org.openrdf.repository.RepositoryResult;
 import org.openrdf.rio.RDFFormat;
 import org.openrdf.rio.Rio;
 import org.semanticweb.owlapi.model.IRI;
 import org.semanticweb.owlapi.model.OWLException;
 import org.semanticweb.owlapi.model.OWLOntology;
 import org.semanticweb.owlapi.model.OWLOntologyID;
 import org.semanticweb.owlapi.profiles.OWLProfileReport;
 import org.semanticweb.owlapi.reasoner.OWLReasoner;
 import org.semanticweb.owlapi.rio.RioMemoryTripleSource;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.github.podd.api.DanglingObjectPolicy;
 import com.github.podd.api.FileReferenceVerificationPolicy;
 import com.github.podd.api.PoddArtifactManager;
 import com.github.podd.api.PoddOWLManager;
 import com.github.podd.api.PoddRepositoryManager;
 import com.github.podd.api.PoddSchemaManager;
 import com.github.podd.api.PoddSesameManager;
 import com.github.podd.api.UpdatePolicy;
 import com.github.podd.api.file.FileReference;
 import com.github.podd.api.file.FileReferenceManager;
 import com.github.podd.api.file.PoddFileRepositoryManager;
 import com.github.podd.api.purl.PoddPurlManager;
 import com.github.podd.api.purl.PoddPurlReference;
 import com.github.podd.exception.DeleteArtifactException;
 import com.github.podd.exception.DisconnectedObjectException;
 import com.github.podd.exception.EmptyOntologyException;
 import com.github.podd.exception.FileReferenceVerificationFailureException;
 import com.github.podd.exception.InconsistentOntologyException;
 import com.github.podd.exception.OntologyNotInProfileException;
 import com.github.podd.exception.PoddException;
 import com.github.podd.exception.PoddRuntimeException;
 import com.github.podd.exception.PublishArtifactException;
 import com.github.podd.exception.PurlProcessorNotHandledException;
 import com.github.podd.exception.UnmanagedArtifactIRIException;
 import com.github.podd.exception.UnmanagedSchemaIRIException;
 import com.github.podd.utils.InferredOWLOntologyID;
 import com.github.podd.utils.PoddRdfConstants;
 import com.github.podd.utils.RdfUtility;
 
 /**
  * Implementation of the PODD Artifact Manager API, to manage the lifecycle for PODD Artifacts.
  * 
  * @author Peter Ansell p_ansell@yahoo.com
  * 
  */
 public class PoddArtifactManagerImpl implements PoddArtifactManager
 {
     private final Logger log = LoggerFactory.getLogger(this.getClass());
     
     private FileReferenceManager fileReferenceManager;
     private PoddFileRepositoryManager fileRepositoryManager;
     private PoddOWLManager owlManager;
     private PoddPurlManager purlManager;
     private PoddSchemaManager schemaManager;
     private PoddRepositoryManager repositoryManager;
     
     private PoddSesameManager sesameManager;
     
     /**
      * 
      */
     public PoddArtifactManagerImpl()
     {
     }
     
     @Override
     public InferredOWLOntologyID attachFileReference(final InferredOWLOntologyID artifactId, final URI objectUri,
             final FileReference fileReference) throws OpenRDFException, PoddException
     {
         throw new RuntimeException("TODO: Implement attachFileReference");
     }
     
     @Override
     public InferredOWLOntologyID attachFileReferences(final URI artifactUri, final URI versionUri,
             final InputStream inputStream, final RDFFormat format,
             final FileReferenceVerificationPolicy fileReferenceVerificationPolicy) throws OpenRDFException,
         IOException, OWLException, PoddException
     {
         final Model model = Rio.parse(inputStream, "", format);
         
         model.removeAll(model.filter(null, PoddRdfConstants.PODD_BASE_INFERRED_VERSION, null));
         
         Set<Resource> fileReferences =
                 model.filter(null, RDF.TYPE, PoddRdfConstants.PODD_BASE_FILE_REFERENCE_TYPE).subjects();
         Collection<URI> fileReferenceObjects = new ArrayList<URI>(fileReferences.size());
         for(Resource nextFileReference : fileReferences)
         {
             if(nextFileReference instanceof URI)
             {
                 fileReferenceObjects.add((URI)nextFileReference);
             }
             else
             {
                 log.warn("Will not be updating file reference for blank node reference, will instead be creating a new file reference for it.");
             }
         }
         
         final ByteArrayOutputStream output = new ByteArrayOutputStream(8192);
         
         Rio.write(model, output, RDFFormat.RDFJSON);
         
         return this.updateArtifact(artifactUri, versionUri, fileReferenceObjects,
                 new ByteArrayInputStream(output.toByteArray()), RDFFormat.RDFJSON, UpdatePolicy.MERGE_WITH_EXISTING,
                 DanglingObjectPolicy.REPORT, fileReferenceVerificationPolicy);
     }
     
     /**
      * Internal helper method to build an array of Contexts to search in. If a valid artifact ID is
      * passed in, the artifact's concrete triples and its imported schema ontologies are assigned as
      * the contexts to return. If the artifact ID is NULL, all schema ontologies currently
      * configured in the PODD server are assigned into the returned array.
      * 
      * TODO: Could this be moved into PoddSesameManager.java, possibly with the option to add
      * inferred statements also as contexts.
      * 
      * @param artifactID
      * @param connection
      * @param managementGraph
      * @throws OpenRDFException
      */
     private URI[] buildContextArray(final InferredOWLOntologyID artifactID, final RepositoryConnection connection,
             final URI managementGraph) throws OpenRDFException
     {
         final List<URI> contexts = new ArrayList<URI>();
         if(artifactID != null)
         {
             contexts.add(artifactID.getVersionIRI().toOpenRDFURI());
             
             final Set<IRI> directImports = this.sesameManager.getDirectImports(artifactID, connection);
             for(final IRI directImport : directImports)
             {
                 contexts.add(directImport.toOpenRDFURI());
             }
         }
         else
         {
             final List<InferredOWLOntologyID> allSchemaOntologyVersions =
                     this.sesameManager.getAllSchemaOntologyVersions(connection, managementGraph);
             for(final InferredOWLOntologyID schemaOntology : allSchemaOntologyVersions)
             {
                 contexts.add(schemaOntology.getVersionIRI().toOpenRDFURI());
             }
         }
         return contexts.toArray(new URI[0]);
     }
     
     @Override
     public boolean deleteArtifact(final InferredOWLOntologyID artifactId) throws PoddException
     {
         if(artifactId.getOntologyIRI() == null)
         {
             throw new PoddRuntimeException("Ontology IRI cannot be null");
         }
         
         RepositoryConnection connection = null;
         
         try
         {
             connection = this.getRepositoryManager().getRepository().getConnection();
             
             List<InferredOWLOntologyID> requestedArtifactIds =
                     this.getSesameManager().getAllOntologyVersions(artifactId.getOntologyIRI(), connection,
                             this.getRepositoryManager().getArtifactManagementGraph());
             
             if(artifactId.getVersionIRI() != null)
             {
                 final IRI requestedVersionIRI = artifactId.getVersionIRI();
                 
                 for(final InferredOWLOntologyID nextVersion : new ArrayList<InferredOWLOntologyID>(requestedArtifactIds))
                 {
                     if(requestedVersionIRI.equals(nextVersion.getVersionIRI()))
                     {
                         requestedArtifactIds = Arrays.asList(nextVersion);
                     }
                 }
             }
             
             connection.begin();
             this.getSesameManager().deleteOntologies(requestedArtifactIds, connection,
                     this.getRepositoryManager().getArtifactManagementGraph());
             connection.commit();
             
             return !requestedArtifactIds.isEmpty();
         }
         catch(final OpenRDFException e)
         {
             try
             {
                 if(connection != null && connection.isActive())
                 {
                     connection.rollback();
                 }
             }
             catch(final RepositoryException e1)
             {
             }
             
             throw new DeleteArtifactException("Repository exception occurred", e, artifactId);
         }
         finally
         {
             try
             {
                 if(connection != null && connection.isOpen())
                 {
                     connection.close();
                 }
             }
             catch(final RepositoryException e)
             {
                 throw new DeleteArtifactException("Repository exception occurred", e, artifactId);
             }
         }
     }
     
     @Override
     public void exportArtifact(final InferredOWLOntologyID ontologyId, final OutputStream outputStream,
             final RDFFormat format, final boolean includeInferred) throws OpenRDFException, PoddException, IOException
     {
         if(ontologyId.getOntologyIRI() == null || ontologyId.getVersionIRI() == null)
         {
             throw new PoddRuntimeException("Ontology IRI and Version IRI cannot be null");
         }
         
         if(includeInferred && ontologyId.getInferredOntologyIRI() == null)
         {
             throw new PoddRuntimeException("Inferred Ontology IRI cannot be null");
         }
         
         List<URI> contexts;
         
         if(includeInferred)
         {
             contexts =
                     Arrays.asList(ontologyId.getVersionIRI().toOpenRDFURI(), ontologyId.getInferredOntologyIRI()
                             .toOpenRDFURI());
         }
         else
         {
             contexts = Arrays.asList(ontologyId.getVersionIRI().toOpenRDFURI());
         }
         
         RepositoryConnection connection = null;
         
         try
         {
             connection = this.getRepositoryManager().getRepository().getConnection();
             
             connection.export(Rio.createWriter(format, outputStream), contexts.toArray(new Resource[] {}));
         }
         finally
         {
             if(connection != null)
             {
                 connection.close();
             }
         }
     }
     
     @Override
     public void exportObjectMetadata(final URI objectType, final OutputStream outputStream, final RDFFormat format,
             final boolean includeDoNotDisplayProperties, final InferredOWLOntologyID artifactID)
         throws OpenRDFException, PoddException, IOException
     {
         RepositoryConnection connection = null;
         
         try
         {
             connection = this.getRepositoryManager().getRepository().getConnection();
             
             final URI[] contexts =
                     this.buildContextArray(artifactID, connection, this.repositoryManager.getSchemaManagementGraph());
             
             final Model model =
                     this.sesameManager.getObjectTypeMetadata(objectType, includeDoNotDisplayProperties, connection,
                             contexts);
             
             Rio.write(model, outputStream, format);
         }
         finally
         {
             if(connection != null)
             {
                 connection.close();
             }
         }
     }
     
     @Override
     public InferredOWLOntologyID getArtifactByIRI(final IRI artifactIRI) throws UnmanagedArtifactIRIException
     {
         RepositoryConnection repositoryConnection = null;
         
         try
         {
             repositoryConnection = this.getRepositoryManager().getRepository().getConnection();
             
             return this.getSesameManager().getCurrentArtifactVersion(artifactIRI, repositoryConnection,
                     this.getRepositoryManager().getArtifactManagementGraph());
         }
         catch(final OpenRDFException e)
         {
             throw new UnmanagedArtifactIRIException(artifactIRI, e);
         }
         finally
         {
             if(repositoryConnection != null)
             {
                 try
                 {
                     repositoryConnection.close();
                 }
                 catch(final RepositoryException e)
                 {
                     this.log.error("Failed to close repository connection", e);
                 }
             }
         }
     }
     
     /*
      * (non-Javadoc)
      * 
      * @see com.github.podd.api.PoddArtifactManager#getFileReferenceManager()
      */
     @Override
     public FileReferenceManager getFileReferenceManager()
     {
         return this.fileReferenceManager;
     }
     
     @Override
     public Set<FileReference> getFileReferences(final InferredOWLOntologyID artifactId)
     {
         // TODO Auto-generated method stub
         return null;
     }
     
     @Override
     public Set<FileReference> getFileReferences(final InferredOWLOntologyID artifactId, final String alias)
     {
         // TODO Auto-generated method stub
         return null;
     }
     
     @Override
     public Set<FileReference> getFileReferences(final InferredOWLOntologyID artifactId, final URI objectUri)
     {
         // TODO Auto-generated method stub
         return null;
     }
     
     /*
      * (non-Javadoc)
      * 
      * @see com.github.podd.api.PoddArtifactManager#getFileRepositoryManager()
      */
     @Override
     public PoddFileRepositoryManager getFileRepositoryManager()
     {
         return this.fileRepositoryManager;
     }
     
     /*
      * (non-Javadoc)
      * 
      * @see com.github.podd.api.PoddArtifactManager#getOWLManager()
      */
     @Override
     public PoddOWLManager getOWLManager()
     {
         return this.owlManager;
     }
     
     /*
      * (non-Javadoc)
      * 
      * @see com.github.podd.api.PoddArtifactManager#getPurlManager()
      */
     @Override
     public PoddPurlManager getPurlManager()
     {
         return this.purlManager;
     }
     
     @Override
     public PoddRepositoryManager getRepositoryManager()
     {
         return this.repositoryManager;
     }
     
     @Override
     public PoddSchemaManager getSchemaManager()
     {
         return this.schemaManager;
     }
     
     @Override
     public PoddSesameManager getSesameManager()
     {
         return this.sesameManager;
     }
     
     /**
      * Helper method to cache schema ontologies in memory before loading statements into OWLAPI
      */
     private void handleCacheSchemasInMemory(final RepositoryConnection permanentRepositoryConnection,
             final RepositoryConnection tempRepositoryConnection, final URI tempContext) throws OpenRDFException,
         OWLException, IOException, PoddException
     {
         final Set<IRI> importedSchemas =
                 this.getSesameManager().getDirectImports(tempRepositoryConnection, tempContext);
         
         for(final IRI importedSchemaIRI : importedSchemas)
         {
             final InferredOWLOntologyID ontologyVersion =
                     this.getSesameManager().getSchemaVersion(importedSchemaIRI, permanentRepositoryConnection,
                             this.getRepositoryManager().getSchemaManagementGraph());
             
             this.getOWLManager().cacheSchemaOntology(ontologyVersion, permanentRepositoryConnection,
                     this.getRepositoryManager().getSchemaManagementGraph());
         }
     }
     
     /**
      * Checks for dangling objects that are not linked to the artifact and deletes them if
      * <i>force</i> is true.
      * 
      * @param artifactID
      * @param repositoryConnection
      * @param context
      * @param force
      *            If true, deletes any dangling objects. If false, throws a
      *            DisconnectedObjectException if any dangling objects are found.
      * @throws RepositoryException
      * @throws DisconnectedObjectException
      */
     private void handleDanglingObjects(final IRI artifactID, final RepositoryConnection repositoryConnection,
             final URI context, final DanglingObjectPolicy policy) throws RepositoryException,
         DisconnectedObjectException
     {
         final Set<URI> danglingObjects =
                 RdfUtility.findDisconnectedNodes(artifactID.toOpenRDFURI(), repositoryConnection, context);
         this.log.info("Found {} dangling object(s). \n {}", danglingObjects.size(), danglingObjects);
         
         if(danglingObjects.isEmpty())
         {
             return;
         }
         
         if(policy.equals(DanglingObjectPolicy.REPORT))
         {
             throw new DisconnectedObjectException(danglingObjects, "Update leads to disconnected PODD objects");
         }
         else if(policy.equals(DanglingObjectPolicy.FORCE_CLEAN))
         {
             for(final URI danglingObject : danglingObjects)
             {
                 repositoryConnection.remove(danglingObject, null, null, context);
                 repositoryConnection.remove(null, null, (Value)danglingObject, context);
             }
         }
     }
     
     /**
      * Helper method to handle File References in a newly loaded/updated set of statements.
      * 
      * TODO: Optionally remove invalid file references or mark them as invalid using RDF
      * statements/OWL Classes
      * 
      * @param repositoryConnection
      * @param context
      * @param policy
      *            If true, verifies that FileReference objects are accessible from their respective
      *            remote File Repositories
      * 
      * @throws OpenRDFException
      * @throws PoddException
      */
     private void handleFileReferences(final RepositoryConnection repositoryConnection,
             final FileReferenceVerificationPolicy policy, final URI... contexts) throws OpenRDFException, PoddException
     {
         if(this.getFileReferenceManager() == null)
         {
             return;
         }
         
         this.log.info("Handling File reference validation");
         
         final Set<FileReference> fileReferenceResults =
                 this.getFileReferenceManager().extractFileReferences(repositoryConnection, contexts);
         
         if(FileReferenceVerificationPolicy.VERIFY.equals(policy))
         {
             try
             {
                 this.fileRepositoryManager.verifyFileReferences(fileReferenceResults);
             }
             catch(final FileReferenceVerificationFailureException e)
             {
                 this.log.warn("From " + fileReferenceResults.size() + " file references, "
                         + e.getValidationFailures().size() + " failed validation.");
                 throw e;
             }
         }
     }
     
     /**
      * Helper method to handle File References in a newly loaded/updated set of statements
      */
     private void handlePurls(final RepositoryConnection repositoryConnection, final URI context)
         throws PurlProcessorNotHandledException, OpenRDFException
     {
         if(this.getPurlManager() != null)
         {
             this.log.info("Handling Purl generation");
             final Set<PoddPurlReference> purlResults =
                     this.getPurlManager().extractPurlReferences(repositoryConnection, context);
             
             this.getPurlManager().convertTemporaryUris(purlResults, repositoryConnection, context);
         }
     }
     
     /**
      * Helper method to check schema ontology imports and update use of ontology IRIs to version
      * IRIs.
      */
     private void handleSchemaImports(final IRI ontologyIRI, final RepositoryConnection permanentRepositoryConnection,
             final RepositoryConnection tempRepositoryConnection, final URI tempContext) throws OpenRDFException,
         UnmanagedSchemaIRIException
     {
         final Set<IRI> importedSchemas =
                 this.getSesameManager().getDirectImports(tempRepositoryConnection, tempContext);
         for(final IRI importedSchemaIRI : importedSchemas)
         {
             final InferredOWLOntologyID schemaOntologyID =
                     this.getSesameManager().getSchemaVersion(importedSchemaIRI, permanentRepositoryConnection,
                             this.getRepositoryManager().getSchemaManagementGraph());
             
             if(!importedSchemaIRI.equals(schemaOntologyID.getVersionIRI()))
             {
                 // modify import to be a specific version of the schema
                 this.log.info("Updating import to version <{}>", schemaOntologyID.getVersionIRI());
                 tempRepositoryConnection.remove(ontologyIRI.toOpenRDFURI(), OWL.IMPORTS,
                         importedSchemaIRI.toOpenRDFURI(), tempContext);
                 tempRepositoryConnection.add(ontologyIRI.toOpenRDFURI(), OWL.IMPORTS, schemaOntologyID.getVersionIRI()
                         .toOpenRDFURI(), tempContext);
             }
         }
     }
     
     /**
      * This is not an API method. QUESTION: Should this be moved to a separate utility class?
      * 
      * This method takes a String terminating with a colon (":") followed by an integer and
      * increments this integer by one. If the input String is not of the expected format, appends
      * "1" to the end of the String.
      * 
      * E.g.: "http://purl.org/ab/artifact:55" is converted to "http://purl.org/ab/artifact:56"
      * "http://purl.org/ab/artifact:5A" is converted to "http://purl.org/ab/artifact:5A1"
      * 
      * @param oldVersion
      * @return
      */
     public String incrementVersion(final String oldVersion)
     {
         final char versionSeparatorChar = ':';
         
         final int positionVersionSeparator = oldVersion.lastIndexOf(versionSeparatorChar);
         if(positionVersionSeparator > 1)
         {
             final String prefix = oldVersion.substring(0, positionVersionSeparator);
             final String version = oldVersion.substring(positionVersionSeparator + 1);
             try
             {
                 int versionInt = Integer.parseInt(version);
                 versionInt++;
                 return prefix + versionSeparatorChar + versionInt;
             }
             catch(final NumberFormatException e)
             {
                 return oldVersion.concat("1");
             }
         }
         return oldVersion.concat("1");
     }
     
     private List<InferredOWLOntologyID> listArtifacts(final boolean published, final boolean unpublished)
         throws OpenRDFException
     {
         if(!published && !unpublished)
         {
             throw new IllegalArgumentException("Cannot choose to exclude both published and unpublished artifacts");
         }
         
         final List<InferredOWLOntologyID> results = new ArrayList<InferredOWLOntologyID>();
         
         RepositoryConnection conn = null;
         
         try
         {
             conn = this.getRepositoryManager().getRepository().getConnection();
             final Collection<InferredOWLOntologyID> ontologies =
                     this.getSesameManager().getOntologies(true, conn,
                             this.getRepositoryManager().getArtifactManagementGraph());
             
             for(final InferredOWLOntologyID nextOntology : ontologies)
             {
                 final boolean isPublished =
                         this.getSesameManager().isPublished(nextOntology, conn,
                                 this.getRepositoryManager().getArtifactManagementGraph());
                 
                 if(isPublished)
                 {
                     if(published)
                     {
                         results.add(nextOntology);
                     }
                 }
                 else if(unpublished)
                 {
                     results.add(nextOntology);
                 }
             }
         }
         finally
         {
             if(conn != null && conn.isOpen())
             {
                 conn.close();
             }
         }
         return results;
     }
     
     @Override
     public List<InferredOWLOntologyID> listPublishedArtifacts() throws OpenRDFException
     {
         return this.listArtifacts(true, false);
     }
     
     @Override
     public List<InferredOWLOntologyID> listUnpublishedArtifacts() throws OpenRDFException
     {
         return this.listArtifacts(false, true);
     }
     
     /*
      * (non-Javadoc)
      * 
      * @see com.github.podd.api.PoddArtifactManager#loadArtifact(java.io.InputStream,
      * org.openrdf.rio.RDFFormat)
      */
     @Override
     public InferredOWLOntologyID loadArtifact(final InputStream inputStream, RDFFormat format) throws OpenRDFException,
         PoddException, IOException, OWLException
     {
         if(inputStream == null)
         {
             throw new NullPointerException("Input stream must not be null");
         }
         
         if(format == null)
         {
             format = RDFFormat.RDFXML;
         }
         
         // connection to the temporary repository that the artifact RDF triples will be stored while
         // they are initially parsed by OWLAPI.
         final Repository tempRepository = this.repositoryManager.getNewTemporaryRepository();
         RepositoryConnection temporaryRepositoryConnection = null;
         
         RepositoryConnection permanentRepositoryConnection = null;
         InferredOWLOntologyID inferredOWLOntologyID = null;
         try
         {
             temporaryRepositoryConnection = tempRepository.getConnection();
             final URI randomContext =
                     ValueFactoryImpl.getInstance().createURI("urn:uuid:" + UUID.randomUUID().toString());
             
             // Load the artifact RDF triples into a random context in the temp repository, which may
             // be shared between different uploads
             temporaryRepositoryConnection.add(inputStream, "", format, randomContext);
             
             this.handlePurls(temporaryRepositoryConnection, randomContext);
             
             final Repository permanentRepository = this.getRepositoryManager().getRepository();
             permanentRepositoryConnection = permanentRepository.getConnection();
             permanentRepositoryConnection.begin();
             
             // Set a Version IRI for this artifact
             /*
              * Version information need not be available in uploaded artifacts (any existing values
              * are ignored).
              * 
              * For a new artifact, a Version IRI is created based on the Ontology IRI while for a
              * new version of a managed artifact, the most recent version is incremented.
              */
             final IRI ontologyIRI =
                     this.getSesameManager().getOntologyIRI(temporaryRepositoryConnection, randomContext);
             if(ontologyIRI != null)
             {
                 // check for managed version from artifact graph
                 OWLOntologyID currentManagedArtifactID = null;
                
                 try
                 {
                     currentManagedArtifactID =
                             this.getSesameManager().getCurrentArtifactVersion(ontologyIRI,
                                     permanentRepositoryConnection,
                                     this.getRepositoryManager().getArtifactManagementGraph());
                 }
                 catch(final UnmanagedArtifactIRIException e)
                 {
                     // ignore. indicates a new artifact is being uploaded
                    this.log.info("This is an unmanaged artifact IRI {}", ontologyIRI);
                 }
                 
                 IRI newVersionIRI = null;
                 if(currentManagedArtifactID == null || currentManagedArtifactID.getVersionIRI() == null)
                 {
                     newVersionIRI = IRI.create(ontologyIRI.toString() + ":version:1");
                 }
                 else
                 {
                     newVersionIRI =
                             IRI.create(this.incrementVersion(currentManagedArtifactID.getVersionIRI().toString()));
                 }
                 
                 // set version IRI in temporary repository
                 this.log.info("Setting version IRI to <{}>", newVersionIRI);
                 temporaryRepositoryConnection.remove(ontologyIRI.toOpenRDFURI(), PoddRdfConstants.OWL_VERSION_IRI,
                         null, randomContext);
                 temporaryRepositoryConnection.add(ontologyIRI.toOpenRDFURI(), PoddRdfConstants.OWL_VERSION_IRI,
                         newVersionIRI.toOpenRDFURI(), randomContext);
             }
             else
             {
                 throw new EmptyOntologyException(null, "Loaded ontology is empty");
             }
             this.handleDanglingObjects(ontologyIRI, temporaryRepositoryConnection, randomContext,
                     DanglingObjectPolicy.REPORT);
             
             // check and ensure schema ontology imports are for version IRIs
             this.handleSchemaImports(ontologyIRI, permanentRepositoryConnection, temporaryRepositoryConnection,
                     randomContext);
             
             // ensure schema ontologies are cached in memory before loading statements into OWLAPI
             this.handleCacheSchemasInMemory(permanentRepositoryConnection, permanentRepositoryConnection, randomContext);
             
             // TODO: This web service could be used accidentally to insert invalid file references
             inferredOWLOntologyID =
                     this.loadInferStoreArtifact(temporaryRepositoryConnection, permanentRepositoryConnection,
                             randomContext, FileReferenceVerificationPolicy.DO_NOT_VERIFY);
             
             permanentRepositoryConnection.commit();
             
             return inferredOWLOntologyID;
         }
         catch(final Exception e)
         {
             if(temporaryRepositoryConnection != null && temporaryRepositoryConnection.isActive())
             {
                 temporaryRepositoryConnection.rollback();
             }
             
             if(permanentRepositoryConnection != null && permanentRepositoryConnection.isActive())
             {
                 permanentRepositoryConnection.rollback();
             }
             
             throw e;
         }
         finally
         {
             // release resources
             if(inferredOWLOntologyID != null)
             {
                 this.getOWLManager().removeCache(inferredOWLOntologyID);
             }
             
             if(temporaryRepositoryConnection != null && temporaryRepositoryConnection.isOpen())
             {
                 try
                 {
                     temporaryRepositoryConnection.close();
                 }
                 catch(final RepositoryException e)
                 {
                     this.log.error("Found exception closing repository connection", e);
                 }
             }
             tempRepository.shutDown();
             
             if(permanentRepositoryConnection != null && permanentRepositoryConnection.isOpen())
             {
                 try
                 {
                     permanentRepositoryConnection.close();
                 }
                 catch(final RepositoryException e)
                 {
                     this.log.error("Found exception closing repository connection", e);
                 }
             }
         }
     }
     
     /**
      * Helper method to load the artifact into OWLAPI from a temporary location, perform reasoning
      * and store in permanent repository.
      * 
      * @param fileReferencePolicy
      */
     private InferredOWLOntologyID loadInferStoreArtifact(final RepositoryConnection tempRepositoryConnection,
             final RepositoryConnection permanentRepositoryConnection, final URI tempContext,
             final FileReferenceVerificationPolicy fileReferencePolicy) throws OpenRDFException, OWLException,
         IOException, PoddException, OntologyNotInProfileException, InconsistentOntologyException
     {
         // load into OWLAPI
         this.log.debug("Loading podd artifact from temp repository: {}", tempContext);
         final List<Statement> statements =
                 Iterations.asList(tempRepositoryConnection.getStatements(null, null, null, true, tempContext));
         
         final RioMemoryTripleSource owlSource = new RioMemoryTripleSource(statements.iterator());
         
         owlSource.setNamespaces(tempRepositoryConnection.getNamespaces());
         
         final OWLOntology nextOntology = this.getOWLManager().loadOntology(owlSource);
         
         // Check the OWLAPI OWLOntology against an OWLProfile to make sure it is in profile
         final OWLProfileReport profileReport = this.getOWLManager().getReasonerProfile().checkOntology(nextOntology);
         if(!profileReport.isInProfile())
         {
             this.getOWLManager().removeCache(nextOntology.getOntologyID());
             throw new OntologyNotInProfileException(nextOntology, profileReport,
                     "Ontology is not in required OWL Profile");
         }
         
         // Use the OWLManager to create a reasoner over the ontology
         final OWLReasoner nextReasoner = this.getOWLManager().createReasoner(nextOntology);
         
         // Test that the ontology was consistent with this reasoner
         // This ensures in the case of Pellet that it is in the OWL2-DL profile
         if(!nextReasoner.isConsistent())
         {
             this.getOWLManager().removeCache(nextOntology.getOntologyID());
             throw new InconsistentOntologyException(nextReasoner, "Ontology is inconsistent");
         }
         
         // Copy the statements to permanentRepositoryConnection
         this.getOWLManager().dumpOntologyToRepository(nextOntology, permanentRepositoryConnection,
                 nextOntology.getOntologyID().getVersionIRI().toOpenRDFURI());
         
         // NOTE: At this stage, a client could be notified, and the artifact could be streamed
         // back to them from permanentRepositoryConnection
         
         // Use an OWLAPI InferredAxiomGenerator together with the reasoner to create inferred
         // axioms to store in the database.
         // Serialise the inferred statements back to a different context in the permanent
         // repository connection.
         // The contexts to use within the permanent repository connection are all encapsulated
         // in the InferredOWLOntologyID object.
         final InferredOWLOntologyID inferredOWLOntologyID =
                 this.getOWLManager().inferStatements(nextOntology, permanentRepositoryConnection);
         
         // Check file references after inferencing to accurately identify the parent object
         this.handleFileReferences(permanentRepositoryConnection, fileReferencePolicy, inferredOWLOntologyID
                 .getVersionIRI().toOpenRDFURI(), inferredOWLOntologyID.getInferredOntologyIRI().toOpenRDFURI());
         
         this.getSesameManager().updateManagedPoddArtifactVersion(inferredOWLOntologyID, true,
                 permanentRepositoryConnection, this.getRepositoryManager().getArtifactManagementGraph());
         return inferredOWLOntologyID;
     }
     
     /*
      * (non-Javadoc)
      * 
      * @see com.github.podd.api.PoddArtifactManager#publishArtifact(org.semanticweb.owlapi.model.
      * OWLOntologyID)
      */
     @Override
     public InferredOWLOntologyID publishArtifact(final InferredOWLOntologyID ontologyId) throws OpenRDFException,
         PublishArtifactException, UnmanagedArtifactIRIException
     {
         final IRI ontologyIRI = ontologyId.getOntologyIRI();
         final IRI versionIRI = ontologyId.getVersionIRI();
         
         if(versionIRI == null)
         {
             throw new PublishArtifactException("Could not publish artifact as version was not specified.", ontologyId);
         }
         
         Repository repository = null;
         RepositoryConnection repositoryConnection = null;
         try
         {
             repository = this.getRepositoryManager().getRepository();
             repositoryConnection = repository.getConnection();
             repositoryConnection.begin();
             
             if(this.getSesameManager().isPublished(ontologyId, repositoryConnection,
                     this.getRepositoryManager().getArtifactManagementGraph()))
             {
                 // Cannot publish multiple versions of a single artifact
                 throw new PublishArtifactException("Could not publish artifact as a version was already published",
                         ontologyId);
             }
             
             final InferredOWLOntologyID currentVersion =
                     this.getSesameManager().getCurrentArtifactVersion(ontologyIRI, repositoryConnection,
                             this.getRepositoryManager().getArtifactManagementGraph());
             
             if(!currentVersion.getVersionIRI().equals(versionIRI))
             {
                 // User must make the given artifact version the current version manually before
                 // publishing, to ensure that work from the current version is not lost accidentally
                 throw new PublishArtifactException(
                         "Could not publish artifact as it was not the most current version.", ontologyId);
             }
             
             this.getSesameManager().setPublished(currentVersion, repositoryConnection,
                     this.getRepositoryManager().getArtifactManagementGraph());
             
             final InferredOWLOntologyID published =
                     this.getSesameManager().getCurrentArtifactVersion(ontologyIRI, repositoryConnection,
                             this.getRepositoryManager().getArtifactManagementGraph());
             
             repositoryConnection.commit();
             
             return published;
         }
         catch(final OpenRDFException | PublishArtifactException | UnmanagedArtifactIRIException e)
         {
             if(repositoryConnection != null && repositoryConnection.isActive())
             {
                 repositoryConnection.rollback();
             }
             
             throw e;
         }
         finally
         {
             // release resources
             if(repositoryConnection != null && repositoryConnection.isOpen())
             {
                 try
                 {
                     repositoryConnection.close();
                 }
                 catch(final RepositoryException e)
                 {
                     this.log.error("Found exception closing repository connection", e);
                 }
             }
         }
     }
     
     /*
      * (non-Javadoc)
      * 
      * @see
      * com.github.podd.api.PoddArtifactManager#setFileReferenceManager(com.github.podd.api.file.
      * PoddFileReferenceManager)
      */
     @Override
     public void setFileReferenceManager(final FileReferenceManager fileManager)
     {
         this.fileReferenceManager = fileManager;
     }
     
     /*
      * (non-Javadoc)
      * 
      * @see
      * com.github.podd.api.PoddArtifactManager#setFileRepositoryManager(com.github.podd.api.file
      * .PoddFileRepositoryManager)
      */
     @Override
     public void setFileRepositoryManager(final PoddFileRepositoryManager fileRepositoryManager)
     {
         this.fileRepositoryManager = fileRepositoryManager;
     }
     
     /*
      * (non-Javadoc)
      * 
      * @see
      * com.github.podd.api.PoddArtifactManager#setOwlManager(com.github.podd.api.PoddOWLManager)
      */
     @Override
     public void setOwlManager(final PoddOWLManager owlManager)
     {
         this.owlManager = owlManager;
     }
     
     /*
      * (non-Javadoc)
      * 
      * @see
      * com.github.podd.api.PoddArtifactManager#setPurlManager(com.github.podd.api.purl.PoddPurlManager
      * )
      */
     @Override
     public void setPurlManager(final PoddPurlManager purlManager)
     {
         this.purlManager = purlManager;
     }
     
     @Override
     public void setRepositoryManager(final PoddRepositoryManager repositoryManager)
     {
         this.repositoryManager = repositoryManager;
     }
     
     @Override
     public void setSchemaManager(final PoddSchemaManager schemaManager)
     {
         this.schemaManager = schemaManager;
     }
     
     @Override
     public void setSesameManager(final PoddSesameManager sesameManager)
     {
         this.sesameManager = sesameManager;
     }
     
     /*
      * (non-Javadoc)
      * 
      * @see com.github.podd.api.PoddArtifactManager#updateArtifact(org.openrdf.model.URI,
      * java.io.InputStream, org.openrdf.rio.RDFFormat)
      * 
      * This implementation performs most of the steps carried out during loadArtifact(). Therefore,
      * code and comments are duplicated across these two methods.
      */
     @Override
     public InferredOWLOntologyID updateArtifact(final URI artifactUri, final URI versionUri,
             final Collection<URI> objectUris, final InputStream inputStream, RDFFormat format,
             final UpdatePolicy updatePolicy, final DanglingObjectPolicy danglingObjectAction,
             final FileReferenceVerificationPolicy fileReferenceAction) throws OpenRDFException, IOException,
         OWLException, PoddException
     {
         if(inputStream == null)
         {
             throw new NullPointerException("Input stream must not be null");
         }
         
         if(format == null)
         {
             format = RDFFormat.RDFXML;
         }
         
         // check if updating from the most current version of the artifact
         InferredOWLOntologyID artifactID = null;
         try
         {
             artifactID = this.getArtifactByIRI(IRI.create(versionUri));
         }
         catch(final UnmanagedArtifactIRIException e)
         {
             // if the version IRI is not the most current, it is unmanaged
             final InferredOWLOntologyID currentArtifactID = this.getArtifactByIRI(IRI.create(artifactUri));
             this.log.error(
                     "Attempting to update from an older version of an artifact. <{}> has been succeeded by <{}>",
                     versionUri, currentArtifactID.getVersionIRI().toString());
             throw e;
             // FIXME - handle this conflict intelligently instead of rejecting the update.
         }
         
         final Repository tempRepository = this.getRepositoryManager().getNewTemporaryRepository();
         RepositoryConnection tempRepositoryConnection = null;
         RepositoryConnection permanentRepositoryConnection = null;
         InferredOWLOntologyID inferredOWLOntologyID = null;
         
         try
         {
             // create a temporary in-memory repository
             tempRepositoryConnection = tempRepository.getConnection();
             tempRepositoryConnection.begin();
             
             permanentRepositoryConnection = this.getRepositoryManager().getRepository().getConnection();
             permanentRepositoryConnection.begin();
             
             // load and copy the artifact's concrete statements to the temporary store
             final RepositoryResult<Statement> repoResult =
                     permanentRepositoryConnection.getStatements(null, null, null, false, artifactID.getVersionIRI()
                             .toOpenRDFURI());
             final URI tempContext = artifactID.getVersionIRI().toOpenRDFURI();
             tempRepositoryConnection.add(repoResult, tempContext);
             
             // update the artifact statements
             if(UpdatePolicy.REPLACE_EXISTING.equals(updatePolicy))
             {
                 // create an intermediate context and add "edit" statements to it
                 final URI intContext = PoddRdfConstants.VF.createURI("urn:intermediate:", UUID.randomUUID().toString());
                 
                 tempRepositoryConnection.add(inputStream, "", format, intContext);
                 
                 Collection<URI> replaceableObjects = new ArrayList<URI>(objectUris);
                 
                 // If they did not send a list, we create one ourselves.
                 if(replaceableObjects.isEmpty())
                 {
                     // get all Subjects in "edit" statements
                     final RepositoryResult<Statement> statements =
                             tempRepositoryConnection.getStatements(null, null, null, false, intContext);
                     final List<Statement> allEditStatements = Iterations.addAll(statements, new ArrayList<Statement>());
                     
                     // remove all references to these Subjects in "main" context
                     for(final Statement statement : allEditStatements)
                     {
                         if(statement.getSubject() instanceof URI)
                         {
                             replaceableObjects.add((URI)statement.getSubject());
                         }
                         else
                         {
                             // We do not support replacing objects that are not referenced using
                             // URIs, so they must stay for REPLACE_EXISTING
                             // To remove blank node subject statements, replace the entire object
                             // using REPLACE_ALL
                         }
                     }
                 }
                 
                 for(URI nextReplaceableObject : replaceableObjects)
                 {
                     tempRepositoryConnection.remove(nextReplaceableObject, null, null, tempContext);
                 }
                 
                 // copy the "edit" statements from intermediate context into our "main" context
                 tempRepositoryConnection.add(
                         tempRepositoryConnection.getStatements(null, null, null, false, intContext), tempContext);
             }
             else
             {
                 tempRepositoryConnection.add(inputStream, "", format, tempContext);
             }
             
             this.handleDanglingObjects(artifactID.getOntologyIRI(), tempRepositoryConnection, tempContext,
                     danglingObjectAction);
             
             this.handlePurls(tempRepositoryConnection, tempContext);
             
             // this.handleFileReferences(tempRepositoryConnection, tempContext,
             // fileReferenceAction);
             
             // increment the version
             final OWLOntologyID currentManagedArtifactID =
                     this.getSesameManager().getCurrentArtifactVersion(IRI.create(artifactUri),
                             permanentRepositoryConnection, this.getRepositoryManager().getArtifactManagementGraph());
             final IRI newVersionIRI =
                     IRI.create(this.incrementVersion(currentManagedArtifactID.getVersionIRI().toString()));
             
             // set version IRI in temporary repository
             this.log.info("Setting version IRI to <{}>", newVersionIRI);
             tempRepositoryConnection.remove(artifactID.getOntologyIRI().toOpenRDFURI(),
                     PoddRdfConstants.OWL_VERSION_IRI, null, tempContext);
             tempRepositoryConnection.add(artifactID.getOntologyIRI().toOpenRDFURI(), PoddRdfConstants.OWL_VERSION_IRI,
                     newVersionIRI.toOpenRDFURI(), tempContext);
             
             // check and ensure schema ontology imports are for version IRIs
             this.handleSchemaImports(artifactID.getOntologyIRI(), permanentRepositoryConnection,
                     tempRepositoryConnection, tempContext);
             
             // ensure schema ontologies are cached in memory before loading statements into OWLAPI
             this.handleCacheSchemasInMemory(permanentRepositoryConnection, tempRepositoryConnection, tempContext);
             
             inferredOWLOntologyID =
                     this.loadInferStoreArtifact(tempRepositoryConnection, permanentRepositoryConnection, tempContext,
                             fileReferenceAction);
             
             permanentRepositoryConnection.commit();
             tempRepositoryConnection.rollback();
             
             return inferredOWLOntologyID;
         }
         catch(final Exception e)
         {
             if(tempRepositoryConnection != null && tempRepositoryConnection.isActive())
             {
                 tempRepositoryConnection.rollback();
             }
             
             if(permanentRepositoryConnection != null && permanentRepositoryConnection.isActive())
             {
                 permanentRepositoryConnection.rollback();
             }
             
             throw e;
         }
         finally
         {
             // release resources
             if(inferredOWLOntologyID != null)
             {
                 this.getOWLManager().removeCache(inferredOWLOntologyID);
             }
             
             if(tempRepositoryConnection != null && tempRepositoryConnection.isOpen())
             {
                 try
                 {
                     tempRepositoryConnection.close();
                 }
                 catch(final RepositoryException e)
                 {
                     this.log.error("Found exception closing repository connection", e);
                 }
             }
             tempRepository.shutDown();
             
             if(permanentRepositoryConnection != null && permanentRepositoryConnection.isOpen())
             {
                 try
                 {
                     permanentRepositoryConnection.close();
                 }
                 catch(final RepositoryException e)
                 {
                     this.log.error("Found exception closing repository connection", e);
                 }
             }
         }
     }
     
     @Override
     public InferredOWLOntologyID updateSchemaImports(final InferredOWLOntologyID artifactId,
             final Set<OWLOntologyID> oldSchemaOntologyIds, final Set<OWLOntologyID> schemaOntologyId)
     {
         throw new RuntimeException("TODO: Implement updateSchemaImport");
     }
     
 }
