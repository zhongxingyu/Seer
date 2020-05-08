 package aiml.script;
 
 import java.io.IOException;
 
 import org.xmlpull.v1.XmlPullParser;
 import org.xmlpull.v1.XmlPullParserException;
 
import aiml.classifier.MatchState;
 import aiml.parser.AimlParserException;
 import aiml.parser.AimlSyntaxException;
 
public class ConditionElement implements Script {
 
   public Script parse(XmlPullParser parser) throws XmlPullParserException,
       IOException, AimlParserException {
     String name = parser.getAttributeValue(null, "name");
     String value = parser.getAttributeValue(null, "value");
     if (name != null && value != null) {
       return new If(name, value).parse(parser);
     } else if (name != null && value == null) {
       return new Switch(name).parse(parser);
     } else if (name == null && value == null) {
       return new IfThenElse().parse(parser);
     } else
       // name==null && value!=null
       throw new AimlSyntaxException(
           "Syntax error: illegal name/value combination for condition " +
               parser.getPositionDescription());
   }

  public String evaluate(MatchState m) {
    throw new UnsupportedOperationException("evaluate()");
  }
 }
