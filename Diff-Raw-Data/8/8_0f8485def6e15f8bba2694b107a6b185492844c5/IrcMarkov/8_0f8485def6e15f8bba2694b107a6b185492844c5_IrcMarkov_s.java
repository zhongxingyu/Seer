 package uk.co.ignignokt.markov.irc;
 
 import java.util.Collection;
 import java.util.List;
 
 import uk.co.ignignokt.markov.Markov;
 
 public class IrcMarkov implements Markov {
 	IrcList master = new IrcList();
 
 	public void addParagraph(String paragraph){
 		paragraph = paragraph.trim();
 		String removed = paragraph.replace('\n', ' ');
 
 		String[] list = removed.split(" +");
 		
 		for(int i = 0; i < list.length-1; i++){
 			master.addWord(i, list.length-1, list[i], list[i+1]);
 		}
 	}
 
 	public IrcMarkov(){
 	}
 	
 	public String getStructure(){
 		StringBuilder retval = new StringBuilder();
 		
 		Collection<Irc> words = master.getStructure();
 		
 		for(Irc word : words){
 			retval.append(word.getText() + " -> " + word.isStart() + "\n");
 			List<Irc> children = word.getChildren();
 			
 			for(Irc child : children){
 				retval.append("    " +  child.getText() + "\n");
 			}
 		}
 		
 		return retval.toString();
 	}
 	
 	public String getLimit(int limit){
 		StringBuilder retval = new StringBuilder();
 		
 		Irc current = master.getRandom();
 		
 		while(limit-- > 0){
 			retval.append(current.getText());
 			retval.append(" ");
 			
 			current = current.getNext();
 			if(current == null) return retval.toString();
 		}

 		return retval.toString();
 	}
 	
 	public String getSentence(){
 		return getLimit(20);
 	}
 }
