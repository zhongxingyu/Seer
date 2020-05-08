 /*
    Copyright (C) 2011  Kristian 'Bobby' Lundkvist, Niclas 'Prosten' Bjrner
 
 	This file is a part of GLaDOS
 
     This GLaDOS is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     This program is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 /*
    Copyright (C) 2011  Kristian 'Bobby' Lundkvist, Niclas 'Prosten' Bjrner
 
 	This file is a part of GLaDOS
 
     This GLaDOS is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     This program is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package se.bthstudent.sis.afk.GLaDOS;
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.io.Serializable;
 
 /**
  * A general class for doing stuff that doesn't really fit in anywhere else.
  * @author Bobby
  *
  */
 public class GenericUtilityProcessor implements Serializable{
 
 	/**
 	 * Field serialVersionUID. (value is 5606536754790417834)
 	 */
 	private static final long serialVersionUID = 5606536754790417834L;
 
 	/**
 	 * Constructor
 	 */
 	public GenericUtilityProcessor(){}
 	
 	/**
 	 * Saves GLaDOS to a file for safety (to be ready when the shit/neurotoxin hits the fan.
 	 * @param temp GLaDOS
 	 */
 	public void saveGLaDOStoFile(Object temp){
 		try {
 			ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("GLaDOS.backup"));
 			
 			out.writeObject(temp);
 			
 			out.close();
 		} 
 		catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} 
 		catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	/**
 	 * Loads GLaDOS from backup.
 	 * @return GLaDOS
 	 */
 	public Object loadGLaDOSfromFile(){
 		Object temp = new Object();
 		
 		try{
 			ObjectInputStream in = new ObjectInputStream(new FileInputStream("GLaDOS.backup"));
 			
 			try {
 				temp = (GLaDOS)in.readObject();
 			} catch (ClassNotFoundException e) {
 				e.printStackTrace();
 			}
 			
 			in.close();
 		} catch (FileNotFoundException e) {
 			System.err
 					.println("Error: could not load GLaDOS from GLaDOS.backup, file not found");
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 		return temp;
 	}
 
 	/**
 	 * Backup test subjects to file (TestSubjects.backup).
 	 * 
 	 * @param temp
 	 *            Test subjects
 	 */
 	public void saveTestSubjectsToFile(Object temp) {
 		try {
 			ObjectOutputStream out = new ObjectOutputStream(
 					new FileOutputStream("TestSubjects.backup"));
 
 			out.writeObject(temp);
 
 			out.close();
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Load test subjects from file (TestSubjects.backup).
 	 * 
 	 * @return Object Test Subjects */
 	public Object loadTestSubjectsFromFile() {
 		Object temp = new Object();
 
 		try {
 			ObjectInputStream in = new ObjectInputStream(new FileInputStream(
 					"TestSubjects.backup"));
 
 			try {
 				temp = in.readObject();
 			} catch (ClassNotFoundException e) {
 				e.printStackTrace();
 			}
 		} catch (FileNotFoundException e) {
 			System.err
 					.println("Error: could not load TestSubject from TestSubjects.backup, file not found");
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		
 		return temp;
 	}
 
 }
