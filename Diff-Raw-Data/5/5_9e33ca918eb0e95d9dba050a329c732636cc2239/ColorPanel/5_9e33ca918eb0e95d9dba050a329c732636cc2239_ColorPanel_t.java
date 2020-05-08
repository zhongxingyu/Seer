 package display;
 
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.*;
 import java.util.ArrayList;
 
 public class ColorPanel extends JPanel implements ActionListener{
 
 	private static final long serialVersionUID = 1L;
 	
 	private JButton chooserButton;
 	private JPanel colorPanel;
 	private JPanel greyPanel;
 	protected JButton deleteButton;
 	private JFrame parent;
 	
 	public ColorPanel(JFrame frame) 
 	{
 		super();
 		this.parent = frame;
 		setBorder(BorderFactory.createLineBorder(Color.BLACK, 3));
 		GridBagLayout gbl_panel = new GridBagLayout();
 		gbl_panel.columnWidths = new int[] {0};
 		gbl_panel.rowHeights = new int[] {0, 0, 0, 0, 0, 0};
 		gbl_panel.columnWeights = new double[]{0.0};
 		gbl_panel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0};
 		setLayout(gbl_panel);
 		
 		chooserButton = new JButton("Choose Color");
 		GridBagConstraints gbc_btnChooseColor = new GridBagConstraints();
 		gbc_btnChooseColor.fill = GridBagConstraints.HORIZONTAL;
 //		gbc_btnChooseColor.insets = new Insets(0, 0, 5, 0);
 		gbc_btnChooseColor.gridwidth = 2;
 		gbc_btnChooseColor.gridx = 0;
 		gbc_btnChooseColor.gridy = 0;
 		chooserButton.addActionListener(this);
 		add(chooserButton, gbc_btnChooseColor);
 		
 		JLabel lblChosenColor = new JLabel("Chosen Color:");
 		GridBagConstraints gbc_lblChosenColor = new GridBagConstraints();
 		gbc_lblChosenColor.fill = GridBagConstraints.VERTICAL;
 		gbc_lblChosenColor.anchor = GridBagConstraints.FIRST_LINE_START;
 		gbc_lblChosenColor.gridx = 0;
 		gbc_lblChosenColor.gridy = 1;
 		add(lblChosenColor, gbc_lblChosenColor);
 		
 		colorPanel = new JPanel();
		colorPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, Color.BLACK));
 		GridBagConstraints gbc_panel_1 = new GridBagConstraints();
 		gbc_panel_1.insets = new Insets(0, 0, 5, 0);
 		gbc_panel_1.fill = GridBagConstraints.BOTH;
 		gbc_panel_1.gridx = 0;
 		gbc_panel_1.gridy = 2;
 		colorPanel.setPreferredSize(new Dimension(50, 50));
 		add(colorPanel, gbc_panel_1);
 		
 		JLabel lblChosenColorGrey = new JLabel("Chosen Color in greyscale: ");
 		GridBagConstraints gbc_lblChosenColorIn = new GridBagConstraints();
 		gbc_lblChosenColorIn.anchor = GridBagConstraints.BASELINE;
 //		gbc_lblChosenColorIn.insets = new Insets(0, 0, 0, 5);
 		gbc_lblChosenColorIn.gridx = 0;
 		gbc_lblChosenColorIn.gridy = 3;
 		add(lblChosenColorGrey, gbc_lblChosenColorIn);
 		
 		greyPanel = new JPanel();
		greyPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, Color.BLACK));
 		GridBagConstraints gbc_panel_2 = new GridBagConstraints();
 		gbc_panel_2.fill = GridBagConstraints.BOTH;
 		gbc_panel_2.gridx = 0;
 		gbc_panel_2.gridy = 4;
 		greyPanel.setPreferredSize(new Dimension(50, 50));
 		add(greyPanel, gbc_panel_2);
 		
 		deleteButton = new JButton("Delete Color");
 		GridBagConstraints gbc_btnDeleteColor = new GridBagConstraints();
 		gbc_btnDeleteColor.fill = GridBagConstraints.HORIZONTAL;
 		//gbc_btnDeleteColor.insets = new Insets(0, 0, 5, 0);
 		gbc_btnDeleteColor.gridwidth = 2;
 		gbc_btnDeleteColor.gridx = 0;
 		gbc_btnDeleteColor.gridy = 5;
 		deleteButton.addActionListener(this);
 		add(deleteButton, gbc_btnDeleteColor);
 	}
 	
 	public int GetAvailableGreyScale(Color tmp)
 	{
 		double GreyComponent = 0.2125*tmp.getRed() + 0.7154*tmp.getGreen() + 0.0721*tmp.getBlue();
 		double SubdivisionLength = 255.0/9;
 		int IndexGreyScaleInTable = (int) (GreyComponent/SubdivisionLength);
 		if (((SecondWindow)parent).TableGreyScale[IndexGreyScaleInTable] == false) 
 		{
 			((SecondWindow)parent).TableGreyScale[IndexGreyScaleInTable] = true;
 			((SecondWindow)parent).TableColour[IndexGreyScaleInTable][0] = tmp.getRed();
 			((SecondWindow)parent).TableColour[IndexGreyScaleInTable][1] = tmp.getGreen();
 			((SecondWindow)parent).TableColour[IndexGreyScaleInTable][2] = tmp.getBlue();
 			return (int) (IndexGreyScaleInTable*SubdivisionLength);
 		}
 		else 
 		{
 			int i=0;
 			while (((SecondWindow)parent).TableGreyScale[i] == true && i<40)
 			{
 				i++;
 			}
 			((SecondWindow)parent).TableGreyScale[i] = true;
 			((SecondWindow)parent).TableColour[i][0] = tmp.getRed();
 			((SecondWindow)parent).TableColour[i][1] = tmp.getGreen();
 			((SecondWindow)parent).TableColour[i][2] = tmp.getBlue();
 			return (int) (i*SubdivisionLength);
 		}
 	}
 	
 	
 	public void RemoveColorInTables(Color tmp)
 	{
 		boolean IndexFound = false;
 		int i=0;
 		while(IndexFound == false && i<10)
 		{
 			if(((SecondWindow)parent).TableColour[i][0] == tmp.getRed())
 			{
 				if(((SecondWindow)parent).TableColour[i][1] == tmp.getGreen())
 				{
 					if(((SecondWindow)parent).TableColour[i][2] == tmp.getBlue())
 					{
 						IndexFound = true;
 					}
 				}
 			}
 			i++;
 		}
 		if (IndexFound == true)
 		{
 			((SecondWindow)parent).TableGreyScale[i-1] = false;
 			((SecondWindow)parent).TableColour[i-1][0] = -1;
 			((SecondWindow)parent).TableColour[i-1][1] = -1;
 			((SecondWindow)parent).TableColour[i-1][2] = -1;	
 		}
 	}
 	
 	public void actionPerformed(ActionEvent ae) {
 		if (ae.getSource() == chooserButton)
 		{
 		   Color tmp = JColorChooser.showDialog(this,"Choose color",this.getBackground());
 		   System.out.println(tmp);
 		   if (tmp != null)
 		   {
 			   Color chosenColor = tmp;
 			   colorPanel.setBackground(chosenColor);
 			   //Greyscale (luminosity method)
 			   int GreyComponentAvailable = GetAvailableGreyScale(tmp);
 			   Color chosenColorGrey = new Color (GreyComponentAvailable, GreyComponentAvailable, GreyComponentAvailable);
 			   greyPanel.setBackground(chosenColorGrey);
 		   }
 		 }else if (ae.getSource() == deleteButton)
 		 {
 			 Color tmp = this.colorPanel.getBackground();
 			 if (tmp != null)
 			 {
 				 RemoveColorInTables(tmp);
 			 }
 			 this.parent.remove(this);
 			 this.parent.pack();
 		 }
 	}
 }
