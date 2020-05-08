 package br.com.almana;
 
 import java.text.SimpleDateFormat;
 import java.util.concurrent.Callable;
 
 public class DateFormatCaller implements Callable<ExpectedVsActualPair> {
 
     private SimpleDateFormat simpleDateFormat;
     private RandomDate randomDate;
 
     public DateFormatCaller(SimpleDateFormat simpleDateFormat) {
         this.simpleDateFormat = simpleDateFormat;
         this.randomDate = new RandomDate();
     }
 
     @Override
     public ExpectedVsActualPair call() throws Exception {
         return new ExpectedVsActualPair(randomDate.getFormatted(), simpleDateFormat.format(randomDate.get()));
     }
 }
