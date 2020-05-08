 package lightbeam.playground;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.JToolBar;
 
 import core.GamePlayground;
 import core.tilestate.TileArray;
 
 public class Playground extends GamePlayground
 {
 	private JPanel left_panel				= new JPanel();
 	private TilePalette palette				= null;
 	private MapArea mapArea					= null;
 	
 	private JToolBar toolBar				= null;
 	private JButton saveButton				= null;
 	private JButton openMapButton			= null;
 	private JButton openGameButton 			= null;
 	private JButton closeButton				= null;
 	
 	private int initRows					= 10;
 	private int initCols					= 10;	
 	
 	public Playground()
 	{
 		//Setzen eines Fenstertitels
 		this.frame.setTitle( "Spiel - spielen" );
 		
 		this.toolBar = new JToolBar("Toolbar", JToolBar.HORIZONTAL);
         		
 		this.mapArea				= new MapArea( this.gTileset, this.initRows, this.initCols );		
 		
 		this.left_panel.setLayout( null );
 		this.left_panel.setPreferredSize( new Dimension( 100, 200 ) );
 
 		this.frame.setLayout( new BorderLayout() );
  
 		this.frame.add( this.toolBar,BorderLayout.NORTH );
 		this.frame.add( this.left_panel, BorderLayout.WEST );
 		this.frame.add( this.mapArea.getScrollPane() , BorderLayout.CENTER );
 
 		//ToolBar fllen
 		// Neue map ertellen:
 		ImageIcon openImage = new ImageIcon("src/fx/Toolbar/open.png");
 		this.openGameButton = new JButton(openImage);
 		this.openGameButton.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent e) {
 				Playground.this.loadGame();
 			}
 		});		
 		this.toolBar.add(this.openGameButton);
 		// Map laden/ffnen:
 		this.openMapButton = new JButton(openImage);
 		this.openMapButton.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent e) {
 				Playground.this.loadMap();
 			}
 		});
 		
 		this.toolBar.add(this.openMapButton);	
 		// Map speichern:
 		ImageIcon saveImage = new ImageIcon("src/fx/Toolbar/save.png");
 		this.saveButton = new JButton(saveImage);
 		this.saveButton.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent e) {
 				Playground.this.saveGame();
 			}
 		});
 		this.toolBar.add(this.saveButton);
 		// Editor schlieen:
 		ImageIcon closeImage = new ImageIcon("src/fx/Toolbar/close.png");
 		this.closeButton = new JButton(closeImage);
 		this.closeButton.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent e) {
 				Playground.this.closePlayground();
 			}
 		});	
 		this.toolBar.add(this.closeButton);
 		
 		// Gre des Fensters setzen
 		this.frame.setSize( 800, 600 );
 		this.frame.setLocationRelativeTo( null );
 	}
 	
 	public JFrame getFrame()			{ return this.frame;				}
 	public TilePalette getPalette() 	{ return this.palette;				}
 	public JPanel getPanel() 			{ return this.left_panel;			}
 	
 	private void loadMap()
 	{
 		try {
 			JFileChooser loadDialog	= new JFileChooser();
 			
 			loadDialog.showOpenDialog( this.frame );
 			
 			FileInputStream file 	= new FileInputStream( loadDialog.getSelectedFile() );
 			BufferedInputStream buf	= new BufferedInputStream( file );
 			ObjectInputStream read 	= new ObjectInputStream( buf );
  
 			TileArray map 			= (TileArray) read.readObject();
 			String 	mapName			= (String) read.readObject();
 
 			this.mapArea.setMap( map, false );
 			this.mapArea.setMapName( mapName );
 			this.mapArea.reload();
 			
 			read.close();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (ClassNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}				
 	}
 	
 	private void loadGame()
 	{
 		try 
 		{
 			JFileChooser loadDialog	= new JFileChooser();
 			
 			loadDialog.showOpenDialog( this.frame );
 			
 			FileInputStream file 	= new FileInputStream( loadDialog.getSelectedFile() );
 			BufferedInputStream buf	= new BufferedInputStream( file );
 			ObjectInputStream read 	= new ObjectInputStream( buf );
 
			TileArray map 			= (TileArray) read.readObject();
 			String 	mapName			= (String) read.readObject();
 
 			this.mapArea.setMap( map, true );
 			this.mapArea.setMapName( mapName );
 			this.mapArea.reload();
 			
 			read.close();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (ClassNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}				
 	}	
 	
 	private void saveGame()
 	{
 		try 
 		{
 			JFileChooser saveDialog		= new JFileChooser();
 			
 			saveDialog.showSaveDialog( this.frame );
 			
 			FileOutputStream file		= new FileOutputStream( saveDialog.getSelectedFile() );
 			BufferedOutputStream buf	= new BufferedOutputStream( file );
 			ObjectOutputStream write 	= new ObjectOutputStream( buf );
 			
 			write.writeObject( this.mapArea.getMap() );
 			write.writeObject( this.mapArea.getMapName() );
 
 			write.close();
 		} catch( IOException e )
 		{
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 	public JFrame getWindow()	{ return this.frame;	}
 }
