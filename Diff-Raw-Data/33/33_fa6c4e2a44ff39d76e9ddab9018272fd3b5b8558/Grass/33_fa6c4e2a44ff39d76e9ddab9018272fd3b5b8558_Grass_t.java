 package com.cegeka.nocturne.godgame;
 
 /**
  * Created by monicat on 03/04/2014.
  */
 public class Grass  implements Creature{
 
     private Integer age;
 
    public Grass(){
        this.age = 0;
    }
     public Integer getAge(){
         return this.age;
     }

    public void incrementAge(){
        age++;
    }
 }
