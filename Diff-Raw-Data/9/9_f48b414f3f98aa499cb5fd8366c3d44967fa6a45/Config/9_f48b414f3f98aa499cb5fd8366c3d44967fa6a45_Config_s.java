 package br.com.javasign.config;
 
 import org.bouncycastle.jce.provider.BouncyCastleProvider;
 
 import java.security.Provider;
 import java.security.Security;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Weslley Andrade
  * Date: 28/07/13
  * Time: 11:35
  */
public class Config {
     private static Config config;
     private String provider;
 
     private Config(){
 
     }
 
     public static Config getInstance(){
         if (config == null) {
             config = new Config().lazyConfig();
         }
         return config;
     }
 
 
     public Config addProvider(Provider provider, String strProvider){
         Security.addProvider(provider);
         this.provider = strProvider;
         return this;
     }
 
     private Config lazyConfig(){
         this.addProvider(new BouncyCastleProvider(),"BC");
         return this;
     }
 
     public String getProvider() {
         return provider;
     }
 }
