 package de.hszg.atocc.kfgedit.transform.internal;
 
 import de.hszg.atocc.core.util.AbstractXmlTransformationService;
 import de.hszg.atocc.core.util.SerializationException;
 import de.hszg.atocc.core.util.WebServiceResultStatus;
 import de.hszg.atocc.core.util.WebUtilService;
 import de.hszg.atocc.core.util.XmlTransformationException;
 import de.hszg.atocc.core.util.XmlUtilService;
 import de.hszg.atocc.core.util.XmlValidationException;
 import de.hszg.atocc.core.util.grammar.Grammar;
 import de.hszg.atocc.core.util.grammar.GrammarService;
 
 import org.w3c.dom.Document;
 
 public class TransformToCnf extends AbstractXmlTransformationService {
 
     private GrammarService grammarService;
     private WebUtilService webUtilService;
     private XmlUtilService xmlService;
     private Grammar grammar;
 
     @Override
     protected void transform() throws XmlTransformationException {
         tryToGetRequiredServices();
 
         try {
             validateInput("GRAMMAR");
 
             grammar = grammarService.grammarFrom(getInput());
 
             step1();
             step2();
             step3();
 
             setOutput(grammarService.grammarToXml(grammar));
         } catch (XmlValidationException e) {
             throw new XmlTransformationException("TransformToCNF|INVALID_INPUT", e);
         } catch (SerializationException e) {
             throw new XmlTransformationException("TransformToCNF|SERVICE_ERROR", e);
         }
 
         try {
             validateOutput("GRAMMAR");
         } catch (XmlValidationException e) {
             throw new XmlTransformationException("TransformToCNF|INVALID_OUTPUT", e);
         }
     }
 
     private void step1() throws SerializationException {
         optimizeGrammar();
     }
     
     private void step2() {
         
     }
     
     private void step3() {
         
     }
 
     private void optimizeGrammar() throws SerializationException {
         removeEpsilonRules();
         removeUnneededNonterminals();
        removeChainRules();
        removeUnneededNonterminals();
     }
 
     private void removeEpsilonRules() throws SerializationException {
         transformGrammar("removeEpsilonRules");
     }
 
     private void removeChainRules() throws SerializationException {
         transformGrammar("removeUnneededChainRules");
     }
 
     private void removeUnneededNonterminals() throws SerializationException {
         transformGrammar("removeUnneededNonterminals");
     }
 
     private void transformGrammar(String transformationServiceUrl) throws SerializationException {
         final Document grammarDocument = grammarService.grammarToXml(grammar);
 
         final Document resultDocument = webUtilService.post(getServiceUrl("kfgedit/transform/"
                 + transformationServiceUrl), grammarDocument);
 
         if (xmlService.getResultStatus(resultDocument) != WebServiceResultStatus.SUCCESS) {
             throw new RuntimeException(xmlService.getErrorMessage(resultDocument));
         }
 
         grammar = grammarService.grammarFrom(xmlService.getContent(resultDocument));
     }
 
     private void tryToGetRequiredServices() {
         grammarService = getService(GrammarService.class);
         webUtilService = getService(WebUtilService.class);
         xmlService = getService(XmlUtilService.class);
     }
 
 }
