 package com.thoughtworks.twu.controller;
 
 import org.junit.Test;
 import org.springframework.web.servlet.ModelAndView;
 
 import javax.servlet.http.HttpServletRequest;
 
 import static org.hamcrest.CoreMatchers.is;
 import static org.junit.Assert.assertThat;
 import static org.mockito.Mockito.mock;
 
 public class DashboardControllerTest {
 
 
     @Test
     public void shouldShowDashboard(){
         DashboardController dashboardController = new DashboardController();
 
         HttpServletRequest request = mock(HttpServletRequest.class);
 
         ModelAndView modelAndView = dashboardController.show(request);
 
         assertThat(modelAndView.getViewName(), is("ui/dashboard/dashboard"));
     }
 
    @Test
    public void shouldShowUserName(){
 
    }
 
 }
