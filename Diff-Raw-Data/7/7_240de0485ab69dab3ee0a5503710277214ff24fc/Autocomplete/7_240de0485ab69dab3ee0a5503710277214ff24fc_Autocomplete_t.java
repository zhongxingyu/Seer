 /**
  * CS 200 Colorado State University, Spring 2013
  * This class builds a list of candidate words those
  * can be the final outputs if the user completes the
  * specified word.
  *
  */
 
 public class Autocomplete {
     // this is a constructor of this class.
     public Autocomplete(){
     } 
 
     // listCandidates will generate a list of the candidnate words.
     // (1) data_file is the location of the input data file. 
     // Students should be able to find an example input data file from
     // ./Words.txt 
     // Words.txt contains around 20,000 unsorted words.
     // During the grading, Words.txt file will be stored in the same 
     // directory.
     // (2) max_count is the maxinum number of words returned by this method
     // If you cannot find enough candidates, please fill the array
     // as much as possible and return it.
     // If you could find more than max_count, please return only as many as
     // max_count.
     // (3) inputString is the specified string that user is working on.
     // inputString can contain uppercase and lower case alphabet. (no number or sign)
     // Your method should not be case-sensitive.
     //
     // To complete this method, 
     // Step 1: read data from data_file
     // Step 2: sort them with the quicksort method
     // Step 3: find the list and return it
     public String [] listCandidates(String data_file, int max_count, String inputString){	     
     	Comparable[] matches = new String[max_count];
     	int numMatches = 0;
     	
     	Quicksort qs = new Quicksort();
    	Comparable[] words = qs.storeInput(data_file);
     	qs.sort(words);
 
     	for(int i = 0; i < words.length;i++){
     		if(doesWordMatch(inputString,(String)words[i]) && numMatches < max_count){
     			matches[numMatches] = words[i];
     			numMatches++;
     		}
     	}
 
     	return (String[])matches;	
     }
     
     //Return true if little word is first part (or entire part) of parameter 'word'.
     private boolean doesWordMatch(String little, String word){
 
     	if(word.length() >= little.length()){
     		for(int i = 0; i < little.length();i++){
     			if(little.toLowerCase().charAt(i) == word.toLowerCase().charAt(i)){
     			}
     			else//Returns false here if words don't match(checking each character).
     				return false;
     		}
     	}else{//Returns false here if your input is larger then word.
     		return false;
     	}
     	
     	return true;
     }
     
 
 }
