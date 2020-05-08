 package swarm.client.managers;
 
 import java.util.ArrayList;
 
 import swarm.client.entities.BufferCell;
 import swarm.client.structs.BufferCellPool;
 
 abstract class A_BufferCellList
 {
 	protected final BufferCellPool m_cellPool;
 	protected final ArrayList<BufferCell> m_cellList = new ArrayList<BufferCell>();
 	
 	A_BufferCellList(BufferCellPool pool)
 	{
 		m_cellPool = pool;
 	}
 	
 	void drain()
 	{
 		for ( int i = 0; i < this.m_cellList.size(); i++ )
 		{
 			BufferCell ithCell = this.m_cellList.get(i);
 			
 			if( ithCell != null )
 			{
 				m_cellPool.deallocCell(ithCell);
 			}
 		}
 		
 		this.m_cellList.clear();
 	}
 	
 	protected static boolean swap(int m, int n, A_BufferCellList from, A_BufferCellList to)
 	{
 		BufferCell cell = from.removeCellAtAbsCoord(m, n);
 		
 		if( cell == null )  return false;
 		
 		to.m_cellList.add(cell);
 		
 		return true;
 	}
 	
 	public BufferCell getCellAtAbsoluteCoord(int m, int n)
 	{
 		for( int i = 0; i < m_cellList.size(); i++ )
 		{
 			BufferCell ithCell = m_cellList.get(i);
 			
			if( ithCell != null && ithCell.getCoordinate().isEqualTo(m, n) )
 			{
 				return ithCell;
 			}
 		}
 		
 		return null;
 	}
 	
 	BufferCell removeCellAtAbsCoord(int m, int n)
 	{
 		for( int i = 0; i < m_cellList.size(); i++ )
 		{
 			BufferCell ithCell = m_cellList.get(i);
 			
 			if( ithCell == null )  continue;
 			
 			if( ithCell.getCoordinate().isEqualTo(m, n) )
 			{
 				m_cellList.set(i, null);
 				
 				return ithCell;
 			}
 		}
 		
 		return null;
 	}
 }
