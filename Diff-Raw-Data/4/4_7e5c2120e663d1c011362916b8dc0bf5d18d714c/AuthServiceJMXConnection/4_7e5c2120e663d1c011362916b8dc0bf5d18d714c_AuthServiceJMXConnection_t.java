 package com.jclarity.had_one_dismissal.jmx;
 
 import com.jclarity.crud_common.api.AuthServicePerformanceVariablesMXBean;
 
 public class AuthServiceJMXConnection extends JMXConnection {
    private static final String JMX_AUTH_SERVER_HOST = System.getProperty("JMX_AUTH_SERVER_HOST", "127.0.0.1");
     private static final int JMX_AUTH_SERVER_PORT = Integer.parseInt(System.getProperty("JMX_AUTH_SERVER_PORT", "1100"));
 
     public AuthServiceJMXConnection() {
        super(JMX_AUTH_SERVER_HOST, JMX_AUTH_SERVER_PORT);
     }
 
     public AuthServicePerformanceVariablesMXBean getAuthServicePerformanceVariables() throws JMXConnectionException {
         return getBean(AuthServicePerformanceVariablesMXBean.ADDRESS, AuthServicePerformanceVariablesMXBean.class);
     }
 
 }
