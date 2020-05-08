 /* vim: set ts=2 et sw=2 cindent fo=qroca: */
 
 package com.globant.katari.shindig;
 
 import org.apache.commons.lang.Validate;
 
 import org.apache.shindig.auth.SecurityTokenDecoder;
 import org.apache.shindig.common.crypto.BlobCrypter;
 import org.apache.shindig.config.ContainerConfig;
 import org.apache.shindig.gadgets.http.HttpFetcher;
 import org.apache.shindig.gadgets.render.DefaultServiceFetcher;
import org.apache.shindig.gadgets.render.RenderingGadgetRewriter;
 import org.apache.shindig.social.opensocial.oauth.OAuthDataStore;
 import org.apache.shindig.social.opensocial.spi.ActivityService;
 import org.apache.shindig.social.opensocial.spi.AppDataService;
 import org.apache.shindig.social.opensocial.spi.MessageService;
 import org.apache.shindig.social.opensocial.spi.PersonService;
 import org.apache.shindig.social.sample.oauth.SampleOAuthDataStore;
 import org.apache.shindig.social.sample.spi.JsonDbOpensocialService;
 
 import com.google.inject.Provides;
 import com.google.inject.name.Names;
 
 import com.globant.katari.shindig.application.FakeUserHttpFetcher;
 import com.globant.katari.shindig.SpringJsonContainerConfig;
 
 /** Bindings for katari implementation of shindig services.
  *
  * This Guice module implements ApplicationContextAware to be notified by the
  * GuiceInitializerListener of the spring application context.
  */
 public class ShindigServicesModule extends ShindigSocialApiGuiceModule {
 
   /** The person service implementation, never null.
    */
   private final PersonService personService;
 
   /** The implementation of the activity service, never null.
    */
   private final ActivityService activityService;
 
   /** Security token decoder.
    */
   private final SecurityTokenDecoder tokenDecoder;
 
   /** Crypter implementation to crypt the tokens.
    */
   private final BlobCrypter crypter;
 
   private final String contextPath;
 
   /** Constructor.
    *
    * @param personServiceImpl The implementation of the person service. It
    * cannot be null.
    *
    * @param activityService The implementation of the activity service. It
    * cannot be null.
    *
    * @param decoder The implementation of the SecurityTokenDecoder. It cannot
    * be null.
    *
    * @param blobCrypter The implementation of the BlobCrypter. It cannot be null.
    *
    * @param theContextPath The web context path where the application will be
    * deployed. This cannot be determined automatically because shindig needs it
    * very early in the startup cycle. It cannot be null.
    */
   public ShindigServicesModule(final PersonService personServiceImpl,
       final ActivityService activityServiceImpl,
       final SecurityTokenDecoder decoder, final BlobCrypter blobCrypter,
       final String theContextPath) {
 
     Validate.notNull(personServiceImpl,
         "The person service implementation cannot be null.");
     Validate.notNull(activityServiceImpl,
         "The activity service implementation cannot be null.");
     Validate.notNull(decoder,
         "The token decoder implementation cannot be null.");
     Validate.notNull(blobCrypter,
         "The blob crypter implementation cannot be null.");
     Validate.notNull(theContextPath, "The context path cannot be null.");
 
     personService = personServiceImpl;
     activityService = activityServiceImpl;
     tokenDecoder = decoder;
     crypter = blobCrypter;
     contextPath = theContextPath;
   }
 
   /** Wires the shindig services to the corresponding Katari implementations.
    *
    * Katari services are obtained from the application context.
    *
    * This implementation only wires the ActivityService and PersonService to
    * Katari, the other services get wired to the shindig provided mock.
    */
   @Override
   protected void configure() {
     super.configure();
 
     bind(String.class).annotatedWith(Names.named("katari.contextPath"))
         .toInstance(contextPath);
     bind(ContainerConfig.class).to(SpringJsonContainerConfig.class);
 
     bind(PersonService.class).toInstance(personService);
     bind(ActivityService.class).toInstance(activityService);
     bind(SecurityTokenDecoder.class).toInstance(tokenDecoder);
     bind(BlobCrypter.class).toInstance(crypter);
 
     bind(String.class).annotatedWith(Names.named("shindig.canonical.json.db"))
         .toInstance("sampledata/canonicaldb.json");
     bind(AppDataService.class).to(JsonDbOpensocialService.class);
     bind(MessageService.class).to(JsonDbOpensocialService.class);
 
     bind(OAuthDataStore.class).to(SampleOAuthDataStore.class);
   }
 
   /** Wires a DefaultServiceFetcher to a special HttpFetcher that hardcodes a
    * user token so that the request to list rpc operations do not fail.
    *
    * @param config The container config, provided by shindig.
    *
    * @param fetcher The original fetcher. It cannot be null.
    *
    * @param blobCrypter The implementation of the BlobCrypter. It cannot be
    * null.
    */
   @Provides
   DefaultServiceFetcher provideServiceFetcher(final ContainerConfig config,
       final HttpFetcher fetcher, final BlobCrypter crypter) {
     DefaultServiceFetcher serviceFetcher = new DefaultServiceFetcher(config,
           new FakeUserHttpFetcher(config, fetcher, crypter));
     return serviceFetcher;
   }
 }
 
