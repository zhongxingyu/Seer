 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 import javax.swing.JTable;
 import javax.swing.event.TableModelEvent;
 import javax.swing.event.TableModelListener;
 import javax.swing.table.TableModel;
 
 public class TeacherController implements ActionListener, TableModelListener {
 	JFrame frame;
 	TeacherDB teachers;
 	JTable table;
 	Object[][] data;
 	Object[][] backup;
 	String[] columnNames = { "Name", "Math Class Levels",
 			"Reading Class Levels", "Language Arts Class Levels" };
 
 	public TeacherController(JFrame f, TeacherDB t) {
 		frame = f;
 		teachers = t;
 	}
 
 	public void actionPerformed(ActionEvent event) {
 		Object obj = event.getSource();
 		TeacherMenu.chooser = new JFileChooser();
 		TeacherMenu.chooser
 				.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
 		TeacherMenu.chooser.setFileFilter(TeacherMenu.chooser
 				.getAcceptAllFileFilter());
 		if (obj.equals(TeacherMenu.open) || obj.equals(TeacherMenu.openb)) {
 			// use chooser.getSelectedFile() to get file
 			// Some code here to parse or call a parser
 			if (TeacherMenu.chooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION)
 				readFile(TeacherMenu.chooser.getSelectedFile());
 
 		} else if (obj.equals(TeacherMenu.save)
 				|| obj.equals(TeacherMenu.saveb)) {
 			if (TeacherMenu.chooser.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION)
 				writeFile(TeacherMenu.chooser.getSelectedFile());
 		} else if (obj.equals(TeacherMenu.copy)) {
 
 		} else if (obj.equals(TeacherMenu.paste)) {
 
 		} else if (obj.equals(TeacherMenu.exit1)
 				|| obj.equals(TeacherMenu.exitb)) {
 			// Probably want to do checking here if user wants to save
 			System.exit(0);
 		} else if (obj.equals(TeacherMenu.scheduleb)) {
 			// TODO: Code here to call schedule algorithm and display schedules
 			ScheduleTeachers.assign(teachers);
 		}
 		// Starting Kai's edit
 		else if (obj.equals(TeacherMenu.pref1)) {
 			String str = JOptionPane.showInputDialog(null,
 					"Enter max number of classes : ",
 					"Set Max number of Classes", 1);
 			try {
 				if (str != null) {
 					ClassFactory.setMaxCls(Integer.parseInt(str));
 					JOptionPane.showMessageDialog(null,
 							"Max number of classes successfully set to"
 									+ ClassFactory.getMaxCls(), "Success", 1);
 				}
 			} catch (Exception e) {
 				JOptionPane.showMessageDialog(null, "Please input an integer.",
 						"Warning", 1);
 			}
 		}
 		// end of kai's edit
 	}
 
 	private void readFile(File filename) {
 		try {
 			FileInputStream fis = new FileInputStream(filename);
 			BufferedReader br = new BufferedReader(new InputStreamReader(fis));
 			while (br.ready()) {
 				String line = br.readLine();
 				String[] params = line.split(",");
				System.out.println(params.length);

 				Teachers t;
 				if (!Utilities.isBlank(params[0])) {
 					t = new Teachers(params[0]);
 				} else {
 					return;
 				}
 				if (params.length == 1) {
 					teachers.addTeacher(t);
 					return;
 				}
 				String[] mClassList = params[1].split(";");
 				ArrayList<Integer> mClasses = new ArrayList<Integer>();
 				for (int i = 0; i < mClassList.length; i++) {
 					try {
 						int tmp = Integer.parseInt(mClassList[i]);
 						mClasses.add(tmp);
 					} catch (NumberFormatException e) {
 						// maybe an error here, but they probably wont be
 						// inputting integers so we'll need to do conversions
 					}
 				}
 				t.setPreference(mClasses, Teachers.Type.MATH);
 				if (params.length == 2) {
 					teachers.addTeacher(t);
 					return;
 				}
 
 				String[] rClassList = params[2].split(";");
 				ArrayList<Integer> rClasses = new ArrayList<Integer>();
 				for (int i = 0; i < rClassList.length; i++) {
 					try {
 						int tmp = Integer.parseInt(rClassList[i]);
 						rClasses.add(tmp);
 					} catch (NumberFormatException e) {
 						// maybe an error here, but they probably wont be
 						// inputting integers so we'll need to do conversions
 					}
 				}
 				t.setPreference(rClasses, Teachers.Type.READ);
 				if (params.length == 3) {
 					teachers.addTeacher(t);
 					return;
 				}
 
 				String[] lClassList = params[3].split(";");
 				ArrayList<Integer> lClasses = new ArrayList<Integer>();
 				for (int i = 0; i < lClassList.length; i++) {
 					try {
 						int tmp = Integer.parseInt(lClassList[i]);
 						lClasses.add(tmp);
 					} catch (NumberFormatException e) {
 						// maybe an error here, but they probably wont be
 						// inputting integers so we'll need to do conversions
 					}
 				}
 				t.setPreference(lClasses, Teachers.Type.LA);
 				teachers.addTeacher(t);
 
 			}
			new TeacherFrame(frame, teachers);

 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (NumberFormatException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	private void writeFile(File filename) {
 		try {
 			FileOutputStream fos = new FileOutputStream(filename);
 			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
 			ArrayList<Teachers> tchrs = teachers.getTeachers();
 
 			for (int i = 0; i < teachers.getSize(); i++) {
 				Teachers t = tchrs.get(i);
 				System.out.println(t.getName());
 				StringBuilder line = new StringBuilder(t.getName() + ",");
 				ArrayList<Integer> mClasses = t
 						.getPreference(Teachers.Type.MATH);
 				for (int j = 0; j < mClasses.size(); j++) {
 					line.append(mClasses.get(j) + ";");
 				}
 				line.append(',');
 				ArrayList<Integer> rClasses = t
 						.getPreference(Teachers.Type.READ);
 				for (int j = 0; j < rClasses.size(); j++) {
 					line.append(rClasses.get(j) + ";");
 				}
 				line.append(',');
 				ArrayList<Integer> lClasses = t.getPreference(Teachers.Type.LA);
 				for (int j = 0; j < lClasses.size(); j++) {
 					line.append(lClasses.get(j) + ";");
 				}
				line.append("\n");
 				bw.append(line.toString());
 			}
 			bw.close();
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
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
 
 		backup = data;
 		table = new JTable(data, columnNames);
 		table.getModel().addTableModelListener(this);
 
 	}
 
 	@Override
 	public void tableChanged(TableModelEvent e) {
 		int row = e.getFirstRow();
 		int column = e.getColumn();
 		TableModel model = (TableModel) e.getSource();
 		Object d = model.getValueAt(row, column);
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
 			t = new Teachers(currName);
 			newTeacher = true;
 		}
 
 		switch (column) {
 		case 0:
 			if (isBlank) {
 				cleanTeacherDB();
 			} else {
 				if (teachers.hasTeacher(d.toString())) {
 					JOptionPane
 							.showMessageDialog(frame,
 									"A teacher with that name already exists in the scheduling system.");
 					table.setValueAt("", row, column);
 				} else {
 					t.setName(d.toString());
 				}
 			}
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
 	}
 
 }
