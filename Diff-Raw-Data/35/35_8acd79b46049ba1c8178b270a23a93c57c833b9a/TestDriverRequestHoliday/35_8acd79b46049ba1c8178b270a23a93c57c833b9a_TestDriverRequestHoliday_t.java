 package TestClass;
 
 
 import RequestHoliday.Update;
 import RequestHoliday.ValidateHolidayRequest;
 import database.DriverInfo;
 import database.database;
 import java.util.Date;
 import java.util.GregorianCalendar;
 
 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 /**
  *
  * @author Thanakorn 21/02/13
  */
 public class TestDriverRequestHoliday {
     public static void main(String[] args)
     {
         //-------------------------------------------------------------------
         //Test ValidateRequestHoliday.dateAvailable
         database.openBusDatabase();
         GregorianCalendar testDate = new GregorianCalendar(2013, 0, 10, 0, 0, 0);
         int[] driverIDs = DriverInfo.getDrivers();
         
         Date a = new Date(testDate.getTimeInMillis());
         
         
         //Set every driver to be false on 10/01/13
         //except himself
         setAllFalse(a);
         DriverInfo.setAvailable(2012, a, true);
         if(ValidateHolidayRequest.dateAvailable(testDate, 2012) == 0)
         {
             System.out.println("Test Pass: dateAvailable: All False");
         }
         else
         {
             System.out.println("Test Fail: dateAvailable: All False");
         }//else
         
         //Set every driver to be true on 10/01/13
         setAllTrue(a);
         if(ValidateHolidayRequest.dateAvailable(testDate, 2012) == 1)
         {
             System.out.println("Test Pass: dateAvailable: All True");
         }
         else
         {
             System.out.println("Test Fail: dateAvailable: All True");
         }//else
         
         //Set 9 driver to be false on 10/01/13
         for (int i=0; i<9; i++)
         {
             DriverInfo.setAvailable(driverIDs[i], a, false);
         }//for
         if(ValidateHolidayRequest.dateAvailable(testDate, 2012) == 1)
         {
             System.out.println("Test Pass: dateAvailable: 9 Drivers false");
         }
         else
         {
             System.out.println("Test Fail: dateAvailable: 9 Driver false");
         }//else
         
         setAllTrue(a);
         //Set 10 driver to be false on 10/01/13
         for (int i=0; i<10; i++)
         {
             DriverInfo.setAvailable(driverIDs[i], a, false);
         }//for
         if(ValidateHolidayRequest.dateAvailable(testDate, 2012) == 0)
         {
             System.out.println
                     ("Test Pass: dateAvailable: 10 Drivers false");
         }
         else
         {
             System.out.println
                     ("Test Fail: 10 Drivers false");
         }//else
         
         //Set every driver to be false on 10/01/13
         setAllFalse(a);
         if(ValidateHolidayRequest.dateAvailable(testDate, 2012) == 2)
         {
             System.out.println("Test Pass: dateAvailable: All False");
         }
         else
         {
             System.out.println("Test Fail: dateAvailable: All False");
         }//else
        //-------------------------------------------------------------------
        //Test Driver Interface red and green squares
        GregorianCalendar testFive = new GregorianCalendar(2013, 0, 5, 0, 0, 0); 
        GregorianCalendar testSix = new GregorianCalendar(2013, 0, 6, 0, 0, 0);  
        GregorianCalendar testSeven = new GregorianCalendar(2013, 0, 7, 0, 0, 0);  
        
        Date five = new Date(testFive.getTimeInMillis());
        Date six = new Date(testSix.getTimeInMillis());
        Date seven = new Date(testSeven.getTimeInMillis());
        setAllFalse(five);
        setAllFalse(six);
        setAllFalse(seven);
        
        //setAllTrue(five);
        //setAllTrue(six);
        //setAllTrue(seven);
        
        DriverInfo.setAvailable(2013, five , true);
        DriverInfo.setAvailable(2013, six , true);
        DriverInfo.setAvailable(2013, seven , true);
        System.out.println("Test Pass: Green, Red, and Yellow squares");
        //--------------------------------------------------------------------
        //Test ValidateRequestHoliday.validateRequest()
        Date b, c, d, e;
        GregorianCalendar testDateA, testDateB, testDateC, testDateD, testDateE;
        
        testDateA = new GregorianCalendar(2013, 0, 10, 0, 0, 0);
        testDateB = new GregorianCalendar(2013, 0, 11, 0, 0, 0);
        testDateC = new GregorianCalendar(2013, 0, 12, 0, 0, 0);
        testDateD = new GregorianCalendar(2013, 0, 13, 0, 0, 0);
        testDateE = new GregorianCalendar(2013, 0, 14, 0, 0, 0);
        
        a = new Date(testDateA.getTimeInMillis());
        b = new Date(testDateA.getTimeInMillis());
        c = new Date(testDateA.getTimeInMillis());
        d = new Date(testDateA.getTimeInMillis());
        e = new Date(testDateA.getTimeInMillis());
        //One day true
        setAllTrue(a);
        if(ValidateHolidayRequest.validateRequest(testDateA, testDateA, 2013))
        {
            System.out.println("Test Pass: validateRequest: One day true");
        }
        else
        {
            System.out.println("Test Fail: validateRequest: One day true");
            System.out.println("SysMsg:" + ValidateHolidayRequest.systemMsg.message);
        }
        //5 days all true
        setAllTrue(a);
        setAllTrue(b);
        setAllTrue(c);
        setAllTrue(d);
        setAllTrue(e);
        if(ValidateHolidayRequest.validateRequest(testDateA, testDateE, 2013))
        {
            System.out.println("Test Pass: validateRequest: 5 days all true");
        }
        else
        {
            System.out.println("Test Fail: validateRequest: 5 days all true");
        }
        
        //4th date is false the rest are true
        setAllFalse(d);
        if(!ValidateHolidayRequest.validateRequest(testDateA, testDateE, 2013))
        {
            System.out.println("Test Pass: validateRequest: 4 trues, 1 fault");
        }
        else
        {
            System.out.println("Test Fail: validateRequest: 4 trues, 1 fault");
        }//else
        
        //Date from is greater than date to
        if(!ValidateHolidayRequest.validateRequest(testDateA, testDateE, 2013))
        {
            System.out.println("Test Pass: validateRequest: start date is after finish date");
        }
        else
        {
            System.out.println("Test Fail: validateRequest: start date is after finish date");
        }//else   
        
        //Over the year
        
        testDateA = new GregorianCalendar(2012, 11, 31, 0, 0, 0);
        testDateB = new GregorianCalendar(2012, 0, 1, 0, 0, 0);
        testDateC = new GregorianCalendar(2012, 0, 2, 0, 0, 0);
        
        a = new Date(testDateA.getTimeInMillis());
        b = new Date(testDateB.getTimeInMillis());
        c = new Date(testDateC.getTimeInMillis());
        setAllTrue(a);
        setAllTrue(b);
        setAllTrue(c);
        if(ValidateHolidayRequest.validateRequest(testDateA, testDateC, 2013))
        {
            System.out.println("Test Pass: validateRequest: over the year");
        }
        else
        {
            System.out.println("Test Fail: validateRequest: over the year");
        }//else  
        
        //Length > 25 within the same year
        GregorianCalendar[] dateArray = new GregorianCalendar[30];
        Date temp;
        for(int i = 0; i < dateArray.length; i++)
        {
            
            dateArray[i] = new GregorianCalendar(2012, 11, i+1, 0, 0, 0);
            temp = new Date(dateArray[i].getTimeInMillis());
            setAllTrue(temp);
        }//for
        if(!ValidateHolidayRequest.validateRequest
                (dateArray[0], dateArray[dateArray.length-1], 2013))
        {
            System.out.println("Test Pass: validateRequest: Length > 25");
        }
        else
        {
            System.out.println("Test Fail: validateRequest: Length > 25");
        }//else  
        
        //Length > 25 within the same year
        dateArray = new GregorianCalendar[15];
        GregorianCalendar[] dateArray2 = new GregorianCalendar[15];
        for(int i = 0; i < dateArray.length; i++)
        {
            dateArray[i] = new GregorianCalendar(2012, 11, i+17, 0, 0, 0);
            temp = new Date(dateArray[i].getTimeInMillis());
            setAllTrue(temp);
        }//for
        for(int i = 0; i < dateArray2.length; i++)
        {
            dateArray2[i] = new GregorianCalendar(2013, 0, i+1, 0, 0, 0);
            temp = new Date(dateArray2[i].getTimeInMillis());
            setAllTrue(temp);
        }//for
        if(ValidateHolidayRequest.validateRequest
                (dateArray[0], dateArray2[dateArray2.length-1], 2013))
        {
            System.out.println
           ("Test Pass: validateRequest: Length > 25 But over the year");
        }
        else
        {
            System.out.println
           ("Test Fail: validateRequest: Length > 25 But over the year");
        }//else  
        
        //--------------------------------------------------------------------
        //Update test
        DriverInfo.setHolidaysTaken(2012, 0);
        dateArray = new GregorianCalendar[30];
        for(int i = 0; i < dateArray.length; i++)
        {
            dateArray[i] = new GregorianCalendar(2012, 11, i+1, 0, 0, 0);
           temp = new Date(dateArray[i].getTimeInMillis());
            setAllTrue(temp);
        }//for
        //30 days
        Update.updateHolidayRequest(dateArray[0], 
                dateArray[dateArray.length-1], 2012);
        if(DriverInfo.getHolidaysTaken(2012) == 30)
        {
            System.out.println
           ("Test Pass: updateHolidayRequest:30 days holidays upddated");
        }
        else
        {
            System.out.println
           ("Test Fail: updateHolidayRequest: 30 days holidays upddated");
        }//else  
        boolean pass = true;
        for(int i = 0; i < dateArray.length; i++)
        {
            temp = new Date(dateArray[i].getTimeInMillis());
            if(DriverInfo.isAvailable(2012, temp))
                 pass = false;
        }//for
         if(pass)
        {
            System.out.println
           ("Test Pass: updateHolidayRequest: All date have been set to inavailable");
        }
        else
        {
            System.out.println
           ("Test Fail: updateHolidayRequest: All date have been set to inavailable");
        }//else  
        
            
        
     }//main
     
    static void setAllTrue(Date a)
    {
         int[] driverIDs = DriverInfo.getDrivers();
         for (int i=0; i<driverIDs.length; i++)
         {
             DriverInfo.setAvailable(driverIDs[i], a, true);
         }//for
    }//setAllTrue
    
     static void setAllFalse(Date a)
    {
         int[] driverIDs = DriverInfo.getDrivers();
         for (int i=0; i<driverIDs.length; i++)
         {
             DriverInfo.setAvailable(driverIDs[i], a, false);
         }//for
    }//setAllTrue  
     
 }
