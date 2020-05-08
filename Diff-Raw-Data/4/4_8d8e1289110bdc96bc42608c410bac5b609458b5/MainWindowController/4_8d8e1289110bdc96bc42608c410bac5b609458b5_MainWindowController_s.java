 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package eu.mihosoft.vrl.codevisualization;
 
 import eu.mihosoft.vrl.instrumentation.CodeEntity;
 import eu.mihosoft.vrl.instrumentation.DataFlow;
 import eu.mihosoft.vrl.instrumentation.DataRelation;
 import eu.mihosoft.vrl.instrumentation.Invocation;
 import eu.mihosoft.vrl.instrumentation.Scope;
 import eu.mihosoft.vrl.instrumentation.ScopeInvocation;
 import eu.mihosoft.vrl.instrumentation.ScopeType;
 import eu.mihosoft.vrl.instrumentation.UIBinding;
 import eu.mihosoft.vrl.instrumentation.Variable;
 import eu.mihosoft.vrl.worflow.layout.Layout;
 import eu.mihosoft.vrl.worflow.layout.LayoutFactory;
 import eu.mihosoft.vrl.workflow.Connector;
 import eu.mihosoft.vrl.workflow.FlowFactory;
 import eu.mihosoft.vrl.workflow.VFlow;
 import eu.mihosoft.vrl.workflow.VNode;
 import eu.mihosoft.vrl.workflow.fx.FXSkinFactory;
 import eu.mihosoft.vrl.workflow.fx.ScalableContentPane;
 import groovy.lang.GroovyClassLoader;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.net.URL;
 import java.nio.charset.Charset;
 import java.nio.file.Files;
 import java.nio.file.Paths;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.ResourceBundle;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javafx.event.ActionEvent;
 import javafx.fxml.FXML;
 import javafx.fxml.Initializable;
 import javafx.scene.control.TextArea;
 import javafx.scene.input.KeyEvent;
 import javafx.scene.layout.Pane;
 import javafx.scene.paint.Color;
 import javafx.stage.FileChooser;
 import javafx.stage.FileChooserBuilder;
 
 /**
  * FXML Controller class
  *
  * @author Michael Hoffer <info@michaelhoffer.de>
  */
 public class MainWindowController implements Initializable {
 
     File currentDocument;
     @FXML
     TextArea editor;
     @FXML
     Pane view;
     private Pane rootPane;
     private VFlow flow;
     private Map<CodeEntity, VNode> invocationNodes = new HashMap<>();
 
     /**
      * Initializes the controller class.
      */
     @Override
     public void initialize(URL url, ResourceBundle rb) {
 
         System.out.println("Init");
 
         ScalableContentPane canvas = new ScalableContentPane();
         canvas.setStyle("-fx-background-color: rgb(0,0, 0)");
         view.getChildren().add(canvas);
 
         Pane root = new Pane();
         canvas.setContentPane(root);
         root.setStyle("-fx-background-color: linear-gradient(to bottom, rgb(10,32,60), rgb(42,52,120));");
 
         rootPane = root;
 
         flow = FlowFactory.newFlow();
     }
 
     @FXML
     public void onKeyTyped(KeyEvent evt) {
 //        String output = editor.getText();
 //
 //        output = MultiMarkdown.convert(output);
 //
 //        System.out.println(output);
 //
 //        
 //
 //        URL.setURLStreamHandlerFactory(new URLStreamHandlerFactory() {
 //
 //            @Override
 //            public URLStreamHandler createURLStreamHandler(String protocol) {
 //                
 //            }
 //        });
 //        
 //        
 //        outputView.getEngine().s
     }
 
     @FXML
     public void onLoadAction(ActionEvent e) {
         loadTextFile(null);
     }
 
     @FXML
     public void onSaveAction(ActionEvent e) {
         saveDocument(false);
         updateView();
     }
 
     private void saveDocument(boolean askForLocationIfAlreadyOpened) {
 
         if (askForLocationIfAlreadyOpened || currentDocument == null) {
             FileChooser.ExtensionFilter mdFilter =
                     new FileChooser.ExtensionFilter("Text Files (*.groovy, *.txt)", "*.groovy", "*.txt");
 
             FileChooser.ExtensionFilter allFilesfilter =
                     new FileChooser.ExtensionFilter("All Files (*.*)", "*.*");
 
             currentDocument =
                     FileChooserBuilder.create().title("Save Groovy File").
                     extensionFilters(mdFilter, allFilesfilter).build().
                     showSaveDialog(null).getAbsoluteFile();
         }
 
         try (FileWriter fileWriter = new FileWriter(currentDocument)) {
             fileWriter.write(editor.getText());
         } catch (IOException ex) {
             Logger.getLogger(MainWindowController.class.getName()).
                     log(Level.SEVERE, null, ex);
         }
 
     }
 
     private void insertStringAtCurrentPosition(String s) {
         editor.insertText(editor.getCaretPosition(), s);
     }
 
     @FXML
     public void onSaveAsAction(ActionEvent e) {
         saveDocument(true);
         updateView();
     }
 
     @FXML
     public void onCloseAction(ActionEvent e) {
     }
 
     void loadTextFile(File f) {
 
         try {
             if (f == null) {
                 FileChooser.ExtensionFilter mdFilter =
                         new FileChooser.ExtensionFilter("Text Files (*.groovy, *.txt)", "*.groovy", "*.txt");
 
                 FileChooser.ExtensionFilter allFilesfilter =
                         new FileChooser.ExtensionFilter("All Files (*.*)", "*.*");
 
                 currentDocument =
                         FileChooserBuilder.create().title("Open Groovy File").
                         extensionFilters(mdFilter, allFilesfilter).build().
                         showOpenDialog(null).getAbsoluteFile();
             } else {
                 currentDocument = f;
             }
 
             List<String> lines =
                     Files.readAllLines(Paths.get(currentDocument.getAbsolutePath()),
                     Charset.defaultCharset());
 
             String document = "";
 
             for (String l : lines) {
                 document += l + "\n";
             }
 
             editor.setText(document);
 
             updateView();
 
         } catch (IOException ex) {
             Logger.getLogger(MainWindowController.class.getName()).
                     log(Level.SEVERE, null, ex);
         }
     }
 
     private void updateView() {
 
         if (rootPane == null) {
             System.err.println("UI NOT READY");
             return;
         }
 
         UIBinding.scopes.clear();
 
         GroovyClassLoader gcl = new GroovyClassLoader();
         gcl.parseClass(editor.getText());
 
         System.out.println("UPDATE UI");
 
 
         flow.clear();
 
         flow.setSkinFactories();
 
         System.out.println("FLOW: " + flow.getSubControllers().size());
 
         flow.getModel().setVisible(true);
 
         if (UIBinding.scopes == null) {
             System.err.println("NO SCOPES");
             return;
         }
 
         for (Collection<Scope> scopeList : UIBinding.scopes.values()) {
             for (Scope s : scopeList) {
                 scopeToFlow(s, flow);
             }
         }
 
         FXSkinFactory skinFactory = new FXSkinFactory(rootPane);
 
         flow.setSkinFactories(skinFactory);
 
         Layout layout = LayoutFactory.newDefaultLayout();
         layout.doLayout(flow);
 
     }
 
     public void dataFlowToFlow(Scope scope, VFlow parent) {
 
         DataFlow dataflow = scope.getDataFlow();
         dataflow.create(scope.getControlFlow());
 
         for (Invocation i : scope.getControlFlow().getInvocations()) {
 
 //            Variable retValue = scope.getVariable(i.getReturnValueName());
 
             List<DataRelation> relations = dataflow.getRelationsForReceiver(i);
             
             System.out.println("relations: " + relations.size());
 
             for (DataRelation dataRelation : relations) {
                 
                 
                 VNode sender = invocationNodes.get(dataRelation.getSender());
                 VNode receiver = invocationNodes.get(dataRelation.getReceiver());
                 
                 System.out.println("SENDER: " + sender.getId() + ", receiver: " + receiver.getId());
                 
                 Connector senderConnector = sender.getConnector("4");
 
                 String retValueName =
                         dataRelation.getSender().getReturnValueName();
                 
 //                 parent.connect(
 //                                senderConnector, receiver.getConnector(""));
 
                 int inputIndex = 0;
 
                 for (Variable var : dataRelation.getReceiver().getArguments()) {
                     System.out.println("var: " + var);
                     if (var.getName().equals(retValueName)) {
                         Connector receiverConnector =
                                 receiver.getConnector("3");
 
                         parent.connect(
                                 senderConnector, receiverConnector);
 
                         System.out.println( inputIndex + "connect: " + senderConnector.getType()+":"+senderConnector.isOutput()+ " -> " + receiverConnector.getType()+ ":" + receiverConnector.isInput());
                     }
                     inputIndex++;
                 }
             }
         }
     }
 
     public VFlow scopeToFlow(Scope scope, VFlow parent) {
 
        boolean isClassOrScript = scope.getType() == ScopeType.CLASS || scope.getType() == ScopeType.NONE;
 
         VFlow result = parent.newSubFlow();
 
         invocationNodes.put(scope, result.getModel());
 
         String title = "" + scope.getType() + " " + scope.getName() + "(): " + scope.getId();
 
         if (isClassOrScript) {
             result.getModel().setWidth(550);
             result.getModel().setHeight(800);
             result.setVisible(true);
         } else {
             result.getModel().setWidth(400);
             result.getModel().setHeight(300);
         }
 
         result.getModel().setTitle(title);
 
         System.out.println("Title: " + title + ", " + scope.getType());
 
         VNode prevNode = null;
 
         for (Invocation i : scope.getControlFlow().getInvocations()) {
 
             VNode n;
 
             if (i.isScope() && !isClassOrScript) {
 
                 ScopeInvocation sI = (ScopeInvocation) i;
                 n = scopeToFlow(sI.getScope(), result).getModel();
                 
             } else {
                 n = result.newNode();
                 String mTitle = "" + i.getVarName() + "." + i.getMethodName() + "(): " + i.getId();
                 n.setTitle(mTitle);
 
                 invocationNodes.put(i, n);
             }
 
             n.setMainInput(n.addInput("control"));
             n.setMainOutput(n.addOutput("control"));
 
             if (prevNode != null) {
                 result.connect(prevNode, n, "control");
             }
 
             for (Variable v : i.getArguments()) {
                 n.addInput("data");
             }
 
             if (!i.isVoid()) {
                 n.addOutput("data");
             }
 
             n.setWidth(400);
             n.setHeight(100);
 
             System.out.println("Node: " + i.getCode());
 
             prevNode = n;
         }
 
         if (isClassOrScript) {
             for (Scope s : scope.getScopes()) {
                 scopeToFlow(s, result);
             }
         }
         
         dataFlowToFlow(scope, result);
 
         return result;
     }
 }
