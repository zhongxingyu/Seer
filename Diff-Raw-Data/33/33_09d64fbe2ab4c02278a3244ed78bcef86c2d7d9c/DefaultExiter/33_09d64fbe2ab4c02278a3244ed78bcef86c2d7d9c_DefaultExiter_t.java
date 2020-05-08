 /*
  * LICENSE
  *
  * "THE BEER-WARE LICENSE" (Revision 42):
  * "Sven Strittmatter" <weltraumschaf@googlemail.com> wrote this file.
  * As long as you retain this notice you can do whatever you want with
  * this stuff. If we meet some day, and you think this stuff is worth it,
  * you can buy me a beer in return.
  *
  */
 
 package de.weltraumschaf.jebnf.cli.system;
 
 /**
  * Invokes {@link System#exit(int)}.
 *
  * @author Sven Strittmatter <weltraumschaf@googlemail.com>
  */
 public class DefaultExiter extends BaseExiter {
 
     @Override
     public void exit(int status) {
         System.exit(status);
     }
 
 }
