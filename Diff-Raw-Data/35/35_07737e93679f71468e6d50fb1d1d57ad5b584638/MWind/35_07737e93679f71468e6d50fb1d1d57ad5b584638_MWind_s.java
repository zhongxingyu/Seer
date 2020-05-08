 package main;
 
 import java.awt.BorderLayout;
import java.awt.Event;
 import java.awt.EventQueue;
 
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 
 import javax.swing.JMenuBar;
 import javax.swing.JMenu;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.SwingUtilities;
 
 import lineGeneration.LineGenerator;
 import model.*;
 import view.*;
 import javax.swing.JButton;
 import java.awt.event.ActionListener;
 import java.awt.event.ActionEvent;
 import java.awt.Checkbox;
 import java.awt.event.MouseWheelListener;
 import java.awt.event.MouseWheelEvent;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.util.ArrayList;
 
 import javax.swing.JPopupMenu;
 
 import curveGeneration.CurveGenerator;
 
 import java.awt.Component;
 import circle.*;
 import java.awt.event.ItemListener;
 import java.awt.event.ItemEvent;
 public class MWind extends JFrame {
 	
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 	private Grid grid = new Grid(300);
 	private RadiusDialog dialog = null;
 
 	/**
 	 * Launch the application.
 	 */
 	public static void main(String[] args) {
 		EventQueue.invokeLater(new Runnable() {
 			public void run() {
 				try {
 					MWind frame = new MWind();
 					frame.setVisible(true);
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		});
 	}
 
 	/**
 	 * Create the frame.
 	 */
 	public MWind() {
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		setBounds(100, 100, 507, 485);
 		// 
 		final GridView gridView = new GridView(grid);
 		//  
 		JMenuBar menuBar = new JMenuBar();
 		setJMenuBar(menuBar);
 		
 		getContentPane().setLayout(new BorderLayout(0, 0));
 		
 		final JScrollPane scrollPane = new JScrollPane();
 		getContentPane().add(scrollPane, BorderLayout.CENTER);
 		
 		scrollPane.setViewportView(gridView);
 		//   
 		JPanel bottonPanel = new JPanel();
 		getContentPane().add(bottonPanel, BorderLayout.SOUTH);
 		
 		
 		final JButton stepBotton = new JButton("  STEP  ");
 		stepBotton.setEnabled(false);
 		//       ,    "steply"
 		stepBotton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				gridView.drawDotSteply();
 				gridView.repaint();
 			}
 
 		});
 		bottonPanel.add(stepBotton);
 		
 		
 		JButton zoomOutBotton = new JButton("  -  ");
 		//      '-'
 		zoomOutBotton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				if (grid.getStep() > 2) {
 					grid.setStep(grid.getStep() - 2);
 					gridView.repaint();
 				}
 			}
 		});
 		
 		bottonPanel.add(zoomOutBotton);
 		
 		JButton zoomInBotton = new JButton("  +  ");
 		//      '+'
 		bottonPanel.add(zoomInBotton);
 		zoomInBotton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				grid.setStep(grid.getStep() + 3);
 				gridView.repaint();
 			}
 		});
 		
 		final Checkbox stepCheckBox = new Checkbox("Steply     ");
 		stepCheckBox.addItemListener(new ItemListener() {
 			public void itemStateChanged(ItemEvent arg0) {
 				 if (arg0.getStateChange() == ItemEvent.DESELECTED)
 					 stepBotton.setEnabled(false);
 				 if (arg0.getStateChange() == ItemEvent.SELECTED)
 					 stepBotton.setEnabled(true);	 
 			}
 		});
 		bottonPanel.add(stepCheckBox);
 		
 		
 	//---------------------------------------------------------------------------------------------------------
 		// 
 		JMenu menuLines = new JMenu("\u041E\u0442\u0440\u0435\u0437\u043A\u0438");
 		menuBar.add(menuLines);
 		//.  
 		JMenuItem itemDDA = new JMenuItem("\u0426\u0414\u0410");
 		
 		//        	
 		itemDDA.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mousePressed(MouseEvent arg0) {
 				
 				ArrayList<Excel> coloredEx = LineGenerator.DDA(grid);	
 				if (coloredEx == null)
 				{
 					JOptionPane.showMessageDialog(new JButton(),
 							" 2 !", "",
 							JOptionPane.WARNING_MESSAGE);
 				}
 				else
 				{		if (stepCheckBox.getState() == false)
 						{
 							gridView.drawDots(coloredEx);
 							gridView.repaint();	
 						}
 						else 
 						{
 							gridView.setSteplyArray(coloredEx);
 						}
 					
 				}
 			}
 		});
 		menuLines.add(itemDDA);
 		
 		//
 		JMenuItem itemBrezenh = new JMenuItem("\u0411\u0440\u0435\u0437\u0435\u043D\u0445\u0435\u043C");
 		itemBrezenh.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mousePressed(MouseEvent e) {
 				ArrayList<Excel> coloredEx = LineGenerator.Brezenhem(grid.getClickedEx(), grid.getClickedEx());
 				
 				if (coloredEx == null)
 				{
 					JOptionPane.showMessageDialog(new JButton(),
 							" 2 !", "",
 							JOptionPane.WARNING_MESSAGE);
 				}
 				else
 				{					
 					if(stepCheckBox.getState() == false)
 					{
 						gridView.drawDots(coloredEx);
 						gridView.repaint();
 
 					}
 					else
 					{
 						gridView.setSteplyArray(coloredEx);
 					}
 					
 				}
 			}
 		});
 		menuLines.add(itemBrezenh);	
 		
 		// 
 		JMenuItem itemAntiAliasing = new JMenuItem("\u0423\u0441\u0442\u0440\u0430\u043D\u0435\u043D\u0438\u0435 \u0441\u0442\u0443\u043F\u0435\u043D\u0447\u0430\u0442\u043E\u0441\u0442\u0438");
 		itemAntiAliasing.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mousePressed(MouseEvent arg0) {
 				ArrayList<Excel> coloredEx = LineGenerator.WuAlgorithm(grid.getClickedEx(), grid.getClickedEx());
 				
 				if (coloredEx == null)
 				{
 					JOptionPane.showMessageDialog(new JButton(),
 							" 2 !", "",
 							JOptionPane.WARNING_MESSAGE);
 				}
 				else
 				{
 					if(stepCheckBox.getState() == false)
 					{
 						gridView.drawDots(coloredEx);
 						gridView.repaint();
 
 					}
 					else 
 					{
 						gridView.setSteplyArray(coloredEx);
 					}
 				}
 			}
 		});
 		menuLines.add(itemAntiAliasing);
 		
 		JMenu menu = new JMenu("\u041A\u0440\u0438\u0432\u044B\u0435");
 		menuBar.add(menu);
 		
 
 	//-------------------------------------------------------------------------------------------------------------------------------
 		
 	//---------------------------------  -------------------------------------------------------------------------
 		
 		//
 		JMenuItem menuCircle = new JMenuItem("\u041E\u043A\u0440\u0443\u0436\u043D\u043E\u0441\u0442\u044C");
 		menuCircle.addActionListener(new ActionListener(){
 
 			public void actionPerformed(ActionEvent e) {
 				
 				int radius = -1;
 				
 				if(dialog == null)
 				{
 					dialog = new RadiusDialog();
 				}
 				dialog.setText("10");
 				if(dialog.showDialog(MWind.this, "Radius"))
 				{
 					radius = dialog.getRadius();
 				}
 				
 				if(radius != -1)
 				{
 					ArrayList<Excel> coloredEx = CircleGenerator.circle(grid.getClickedEx(),radius);
 					if(coloredEx == null) 
 						{
 						JOptionPane.showMessageDialog(new JButton(),
 							"  !", "",
 							JOptionPane.WARNING_MESSAGE);
 						}
 					else 
 					{	
 						if(stepCheckBox.getState() == false)
 						{
 							gridView.drawDots(coloredEx);
 							gridView.repaint();
 						}
 						else
 						{
 							gridView.setSteplyArray(coloredEx);
 						}
 					}
 				}
 				
 			}
 		});
 		menu.add(menuCircle);
 		
 		
 	//-------------------------------------------------------------------------------------------------------------------------------
 		
 	//--------------------------------- -------------------------------------------------------------------------
<<<<<<< HEAD
		JMenu curveMenu = new JMenu(" ");
=======
 		JMenu curveMenu = new JMenu("\u041F\u0430\u0440\u0430\u043C\u0435\u0442\u0440\u0438\u0447\u0435\u0441\u043A\u0438\u0435 \u043A\u0440\u0438\u0432\u044B\u0435");
>>>>>>> d08be521e02674f2fca8061f5e212114df6866f9
 		menuBar.add(curveMenu);
 		
 		//
 		JMenuItem ermitaItem = new JMenuItem(" ");
 		ermitaItem.addMouseListener(new MouseAdapter(){
 			@Override
 			public void mousePressed(MouseEvent e)
 			{
 				ArrayList<Excel> coloredEx = CurveGenerator.ErmitForm(grid.getClickedEx(), grid.getClickedEx(),grid.getClickedEx(),grid.getClickedEx());
<<<<<<< HEAD
 
=======
>>>>>>> d08be521e02674f2fca8061f5e212114df6866f9
 				if (coloredEx == null)
 				{
 					JOptionPane.showMessageDialog(new JButton(),
 							" 4 !", "",
 							JOptionPane.WARNING_MESSAGE);
 				}
 				else
 				{					
 					if(stepCheckBox.getState() == false)
 					{
 						gridView.drawDots(coloredEx);
 						gridView.repaint();
 					}
 					else
 					{
 						gridView.setSteplyArray(coloredEx);
 					}
 					
 				}
 			}
 			
 		});
 		curveMenu.add(ermitaItem);
 		
 	//-----------------------------------------------------------------------------------------------------------------------------------
 	
 	//
 	//-----------------------------------------------------------------------------------------------------------------------------------	
 		// 
 		JPopupMenu popupMenu = new JPopupMenu();
 		addPopup(gridView, popupMenu);
 		//
 		JMenuItem popupClear = new JMenuItem("\u041E\u0447\u0438\u0441\u0442\u0438\u0442\u044C");	
 		popupClear.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mousePressed(MouseEvent e) {
 				grid.clear();
 				gridView.setSteplyArray(null);
 				gridView.repaint();
 			}
 		});
 		
 		popupMenu.add(popupClear);
 				
 		//    
 		gridView.addMouseWheelListener(new MouseWheelListener() {
 			public void mouseWheelMoved(MouseWheelEvent arg0) {
 				
 				
 				if(arg0.getWheelRotation() < 0)
 					grid.setStep(grid.getStep() + 2);
 				else if (grid.getStep() > 1) 
 					grid.setStep(grid.getStep() - 1);	
 			gridView.repaint();
 
 			}
 		});
 		
 		//    
 		gridView.addMouseListener(new MouseAdapter() {
 			public void mouseClicked(MouseEvent event) {				
 				//           -  
 				if (SwingUtilities.isLeftMouseButton(event) 
 						&& event.getX()<=grid.getSize()*grid.getStep()
 							&& event.getY()<=grid.getSize()*grid.getStep()){
 					int x = event.getX() / grid.getStep();
 					int y = event.getY() / grid.getStep();
 					if (grid.getExcel(x, y).isColored() == false) {
 						grid.getExcel(x, y).setColored(true);
 						
 						grid.addClickedEx(grid.getExcel(x, y));
 						
 						gridView.repaint();
 					} else {
 						grid.getExcel(x, y).setColored(false);
 						grid.removeClickedEx(grid.getExcel(x, y));
 						gridView.repaint();
 					}
 				}
 			}
 		});
 		
 		
 		
 	}
 	
 	//-   ,  
 	private static void addPopup(Component component, final JPopupMenu popup) {
 		component.addMouseListener(new MouseAdapter() {
 			public void mousePressed(MouseEvent e) {
 				if (e.isPopupTrigger()) {
 					showMenu(e);
 				}
 			}
 			public void mouseReleased(MouseEvent e) {
 				if (e.isPopupTrigger()) {
 					showMenu(e);
 				}
 			}
 			private void showMenu(MouseEvent e) {
 				popup.show(e.getComponent(), e.getX(), e.getY());
 			}
 		});
 	}
 }
