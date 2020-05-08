 package lightbeam.editor;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.Rectangle;
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import core.GameEditor;
 import core.tilestate.TileArray;
 
 public class Editor extends GameEditor
 {
 	public MapSettings mapsettings			= null;
 	
 	private JPanel left_panel				= new JPanel();
 	private TilePalette palette				= null;
 	private MapArea mapArea					= null;
 	private Toolbar toolbar					= null;
 	private int initRows					= 10;
 	private int initCols					= 10;	
 	
 	public Editor()
 	{
 		//Setzen eines Fenstertitels
 		this.frame.setTitle( "Karte - erstellen/verndern" );
 		this.toolbar			= new Toolbar( this );
 		
 		this.mapArea			= new MapArea( this.eTileset, this.initRows, this.initCols );		
 		this.mapsettings		= new MapSettings( this.mapArea, this.initRows, this.initCols );		
 		this.palette			= new TilePalette( this.eTileset );
 		
 		this.palette.setBounds( new Rectangle( 5, this.mapsettings.panel().getBounds().y + this.mapsettings.panel().getBounds().height, 128, 128 ) );
 
 		this.left_panel.setLayout( null );
 		this.left_panel.setPreferredSize( new Dimension( this.mapsettings.panel().getBounds().x + this.mapsettings.panel().getBounds().width + 5, 200 ) );
 
 		this.left_panel.add( this.mapsettings.panel() );
 		this.left_panel.add( this.palette );
 		
 		this.frame.setLayout( new BorderLayout() );
  
 		this.frame.add( this.toolbar.get() ,BorderLayout.NORTH );
 		this.frame.add( this.left_panel, BorderLayout.WEST );
 		this.frame.add( this.mapArea.getScrollPane() , BorderLayout.CENTER );
 		
 		// Gre des Fensters setzen
 		this.frame.setSize( 800, 600 );
 		this.frame.setLocationRelativeTo( null );		
 	}
 
 
 //	public void show() 					{ this.frame.setVisible( true ); 	}
 	public JFrame getFrame()			{ return this.frame;				}
 	
 	public MapSettings getMapSettings()	{ return this.mapsettings;			}
 	public TilePalette getPalette() 	{ return this.palette;				}
 	public JPanel getPanel() 			{ return this.left_panel;			}
 //	public TileSet getTileset()			{ return this.tileSet;				}
 	
 	public MapArea getMap() { return this.mapArea; }
 	
 	public void saveMap()
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
 	
 	public void loadMap()
 	{
 		try {
 			JFileChooser loadDialog	= new JFileChooser();
 			
 			loadDialog.showOpenDialog( this.frame );
 			
 			FileInputStream file 	= new FileInputStream( loadDialog.getSelectedFile() );
 			BufferedInputStream buf	= new BufferedInputStream( file );
 			ObjectInputStream read 	= new ObjectInputStream( buf );
  
 			TileArray map 			= (TileArray) read.readObject();
 			String 	mapName			= (String) read.readObject();
 
//			this.mapArea.setMap( map );
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
 	
 	public void newMap()
 	{
//		this.mapArea.resetMap( this.initRows, this.initCols );
 		this.mapsettings.resetSettings( this.initRows, this.initCols );
 	}
 	
 	public JFrame getWindow()	{ return this.frame;	}
 }
