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
 package com.Grupp01.gymapp.Model;
 
 import android.content.Context;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 
 /**
  * @author GivDev
  * @version 0.1
  * @peer reviewed by Erik
  * @date 20/10/12
  * Database help class, used by Database.java to create and set up database.
  */
 public class DbHelper extends SQLiteOpenHelper{
 
 	private static final String DATABASE_NAME = "GymAppDatabase"; 
 	private static final int DATABASE_VERSION = 2;
 
 	/**
 	 * This method forwards the Context, database name and database version to the
 	 * superclass.
 	 * @param context Reference to calling object.
 	 */
 	public DbHelper(Context context)
 	{	
 		super(context, DATABASE_NAME, null, DATABASE_VERSION);
 	}
 
 	/**
 	 * Creates new tables in database.
 	 * 
 	 * @param db The SQLite database in which the tables is added.
 	 */
 	@Override
 	public void onCreate(SQLiteDatabase db)
 	{
 		createExercises(db);
 		createMeasureDataTypes(db);
 		createExerciseTypes(db);
 		createWorkouts(db);
 		createMeasures(db);
 		createMeasureData(db);		
 		createMuscles(db);
 		createSets(db);
 		createUsers(db);
 		createWorkoutTemplates(db);
 		createWorkoutTemplateExercises(db);
 		createSports(db);
 		createMuscleGroups(db);
 	}
 
 	/**
 	 * Deletes all the old tables and creates fresh ones.
 	 * 
 	 * @param db The SQLite database to upgrade.
 	 * @param oldVersion The version number of the current database.
 	 * @param newVersion The version number of the new database.
 	 * @author GivDev
 	 */
 	@Override
 	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 		db.execSQL("DROP TABLE IF EXIST Exercises");
 		db.execSQL("DROP TABLE IF EXIST ExerciseTypes");
 		db.execSQL("DROP TABLE IF EXIST MeasureDataTypes");
 		db.execSQL("DROP TABLE IF EXIST MeasureData");
 		db.execSQL("DROP TABLE IF EXIST Workouts");
 		db.execSQL("DROP TABLE IF EXIST Measures");
 		db.execSQL("DROP TABLE IF EXIST Muscles");
 		db.execSQL("DROP TABLE IF EXIST Sets");
 		db.execSQL("DROP TABLE IF EXIST Sports");
 		db.execSQL("DROP TABLE IF EXIST MuscleGroups");
 		db.execSQL("DROP TABLE IF EXIST Users");
 		db.execSQL("DROP TABLE IF EXIST WorkoutTemplates");
 		db.execSQL("DROP TABLE IF EXIST WorkoutTemplatesExercise");
 		onCreate(db);//Creates fresh tables
 	}
 
 	/**
 	 * Adds table Exercises database and adds default content.
 	 * 
 	 * @author GivDev
 	 * @param db The SQLite database in which the table is added.
 	 */
 	private void createExercises(SQLiteDatabase db)
 	{
 		db.execSQL("CREATE TABLE Exercises (ExerciseId INTEGER PRIMARY KEY AUTOINCREMENT, " +
 				"ExercisePri INTEGER, ExerciseSec INTEGER, ExerciseName TEXT NOT NULL, " +
 				"ExerciseDesc TEXT, ExerciseNote Text, " +
 				"ExerciseSportId INTEGER, ExerciseTypeId INTEGER);");
 		db.execSQL("INSERT INTO Exercises(ExercisePri, ExerciseSec, ExerciseName, " +
 				"ExerciseTypeId) VALUES (14,15,'Bench Press', 2);");
 		db.execSQL("INSERT INTO Exercises(ExercisePri, ExerciseSec, ExerciseName, " +
 				"ExerciseTypeId) VALUES (2,8,'Dips', 2);");
 		db.execSQL("INSERT INTO Exercises(ExercisePri, ExerciseSec, ExerciseName, " +
 				"ExerciseTypeId) VALUES (1,22,'Barbell Curls', 2);");
 		db.execSQL("INSERT INTO Exercises(ExercisePri, ExerciseSec, ExerciseName, " +
 				"ExerciseTypeId) VALUES (8,2,'Military Press', 2);");
 		db.execSQL("INSERT INTO Exercises(ExercisePri, ExerciseSec, ExerciseName, " +
 				"ExerciseTypeId) VALUES (13,10,'Deadlift', 2);");
 		db.execSQL("INSERT INTO Exercises(ExerciseName, ExerciseSportId, " +
				"ExerciseTypeId) VALUES ('Swimming', 9, 2);");
 		db.execSQL("INSERT INTO Exercises(ExerciseName, ExerciseSportId, " +
				"ExerciseTypeId) VALUES ('Cycling', 1, 2);");
 		db.execSQL("INSERT INTO Exercises(ExerciseName, ExerciseSportId, " +
				"ExerciseTypeId) VALUES ('Running', 5, 2);");
 		db.execSQL("INSERT INTO Exercises(ExercisePri, ExerciseSec, ExerciseName, " +
 				"ExerciseTypeId) VALUES (2,2,'Pushdowns', 2);");
 		db.execSQL("INSERT INTO Exercises(ExercisePri, ExerciseSec, ExerciseName, " +
 				"ExerciseTypeId) VALUES (8,18,'Shoulder press', 2);");
 		db.execSQL("INSERT INTO Exercises(ExercisePri, ExerciseSec, ExerciseName, " +
 				"ExerciseTypeId) VALUES (11,16,'Full squat', 2);");
 		db.execSQL("INSERT INTO Exercises(ExercisePri, ExerciseSec, ExerciseName, " +
 				"ExerciseTypeId) VALUES (4,11,'Lunge', 2);");
 		db.execSQL("INSERT INTO Exercises(ExercisePri, ExerciseSec, ExerciseName, " +
 				"ExerciseTypeId) VALUES (11,11,'Leg extension', 2);");
 		db.execSQL("INSERT INTO Exercises(ExercisePri, ExerciseSec, ExerciseName, " +
 				"ExerciseTypeId) VALUES (4,11,'Leg press', 2);");
 	}
 
 	/**
 	 * Adds table ExerciseTypes to database and adds default content.
 	 * 
 	 * @author GivDev
 	 * @param db The SQLite database in which the table is added.
 	 */
 	private void createExerciseTypes(SQLiteDatabase db)
 	{
 		db.execSQL("CREATE TABLE ExerciseTypes (ExerciseTypeId INTEGER " +
 				"PRIMARY KEY AUTOINCREMENT, ExerciseTypeName TEXT);");
 		db.execSQL("insert into ExerciseTypes (ExerciseTypeName) VALUES ('Cardio');");
 		db.execSQL("insert into ExerciseTypes (ExerciseTypeName) VALUES ('Dynamic');");
 		db.execSQL("insert into ExerciseTypes (ExerciseTypeName) VALUES ('Static');");		
 	}
 
 	/**
 	 * Adds table MeasureDataTypes to database.
 	 * 
 	 * @author GivDev
 	 * @param db The SQLite database in which the table is added.
 	 */
 	private void createMeasureDataTypes(SQLiteDatabase db)
 	{
 		db.execSQL("CREATE TABLE MeasureDataTypes (MeasureDataTypeId INTEGER " +
 				"PRIMARY KEY AUTOINCREMENT, MeasureDataTypeName TEXT);");
 	}
 
 	/**
 	 * Adds table MeasureData to database.
 	 * 
 	 * @author GivDev
 	 * @param db The SQLite database in which the table is added.
 	 */
 	private void createMeasureData(SQLiteDatabase db)
 	{
 		db.execSQL("CREATE TABLE MeasureData (MeasureDataId INTEGER PRIMARY KEY " +
 				"AUTOINCREMENT, MeasureData REAL NOT NULL, " +
 				"MeasureDataTypeId INTEGER NOT NULL, MeasureId INTEGER NOT NULL);");
 	}
 
 	/**
 	 * Adds table Workout to database.
 	 * 
 	 * @author GivDev
 	 * @param db The SQLite database in which the table is added.
 	 */
 	private void createWorkouts(SQLiteDatabase db)
 	{
 		db.execSQL("CREATE TABLE Workouts (WorkoutId INTEGER PRIMARY KEY AUTOINCREMENT, " +
 				"WorkoutDate TEXT DEFAULT current_timestamp, WorkoutName TEXT, " +
 				"UserId INTEGER);");
 	}
 
 	/**
 	 * Adds table Measures to database.
 	 * 
 	 * @author GivDev
 	 * @param db The SQLite database in which the table is added.
 	 */
 	private void createMeasures(SQLiteDatabase db)
 	{
 		db.execSQL("CREATE TABLE Measures (MeasureId INTEGER PRIMARY KEY AUTOINCREMENT, " +
 				"MeasureData REAL NOT NULL, " +
 				"UserId INTEGER NOT NULL);");
 	}
 
 	/**
 	 * Adds table MuscleGroups to database and adds default content.
 	 * 
 	 * @author GivDev
 	 * @param db The SQLite database in which the table is added.
 	 */
 	private void createMuscleGroups(SQLiteDatabase db)
 	{
 		db.execSQL("CREATE TABLE MuscleGroups (MuscleGroupId  INTEGER PRIMARY KEY " +
 				"AUTOINCREMENT, MuscleGroupName TEXT NOT NULL);");
 		db.execSQL("INSERT INTO MuscleGroups(MuscleGroupName) VALUES ('Arms');");
 		db.execSQL("INSERT INTO MuscleGroups(MuscleGroupName) VALUES ('Chest');");
 		db.execSQL("INSERT INTO MuscleGroups(MuscleGroupName) VALUES ('Back');");
 		db.execSQL("INSERT INTO MuscleGroups(MuscleGroupName) VALUES ('Shoulders');");
 		db.execSQL("INSERT INTO MuscleGroups(MuscleGroupName) VALUES ('Abdomen');");
 		db.execSQL("INSERT INTO MuscleGroups(MuscleGroupName) VALUES ('Legs');");
 	}
 
 	/**
 	 * Adds table Muscles to database and adds default content.
 	 * 
 	 * @author GivDev
 	 * @param db The SQLite database in which the table is added.
 	 */
 	private void createMuscles(SQLiteDatabase db)
 	{
 		db.execSQL("CREATE TABLE Muscles (MuscleId INTEGER PRIMARY KEY AUTOINCREMENT, " +
 				"MuscleName TEXT NOT NULL, " +
 				"MuscleGroupId INTEGER NOT NULL, MuscleImage TEXT);");
 		db.execSQL("INSERT INTO Muscles (MuscleName, MuscleGroupId) VALUES ('Biceps', 1);");
 		db.execSQL("INSERT INTO Muscles (MuscleName, MuscleGroupId) VALUES ('Triceps', 1);");
 		db.execSQL("INSERT INTO Muscles (MuscleName, MuscleGroupId) VALUES ('Abs', 5);");
 		db.execSQL("INSERT INTO Muscles (MuscleName, MuscleGroupId) VALUES ('Gluteus Maximus',6);");
 		db.execSQL("INSERT INTO Muscles (MuscleName, MuscleGroupId) VALUES ('Straight thigh', 6);");
 		db.execSQL("INSERT INTO Muscles (MuscleName, MuscleGroupId) VALUES " +
 				"('Tailors muscle', 6);");
 		db.execSQL("INSERT INTO Muscles (MuscleName, MuscleGroupId) VALUES ('Hips', 2);");
 		db.execSQL("INSERT INTO Muscles (MuscleName, MuscleGroupId) VALUES ('Shoulders', 4);");
 		db.execSQL("INSERT INTO Muscles (MuscleName, MuscleGroupId) VALUES ('Lower back', 3);");
 		db.execSQL("INSERT INTO Muscles (MuscleName, MuscleGroupId) VALUES ('Legs',6);");
 		db.execSQL("INSERT INTO Muscles (MuscleName, MuscleGroupId) VALUES ('Thigh',6);");
 		db.execSQL("INSERT INTO Muscles (MuscleName, MuscleGroupId) VALUES ('Inner thigh',6);");
 		db.execSQL("INSERT INTO Muscles (MuscleName, MuscleGroupId) VALUES ('Back',3);");
 		db.execSQL("INSERT INTO Muscles (MuscleName, MuscleGroupId) VALUES ('Upper pecs',2);");
 		db.execSQL("INSERT INTO Muscles (MuscleName, MuscleGroupId) VALUES ('Lower pecs',2);");
 		db.execSQL("INSERT INTO Muscles (MuscleName, MuscleGroupId) VALUES ('Calf',6);");
 		db.execSQL("INSERT INTO Muscles (MuscleName, MuscleGroupId) VALUES ('Tibias',6);");
 		db.execSQL("INSERT INTO Muscles (MuscleName, MuscleGroupId) VALUES ('Upper traps',4);");
 		db.execSQL("INSERT INTO Muscles (MuscleName, MuscleGroupId) VALUES ('Lower traps',4);");
 		db.execSQL("INSERT INTO Muscles (MuscleName, MuscleGroupId) VALUES ('Traps',4);");
 		db.execSQL("INSERT INTO Muscles (MuscleName, MuscleGroupId) VALUES ('Forearm',1);");
 		db.execSQL("INSERT INTO Muscles (MuscleName, MuscleGroupId) VALUES ('Hand flexors',1);");
 		
 	}
 
 	/**
 	 * Adds table Sets to database.
 	 * 
 	 * @author GivDev
 	 * @param db The SQLite database in which the table is added.
 	 */
 	private void createSets(SQLiteDatabase db)
 	{
 		db.execSQL("CREATE TABLE Sets (SetId INTEGER PRIMARY KEY AUTOINCREMENT, SetReps INTEGER, " +
 				"SetWeight REAL, SetDistance INTEGER, WorkoutId INTEGER NOT NULL, SetDuration INTEGER, " +
 				"SetTime TEXT, ExerciseId INTEGER NOT NULL);");
 	}
 
 	/**
 	 * Adds table Sports to database and adds default content.
 	 * 
 	 * @author GivDev
 	 * @param db The SQLite database in which the table is added.
 	 */
 	private void createSports(SQLiteDatabase db)
 	{
 		db.execSQL("CREATE TABLE Sports (SportId  INTEGER PRIMARY KEY AUTOINCREMENT, SportName " +
 				"TEXT NOT NULL);");
 		db.execSQL("INSERT INTO Sports (SportName) VALUES ('Cycling');");
 		db.execSQL("INSERT INTO Sports (SportName) VALUES ('Powerwalking');");
 		db.execSQL("INSERT INTO Sports (SportName) VALUES ('Mountainbiking');");
 		db.execSQL("INSERT INTO Sports (SportName) VALUES ('Orienteering');");
 		db.execSQL("INSERT INTO Sports (SportName) VALUES ('Running');");
 		db.execSQL("INSERT INTO Sports (SportName) VALUES ('Spinning');");
 		db.execSQL("INSERT INTO Sports (SportName) VALUES ('Walking');");
 		db.execSQL("INSERT INTO Sports (SportName) VALUES ('Skiing');");
 		db.execSQL("INSERT INTO Sports (SportName) VALUES ('Swimming');");
 		db.execSQL("INSERT INTO Sports (SportName) VALUES ('Rowing');");
 		db.execSQL("INSERT INTO Sports (SportName) VALUES ('Floorball');");
 	}
 
 	/**
 	 * Adds table Users to database.
 	 * 
 	 * @author GivDev
 	 * @param db The SQLite database in which the table is added.
 	 */
 	private void createUsers(SQLiteDatabase db)
 	{
 		db.execSQL("CREATE TABLE Users (UserId INTEGER PRIMARY KEY AUTOINCREMENT, UserName " +
 				"TEXT NOT NULL, " +
 				"UserBirthday TEXT NOT NULL, UserInitialWeight REAL, UserHeight REAL);");
 	}
 
 	/**
 	 * Adds table WorkoutTemplates to database and adds deafult content.
 	 * 
 	 * @author GivDev
 	 * @param db The SQLite database in which the table is added.
 	 */
 	private void createWorkoutTemplates(SQLiteDatabase db)
 	{
 		db.execSQL("CREATE TABLE WorkoutTemplates (WorkoutTemplateId INTEGER PRIMARY KEY " +
 				"AUTOINCREMENT, WorkoutTemplateName TEXT NOT NULL, UserId INTEGER);");
 		db.execSQL("INSERT INTO WorkoutTemplates (WorkoutTemplateName) VALUES ('Upper body');");
 		db.execSQL("INSERT INTO WorkoutTemplates (WorkoutTemplateName) VALUES ('Lower body');");
 		db.execSQL("INSERT INTO WorkoutTemplates (WorkoutTemplateName) VALUES ('Full body');");
 		db.execSQL("INSERT INTO WorkoutTemplates (WorkoutTemplateName) VALUES ('Triathlon');");
 	}
 
 	/**
 	 * Adds table SetTemplates to database and adds default content.
 	 * 
 	 * @author GivDev
 	 * @param db The SQLite database in which the table is added.
 	 */
 	private void createWorkoutTemplateExercises(SQLiteDatabase db)
 	{
 		db.execSQL("CREATE TABLE WorkoutTemplateExercises (WorkoutTemplateExerciseId " +
 				"INTEGER PRIMARY KEY AUTOINCREMENT, ExerciseId INTEGER NOT NULL, " +
 				"WorkoutTemplateId INTEGER NOT NULL);");
 		db.execSQL("INSERT INTO WorkoutTemplateExercises (ExerciseId, WorkoutTemplateId) " +
 				"VALUES (1 ,1);");
 		db.execSQL("INSERT INTO WorkoutTemplateExercises (ExerciseId, WorkoutTemplateId) " +
 				"VALUES (2 ,1);");
 		db.execSQL("INSERT INTO WorkoutTemplateExercises (ExerciseId, WorkoutTemplateId) " +
 				"VALUES (3 ,1);");
 		db.execSQL("INSERT INTO WorkoutTemplateExercises (ExerciseId, WorkoutTemplateId) " +
 				"VALUES (6 ,4);");
 		db.execSQL("INSERT INTO WorkoutTemplateExercises (ExerciseId, WorkoutTemplateId) " +
 				"VALUES (7 ,4);");
 		db.execSQL("INSERT INTO WorkoutTemplateExercises (ExerciseId, WorkoutTemplateId) " +
 				"VALUES (8 ,4);");
 		db.execSQL("INSERT INTO WorkoutTemplateExercises (ExerciseId, WorkoutTemplateId) " +
 				"VALUES (1 ,3);");
 		db.execSQL("INSERT INTO WorkoutTemplateExercises (ExerciseId, WorkoutTemplateId) " +
 				"VALUES (2 ,3);");
 		db.execSQL("INSERT INTO WorkoutTemplateExercises (ExerciseId, WorkoutTemplateId) " +
 				"VALUES (3 ,3);");
 		db.execSQL("INSERT INTO WorkoutTemplateExercises (ExerciseId, WorkoutTemplateId) " +
 				"VALUES (4 ,3);");
 		db.execSQL("INSERT INTO WorkoutTemplateExercises (ExerciseId, WorkoutTemplateId) " +
 				"VALUES (5 ,3);");
 		db.execSQL("INSERT INTO WorkoutTemplateExercises (ExerciseId, WorkoutTemplateId) " +
 				"VALUES (8 ,3);");
 		db.execSQL("INSERT INTO WorkoutTemplateExercises (ExerciseId, WorkoutTemplateId) " +
 				"VALUES (4 ,1);");
 		db.execSQL("INSERT INTO WorkoutTemplateExercises (ExerciseId, WorkoutTemplateId) " +
 				"VALUES (10 ,1);");
 		db.execSQL("INSERT INTO WorkoutTemplateExercises (ExerciseId, WorkoutTemplateId) " +
 				"VALUES (11 ,2);");
 		db.execSQL("INSERT INTO WorkoutTemplateExercises (ExerciseId, WorkoutTemplateId) " +
 				"VALUES (12 ,2);");
 		db.execSQL("INSERT INTO WorkoutTemplateExercises (ExerciseId, WorkoutTemplateId) " +
 				"VALUES (13 ,2);");
 		db.execSQL("INSERT INTO WorkoutTemplateExercises (ExerciseId, WorkoutTemplateId) " +
 				"VALUES (14 ,2);");
 		db.execSQL("INSERT INTO WorkoutTemplateExercises (ExerciseId, WorkoutTemplateId) " +
 				"VALUES (10 ,3);");
 		db.execSQL("INSERT INTO WorkoutTemplateExercises (ExerciseId, WorkoutTemplateId) " +
 				"VALUES (12 ,3);");
 		db.execSQL("INSERT INTO WorkoutTemplateExercises (ExerciseId, WorkoutTemplateId) " +
 				"VALUES (13 ,3);");
 		db.execSQL("INSERT INTO WorkoutTemplateExercises (ExerciseId, WorkoutTemplateId) " +
 				"VALUES (14 ,3);");
 	}
 }
