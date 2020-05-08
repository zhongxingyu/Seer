 package de.tarent.maven.plugins.pkg.annotations;
 
 import java.lang.annotation.Retention;
 import java.lang.annotation.RetentionPolicy;
 
 /**
  * Interface needed by Utils::mergeTargetConfigurations to do its job.
  * 
 * <p>If the interface is used by a field no defaulg values are defined, 
 * the fall bakc behaviour is either an empty String or a Boolean set to false.</p>
  * 
  * <p>If defaultValueIsNull is set, then a null value will be used as default
  * when merging.</p>
  * 
  * @author plafue
  *
  */
 @Retention(RetentionPolicy.RUNTIME)
 public @interface MergeMe {
 	String defaultString() default "";
 	boolean defaultBoolean() default false;
 	boolean defaultValueIsNull() default false;
 }
