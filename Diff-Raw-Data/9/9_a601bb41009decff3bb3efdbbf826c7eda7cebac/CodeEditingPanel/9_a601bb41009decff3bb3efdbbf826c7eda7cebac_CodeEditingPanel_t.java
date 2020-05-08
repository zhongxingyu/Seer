 package maze.gui;
 
 import java.awt.event.ActionEvent;
 import java.io.File;
 
 import javax.swing.AbstractAction;
 import javax.swing.Action;
 import javax.swing.JEditorPane;
 import javax.swing.JFileChooser;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JSplitPane;
 import javax.swing.JTabbedPane;
 import javax.swing.SwingUtilities;
 import javax.swing.filechooser.FileFilter;
 
 /**
  * Creates a robot AI script editor that allows the creation of Python scripts
  * to control the AI robot.
  * @author Luke Last
  */
 public class CodeEditingPanel extends JSplitPane
 {
    private final JTabbedPane editorTabs = new JTabbedPane();
 
    /**
     * Constructor.
     */
    public CodeEditingPanel()
    {
 
       //Set up the documentation browser.
       final JEditorPane docPane = new JEditorPane();
       docPane.setEditable(false);
       docPane.setContentType("text/html");
       docPane.setText("<html><h2>There was an error loading the documentation</h2></html>");
 
       try
       {
          docPane.setPage(CodeEditingPanel.class.getResource("api.html"));
       }
       catch (Exception e)
       {
          e.printStackTrace();
       }
 
       //Set up the documentation tabs.
       final JTabbedPane docTabs = new JTabbedPane();
 
       docTabs.add("Code Information", new CodeInformationPanel());
       docTabs.add("API Documentation", new JScrollPane(docPane));
 
       //Set up split pane.
       this.setLeftComponent(this.editorTabs);
       this.setRightComponent(docTabs);
       this.doLayout();
       this.setResizeWeight(.8);
       //We set the divider after a delay so the HTML pane doesn't mess it up.
       SwingUtilities.invokeLater(new Runnable()
       {
          @Override
          public void run()
          {
             setDividerLocation(.7);
          }
       });
    }
 
    public void createNewEditor()
    {
       this.editorTabs.add("New Editor", new CodeEditorPane());
    }
 
    public void openScript()
    {
       JFileChooser fc = new JFileChooser();
       //fc.setCurrentDirectory(new File(".." + File.separator + "Scripts"));
 
       fc.addChoosableFileFilter(new FileFilter()
       {
          @Override
          public boolean accept(File f)
          {
        	 if (f.isDirectory()) 
        	 { 
        	      return true;
        	 }  
        	 else 
        	 { 
        		 return f.getName().toLowerCase().endsWith(".py");
        	 }
          }
 
          @Override
          public String getDescription()
          {
             return "Python Scripts";
          }
       });
 
       int result = fc.showOpenDialog(this);
       if (result == JFileChooser.APPROVE_OPTION)
       {
          File file = fc.getSelectedFile();
          if (file.exists())
          {
             CodeEditorPane editor = new CodeEditorPane(file);
             this.editorTabs.add(file.getName(), editor);
             this.editorTabs.setSelectedComponent(editor);
          }
       }
    }
 
    /**
     * Saves the contents of the currently selected editor tab.
     */
    public void saveScript()
    {
       try
       {
          CodeEditorPane editor = (CodeEditorPane) this.editorTabs.getSelectedComponent();
          editor.saveScriptFile();
       }
       catch (Exception ex)
       {
          ex.printStackTrace();
       }
    }
 
    /**
     * Closes the currently visible editor tab.
     */
    final Action closeScriptAction = new AbstractAction()
    {
       {
          this.putValue(Action.NAME, "Close AI Script");
       }
 
       @Override
       public void actionPerformed(ActionEvent e)
       {
          CodeEditorPane editor = (CodeEditorPane) editorTabs.getSelectedComponent();
          if (editor != null)
          {
             editor.removeFromRobotModel();
             editorTabs.remove(editor);
          }
 
       }
    };
 
    private final class CodeInformationPanel extends JPanel
    {
    }
 }
