 import java.awt.*;
 import java.util.*;
 import java.awt.event.*;
 import java.io.File;
 
 import javax.swing.*;
 import javax.swing.filechooser.FileFilter;
 import javax.swing.table.DefaultTableCellRenderer;
 import javax.swing.table.DefaultTableModel;
 import javax.swing.table.JTableHeader;
 import javax.swing.table.TableColumn;
 import javax.swing.table.TableColumnModel;
 
 public class GUI1
 {
 	private JFrame frame;
 	private JPanel pane;
 	private JPanel buttons1;
 	private JPanel buttons2;
 	private JPanel top;
 	private JPanel bottom;
 	private JButton button2;
 	private JButton button3;
 	private JButton button4;
 	private JButton button5;
 	private JButton button6;
 	public JTable table;
 	private JOptionPane pop;
 	private String artist;
 	private String popu;
 	private String podium;
 	private String startTime;
 	private String endTime;
 	private int index = 0;
 	private inputFrame frame2;
 	private JComboBox test;
 	private ArrayList<Stage> stages;
 	private String fileName;
 	private ArrayList<Act> acts;
 	
 	public static void main(String args[])
 	{
 		new GUI1();
 	}
 
 	public GUI1()
 	{
 		makeFrame();
 		frame2 = new inputFrame();
 		frame2.getGui(this);
 		stages = new ArrayList<Stage>();
 		acts = new ArrayList<Act>();
 	}
 
 	public void makeFrame()
 	{
 		frame = new JFrame("Agenda");
 		frame.setDefaultCloseOperation(frame.EXIT_ON_CLOSE);
 		frame.setSize(800, 600);
 		frame.setResizable(false);
 
 		try
 		{
 			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
 		} catch (ClassNotFoundException e1)
 		{
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		} catch (InstantiationException e1)
 		{
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		} catch (IllegalAccessException e1)
 		{
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		} catch (UnsupportedLookAndFeelException e1)
 		{
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
 		
 		JMenuBar menu = new JMenuBar();
 		JMenu m1 = new JMenu("Bestand");
 		JMenuItem mi1 = new JMenuItem("Open Bestand");
 		JMenuItem mi2 = new JMenuItem("Opslaan");
 		JMenuItem mi3 = new JMenuItem("Opslaan Als");
 		JMenuItem mi4 = new JMenuItem("Close");
 		mi1.addActionListener(new ActionListener()
 		{
 			public void actionPerformed(ActionEvent e)
 			{
 				JFileChooser chooser = new JFileChooser();
 				chooser.setFileFilter(new CSVFilter());
 				chooser.setFileFilter(new XMLFilter());
 			    int returnVal = chooser.showOpenDialog(frame);
 			    if(returnVal == JFileChooser.APPROVE_OPTION)
 			    {
 			    	try
 			    	{
 			    		fileName = chooser.getSelectedFile().getAbsolutePath();
 			    	} catch (Exception e1)
 			    	{
 			    		System.err.println("File does not exist!");
 			    	}
 			       readFile();
 			    }
 //			    if(returnVal == JFileChooser.CANCEL_OPTION)
 //			    {
 //			    	
 //			    }
 			}
 		});
 		mi2.addActionListener(new ActionListener()
 		{
 			public void actionPerformed(ActionEvent e)
 			{
 				if (fileName == null)
 				{
 					JFileChooser chooser = new JFileChooser();
 					chooser.setFileFilter(new CSVFilter());
 					chooser.setFileFilter(new XMLFilter());
 				    int returnVal = chooser.showSaveDialog(frame);
 				    if(returnVal == JFileChooser.APPROVE_OPTION)
 				    {
 				       fileName = chooser.getSelectedFile().getAbsolutePath();
 				    }
 //				    if(returnVal == JFileChooser.CANCEL_OPTION)
 //				    {
 //				    	
 //				    }
 				}
 				writeFile();
 			}
 		});
 		mi3.addActionListener(new ActionListener()
 		{
 			public void actionPerformed(ActionEvent e)
 			{
 				JFileChooser chooser = new JFileChooser();
 				chooser.setFileFilter(new CSVFilter());
 				chooser.setFileFilter(new XMLFilter());
 			    int returnVal = chooser.showSaveDialog(frame);
 			    if(returnVal == JFileChooser.APPROVE_OPTION)
 			    {
 			       fileName = chooser.getSelectedFile().getAbsolutePath();
 			    }
 //				    if(returnVal == JFileChooser.CANCEL_OPTION)
 //				    {
 //				    	
 //				    }
 			writeFile();
 			}
 		});
 		m1.add(mi1);
 		m1.add(mi2);
 		m1.add(mi3);
 		m1.add(mi4);
 		menu.add(m1);
 		frame.setJMenuBar(menu);
 		pane = new JPanel();
 		frame.add(pane);
 		pane.setLayout(new BorderLayout());
 		Dimension buttonSize = new Dimension(230, 50);
 
 		pane = new JPanel(new BorderLayout(25, 25));
 		frame.add(pane);	
 		
 		JPanel buttons1a = new JPanel(new BorderLayout(25, 25));
 		buttons1 = new JPanel(new GridLayout(3, 1, 25, 25));	
 		buttons1a.add(buttons1, BorderLayout.EAST);
 		
 		JPanel buttons2a = new JPanel(new BorderLayout(25, 25));
 		buttons2 = new JPanel(new GridLayout(3, 1, 25, 25));
 		buttons2a.add(buttons2, BorderLayout.WEST);
 		
 		JPanel tablea = new JPanel(new BorderLayout());
 		table = new JTable(80, 5);
 		Dimension tableDimension = new Dimension(800, 600);
 		table.setPreferredSize(tableDimension);
 		table.setRowHeight(table.getRowHeight(0)*2);
 		table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
 		table.setFont(new Font("Consolas", Font.PLAIN, 11));
 		JScrollPane scrollpane = new JScrollPane(table);
 		
 		JTableHeader th0 = table.getTableHeader();
 		TableColumnModel tcm0 = th0.getColumnModel();
 		TableColumn tc0 = tcm0.getColumn(0);
 		tc0.setHeaderValue("Artiest");
 		th0.repaint();
 		JTableHeader th1 = table.getTableHeader();
 		TableColumnModel tcm1 = th1.getColumnModel();
 		TableColumn tc1 = tcm1.getColumn(1);
 		tc1.setHeaderValue("Populariteit");
 		th1.repaint();
 		JTableHeader th2 = table.getTableHeader();
 		TableColumnModel tcm2 = th2.getColumnModel();
 		TableColumn tc2 = tcm2.getColumn(2);
 		tc2.setHeaderValue("Podium");
 		th2.repaint();
 		JTableHeader th3 = table.getTableHeader();
 		TableColumnModel tcm3 = th3.getColumnModel();
 		TableColumn tc3 = tcm3.getColumn(3);
 		tc3.setHeaderValue("Begintijd");
 		th3.repaint();
 		JTableHeader th4 = table.getTableHeader();
 		TableColumnModel tcm4 = th4.getColumnModel();
 		TableColumn tc4 = tcm4.getColumn(4);
 		tc4.setHeaderValue("Eindtijd");
 		th4.repaint();
 		
 		table.getTableHeader().setFont(new Font("Consolas", Font.BOLD, 11));
 		table.setGridColor(Color.LIGHT_GRAY);
 		tablea.add(scrollpane);
 		
 		JPanel top = new JPanel(new BorderLayout(25, 25));
 		
 		JPanel bottom = new JPanel(new BorderLayout(25, 25));
 
 		// button1 = new JButton("Set Data");
 		// buttons1.add(button1);
 		// button1.addActionListener(new ActionListener()
 		// {
 		// public void actionPerformed(ActionEvent e)
 		// {
 		// setData();
 		// }
 		// });
 		
 		
 		Icon simulatorIcon = new ImageIcon("btn_simulator.png");
 		button2 = new JButton(simulatorIcon);	
 		button2.setPreferredSize(buttonSize);
 		button2.setFocusPainted(false);
 		button2.setBorderPainted(false);
 		buttons2.add(button2);
 		button2.setContentAreaFilled(false);
 		button2.addActionListener(new ActionListener()
 		{
 			public void actionPerformed(ActionEvent e)
 			{
 				//s = new Simulator();
 			}
 
 		});
 
 		Icon removeAllIcon = new ImageIcon("btn_removeall.png");
 		button3 = new JButton(removeAllIcon);		
 		button3.setPreferredSize(buttonSize);
 		button3.setContentAreaFilled(false);
 		button3.setBorderPainted(false);
 		button3.setFocusPainted(false);
 		buttons2.add(button3);
 		button3.addActionListener(new ActionListener()
 		{
 			public void actionPerformed(ActionEvent e)
 			{
 				removeData();
 				index = 0;
 			}
 		});
 		
 		Icon addIcon = new ImageIcon("btn_add.png");
 		button4 = new JButton(addIcon);
 		button4.setPreferredSize(buttonSize);
 		button4.setContentAreaFilled(false);
 		button4.setFocusPainted(false);
 		button4.setBorderPainted(false);
 		buttons1.add(button4);
 		button4.addActionListener(new ActionListener()
 		{
 			public void actionPerformed(ActionEvent e)
 			{
 				//artist = pop.showInputDialog(null, "Artiest");
 				//popu = pop.showInputDialog(null, "Populariteit");
 				//podium = pop.showInputDialog(null, "Podium");
 				//startTime = pop.showInputDialog(null, "Start Tijd:");
 				//endTime =pop.showInputDialog(null, "Eind Tijd: ");
 				//table.setValueAt(artist,index,0);
 				//table.setValueAt(popu,index,1);
 				//table.setValueAt(podium,index,2);
 				//table.setValueAt(startTime,index,3);
 				//table.setValueAt(endTime, index, 4);
 				frame2.setType(false);
 				frame2.Visible(true);
 				//index++;
 				// pop.showConfirmDialog(null,
 				// "123","Test",JOptionPane.YES_NO_CANCEL_OPTION );
 			}
 		});
 
 		Icon removeIcon = new ImageIcon("btn_remove.png");
 		button5 = new JButton(removeIcon);		
 		button5.setPreferredSize(buttonSize);
 		button5.setContentAreaFilled(false);
 		button5.setBorderPainted(false);
 		button5.setFocusPainted(false);
 		buttons1.add(button5);
 		button5.addActionListener(new ActionListener()
 		{
 			public void actionPerformed(ActionEvent e)
 			{
				if (table.getSelectedRow() != 0)
				{
 					removeRow(table.getSelectedRow());
				}
 
 			}
 		});
 
 		Icon editIcon = new ImageIcon("btn_edit.png");
 		button6 = new JButton(editIcon);		
 		button6.setPreferredSize(buttonSize);
 		button6.setContentAreaFilled(false);
 		button6.setBorderPainted(false);
 		button6.setFocusPainted(false);
 		buttons1.add(button6);
 		button6.addActionListener(new ActionListener()
 		{
 			public void actionPerformed(ActionEvent e)
 			{
 				// artist = pop.showInputDialog(null, "Artiest");
 				// popu = pop.showInputDialog(null, "Populariteit");
 				// podium = pop.showInputDialog(null, "Podium");
 				// startTime = pop.showInputDialog(null, "Start Tijd:");
 				// endTime =pop.showInputDialog(null, "Eind Tijd: ");
 				frame2.setType(true);
 				frame2.Visible(true);
 				// artist = frame2.getArtist();
 				// popu = frame2.getPopularity();
 				// podium = frame2.getPodium();
 				// startTime = frame2.getStartTime();
 				// endTime = frame2.getEndTime();
 			}
 		});
 
 		// test = new JComboBox();
 		// test.setEditable(true);
 		// test.addItem("Paul");
 		// test.addItem("Iemand");
 		// test.setSelectedItem("");
 		// pane.add(test,BorderLayout.SOUTH);
 		// test.addActionListener(new ActionListener()
 		// {
 		// public void actionPerformed(ActionEvent e)
 		// {
 		// System.out.println(test.getSelectedItem());
 		// }
 		// });
 		
 		pane.add(buttons1a, BorderLayout.WEST);
 		pane.add(buttons2a, BorderLayout.EAST);
 		pane.add(tablea, BorderLayout.CENTER);
 		pane.add(bottom, BorderLayout.NORTH);
 		pane.add(top, BorderLayout.SOUTH);
 
 		pop = new JOptionPane();
 		frame.setLocationRelativeTo(null);
 		frame.pack();
 		frame.setVisible(true);
 	}
 
 	public void setInfo()
 	{
 		artist = frame2.getArtist();
 		popu = frame2.getPopularity();
 		podium = frame2.getPodium();
 		startTime = frame2.getStartTime();
 		endTime = frame2.getEndTime();
 	}
 	
 	public void newStage(String name)
 	{
 		stages.add(new Stage(name));
 	}
 	
 	public void addAct(String artist,int pop,String stage,String startTime,String endTime)
 	{
 		acts.add(new Act(artist, pop, stage, startTime, endTime));
 	}
 	
 	public ArrayList<Stage> getStages()
 	{
 		return stages;
 	}
 	
 	public int getStageNumberOfActs(int index)
 	{
 		return index;
 		//return Stages.get(index).getActSize();
 	}
 	
 	public int getSRow()
 	{
 		return table.getSelectedRow();
 	}
 
 	public void getData()
 	{
 		int index1 = 0;
 		int index2 = 1;
 		while (index1 < 5)
 		{
 			while (index2 < 10)
 			{
 				if (table.getValueAt(index2, index1) != null)
 				{
 					System.out.println(table.getValueAt(index2, index1)
 							+ " Row: " + index2 + " Column: " + index1);
 				}
 				index2++;
 			}
 			index1++;
 			index2 = 1;
 		}
 	}
 
 	public int getIndex()
 	{
 		return index;
 	}
 
 	public void setIndex(int index)
 	{
 		this.index = index;
 	}
 
 	public void setData()
 	{
 		int index1 = 0;
 		int index2 = 0;
 		while (index1 < 5)
 		{
 			while (index2 < 10)
 			{
 				table.setValueAt("Hello", index2, index1);
 				index2++;
 			}
 			index1++;
 			index2 = 0;
 
 		}
 	}
 
 	public void removeData()
 	{
 		int index1 = 0;
 		int index2 = 0;
 		while (index1 < 5)
 		{
 			while (index2 < 10)
 			{
 				table.setValueAt(null, index2, index1);
 				index2++;
 			}
 			index1++;
 			index2 = 0;
 		}
 	}
 
 	public void removeRow(int index2)
 	{
 		int index1 = 0;
 		while (index1 < 5)
 		{
 			table.setValueAt(null, index2, index1);
 			index1++;
 		}
 	}
 
 	public void setRow(int index2)
 	{
 		table.setValueAt(artist, index2, 0);
 		table.setValueAt(popu, index2, 1);
 		table.setValueAt(podium, index2, 2);
 		table.setValueAt(startTime, index2, 3);
 		table.setValueAt(endTime, index2, 4);
 	}
 
 	public void setRow2()
 	{
 		table.setValueAt(artist, index, 0);
 		table.setValueAt(popu, index, 1);
 		table.setValueAt(podium, index, 2);
 		table.setValueAt(startTime, index, 3);
 		table.setValueAt(endTime, index, 4);
 	}
 	
 	public void setRowSane(int index2, String artist, String popu, String podium, String start, String end)
 	{
 		table.setValueAt(artist,index2,0);
 		table.setValueAt(popu, index2, 1);
 		table.setValueAt(podium,index2,2);
 		table.setValueAt(start,index2,3);
 		table.setValueAt(end,index2,4);
 	}
 	
 	public void writeFile()
 	{
 		String dataStream = "";
 		if (fileName.endsWith(".csv"))
 			dataStream = genCSV();
 		else if (fileName.endsWith(".xml"))
 			dataStream = genXML();
 		//TODO make exception for unsupported extensions
 		IOWrite writer = new IOWrite();
 		writer.write(dataStream, fileName);
 		System.out.println(dataStream);
 	}
 	
 	public void readFile()
 	{
 		IOWrite reader = new IOWrite();
 		String dataStream = reader.read(fileName);
 		if (!dataStream.equals(""))
 		{
 			removeData();
 			if (fileName.endsWith(".csv"))
 				parseCSV(dataStream);
 			else if (fileName.endsWith(".xml"))
 				parseXML(dataStream);
 			//else
 				//TODO make exception for other filetypes
 			
 		}
 		else
 		{
 			System.out.println("File doesn't match expectations, aborting");
 			fileName = null;
 		}
 	}
 	
 	public void parseCSV(String buffer)
 	{
 		stages = new ArrayList<Stage>();
 		acts = new ArrayList<Act>();
 		String[] dataChunks = buffer.split("Acts;");
 		String actData = dataChunks[0].split("Stages;")[1];
 		System.out.println(actData);
 		for (String item : actData.split("\n"))
 		{
 			System.out.println("adding stage " + item);
 			newStage(item);
 		}
 		System.out.println(dataChunks[1]);
 		for (String item : dataChunks[1].split("\n"))
 		{
 			if (item.contains(";"))
 			{
 				String[] actdata = item.split(";");
 				System.out.println("adding an act with artist " + actdata[0] + ", an endtime of " + actdata[1] + ", an popularity of " + actdata[2] + ", on the " + actdata[3] + " stage, ending on " + actdata[4]);
 				setRowSane(getIndex(), actdata[0], actdata[2], actdata[3], actdata[4], actdata[1]);
 				setIndex(getIndex() + 1);
 				addAct(actdata[0], Integer.parseInt(actdata[2]), actdata[3], actdata[4], actdata[1]);
 			}
 		}
 	}
 	
 	public String genCSV()
 	{
 		//this generates the CSV file used in the saving process
 		String dataStream = "AGENDAFILE\nStages;\n";
 		for (Stage item : stages)
 		{
 			dataStream += item.getName() + ";\n";
 		}
 		dataStream += "Acts;\n";
 		for (Act item : acts)
 		{
 			dataStream += item.getArtist()
 						+ ";"
 						+ item.getEndTime()
 						+ ";"
 						+ item.getPopularity()
 						+ ";"
 						+ item.getStage()
 						+ ";"
 						+ item.getStartTime()
 						+ ";"
 						+ "\n";
 		}
 		return dataStream;
 	}
 	
 
 	public void parseXML(String buffer)
 	{
 		//>not hardcoding parsers
 		//>losing performance by using libraries
 		//>2013
 		stages = new ArrayList<Stage>();
 		acts = new ArrayList<Act>();
 		String[] dataChunks = buffer.split("<xml>")[1].split("</xml>")[0].split("</stages>");
 		for (String item : dataChunks[0].split("<stages>")[1].split("<stage>"))
 		{
 			if (item.contains("<name"))
 				newStage(item.split("<name>")[1].split("</")[0]);
 		}
 		for (String item : dataChunks[1].split("<acts>")[1].split("</acts>")[0].split("<act>"))
 		{
 			item = item.split("</act>")[0];
 			//
 			String[] lel = item.split("<artist>");
 			if (lel.length >= 2)
 			{
 				setRowSane(getIndex(), item.split("<artist>")[1].split("</")[0], item.split("<popularity>")[1].split("</")[0], item.split("<stage>")[1].split("</")[0], item.split("<stime>")[1].split("</")[0], item.split("<etime>")[1].split("</")[0]);
 				setIndex(getIndex() + 1);
 				addAct(item.split("<artist>")[1].split("</")[0], Integer.parseInt(item.split("<popularity>")[1].split("</")[0]), item.split("<stage>")[1].split("</")[0], item.split("<stime>")[1].split("</")[0], item.split("<etime>")[1].split("</")[0]);
 			}
 		}
 	}
 	
 	public String genXML()
 	{
 		String dataStream = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n<xml>";
 		dataStream += "\n\t<stages>";
 		for (Stage item : stages)
 		{
 			dataStream += "\n\t\t<stage>\n\t\t\t<name>" + item.getName() + "</name>\n\t\t</stage>";
 		}
 		dataStream += "\n\t</stages>";
 		dataStream += "\n\t<acts>";
 		for (Act item : acts)
 		{
 			dataStream += "\n\t\t<act>"
 						+ "\n\t\t\t<artist>" + item.getArtist() + "</artist>"
 						+ "\n\t\t\t<etime>" + item.getEndTime() + "</etime>"
 						+ "\n\t\t\t<popularity>" + item.getPopularity() + "</popularity>"
 						+ "\n\t\t\t<stage>" + item.getStage() + "</stage>"
 						+ "\n\t\t\t<stime>" + item.getStartTime() + "</stime>"
 						+ "\n\t\t</act>";
 		}
 		dataStream += "\n\t</acts>\n</xml>";
 		return dataStream;
 	}
 }
 
 class CSVFilter extends FileFilter  
 {  
 	public boolean accept(File f)  
 	{
 		return f.isDirectory()||f.getName().toLowerCase().endsWith(".csv");
 	}  
 	 
 	public String getDescription()  
 	{  
 		return ".csv files";  
 	}
 
 }
 
 class XMLFilter extends FileFilter  
 {  
 	public boolean accept(File f)  
 	{
 		return f.isDirectory()||f.getName().toLowerCase().endsWith(".xml");
 	}  
 	 
 	public String getDescription()  
 	{  
 		return ".xml files";  
 	}
 
 }
