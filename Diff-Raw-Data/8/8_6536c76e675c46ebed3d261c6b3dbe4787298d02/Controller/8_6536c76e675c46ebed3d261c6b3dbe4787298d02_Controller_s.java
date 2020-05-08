 /**
  * Controller, handling user interaction and abstracting away the model.
  *
  * @author Antonino Cucchiara
  */
 package gpxsplitter;
 
 import gpxsplitter.tools.FileNotValidException;
 import gpxsplitter.tools.builders.GpxFileBuilder;
 import gpxsplitter.tools.builders.GpxRouteFileBuilder;
 import gpxsplitter.tools.builders.GpxTrackFileBuilder;
 import gpxsplitter.model.GpxType;
 import gpxsplitter.model.Gpx;
 import gpxsplitter.tools.builders.SingleTrackGpxFileBuilder;
 import gpxsplitter.tools.loaders.GpxFileLoader;
 import gpxsplitter.tools.loaders.GpxMultiTrackFileLoader;
 import gpxsplitter.tools.loaders.GpxRouteFileLoader;
 import gpxsplitter.tools.loaders.GpxTrackFileLoader;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
 import javax.swing.JFileChooser;
 import javax.swing.UIManager;
 import org.jdom.Document;
 import org.jdom.JDOMException;
 import org.jdom.input.SAXBuilder;
 
 public class Controller
 {
 
     private UI view;
     private Gpx loadedGpx;
 
     public static void main(String[] args)
     {
         try
         {
             UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
         }
         catch (Exception e)
         {
             System.out.println("Error setting native LAF: " + e);
         }
 
         final UI view = new UI();
         new Controller(view);
         view.setVisible(true);
     }
 
     private Controller(UI view)
     {
         this.view = view;
         this.view.getBrowseButton().addMouseListener(new MouseAdapter()
         {
 
             @Override
             public void mousePressed(MouseEvent e)
             {
                 try
                 {
                     loadGpxFile();
                 }
                 catch (FileNotValidException ex)
                 {
                     Controller.this.view.showMessage(ex.getMessage());
                 }
             }
         });
         this.view.getSaveFileButton().addMouseListener(new MouseAdapter()
         {
 
             @Override
             public void mousePressed(MouseEvent e)
             {
                 saveGpxFile();
             }
         });
         this.view.getExitMenuItem().addMouseListener(new MouseAdapter()
         {
 
             @Override
             public void mousePressed(MouseEvent e)
             {
                 System.exit(0);
             }
         });
         this.view.getAboutMenuItem().addMouseListener(new MouseAdapter()
         {
 
             @Override
             public void mouseReleased(MouseEvent e)
             {
                 Controller.this.view.showAboutPane();
             }
         });
         this.view.getHelpMenuItem().addMouseListener(new MouseAdapter()
         {
 
             @Override
             public void mousePressed(MouseEvent e)
             {
                 Controller.this.view.browseHelpSystem();
             }
         });
         this.view.getSeparateTracksRadioButton().addMouseListener(new MouseAdapter()
         {
 
             @Override
             public void mousePressed(MouseEvent e)
             {
                 Controller.this.view.setSplittingEnabled(false);
             }
         });
         this.view.getJoinTracksRadioButton().addMouseListener(new MouseAdapter()
         {
 
             @Override
             public void mousePressed(MouseEvent e)
             {
                 Controller.this.view.setSplittingEnabled(true);
             }
         });
     }
 
     private void loadGpxFile() throws FileNotValidException
     {
         JFileChooser fileChooser = new JFileChooser();
         fileChooser.setFileFilter(new GpxFileFilter());
         fileChooser.showOpenDialog(view);
         File selectedFile = fileChooser.getSelectedFile();
         if (selectedFile != null)
         {
             try
             {
                 loadGpxFile(selectedFile);
             }
             catch (IOException ex)
             {
                Logger.getLogger(UI.class.getName()).log(Level.SEVERE, null, ex);
             }
             catch (JDOMException ex)
             {
                Logger.getLogger(UI.class.getName()).log(Level.SEVERE, null, ex);
             }
         }
     }
 
     void loadGpxFile(File gpxFile) throws IOException, JDOMException, FileNotValidException
     {
         GpxFileLoader gpxLoader;
 
         final SAXBuilder builder = new SAXBuilder();
         final Document gpxDocument = builder.build(new FileInputStream(gpxFile));
 
         if (GpxFileLoader.getType(gpxDocument) == GpxType.Route)
         {
             gpxLoader = new GpxRouteFileLoader(new FileInputStream(gpxFile), gpxFile.getAbsolutePath());
             view.setMultiTrackEnabled(false);
 
         }
         else if ((GpxFileLoader.getType(gpxDocument) == GpxType.Track))
         {
             if (GpxFileLoader.isGpxMultitrack(gpxDocument))
             {
                 gpxLoader = new GpxMultiTrackFileLoader(new FileInputStream(gpxFile), gpxFile.getAbsolutePath());
                 view.setTracksNumValue(String.valueOf(GpxFileLoader.getTracksNum(gpxDocument)));
                 view.setMultiTrackEnabled(true);
             }
             else
             {
                 gpxLoader = new GpxTrackFileLoader(new FileInputStream(gpxFile), gpxFile.getAbsolutePath());
                 view.setMultiTrackEnabled(false);
             }
         }
         else
         {
             throw new FileNotValidException("The file you are trying to load is not route nor track.");
         }
         loadedGpx = gpxLoader.load();
 
         String fileType = "Gpx " + loadedGpx.getType() + " " + loadedGpx.getVersion();
         fileType += " (" + loadedGpx.getNumOfInstructions() + " instructions)";
         view.setOpenFileField(loadedGpx.getFilePath());
         view.setFileTypeValue(fileType);
     }
 
     private void saveGpxFile()
     {
         String numOfInstructions = view.getInstructionsNumber();
         if (!isAValidInteger(numOfInstructions))
         {
             view.showMessage("Instructions number not valid");
             return;
         }
         int instNum = Integer.parseInt(numOfInstructions);
         String gpxType = view.getHighlightedGpxType();
         String fileName = view.getNewGpxFileName();
         if (fileName.isEmpty())
         {
             view.showMessage("File name cannot be empty");
             return;
         }
         try
         {
             JFileChooser saveFileChooser = new JFileChooser();
             saveFileChooser.setSelectedFile(new File(fileName));
             saveFileChooser.setFileFilter(new GpxFileFilter());
             saveFileChooser.showSaveDialog(view);
             saveGpxFile(instNum, gpxType, saveFileChooser.getSelectedFile());
 
         }
         catch (IOException e)
         {
             view.showMessage("Problem whilst saving the file." + UI.LINE_SEPARATOR + "Do you have the rights to write to that location?");
         }
     }
 
     void saveGpxFile(int desiredInstrNum, String gpxType, File file) throws IOException
     {
         if (loadedGpx == null)
         {
             view.showMessage("A GPX to split has to be selected");
             return;
         }
 
         view.showMessage("Saving Gpx file(s) \n Instructions number: " + desiredInstrNum + "\n Gpx Type: " + gpxType);
 
         GpxFileBuilder gpxBuilder;
         if (view.getSeparateTracksRadioButton().isSelected())
         {
             gpxBuilder = new SingleTrackGpxFileBuilder(loadedGpx);
         }
         else if (view.getSelectedGpxType() == GpxType.Track)
         {
             gpxBuilder = new GpxTrackFileBuilder(loadedGpx);
         }
         else
         {
             gpxBuilder = new GpxRouteFileBuilder(loadedGpx);
         }
 
         gpxBuilder.build(file, desiredInstrNum);
         view.showMessage("Split GPX successfully saved.");
     }
 
     private boolean isAValidInteger(String intAsStr)
     {
         try
         {
             Integer.parseInt(intAsStr);
             return true;
         }
         catch (NumberFormatException e)
         {
             return false;
         }
     }
 }
