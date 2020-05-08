 package com.springapp.mvc;
 
 /**
  * Created with IntelliJ IDEA.
  * User: root
  * Date: 7/10/13
  * Time: 8:49 PM
  * To change this template use File | Settings | File Templates.
  */
 
 import com.mchange.v2.c3p0.ComboPooledDataSource;
 import java.beans.PropertyVetoException;
 
 import com.springapp.mvc.data.TokenRepository;
 import com.springapp.mvc.interceptors.CheckAuthorizedInterceptor;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.context.annotation.Bean;
 import org.springframework.context.annotation.ComponentScan;
 import org.springframework.context.annotation.Configuration;
 import org.springframework.context.annotation.PropertySource;
 import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericToStringSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
 import org.springframework.jdbc.core.JdbcTemplate;
 import org.springframework.transaction.annotation.EnableTransactionManagement;
 import org.springframework.web.servlet.HandlerInterceptor;
 import org.springframework.web.servlet.ModelAndView;
 import org.springframework.web.servlet.config.annotation.EnableWebMvc;
 import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
 import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
 import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
 import com.springapp.mvc.data.UserRepository;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 @Configuration
 @ComponentScan(basePackages = "com.springapp.mvc")
 @PropertySource(value = "classpath:/application.properties")
 @EnableWebMvc
 @EnableTransactionManagement
 public class AppConfig extends WebMvcConfigurerAdapter{
 
     private TokenRepository tokenRepository;
 
     @Bean
    JedisConnectionFactory jedisConnectionFactory() {
        return new JedisConnectionFactory();
    }

    @Bean
    RedisTemplate< String, Object > redisTemplate() {
        final RedisTemplate< String, Object > template =  new RedisTemplate< String, Object >();
        template.setConnectionFactory(jedisConnectionFactory());
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new GenericToStringSerializer<Object>(Object.class));
        template.setValueSerializer( new GenericToStringSerializer< Object >( Object.class ) );
        return template;
    }

    @Bean
     public JdbcTemplate jdbcTemplate(@Value("${db.driver.class}") String driverClass,
                                      @Value("${db.url}") String url,
                                      @Value("${db.user}") String username,
                                      @Value("${db.password}") String password) throws PropertyVetoException {
         ComboPooledDataSource dataSource = new ComboPooledDataSource();
         dataSource.setDriverClass(driverClass);
         dataSource.setJdbcUrl(url);
         dataSource.setUser(username);
         dataSource.setPassword(password);
         return new JdbcTemplate(dataSource);
     }
 
     @Bean
     public static PropertySourcesPlaceholderConfigurer propertiesConfigurer() {
         return new PropertySourcesPlaceholderConfigurer();
     }
 
     public void addInterceptors(InterceptorRegistry registry){
         CheckAuthorizedInterceptor interceptor = new CheckAuthorizedInterceptor(tokenRepository);
         registry.addInterceptor(interceptor).addPathPatterns("MiniTwitter/statuses/update");
     }
 }
