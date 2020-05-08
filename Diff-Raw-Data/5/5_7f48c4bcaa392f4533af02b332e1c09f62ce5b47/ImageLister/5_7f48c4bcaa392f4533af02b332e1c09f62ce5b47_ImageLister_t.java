 /* ImageLister.java
  *
  * Jim McBeath, September 18, 2001
  */
 
 package net.jimmc.mimprint;
 
 import net.jimmc.util.FileUtil;
 import net.jimmc.util.MoreException;
 
 import java.awt.AlphaComposite;
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.datatransfer.StringSelection;
 import java.awt.datatransfer.Transferable;
 import java.awt.Dimension;
 import java.awt.Graphics2D;
 import java.awt.Image;
 import java.awt.image.BufferedImage;
 import java.awt.Point;
 import java.awt.Toolkit;
 import java.awt.dnd.DnDConstants;
 import java.awt.dnd.DragGestureEvent;
 import java.awt.dnd.DragGestureListener;
 import java.awt.dnd.DragSource;
 import java.awt.dnd.DragSourceAdapter;
 import java.awt.dnd.DragSourceDropEvent;
 import java.awt.dnd.DragSourceListener;
 import java.awt.dnd.InvalidDnDOperationException;
 import java.io.File;
 import java.io.FilenameFilter;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.Arrays;
 import javax.swing.DefaultListCellRenderer;
 import javax.swing.DefaultListModel;
 import javax.swing.event.ListSelectionListener;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.ImageIcon;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JSplitPane;
 import javax.swing.JTextArea;
 import javax.swing.ListCellRenderer;
 import javax.swing.ListModel;
 
 /** Maintains a list of images and associated information.
  */
 public class ImageLister extends JPanel {
     protected final static int ICON_SIZE = 64;     //TBD - use a preference
             //the size of each icon in the list
     private final static int ICON_LIST_WIDTH=450;
             //width of each list element when showing images and file text
 
     /** Our App. */
     private App app;
 
     /** Our parent window. */
     private Viewer viewer;
 
     private boolean listOnly = true;
     private JSplitPane mainSplitPane;
     private JSplitPane infoSplitPane;
     private JScrollPane listScrollPane;
 
     private DragSourceListener dsListener;
 
     /** The image area which displays the image. */
     private ImageWindow imageWindow;
 
     /** Our list. */
     private JList fileNameList;
     private IconLoader iconLoader;
 
     private int listMode;
         public static final int MODE_NAME = 0;
         public static final int MODE_FULL = 1;
         private static final int MODE_MAX = MODE_FULL; //highest legal value
 
     /** The status area. */
     private JTextArea statusLabel;
 
     /** The label showing the directory info text. */
     private JTextArea dirTextLabel;
 
     /** The label showing the file info text. */
     private JTextArea fileTextLabel;
 
     /** The current directory in which we are displaying files. */
     private File targetDirectory;
 
     /** The file names we are displaying, within the targetDirectory. */
     private String[] fileNames;
     private FileInfo[] fileInfos;
 
     /** The currently displayed image. */
     protected ImageBundle currentImage;
 
     /** The next image in the list. */
     private ImageBundle nextImage;
 
     /** The previous image in the list. */
     private ImageBundle previousImage;
 
     /** The image loader thread, used as a lock object for image loader. */
     private Thread imageLoader;
 
     /** The display updater thread, used as a lock object
      * for display updater. */
     private Thread displayUpdater;
 
     /** Create a new list. */
     public ImageLister(App app, Viewer viewer) {
         super();
         this.app = app;
         this.viewer = viewer;
 
         //statusLabel = new JTextArea("status here");
         //statusLabel.setEditable(false);
             //status line is now in viewer
         dirTextLabel = new JTextArea("dir info here");
         dirTextLabel.setEditable(false);
         fileTextLabel = new JTextArea("file info here");
         fileTextLabel.setEditable(false);
         infoSplitPane = new JSplitPane(
                 JSplitPane.VERTICAL_SPLIT,
                 new JScrollPane(dirTextLabel),
                 new JScrollPane(fileTextLabel));
         //infoSplitPane.setBackground(Color.black);
         //JSplitPane statusInfoPane = new JSplitPane(
                 //JSplitPane.VERTICAL_SPLIT,
                 //statusLabel,infoSplitPane);
 
         fileNameList = new JList();
         fileNameList.addListSelectionListener(
                 new ImageListerListSelectionListener());
         listScrollPane = new JScrollPane(fileNameList);
         listScrollPane.setPreferredSize(new Dimension(600,140));
 
         mainSplitPane = new JSplitPane(
                 JSplitPane.HORIZONTAL_SPLIT,
                 listScrollPane,infoSplitPane);
         mainSplitPane.setDividerLocation(200);
         listOnly = false;
 
         setLayout(new BorderLayout());
         add(mainSplitPane);
 
         initImageLoader();
         initIconLoader();
         initDisplayUpdater();
 
         setupDrag();
     }
 
     //Recognize dragging from the file list
     private void setupDrag() {
         //enable dragging from this list
         DragSource dragSource = DragSource.getDefaultDragSource();
         DragGestureListener dgListener = new ImageListerDragGestureListener();
         dragSource.createDefaultDragGestureRecognizer(
             fileNameList,DnDConstants.ACTION_COPY,dgListener);
 
         dsListener = new DragSourceAdapter() {
             public void dragDropEnd(DragSourceDropEvent ev) {
                 if (imageWindow!=null)
                     imageWindow.requestFocus();
             }
         };
     }
 
     class ImageListerDragGestureListener implements DragGestureListener {
     //The DragGestureListener interface
         public void dragGestureRecognized(DragGestureEvent ev) {
             int index = fileNameList.getSelectedIndex();
             if (index==-1)
                 return;         //no item selected for dragging
             FileInfo fileInfo = getFileInfo(index);
             //String path = new File(targetDirectory,fileNames[index]).toString();
             String path = fileInfo.getPath();
             ImageIcon icon = fileInfo.icon;
             //If the platform does not support image dragging, don't do it
             Image image = null;
             Point offset = null;
             Image iconImage = (icon==null)?null:icon.getImage();
             //Get an image suitable for dragging
             if (DragSource.isDragImageSupported()) {
                 image = app.getImageUtil().createTransparentIconImage(
                         iconImage,path);
                 int width = image.getWidth(null);
                 int height = image.getHeight(null);
                 offset = new Point(-width/2, -height/2);
             } else {
                 image = null;   //image dragging not supported
             }
             try {
                 Transferable transferable = new StringSelection(path);
                 if (image!=null) {
                     ev.startDrag(DragSource.DefaultCopyNoDrop,
                             image, offset,
                             transferable, dsListener);
                 } else {
                     ev.startDrag(DragSource.DefaultCopyNoDrop,
                             transferable, dsListener);
                 }
             } catch (InvalidDnDOperationException ex) {
                 System.err.println(ex); //TODO - better error handling
             }
         }
     //End DragGesture interface
     }
 
     /** Show only the list, not the other status areas. */
     public void showListOnly(boolean t) {
         if (t==listOnly)
             return;
         if (t) {
             //TODO - we should still display the directory info,
             //so "showListOnly" is perhaps not the right thing here
             remove(mainSplitPane);
             add(listScrollPane);
         } else {
             remove(listScrollPane);
             mainSplitPane.setLeftComponent(listScrollPane);
             add(mainSplitPane);
         }
         listOnly = t;
     }
 
     /** Set the mode for what we display in the image file list. */
     public void setListMode(int mode) {
         if (mode<0 || mode>MODE_MAX)
             throw new IllegalArgumentException("Bad mode");
         if (mode==listMode)
             return;         //no change
         synchronized (this) {
             //Change the renderer and the mode within sync block
             switch (mode) {
             default:
             case MODE_NAME:
                 fileNameList.setCellRenderer(new DefaultListCellRenderer());
                 fileNameList.setFixedCellWidth(-1);
                 fileNameList.setFixedCellHeight(-1);
                 break;
             case MODE_FULL:
                 fileNameList.setCellRenderer(new FileListRenderer());
                 //fileNameList.setFixedCellWidth(ICON_LIST_WIDTH);
                 //fileNameList.setFixedCellHeight(ICON_SIZE);
                     //set fixed cell height and width to prevent the list
                     //from rendering every item immediately
                 break;
             }
             listMode = mode;
         }
         //TODO - redisplay the list
     }
 
     /** Initialize our image loader thread. */
     protected void initImageLoader() {
         imageLoader = new Thread() {
             public void run() {
                 imageLoaderRun();
             }
         };
         imageLoader.setPriority(imageLoader.getPriority()-2);
         imageLoader.start();
         app.debugMsg("image loader thread started");
     }
 
     /** Initialize our icon loader thread. */
     private void initIconLoader() {
         iconLoader = new IconLoader(app,this);
         iconLoader.setPriority(iconLoader.getPriority()-3);
         iconLoader.start();
         app.debugMsg("icon loader thread started");
     }
 
     /** Initialize a thread to update our display. */
     protected void initDisplayUpdater() {
         displayUpdater = new Thread() {
             public void run() {
                 displayUpdaterRun();
             }
         };
         displayUpdater.setPriority(imageLoader.getPriority()-1);
         displayUpdater.start();
         app.debugMsg("display updater thread started");
     }
 
     /** Set the ImageWindow. */
     public void setImageWindow(ImageWindow imageWindow) {
         this.imageWindow = imageWindow;
         if (currentImage!=null)
             currentImage.setImageWindow(imageWindow);
         if (nextImage!=null)
             nextImage.setImageWindow(imageWindow);
         if (previousImage!=null)
             previousImage.setImageWindow(imageWindow);
     }
 
     /** Open the specified target.
      * @param target The file or directory to open.
      * @see #open(File)
      */
     public void open(String target) {
         open(new File(target));
     }
 
     /** Select an item from our list. */
     private void setSelectedIndex(int n) {
         try {
             appIsSelecting = true;
             fileNameList.setSelectedIndex(n);
         } finally {
             appIsSelecting = false;
         }
     }
     private boolean appIsSelecting;
 
     /** Activate the currently selected item. */
     public void activateSelection() {
         int n = fileNameList.getSelectedIndex();
         if (n<0)
             return;             //nothing selected
         //Process as if the user selected a value
         //without having the appIsSelecting flag set.
         listValueChanged();
     }
 
     /** Open the specified target.
      * If it is a directory, list all of the image files in the directory
      * and select the first one.
      * If it is a file, list all of the image files in the containing
      * directory, and selected the given file.
      * @param targetFile The file or directory to open.
      */
     public void open(File targetFile) {
         if (!targetFile.exists()) {
             Object[] eArgs = { targetFile.getName() };
             String msg = app.getResourceFormatted("error.NoSuchFile",eArgs);
             viewer.errorDialog(msg);
             return;
         }
         try {
             targetFile = new File(targetFile.getCanonicalPath());
             //Clean up the path,
             //to avoid problems when attempting to traverse dirs.
         } catch (IOException ex) {
             throw new MoreException(ex);
         }
         File formerTargetDirectory = targetDirectory;
         if (targetFile.isDirectory()) {
             //It's a directory, use it
             targetDirectory = targetFile;
             targetFile = null;	//get the real file later
         } else {
             //It's not a directory, get the containing directory
             targetDirectory = targetFile.getParentFile();
             if (targetDirectory==null) {
                 //No parent, so the file must not name a
                 //directory, so the directory must be "."
                 targetDirectory = new File(".");
             }
         }
         currentImage = null;
         nextImage = null;
         previousImage = null;
         fileNames = getListableFileNames(targetDirectory);
         fileInfos = new FileInfo[fileNames.length];
             //Allocate space for the rest of the file info
         for (int i=0; i<fileNames.length; i++) {
             fileInfos[i] = new FileInfo(i,fileNames.length,targetDirectory,
                     fileNames[i]);
         }
         Arrays.sort(fileInfos,new ImageFileNameComparator(targetDirectory));
        String[] sortedFileNames = new String[fileInfos.length];
        for (int i=0; i<fileInfos.length; i++) {
            sortedFileNames[i] = fileInfos[i].name;
        }
        fileNames = sortedFileNames;
         if (formerTargetDirectory==null || targetDirectory==null ||
                 !formerTargetDirectory.toString().equals(
                         targetDirectory.toString())) {
             setDirectoryInfo(targetDirectory);
         }
         fileNameList.setListData(fileNames);
         if (fileNames.length==0) {
             //No files in the list, so don't try to select anything
         } else if (targetFile==null) {
             //No file specified, so select the first file in the dir
             setSelectedIndex(0);
         } else {
             //Find the index of the specified file and select it
             String targetFileName = targetFile.getName();
             int n = Arrays.binarySearch(fileNames,targetFileName);
             if (n<0)
                 n = 0;	//if file not found, select first file
             setSelectedIndex(n);
         }
     }
 
     /** Display new directory info. */
     protected void setDirectoryInfo(File dir) {
         String dirText = "Directory: "+dir.toString();
         try {
             File summaryFile = new File(dir,"summary.txt");
             String dirSummary = FileUtil.readFile(summaryFile);
             if (dirSummary!=null) {
                 if (dirSummary.endsWith("\n")) {
                     dirSummary = dirSummary.substring(0,
                             dirSummary.length()-1);
                 }
                 dirText += "\nSummary: "+dirSummary;
             }
         } catch (Exception ex) {
             //on error, ignore summary
         }
         dirTextLabel.setText(dirText);
     }
 
     protected void setFileText(String info) {
         //Display the text for the image
         if (info==null)
             info = "";
         fileTextLabel.setText(info);
     }
 
     /** Write new text associated with a file.
      * @param path The path to the image file.
      * @param text The text about that image.
      */
     protected void writeFileText(String path, String text) {
         if (path==null) {
             return;	//no file, so no info
         }
         if (text.length()>0 && !text.endsWith("\n"))
             text = text + "\n";	//terminate with a newline
         try {
             String textPath = FileInfo.getTextFileNameForImage(path);
             File f = new File(textPath);
             FileUtil.writeFile(f,text);
         } catch (Exception ex) {
             throw new RuntimeException(ex);  //TBD more info
         }
         displayCurrentImage();	//refresh image text
     }
 
     /** Set the contents of the status area. */
     public void setStatus(String status) {
         //statusLabel.setText(status);
         viewer.showStatus(status);
     }
 
     /** True if the file name is for an image file that we recognize. */
     public boolean isImageFileName(String name) {
         int dotPos = name.lastIndexOf('.');
         if (dotPos<0)
             return false;	//no extension
         String extension = name.substring(dotPos+1).toLowerCase();
         if (extension.equals("gif") || extension.equals("jpg") ||
                 extension.equals("jpeg")) {
             return true;
         }
         return false;
     }
 
     class ImageListerListSelectionListener implements ListSelectionListener {
     //The ListSelectionListener interface
         /** Here when the list selection changes. */
         public void valueChanged(ListSelectionEvent ev) {
             if (listValueChangeInProgress)
                 return;
             try {
                 listValueChangeInProgress = true;
                 listValueChanged();
             } finally {
                 listValueChangeInProgress = false;
             }
         }
         private boolean listValueChangeInProgress;
     //End ListSelectionListener interface
     }
 
     private void listValueChanged() {
         int newSelection = fileNameList.getSelectedIndex();
         if (newSelection<0)
             return;
         FileInfo fi = getFileInfo(newSelection);
         switch (fi.type) {
         case FileInfo.DIR:
             //If the user selected this item, open it;
             //but if the app selected it, don't.  That
             //means we are selecting based on an up or down
             //rather than a direct mouse click.
             if (!appIsSelecting)
                 open(fi.getFile());
             break;
         case FileInfo.IMAGE:
             displayCurrentSelection();
             break;
         case FileInfo.MMP:
             //If the user selected this item, open it,
             //but not if the app selected it.
             if (!appIsSelecting)
                 viewer.loadLayoutTemplate(fi.getFile());
             break;
         default:    //ignore unknown stuff
             break;
         }
         if (imageWindow!=null)
             imageWindow.requestFocus();
     }
 
     /** Show the currently selected file. */
     public void displayCurrentSelection() {
         //If we are displaying an ImagePage, don't make the
         //selection change the window; user can drag an image
         //from the list into the ImagePage window.
         if (imageWindow instanceof ImagePage)
             return;
         if (imageWindow==null)
             return;
         synchronized (displayUpdater) {
             displayUpdater.notifyAll();  //wake up updater
         }
     }
 
     /** Set up our images.
      * @return The path to the current image.
      */
     protected void setupCurrentImage() {
         int newSelection = fileNameList.getSelectedIndex();
         int currentSelection = (currentImage==null)?
                 -1:currentImage.getListIndex();
         if (newSelection==currentSelection)
             return;		//no change, ignore this call
 
         if (newSelection<0) {
             //Nothing selected
             currentImage = null;
             nextImage = null;
             previousImage = null;
             return;
         }
 
         //Most common case: user is advancing through the images
         //one at a time.
         if (newSelection == currentSelection+1 && currentSelection>=0) {
             previousImage = currentImage;
             currentImage = nextImage;
             nextImage = null;
             return;
         }
 
         //Second common case: user is going backwards through the list.
         if (newSelection == currentSelection-1) {
             nextImage = currentImage;
             currentImage = previousImage;
             previousImage = null;
             return;
         }
 
         //Not an adjacent image
         nextImage = null;
         previousImage = null;
 
         File file = new File(targetDirectory,fileNames[newSelection]);
         if (file==null) {
             if (imageWindow!=null)
                 imageWindow.showText("No file");        //TODO i18n
             currentImage = null;
             return;		//nothing there
         }
         if (file.isDirectory()) {
             if (imageWindow!=null)
                 imageWindow.showText("Directory");      //TODO i18n
             currentImage = null;
             return;
         }
         currentImage = new ImageBundle(app,imageWindow,file,newSelection);
     }
 
     /** Set up the next and previous images. */
     protected void setupAdjacentImages() {
         if (!app.useLookAhead())
             return;		//lookahead disabled
         int currentSelection = (currentImage==null)?
                 -1:currentImage.getListIndex();
         int maxIndex = fileNameList.getModel().getSize();
         if (nextImage==null && currentSelection+1<maxIndex) {
             FileInfo fileInfo = getFileInfo(currentSelection+1);
             if (!fileInfo.isDirectory()) {
                 nextImage = new ImageBundle(app,imageWindow,
                         fileInfo.getFile(),currentSelection+1);
                 synchronized(imageLoader) {
                     imageLoader.notifyAll();
                             //start imageLoader
                 }
             }
         }
         if (previousImage==null && currentSelection-1>=0) {
             FileInfo fileInfo = getFileInfo(currentSelection-1);
             if (!fileInfo.isDirectory()) {
                 previousImage = new ImageBundle(app,imageWindow,
                         fileInfo.getFile(), currentSelection-1);
                 synchronized(imageLoader) {
                     imageLoader.notifyAll();
                             //start imageLoader
                 }
             }
         }
     }
 
     /** Display the current image. */
     protected void displayCurrentImage() {
         String path;
         setFileText(null);	//clear image text while changing
         if (currentImage==null) {
             path = null;
             if (imageWindow!=null)
                 imageWindow.showText("No image");
         } else {
             path = currentImage.getPath();
             int index = currentImage.getListIndex();
             FileInfo fileInfo = getFileInfo(index);
             String imageTextInfo = fileInfo.info;
             setFileText(imageTextInfo);
             if (imageWindow!=null)
                 imageWindow.showImage(currentImage,imageTextInfo);
         }
         viewer.setTitleFileName(path);
     }
 
     /** Get the text for the current file.
      * If no text, return an empty string.
      * If no file selected, return null.
      */
     protected String getCurrentImageFileText() {
         if (currentImage==null)
             return null;
         int index = currentImage.getListIndex();
         FileInfo fileInfo = getFileInfo(index);
         if (fileInfo.text!=null)
             return fileInfo.text;
         return "";
     }
 
     protected void setCurrentImageFileText(String imageText) {
         if (currentImage==null)
             throw new RuntimeException("No image selected"); //TODO i18n?
         String path = currentImage.getPath();
         writeFileText(path,imageText);
     }
 
     protected String getCurrentImageFileInfo() {
         if (currentImage==null)
             return null;
         int index = currentImage.getListIndex();
         FileInfo fileInfo = getFileInfo(index);
         return fileInfo.info;
     }
 
     /** Move to previous directory */
     public void left() {
         File newDir = getPreviousDirectory(targetDirectory);
         if (newDir==null) {
             String eMsg = "No previous directory";
             viewer.errorDialog(eMsg);
             return;
         }
         File lastFile = getLastFileInDir(newDir);
         if (lastFile!=null)
             open(lastFile);
         else
             open(newDir);	//TBD - skip back farther?
     }
 
     /** Move to next directory */
     public void right() {
         File newDir = getNextDirectory(targetDirectory);
         if (newDir==null) {
             String eMsg = "No next directory";
             viewer.errorDialog(eMsg);
             return;
         }
         open(newDir);	//TBD - skip forward farther if no imgs?
     }
 
     /** Move the selection up one item and show that file. */
     public void up() {
         move(-1);
     }
 
     /** Move the selection down one item and show that file. */
     public void down() {
         move(1);
     }
 
     /** Move the selection by the specified amount and show that file. */
     public void move(int inc) {
         int sel = fileNameList.getSelectedIndex();
         sel += inc;
         int maxIndex = fileNameList.getModel().getSize();
         /*
         while (sel>=0 && sel<maxIndex && isDirectory(sel))
             sel += inc;         //skip non-images when using arrows from image area
         */
         if (sel<0) {
             String prompt = "At beginning; move to previous dir?";
                     //TBD i18n this section
             if (!viewer.confirmDialog(prompt))
                 return;		//cancelled
             //User is trying to move off the beginning of the list,
             //see about moving back to the previous directory
             left();
             return;
         }
         if (sel>=maxIndex) {
             String prompt = "At end; move to next dir?";
                     //TBD i18n this section
             if (!viewer.confirmDialog(prompt))
                 return;		//cancelled
             //User is trying to move off the end of the list,
             //see about moving forward to the next directory
             right();
             return;
         }
         setSelectedIndex(sel);
         fileNameList.ensureIndexIsVisible(sel);
         displayCurrentSelection();
     }
 
     /** The image loader thread, which loads images in the background. */
     public void imageLoaderRun() {
         app.debugMsg("image loader thread running");
         while (true) {
             synchronized(imageLoader) {
                 try {
                     app.debugMsg("image loader thread waiting");
                     imageLoader.wait();
                 } catch (InterruptedException ex) {
                     //ignore
                 }
             }
             try {
                 //Do this outside of the sync
                 Thread.sleep(100);
             } catch (InterruptedException ex) {
                 //ignore
             }
             app.debugMsg("image loader thread awakened");
             if (nextImage!=null) {
                 setStatus("Loading next image");
                 setCursorBusy(true);
                 app.debugMsg("imageLoader load next image");
                 nextImage.loadTransformedImage();
                 app.debugMsg("imageLoader done next image");
                 setCursorBusy(false);
                 setStatus("");
             }
             if (previousImage!=null) {
                 setStatus("Loading previous image");
                 setCursorBusy(true);
                 app.debugMsg("imageLoader load prev image");
                 previousImage.loadTransformedImage();
                 app.debugMsg("imageLoader done prev image");
                 setCursorBusy(false);
                 setStatus("");
             }
         }
     }
 
     /** The display updater thread, which handles requests to update the
      * display so that the Event thread will be freed up.
      */
     public void displayUpdaterRun() {
         app.debugMsg("display updater thread running");
         while (true) {
             //Check to see if the list selection changed while
             //we were busy updating.  If so, don't do the wait.
             int newSelection = fileNameList.getSelectedIndex();
 	    if (newSelection>=0) {
 		FileInfo fileInfo = getFileInfo(newSelection);
 		if (fileInfo.isDirectory())
 		    newSelection = -1;	//no image file selected
 	    }
             int currentSelection = (currentImage==null)?
                     -1:currentImage.getListIndex();
             if (newSelection==currentSelection) {
                 //selection is correct, wait for a notify
                 synchronized(displayUpdater) {
                     try {
                         app.debugMsg(
                             "display updater thread waiting");
                         displayUpdater.wait();
                     } catch (InterruptedException ex) {
                         //ignore
                     }
                     app.debugMsg("display updater thread awakened");
                 }
             } else {
                 app.debugMsg("display updater thread no wait");
             }
             //Update the display
             setupCurrentImage();
             displayCurrentImage();
             setupAdjacentImages();
         }
     }
 
     /** Return a list of all the filenames in this dir we want to display. */
     private String[] getListableFileNames(File dir) {
         FilenameFilter filter = new FilenameFilter() {
             public boolean accept(File dir, String name) {
                 return isImageFileName(name) ||
                     FileInfo.isOurFileName(name) ||
                     new File(dir,name).isDirectory();
             }
         };
         String[] list = dir.list(filter);
         if (list==null)
             list = new String[0];
         String[] listPlus = new String[list.length+2];
         listPlus[0] = ".";
         listPlus[1] = "..";
         System.arraycopy(list,0,listPlus,2,list.length);
         return listPlus;
     }
 
     /** Get the list of file infos. */
     protected FileInfo[] getFileInfoList() {
         return fileInfos;
     }
 
     //Get the FileInfo for the specified file in the fileNames list
     private FileInfo getFileInfo(int index) {
         FileInfo fileInfo = fileInfos[index];
         if (fileInfo!=null && fileInfo.infoLoaded)
             return fileInfo;        //already loaded
         //int totalCount = fileNameList.getModel().getSize();
         //fileInfo = new FileInfo(index,totalCount,targetDirectory,fileNames[index]);
         fileInfo.loadInfo();
 
         //leave icon null, let iconLoader fill it in
         //fileInfos[index] = fileInfo;
         iconLoader.moreIcons();         //tell iconLoader to load icons
         return fileInfo;
     }
 
     private boolean isDirectory(int index) {
         FileInfo fi = getFileInfo(index);
         return (fi.type==FileInfo.DIR);
     }
 
     /** Here when the iconLoader has loaded another icon.
      * @param fileInfos The list of file items the icon loader is
      *        working on.
      * @param n The index into that list of the loaded icon.
      * @return True if we are still using that list, false if
      *         we have moved on to another list.
      */
     public boolean iconLoaded(FileInfo[] fileInfos, int n) {
         if (fileInfos!=this.fileInfos) {
             //Our list of files has changed, ignore this call
             //and let the caller know.
             return false;
         }
         //Refresh that list item
         //We just want to tell the list to recalculate the size
         //of the cell we have just updated, but we can't fire off
         //a change notification here because that method of the
         //model is protected.  Calling revalidate doesn't do the
         //trick, so we hack it by setting the renderer again.
         //Need to check our list mode and only do this if we are
         //in the right mode; need to do that check within a sync
         //block to prevent race condition.
         synchronized (this) {
             if (listMode==MODE_FULL) {
                 fileNameList.setCellRenderer(new FileListRenderer());
             }
         }
         return true;
     }
 
     /** Given a directory, get the next sibling directory. */
     protected File getNextDirectory(File dir) {
         return getRelativeDirectory(dir,1);
     }
 
     protected File getPreviousDirectory(File dir) {
         return getRelativeDirectory(dir,-1);
     }
 
     protected File getRelativeDirectory(File dir, int move) {
         File parentDir = dir.getParentFile();
         if (parentDir==null)
             parentDir = new File(".");
         String[] siblings = parentDir.list();
         int dirIndex=0;
         if (siblings!=null) {
             Arrays.sort(siblings);
             String dirName = dir.getName();
             dirIndex = Arrays.binarySearch(siblings,dirName);
             if (dirIndex<0) {
                 String msg = "Can't find dir "+dirName+
                         " in parent list";
                 throw new RuntimeException(msg);
             }
         }
         int newDirIndex = dirIndex + move;
         while (siblings==null || newDirIndex<0 || newDirIndex>=siblings.length) {
             //We are at the end/start of our sibling directories,
             //so recurse up the directory tree and move the
             //parent to the next directory.
             parentDir = getRelativeDirectory(parentDir,move);
             if (parentDir==null)
                 return null;
             siblings = parentDir.list();
             if (siblings==null || siblings.length==0)
                 continue;	//no files, try next dir
             Arrays.sort(siblings);
             if (newDirIndex<0)	//backing up
                 newDirIndex = siblings.length-1;
             else
                 newDirIndex = 0;
         }
         return new File(parentDir,siblings[newDirIndex]);
     }
 
     /** Given a directory, get the last image file in that directory. */
     protected File getLastFileInDir(File dir) {
         String[] names = getListableFileNames(dir);
         if (names==null || names.length==0)
             return null;
         return new File(dir,names[names.length-1]);
     }
 
     private void setCursorBusy(boolean busy) {
         if (imageWindow==null)
             return;
         imageWindow.setCursorBusy(busy);
     }
 
     class FileListRenderer extends DefaultListCellRenderer {
         public Component getListCellRendererComponent(JList list,
                 Object value, int index,
                 boolean isSelected, boolean cellHasFocus) {
             JLabel cell = (JLabel)super.getListCellRendererComponent(
                     list, value, index, isSelected, cellHasFocus);
             FileInfo fileInfo = getFileInfo(index);
             String fileInfoText = fileInfo.html;
             String labelText;
             if (fileInfoText==null) {
                 labelText = fileInfo.name;
             } else {
                 labelText = fileInfoText;
                     //label doesn't normally do newlines, so we use html and
                     //<br> tags instead.
             }
             cell.setText(labelText);
             //cell.setVerticalAlignment(TOP);      //put text at top left
             //cell.setHorizontalAlignment(LEFT);
             cell.setIcon(fileInfo.icon);
             /* the rest is handled by superclass...
             if (isSelected) {
                 cell.setBackground(list.getSelectionBackground());
                 cell.setForeground(list.getSelectionForeground());
             } else {
                 cell.setBackground(list.getBackground());
                 cell.setForeground(list.getForeground());
             }
             cell.setEnabled(list.isEnabled());
             cell.setFont(list.getFont());
             */
             return cell;
         }
     }
 }
 
 /* end */
