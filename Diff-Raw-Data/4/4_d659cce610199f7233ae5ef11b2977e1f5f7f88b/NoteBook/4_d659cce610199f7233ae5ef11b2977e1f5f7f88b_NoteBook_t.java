 // Copyright (c) 2011 Martin Ueding <dev@martin-ueding.de>
 
 import java.awt.event.ActionListener;
 import java.io.File;
 import java.io.IOException;
 import java.util.LinkedList;
 
 
 /**
  * A container for several NoteSheets.
  *
  * The NoteBook contains at least one NoteSheet and adds new sheets whenever
  * the forward() function is called. The whole notebook can be saved into
  * individual pictures.
  */
 public class NoteBook {
 	// TODO save sheets that are far away in the list to save RAM space then
 	// reload images when the user is going back in the history
 	private LinkedList<NoteSheet> sheets;
 
 	private int currentSheet = 0;
 	
 	private File folder;
 
 	private NoteSheet current;
 
 	private int width;
 	private int height;
 
 	private ActionListener doneDrawing;
 	
 	/**
 	 * Count of pages. Latest page number is pagecount.
 	 */
 	private static int pagecount = 1;
 	
 	String name;
 
 	/**
 	 * Creates an empty note book with a single note sheet.
 	 *
 	 * @param width width of the individual sheets
 	 * @param height height of the individual sheets
 	 */
 	public NoteBook(int width, int height, File folder, String name) {
 		this.width = width;
 		this.height = height;
 		
 		this.folder = folder;
 		this.name = name;
 
 		sheets = new LinkedList<NoteSheet>();
 		
 		// if a notebook should be used
 		if (folder != null && name != null) {
 			if (!folder.exists()) {
 				folder.mkdirs();
 			}
 			
 			// try to load all images that match the name
 			File[] allImages = folder.listFiles(new NoteSheetFileFilter(name));
 			
 			// FIXME load pictures in correct order
 			for (File file : allImages) {
 				try {
 					System.out.println(String.format("loading from file %s", file.getCanonicalPath()));
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 				sheets.add(new NoteSheet(width, height, pagecount++, file));
 			}
 			
 			
 			
 		}
 		
 		// add an empty sheet if the notebook would be empty otherwise
 		if (sheets.size() == 0) {
 			System.out.println("generating new sheet in empty notebook");
 			sheets.add(new NoteSheet(width, height, pagecount, generateNextFilename(pagecount)));
 			pagecount++;
 		}
 
 		
 		updateCurrrentItem();
 	}
 	
 	private File generateNextFilename(int pagenumber) {
 		if (folder != null && name != null) {
 		try {
 			return new File(folder.getCanonicalPath() + File.separator + name + "-" +String.valueOf(pagenumber) + ".png");
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		}
 		return null;
 	}
 
 	private boolean touched = false;
 
 	/**
 	 * Draws a line onto the current sheet.
 	 */
 	public void drawLine(int x, int y, int x2, int y2) {
 		current.drawLine(x, y, x2, y2);
 
 		fireDoneDrawing();
 
 		touched = true;
 	}
 
 	/**
 	 * Sets an action listener to be called when something new was drawn.
 	 *
 	 * @param doneDrawing ActionListener to be called after drawing a new line.
 	 */
 	public void setDoneDrawing(ActionListener doneDrawing) {
 		this.doneDrawing = doneDrawing;
 	}
 
 	private void fireDoneDrawing() {
 		if (doneDrawing != null) {
 			doneDrawing.actionPerformed(null);
 		}
 	}
 
 	/**
 	 * Flip the pages forward. It creates a new page if needed. If the current
 	 * page is a blank page, no new blank page will be added.
 	 */
 	public void forward() {
 		currentSheet++;
 		if (sheets.size() > currentSheet) {
 		}
 		else if (current.touched()) {
 			sheets.add(new NoteSheet(width, height, pagecount, generateNextFilename(pagecount)));
 			
 			
 			pagecount++;
 		}
 		else {
 			// correct the advancing
 			currentSheet--;
 			return;
 		}
 		updateCurrrentItem();
 		fireDoneDrawing();
 	}
 	
 
 
 	/**
 	 * @return The number of pages given out so far.
 	 */
 	public static int getPagecount() {
 		return pagecount;
 	}
 
 	/**
 	 * Goes back one sheet.
 	 */
 	public void backward() {
 		if (currentSheet > 0) {
 			currentSheet--;
 			updateCurrrentItem();
 			fireDoneDrawing();
 		}
 	}
 
 	public NoteSheet getCurrentSheet() {
 		return sheets.get(currentSheet);
 	}
 
 	private void updateCurrrentItem() {
 		assert(currentSheet >= 0);
 		assert(currentSheet < sheets.size());
 		current = sheets.get(currentSheet);
 	}
 
 	/**
 	 * Persists the whole notebook into individual files. The user is promted
 	 * for a basename, the current date is added to the front. Each sheet
 	 * suffixed with the page number, padded with as many zeros as needed.
 	 *
 	 * The filename will look like this:
 	 * basename-1.png
 	 */
 	public void saveToFiles() {
 		if (!touched) {
 			return;
 		}
 
 		int maxnum = sheets.size() - 1;
 		int length = 1;
 		while (Math.pow(10, length) < maxnum + 1) {
 			length++;
 		}
 
 		for (NoteSheet s : sheets) {
 			s.saveToFile();
 		}
 	}
 
 	/**
 	 * @return number of sheets in the notebook
 	 */
 	public int getSheetCount() {
 		return sheets.size();
 	}
 
 }
