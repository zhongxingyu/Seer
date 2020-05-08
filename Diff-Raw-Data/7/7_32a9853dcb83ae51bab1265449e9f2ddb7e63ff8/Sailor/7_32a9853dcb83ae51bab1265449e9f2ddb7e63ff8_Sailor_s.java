 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package pirate.seamen;
 
 /**
  *
  * @author Ben
  */
 public class Sailor {
     
     public String name;
     public boolean isScallawag = false;
     
     public Sailor(String name, boolean scallawag){
 	this.name = name;
 	isScallawag = scallawag;
     }
     
     public String talkLikeAPirate(){
 	String sentence;
	if(Math.random() > 0.5){
 	    sentence = Vernacularrrgh.PICK_UP_LINES[(int)(Math.random() * Vernacularrrgh.PICK_UP_LINES.length)];
 	} else {
 	    sentence = Vernacularrrgh.SENTENCES[(int)(Math.random() * Vernacularrrgh.SENTENCES.length)];
 	    for(int i = 0; i < Vernacularrrgh.PARTS_OF_SPEECH.length; i++){
		while(sentence.indexOf(name) > -1){
		    sentence = sentence.replaceFirst(Vernacularrrgh.PARTS_OF_SPEECH[i], Vernacularrrgh.get(Vernacularrrgh.PARTS_OF_SPEECH[i]));
 		}
 	    }
 	}
 	
 	return sentence;
     }
 }
