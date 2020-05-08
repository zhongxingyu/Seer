 package com.adaptiweb.tools;
 
 import javax.activation.DataSource;
 import javax.naming.Context;
 import javax.naming.InitialContext;
 import javax.naming.NameAlreadyBoundException;
 import javax.naming.NamingException;
 import javax.naming.NoInitialContextException;
 
 import org.apache.tomcat.dbcp.dbcp.BasicDataSource;
 
 import com.adaptiweb.tools.dbtest.DbTestJNDIDataSource;
 
 
 public final class JndiUtils {
 	
 	private JndiUtils() {}
 
 	public static void unbind(String jndiName) {
 		try {
 			InitialContext initialContext = new InitialContext();
 			initialContext.unbind(jndiName);
 		} catch (NoInitialContextException ignore) {
 		} catch (NamingException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	/**
 	 * Load {@link DataSource} configuration from class annotated by {@link DbTestJNDIDataSource}
 	 * and register it to JNDI name-space.
 	 */
 	public static void setUpJNDIDataSource(Class<?> annotatedClass) {
 		if (!annotatedClass.isAnnotationPresent(DbTestJNDIDataSource.class))
 			throw new IllegalArgumentException("Class " + annotatedClass + " must be annotated by annotation " + DbTestJNDIDataSource.class);
 		setUpJNDIDataSource(annotatedClass.getAnnotation(DbTestJNDIDataSource.class));
 	}
 
 	public static void setUpJNDIDataSource(DbTestJNDIDataSource conf) {
 		BasicDataSource dataSource = new BasicDataSource();
 		dataSource.setDriverClassName(conf.driver().getName());
 		dataSource.setUrl(conf.url());
 		
 		if (conf.username().length() > 0) dataSource.setUsername(conf.username());
 		if (conf.password().length() > 0) dataSource.setPassword(conf.password());
 		
 		JndiUtils.bind(conf.jndiName(), dataSource);
 	}
 
 	public static void bind(String jndiName, Object value) {
 		try {
 			System.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.apache.naming.java.javaURLContextFactory");
 			System.setProperty(Context.URL_PKG_PREFIXES, "org.apache.naming");
 
 			InitialContext initialContext = new InitialContext();
 
 			for (int start = 0, index; (index = jndiName.indexOf('/', start)) != -1; start = index + 1) {
 				try {
 					initialContext.createSubcontext(jndiName.substring(0, index));
 				} catch (NameAlreadyBoundException ignore) {}
 			}

			initialContext.bind(jndiName, value);
		} catch (NameAlreadyBoundException ignore) {
 		} catch (NamingException e) {
 			throw new RuntimeException(e);
 		}
 	}
 	
 }
