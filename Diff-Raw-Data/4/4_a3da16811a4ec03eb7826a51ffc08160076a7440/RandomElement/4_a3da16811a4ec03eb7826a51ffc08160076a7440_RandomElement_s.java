 package aiml.script;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.logging.Logger;
 
 import org.xmlpull.v1.XmlPullParser;
 import org.xmlpull.v1.XmlPullParserException;
 
 import aiml.classifier.MatchState;
 import aiml.parser.AimlParserException;
 import aiml.parser.AimlSyntaxException;
 
 public class RandomElement implements Script {
 
   private ArrayList<Script> items = new ArrayList<Script>();
   
   private void parseItem(XmlPullParser parser) throws XmlPullParserException, IOException, AimlParserException {
     if (!(parser.getEventType()==XmlPullParser.START_TAG && parser.getName().equals("li")))
       throw new AimlSyntaxException("Syntax error: expecting start tag 'li' while parsing 'random' "+parser.getPositionDescription());
     items.add(new Block().parse(parser));
     if (!(parser.getEventType()==XmlPullParser.END_TAG && parser.getName().equals("li")))
       throw new AimlSyntaxException("Syntax error: expecting end tag 'li' while parsing 'random' "+parser.getPositionDescription());
     parser.nextTag();
   }
   public Script parse(XmlPullParser parser) throws XmlPullParserException, IOException, AimlParserException {
     parser.nextTag();
     do {
       parseItem(parser);
     } while (!(parser.getEventType()==XmlPullParser.END_TAG && parser.getName().equals("random")));
     if (items.size()==1) {
       Logger.getLogger(RandomElement.class.getName()).warning("random element "+parser.getPositionDescription() + " contains only one alternative");
       parser.next();
       return items.get(0);
     } else {
       parser.next();
       return this;
     }
     
   }
 
   public String evaluate(MatchState m) {
     StringBuffer result =  new StringBuffer("random(");
     result.append(items.size());
     for (Script i : items) {
       result.append(',').append(i.evaluate(m));
     }
     result.append(')');
     return result.toString();
     
   }
 
   public String execute(MatchState m) {
     StringBuffer result = new StringBuffer();
    result.append("switch(rand(1,").append(items.size()).append(") {");
     int n=1;
     for (Script i : items) {
       result.append("case ").append(n).append(":\n");
       result.append("\t").append(i.execute(m)).append("\n");
       result.append("\tbreak;\n");
     }
     result.append('}');  
     return result.toString();
   }
   
   public String toString() {
     return "random("+items.size()+":"+items+")";
   }
 }
