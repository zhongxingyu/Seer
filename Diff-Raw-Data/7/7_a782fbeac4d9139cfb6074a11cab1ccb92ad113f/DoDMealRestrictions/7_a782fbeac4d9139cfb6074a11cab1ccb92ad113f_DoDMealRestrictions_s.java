 // DoDGrantRestrictions.java
 package edu.umn.se.trap.rules.business;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import edu.umn.se.trap.data.Grant;
 import edu.umn.se.trap.data.MealExpense;
 import edu.umn.se.trap.data.MealTypeEnum;
 import edu.umn.se.trap.data.ReimbursementApp;
 import edu.umn.se.trap.data.TRAPConstants;
 import edu.umn.se.trap.db.GrantDBWrapper;
 import edu.umn.se.trap.db.KeyNotFoundException;
 import edu.umn.se.trap.db.PerDiemDBWrapper;
 import edu.umn.se.trap.exception.BusinessLogicException;
 import edu.umn.se.trap.exception.TRAPException;
 
 /**
  * This rule checks all the requirements of being reimbursed under a DoD grant. Specifically, DoD
  * grants do not fund breakfast, only fund car rental through Hertz, and air travel must be
  * domestic.
  * 
  * @author nagell2008
  * 
  */
 public class DoDMealRestrictions extends BusinessLogicRule
 {
 
     /**
      * This rule checks all the requirements of being reimbursed under a DoD grant. Specifically,
      * DoD grants do not fund breakfast, only fund car rental through Hertz, and air travel must be
      * domestic.
      */
     @Override
     public void checkRule(ReimbursementApp app) throws TRAPException
     {
         // Holds DoD grants
         List<Grant> dodGrants;
         try
         {
             dodGrants = GrantDBWrapper.getDODGrants(app.getGrantList());
         }
         catch (KeyNotFoundException e1)
         {
             throw new BusinessLogicException("Failed to get a list of available DoD grants");
         }
 
         // Keeps track of the total amount of funds available in the DoD grants
         double nonDoDGrantTotalAvailable = 0;
 
         // Holds non-DoD grants
         List<Grant> nonDoDGrants = app.getGrantList();
         nonDoDGrants.removeAll(dodGrants);
 
         /*
          * This loops breaks apart the DoD grants and the non-DoD grants
          */
         for (Grant grant : nonDoDGrants)
         {
             try
             {
                 nonDoDGrantTotalAvailable += GrantDBWrapper
                         .getGrantBalance(grant.getGrantAccount());
             }
             catch (KeyNotFoundException e)
             {
                 throw new BusinessLogicException("Could not grab grant balance from grant: "
                         + grant.getGrantAccount(), e);
             }
         }
 
         // Holds meal expenses
         List<MealExpense> mealExpenses = app.getMealExpenseList();
 
         // Holds only allowed meal expenses
         List<MealExpense> allowedMealExpenses = new ArrayList<MealExpense>();
 
         /*
          * This for-loop breaks apart allowed meal expenses vs. non-allowed meal expenses. DoD
          * grants only allow Lunch and Dinner claims.
          */
         for (MealExpense me : mealExpenses)
         {
             if (me.getType() == MealTypeEnum.BREAKFAST)
             {
 
                 // Country where the meal was claimed
                 String country = me.getCountry();
                 double perDiem = 0;
 
                 try
                 {
                     if (country.compareToIgnoreCase(TRAPConstants.USA) == 0)
                     {
                         perDiem = PerDiemDBWrapper.getDomesticPerDiemMeal(me.getCity(),
                                 me.getState(), me.getType());
 
                         // Add an allowed meal expense
                         allowedMealExpenses.add(me);
                     }
                 }
                 catch (KeyNotFoundException e)
                 {
                     throw new BusinessLogicException("Failed to find per diem for meal expense", e);
                 }
 
                 if (perDiem > nonDoDGrantTotalAvailable)
                 {
                    throw new BusinessLogicException("Meal per diem of $" + perDiem
                            + " is greater than $" + nonDoDGrantTotalAvailable
                            + " in available DoD grants");
                 }
             }
         }
 
         // All meal checks under DoD grants have passed
         return;
 
     }
 }
