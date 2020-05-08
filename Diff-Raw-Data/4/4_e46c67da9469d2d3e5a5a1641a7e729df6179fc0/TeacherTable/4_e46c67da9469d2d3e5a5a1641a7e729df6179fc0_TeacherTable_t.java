 import java.awt.Color;
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 import javax.swing.JTable;
 import javax.swing.event.TableModelEvent;
 import javax.swing.event.TableModelListener;
 import javax.swing.table.DefaultTableModel;
 import javax.swing.table.TableModel;
 
 
 public class TeacherTable implements TableModelListener, Serializable {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 	TeacherDB teachers;
 	JTable table;
 	DefaultTableModel tm;
 	JFrame frame;
 	Object[][] data;
 	String[] columnNames = { "Name", "Math Class Levels",
 			"Reading Class Levels", "Language Arts Class Levels" };
 	TeacherController controller;
 	ClassFactory clsFac;
 
 	public TeacherTable(JFrame f, TeacherDB t, ClassFactory cf) {
 		clsFac = cf;
 		teachers = t;
 		frame = f;	
 		data = new Object[300][4];
 		for (int i = 0; i < 300; i++) {
 			for (int j = 0; j < 4; j++)
 				data[i][j] = "";
 		}
 
 		tm = new DefaultTableModel(data, columnNames);
 		table = new JTable();
 		table.setModel(tm);
 		tm.addTableModelListener(this);
 		
 		update();
 	}
 	
 	public void update() {
 		populateTable();
 		renderTable();
 		
 	}
 	
 	public void renderTable() {
 		table.setShowGrid(true);
 		table.setGridColor(Color.BLACK);
 		table.setRowHeight(20);
 		table.setCellSelectionEnabled(true);
 	}
 
 	
 	public JTable getTeacherTable() {
 		return table;
 	}
 	
 	
 	// Make the object [][] representation of students to be added into the
 		// JTable
 		public void populateTable() {
 			int i = 0;
 			data = new Object[300][4];
 			if (teachers.getSize() > 0) {
 				ArrayList<Teachers> tList = teachers.getTeachers();
 				Iterator<Teachers> it = tList.iterator();
 				while (it.hasNext()) {
 					Teachers t = it.next();
 					data[i][0] = t.getName();
 					StringBuilder line = new StringBuilder();
 					ArrayList<Integer> mClasses = t
 							.getPreference(Teachers.Type.MATH);
 					for (int j = 0; j < mClasses.size(); j++) {
 						line.append(mClasses.get(j) + ";");
 					}
 					data[i][1] = line.toString();
 					line = new StringBuilder();
 					ArrayList<Integer> rClasses = t
 							.getPreference(Teachers.Type.READ);
 					for (int j = 0; j < rClasses.size(); j++) {
 						line.append(rClasses.get(j) + ";");
 					}
 					data[i][2] = line.toString();
 					line = new StringBuilder();
 					ArrayList<Integer> lClasses = t.getPreference(Teachers.Type.LA);
 					for (int j = 0; j < lClasses.size(); j++) {
 						line.append(lClasses.get(j) + ";");
 					}
 					data[i][3] = line.toString();
 					i++;
 
 				}
 			}
 
 			// Add an empty row
 
 			while (i < 300) {
 				data[i][0] = "";
 				data[i][1] = "";
 				i++;
 			}
 			
 			tm.setDataVector(data, columnNames);
 
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
 
 			String currName = data[row][0].toString();
 
 			Teachers t;
 			boolean newTeacher = false;
 			if (teachers.hasTeacher(currName)) {
 				t = teachers.getTeacher(currName);
 			} else {
 				t = new Teachers(currName, clsFac);
 				newTeacher = true;
 			}
 
 			switch (column) {
 			case 0:
 				if (isBlank) {
					//cleanTeacherDB();
 				} else {
 					boolean isRepeat = false;
 					for (int i = 0; i < 300; i ++) {
 						if (d.toString().equals(data[i][0].toString()) && i != row)
 							isRepeat = true;
 					}
 					if (isRepeat) {
 						JOptionPane
 								.showMessageDialog(frame,
 										"A teacher with that name already exists in the scheduling system.");
 						table.setValueAt("", row, column);
 					} else {
 						t.setName(d.toString());
 					}
 				}
				cleanTeacherDB();
 				break;
 			case 1:
 				if (data[row][0].toString().isEmpty()) {
 					if (!isBlank) {
 						JOptionPane.showMessageDialog(frame,
 								"Please provide a name first.\n");
 						table.setValueAt("", row, column);
 					}
 				} else {
 					String[] classList = d.toString().split(";");
 					ArrayList<Integer> classes = new ArrayList<Integer>();
 					for (int i = 0; i < classList.length; i++) {
 						try {
 							int tmp = Integer.parseInt(classList[i]);
 							classes.add(tmp);
 
 						} catch (NumberFormatException ne) {
 							// maybe an error here, but they probably wont be
 							// inputting integers so we'll need to do conversions
 						}
 					}
 					t.setPreference(classes, Teachers.Type.MATH);
 				}
 				break;
 			case 2:
 				if (data[row][0].toString().isEmpty()) {
 					if (!isBlank) {
 						JOptionPane.showMessageDialog(frame,
 								"Please provide a name first.\n");
 						table.setValueAt("", row, column);
 					}
 				} else {
 					String[] classList = d.toString().split(";");
 					ArrayList<Integer> classes = new ArrayList<Integer>();
 					for (int i = 0; i < classList.length; i++) {
 						try {
 							int tmp = Integer.parseInt(classList[i]);
 							classes.add(tmp);
 
 						} catch (NumberFormatException ne) {
 							// maybe an error here, but they probably wont be
 							// inputting integers so we'll need to do conversions
 						}
 					}
 					t.setPreference(classes, Teachers.Type.READ);
 				}
 				break;
 			case 3:
 				if (data[row][0].toString().isEmpty()) {
 					if (!isBlank) {
 						JOptionPane.showMessageDialog(frame,
 								"Please provide a name first.\n");
 						table.setValueAt("", row, column);
 					}
 				} else {
 					String[] classList = d.toString().split(";");
 					ArrayList<Integer> classes = new ArrayList<Integer>();
 					for (int i = 0; i < classList.length; i++) {
 						try {
 							int tmp = Integer.parseInt(classList[i]);
 							classes.add(tmp);
 
 						} catch (NumberFormatException ne) {
 							// maybe an error here, but they probably wont be
 							// inputting integers so we'll need to do conversions
 						}
 					}
 					t.setPreference(classes, Teachers.Type.LA);
 				}
 				break;
 			}
 
 			if (!isBlank) {
 				if (newTeacher) {
 					teachers.addTeacher(t);
 				} else {
 					teachers.modifyTeacher(currName, t);
 				}
 			}
 			tm.setDataVector(data, columnNames);
 			renderTable();
 		}
 
 		/*
 		 * I apologize for the inefficiency of this, but given the small data set,
 		 * it shouldn't be awful
 		 */
 		private void cleanTeacherDB() {
 			List<Teachers> tchrs = teachers.getTeachers();
 			for (int i = 0; i < tchrs.size(); i++) {
 				Teachers t = tchrs.get(i);
 				String name = t.getName();
 				for (int j = 0; j < teachers.getSize(); j++) {
 					if (name.equals(data[j][0].toString())) {
 						return;
 					}
 				}
 				// if we got here, the student is not in the data array anymore
 				teachers.removeTeacher(name);
 			}
 			update();
 		}
 	
 	
 
 }
