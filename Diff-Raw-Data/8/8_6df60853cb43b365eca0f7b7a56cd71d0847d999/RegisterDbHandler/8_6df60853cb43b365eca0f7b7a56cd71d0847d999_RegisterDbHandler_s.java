 /*Copyright  2012 GivDev
  * 
  * This file is part of Gymapp.
  *
  *   Gymapp is free software: you can redistribute it and/or modify
  *   it under the terms of the GNU General Public License as published by
  *   the Free Software Foundation, either version 3 of the License, or
  *   (at your option) any later version.
  *
  *   Gymapp is distributed in the hope that it will be useful,
  *   but WITHOUT ANY WARRANTY; without even the implied warranty of
  *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *   GNU General Public License for more details.
  *
  *   You should have received a copy of the GNU General Public License
  *  along with Gymapp.  If not, see <http://www.gnu.org/licenses/>.
  *
  */
 
 package com.Grupp01.gymapp.Controller.Workout;
 
 import java.util.LinkedList;
 import java.util.List;
 
 import android.content.Context;
 import android.database.Cursor;
 
 import com.Grupp01.gymapp.Model.Database;
 
 public class RegisterDbHandler extends Database {
 
 	public final static int NUMBER_SECONDS_IN_HOUR  = 3600; //Used for timeformating equation
 	public final static int NUMBER_SECONDS_IN_MIN = 60; //Used for timeformating equation
 	public final static int NUMBER_OF_LATEST_SET = 4; //Number of sets that are going to be shown in text: "Latest sets"
 	;
 	public RegisterDbHandler(Context c)
 	{
 		super(c);
 	}
 
 
 	/**
 	 * Adds a cardioset to table Sets in database
 	 * @param sec number of seconds that the set has taken
 	 * @param min minutes that the set has taken
 	 * @param distance the distance done in the set
 	 * @param workoutId workoutId for current workout
 	 * @param exerciseId exerciseId for current exercise
 	 */
 	public void addCardioSet(int sec, int min, float distance, int workoutId, int exerciseId)
 	{
		int duration = sec + (min*60);
 		open();
 		ourDatabase.execSQL("INSERT INTO Sets (SetDistance, WorkoutId, SetDuration, SetTime, ExerciseId) VALUES "
 				+ "(" + distance + ", " + workoutId + ", " + duration +  ", " + "datetime('now')" + ", " + exerciseId + ");");
 		close();
 	}
 
 	/**
 	 * Adds a dynamic set to table Sets in database
 	 * @param weight weight of the tool used in training
 	 * @param reps number of repitions done
 	 * @param workoutId workoutId for current workout
 	 * @param exerciseId exerciseId for current exercise
 	 */
 	public void addDynamicSet(int weight, int reps, int workoutId, int exerciseId)
 	{
 		open();
 		ourDatabase.execSQL("INSERT INTO Sets(SetReps, SetWeight, WorkoutId, ExerciseId) VALUES " +
 				"(" + reps + ", " + weight + ", " + workoutId + ", " + exerciseId + ");");
 		close();
 	}
 
 	/**
 	 * Adds a static set to table Sets in database
 	 * @param min minutes that the set has taken 
 	 * @param sec seconds that the set has taken
 	 * @param weight weight of the tool used in training
 	 * @param workoutId workoutId for current workout
 	 * @param exerciseId exerciseId for current exercise
 	 */
 	public void addStaticSet(int min, int sec, int weight, int workoutId, int exerciseId)
 	{
		int duration = sec + (min*60);
 		open();
 		ourDatabase.execSQL("INSERT INTO Sets(SetWeight, WorkoutId, SetDuration, ExerciseId) VALUES " +
 				"(" + weight + ", " + workoutId + ", " + duration + ", " + exerciseId + ");");
 		close();
 	}
 
 	/**
 	 * Gets a list of the 4 latest CardioSets for a specific exercise
 	 * @param workoutId workoutId for current workout
 	 * @param exerciseId exerciseId for current exercise
 	 * @param exerciseTypeId exerciseTypeId for current exercise
 	 * @return A List containing the four latest sets in SetData objects with SetDuration and SetDistance parameters.
 	 */
 	public List<SetsData> getPreviouslyCardioSets(int workoutId, int exerciseId, int exerciseTypeId)
 	{
 		List<SetsData> cardioSetsList = new LinkedList<SetsData>();
 		open();	
 		Cursor c = ourDatabase.rawQuery("SELECT Sets.SetDuration, Sets.SetDistance FROM Sets, Exercises WHERE Sets.WorkoutId = " + workoutId +" AND " +
 				"Sets.ExerciseId = " + exerciseId + " AND Exercises.ExerciseId = Sets.ExerciseId AND " +
 				"Exercises.ExerciseTypeId = " + exerciseTypeId + " ORDER BY SetId DESC LIMIT 4;", null);
 		c.moveToFirst();
 		int duration = c.getColumnIndex("SetDuration");
 		int distance = c.getColumnIndex("SetDistance");
 
 		for(c.moveToFirst(); !c.isAfterLast(); c.moveToNext())
 		{
 
 			//Equation for formating number of seconds a set has taken into a String with format hh:mm:ss
 			String durationString = (((int) (c.getInt(duration) / NUMBER_SECONDS_IN_HOUR)) + ":" + (((int) (c.getInt(duration) 
 					/ NUMBER_SECONDS_IN_MIN)) % NUMBER_SECONDS_IN_MIN) + ":" + (c.getInt(duration) % NUMBER_SECONDS_IN_MIN)); 
 			cardioSetsList.add(new SetsData(durationString,c.getInt(distance)));
 		}
 		c.close();
 		close();
 		return cardioSetsList;
 	}
 
 	/**
 	 * Gets a list of the 4 latestDynamicSets for a specific exercise
 	 * @param workoutId workoutId for current workout
 	 * @param exerciseId exerciseId for current exercise
 	 * @param exerciseTypeId exerciseTypeId for current exercise
 	 * @return
 	 */
 	public List<SetsData> getPreviouslyDynamicSets(int workoutId, int exerciseId, int exerciseTypeId)
 	{
 		List<SetsData> dynamicSetsList = new LinkedList<SetsData>();
 		open();
 		Cursor c = ourDatabase.rawQuery("SELECT Sets.SetWeight, Sets.SetReps FROM Sets, Exercises WHERE Sets.WorkoutId = " + workoutId +" AND " +
 				"Sets.ExerciseId = " + exerciseId + " AND Exercises.ExerciseId = Sets.ExerciseId AND " +
 				"Exercises.ExerciseTypeId = " + exerciseTypeId + " ORDER BY SetId DESC LIMIT 4;", null);
 		c.moveToFirst();
 		int weight = c.getColumnIndex("SetWeight");
 		int reps = c.getColumnIndex("SetReps");
 
 		for(c.moveToFirst(); !c.isAfterLast(); c.moveToNext())
 		{
 			dynamicSetsList.add(new SetsData(c.getInt(weight),c.getInt(reps)));
 		}
 
 		c.close();
 		close();
 		return dynamicSetsList;
 	}
 
 
 	public List<SetsData> getPreviouslyStaticSets(int workoutId, int exerciseId, int exerciseTypeId)
 	{
 		List<SetsData> staticSetsList = new LinkedList<SetsData>();
 		open();
 		Cursor c = ourDatabase.rawQuery("SELECT Sets.SetDuration, Sets.SetWeight FROM Sets, Exercises WHERE Sets.WorkoutId = " + workoutId +" AND " +
 				"Sets.ExerciseId = " + exerciseId + " AND Exercises.ExerciseId = Sets.ExerciseId AND " +
 				"Exercises.ExerciseTypeId = " + exerciseTypeId + " ORDER BY SetId DESC LIMIT 4;", null);
 		c.moveToFirst();
 		int duration = c.getColumnIndex("SetDuration");
 		int weight = c.getColumnIndex("SetWeight");
 
 		for(c.moveToFirst(); !c.isAfterLast(); c.moveToNext())
 		{
			String durationString = (((int) (c.getInt(duration) / 3600)) + ":" + (((int) (c.getInt(duration) / 60)) % 60) + ":" + (c.getInt(duration) % 60)); 
 			staticSetsList.add(new SetsData(c.getInt(weight),durationString));
 		}
 
 		c.close();
 		close();
 		return staticSetsList;
 	}
 
 	public void removeLatestCardioSet(int setCardioId)
 	{
 		open();
 		ourDatabase.execSQL("DELETE FROM Sets WHERE SetId="+setCardioId+";");
 		getLatestCardioSetId();
 		close();
 	}
 
 	public int getLatestCardioSetId()
 
 	{
 		open();
 		Cursor c = ourDatabase.rawQuery("SELECT MAX(SetId) FROM Sets", null);
 		c.moveToFirst();
 		int latestRowId = c.getInt(0);
 		c.close();
 		close();
 		return latestRowId;
 	}
 
 }
