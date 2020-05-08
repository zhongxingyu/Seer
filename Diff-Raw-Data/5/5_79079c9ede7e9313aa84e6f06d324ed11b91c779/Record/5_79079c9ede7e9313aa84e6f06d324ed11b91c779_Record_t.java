 package util;
 import java.util.ArrayList;
 
 import compare.Comparer;
 
 
 public class Record {
 	
     // columnArray has a value of 1 if there is a condition to be checked for that respective column
 	// each index in the columnArray corresponds with the index of the schema array. So, 
 	// columnArray[2] = 1, then schemaArray[2] must be compared
 	
 	int[] columnArray;
 	int[] compareValues;
 	String name = "dummyRecord";
 	
 	public Record (){
 	    
 			
 			}
 	
 	public int checkCompareResult(String op, int compareResult){
 	
 		// compare result returns 1 for >, -1 for <, and 0 for ==
 		
 		// equals
 			if(op.equals("=")){
 				if(compareResult == 0)
 					return 1;
 				else
 					return 0;
 			}
 		// not equal to
 			if(op.equals("<>")){
 				if(compareResult == 0)
 					return 0;
 				else
 					return 1;
 			}
 			if(op.equals(">")){
				if(compareResult == -1)
 					return 1;
 				else 
 					return 0;
 			}
 			if(op.equals("<")){
				if(compareResult == 1)
 					return 1;
 				else
 					return 0;
 			}
 			if(op.equals(">=")){
 				if(compareResult == 1)
 					return 0;
 				else
 					return 1;
 			}
 			if(op.equals("<=")){
 				if(compareResult == -1)
 					return 0;
 				else
 					return 1;
 			}
 			return 0;
 		}
 	
 
 
 public int[] writeDummyFile(ArrayList<Condition> conditionList, int[] compareList, HeapFile heapFile){
 	Comparer comparer = new Comparer();
 // write dummy file
 //RandomAccessFile dummy = new RandomAccessFile(new File("dummy"), "rw");
 int i;
 int condIndex = 0;
 for(i = 0; i < heapFile.numberOfFields; i++){
 	if(condIndex < conditionList.size() && conditionList.get(condIndex).parameter.contains(i + 1 + "")){
 		comparer.compare_functions[heapFile.schemaArray[i]].write("dummy", 0, conditionList.get(condIndex).value, heapFile.lengthArray[i]);
 		compareList[i] = 1;
 
 		condIndex++;
 	}
 	else{
 		compareList[i] = 0;
 		if(heapFile.schemaArray[i] < 6)
 			comparer.compare_functions[heapFile.schemaArray[i]].write("dummy", 0, "0", heapFile.lengthArray[i]);
 		else 
 			comparer.compare_functions[heapFile.schemaArray[i]].write("dummy", 0, "n", heapFile.lengthArray[i]);
 	}
 } // end of filling dummy record loop
 return compareList;
 }
 }
