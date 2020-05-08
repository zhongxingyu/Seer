 package curveGeneration;
 
 import java.awt.Color;
 import java.text.DecimalFormat;
 import java.text.NumberFormat;
 import java.util.ArrayList;
 
 import lineGeneration.LineGenerator;
 import model.Excel;
 
 public class CurveGenerator {
 	
 	public static ArrayList<Excel> ErmitForm(Excel ex1, Excel ex2, Excel ex3, Excel ex4)
 	{
 		System.out.println("ermitForm");
 		ArrayList<Excel> result = new ArrayList<Excel>();
 		ArrayList<Excel> Result = new ArrayList<Excel>();	
 		NumberFormat formatter = new DecimalFormat("0.0##########");
 		
 		if(ex1==null || ex2==null || ex3==null || ex4==null) return null;
 		
 			int Px1 = ex4.getX();
 			int Py1 = ex4.getY();
 			int Px4 = ex3.getX();
 			int Py4 = ex3.getY();
 			
 			int Rx1 = ex2.getX();
 			int Ry1 = ex2.getY();
 			int Rx4 = ex1.getX();
 			int Ry4 = ex1.getY();
 			
			ex1.setColor(Color.red);
			ex2.setColor(Color.red);
			
 			Matrix Gnx = new Matrix(4,2);
 			double masGnx[] = {Px1,Py1,Px4,Py4,Rx1,Ry1,Rx4,Ry4};
 			Gnx.fillingMatrix(masGnx);
 			Gnx.showMatrix();
 			
 			Matrix Mn = new Matrix(4,4);
 			double masMn[] = {2,-2,1,1,
 							 -3,3,-2,-1,
 							  0,0,1,0,
 							  1,0,0,0};
 			Mn.fillingMatrix(masMn);
 			
 			Matrix Cx;
 			if(matrixMultiplication(Mn,Gnx) != null)
 			{
 				Cx = matrixMultiplication(Mn,Gnx);
 				
 				for(double i=0; i<=1; i+=0.1)
 				{
 					Matrix T = new Matrix(1,4);
 					double masT[] = {Double.parseDouble(formatter.format(Math.pow(i,3)).replace(',', '.')),
 							Double.parseDouble(formatter.format(Math.pow(i,2)).replace(',', '.')),
 							Double.parseDouble(formatter.format(Math.pow(i,1)).replace(',', '.')),
 							1};
 					T.fillingMatrix(masT);
 					
 					if(matrixMultiplication(T,Cx) != null)
 					{
 						Matrix Pt = new Matrix(0,0);
 						Pt = matrixMultiplication(T,Cx);
 						
 						if(Pt.getColumns() == 1 && Pt.getRows() == 2)
 						{
 							result.add(new Excel((int)Pt.getEl(0, 0),(int)Pt.getEl(0, 1),Color.black));
 						}
 					}
 				}
 				int size = result.size()-1;
				Result.add(result.get(0));
 				for(int i=0;i<size;i++)
 				{
 					ArrayList<Excel> coloredEx = LineGenerator.Brezenhem(result.get(i),result.get(i+1));
 					for(int j=0; j<coloredEx.size(); j++)
 					{
 						Result.add(coloredEx.get(j));
 					}
 				}
 				
				Result.get(0).setColor(Color.red);
				Result.get(Result.size()-1).setColor(Color.red);
 			}
 		
 		return Result;
 	}
 
 	public static Matrix matrixMultiplication(Matrix A, Matrix B)
 	{
 		Matrix C = new Matrix(A.getColumns(),B.getRows());
 		
 		if(A.getRows() == B.getColumns())
 		{
 			double rezultValue = 0;
 	
 			for(int i=0; i<A.getColumns(); i++)
 			{
 				for(int j=0; j<B.getRows(); j++)
 				{
 					for(int p=0; p<A.getRows(); p++)
 					{
 						rezultValue+=A.getEl(i, p)*B.getEl(p, j);
 					}
 					C.updateEl(i, j, rezultValue);
 					rezultValue = 0;
 				}
 			}
 			return C;
 		}
 		else
 		{
			System.out.println("  1    " +
 					"   2 !!!");
 		}
 		
 		//C.showMatrix();
 		return null;
 	}
 }
