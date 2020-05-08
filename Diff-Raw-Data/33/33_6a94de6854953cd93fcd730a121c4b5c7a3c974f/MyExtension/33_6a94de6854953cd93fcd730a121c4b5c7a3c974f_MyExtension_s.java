 /**
  * 
  */
 package org.jboss.solderbug;
 
 import javax.enterprise.event.Observes;
 import javax.enterprise.inject.spi.AfterBeanDiscovery;
 import javax.enterprise.inject.spi.AnnotatedType;
 import javax.enterprise.inject.spi.BeanManager;
 import javax.enterprise.inject.spi.Extension;
 
import org.jboss.seam.solder.bean.BeanBuilder;
import org.jboss.seam.solder.reflection.annotated.AnnotatedTypeBuilder;
 
 /**
  * @author antoine
  * 
  */
 public class MyExtension implements Extension {
 
     @SuppressWarnings({ "unchecked", "rawtypes" })
     public void afterBeanDiscovery(@Observes AfterBeanDiscovery abd, BeanManager bm) {
 
         AnnotatedTypeBuilder annoBuilder = new AnnotatedTypeBuilder().readFromType(MyBean.class).removeFromClass(
                 MyQualifier.class);
         AnnotatedType myAnnotatedType = annoBuilder.create();
 
         BeanBuilder beanBuilder = new BeanBuilder(bm).readFromType(myAnnotatedType);
         abd.addBean(beanBuilder.create());
    }
 
 }
