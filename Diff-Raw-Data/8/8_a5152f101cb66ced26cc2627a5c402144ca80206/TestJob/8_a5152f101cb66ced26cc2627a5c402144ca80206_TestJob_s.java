 package jobs;
 
 import java.util.Date;
 
@AkkaJob(cronExpression = "* 0/5 * * * ?")
 public class TestJob extends AbstractJob {
 
   public TestJob() throws Exception {
     super();
     setRestartOnFail(false);
   }
 
   @Override
   public void runInternal() {
     System.out.println("I was called at: " + new Date());
   }
 
 }
