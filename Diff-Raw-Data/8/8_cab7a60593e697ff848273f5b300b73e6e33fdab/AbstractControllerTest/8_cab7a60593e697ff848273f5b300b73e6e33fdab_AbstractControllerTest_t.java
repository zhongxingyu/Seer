 package com.mjeanroy.springhub.test.controllers;
 
import org.dozer.DozerBeanMapper;
import org.dozer.Mapper;
 import org.junit.runner.RunWith;
 import org.mockito.Mock;
import org.mockito.Spy;
 import org.mockito.runners.MockitoJUnitRunner;
 
 import javax.servlet.http.HttpServletRequest;
 
 @RunWith(MockitoJUnitRunner.class)
 public class AbstractControllerTest {
 
	@Spy
	protected Mapper mapper = new DozerBeanMapper();

 	@Mock
 	protected HttpServletRequest httpRequest;
 
 }
