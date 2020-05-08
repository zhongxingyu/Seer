 
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.widgets.Menu;
 import org.eclipse.swt.widgets.MenuItem;
 import org.eclipse.swt.widgets.Table;
 import org.eclipse.swt.widgets.TableItem;
 import org.eclipse.swt.widgets.TableColumn;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.swt.widgets.Control;
 
 import java.awt.Dimension;
 import java.awt.Image;
 import java.awt.Toolkit;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 import java.sql.*;
 import java.util.*;
 
 import javax.imageio.ImageIO;
 
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.layout.FormLayout;
 import org.eclipse.swt.layout.FormData;
 import org.eclipse.swt.layout.FormAttachment;
 import org.eclipse.swt.widgets.Combo;
 
 
 
 public class Zayavku {
 
 	protected Shell shell;
 	private static Table table;
 	private static Combo combo;
 	private static Connection conn;
 	private Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
 	
 	/**
 	 * Launch the application.
 	 * 
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		try {
 			Zayavku window = new Zayavku();
 			window.open();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Open the window.
 	 */
 	public void open() {
 		Display display = Display.getDefault();
 		createContents();
 		shell.open();
 		shell.layout();
 		while (!shell.isDisposed()) {
 			if (!display.readAndDispatch()) {
 				display.sleep();
 			}
 		}
 	}
 
 	/**
 	 * Create contents of the window.
 	 */
 	protected void createContents() {
 		shell = new Shell();
 		shell.setSize(1304, 768);
 		shell.setLocation((screenSize.width - shell.getSize().x) /2, (screenSize.height - shell.getSize().y)/2);
 		shell.setText("Заявки");
 		shell.setLayout(new FormLayout());
 
 		Menu menu = new Menu(shell, SWT.BAR);
 		shell.setMenuBar(menu);
 
 		MenuItem mntmNewSubmenu = new MenuItem(menu, SWT.CASCADE);
 		mntmNewSubmenu.setText("File");
 
 		Menu menu_1 = new Menu(mntmNewSubmenu);
 		mntmNewSubmenu.setMenu(menu_1);
 
 		MenuItem mntmExit_2 = new MenuItem(menu_1, SWT.NONE);
 		mntmExit_2.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				System.exit(0);
 			}
 		});
 		mntmExit_2.setText("Exit");
 				
 						table = new Table(shell, SWT.BORDER | SWT.FULL_SELECTION);
 						FormData fd_table = new FormData();
 						fd_table.top = new FormAttachment(0, 5);
 						fd_table.right = new FormAttachment(100, -10);
 						fd_table.left = new FormAttachment(0, 5);
 						table.setLayoutData(fd_table);
 						table.setHeaderVisible(true);
 						table.setLinesVisible(true);
 						
 								TableColumn tblclmnNewColumn_1 = new TableColumn(table, SWT.LEFT);
 								tblclmnNewColumn_1.setWidth(120);
 								tblclmnNewColumn_1.setText("Вулиця");
 								
 								TableColumn tblclmnNewColumn = new TableColumn(table, SWT.LEFT);
 								tblclmnNewColumn.setWidth(100);
 								tblclmnNewColumn.setText("Кв.");
 								
 								TableColumn tableColumn = new TableColumn(table, SWT.LEFT);
 								tableColumn.setWidth(100);
 								tableColumn.setText("Логін");
 								
 								TableColumn tableColumn_1 = new TableColumn(table, SWT.LEFT);
 								tableColumn_1.setWidth(100);
 								tableColumn_1.setText("Помилка");
 								
 								TableColumn tableColumn_2 = new TableColumn(table, SWT.LEFT);
 								tableColumn_2.setWidth(120);
 								tableColumn_2.setText("Дата виклику");
 								
 								TableColumn tableColumn_3 = new TableColumn(table, SWT.LEFT);
 								tableColumn_3.setWidth(100);
 								tableColumn_3.setText("Час Виклику");
 								
 								TableColumn tableColumn_4 = new TableColumn(table, SWT.LEFT);
 								tableColumn_4.setWidth(100);
 								tableColumn_4.setText("Статус");
 								
 								TableColumn tableColumn_5 = new TableColumn(table, SWT.LEFT);
 								tableColumn_5.setWidth(147);
 								tableColumn_5.setText("Домашній телефон");
 		
 				Button btnLoad = new Button(shell, SWT.NONE);
 				fd_table.bottom = new FormAttachment(btnLoad, -6);
 				
 				TableColumn tableColumn_6 = new TableColumn(table, SWT.LEFT);
 				tableColumn_6.setWidth(150);
 				tableColumn_6.setText("Мобільний телефон");
 				
 				TableColumn tblclmnNewColumn_2 = new TableColumn(table, SWT.LEFT);
 				tblclmnNewColumn_2.setWidth(100);
 				tblclmnNewColumn_2.setText("Виконавець");
 				
 				TableColumn tableColumn_7 = new TableColumn(table, SWT.LEFT);
 				tableColumn_7.setWidth(110);
 				tableColumn_7.setText("Коментарі");
 				
 				
 				combo = new Combo(shell, SWT.NONE);
 				combo.setItems(new String[] {"Всі", "Виконані", "Не виконані"});
 				FormData fd_combo = new FormData();
 				fd_combo.bottom = new FormAttachment(btnLoad, 0, SWT.BOTTOM);
 				fd_combo.left = new FormAttachment(table, 0, SWT.LEFT);
 				combo.setLayoutData(fd_combo);
 				combo.select(2);
 				
 				FormData fd_btnLoad = new FormData();
 				fd_btnLoad.left = new FormAttachment(100, -102);
 				fd_btnLoad.bottom = new FormAttachment(100, -10);
 				fd_btnLoad.right = new FormAttachment(100, -10);
 				btnLoad.setLayoutData(fd_btnLoad);
 				btnLoad.addSelectionListener(new SelectionAdapter() {
 					@Override
 					public void widgetSelected(SelectionEvent e) {
 						table.removeAll();
 						db_connect();
 						select_all(combo.getSelectionIndex());
					}										
 				});
 				btnLoad.setText("Load");
 				shell.setTabList(new Control[]{table, btnLoad});
 	}
 	
 	private static void db_connect(){
 		try {
 		String url = "jdbc:postgresql://localhost/zayavka";
 		Properties props = new Properties();
 		props.setProperty("user","postgres");
 		props.setProperty("password","palamarc");
 		conn = DriverManager.getConnection(url, props);
 		} catch (SQLException e) {
			System.out.println("!!!Connection error!!!");
 			e.printStackTrace();
 		}
 	}
 
 	private static void select_all(int status){
 		String stat = new String();
 		switch (status) {
 		case 0:
 			stat = "";
 			break;
 		case 1:
 			stat = "AND zayavka_zayavka.status = true";
 			break;
 		case 2:
 			stat = "AND zayavka_zayavka.status = false";
 			break;
 		}
 		
 		try {
 		Statement st = conn.createStatement();
 		ResultSet rs = st.executeQuery(
 				"SELECT sorting, kv, login, zayavka_error.name, date, time ,status, domtel, mobtel, zayavka_worker.name, comments " +
 				"FROM zayavka_zayavka, zayavka_dom, zayavka_error, zayavka_worker " +
 				"WHERE zayavka_dom.id = zayavka_zayavka.vyl_id " +
 				"AND zayavka_zayavka.error_id = zayavka_error.id " +
 				"AND zayavka_zayavka.who_do_id = zayavka_worker.id " + stat);
 		while (rs.next()) {
 		    TableItem tableItem= new TableItem(table, SWT.NONE);
 		    tableItem.setText(0, rs.getString(1));
 		    tableItem.setText(1, rs.getString(2));
 		    tableItem.setText(2, rs.getString(3));
 		    tableItem.setText(3, rs.getString(4));
 			try {
 				tableItem.setText(4, rs.getDate(5).toString());
 			} catch (NullPointerException e) {
 				tableItem.setText(4, "Немає");}
 
 			try {
 				tableItem.setText(5, rs.getString(6));
 			} catch (IllegalArgumentException e) {
 				tableItem.setText(5, "Немає");}
 			
 			tableItem.setText(6, new Boolean(rs.getBoolean(7)).toString());
 			tableItem.setText(7, rs.getString(8));
 			tableItem.setText(8, rs.getString(9));
 			tableItem.setText(9, rs.getString(10));
 			tableItem.setText(10, rs.getString(11));
 		}
 		st.close();
 		rs.close();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 	}
 }
