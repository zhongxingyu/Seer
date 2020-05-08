 package com.mjeanroy.springhub.test.controllers;
 
 import org.junit.runner.RunWith;
 import org.mockito.Mock;
 import org.mockito.runners.MockitoJUnitRunner;
 
 import javax.servlet.http.HttpServletRequest;
 
 @RunWith(MockitoJUnitRunner.class)
 public class AbstractControllerTest {
 
 	@Mock
 	protected HttpServletRequest httpRequest;
 
 }
