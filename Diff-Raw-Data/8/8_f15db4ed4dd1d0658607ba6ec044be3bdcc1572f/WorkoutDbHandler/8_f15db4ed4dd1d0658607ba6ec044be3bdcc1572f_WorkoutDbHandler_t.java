 /*This file is part of Gymapp.
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
 
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 
 import com.Grupp01.gymapp.Controller.IdName;
 import com.Grupp01.gymapp.Controller.Exercise.ExerciseData;
 import com.Grupp01.gymapp.Model.Database;
 import com.Grupp01.gymapp.View.Workout.ExerciseListElementData;
 
 public class WorkoutDbHandler extends Database {
 
 	
 	public WorkoutDbHandler(Context c)
 	{
 		super(c);
 	}
 	
 
 	/**
 	 * Gets all exercises id and name from databasetable Exercises and puts these values into an IdName object. 
 	 * @return a LinkedList with type of IdName
 	 */
 	public List<IdName> getWorkoutsIdName()
 	{
 		
 		open();
 		Cursor c = ourDatabase.rawQuery("SELECT WorkoutTemplateId, WorkoutTemplateName FROM WorkoutTemplates;", null);
 		List<IdName> idNameList = new LinkedList<IdName>();
 		c.moveToFirst();
 		int id = c.getColumnIndex("WorkoutTemplateId");
 		int name = c.getColumnIndex("WorkoutTemplateName");
 		//Forlopp som gr igenom hela databasen, alla kolummer
 		for(c.moveToFirst(); !c.isAfterLast(); c.moveToNext())
 		{
 			idNameList.add(new IdName(c.getInt(id),c.getString(name)));
 		}
 		c.close();
 		close();
 		return idNameList;
 	}
 	
 	/**
 	 * Returns the specific 
 	 * @param workoutId
 	 * @return Id and Name of a workout in a IdName
 	 */
 	public IdName getWorkoutIdNameById(int workoutId)
 	{
 		open();
 		Cursor c = ourDatabase.rawQuery("SELECT WorkoutTemplateId, WorkoutTemplateName FROM WorkoutTemplates WHERE " + 
 										"WorkoutTemplateId= '" + workoutId + "';", null);
 		c.moveToFirst();
 		IdName temp = new IdName(c.getInt(c.getColumnIndex("WorkoutTemplateId")),c.getString(c.getColumnIndex("WorkoutTemplateName")));
 		c.close();
 		close();
 		return temp;
 		
 	}
 	
 	
 	/**
 	 * 
 	 * @param workoutName
 	 */
 	public void putNewWorkout(String workoutName)
 	{
 		open();
 		ourDatabase.execSQL("INSERT INTO WorkoutTemplates (WorkoutTemplateName) VALUES ('" + workoutName + "');");
 		close();
 	}
 	
 	/**
 	 * 
 	 * @param workoutTemplateId
 	 * @return List<Integer> that contains id for all exercises that are in a workout
 	 */
 	public List<Integer> getExercisesbyWorkoutId(int workoutTemplateId)
 	{
 		List<Integer> integerList = new LinkedList<Integer>();
 		open();
 		Cursor c = ourDatabase.rawQuery("SELECT ExerciseId FROM WorkoutTemplateExercises WHERE WorkoutTemplateId='" + workoutTemplateId + "';", null);
 		c.moveToFirst();
 		int id = c.getColumnIndex("ExerciseId");
 		for(c.moveToFirst(); !c.isAfterLast(); c.moveToNext())
 		{
 			integerList.add(c.getInt(id));
 		}
 		c.close();
 		close();
 		return integerList;
 	}
 	
 	/**
 	 * Returns all exercises in form of ExerciseData that a workout contains.
 	 *  
 	 * @param integerList
 	 * @return LinkedList of ExerciseData, ExerciseData contains ExerciseId, ExerciseName and ExerciseTypeId
 	 */
 	public List<ExerciseData> getExerciseIdNameById(List<Integer> integerList)
 	{
 		List<ExerciseData> exerciseDataList = new LinkedList<ExerciseData>();
 		open();
 		for(Integer exerciseId: integerList)
 		{
 			Cursor d = ourDatabase.rawQuery("SELECT ExerciseId, ExerciseName, ExerciseTypeId FROM Exercises WHERE ExerciseId='" + exerciseId + "';", null);
 			d.moveToFirst();
 			int id = d.getColumnIndex("ExerciseId");
 			int name = d.getColumnIndex("ExerciseName");
 			int typeId = d.getColumnIndex("ExerciseTypeId");
 			exerciseDataList.add(new ExerciseData(d.getInt(id), d.getString(name), d.getInt(typeId)));
 			d.close();
 			
 		}
 		close();
 		return exerciseDataList;
 
 	}
 	
 	/**
 	 * Gets an Exercise by ExerciseId from Exercises table in database.
 	 * 
 	 * @param exerciseId
 	 * @return a Exercise in form of a ExerciseData object
 	 */
 	public ExerciseData getExerciseDataFromExerciseId(int exerciseId)
 	{
 		open();
 		Cursor c = ourDatabase.rawQuery("SELECT * FROM Exercises WHERE ExerciseId='" + exerciseId + "';", null);
 		c.moveToFirst();
 		int id = c.getColumnIndex("ExerciseId");
 		int pri = c.getColumnIndex("ExercisePri");
 		int sec = c.getColumnIndex("ExerciseSec");
 		int name = c.getColumnIndex("ExerciseName");
 		int desc = c.getColumnIndex("ExerciseDesc");
 		int note = c.getColumnIndex("ExerciseNote");
 		int sportid = c.getColumnIndex("ExerciseSportId");
 		int type = c.getColumnIndex("ExerciseTypeId");
 		ExerciseData temp = new ExerciseData(c.getInt(id), c.getInt(pri), c.getInt(sec), c.getString(name), c.getString(desc), c.getString(note), c.getInt(sportid), c.getInt(type));
 		c.close();
 		close();
 		return temp;
 	}
 	
 	/**
 	 * Adds a Cardioset into Databasetable Sets
 	 * @param min
 	 * @param sec
 	 * @param distance
 	 * @param workoutId
 	 * @param exerciseId
 	 */
 	public void addCardioSet(int sec, int min, float distance, int workoutId, int exerciseId)
 	{
 		int duration = sec + (min*60);
 		open();
 	ourDatabase.execSQL("INSERT INTO Sets (SetDistance, WorkoutId, SetDuration, SetTime, ExerciseId) VALUES "
 			+ "(" + distance + ", " + workoutId + ", " + duration +  ", " + "datetime('now')" + ", " + exerciseId + ");");
 		close();
 	}
 	
 	public void addDynamicSet(int weight, int reps, int workoutId, int exerciseId)
 	{
 		open();
 		ourDatabase.execSQL("INSERT INTO Sets(SetReps, SetWeight, WorkoutId, ExerciseId) VALUES " +
 							 "(" + reps + ", " + weight + ", " + workoutId + ", " + exerciseId + ");");
 		close();
 	}
 	
 	public void addStaticSet(int min, int sec, int weight, int workoutId, int exerciseId)
 	{
 		int duration = sec + (min*60);
 		open();
 		ourDatabase.execSQL("INSERT INTO Sets(SetsWeight, WorkoutId, SetDuration, ExerciseId) VALUES " +
 							"(" + weight + ", " + workoutId + ", " + duration + ", " + exerciseId + ");");
 		close();
 	}
 
 
 
 	public List<SetsData> getPreviouslyCardioSets(int workoutId, int exerciseId)
 	{
 		System.out.println("Inne i get PreviouslySets");
 		List<SetsData> cardioSetsList = new LinkedList<SetsData>();
 		open();
 		Cursor c = ourDatabase.rawQuery("SELECT * FROM SETS WHERE WorkoutId = " +"workoutId" +" AND ExerciseId = " + exerciseId + " ORDER BY SetId "
 				+"DESC LIMIT 4;", null);
 		c.moveToFirst();
 		int duration = c.getColumnIndex("SetDuration");
 		int distance = c.getColumnIndex("SetDistance");
 		
 		for(c.moveToFirst(); !c.isAfterLast(); c.moveToNext())
 		{
 			System.out.println("Inne i get PreviouslySetsFoorloop");
 
 			String durationString = (((int) (c.getInt(duration) / 3600)) + ":" + (((int) (c.getInt(duration) / 60)) % 60) + ":" + (c.getInt(duration) % 60)); 
 			cardioSetsList.add(new SetsData(durationString,c.getInt(distance)));
 		}
 		System.out.println("Efter Lopp");
 		c.close();
 		close();
 		return cardioSetsList;
 	}
 	
 	public List<SetsData> getPreviouslyDynamicSets(int workoutId, int exerciseId)
 	{
 		List<SetsData> dynamicSetsList = new LinkedList<SetsData>();
 		open();
 		Cursor c = ourDatabase.rawQuery("SELECT * FROM SETS WHERE WorkoutId = " +"workoutId" +" AND ExerciseId = " + exerciseId + " ORDER BY SetId "
 				+"DESC LIMIT 4;", null);
 		c.moveToFirst();
 		int weight = c.getColumnIndex("SetWeight");
 		int reps = c.getColumnIndex("SetReps");
 			
 		for(c.moveToFirst(); !c.isAfterLast(); c.moveToNext())
 		{
 			dynamicSetsList.add(new SetsData(c.getInt(weight),c.getInt(reps)));
 			System.out.println(c.getInt(weight) + " " + c.getInt(reps));
 		}
 		
 		c.close();
 		close();
 		return dynamicSetsList;
 	}
 	
 	
 	public ArrayList<ExerciseListElementData> getExercisesCheckedByWorkoutTemplateId(int WorkoutTemplateId)
 	{
 		open();
 		
 		ArrayList<ExerciseListElementData> list = new ArrayList<ExerciseListElementData>();
 		
 		Cursor c1 = ourDatabase.rawQuery("SELECT ExerciseId, ExerciseName FROM Exercises;", null);
 		Cursor c2 = ourDatabase.rawQuery("SELECT * FROM WorkoutTemplateExercises WHERE WorkoutTemplateId = '" + WorkoutTemplateId + "';" , null);
 		
 		int c1Id = c1.getColumnIndex("ExerciseId");
 		int c2Id = c2.getColumnIndex("ExerciseId");
 		int name = c1.getColumnIndex("ExerciseName");
 		
 		for(c1.moveToFirst(); !c1.isAfterLast(); c1.moveToNext())
 		{
 			boolean isChecked = false;
 			
 			for(c2.moveToFirst(); !c2.isAfterLast(); c2.moveToNext())
 			{
 				if(c1.getInt(c1Id) == c2.getInt(c2Id))
 				{
 					isChecked = true;
 				}
 			}
 			
 			ExerciseListElementData exerciseListElementData = new ExerciseListElementData(c1.getInt(c1Id), c1.getString(name), isChecked);
 			list.add(exerciseListElementData);
 		}
 		close();
 		
 		return list;
 	}
 
 	public int getLatestCardioSetId()
 
 	{
 		open();
 		Cursor c = ourDatabase.rawQuery("SELECT MAX(SetId) FROM Sets", null);
 		c.moveToFirst();
 		int latestRowId = c.getInt(0);
 		System.out.println(c.getInt(0));
 		c.close();
 		close();
 		return latestRowId;
 	}
 	
 	public void removeLatestCardioSet(int setCardioId)
 	{
 		open();
 		ourDatabase.execSQL("DELETE FROM Sets WHERE SetId="+setCardioId+";");
 		getLatestCardioSetId();
 		close();
 	}
 	
 	public int getExerciseLastTimePerformed(int ExerciseId)
 	{
 		open();
 		Cursor c = ourDatabase.rawQuery("SELECT WorkoutTime FROM Workouts WHERE WorkoutId = Sets.WorkoutId AND Sets.ExerciseId = '" + ExerciseId + "';", null);
 		int WorkoutTime = c.getColumnIndex("WorkoutTime");
 		c.moveToFirst();
 		close();
 		return c.getInt(WorkoutTime);
 	}
 	
 	public void editWorkoutTemplate(ExerciseListElementData exerciseListItemData, int WorkoutTemplateId)
 	{
 		System.out.println(exerciseListItemData.getName());
 		open();
 		if(exerciseListItemData.isChecked())
 		{
 			//add
 			String tmp = "INSERT INTO WorkoutTemplateExercises (WorkoutTemplateId, ExerciseId) VALUES ('" + WorkoutTemplateId + "', '" + exerciseListItemData.getId() + "');";
 			System.out.println(tmp);
 			ourDatabase.execSQL(tmp);
 		} else
 		{
 			//remove
 			String tmp = "DELETE FROM WorkoutTemplateExercises WHERE WorkoutTemplateId = '" + WorkoutTemplateId + "' AND ExerciseId = '" + exerciseListItemData.getId() + "';";
 			System.out.println(tmp);
 			ourDatabase.execSQL(tmp);
 		}
 		close();
 	}
 	
 	public int addWorkout(String WorkoutName)
 	{
 		open();
 		ContentValues values = new ContentValues();
 		values.put("WorkoutName", WorkoutName);
 		int WorkoutId = (int) (long) ourDatabase.insert("Workouts", null, values);
 		close();
 		return WorkoutId;
 	}
 	
 	public void deleteWorkout(int WorkoutId)
 	{
 		open();
 		//Delete sets associated with workout
 		ourDatabase.execSQL("DELETE FROM Sets WHERE WorkoutId = '" + WorkoutId + "';");
 		//Delete the workout
 		ourDatabase.execSQL("DELETE FROM Workouts WHERE WorkoutId = '" + WorkoutId + "';");
 		close();
 	}
 	
 	public void deleteWorkoutTemplate(int WorkoutTemplateId)
 	{
 		open();
 		//Delete exercise associations to WorkoutTemplate
 		ourDatabase.execSQL("DELETE FROM WorkoutTemplateExercises WHERE WorkoutTemplateId = '" + WorkoutTemplateId + "';");
 		//Delete WorkoutTemplate
 		ourDatabase.execSQL("DELETE FROM WorkoutTemplates WHERE WorkoutTemplateId = '" + WorkoutTemplateId + "';");
 		close();
 	}
 	
 	public void deleteExercise(int ExerciseId)
 	{
 		open();
 		//Delete WorkoutTemplateExercises associated with Exercise
 		ourDatabase.execSQL("DELETE FROM WorkoutTemplateExercises WHERE ExerciseId = '" + ExerciseId + "';");
 		//Delete sets associated with Exercise
 		ourDatabase.execSQL("DELETE FROM Sets WHERE ExerciseId = '" + ExerciseId + "';");
 		//Delete Exercises
 		ourDatabase.execSQL("DELETE FROM WorkoutTemplates WHERE WorkoutTemplateId = '" + ExerciseId + "';");
 		close();
 	}
 }
