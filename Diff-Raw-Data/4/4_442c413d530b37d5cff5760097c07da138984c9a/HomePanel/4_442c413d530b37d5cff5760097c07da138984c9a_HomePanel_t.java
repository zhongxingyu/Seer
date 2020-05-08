 package database.tabs;
 
 import javax.swing.*;
 
 import java.awt.*;
 import java.awt.event.*;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.Locale;
 import java.util.StringTokenizer;
 
 import com.toedter.calendar.JDateChooser;
 import database.CheckURL;
 
 public class HomePanel extends JPanel {
 
 	private JTable homeTable;
 
 	private String[] homeTableHeader = new String[]{"Buch", "Autor", "Verlag", "Genre", "Schlagwort", "Regal", "verliehen an"};
 
 	private JScrollPane homePane = new JScrollPane();
 
 	private CheckURL db;
 
 	public HomePanel(CheckURL db) {
 		this.db = db;
 		setLayout(new GridLayout(1, 1));
 		updateAndAddTable();
 
 	}
 
 	public void updateAndAddTable() {
 		ResultSet bookResults = db.executeSelect("select * from overview;");
 		homeTable = new JTable(getTableContent(bookResults, homeTableHeader.length), homeTableHeader) {
 			public boolean isCellEditable(int rowIndex, int colIndex) {
 				return false;   //Disallow the editing of any cell
 			}
 		};
 		homeTable.setAutoCreateRowSorter(true);
 		homeTable.setRowSelectionAllowed(true);
 		final JPopupMenu popupMenu = new JPopupMenu();
 		popupMenu.add(new JPopupMenu.Separator());
 		homeTable.setComponentPopupMenu(popupMenu);
 		homeTable.addMouseListener(new MouseListener() {
 			@Override
 			public void mouseClicked(MouseEvent mouseEvent) {
 				if (SwingUtilities.isRightMouseButton(mouseEvent)) {
 					homeTable.changeSelection(homeTable.rowAtPoint(mouseEvent.getPoint()), homeTable.rowAtPoint(mouseEvent.getPoint()), false, false);
 					popupMenu.show(mouseEvent.getComponent(), mouseEvent.getX(), mouseEvent.getY());
 				}
 			}
 
 			@Override
 			public void mousePressed(MouseEvent mouseEvent) {
 			}
 
 			@Override
 			public void mouseReleased(MouseEvent mouseEvent) {
 			}
 
 			@Override
 			public void mouseEntered(MouseEvent mouseEvent) {
 			}
 
 			@Override
 			public void mouseExited(MouseEvent mouseEvent) {
 			}
 		});
 		popupMenu.add("Buch ausleihen").addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent actionEvent) {
 				lendBook();
 			}
 		});
 		popupMenu.add("Buch wiederbekommen").addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent actionEvent) {
 				getBookBack();
 			}
 		});
 		remove(homePane);
 		homePane = new JScrollPane(homeTable);
 		add(homePane);
 	}
 
 	private void getBookBack() {
 		String isbn = "";
 		ResultSet isbnSet = db.executeSelect("Select isbn from Buch where titel = '" + String.valueOf(homeTable.getValueAt(homeTable.getSelectedRow(), 0)) + "'");
 		try {
 			if (isbnSet.next()) {
 				isbn = isbnSet.getString("isbn");
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		final String lender = String.valueOf(homeTable.getValueAt(homeTable.getSelectedRow(), homeTable.getColumnCount() - 1));
 		if (!lender.equals("Nicht verliehen")) {
 			final JFrame receiveFrame = new JFrame();
 			receiveFrame.setTitle("Buch wiederbekommen.");
 			receiveFrame.setVisible(true);
 			receiveFrame.setAlwaysOnTop(true);
 			receiveFrame.setSize(400, 400);
 			JLabel dateLabel = new JLabel("Datum");
 			Date date = GregorianCalendar.getInstance(Locale.GERMANY).getTime();
 			final JDateChooser jDateChooser = new JDateChooser();
 			jDateChooser.setDateFormatString("dd/MM/yyyy");
 			jDateChooser.setDate(date);
 			((JTextField) jDateChooser.getDateEditor()).setEditable(false);
 			JLabel shelfLabel = new JLabel("Regal");
 			ResultSet shelfResultSet = db.executeSelect("Select Ort from Regal;");
 			String[] shelfArray = db.resultSetToStringArray(shelfResultSet, 1);
 			final JComboBox shelfBox = new JComboBox(shelfArray);
 			JButton okButton = new JButton("OK");
 			final String finalIsbn = isbn;
 			okButton.addActionListener(new ActionListener() {
 				@Override
 				public void actionPerformed(ActionEvent actionEvent) {
 					int shelfID = getID("Select regalid from regal where Ort='", String.valueOf(shelfBox.getSelectedItem()));
 					Date date = jDateChooser.getDate();
 					db.executeChanges("UPDATE ausgeliehenAn SET rueckgabedatum='" + date + "' WHERE buch='" + finalIsbn + "'");
 					db.executeChanges("UPDATE liegtin SET regal=" + shelfID + " WHERE buch='" + finalIsbn + "'");
 					receiveFrame.setVisible(false);
 					updateAndAddTable();
 				}
 			});
 			JButton cancelButton = new JButton("Cancel");
 			cancelButton.addActionListener(new ActionListener() {
 				@Override
 				public void actionPerformed(ActionEvent actionEvent) {
 					receiveFrame.setVisible(false);
 				}
 			});
 			receiveFrame.setLayout(new GridLayout(0,2));
 			receiveFrame.add(dateLabel);
 			receiveFrame.add(jDateChooser);
 			receiveFrame.add(shelfLabel);
 			receiveFrame.add(shelfBox);
 			receiveFrame.add(cancelButton);
 			receiveFrame.add(okButton);
 
 		}else{
 			JOptionPane jOptionPane = new JOptionPane();
 			JOptionPane.showMessageDialog(jOptionPane, "Das Buch ist nicht verliehen.", "FEHLER", JOptionPane.ERROR_MESSAGE);
 
 		}
 
 		}
 
 	private void lendBook() {
 		String isbn = "";
 		ResultSet isbnSet = db.executeSelect("Select isbn from Buch where titel = '" + String.valueOf(homeTable.getValueAt(homeTable.getSelectedRow(), 0)) + "'");
 		try {
 			if (isbnSet.next()) {
 				isbn = isbnSet.getString("isbn");
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		final String lender = String.valueOf(homeTable.getValueAt(homeTable.getSelectedRow(), homeTable.getColumnCount() - 1));
 		if (lender.equals("Nicht verliehen")) {
 			final JFrame lendFrame = new JFrame();
 			lendFrame.setTitle("Buch verleihen.");
 			lendFrame.setVisible(true);
 			lendFrame.setAlwaysOnTop(true);
 			lendFrame.setSize(400, 400);
 			JLabel lenderLabel = new JLabel("ausleihen an");
 			ResultSet lenderSet = db.executeSelect("Select name, vorname from Ausleiher");
 			String[] lenderArray = db.resultSetToStringArrayWithTwo(lenderSet, 1, 2);
 			final JComboBox lenderBox = new JComboBox(lenderArray);
 			JLabel dateLabel = new JLabel("Datum");
 			Date date = GregorianCalendar.getInstance(Locale.GERMANY).getTime();
 			final JDateChooser jDateChooser = new JDateChooser();
 			jDateChooser.setDateFormatString("dd/MM/yyyy");
 			jDateChooser.setDate(date);
 			((JTextField) jDateChooser.getDateEditor()).setEditable(false);
 			JButton okButton = new JButton("OK");
 			final String finalIsbn = isbn;
 			okButton.addActionListener(new ActionListener() {
 				@Override
 				public void actionPerformed(ActionEvent actionEvent) {
 					Object[] lenderArray = lenderBox.getSelectedObjects();
 					int lenderid = 0;
					Date lendDate = jDateChooser.getDate();
 					StringTokenizer stringTokenizer = new StringTokenizer(lenderArray[0].toString());
 					String name = stringTokenizer.nextToken();
 					String firstName = "";
 					//because of more than one firstName
 					while (stringTokenizer.hasMoreTokens()) {
 						if (! firstName.equals("")) {
 							firstName += " ";
 						}
 						firstName += stringTokenizer.nextToken();
 					}
 					name = name.substring(0, name.length() - 1);
 					ResultSet lenderIDSet = db.executeSelect("SELECT ausleiherid FROM ausleiher WHERE name='" + name + "' AND vorname='" + firstName + "'");
 					try {
 						lenderIDSet.next();
 						lenderid = lenderIDSet.getInt("ausleiherid");
 					} catch (SQLException e) {
 						e.printStackTrace();
 					}
					db.executeChanges("UPDATE ausgeliehenAn SET Ausleiher=" + lenderid + ", leihdatum='"+lendDate+"' WHERE buch='" + finalIsbn + "'");
 					lendFrame.setVisible(false);
 					updateAndAddTable();
 				}
 			});
 			JButton cancelButton = new JButton("Cancel");
 			cancelButton.addActionListener(new ActionListener() {
 				@Override
 				public void actionPerformed(ActionEvent actionEvent) {
 					lendFrame.setVisible(false);
 				}
 			});
 
 			lendFrame.setLayout(new GridLayout(0, 2));
 			lendFrame.add(lenderLabel);
 			lendFrame.add(lenderBox);
 			lendFrame.add(dateLabel);
 			lendFrame.add(jDateChooser);
 			lendFrame.add(cancelButton);
 			lendFrame.add(okButton);
 		} else {
 			JOptionPane jOptionPane = new JOptionPane();
 			JOptionPane.showMessageDialog(jOptionPane, "Das Buch ist bereits verliehen.", "FEHLER", JOptionPane.ERROR_MESSAGE);
 		}
 
 	}
 
 	private int getID(String sql, String idName) {
 		int id = 0;
 		ResultSet resultSet = db.executeSelect(sql + idName + "'");
 		try {
 			resultSet.next();
 			id = resultSet.getInt(1);
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 
 		return id;
 	}
 
 	public String[][] getTableContent(ResultSet bookResults, int columnLength) {
 		//needs changes
 		String[][] tableContent = null;
 		try {
 			int rowAmount = 0;
 			while (bookResults.next()) {
 				rowAmount++;
 			}
 			bookResults.beforeFirst();
 			tableContent = new String[rowAmount][columnLength];
 			int rowIndex = 0;
 			while (bookResults.next()) {
 				tableContent[rowIndex][0] = bookResults.getString("buch");
 				tableContent[rowIndex][1] = bookResults.getString("autor");
 				tableContent[rowIndex][2] = bookResults.getString("verlag");
 				tableContent[rowIndex][3] = bookResults.getString("genre");
 				tableContent[rowIndex][4] = bookResults.getString("schlagwort");
 				tableContent[rowIndex][5] = bookResults.getString("regal");
 				tableContent[rowIndex][6] = bookResults.getString("ausleiher");
 				rowIndex++;
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return tableContent;
 	}
 }
