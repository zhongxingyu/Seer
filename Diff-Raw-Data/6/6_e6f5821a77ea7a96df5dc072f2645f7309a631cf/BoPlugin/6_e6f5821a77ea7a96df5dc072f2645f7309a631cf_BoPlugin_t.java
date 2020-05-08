 package fr.cg95.cvq.generator.plugins.bo;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.math.BigInteger;
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
 import fr.cg95.cvq.generator.common.RequestCommon;
 import groovy.lang.Writable;
 import groovy.text.SimpleTemplateEngine;
 import groovy.text.Template;
 
 /**
  * @author rdj@zenexity.fr
  */
 public class BoPlugin implements IPluginGenerator {
     
     private static Logger logger = Logger.getLogger(BoPlugin.class);
     
     private int depth;
     
     private String outputDir;
     private String groovyTemplate;
     
     // i18n 
     private String i18n0utputDir;
     private String i18nTemplate;
     private String i18nTmpTemplate;
     private List<ElementBo> i18nElements = new ArrayList<ElementBo>();
     
     private RequestBo requestBo;
     
     private ElementStack elementBoStack;
     
     
     public void initialize(Node configurationNode) {
         logger.debug("initialize()");
         try {
             NamedNodeMap childAttributesMap = configurationNode.getFirstChild().getAttributes();
             outputDir = childAttributesMap.getNamedItem("outputdir").getNodeValue();
             groovyTemplate = childAttributesMap.getNamedItem("groovytemplate").getNodeValue();
             
             // i18n
             i18n0utputDir = childAttributesMap.getNamedItem("i18noutputdir").getNodeValue();
             i18nTemplate = childAttributesMap.getNamedItem("i18ntemplate").getNodeValue();
             i18nTmpTemplate = childAttributesMap.getNamedItem("i18ntmptemplate").getNodeValue();
             
         } catch (NullPointerException npe) {
             throw new RuntimeException ("Check bo-plugin.xml <properties outputdir=\"\" groovytemplate=\"\"/> configuration tag");
         }
     }
     
     public void startRequest(String requestName, String targetNamespace) {
         logger.debug("startRequest()");
         depth = 0;
         requestBo = new RequestBo(requestName, targetNamespace);
         elementBoStack = new ElementStack();
     }
     
     public void endRequest(String requestName) {
         logger.warn("endRequest()");
         try {
             SimpleTemplateEngine templateEngine = new SimpleTemplateEngine();
             Template template = templateEngine.createTemplate(new File(groovyTemplate));
             
             Map<String, Object> bindingMap = new HashMap<String, Object>();
             bindingMap.put("requestBo", requestBo);
             Writable w = template.make(bindingMap);
             w.writeTo(new FileWriter(outputDir + "_" + requestBo.getName() + ".gsp"));
         } catch (CompilationFailedException cfe) {
             logger.error(cfe.getMessage()); 
         } catch (ClassNotFoundException cnfe) {
             logger.error(cnfe.getMessage()); 
         } catch (IOException ioe) {
             logger.error(ioe.getMessage()); 
         }
         
         // i18n
         try {
             SimpleTemplateEngine templateEngine = new SimpleTemplateEngine();
             Template template = templateEngine.createTemplate(new File(i18nTemplate));
             Template template2 = templateEngine.createTemplate(new File(i18nTmpTemplate));
             
             for (String lang: requestBo.getI18nLabels().keySet()) {
                 String output = i18n0utputDir + requestBo.getAcronym();
                 String templateOutput = i18n0utputDir + requestBo.getAcronym() + "customized";
                 if (!lang.equals("en")) {
                     output += "_" + lang;
                     templateOutput += "_" + lang;
                 }
                 output += ".properties";
                 templateOutput += ".properties";
                 
                 Map<String, Object> bindingMap = new HashMap<String, Object>();
                 bindingMap.put("lang", lang);
                 bindingMap.put("acronym", requestBo.getAcronym());
                 bindingMap.put("requestI18n", requestBo.getI18nLabels());
                 bindingMap.put("steps", requestBo.getSteps());
                 bindingMap.put("elements", i18nElements);
                 
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
         elementBoStack.push(++depth, new ElementBo(elementName, this.requestBo.getAcronym()));
     }
     
     public void endElement(String elementName) {
         logger.debug("endElement()");
        
         if (depth <= 1 && elementBoStack.peek(depth).isDisplay())
             requestBo.addElement(elementBoStack.pop(depth));
         else
             elementBoStack.store(depth);
         
         depth--;
     }
 
     public void startElementProperties(ElementProperties elementProp) {
         logger.debug("startElementProperties()");
         ElementBo elementBo = elementBoStack.peek(depth);
         elementBo.setType(elementProp.getXmlSchemaType());
         
        // TODO - define a more robust namespace mapping policy
        if (elementProp.isReferentialType())
            elementBo.setModelNamespace(IPluginGenerator.MODEL_BASE_TARGET_NS + ".users");
        
         if (elementProp.isSimpleType())
             elementBo.setTypeClass(ElementBo.ElementTypeClass.SIMPLE);
         else if (elementProp.isComplexType())
             elementBo.setTypeClass(ElementBo.ElementTypeClass.COMPLEX);
        
         if (elementProp.getMaxOccurs() == null
                 || elementProp.getMaxOccurs().compareTo(BigInteger.valueOf(1)) == 1)
             elementBo.setTypeClass(ElementBo.ElementTypeClass.COLLECTION);
         
         if (elementProp.getMinOccurs().compareTo(BigInteger.valueOf(0)) == 0)
             elementBo.setMandatory(false);
         
         if (elementProp.getEnumValues() != null)
             elementBo.setWidget("capdematEnum");
         else
             elementBo.setWidget(elementProp.getXmlSchemaType());
     }
     
     public void endElementProperties() {
         logger.debug("endElementProperties()");
     }
 
     public void onApplicationInformation(ApplicationDocumentation appDoc) {
         logger.debug("onApplicationInformation()");
         if (depth < 1) {
             requestBo.setSteps(appDoc.getRequestCommon().getSteps());
             requestBo.setConditions(appDoc.getRequestCommon().getConditions());
         } else if (depth >= 1) {
             ElementBo elementBo = elementBoStack.peek(depth);
             elementBo.setStep(appDoc.getRequestCommon().getCurrentElementCommon().getStep());
             elementBo.setConditions(appDoc.getRequestCommon().getCurrentElementCommon().getConditions());
             elementBo.setModelNamespace(RequestCommon.MODEL_REQUEST_NS
                     + "." + appDoc.getRequestCommon().getNamespace());
             elementBo.setDisplay(true);
             
             // i18n
             i18nElements.add(elementBo);
             
             if (appDoc.getNodeName().equals("bo")) {
                 Map<String,String> attributes = new HashMap<String,String>();
                 Node node = appDoc.getXmlNode();
                 attributes.put("column", 
                         ApplicationDocumentation.getNodeAttributeValue(node, "column"));
                 attributes.put("after", 
                         ApplicationDocumentation.getNodeAttributeValue(node, "after"));
                 attributes.put("jsregexp", 
                         ApplicationDocumentation.getNodeAttributeValue(node, "jsregexp"));
                 
                 elementBo.setColumn(attributes.get("column"));
                 elementBo.setAfter(attributes.get("after"));
                 elementBo.setJsRegexp(attributes.get("jsregexp"));
             }
          }
     }
     
     public void onUserInformation(UserDocumentation userDoc) {
         logger.debug("onUserInformation()");
         
         if (depth == 0) {
             if (userDoc.getSourceUri().equals(IPluginGenerator.SHORT_DESC))
                 requestBo.addI18nLabel(userDoc.getLang(), "short", userDoc.getText());
             if (userDoc.getSourceUri().equals(IPluginGenerator.LONG_DESC))
                 requestBo.addI18nLabel(userDoc.getLang(), "long", userDoc.getText());
         }
         if (elementBoStack.peek(depth) != null) {
             if (userDoc.getSourceUri().equals(IPluginGenerator.SHORT_DESC))
                 elementBoStack.peek(depth).addi18nUserDocText(userDoc.getLang(), userDoc.getText());
             if (userDoc.getSourceUri().equals(IPluginGenerator.ENUM_TRANS))
                 elementBoStack.peek(depth).addi18nUserDocEnums(userDoc.getLang(), userDoc.getXmlTranslationNodes());
         }
             
     }
     
     public void shutdown() { }
 
 }
