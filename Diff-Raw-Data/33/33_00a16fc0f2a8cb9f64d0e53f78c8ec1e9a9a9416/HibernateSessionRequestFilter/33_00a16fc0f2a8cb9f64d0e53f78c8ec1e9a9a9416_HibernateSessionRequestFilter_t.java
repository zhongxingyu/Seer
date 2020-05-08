 package uk.ac.cam.cl.dtg.teaching.hibernate;
 
 import java.io.IOException;
 import java.sql.Driver;
 import java.sql.DriverManager;
 import java.sql.SQLException;
 import java.util.Enumeration;
 
 import javax.servlet.Filter;
 import javax.servlet.FilterChain;
 import javax.servlet.FilterConfig;
 import javax.servlet.ServletException;
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
 
 import org.hibernate.HibernateException;
 import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
 
 public class HibernateSessionRequestFilter implements Filter {
 
 	private HibernateUtil hibernateUtil;
 
	private static Logger log = LoggerFactory.getLogger(HibernateSessionRequestFilter.class);
	
 	@Override
 	public void init(FilterConfig filterConfig) throws ServletException {
 		hibernateUtil = HibernateUtil.getInstance();
 	}
 
 	@Override
 	public void doFilter(ServletRequest request, ServletResponse response,
 			FilterChain chain) throws IOException, ServletException {
 
 		if (hibernateUtil.isReady()) {
			try {
				hibernateUtil.getSF().getCurrentSession().beginTransaction();
			} catch (HibernateException e) {
				// failed to get a new transaction - one reason for this might
				// be that the connection pool is empty
				log.error("Unable to open a database connection");
				((HttpServletResponse)response).sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,"Unable to open a database connection");
				return;
			}
 		}
 
 		chain.doFilter(request, response);
 
 		if (hibernateUtil.isReady()) {
 			Transaction t = hibernateUtil.getSF().getCurrentSession()
 					.getTransaction();
 			try {
 				t.commit();
 			} catch (HibernateException e) {
 				try {
					log.warn("Caught exception when trying to commit transaction. Rolling back.",e);
 					t.rollback();
					((HttpServletResponse)response).sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,"");
					throw new HibernateException(
							"Caught exception trying to commit transaction", e);
				} catch (HibernateException e2) {
					log.error("Failed to rollback transaction after failing to commit",e2);
					HibernateException e3 = new HibernateException(
							"Got exception trying to rollback transaction after failure",
							e2);
 					e3.addSuppressed(e);
 					throw e3;
 				}
 			}
 		}
 	}
 
 	@Override
 	public void destroy() {
 		hibernateUtil.getSession().close();
 		hibernateUtil.close();
 
 		Enumeration<Driver> drivers = DriverManager.getDrivers();
 		while (drivers.hasMoreElements()) {
 			Driver driver = drivers.nextElement();
 			try {
 				DriverManager.deregisterDriver(driver);
 			} catch (SQLException e) {
 
 			}
 
 		}
 	}
 }
