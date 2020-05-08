 package au.com.miskinhill.preprocessor;
 
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.Set;
 import java.util.logging.Logger;
 
 import com.hp.hpl.jena.rdf.model.Model;
 import com.hp.hpl.jena.rdf.model.Property;
 import com.hp.hpl.jena.rdf.model.RDFNode;
 import com.hp.hpl.jena.rdf.model.Resource;
 import com.hp.hpl.jena.rdf.model.ResourceFactory;
 import com.hp.hpl.jena.rdf.model.Statement;
 import com.hp.hpl.jena.rdf.model.StmtIterator;
 import com.hp.hpl.jena.vocabulary.DCTerms;
 import com.hp.hpl.jena.vocabulary.OWL;
 import com.hp.hpl.jena.vocabulary.RDF;
 import com.hp.hpl.jena.vocabulary.RDFS;
 
 import au.com.miskinhill.rdf.RDFUtil;
 import au.com.miskinhill.rdf.vocabulary.FOAF;
 import au.com.miskinhill.rdf.vocabulary.Prism;
 
 public class Validator {
     
     public static final class Failure extends Exception {
         private static final long serialVersionUID = 5073419954124017052L;
         public Failure(String message) {
             super(message);
         }
     }
     
     private static final Logger LOG = Logger.getLogger(Validator.class.getName());
     private static final Set<Resource> DOMAIN_OBJECT_EXCEPTIONS = Collections.unmodifiableSet(new HashSet<Resource>(Arrays.asList(
             RDFS.Resource, OWL.Thing)));
     private static final Set<Property> RANGE_PROPERTY_EXCEPTIONS = Collections.unmodifiableSet(new HashSet<Property>(Arrays.asList(
             ResourceFactory.createProperty(DCTerms.NS + "publisher"), ResourceFactory.createProperty(DCTerms.NS + "identifier"),
             ResourceFactory.createProperty(DCTerms.NS + "coverage"), Prism.publicationDate)));
     private static final Set<Resource> RANGE_OBJECT_EXCEPTIONS = Collections.unmodifiableSet(new HashSet<Resource>(Arrays.asList(
             ResourceFactory.createResource("http://www.w3.org/TR/2000/CR-rdf-schema-20000327#Literal"), FOAF.Document, OWL.Thing)));
     
     private final Model m;
     
     public Validator(Model m) {
         this.m = m;
     }
     
     public void validate() throws Failure {
         LOG.info("Validating domain constraints");
         for (StmtIterator it = m.listStatements(null, RDFS.domain, (RDFNode) null); it.hasNext(); ) {
             Statement domainConstraint = it.nextStatement();
             if (DOMAIN_OBJECT_EXCEPTIONS.contains((Resource) domainConstraint.getObject()))
                 continue;
             for (StmtIterator jt = m.listStatements(null, (Property) domainConstraint.getSubject().as(Property.class), (RDFNode) null); jt.hasNext(); ) {
                 Statement stmt = jt.nextStatement();
                 if (!RDFUtil.getTypes(stmt.getSubject()).contains(domainConstraint.getObject()))
                     throw new Failure("Property " + domainConstraint.getSubject() + " on " + stmt.getSubject() +
                            " violates rdfs:domain constraint of " + domainConstraint.getObject());
             }
         }
         
         LOG.info("Validating range constraints");
         for (StmtIterator it = m.listStatements(null, RDFS.range, (RDFNode) null); it.hasNext(); ) {
             Statement rangeConstraint = it.nextStatement();
             if (RANGE_PROPERTY_EXCEPTIONS.contains((Property) rangeConstraint.getSubject().as(Property.class)) ||
                     RANGE_OBJECT_EXCEPTIONS.contains((Resource) rangeConstraint.getObject()))
                 continue;
             if (rangeConstraint.getObject().equals(RDFS.Literal) ||
                     rangeConstraint.getObject().equals(ResourceFactory.createResource(RDF.getURI() + "Literal"))) {
                 for (StmtIterator jt = m.listStatements(null, (Property) rangeConstraint.getSubject().as(Property.class), (RDFNode) null); jt.hasNext(); ) {
                     Statement stmt = jt.nextStatement();
                     if (!stmt.getObject().isLiteral())
                         throw new Failure("Property " + rangeConstraint.getSubject() + " to " + stmt.getObject() +
                                 " violates rdfs:range literal constraint");
                 }
             } else {
                 for (StmtIterator jt = m.listStatements(null, (Property) rangeConstraint.getSubject().as(Property.class), (RDFNode) null); jt.hasNext(); ) {
                     Statement stmt = jt.nextStatement();
                     if (!RDFUtil.getTypes((Resource) stmt.getObject()).contains(rangeConstraint.getObject()))
                         throw new Failure("Property " + rangeConstraint.getSubject() + " to " + stmt.getObject() +
                                " violates rdfs:range constraint of " + rangeConstraint.getObject());
                 }
             }
         }
     }
 
 }
