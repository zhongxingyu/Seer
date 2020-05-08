 package pl.psnc.dl.wf4ever.model.RDF;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.InputStream;
 import java.net.URI;
 import java.nio.file.Paths;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 
 import javax.ws.rs.core.UriBuilder;
 
 import org.apache.commons.lang.NotImplementedException;
 import org.apache.log4j.Logger;
 import org.joda.time.DateTime;
 import org.openrdf.rio.RDFFormat;
 
 import pl.psnc.dl.wf4ever.common.db.UserProfile;
 import pl.psnc.dl.wf4ever.connection.DigitalLibraryFactory;
 import pl.psnc.dl.wf4ever.dl.AccessDeniedException;
 import pl.psnc.dl.wf4ever.dl.DigitalLibraryException;
 import pl.psnc.dl.wf4ever.dl.NotFoundException;
 import pl.psnc.dl.wf4ever.dl.ResourceMetadata;
 import pl.psnc.dl.wf4ever.dl.UserMetadata;
 import pl.psnc.dl.wf4ever.exceptions.IncorrectModelException;
 import pl.psnc.dl.wf4ever.model.Builder;
 import pl.psnc.dl.wf4ever.sms.RO_RDFXMLWriter;
 import pl.psnc.dl.wf4ever.sms.RO_TurtleWriter;
 import pl.psnc.dl.wf4ever.sms.ResearchObjectRelativeWriter;
 import pl.psnc.dl.wf4ever.sms.SemanticMetadataServiceTdb;
 import pl.psnc.dl.wf4ever.vocabulary.AO;
 import pl.psnc.dl.wf4ever.vocabulary.FOAF;
 import pl.psnc.dl.wf4ever.vocabulary.ORE;
 
 import com.hp.hpl.jena.ontology.Individual;
 import com.hp.hpl.jena.ontology.OntModel;
 import com.hp.hpl.jena.ontology.OntModelSpec;
 import com.hp.hpl.jena.query.Dataset;
 import com.hp.hpl.jena.query.Query;
 import com.hp.hpl.jena.query.QueryExecution;
 import com.hp.hpl.jena.query.QueryExecutionFactory;
 import com.hp.hpl.jena.query.QueryFactory;
 import com.hp.hpl.jena.query.ReadWrite;
 import com.hp.hpl.jena.rdf.model.Model;
 import com.hp.hpl.jena.rdf.model.ModelFactory;
 import com.hp.hpl.jena.rdf.model.RDFNode;
 import com.hp.hpl.jena.rdf.model.Resource;
 import com.hp.hpl.jena.rdf.model.Statement;
 import com.hp.hpl.jena.tdb.TDB;
 import com.hp.hpl.jena.tdb.TDBFactory;
 import com.hp.hpl.jena.vocabulary.DCTerms;
 
 import de.fuberlin.wiwiss.ng4j.NamedGraphSet;
 import de.fuberlin.wiwiss.ng4j.impl.NamedGraphSetImpl;
 
 /**
  * The root class for the model.
  * 
  * @author piotrekhol
  * 
  */
 public class Thing {
 
     /** resource URI. */
     protected URI uri;
 
     /** creator URI. */
     protected UserMetadata creator;
 
     /** creation date. */
     protected DateTime created;
 
     /** all contributors. */
     protected Set<URI> contributors = new HashSet<>();
 
     /** last modification date. */
     protected DateTime modified;
 
     /** Use tdb transactions. */
     protected boolean useTransactions;
 
     /** Jena dataset. */
     protected Dataset dataset;
 
     /** Logger. */
     private static final Logger LOGGER = Logger.getLogger(SemanticMetadataServiceTdb.class);
 
     /** Jena model of the named graph. */
     protected OntModel model;
 
     /** User creating the instance. */
     protected UserMetadata user;
 
     /** builder creating the instance, which may be reused for loading other resources. */
     protected Builder builder;
 
     /** Snapshotting/archival date (if this resource has been shapshotted/archived). */
     protected DateTime copyDateTime;
 
     /** User snapshotting/archiving this resource (if this resource has been shapshotted/archived). */
     protected UserMetadata copyAuthor;
 
     /** The original resource (if this resource has been shapshotted/archived). */
     protected Thing copyOf;
 
 
     /**
      * Constructor that allows to specify a custom dataset.
      * 
      * @param user
      *            user creating the instance
      * @param dataset
      *            custom dataset
      * @param useTransactions
      *            should transactions be used. Note that not using transactions on a dataset which already uses
      *            transactions may make it unreadable.
      * @param uri
      *            resource URI
      */
     public Thing(UserMetadata user, Dataset dataset, Boolean useTransactions, URI uri) {
         this.user = user;
         this.dataset = dataset;
         this.useTransactions = useTransactions;
         this.uri = uri;
     }
 
 
     /**
      * Constructor that uses a default dataset on a local drive using transactions.
      * 
      * @param user
      *            user creating the instance
      * @param uri
      *            resource URI
      */
     public Thing(UserMetadata user, URI uri) {
         this(user, null, true, uri);
     }
 
 
     /**
      * Constructor that uses a default dataset on a local drive using transactions.
      * 
      * @param user
      *            user creating the instance
      */
     public Thing(UserMetadata user) {
         this(user, null, true, null);
     }
 
 
     public URI getUri() {
         return uri;
     }
 
 
     /**
      * Return an RDF format specific URI.
      * 
      * @param format
      *            RDF format
      * @return the URI
      */
     public URI getUri(RDFFormat format) {
         //TODO what in case of the "/" at the end?
         if (uri == null) {
             return null;
         }
         if (format == null) {
             return getUri();
         }
         URI base = uri.resolve(".");
         String name = base.relativize(uri).getPath();
         String specific;
         if (RDFFormat.forFileName(name) != null) {
             specific = name.substring(0, name.lastIndexOf(".")) + "." + format.getDefaultFileExtension();
         } else {
             specific = name + "." + format.getDefaultFileExtension();
         }
         return UriBuilder.fromUri(base).path(specific).queryParam("original", name).build();
     }
 
 
     /**
      * Return a shortest possible name of the Thing (name of group of filename).
      * 
      * @return a filename or URI if there is no path
      */
     public String getName() {
         if (uri.getPath() == null || uri.getPath().isEmpty()) {
             return uri.toString();
         }
         return Paths.get(uri.getPath()).getFileName().toString();
     }
 
 
     public void setUri(URI uri) {
         this.uri = uri;
     }
 
 
     public UserMetadata getCreator() {
         return creator;
     }
 
 
     public void setCreator(UserMetadata creator) {
         this.creator = creator;
     }
 
 
     public DateTime getCreated() {
         return created;
     }
 
 
     public void setCreated(DateTime created) {
         this.created = created;
     }
 
 
     public DateTime getModified() {
         return modified;
     }
 
 
     public void setModified(DateTime modified) {
         this.modified = modified;
     }
 
 
     /**
      * Not implemented yet.
      * 
      * @return throw an exception.
      */
     public Set<URI> getContributors() {
         throw new NotImplementedException();
         //return contributors;
     }
 
 
     /**
      * Check if the dataset contains a named graph for this resource.
      * 
      * @return true if the named graph exists, false otherwise
      */
     public boolean isNamedGraph() {
         boolean transactionStarted = beginTransaction(ReadWrite.READ);
         try {
             return dataset.containsNamedModel(uri.toString());
         } finally {
             endTransaction(transactionStarted);
         }
     }
 
 
     public Builder getBuilder() {
         return builder;
     }
 
 
     public void setBuilder(Builder builder) {
         this.builder = builder;
     }
 
 
     /**
      * Take out an RDF graph from the triplestore and serialize it in storage (e.g. dLibra) with relative URI
      * references.
      * 
      * @param base
      *            the object whose URI is the base
      * @param format
      *            in which the resource should be saved
      * @return resource serialization metadata
      * @throws NotFoundException
      *             could not find the resource in DL
      * @throws DigitalLibraryException
      *             could not connect to the DL
      * @throws AccessDeniedException
      *             access denied when updating data in DL
      */
     public ResourceMetadata serialize(URI base, RDFFormat format)
             throws DigitalLibraryException, NotFoundException, AccessDeniedException {
         String filePath = base.relativize(uri).getPath();
         InputStream dataStream = getGraphAsInputStreamWithRelativeURIs(base, format);
         return DigitalLibraryFactory.getDigitalLibrary().createOrUpdateFile(base, filePath, dataStream,
             format.getDefaultMIMEType());
     }
 
 
     /**
      * Return this resource as a named graph in a selected RDF format.
      * 
      * @param syntax
      *            RDF format
      * @return an input stream or null of no model is found
      */
     public InputStream getGraphAsInputStream(RDFFormat syntax) {
         boolean transactionStarted = beginTransaction(ReadWrite.READ);
         try {
             if (!dataset.containsNamedModel(uri.toString())) {
                 return null;
             }
             ByteArrayOutputStream out = new ByteArrayOutputStream();
             if (syntax.supportsContexts()) {
                 Dataset tmpDataset = TDBFactory.createDataset();
                 addNamedModelsRecursively(tmpDataset);
 
                 NamedGraphSet ngs = new NamedGraphSetImpl();
                 Iterator<String> it = tmpDataset.listNames();
                 while (it.hasNext()) {
                     String graphUri = it.next();
                     Model ng4jModel = ModelFactory.createModelForGraph(ngs.createGraph(graphUri));
                     Model tdbModel = tmpDataset.getNamedModel(graphUri);
                     List<Statement> statements = tdbModel.listStatements().toList();
                     ng4jModel.add(statements);
                 }
                 ngs.write(out, syntax.getName().toUpperCase(), null);
             } else {
                 dataset.getNamedModel(uri.toString()).write(out, syntax.getName().toUpperCase());
             }
             return new ByteArrayInputStream(out.toByteArray());
         } finally {
             endTransaction(transactionStarted);
         }
     }
 
 
     /**
      * Return this resource as a named graph in a selected RDF format. Only triples describing the resources given as
      * parameters will be returned.
      * 
      * @param syntax
      *            RDF format
      * @param resources
      *            resources which will be included
      * @return an input stream or null of no model is found
      */
     public InputStream getGraphAsInputStream(RDFFormat syntax, Thing... resources) {
         boolean transactionStarted = beginTransaction(ReadWrite.READ);
         try {
             Model result = ModelFactory.createDefaultModel();
             for (Thing resource : resources) {
                 String queryString = String.format("DESCRIBE <%s>", resource.getUri().toString());
                 Query query = QueryFactory.create(queryString);
 
                 QueryExecution qexec = QueryExecutionFactory.create(query, model);
                 result.add(qexec.execDescribe());
                 qexec.close();
             }
             if (result.isEmpty()) {
                 return null;
             }
 
             ByteArrayOutputStream out = new ByteArrayOutputStream();
             result.removeNsPrefix("xml");
             result.write(out, syntax.getName().toUpperCase());
             return new ByteArrayInputStream(out.toByteArray());
         } finally {
             endTransaction(transactionStarted);
         }
     }
 
 
     /**
      * Return this resource as a named graph in a selected RDF format, with all URIs relativized against this resource's
      * URI. Only RDF/XML and Turtle formats are supported.
      * 
      * @param filterUri
      *            the URI used to determine which URIs will be relativized. Only URIs with the same host and paths
      *            having the filter path as suffix will be relativized.
      * @param syntax
      *            RDF/XML or Turtle format
      * @return an input stream or null of no model is found
      */
     public InputStream getGraphAsInputStreamWithRelativeURIs(URI filterUri, RDFFormat syntax) {
         boolean transactionStarted = beginTransaction(ReadWrite.READ);
         try {
             ResearchObjectRelativeWriter writer;
             if (syntax != RDFFormat.RDFXML && syntax != RDFFormat.TURTLE) {
                 throw new IllegalArgumentException("Format " + syntax + " is not supported");
             } else if (syntax == RDFFormat.RDFXML) {
                 writer = new RO_RDFXMLWriter();
             } else {
                 writer = new RO_TurtleWriter();
             }
             if (model == null) {
                 return null;
             }
             writer.setResearchObjectURI(filterUri);
             writer.setBaseURI(uri);
 
             ByteArrayOutputStream out = new ByteArrayOutputStream();
             writer.write(model, out, null);
             return new ByteArrayInputStream(out.toByteArray());
         } finally {
             endTransaction(transactionStarted);
         }
     }
 
 
     /**
      * Find the dcterms:creator of a resource in the model.
      * 
      * @param thing
      *            the resource
      * @return creator URI or null if not defined
      */
     public UserMetadata extractCreator(Thing thing) {
         boolean transactionStarted = beginTransaction(ReadWrite.READ);
         try {
             if (model == null) {
                 return null;
             }
             Individual ro = model.getIndividual(thing.getUri().toString());
             if (ro == null) {
                 throw new IncorrectModelException("RO not found in the manifest" + thing.getUri());
             }
             com.hp.hpl.jena.rdf.model.Resource c = ro.getPropertyResourceValue(DCTerms.creator);
             URI curi = c != null ? URI.create(c.getURI()) : null;
             RDFNode n = c.getProperty(FOAF.name).getObject();
             String name = n != null ? n.asLiteral().getString() : null;
             return new UserProfile(null, name, null, curi);
         } finally {
             endTransaction(transactionStarted);
         }
     }
 
 
     /**
      * Find the dcterms:created date of a resource in the model.
      * 
      * @param thing
      *            the resource
      * @return creation date or null if not defined
      */
     public DateTime extractCreated(Thing thing) {
         boolean transactionStarted = beginTransaction(ReadWrite.READ);
         try {
             if (model == null) {
                 return null;
             }
             Individual ro = model.getIndividual(thing.getUri().toString());
             if (ro == null) {
                 throw new IncorrectModelException("RO not found in the manifest" + thing.getUri());
             }
             RDFNode d = ro.getPropertyValue(DCTerms.created);
             if (d == null || !d.isLiteral()) {
                 return null;
             }
             return DateTime.parse(d.asLiteral().getString());
         } finally {
             endTransaction(transactionStarted);
         }
     }
 
 
     @Override
     public String toString() {
         return getUri().toString();
     }
 
 
     /**
      * Start a TDB transaction provided that the flag useTransactions is set, the dataset supports transactions and
      * there is no open transaction. According to TDB, many read or one write transactions are allowed.
      * 
      * @param mode
      *            read or write
      * @return true if a new transaction has been started, false otherwise
      */
     protected boolean beginTransaction(ReadWrite mode) {
         boolean started = false;
         if (useTransactions && dataset.supportsTransactions() && !dataset.isInTransaction()) {
             dataset.begin(mode);
             started = true;
         }
         if (mode == ReadWrite.WRITE || dataset.containsNamedModel(uri.toString())) {
             model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM, dataset.getNamedModel(uri.toString()));
         }
         return started;
     }
 
 
     /**
      * Commit the transaction provided that the flag useTransactions is set, the dataset supports transactions and the
      * parameter is true.
      * 
      * @param wasStarted
      *            a convenience parameter to specify if the transaction should be committed
      */
     protected void commitTransaction(boolean wasStarted) {
         if (useTransactions && dataset.supportsTransactions() && wasStarted) {
             dataset.commit();
         }
     }
 
 
     /**
      * End the transaction provided that the flag useTransactions is set, the dataset supports transactions and the
      * parameter is true.
      * 
      * @param wasStarted
      *            a convenience parameter to specify if the transaction should be ended
      */
     protected void endTransaction(boolean wasStarted) {
         if (useTransactions && dataset.supportsTransactions() && wasStarted) {
             if (model != null) {
                 TDB.sync(model);
                 model = null;
             }
             dataset.end();
         }
     }
 
 
     /**
      * Abort the transaction provided that the flag useTransactions is set, the dataset supports transactions and the
      * parameter is true.
      * 
      * @param wasStarted
      *            a convenience parameter to specify if the transaction should be aborted
      */
     protected void abortTransaction(boolean wasStarted) {
         if (useTransactions && dataset.supportsTransactions() && wasStarted) {
             dataset.abort();
         }
     }
 
 
     // *****Private***** 
 
     /**
      * Add this model to a dataset and call this method recursively for all dependent models, unless they have already
      * been added to the dataset.
      * 
      * @param tmpDataset
      *            the dataset to which to add the model
      */
     private void addNamedModelsRecursively(Dataset tmpDataset) {
         boolean transactionStarted = beginTransaction(ReadWrite.READ);
         try {
             if (model == null) {
                 LOGGER.warn("Could not find model for URI " + uri);
                 return;
             }
             tmpDataset.addNamedModel(uri.toString(), model);
             List<RDFNode> it = model.listObjectsOfProperty(AO.body).toList();
             it.addAll(model.listObjectsOfProperty(ORE.isDescribedBy).toList());
             for (RDFNode namedModelRef : it) {
                 URI childURI = URI.create(namedModelRef.asResource().getURI());
                 if (dataset.containsNamedModel(childURI.toString())
                         && !tmpDataset.containsNamedModel(childURI.toString())) {
                     Thing relatedModel = builder.buildThing(childURI);
                     relatedModel.addNamedModelsRecursively(tmpDataset);
                 }
             }
         } finally {
             endTransaction(transactionStarted);
         }
     }
 
 
     /**
      * Replace all occurrences of a blank node with a URI resource in this resource's model.
      * 
      * @param nodeUri
      *            the URI to use instead of the blank node id
      * @param node
      *            the blank node
      * @return the URI resource
      */
     protected Resource changeBlankNodeToUriResources(URI nodeUri, RDFNode node) {
         Resource r = model.createResource(nodeUri.toString());
         List<Statement> statements = new ArrayList<Statement>();
         for (Statement s : model.listStatements(node.asResource(), null, (RDFNode) null).toList()) {
             Statement s2 = model.createStatement(r, s.getPredicate(), s.getObject());
             statements.add(s);
             model.add(s2);
         }
 
         for (Statement s : model.listStatements(null, null, node).toList()) {
             Statement s2 = model.createStatement(s.getSubject(), s.getPredicate(), r);
             model.add(s2);
             statements.add(s);
         }
         model.remove(statements);
         return r;
     }
 
 
     /**
      * Save the resource to the triplestore and data storage backend.
      */
     public void save() {
     }
 
 
     /**
      * Delete the named graph if exists.
      */
     public void delete() {
         boolean transactionStarted = beginTransaction(ReadWrite.WRITE);
         try {
             if (model != null) {
                 dataset.removeNamedModel(uri.toString());
                 model = null;
             }
             commitTransaction(transactionStarted);
         } finally {
             endTransaction(transactionStarted);
         }
     }
 
 
     /**
      * Delete a resource from the model.
      * 
      * @param resource
      *            resource to delete
      */
     public void deleteResource(Thing resource) {
         boolean transactionStarted = beginTransaction(ReadWrite.WRITE);
         try {
             Resource resR = model.getResource(resource.getUri().toString());
             if (resR != null) {
                 model.removeAll(resR, null, null);
                 model.removeAll(null, null, resR);
             }
             commitTransaction(transactionStarted);
         } finally {
             endTransaction(transactionStarted);
         }
     }
 
 
     /**
      * Save the dcterms:creator and dcterms:created in the current model.
      * 
      * @param subject
      *            the resource being described
      */
     public void saveAuthor(Thing subject) {
         boolean transactionStarted = beginTransaction(ReadWrite.WRITE);
         try {
             com.hp.hpl.jena.rdf.model.Resource subjectR = model.getResource(subject.getUri().toString());
             if (!subjectR.hasProperty(DCTerms.created) && subject.getCreated() != null) {
                 model.add(subjectR, DCTerms.created, model.createTypedLiteral(subject.getCreated().toCalendar(null)));
             }
             if (!subjectR.hasProperty(DCTerms.creator) && subject.getCreator() != null) {
                 Individual author = model.createIndividual(subject.getCreator().getUri().toString(), FOAF.Agent);
                 model.add(subjectR, DCTerms.creator, author);
                 author.setPropertyValue(FOAF.name, model.createLiteral(subject.getCreator().getName()));
             }
             commitTransaction(transactionStarted);
         } finally {
             endTransaction(transactionStarted);
         }
     }
 
 
     /**
      * Save the dcterms:contributor and dcterms:modified in the current model.
      * 
      * @param subject
      *            the resource being described
      */
     public void saveContributors(Thing subject) {
         boolean transactionStarted = beginTransaction(ReadWrite.WRITE);
         try {
             com.hp.hpl.jena.rdf.model.Resource subjectR = model.getResource(subject.getUri().toString());
             model.removeAll(subjectR, DCTerms.modified, null);
             if (subject.getModified() != null) {
                 model.add(subjectR, DCTerms.modified, model.createTypedLiteral(subject.getModified().toCalendar(null)));
             }
             for (URI contributor : subject.getContributors()) {
                 model.add(subjectR, DCTerms.contributor, model.createResource(contributor.toString()));
             }
             commitTransaction(transactionStarted);
         } finally {
             endTransaction(transactionStarted);
         }
     }
 
 
     /**
      * Is the resource writable only by RODL (the manifest or the evolution annotation body).
      * 
      * @return true if object represents a special object, false otherwise.
      */
     public Boolean isSpecialResource() {
         if (uri.toString().matches("(.+)?manifest\\.rdf(\\/)?") || uri.toString().matches("(.+)?evo_info\\.ttl(\\/)?")) {
             return true;
         }
         return false;
     }
 
 
     public DateTime getCopyDateTime() {
         return copyDateTime;
     }
 
 
     public void setCopyDateTime(DateTime copyDateTime) {
         this.copyDateTime = copyDateTime;
     }
 
 
     public UserMetadata getCopyAuthor() {
         return copyAuthor;
     }
 
 
     public void setCopyAuthor(UserMetadata copyAuthor) {
         this.copyAuthor = copyAuthor;
     }
 
 
     public Thing getCopyOf() {
         return copyOf;
     }
 
 
     public void setCopyOf(Thing copyOf) {
         this.copyOf = copyOf;
     }
 
 
     @Override
     public int hashCode() {
         final int prime = 31;
         int result = 1;
         result = prime * result + ((uri == null) ? 0 : uri.hashCode());
         return result;
     }
 
 
     @Override
     public boolean equals(Object obj) {
         if (this == obj) {
             return true;
         }
         if (obj == null) {
             return false;
         }
         if (getClass() != obj.getClass()) {
             return false;
         }
         Thing other = (Thing) obj;
         if (uri == null) {
             if (other.uri != null) {
                 return false;
             }
         } else if (!uri.equals(other.uri)) {
             return false;
         }
         return true;
     }
 
 }
