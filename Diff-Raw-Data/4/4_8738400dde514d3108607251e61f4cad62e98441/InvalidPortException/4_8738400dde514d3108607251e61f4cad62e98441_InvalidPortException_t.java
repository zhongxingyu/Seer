 /*
  * This file is part of CBCJVM.
  * CBCJVM is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * CBCJVM is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with CBCJVM.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package cbccore;
 
 /**
 * Ports for motors are 0-3. Digital outputs are 0-7, while analog outputs are
 * 8-15. Check your ports!
  *
  * @author Braden McDorman
  */
 
 public class InvalidPortException extends RuntimeException {
 	private static final long serialVersionUID = 3853895805956598697L;
 }
