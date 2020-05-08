 /*
  * Created on 13-Jan-2006
  */
 package uk.org.ponder.rsf.components;
 
 import uk.org.ponder.beanutil.BeanUtil;
 
 /** A special class to hold EL references so they may be detected in the
  * component tree. When held in this member, it is devoid of the packaging #{..}
  * characters - they are removed and replaced by the parser in transit from
  * XML form.
  * @author Antranig Basman (amb26@ponder.org.uk)
  *
  */
 // TODO: in RSF 0.8 this class will be deprecated in favour of the version
 // in PUC.
 public class ELReference {
   public ELReference() {}
   public ELReference(String value) {
     String stripped = BeanUtil.stripEL(value);
     this.value = stripped == null? value : stripped;
    if ("".equals(value)) {
      throw new IllegalArgumentException(
          "Cannot issue an EL reference to an empty path. For an empty binding please either supply null, or else provide a non-empty String as path");
    }
   }
   public String value;
   public static ELReference make(String value) {
     return value == null? null : new ELReference(value);
   }
 }
