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
 
 import org.hibernate.HibernateException;
 import org.hibernate.Transaction;
 
 public class HibernateSessionRequestFilter implements Filter {
 
 	private HibernateUtil hibernateUtil;
 
 	@Override
 	public void init(FilterConfig filterConfig) throws ServletException {
 		hibernateUtil = HibernateUtil.getInstance();
 	}
 
 	@Override
 	public void doFilter(ServletRequest request, ServletResponse response,
 			FilterChain chain) throws IOException, ServletException {
 
 		if (hibernateUtil.isReady()) {
			hibernateUtil.getSF().getCurrentSession().beginTransaction();
 		}
 
 		chain.doFilter(request, response);
 
 		if (hibernateUtil.isReady()) {
 			Transaction t = hibernateUtil.getSF().getCurrentSession()
 					.getTransaction();
 			try {
 				t.commit();
 			} catch (HibernateException e) {
 				try {
 					t.rollback();
					throw new HibernateException("Caught exception trying to commit transaction",e);
				}
				catch (HibernateException e2) {
					HibernateException e3 = new HibernateException("Got exception trying to rollback transaction after failure",e2);
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
