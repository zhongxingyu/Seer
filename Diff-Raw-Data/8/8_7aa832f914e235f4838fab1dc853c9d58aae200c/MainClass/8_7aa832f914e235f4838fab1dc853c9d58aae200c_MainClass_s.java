 
 import javax.mail.MessagingException;
 
 import br.unifesp.profetas.business.MailProfetas;
 
 public class MainClass {
 
 	public static void main(String[] args) {
 		/*ApplicationContext appContext = new ClassPathXmlApplicationContext("META-INF/spring/app-context.xml");
 		
 		Login login = (Login) appContext.getBean(LoginImpl.class);
 		UserDTO u = login.getUserByUsername("sirghost@gmail.com");
 		
		System.out.println("user: " + u.getFullName());*/
		try {
			new MailProfetas().sendEmail("sirghost@gmail.com", "ups!", "no mames");
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
 
 	}
 }
