 package abc.parser.def;
 
 import scanner.AutomataDefinition;
 import scanner.State;
 import scanner.Transition;
 import abc.parser.AbcTokenType;
 
 /** **/
 public class TextDefinition extends AutomataDefinition {
 
   private static char[] chars = {
     '0','1','2','3','4','5','6','7','8','9',
     ' ',
     '\t','"','!','#','$','&','\'','(',')','*','+',',','-','.','/',':',';','<','=','>','?','@','[','\\',']','^','_',
     '`','{','|','}','~',
     'A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z',
     'a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z', 
     //==== not really part of v1.6
     //check http://en.wikipedia.org/wiki/Basic_Latin for values.
    //'',    '',      '',      '',      '',      '',      '',      '', 
    '\u00E1', '\u00E2', '\u00E3', '\u00E9', '\u00EA', '\u00ED', '\u00F3', '\u00F4', // as requiered by Hugo
     //'',    '',      '',
     '\u00F5', '\u00FA', '\u00E7',													// as requiered by Hugo 
     //'',     '',     '',      '',      '',      '',      '',      '', 
     '\u00C0', '\u00C2', '\u00C3', '\u00C9', '\u00CA', '\u00CD', '\u00D3', '\u00D4',	// as requiered by Hugo
     //'',    '',      '',
     '\u00D5', '\u00DA', '\u00C7',													// as requiered by Hugo
     //'',    '',      ''
     '\u00E8', '\u00E0', '\u00F9',													// French characters
     };
 
     public TextDefinition() {   
     	//===================== FIELD
         State stateTEXT_CHAR = new State(AbcTokenType.TEXT, true);
         Transition trans = new Transition(stateTEXT_CHAR, chars);
         getStartingState().addTransition(trans);
         stateTEXT_CHAR.addTransition(new Transition(stateTEXT_CHAR, chars));
     }
 
 }
 
