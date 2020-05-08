 /*
  * Copyright (c) 2013. Tomasz Szuba, Paulina Schab, Michał Tkaczyk. All rights reserved.
  */
 
 package com.miniinf.OSPManager.validation;
 
 import javax.validation.ConstraintValidator;
 import javax.validation.ConstraintValidatorContext;
 
 /**
  * Created by Tomasz Szuba
  * Date: 02.01.13
  */
 public class PESELValidator implements ConstraintValidator<PESEL, String> {
     @Override
     public void initialize(PESEL constraintAnnotation) {}
 
     @Override
     public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
         if (value.length() != 11) {
             return false;
         }
         char[] chars = value.toCharArray();
         int checksum = 0;
         for (int i = 0; i < 10; i++) {
             char character = chars[i];
             if (!Character.isDigit(character)) {
                 return false;
             }
             int digit = Character.digit(character, 10);
             switch (i % 4) {
                 case 0:
                     checksum += digit;
                     break;
                 case 1:
                     checksum += digit * 3;
                     break;
                 case 2:
                     checksum += digit * 7;
                 case 3:
                     checksum += digit * 9;
             }
         }
         int control = (10 - checksum % 10) % 10;
         char last = chars[10];
         if (!Character.isDigit(last)) {
             return false;
         }
         int lastDigit = Character.digit(last, 10);
         return lastDigit == control;
     }
 }
