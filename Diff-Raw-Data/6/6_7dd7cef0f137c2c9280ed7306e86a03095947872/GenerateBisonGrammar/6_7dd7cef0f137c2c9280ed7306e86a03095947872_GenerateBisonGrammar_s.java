 package de.hszg.atocc.vcc.bison.internal;
 
 import de.hszg.atocc.core.util.AbstractXmlTransformationService;
 import de.hszg.atocc.core.util.CollectionHelper;
 import de.hszg.atocc.core.util.XmlTransformationException;
 import de.hszg.atocc.core.util.XmlUtilService;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.xml.xpath.XPathExpressionException;
 
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 public class GenerateBisonGrammar extends AbstractXmlTransformationService {
 
     private StringBuilder bisonInput;
     
     private XmlUtilService xmlUtils;
     
     @Override
     protected void transform() throws XmlTransformationException {
         // TODO validateInput("COMPILER");
         
         try {
             initialize();
         } catch (XPathExpressionException e) {
             throw new XmlTransformationException("Bison|ERROR", e);
         }
         
         createTokens();
         createRules();
         
         setOutput("bison", bisonInput.toString());
     }
 
     private void initialize() throws XPathExpressionException {
         final String language = xmlUtils.extractString(getInput(), "//CODETYPE/@value");
         
         bisonInput = new StringBuilder();
         bisonInput.append("%language \"" + language + "\"\n");
         bisonInput.append("%define stype \"String\"\n\n");
         
         bisonInput.append("%code imports {");
         NodeList importElements = getInput().getElementsByTagName("IMPORTS");
         
         if(importElements.getLength() > 0) {
             Element imports = (Element) importElements.item(0);
             bisonInput.append(imports.getTextContent().trim() + "\n");
         }
         bisonInput.append("}\n\n");
        
         bisonInput.append("%code {\n");
         Element globalCode = (Element) getInput().getElementsByTagName("GLOBALCODE").item(0);
         bisonInput.append(globalCode.getTextContent().trim());
         bisonInput.append("}\n\n");
     }
     
     private void createTokens() {
         final NodeList readElements = getInput().getElementsByTagName("READ");
         
         for(int i = 0; i < readElements.getLength(); ++i) {
             final Element readElement = (Element) readElements.item(i);
             final String token = readElement.getAttribute("token");
             
             if(!"IGNORE".equals(token)) {
                 bisonInput.append("%token " + token + "\n");
             }
         }
     }
     
     private void createRules() {
         bisonInput.append("\n%%\n");
         
         final Element rulesElement = (Element) getInput().getElementsByTagName("RULES").item(0);
         
         final NodeList childNodes = rulesElement.getChildNodes();
         
         for(int i = 0; i < childNodes.getLength(); ++i) {
             final Node child = childNodes.item(i);
             
             if(child.getNodeType() != Node.ELEMENT_NODE) continue;
             
             final Element ruleElement = (Element) child;
             createRuleFor(ruleElement);
         }
     }
 
     private void createRuleFor(Element ruleElement) {
         final String name = ruleElement.getAttribute("name");
         final List<String> rightSides = new ArrayList<>();
         
         bisonInput.append(String.format("%s: ", name));
         
         final NodeList childNodes = ruleElement.getChildNodes();
         
         for(int i = 0; i < childNodes.getLength(); ++i) {
             final Node child = childNodes.item(i);
             
             if(child.getNodeType() != Node.ELEMENT_NODE) continue;
             
             final Element rightSideElement = (Element) child;
             createRightSideFor(rightSideElement, rightSides);
         }
         
         bisonInput.append(String.format("%s;\n", CollectionHelper.makeString(rightSides, " | ")));
     }
 
     private void createRightSideFor(Element rightSideElement, List<String> rhs) {
         final NodeList childNodes = rightSideElement.getChildNodes();
         
         final List<String> ruleParts = new ArrayList<>();
         final List<String> actionParts = new ArrayList<>();
         
         int currentPart = 0;
         
         for(int i = 0; i < childNodes.getLength(); ++i) {
             final Node child = childNodes.item(i);
             
             if(child.getNodeType() != Node.ELEMENT_NODE) continue;
             
             currentPart++;
             
             final Element ruleElement = (Element) child;
             ruleParts.add(ruleElement.getAttribute("name"));
             actionParts.add(String.format("$%d", currentPart));
         }
         
         final String rule = CollectionHelper.makeString(ruleParts, " ");
         
         String code = "";
         
         NodeList codeElements = rightSideElement.getElementsByTagName("CODE");
         
         if(codeElements.getLength() == 0) {
             code = String.format("$$ = %s;", CollectionHelper.makeString(actionParts, " + "));
         } else {
             final Element codeElement = (Element) codeElements.item(0);
             code = codeElement.getTextContent().trim();
         }
         
         final String action = String.format("{\n%s\n}", code);
         
         rhs.add(String.format("%s %s", rule, action));
     }
 
 }
