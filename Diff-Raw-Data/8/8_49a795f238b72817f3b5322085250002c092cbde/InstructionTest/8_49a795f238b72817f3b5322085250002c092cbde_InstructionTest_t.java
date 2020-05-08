 /*
  *  LICENSE
  *
  * "THE BEER-WARE LICENSE" (Revision 43):
  * "Sven Strittmatter" <weltraumschaf@googlemail.com> wrote this file.
  * As long as you retain this notice you can do whatever you want with
  * this stuff. If we meet some day, and you think this stuff is worth it,
  * you can buy me a non alcohol-free beer in return.
  *
  * Copyright (C) 2012 "Sven Strittmatter" <weltraumschaf@googlemail.com>
  */
 
 package de.weltraumschaf.shackhack;
 
 import org.junit.Test;
 import static org.junit.Assert.assertThat;
 import static org.hamcrest.CoreMatchers.is;
 
 /**
  *
  * @author Sven Strittmatter <weltraumschaf@googlemail.com>
  */
 public class InstructionTest {
 
     private final Instruction sut = Instruction.newInstance(ByteCode.DADD);
 
     @Test
     public void toString_withNoArgs() {
         assertThat(sut.toString(), is("dadd"));
     }
 
     @Test
     public void toString_withNoArgsAndComment() {
         sut.setComment("a comment");
         assertThat(sut.toString(), is("dadd ; a comment"));
     }
 
     @Test
     public void toString_withOneStringArgs() {
         sut.addArgument("12");
         assertThat(sut.toString(), is("dadd 12"));
     }
 
     @Test
     public void toString_withOneStringArgsAndComment() {
         sut.addArgument("12");
         sut.setComment("a comment");
         assertThat(sut.toString(), is("dadd 12 ; a comment"));
     }
 
     @Test
     public void toString_withTwoStringArgs() {
         sut.addArgument("1");
         sut.addArgument("2");
         assertThat(sut.toString(), is("dadd 1 2"));
     }
 
     @Test
     public void toString_withTwoStringArgsAndComment() {
         sut.addArgument("1");
         sut.addArgument("2");
         sut.setComment("a comment");
         assertThat(sut.toString(), is("dadd 1 2 ; a comment"));
     }
 
     @Test
     public void toString_withThreeStringArgs() {
         sut.addArgument("1");
         sut.addArgument("2");
         sut.addArgument("3");
         assertThat(sut.toString(), is("dadd 1 2 3"));
     }

     @Test
     public void toString_withThreeStringArgsAndComment() {
         sut.addArgument("1");
         sut.addArgument("2");
         sut.addArgument("3");
         sut.setComment("a comment");
         assertThat(sut.toString(), is("dadd 1 2 3 ; a comment"));
     }
 
 }
