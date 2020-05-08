 package kwitches.text;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  * 行中のレス番号にあたる文字列を変換するクラス
  * @author voidy21
  *
  */
 public class ResMessageTransformer implements LineMessageTransformInterface {
 
     private final static String REGEXP_RES_STRING = "&gt;&gt;([0-9]+)";

     /* (非 Javadoc)
      * @see kwitches.text.LineMessageTransformInterface#getRegexp()
      */
     public String getRegexp() {
         return REGEXP_RES_STRING;
     }
 
     /* (非 Javadoc)
      * @see kwitches.text.LineMessageTransformInterface#transform(java.lang.String)
      */
     public String transform(String rawString) {
         Pattern p = Pattern.compile(this.getRegexp());
         Matcher m = p.matcher(rawString);
        return m.replaceAll("<a class='res like-link' data-resnum='$1'>&gt;&gt;$1</a>");
     }
 
 }
