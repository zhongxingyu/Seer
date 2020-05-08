 package org.sdu.database;
 
 import java.awt.*;
 import java.awt.event.*;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.sql.ResultSet;
 
 import javax.swing.*;
 
 import org.sdu.ui.UIHelper;
 
 /**
  * Demo of database management application.
  * 
  * @version 0.1 rev 8003 Dec. 26, 2012
  * Copyright (c) HyperCube Dev Team
  */
 public class Main extends JFrame {
 
 	private static final long serialVersionUID = 1L;
 	
 	private JList<?> list;
 	private Dimension modeButton = new Dimension(30, 30);
 	private Dimension dataButton = new Dimension(25, 25);
 	String[] name = new String[1000], idList = new String[1000],
 			faculty = new String[1000], pic = new String[1000],
 			idnum = new String[1000];
 
 	class pictextlist extends JPanel implements ListCellRenderer<Object> {
 		private static final long serialVersionUID = 1L;
 		
 		private boolean isSelected;
 		private int index;
 
 		@Override
 		public Component getListCellRendererComponent(JList<?> list, Object value,
 				int index, boolean isSelected, boolean cellHasFocus) {
 			this.isSelected = isSelected;
 			this.index = index;
 			return this;
 		}
 
 		public void paintComponent(Graphics g) {
 			if (isSelected) {
 				g.setColor(UIHelper.lightColor);
 				g.fillRect(0, 0, 200, 100);
 				g.setColor(Color.white);
 			}
 			g.drawImage(new ImageIcon("art/database/nopic.png").getImage(), 0,
 					0, this);
 			if (pic[index].length() == 32)
 				try {
 					URL picURL = new URL("http://211.87.219.40/pic/"
 							+ pic[index].substring(0, pic[index].length() - 5)
 							+ "/"
 							+ pic[index].substring(pic[index].length() - 5)
 							+ ".jpg");
 					g.drawImage(
 							new ImageIcon(new ImageIcon(picURL).getImage()
 									.getScaledInstance(75, 100,
 											java.awt.Image.SCALE_SMOOTH))
 									.getImage(), 0, 0, this);
 				} catch (MalformedURLException e) {
 				}
 			g.drawString(name[index], 80, 20);
 			g.drawString(idList[index], 80, 40);
 			g.drawString(faculty[index], 80, 60);
 			g.drawString("山东大学", 80, 80);
 		}
 
 		public Dimension getPreferredSize() {
 			return new Dimension(200, 100);
 		}
 	}
 
 	/**
 	 * Create the frame.
 	 */
 	public Main() {
 		// Window
 		super("数据库管理");
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		setBounds(100, 100, 450, 300);
 		setMinimumSize(new Dimension(450, 300));
 
 		// Menu
 		JMenuBar menuBar = new JMenuBar();
 		setJMenuBar(menuBar);
 
 		JMenu dataMenu = new JMenu("数据");
 		menuBar.add(dataMenu);
 
 		JMenuItem importData = new JMenuItem("导入数据库...");
 		dataMenu.add(importData);
 
 		JMenuItem exportData = new JMenuItem("导出数据库...");
 		dataMenu.add(exportData);
 
 		JMenuItem exportCurrentData = new JMenuItem("导出当前条目...");
 		dataMenu.add(exportCurrentData);
 
 		// Buttons
 		JButton statButton = new JButton("统计");
 		// statButton.addActionListener(this);
 
 		final JButton filterButton = new JButton("筛选");
 		// filterButton.addActionListener(this);
 		getRootPane().setDefaultButton(filterButton);
 
 		JButton addButton = new JButton(new ImageIcon(
 				"art/database/addicon.png"));
 		addButton.setPreferredSize(dataButton);
 		addButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 			}
 		});
 
 		JButton deleleButton = new JButton(new ImageIcon(
 				"art/database/deleteicon.png"));
 		deleleButton.setPreferredSize(dataButton);
 
 		JToggleButton textmode = new JToggleButton(new ImageIcon(
 				"art/database/texticon.png"), true);
 		textmode.setSelectedIcon(new ImageIcon(
 				"art/database/texticonselected.png"));
 		textmode.setPreferredSize(modeButton);
 
 		JToggleButton pictextmode = new JToggleButton(new ImageIcon(
 				"art/database/pictexticon.png"));
 		pictextmode.setSelectedIcon(new ImageIcon(
 				"art/database/pictexticonselected.png"));
 		pictextmode.setPreferredSize(modeButton);
 
 		JToggleButton picmode = new JToggleButton(new ImageIcon(
 				"art/database/picicon.png"));
 		picmode.setSelectedIcon(new ImageIcon(
 				"art/database/piciconselected.png"));
 		picmode.setPreferredSize(modeButton);
 
 		String listLabel = "";
 		String[] id = new String[1000];
 		try {
 			Database stuData = new Database("stu");
 			ResultSet rs = stuData.getAll();
 			int i = 0;
 			while (rs.next() && (i < 1000)) {
 				name[i] = rs.getString("name");
 				id[i] = rs.getString("id");
 				faculty[i] = rs.getString("faculty");
 				pic[i] = rs.getString("pic");
 				i++;
 			}
 			if (i == 1000) {
 				listLabel = "1~1000 of " + stuData.getAllCount();
 				idList = id;
 			} else {
 				listLabel = "All " + i;
 				idList = new String[i];
 				for (int j = 0; j < i; j++)
 					idList[j] = id[j];
 			}
 			rs.close();
 			stuData.close();
 		} catch (Exception e1) {
 			e1.printStackTrace();
 		}
 
 		// Label
 		JLabel totalNum = new JLabel(listLabel);
 
 		// List
 		list = new JList<String>(idList);
 		list.setCellRenderer(new pictextlist());
		try {
			nopic=ImageIO.read(new File("art/database/nopic.png"));
		} catch (IOException e1) {
			JOptionPane.showMessageDialog(null, "找不到图片资源","缺少资源",JOptionPane.ERROR_MESSAGE);
			System.exit(-1);
		}
		list = new JList(idList);
		list.setCellRenderer(new PicList());
 		list.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
 		list.setLayoutOrientation(JList.HORIZONTAL_WRAP);
 		list.setVisibleRowCount(-1);
 		list.addMouseListener(new MouseAdapter() {
 			public void mouseClicked(MouseEvent e) {
 				if (e.getClickCount() == 2) {
 					filterButton.doClick(); // emulate button click
 				}
 			}
 		});
 
 		// Lay out
 		JScrollPane listScroller = new JScrollPane(list);
 		listScroller.setAlignmentX(LEFT_ALIGNMENT);
 
 		JPanel listPane = new JPanel();
 		listPane.setLayout(new BoxLayout(listPane, BoxLayout.PAGE_AXIS));
 		listPane.add(listScroller);
 		listPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
 
 		JPanel topPane = new JPanel();
 		topPane.setLayout(new BoxLayout(topPane, BoxLayout.LINE_AXIS));
 		topPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
 		topPane.add(totalNum);
 		topPane.add(Box.createHorizontalGlue());
 		topPane.add(textmode);
 		topPane.add(pictextmode);
 		topPane.add(picmode);
 
 		JPanel buttonPane = new JPanel();
 		buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.LINE_AXIS));
 		buttonPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
 		buttonPane.add(addButton);
 		buttonPane.add(deleleButton);
 		buttonPane.add(Box.createHorizontalGlue());
 		buttonPane.add(statButton);
 		buttonPane.add(filterButton);
 
 		Container contentPane = getContentPane();
 		contentPane.add(topPane, BorderLayout.NORTH);
 		contentPane.add(listPane, BorderLayout.CENTER);
 		contentPane.add(buttonPane, BorderLayout.PAGE_END);
 	}
 
 	/**
 	 * @param args
 	 *            Launch
 	 */
 	public static void main(String[] args) {
 		EventQueue.invokeLater(new Runnable() {
 			public void run() {
 				try {
 					Main frame = new Main();
 					frame.setIconImage(new ImageIcon("art/database/icon.png")
 							.getImage());
 					frame.setVisible(true);
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		});
 	}
 }
