 package fr.keemto.provider.exchange.importer;
 
 import microsoft.exchange.webservices.data.*;
 import org.junit.Before;
 import org.junit.Test;
 
 import java.net.URI;
 import java.util.Date;
 import java.util.Enumeration;
 import java.util.List;
 
 import static org.hamcrest.CoreMatchers.*;
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.Matchers.equalTo;
 import static org.junit.Assume.assumeThat;
 
 public class EmailExchangeServiceIT {
    private Enumeration<List<EmailMessage>> emails;
 
     @Before
     public void setUp() throws Exception {
 
         String login = System.getProperty("provider.ews.xebia.login");
         assumeThat(login, notNullValue());
         String pwd = System.getProperty("provider.ews.xebia.password");
 
         ExchangeService exchangeService = new ExchangeService(ExchangeVersion.Exchange2010_SP1);
         exchangeService.setUrl(new URI("https://owa.exchange-login.net/EWS/Exchange.asmx"));
         ExchangeCredentials credentials = new WebCredentials(login, pwd);
         exchangeService.setCredentials(credentials);
 
        ExchangeServiceWrapper serviceWrapper = new ExchangeServiceWrapper(exchangeService);
        emails = serviceWrapper.getEmailsNewerThan(0L);
     }
 
     @Test
     public void shouldReturnMailsSinceEpoch() throws Exception {
         List<EmailMessage> messages = emails.nextElement();
 
         assertThat(messages.isEmpty(), is(false));
         assertThat(messages.size(), equalTo(100));
         EmailMessage emailMessage = messages.get(0);
         assertThat(emailMessage.getBody(), notNullValue());
         assertThat(emailMessage.getId(), notNullValue());
         assertThat(emailMessage.getSender(), notNullValue());
         assertThat(emailMessage.getSubject(), notNullValue());
         assertThat(emailMessage.getDateTimeCreated(), not(equalTo(new Date())));
 
         assertThat(emails.hasMoreElements(), is(true));
     }
 
 }
