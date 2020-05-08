 package cz.cvut.fel.bupro.config;
 
 import java.nio.charset.Charset;
 import java.util.List;
 import java.util.Locale;
 
 import org.springframework.beans.factory.annotation.Qualifier;
 import org.springframework.context.MessageSource;
 import org.springframework.context.annotation.Bean;
 import org.springframework.context.annotation.Configuration;
 import org.springframework.context.support.ResourceBundleMessageSource;
 import org.springframework.core.convert.support.DefaultConversionService;
 import org.springframework.data.repository.support.DomainClassConverter;
 import org.springframework.data.web.PageableArgumentResolver;
 import org.springframework.format.support.FormattingConversionService;
 import org.springframework.http.converter.HttpMessageConverter;
 import org.springframework.http.converter.StringHttpMessageConverter;
 import org.springframework.web.method.support.HandlerMethodArgumentResolver;
 import org.springframework.web.servlet.LocaleResolver;
 import org.springframework.web.servlet.ViewResolver;
 import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
 import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
 import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
 import org.springframework.web.servlet.i18n.CookieLocaleResolver;
 import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
 import org.springframework.web.servlet.mvc.method.annotation.ServletWebArgumentResolverAdapter;
 import org.thymeleaf.extras.springsecurity3.dialect.SpringSecurityDialect;
 import org.thymeleaf.spring3.SpringTemplateEngine;
 import org.thymeleaf.spring3.view.ThymeleafViewResolver;
 import org.thymeleaf.templateresolver.ServletContextTemplateResolver;
 import org.thymeleaf.templateresolver.TemplateResolver;
 
 import cz.cvut.fel.bupro.filter.FilterableArgumentResolver;
 import cz.cvut.fel.bupro.support.ExtendedDomainClassConverter;
 
 @Configuration
 public class MvcConfig extends WebMvcConfigurationSupport {
 
 	@Override
 	protected void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry.addResourceHandler("/css/**").addResourceLocations("classpath:/WEB-INF/resources/css/");
		registry.addResourceHandler("/js/**").addResourceLocations("classpath:/WEB-INF/resources/js/");
		registry.addResourceHandler("/images/**").addResourceLocations("classpath:/WEB-INF/resources/images/");
 	}
 
 	@Override
 	protected void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
 		argumentResolvers.add(new ServletWebArgumentResolverAdapter(new PageableArgumentResolver()));
 		argumentResolvers.add(new FilterableArgumentResolver());
 	}
 
 	@Override
 	protected void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
 		converters.add(new StringHttpMessageConverter(Charset.forName("UTF-8")));
 	}
 
 	@Override
 	protected void addInterceptors(InterceptorRegistry registry) {
 		LocaleChangeInterceptor localeChangeInterceptor = new LocaleChangeInterceptor();
 		localeChangeInterceptor.setParamName("lang");
 		registry.addInterceptor(localeChangeInterceptor);
 	}
 
 	@Bean
 	public LocaleResolver localeResolver() {
 		CookieLocaleResolver localeResolver = new CookieLocaleResolver();
 		localeResolver.setDefaultLocale(new Locale("cs"));
 		return localeResolver;
 	}
 
 	@Bean
 	public PageableArgumentResolver pageableArgumentResolver() {
 		return new PageableArgumentResolver();
 	}
 
 	@Bean
 	public DefaultConversionService conversionService() {
 		return new DefaultConversionService();
 	}
 
 	@Bean
 	public DomainClassConverter<FormattingConversionService> extendedDomainClassConverter() {
 		return new ExtendedDomainClassConverter<FormattingConversionService>(mvcConversionService());
 	}
 
 	@Bean
 	public TemplateResolver templateResolver() {
 		TemplateResolver resolver = new ServletContextTemplateResolver();
 		// setPrefix - prefixes any controller RequestMapping return value
 		resolver.setPrefix("/WEB-INF/templates/");
 		resolver.setSuffix(".html"); // suffixes any RequestMapping return value
 		resolver.setTemplateMode("HTML5");
 		return resolver;
 	}
 
 	@Bean
 	public SpringTemplateEngine templateEngine() {
 		SpringTemplateEngine templateEngine = new SpringTemplateEngine();
 		templateEngine.setTemplateResolver(templateResolver());
 		templateEngine.setCacheManager(null); // FIXME devel prevent caching
 		templateEngine.addDialect(new SpringSecurityDialect());
 		return templateEngine;
 	}
 
 	@Bean
 	public ViewResolver viewResolver() {
 		ThymeleafViewResolver viewResolver = new ThymeleafViewResolver();
 		viewResolver.setTemplateEngine(templateEngine());
 		viewResolver.setCharacterEncoding("UTF-8");
 		return viewResolver;
 	}
 
 	@Bean
 	public MessageSource messageSource() {
 		ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
 		messageSource.setBasename("locale/messages");
 		return messageSource;
 	}
 
 	@Bean
 	@Qualifier(Qualifiers.EMAIL)
 	public MessageSource emailMessageSource() {
 		ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
 		messageSource.setBasename("locale/emails");
 		return messageSource;
 	}
 
 }
