 package com.onedatapoint;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.View;
 import android.view.Window;
 import android.widget.TextView;
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
     private Vector<View> questionViews;
     private boolean canExit = true;
 
     @Override
     public void onCreate(Bundle savedInstanceState)
     {
         requestWindowFeature(Window.FEATURE_NO_TITLE);
         super.onCreate(savedInstanceState);
 
         questions = new Vector<Question>();
         questionViews = new Vector<View>();
 
         loadQuestions(questionFileLocation);
         createQuestionViews();
 
         setContentView(R.layout.home);
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
 
     private static void stripSpace(Node node) {
         Node child = node.getFirstChild();
         while(child!=null) {
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
 
     // Home screen button onClick handlers
     public void openJournal(View v) {
         canExit = false;
         setContentView(R.layout.journal);
     }
     public void openReview(View v) {
         canExit = false;
         setContentView(R.layout.review);
     }
     public void openMedicine(View v) {
         canExit = false;
         setContentView(R.layout.medicine);
     }
     public void openGraphs(View v) {
         canExit = false;
         setContentView(R.layout.graphs);
     }
 
     public void saveAndGoHome(View v) {
         canExit = true;
         setContentView(R.layout.home);
     }
 
     public void saveAndQuit(View v) {
         moveTaskToBack(true);
     }
 
     @Override
     public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (canExit) {
                moveTaskToBack(true);
                return true;
            }

             canExit = true;
             setContentView(R.layout.home);
             return true;
         }
         return super.onKeyDown(keyCode, event);
     }
 
     private void createQuestionViews() {
         for (Question question : questions) {
             View questionView = new View(this);
             // Create respective View object
             if (question.type.equals("graph"))
                 // Instantiate View
                 break;
             else if (question.type.equals("slider"))
                 // Instantiate View
                 break;
             else if (question.type.equals("buttons"))
                 // Instantiate View
                 break;
             questionViews.add(questionView);
         }
     }
 
     private void showQuestions() {
         setContentView(R.layout.main);
 
         TextView text = (TextView)findViewById(R.id.xmlText);
         for (Question question : questions) {
             text.append("\n" + question.toString());
         }
     }
 }
