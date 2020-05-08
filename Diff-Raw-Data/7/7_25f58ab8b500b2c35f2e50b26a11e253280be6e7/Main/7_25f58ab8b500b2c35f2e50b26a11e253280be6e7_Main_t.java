 /*
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.aperigeek.jlad.trainer.wikipedia;
 
 import com.aperigeek.jlad.trainer.TrainerException;
 
 /**
  * Main class responsible for running the Wikipedia trainer.
  * 
  * @author Vivien Barousse
  */
 public class Main {
     
     /**
      * Runs the application.
      * 
      * Currently, the application takes the input file on the standard input
      * and outputs the n-grams repartitions on the standard output, without
      * any specific order.
      * 
      * @param args Command line arguments
      */
     public static void main(String[] args) {
         WikipediaTrainer trainer = new WikipediaTrainer(System.in);
         try {
             trainer.train();
         } catch (TrainerException ex) {
             System.err.println("Unexpected error while training.");
             System.err.println("Following are the debug informations.");
             ex.printStackTrace(System.err);
         }
         trainer.dump(System.out);
     }
     
 }
