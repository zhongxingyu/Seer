 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.GridLayout;
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
  
 import javax.media.opengl.awt.GLCanvas;
 import javax.swing.BorderFactory;
 import javax.swing.Box;
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JSpinner;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 import javax.swing.SpinnerListModel;
 import javax.swing.border.EmptyBorder;
 import javax.swing.border.LineBorder;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 
 
 
 
public class RotatePanel {
 	
     JPanel buttonsPanel = new JPanel();
     JPanel blankPanel = new JPanel();
     
 	//private Scene s;
 	//private GLCanvas c;
     
     private Scene s = GUI.getScene();
 	
 	
 	public RotatePanel(JPanel panel)
 	{
 		
 	    panel.setLayout(new BorderLayout());
 	    
 
 	    JLabel resize_title = new JLabel("R O T A T I O N");
 	    resize_title.setOpaque(true);
 	    resize_title.setBackground(Color.lightGray);
 	    
 
 	    panel.add(resize_title, BorderLayout.PAGE_START);
 	    
 	    JPanel rotatePanel1 = new JPanel();
 	    rotatePanel1.setLayout(new GridBagLayout());
 	    panel.add(rotatePanel1, BorderLayout.LINE_START);
 	    
 	    
 	    GridBagConstraints constraints = new GridBagConstraints();
 	    constraints.fill = GridBagConstraints.HORIZONTAL;
 	    constraints.insets = new Insets(4, 4, 4, 4); // 5-pixel margins on all sides
 	    constraints.anchor = GridBagConstraints.NORTHWEST;
 
 	    
 
 	    
 	    constraints.gridx = 1;
 	    constraints.gridy = 0;
 	    constraints.gridwidth = 3;
 	    constraints.gridheight = 1;
 	    constraints.weightx = constraints.weighty = 1.0;
 	    rotatePanel1.add(new JLabel(""), constraints);
 	    
 	    
 	    constraints.gridx = 1;
 	    constraints.gridy = 1;
 	    constraints.gridwidth = 3;
 	    constraints.gridheight = 1;
 	    constraints.weightx = constraints.weighty = 1.0;
 	    rotatePanel1.add(new JLabel("Axis of Rotation:"), constraints);
 	    
 	    
 	    /**********************************
 	     -----------CHECK BUTTONS----------
 	     **********************************/
         final JCheckBox x_cb = new JCheckBox("x");
 	    constraints.gridx = 1;
 	    constraints.gridy = 2;
 	    constraints.gridwidth = 1;
 	    constraints.gridheight = 1;
 	    constraints.weightx = constraints.weighty = 1.0;
 	    rotatePanel1.add(x_cb, constraints);
 
 	    final JCheckBox y_cb = new JCheckBox("y");
 	    constraints.gridx = 2;
 	    constraints.gridy = 2;
 	    constraints.gridwidth = 1;
 	    constraints.gridheight = 1;
 	    constraints.weightx = constraints.weighty = 1.0;
 	    rotatePanel1.add(y_cb, constraints);
 	    
 	    final JCheckBox z_cb = new JCheckBox("z");
 	    constraints.gridx = 3;
 	    constraints.gridy = 2;
 	    constraints.gridwidth = 1;
 	    constraints.gridheight = 1;
 	    constraints.weightx = constraints.weighty = 1.0;
 	    rotatePanel1.add(z_cb, constraints);
 	    
 
 	    ItemListener listener = new ItemListener()
 	    {
 	        public void itemStateChanged(ItemEvent e) {
 	        	s.setXAxisRotation(0.0f);
 	        	s.setYAxisRotation(0.0f);
 	        	s.setZAxisRotation(0.0f);
 	        	
 	        	if (x_cb.isSelected())
 	        		s.setXAxisRotation(1.0f);
 	        	if (y_cb.isSelected())
 	        		s.setYAxisRotation(1.0f);
 	        	if (z_cb.isSelected())
 	        		s.setZAxisRotation(1.0f);
 	        }
 	    };
 	    
 	    
 	    x_cb.addItemListener(listener);
 	    y_cb.addItemListener(listener);
 	    z_cb.addItemListener(listener);
 	    
 	    
 	    
 	    /**********************************
 	     ------------BLANK SPACE-----------
 	     **********************************/
 	    constraints.gridx = 1;
 	    constraints.gridy = 3;
 	    constraints.gridwidth = 3;
 	    constraints.gridheight = 1;
 	    constraints.weightx = constraints.weighty = 1.0;
 	    rotatePanel1.add( Box.createHorizontalStrut( 10 ), constraints );
 	    rotatePanel1.add( Box.createVerticalStrut( 10 ), constraints );
 	    
 	    
 	    /**********************************
 	     -------------SPINNERS-------------
 	     **********************************/
 	    //number of rotations
 	    constraints.gridx = 1;
 	    constraints.gridy = 4;
 	    constraints.gridwidth = 2;
 	    constraints.gridheight = 1;
 	    constraints.weightx = constraints.weighty = 0.0;
 	    rotatePanel1.add(new JLabel("Num. Rotations:"), constraints);
 	    
         
         
 	    Integer[] numRotList = new Integer[25];
 	    for (int i = 0; i < 25; i++)
 	    	numRotList[i] = i+1;
 	    
 	    Integer[] speedList = new Integer[20];
 	    for (int i = 0; i < 20; i++)
 	    	speedList[i] = i+1;
 	    
 	    
 	    SpinnerListModel numRotModel = new SpinnerListModel(numRotList);
 	    SpinnerListModel speedModel = new SpinnerListModel(speedList);
 
 	    
 	    final JSpinner numRotations = new JSpinner(numRotModel);
 	    ((JSpinner.DefaultEditor)numRotations.getEditor()).getTextField().setColumns(2);
 	    ((JSpinner.DefaultEditor)numRotations.getEditor()).getTextField().setEditable(false); 
 	    numRotations.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.GRAY));
 	    
 	    
 	    /*
 	    ChangeListener listener = new ChangeListener() {
 	        public void stateChanged(ChangeEvent e) {
 	          System.out.println("Source: " + e.getSource());
 	        }
 	      };
 	    
 	      
 	    numRotations.addChangeListener(listener);
 	    */
 	    
 
 	    final JSpinner speed = new JSpinner(speedModel);
 	    ((JSpinner.DefaultEditor)speed.getEditor()).getTextField().setEditable(false); 
 	    speed.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.GRAY));
 
         
 
 	    constraints.gridx = 3;
 	    constraints.gridy = 4;
 	    constraints.gridwidth = 2;
 	    constraints.gridheight = 1;
 	    constraints.weightx = constraints.weighty = 1.0;
 	    rotatePanel1.add(numRotations, constraints);
 	    
 	    
 	    //speed
 	    constraints.gridx = 1;
 	    constraints.gridy = 5;
 	    constraints.gridwidth = 2;
 	    constraints.gridheight = 1;
 	    constraints.weightx = constraints.weighty = 0.0;
 	    rotatePanel1.add(new JLabel("Speed (1-20):"), constraints);
 	    
 	    
 	    constraints.gridx = 3;
 	    constraints.gridy = 5;
 	    constraints.gridwidth = 2;
 	    constraints.gridheight = 1;
 	    constraints.weightx = constraints.weighty = 1.0;
 	    constraints.fill = GridBagConstraints.HORIZONTAL;
 	    rotatePanel1.add(speed, constraints);
 	    
 	    
 	    
 	    /**********************************
 	     ------------BLANK SPACE-----------
 	     **********************************/
 	    constraints.gridx = 1;
 	    constraints.gridy = 6;
 	    constraints.gridwidth = 3;
 	    constraints.gridheight = 1;
 	    constraints.weightx = constraints.weighty = 1.0;
 	    rotatePanel1.add( Box.createHorizontalStrut( 8 ), constraints );
 	    rotatePanel1.add( Box.createVerticalStrut( 8 ), constraints );
 	    
 	    
 	    
 	    /**********************************
 	     -------START / PAUSE BUTTONS------
 	     **********************************/
 	    JPanel rotatePanel2 = new JPanel();
 	    panel.add(rotatePanel2, BorderLayout.PAGE_END);
 	    
 	    JButton start_b = new JButton("start");
 	    JButton pause_b = new JButton("pause");
 	    start_b.setFont(new Font("sansserif",Font.PLAIN,11));
 	    pause_b.setFont(new Font("sansserif",Font.PLAIN,11));
 	    
 	    
 	    start_b.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e)
             {
             	s.setNumRotations((Integer)numRotations.getValue());
            	s.setRotationSpeed((Integer)speed.getValue()*0.2);
  
             	if (!((s.getXAxisRotation() == 0.0f) && (s.getYAxisRotation() == 0.0f) 
             			&& (s.getZAxisRotation() == 0.0f)))
             		s.animator.start();
             } 
         });      
 	    
 	   
 	    buttonsPanel.setLayout(new GridLayout(1,2,7,0));
 	    buttonsPanel.add(start_b, constraints);
 	    buttonsPanel.add(pause_b, constraints);
 
 	    rotatePanel2.add(buttonsPanel, BorderLayout.PAGE_START);
 	    blankPanel.setBorder(new EmptyBorder(10, 10, 15, 10) );
 	    rotatePanel2.add(blankPanel, BorderLayout.PAGE_END);
 
 	}
 	
 
 	public static void main(String[] args)
 	{
 		JFrame frame = new JFrame("3D GUI");
 
 		frame.setSize(150, 230);
 
 		
 		frame.addWindowListener(new WindowAdapter() {
 			public void windowClosing(WindowEvent e) {
 				new Thread() {
 					public void run() 
 					{
 						System.exit(0);
 					}
 				}.start();
 			}
 		});
 		
 		
 	    JPanel panel = new JPanel();
 	    frame.add(panel);
 	    
 		new RotatePanel(panel);
 		
 		
 
 		frame.setVisible(true);
 	}
 	
 }
