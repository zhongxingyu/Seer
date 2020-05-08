 package lightbeam.solution;
 
 import core.tilestate.Tile;
 import core.tilestate.TileArray;
 import lightbeam.solution.strategies.ConclusiveReachable;
 
 public class LogicClient 
 {
 	private boolean result	= false; 
 	private TileArray map	= null;
 	
 	public LogicClient() {}
 	
 	public void check( TileArray map )
 	{
 		this.map	= map.createClone();
		this.result	= false;
		
 		this.simulateMode( this.map );		
 		
 		LogicContext lContext	= new LogicContext( this.map );
 		
 		lContext.setLogic( new ConclusiveReachable() );
 		
 		while( lContext.getResult() == true )
 		{		
 			lContext.executeLogic();
 
 			if( lContext.getResult() == true )
 			{
 				this.map	= lContext.getMap();
 				this.result	= true;
 				
 				lContext.setMap( this.map );
 			} else
 			{
 				// ToDo: Nchste Strategie auswhlen!!
 			}
 		}
 	}
 	
 	public boolean getResult() 	{ return this.result; 	}
 	public TileArray getMap() 	{ return this.map; 		}
 	
 	private void simulateMode( TileArray map )
 	{
 		int rows	= map.rows();
 		int cols	= map.cols();
 		
 		for( int row = 0; row < rows; row++ )
 		{
 			for( int col = 0; col < cols; col++ )
 			{
 				Tile tile	= map.tile( row, col );
 				
 				if( tile.type().equals( "beam" ) ) { tile.type( "field" ); }
 			}
 		}
 	}
 }
