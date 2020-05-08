 package eqn;
 
 import map.*;
 import set.*;
 import java.util.Random;
 import eqn.CSPCell;
 
 public final class CSPStrategy implements Strategy
 {
 
 	public CSPCell[][] constraints;
 	public void play(Map m)
 	{
 
		//System.out.println("New game");
 		Random rand = new Random();
 		boolean first = true;
		constraints = new CSPCell[m.columns()][m.rows()];
 
 		//Initialize
		for(int i = 0; i < m.columns(); i++)
			for(int j = 0; j < m.rows(); j++)
 				constraints[i][j] = new CSPCell(i, j);
 
 		//Link all of the cell constraints
		for(int i = 0; i < m.columns(); i++)
			for(int j = 0; j < m.rows(); j++)
 			{
 				boolean L = false, R = false, D = false, U = false;
 				if(i > 0)//up
 					U = true;
 				if(j > 0)//left
 					L = true;
				if(i < m.columns() - 1)//down
 					D = true;
				if(j < m.rows() - 1)//right
 					R = true;
 
 				if(U)//up
 					constraints[i][j].neighbours.add(constraints[i - 1][j]);
 				if(L)//left
 					constraints[i][j].neighbours.add(constraints[i][j - 1]);
 				if(D)//down
 					constraints[i][j].neighbours.add(constraints[i + 1][j]);
 				if(R)//right
 					constraints[i][j].neighbours.add(constraints[i][j + 1]);
 				if(U && R)
 					constraints[i][j].neighbours.add(constraints[i - 1][j + 1]);
 				if(U && L)
 					constraints[i][j].neighbours.add(constraints[i - 1][j - 1]);
 				if(D && L)
 					constraints[i][j].neighbours.add(constraints[i + 1][j - 1]);
 				if(D && R)
 					constraints[i][j].neighbours.add(constraints[i + 1][j + 1]);
 			}
 
 		while(!m.done())
 		{
 			if(first)
 			{
 				first = false;
 				constraints[0][0].setValue(m.probe(0, 0));
 				continue;
 			}
 
 			int x = 0, y = 0;
 			//Start building constraints for this state
			for(int i = 0; i < m.columns(); i++)
				for(int j = 0; j < m.rows(); j++)
 				{
 					constraints[i][j].calculate(m);
 					//constraints[i][j].simplify();
 					int cellVal = m.look(i, j);//Look at a cell
 
 					//A cell that hasn't been probed but constraints show to be clear
 					if(cellVal == m.UNPROBED && constraints[i][j].cellType == CSPCell.type.CLEAR)
 					{
 						x = i;
 						y = j;
 						break;
 					}
 				}
 
 
 			//Use constraints to try to find a clear cell
 			CSPFindBestChoice(m);
 
 			//No known clear space. Pick a random cell
 			if( x == 0 && y == 0)
 			{
				x = rand.nextInt(m.columns());
				y = rand.nextInt(m.rows());
				//System.out.println("Guessing!");
 			}
 			//else
 				//System.out.println("Not Guessing!");
 
 			constraints[x][y].setValue(m.probe(x, y));
 			//System.out.println(x + " " + y);
 		}
 	}
 
 	public int CSPFindBestChoice(Map m)
 	{
 		return 0;
 	}
 }
