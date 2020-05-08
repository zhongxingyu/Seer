 package polly.core.mail;
 
 import org.apache.log4j.Logger;
 
import de.skuzzle.polly.sdk.Configuration;

import polly.configuration.ConfigurationProviderImpl;
 import polly.core.mail.senders.MailSender;
 import polly.moduleloader.AbstractProvider;
 import polly.moduleloader.ModuleLoader;
 import polly.moduleloader.SetupException;
 import polly.moduleloader.annotations.Module;
 import polly.moduleloader.annotations.Require;
 
 
@Module(requires = {
    @Require(component = MailConfig.class),
    @Require(component = ConfigurationProviderImpl.class)
})
 public class ErrorMailerProvider extends AbstractProvider {
 
    private final static Logger logger = Logger
        .getLogger(ErrorMailerProvider.class.getName());
     
     public ErrorMailerProvider(ModuleLoader loader) {
         super("ERROR_MAILER_PROVIDER", loader, false);
     }
     
     
 
     @Override
     public void setup() throws SetupException {
         MailConfig mailCfg = this.requireNow(MailConfig.class, true);
        ConfigurationProviderImpl configManager = 
                this.requireNow(ConfigurationProviderImpl.class, true);
        
        if (configManager.getRootConfiguration().readBoolean(Configuration.DEBUG_MODE)) {
            logger.warn("Disabling Error Mailer because polly debug mode is enabled");
            return;
        }
        
         int delay = Integer.parseInt(
             mailCfg.readString(MailConfig.MAIL_DELAY));
         MailSender sender = mailCfg.getSender();
         EMailLogAppender appender = new EMailLogAppender(
             sender, mailCfg.getLevel(), delay, 
             new EMailLogFormatter(mailCfg.getLevel()));
         
         Logger.getRootLogger().addAppender(appender);
     }
 
 }
