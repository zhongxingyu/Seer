 import java.awt.*;
 import java.awt.event.*;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 import java.util.Iterator;
 import java.util.LinkedList;
 
 import javax.imageio.ImageIO;
 import javax.swing.*;
 import javax.swing.filechooser.FileNameExtensionFilter;
 
 /**
  * Class used to modify all of the map data.  
  * Handles user interactions with the menu bar as well as the mouse.
  * Basically is the brains of the project.
  * @author Charles
  *
  */
 public class MapEditor extends JLabel implements Scrollable, ActionListener, MouseMotionListener, MouseListener {
 
 	private static final long serialVersionUID = -7536479136945827608L;
 	private int maxUnitIncrement = 1;
     private boolean missingPicture = false;
     private ImageIcon image;
     private MapContainer mapData;
     private boolean isSelecting;
     private boolean isFilling;
     private boolean init = true;
     private LinkedList<SaveCommand> saveQueue;
     private CopyChunks copyChunkList;
     private MapDeleter parent;
     private TreeGeneratorDialog treeDialog;
     private TerraformingDialog terraformDialog;
     private int zOffset;
     private int xOffset;
     private int viewMode;
     private int drawMode;
     private int blockSelectRadius;
     
     public static final int CHUNK_SELECT = 0;
     public static final int BLOCK_SELECT = 1;
     public static final int COLORED_VIEW = 10;
     public static final int TOPOGRAPHIC = 11;
     public static final int TOPOGRAPHIC_LINE_DISTANCE = 10;
 
 
     /**
      * Make the map editor with the image of the map. 
      * @param i The image of the map.
      * @param parent The MapDeleter that holds the MapEditor.
      */
     public MapEditor(ImageIcon i, MapDeleter parent) {
         super(i);
         this.blockSelectRadius = 5;
         this.parent = parent;
         this.image = i;
         this.saveQueue = new LinkedList<SaveCommand>();
         //TODO CHANGE THESE BACKS
         this.viewMode = COLORED_VIEW;
         this.drawMode = CHUNK_SELECT;
         //Let the user scroll by dragging to outside the window.
         setAutoscrolls(true); //enable synthetic drag events
         
         addMouseMotionListener(this); //handle mouse drags
         addMouseListener(this);
     }
 
 	public enum OS {
 		WINDOWS, WINDOWS_7, UNIX, MAC, OTHER
 	};
 	
 	private String findMinecraftDataFolder() {
 		String mcPath = "";
 
 		OS os = identifyOS();
 		switch (os) {
 		case WINDOWS:
 		case WINDOWS_7:
 			mcPath = System.getenv("APPDATA") + "\\.minecraft";
 			break;
 		case UNIX:
 			mcPath = System.getProperty("user.home") + "/.minecraft";
 			break;
 		case MAC:
 			mcPath = System.getProperty("user.home") + "/Library/Application Support/minecraft";
 			break;
 		default:
 			break;
 		}
 
 		return mcPath;
 	}
 
 	public OS identifyOS() {
 		String os = null;
 		String version = null;
 
 		os = System.getProperty("os.name").toLowerCase();
 		version = System.getProperty("os.version");
 
 		if (os.indexOf("win") > -1) {
 			if (version.equals("6.1"))
 				return OS.WINDOWS_7;
 			if (version.equals("6.0")) {
 				return OS.WINDOWS;
 			}
 			return OS.WINDOWS;
 		}
 		if ((os.indexOf("nix") > -1) || (os.indexOf("nux") > -1))
 			return OS.UNIX;
 		if (os.indexOf("mac") > -1) {
 			return OS.MAC;
 		}
 		return OS.OTHER;
 	}
     /*
      * Takes care of all the the buttons in the MapDeleter.
      * (non-Javadoc)
      * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
      */
     @Override
 	public void actionPerformed(ActionEvent e) {
     	//Open a world.
     	if(e.getActionCommand().compareTo("Open (World Directory)") == 0){
 			JFileChooser getDirToOpen = new JFileChooser(findMinecraftDataFolder() + "/saves/");
 			getDirToOpen.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
 			int returnVal = getDirToOpen.showOpenDialog(this);
 			if(returnVal == JFileChooser.APPROVE_OPTION){
 				File file = getDirToOpen.getSelectedFile();
 				if(file.isDirectory()){
 					this.loadMap(file.getPath());
 				}
 			}
 			return;
 		}
     	
     	//Exit the program.
 		if(e.getActionCommand().compareTo("Exit") == 0){
 			return;
 		}
 
 		//View the colored version of your map.
 		if(e.getActionCommand().compareTo("View Colored Map") == 0){
 			if(this.viewMode == COLORED_VIEW)
 				return;
 			
 			this.viewMode = COLORED_VIEW;
 			if(!this.init){
 				this.repaintAllChunks();
 			}
 			return;
 		}
 		//View the Topographic version of the map.
 		if(e.getActionCommand().compareTo("View Topographic Map") == 0){
 			if(this.viewMode == TOPOGRAPHIC)
 				return;
 			this.viewMode = TOPOGRAPHIC;
 			if(!this.init){
 				this.repaintAllChunks();
 			}
 			return;
 		}
 		//Use chunk selection to select Chunks.
 		if(e.getActionCommand().compareTo("Chunk Selection") == 0){
 			this.drawMode = CHUNK_SELECT;
 			return;
 		}
 		//Use Block Selection to select blocks.
 		if(e.getActionCommand().compareTo("Block Selection") == 0){
 			this.drawMode = BLOCK_SELECT;
 			return;
 		}
 		//Safety device!  Don't let any of the following actions be executed 
 		//unless there is a map loaded.
 		if(this.init)
     		return;
 		
 		//-------- Dialog Buttons -------\\
 		if(e.getActionCommand().compareTo("Generate") == 0){
 			this.generateTrees();
 		}
 		
     	//--------  Top Button Panel Events  -----------\\  
     	System.out.println(e.getActionCommand());
 		if(e.getActionCommand().compareTo("Fill Selection") == 0){
 			JToggleButton tempButton = (JToggleButton) e.getSource();
 			this.isFilling = tempButton.isSelected();
 			if(tempButton.isSelected())
 				setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
 			else
 				setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
 			return;
 		}
 
 		
 		if(e.getActionCommand().compareTo("Invert Selection") == 0){
 			this.invertAll();
 			return;
 		}
 		if(e.getActionCommand().compareTo("Select All") == 0){
 			this.selectAll();
 			return;
 		}
 		if(e.getActionCommand().compareTo("Unselect All") == 0){
 			this.unselectAll();
 			return;
 		}
 		
 		//--------  Menu Bar Events  -----------\\
 		
 		//File Menu
 
 		
 		if(e.getActionCommand().compareTo("Save") == 0){
 			int save = JOptionPane.showConfirmDialog(this, "Are you positive you want to save/override your map? \n (make sure you have a backup)", "Save Map", JOptionPane.YES_NO_OPTION);
 			if(save == 0){
 				MapSaver mSaver = new MapSaver(this.saveQueue, this.parent);
 				mSaver.save();
 				this.saveQueue = new LinkedList<SaveCommand>();
 			}
 			
 			return;
 		}
 		
 		if(e.getActionCommand().compareTo("Save as PNG") == 0){
 			JFileChooser savePNGChooser = new JFileChooser();
 			savePNGChooser.setFileSelectionMode(JFileChooser.SAVE_DIALOG);
 			FileNameExtensionFilter pngFilter = new FileNameExtensionFilter("*.PNG (.png)",".png", ".PNG");
 			savePNGChooser.addChoosableFileFilter(pngFilter);
 			savePNGChooser.setSelectedFile(new File("Hurrdurr.png"));
 			savePNGChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
 			int returnVal = savePNGChooser.showSaveDialog(this);
 			if(returnVal == JFileChooser.APPROVE_OPTION){
 				File file = savePNGChooser.getSelectedFile();
 				System.out.println(file);
 				if(!file.isDirectory()){
 					System.out.println("Is a file");
 					Image tempImage = this.image.getImage();
 					BufferedImage saveImage = new BufferedImage(this.mapData.getMapWidth() * 16,this.mapData.getMapHeight() * 16,BufferedImage.TYPE_INT_RGB);
 					saveImage.getGraphics().drawImage(tempImage, 0, 0, new Color(0,0,0), this.image.getImageObserver());
 					try {
 						ImageIO.write(saveImage, "png", file);
 					} catch (IOException e1) {
 						// TODO Auto-generated catch block
 						e1.printStackTrace();
 					}
 				}
 			}
 			return;
 		}
 		
 		// Map Modification Menu
 
 		if(e.getActionCommand().compareTo("Copy Selection") == 0){
 			this.copyChunks();
 			return;
 		}
 		if(e.getActionCommand().compareTo("Paste Selection") == 0){
 			this.pasteChunks();
 			return;
 		}
 		if(e.getActionCommand().compareTo("Delete Selection") == 0){
 			LinkedList<Coordinates> tempList = this.mapData.getSelectedIds();
 			Iterator<Coordinates> iter = tempList.iterator();
 			while(iter.hasNext()){
 				Coordinates temp = iter.next();
 				saveQueue.add(new SaveCommand(SaveCommand.DELETE_COMMAND, this.mapData.getChunk(temp.getZ(), temp.getX()).getFilePath()));
 				this.deleteChunk(temp.getZ(), temp.getX());
 			}
 			return;
 		}
 		if(e.getActionCommand().compareTo("Generate Trees") == 0){
 			this.treeDialog = new TreeGeneratorDialog(this.parent);
 			this.treeDialog.setVisible(true);
 			return;
 		}
 		
 		//----- Teraform Selections -----\\
 		System.out.println(e.getActionCommand());
 		if(e.getActionCommand().compareTo("Terraform Selection") == 0){
 			this.terraformDialog = new TerraformingDialog(this.parent, this.mapData);
 			this.terraformDialog.setVisible(true);
 			return;
 		}
 		if(e.getActionCommand().compareTo("Preview") == 0){
 			this.terraformDialog.setPreviewHeights();
 			this.repaintSelectedChunks();
 			return;
 		}
 		if(e.getActionCommand().compareTo("Commit") == 0){
 			this.terraformDialog.setPreviewHeights();
 			this.saveQueue.addAll(this.terraformDialog.getSaveCommands());
 			this.unselectAll();
 		}
 			
 		
 	}
 
     /**
      * Command for copying the selected chunks.
      */
     private void copyChunks(){
     	LinkedList<MapChunk> tempChunks = this.mapData.getSelectedChunks();
     	this.copyChunkList = new CopyChunks(tempChunks);
     	
     }
     
     /**
      * Command for deleting a chunk at the given coordinates.
      * @param z Z-Position of the chunk.
      * @param x X-Position of the chunk.
      */
     private void deleteChunk(int z, int x){
     	this.saveQueue.add(new SaveCommand(SaveCommand.DELETE_COMMAND, this.mapData.getChunk(z, x).getFilePath()));
     	this.mapData.deleteChunk(z, x);
     	this.repaintChunk(z, x);
     }
     
     /**
      * Select Blocks connected to tempCoord.
      * @param z
      * @param x
      * @param tempCoord
      * @param selecting
      * @return
      */
     private boolean fillBlocks(int z, int x, LinkedList<Coordinates> tempCoord, boolean selecting ){
     	if(!this.mapData.containsChunk(z / 16, x / 16))
     		return true;
     	if(!this.mapData.hasBlocksSelected(z / 16, x / 16)){
     		fill(z, x, selecting);
     		return true;
     	}
     	LinkedList<Coordinates> returnC = this.mapData.fillMapChunkSelected(z / 16, x / 16, tempCoord);
 
     	LinkedList<Coordinates> fillNorth = new LinkedList<Coordinates>();
     	LinkedList<Coordinates> fillEast = new LinkedList<Coordinates>();
     	LinkedList<Coordinates> fillSouth = new LinkedList<Coordinates>();
     	LinkedList<Coordinates> fillWest = new LinkedList<Coordinates>();
     	
     	if(returnC == null)
     		return true;
     	
     	Iterator<Coordinates> fiter = returnC.iterator();
     	while(fiter.hasNext()){
     		Coordinates tempCo = fiter.next();
     		if(tempCo.getZ() == 0)
     			fillNorth.add(new Coordinates(15, tempCo.getX()));
     		if(tempCo.getX() == 15)
     			fillEast.add(new Coordinates(tempCo.getZ(), 0));
     		if(tempCo.getZ() == 15)
     			fillSouth.add(new Coordinates(0, tempCo.getX()));
     		if(tempCo.getX() == 0)
     			fillWest.add(new Coordinates(tempCo.getZ(), 15));
     	}
     	if(fillNorth.size() > 0)
     		fillBlocks(z - 16, x, fillNorth, selecting);
     	if(fillEast.size() > 0)
     		fillBlocks(z, x + 16, fillEast, selecting);
     	if(fillSouth.size() > 0)
     		fillBlocks(z + 16, x, fillSouth, selecting);
     	if(fillWest.size() > 0)
     		fillBlocks(z, x - 16, fillWest, selecting);
     	return true;
     }
     
     /**
      * Go through each chunk and re-render it.
      */
 	public void repaintAllChunks(){
 		int numChunks = this.mapData.getChunkList().size();
 		int numCurrChunk = 0;
 		Iterator<MapChunk> iter = this.mapData.getChunkList().iterator();
 		while(iter.hasNext()){
 			this.parent.setProgressBarValue(((int)(((float)numCurrChunk / (float)numChunks) * 100)), "Drawing Map");
 			this.repaintChunk(iter.next());
 			numCurrChunk++;
 		}
 		this.parent.progressBarComplete("Success");
 	}
 	
 	/**
 	 * Fill a full chunk.
 	 * @param z
 	 * @param x
 	 * @param selecting
 	 * @return
 	 */
 	private boolean fill(int z, int x, boolean selecting){
 		//TODO FIX ADDING SHIT
 		int chunkX = x / 16;
 		int chunkZ = z / 16;
 		if(!this.mapData.containsChunk(chunkZ, chunkX) || this.mapData.isChunkSelected(chunkZ, chunkX))
 			return true;
 		this.mapData.setSelected(chunkZ, chunkX, true);
 		LinkedList<Coordinates> tempCoord = new LinkedList<Coordinates>();
 		//NORTH
 		if(this.mapData.containsChunk(chunkZ - 1, chunkX) && this.mapData.isChunkSelected(chunkZ - 1, chunkX) != selecting){
 			if(this.mapData.hasBlocksSelected(chunkZ - 1, chunkX)){
 				tempCoord = new LinkedList<Coordinates>();
 				for(int i = 0; i < 16; i++){
 					tempCoord.add(new Coordinates(15, i));
 				}
 				this.fillBlocks((chunkZ - 1) * 16, chunkX * 16,tempCoord, selecting);
 			}
 			else
 				this.fill((chunkZ - 1) * 16, chunkX * 16, selecting);
 		}
 		//WEST
 		if(this.mapData.containsChunk(chunkZ, chunkX - 1) && this.mapData.isChunkSelected(chunkZ, chunkX - 1) != selecting){
 			if(this.mapData.hasBlocksSelected(chunkZ, chunkX - 1)){
 				tempCoord = new LinkedList<Coordinates>();
 				for(int i = 0; i < 16; i++){
 					tempCoord.add(new Coordinates(i, 15));
 				}
 				this.fillBlocks(chunkZ * 16, (chunkX - 1) * 16, tempCoord, selecting);
 			}
 			else
 				this.fill(chunkZ * 16, (chunkX - 1) * 16 , selecting);
 		}
 		//SOUTH
 		if(this.mapData.containsChunk(chunkZ + 1, chunkX) && this.mapData.isChunkSelected(chunkZ + 1, chunkX) != selecting){
 			if(this.mapData.hasBlocksSelected(chunkZ + 1, chunkX)){
 				tempCoord = new LinkedList<Coordinates>();
 				for(int i = 0; i < 16; i++){
 					tempCoord.add(new Coordinates(0, i));
 				}
 				this.fillBlocks((chunkZ + 1) * 16, chunkX * 16, tempCoord, selecting);
 			}
 			else
 				this.fill((chunkZ + 1) * 16, chunkX * 16, selecting);
 		}
 		//EAST
 		if(this.mapData.containsChunk(chunkZ, chunkX + 1) && this.mapData.isChunkSelected(chunkZ, chunkX + 1) != selecting){	
 			if(this.mapData.hasBlocksSelected(chunkZ, chunkX + 1)){
 				tempCoord = new LinkedList<Coordinates>();
 				for(int i = 0; i < 16; i++){
 					tempCoord.add(new Coordinates(i, 0));
 				}
 				this.fillBlocks(chunkZ * 16, (chunkX + 1) * 16 , tempCoord, selecting);
 			}
 			else
 				this.fill(chunkZ * 16, (chunkX + 1) * 16, selecting);
 		}
 		return true;
 	}
 
 	/**
 	 * Get the default size of the program.
 	 */
 	public Dimension getPreferredSize() {
         if (missingPicture) {
             return new Dimension(320, 480);
         } else {
             return super.getPreferredSize();
         }
     }
 
 	/**
 	 * Get the default size of the viewed map.
 	 */
     public Dimension getPreferredScrollableViewportSize() {
         return getPreferredSize();
     }
 
     @Override
     public boolean getScrollableTracksViewportWidth() {
 	    return false;
 	}
 
     @Override
 	public boolean getScrollableTracksViewportHeight() {
 	    return false;
 	}
 
     /**
      * Loads the world from the given file path.
      * @param file The file path of the world.
      */
 	public void loadMap(String file){
 		if(file.compareTo("") == 0){
 			file = "src/images/mapdeleterthingy.JPG";
 			this.image.setImage(new ImageIcon(getClass().getResource("images/mapdeleterthingy.jpg")).getImage());
 			this.init = true;
 		}
 		else{
 			this.init = false;
 		    mapData = null;
 		    isSelecting = false;
 		    isFilling = false;
 		    saveQueue = new LinkedList<SaveCommand>();
 		    this.mapData = new MapContainer(file, this.parent);
 		    this.zOffset = this.mapData.getZOffset();
 		    this.xOffset = this.mapData.getXOffset();
 		    System.out.println("zOffset: " + this.zOffset);
 		    System.out.println("xOffset: " + this.xOffset);
 	    	this.image.setImage(this.mapData.createImage());
 	    	if(this.viewMode == TOPOGRAPHIC){
 	    		Iterator<MapChunk> iter = this.mapData.getChunkList().iterator();
 				while(iter.hasNext()){
 					this.repaintChunk(iter.next());
 				}
 				return;
 	    	}
 	    		
 		}
 	    	this.repaint();
 	    	this.parent.scrollPaneUpdate();
     }
     
     @Override
 	public void mouseClicked(MouseEvent e) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void mouseEntered(MouseEvent e) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	//Methods required by the MouseMotionListener interface:
 	@Override
 	public void mouseMoved(MouseEvent e) { }
 
 	@Override
 	public void mouseDragged(MouseEvent e) {
 		if(this.init)
 			return;
 		int z = e.getY() / 16;
 		int x = e.getX() / 16;
 		//Drag the chunk and see if it should selected or unselect the chunks the mouse rolls over.
 		if(this.drawMode == CHUNK_SELECT){
 		    if(this.isSelecting != this.mapData.isChunkSelected(z, x)){
 		    	this.mapData.setSelected(z, x, this.isSelecting);
 		    	this.repaintChunk(z, x);
 		    }
 	    }
 		else{
 			this.selectCircle(e.getY(), e.getX(), this.isSelecting, this.blockSelectRadius);
 		}
 	}
 	
 	/**
 	 * Select blocks in a circle with the given radius.
 	 * @param z Z-Position of the center block.
 	 * @param x X-Position of the center block.
 	 * @param select - Should select or unselect the blocks.
 	 * @param radius - Radius of the circle to select the blocks.
 	 */
 	private void selectCircle(int z, int x, boolean select, int radius){
 		//TODO Scale this up.
 		System.out.println("Selecting Block: z|" + z + " x|" + x);
 		for(int i = 0; i <= radius; i++){
 			int tempInt = (int) Math.round(Math.sqrt(Math.pow(radius, 2.0) - Math.pow(i, 2.0)));
 			//Quadrant I
 			this.mapData.setSelectedBlock(z + tempInt, x + i, select);
 			this.mapData.setSelectedBlock(z + i, x + tempInt, select);
 			//Quadrant II
 			this.mapData.setSelectedBlock(z + tempInt, x - i, select);
 			this.mapData.setSelectedBlock(z + i, x - tempInt, select);
 			//Quadrant II
 			this.mapData.setSelectedBlock(z - tempInt, x - i, select);
 			this.mapData.setSelectedBlock(z - i, x - tempInt, select);
 			//Quadrant VI
 			this.mapData.setSelectedBlock(z - tempInt, x + i, select);
 			this.mapData.setSelectedBlock(z - i, x + tempInt, select);
 			for(int tx = x - tempInt; tx < x + tempInt; tx++){
 				this.mapData.setSelectedBlock(z - i, tx, select);
 				this.mapData.setSelectedBlock(z + i, tx, select);
 			}
 		}
 		
 		this.repaintChunk((z + radius) / 16, (x + radius) / 16);
 		this.repaintChunk((z + radius) / 16, (x - radius) / 16);
 		this.repaintChunk((z - radius) / 16, (x + radius) / 16);
 		this.repaintChunk((z - radius) / 16, (x - radius) / 16);
 	}
 	
 	@Override
 	public void mouseExited(MouseEvent e) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void mousePressed(MouseEvent e) {
 		if(this.init)
 			return;
 		int x = e.getX() / 16;
 		int z = e.getY() / 16;
 		
 		//Check to see if the program should start filling the selected area.
 		if(this.isFilling){
 			this.parent.setProgressBarValue(100, "Filling Selection");
 			if(this.mapData.hasBlocksSelected(x, z)){
 				LinkedList<Coordinates> tempList = new LinkedList<Coordinates>();
 				tempList.add(new Coordinates(e.getY() % 16, e.getX() % 16));
 				fillBlocks(e.getY(), e.getX(), tempList, true);
 			}
 			else
 				fill(e.getY(), e.getX(), true);
 			this.parent.progressBarComplete("Finished");
 			repaintSelectedChunks();
 			
 		}
 		//Without the fill just select or unselect some blocks or chunks.
 		else{
 			if(this.drawMode == CHUNK_SELECT){
 				if(!this.mapData.isChunkSelected(e.getY() / 16, e.getX() / 16)){
 					this.mapData.setSelected(z, x, true);
 					this.isSelecting = true;
 					repaintChunk(z, x);
 				}
 				else{
 					this.unselectChunk(z, x);
 					this.isSelecting = false;
 					repaintChunk(z, x);
 				}
 			}
 			else if(this.drawMode == BLOCK_SELECT){
 				if(!this.mapData.isBlockSelected(e.getY(), e.getX())){
 					this.selectCircle(e.getY(), e.getX(), true, 5);
 					this.isSelecting = true;
 				}
 				else{
 					this.selectCircle(e.getY(), e.getX(), false, 5);
 					this.isSelecting = false;
 				}
 			}
 		}
 	}
 
 	@Override
 	public void mouseReleased(MouseEvent e) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	/**
 	 * Paste chunks that are currently stored in copyChunkList.
 	 */
 	public void pasteChunks(){
 		
 		MapChunkSelected[][] tempSelected = this.mapData.getSelectedArray();
 		int zSmall = Integer.MAX_VALUE;
     	int zLarge = Integer.MIN_VALUE;
     	int xSmall = Integer.MAX_VALUE;
     	int xLarge = Integer.MIN_VALUE;
     	for(int z = 0; z < tempSelected.length; z++){
     		for(int x = 0; x < tempSelected[0].length; x++){
     			if(this.mapData.isChunkSelected(z, x)){
     				if(z < zSmall)
 		    			zSmall = z;
 		    		if(z > zLarge)
 		    			zLarge = z;
 		    		if(x < xSmall)
 		    			xSmall = x;
 		    		if(x > xLarge)
 		    			xLarge = x;
 		   		}
     		}
     	}
     	boolean[][] checkArray = new boolean[zLarge - zSmall + 1][xLarge - xSmall + 1];
     	System.out.println((zSmall) + " | " + (xSmall) + " | " + tempSelected[zSmall][xSmall]);	
     	for(int z = zSmall; z < zLarge + 1; z++){
     		for(int x = xSmall; x < xLarge + 1; x++){
     			checkArray[z - zSmall][x - xSmall] = tempSelected[z][x].isChunkSelected();
     		}
     	}
     	if(this.copyChunkList.canPaste(checkArray)){
     		System.out.println("CopyChunk Can Print!");
     		MapChunk[][] tempChunks = this.copyChunkList.getChunks();
     		for(int z = 0; z < tempChunks.length; z++){
     			for(int x = 0; x < tempChunks[0].length; x++){
     				if(tempChunks[z][x] != null){
 	    				this.mapData.getChunk(zSmall + z, xSmall + x).setTopBlockMap(tempChunks[z][x].getTopBlockArray());
 	    				this.mapData.getChunk(zSmall + z, xSmall + x).setHeightMap(tempChunks[z][x].getHeightMap());
 	    				this.mapData.getChunk(zSmall + z, xSmall + x).setHeightMapTopo(tempChunks[z][x].getHeightMapTopo());
 	    				this.saveQueue.add(new SaveCommand(SaveCommand.COPY_COMMAND, tempChunks[z][x].getFilePath(), this.mapData.getChunk(zSmall + z, xSmall + x).getFilePath()));
     				}
     			}
     		}
     		this.unselectAll();
     		if(this.viewMode == TOPOGRAPHIC){
     			for(int z = zSmall - 1; z < tempChunks.length + zSmall; z++)
     				this.repaintChunk(z, xSmall - 1);
     			for(int x = xSmall - 1; x < tempChunks[0].length + xSmall; x++)
     				this.repaintChunk(zSmall - 1, x);
     		}
     	}
    	else {
			JOptionPane
					.showMessageDialog(
							this,
							"Copied chunks' shape and size does not fit onto target selection.",
							"Pasting", JOptionPane.ERROR_MESSAGE);
    	}
 	}
 	
 	/**
 	 * Repaints the given chunk in the image.
 	 * @param chunk Chunk to be repainted.
 	 */
 	public void repaintChunk(MapChunk chunk){
 		Coordinates chunkCoord = this.mapData.locateChunk(chunk);
 		this.repaintChunk(chunkCoord.getZ(), chunkCoord.getX());
 	}
 	
 	/**
 	 * Repaints the chunk at the given coordinates.
 	 * @param z Z-Position of the chunk to repaint.
 	 * @param x X-Position of the chunk to repaint.
 	 */
 	public void repaintChunk(int z, int x){
 
 		Graphics g = this.image.getImage().getGraphics();
 		if(!this.mapData.containsChunk(z, x)){
 			g.setColor(new Color(240, 240, 240));
 			g.fillRect(x * 16, z * 16, 16, 16);
 			this.repaint();
 			return;
 		}
 		if(this.viewMode == COLORED_VIEW)
 			this.repaintChunkColored(z, x);
 		if(this.viewMode == TOPOGRAPHIC)
 			this.repaintChunkTopographic(z, x);
 	}
 
 	/**
 	 * Repaint the chunk using the colored view.
 	 * @param z Z-Position of the chunk.
 	 * @param x X-Position of the chunk.
 	 */
 	private void repaintChunkColored(int z, int x){
 		Graphics g = this.image.getImage().getGraphics();
 		byte[][] topArray = this.mapData.getChunk(z, x).getTopBlockArray();
 		for(int az = 0; az < topArray.length; az++){
 			for(int ax = 0; ax < topArray[0].length; ax++){
 				Color tempColor = this.mapData.getTopBlockColor(topArray[az][ax]);
 				int tempRed = tempColor.getRed();
 				int tempGrn = tempColor.getGreen();
 				int tempBlu = tempColor.getBlue();
 				
 				//Create a height map if the block is grass
 				if(topArray[az][ax] == Blocks.GRASS){
 					int tempHeight = this.mapData.getChunk(z, x).getHeightMap()[az][ax];
 					if(tempHeight < 0){
 						if(this.terraformDialog.getChangeHeight() < 0)
 							tempHeight = 0;
 						else
 							tempHeight = 127;
 					}
 					int subtractInt = 127 - tempHeight;
 					tempRed = (int) (tempRed - subtractInt * 1.5);
 					tempGrn = (int) (tempGrn - subtractInt * 1.5);
 					tempBlu = (int) (tempBlu - subtractInt * 1.5);
 	    		}
 				if(this.mapData.isBlockSelected(z * 16 + az, x * 16 + ax)){
 					tempRed += 100;
 					tempGrn -= 30;
 					tempBlu -= 30;
 				}
 				if(tempRed < 0)
 					tempRed = 0;
 				if(tempGrn < 0)
 					tempGrn = 0;
 				if(tempBlu < 0)
 					tempBlu = 0;
 				if(tempRed > 255)
 					tempRed = 255;
 				if(tempGrn > 255)
 					tempGrn = 255;
 				if(tempBlu > 255)
 					tempBlu = 255;
 				tempColor = new Color(tempRed, tempGrn, tempBlu);
 	
 				g.setColor(tempColor);
 				g.fillRect(x * 16 + ax, z * 16 + az, 1, 1);
 				this.repaint();
 			}
 		}
 	}
 	
 	/**
 	 * Repaint the chunk using the Topographic mode.
 	 * @param z Z-Position of the chunk.
 	 * @param x X-Position of the chunk.
 	 */
 	private void repaintChunkTopographic(int z, int x){
 		Graphics g = this.image.getImage().getGraphics();
 		byte[][] topArray = this.mapData.getChunk(z, x).getHeightMapTopo();
 		byte[][] topBlockArray = this.mapData.getChunk(z, x).getTopBlockArray();
 		byte[] nextTopArray = null;
 		byte[] nextBlockArray = null;
 		byte[] belowTopArray = null;
 		byte[] belowBlockArray = null;
 		if(this.mapData.containsChunk(z, x + 1)){
 			byte[][] tempArray = this.mapData.getChunk(z, x + 1).getHeightMapTopo();
 			byte[][] tempBrray = this.mapData.getChunk(z, x + 1).getTopBlockArray();
 			nextTopArray = new byte[tempArray.length];
 			nextBlockArray = new byte[tempArray.length];
 			for(int i = 0; i < tempArray.length; i++){
 				nextTopArray[i] = tempArray[i][0];
 				nextBlockArray[i] = tempBrray[i][0];
 			}
 		}
 		if(this.mapData.containsChunk(z + 1, x)){
 			belowTopArray = this.mapData.getChunk(z + 1, x).getHeightMapTopo()[0];
 			belowBlockArray = this.mapData.getChunk(z + 1, x).getTopBlockArray()[0];
 		}
 
 		
 				
 		for(int az = 0; az < topArray.length; az++){
 			for(int ax = 0; ax < topArray[0].length; ax++){
 				boolean colorBlack = false;
 				byte tempTopo = topArray[az][ax];
 				if(tempTopo < 0)
 					if(terraformDialog.getChangeHeight() > 0)
 						tempTopo = (byte) 127;
 					else
 						tempTopo = 0;
 				//Start checking if this chunk should be black.
 				byte nextHeight = tempTopo;
 				byte belowHeight = tempTopo;
 				byte nextBlock = topBlockArray[az][ax];
 				byte belowBlock = topBlockArray[az][ax];
 				
 				if(ax == topArray[0].length - 1){
 					if(nextTopArray != null){
 						nextHeight = nextTopArray[az];
 						nextBlock = nextBlockArray[az];
 					}
 				}
 				else{
 					nextHeight = topArray[az][ax + 1];
 					nextBlock = topBlockArray[az][ax + 1];
 				}
 				
 				
 				if(az == topArray.length - 1){
 					if(belowTopArray != null){
 						belowHeight = belowTopArray[ax];
 						belowBlock = belowBlockArray[ax];
 					}
 				}
 				else{
 					belowHeight = topArray[az + 1][ax];
 					belowBlock = topBlockArray[az + 1][ax];
 				}
 				
 				
 				if(((int)(nextHeight / TOPOGRAPHIC_LINE_DISTANCE) != (int)(tempTopo / TOPOGRAPHIC_LINE_DISTANCE) ||
 						(int)(belowHeight / TOPOGRAPHIC_LINE_DISTANCE) != (int)(tempTopo / TOPOGRAPHIC_LINE_DISTANCE)) ||
 						
 						(((nextBlock == Blocks.STILL_WATER || nextBlock == Blocks.WATER || nextBlock == Blocks.ICE) && (topBlockArray[az][ax] != Blocks.STILL_WATER && topBlockArray[az][ax] != Blocks.WATER && topBlockArray[az][ax] != Blocks.ICE)) ||
 						((belowBlock == Blocks.STILL_WATER || belowBlock == Blocks.WATER || belowBlock == Blocks.ICE) && (topBlockArray[az][ax] != Blocks.STILL_WATER && topBlockArray[az][ax] != Blocks.WATER && topBlockArray[az][ax] != Blocks.ICE)))
 						^
 						(((topBlockArray[az][ax] == Blocks.STILL_WATER || topBlockArray[az][ax] == Blocks.WATER || topBlockArray[az][ax] == Blocks.ICE) && (nextBlock != Blocks.STILL_WATER && nextBlock != Blocks.WATER && nextBlock != Blocks.ICE)) ||
 						((topBlockArray[az][ax] == Blocks.STILL_WATER || topBlockArray[az][ax] == Blocks.WATER || topBlockArray[az][ax] == Blocks.ICE) && (belowBlock != Blocks.STILL_WATER && belowBlock != Blocks.WATER && belowBlock != Blocks.ICE)
 								
 				
 				))){
 					colorBlack = true;
 				}
 				
 				int tempRed = 255;
 				int tempGrn = 178;
 				int tempBlu = 119;
 				
 				if(colorBlack){
 						tempRed = 0;
 						tempGrn = 0;
 						tempBlu = 0;
 				}
 				
 				int subtractInt = 127 - this.mapData.getChunk(z, x).getHeightMapTopo()[az][ax];
 				
 				tempRed = (int) (tempRed - subtractInt * 1.5);
 				tempGrn = (int) (tempGrn - subtractInt * 1.5);
 				tempBlu = (int) (tempBlu - subtractInt * 1.5);
 				if(this.mapData.isBlockSelected(z * 16 + az, x * 16 + ax)){
 					tempRed += 100;
 					tempGrn -= 30;
 					tempBlu -= 30;
 				}
 				if(tempRed < 0)
 					tempRed = 0;
 				if(tempGrn < 0)
 					tempGrn = 0;
 				if(tempBlu < 0)
 					tempBlu = 0;
 				if(tempRed > 255)
 					tempRed = 255;
 				if(tempGrn > 255)
 					tempGrn = 255;
 				if(tempBlu > 255)
 					tempBlu = 255;
 				Color tempColor = new Color(tempRed, tempGrn, tempBlu);
 	
 				g.setColor(tempColor);
 				g.fillRect(x * 16 + ax, z * 16 + az, 1, 1);
 				this.repaint();
 			}
 		}
 	}
 	
 	//uhhh... I don't know why I even have this...
 	public void setMaxUnitIncrement(int pixels) {
 	    maxUnitIncrement = pixels;
 	}
 
 	/**
 	 * Unselect all of the chunks in the map. Then repaint.
 	 */
 	public void unselectAll(){
 		for(int z = 0; z < this.mapData.getMapHeight(); z++){
 			for(int x = 0; x < this.mapData.getMapWidth(); x++){
 				if(this.mapData.containsChunk(z, x) && this.mapData.hasBlocksSelected(z, x)){
 					this.mapData.setSelected(z, x, false);
 					this.repaintChunk(z, x);
 				}
 			}
 		}
 	}
 	
 	/**
 	 * Invert all of the blocks in the map so each selected block will now be unselected
 	 * and vice versa.  Repaints when done.
 	 */
 	public void invertAll(){
 		for(int z = 0; z < this.mapData.getMapHeight(); z++){
 			for(int x = 0; x < this.mapData.getMapWidth(); x++){
 				if(this.mapData.containsChunk(z, x)){
 					this.mapData.invertChunk(z, x);
 					this.repaintChunk(z, x);
 				}
 			}
 		}
 	}
 	
 	/**
 	 * Selects all of the Chunks in the map.
 	 */
 	public void selectAll(){
 		for(int z = 0; z < this.mapData.getMapHeight(); z++){
 			for(int x = 0; x < this.mapData.getMapWidth(); x++){
 				if(this.mapData.containsChunk(z, x) && !this.mapData.isChunkSelected(z, x)){
 					this.mapData.setSelected(z, x, true);
 					this.repaintChunk(z, x);
 				}
 			}
 		}
 	}
 	
 	/**
 	 * Unselects a specific chunk at the given Position
 	 * @param z Z-Position of the chunk.
 	 * @param x X-Position of the chunk.
 	 */
 	public void unselectChunk(int z, int x){
 		this.mapData.setSelected(z, x, false);
 	}
 	
 	/**
 	 * Generate trees on the selected chunks/blocks.
 	 */
 	public void generateTrees(){
 		int[] tempArray = new int[1];
 		tempArray[0] = Tree.PINE_TREE;
 		TreeGenerator tempGen = new TreeGenerator(this.mapData.getSelectedChunks(), this.mapData, this.treeDialog.getDensity(), tempArray);
 		tempGen.generateHeightMapArray();
 		LinkedList<SaveCommand> saveCo = tempGen.getBlocksToSave();
 		Iterator<SaveCommand> iterc = saveCo.iterator();
 		while(iterc.hasNext()){
 			SaveCommand tempSave = iterc.next();
 			System.out.println("Generating Chunk: " + tempSave.getChunkA());
 			Iterator<MapChunk> iterC = this.mapData.getSelectedChunks().iterator();
 			//TODO: I really need to fix this to make it faster :/
 			while(iterC.hasNext()){
 				MapChunk tempChunk = iterC.next();
 				if(tempChunk.getFilePath().compareTo(tempSave.getChunkA()) == 0){
 					byte[][] tempTopBlock = tempChunk.getTopBlockArray();
 					byte[][] tempHeightMap = tempChunk.getHeightMap();
 					int i = tempSave.getCoordinates().getBlockArray().length - 1;
 					tempTopBlock[tempSave.getCoordinates().getZ()][tempSave.getCoordinates().getX()] = tempSave.getCoordinates().getBlockArray()[i];
 					tempHeightMap[tempSave.getCoordinates().getZ()][tempSave.getCoordinates().getX()] = (byte) (tempSave.getCoordinates().getY() + i);
 					tempChunk.setTopBlockMap(tempTopBlock);
 					tempChunk.setHeightMap(tempHeightMap);
 				}
 			}
 			//tempSave.getChunkA();
 		}
 		this.saveQueue.addAll(saveCo);
 		this.repaintSelectedChunks();
 	}
 	
 	/**
 	 * Repaints all of the chunks that are currently selected.
 	 */
 	public void repaintSelectedChunks() {
 		Iterator<MapChunk> iter = this.mapData.getSelectedChunks().iterator();
 		while(iter.hasNext()){
 			this.repaintChunk(iter.next());
 		}
 		
 	}
 
 	/**
 	 * lol I dunno.
 	 */
 	public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
 		//Get the current position.
 		int currentPosition = 0;
 		if (orientation == SwingConstants.HORIZONTAL)
 			currentPosition = visibleRect.x;
 		else
 			currentPosition = visibleRect.y;
 		
 		//Return the number of pixels between currentPosition
 		//and the nearest tick mark in the indicated direction.
 		if (direction < 0) {
 			int newPosition = currentPosition - (currentPosition / maxUnitIncrement) * maxUnitIncrement;
 			return (newPosition == 0) ? maxUnitIncrement : newPosition;
 		} 
 		else {
 			return ((currentPosition / maxUnitIncrement) + 1) * maxUnitIncrement - currentPosition;
 		}
 	}
 		
 	/**
 	 * Beats me.  Forgot why I had this.
 	 */
 	public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
 		if (orientation == SwingConstants.HORIZONTAL)
 			return visibleRect.width - maxUnitIncrement;
 		else
 			return visibleRect.height - maxUnitIncrement;
 	}
 	
 }
