 /**
  * Copyright (C) 2012 BonitaSoft S.A.
  * 
  * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 2.0 of the License, or
  * (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program. If not, see <http://www.gnu.org/licenses/>.
  */
 package org.bonitasoft.dojo.fizzbuzz;
 
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.Matchers.is;
 
 import org.junit.Before;
 import org.junit.Test;
 
 public class FizzBuzzGameTest2 {
 
     private FizzBuzzGame fizzBuzzGame;
 
     @Before
     public void intializeFizzBuzzGame() {
         fizzBuzzGame = new FizzBuzzGame(new FizzBuzzer());
     }
     
     @Test
     public void testPlayUntil() throws Exception {
        String expectedGameResult = "1 2 Fizz 4 5 6 Buzz 8 Fizz 10 11 Fizz 13 Buzz Fizz 16 Buzz Fizz 19 20 FizzBuzz 22 " +
        		"Fizz Fizz 25 26 Buzz Fizz 29 Fizz Fizz Fizz Fizz Fizz Fizz Fizz FizzBuzz Fizz Fizz 40 41";
         
         String gameResult = fizzBuzzGame.playUntil(41);
         
         assertThat(gameResult, is(expectedGameResult));
     }
 }
