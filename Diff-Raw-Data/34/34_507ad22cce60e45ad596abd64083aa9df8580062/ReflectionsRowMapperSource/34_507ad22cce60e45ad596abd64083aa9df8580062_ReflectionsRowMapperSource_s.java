 package com.jshnd.casrom.config;
 
 import java.util.Collection;
 
 import org.reflections.Reflections;
 import org.reflections.util.ClasspathHelper;
 import org.reflections.util.ConfigurationBuilder;
 import org.reflections.util.FilterBuilder;
 
 import com.jshnd.casrom.annotation.RowMapper;
 
 public class ReflectionsRowMapperSource implements RowMapperSource {
 
 	private String basePackage;
 
 	public Collection<Class<?>> getRowMapperClasses() {
 		ConfigurationBuilder builder = new ConfigurationBuilder();
 		builder.setUrls(ClasspathHelper.forJavaClassPath());
		builder.filterInputsBy(new FilterBuilder.Include(FilterBuilder.prefix(basePackage)));
 		Reflections reflections = new Reflections(builder);
 		return reflections.getTypesAnnotatedWith(RowMapper.class);
 	}
 
 	public void setBasePackage(String basePackage) {
 		this.basePackage = basePackage;
 	}
 
 }
