 package sound;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class Voice
 {
     public static final String DEFAULT_VOICE_NAME = "DEFAULT_VOICE";
     
     private final List<Playable> elementsOfVoice; 
     private final String name;
     
     public Voice(String name, List<Playable> elements)
     {
         this.name = name;
         elementsOfVoice = new ArrayList<Playable>(elements);
     }
     
     public String getName()
     {
         return this.name;
     }
     
     public List<Playable> getElementsOfVoice()
     {
         return new ArrayList<Playable>(elementsOfVoice); 
     }
     
     @Override
     public String toString() {
     	StringBuilder newStringBuilder = new StringBuilder();
     	newStringBuilder.append("Voice("+name+")" + "\n");
     	for (Playable elementOfVoice: elementsOfVoice) {
     		newStringBuilder.append(elementOfVoice.toString() + "\n");
     	}
     	return newStringBuilder.toString();
     }
     
     public int findLCMOfNoteDenoms()
     {
         int lcm = -1; 
         for(int i = 0; i < elementsOfVoice.size(); i++)
         {
             int denomOfElement = elementsOfVoice.get(i).getLength().getDenom();
             if (i == 0)
             {
                 lcm = denomOfElement;
             }
             
             else 
             {
                 lcm = Utilities.LCM(lcm, denomOfElement); 
             }
         }
         
         return lcm;
     }
     
     public List<SequencePlayerNote> play(RatNum defaultNoteLength, int numTicksPerQuarter) {
         
         int ticks = 0;
     	List<SequencePlayerNote> sequencePlayerNotes = new ArrayList<SequencePlayerNote> ();
     	for (Playable elementOfVoice: elementsOfVoice) {
     		sequencePlayerNotes.addAll(elementOfVoice.play(ticks, numTicksPerQuarter, defaultNoteLength)); //Need to handle timing here
     		RatNum length = elementOfVoice.getLength();
    		ticks += (numTicksPerQuarter * length.getNumer() * 4 * defaultNoteLength.getNumer())
    		        / (length.getDenom() * defaultNoteLength.getDenom());
     	}
     	return sequencePlayerNotes;
     }
 }
