 package ArgumentStructure;
 
 import org.w3c.dom.Attr;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 
 /**
  * User: Mark Hinshaw
  * Email: mahinshaw@gmail.com
  * Date: 8/6/13
  * github: https://github.com/mahinshaw/msproject
  * <p/>
  * This class handles writitng xml documents based upon the Argument Tree.
  */
 public class XMLWriter {
 
 
     public void writeXML(ArrayList<ArgumentTree> trees, String q) {
         try {
             DocumentBuilderFactory docFac = DocumentBuilderFactory.newInstance();
             DocumentBuilder builder = docFac.newDocumentBuilder();
 
             Document document = builder.newDocument();
             Element session = document.createElement("Session");
             //set root element
             document.appendChild(session);
 
             // append the question to the session
             Element question = document.createElement("Question");
             session.appendChild(question);
             if (q != null) {
                 question.appendChild(document.createTextNode(q));
             } else {
                 question.appendChild(document.createTextNode("No question was passed."));
             }
 
             for (ArgumentTree tree : trees) {
                 System.out.println("Tree1 " + tree.getRoot().getARGID());
 
                 // append argument
                 Element argument = document.createElement("Argument");
                 Attr argIndex = document.createAttribute("arg");
                 argument.setAttributeNode(argIndex);
                 argIndex.setValue(Integer.toString(tree.getRoot().getARGID()));
                 session.appendChild(argument);
 
                 // print arguments in each tree.
                 addArgument(document, argument, tree);
             }
 
             TransformerFactory tFactory = TransformerFactory.newInstance();
             Transformer transformer = tFactory.newTransformer();
             DOMSource source = new DOMSource(document);
             StreamResult result = new StreamResult("src/XMLOutput/session_" + getTimeStamp() + ".xml");
 
             transformer.transform(source, result);
         } catch (Exception e) {
             e.printStackTrace();
         }
 
     }
 
     /**
      * This method prints the agrument of the specified tree.
      *
      * @param document - the document to be printed to.
      * @param element  - The element to append to.
      * @param tree     - The tree (Single Argument/Node) to be printed
      */
     private void addArgument(Document document, Element element, ArgumentTree tree) {
         //check and see if there is a conjunction
         if (tree.getRoot().getIsConjunction()) {
             Element conjunction = document.createElement("Conjunction");
             //conjunctions should have a left and right child, so add them
             if (tree.hasLeftChild())
                addArgument(document, conjunction, tree.getLeftChild());
             if (tree.hasRightChild())
                addArgument(document, conjunction, tree.getRightChild());
             element.appendChild(conjunction);
 
         }
         else {
             //append the hypothesis to element
             Element hypothesis = document.createElement("Hypothesis");
             Element hNode = document.createElement("Node");
             Element hText = document.createElement("Text");
             hNode.appendChild(document.createTextNode("Node " + tree.getRoot().getHypothesis().getKBNODEID()));
             hText.appendChild(document.createTextNode(tree.getRoot().getHypothesis().getTEXT()));
             hypothesis.appendChild(hNode);
             hypothesis.appendChild(hText);
             element.appendChild(hypothesis);
 
             // append the generalizations to the element
             for (Generalization g : tree.getRoot().getGeneralizations()) {
                 Element generalization = document.createElement("Generalization");
                 Element gArc = document.createElement("Arc");
                 Element gText = document.createElement("Text");
                 gArc.appendChild(document.createTextNode("Arc " + g.getKBARCID()));
                 gText.appendChild(document.createTextNode(g.getTEXT()));
                 generalization.appendChild(gArc);
                 generalization.appendChild(gText);
                 element.appendChild(generalization);
             }
 
             // append the data to the element
             Element data = document.createElement("Data");
             // if the tree has children recurse in the data node with subargument tags
             if (tree.hasLeftChild() || tree.hasRightChild()) {
                 // left child
                 if (tree.hasLeftChild()) {
                     // Element subArg1 = document.createElement("SubArgument");
                     addArgument(document, data, tree.getLeftChild());
                     // data.appendChild(subArg1);
                 }
                 if (tree.hasRightChild()) {
                     // right child
                     // Element subArg2 = document.createElement("SubArgument");
                     addArgument(document, data, tree.getRightChild());
                     // data.appendChild(subArg2);
                 }
                 // element.appendChild(data);
             }
             //if no children print the data
             else if (!tree.hasLeftChild() && !tree.hasRightChild()) {
                 Element dNode = document.createElement("Node");
                 Element dText = document.createElement("Text");
                 dNode.appendChild(document.createTextNode("Node " + tree.getRoot().getDatum().getKBNODEID()));
                 dText.appendChild(document.createTextNode(tree.getRoot().getDatum().getTEXT()));
                 data.appendChild(dNode);
                 data.appendChild(dText);
             }
 
             //append the data node
             element.appendChild(data);
         }
 
     }
 
     /**
      * This method was reused from the GAIL src code.
      *
      * @return - A timestamp that signifies the individual xml file.
      */
     private String getTimeStamp() {
         Calendar ca = Calendar.getInstance();
         String hour = "" + ca.get(Calendar.HOUR_OF_DAY);
         String minute = "" + ca.get(Calendar.MINUTE);
         String second = "" + ca.get(Calendar.SECOND);
         if (hour.length() < 2) {
             hour = 0 + hour;
         }
         if (minute.length() < 2) {
             minute = 0 + minute;
         }
         if (second.length() < 2) {
             second = 0 + second;
         }
         return new SimpleDateFormat("MMddyy").format(new Date()) + "_" + hour
                 + "" + minute + "" + second;
     }
 }
