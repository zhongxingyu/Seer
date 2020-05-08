 package org.nohope.akka.spring;
 
 import akka.actor.Props;
 import akka.actor.UntypedActor;
 import akka.actor.UntypedActorFactory;
 import org.springframework.context.ApplicationContext;
 import org.nohope.spring.PartiallyDefinedArgumentsFactory;
 
 import javax.annotation.Nonnull;
 import javax.annotation.Nullable;
 import java.io.IOException;
 import java.io.NotSerializableException;
 import java.io.ObjectOutputStream;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import static org.nohope.spring.SpringUtils.instantiate;
 import static org.nohope.spring.SpringUtils.registerSingleton;
 
 /**
  * This class is used for spring-driven actor creation. Allows to inject
  * bean dependencies in actor and add external inject dependencies which
  * can be not defined in given context.
  * <p/>
  * <b>Typical usage</b>:
  * <pre>
  *     class MyActor extends {@link akka.actor.UntypedActor UntypedActor} {
  *          &#064;{@link javax.inject.Inject Inject}
  *          MyActor (&#064;{@link javax.inject.Named Named}(name="stringFromContext") String param,
  *                   GlobalBean beanFromContext,
  *                   &#064;{@link javax.inject.Named Named}(name="bean") Bean bean,
  *                   OtherBean otherBean) {
  *              ...
  *          }
  *     }
  *
  *     {@link org.springframework.context.ApplicationContext ApplicationContext} ctx;
  *     {@link akka.actor.ActorSystem ActorSystem} system;
  *
  *     {@link akka.actor.ActorRef ActorRef} ref = system.actorOf(
  *          {@link SpringActorFactory#create() create}(ctx, MyActor.class)
  *              .addBean("bean", new Bean()) // additional @Named inject
  *              .addBean(new OtherBean())    // additional typed inject
  *              .getProps(),
  *          "myActor");
  * </pre>
  *
  * @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
  * @since 9/16/12 11:09 PM
  */
 public final class SpringActorFactory<T extends UntypedActor> extends PartiallyDefinedArgumentsFactory<T> implements UntypedActorFactory {
     private static final long serialVersionUID = 0L;
 
     public SpringActorFactory(@Nonnull final ApplicationContext ctx, @Nonnull final Class<T> clazz) {
         super(ctx, clazz);
     }
 
     public SpringActorFactory(@Nonnull final ApplicationContext ctx,
                               @Nonnull final Class<T> clazz,
                               @Nullable final List<Object> objects,
                               @Nullable final Map<String, Object> namedObjects) {
         super(ctx, clazz, objects, namedObjects);
     }
 
     public Props getProps() {
         return new Props(this);
     }
 
     @Override
     public SpringActorFactory<T> addBeans(@Nonnull final Object... beans) {
         super.addBeans(beans);
         return this;
     }
 
     @Override
     public SpringActorFactory<T> addBean(final String name, @Nonnull final Object bean) {
         super.addBean(name, bean);
         return this;
     }
 
     @SuppressWarnings("PMD.UnusedFormalParameter")
     private void writeObject(final ObjectOutputStream oos) throws IOException {
         throw new NotSerializableException();
     }
 
     @Override
     public UntypedActor create() throws Exception {
         return super.instantiate();
     }
 }
