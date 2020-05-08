 package edu.teco.dnd.module.permissions;
 
 import java.io.ObjectStreamClass;
 import java.security.Permission;
 import java.util.Arrays;
 
 import org.apache.logging.log4j.LogManager;
 import org.apache.logging.log4j.Logger;
 
 import edu.teco.dnd.blocks.FunctionBlock;
 import edu.teco.dnd.module.Application;
 import edu.teco.dnd.module.FunctionBlockSecurityDecorator;
 import edu.teco.dnd.module.UsercodeWrapper;
 import edu.teco.dnd.module.messages.values.ValueMessageAdapter;
 
 /**
  * A SecurityManager used to restrict permissions for FunctionBlocks running on a Module.
  * 
  * @author Philipp Adolf
  */
 public class ApplicationSecurityManager extends SecurityManager {
 	private static final Logger LOGGER = LogManager.getLogger(ApplicationSecurityManager.class);
 
 	private static final PolicyRule rule;
 	static {
 		final PolicyRuleCombiner ruleCombiner = new PolicyRuleCombiner();
 
 		final GrantPermissionPolicyRule grantPermissionRule = new GrantPermissionPolicyRule();
 		grantPermissionRule.addPermission(new RuntimePermission("getClassLoader"));
 		grantPermissionRule.addPermission(new RuntimePermission("getenv.*"));
 		grantPermissionRule.addPermission(new RuntimePermission("getFileSystemAttributes"));
 		ruleCombiner.addRule(grantPermissionRule);
 
 		final StackTraceElementMatcherPolicyRule stemMatcher = new StackTraceElementMatcherPolicyRule();
 		stemMatcher.addInsecureMatcher(new ClassMatcher(UsercodeWrapper.class));
 		stemMatcher.addInsecureMatcher(new ClassMatcher(FunctionBlockSecurityDecorator.class));
 		stemMatcher.addInsecureMatcher(new MethodMatcher(ObjectStreamClass.class, "invokeReadObject"));
 		stemMatcher.addInsecureMatcher(new MethodMatcher(ObjectStreamClass.class, "invokeReadResolve"));
 		stemMatcher.addSecureMatcher(new MethodMatcher(FunctionBlock.class, "doInit"));
 		stemMatcher.addSecureMatcher(new MethodMatcher(Application.class, "sendValue"));
 		stemMatcher.addSecureMatcher(new MethodMatcher(ClassLoader.class, "loadClass"));
 		stemMatcher.addSecureMatcher(new ClassMatcher(ValueMessageAdapter.class));
 		ruleCombiner.addRule(stemMatcher);
 
		ruleCombiner.addRule(new JITPolicyRule());

 		rule = ruleCombiner;
 	}
 
 	@Override
 	public void checkPermission(final Permission permission) {
 		LOGGER.entry(permission);
 		
 		final StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
 		final Boolean policy = rule.getPolicy(permission, stackTrace);
 		
 		if (policy == null || policy == true) {
 			LOGGER.exit();
 			return;
 		}
 		
 		if (LOGGER.isWarnEnabled()) {
 			LOGGER.warn("denying {} for {}", permission, Arrays.asList(stackTrace));
 		}
 		throw new SecurityException();
 	}
 
 	@Override
 	public void checkPermission(final Permission perm, final Object context) {
 		checkPermission(perm);
 	}
 }
