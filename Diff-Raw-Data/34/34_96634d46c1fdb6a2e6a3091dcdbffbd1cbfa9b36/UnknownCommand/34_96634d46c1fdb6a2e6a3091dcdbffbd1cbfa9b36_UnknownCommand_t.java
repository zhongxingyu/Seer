 package com.formation.spring.commands;
 
 import org.springframework.beans.factory.annotation.Autowired;
 
 /**
  * Created with IntelliJ IDEA.
  * User: notdryft
  * Date: 5/13/13
  * Time: 11:36 PM
  */
 public class UnknownCommand implements Command {
 
     private Receiver receiver;
 
     @Autowired
     public UnknownCommand(Receiver receiver) {
         this.receiver = receiver;
     }
 
     @Override
     public int getMaxParameters() {
         return 0;
     }
 
     @Override
     public boolean setParameters(String[] args) {
         return false;
     }
 
     @Override
     public void execute() {
        System.out.println("! Unknown command");
     }
 }
