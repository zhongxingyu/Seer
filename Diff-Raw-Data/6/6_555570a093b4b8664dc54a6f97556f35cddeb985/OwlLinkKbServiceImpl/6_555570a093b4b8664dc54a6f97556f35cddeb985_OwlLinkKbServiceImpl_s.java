 
 /*
  * Copyright 2012 EMBL-EBI
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package uk.ac.ebi.ricordo.owlkb.service;
 
 import org.apache.log4j.Logger;
 import org.semanticweb.owlapi.model.*;
 import org.semanticweb.owlapi.owllink.*;
 import org.semanticweb.owlapi.owllink.builtin.requests.*;
 import org.semanticweb.owlapi.owllink.builtin.response.*;
 import org.semanticweb.owlapi.owllink.retraction.RetractRequest;
 import org.semanticweb.owlapi.owllink.server.OWLlinkServer;
 import org.semanticweb.owlapi.owllink.server.serverfactory.PelletServerFactory;
 import org.semanticweb.owlapi.reasoner.Node;
 import uk.ac.ebi.ricordo.owlkb.bean.Term;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.Set;
 
 
 /**
  * Created by IntelliJ IDEA.
  * User: Sarala Wimalaratne
  * Date: 02/03/12
  * Time: 14:22
  */
 public class OwlLinkKbServiceImpl implements OwlKbService {
     private IRI docIRI;
     private IRI kbIRI;
     private String kbNs;
     private OWLlinkReasoner reasoner =null;
     private OWLOntologyManager owlOntologyManager = null;
     private OWLlinkServer server = null;
 
     private final String serverUrl;
     private final String serverPort;
     private QueryConstructorService queryConstructorService;
 
     Logger logger = Logger.getLogger(OwlKbServiceImlp.class);
 
     public OwlLinkKbServiceImpl(String serverUrl, String serverPort, String kbNs, IRI docIRI, OWLOntologyManager owlOntologyManager, QueryConstructorService queryConstructorService){
         this.serverUrl = serverUrl;
         this.serverPort = serverPort;
         this.queryConstructorService = queryConstructorService;
         this.kbNs = kbNs;
         this.owlOntologyManager = owlOntologyManager;
         this.docIRI = docIRI;
         kbIRI = org.semanticweb.owlapi.model.IRI.create(kbNs);
     }
 
     /**
      * Calls to the knowledge base
      * @param request
      * @return response
      */
     private Response executeReasoner(Request request){
         if(reasoner==null)
             setUpReasoner(serverUrl, serverPort);
 
         Response response = null;
         try{
             response = reasoner.answer(request);
         }catch(OWLlinkReasonerIOException e){
             e.printStackTrace();
         }
         return response;
     }
 
     /**
      * Queries for all subclasses
      * @param query following Manchester Query Syntax
      * @return list of terms
      */
     public ArrayList<Term> getSubTerms(String query) {
         OWLClassExpression exp = queryConstructorService.runManchesterQuery(query);
         return getSubTerms(exp);
     }
 
     /**
      * Queries for all subclasses
      * @param exp Owl class expression constructed from the manchester query
      * @return list of terms
      */
     private ArrayList<Term> getSubTerms(OWLClassExpression exp) {
         ArrayList<Term> idList = new ArrayList<Term>();
         if(exp!=null){
             GetSubClasses getSubClasses = new GetSubClasses(kbIRI, exp);
             SetOfClassSynsets synsets = (SetOfClassSynsets)executeReasoner(getSubClasses);
             for (Object synset : synsets) {
                 Node<OWLClass> owlClassNode = (Node<OWLClass>) synset;
                 idList.add(new Term(owlClassNode.getEntities().iterator().next().toStringID()));
             }
         }
         return idList;
     }
 
 
     public ArrayList<Term> getEquivalentTerms(String query){
         OWLClassExpression exp = queryConstructorService.runManchesterQuery(query);
         return getEquivalentTerms(exp);
     }
 
     /**
      * Queries for all equivalent
      * @param exp Owl class expression constructed from the manchester query
      * @return list of terms
      */
     private ArrayList<Term> getEquivalentTerms(OWLClassExpression exp){
         ArrayList<Term> idList = new ArrayList<Term>();
         if(exp!=null){
             GetEquivalentClasses getEquivalentClasses = new GetEquivalentClasses(kbIRI,exp);
             SetOfClasses eqclasses = (SetOfClasses)executeReasoner(getEquivalentClasses);
             for (Object eqclass : eqclasses) {
                 OWLClass eqClass = (OWLClass) eqclass;
                 idList.add(new Term(eqClass.toStringID()));
             }
         }
         return idList;
     }
 
     public ArrayList<Term> getTerms(String query) {
         ArrayList<Term> idList = new ArrayList<Term>();
         OWLClassExpression exp = queryConstructorService.runManchesterQuery(query);
         idList.addAll(getEquivalentTerms(exp));
         idList.addAll(getSubTerms(exp));
         return idList;
     }
 
     public ArrayList<Term> addTerm(String query) {
         OWLClassExpression exp = queryConstructorService.runManchesterQuery(query);
         ArrayList<Term> idList = getEquivalentTerms(exp);
         if(idList.isEmpty()){
             String ricordoid = String.valueOf(System.currentTimeMillis());
             OWLClass newowlclass = owlOntologyManager.getOWLDataFactory().getOWLClass(IRI.create(kbNs+"#RICORDO_"+ricordoid));
 
             OWLAxiom axiom = owlOntologyManager.getOWLDataFactory().getOWLEquivalentClassesAxiom(newowlclass, exp);
             Set<OWLAxiom> axiomSet = new HashSet<OWLAxiom>();
             axiomSet.add(axiom);
             Tell tellRequest = new Tell(kbIRI,axiomSet);
             OK okResponse = (OK)executeReasoner(tellRequest);//reasoner.answer(tellRequest);
 
             //add to owlfile
             queryConstructorService.addAxioms(axiomSet);
 
             idList.add(new Term(newowlclass.toStringID()));
         }
         
         return idList;
     }
 
 
 
     public ArrayList<Term> deleteTerm(String query) {
         OWLClassExpression exp = queryConstructorService.runManchesterQuery(query);
         ArrayList<Term> idList = getEquivalentTerms(exp);
 
         if(!exp.isAnonymous()){
             OWLClass owlClass = exp.asOWLClass();
 
             Set<OWLClassAxiom> owlClassAxiomSet = owlOntologyManager.getOntology(kbIRI).getAxioms(owlClass);
             Set<OWLAxiom> owlAxiomSet = new HashSet<OWLAxiom>(owlClassAxiomSet.size());
 
             for (OWLClassAxiom anOwlClassAxiomSet : owlClassAxiomSet) {
                 owlAxiomSet.add(anOwlClassAxiomSet);
             }
             RetractRequest retractRequest = new RetractRequest(kbIRI, owlAxiomSet);
             OK okResponse = (OK)executeReasoner(retractRequest);
 
             queryConstructorService.deleteAxioms(owlAxiomSet);
             idList.clear();
         }
         return idList;
     }
 
     public void startService() {
         logger.info("OWLlink server starting.");
         startPelletServer();
         setUpReasoner(serverUrl, serverPort);
        createKB();
     }
 
     private void startPelletServer(){
         PelletServerFactory pellet = new PelletServerFactory();
         server = pellet.createServer(Integer.parseInt(serverPort));
         server.run();
     }
 
     /**
      * setting up reasoner
      * @param serverUrl
      * @param serverPort
      */
     private void setUpReasoner(String serverUrl, String serverPort){
         OWLlinkHTTPXMLReasonerFactory factory = new OWLlinkHTTPXMLReasonerFactory();
         try{
             OWLlinkReasonerConfiguration reasonerConfiguration = new OWLlinkReasonerConfiguration(new URL(serverUrl+":"+serverPort));
             reasoner = factory.createNonBufferingReasoner(owlOntologyManager.createOntology(), reasonerConfiguration);
         }catch (OWLlinkReasonerIOException e){
             //restart service
             logger.info("OWLlink server is not running.");
             startService();
         } catch (MalformedURLException e) {
             e.printStackTrace();
         } catch (OWLOntologyCreationException e) {
             e.printStackTrace();
         }
     }
 
     /**
      *  Creating the knowledgebase and loading the ontologies from the owl document
      */
     private void createKB(){
         try {
             CreateKB createKBRequest = new CreateKB(kbIRI);
             KB kbResponse = reasoner.answer(createKBRequest);
             reasoner.answer(new LoadOntologies(kbResponse.getKB(),docIRI));
         } catch (OWLlinkErrorResponseException e) {
            e.printStackTrace();
         }
     }
 
 
     public void stopService() {
         if(server!=null) {
             try {
                 server.stop();
             } catch (InterruptedException e) {
                 e.printStackTrace();
             }
         }
     }
 
 }
