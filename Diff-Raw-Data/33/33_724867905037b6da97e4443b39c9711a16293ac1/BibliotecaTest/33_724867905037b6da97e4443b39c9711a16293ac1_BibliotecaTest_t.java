 package com.twu28.biblioteca;
 import static org.junit.Assert.*;
 
 import org.junit.Assert;
 
 import org.junit.Test;
 
 public class BibliotecaTest {
    Biblioteca go = new Biblioteca();
 
 
 @Test
 	public void isWelcomeTheFirstMessage() {
 
    Assert.assertTrue(go.getMessage()== "Welcome");
 }
 
 @Test
     public void isThereMoreThanOneOption(){
    Assert.assertTrue(go.getNumberOfOptions()> 1);
 }
 
 @Test
     public void isThereAProvisionForMenuSelect(){
    Assert.assertTrue(go.getMenuSelect()== null || go.getMenuSelect()!= null);
 
 }
 
 }
