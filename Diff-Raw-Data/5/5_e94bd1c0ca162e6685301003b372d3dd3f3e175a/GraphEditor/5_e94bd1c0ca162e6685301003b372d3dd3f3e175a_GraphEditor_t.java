 /*
  * WOLFED
  * 
  * WO rkflow 
  * L ight
  * F ast
  * ED itor
  */
 package it.wolfed.swing;
 
 import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
 import com.mxgraph.layout.mxCompactTreeLayout;
 import com.mxgraph.layout.mxOrganicLayout;
 import com.mxgraph.view.mxGraph;
 import it.wolfed.model.PetriNetGraph;
 import it.wolfed.operations.DefferedChoiceOperation;
 import it.wolfed.operations.ExplicitChoiceOperation;
 import it.wolfed.operations.OneOrMoreIterationOperation;
 import it.wolfed.operations.OneServePerTimeOperation;
 import it.wolfed.operations.ParallelismOperation;
 import it.wolfed.operations.SequencingOperation;
 import it.wolfed.operations.ZeroOrMoreIterationOperation;
 import it.wolfed.util.Constants;
 import it.wolfed.util.IterableNodeList;
 import java.awt.Toolkit;
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JTabbedPane;
 import javax.swing.UIManager;
 import javax.swing.filechooser.FileNameExtensionFilter;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 import org.xml.sax.SAXException;
 
 public final class GraphEditor extends JFrame
 {
     public static final String VERSION = "0.9.6";
     private List<PetriNetGraph> graphs = new ArrayList<>();
     private JTabbedPane tabController = new JTabbedPane();
     private String[] operations =
     {
         //Constants.OPERATION_ALTERNATION,
         Constants.OPERATION_DEFFEREDCHOICE,
         Constants.OPERATION_EXPLICITCHOICE,
         Constants.OPERATION_ITERATIONONEORMORE,
         Constants.OPERATION_ITERATIONONESERVEPERTIME,
         Constants.OPERATION_ITERATIONZEROORMORE,
         //Constants.OPERATION_MUTUALEXCLUSION,
         Constants.OPERATION_PARALLELISM,
         Constants.OPERATION_SEQUENCING
     };
     
     private String[] layouts =
     {
         Constants.LAYOUT_VERTICALTREE,
         Constants.LAYOUT_HIERARCHICAL,
         Constants.LAYOUT_ORGANIC,
     };
 
     public GraphEditor()
     {
         setTitle("Wolfed " + GraphEditor.VERSION);
         setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         setJMenuBar(new MenuBarController(this));
         getContentPane().add(tabController);
         setLookAndFeel();
 
         /*
          * Aggiungo due reti solo per fare qualche test
          * In realtà dovrebbero esser prese dal box di selezione
          */
         importFile(new File("nets/pNet_A.pnml"));
         PetriNetGraph n1 = getEditorGraphs().get(0);
         applyLayout(n1, Constants.LAYOUT_HORIZONTALTREE);
 
         importFile(new File("nets/pNet_B.pnml"));
         PetriNetGraph n2 = getEditorGraphs().get(1);
         applyLayout(n2, Constants.LAYOUT_HORIZONTALTREE);
     }
 
     /**
      * Ritorna le operazioni disponibili nell'editor.
      *
      * @return String[]
      */
     public String[] getOperations()
     {
         return operations;
     }
 
     /**
      * Ritorna i layouts disponibili nell'editor.
      *
      * @return String[]
      */
     public String[] getLayouts()
     {
         return layouts;
     }
 
     /**
      * Ritorna tutti i grafi inseriti nell'editor.
      *
      * @return List<PetriNetGraph>
      */
     public List<PetriNetGraph> getEditorGraphs()
     {
         return graphs;
     }
 
     /**
      * Imposta il look and feel.
      *
      * @return void
      */
     private void setLookAndFeel()
     {
         try
         {
             UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
         } 
         catch (Exception e)
         {
             e.printStackTrace();
         }
 
         // FullScreen
         Toolkit tk = Toolkit.getDefaultToolkit();
         int xSize = ((int) tk.getScreenSize().getWidth());
         int ySize = ((int) tk.getScreenSize().getHeight() - 40);
         this.setSize(xSize, ySize);
     }
 
     /**
      * Ritorna il grafo attualmente selezionato nell'editor.
      *
      * @return PetriNetGraph
      */
     public PetriNetGraph getCurrentGraph()
     {
         GraphViewContainer view = (GraphViewContainer) tabController.getSelectedComponent();
         return view.getGraph();
     }
 
     /**
      * Inserisce un nuovo grafo nell'editor e lo seleziona.
      *
      * @param name il nome che verrà mostrato nel tab
      * @param graph il grafo da inserire
      * @return void
      */
     public void insertGraph(String name, PetriNetGraph graph)
     {
         tabController.add(name, new GraphViewContainer(graph));
         tabController.setSelectedIndex(tabController.getTabCount() - 1);
         getEditorGraphs().add(graph);
     }
 
     /**
      * Aggiunge un nuovo tab nell'editor con un grafo vuoto.
      *
      * @return void
      */
     public void newFile()
     {
         String name = String.valueOf(tabController.getTabCount() + 1);
         PetriNetGraph net = new PetriNetGraph(name);
         insertGraph("new_" + name, net);
     }
 
     /**
      * Apre il box di scelta per importare un nuovo file pnml.
      *
      * @return void
      */
     public void openFile()
     {
         JFileChooser fc = new JFileChooser();
         fc.setFileFilter(new FileNameExtensionFilter("xml, pnml", "xml", "pnml"));
         fc.setCurrentDirectory(new File("/nets"));
 
         if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
         {
             File file = fc.getSelectedFile();
             importFile(file);
         }
     }
 
     /**
      * Importa un file xml\pnml nell'editor.
      *
      * @see http://www.pnml.org/
      * @param File pnml complaint file
      */
     private void importFile(File fileXml)
     {
         try
         {
             DocumentBuilder builder = DocumentBuilderFactory
                     .newInstance()
                     .newDocumentBuilder();
 
             Document doc = builder.parse(fileXml);
             doc.getDocumentElement().normalize();
 
             for (final Node netNode : new IterableNodeList(doc.getElementsByTagName(Constants.PNML_NET)))
             {
                 String defaultId = fileXml.getName().substring(0, fileXml.getName().lastIndexOf('.'));
                 insertGraph(fileXml.getName(), PetriNetGraph.factory(netNode, defaultId));
             }
         } 
         catch (ParserConfigurationException | SAXException | IOException ex)
         {
             ex.printStackTrace();
         }
     }
 
     /**
      * Esporta il grafo corrente in un file xml\pnml valido.
      *
      * @return void
      */
     public void saveFile()
     {
         // TODO
         // Document xml = getCurrentGraph().export();
         // String fileXml = xml.toString();
     }
 
     /**
      * Esegue l'operazione sui grafi scelti.
      *
      * @param operationName il tipo di operazione
      */
     public void executeOperation(String operationName)
     {
         PetriNetGraph opGraph = null;
         OperationDialog selectionBox;
          
         try
         { 
             switch (operationName)
             {
                 case Constants.OPERATION_ALTERNATION:
                     break;
                 case Constants.OPERATION_DEFFEREDCHOICE:
                     selectionBox = new OperationDialog(getEditorGraphs(), 2);
                     opGraph = (new DefferedChoiceOperation(selectionBox.getSelectedGraphs())).getOperationGraph();
                     break;
                 case Constants.OPERATION_EXPLICITCHOICE:
                     selectionBox = new OperationDialog(getEditorGraphs(), 2);
                     opGraph = (new ExplicitChoiceOperation(selectionBox.getSelectedGraphs())).getOperationGraph();
                     break;
                 case Constants.OPERATION_ITERATIONONEORMORE:
                     selectionBox = new OperationDialog(getEditorGraphs(), 1);
                     opGraph = (new OneOrMoreIterationOperation(selectionBox.getSelectedGraphs())).getOperationGraph();
                     break;
                 case Constants.OPERATION_ITERATIONONESERVEPERTIME:
                     selectionBox = new OperationDialog(getEditorGraphs(), 1);
                     opGraph = (new OneServePerTimeOperation(selectionBox.getSelectedGraphs())).getOperationGraph();
                     break;
                 case Constants.OPERATION_ITERATIONZEROORMORE:
                     selectionBox = new OperationDialog(getEditorGraphs(), 1);
                     opGraph = (new ZeroOrMoreIterationOperation(selectionBox.getSelectedGraphs())).getOperationGraph();
                     break;
                 case Constants.OPERATION_MUTUALEXCLUSION:
                     break;
                 case Constants.OPERATION_PARALLELISM:
                     selectionBox = new OperationDialog(getEditorGraphs(), 2);
                     opGraph = (new ParallelismOperation(selectionBox.getSelectedGraphs())).getOperationGraph();
                     break;
                 case Constants.OPERATION_SEQUENCING:
                     selectionBox = new OperationDialog(getEditorGraphs(), 2);
                     opGraph = (new SequencingOperation(selectionBox.getSelectedGraphs())).getOperationGraph();
                     break;
             }
             
             insertGraph(opGraph.getId(), opGraph);
             applyLayout(opGraph, Constants.LAYOUT_HORIZONTALTREE);
         } 
         catch (Exception ex)
         {
             ex.printStackTrace();
         }
     }
 
 
     /**
      * Applica un layout ad un grafo.
      *
      * @param graphComponent il grafo da stilizzare
      * @param name il nome del layout
      * @return void
      */
     public void applyLayout(mxGraph graph, String name)
     {
         if(graph == null)
         {
             graph = getCurrentGraph();
         }
         
        Object parent = graph.getDefaultParent();
        
         switch (name)
         {
             case Constants.LAYOUT_VERTICALTREE:
                 (new mxCompactTreeLayout(graph)).execute(parent);
                 break;
 
             case Constants.LAYOUT_HORIZONTALTREE:
                 (new mxCompactTreeLayout(graph, true)).execute(parent);
                 break;
 
             case Constants.LAYOUT_HIERARCHICAL:
                 (new mxHierarchicalLayout(graph)).execute(parent);
                 break;
 
             case Constants.LAYOUT_ORGANIC:
                 (new mxOrganicLayout(graph)).execute(parent);
                 break;
         }
     }
 }
