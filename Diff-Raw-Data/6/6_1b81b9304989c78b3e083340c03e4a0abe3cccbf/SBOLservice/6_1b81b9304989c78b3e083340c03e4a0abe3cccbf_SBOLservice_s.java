 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.sbolstandard.libSBOLj;
 
 import com.clarkparsia.empire.Empire;
 import com.clarkparsia.empire.config.EmpireConfiguration;
 import com.clarkparsia.empire.sesametwo.OpenRdfEmpireModule;
 import com.clarkparsia.openrdf.ExtGraph;
 import com.clarkparsia.openrdf.ExtRepository;
 import com.clarkparsia.openrdf.OpenRdfUtil;
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.StringWriter;
 import java.io.UnsupportedEncodingException;
 import java.net.URI;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.persistence.EntityManager;
 import javax.persistence.Persistence;
 import javax.persistence.Query;
 import org.openrdf.rio.RDFFormat;
 import org.openrdf.rio.RDFParseException;
 
 /**
  * SBOLservice provides the methods for making new SBOL objects and adding SBOL
  * data.
  *
  * Use methods of SBOLservice when creating new SBOL objects and adding data.
  * It is called a service as it performs operations on the SBOL objects that do
  * not really belong to the class itself. These convenience methods SHOLD include
  * an entity manager from empire, delete methods, creating entities SHOULD check
  * for previously existing objects and eventually should be the only way most
  * applications interact with the SBOL API.
  * 
  * inspired by examples on: http://www.java2s.com/Code/Java/JPA
  * it should do more of the things found in the intro to jpa:
  * http://www.javaworld.com/javaworld/jw-01-2008/jw-01-jpa1.html
  * @todo update and delete methods.
  *
  * @author mgaldzic
 * @since 0.2, 03/2/2011
  */
 public class SBOLservice {

     private EntityManager aManager = null;
     private Library library = null;
 
     public SBOLservice() {
         EmpireConfiguration empireConfig = new EmpireConfiguration();
         empireConfig.getGlobalConfig().put("annotation.index", "config//libSBOLj.empire.annotation.config");
         empireConfig.getGlobalConfig().put("name", "michal");
         empireConfig.getGlobalConfig().put("factory", "sesame");
         empireConfig.getGlobalConfig().put("files", "data//blank.rdf");
         Empire.init(empireConfig, new OpenRdfEmpireModule());
         aManager = Persistence.createEntityManagerFactory("newRDF").createEntityManager();
     }
 
     public SBOLservice(String rdfString) {
         this();
         InputStream is = null;
         try {
             ExtRepository aRepo = OpenRdfUtil.createInMemoryRepo();
             is = new ByteArrayInputStream(rdfString.getBytes("UTF-8"));
             try {
                 aRepo.read(is, RDFFormat.RDFXML);
             } catch (IOException ex) {
                 Logger.getLogger(SBOLservice.class.getName()).log(Level.SEVERE, null, ex);
             } catch (RDFParseException ex) {
                 Logger.getLogger(SBOLservice.class.getName()).log(Level.SEVERE, null, ex);
             }
             Map aMap = new HashMap();
             aMap.put("repo_handle", aRepo);
 
             aManager = Persistence.createEntityManagerFactory("existingRDF").createEntityManager(aMap);
         } catch (UnsupportedEncodingException ex) {
             Logger.getLogger(SBOLservice.class.getName()).log(Level.SEVERE, null, ex);
         } finally {
             try {
                 is.close();
             } catch (IOException ex) {
                 Logger.getLogger(SBOLservice.class.getName()).log(Level.SEVERE, null, ex);
             }
         }
 
     }
 
     /**
      * Builds a new DnaSequence using a string of dna sequence is input.
      *
      * @param dnaSequence a string with a sequence of [a|c|t|g] letters
      *
      * @see DnaSequence#setDnaSequence
      * 
      * @return DnaSequence can be used as the sequence of a DnaComponent or
      * SequenceFeature
      */
     public DnaSequence createDnaSequence(String dnaSequence) {
         DnaSequence aDS = new DnaSequence();
         aDS.setDnaSequence(dnaSequence);
         aManager.persist(aDS);
         return aDS;
     }
 
     /**
      * Builds a new SequenceFeature which SequenceAnnotations can point to.
      *
      * @param displayId A human readable identifier
      * @param name commonly used to refer to this SequenceFeature (eg. pLac-O1)
      * @param description human readable text describing the feature
      * @param type Sequence Ontology vocabulary term describing the kind of thing
      * the feature is
      * @return a SequenceFeature which can be used to describe a SequenceAnnotation
      *
      * @see SequenceFeature
      */
     public SequenceFeature createSequenceFeature(String displayId, String name,
             String description, String type) {
         SequenceFeature aSF = new SequenceFeature();
         aSF.setDisplayId(displayId);
         aSF.setName(name);
         aSF.setDescription(description);
         aSF.addType(URI.create("http://purl.org/obo/owl/SO#" + type));
         aManager.persist(aSF);
         return aSF;
     }
 
     /**
      * Builds a new SequenceAnnotation to describe a segment of the DnaComponent.
      *
      * Links the SequenceAnnotation to the component, it can only describe one
      * DnaComponent. (But, SequenceFeatures are to be re-used, when the same one
      * is describes multiple DnaCompenents)
      *
      * @param start coordinate of first base of the SequenceFeature
      * @param stop coordinate of last base of the SequenceFeature
      * @param strand <code>+</code> if feature aligns in same direction as DnaComponent,
      *               <code>-</code> if feature aligns in opposite direction as DnaComponent.
      * @return a SequenceAnnotation made to annotate a DnaComponent
      *
      * @see SequenceAnnotation#setStrand(java.lang.String)
      */
     public SequenceAnnotation createSequenceAnnotationForDnaComponent(Integer start,
             Integer stop, String strand, DnaComponent component) {
         SequenceAnnotation aSA = new SequenceAnnotation();
         aSA.setStart(start);
         aSA.setStop(stop);
         aSA.setStrand(strand);
         aSA.setId(component);
         aManager.persist(aSA);
         component.addAnnotation(aSA);
         aManager.merge(component);
         return aSA;
     }
 
     /**
      * Links SequenceFeature to its SequenceAnnotation.
      *
      * @param feature description of the position
      * @param annotation position information for a DnaComponent being described
      * @return The linked SequenceAnnotation. //WHY? here is an example of when
      * objects should be kept in a entity manager
      */
     public SequenceAnnotation addSequenceFeatureToSequenceAnnotation(
             SequenceFeature feature, SequenceAnnotation annotation) {
         annotation.addFeature(feature);
         if (aManager.contains(annotation)){
             aManager.merge(annotation);
         } else {
             aManager.persist(annotation);
         }
         
         return annotation;
     }
 
     /**
      * Builds a new DnaComponent to describe and add to Library
      *
      * @param displayId    human readable identifier
      * @param name         commonly used to refer to this DnaComponent
      *                     (eg. pLac-O1)
      * @param description  human readable text describing the component
      * @param isCircular   <code>true</code> if DNA is circular
      *                     <code>false</code> if DNA is linear
      * @param type         Sequence Ontology vocabulary term specifying the kind
      *                     of thing the DnaComponent is
      * @param dnaSequence  previously created DnaSequence of this DnaComponent
      *
 
      * @return DnaComponent which should be added to a Library
      * @see DnaComponent#setDisplayId(java.lang.String)
      * @see DnaComponent#setName(java.lang.String)
      * @see DnaComponent#setDescription(java.lang.String)
      * @see DnaComponent#setCircular(boolean)
      * @see DnaComponent#setDnaSequence(org.sbolstandard.libSBOLj.DnaSequence)
      */
     public DnaComponent createDnaComponent(String displayId, String name,
             String description, Boolean isCircular, String type,
             DnaSequence dnaSequence) {
         DnaComponent aDC = new DnaComponent();
         aDC.setDisplayId(displayId);
         aDC.setName(name);
         aDC.setDescription(description);
         aDC.setCircular(isCircular);
 
         aDC.addType(URI.create("http://purl.org/obo/owl/SO#" + type));
         aDC.setDnaSequence(dnaSequence);
         aManager.persist(aDC);
 
 
         return aDC;
 
 
     }
 
     /**
      * Builds a new Library, ready to collect any DnaComponents or Features.
      *
      * A Library is the primary object that holds everything to exchange via SBOL.
      * Create one, and then add DnaComponents and/or SequenceFeatures to it,
      * then send it to a friend or another SBOL application.
      *
      * @param displayId A human readable identifier
      * @param name commonly used to refer to this Library (eg BIOAFAB Pilot Project)
      * @param description human readable text describing the Library (eg Pilot Project Designs, see http://biofab.org/data)
      * @return a Library with the metadata fields set, empty otherwise (ie no components or features)
      */
     public Library createLibrary(String displayId, String name, String description) {
         Library aL = new Library();
         aL.setDisplayId(displayId);
         aL.setName(name);
         aL.setDescription(description);
         aManager.persist(aL);
 
 
         return aL;
 
 
     }
 
     /**
      * Link the DnaComponent into a Library for organizing it as a list of components
      * that can be re-used, exchanged with another application, or published on the web.
      *
      * @param component a DnaComponent that is to be part of the Library
      * @param library a Library which will hold this DnaComponent
      * @return the Library with the DnaComponent inside
      */
     public Library addDnaComponentToLibrary(DnaComponent component, Library library) {
         library.addComponent(component);
         if (aManager.contains(library)){
         aManager.merge(library);
         }else{
             aManager.persist(library);
         }
 
         return library;
     }
 
     /**
      * Link the SequenceFeature into a Library for organizing it as a list of
      * features that can be re-used, exchanged with another application, or published
      * on the web.
      *
      * Features do not have to describe components. They are useful on their own
      * if you want to take a library of features and annotate your own DnaComponents,
      * also known as, DNA constructs, with them.
      *
      * @param feature SequenceFeature to be added to a Library
      * @param library Library that will hold the SequenceFeature
      * @return
      */
     public Library addSequenceFeatureToLibrary(SequenceFeature feature, Library library) {
         library.addFeature(feature);
         if (aManager.contains(library)) {
             aManager.merge(library);
         } else {
             aManager.persist(library);
         }
 
         return library;
     }
 
     public String getAllAsRDF() {
         String rdfString = null;
 
 
         try {
             Query aQuery = aManager.createQuery("CONSTRUCT {?s ?p ?o} WHERE {?s ?p ?o.}");
             ExtGraph singleResult = (ExtGraph) aQuery.getSingleResult();
             StringWriter out = new StringWriter();
             //RDFXMLPrettyWriter rdfWriter = new RDFXMLPrettyWriter(out);
             singleResult.write(out, RDFFormat.RDFXML);
             rdfString = out.toString();
 
         } catch (IOException ex) {
             Logger.getLogger(SBOLservice.class.getName()).log(Level.SEVERE, null, ex);
         }
         return rdfString;
     }
 
     public Library getLibrary(String id) {
         Library findMe = new Library();
         findMe.setId(id);
         aManager.persist(findMe);
         Library lib = aManager.find(Library.class, findMe.getRdfId());
         return lib;
     }
 
     public DnaComponent addSequenceAnnotationToDnaComponent(SequenceAnnotation annotation, DnaComponent component) {
 
         component.addAnnotation(annotation);
         if (aManager.contains(component)) {
             aManager.merge(component);
         } else {
             aManager.persist(component);
         }
         return component;
     }
 
     /**
      * Adds the Library given as input to the SBOLservice.
      *
      * If you already have a Library of components and features, you can add it
      * directly to the SBOLservice, to get the benefits of SBOL data persistence services.
      * 
      * @param a Library with the metadata fields set, empty otherwise (ie no components or features)
      */
     public void insertLibrary(Library lib) {
         aManager.persist(lib);
     }
 
     public void insertDnaComponent(DnaComponent comp) {
         aManager.persist(comp);
     }
 
     public void insertSequenceAnnotation(SequenceAnnotation anot) {
         aManager.persist(anot);
     }
 
     public void insertSequenceFeature(SequenceFeature feat) {
         aManager.persist(feat);
     }
 }
