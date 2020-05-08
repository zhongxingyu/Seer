 import java.awt.event.ActionListener;
 import java.util.Calendar;
 import java.util.LinkedList;
 
 import javax.swing.JOptionPane;
 
 
 public class NoteBook {
 	private LinkedList<NoteSheet> sheets;
 
 	private int currentSheet = 0;
 
 	private NoteSheet current;
 	
 	private int width;
 	private int height;
 	
 	private ActionListener doneDrawing;
 
 	/**
 	 * Creates an empty note book with a single note sheet.
 	 * 
 	 * @param width width of the individual sheets
 	 * @param height height of the individual sheets
 	 */
 	public NoteBook(int width, int height) {
 		this.width = width;
 		this.height = height;
 		
 		sheets = new LinkedList<NoteSheet>();
 		
 		sheets.add(new NoteSheet(width, height));
 		updateCurrrentItem();
 	}
 	
 	private boolean touched = false;
 	
 	public void drawLine(int x, int y, int x2, int y2) {
 		current.drawLine(x, y, x2, y2);
 		
 		fireDoneDrawing();
 		
 		touched = true;
 	}
 
 	public void setDoneDrawing(ActionListener doneDrawing) {
 		this.doneDrawing = doneDrawing;
 	}
 	
 	private void fireDoneDrawing() {
 		if (doneDrawing != null) {
 			doneDrawing.actionPerformed(null);
 		}
 	}
 
 	public void forward() {
		if (sheets.size() <= currentSheet -1) {
 		}
 		else if (current.touched()) {
 			sheets.add(new NoteSheet(width, height));
 		}
 		else {
 			return;
 		}
		currentSheet++;
 		updateCurrrentItem();
 		fireDoneDrawing();
 	}
 	
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
 	
 	public void saveToFiles() {
 		if (!touched) {
 			return;
 		}
 		
 		String basename = JOptionPane.showInputDialog("Please enter basename (press cancel to stop saving)");
 		if (basename == null) {
 			return;
 		}
 		
 		Calendar now = Calendar.getInstance();
 		
 		int maxnum = sheets.size()-1;
 		int length = 1;
 		while (Math.pow(10, length) < maxnum+1) {
 			length++;
 		}
 		
 		for (NoteSheet s : sheets) {
 			String filename = (now.get(Calendar.YEAR)-2000)+""+now.get(Calendar.MONTH)+""+now.get(Calendar.DAY_OF_MONTH)+"-" + basename + "-" + String.format("%0"+length+"d", s.getPagenumber()) + ".png";
 			s.saveToFile(filename);
 		}
 
 	}
 
 	public int getSheetCount() {
 		return sheets.size();
 	}
 
 }
