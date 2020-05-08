 package se.su.it.svc.annotations;
 
import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
 public @interface AuditHideReturnValue {
 }
