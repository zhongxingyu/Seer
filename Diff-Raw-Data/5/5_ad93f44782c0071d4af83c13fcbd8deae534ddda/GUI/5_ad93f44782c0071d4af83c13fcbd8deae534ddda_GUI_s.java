 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.IOException;
 import java.util.ArrayList;
 
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JScrollBar;
 import javax.swing.JScrollPane;
 import javax.swing.JSpinner;
 import javax.swing.JTextField;
 import javax.swing.SpinnerNumberModel;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 
 public class GUI extends JFrame implements ChangeListener, ActionListener
 {
 	// Static Variables
 	private static int MANHATTEN	= 0;
 	private static int EUCLIDIAN	= 1;
 
 	// Initial Settings
 	private static int dimensionCount = 3;
 	private static int clusterCount = 4;
 
 	// Parameter GridLayout and Panel
 	GridLayout gl = new GridLayout(4,1);
 	JPanel p = new JPanel(gl);
 	
 	// Point Field GridLayout, Panel, and ScrollPane
 	GridLayout spGridLayout = new GridLayout(5, 3);
 	JPanel spPanel = new JPanel(spGridLayout);
 	JScrollPane sp = new JScrollPane(spPanel);
 	JScrollBar vScrollBar;
 
 	// Button Panel and Buttons
 	JPanel pButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
 	JButton calculateButton = new JButton("Calculate");
 	JButton addButton = new JButton("+");
 	JButton removeButton = new JButton("-");
 
 	// Labels and Fields
 	ArrayList<JLabel> labels = new ArrayList<JLabel>();
 	ArrayList<JComboBox> comboBoxes = new ArrayList<JComboBox>();
 	ArrayList<JSpinner> spinners = new ArrayList<JSpinner>();
 
 	// Label Strings
 	String[] labelStrings 		= {"Algorithm", "Distance Type", "Dimensions", "Clusters"};
 	String[] algorithmStrings	= {"MST", "K-Means", "Z-Score"};
 	String[] distanceStrings	= {"Manhatten", "Euclidian"};
 
 	GUI()
 	{
 		super("Michelizer");
 		FlowLayout fl = new FlowLayout();
 		fl.setAlignment(FlowLayout.LEFT);
 		setLayout(fl);
 
 		for(int i = 0; i < labelStrings.length; i++)
 			labels.add(new JLabel(labelStrings[i] + ":"));
 		
 		comboBoxes.add(new JComboBox(algorithmStrings));
 		comboBoxes.add(new JComboBox(distanceStrings));
 		
 		spinners.add(new JSpinner(new SpinnerNumberModel(3, 1, 10, 1)));
 		spinners.add(new JSpinner(new SpinnerNumberModel(4, 1, 100, 1)));
 		
 		spinners.get(0).setValue(dimensionCount);
 		spinners.get(0).addChangeListener(this);
 		spinners.get(1).setValue(clusterCount);
 		
 		for(int i = 0; i < labels.size(); i++)
 		{
 			p.add(labels.get(i));
 			p.add((i > 1)?spinners.get(i-2):comboBoxes.get(i));
 		}
 
 		sp.setPreferredSize(new Dimension(285, 200));
 		vScrollBar = sp.getVerticalScrollBar();
 		add(p);
 		add(sp);
 		
 		pButtons.add(calculateButton);
 		pButtons.add(addButton);
 		pButtons.add(removeButton);
 		calculateButton.addActionListener(this);
 		addButton.addActionListener(this);
 		removeButton.addActionListener(this);
 		
 		add(pButtons);
 		
 		updateScrollPane(5, dimensionCount);
 	}
 	
 	public void updateScrollPane(int points, int dimensions)
 	{
 		spPanel.removeAll();
 		spGridLayout.setRows(points+1);
 		spGridLayout.setColumns(dimensions);
 		for(int i = 0; i < dimensions; i++)
 		{
 			JLabel jL = new JLabel("D" + (i+1));
 			jL.setHorizontalAlignment(JLabel.CENTER);
 			spPanel.add(jL);
 		}
 			
 		for(int i = 0; i < points; i++)
 			for(int j = 0; j < dimensions; j++)
 				spPanel.add(new JTextField());
 	
 		revalidate();
 		repaint();
 		vScrollBar.setValue(vScrollBar.getMaximum());
 	}
 	
 	public int getGUIPointCount()
 	{
 		return spPanel.getComponentCount() / dimensionCount - 1;
 	}
 	
 	public ArrayList<Point> getPoints()
 	{
 		ArrayList<Point> points = new ArrayList<Point>();
 		
 		for(int i = 0; i < getGUIPointCount(); i++)
 		{
 			Point p = new Point();
 			for(int j = 0; j < dimensionCount; j++)
			{
				p.addDimensionValue(Double.parseDouble(((JTextField)spPanel.getComponent(i*j+1)).getSelectedText()));
			}
 			points.add(p);
 		}
 		
 		return points;
 	}
 
 	@Override
 	public void stateChanged(ChangeEvent e)
 	{
 		if(e.getSource() == spinners.get(0))
 		{
 			int oldDim = dimensionCount;
 			dimensionCount = (Integer)spinners.get(0).getValue();
 			updateScrollPane((spPanel.getComponentCount()/oldDim - 1), dimensionCount);
 		}
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent e)
 	{
 		if(e.getSource() == calculateButton)
 		{
 			Functions funcObj = new Functions();
 			ArrayList<Point> points = getPoints();
 			int distanceType = (((String)comboBoxes.get(0).getSelectedItem()).equals("Manhatten"))?MANHATTEN:EUCLIDIAN;
 			int clusterCount = (Integer)spinners.get(1).getValue();
 
 			boolean result;
 			if(((String)comboBoxes.get(0).getSelectedItem()).equals("MST"))
 			{
 				try
 				{
 					result = funcObj.MST(points, clusterCount, distanceType);
 				}
 				catch (IOException e1) { e1.printStackTrace(); }
 			}
 			else if(((String)comboBoxes.get(0).getSelectedItem()).equals("K-Means"))
 			{
 				try
 				{
 					result = funcObj.K_Means(points, clusterCount, distanceType);
 				}
 				catch (IOException e1) { e1.printStackTrace(); }
 			}
 			else if(((String)comboBoxes.get(0).getSelectedItem()).equals("Z-Score"))
 			{
 				try
 				{
 					result = funcObj.Z_Score(points);
 				}
 				catch (IOException e1) { e1.printStackTrace(); }
 			}
 			
 			//Pop-Up Based on Result
 		}
 		else if(e.getSource() == addButton)
 			updateScrollPane(getGUIPointCount() + 1, dimensionCount);
 		else if(e.getSource() == removeButton)
 		{
 			if(getGUIPointCount() > 1)
 				updateScrollPane(getGUIPointCount() - 1, dimensionCount);
 		}
 	}
 }
