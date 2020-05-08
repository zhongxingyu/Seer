 package az.his.filters;
 
 import az.his.DBManager;
 import az.his.DBUtil;
 import org.hibernate.Session;
 import org.hibernate.Transaction;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import javax.servlet.*;
 import java.io.IOException;
 
 /**
  * Manages transaction. Not annotated because processing order is significant and set in web.xml
  */
 public class TransactionFilter implements Filter {
     public void destroy() {
     }
 
     public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws ServletException, IOException {
         if (Boolean.TRUE.equals(req.getAttribute(StaticFilter.STATIC_CONTENT))) {
             chain.doFilter(req, resp);
         } else {
             Session sess = DBUtil.openSession();
             Transaction tx = null;
 
             try {
                 if(!sess.isConnected()) sess.reconnect(sess.disconnect());
                 tx = sess.beginTransaction();
                req.setAttribute("DBM", new DBManager(sess));
                 chain.doFilter(req, resp);
                 tx.commit();
             } catch (Exception e) {
                 Logger log = LoggerFactory.getLogger(this.getClass());
                 log.error("Transaction error", e);
                 if (tx != null) tx.rollback();
                throw new ServletException(e);
             } finally {
                 sess.close();
             }
         }
     }
 
     public void init(FilterConfig config) throws ServletException {
 
     }
 
 }
