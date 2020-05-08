 /*
  * SchemaManagerImpl.java
  *
  * Created on October 16, 2010, 6:15 PM
  *
  * To change this template, choose Tools | Template Manager
  * and open the template in the editor.
  */
 
 package com.rameses.eserver;
 
 import com.rameses.common.ExpressionResolver;
 import com.rameses.common.PropertyResolver;
 import com.rameses.schema.SchemaConf;
 import com.rameses.schema.SchemaManager;
 import com.rameses.schema.SchemaSerializer;
 import com.rameses.util.ObjectSerializer;
 import groovy.lang.Binding;
 import groovy.lang.GroovyShell;
 import java.io.Serializable;
 import java.util.HashMap;
 import java.util.Map;
 import org.apache.commons.beanutils.PropertyUtils;
 
 /**
  *
  * @author ms
  */
 public class SchemaManagerImpl extends SchemaManager {
     
     private SchemaConf conf;
     
     public SchemaManagerImpl() {
         conf = new SchemaConfImpl(this);
         conf.setPropertyResolver( new BeanUtilPropertyResolver() );
         conf.setSerializer(new SchemaMgmtSerializer());
         conf.setExpressionResolver(new SchemaMgmtExpressionResolver() );
     }
     
     public SchemaConf getConf() {
         return conf;
     }
     
     public class SchemaConfImpl extends SchemaConf implements Serializable {
         SchemaConfImpl(SchemaManager sm) {
             super(sm);
         }
     }
     
     public class BeanUtilPropertyResolver implements PropertyResolver {
         
         public void setProperty(Object bean, String propertyName, Object value) {
             try {
                 PropertyUtils.setNestedProperty( bean, propertyName, value );
             } catch(Exception e){;}
         }
         
         public Class getPropertyType(Object bean, String propertyName) {
             try {
                 Object o = PropertyUtils.getNestedProperty( bean, propertyName );
                 if(o!=null) return o.getClass();
                 return PropertyUtils.getPropertyType(bean, propertyName);
             } catch(Exception e) {
                 //System.out.println("error " + e.getMessage());
                 return null;
             }
         }
         
         public Object getProperty(Object bean, String propertyName) {
             try {
                 return PropertyUtils.getNestedProperty( bean, propertyName );
             } catch(Exception e) {
                 return null;
             }
         }
     }
     
     public class SchemaMgmtExpressionResolver implements ExpressionResolver {
         public Object evaluate(Object bean, String expression) {
             GroovyShell shell = null;
             try {
                 Binding b = null;
                 if(bean instanceof Map) {
                     b = new Binding( (Map)bean );
                 } else {
                     Map m = new HashMap();
                     m.put("bean", bean);
                     b = new Binding(m);
                 }
                 shell = new GroovyShell(b);
                 return shell.evaluate( expression );
             } catch(Exception e) {
                 throw new RuntimeException(e);
             } finally {
                 shell = null;
             }
         }
     }
     
     public class SchemaMgmtSerializer implements SchemaSerializer, Serializable {
         
         public Object read(String s) {
             GroovyShell shell = null;
             try {
                 shell = new GroovyShell();
                 return shell.evaluate( s );
             } catch(Exception e) {
                 throw new RuntimeException(e);
             } finally {
                 shell = null;
             }
         }
         
         public String write(Object o) {
            return new ObjectSerializer().toString( o );
         }
     }
     
 }
