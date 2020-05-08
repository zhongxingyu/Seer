 package com.aciertoteam.generator.transformers.impl;
 
 import com.aciertoteam.generator.transformers.Transformer;
 
 /**
  * Examples of transformation
 * <p/>
  * Moldova, Republic of         -----> Republic of Moldova
  * Tanzania, United Republic of -----> United Republic of Tanzania
  *
  * @author Bogdan Nechyporenko
  */
 public class CountryNameTransformer implements Transformer {
 
     @Override
     public String transform(String inputText) {
         if (!inputText.contains(",")) {
             return inputText;
         }
         String[] values = inputText.split(",");
        if (values.length < 2) {
            return null;
        }
         return String.format("%s %s", values[1].trim(), values[0].trim());
     }
 }
