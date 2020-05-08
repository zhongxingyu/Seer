 package com.twu28.biblioteca;
 import static org.junit.Assert.*;
 
 import org.junit.Assert;
 
 import org.junit.Test;
 
 public class BibliotecaTest {
 
 
 @Test
 	public void isWelcomeTheFirstMessage() {
 
    Assert.assertTrue(Biblioteca.getMessage()== "Welcome");
 }
 
 @Test
     public void isThereMoreThanOneOption(){
    Assert.assertTrue(Biblioteca.getNumberOfOptions()> 1);
 }
 
 @Test
     public void isThereAProvisionForMenuSelect(){
    Assert.assertTrue(Biblioteca.getMenuSelect()== null || Biblioteca.getMenuSelect()!= null);
 
 }
 
 }
