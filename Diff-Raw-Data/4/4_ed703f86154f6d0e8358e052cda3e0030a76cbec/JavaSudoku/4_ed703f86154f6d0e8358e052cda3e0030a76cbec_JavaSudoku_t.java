 # Program Name:	 		JavaSudoku
# Author: 		 	 Mansoor Nathani
# Email:			 mnathani@gmail.com
 # Date Authored:		2011-11-03
 import java.util.Scanner;
 
 public class JavaSudoku
 {
     public static void main (String[] args)
     {
         Scanner scan = new Scanner(System.in);
         // Array to hold initial soduku and to be updated when confirmation of value in reached.
 		int [][] x= new int [10][10];
 		
 		// Variables used for cleanup
 		int count;
 		int value;
 		int changes;
 		int loop;
 		
 		// Variable used for unique box count
 		int ucount;
 		
 		// Variables used for row and col count
 		int rcount;
 		int ccount;
 		
 		// Variable used for dual row col box count
 		int boxcount;
 		int x1=0,y1=0,x2=0,y2=0;
 		
 		for(int i=1;i<10;i+=1)
 		{
 			for(int j=1;j<10;j+=1)
 			{
 				x[i][j] = scan.nextInt();
 			}
 		}
 		
 		// Print initial soduku table
 		prso(x);
 		// Array to store possibilities for each position
 		int [][][] pos = new int [10][10][10];
 		
 		//Initialize possibility array:
 		for (int a=1;a<10;a++)
 		{
 			for (int b=1;b<10;b++)
 			{
 				pos[a][b][0]=0;
 				for (int c=1;c<10;c++)
 				{
 					pos[a][b][c]= 1;
 				}
 			}
 		}
 		
 		for(int i=1;i<10;i+=1)
 		{
 			for(int j=1;j<10;j+=1)
 			{
 				if(	x[i][j] != 0)
 				{
 					pos[i][j][0]=x[i][j];
 					for(int k =1;k<10;k+=1)
 					{
 						if(x[i][j]!=k)
 						{
 						pos[i][j][k]=0;
 						}
 					}
 				}
 			}
 		}
 		
 		System.out.println("Possibilities after first round:");
 		prpos(pos);
 		
 		loop=0;
 		do
 		{
 			loop+=1;
 			changes=0;
 			// start row & col
 			for(int i=1;i<10;i+=1)
 			{
 				for(int j=1;j<10;j+=1)
 				{
 					if( pos[i][j][0] != 0)
 					{
 						//row
 						for(int k =1;k<10;k+=1)
 						{
 							if(k!=j)
 							{
 								if(pos[i][k][pos[i][j][0]]!=0)
 								{
 								pos[i][k][pos[i][j][0]]=0;
 								changes+=1;
 								}
 							}
 						}
 						//col
 						for(int k =1;k<10;k+=1)
 						{
 							if (k!=i)
 							{
 								if(pos[k][j][pos[i][j][0]]!=0)
 								{
 									pos[k][j][pos[i][j][0]]=0;
 									changes+=1;
 								}
 							}
 							
 						}
 
 					}
 				}
 			}
 			
 			// end row & col
 			
 			
 			
 			// start row & col cleanup 
 			
 			System.out.println("After row & col / Before row & col cleanup: loop=" + loop + " changes = "+ changes);
 			prpos(pos);
 			
 			for(int i=1;i<10;i+=1)
 			{
 				for(int j=1;j<10;j+=1)
 				{
 					count=0;
 					value=0;
 					if (pos[i][j][0]==0)
 					{
 						for (int l=1;l<10;l+=1)
 						{
 							if(pos[i][j][l]==1)
 							{
 							count+=1;
 							value=l;
 							}
 						}
 						if(count==1)
 						{
 							pos[i][j][0]=value;
 							changes+=1;
 						}
 					}
 				
 				}
 			}
 			
 			// end row & col cleanup
 			
 			System.out.println("After row & col cleanup / before Box: loop=" + loop + " changes = "+ changes);
 			prpos(pos);
 			
 			
 			
 			// Box
 			for(int i=1;i<10;i+=3)
 			{
 				for(int j=1;j<10;j+=3)
 				{
 					for(int k=0;k<3;k+=1)
 					{
 						for(int l=0;l<3;l+=1)
 						{
 							if( pos[i+k][j+l][0] != 0)
 							{
 								for (int m=0;m<3;m+=1)
 								{
 									for (int n=0;n<3;n+=1)
 									{
 										if((n!=l)&&(m!=k))
 										{
 											if(pos[i+m][j+n][pos[i+k][j+l][0]]!=0)
 											{
 											pos[i+m][j+n][pos[i+k][j+l][0]]=0;
 											changes+=1;
 											}
 										}
 									}
 								}
 							}
 						}
 					}
 				}
 			} // End Box
 			
 			System.out.println("After box / Before box cleanup: Loop# =" + loop + " changes = "+ changes);
 			prpos(pos);
 			
 			
 			// start Box cleanup		
 			for(int i=1;i<10;i+=1)
 			{
 				for(int j=1;j<10;j+=1)
 				{
 					count=0;
 					value=0;
 					if (pos[i][j][0]==0)
 					{
 						for (int l=1;l<10;l+=1)
 						{
 							if(pos[i][j][l]==1)
 							{
 							count+=1;
 							value=l;
 							}
 						}
 						if(count==1)
 						{
 							pos[i][j][0]=value;
 							changes+=1;
 						}
 					}
 				
 				}
 			}
 			
 			// end box cleanup
 			
 			System.out.println("After box cleanup: Loop# =" + loop + " changes = "+ changes);
 			prpos(pos);
 			
 			
 			// Check box unique
 			for(int i=1;i<10;i+=1)
 			{
 				for(int j=1;j<10;j+=1)
 				{
 					if (pos[i][j][0]==0)
 					{	
 						for(int k=1;k<10;k+=1)
 						{
 						ucount=0;
 							if (pos[i][j][k]!=0)
 							{
 								for(int c=1;c<4;c+=1)
 								{
 									for(int d=1;d<4;d+=1)
 									{
 										//System.out.println("i/c:"+ (((i-1)/3)*3+c) + " j/d:" + (((j-1)/3)*3+d));
 										if(pos[((i-1)/3)*3+c][((j-1)/3)*3+d][k]==1)
 										{
 											ucount+=1;
 										}
 																	
 										
 										
 									}
 								}	
 								/*Second Attempt
 								for(int a =1;a<4;a+=1)
 								{
 									for(int b =1;b<4;b+=1)
 									{
 									System.out.println("i:"+ i +" j:" + j +" k " +k + " a:" + a + " b:" + b + " i/a:"+ ((i-1)/3+a) + " j/b:"+((j-1)/3+b) ); 
 										if(pos[(i-1)/3+a][(j-1)/3+b][k]==1)
 										{
 											ucount+=1;
 										}
 									}
 								}
 								*/
 								
 								/* First Attempt
 								for (int l=(i/3)+1;l<(i/3+4);l+=1)
 								{
 									for (int m=((j/3)+1);m<(j/3+4);m+=1)
 									{
 									//System.out.println("ucount: " + ucount +" i: " + i+" j: " + j+" k: " + k + " l: " + l + " m: " + m);
 										if(pos[l][m][k]==1)
 										{
 											ucount+=1;
 											//System.out.println("ucount: " + ucount +" i: " + i+" j: " + j+" k: " + k + " l: " + l + " m: " + m);
 										}
 									}
 								}
 								*/
 							}
 							if (ucount ==1)
 							{
 							//System.out.println("ucount: " + ucount +" i: " + i+" j: " + j+" k: " + k);
 								for(int n=1; n<10; n+=1)
 								{
 									if(n!=k)
 									{
 										
 									//System.out.println("ucount: " + ucount +" i: " + i+" j: " + j+" k: " + k + " n: " + n);
 										
 									if (pos[i][j][n]!=0)
 											{
 											//System.out.println(" i: " + i+" j: " + j+" k: "+k +" n: " + n);
 												pos[i][j][n]=0;
 												changes+=1;
 											}
 										}
 										
 									}
 								}
 							
 						}
 					}
 				}
 			} // End Check row unique
 			
 			System.out.println("After check box unique / Before check box cleanup: Loop# =" + loop + " changes = "+ changes);
 			prpos(pos);
 			
 			
 			// start check box unique cleanup		
 			for(int i=1;i<10;i+=1)
 			{
 				for(int j=1;j<10;j+=1)
 				{
 					count=0;
 					value=0;
 					if (pos[i][j][0]==0)
 					{
 						for (int l=1;l<10;l+=1)
 						{
 							if(pos[i][j][l]==1)
 							{
 							count+=1;
 							value=l;
 							}
 						}
 						if(count==1)
 						{
 							pos[i][j][0]=value;
 							changes+=1;
 						}
 					}
 				
 				}
 			}
 			
 			// end check box cleanup
 			
 			System.out.println("After check box cleanup: Loop# =" + loop + " changes = "+ changes);
 			prpos(pos);
 			
 
 						
 			// Check col unique
 			for(int i=1;i<10;i+=1)
 			{
 				for(int j=1;j<10;j+=1)
 				{
 					if (pos[i][j][0]==0)
 					{
 						for (int k=0;k<10;k+=1)
 						{
 							if (pos[i][j][k]==1) 
 							{
 								ccount=0;
 								for (int l=1;l<10;l++)
 								{
 									if (pos[l][j][k]==1)
 									{
 										ccount+=1;
 									}
 								}
 								if (ccount==1)
 								{
 									for(int m=0;m<10;m++)
 									{
 										if (m!=k)
 										{
 										pos[i][j][m]=0;
 										changes+=1;
 										}
 									}
 								}
 							}
 						}
 					}
 				}	
 			} // End Check col  unique
 			
 			System.out.println("After check col unique / Before check col unique cleanup: Loop# =" + loop + " changes = "+ changes);
 			prpos(pos);
 			
 			
 			// start check col unique cleanup		
 			for(int i=1;i<10;i+=1)
 			{
 				for(int j=1;j<10;j+=1)
 				{
 					count=0;
 					value=0;
 					if (pos[i][j][0]==0)
 					{
 						for (int l=1;l<10;l+=1)
 						{
 							if(pos[i][j][l]==1)
 							{
 							count+=1;
 							value=l;
 							}
 						}
 						if(count==1)
 						{
 							pos[i][j][0]=value;
 							changes+=1;
 						}
 					}
 				
 				}
 			}
 			
 			// end check col unique cleanup
 			
 			System.out.println("After check col unique cleanup: Loop# =" + loop + " changes = "+ changes);
 			prpos(pos);
 			
 			
 			
 
 			
 			// start check row unique
 			for(int i=1;i<10;i+=1)
 			{
 				for(int j=1;j<10;j+=1)
 				{
 					if (pos[i][j][0]==0)
 					{
 						for (int k=0;k<10;k+=1)
 						{
 							if (pos[i][j][k]==1) 
 							{
 								rcount=0;
 								for (int l=1;l<10;l++)
 								{
 									if (pos[i][l][k]==1)
 									{
 										rcount+=1;
 									}
 								}
 								if (rcount==1)
 								{
 									for(int m=0;m<10;m++)
 									{
 										if (m!=k)
 										{
 										pos[i][j][m]=0;
 										changes+=1;
 										}
 									}
 								}
 							}
 						}
 					}
 				}
 			} // End Check Row unique
 			
 			System.out.println("After check row unique / Before check row cleanup: Loop# =" + loop + " changes = "+ changes);
 			prpos(pos);
 			
 			
 			// start check Row cleanup		
 			for(int i=1;i<10;i+=1)
 			{
 				for(int j=1;j<10;j+=1)
 				{
 					count=0;
 					value=0;
 					if (pos[i][j][0]==0)
 					{
 						for (int l=1;l<10;l+=1)
 						{
 							if(pos[i][j][l]==1)
 							{
 							count+=1;
 							value=l;
 							}
 						}
 						if(count==1)
 						{
 							pos[i][j][0]=value;
 							changes+=1;
 						}
 					}
 				
 				}
 			}
 			
 			// end check Row cleanup
 			
 			System.out.println("After check row cleanup: Loop# =" + loop + " changes = "+ changes);
 			prpos(pos);
 			
 			
 			
 // Start check dual row col in a box
 
 			for(int i=1;i<10;i+=1)
 			{
 				for(int j=1;j<10;j+=1)
 				{
 					if (pos[i][j][0]==0)
 					{
 						for (int k=0;k<10;k+=1)
 						{
 							if (pos[i][j][k]==1) 
 							{
 								boxcount=0;
 								for(int l=1;l<4;l+=1)
 								{
 									for(int m=1;m<4;m+=1)
 									{
 										if((pos[((i-1)/3)*3+l][((j-1)/3)*3+m][0]!=0)&&(pos[((i-1)/3)*3+l][((j-1)/3)*3+m][k]==1))
 										{
 											boxcount+=1;
 											if(boxcount==1)
 											{
 												x1=((i-1)/3)*3+l;
 												y1=((j-1)/3)*3+m;
 												System.out.println("x1:"+x1+" y1: "+y1 +  "x2:"+x2 +" y2: "+y2); 
 											}
 											if(boxcount==2)
 											{
 												x2=((i-1)/3)*3+l;
 												y2=((j-1)/3)*3+m;
 												System.out.println("x1:"+x1 +" y1: "+y1 +  "x2:"+x2 +" y2: "+y2);
 											
 											}
 										}
 									}
 								}
 							//	System.out.println("number:"+k + "boxcount:"+boxcount+" x1:"+x1 +" y1: "+y1 +  " x2:"+x2 +" y2: "+y2);
 								if(boxcount==2)
 								{
 									if(x1==x2)
 									{	
 										System.out.println("P:"+pos[i][j][0] +"number:"+k + "boxcount:"+boxcount+" x1:"+x1 +" y1:"+y1 +  " x2:"+x2 +" y2:"+y2);
 										for(int n=1;n<10;n+=1)
 										{
 											if((n!=x1)&&(n!=x2))
 											{
 												pos[n][j][k]=0;
 											}
 										}
 									}
 									
 									if(y1==y2)
 									{
 										System.out.println("P:"+pos[i][j][0] +"number:"+k + "boxcount:"+boxcount+" x1:"+x1 +" y1:"+y1 +  " x2:"+x2 +" y2:"+y2);
 										for(int n=1;n<10;n+=1)
 										{
 											if((n!=y1)&&(n!=y2))
 											{
 												pos[i][n][k]=0;
 											}
 										}
 									}
 									
 								}
 							}
 						}
 					}
 				}
 			} // End check dual row col
 			
 			System.out.println("After check dual row col / Before cleanup: Loop# =" + loop + " changes = "+ changes);
 			prpos(pos);
 			
 			
 			// start check dual row col cleanup		
 			for(int i=1;i<10;i+=1)
 			{
 				for(int j=1;j<10;j+=1)
 				{
 					count=0;
 					value=0;
 					if (pos[i][j][0]==0)
 					{
 						for (int l=1;l<10;l+=1)
 						{
 							if(pos[i][j][l]==1)
 							{
 							count+=1;
 							value=l;
 							}
 						}
 						if(count==1)
 						{
 							pos[i][j][0]=value;
 							changes+=1;
 						}
 					}
 				
 				}
 			}
 			
 	
 			
 			System.out.println("After check dual row col cleanup: Loop# =" + loop + " changes = "+ changes);
 			prpos(pos);
 
 // End check dual row col
 			
 			
 			
 		} while (changes!=0);
 		
 		// Print initial soduku table
 		prso(x);		
 		// Print final soduku table
 		finpos(pos);
 		
 		
     } // End Main
 	
 	
 	public static void prso (int arr [] [] )
 	{
 		String output = "\nSoduku Display:\n\n";
 		for(int i=1;i<10;i+=1)
 		{
 			for(int j=1;j<10;j+=1)
 			{
 				output += " " + arr[i][j];
 				if (j % 3 == 0)
 				{
 					output += " ";
 				}				
 			}
 				if (i % 3 == 0)
 				{
 					output += "\n";
 				}
 			output += "\n";
 		}
 		System.out.println(output);
 
 	}
 	
 	public static void prpos (int arr [][][] )
 	{
 		String output = "\nPossibility Display:\n\n ";
 		for(int i=1;i<10;i+=1)
 		{
 			for(int j=1;j<10;j+=1)
 			{
 				for(int k=0;k<10;k+=1)
 				{
 				if(k==0){ output+="("+arr[i][j][k]+")";}
 				else{
 				
 				if ( arr[i][j][k]==1)
 				{
 				output += k;
 				}
 				else
 				{
 				output +=" ";
 				}
 				
 				
 				}
 				}
 				if (j%3==0)
 				{
 			output +="  ";
 			}
 			else{
 			output +=" ";
 			}
 			}
 			if (i%3==0){
 		output += "\n\n ";
 		}
 		else{
 		output += "\n ";
 	}
 		}
 		System.out.println(output);
 
 	}
 	
 	public static void finpos (int arr [][][] )
 	{
 		String output = "\nFinal Soduku Display:\n\n ";
 		for(int i=1;i<10;i+=1)
 		{
 			for(int j=1;j<10;j+=1)
 			{
 				output += arr[i][j][0];
 				output +=" ";
 				if (j%3==0)
 				{
 					output +=" ";
 				}
 			}
 			if (i%3==0)
 			{
 				output += "\n\n ";
 			}
 			else
 			{
 			output += "\n ";
 			}
 		}
 		
 		System.out.println(output);
 
 	}
 }
