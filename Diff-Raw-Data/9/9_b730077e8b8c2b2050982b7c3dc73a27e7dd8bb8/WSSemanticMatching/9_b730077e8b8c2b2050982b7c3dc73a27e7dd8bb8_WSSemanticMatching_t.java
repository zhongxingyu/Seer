 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package wssemanticmatching;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.util.ArrayList;
 import java.util.Scanner;
 import org.ow2.easywsdl.wsdl.WSDLFactory;
 import org.ow2.easywsdl.wsdl.api.BindingOperation;
 import org.ow2.easywsdl.wsdl.api.Description;
 import org.ow2.easywsdl.wsdl.api.Endpoint;
 import org.ow2.easywsdl.wsdl.api.Part;
 import org.ow2.easywsdl.wsdl.api.Service;
 import org.xml.sax.InputSource;
 import wssemanticmatching.ontology.OntologyMatcher;
 
 /**
  *
  * @author victor, ashwini & alex
  */
 public class WSSemanticMatching {
 
     public static void testOntologyMatcher() {
         OntologyMatcher ontologyMatcher = OntologyMatcher.getInstance();
         System.out.println(ontologyMatcher.getScore("Anglican", "Christian"));
     }
 
     /**
      * @param args the command line arguments
      */
     public static void main(String[] args) throws FileNotFoundException, Exception {
        //testOntologyMatcher();
 
         ArrayList<String> iElements = new ArrayList<>();
         ArrayList<String> oElements = new ArrayList<>();
 
         Scanner scanner = new Scanner(System.in);
 
         // getting all the WSDL files
         File folder = new File("src/SAWSDL");
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
 
                 System.out.println("inputOperation:" + mo.getInputOperationName() + "");
                 System.out.println("outputOperation:" + mo.getOutputOperationName());
 
                 // change input to output to see the same service score....
                 for (Part e_i : o_i.getOperation().getInput().getParts()) {
 
                     System.out.println("InputElement: " + e_i.getPartQName().getLocalPart());
                     String input_element = e_i.getPartQName().getLocalPart();
 
                     for (Part e_o : o_o.getOperation().getOutput().getParts()) {
                         System.out.println("OutputElement " + e_o.getPartQName().getLocalPart());
                         String output_element = e_o.getPartQName().getLocalPart();
 
                         OntologyMatcher ontologyMatcher = OntologyMatcher.getInstance();
                         System.out.println(ontologyMatcher.getScore(input_element, output_element));
                     }
 
                 }
 
 
             }
         }
     }
 }
