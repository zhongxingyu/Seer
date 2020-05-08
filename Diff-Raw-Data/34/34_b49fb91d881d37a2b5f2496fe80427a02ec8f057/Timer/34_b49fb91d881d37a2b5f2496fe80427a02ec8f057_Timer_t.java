 /* ============================================================
  * Copyright 2012 Bjorn Persson Mattsson, Johan Gronvall, Daniel Jonsson,
  * Viktor Anderling
  *
  * This file is part of UltraExtreme.
  *
  * UltraExtreme is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * UltraExtreme is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with UltraExtreme. If not, see <http://www.gnu.org/licenses/>.
  * ============================================================ */
 
 package ultraextreme.util;
 
 /**
  * A timer that is dependent on an external update for the elapsed time. On each
  * update it will tell if it has reached its time.
  * 
  * @author Viktor Anderling
  * 
  */
 public class Timer {
 
 	/**
 	 * The number of runs left.
 	 */
 	private int runs;
 
 	/**
 	 * The elapsed time since last repeat.
 	 */
 	private float timeElapsed;
 
 	/**
 	 * The amount of time it takes for a fire change.
 	 */
 	private float totalTime;
 
 	/**
 	 * The name of this timers property.
 	 */
 	private String propertyName;
 
 	/**
 	 * The object that is connected to the property.
 	 */
 	private Object object;
 
 	/**
 	 * If the timer is currently running.
 	 */
 	private boolean isRunning;
 
 	/**
 	 * Creates a timer that has a property, a time, an object and a number of
 	 * runs.
 	 * 
 	 * @param propertyName
 	 *            The name of the property.
 	 * @param totalTime
 	 *            The time before a property change.
 	 * @param o
 	 *            The object that is related to the property change.
 	 * @param runs
 	 *            How many times the timer will run.
 	 * 
 	 * @throws IllegalArgumentException
 	 *             if the number of given runs are less than one.
 	 */
	public Timer(String propertyName, float totalTime, Object o, int runs) {
 		if (runs < 1) {
 			throw new IllegalArgumentException("Must run at least one time");
 		}
 		this.propertyName = propertyName;
 		this.object = o;
 		this.runs = runs;
 		this.isRunning = true;
 		this.totalTime = totalTime;
 		this.timeElapsed = 0;
 	}
 
 	/**
 	 * Creates a timer which only runs one time.
 	 */
 	public Timer(String propertyName, float time, Object o) {
 		this(propertyName, time, o, 1);
 	}
 
 	/**
 	 * Updates the timer with the time that has elapsed since the last update.
 	 * 
 	 * @param timeElapsed
 	 *            The time that has elapsed since last update.
 	 * 
 	 * @return true if the totalTime has been reached and the timer is running,
 	 *         false otherwise.
 	 */
 	public boolean update(float timeElapsed) {
 		boolean timeReached = false;
 		if (isRunning) {
 			this.timeElapsed += timeElapsed;
 			if (this.timeElapsed >= totalTime) {
 				timeReached = true;
 				runs -= 1;
 				this.timeElapsed -= totalTime;
 			}
 			if (runs == 0) {
 				isRunning = false;
 			}
 		}
 		return timeReached;
 	}
 
 	/**
 	 * @return The object that this timer is connected to.
 	 */
 	public Object getObject() {
 		return this.object;
 	}
 
 	/**
 	 * @return The property name of this timer.
 	 */
 	public String getPropertyName() {
 		return this.propertyName;
 	}
 
 	/**
 	 * @return True if this timer is running, false otherwise.
 	 */
 	public boolean isRunning() {
 		return this.isRunning;
 	}
 }
