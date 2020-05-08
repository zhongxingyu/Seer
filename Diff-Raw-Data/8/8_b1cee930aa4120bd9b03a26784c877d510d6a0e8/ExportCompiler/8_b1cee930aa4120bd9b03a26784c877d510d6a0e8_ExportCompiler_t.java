 package de.hszg.atocc.autoedit.export.compiler.internal;
 
 import de.hszg.atocc.core.util.AbstractXmlTransformationService;
 import de.hszg.atocc.core.util.CharacterHelper;
 import de.hszg.atocc.core.util.GrammarService;
 import de.hszg.atocc.core.util.SerializationException;
 import de.hszg.atocc.core.util.XmlTransformationException;
 import de.hszg.atocc.core.util.XmlUtilService;
 import de.hszg.atocc.core.util.grammar.Grammar;
 
 import java.util.Set;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
 public class ExportCompiler extends AbstractXmlTransformationService {
 
     private XmlUtilService xmlUtils;
     private GrammarService grammarUtils;
 
     private String language = "Java";
 
     private Document vccDocument;
     private Element vccElement;
 
     private Grammar grammar;
     private Set<String> terminals;
 //    private Set<String> nonTerminals;
 
     // @Post
     // public Document process(String grammar) {
     // final Document grammarDocument = xmlUtils.createEmptyDocument();
     //
     // final Element grammarElement = grammarDocument.createElement("grammar");
     // grammarElement.setTextContent(grammar);
     // grammarDocument.appendChild(grammarElement);
     //
     // return process(grammarDocument);
     // }
 
     @Override
     protected void transform() throws XmlTransformationException {
         tryToGetRequiredServices();
         // TODO validateInput("GRAMMAR");
 
         try {
             grammar = grammarUtils.grammarFrom(getInput());
             terminals = grammar.findTerminals();
             // nonTerminals = grammar.findNonTerminals();
         } catch (SerializationException e) {
             throw new XmlTransformationException("ExportCompiler|INVALID_INPUT", e);
         }
 
         createOutputDocument();
         setOutput(vccDocument);
         // TODO validateOutput("COMPILER");
     }
 
     private void tryToGetRequiredServices() {
         xmlUtils = getService(XmlUtilService.class);
         grammarUtils = getService(GrammarService.class);
     }
 
     private void createOutputDocument() {
         vccDocument = xmlUtils.createEmptyDocument();
 
         vccElement = vccDocument.createElement("VCC");
         vccDocument.appendChild(vccElement);
         createCodeTypeElement();
        createParserTypeElement();
         createScannerElement();
         createGlobalCodeElement();
         createRulesElement();
     }
 
     private void createRulesElement() {
         final Element rulesElement = vccDocument.createElement("RULES");
         vccElement.appendChild(rulesElement);
 
         for (String lhs : grammar.getLeftHandSides()) {
             final Element ruleElement = vccDocument.createElement("RULE");
             ruleElement.setAttribute("name", lhs);
             ruleElement.setAttribute("color", "#FFE1FF");
             rulesElement.appendChild(ruleElement);
 
             for (String rhs : grammar.getRightHandSidesFor(lhs)) {
                 final Element rightSideElement = vccDocument.createElement("RIGHTSIDE");
                 ruleElement.appendChild(rightSideElement);
 
                 for (String part : rhs.split(" ")) {
                     if(terminals.contains(part.trim())) {
                         final Element tokenElement = vccDocument.createElement("TOKEN");
                         tokenElement.setAttribute("name", generateTokenNameFor(part.trim()));
                         rightSideElement.appendChild(tokenElement);
                     } else {
                         final Element innerRuleElement = vccDocument.createElement("RULE");
                         innerRuleElement.setAttribute("name", part.trim());
                         rightSideElement.appendChild(innerRuleElement);
                     }
                 }
             }
         }
     }
 
     private void createGlobalCodeElement() {
         final Element globalCodeElement = vccDocument.createElement("GLOBALCODE");
         vccElement.appendChild(globalCodeElement);
     }
 
     private void createScannerElement() {
         final Element scannerElement = vccDocument.createElement("SCANNER");
         vccElement.appendChild(scannerElement);
 
         for (String terminal : terminals) {
             final Element readElement = vccDocument.createElement("READ");
             readElement.setAttribute("value", generateTokenValueFor(terminal));
             readElement.setAttribute("token", generateTokenNameFor(terminal));
             readElement.setAttribute("color", "#E1FFFF");
             scannerElement.appendChild(readElement);
         }
 
         final Element readElement = vccDocument.createElement("READ");
         readElement.setAttribute("value", "[\\r\\n\\t\\s]");
         readElement.setAttribute("token", "IGNORE");
         readElement.setAttribute("color", "#E1FFFF");
         scannerElement.appendChild(readElement);
     }
 
     private void createCodeTypeElement() {
         final Element codeTypeElement = vccDocument.createElement("CODETYPE");
         codeTypeElement.setAttribute("value", language);
         vccElement.appendChild(codeTypeElement);
     }
     
    private void createParserTypeElement() {
        final Element parserTypeElement = vccDocument.createElement("PARSERTYPE");
        parserTypeElement.setAttribute("value", "LALR(1)");
        vccElement.appendChild(parserTypeElement);
    }
    
     private String generateTokenValueFor(String value) {
 //        if (value.length() == 1) {
 //            if (CharacterHelper.isSpecialChar(value.charAt(0))) {
 //                return String.format("\\%s", value);
 //            }
 //        }
 
         return value;
     }
 
     private String generateTokenNameFor(String value) {
         if (value.length() == 1) {
             if (CharacterHelper.isLetter(value.charAt(0))) {
                 return value;
             }
 
             if (CharacterHelper.isSpecialChar(value.charAt(0))) {
                 return String.format("Char%d", (int)value.charAt(0));
             }
         }
 
         return String.format("T_%s", value);
     }
 }
