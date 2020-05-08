 /*
 
    Copyright 2011 Monits
  
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
 
        http://www.apache.org/licenses/LICENSE-2.0
 
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
 
 */
 
 
 
 /**
  * Random Utils.
  *
  * @copyright 2011 Monits
  * @license   Apache 2.0 License
  * @version   Release: 1.0.0
  * @link      http://www.monits.com/
  * @since     1.0.0
  */
 package com.monits.commons.utils;
 
 import java.util.Random;
 
 /**
  * Random Utils.
  *
  * @author    Maximiliano Luque <mluque@monits.com>
  * @copyright 2011 Monits
  * @license   Apache 2.0 License
  * @version   Release: 1.0.0
  * @link      http://www.monits.com/
  * @since     1.0.0
  */
 public class RandomUtils {
 
 	/**
 	 * Utility classes should not have a public or default constructor.
 	 */
 	private RandomUtils() {
 		throw new UnsupportedOperationException();
 	}
 
 	/**
 	 * Generates a random String with the number of characters requested.
 	 *
 	 * @param amount The number of characters in the string. If negative, it will be treated as zero.
 	 *
 	 * @return The random String.
 	 */
 	public static String generateRandomString(int amount) {
 
 		Random random = new Random();
 
 		char[] characters = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
			'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l',
			'm', 'n', 'ñ', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w',
			'x', 'y', 'z', };
 
 		StringBuffer buffer = new StringBuffer();
 
 		for (int i = 0; i < amount ; i++) {
 			buffer.append(characters[random.nextInt(characters.length - 1)]);
 		}
 
 		return buffer.toString();
 	}
 
 }
