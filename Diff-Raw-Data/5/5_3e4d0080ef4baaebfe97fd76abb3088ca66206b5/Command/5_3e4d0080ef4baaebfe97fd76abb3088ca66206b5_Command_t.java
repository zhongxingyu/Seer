 package com.github.judoole.webdav;
 
 import org.apache.tools.ant.ProjectComponent;
 
 public abstract class Command extends ProjectComponent {
     protected String user;
     protected String password;
 
     protected abstract void execute() throws Exception;
 
    @Override
    public void log(String msg) {
        System.out.println(msg);
    }

     /**
      * Set webdav user name
      *
      * @param user
      */
     void setUser(String user) {
         this.user = user;
     }
 
     /**
      * Set webdav password
      *
      * @param password
      */
     void setPassword(String password) {
         this.password = password;
     }
 }
