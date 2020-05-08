 package testspring.testspring;
 
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.support.ClassPathXmlApplicationContext;
 
 public class ConsumerMain {
 
 	public static void main(String args[])
 	{
 		ApplicationContext context = new ClassPathXmlApplicationContext("consumer.xml");
 		System.out.println("From consumer...");	
		RuntimeRecordHandler handler =(RuntimeRecordHandler) context.getBean("runtimeRecordHandler");
		RuntimeRecordJDBCTemplate runtimeJDBCTemplate = 
			      (RuntimeRecordJDBCTemplate)context.getBean("JDBCTemplate");
 	}
 }
