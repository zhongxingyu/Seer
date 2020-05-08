 package jcube.core.controller.invoker;
 
 import java.util.Map;
 
 import jcube.core.controller.ControllerClass;
import jcube.core.exception.BeanException;
 import jcube.core.exception.ConfigException;
 import jcube.core.exception.InitializeException;
 import jcube.core.exception.LogicException;
 import jcube.core.exception.MethodException;
 import jcube.core.helpers.ClassLoader;
 import jcube.core.server.environ.Environ;
 
 // TODO: Auto-generated Javadoc
 /**
  * The Class ControllerConstruct.
  * 
  * @author iorlov
  */
 public class ControllerConstruct
 {
 
 	/**
 	 * Construct.
 	 * 
 	 * @param controllerName the controller name
 	 * @param actionName the action name
 	 * @param context the context
 	 * @throws LogicException the logic exception
 	 * @throws InitializeException the initialize exception
 	 * @throws MethodException
 	 * @throws ConfigException 
	 * @throws BeanException 
 	 */
 	public static ControllerMethodPerformer construct(Environ env, String controllerName, String actionName,
			Map<String, ControllerClass> context) throws LogicException, InitializeException, MethodException, ConfigException, BeanException
 	{
 
 		if (!context.containsKey(controllerName))
 			throw new LogicException(404, "Can't find the controller with current context!");
 
 		ControllerClass controlerClass = context.get(controllerName);
 		if (!controlerClass.hasMethod(actionName))
 			throw new LogicException(404, "Can't find the method withing the current contoller!");
 
		Object instance = ClassLoader.create(controlerClass.getControllerClass(), env);
 		
 		return new ControllerMethodPerformer(env, instance, controlerClass, actionName);
 	}
 
 }
