 package windows;
 
 import java.awt.EventQueue;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JSlider;
 import javax.swing.SwingConstants;
 import javax.swing.border.EmptyBorder;
 
 public class SoundManagerGUI extends JFrame
 {
 	private static final long serialVersionUID = 874341534684864135L;
 	private static JPanel contentPane;
 	private static JPanel soundBarPane;
 	public static JSlider[] slider = new JSlider[manager.ManagerC.numApps];
	private static JLabel[] label = new JLabel[9];
 	
 	
 	public SoundManagerGUI()
 	{
 		setTitle("Sound Manager by Mats & Eivind");
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		setBounds(100, 100, 550, 485);
 		bldcontentPane();
 		setContentPane(contentPane);
 	}
 	public static void bldcontentPane()
 	{
 		contentPane = new JPanel();
 		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
 		GridBagLayout gbl_contentPane = new GridBagLayout();
 		gbl_contentPane.columnWidths = new int[]{90, 90, 90, 90, 90, 90, 90, 90, 90};
 		gbl_contentPane.rowHeights = new int[]{80,80,80,80,80,80};
 		contentPane.setLayout(gbl_contentPane);
 		bldsoundBarPane();
 	}
 	public static void bldsoundBarPane()
 	{
 		soundBarPane = new JPanel();
 		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
 		GridBagLayout gbl_soundBarPane = new GridBagLayout();
 		gbl_soundBarPane.columnWidths = new int[]{90, 90, 90, 90, 90, 90, 90, 90, 90};
 		gbl_soundBarPane.rowHeights = new int[]{60, 160, 40};
 		GridBagConstraints gbc_soundBarPane = new GridBagConstraints();
 		gbc_soundBarPane.insets = new Insets(0,5,5,5);
 		gbc_soundBarPane.gridx = 0;
 		gbc_soundBarPane.gridwidth = 6;
 		gbc_soundBarPane.gridy = 2;
 		gbc_soundBarPane.gridheight = 3;
 		soundBarPane.setLayout(gbl_soundBarPane);
 		contentPane.add(soundBarPane, gbc_soundBarPane);
 		bldSoundBarComponents();
 	}
 	public static void bldSoundBarComponents()
 	{
 		soundBar(manager.ManagerC.numApps);
 	}
 	public static void soundBar(int numApps)
 	{
 		slider = new JSlider[numApps]; // Create a slider for each detected app. Variable to avoid building unnecessary sliders
 		for (int i = 0; i < slider.length; i++)
 		{
 			GridBagConstraints gbc_slider = new GridBagConstraints();
 			gbc_slider.insets = new Insets(0, 0, 0, 0);
 			gbc_slider.gridx = 0+i;
 			gbc_slider.gridy = 1;
 			gbc_slider.anchor = GridBagConstraints.CENTER;
 			gbc_slider.fill = GridBagConstraints.VERTICAL;
 			soundBarPane.add(slider[i], gbc_slider);
			buildLabel(appName);
 		}
 /*		slider0 = new JSlider();
 		slider0.setOrientation(SwingConstants.VERTICAL);
 		GridBagConstraints gbc_slider0 = new GridBagConstraints();
 		gbc_slider0.insets = new Insets(0, 0, 0, 0);
 		gbc_slider0.gridx = 0;
 		gbc_slider0.gridy = 1;
 		gbc_slider0.anchor = GridBagConstraints.CENTER;
 		gbc_slider0.fill = GridBagConstraints.VERTICAL;
 		soundBarPane.add(slider0, gbc_slider0);
 		buildLabel(appName); */
 	}
 	public static void buildLabel(String appName)
 	{
 /*		label = new JLabel(appName);
 		GridBagConstraints gbc_label0 = new GridBagConstraints();
 		gbc_label0.insets = new Insets(5,5,5,5);
 		gbc_label0.gridx = 0;
 		gbc_label0.gridy = 2;
 		gbc_label0.anchor = GridBagConstraints.CENTER;
 		gbc_label0.fill = GridBagConstraints.HORIZONTAL;
 		soundBarPane.add(label0, gbc_label0); */
 		
 	}
 	public static void windowStart()
 	{
 		EventQueue.invokeLater
 		(
 				new Runnable()
 				{
 					public void run()
 					{
 						try
 						{
 							SoundManagerGUI frame = new SoundManagerGUI();
 							frame.setVisible(true);
 						}
 						catch (Exception e)
 						{
 							e.printStackTrace();
 						}
 					}
 				}
 		);
 	}
 }
