 /*
  * To change this license header, choose License Headers in Project Properties.
  * To change this template file, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package com.hackathon.gavin.statement;
 
 import com.espertech.esper.client.EPAdministrator;
 import com.espertech.esper.client.EPStatement;
 import com.espertech.esper.client.UpdateListener;
 import com.hackathon.gavin.account.MyAccount;
 import com.hackathon.gavin.account.MyAccountGenerator;
 
 /**
  *
  * @author Gavin
  */
 public class SellOrderStatement {
     
     private EPStatement statement;
    static MyAccount myAccountInfo = MyAccountGenerator.createMyAccount();
     public SellOrderStatement(EPAdministrator admin){
     
         String EPLStatement;
        EPLStatement = "select bid from CurrencyEvent where bid > "+ myAccountInfo.getPrice();
         
         statement = admin.createEPL(EPLStatement);
     }
     
       public void addListener(UpdateListener listener) {
         statement.addListener(listener);
     }
 }
