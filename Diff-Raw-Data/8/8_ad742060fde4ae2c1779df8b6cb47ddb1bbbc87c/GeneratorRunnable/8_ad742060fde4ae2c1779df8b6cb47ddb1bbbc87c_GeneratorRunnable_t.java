 /*-----------------------------------------------------------------------------
  * Copyright © 2013 Keith Webster Johnston.
  * All rights reserved.
  *
  * This file is part of avalanche.
  *
  * avalanche is free software: you can redistribute it and/or modify it under
  * the terms of the GNU General Public License as published by the Free
  * Software Foundation, either version 3 of the License, or (at your option) any
  * later version.
  *
  * avalanche is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  *
  * You should have received a copy of the GNU General Public License
  * along with avalanche. If not, see <http://www.gnu.org/licenses/>.
  *---------------------------------------------------------------------------*/
 package com.johnstok.avalanche;
 
 
 public class GeneratorRunnable<T> implements Runnable {
 
     private final Generator<T>       _generator;
     private final Callback _callback;
 
 
     /**
      * Constructor.
      *
      * @param generator
      * @param callback
      */
     public GeneratorRunnable(final Generator<T> generator,
                              final Callback callback) {
         _generator = generator;
         _callback = callback;
     }
 
 
 
     /** {@inheritDoc} */
     public void run() {
        try {
            _generator.generate(_callback);
        } catch (final RuntimeException e) {
            e.printStackTrace(); // FIXME: Handle correctly.
        }
     }
 
 }
