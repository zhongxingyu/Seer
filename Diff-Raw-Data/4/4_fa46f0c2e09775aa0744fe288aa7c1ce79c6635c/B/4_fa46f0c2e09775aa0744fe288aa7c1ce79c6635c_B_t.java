 package dkwestbr.spring.autowired.example.stringGetter.impl;
 
 import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
 import org.springframework.stereotype.Component;
 import org.springframework.test.context.ActiveProfiles;
 
 import dkwestbr.spring.autowired.example.IStringGetter;
 
 @Component
@Profile("test")
 public class B implements IStringGetter {
 
 	@Value("${my.property}")
 	private String configValue;
 	
 	@Override
 	public String getItGood() {
 		return String.format("I am an B: %s", configValue);
 	}
 }
