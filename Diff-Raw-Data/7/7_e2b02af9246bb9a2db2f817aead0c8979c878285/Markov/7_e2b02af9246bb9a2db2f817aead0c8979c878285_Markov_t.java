 package uk.co.ignignokt.markov;
 
 import java.util.Collection;
 import java.util.List;
 
 import uk.co.ignignokt.markov.word.Word;
 import uk.co.ignignokt.markov.word.WordList;
 
 public class Markov {
 	WordList master = new WordList();
 
 	public void addSentence(String sentence){
 		sentence = sentence.trim();
 		String removed = sentence.replace('\n', ' ');
 		
 		String[] list = removed.split(" +");

 		for(int i = 0; i < list.length; i++){
			if(i == 0 && list.length == 1)
				master.addWord(list[i], null, true);
			else if(i == 0)
 				master.addWord(list[i], list[i+1], true);
 			else if(i == list.length-1)
 				master.addWord(list[i], null);
 			else
 				master.addWord(list[i], list[i+1]);
 		}
 	}
 	
 	public String getStructure(){
 		StringBuilder retval = new StringBuilder();
 		
 		Collection<Word> words = master.getStructure();
 
 		for(Word word : words){
 			retval.append(word.getText() + "->" + word.isStart() + "\n");
 			List<Word> children = word.getChildren();
 			
 			for(Word child : children){
 				if(child == null)
 					retval.append("    " + null + "\n");
 				else
 					retval.append("    " +  child.getText() + "\n");
 			}
 		}
 		
 		return retval.toString();
 	}
 	
 	public String getLimit(int limit){
 		StringBuilder retval = new StringBuilder();
 		
 		Word current = master.getRandom();
 		
 		while(limit-- > 0){
 			retval.append(current.getText());
 			retval.append(" ");
 			
 			current = current.getNext();
 			if(current == null) return retval.toString();
 		}
 
 		return retval.toString();
 	}
 	
 	public String getSentence(){
 		StringBuilder retval = new StringBuilder();
 		
 		Word current = master.getRandom();
 		
 		while(!current.isStart()) current = master.getRandom();
 		
 		while(current != null){
 			retval.append(current.getText());
 			retval.append(" ");
 			current = current.getNext();
 		}
 		
 		return retval.toString();
 	}
 }
