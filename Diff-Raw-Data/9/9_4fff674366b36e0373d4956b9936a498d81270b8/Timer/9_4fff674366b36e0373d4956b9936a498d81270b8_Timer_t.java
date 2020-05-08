 /*
  * This software is licensed under the GPLv3 license, included as
  * ./GPLv3-LICENSE.txt in the source distribution.
  *
  * Portions created by Brett Wilson are Copyright 2008 Brett Wilson.
  * All rights reserved.
  */
 
 package org.wwscc.bwtimer;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Font;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ComponentAdapter;
 import java.awt.event.ComponentEvent;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.io.IOException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.swing.ButtonGroup;
 import javax.swing.JButton;
 import javax.swing.JComponent;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JPanel;
 import javax.swing.JRadioButtonMenuItem;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 import javax.swing.ScrollPaneConstants;
 import javax.swing.SwingUtilities;
 import javax.swing.event.TableColumnModelEvent;
 import javax.swing.event.TableModelEvent;
 import javax.swing.table.DefaultTableCellRenderer;
 import javax.swing.table.TableColumn;
 import javax.swing.table.TableColumnModel;
 import org.wwscc.timercomm.SerialDataInterface;
 import org.wwscc.timercomm.TimerService;
 import org.wwscc.util.Logging;
 import org.wwscc.util.NF;
 import org.wwscc.util.Prefs;
 
 /**
  */
 public class Timer extends JPanel implements ActionListener
 {
 	private static final Logger log = Logger.getLogger(Timer.class.getCanonicalName());
 
 	ButtonGroup lights;
 	JLabel openPort;
 	JButton ds, df, ff; 
 	TimerTable table;
 	TimerModel model;
 	SerialDataInterface serial;
 	TimerService server;
 
 	public Timer() throws IOException
 	{
 		super(new BorderLayout());
 
 		model = new TimerModel();
 		table = new TimerTable(model);
 		JPanel bottom = new JPanel();
 		openPort = new JLabel("Not Connected");
 		bottom.add(openPort);
 
 		JScrollPane scroll = new JScrollPane(table);
 		scroll.getViewport().setBackground(Color.WHITE);
 		scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
 		scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
 
 		add(controls(), BorderLayout.NORTH);
 		add(scroll, BorderLayout.CENTER);
 		add(bottom, BorderLayout.SOUTH);
 		
 		lights = new ButtonGroup();
 		serial = null;
 		
 		try {
 			server = new TimerService("BWTimer");
 			model.addRunServerListener(server);
 			server.start();
 		} catch (IOException ioe) {
 			log.log(Level.SEVERE, "Timer Server Failed to start: {0}", ioe.getMessage());
 		}
 	}
 
 	private JComponent controls()
 	{
 		ds = new JButton("Delete Start");
 		ds.addActionListener(this);
 		ff = new JButton("Fake Finish");
 		ff.addActionListener(this);
 
 		df = new JButton("Delete Finish");
 		df.addActionListener(this);
 		df.setFont(new Font("dialog", Font.BOLD, 13));
 
 		JPanel top = new JPanel();
 		top.add(ds);
 		top.add(df);
 		top.add(ff);
 
 		return top;
 	}
 	
 	public JMenuBar getMenuBar()
 	{
 		JMenu timer = new JMenu("Timer");
 		JMenuItem co = new JMenuItem("Connect");
 		co.addActionListener(this);
 		JMenuItem di = new JMenuItem("Disconnect");
 		di.addActionListener(this);
 		JMenuItem re = new JMenuItem("Reset");
 		re.addActionListener(this);
 
 		JMenu li = new JMenu("Lights");
 		for (int ii = 2; ii <= 6; ii++)
 		{
 			JRadioButtonMenuItem m = new JRadioButtonMenuItem(""+ii);
 			m.setSelected(false);
 			m.addActionListener(this);
 			lights.add(m);
 			li.add(m);
 		}
 
 		timer.add(co);
 		timer.add(di);
 		timer.add(re);
 		timer.add(li);
 
 		JMenuBar bar = new JMenuBar();
 		bar.add(timer);
 		return bar;
 	}
 
 	public void openPort()
 	{
 		String port;
 		try
 		{
 			if ((port = SerialDataInterface.selectPort("BasicSerial")) != null)
 			{
 				if (serial != null)
 				try { serial.close(); } catch (IOException ioe) {}
 				serial = SerialDataInterface.open(port);
 				openPort.setText("Connected to " + port);
 			}
 		}
 		catch (Exception e)
 		{
 			log.log(Level.SEVERE, "Can't open port: " + e, e);
 		}
 	}
 
 	public void closePort()
 	{
 		if (serial == null)
 			return;
 		try {
 			serial.close();
 			openPort.setText("Not connected");
 		} catch (IOException ioe) {
 			log.log(Level.SEVERE, "Failed to close: " + ioe, ioe);
 		}
 		
 		serial = null;
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent e)
 	{
 		Object s = e.getSource();
 		String com = e.getActionCommand();
 		if (s == ds)
 			model.deleteStart();
 		else if (s == df)
 			model.deleteFinish();
 		else if (s == ff)
 			model.fakeFinish();
 		else if (com.equals("Reset"))
 			model.reset();
 		else if (com.equals("Connect"))
 			openPort();
 		else if (com.equals("Disconnect"))
 			closePort();
 		else
 		{
 			try {
 				model.setLights(Integer.parseInt(com));
 			} catch (NumberFormatException nfe) {}
 		}
 	}
 
 	final class TimerTable extends JTable 
 	{
 		TimeRenderer totalRenderer;
 		TimeRenderer seqRenderer;
 		int finishcolor;
 
 		public TimerTable(TimerModel model)
 		{
 			super(model);
 
 			totalRenderer = new TimeRenderer(30);
 			seqRenderer = new TimeRenderer(24);
 			finishcolor = 1;
 
 			getTableHeader().setReorderingAllowed(false);
 			setDefaultRenderer(Object.class, seqRenderer);
 			adjustColumnRatios();
 			adjustRenderers();
 		}
 
 		@Override
 		public void tableChanged(TableModelEvent e)
 		{
 			super.tableChanged(e);
 			if (e.getType() == TableModelEvent.INSERT)
 				scrollToBottom();
 			else if (e.getType() == 42)
 			{
 				finishcolor = (finishcolor+1)%2;
 				totalRenderer.setHighlight(finishcolor);
 				seqRenderer.setHighlight(finishcolor);
 				getSelectionModel().setSelectionInterval(e.getFirstRow(), e.getLastRow());
 				Toolkit.getDefaultToolkit().beep();
 			}
 		}
 
 		public void scrollToBottom()
 		{
 			Runnable scrollit = new Runnable() {
 				public void run() {
 					scrollRectToVisible(getCellRect(getModel().getRowCount(), 1, true));
 				}
 			};
 			SwingUtilities.invokeLater(scrollit);
 		}
 
 		void adjustSeqRenderer(int newWidth)
 		{
 			if (seqRenderer == null) return;
 			if (newWidth < 50)
 				seqRenderer.setSize(12);
 			if (newWidth < 100)
 				seqRenderer.setSize(18);
 			else if (newWidth < 150)
 				seqRenderer.setSize(28);
 			else
 				seqRenderer.setSize(40);
 		}
 
 		int adjustFinalRenderer(int newWidth)
 		{
 			if (totalRenderer == null) return 14;
 			if (newWidth < 100)
 				return totalRenderer.setSize(14);
 			else if (newWidth < 200)
 				return totalRenderer.setSize(28);
 			else if (newWidth < 300)
 				return totalRenderer.setSize(40);
 			else
 				return totalRenderer.setSize(60);
 		}
 		
 		public void adjustRenderers()
 		{
 			TableColumnModel m = getColumnModel();
 			if (m.getColumnCount() > 1)
 				adjustSeqRenderer(m.getColumn(0).getWidth());
 			int newFontSize = adjustFinalRenderer(m.getColumn(m.getColumnCount()-1).getWidth());
 
 			setRowHeight((int)(newFontSize*1.5));
 		}
 
 		public void adjustColumnRatios()
 		{
 			TableColumnModel m = getColumnModel();
 			if (m == null) return;
 			TableColumn c;
 			int ii;
 			for (ii = 0; ii < m.getColumnCount()-1; ii++)
 			{
 				c = m.getColumn(ii);
 				c.setCellRenderer(seqRenderer);
 				c.setMinWidth(35);
 				c.setPreferredWidth(100);
 				c.setMaxWidth(Integer.MAX_VALUE/2);
 			}
 
 			if (ii < m.getColumnCount())
 			{
 				c = m.getColumn(ii);
 				c.setCellRenderer(totalRenderer);
 				c.setMinWidth(70);
 				c.setPreferredWidth(200);
 				c.setMaxWidth(Integer.MAX_VALUE);
 			}
 		}
 
 		@Override
 		public void doLayout()
 		{
 			super.doLayout();
 			adjustRenderers(); // silly but...
 		}
 
 		@Override
 		public void columnAdded(TableColumnModelEvent e)
 		{
 			super.columnAdded(e);
 			adjustColumnRatios();
 		}
 	}
 
 	
 	final class TimeRenderer extends DefaultTableCellRenderer
 	{
 		Color bglist[] = new Color[2];
 		Color bg;
 
 		public TimeRenderer(int size)
 		{
 			super();
 			setHorizontalAlignment(CENTER);
 			setSize(size);
 			bglist[0] = new Color(170, 170, 255);
 			bglist[1] = new Color(170, 255, 170);
 			setHighlight(0);
 		}
 
 		public void setHighlight(int idx)
 		{
 			bg = bglist[idx];
 		}
 
 		public int setSize(int size)
 		{
 			setFont(new Font("dialog", Font.PLAIN, size));
 			return size;
 		}
 
 		@Override
 		public Component getTableCellRendererComponent (JTable t, Object o, boolean isSelected, boolean hasFocus, int row, int column)
 		{
 			if (isSelected)
 				setBackground(bg);
 			else
 				setBackground(Color.WHITE);
 			if (o instanceof Double)
 			{
 				Double d = (Double)o;
 				if (d > 0)
 					setText(NF.format(d));
 				else
 					setText("-");
 			}
 			else if (o != null)
 				setText(o.toString());
 			else
 				setText("");
 			return this;
 		}
 	}
 
 	public static void main(String args[])
 	{
 		try
 		{
 			Logging.logSetup("bwtimer");
 			final Timer t = new Timer();
			final JFrame f = new JFrame("Timer");
 			f.setJMenuBar(t.getMenuBar());
 			f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 			f.getContentPane().add(t);
 			f.setBounds(Prefs.getTimerWindow());
 			f.setVisible(true);
 			f.addComponentListener(new ComponentAdapter() {
 				public void componentResized(ComponentEvent e) {
 					Prefs.setTimerWindow(e.getComponent().getBounds());
 				}
 				public void componentMoved(ComponentEvent e) {
 					Prefs.setTimerWindow(e.getComponent().getBounds());
 				}
 			});
 			f.addWindowListener(new WindowAdapter() {
				 public void windowOpened(WindowEvent e) {					 
 					 t.openPort();
 				 }
 			});
			f.getRootPane().setDefaultButton(t.df);
 		}
 		catch (Throwable e)
 		{
 			log.log(Level.SEVERE, "Timer stopped: " + e, e);
 			e.printStackTrace();
 		}
 	}
 }
 
 
