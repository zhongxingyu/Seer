 import java.awt.Color;
 import java.io.Serializable;
 import java.util.List;
 
 import javax.swing.BorderFactory;
 import javax.swing.JButton;
 import javax.swing.JTable;
 import javax.swing.border.Border;
 import javax.swing.table.DefaultTableModel;
 import javax.swing.table.TableColumnModel;
 
 public class ScheduleDisplay implements Serializable{
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 	transient private JTable table;
 	transient DefaultTableModel tm;
 	int maxStudentsPerClass;
 	Object[][] data;
 	String[] columnNames;
 	JButton button = new JButton("Schedule Teachers");
 	// numClasses = #subjects; numRooms = #divisions
 	private int numRooms;
 	private List<Students> unluckyStudents;
 	private int addStdBuf = 2;
 	ClassFactory clsFac;
 
 	public ScheduleDisplay(ClassFactory cf) {
 
 		clsFac = cf;
 		numRooms = clsFac.getMaxCls();
 		maxStudentsPerClass = clsFac.getMaxStdPerCls() + 2;
 
 		int tabRows = (7 * maxStudentsPerClass) + 1;
 		data = new Object[tabRows][numRooms + 2];
 		for (int i = 0; i < tabRows; i++) {
 			for (int j = 0; j < (numRooms + 2); j++)
 				data[i][j] = "";
 		}
 		columnNames = new String[numRooms + 2];
 		columnNames[0] = "Subject";
 		for (int i = 1; i < numRooms + 1; i++) {
 			columnNames[i] = " ";
 		}
 		columnNames[numRooms + 1] = "Waitlist";
 		tm = new DefaultTableModel(data, columnNames);  
 		
 		table = new RowColoredTable(data);  
 		table.setModel(tm);
 		
 		update();
 	}
 
 	public void update() {
 		numRooms = clsFac.getMaxCls();
 		columnNames = new String[numRooms + 2];
 		columnNames[0] = "Subject";
 		for (int i = 1; i < numRooms + 1; i++) {
 			columnNames[i] = " ";
 		}
 		columnNames[numRooms + 1] = "Waitlist";
		
		
 		populateTable();
 		tm.setDataVector(data, columnNames);
 		// format table
 		table.setShowGrid(true);
 		table.setGridColor(Color.BLACK);
 		table.setRowHeight(20);
 		Border border = BorderFactory.createLineBorder(Color.BLACK, 1);
 		table.setBorder(border);
 		table.setColumnSelectionAllowed(true);
 		// Make table scrollable
 		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
 
 		// Fix Sizing
 		TableColumnModel cm = table.getColumnModel();
 		cm.getColumn(0).setMinWidth(100);
 		for (int i = 1; i < cm.getColumnCount()-1; i++) {
 			cm.getColumn(i).setMinWidth(200);
 		}
 		cm.getColumn(cm.getColumnCount() - 1).setMinWidth(500);
 		
 
 		// for (int i = 0; i < table.getRowCount(); i++) {
 		// for (int j = 0; j < table.getColumnCount(); j++) {
 		// if ((i % 9) == 0) {
 		// Component c = table.getComponentAt(i, j);
 		//
 		// // table.add(c, j);
 		// }
 		// }
 		// }
 
 	}
 
 	public void populateTable() {
 		int tabRows = (7 * maxStudentsPerClass) + 1;
 		unluckyStudents = clsFac.unlucky;
 
 		if (unluckyStudents.size() > tabRows) {
 			data = new Object[unluckyStudents.size()][numRooms + 2];
 		} else {
 			data = new Object[tabRows][numRooms + 2];
 		}
 
 		data[0][0] = " Reading";
 		data[1][0] = " 9 - 10:35";
 		data[maxStudentsPerClass + 3][0] = " Language Arts";
 		data[maxStudentsPerClass + 4][0] = " 10:40 - 11:55";
 		data[(2 * maxStudentsPerClass) + 6][0] = " Specials";
 		data[(2 * maxStudentsPerClass) + 7][0] = " 12:50 - 1:20";
 		data[(3 * maxStudentsPerClass) + 9][0] = " Math";
 		data[(3 * maxStudentsPerClass) + 10][0] = " 1:25 - 2:55";
 		data[(4 * maxStudentsPerClass) + 12][0] = " Homeroom";
 
 		// rows for specials by day
 		// String[] daysOfWeek = { " Monday", " Tuesday", " Wednesday",
 		// " Thursday", " Friday" };
 		// for (int i = 0; i < daysOfWeek.length; i++) {
 		// data[(2 * maxStudentsPerClass) + (i + 6 + 2)][0] = daysOfWeek[i];
 		// }
 
 		// TODO: add rows for specials and homeroom (same as math)
 		// TODO: add formatting- times of classes
 
 		// fill in students for Reading
 		int currRow = 0;
 		int currCol = 1;
 		for (int i = 0; i < clsFac.readClsLst.size(); ++i) {
 			Classes cls = clsFac.readClsLst.get(i);
 			/*String clsHeader = cls.getFormalClassName();
 			clsHeader = formatName(clsHeader);
 			clsHeader += " " + cls.getLvl();
 			clsHeader += " " + cls.getClsID();*/
 			String clsHeader = cls.getTableName();
 			
 			if (cls.hasTeacher())
 				clsHeader += ":  " + cls.getTeacher().getName();
 
 			data[currRow][currCol] = clsHeader;
 
 			data[currRow + 1][currCol] = "Ages: " + (int) cls.getLowestAge()
 					+ " - " + (int) cls.getHighestAge();
 
 			List<Students> students = cls.getStudents();
 			for (int j = 1; j <= students.size(); j++) {
 				Students std = students.get(j - 1);
 				// table.setValueAt(stdNameStr, j + 15, i + 1);
 				data[j + currRow + 1][currCol] = std; // changed
 																					// from
 																					// stdNameStr
 			}
 			currCol++;
 		}
 
 		// fill in students for LA
 		currRow = maxStudentsPerClass + 3;
 		currCol = 1;
 		for (int i = 0; i < clsFac.getTotalLA(); ++i) {
 			Classes cls = clsFac.laClsLst.get(i);
 			/*String clsHeader = cls.getFormalClassName();
 			clsHeader = formatName(clsHeader);
 			clsHeader += " " + cls.getLvl();
 			clsHeader += " " + cls.getClsID();*/
 			String clsHeader = cls.getTableName();
 
 			if (cls.hasTeacher())
 				clsHeader += ":  " + cls.getTeacher().getName();
 
 			data[currRow][currCol] = clsHeader;
 			data[currRow + 1][currCol] = "Ages: " + (int) cls.getLowestAge()
 					+ " - " + (int) cls.getHighestAge();
 
 			List<Students> students = cls.getStudents();
 			for (int j = 1; j <= students.size(); j++) {
 				Students std = students.get(j - 1);
 				// table.setValueAt(stdNameStr, j + 15, i + 1);
 				data[j + currRow + 1][currCol] = std; // changed
 																					// from
 																					// stdNameStr
 			}
 			currCol++;
 		}
 
 		// fill in students for Specials- currently using same class list as
 		// homeroom
 		currRow = (2 * maxStudentsPerClass) + 6;
 		currCol = 1;
 		for (int i = 0; i < clsFac.getTotalSpecial(); ++i) {
 			Classes cls = clsFac.specialClsLst.get(i);
 			/*String clsHeader = cls.getFormalClassName();
 			clsHeader = formatName(clsHeader);
 			clsHeader += " " + cls.getLvl();
 			clsHeader += " " + cls.getClsID();*/
 			String clsHeader = cls.getTableName();
 
 			if (cls.hasTeacher())
 				clsHeader += ":  " + cls.getTeacher().getName();
 
 			data[currRow][currCol] = clsHeader;
 			data[currRow + 1][currCol] = "Ages: " + (int) cls.getLowestAge()
 					+ " - " + (int) cls.getHighestAge();
 
 			List<Students> students = cls.getStudents();
 			for (int j = 1; j <= students.size(); j++) {
 				Students std = students.get(j - 1);
 				// table.setValueAt(stdNameStr, j + 15, i + 1);
 				data[j + currRow + 1][currCol] = std; // changed
 																					// from
 																					// stdNameStr
 			}
 			currCol++;
 		}
 
 		// fill in students for Math
 		currRow = (3 * maxStudentsPerClass) + 9;
 		currCol = 1;
 		for (int i = 0; i < clsFac.getTotalMath(); ++i) {
 			Classes cls = clsFac.mathClsLst.get(i);
 			/*String clsHeader = cls.getFormalClassName();
 			clsHeader = formatName(clsHeader);
 			clsHeader += " " + cls.getLvl();
 			clsHeader += " " + cls.getClsID();*/
 			String clsHeader = cls.getTableName();
 			
 			if (cls.hasTeacher())
 				clsHeader += ":  " + cls.getTeacher().getName();
 
 			data[currRow][currCol] = clsHeader;
 			data[currRow + 1][currCol] = "Ages: " + (int) cls.getLowestAge()
 					+ " - " + (int) cls.getHighestAge();
 
 			List<Students> students = cls.getStudents();
 			for (int j = 1; j <= students.size(); j++) {
 				Students std = students.get(j - 1);
 				// table.setValueAt(stdNameStr, j + 15, i + 1);
 				data[j + currRow + 1][currCol] = std; // changed
 																					// from
 																					// stdNameStr
 			}
 			currCol++;
 		}
 
 		// fill in students for homeroom
 		currRow = (4 * maxStudentsPerClass) + 12;
 		currCol = 1;
 		for (int i = 0; i < clsFac.getTotalHomeroom(); ++i) {
 			Classes cls = clsFac.homeroomClsLst.get(i);
 			/*String clsHeader = cls.getFormalClassName();
 			clsHeader = formatName(clsHeader);
 			clsHeader += " " + cls.getLvl();
 			clsHeader += " " + cls.getClsID();*/
 			String clsHeader = cls.getTableName();
 
 			if (cls.hasTeacher())
 				clsHeader += ":  " + cls.getTeacher().getName();
 
 			data[currRow][currCol] = clsHeader;
 			data[currRow + 1][currCol] = "Ages: " + (int) cls.getLowestAge()
 					+ " - " + (int) cls.getHighestAge();
 
 			List<Students> students = cls.getStudents();
 			for (int j = 1; j <= students.size(); j++) {
 				Students std = students.get(j - 1);
 				// table.setValueAt(stdNameStr, j + 15, i + 1);
 				data[j + currRow + 1][currCol] = std; // changed
 																					// from
 																					// stdNameStr
 			}
 			currCol++;
 		}
 
 		// fill in unlucky students
 		System.out.println("unlucky students size: " + unluckyStudents);
 		if (unluckyStudents != null) {
 			System.out.println("there are unlucky students!");
 			System.out.println("unluckyStudents size:  "
 					+ unluckyStudents.size());
 			for (int i = 0; i < unluckyStudents.size(); ++i) {
 				Students std = unluckyStudents.get(i);
 				data[i][numRooms + 1] = std;
 			}
 		} else {
 			data[0][numRooms + 1] = "all were sheduled";
 		}
 	}
 
 	public JTable getScheduleTable() {
 		return table;
 	}
 
 	// used for re-writing class names only
 	private String formatName(String str) {
 		String newStr;
 		if (str.equals("read"))
 			return "Reading";
 
 		else if (str.equals("la"))
 			return "Language Arts";
 
 		else if (str.equals("math"))
 			return "Math";
 
 		else
 			return "Class";
 	}
 
 	// finds the max class sizes in math, reading, and la after schedulizing,
 	// sums them, and decides
 	// which is larger- the students in the classes, or the unlucky students to
 	// determine the max
 	// rows needed in the table
 	private int calculateRows() {
 
 		if (unluckyStudents != null) {
 			return Math.max(((3 * maxStudentsPerClass) + 3),
 					unluckyStudents.size());
 		}
 
 		return (3 * maxStudentsPerClass) + 3;
 	}
 	
 }
