 package lightbeam.playground;
 
 import java.awt.Color;
 import java.awt.Cursor;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.Graphics;
 import java.awt.Rectangle;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseMotionAdapter;
 import java.awt.image.BufferedImage;
 import java.io.IOException;
 
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.RepaintManager;
 
 import core.tilefactory.AbstractTileSetFactory;
 import core.tilestate.ITileState;
 import core.tilestate.Tile;
 import core.tilestate.TileArray;
 
 import lightbeam.tiles.TileBeam;
 import lightbeam.tiles.TileBeamsource;
 import lightbeam.tiles.TileField;
 
 public class MapArea
 {
 	private AbstractTileSetFactory tileset		= null;
 	private JPanel panel						= null;
 	private JScrollPane scroll					= new JScrollPane();
 	
 	final static Color CBLUE					= new Color( 3, 115, 210, 110 );
 	final static Color CRED						= new Color( 255, 0, 0, 100 );
	final static Color CGREEN					= new Color( 76, 188, 64, 100 );
	final static Color CTRANSPARENT				= new Color( 255, 255, 255, 0 );	
 		
 	final static Cursor CURSOR_HAND				= new Cursor( Cursor.HAND_CURSOR );
 	final static Cursor CURSOR_DEFAULT			= new Cursor( Cursor.DEFAULT_CURSOR );
 	
 	private RepaintManager m					= null;
 	private TileArray map						= null;
 	private int[] oldFocused 					= new int[2];
 //	private TileArray oldMap					= null;
 	
 	private String mapName						= "Testkarte";
 	
 	public MapArea( AbstractTileSetFactory tileset, int rows, int cols )
 	{
 		this.tileset	= tileset;
 
 		this.tileset.setSelected( this.tileset.tile( 1 ) );
 		
 		this.panel		= new JPanel() 
 		{
 			private static final long serialVersionUID = -6409045276617050561L;
 
 			public void paintComponent( Graphics g )
 			{
 				MapArea.this.paintComponent( g );
 			}
 		};
 		
 		this.panel.setLayout( null );
 		
 		// MapArray init:
 		this.initMap( rows, cols );
 		
 		scroll.setViewportView( this.panel );
 		this.panel.setDoubleBuffered( true );
 		this.initScrollpane();
 		
 		this.m	= RepaintManager.currentManager( this.panel );
 		
 		this.panel.addMouseListener(new MouseAdapter(){public void mouseClicked( MouseEvent e ) 
 		{
 		}});
 		
 		this.panel.addMouseMotionListener(new MouseMotionAdapter(){public void mouseDragged(MouseEvent e) 
 		{
 		}});
 		
 		this.panel.addMouseMotionListener(new MouseMotionAdapter(){public void mouseMoved( MouseEvent e ) 
 		{
 			int row	= e.getY() / 32;
 			int col = e.getX() / 32;
			
			if( MapArea.this.isInArea( row, col ) )
			{
				MapArea.this.dehighlightPossibleBeams();
				MapArea.this.paintFocused( row, col );
				
				if( MapArea.this.isBeamsource( row, col ) )
				{
					MapArea.this.setPossibleBeams( row, col );
				}
			}
 		}});
 	}
 	
 	public JScrollPane getScrollPane() { return this.scroll; }
 	
 	private void paintComponent( Graphics g )
 	{
 		Rectangle r	= g.getClipBounds();
 
 		int startRow	= ( r.y / 32 );
 		int startCol	= ( r.x / 32 ); 
 
 		int endRow		= this.map.rows();
 		int endCol		= this.map.cols();
 		
 		for( int row = startRow; row < endRow; row++ )
 		{
 			for( int col = startCol; col < endCol; col++ )
 			{
 				Tile tile				= this.map.tile( row, col );
 
 				BufferedImage imgTile	= tile.image();
 				
 				int strength			= tile.strength();
 				
 				g.drawImage( imgTile, col * 32, row * 32, this.panel );
 				g.setColor( tile.color() );
 				g.fillRect( ( col * 32 ), ( row * 32 ), 32, 32 );				
 				
 				// Ich denke max 999 Beams/Beamsource sollten reichen!
 				// Keine Lust auf Relative Grenermittlung der FontSizes sowie Padding,
 				// Margin, etc. in Relation zum parentTile !!!
 				if( strength > 0 && tile.type() == "beamsource" )
 				{
 					g.setColor( new Color( 0, 0, 0, 255 ) );
 					
 					if( strength < 100 )
 					{
 						g.setFont( new Font( "Arial", Font.BOLD, 22 ) );
 						
 						if( strength < 10 )
 						{
 							g.drawString( strength+"", ( col * 32 ) + 11, ( row * 32 ) + 24 );		
 						} else
 						{
 							g.drawString( strength+"", ( col * 32 ) + 4, ( row * 32 ) + 24 );		
 						}
 					} else
 					{
 						g.setFont( new Font( "Arial", Font.BOLD, 12 ) );
 						g.drawString( strength+"", ( col * 32 ) + 5, ( row * 32 ) + 21 );
 					}
 				} else if( strength == 0 && tile.type() == "beamsource" )
 				{
 					g.setFont( new Font( "Arial", Font.BOLD, 22 ) );
 					g.setColor( Color.RED );
 					g.drawString( "!", ( col * 32 ) + 13, ( row * 32 ) + 24 );
 					g.setColor( new Color( 149, 47, 49, 100 ) );
 					g.fillRect( ( col * 32 ) + 2, ( row * 32 ) + 2, 28, 28 );
 				}
 			}
 		}
 	}
 	
 	public void initScrollpane()
 	{
 		//Hier wird nun eine Feste gre des JPanel gesetzt.
 		this.panel.setPreferredSize( new Dimension( this.map.rows() * 32, this.map.cols() * 32 ) );
 		
 		this.scroll.setViewportView( this.panel );
 	}
 	
 	public TileArray getMap() 	{ return this.map; 		} 
 	public String getMapName()	{ return this.mapName; 	}
 	
 	public void setMap( TileArray map, Boolean savegame )			
 	{ 
 		this.map 			= map;
 		
 		ITileState newTile	= null;
 		int rows			= this.map.rows();
 		int cols			= this.map.cols();
 		
 		for( int row = 0; row < rows; row++ )
 		{
 			for( int col = 0; col < cols; col++ )
 			{
 				String type			= this.map.tile( row, col ).type();
 				
 				if( type.equals( "field" ) )
 				{
 					try 						{ newTile = new TileField(); 		}
 					catch( IOException e ) 		{ e.printStackTrace();				}
 				} else if( type.equals( "beam" ) )
 				{
 					try 						{ newTile = new TileBeam(); 		}
 					catch( IOException e ) 		{ e.printStackTrace();				}
 					
 					if( savegame == false  )	
 					{ 
 						newTile.hidden( true );
 						newTile.image( this.tileset.tile( 1 ).image() );
 					}
 				} else if( type.equals( "beamsource" ) )
 				{
 					try 						{ newTile = new TileBeamsource(); 	}
 					catch( IOException e ) 		{ e.printStackTrace();				}
 				}
 				
 				this.map.tile( row, col ).setTileState( newTile );
 			}
 		}
 	}
 	
 	public void setMapName( String mapName )	{ this.mapName = mapName; 	}
 	public void reload()						{ this.scroll.repaint();	}
 	
 	public void initMap( int rows, int cols )
 	{
 		this.map		= new TileArray( rows, cols );
 
 		for( int row = 0; row < rows; row++ )
 		{
 			for( int col = 0; col < cols; col++ ) 
 			{
 				this.map.setTile( row, col ).withState( this.tileset.getSelected() );
 			}
 		}
 	}
 	
 	private boolean isInArea( int row, int col )
 	{
 		int maxRow	= MapArea.this.map.rows();
 		int maxCol	= MapArea.this.map.cols();
 		
 		return ( row < maxRow && col < maxCol && row > -1 && col > -1 )? true : false;
 	}
 	
 	
 	private Tile getFocused( int row, int col )
 	{
 		if( this.isInArea( row, col ) )
 		{
 			this.oldFocused[0]	= row;
 			this.oldFocused[1]	= col;
 			
 			return this.map.tile( row, col );
 		} else
 		{
 			return null;
 		}
 	}
 	
 	private void paintFocused( int row, int col )
 	{
 		
 		if( this.oldFocused != null )
 		{
 			int oldRow		= this.oldFocused[0];
 			int oldCol		= this.oldFocused[1];
 
 			this.map.tile( oldRow, oldCol ).color( CTRANSPARENT );
 			this.m.addDirtyRegion( this.scroll, oldRow * 32, oldCol * 32, 32, 32 );
 		}
 		
 		if( MapArea.this.isInArea( row, col ) )
 		{
 			this.scroll.setCursor( CURSOR_HAND );
 			
 			Tile tile	= this.getFocused( row, col );
 			
 			if( tile.type() == "field" || ( tile.type() == "beam" && tile.hidden() ) )
 			{
 				tile.color( CBLUE );
 				
 				this.m.addDirtyRegion( this.scroll, row * 32, col * 32, 32, 32 );
 			} else if( tile.type() == "beamsource" )
 			{
				tile.color( CGREEN );
 				
 				this.m.addDirtyRegion( this.scroll, row * 32, col * 32, 32, 32 );
 			}
 			
 			this.scroll.repaint();
 		} else
 		{
 			this.scroll.setCursor( MapArea.CURSOR_DEFAULT );
 		}
 	}
 	
 	private boolean isBeamsource( int row, int col )
 	{
 		return ( this.map.tile( row, col ).type() == "beamsource" )? true : false; 
 	}
 	
 	private void setPossibleBeams( int row, int col )
 	{
 		Tile source		= this.map.tile( row, col );
 		
 		int toLeft		= this.getLeftPossibleBeams( source );
 		int toTop		= this.getTopPossibleBeams( source );
 		int toRight		= this.getRightPossibleBeams( source );
 		int toBottom	= this.getBottomPossibleBeams( source );
 
 		for( int cntCol = col; cntCol > toLeft; cntCol-- )
 		{
 			highlightPossibleBeams( this.map.tile( row, cntCol ) );
 		}
 		
 		for( int cntRow = row; cntRow > toTop; cntRow-- )
 		{
 			highlightPossibleBeams( this.map.tile( cntRow, col ) );
 		}
 		
 		for( int cntCol = col; cntCol < toRight; cntCol++ )
 		{
 			highlightPossibleBeams( this.map.tile( row, cntCol ) );
 		}
 		
 		for( int cntRow = row; cntRow < toBottom; cntRow++ )
 		{
 			highlightPossibleBeams( this.map.tile( cntRow, col ) );
 		}
 		
 		this.scroll.repaint();
 	}
 	
 	private int getLeftPossibleBeams( Tile beamsource )
 	{
 		int bRow		= beamsource.row();
 		int bCol		= beamsource.col() - 1;
 		int bStrength	= beamsource.strength();
 		
 		int min 		= ( ( bCol - bStrength ) > 0 )? bCol - bStrength : - 1;
 		
 		for( int col = bCol; col > min; col-- )
 		{
 			Tile crossing	= this.map.tile( bRow, col );
 			
 			if( crossing.type() == "beamsource" || ( crossing.type() == "beam" && crossing.hidden() == false ) )
 			{
 				return bCol - ( bCol - col ) - 1;
 			}
 		}
 		
 		return min;
 	}
 	
 	private int getTopPossibleBeams( Tile beamsource )
 	{
 		int bRow		= beamsource.row() - 1;
 		int bCol		= beamsource.col();		
 		int bStrength	= beamsource.strength();
 		
 		int min			= ( bRow - bStrength > 0 )? bRow - bStrength : - 1;
 		
 		for( int row = bRow; row > min; row-- )
 		{
 			Tile crossing	= this.map.tile( row, bCol );
 			
 			if( crossing.type() == "beamsource" || ( crossing.type() == "beam" && crossing.hidden() == false ) ) 
 			{
 				return bRow - ( bRow - row ) - 1; 
 			}
 		}
 		
 		return min;	
 	}
 	
 	private int getRightPossibleBeams( Tile beamsource )
 	{
 		int bRow		= beamsource.row();
 		int bCol		= beamsource.col() + 1;		
 		int bStrength	= beamsource.strength();
 		
 		int max			= ( bCol + bStrength < this.map.cols() )? bCol + bStrength : this.map.cols();
 		
 		for( int col = bCol; col < max; col++ )
 		{
 			Tile crossing	= this.map.tile( bRow, col );
 			
 			if( crossing.type() == "beamsource" || ( crossing.type() == "beam" && crossing.hidden() == false ) ) 
 			{
 				return col; 
 			}
 		}
 		
 		return max;	
 	}
 	
 	private int getBottomPossibleBeams( Tile beamsource )
 	{
 		int bRow		= beamsource.row() + 1;
 		int bCol		= beamsource.col();		
 		int bStrength	= beamsource.strength();
 		
 		int max			= ( bRow + bStrength < this.map.rows() )? bRow + bStrength : this.map.rows();
 		
 		for( int row = bRow; row < max; row++ )
 		{
 			Tile crossing	= this.map.tile( row, bCol );
 			
 			if( crossing.type() == "beamsource" || ( crossing.type() == "beam" && crossing.hidden() == false ) ) 
 			{
 				return row; 
 			}
 		}
 		
 		return max;	
 	}
 	
 	private void highlightPossibleBeams( Tile pBeam )
 	{
 		if( pBeam.type() == "field" )
 		{
 			pBeam.color( MapArea.CBLUE );
 			this.m.addDirtyRegion( this.scroll, pBeam.row() * 32, pBeam.col() * 32, 32, 32 );
 		} else if( pBeam.type() == "beam" && pBeam.hidden() == true )
 		{
 			pBeam.color( MapArea.CBLUE );
 			this.m.addDirtyRegion( this.scroll, pBeam.row() * 32, pBeam.col() * 32, 32, 32 );
 		}
 	}
 	
 	private void dehighlightPossibleBeams()
 	{
 		int rows	= this.map.rows();
 		int cols	= this.map.cols();
 		
 		for( int row = 0; row < rows; row++ )
 		{
 			for( int col = 0; col < cols; col++ )
 			{
 				Tile tile	= this.map.tile( row, col );
 				
 				if( !tile.type().equals( "beamsource" ) ) { tile.color( CTRANSPARENT ); }
 			}
 		}
 	}
 }
