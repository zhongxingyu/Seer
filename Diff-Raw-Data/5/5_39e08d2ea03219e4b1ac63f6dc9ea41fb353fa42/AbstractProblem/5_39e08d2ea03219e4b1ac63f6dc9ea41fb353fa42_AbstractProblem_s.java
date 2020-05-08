 /*
  * Copyright (C) 2012 Sietse van der Molen <sietse@vdmolen.eu>
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package projecteuler;
 
 import projecteuler.util.Stopwatch;
 
 /**
  * An abstract description of a Project Euler problem.
  * This should make the actual problem classes a bit cleaner.
  * @author Sietse van der Molen <sietse@vdmolen.eu>
  */
 abstract class AbstractProblem {
 
 	Stopwatch stopwatch = new Stopwatch();
 	long nsTaken = 0;
 	long answer = 0;
 	String description;
 
 	public long getAverageNanosecondsTaken() {
 		return nsTaken;
 	}
 
 	public long getAnswer() {
 		return answer;
 	}
 
 	abstract void setDescription();
 
 	protected void benchmark() {
 		long avgTime = 0;
 		// Do specified number of runs
 		for (int i = 0; i < 20; i++) {
 			stopwatch.start();
			_solve();
 			stopwatch.stop();
 			avgTime += stopwatch.getElapsedTime();
 			stopwatch.reset();
 		}
 		nsTaken = avgTime / 20;
 	}
 
 	/**
 	 * The actual problem solving happens in here
 	 *
 	 * @return The answer to this problem as a long
 	 */
 	abstract long solve();
 
 	private void _solve() {
 		// Do a couple of test runs to benchmark the algorithm
 		benchmark();
 		// Solve the problem and save the answer
 		answer = solve();
 	}
 
 	@Override
 	public String toString() {
 		setDescription();
		solve();
 		return description + System.lineSeparator() + "Got answer " + getAnswer() + " in " + getAverageNanosecondsTaken() / 1000000.0 + " milliseconds";
 	}
 }
