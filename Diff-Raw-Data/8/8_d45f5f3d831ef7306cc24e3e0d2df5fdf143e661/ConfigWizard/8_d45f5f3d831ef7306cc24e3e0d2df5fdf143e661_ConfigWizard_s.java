 package edu.unh.schwartz.parawrap.config;
 
 import java.awt.Dimension;
 import java.util.ArrayList;
 import java.util.List;
 // import javax.swing.border.EmptyBorder;
 import javax.swing.JCheckBox;
 import javax.swing.JDialog;
 import javax.swing.JLabel;
 import javax.swing.JTextField;
 import org.ciscavate.cjwizard.PageFactory;
 import org.ciscavate.cjwizard.pagetemplates.DefaultPageTemplate;
 import org.ciscavate.cjwizard.WizardContainer;
 import org.ciscavate.cjwizard.WizardListener;
 import org.ciscavate.cjwizard.WizardPage;
 import org.ciscavate.cjwizard.WizardSettings;
 
 /**
  * GUI Wizard to help create configuration file.
  */ 
 public final class ConfigWizard
 {
     /**
      * The instance.
      */
     private static ConfigWizard instance;
 
     /**
      * The last configuration created.
      */ 
     private Configuration config;
 
     /**
      * The window for the wizard.
      */
     private JDialog window;
 
     /**
      * Prevents instantiation.
      */
     private ConfigWizard()
     {
         window = new JDialog();
 
         final WizardContainer wc = 
             new WizardContainer(new ConfigPageFactory(),
                     new DefaultPageTemplate());
 
         wc.addWizardListener(new ConfigWizardListener());
 
         window.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
         window.getContentPane().add(wc);
         window.setSize(375, 250);
         window.setLocationRelativeTo(null);
     }
 
     /**
      * @return the instance of the <code>ConfigWizard</code>
      */
     public static ConfigWizard getInstance()
     {
         if (instance == null)
         {
             instance = new ConfigWizard();
         }
         return instance;
     }
 
     /**
      * Goes through the process of creating a configuration file. Saves it?
      *
      * @return the configuration created by the user
      */
     public Configuration createConfiguration()
     {
         this.window.setVisible(true);
         synchronized(this)
         {
             try
             {
                 this.wait();
             }
             catch (InterruptedException e)
             {
                 System.err.println("Oh noes!");
             }
         }
 
        System.err.println("IT WORKED");

         return this.config;
     }
 
     /**
      * Handles the callbacks from the wizard.
      */
     private final class ConfigWizardListener implements WizardListener
     {
         /**
          * {@inheritDoc}
          */
         @Override
         public void onCanceled(final List<WizardPage> path, 
                 final WizardSettings settings)
         {
             //TODO: Add a log message here
             System.err.println("Cancelled-> " + settings);
             ConfigWizard.this.window.dispose();
             synchronized(ConfigWizard.this)
             {
                 ConfigWizard.this.notify();
             }
         }
 
         /**
          * {@inheritDoc}
          */
         @Override
         public void onFinished(final List<WizardPage> path, 
                 final WizardSettings settings)
         {
             //TODO: Add a log message here
             ConfigWizard.this.config = new Configuration(settings);
             ConfigWizard.this.window.dispose();
             synchronized(ConfigWizard.this)
             {
                 ConfigWizard.this.notify();
             }
         }
 
         /**
          * {@inheritDoc}
          */
         @Override
         public void onPageChanged(final WizardPage newPage, 
                 final List<WizardPage> path) 
         {
             ConfigWizard.this.window.setTitle(newPage.getDescription());
         }
     }
 
     private final class ConfigPageFactory implements PageFactory
     {
         /**
          * The pages of the wizard.
          */
         private final WizardPage[] pages = {new IOPage(), new ExecPage(), 
             new OtherPage(),}; 
 
         /**
          * {@inheritDoc}
          */
         public WizardPage createPage(final List<WizardPage> path, 
                 final WizardSettings settings)
         {
             if (path.size() == pages.length)
             {
                 return new ReviewPage(settings);
             }
             else
             {
                 return pages[path.size()];
             }
         }
     }
 
     /**
      * Border for all components.
      */
     // private static final EmptyBorder BORDER = new EmptyBorder(0, 0, 40, 0);
 
     // private class Seperator extends JLabel
     // {
         // private Seperator()
         // {
             // super(" ");
             // setBorder(BORDER);
         // }
     // }
 
     private final class IOPage extends WizardPage
     {
         private IOPage()
         {
             super("IO", "IO Settings");
 
             // Settings for the input file
             final JFileChooserButton inChooser = new JFileChooserButton(false);
             inChooser.setName(Configuration.IN_FILE_KEY);
             add((new JLabel("Select the input file: ")));
             add(inChooser);
 
             // Setting for the split pattern
             final JTextField splitField = new JTextField();
             splitField.setName(Configuration.SPLIT_PATTERN_KEY);
             splitField.setPreferredSize(new Dimension(50, 20));
             add(new JLabel("Pattern used to split input: "));
             add(splitField);
             add(new JLabel("(Leave blank to use every new line)"));
 
             // Settings for the output location
             final JFileChooserButton outChooser = new JFileChooserButton(true);
             outChooser.setName(Configuration.OUT_FILE_KEY);
             add((new JLabel("Select the output directory: ")));
             add(outChooser);
         }
     }
 
     private final class ExecPage extends WizardPage
     {
         private ExecPage()
         {
             super("Exec", "Executable Settings");
 
             // Settings for the output location
             final JFileChooserButton execChooser = 
                 new JFileChooserButton(false);
             execChooser.setName(Configuration.EXEC_LOC_KEY);
             add((new JLabel("Select the executable: ")));
             add(execChooser);
 
             // Name for input and output flags
             // Setting for the split pattern
             final JTextField inFlagField = new JTextField();
             inFlagField.setName(Configuration.IN_FLAG_KEY);
             inFlagField.setPreferredSize(new Dimension(50, 20));
             add(new JLabel("Flag for executable to define input file"));
             add(inFlagField);
             add(new JLabel("(Leave blank to use stdin)"));
 
             // Setting for the split pattern
             final JTextField outFlagField = new JTextField();
             outFlagField.setName(Configuration.OUT_FLAG_KEY);
             outFlagField.setPreferredSize(new Dimension(50, 20));
             add(new JLabel("Flag for executable to define output dir"));
             add(outFlagField);
             add(new JLabel("(Leave blank to use stdout)"));
         }
     }
 
     private final class OtherPage extends WizardPage
     {
         private OtherPage()
         {
             super("Threads", "Other Settings");
             
             // Settings for the number of threads
             final int max = Runtime.getRuntime().availableProcessors();
             final List<String> options = new ArrayList<String>();
             int i = 1;
             while (i <= max)
             {
                 options.add(Integer.toString(i));
                 i *= 2;
             }
 
             final WizardSpinner spinner = new WizardSpinner(options);
             spinner.setName(Configuration.NUM_PROCESSES_KEY);
             add(new JLabel("Select the number of processes: "));
             add(spinner);
 
             final JCheckBox box = new JCheckBox();
             box.setName(Configuration.STATS_KEY);
             add(new JLabel("Create stats file?"));
             add(box);
 
             final JTextField headerField = new JTextField();
             headerField.setName(Configuration.NUM_HEADER_KEY);
             headerField.setText("0");
             headerField.setPreferredSize(new Dimension(50, 20));
             add(new JLabel("Number of header lines in output files: "));
             add(headerField);
         }
     }
 
     private final class ReviewPage extends WizardPage
     {
         private ReviewPage(final WizardSettings settings)
         {
             super("Confirmation", "Confirmation Page");
 
             add(new JLabel("This will eventually work"));
         }
 
         /**
          * {@inheritDoc}
          */
         public void rendering(final List<WizardPage> path, 
                 final WizardSettings settings) 
         {
             super.rendering(path, settings);
             setFinishEnabled(true);
             setNextEnabled(false);
         }
     }
 
     /**
      * Test main method. Isn't hanging around.
      * @param args - not 
      */
     public static void main(final String[] args)
     {
         final Configuration c = ConfigWizard.getInstance().createConfiguration();
         System.out.println(c);
     }
 }
