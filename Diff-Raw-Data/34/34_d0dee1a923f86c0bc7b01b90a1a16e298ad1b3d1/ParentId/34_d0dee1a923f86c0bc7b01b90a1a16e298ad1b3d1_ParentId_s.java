 package cat.mobilejazz.database.annotation;
 
 import java.lang.annotation.ElementType;
 import java.lang.annotation.Retention;
 import java.lang.annotation.RetentionPolicy;
 import java.lang.annotation.Target;
 
import cat.mobilejazz.database.EntityContext;

 /**
  * Marks this column to be the parent id of this table. There can be only one
  * such column for each table. The parent id is used for relations to identify
  * the corresponding element in the parent relation.
  */
 @Target(ElementType.FIELD)
 @Retention(RetentionPolicy.RUNTIME)
 public @interface ParentId {
 	
	public static final String TABLE_FROM_CONTEXT = "";
	
	/**
	 * 
	 * @return The name of the parent table if it is static, or 
	 */
	String parentTable() default TABLE_FROM_CONTEXT;
	
	Class<? extends EntityContext<String>> parentTableFromContext() default DefaultContext.class;
	
 }
