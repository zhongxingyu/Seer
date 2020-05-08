 package de.hszg.atocc.autoedit.export.compiler.internal;
 
 import de.hszg.atocc.core.util.WebServiceResultStatus;
 import de.hszg.atocc.core.util.XmlUtilsException;
 import de.hszg.atocc.core.util.test.AbstractTestHelper;
 
 import javax.xml.transform.TransformerException;
 
 import org.junit.Assert;
 import org.junit.Test;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 
 public final class ExportCompilerTests extends AbstractTestHelper {
 
     @Test
     public void testMLGrammarToCompiler() throws XmlUtilsException, TransformerException {
         final Document grammarDocument = getXmlService().documentFromFile("ML.xml");
 
         final Document resultDocument = getWebUtils().post(
                 "http://localhost:8081/autoedit/export/compiler", grammarDocument);
 
         Assert.assertEquals(WebServiceResultStatus.SUCCESS,
                 getXmlService().getResultStatus(resultDocument));
 
         final Document compilerDocument = getXmlService().getContent(resultDocument);
 
         final NodeList codeTypeNodes = compilerDocument.getElementsByTagName("CODETYPE");
         Assert.assertEquals(1, codeTypeNodes.getLength());
         
         final Element codeTypeElement = (Element) codeTypeNodes.item(0);
         Assert.assertEquals("Java", codeTypeElement.getAttribute("value"));
         
         final NodeList parserTypeNodes = compilerDocument.getElementsByTagName("PARSERTYPE");
        Assert.assertEquals("1", parserTypeNodes.getLength());
         
         final Element parserTypeElement = (Element) parserTypeNodes.item(0);
         Assert.assertEquals("LALR(1)", parserTypeElement.getAttribute("value"));
         
         // TODO SCANNER and READ elements
         final NodeList scannerNodes = compilerDocument.getElementsByTagName("SCANNER");
        Assert.assertEquals("1", scannerNodes.getLength());
         
         final Element scannerElement = (Element) scannerNodes.item(0);
         
         final NodeList readNodes = scannerElement.getElementsByTagName("READ");
         
         Assert.assertEquals(18, readNodes.getLength());
         
         // TODO GLOBALCODE
         final NodeList globalCodeNodes = compilerDocument.getElementsByTagName("GLOBALCODE");
        Assert.assertEquals("1", globalCodeNodes.getLength());
         
         final Element globalCodeElement = (Element) globalCodeNodes.item(0);
         Assert.assertEquals("", globalCodeElement.getTextContent().trim());
         
         // TODO RULES
         //final NodeList codeTypeNodes = compilerDocument.getElementsByTagName("CODETYPE");
     }
 }
