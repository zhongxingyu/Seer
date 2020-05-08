 package org.apache.myfaces.javaloader.test;
 
 public class TestBean2 {
     String sayHello = "hello world ";
    String hello2 = "hello from added attribute hello2";
    String hello3 = "hello from  added attribute hello3";
 
     public String getSayHello() {
        return "Hello from the dynamic bean TestBean2  - " + TestClass2.hello2 +" - "+ hello3;
     }
 
     public String getSayHello2() {
         return hello2;
     }
 
     public void setSayHello(String hello) {
         this.sayHello = hello;
         System.out.println("hello world");
     }
 
 }
