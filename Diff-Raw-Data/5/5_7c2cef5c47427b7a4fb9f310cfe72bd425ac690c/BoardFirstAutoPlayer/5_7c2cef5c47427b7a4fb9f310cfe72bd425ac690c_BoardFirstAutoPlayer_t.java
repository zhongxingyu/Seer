 import java.util.ArrayList;
 import java.util.List;
 
 
 
 public class BoardFirstAutoPlayer extends AbstractAutoPlayer {
     
     @Override
     public void findAllValidWords(BoggleBoard board, ILexicon lex, int minLength) {
 	    // set score to zero & clear all words already stored
     	clear();
 
     	
     	// try to form words by trying all paths on the board
     	// double for loop goes here
     	for(int r=0; r<board.size(); r++){
 			for(int c=0; c<board.size(); c++){
 				StringBuilder word = new StringBuilder();
 				word.append("");
 				List<BoardCell> list = new ArrayList<BoardCell>(); // may need to move this below
 				helper(board, r, c, list, word, lex);
 			}
 		}
     	
     	// prune searches based on prefixes
     }
     
     // recursive helper here
     public void helper(BoggleBoard board, int r, int c, 
     		List<BoardCell> list, StringBuilder soFar, ILexicon lex){
     	
     	if(r < 0 || c < 0 || r >= board.size() || c >= board.size() ){
 			return; // discontinue search 
 		}
     	BoardCell cell = new BoardCell(r, c);
 		if(list.contains(cell)){ return; } // no duplicating cells
 		
     	// add letter at rc to soFar
 		String current = board.getFace(r, c);
 		soFar.append(current);
 		
 		
     	// if soFar is a word or a prefix, continue by calling helper on adjacent cubes
 		if(lex.wordStatus(soFar) != LexStatus.NOT_WORD){
 			list.add(cell);
 			if(lex.wordStatus(soFar) == LexStatus.WORD){
 				add(soFar.toString());
 			}
 			
 			int[] rdelta = {-1,-1,-1, 0, 0, 1, 1, 1};
 			int[] cdelta = {-1, 0, 1,-1, 1,-1, 0, 1};
 			for(int k=0; k < rdelta.length; k++){
 			  helper(board, r+rdelta[k], c+cdelta[k], 
 			    		list, soFar, lex); 
 			}			
 			
 			// backtracking step
 			list.remove(cell);	
 		}
 		
		for(int i=0; i<current.length(); i++){
			soFar.deleteCharAt(soFar.length()-1);
		}
		
     	return; 
     }
     
     // can override inherited methods if necessary 
 
 }
