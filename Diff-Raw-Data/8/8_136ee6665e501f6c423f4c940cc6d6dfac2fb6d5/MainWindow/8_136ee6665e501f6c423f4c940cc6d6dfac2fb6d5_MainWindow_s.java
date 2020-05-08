 package MusicCrawler;
 
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.EventQueue;
 import java.awt.Font;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.Observable;
 import java.util.Observer;
 
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextField;
 import javax.swing.border.BevelBorder;
 import javax.swing.border.EmptyBorder;
 
 public class MainWindow extends JFrame implements Observer
 {
 	/**
 	 * 
 	 */
 	Mp3Crawler music;
 	Thread thread;
 	String preview;
 	static MusicPlayer player;
 	static DownloadManager manager;
 	private static final long serialVersionUID = 1L;
 	JPanelWithBackground contentPane;
 	JPanelUpdatable panel; 
 	private String MainBgPath="res/background.jpg";
 	private JTextField textField;
 	final JLabel lblNewLabel;
 	final JButton btnNewButton;
 	JButton btnPreview,btnStopPreview;
 	static MainWindow frame;
 	boolean dManagerShowing=false;
 	
 	/**
 	 * Launch the application.
 	 */
 	public static void main(String[] args) 
 	{
 		manager=new DownloadManager();
 		player = new MusicPlayer();
 		EventQueue.invokeLater(new Runnable() 
 		{
 			public void run()
 			{
 				try 
 				{
 					frame = new MainWindow();
 					frame.setVisible(true);
 					frame.setDefaultCloseOperation(EXIT_ON_CLOSE);
 				} 
 				catch (Exception e)
 				{
 					e.printStackTrace();
 				}
 			}
 		});
 	}
 
 	/**
 	 * Create the frame.
 	 */
 	public MainWindow() 
 	{
 		setResizable(false);
 		setMinimumSize(new Dimension(950, 700));
 		setTitle("Music Crawler 2.0 by H.K");
 		setFont(new Font("Monotype Corsiva", Font.BOLD | Font.ITALIC, 19));
 		setForeground(Color.WHITE);
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		setBounds(100, 100, 450, 300);
 		
 		contentPane = new JPanelWithBackground(MainBgPath,this.getWidth(),this.getHeight());
 		contentPane.setBackground(Color.GRAY);
 		contentPane.setForeground(Color.WHITE);
 		contentPane.setToolTipText("Programs crawls the web from any starting url for music");
 		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
 		setContentPane(contentPane);
 		contentPane.setLayout(null);
 		
 		JLabel lblEnterSearch = new JLabel("\t\t\tEnter Search:");
 		lblEnterSearch.setFont(new Font("Monotype Corsiva", Font.BOLD | Font.ITALIC, 25));
 		lblEnterSearch.setForeground(Color.WHITE);
 		lblEnterSearch.setBounds(22, 43, 315, 80);
 		lblEnterSearch.setMinimumSize(new Dimension(100,100));
 		contentPane.add(lblEnterSearch);
 		
 		textField = new JTextField();
 		textField.setFont(new Font("Monotype Corsiva", Font.BOLD | Font.ITALIC, 23));
 		textField.setDragEnabled(false);
 		textField.setToolTipText("Please enter what yo want to crawl for and we will find it for you.");
 		textField.setBounds(220, 63, 641, 42);
 		contentPane.add(textField);
 		textField.setColumns(10);
 		
 		btnNewButton = new JButton("Crawl");
 		//btnNewButton.setText("Music");
 		btnNewButton.setFont(new Font("Monotype Corsiva", Font.BOLD | Font.ITALIC,24));
 		btnNewButton.setForeground(Color.BLACK);
 		btnNewButton.setBackground(Color.DARK_GRAY);
 		btnNewButton.setBounds(47, 117, 117, 58);
 		contentPane.add(btnNewButton);
 		
 		JPanelWithBackground panelWithBackground = new JPanelWithBackground("res/mainbg.png",861,446);
 		panelWithBackground.setBorder(new BevelBorder(BevelBorder.LOWERED, new Color(64, 64, 64), new Color(64, 64, 64), Color.DARK_GRAY, Color.DARK_GRAY));
 		panelWithBackground.setForeground(Color.WHITE);
 		panelWithBackground.setBounds(47, 226, 861, 446);
 		panelWithBackground.setFont(new Font("Monotype Corsiva", Font.BOLD | Font.ITALIC, 19));
 		contentPane.add(panelWithBackground);
 		
 		
 		JScrollPane scrollPane = new JScrollPane();
 		scrollPane.setBounds(47, 226, 861, 446);
 		contentPane.add(scrollPane);
 		
 		panel = new JPanelUpdatable();
 		panel.setBackground(Color.BLACK);
 		scrollPane.setViewportView(panel);
 		panel.setForeground(Color.WHITE);
 		panel.setFont(new Font("Monotype Corsiva", Font.BOLD | Font.ITALIC, 19));
 		panel.setLayout(new GridLayout(0, 1, 10, 0));
 		
 		lblNewLabel = new JLabel("Your results will be displayed below\u2026\u2026.");
 		lblNewLabel.setBackground(Color.BLACK);
 		lblNewLabel.setForeground(Color.WHITE);
 		lblNewLabel.setFont(new Font("Lucida Grande", Font.BOLD | Font.ITALIC, 15));
 		lblNewLabel.setBounds(57, 177, 851, 37);
 		contentPane.add(lblNewLabel);
 		
 		JLabel lblNewLabel_1 = new JLabel("\t\tMusic Crawler 2.0.   Download  any file you want\u2026.");
 		lblNewLabel_1.setForeground(Color.WHITE);
 		lblNewLabel_1.setFont(new Font("Monotype Corsiva", Font.BOLD | Font.ITALIC, 24));
 		lblNewLabel_1.setBounds(220, 6, 641, 45);
 		contentPane.add(lblNewLabel_1);
 		
 		JButton btnDownload = new JButton("Add to Que");
 		btnDownload.setToolTipText("select a link/links then press to add to download manager");
 		btnDownload.setFont(new Font("Monotype Corsiva", Font.BOLD | Font.ITALIC, 16));
 		btnDownload.setBounds(617, 117, 117, 29);
 		contentPane.add(btnDownload);
 		
 		JButton btnNewButton_5 = new JButton("Clear");
 		btnNewButton_5.setToolTipText("press to clear the seach results if any");
 		btnNewButton_5.setBounds(736, 117, 117, 29);
 		btnNewButton_5.setFont(new Font("Monotype Corsiva", Font.BOLD | Font.ITALIC, 16));
 		contentPane.add(btnNewButton_5);
 		
 		btnPreview = new JButton("Preview");
 		btnPreview.setToolTipText("Press this to preview the links and listen to them");
 		btnPreview.setBounds(617, 150, 117, 29);
 		btnPreview.setFont(new Font("Monotype Corsiva", Font.BOLD | Font.ITALIC, 16));
 		contentPane.add(btnPreview);
 		
 		btnStopPreview = new JButton("Stop Preview");
 		btnStopPreview.setToolTipText("Press this to stop the preview or click on another link and press preview");
 		btnStopPreview.setBounds(736, 149, 117, 29);
 		btnStopPreview.setFont(new Font("Monotype Corsiva", Font.BOLD | Font.ITALIC, 16));
 		contentPane.add(btnStopPreview);
 		
 				
 		//actions performed
 		btnNewButton.addActionListener(new ActionListener()
 		{
 			public void actionPerformed(ActionEvent arg0) 
 			{
 				try
 				{
 					panel.removeAll();
 					panel.revalidate();
 					if(manager.isShowing())
 					{
 						manager.show();
 					}
 					if(!textField.getText().isEmpty())
 					{
 						music=new Mp3Crawler(textField.getText(),lblNewLabel);
 						
 						music.addObserver(panel);
 						music.addObserver(frame);
 						
 						thread = new Thread(music);
 						thread.start();
 						
 						lblNewLabel.setText("Your results will be displayed below\u2026\u2026.");
 					}
 					else
 					{
 						lblNewLabel.setText("Please enter search term before searching");
 					}
 				}
 				catch(Exception e)
 				{
 					e.printStackTrace();
 				}
 				finally
 				{
 					MainWindow.this.validate();
 				}
 			}
 		});
 		
 		btnDownload.addActionListener(new ActionListener() 
 		{
 			
 			public void actionPerformed(ActionEvent arg0)
 			{
 				if(!manager.isShowing())
 				{
 					manager.show();
 				}
 				if(panel.getComponentCount()>0)
 				{
 					try
 					{
 						for(Component link:panel.getComponents())
 						{
 							JCheckBox temp=(JCheckBox)link;
 							
 							if(temp.isSelected())
 							{
 								
 								manager.actionAdd(temp.getText());
 								panel.remove(temp);
 							}
 						}
 						panel.revalidate();
 					}
 					catch(Exception e)
 					{
 						lblNewLabel.setText("Please Search FIRST,SELECT then press download" );
 					}
 				}
 			}
 		});
 		
 		btnNewButton_5.addActionListener(new ActionListener() 
 		{
 			public void actionPerformed(ActionEvent arg0) 
 			{
 				panel.removeAll();
 				panel.revalidate();
 				textField.setText("");
 				player.stopSong();
 				lblNewLabel.setText("Your results will be displayed below\u2026\u2026.");
 			}
 		});
 		
 		btnPreview.addActionListener(new ActionListener() 
 		{
 			public void actionPerformed(ActionEvent arg0) 
 			{
 				try
 				{
 					if(!preview.isEmpty())
 					{
 						player.stopSong();
 						player.playSong(preview);
 						lblNewLabel.setText(preview);
 					}
 					else
 					{
 						lblNewLabel.setText("Click the link you want to preview then press preview");
 					}
 					
 				}
 				catch(Exception e)
 				{
 					e.printStackTrace();
 				}
 			}
 		});
 		btnStopPreview.addActionListener(new ActionListener() 
 		{
 			public void actionPerformed(ActionEvent arg0) 
 			{
 				try
 				{
 					player.stopSong();
 					lblNewLabel.setText("Your results will be displayed below\u2026\u2026.");
 					
 				}
 				catch(Exception e)
 				{
 					e.printStackTrace();
 				}
 			}
 		});
 	}
 	
 	
 
 	@Override
 	public void update(Observable o, Object arg) 
 	{
 		// TODO Auto-generated method stub
 		if(arg instanceof String)
 		{
 			String state=(String)arg;
 			if(state.equalsIgnoreCase("crawling started"))
 			{
 				btnNewButton.setEnabled(false);
 				btnNewButton.setOpaque(false);
 				state="System Crawling the web....";
 			}
 			else if(state.equalsIgnoreCase("crawling done"))
 			{
 				btnNewButton.setEnabled(true);
 				btnNewButton.setOpaque(true);
 				state="System found some results. Displaying them below";
 				if(panel.getComponentCount()<1)
 				{
 					state="No results found. please try a different search term";
 				}
 			}
 			else if(state.startsWith("preview"))
 			{
 				preview=state.substring(state.indexOf('/')+1);
 				//player.playSong(state.substring(state.indexOf('/')+1));
 			}
 			else if(state.startsWith("stop"))
 			{
 				preview="";
 				lblNewLabel.setText("Click the link you want to preview then press preview");
 			}
 			else
 			{
 				lblNewLabel.setText(state);
 			}
 		}
 	}
 }
