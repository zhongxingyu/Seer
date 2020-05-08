 package com.euroit.eshop.sec;
 
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
 import org.springframework.context.i18n.LocaleContextHolder;
 import org.springframework.web.filter.OncePerRequestFilter;
 import org.springframework.web.servlet.LocaleResolver;
 
 import javax.servlet.FilterChain;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.io.IOException;
 import java.util.Locale;
 
 /**
  * @author EuroITConsulting
  */
 public class LocaleContextFilter extends OncePerRequestFilter {
	private static final Logger LOG = LoggerFactory.getLogger(LocaleContextFilter.class);
     private LocaleResolver localeResolver;
 
     @Override
     protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
             throws ServletException, IOException {

         if (this.localeResolver != null) {
             final Locale locale = this.localeResolver.resolveLocale(request);
             LocaleContextHolder.setLocale(locale);
         }
         try {
             filterChain.doFilter(request, response);
         } finally {
             LocaleContextHolder.resetLocaleContext();
         }
     }
 
     public void setLocaleResolver(LocaleResolver localeResolver) {
         this.localeResolver = localeResolver;
     }
 }
