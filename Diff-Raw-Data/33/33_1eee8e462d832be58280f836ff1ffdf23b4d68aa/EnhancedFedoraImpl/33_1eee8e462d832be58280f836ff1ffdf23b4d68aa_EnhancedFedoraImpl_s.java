 package dk.statsbiblioteket.doms.central.connectors;
 
 import dk.statsbiblioteket.doms.central.connectors.fedora.Fedora;
 import dk.statsbiblioteket.doms.central.connectors.fedora.FedoraRest;
 import dk.statsbiblioteket.doms.central.connectors.fedora.inheritance.ContentModelInheritance;
 import dk.statsbiblioteket.doms.central.connectors.fedora.inheritance.ContentModelInheritanceImpl;
 import dk.statsbiblioteket.doms.central.connectors.fedora.methods.Methods;
 import dk.statsbiblioteket.doms.central.connectors.fedora.methods.MethodsImpl;
 import dk.statsbiblioteket.doms.central.connectors.fedora.methods.generated.Method;
 import dk.statsbiblioteket.doms.central.connectors.fedora.pidGenerator.PIDGeneratorException;
 import dk.statsbiblioteket.doms.central.connectors.fedora.pidGenerator.PidGenerator;
 import dk.statsbiblioteket.doms.central.connectors.fedora.pidGenerator.PidGeneratorImpl;
 import dk.statsbiblioteket.doms.central.connectors.fedora.structures.FedoraRelation;
 import dk.statsbiblioteket.doms.central.connectors.fedora.structures.ObjectProfile;
 import dk.statsbiblioteket.doms.central.connectors.fedora.structures.SearchResult;
 import dk.statsbiblioteket.doms.central.connectors.fedora.templates.ObjectIsWrongTypeException;
 import dk.statsbiblioteket.doms.central.connectors.fedora.templates.Templates;
 import dk.statsbiblioteket.doms.central.connectors.fedora.templates.TemplatesImpl;
 import dk.statsbiblioteket.doms.central.connectors.fedora.tripleStore.TripleStore;
 import dk.statsbiblioteket.doms.central.connectors.fedora.tripleStore.TripleStoreRest;
 import dk.statsbiblioteket.doms.central.connectors.fedora.views.Views;
 import dk.statsbiblioteket.doms.central.connectors.fedora.views.ViewsImpl;
 import dk.statsbiblioteket.doms.webservices.authentication.Credentials;
 import dk.statsbiblioteket.util.Pair;
 import org.w3c.dom.Document;
 
 
 import javax.xml.bind.JAXBException;
 import java.net.MalformedURLException;
 import java.util.List;
 
 /**
  * Created by IntelliJ IDEA.
  * User: abr
  * Date: 3/29/12
  * Time: 2:34 PM
  * To change this template use File | Settings | File Templates.
  */
 public class EnhancedFedoraImpl implements EnhancedFedora{
 
     Fedora fedora;
     TripleStore ts;
     Templates templates;
     Views views;
     ContentModelInheritance cmInher;
     PidGenerator pidGenerator;
     private Methods methods;
     private String thisLocation;
 
     public EnhancedFedoraImpl(Credentials creds, String fedoraLocation, String pidGenLocation, String thisLocation)
             throws MalformedURLException, PIDGeneratorException, JAXBException {
         this.thisLocation = thisLocation;
 
         //1.st level
         fedora = new FedoraRest(creds,fedoraLocation);
         ts = new TripleStoreRest(creds,fedoraLocation);
         pidGenerator = new PidGeneratorImpl(pidGenLocation);
 
         //2. level
         cmInher = new ContentModelInheritanceImpl(fedora, ts);
 
         //3. level
         templates = new TemplatesImpl(fedora,pidGenerator,ts,cmInher);
         views = new ViewsImpl(ts,cmInher,fedora);
 
         methods = new MethodsImpl(fedora,thisLocation);
     }
 
     public String cloneTemplate(String templatepid, List<String> oldIDs, String logMessage)
             throws BackendInvalidCredsException, BackendMethodFailedException, ObjectIsWrongTypeException,
                    BackendInvalidResourceException, PIDGeneratorException {
         return templates.cloneTemplate(templatepid, oldIDs, logMessage);
     }
 
     public ObjectProfile getObjectProfile(String pid, Long asOfTime)
             throws BackendMethodFailedException, BackendInvalidCredsException, BackendInvalidResourceException {
         return fedora.getObjectProfile(pid, asOfTime);
     }
 
     @Override
     public void modifyObjectLabel(String pid, String name, String comment)
             throws BackendInvalidCredsException, BackendMethodFailedException, BackendInvalidResourceException {
         fedora.modifyObjectLabel(pid,name,comment);
     }
 
     @Override
     public void modifyObjectState(String pid, String stateDeleted, String comment)
             throws BackendInvalidCredsException, BackendMethodFailedException, BackendInvalidResourceException {
         fedora.modifyObjectState(pid,stateDeleted,comment);
     }
 
     @Override
     public void modifyDatastreamByValue(String pid, String datastream, String contents, String comment)
             throws BackendInvalidCredsException, BackendMethodFailedException, BackendInvalidResourceException {
         fedora.modifyDatastreamByValue(pid,datastream,contents,comment);
     }
 
     @Override
     public String getXMLDatastreamContents(String pid, String datastream,Long asOfDateTime)
             throws BackendInvalidCredsException, BackendMethodFailedException, BackendInvalidResourceException {
         return fedora.getXMLDatastreamContents(pid,datastream,asOfDateTime);
     }
 
     @Override
     public void addExternalDatastream(String pid, String contents, String filename, String permanentURL,
                                       String formatURI, String mimetype, String comment)
             throws BackendInvalidCredsException, BackendMethodFailedException, BackendInvalidResourceException {
         fedora.addExternalDatastream(pid,contents,filename,permanentURL,formatURI,mimetype,comment);
     }
 
     @Override
     public List<String> listObjectsWithThisLabel(String label)
             throws BackendInvalidCredsException, BackendMethodFailedException {
         return  ts.listObjectsWithThisLabel(label);
     }
 
     @Override
     public void addRelation(String pid, String subject, String predicate, String object, boolean literal,
                             String comment)
             throws BackendInvalidCredsException, BackendMethodFailedException, BackendInvalidResourceException {
         fedora.addRelation(pid,subject,predicate,object,literal,comment);
     }
 
     @Override
     public List<FedoraRelation> getNamedRelations(String pid, String predicate, Long asOfTime)
             throws BackendInvalidCredsException, BackendMethodFailedException, BackendInvalidResourceException {
         return  fedora.getNamedRelations(pid,predicate, asOfTime);
     }
 
     @Override
     public List<FedoraRelation> getInverseRelations(String pid, String name)
             throws BackendInvalidCredsException, BackendMethodFailedException, BackendInvalidResourceException {
         return ts.getInverseRelations(pid,name);
     }
 
     @Override
     public void deleteRelation(String pid, String subject, String predicate, String object, boolean literal,
                                String comment)
             throws BackendInvalidCredsException, BackendMethodFailedException, BackendInvalidResourceException {
         fedora.deleteRelation(pid,subject,predicate,object,literal,comment);
     }
 
     @Override
     public Document createBundle(String pid, String viewAngle, Long asOfTime)
             throws BackendInvalidCredsException, BackendMethodFailedException, BackendInvalidResourceException {
         return views.getViewObjectBundleForObject(pid,viewAngle,asOfTime);
     }
 
     @Override
     public List<String> findObjectFromDCIdentifier(String string)
             throws BackendInvalidCredsException, BackendMethodFailedException {
         return ts.findObjectFromDCIdentifier(string);
     }
 
     @Override
     public List<SearchResult> fieldsearch(String query, int offset, int pageSize)
             throws BackendInvalidCredsException, BackendMethodFailedException {
         return fedora.fieldsearch(query,offset,pageSize);
     }
 
     @Override
     public void flushTripples() throws BackendInvalidCredsException, BackendMethodFailedException {
         ts.flushTriples();
     }
 
     @Override
     public List<String> getObjectsInCollection(String collectionPid, String contentModelPid)
             throws BackendInvalidCredsException, BackendMethodFailedException {
         return ts.getObjectsInCollection(collectionPid,contentModelPid);
     }
 
     @Override
     public List<Method> getStaticMethods(String cmpid, Long asOfTime) throws BackendInvalidCredsException, BackendMethodFailedException, BackendInvalidResourceException {
         return methods.getStaticMethods(cmpid,asOfTime);
     }
 
     @Override
     public List<Method> getDynamicMethods(String objpid, Long asOfTime) throws BackendInvalidCredsException, BackendMethodFailedException, BackendInvalidResourceException {
         return methods.getDynamicMethods(objpid,asOfTime);
     }
 
 
     @Override
    public String invokeMethod(String cmpid, String methodName, List<Pair<String, String>> parameters, Long asOfTime,String logMessage) throws BackendInvalidCredsException, BackendMethodFailedException, BackendInvalidResourceException {
         return methods.invokeMethod(cmpid,methodName,parameters,asOfTime,logMessage);
     }
 
 }
