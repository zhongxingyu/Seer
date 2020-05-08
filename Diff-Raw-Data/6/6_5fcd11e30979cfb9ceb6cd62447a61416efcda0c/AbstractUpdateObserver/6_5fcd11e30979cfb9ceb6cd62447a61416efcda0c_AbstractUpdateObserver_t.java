 package utilits.aspect.observer.update;
 
 import org.apache.commons.beanutils.PropertyUtils;
 import org.apache.commons.lang3.ObjectUtils;
 import org.aspectj.lang.ProceedingJoinPoint;
 import utilits.aspect.ActionType;
 import utilits.aspect.IChangeType;
 import utilits.aspect.observer.AbstractObserver;
 import utilits.entity.Action;
 import utilits.service.ActionService;
 
 import java.lang.reflect.InvocationTargetException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 
 /**
  * Here will be javadoc
  *
  * @author karlovsky
  * @since 1.0
  */
 public abstract class AbstractUpdateObserver<E, C> extends AbstractObserver {
 
     private final ProceedingJoinPoint pjp;
 
     private final ActionService actionService;
 
     private final Class<E> clazz;
 
     private final SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
 
     private final IChangeType[] changeTypes;
 
     public AbstractUpdateObserver(ActionType actionType, IChangeType[] changeTypes, Class<E> clazz, ProceedingJoinPoint pjp,
                                   ActionService actionService) {
         super(actionType);
         this.clazz = clazz;
         this.pjp = pjp;
         this.actionService = actionService;
         this.changeTypes = changeTypes;
     }
 
     @Override
     public Object observe() throws Throwable {
         Long id = findIdArgument();
         E oldEntity = actionService.loadEntity(id, clazz);
         Object result = pjp.proceed();
         E newEntity = actionService.loadEntity(id, clazz);
         Collection<C> changes = buildChanges(oldEntity, newEntity);
         Action action = buildAction();
         addChanges(action, changes);
         actionService.saveAction(action);
         return result;
     }
 
     protected abstract void addChanges(Action action, Collection<C> changes);
 
     protected abstract C buildChange(IChangeType changeType, String oldValue, String newValue, Long id);
 
     protected abstract Long makeEntityId(E e);
 
     protected abstract int getArgumentIndex(ActionType actionType);
 
     protected Collection<C> buildChanges(E oldEntity, E newEntity) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
         Collection<C> result = new ArrayList<C>();
         Long id = makeEntityId(newEntity);
         for (IChangeType changeType : changeTypes) {
             Object oldValue = PropertyUtils.getProperty(oldEntity, changeType.getFieldName());
             Object newValue = PropertyUtils.getProperty(newEntity, changeType.getFieldName());
             if (ObjectUtils.notEqual(oldValue, newValue)) {
                 result.add(makeChange(changeType, oldValue, newValue, id));
             }
         }
         return result;
     }
 
     private C makeChange(IChangeType changeType, Object oldValue, Object newValue, Long id) {
         C change = null;
        if (oldValue instanceof String || newValue instanceof String) {
            change = buildChange(changeType, (String) oldValue, (String) newValue, id);
         } else if (oldValue instanceof Date && newValue instanceof Date) {
             Date oldDate = (Date) oldValue;
             Date newDate = (Date) newValue;
             change = buildChange(changeType, sdf.format(oldDate), sdf.format(newDate), id);
         } else if (oldValue instanceof Enum && newValue instanceof Enum) {
             Enum oldEnumType = (Enum) oldValue;
             Enum newEnumType = (Enum) newValue;
             change = buildChange(changeType, oldEnumType.name(), newEnumType.name(), id);
         }
         return change;
     }
 
     private Long findIdArgument() {
         Long id = null;
         try {
             int index = getArgumentIndex(actionType);
             Object object = pjp.getArgs()[index];
             if (object instanceof Long) {
                 id = (Long) object;
             }
         } catch (ArrayIndexOutOfBoundsException e) {
             //ignored
         }
         return id;
     }
 }
