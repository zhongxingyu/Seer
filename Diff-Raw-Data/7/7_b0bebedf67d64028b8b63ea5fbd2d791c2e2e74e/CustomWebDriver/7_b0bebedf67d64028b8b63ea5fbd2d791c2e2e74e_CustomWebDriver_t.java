 package org.bdd.example.functional_specs.selenium;
 
 import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
 import org.openqa.selenium.htmlunit.HtmlUnitDriver;
 
 import java.util.List;
 import java.util.Set;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Augusto
  * Date: 09/09/12
  * Time: 09:32
  * To change this template use File | Settings | File Templates.
  */
public class CustomWebDriver extends FirefoxDriver {
 
     public CustomWebDriver() {
         super();
        //setJavascriptEnabled(true);
     }
 }
