 import java.util.Random;
 
 public class Sudoku
 {
     private final int[][] m_solved;
     private int[][] m_toSolve;
     private final boolean[][] m_editable;
 
     private State m_state;
     int m_assist;
     
     public final static int NOT_SET = 0;
     
    enum Completeness { Complete, Incomplete, Invalid };
    
     public Sudoku(int[][] toSolve, final int[][] solved, int assist)
     {
     	m_solved = new int[9][9];
     	m_toSolve = new int[9][9];
     	m_editable = new boolean[9][9];
     	m_assist = assist;
         m_state = new State(null, null, m_assist, null);
         
         for (int i = 0; i < 9; ++i)
         {
             System.arraycopy(toSolve[i], 0, m_toSolve[i], 0, 9);
             System.arraycopy(solved[i], 0, m_solved[i], 0, 9);
         }
 
         for (int i = 0; i != 9; ++i)
         {
             for (int j = 0; j != 9; ++j)
             {
                 m_editable[i][j] = (m_toSolve[i][j] == 0);
             }
         }
         
         m_assist = assist;
     }
     
     public Sudoku(int[][] board, final int[][] solution, final boolean[][] editable, int assist)
     {
         m_toSolve = board;
         m_solved = solution;
         m_editable = editable;
         m_assist = assist;
         m_state = new State(null, null, m_assist, null);
     }
 
 	public boolean setNode(int x, int y, int val)
     {
         if (m_editable[x][y] == true && val >= 0 && val < 10)
         {
             m_state = new State(m_state, null, m_assist, new Vector3D(x, y, val));
             m_toSolve[x][y] = val;
         }
 
         return (m_editable[x][y] && val >= 0 && val < 10);
     }
 
     public String getProblem()
     {
         return getBoard(false);
     }
     
     public String getSolution()
     {
         return getBoard(true);
     }
 
     public int getRemainingAssists()
     {
         return m_assist;
     }
     
     public void setAssists(int assists)
     {
         m_assist = assists;
     }
     
     public int addAssist()
     {
         return ++m_assist;
     }
     
     public int removeLife()
     {
         return --m_assist;
     }
     
     public Completeness validate()
     {
         Completeness completeness = Completeness.Complete;
         for (int i = 0; i != 9; ++i)
         {
             for (int j = 0; j != 9; ++j)
             {
                 if (m_toSolve[i][j] == NOT_SET)
                 {
                 	completeness = Completenes.Incomplete;
                 }
                 else if(m_toSolve[i][j] != m_solved[i][j] && m_toSolve != NOT_SET)
                 {
                     return Completeness.Invalid;
                 }
             }
         }
 
         return Completeness.Valid;
     }
     
     public Vector3D getAssist()
     {
         int x;
         int y;
         Random generator = new Random();
         
         if (m_assist == 0)
         {
             return null;
         }
         
         do
         {
             x = generator.nextInt(9);
             y = generator.nextInt(9);
         }
         while (m_toSolve[x][y] != NOT_SET);
         
         --m_assist;
         m_state.loseAssist();
         
         m_toSolve[x][y] = m_solved[x][y];
         return new Vector3D(x, y, m_solved[x][y]);
     }
     
     public boolean undo()
     {
         if (m_state.getPast() != null)
         {
             State temp = m_state;
             Vector3D v;
             
             m_state = m_state.getPast();
             
             v = m_state.getValue();
             m_assist = m_state.getAssist();
             m_toSolve[v.getX()][v.getY()] = v.getZ();
             m_state.setFuture(temp);
         }
         
         return (m_state.getPast() != null);
     }
     
     public boolean redo()
     {
         if (m_state.getFuture() != null)
         {
             Vector3D v;
             
             m_state = m_state.getFuture();
             
             v = m_state.getValue();
             m_assist = m_state.getAssist();
             m_toSolve[v.getX()][v.getY()] = v.getZ();
         }
         
         return (m_state.getFuture() != null);
     }    
     
     private String getBoard(boolean solved)
     {
         int[][] board = solved == true ? m_solved : m_toSolve;
         String str = new String();
 
         for (int i = 0; i != 9; ++i)
         {
             for (int j = 0; j != 9; ++j)
             {
                 str = str.concat(Integer.toString(board[i][j]) + " ");
             }
         }
         
         return str;
     }
     
     public String getEditable()
     {
     	String editable = new String();
     	
     	for (int i = 0; i != 9; ++i)
     	{
     		for (int j = 0; j != 9; ++j)
     		{
     			editable += Boolean.toString(m_editable[i][j]);
     			editable += " ";
     		}
     	}
     	
     	return editable;
     }
     
     /*public boolean isComplete()
     {
         for (int i = 0; i != 9; ++i)
         {
             for (int j = 0; j != 9; ++j)
             {
                 if (m_toSolve[i][j] != m_solved[i][j])
                 {
                    return false;
                 }
             }
         }
           
         return true;
     }*/
 }
