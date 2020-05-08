 package com.jclarity.had_one_dismissal.jmx;
 
 import com.jclarity.crud_common.api.AuthServicePerformanceVariablesMXBean;
 
 public class AuthServiceJMXConnection extends JMXConnection {
     private static final int JMX_AUTH_SERVER_PORT = Integer.parseInt(System.getProperty("JMX_AUTH_SERVER_PORT", "1100"));
 
     public AuthServiceJMXConnection() {
        super(JMX_SERVER_HOST, JMX_AUTH_SERVER_PORT);
     }
 
     public AuthServicePerformanceVariablesMXBean getAuthServicePerformanceVariables() throws JMXConnectionException {
         return getBean(AuthServicePerformanceVariablesMXBean.ADDRESS, AuthServicePerformanceVariablesMXBean.class);
     }
 
 }
