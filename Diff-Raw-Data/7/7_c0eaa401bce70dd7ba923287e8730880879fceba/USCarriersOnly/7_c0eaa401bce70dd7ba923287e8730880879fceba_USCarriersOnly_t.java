 // USCarriersOnly.java
 package edu.umn.se.trap.rules.business;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import edu.umn.se.trap.data.ReimbursementApp;
 import edu.umn.se.trap.data.TransportationCarrierEnum;
 import edu.umn.se.trap.data.TransportationExpense;
 import edu.umn.se.trap.data.TransportationTypeEnum;
 import edu.umn.se.trap.exception.BusinessLogicException;
 import edu.umn.se.trap.exception.TRAPException;
 
 /**
  * @author Dylan
  * 
  */
 public class USCarriersOnly extends BusinessLogicRule
 {
     private static Logger log = LoggerFactory.getLogger(USCarriersOnly.class);
 
     List<String> USCarriers = new ArrayList<String>();

    public USCarriersOnly()
    {
        USCarriers.add("Alaska Airlines");
    }
 
     @Override
     public void checkRule(ReimbursementApp app) throws TRAPException
     {
         List<TransportationExpense> transportationExpenses = app.getTransportationExpenseList();
 
         for (TransportationExpense expense : transportationExpenses)
         {
             TransportationTypeEnum type = expense.getTransportationType();
             if (type == TransportationTypeEnum.AIR)
             {
                 String carrier = expense.getTransportationCarrier();
                 try
                 {
                     TransportationCarrierEnum.valueOf(carrier);
                 }
                 catch (IllegalArgumentException e)
                 {
                     throw new BusinessLogicException(String.format(
                             "Air carrier is not recognized or not a US based carrier (%s).",
                             carrier), e);
                 }
             }
         }
     }
 }
