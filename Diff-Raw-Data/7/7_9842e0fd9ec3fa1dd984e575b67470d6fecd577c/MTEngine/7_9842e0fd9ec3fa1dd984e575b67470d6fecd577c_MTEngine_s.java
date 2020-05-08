 package chb.template;
 
 
 import chb.text.TextUtility;
 import org.w3c.dom.NodeList;
 
 import javax.xml.xpath.*;
 
 public class MTEngine extends TEngine {
 
     private XPathFactory factory = null;
 
     public MTEngine() {
         super();
         factory = XPathFactory.newInstance();
     }
 
     /**
      * Query template information for the given word.
      *
      * @param word String word
      * @return TInfo instance which has the information.
      */
     public TInfo query(String word) {
         if (xml == null || word == null || word.length() == 0) {
             return null;
         }
 
         TInfo ti = TInfo.createTInfo(word);
 
         XPath xpath = factory.newXPath();
         String xp = null;
         String k = null;
         if (word.length() == 1) {
             /* If the length of word is 1, then it may be a single
             * character or punctuation. */
             if (TextUtility.IsChinesePunctuation(word.charAt(0))) {
                 k = "pause";
                 ti.type = TInfo.PUNCTUATION;
                 xp = "//pauses/pause[@type='punctuation' and @value='" + word + "']/@msec";
             } else {
                /* For a single, no diaphone is needed.*/
                 k = "diaphone";
                 ti.type = TInfo.WORD;
                ti.set(k, "-1");
                return ti;
             }
         } else {
             k = "diaphone";
             ti.type = TInfo.WORD;
             xp = "//diaphones/diaphone[@type='word' and @length='" + word.length() + "']/@msec";
         }
 
         NodeList lst = doXPath(xpath, xp);
 
         /*
          * If the length or  the value of word falls outside those specified
          * in meta.xml, uses the default setting (*).
          */
         if (lst == null || lst.getLength() < 1) {
             if (k == "pause") {
                 xp = "//pauses/pause[@type='punctuation' and @value='*']/@msec";
             } else if (k == "diaphone") {
                 xp = "//diaphones/diaphone[@type='word' and @length='*']/@msec";
             }
 
             lst = doXPath(xpath, xp);
 
             if (lst == null || lst.getLength() < 1) {
                 return ti;
             }
         }
 
         String v = lst.item(0).getNodeValue();
         ti.set(k, v);
 
         return ti;
 
     }
 
     /**
      * Encapsulate the xpath operation into the method.
      *
      * @param xpath XPath instance.
      * @param xp    XPath string.
      * @return NodeList that is returned by XPath operation.
      */
     protected NodeList doXPath(XPath xpath, String xp) {
         if (xpath == null || xp == null) {
             return null;
         }
 
         Object res = null;
         try {
             XPathExpression xpre = xpath.compile(xp);
             res = xpre.evaluate(xml, XPathConstants.NODESET);
         } catch (XPathExpressionException e) {
             chb.Utility.Log(e.getStackTrace());
         }
 
         if (res == null) {
             return null;
         }
 
         NodeList lst = (NodeList) res;
         return lst;
     }
 
     /**
      * Get pause between words.
      *
      * @return TInfo instance.
      */
     public TInfo queryBt() {
         String xp = "//pauses/pause[@type='btword']/@msec";
         XPath xpath = factory.newXPath();
 
         TInfo ti = TInfo.createTInfo("");
 
         NodeList lst = doXPath(xpath, xp);
         if (lst == null || lst.getLength() < 1) {
             ti.set("pause", "10");
             ti.type = TInfo.PUNCTUATION;
             return ti;
         }
 
         String v = lst.item(0).getNodeValue();
         ti.set("pause", v);
 
         return ti;
     }
 }
