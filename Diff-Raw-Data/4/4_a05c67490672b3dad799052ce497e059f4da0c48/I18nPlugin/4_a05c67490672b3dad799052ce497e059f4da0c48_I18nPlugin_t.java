 package fr.cg95.cvq.generator.plugins.i18n;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.log4j.Logger;
 import org.codehaus.groovy.control.CompilationFailedException;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 
 import fr.cg95.cvq.generator.ApplicationDocumentation;
 import fr.cg95.cvq.generator.ElementProperties;
 import fr.cg95.cvq.generator.IPluginGenerator;
 import fr.cg95.cvq.generator.UserDocumentation;
 import groovy.text.SimpleTemplateEngine;
 import groovy.text.Template;
 
 /**
  * @author rdj@zenexity.fr
  */
 public class I18nPlugin implements IPluginGenerator {
 
  private static Logger logger = Logger.getLogger(I18nPlugin.class);
     
     private int depth;
     
     private String outputDir;
     private String i18nTemplate;
     private String i18nTmpTemplate;
     
     private RequestI18n requestI18n;
     private List<ElementI18n> elementI18ns = new ArrayList<ElementI18n>();
     
    private ElementStack elementI18nStack;
     
     public void initialize(Node configurationNode) {
         logger.debug("initialize()");
         try {
             NamedNodeMap childAttributesMap = configurationNode.getFirstChild().getAttributes();
             
             outputDir = childAttributesMap.getNamedItem("outputdir").getNodeValue();
             i18nTemplate = childAttributesMap.getNamedItem("i18ntemplate").getNodeValue();
             i18nTmpTemplate = childAttributesMap.getNamedItem("i18ntmptemplate").getNodeValue();
         } catch (NullPointerException npe) {
             throw new RuntimeException ("Check i18n-plugin.xml " +
             		"<properties outputdir=\"\" i18ntemplate=\"\" i18ntmptemplate=\"\"/> configuration tag");
         }
     }
     
     public void startRequest(String requestName, String targetNamespace) {
         logger.debug("startRequest()");
         depth = 0;
         requestI18n = new RequestI18n(targetNamespace);
         elementI18nStack = new ElementStack();
        elementI18ns.clear();
     }
     
     public void endRequest(String requestName) {
         logger.warn("endRequest()");
         try {
             SimpleTemplateEngine templateEngine = new SimpleTemplateEngine();
             Template template = templateEngine.createTemplate(new File(i18nTemplate));
             Template template2 = templateEngine.createTemplate(new File(i18nTmpTemplate));
             
             for (String lang: requestI18n.getI18nLabels().keySet()) {
                 String output = outputDir + requestI18n.getAcronym();
                 String templateOutput = outputDir + requestI18n.getAcronym() + "customized";
                 if (!lang.equals("en")) {
                     output += "_" + lang;
                     templateOutput += "_" + lang;
                 }
                 output += ".properties";
                 templateOutput += ".properties";
                 
                 Map<String, Object> bindingMap = new HashMap<String, Object>();
                 bindingMap.put("lang", lang);
                 bindingMap.put("acronym", requestI18n.getAcronym());
                 bindingMap.put("requestI18n", requestI18n.getI18nLabels());
                 bindingMap.put("steps", requestI18n.getSteps());
                 bindingMap.put("elements", elementI18ns);
                 
                 template.make(bindingMap).writeTo(new FileWriter(output));
                 template2.make(bindingMap).writeTo(new FileWriter(templateOutput + ".tmp"));
             }
         } catch (CompilationFailedException cfe) {
             logger.error(cfe.getMessage()); 
         } catch (ClassNotFoundException cnfe) {
             logger.error(cnfe.getMessage()); 
         } catch (IOException ioe) {
             logger.error(ioe.getMessage()); 
         }
     }
     
     public void startElement(String elementName, String type) {
         logger.debug("endElement()");
         elementI18nStack.push(++depth, new ElementI18n(elementName, requestI18n.getAcronym()));
     }
     
     public void endElement(String elementName) {
         logger.debug("endElement()");        
         if (depth > 1)
             elementI18nStack.store(depth);
         
         depth--;
     }
 
     public void startElementProperties(ElementProperties elementProp) {
         logger.debug("startElementProperties()");
     }
     
     public void endElementProperties() {
         logger.debug("endElementProperties()");
     }
 
     public void onApplicationInformation(ApplicationDocumentation appDoc) {
         logger.debug("onApplicationInformation()");
         if (depth < 1)
             requestI18n.setSteps(appDoc.getRequestCommon().getSteps());
         else if (depth >= 1) {
             ElementI18n element = elementI18nStack.peek(depth);
             elementI18ns.add(element);
         }
     }
     
     public void onUserInformation(UserDocumentation userDoc) {
         logger.debug("onUserInformation()");
         if (depth == 0) {
             if (userDoc.getSourceUri().equals(IPluginGenerator.SHORT_DESC))
                 requestI18n.addI18nLabel(userDoc.getLang(), "short", userDoc.getText());
             if (userDoc.getSourceUri().equals(IPluginGenerator.LONG_DESC))
                 requestI18n.addI18nLabel(userDoc.getLang(), "long", userDoc.getText());
         }
         if (elementI18nStack.peek(depth) != null) {
             if (userDoc.getSourceUri().equals(IPluginGenerator.SHORT_DESC))
                 elementI18nStack.peek(depth).addi18nUserDocText(userDoc.getLang(), userDoc.getText());
             if (userDoc.getSourceUri().equals(IPluginGenerator.ENUM_TRANS))
                 elementI18nStack.peek(depth).addi18nUserDocEnums(userDoc.getLang(), userDoc.getXmlTranslationNodes());
         }
     }
     
     public void shutdown() { }
 }
