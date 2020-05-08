 package validation;
 
 
 import java.util.HashMap;
 import java.util.Map;
 
 import net.sf.oval.Validator;
 import net.sf.oval.configuration.annotation.AbstractAnnotationCheck;
 import net.sf.oval.context.OValContext;
 import captcha.CaptchaManager;
 import captcha.CaptchaManager.CaptchaAuth;
 
 public class CaptchaCheck extends AbstractAnnotationCheck<Captcha> {
     public static final String mes = "validation.captcha";
     
     @Override
    public void configure(Captcha unique) {
        //System.out.println("configure");
        setMessage(unique.message());
     }
 
     @Override
     public boolean isSatisfied(Object validatedObject, Object value, OValContext context, Validator validator) {
         if(!CaptchaManager.enabled()) return true;
         
         CaptchaAuth captcha = (CaptchaAuth)value;
         if(captcha == null) return true;
         
         return CaptchaManager.check(captcha);
     }
 }
