 /**
  * 
  *    This module represents an engine for the load testing framework
  *    Copyright (C) 2008  Imran M Yousuf (imran@smartitengineering.com)
  *
  *    This program is free software: you can redistribute it and/or modify
  *    it under the terms of the GNU General Public License as published by
  *    the Free Software Foundation, either version 3 of the License, or
  *    (at your option) any later version.
  *
  *    This program is distributed in the hope that it will be useful,
  *    but WITHOUT ANY WARRANTY; without even the implied warranty of
  *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *    GNU General Public License for more details.
  *
  *    You should have received a copy of the GNU General Public License
  *    along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.smartitengineering.loadtest.engine.events;
 
 import com.smartitengineering.loadtest.engine.TestCase;
 
 /**
  *
  * @author imyousuf
  */
 public class TestCaseStateChangedEvent
     extends AbstractStateChangeEvent<TestCase, TestCase.State, TestCase.State> {
 
     /**
      * Constructor of the event representing test case state change
      * @param source Test case of which state has changed
     * @param oldValue Old state
     * @param newValue New state
      */
     public TestCaseStateChangedEvent(TestCase source,
                                      TestCase.State oldState,
                                      TestCase.State newState) {
         super(source, oldState, newState);
     }
 }
