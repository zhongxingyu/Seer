 package suite.util;
 
 import java.io.File;
 import java.io.PrintWriter;
 import java.lang.reflect.InvocationHandler;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.lang.reflect.Proxy;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.log4j.ConsoleAppender;
 import org.apache.log4j.DailyRollingFileAppender;
 import org.apache.log4j.Level;
 import org.apache.log4j.Logger;
 import org.apache.log4j.PatternLayout;
 
 public class LogUtil {
 
 	private static final int maxStackTraceLength = 99;
 	private static final Log suiteLog = LogFactory.getLog("suite");
 
 	static {
 		initLog4j(Level.INFO);
 	}
 
 	public static void initLog4j(Level level) {
 		File logDir = new File(System.getProperty("home.dir"), "logs");
 
 		PatternLayout layout = new PatternLayout("%d %-5p [%c{1}] %m%n");
 
 		ConsoleAppender console = new ConsoleAppender(layout);
 		console.setWriter(new PrintWriter(System.err));
 		console.activateOptions();
 
 		DailyRollingFileAppender file = new DailyRollingFileAppender();
 		file.setFile(new File(logDir, "suite.log").toString());
 		file.setDatePattern("'.'yyyyMMdd");
 		file.setLayout(layout);
 		file.activateOptions();
 
 		Logger logger = Logger.getRootLogger();
 		logger.setLevel(level);
 		logger.removeAllAppenders();
 		logger.addAppender(console);
 		logger.addAppender(file);
 	}
 
 	public static void info(String message) {
 		suiteLog.info(message);
 	}
 
 	public static void error(Throwable th) {
 		boolean isTrimmed = trimStackTrace(th);
 		suiteLog.error(isTrimmed ? "(Trimmed)" : "", th);
 	}
 
 	public static void fatal(Throwable th) {
 		boolean isTrimmed = trimStackTrace(th);
 		suiteLog.fatal(isTrimmed ? "(Trimmed)" : "", th);
 	}
 
 	private static boolean trimStackTrace(Throwable th) {
 		boolean isTrimmed = false;
 
 		// Trims stack trace to appropriate length
 		while (th != null) {
 			StackTraceElement st0[] = th.getStackTrace();
 
 			if (st0.length > maxStackTraceLength) {
 				StackTraceElement st1[] = new StackTraceElement[maxStackTraceLength];
 				Copy.array(st0, 0, st1, 0, maxStackTraceLength);
 				th.setStackTrace(st1);
 
 				isTrimmed = true;
 			}
 
 			th = th.getCause();
 		}
 
 		return isTrimmed;
 	}
 
 	public static <I> I proxy(Class<I> interface_, final I object) {
 		@SuppressWarnings("unchecked")
 		final Class<I> clazz = (Class<I>) object.getClass();
		final Log log = LogFactory.getLog(clazz);
 
 		InvocationHandler handler = new InvocationHandler() {
 			public Object invoke(Object proxy, Method method, Object ps[]) throws Exception {
 				String methodName = method.getName();
 				String prefix = methodName + "()\n";
				StringBuilder sb = new StringBuilder();

				sb.append(prefix);
 
 				if (ps != null)
 					for (int i = 0; i < ps.length; i++)
						DumpUtil.dump(sb, "p" + i, ps[i]);
 
				log.info(sb.toString());
 
 				try {
 					Object value = method.invoke(object, ps);
 					String rd = DumpUtil.dump("return", value);
					log.info(prefix + rd);
 					return value;
 				} catch (InvocationTargetException ite) {
 					Throwable th = ite.getTargetException();
 					boolean isTrimmed = trimStackTrace(th);
					log.error(prefix + (isTrimmed ? "(Trimmed)" : ""), th);
 					throw th instanceof Exception ? (Exception) th : ite;
 				}
 			}
 		};
 
		ClassLoader classLoader = clazz.getClassLoader();
		Class<?> classes[] = { interface_ };

 		@SuppressWarnings("unchecked")
 		I proxied = (I) Proxy.newProxyInstance(classLoader, classes, handler);
 		return proxied;
 	}
 
 }
