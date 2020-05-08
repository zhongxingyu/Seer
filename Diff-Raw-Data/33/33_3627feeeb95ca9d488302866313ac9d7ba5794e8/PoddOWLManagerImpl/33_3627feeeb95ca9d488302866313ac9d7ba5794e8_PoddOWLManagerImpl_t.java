 /**
  * PODD is an OWL ontology database used for scientific project management
  * 
  * Copyright (C) 2009-2013 The University Of Queensland
  * 
  * This program is free software: you can redistribute it and/or modify it under the terms of the
  * GNU Affero General Public License as published by the Free Software Foundation, either version 3
  * of the License, or (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
  * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Affero General Public License for more details.
  * 
  * You should have received a copy of the GNU Affero General Public License along with this program.
  * If not, see <http://www.gnu.org/licenses/>.
  */
 package com.github.podd.impl;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Objects;
 import java.util.Set;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ConcurrentMap;
 
 import org.mindswap.pellet.exceptions.PelletRuntimeException;
 import org.openrdf.OpenRDFException;
 import org.openrdf.OpenRDFUtil;
 import org.openrdf.model.Model;
 import org.openrdf.model.Resource;
 import org.openrdf.model.URI;
 import org.openrdf.model.impl.LinkedHashModel;
 import org.openrdf.model.util.Namespaces;
 import org.openrdf.model.vocabulary.OWL;
 import org.openrdf.query.BindingSet;
 import org.openrdf.query.QueryLanguage;
 import org.openrdf.query.TupleQuery;
 import org.openrdf.query.TupleQueryResult;
 import org.openrdf.query.impl.DatasetImpl;
 import org.openrdf.repository.RepositoryConnection;
 import org.openrdf.repository.RepositoryException;
 import org.openrdf.repository.util.RDFInserter;
 import org.openrdf.rio.RDFFormat;
 import org.openrdf.rio.helpers.StatementCollector;
 import org.semanticweb.owlapi.formats.OWLOntologyFormatFactoryRegistry;
 import org.semanticweb.owlapi.formats.RioRDFOntologyFormatFactory;
 import org.semanticweb.owlapi.io.OWLOntologyDocumentSource;
 import org.semanticweb.owlapi.io.OWLParser;
 import org.semanticweb.owlapi.io.OWLParserException;
 import org.semanticweb.owlapi.io.OWLParserFactoryRegistry;
 import org.semanticweb.owlapi.model.AddImport;
 import org.semanticweb.owlapi.model.IRI;
 import org.semanticweb.owlapi.model.OWLAxiom;
 import org.semanticweb.owlapi.model.OWLException;
 import org.semanticweb.owlapi.model.OWLOntology;
 import org.semanticweb.owlapi.model.OWLOntologyChangeException;
 import org.semanticweb.owlapi.model.OWLOntologyCreationException;
 import org.semanticweb.owlapi.model.OWLOntologyID;
 import org.semanticweb.owlapi.model.OWLOntologyManager;
 import org.semanticweb.owlapi.model.OWLOntologyManagerFactory;
 import org.semanticweb.owlapi.model.OWLRuntimeException;
 import org.semanticweb.owlapi.profiles.OWLProfile;
 import org.semanticweb.owlapi.profiles.OWLProfileRegistry;
 import org.semanticweb.owlapi.profiles.OWLProfileReport;
 import org.semanticweb.owlapi.profiles.OWLProfileViolation;
 import org.semanticweb.owlapi.reasoner.InferenceType;
 import org.semanticweb.owlapi.reasoner.OWLReasoner;
 import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
 import org.semanticweb.owlapi.reasoner.OWLReasonerFactoryRegistry;
 import org.semanticweb.owlapi.reasoner.ReasonerInterruptedException;
 import org.semanticweb.owlapi.reasoner.TimeOutException;
 import org.semanticweb.owlapi.rio.RioMemoryTripleSource;
 import org.semanticweb.owlapi.rio.RioParserImpl;
 import org.semanticweb.owlapi.rio.RioRenderer;
 import org.semanticweb.owlapi.util.InferredAxiomGenerator;
 import org.semanticweb.owlapi.util.InferredClassAssertionAxiomGenerator;
 import org.semanticweb.owlapi.util.InferredDataPropertyCharacteristicAxiomGenerator;
 import org.semanticweb.owlapi.util.InferredEquivalentClassAxiomGenerator;
 import org.semanticweb.owlapi.util.InferredEquivalentDataPropertiesAxiomGenerator;
 import org.semanticweb.owlapi.util.InferredEquivalentObjectPropertyAxiomGenerator;
 import org.semanticweb.owlapi.util.InferredInverseObjectPropertiesAxiomGenerator;
 import org.semanticweb.owlapi.util.InferredObjectPropertyCharacteristicAxiomGenerator;
 import org.semanticweb.owlapi.util.InferredOntologyGenerator;
 import org.semanticweb.owlapi.util.InferredSubClassAxiomGenerator;
 import org.semanticweb.owlapi.util.InferredSubDataPropertyAxiomGenerator;
 import org.semanticweb.owlapi.util.InferredSubObjectPropertyAxiomGenerator;
 import org.semanticweb.owlapi.util.NullProgressMonitor;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.manchester.cs.owl.owlapi.OWLImportsDeclarationImpl;
 
 import com.clarkparsia.owlapi.explanation.PelletExplanation;
 import com.clarkparsia.owlapi.explanation.io.rdfxml.RDFXMLExplanationRenderer;
 import com.clarkparsia.pellet.owlapiv3.PelletReasoner;
 import com.clarkparsia.pellet.owlapiv3.PelletReasonerFactory;
 import com.github.podd.api.PoddOWLManager;
 import com.github.podd.exception.DataRepositoryException;
 import com.github.podd.exception.EmptyOntologyException;
 import com.github.podd.exception.InconsistentOntologyException;
 import com.github.podd.exception.OntologyNotInProfileException;
 import com.github.podd.exception.PoddException;
 import com.github.podd.utils.DeduplicatingRDFInserter;
 import com.github.podd.utils.InferredOWLOntologyID;
 import com.github.podd.utils.PODD;
 
 /**
  * Implementation of PoddOWLManager interface.
  * 
  * @author kutila
  * 
  */
 public class PoddOWLManagerImpl implements PoddOWLManager
 {
     protected Logger log = LoggerFactory.getLogger(this.getClass());
     
     // private final OWLOntologyManager owlOntologyManager;
     
     private final OWLReasonerFactory reasonerFactory;
     
     private final OWLOntologyManagerFactory managerFactory;
     
     private final ConcurrentMap<Set<? extends OWLOntologyID>, OWLOntologyManager> managerCache =
             new ConcurrentHashMap<>();
     
     public PoddOWLManagerImpl(final OWLOntologyManagerFactory nextManager, final OWLReasonerFactory nextReasonerFactory)
     {
         if(nextManager == null)
         {
             throw new IllegalArgumentException("OWLOntologyManagerFactory was null");
         }
         if(nextReasonerFactory == null)
         {
             throw new IllegalArgumentException("OWLReasonerFactory was null");
         }
         this.managerFactory = nextManager;
         // this.owlOntologyManager = nextManager.buildOWLOntologyManager();
         this.managerCache.put(Collections.<OWLOntologyID> emptySet(), managerFactory.buildOWLOntologyManager());
         this.reasonerFactory = nextReasonerFactory;
     }
     
     private List<InferredOWLOntologyID> buildDirectImportsList(final OWLOntologyID ontologyID,
             final RepositoryConnection conn, final URI context) throws OpenRDFException
     {
         final List<InferredOWLOntologyID> importsList = new ArrayList<InferredOWLOntologyID>();
         
         final String subject = ontologyID.getOntologyIRI().toQuotedString();
         final String sparqlQuery =
                 "SELECT ?x ?xv ?xiv WHERE { " + subject + " <" + OWL.IMPORTS.stringValue() + "> ?xv ." + "?x <"
                         + PODD.OWL_VERSION_IRI + "> ?xv ." + "?x <" + PODD.PODD_BASE_CURRENT_INFERRED_VERSION
                         + "> ?xiv ." + " }";
         this.log.debug("Generated SPARQL {}", sparqlQuery);
         final TupleQuery query = conn.prepareTupleQuery(QueryLanguage.SPARQL, sparqlQuery);
         
         final DatasetImpl dataset = new DatasetImpl();
         dataset.addDefaultGraph(context);
         query.setDataset(dataset);
         
         final TupleQueryResult queryResults = query.evaluate();
         while(queryResults.hasNext())
         {
             final BindingSet nextResult = queryResults.next();
             final String ontologyIRI = nextResult.getValue("x").stringValue();
             final String versionIRI = nextResult.getValue("xv").stringValue();
             final String inferredIRI = nextResult.getValue("xiv").stringValue();
             
             final InferredOWLOntologyID inferredOntologyID =
                     new InferredOWLOntologyID(IRI.create(ontologyIRI), IRI.create(versionIRI), IRI.create(inferredIRI));
             
             if(!importsList.contains(inferredOntologyID))
             {
                 this.log.debug("Adding {} to imports list", ontologyIRI);
                 importsList.add(inferredOntologyID);
             }
         }
         return importsList;
     }
     
     /**
      * TODO: Integrate this with other imports identification code in
      * {@link PoddSchemaManagerImpl#mapAndSortImports(Model, ConcurrentMap, ConcurrentMap, ConcurrentMap, List, URI)}
      * 
      * @param ontologyID
      * @param conn
      * @param context
      * @return
      * @throws OpenRDFException
      */
     private List<InferredOWLOntologyID> buildTwoLevelOrderedImportsList(final OWLOntologyID ontologyID,
             final RepositoryConnection conn, final URI context) throws OpenRDFException
     {
         // -- find ontologies directly imported by this schema ontology
         final List<InferredOWLOntologyID> directImports = this.buildDirectImportsList(ontologyID, conn, context);
         
         // -- find second level imports
         final Set<InferredOWLOntologyID> secondLevelImports = new LinkedHashSet<InferredOWLOntologyID>();
         for(final InferredOWLOntologyID inferredOntologyID : directImports)
         {
             final List<InferredOWLOntologyID> directImportsList =
                     this.buildDirectImportsList(inferredOntologyID, conn, context);
             secondLevelImports.addAll(directImportsList);
             // TODO - support multiple levels by converting into a recursive
             // implementation
         }
         
         for(final InferredOWLOntologyID secondImport : secondLevelImports)
         {
             if(directImports.contains(secondImport))
             {
                 directImports.remove(secondImport);
             }
         }
         
         final Set<InferredOWLOntologyID> orderedResultsList = new LinkedHashSet<InferredOWLOntologyID>();
         // add any indirect imports first
         for(final InferredOWLOntologyID inferredOntologyID : secondLevelImports)
         {
             this.log.debug("adding {} to results", inferredOntologyID);
             orderedResultsList.add(inferredOntologyID);
         }
         for(final InferredOWLOntologyID inferredOntologyID : directImports)
         {
             this.log.debug("adding {} to results", inferredOntologyID);
             orderedResultsList.add(inferredOntologyID);
         }
         return new ArrayList<InferredOWLOntologyID>(orderedResultsList);
     }
     
     private OWLOntologyManager getCachedManager(Set<? extends OWLOntologyID> schemaOntologies)
     {
         return managerCache.get(Collections.emptySet());
     }
     
     @Override
     public void cacheSchemaOntologies(final Set<? extends OWLOntologyID> ontologyIDs, final RepositoryConnection conn,
             final URI schemaManagementContext) throws OpenRDFException, OWLException, IOException, PoddException
     {
         // -- validate input
         if(ontologyIDs == null)
         {
             throw new NullPointerException("OWLOntology collection is incomplete");
         }
         
         // NOTE: if InferredOntologyIRI is null, only the base ontology is
         // cached
         
         for(OWLOntologyID ontologyID : ontologyIDs)
         {
             if(ontologyID == null || ontologyID.getOntologyIRI() == null)
             {
                 throw new NullPointerException("OWLOntology is incomplete");
             }
             
             this.log.info("Checking whether the schema ontology is already cached: {}", ontologyID);
             
             if(isCached(ontologyID))
             {
                 this.log.info("Ontology was already cached: {}", ontologyID);
                 continue;
             }
             
             final IRI baseOntologyIRI = ontologyID.getOntologyIRI();
             final IRI baseOntologyVersionIRI = ontologyID.getVersionIRI();
             // final IRI inferredOntologyIRI = ontologyID.getInferredOntologyIRI();
             
             // Only direct imports and first-level indirect imports are identified.
             // This works for the current PODD schema ontologies which have a maximum import depth
             // of 3
             // (PoddPlant -> PoddScience -> PoddBase)
             // TODO: Fix this using a SPARQL which identifies the complete imports closure and sorts
             // them in the proper order for loading.
             
             // FIXME: The following doesn't seem to work on the initial load for new schema
             // ontologies, as it is identifying the foaf ontology as having no imports, yet it
             // imports the dcterms ontology
             final List<InferredOWLOntologyID> imports =
                     this.buildTwoLevelOrderedImportsList(ontologyID, conn, schemaManagementContext);
             this.log.info("The schema ontology {} has {} imports.", baseOntologyVersionIRI, imports.size());
             
             // -- load the imported ontologies into the Manager's cache. It is expected that they
             // are
             // already in the Repository
             for(final InferredOWLOntologyID inferredOntologyID : imports)
             {
                if(!isCached(inferredOntologyID))
                 {
                    final URI contextToLoadFrom = inferredOntologyID.getVersionIRI().toOpenRDFURI();
                    this.log.info("About to load {} from context {}", inferredOntologyID, contextToLoadFrom);
                    this.parseRDFStatements(conn, contextToLoadFrom);
                    
                    final URI inferredContextToLoadFrom = inferredOntologyID.getInferredOntologyIRI().toOpenRDFURI();
                    if(inferredContextToLoadFrom != null)
                    {
                        this.parseRDFStatements(conn, inferredContextToLoadFrom);
                    }
                 }
             }
             
             this.log.info("About to parse schema ontology into managers cache: {}", ontologyID);
             
             // -- load the requested schema ontology (and inferred statements if they exist) into
             // the
             // Manager's cache
             this.parseRDFStatements(conn, baseOntologyVersionIRI.toOpenRDFURI());
             if(ontologyID instanceof InferredOWLOntologyID
                     && ((InferredOWLOntologyID)ontologyID).getInferredOntologyIRI() != null)
             {
                 this.log.info("About to parse inferred schema ontology into managers cache: {}", ontologyID);
                 
                 this.parseRDFStatements(conn, ((InferredOWLOntologyID)ontologyID).getInferredOntologyIRI()
                         .toOpenRDFURI());
             }
             
             this.log.info("Completed caching for schema ontology: {}", ontologyID);
         }
     }
     
     @Override
     public void cacheSchemaOntology(final OWLOntologyID ontologyID, final RepositoryConnection conn,
             final URI schemaManagementContext) throws OpenRDFException, OWLException, IOException, PoddException
     {
         cacheSchemaOntologies(Collections.singleton(ontologyID), conn, schemaManagementContext);
     }
     
     /**
      * Helper method to verify that the statements of a given {@link Model} make up a consistent
      * OWL-DL Ontology.
      * 
      * <br>
      * 
      * NOTES: Any ontologies imported must be already loaded into the OWLOntologyManager's memory
      * before invoking this method. When this method returns, the ontology built from the input
      * Model is in the OWLOntologyManager's memory.
      * 
      * User MUST synchronize on owlOntologyManager before entering this method if the
      * OWLOntologyManager implementation is not threadsafe.
      * 
      * @param model
      *            A Model which should contain an Ontology
      * @return The loaded Ontology if verification succeeds
      * @throws DataRepositoryException
      *             If verification fails
      */
     private OWLOntology checkForConsistentOwlDlOntology(final Model model) throws EmptyOntologyException,
         OntologyNotInProfileException, InconsistentOntologyException
     {
         final RioRDFOntologyFormatFactory ontologyFormatFactory =
                 (RioRDFOntologyFormatFactory)OWLOntologyFormatFactoryRegistry.getInstance().getByMIMEType(
                         RDFFormat.RDFXML.getDefaultMIMEType());
         final RioParserImpl owlParser = new RioParserImpl(ontologyFormatFactory);
         
         OWLOntology nextOntology = null;
         
         try
         {
             try
             {
                 nextOntology = this.getCachedManager(Collections.<OWLOntologyID> emptySet()).createOntology();
                 final RioMemoryTripleSource owlSource = new RioMemoryTripleSource(model.iterator());
                 
                 owlParser.parse(owlSource, nextOntology);
             }
             catch(OWLOntologyCreationException | OWLParserException | IOException e)
             {
                 // throwing up the original Exceptions is also a possibility
                 // here.
                 throw new EmptyOntologyException(nextOntology, "Error parsing Model to create an Ontology");
             }
             
             // Repository configuration can be an empty ontology
             // if(nextOntology.isEmpty())
             // {
             // throw new EmptyOntologyException(nextOntology,
             // "Ontology was empty");
             // }
             
             // verify that the ontology in OWL-DL profile
             final OWLProfile nextProfile = OWLProfileRegistry.getInstance().getProfile(OWLProfile.OWL2_DL);
             final OWLProfileReport profileReport = nextProfile.checkOntology(nextOntology);
             if(!profileReport.isInProfile())
             {
                 if(this.log.isDebugEnabled())
                 {
                     for(final OWLProfileViolation violation : profileReport.getViolations())
                     {
                         this.log.debug(violation.toString());
                     }
                 }
                 throw new OntologyNotInProfileException(nextOntology, profileReport, "Ontology not in OWL-DL profile");
             }
             
             // check consistency
             final OWLReasonerFactory reasonerFactory =
                     OWLReasonerFactoryRegistry.getInstance().getReasonerFactory("Pellet");
             final OWLReasoner reasoner = reasonerFactory.createReasoner(nextOntology);
             
             if(!reasoner.isConsistent())
             {
                 final PelletExplanation exp = new PelletExplanation((PelletReasoner)reasoner);
                 // Get 100 inconsistency explanations, any more than that and they need to make
                 // modifications and try again
                 final RDFXMLExplanationRenderer renderer = new RDFXMLExplanationRenderer();
                 final Set<Set<OWLAxiom>> inconsistencyExplanations = exp.getInconsistencyExplanations(100);
                 throw new InconsistentOntologyException(inconsistencyExplanations, nextOntology.getOntologyID(),
                         renderer, "Ontology is inconsistent");
             }
         }
         catch(final Throwable e)
         {
             if(nextOntology != null)
             {
                 this.getCachedManager(Collections.<OWLOntologyID> emptySet()).removeOntology(nextOntology);
             }
             throw e;
         }
         
         return nextOntology;
     }
     
     /**
      * Computes the inferences using the given reasoner, which has previously been setup based on an
      * ontology.
      * 
      * @param nextReasoner
      *            The reasoner to use to compute the inferences.
      * @param inferredOntologyID
      *            The OWLOntologyID to use for the inferred ontology. This must be unique and not
      *            previously used in either the repository or the OWLOntologyManager
      * @return An OWLOntology instance containing the axioms that were inferred from the original
      *         ontology.
      * @throws ReasonerInterruptedException
      * @throws TimeOutException
      * @throws InconsistentOntologyException
      * @throws OWLOntologyCreationException
      * @throws OWLOntologyChangeException
      */
     private OWLOntology computeInferences(final OWLReasoner nextReasoner, final OWLOntologyID concreteOntologyID,
             final OWLOntologyID inferredOntologyID) throws ReasonerInterruptedException, TimeOutException,
         OWLOntologyCreationException, OWLOntologyChangeException
     {
         final List<InferredAxiomGenerator<? extends OWLAxiom>> axiomGenerators =
                 new ArrayList<InferredAxiomGenerator<? extends OWLAxiom>>();
         axiomGenerators.add(new InferredClassAssertionAxiomGenerator());
         axiomGenerators.add(new InferredDataPropertyCharacteristicAxiomGenerator());
         axiomGenerators.add(new InferredEquivalentClassAxiomGenerator());
         axiomGenerators.add(new InferredEquivalentDataPropertiesAxiomGenerator());
         axiomGenerators.add(new InferredEquivalentObjectPropertyAxiomGenerator());
         axiomGenerators.add(new InferredInverseObjectPropertiesAxiomGenerator());
         axiomGenerators.add(new InferredObjectPropertyCharacteristicAxiomGenerator());
         
         // NOTE: InferredPropertyAssertionGenerator significantly slows down
         // inference computation
         axiomGenerators.add(new org.semanticweb.owlapi.util.InferredPropertyAssertionGenerator());
         
         axiomGenerators.add(new InferredSubClassAxiomGenerator());
         axiomGenerators.add(new InferredSubDataPropertyAxiomGenerator());
         axiomGenerators.add(new InferredSubObjectPropertyAxiomGenerator());
         
         IRI importIRI = concreteOntologyID.getVersionIRI();
         if(importIRI == null)
         {
             importIRI = concreteOntologyID.getOntologyIRI();
         }
         
         synchronized(this.managerFactory)
         {
             final InferredOntologyGenerator iog = new InferredOntologyGenerator(nextReasoner, axiomGenerators);
             
             nextReasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);
             
             final OWLOntology nextInferredAxiomsOntology =
                     this.getCachedManager(Collections.<OWLOntologyID> emptySet()).createOntology(inferredOntologyID);
             
             this.getCachedManager(Collections.<OWLOntologyID> emptySet()).applyChange(
                     new AddImport(nextInferredAxiomsOntology, new OWLImportsDeclarationImpl(importIRI)));
             
             iog.fillOntology(nextInferredAxiomsOntology.getOWLOntologyManager(), nextInferredAxiomsOntology);
             
             return nextInferredAxiomsOntology;
         }
     }
     
     public OWLReasoner createReasoner(final OWLOntology nextOntology)
     {
         if(this.reasonerFactory == null)
         {
             throw new NullPointerException("Could not find OWL Reasoner Factory");
         }
         
         return this.reasonerFactory.createReasoner(nextOntology);
     }
     
     /**
      * Dump the triples representing a given ontology into a Sesame Repository.
      * 
      * @param nextOntology
      *            The ontology to dump into the repository.
      * @param nextRepositoryConnection
      *            The repository connection to dump the triples into.
      * @throws IOException
      * @throws RepositoryException
      */
     public void dumpOntologyToRepository(final OWLOntology nextOntology,
             final RepositoryConnection nextRepositoryConnection, final URI... contexts) throws IOException,
         RepositoryException
     {
         this.dumpOntologyToRepositoryWithoutDuplication(null, nextOntology, nextRepositoryConnection, contexts);
     }
     
     public void dumpOntologyToRepositoryWithoutDuplication(final URI contextToCompareWith,
             final OWLOntology nextOntology, final RepositoryConnection nextRepositoryConnection, final URI... contexts)
         throws IOException, RepositoryException
     {
         IRI contextIRI = nextOntology.getOntologyID().getVersionIRI();
         
         if(contextIRI == null)
         {
             contextIRI = nextOntology.getOntologyID().getOntologyIRI();
         }
         
         if(contextIRI == null)
         {
             throw new IllegalArgumentException("Cannot dump anonymous ontologies to repository");
         }
         
         final URI context = contextIRI.toOpenRDFURI();
         
         // Create an RDFHandler that will insert all triples after they are
         // emitted from OWLAPI
         // into a specific context in the Sesame Repository
         RDFInserter repositoryHandler = new RDFInserter(nextRepositoryConnection);
         if(contextToCompareWith != null)
         {
             repositoryHandler = new DeduplicatingRDFInserter(contextToCompareWith, nextRepositoryConnection);
         }
         
         RioRenderer renderer;
         
         if(contexts == null || contexts.length == 0)
         {
             repositoryHandler.enforceContext(context);
             // Render the triples out from OWLAPI into a Sesame Repository
             renderer =
                     new RioRenderer(nextOntology, nextOntology.getOWLOntologyManager(), repositoryHandler, null,
                             context);
         }
         else
         {
             repositoryHandler.enforceContext(contexts);
             // Render the triples out from OWLAPI into a Sesame Repository
             renderer =
                     new RioRenderer(nextOntology, nextOntology.getOWLOntologyManager(), repositoryHandler, null,
                             contexts);
         }
         renderer.render();
     }
     
     public InferredOWLOntologyID generateInferredOntologyID(final OWLOntologyID ontologyID)
     {
         if(ontologyID == null || ontologyID.getOntologyIRI() == null || ontologyID.getVersionIRI() == null)
         {
             throw new NullPointerException("OWLOntology is incomplete");
         }
         
         final IRI inferredOntologyIRI = IRI.create(PODD.INFERRED_PREFIX + ontologyID.getVersionIRI());
         
         return new InferredOWLOntologyID(ontologyID.getOntologyIRI(), ontologyID.getVersionIRI(), inferredOntologyIRI);
     }
     
     private OWLReasonerFactory getReasonerFactory()
     {
         return this.reasonerFactory;
     }
     
     public Set<OWLProfile> getReasonerProfiles()
     {
         final Set<OWLProfile> profiles = this.reasonerFactory.getSupportedProfiles();
         if(profiles.isEmpty())
         {
             this.log.warn("Could not find any supported OWL Profiles");
         }
         return profiles;
     }
     
     public InferredOWLOntologyID inferStatements(final OWLOntology nextOntology,
             final RepositoryConnection nextRepositoryConnection, OWLReasoner nextReasoner) throws OWLRuntimeException,
         OWLException, OpenRDFException, IOException
     {
         final InferredOWLOntologyID inferredOntologyID = this.generateInferredOntologyID(nextOntology.getOntologyID());
         if(nextReasoner == null)
         {
             nextReasoner = this.createReasoner(nextOntology);
         }
         // final OWLReasoner nextReasoner = this.createReasoner(nextOntology);
         
         final OWLOntology nextInferredAxiomsOntology =
                 this.computeInferences(nextReasoner, nextOntology.getOntologyID(),
                         inferredOntologyID.getInferredOWLOntologyID());
         
         this.dumpOntologyToRepositoryWithoutDuplication(inferredOntologyID.getVersionIRI().toOpenRDFURI(),
                 nextInferredAxiomsOntology, nextRepositoryConnection, nextInferredAxiomsOntology.getOntologyID()
                         .getOntologyIRI().toOpenRDFURI());
         
         return inferredOntologyID;
     }
     
     @Override
     public boolean isCached(final OWLOntologyID ontologyID)
     {
         Objects.requireNonNull(ontologyID, "Ontology ID cannot be null");
         Objects.requireNonNull(ontologyID.getOntologyIRI(), "Ontology IRI cannot be null");
         
         synchronized(this.managerFactory)
         {
             OWLOntologyManager cachedManager = this.getCachedManager(Collections.<OWLOntologyID> emptySet());
             if(ontologyID.getVersionIRI() != null)
             {
                 return cachedManager.contains(ontologyID.getVersionIRI());
             }
             else
             {
                 return cachedManager.contains(ontologyID.getOntologyIRI());
             }
         }
     }
     
     /**
      * @param owlSource
      * @param permanentRepositoryConnection
      * @param inferredOWLOntologyID
      * @return
      * @throws OWLException
      * @throws Throwable
      */
     @Override
     public InferredOWLOntologyID loadAndInfer(final OWLOntologyDocumentSource owlSource,
             final RepositoryConnection permanentRepositoryConnection, final OWLOntologyID replacementOntologyID)
         throws OWLException, PoddException, OpenRDFException, IOException
     {
         return this.loadAndInfer(permanentRepositoryConnection, replacementOntologyID, owlSource, true);
     }
     
     /**
      * @param permanentRepositoryConnection
      * @param owlSource
      * @param inferredOWLOntologyID
      * @param removeFromCacheOnException
      * @return
      * @throws OWLException
      * @throws Throwable
      */
     public InferredOWLOntologyID loadAndInfer(final RepositoryConnection permanentRepositoryConnection,
             final OWLOntologyID ontologyID, final OWLOntologyDocumentSource owlSource,
             final boolean removeFromCacheOnException) throws OWLException, PoddException, OpenRDFException, IOException
     {
         InferredOWLOntologyID inferredOWLOntologyID = null;
         OWLOntology nextOntology = null;
         try
         {
             nextOntology = this.loadOntologyInternal(ontologyID, owlSource);
             
             // Check the OWLAPI OWLOntology against an OWLProfile to make sure
             // it is in profile
             final OWLProfileReport profileReport =
                     this.getReasonerProfiles().iterator().next().checkOntology(nextOntology);
             if(!profileReport.isInProfile())
             {
                 if(this.log.isInfoEnabled())
                 {
                     for(final OWLProfileViolation violation : profileReport.getViolations())
                     {
                         this.log.info(violation.toString());
                     }
                 }
                 throw new OntologyNotInProfileException(nextOntology, profileReport,
                         "Ontology is not in required OWL Profile: " + profileReport.getProfile().getName());
             }
             
             // Use the OWLManager to create a reasoner over the ontology
             final OWLReasoner nextReasoner = this.createReasoner(nextOntology);
             
             // Test that the ontology was consistent with this reasoner
             // This ensures in the case of Pellet that it is in the OWL2-DL
             // profile
             if(!nextReasoner.isConsistent())
             {
                 final RDFXMLExplanationRenderer renderer = new RDFXMLExplanationRenderer();
                 // Get 100 inconsistency explanations, any more than that and they need to make
                 // modifications and try again
                 final ExplanationUtils exp =
                         new ExplanationUtils((PelletReasoner)nextReasoner,
                                 (PelletReasonerFactory)this.getReasonerFactory(), renderer, new NullProgressMonitor(),
                                 100);
                 
                 try
                 {
                     final Set<Set<OWLAxiom>> inconsistencyExplanations = exp.explainClassHierarchy();
                     
                     throw new InconsistentOntologyException(inconsistencyExplanations, nextOntology.getOntologyID(),
                             renderer, "Ontology is inconsistent (explanation available)");
                 }
                 catch(final org.mindswap.pellet.exceptions.InconsistentOntologyException e)
                 {
                     throw new InconsistentOntologyException(new HashSet<Set<OWLAxiom>>(), nextOntology.getOntologyID(),
                             renderer, "Ontology is inconsistent (textual explanation available): " + e.getMessage());
                 }
                 catch(PelletRuntimeException | OWLRuntimeException e)
                 {
                     throw new InconsistentOntologyException(new HashSet<Set<OWLAxiom>>(), nextOntology.getOntologyID(),
                             renderer, "Ontology is inconsistent (no explanation available): " + e.getMessage());
                 }
             }
             
             // Copy the statements to permanentRepositoryConnection
             this.dumpOntologyToRepository(nextOntology, permanentRepositoryConnection, nextOntology.getOntologyID()
                     .getVersionIRI().toOpenRDFURI());
             
             // NOTE: At this stage, a client could be notified, and the artifact
             // could be streamed
             // back to them from permanentRepositoryConnection
             
             // Use an OWLAPI InferredAxiomGenerator together with the reasoner
             // to create inferred
             // axioms to store in the database.
             // Serialise the inferred statements back to a different context in
             // the permanent
             // repository connection.
             // The contexts to use within the permanent repository connection
             // are all encapsulated
             // in the InferredOWLOntologyID object.
             
             inferredOWLOntologyID = this.inferStatements(nextOntology, permanentRepositoryConnection, nextReasoner);
             
             return inferredOWLOntologyID;
         }
         catch(final Throwable e)
         {
             if(nextOntology != null && removeFromCacheOnException)
             {
                 this.removeCache(nextOntology.getOntologyID());
             }
             
             if(inferredOWLOntologyID != null && removeFromCacheOnException)
             {
                 this.removeCache(inferredOWLOntologyID.getInferredOWLOntologyID());
             }
             
             throw e;
         }
     }
     
     public OWLOntology loadOntologyInternal(final OWLOntologyID ontologyID, final OWLOntologyDocumentSource owlSource)
         throws OWLException, IOException, PoddException
     {
         synchronized(this.managerFactory)
         {
             try
             {
                 OWLOntology nextOntology;
                 if(owlSource instanceof RioMemoryTripleSource)
                 {
                     final RioRDFOntologyFormatFactory ontologyFormatFactory =
                             (RioRDFOntologyFormatFactory)OWLOntologyFormatFactoryRegistry.getInstance().getByMIMEType(
                                     "application/rdf+xml");
                     final RioParserImpl owlParser = new RioParserImpl(ontologyFormatFactory);
                     
                     if(ontologyID == null)
                     {
                         nextOntology = this.getCachedManager(Collections.<OWLOntologyID> emptySet()).createOntology();
                     }
                     else
                     {
                         nextOntology =
                                 this.getCachedManager(Collections.<OWLOntologyID> emptySet())
                                         .createOntology(ontologyID);
                     }
                     
                     owlParser.parse(owlSource, nextOntology);
                 }
                 else
                 {
                     if(ontologyID == null)
                     {
                         nextOntology = this.getCachedManager(Collections.<OWLOntologyID> emptySet()).createOntology();
                     }
                     else
                     {
                         nextOntology =
                                 this.getCachedManager(Collections.<OWLOntologyID> emptySet())
                                         .createOntology(ontologyID);
                     }
                     
                     if(owlSource.isFormatKnown())
                     {
                         final OWLParser parser =
                                 OWLParserFactoryRegistry.getInstance().getParserFactory(owlSource.getFormatFactory())
                                         .createParser(nextOntology.getOWLOntologyManager());
                         parser.parse(owlSource, nextOntology);
                     }
                     else
                     {
                         // FIXME: loadOntologyFromOntologyDocument does not allow for this case
                         nextOntology =
                                 this.getCachedManager(Collections.<OWLOntologyID> emptySet())
                                         .loadOntologyFromOntologyDocument(owlSource);
                     }
                 }
                 
                 if(nextOntology.isEmpty())
                 {
                     throw new EmptyOntologyException(nextOntology, "Loaded ontology is empty");
                 }
                 return nextOntology;
             }
             catch(final OWLRuntimeException e)
             {
                 throw new OWLOntologyCreationException("Could not load ontology", e);
             }
         }
     }
     
     public OWLOntologyID parseRDFStatements(final Model model) throws OpenRDFException, OWLException, IOException,
         PoddException
     {
         this.log.info("About to parse statements");
         synchronized(this.managerFactory)
         {
             final RioMemoryTripleSource owlSource =
                     new RioMemoryTripleSource(model.iterator(), Namespaces.asMap(model.getNamespaces()));
             
             final RioParserImpl owlParser = new RioParserImpl(null);
             
             OWLOntologyManager cachedManager = this.getCachedManager(Collections.<OWLOntologyID> emptySet());
             
             this.log.info("Creating new ontology");
             final OWLOntology nextOntology = cachedManager.createOntology();
             
             if(model.size() == 0)
             {
                 throw new EmptyOntologyException(nextOntology, "No statements to create an ontology");
             }
             
             this.log.info("Parsing into the new ontology");
             
             owlParser.parse(owlSource, nextOntology);
             
             this.log.info("Finished parsing into the new ontology");
             
             if(nextOntology.isEmpty())
             {
                 throw new EmptyOntologyException(nextOntology, "Loaded ontology is empty");
             }
             
             return nextOntology.getOntologyID();
         }
     }
     
     public OWLOntologyID parseRDFStatements(final RepositoryConnection conn, final URI... contexts)
         throws OpenRDFException, OWLException, IOException, PoddException
     {
         OpenRDFUtil.verifyContextNotNull(contexts);
         
         this.log.info("Exporting statements to model for parsing");
         final Model model = new LinkedHashModel();
         conn.export(new StatementCollector(model), contexts);
         
         return this.parseRDFStatements(model);
     }
     
     @Override
     public boolean removeCache(final OWLOntologyID ontologyID) throws OWLException
     {
         synchronized(this.managerFactory)
         {
             if(ontologyID instanceof InferredOWLOntologyID)
             {
                 final boolean containsInferredOntology =
                         this.getCachedManager(Collections.<OWLOntologyID> emptySet()).contains(
                                 ((InferredOWLOntologyID)ontologyID).getInferredOntologyIRI());
                 if(containsInferredOntology)
                 {
                     this.getCachedManager(Collections.<OWLOntologyID> emptySet()).removeOntology(
                             this.getCachedManager(Collections.<OWLOntologyID> emptySet()).getOntology(
                                     ((InferredOWLOntologyID)ontologyID).getInferredOntologyIRI()));
                 }
                 // TODO: Verify that this .contains method matches our desired
                 // semantics
                 final boolean containsOntology =
                         this.getCachedManager(Collections.<OWLOntologyID> emptySet()).contains(
                                 ((InferredOWLOntologyID)ontologyID).getBaseOWLOntologyID());
                 
                 if(containsOntology)
                 {
                     this.getCachedManager(Collections.<OWLOntologyID> emptySet()).removeOntology(
                             ((InferredOWLOntologyID)ontologyID).getBaseOWLOntologyID());
                     return !this.getCachedManager(Collections.<OWLOntologyID> emptySet()).contains(
                             ((InferredOWLOntologyID)ontologyID).getBaseOWLOntologyID());
                 }
                 
                 return containsInferredOntology || containsOntology;
             }
             else
             {
                 // TODO: Verify that this .contains method matches our desired
                 // semantics
                 final boolean containsOntology =
                         this.getCachedManager(Collections.<OWLOntologyID> emptySet()).contains(ontologyID);
                 
                 if(containsOntology)
                 {
                     this.getCachedManager(Collections.<OWLOntologyID> emptySet()).removeOntology(ontologyID);
                     
                     // return true if the ontology manager does not contain the
                     // ontology at this point
                     return !this.getCachedManager(Collections.<OWLOntologyID> emptySet()).contains(ontologyID);
                 }
                 else
                 {
                     return false;
                 }
             }
         }
     }
     
     @Override
     public void verifyAgainstSchema(final Model model, final Model schemaModel) throws OntologyNotInProfileException
     {
         OWLOntology dataRepositoryOntology = null;
         OWLOntology defaultAliasOntology = null;
         
         synchronized(this.managerFactory)
         {
             try
             // (final InputStream inputA =
             // this.getClass().getResourceAsStream(PoddRdfConstants.PATH_PODD_DATA_REPOSITORY);)
             {
                 // load poddDataRepository.owl into a Model
                 // final Model schemaModel = Rio.parse(inputA, "", RDFFormat.RDFXML);
                 // Rio.parse(inputA, null, RDFFormat.RDFXML);
                 // verify & load poddDataRepository.owl into OWLAPI
                 dataRepositoryOntology = this.checkForConsistentOwlDlOntology(schemaModel);
                 
                 defaultAliasOntology = this.checkForConsistentOwlDlOntology(model);
             }
             catch(final PoddException e)// | OpenRDFException | IOException e)
             {
                 final String msg = "Failed verification of the DataRepsitory against poddDataRepository.owl";
                 this.log.error(msg, e);
                 throw new OntologyNotInProfileException(null, null, msg, e);
             }
             finally
             {
                 // clear OWLAPI memory
                 if(defaultAliasOntology != null)
                 {
                     this.getCachedManager(Collections.<OWLOntologyID> emptySet()).removeOntology(defaultAliasOntology);
                 }
                 if(dataRepositoryOntology != null)
                 {
                     this.getCachedManager(Collections.<OWLOntologyID> emptySet())
                             .removeOntology(dataRepositoryOntology);
                 }
             }
         }
     }
 }
