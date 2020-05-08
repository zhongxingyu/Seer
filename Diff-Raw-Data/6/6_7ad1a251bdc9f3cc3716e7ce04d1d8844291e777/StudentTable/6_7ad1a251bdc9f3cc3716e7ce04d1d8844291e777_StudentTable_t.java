 import java.awt.Color;
 import java.io.Serializable;
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.Iterator;
 import java.util.List;
 
 import javax.swing.DefaultCellEditor;
 import javax.swing.JComboBox;
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 import javax.swing.JTable;
 import javax.swing.JTextField;
 import javax.swing.event.TableModelEvent;
 import javax.swing.event.TableModelListener;
 import javax.swing.table.DefaultTableModel;
 import javax.swing.table.TableColumnModel;
 import javax.swing.table.TableModel;
 
 public class StudentTable implements TableModelListener, Serializable {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 	transient JTable table;
 	DefaultTableModel tm;
 	StudentDB students;
 	transient JFrame frame;
 
 	Object[][] data;
 	String[] columnNames = { "Student ID", "First Name", "Last Name",
 			"Birth Date", "Math Level", "Reading Level", "Language Arts Level",
 			"Behavioral Level" };
 	String[] validStates = { "", "K", "1", "2", "3", "4", "5", "6", "7", "8" };
 	String[] behaviorLevels = { "1", "2", "3" };
 	private ClassFactory clsFac;
 
 	public StudentTable(JFrame f, StudentDB s, ClassFactory cf) {
 		clsFac = cf;
 		frame = f;
 		students = s;
 		data = new Object[300][8];
 		for (int i = 0; i < 300; i++) {
 			for (int j = 0; j < 8; j++)
 				data[i][j] = "";
 		}
 
 		tm = new DefaultTableModel(data, columnNames);
 		table = new JTable();
 		table.setModel(tm);
 		tm.addTableModelListener(this);
 
 		update();
 	}
 
 	public JTable getStudentTable() {
 		return table;
 	}
 
 	public void update() {
 		populateTable(students);
 		renderTable();
 	}
 
 	public void renderTable() {
 		ComboRenderer cr = new ComboRenderer();
 
 		table.setShowGrid(true);
 		table.setGridColor(Color.BLACK);
 		table.setRowHeight(20);
 		table.setCellSelectionEnabled(true);
 
 		// Create the combo box editor
 		JComboBox comboBox = new JComboBox(validStates);
 		JComboBox behaviorBox = new JComboBox(behaviorLevels);
 		// comboBox.setEditable(true);
 		DefaultCellEditor editor = new DefaultCellEditor(comboBox);
 		DefaultCellEditor bEditor = new DefaultCellEditor(behaviorBox);
 
 		// Create the textfield editor
 		JTextField text = new JTextField();
 		text.setEditable(true);
 		DefaultCellEditor teditor = new DefaultCellEditor(text);
 
 		// Render comboboxes properly
 		TableColumnModel tcm = table.getColumnModel();
 		tcm.getColumn(0).setCellEditor(teditor);
 		tcm.getColumn(1).setCellEditor(teditor);
 		tcm.getColumn(2).setCellEditor(teditor);
 		tcm.getColumn(3).setCellEditor(teditor);
 		tcm.getColumn(4).setCellEditor(editor);
 		tcm.getColumn(4).setCellRenderer(cr);
 		tcm.getColumn(5).setCellEditor(editor);
 		tcm.getColumn(5).setCellRenderer(cr);
 		tcm.getColumn(6).setCellEditor(editor);
 		tcm.getColumn(6).setCellRenderer(cr);
 		tcm.getColumn(7).setCellEditor(bEditor);
 		tcm.getColumn(7).setCellRenderer(cr);
 	}
 
 	public void populateTable(StudentDB st) {
 		students = st;
 		int i = 0;
 		if (students.getSize() > 0) {
 			ArrayList<Students> stdList = students.getStudents();
 			Iterator<Students> it = stdList.iterator();
 			while (it.hasNext()) {
 				DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
 				Students s = it.next();
 				if (s.toString().contains("null"))
 					continue;
 				System.out.println("DEBUG " + s.toString());
 				data[i][0] = s.getId();
 				data[i][1] = s.getFirstName();
 				data[i][2] = s.getLastName();
 				data[i][3] = df.format(s.getBirthDate());
 				int mLevel = s.getMath();
 				String math = (mLevel == 0) ? "K" : Integer.toString(mLevel);
 				data[i][4] = math;
 
 				int rLevel = s.getRead();
 				String reading = (rLevel == 0) ? "K" : Integer.toString(rLevel);
 				data[i][5] = reading;
 
 				int lLevel = s.getLA();
 				String la = (lLevel == 0) ? "K" : Integer.toString(lLevel);
 				data[i][6] = la;
 				data[i][7] = Integer.toString(s.getBL());
 				i++;
 
 			}
 
 		}
 
 		// Add an empty row
 
 		while (i < 300) {
 			data[i][0] = "";
 			data[i][1] = "";
 			data[i][2] = "";
 			data[i][3] = "";
 			data[i][4] = "";
 			data[i][5] = "";
 			data[i][6] = "";
 			data[i][7] = "";
 
 			i++;
 		}
 		tm.setDataVector(data, columnNames);
 	}
 
 	public boolean isValidValue(Object value) {
 		if (value instanceof String) {
 			String sValue = (String) value;
 
 			for (int i = 0; i < validStates.length; i++) {
 				if (sValue.equals(validStates[i])) {
 					return true;
 				}
 			}
 		}
 
 		return false;
 	}
 
 	@Override
 	public void tableChanged(TableModelEvent e) {
 		int row = e.getFirstRow();
 		int column = e.getColumn();
 
 		if (row < 0 || column < 0)
 			return;
 
 		TableModel model = (TableModel) e.getSource();
 		Object d = model.getValueAt(row, column);
 		data[row][column] = d;
 		boolean isBlank = Utilities.isBlank(d.toString());
 
 		if (row > 0 && Utilities.isBlank(data[row - 1][0].toString())
 				&& !isBlank) {
 			JOptionPane.showMessageDialog(frame,
 					"Please do not leave open rows within the table");
 			table.setValueAt("", row, column);
 			return;
 		}
 
 		Object oldIDObj = data[row][0];
 		int oldId;
 
 		if (!Utilities.isBlank(oldIDObj.toString())) {
 			try {
 				oldId = Integer.parseInt(oldIDObj.toString());
 			} catch (Exception ne) {
 				oldId = -1;
 			}
 		} else {
 			oldId = -1;
 		}
 		Students s;
 		boolean newStudent = false;
 		if (students.hasStudent(oldId)) {
 			s = students.getStudent(oldId);
 		} else {
 			s = new Students(clsFac);
 			newStudent = true;
 		}
 
 		int x;
 		switch (column) {
 		case 0:
 			int id;
 
 			if (isBlank) {
 				// System.out.println(d.toString());
 				cleanStudentDB();
 			} else {
 				try {
 					id = Integer.parseInt(d.toString());
 					if (students.hasStudent(id)) {
 						boolean isRepeat = false;
 
 						for (int i = 0; i < 300; i++) {
 							if (Integer.parseInt(data[i][0].toString()) == id
 									&& id != row) {
 								isRepeat = true;
 							}
 						}
 						if (isRepeat) {
 							JOptionPane
 									.showMessageDialog(frame,
 											"A student with that ID already exists in the scheduling system.");
 							table.setValueAt("", row, column);
 						}
 					} else {
 						if (id > 0)
 							s.setId(id);
 					}
 				} catch (NumberFormatException ne) {
 
 					JOptionPane.showMessageDialog(frame,
 							"Student ID should be an integer value");
 
 				}
 			}
			//cleanStudentDB();
			//update();
 			break;
 		case 1:
 			if (data[row][0].toString().isEmpty()) {
 				if (!isBlank) {
 					JOptionPane.showMessageDialog(frame,
 							"Please provide a Student ID first.\n");
 					table.setValueAt("", row, column);
 				}
 			} else {
 				s.setFirstName(d.toString());
 			}
 			break;
 		case 2:
 			if (data[row][0].toString().isEmpty()) {
 				if (!isBlank) {
 					JOptionPane.showMessageDialog(frame,
 							"Please provide a Student ID first.\n");
 					table.setValueAt("", row, column);
 				}
 			} else {
 				s.setLastName(d.toString());
 			}
 			break;
 		case 3:
 			try {
 				if (data[row][0].toString().isEmpty()) {
 					if (!isBlank) {
 						JOptionPane.showMessageDialog(frame,
 								"Please provide a Student ID first.\n");
 						table.setValueAt("", row, column);
 					}
 				} else {
 					DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
 					Date bDate = df.parse(d.toString());
 					System.out.println(bDate);
 					Calendar c = new GregorianCalendar();
 					c.setTime(bDate);
 					int year = c.get(Calendar.YEAR);
 					if (year < 1900) {
 						JOptionPane
 								.showMessageDialog(frame,
 										"Invalid Year. Expected Birth Date in the form yyyy-mm-dd");
 						table.setValueAt("", row, column);
 					} else {
 						s.setBirthDate(bDate);
 					}
 				}
 			} catch (ParseException n) {
 				if (!isBlank) {
 					JOptionPane.showMessageDialog(frame,
 							"Expected Birth Date in the form yyyy-mm-dd");
 					table.setValueAt("", row, column);
 				}
 			}
 			break;
 		case 4:
 			if (data[row][0].toString().isEmpty()) {
 				if (!isBlank) {
 					JOptionPane.showMessageDialog(frame,
 							"Please provide a Student ID first.\n");
 					table.setValueAt("", row, column);
 				}
 
 			} else {
 				if (!isBlank) {
 					x = (d.toString().equals("K")) ? 0 : Integer.parseInt(d
 							.toString());
 					s.setMath(x);
 				}
 			}
 			break;
 		case 5:
 			if (data[row][0].toString().isEmpty()) {
 				if (!isBlank) {
 					JOptionPane.showMessageDialog(frame,
 							"Please provide a Student ID first.\n");
 					table.setValueAt("", row, column);
 				}
 
 			} else {
 				if (!isBlank) {
 					x = (d.toString().equals("K")) ? 0 : Integer.parseInt(d
 							.toString());
 					s.setRead(x);
 				}
 			}
 			break;
 		case 6:
 			if (data[row][0].toString().isEmpty()) {
 				if (!isBlank) {
 					JOptionPane.showMessageDialog(frame,
 							"Please provide a Student ID first.\n");
 					table.setValueAt("", row, column);
 				}
 
 			} else {
 				if (!isBlank) {
 					x = (d.toString().equals("K")) ? 0 : Integer.parseInt(d
 							.toString());
 					s.setLA(x);
 				}
 			}
 			break;
 
 		case 7:
 			if (data[row][0].toString().isEmpty()) {
 				if (!isBlank) {
 					JOptionPane.showMessageDialog(frame,
 							"Please provide a Student ID first.\n");
 					table.setValueAt("", row, column);
 				}
 
 			} else {
 				if (!isBlank) {
 					x = Integer.parseInt(d.toString());
 					s.setBL(x);
 				}
 			}
 			break;
 		}
 
 		if (!isBlank) {
 			if (newStudent) {
 				students.addStudent(s);
 			} else {
 				students.modifyStudent(oldId, s);
 			}
 		}
 		tm.setDataVector(data, columnNames);
 		renderTable();
 	}
 
 	/*
 	 * I apologize for the inefficiency of this, but given the small data set,
 	 * it shouldn't be awful
 	 */
 	private void cleanStudentDB() {
 		List<Students> stds = students.getStudents();
 		for (int i = 0; i < stds.size(); i++) {
 			Students std = stds.get(i);
 			int id = std.getId();
 			for (int j = 0; j < students.getSize(); j++) {
 				try {
 					if (id == Integer.parseInt(data[j][0].toString())) {
 						return;
 					}
 				} catch (NumberFormatException n) {
 					// do nothing
 				}
 			}
 			// if we got here, the student is not in the data array anymore
 			students.removeStudent(id);
			update();
 		}
 	}
 
 }
