 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package wssemanticmatching;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Scanner;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.JAXBException;
 import javax.xml.bind.Marshaller;
 import org.ow2.easywsdl.schema.api.ComplexType;
 import org.ow2.easywsdl.schema.api.Element;
 import org.ow2.easywsdl.schema.api.Sequence;
 import org.ow2.easywsdl.schema.api.SimpleType;
 import org.ow2.easywsdl.schema.api.Type;
 import org.ow2.easywsdl.wsdl.WSDLFactory;
 import org.ow2.easywsdl.wsdl.api.BindingOperation;
 import org.ow2.easywsdl.wsdl.api.Description;
 import org.ow2.easywsdl.wsdl.api.Endpoint;
 import org.ow2.easywsdl.wsdl.api.Part;
 import org.ow2.easywsdl.wsdl.api.Service;
 import org.xml.sax.InputSource;
 
 /**
  *
  * @author victor, ashwini & alex
  */
 public class WSSemanticMatching {
 
     private static boolean editDistance;
 
     /**
      * @param args the command line arguments
      */
     public static void main(String[] args) throws FileNotFoundException, Exception {
         Scanner scanner = new Scanner(System.in);
 
         // getting all the WSDL files
        File folder = new File("src/WSDLs");
         File[] listOfFiles = folder.listFiles();
 
         System.out.println("The available WSDLs are:");
         for (int i = 0; i < listOfFiles.length; i++) {
 
             if (listOfFiles[i].isFile()) {
                 String file_name;
                 file_name = listOfFiles[i].getName();
                 System.out.println("    " + i + " - " + file_name);
             }
         }
         System.out.println();
         System.out.print("Choose the input web service: ");
 
         FileInputStream inputWs = new FileInputStream(listOfFiles[scanner.nextInt()]);
 
         System.out.print("Choose the output web service: ");
 
         FileInputStream outputWs = new FileInputStream(listOfFiles[scanner.nextInt()]);
 
         System.out.println();
         System.out.println("1 - : Use EditDistance");
         System.out.println("2 - : Use WordNet");
         System.out.print("Choose the method for calculating a matched element score: ");
 
         if (scanner.nextInt() == 1) {
             editDistance = true;
         } else {
             editDistance = false;
         }
 
         WSMatching wsmatching = new WSMatching();
         wsmatching.matchedWebServices = new ArrayList<>();
 
         MatchedWebService mws = new MatchedWebService();
         mws.matchedOperations = new ArrayList<>();
 
         Description d_i = WSDLFactory.newInstance().newWSDLReader().read(new InputSource(inputWs));
         Description d_o = WSDLFactory.newInstance().newWSDLReader().read(new InputSource(outputWs));
 
         Service s_i = null;
         for (Service s : d_i.getServices()) {
             s_i = s;
             mws.setInputServiceName(s_i.getQName().getLocalPart());
 
         }
         if (s_i == null) {
             throw new Exception("No services found on the input");
         }
 
         Service s_o = null;
         for (Service s : d_o.getServices()) {
             s_o = s;
 
             mws.setOutputServiceName(s_o.getQName().getLocalPart());
         }
 
         if (s_o == null) {
             throw new Exception("No services found on the output");
         }
 
         Endpoint p_i = null;
         for (Endpoint ep : s_i.getEndpoints()) {
             p_i = ep;
         }
 
         if (p_i == null) {
             throw new Exception("No ports found on the input");
         }
 
         Endpoint p_o = null;
         for (Endpoint ep : s_o.getEndpoints()) {
             p_o = ep;
         }
 
         if (p_o == null) {
             throw new Exception("No ports found on the output");
         }
 
         for (BindingOperation o_i : p_i.getBinding().getBindingOperations()) {
             for (BindingOperation o_o : p_o.getBinding().getBindingOperations()) {
                 MatchedOperation mo = new MatchedOperation();
                 mo.matchedElements = new ArrayList<>();
 
                 mo.setInputOperationName(o_i.getQName().getLocalPart());
                 mo.setOutputOperationName(o_o.getQName().getLocalPart());
 
                 //System.out.println("in" + mo.getInputOperationName() + "");
                 // System.out.println("out" + mo.getOutputOperationName());
 
                 // change input to output to see the same service score....
                 for (Part e_i : o_i.getOperation().getInput().getParts()) {
                     Element element = e_i.getElement();
                     Type type = e_i.getType();
 
                     ArrayList<Element> simpleElements = null;
                     if (type == null) {
                         System.out.println("type1" + type);
                         getAllSimpleTypedElements(element, simpleElements);
                     } else {
                         if (type instanceof SimpleType) {
                             //  System.out.println("type2"+ type);
                             matchWithOutputElements(o_o.getOperation().getOutput().getParts(), e_i.getPartQName().getLocalPart(), mo.matchedElements);
                         } else if (type instanceof ComplexType) {
                             // it's a complex type => must have a sequence
                             Sequence seq = ((ComplexType) type).getSequence();
                             //  System.out.println("seq"+ seq);
                             // though, some of them did not, probably a bug in the schema
                             if (seq != null) {
                                 for (Element el : seq.getElements()) {
                                     getAllSimpleTypedElements(el, simpleElements);
                                 }
                             }
                         }
                     }
 
                     if (simpleElements != null) {
                         for (Element el : simpleElements) {
                             matchWithOutputElements(o_o.getOperation().getOutput().getParts(), el.getQName().getLocalPart(), mo.matchedElements);
 
                         }
 
                     }
                 }
 
                 if (!mo.matchedElements.isEmpty()) {
 
                     mo.calculateOpScore();
                     // TODO uncomment the if
 //                    if (mo.getOpScore() > 0.8) {
                     mws.matchedOperations.add(mo);
 //                    }
                 }
             }
         }
 
         if (!mws.matchedOperations.isEmpty()) {
             mws.calculateWSScore();
             wsmatching.matchedWebServices.add(mws);
         }
         System.out.println(mws.getWsScore());
         createOutput(wsmatching);
 
         //testOutput();
     }
 
     private static void matchWithOutputElements(List<Part> parts, String element_name, ArrayList<MatchedElement> matchedElements) {
         for (Part e_o : parts) {
             Element element = e_o.getElement();
             Type type = e_o.getType();
 
             ArrayList<Element> simpleElements = null;
             if (type == null) {
                 getAllSimpleTypedElements(element, simpleElements);
             } else {
                 if (type instanceof SimpleType) {
                     addMatchedElements(element_name, e_o.getPartQName().getLocalPart(), matchedElements);
                 } else if (type instanceof ComplexType) {
                     // it's a complex type => must have a sequence
                     Sequence seq = ((ComplexType) type).getSequence();
                     // though, some of them did not, probably a bug in the schema
                     if (seq != null) {
                         for (Element el : seq.getElements()) {
                             getAllSimpleTypedElements(el, simpleElements);
                         }
                     }
                 }
             }
 
             if (simpleElements != null) {
                 for (Element el : simpleElements) {
                     addMatchedElements(element_name, el.getQName().getLocalPart(), matchedElements);
                 }
             }
         }
     }
 
     private static void addMatchedElements(String ei_name, String eo_name, ArrayList<MatchedElement> matchedElements) {
         MatchedElement me = new MatchedElement();
         me.setInputElement(ei_name);
         me.setOutputElement(eo_name);
         if (editDistance) {
             me.calculateScoreUsingEditDistance();
         } else {
             me.calculateScoreUsingWordNet();
         }
 
         matchedElements.add(me);
 
     }
 
     private static void createOutput(WSMatching wsm) {
         JAXBContext context;
         Marshaller m;
         try {
             context = JAXBContext.newInstance(WSMatching.class);
 
             m = context.createMarshaller();
             m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
 
             m.marshal(wsm, new File("src/output.xml"));
 
         } catch (JAXBException ex) {
             Logger.getLogger(WSSemanticMatching.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 
     private static void testOutput() {
 
         WSMatching wsm = new WSMatching();
         wsm.matchedWebServices = new ArrayList<>();
 
         MatchedWebService mws = new MatchedWebService();
         mws.matchedOperations = new ArrayList<>();
         mws.setInputServiceName("WS_A");
         mws.setOutputServiceName("WS_B");
 
         MatchedOperation mo = new MatchedOperation();
         mo.matchedElements = new ArrayList<>();
         mo.setInputOperationName("getWine");
         mo.setOutputOperationName("getDrink");
 
         MatchedElement me = new MatchedElement();
         me.setInputElement("Country");
         me.setOutputElement("Country");
         me.setScore(1.0);
         mo.matchedElements.add(me);
 
         me = new MatchedElement();
         me.setInputElement("Price");
         me.setOutputElement("Cost");
         me.setScore(0.9);
         mo.matchedElements.add(me);
 
 
         me = new MatchedElement();
         me.setInputElement("Region");
         me.setOutputElement("Area");
         me.setScore(0.85);
         mo.matchedElements.add(me);
 
         me = new MatchedElement();
         me.setInputElement("Color");
         me.setOutputElement("Colour");
         me.setScore(1.0);
         mo.matchedElements.add(me);
 
         mo.calculateOpScore();
 
         mws.matchedOperations.add(mo);
         mws.calculateWSScore();
         wsm.matchedWebServices.add(mws);
 
         createOutput(wsm);
     }
 
     private static void getAllSimpleTypedElements(Element el, ArrayList<Element> result) {
         if (result == null) {
             result = new ArrayList<>();
         }
         // System.out.println("result"+ result);
         Type type = el.getType();
         if (type instanceof SimpleType) {
             //  System.out.println("method:simple type"+ type);
             result.add(el);
             // System.out.println("final result"+ result);
         } else if (type instanceof ComplexType) {
             // System.out.println("method:type"+ type);
             // it's a complex type => must have a sequence
             Sequence seq = ((ComplexType) type).getSequence();
             // though, some of them did not, probably a bug in the schema
             if (seq != null) {
                 for (Element innerEl : seq.getElements()) {
                     getAllSimpleTypedElements(innerEl, result);
                 }
             }
         }
     }
 }
