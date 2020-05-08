 package faceclicker;
 
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 import java.util.Scanner;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javafx.collections.FXCollections;
 import javafx.embed.swing.SwingFXUtils;
 import javafx.event.ActionEvent;
 import javafx.fxml.FXML;
 import javafx.geometry.Insets;
 import javafx.geometry.Pos;
 import javafx.scene.Scene;
 import javafx.scene.control.ComboBox;
 import javafx.scene.control.Label;
 import javafx.scene.image.Image;
 import javafx.scene.image.ImageView;
 import javafx.scene.input.MouseEvent;
 import javafx.scene.layout.Pane;
 import javafx.scene.layout.VBoxBuilder;
 import javafx.scene.shape.Circle;
 import javafx.scene.text.Text;
 import javafx.stage.DirectoryChooser;
 import javafx.stage.Modality;
 import javafx.stage.Stage;
 
 import javax.imageio.ImageIO;
 
 /**
  *
  * @author Alex McKay
  */
 public class MainController {
 
     /* The following objects are GUI components specified within View.fxml */
     @FXML
     private Text nextclickmessage;
     @FXML
     private Pane pane1;
     @FXML
     private ImageView imageBox;
     @FXML
     private Label dirCount;
     @FXML
     private ComboBox<String> imageCombo;
 
     /* The collection of points currently being modified */
     private SalientPointCollection points;
     
     /* Is true only when there are more salient points to be input for the current image */
     private boolean activated = false; 
     
     /* Contains the circles currently being displayed on the image */
     private final ArrayList<Circle> circleList = new ArrayList<>();
     
     /* The name of the image file currently being displayed */ 
     private String imageName;
     
     /* Contains directory contents for current directory */
     private DirectoryContents dirContents;
     
     /* Specify the width and height of the current image */
     private double curWidth, curHeight;
 
     /**
      * Handles the "Load Directory" button being pressed. Offers a selection 
      * dialog to the user to choose a directory, then creates a new 
      * DirectoryContents object based on the specified directory. Also 
      * configures the drop-down menu with image names and displays the first 
      * image in the directory.
      *
      * @param event "Load Directory" being pressed
      * @throws InterruptedException
      * @throws IOException
      */
     @FXML
     protected void handleLoadDirButton(ActionEvent event) throws InterruptedException, IOException {
 
         Stage primaryStage = (Stage) imageBox.getScene().getWindow();
 
        //display dialogue window for user to choose directory
         DirectoryChooser chooser = new DirectoryChooser();
         chooser.setTitle("Choose Directory");
         File defaultDirectory = new File("c:/");
         chooser.setInitialDirectory(defaultDirectory);
         File dirPath = chooser.showDialog(primaryStage);
         
         //create new DirectoryContents object from specified path
         dirContents = new DirectoryContents(dirPath);
 
         //set the options within the combobox to correspond to the image names
         imageCombo.setItems(FXCollections.observableArrayList(dirContents.getImageNamesList()));
        
        //check that config-points.txt file exists
        File configPoints = new File("config-points.txt");
        if (!configPoints.isFile())
            alert("Error: config-points.txt file not found! \n"
                    + "Please place this file in the same directory\n "
                    + "as FaceClicker.jar before continuing");
 
         //display the first image from the directory
         displayImage(dirContents.getImage(0));
 
     }
 
     /**
      * Handles the "Next Image" button being pressed. Simply calls the
      * nextImage() function. 
      *
      * @param event "Next Image" being pressed
      * @throws IOException
      */
     @FXML
     protected void handleNextImageButton(ActionEvent event) throws IOException {
         nextImage();
     }
 
     /**
      * Handles the user making a new selection in the drop-down menu. Displays 
      * a new image corresponding to the user's selection, unless the user 
      * selects the image they are already on.
      *
      * @param event User making a selection within the combo-box.
      * @throws IOException
      */
     @FXML
     protected void handleComboChange(ActionEvent event) throws IOException {
         int i = imageCombo.getSelectionModel().getSelectedIndex();
         
         //load new image if user has not selected the current image
         if (i > (-1) && i != dirContents.getIterator())
             displayImage(dirContents.getImage(i));
         
     }
 
     /**
      * Handles "undo" button being pushed. Simply calls undo() function.
      *
      * @param event "undo" button being pushed
      */
     @FXML
     protected void handleUndoButton(ActionEvent event) {
         undo();
     }
 
     /**
      * Handles "reset" button being pushed. Removes all circles from current 
      * image and resets the SalientPointCollection. 
      *
      * @param event "reset" button being pushed.
      */
     @FXML
     protected void handleResetButton(ActionEvent event) {
         points = new SalientPointCollection();
         wipeCircles();
         activated = true;
         
         //tell the user to click the first point in the collection
         nextclickmessage.setText(points.getCurrent().getName());
     }
 
     /**
      * Store the x and y values of the cursor in the current salient point 
      * object, and move on to the next one. Will only perform actions if there 
      * are still points to annotate and user has clicked inside the image.
      *
      * @param me The mouse being clicked.
      */
     @FXML
     protected void grabCoords(MouseEvent me) {
         double mouseX = me.getX();
         double mouseY = me.getY();
 
         //if there are still points to be annotated
         if (activated) {
             
             //if the user has clicked on the image
             if (mouseX <= curWidth && mouseY <= curHeight) {
                 
                 //set x/y values for the current point
                 SalientPoint sp = points.getCurrent();
                 sp.setX(mouseX);
                 sp.setY(mouseY);
 
                 //display a circle on the image at the specified point
                 circleList.add(sp.getCircle());
                 pane1.getChildren().add(sp.getCircle());
 
                 //write the point's x/y data to the relevant file
                 try {
                     points.writePoints(dirContents.getPointsFilePath(imageName));
                 } catch (FileNotFoundException | UnsupportedEncodingException ex) {
                     Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
                 }
 
                 //if there are more points to annotate, move onto the next one
                 if (points.hasNext()) {
                     points.iterate();
                     //display the name of the next point to click
                     nextclickmessage.setText(points.getCurrent().getName());
                 } else {
                     nextclickmessage.setText("Image finished");
                     activated = false;
                 }
             }
         }
     }
 
     /**
      * Display a new image for annotation. If file can be displayed, will wipe
      * current points circles from GUI and display new image. If image has 
      * previously been annotated, will load points from file, otherwise will 
      * allow user to start annotating image.
      *
      * @param imagePath The path to the image file to be displayed.
      * @throws IOException
      */
     public void displayImage(String imagePath) throws IOException {
 
         //refresh list of coordinate data files in case new file exists
         dirContents.refreshPointlist();
 
         points = new SalientPointCollection();
         wipeCircles();
 
         //get name of new image to display
         File file = new File(imagePath);
         imageName = file.getName();
 
         //get path to where points data for this image should be stored
         String pointsPath = dirContents.getPointsFilePath(imageName);
 
         //if points data already exists, load it. Otherwise, ask user to input first point
         if (dirContents.getPointsList().contains(pointsPath))
             loadPoints(pointsPath);
         else
             nextclickmessage.setText(points.getCurrent().getName());
 
         BufferedImage bufferedImage = ImageIO.read(file);
         Image image = SwingFXUtils.toFXImage(bufferedImage, null);
 
         //allow user to input points
         activated = true;
         
         curWidth = image.getWidth();
         curHeight = image.getHeight();
         
         //display image
         imageBox.setImage(image);
         
         //display user's current place in directory on GUI
         setDirCount(dirContents.getIterator(), dirContents.getImageList().size());
 
         //set drop-down menu's position to the current image
         imageCombo.getSelectionModel().select(dirContents.getIterator());
     }
 
     /**
      * Remove all circles corresponding to salient points from the GUI. This is 
      * called when a user changes image.
      */
     public void wipeCircles() {
         //remove all circles from GUI
         for (Circle cc : circleList)
             pane1.getChildren().remove(cc);
         
         //make circlelist empty again before use for next image
         circleList.removeAll(circleList);
     }
 
     /**
      * Display a pop-up window to the user with the specified text.
      *
      * @param s The text to display in the alert window.
      */
     public void alert(String s) {
         Stage dialogStage = new Stage();
         dialogStage.initModality(Modality.WINDOW_MODAL);
         dialogStage.setScene(new Scene(VBoxBuilder.create().
                 children(new Text(s)).
                 alignment(Pos.CENTER).padding(new Insets(5)).build()));
         dialogStage.show();
     }
 
     /**
      * Changes the displayed position within the directory on the GUI. Displayed
      * position is 1 higher than actual position so there is no "0" entry.
      *
      * @param number The current position in the directory.
      * @param total The total number of images within the directory.
      */
     public void setDirCount(int number, int total) {
         dirCount.setText(String.format("%d/%d", number + 1, total));
     }
 
     /**
      * Load a collection of points for a previously annotated image. If image is
      * completely annotated, all points will be loaded and displayed, otherwise
      * data for as many points as have been annotated will be displayed and user
      * will be asked to continue annotating.
      *
      * @param pointsFile The file where coordinate data for the image is stored.
      * @throws FileNotFoundException
      */
     public void loadPoints(String pointsFile) throws FileNotFoundException {
         File file = new File(pointsFile);
         String temp;
         Scanner fileScanner = new Scanner(file);
 
         //loop through every line of points file
         while (fileScanner.hasNextLine()) {
             
             //get the corresponding SalientPoint object
             SalientPoint sp = points.getCurrent();
             
             int x, y;
             
             //read in next line of file
             String line = fileScanner.nextLine();
             
             //name of Salient Point is before the colon
             String pointName = line.substring(0, line.lastIndexOf(':'));
             
             //coordinate data is after the colon
             String values = line.substring(line.lastIndexOf(':') + 2);
 
             //read in x and y values from line
             Scanner lineScanner = new Scanner(values);
             temp = lineScanner.next(); //x
             temp = lineScanner.next(); //=
             x = lineScanner.nextInt();
             temp = lineScanner.next(); //y
             temp = lineScanner.next(); //=
             y = lineScanner.nextInt();
 
             //if x & y are both 0, point has yet to be annotated, so break from loop
             if ((x == 0 && y == 0)) 
                 break;
             
             //set salient point values
             sp.setX(x);
             sp.setY(y);
             
             //display circle
             circleList.add(sp.getCircle());
             pane1.getChildren().add(sp.getCircle());
             
             //move onto next point
             points.iterate();
             nextclickmessage.setText("Points loaded from file");
 
             lineScanner.close();
         }
         fileScanner.close();
         
         //if current point exist, there are still points to be annotated,
         //so inform user of next point to click and allow them to continue
         if (points.getCurrent() != null){
             nextclickmessage.setText(points.getCurrent().getName());
             activated = true;
         }
     }
 
     /**
      * Step back a salient point and allow user to re-enter the last clicked
      * point. 
      *
      */
     public void undo() {
         //if not activated, current point will still be the final point, so will not need to step back a point
         if (activated)
             points.undo();
         else 
             activated = true;
         
         //remove the last placed circle
         if (!circleList.isEmpty())
             pane1.getChildren().remove(circleList.remove(circleList.size() - 1));
         
         //display the point to click again
         nextclickmessage.setText(points.getCurrent().getName());
     }
 
     /**
      * Display the next image in the directory.
      *
      * @throws java.io.IOException
      */
     public void nextImage() throws IOException {
         if (dirContents.hasNext()) {
         displayImage(dirContents.getNextImage());
         } else {
             alert("Directory finished");
         }
     }
 
     /**
      * Display the previous image in the directory.
      *
      * @throws java.io.IOException
      */
     public void prevImage() throws IOException {
         if (dirContents.getIterator() != 0)
             displayImage(dirContents.getPrevImage());
     }
 }
