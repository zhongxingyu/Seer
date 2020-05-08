 package com.onedatapoint;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Window;
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.InputSource;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.InputStreamReader;
 import java.util.Vector;
 
 public class CuringDepressionActivity extends Activity
 {
     private final static String LOGTAG = "onedatapoint";
     private final static String questionFileLocation = "/sdcard/onedatapoint-questions.xml";
 
     // Holder for XML data used to trigger creation of Questions and Views.
     private class Question {
         public String type = "";
         public String description = "";
         public String xLabel = "";
         public String yLabel = "";
 
         @Override
         public String toString() {
             return "Type: " + type + "; Description: " + description + "; xLabel: " + xLabel + "; yLabel: " + yLabel;
         }
     }
 
     private Vector<Question> questions;
 
     @Override
     public void onCreate(Bundle savedInstanceState)
     {
         requestWindowFeature(Window.FEATURE_NO_TITLE);
         super.onCreate(savedInstanceState);
 
         questions = new Vector<Question>();
         loadQuestions(questionFileLocation);
         showQuestions();
     }
 
     private boolean loadQuestions(String questionFileLocation) {
         Log.v(LOGTAG, "loadQuestions");
         try {
             File myFile = new File(questionFileLocation);
			FileInputStream fIn = new FileInputStream(myFile);
			BufferedReader myReader = new BufferedReader(new InputStreamReader(fIn));
             DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
             DocumentBuilder db = dbf.newDocumentBuilder();
             Document doc = db.parse(new InputSource(myReader));
             doc.getDocumentElement().normalize();
             stripSpace(doc);
             NodeList nodeList = doc.getElementsByTagName("question");
             Log.v(LOGTAG, nodeList.toString());
             for (int i = 0; i < nodeList.getLength(); ++i) {
                 Question question = new Question();
                 Node node = nodeList.item(i);
                 NodeList children = node.getChildNodes();
 
                 for (int j = 0; j < children.getLength(); ++j) {
                     Node child = children.item(j);
                     if (child.getNodeName().equals("type"))
                         question.type = child.getFirstChild().getNodeValue();
                     else if (child.getNodeName().equals("description"))
                         question.description = child.getFirstChild().getNodeValue();
                     else if (child.getNodeName().equals("xLabel"))
                         question.xLabel = child.getFirstChild().getNodeValue();
                     else if (child.getNodeName().equals("yLabel"))
                         question.yLabel = child.getFirstChild().getNodeValue();
                 }
                 Log.v(LOGTAG, "Question #: " + i + " " + question);
                 questions.add(question);
             }
         } catch (Exception e) {
             e.printStackTrace();
             return false;
         }
         return true;
     }
 
     private static void stripSpace(Node node){
         Node child = node.getFirstChild();
         while(child!=null){
             // save the sibling of the node that will
             // perhaps be removed and set to null
             Node c = child.getNextSibling();
             if((child.getNodeType()==Node.TEXT_NODE &&
                     child.getNodeValue().trim().length()==0) ||
                     ((child.getNodeType()!=Node.TEXT_NODE)&&
                             (child.getNodeType()!=Node.ELEMENT_NODE)))
                 node.removeChild(child);
             else // process children recursively
                 stripSpace(child);
             child=c;
         }
     }
 
     private void showQuestions() {
         setContentView(R.layout.main);
     }
 }
