 package core.tilestate;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 
 public class TileArray implements Serializable
 {
 	private static final long serialVersionUID 		= 5567438036750252438L;
 	
 	public final static int MODE_READY				= 0;
 	public final static int MODE_PREVIEW			= 1;
 	public final static int TILE_FROM_LEFT			= 0;
 	public final static int TILE_FROM_TOP			= 1;	
 	public final static int TILE_FROM_RIGHT			= 2;
 	public final static int TILE_FROM_BOTTOM		= 3;
 	
 	private int rows, cols;
 	private int setRow, setCol;
 	private Tile[][] tiles;
 	private int mode								= TileArray.MODE_READY;
 	
 	private transient Tile[][] conttiles;
 	private transient ArrayList<Tile> filteredTiles	= null;
 	
 	//geta tiles
 	public Tile[][] tiles(){
 		return this.tiles;
 	}
 	
 	public TileArray() {}
 	
 	public TileArray( int rows, int cols )
 	{
 		this.tiles	= new Tile[rows][];
 		
 		for( int i = 0; i < rows; i++ )
 		{
 			this.tiles[i]	= new Tile[cols];
 			
 			for( int j = 0; j < cols; j++ ) { this.tiles[i][j]	= null; }
 		}
 		
 		this.rows	= rows;
 		this.cols	= cols;
 	}
 	
 	public void addRow( ITileState tileState )
 	{
 		int oldRows	= this.rows;
 		
 		++this.rows;
 		
 		Tile tmpTiles[][]	= new Tile[this.rows][];
 
 		for( int row = 0; row < this.rows; row++ )
 		{
 			tmpTiles[row]	= new Tile[this.cols];
 			
 			Tile tmpTile	= null;
 			
 			for( int col = 0; col < this.cols; col++ )
 			{
 				if( row < oldRows )
 				{
 					tmpTile				= this.tiles[row][col];
 				} else
 				{
 					tmpTile				= new Tile( null, row, col );
 					tmpTile.setTileState( tileState );
 					tmpTiles[row][col]	= tmpTile;
 				}
 				
 				tmpTiles[row][col] = tmpTile;
 			}
 		}
 		
 		this.tiles	= tmpTiles;
 	}
 	
 	public void delRow()
 	{
 		--this.rows;
 
 		Tile[][] tmpTiles	= new Tile[this.rows][];
 		
 		for( int row = 0; row < this.rows; row++ )
 		{
 			tmpTiles[row]	= new Tile[this.cols];
 			
 			for( int col = 0; col < this.cols; col++ )
 			{
 				tmpTiles[row][col]	= this.tiles[row][col];
 			}
 		}
 		
 		this.tiles	= tmpTiles;
 	}
 	
 	public void addCol( ITileState tileState )
 	{
 		int oldCols	= this.cols;
 		
 		++this.cols;
 		
 		Tile[][] tmpTiles	= new Tile[this.rows][this.cols];
 
 		for( int row = 0; row < this.rows; row++ )
 		{
 			tmpTiles[row]	= new Tile[this.cols];
 			Tile tmpTile	= null;
 			
 			for( int col = 0; col < this.cols; col++ )
 			{
 				if( col < oldCols )
 				{
 					tmpTile	= this.tiles[row][col];
 				} else
 				{
 					tmpTile	= new Tile( null, row, col );
 					tmpTile.setTileState( tileState );
 				}
 				
 				tmpTiles[row][col]	= tmpTile;
 			}
 		}
 		
 		this.tiles	= tmpTiles;
 	}
 	
 	public void delCol()
 	{
 		--this.cols;
 		
 		Tile[][] tmpTiles	= new Tile[this.rows][this.cols];
 		
 		for( int row = 0; row < this.rows; row++ )
 		{
 			tmpTiles[row]	= new Tile[this.cols];
 			
 			for( int col = 0; col < this.cols; col++ )
 			{
 				tmpTiles[row][col]	= this.tiles[row][col];
 			}
 		}
 		
 		this.tiles	= tmpTiles;
 	}
 
 	public int rows() 						{ return this.tiles.length; 		}
 	public int cols()						{ return this.tiles[0].length;		}
 	
 	public Tile tile( int row, int col )	
 	{ 
 		return this.tiles[row][col];		
 	}
 	
 	public TileArray setTile( int row, int col )
 	{
 		this.setRow	= row;
 		this.setCol	= col;
 		
 		return this;
 	}
 
 	public void withState( ITileState tileState, Tile beamsource ) 
 	{
 		this.withState( tileState );
 		this.tiles[this.setRow][this.setCol].parent( beamsource );
 	}
 	
 	public void withTile( Tile tile )
 	{
 		this.tiles[this.setRow][this.setCol]	= tile;
 		
 	}
 	
 	public void withState( ITileState tileState ) 
 	{
 		Tile tile	= new Tile( null, this.setRow, this.setCol );
 		tile.setTileState( tileState );
 				
 		this.tiles[this.setRow][this.setCol]	= tile;
 	}
 
 	public TileArray filter( String type, int row, int col )
 	{
 		return this.filter( type, this.tiles[row][col] );
 	}
 	
 	public TileArray filter( String type, Tile tileAtPostion )
 	{
 		ArrayList<Tile>	gTiles 	= new ArrayList<Tile>();
 		
 		int row	= tileAtPostion.row();
 		int col	= tileAtPostion.col();
 		
 		for( int cntRow = 0; cntRow < this.rows; cntRow++ )
 		{
 			Tile tile 		= tiles[cntRow][col];
 			String gType	= tile.type();
 			
 			if( gType.equals( type ) ) { gTiles.add( tile ); }
 		}
 		
 		for( int cntCol = 0; cntCol < this.cols; cntCol++ )
 		{
 			Tile tile 		= tiles[row][cntCol];
 			String gType	= tile.type();
 			
 			if( gType.equals( type ) ) { gTiles.add( tile ); }
 		}
 		
 		this.filteredTiles	= gTiles;
 		
 		return this;
 	}
 	
 	public ArrayList<Tile> find( int axis, ArrayList<Tile> tiles )
 	{
 		ArrayList<Tile> foundTiles	= new ArrayList<Tile>();
 		int cntSize					= tiles.size();
  
 		for( int cntTile = 0; cntTile < cntSize; cntTile ++ )
 		{
 			Tile tile	= tiles.get( cntTile );
 			
 			if( tile.direction() == axis ) 
 			{
 				foundTiles.add( tile );
 			}
 		}
 		
 		return foundTiles;
 	}
 	
 	public ArrayList<Tile> removeFrom( int posSource, int fromDirection, ArrayList<Tile> inTiles )
 	{
 		ArrayList<Tile> remain	= new ArrayList<Tile>();
 		int lenTiles			= inTiles.size();
 		
 		for( int cntTile = 0; cntTile < lenTiles; cntTile++ )
 		{
 			Tile tile	= inTiles.get( cntTile );
 			
 			if( fromDirection == Tile.LEFT || fromDirection == Tile.RIGHT )
 			{
 				if( fromDirection == Tile.LEFT && tile.col() < posSource )
 				{
 					remain.add( tile );
 				} else if( fromDirection == Tile.RIGHT && tile.col() > posSource )
 				{
 					remain.add( tile );
 				}
 			} else if( fromDirection == Tile.TOP || fromDirection == Tile.BOTTOM )
 			{
 				if( fromDirection == Tile.TOP && tile.row() < posSource )
 				{
 					remain.add( tile );
 				} else if( fromDirection == Tile.BOTTOM && tile.row() > posSource )
 				{
 					remain.add( tile );
 				}
 			}
 		}
 		
 		return remain;
 	}
 	
 	public ArrayList<Tile> depends( Tile source )
 	{
 		ArrayList<Tile> foundTiles	= new ArrayList<Tile>();
 		
 		int fLen	= this.filteredTiles.size();
 		
 		for( int cntFiltered = 0; cntFiltered < fLen; cntFiltered++ )
 		{
 			Tile child	= this.filteredTiles.get( cntFiltered );
 			Tile parent	= child.parent();
 			
 			if( parent != null && parent.row() == source.row() && parent.col() == source.col() && child.hidden() == false )
 			{
 				foundTiles.add( child );
 			}
 		}
 		
 		return foundTiles;
 	}
 	
 	public int mode() { return this.mode; }
 	
 	public void mode( int mode )
 	{
 		if( mode == TileArray.MODE_PREVIEW && this.mode != TileArray.MODE_PREVIEW )
 		{
 			this.mode		= mode;
 			this.conttiles 	= new Tile[this.rows][this.cols];
 			
 			for( int cntRow = 0; cntRow < this.rows; cntRow++ )
 			{
 				this.conttiles[cntRow]	= new Tile[this.cols];
 				
 				for( int cntCol = 0; cntCol < this.cols; cntCol++ )
 				{
 					this.conttiles[cntRow][cntCol]	= (Tile)this.tiles[cntRow][cntCol].clone();
 				}
 			}
 		} else if( mode == TileArray.MODE_READY && this.mode != TileArray.MODE_READY )
 		{
 			this.mode		= mode;
 			
 			this.tiles		= this.conttiles.clone();
 			this.conttiles	= null;
 		}
 	}
 	
 	public void pushReady( Tile tile, int row, int col )
 	{
 		this.conttiles[row][col]	= tile;
 	}
 	
 	public ArrayList<Tile> diff()
 	{
 		if( this.mode == TileArray.MODE_PREVIEW )
 		{
 			ArrayList<Tile> diffs	= new ArrayList<Tile>();
 			
 			for( int row = 0; row < this.rows; row++ )
 			{
 				for( int col = 0; col < this.cols; col++ )
 				{
 					Tile ready		= this.conttiles[row][col];
 					Tile preview	= this.tiles[row][col];
 					
 					if( !ready.equals( preview ) )
 					{
 						diffs.add( (Tile)preview.clone() );
 					}
 				}
 			}
 			
 			return diffs;
 		} else
 		{
 			return null;
 		}
 	}
 	
 	public ArrayList<Tile> getTilesInArea( String type )
 	{
 		ArrayList<Tile> sources	= new ArrayList<Tile>();
 		
 		for( int row = 0; row < this.rows; row++ )
 		{
 			for( int col = 0; col < this.cols; col++ )
 			{
 				Tile tile	= this.tiles[row][col];
 
 				if( tile.type().equals( type ) )
 				{
 					sources.add( tile );
 				}
 			}
 		}
 		
 		return sources;
 	}
 	
 	public Tile getLeftBlocking( Tile tile, String type )
 	{
 		int tRow	= tile.row();
 		
 		for( int col = tile.col() - 1; col > -1; col-- )
 		{
 			Tile lTile	= this.tile( tRow, col );
 			
 			if( lTile.type().equals( type ) ) { return lTile; }
 		}
 		
 		return null;
 	}
 	
 	public Tile getTopBlocking( Tile tile, String type )
 	{
 		int tCol	= tile.col();
 		
 		for( int row = tile.row() - 1; row > -1; row-- )
 		{
 			Tile lTile	= this.tile( row, tCol );
 			
 			if( lTile.type().equals( type ) ) { return lTile; }
 		}
 		
 		return null;
 	}
 	
 	public Tile getRightBlocking( Tile tile, String type )
 	{
 		int tRow	= tile.row();

		for( int col = tile.col() + 1; col < this.cols; col++ )
 		{
 			Tile lTile	= this.tile( tRow, col );
 			
 			if( lTile.type().equals( type ) ) { return lTile; }
 		}
 		
 		return null;
 	}
 	
 	public Tile getBottomBlocking( Tile tile, String type )
 	{
 		int tCol	= tile.col();
 		
 		for( int row = tile.row() + 1; row < this.rows; row++ )
 		{
 			Tile lTile	= this.tile( row, tCol );
 			
 			if( lTile.type().equals( type ) ) { return lTile; }
 		}
 		
 		return null;
 	}
 	
 	public TileArray createClone()
 	{
 		int oRows		= this.rows;
 		int oCols		= this.cols;
 		
 		TileArray clone	= new TileArray( oRows, oCols );
 		Tile[][] cTiles	= new Tile[oRows][oCols];
 		
 		for( int row = 0; row < oRows; row++ )
 		{
 			cTiles[row]	= new Tile[oCols];
 			
 			for( int col = 0; col < oCols; col++ )
 			{
 				clone.setTile( row, col ).withTile( (Tile)this.tiles[row][col].clone() );
 			}
 		}
 		
 		return clone;
 	}
 }
 
