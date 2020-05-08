 import java.util.ArrayList;
 
 public class DSolve {
     private boolean failed;
     private int totalChanges = 0;
	 DSolve(Puzzle solving){
 	    int row = 0;
 	    int column = 0;
 	    int value =0;
 	    boolean done= false;
 	    int counter = 0;
 	   
 	    
 	    while(done != true && failed != true){
 	    	    
 	     while(column!=9){  
 	    	  while(row!=9){
 	    		if(solving.getValueAtPosition(column,row) != 0 && solving.getPosition(column,row).getListSize() !=0){
 	    			value = solving.getValueAtPosition(column,row);
 	    			setImpossible(solving,column,row,value);
 	    			
 	    		}
 	    		setValueSingleElement(solving);
 	    		setValueSpecialCases(solving);
 	    		row++;
 	    	  }  
 	    //	System.out.println("Counter " + counter);
 	    	row = 0;
 	    	column++;
 	    }
 	     row = 0;
 	     column = 0;
 	     counter++;
 	     if(counter == 81) failed = true;
 	//     System.out.println("Counter " + counter);
 	     done = filled(solving);
 	   }
 	    
 	    
 	   System.out.println("Amount of stuff removed " + totalChanges);	
 	}
 	
 	public boolean didNotSolve(){
 		return failed;
 	}
 	
 	private boolean filled(Puzzle solving){
 	    int column = 0;
 	    int row = 0;
 		boolean filled = true;
 	 
 	    	while(column!=9 && filled == true){  //Checks if we have put something in everysquare
 	    	  while(row!=9){
 	    		if(solving.getValueAtPosition(column,row) == 0){
 	    			filled = false;
 	    		}
 	    		row++;
 	    	  }
 	    	row = 0;
 	    	column++;
 	       }
 //	    System.out.println("We are done? " + filled);
         return filled;
 	}
 	
 	
 	
 	private void setImpossible(Puzzle a,int column, int row, int value){
 	    int rowTmp = 0;
 	    int colTmp = 0;
 //	    System.out.println("Working");
 	    while(colTmp!=9){
 	    	if(a.getPosition(colTmp,row).checkContains(value)){
 	    		a.getPosition(colTmp,row).removeVal(value);
 	    	}
 	    	colTmp++;
 	    }
 	    
 	    while(rowTmp!=9){
 	    	if(a.getPosition(column, rowTmp).checkContains(value)){
 	    		a.getPosition(column,rowTmp).removeVal(value);
 	    	}
 	    	rowTmp++;
 	    }
 	    
 	    //Square 1
 	    rowTmp = 0;
 	    colTmp = 0;
 	    if(row < 3 && column < 3){
 	    	while(rowTmp != 3){
 	    		while(colTmp!=3){
 	    			if(a.getPosition(colTmp, rowTmp).checkContains(value)){
 	    				a.getPosition(colTmp,rowTmp).removeVal(value);
 	    		    }
 	    			colTmp++;
 	    		}
 	    		   colTmp = 0;
 	    		   rowTmp++;
 	    	}
 	    }
 	    
 	    //Square 2
 	    rowTmp = 0;
 	    colTmp = 3;
 	    if(row < 3 && column < 6 && column > 2){
 	    	while(rowTmp != 3){
 	    		while(colTmp!=6){
 	    			if(a.getPosition(colTmp, rowTmp).checkContains(value)){
 	    				a.getPosition(colTmp,rowTmp).removeVal(value);
 	    		    }
 	    			colTmp++;
 	    		}
 	    		   colTmp = 3;
 	    		   rowTmp++;
 	    	}
 	    }
 	    
 	    //Square 3
 	    rowTmp = 0;
 	    colTmp = 6;
 	    if(row < 3 && column < 9 && column > 5){
 	    	while(rowTmp != 3){
 	    		while(colTmp!=9){
 	    			if(a.getPosition(colTmp, rowTmp).checkContains(value)){
 	    				a.getPosition(colTmp,rowTmp).removeVal(value);
 	    		    }
 	    			colTmp++;
 	    		}
 	    		   colTmp = 6;
 	    		   rowTmp++;
 	    	}
 	    }
 	    
 	    //Square 4
 	    rowTmp = 3;
 	    colTmp = 0;
 	    if(row < 6 && column < 3 && row > 2){
 	    	while(rowTmp != 6){
 	    		while(colTmp!=3){
 	    			if(a.getPosition(colTmp, rowTmp).checkContains(value)){
 	    				a.getPosition(colTmp,rowTmp).removeVal(value);
 	    		    }
 	    			colTmp++;
 	    		}
 	    		   colTmp = 0;
 	    		   rowTmp++;
 	    	}
 	    }
 	    
 	    //Square 5
 	    rowTmp = 3;
 	    colTmp = 3;
 	    if(row < 6 && column < 6 && row > 2 && column > 2){
 	    	while(rowTmp != 6){
 	    		while(colTmp!=6){
 	    			if(a.getPosition(colTmp, rowTmp).checkContains(value)){
 	    				a.getPosition(colTmp,rowTmp).removeVal(value);
 	    		    }
 	    			colTmp++;
 	    		}
 	    		   colTmp = 3;
 	    		   rowTmp++;
 	    	}
 	    }
 	    
 	    //Square 6
 	    rowTmp = 3;
 	    colTmp = 6;
 	    if(row < 6 && column < 9 && row > 2 && column > 5){
 	    	while(rowTmp != 6){
 	    		while(colTmp!=9){
 	    			if(a.getPosition(colTmp, rowTmp).checkContains(value)){
 	    				a.getPosition(colTmp,rowTmp).removeVal(value);
 	    		    }
 	    			colTmp++;
 	    		}
 	    		   colTmp = 6;
 	    		   rowTmp++;
 	    	}
 	    }
 	    
 	    //Square 7
 	    rowTmp = 6;
 	    colTmp = 0;
 	    if(row < 9 && column < 3 && row > 5){
 	    	while(rowTmp != 9){
 	    		while(colTmp!=3){
 	    			if(a.getPosition(colTmp, rowTmp).checkContains(value)){
 	    				a.getPosition(colTmp,rowTmp).removeVal(value);
 	    		    }
 	    			colTmp++;
 	    		}
 	    		   colTmp = 0;
 	    		   rowTmp++;
 	    	}
 	    }
 	    
 	    //Square 8
 	    rowTmp = 6;
 	    colTmp = 3;
 	    if(row < 9 && column < 6 && column > 2 && row > 5){
 	    	while(rowTmp != 9){
 	    		while(colTmp!=6){
 	    			if(a.getPosition(colTmp, rowTmp).checkContains(value)){
 	    				a.getPosition(colTmp,rowTmp).removeVal(value);
 	    		    }
 	    			colTmp++;
 	    		}
 	    		   colTmp = 3;
 	    		   rowTmp++;
 	    	}
 	    }
 	    
 	    //Square 9
 	    rowTmp = 6;
 	    colTmp = 6;
 	    if(row < 9 && column < 9 && row > 5 && column > 5){
 	    	while(rowTmp != 9){
 	    		while(colTmp!=9){
 	    			if(a.getPosition(colTmp, rowTmp).checkContains(value)){
 	    				a.getPosition(colTmp,rowTmp).removeVal(value);
 	    		    }
 	    			colTmp++;
 	    		}
 	    		   colTmp = 6;
 	    		   rowTmp++;
 	    	}
 	    }
 	}
 	
 	private void setValueSingleElement(Puzzle solving){
 		int value = 0;
 		int row = 0;
 		int column = 0;
 	//	System.out.println("Changing attempt");
 		while(row != 9){
 			while(column !=9){
 				if(solving.getPosition(column, row).getListSize() == 1 && solving.getValueAtPosition(column,row) == 0){
 			//		System.out.println("Changing");
 					value = solving.getPosition(column, row).getLastVal();
 					solving.changeValueAtPosition(column, row, value);
 					totalChanges++;
 			//		System.out.println("C " + column + " R " + row + " V " + solving.getValueAtPosition(column, row));
 					
 				}
 				column++;
 			}
 			column = 0;
 			row++;
 		}
 	}
 
 	
 	private void setValueSpecialCases(Puzzle solving){
 	//	ArrayList<Integer> specialCase = new ArrayList<Integer>();
 		int row = 0;
 		int column = 0;
 		int colTmp = 0;
 		int rowTmp = 0;
 		int value = 0;
 		int counter = 1;
 		boolean onlyValue = true;
 		boolean changed = false;
 		
 		while(row!=9){
 		  while(column!=9){
 			if(solving.getValueAtPosition(column,row) != 0){  
 				
 			  while(counter != 10 && changed != true){
 				  
 				  rowTmp = 0;
 				  colTmp = 0;
 				  if(solving.getPosition(column, row).checkContains(counter)){
 					  while(rowTmp != 9 && onlyValue == true && changed != true){
 						if(solving.getPosition(column,rowTmp).checkContains(counter)){
 							
 							onlyValue = false;
 						}
 						rowTmp++;
 					  }
 
 					  if(onlyValue == true){
 						  solving.changeValueAtPosition(column, row, counter);
 						  totalChanges++;
 						  changed = true;
 						  System.out.println("Col " + column + " R " + row + " V " + solving.getValueAtPosition(column, row));
 					  }
 				
 					  onlyValue = true;
 				   
 					  while(colTmp != 9 && onlyValue == true && changed != true){
 						  if(solving.getPosition(colTmp,row).checkContains(counter)){
 							  onlyValue = false;
 						  }
 						  colTmp++;
 					  }
 
 					  if(onlyValue == true  && changed != false){
 						  solving.changeValueAtPosition(column, row, counter);
 						  totalChanges++;
 						  changed = true;
 						  System.out.println("Col " + column + " R " + row + " V " + solving.getValueAtPosition(column, row));
 					  }
 					  
 					  onlyValue = true;
 				
 					  //Square 1
 					  rowTmp = 0;
 					  colTmp = 0;
 					  if(row < 3 && column < 3){
 						  while(rowTmp != 3){
 							  while(colTmp!=3){
 								  if(solving.getPosition(colTmp, rowTmp).checkContains(value)){
 									  onlyValue = false;
 								  }
 								  colTmp++;
 							  }
 							  colTmp = 0;
 							  rowTmp++;
 						  }
 					  }
 			    
 					  //Square 2
 					  rowTmp = 0;
 					  colTmp = 3;
 					  if(row < 3 && column < 6 && column > 2){
 						  while(rowTmp != 3){
 							  while(colTmp!=6){
 								  if(solving.getPosition(colTmp, rowTmp).checkContains(value)){
 									  onlyValue = false;
 								  }
 								  colTmp++;
 							  }
 							  colTmp = 3;
 							  rowTmp++;
 						  }
 					  }
 			    
 					  //Square 3
 					  rowTmp = 0;
 					  colTmp = 6;
 					  if(row < 3 && column < 9 && column > 5){
 						  while(rowTmp != 3){
 							  while(colTmp!=9){
 								  if(solving.getPosition(colTmp, rowTmp).checkContains(value)){
 									  onlyValue = false;
 								  }
 								  colTmp++;
 							  }
 							  colTmp = 6;
 							  rowTmp++;
 						  }
 					  }
 			    
 					  //Square 4
 					  rowTmp = 3;
 					  colTmp = 0;
 					  if(row < 6 && column < 3 && row > 2){
 						  while(rowTmp != 6){
 							  while(colTmp!=3){
 								  if(solving.getPosition(colTmp, rowTmp).checkContains(value)){
 									  onlyValue = false;
 								  }
 								  colTmp++;
 							  }
 							  colTmp = 0;
 							  rowTmp++;
 						  }
 					  }
 			    
 					  //Square 5
 					  rowTmp = 3;
 					  colTmp = 3;
 					  if(row < 6 && column < 6 && row > 2 && column > 2){
 						  while(rowTmp != 6){
 							  while(colTmp!=6){
 								  if(solving.getPosition(colTmp, rowTmp).checkContains(value)){
 									  onlyValue = false;
 								  }
 								  colTmp++;
 							  }
 							  colTmp = 3;
 							  rowTmp++;
 						  }
 					  }
 			    
 					  //Square 6
 					  rowTmp = 3;
 					  colTmp = 6;
 					  if(row < 6 && column < 9 && row > 2 && column > 5){
 						  while(rowTmp != 6){
 							  while(colTmp!=9){
 								  if(solving.getPosition(colTmp, rowTmp).checkContains(value)){
 									  onlyValue = false;
 								  }
 								  colTmp++;
 							  }
 							  colTmp = 6;
 							  rowTmp++;
 			    		}
 					  }
 			    
 					  //Square 7
 					  rowTmp = 6;
 					  colTmp = 0;
 					  if(row < 9 && column < 3 && row > 5){
 						  while(rowTmp != 9){
 							  while(colTmp!=3){
 			 				   if(solving.getPosition(colTmp, rowTmp).checkContains(value)){
 			 					   onlyValue = false;
 			 				   }
 			 				   colTmp++;
 							  }
 							  colTmp = 0;
 							  rowTmp++;
 						  }
 					  }
 			    
 					  //Square 8
 					  rowTmp = 6;
 					  colTmp = 3;
 					  if(row < 9 && column < 6 && column > 2 && row > 5){
 						  while(rowTmp != 9){
 							  while(colTmp!=6){
 								  if(solving.getPosition(colTmp, rowTmp).checkContains(value)){
 									  onlyValue = false;
 								  }
 								  colTmp++;
 							  }
 							  colTmp = 3;
 							  rowTmp++;
 						  }	
 					  }
 			    
 					  //Square 9
 					  rowTmp = 6;
 					  colTmp = 6;
 					  if(row < 9 && column < 9 && row > 5 && column > 5){
 						  while(rowTmp != 9){
 							  while(colTmp!=9){
 								  if(solving.getPosition(colTmp, rowTmp).checkContains(value)){
 									  onlyValue = false;
 								  }
 								  colTmp++;
 							  }
 							  colTmp = 6;
 							  rowTmp++;
 						  }
 					  }
 				
 					  if(onlyValue == true  && changed != false){
 						  solving.changeValueAtPosition(column, row, counter);
 						  totalChanges++;
 						  changed = true;
 						  System.out.println("Col " + column + " R " + row + " V " + solving.getValueAtPosition(column, row));
 					  }
 				     }	
 				  counter++;
 			  }
 			}	
 			column++;
 		    counter = 1;
 		    changed = false;
 		    onlyValue = true;
 		  }
		  column = 0;
 		  row++;
 		}
 	}
 }
 	
 
 
