 package org.nohope.akka.spring;
 
 import akka.actor.ActorRef;
 import akka.actor.ActorSystem;
 import akka.actor.Props;
 import org.junit.Test;
 import org.springframework.context.support.GenericApplicationContext;
 import org.nohope.spring.SpringUtils;
 import org.nohope.test.SerializationUtils;
 
 import java.util.List;
 import java.util.Map;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 import static org.nohope.akka.Ask.waitReply;
 import static org.nohope.akka.spring.Bean.Props.*;
 
 /**
  * @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
  * @since 9/16/12 11:11 PM
  */
 public class ReflectActorFactoryTest {
     @Test
     public void partialInject() throws Exception {
         final GenericApplicationContext ctx = new GenericApplicationContext();
         SpringUtils.registerSingleton(ctx, 1);
 
         final ReflectActorFactory<Bean> beanFactory = new ReflectActorFactory<>(ctx, Bean.class);
         beanFactory.addBean("param2", "test1");
         beanFactory.addBean("param3", "test2");
 
         final ActorSystem system = ActorSystem.create();
         final ActorRef ref = system.actorOf(new Props(beanFactory));
 
        assertEquals(1, (int) waitReply(Integer.class, ref, PARAM1));
        assertEquals("test1", waitReply(String.class, ref, PARAM2));
        assertEquals("test2", waitReply(String.class, ref, PARAM3));
 
         assertEquals(ctx, beanFactory.getContext());
         final Map<String,Object> namedBeans = beanFactory.getNamedBeans();
         assertTrue(namedBeans.containsKey("param2") && namedBeans.containsKey("param3"));
         final List<Object> beans = beanFactory.getBeans();
         assertEquals(0, beans.size());
         assertEquals(Bean.class, beanFactory.getTargetClass());
     }
 
     @Test(expected = AssertionError.class)
     public void javaSerialization() {
         final GenericApplicationContext ctx = new GenericApplicationContext();
         final ReflectActorFactory<Bean> beanFactory = new ReflectActorFactory<>(ctx, Bean.class);
         SerializationUtils.cloneJava(beanFactory);
     }
 }
