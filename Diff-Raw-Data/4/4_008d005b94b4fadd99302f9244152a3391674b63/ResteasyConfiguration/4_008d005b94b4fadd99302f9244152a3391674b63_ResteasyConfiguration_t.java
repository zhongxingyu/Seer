 /*
  User: Ophir
  Date: 24/01/12
  Time: 12:14
  */
 package com.moshavit.framework;
 
 import com.fasterxml.jackson.annotation.JsonInclude;
 import com.fasterxml.jackson.databind.MapperFeature;
 import com.fasterxml.jackson.databind.ObjectMapper;
 import com.fasterxml.jackson.databind.SerializationFeature;
 import com.fasterxml.jackson.databind.module.SimpleModule;
 import com.fasterxml.jackson.datatype.guava.GuavaModule;
 import com.fasterxml.jackson.datatype.hibernate4.Hibernate4Module;
import com.fasterxml.jackson.datatype.joda.JodaModule;
 import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
 import org.jboss.resteasy.core.Dispatcher;
 import org.jboss.resteasy.plugins.providers.DefaultTextPlain;
 import org.jboss.resteasy.plugins.providers.StringTextStar;
 import org.jboss.resteasy.plugins.spring.SpringBeanProcessor;
 import org.jboss.resteasy.spi.Registry;
 import org.jboss.resteasy.spi.ResteasyDeployment;
 import org.jboss.resteasy.spi.ResteasyProviderFactory;
 import org.springframework.context.annotation.Bean;
 import org.springframework.context.annotation.Configuration;
 
 @Configuration
 public class ResteasyConfiguration {
 
 	public static final ResteasyDeployment resteasyDeployment = new ResteasyDeployment();
 
 	@Bean
 	public DefaultTextPlain defaultTextPlain() {
 		return new DefaultTextPlain();
 	}
 
 	@Bean
 	public StringTextStar stringTextStar() {
 		return new StringTextStar();
 	}
 
 	@Bean
 	public JacksonJsonProvider jacksonProvider() {
 		return new JacksonJsonProvider(objectMapper());
 	}
 
 	@Bean
 	public ObjectMapper objectMapper() {
 		ObjectMapper objectMapper = new ObjectMapper();
 		objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
 
 		SimpleModule testModule = new SimpleModule();
 		objectMapper.registerModule(testModule);
		objectMapper.registerModule(new JodaModule());
 		objectMapper.registerModule(new GuavaModule());
 		objectMapper.registerModule(new Hibernate4Module());
 
 		objectMapper.disable(SerializationFeature.WRITE_NULL_MAP_VALUES);
 		objectMapper.enable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
 		objectMapper.enable(SerializationFeature.WRITE_DATE_KEYS_AS_TIMESTAMPS);
 		objectMapper.enable(MapperFeature.CAN_OVERRIDE_ACCESS_MODIFIERS);
 
 		return objectMapper;
 	}
 
 	@Bean
 	public static SpringBeanProcessor springBeanProcessor() {
 		ResteasyDeployment deployment = resteasyDeployment();
 		deployment.setRegisterBuiltin(true);
 		deployment.start();
 		Dispatcher dispatcher = deployment.getDispatcher();
 		Registry registry = deployment.getRegistry();
 		ResteasyProviderFactory providerFactory = deployment.getProviderFactory();
 		SpringBeanProcessor springBeanProcessor = new SpringBeanProcessor(dispatcher, registry, providerFactory);
 		springBeanProcessor.setOrder(SpringBeanProcessor.LOWEST_PRECEDENCE);
 		return springBeanProcessor;
 	}
 
 	@Bean
 	public static ResteasyDeployment resteasyDeployment() {
 		return resteasyDeployment;
 	}
 }
