 package com.suicune.notasupna;
 
 import java.util.ArrayList;
 
 import android.database.Cursor;
 
 import com.suicune.notasupna.database.GradesContract;
 
 public class Student {
 	public String mStudentName = "";
 	public String mStudentSurname1;
 	public String mStudentSurname2;
 	public String mStudentFullName;
 	public String mStudentNia;
 	public String mStudentNif;
 	public String mStudentNip;
 
 	public ArrayList<Record> mRecordList;
 	public int mRecordCount = 0;
 
 	public Student(Cursor c) {
 		mRecordList = new ArrayList<Record>();
 		if (c == null || c.getCount() <= 0) {
 			return;
 		}else{
 			c.moveToFirst();
 			c.moveToPrevious();
 		}
 
 		// Auxiliary variables
 		int currentCourseId = -1;
 		String currentSubjectName = "";
 		long currentGradeTime = -1;
 		int currentGradeSubject = -1;
 		Subject currentSubject = null;
 		Grade currentGrade = null;
 		Record currentRecord = null;
 
 		while (c.moveToNext()) {
 			// Add the student information
 			String studentName = c.getString(c
 					.getColumnIndex(GradesContract.StudentsTable.COL_ST_NAME));
 			if (!studentName.equalsIgnoreCase(mStudentName)) {
 				mStudentName = studentName;
 				mStudentSurname1 = c
 						.getString(c
 								.getColumnIndex(GradesContract.StudentsTable.COL_ST_SURNAME_1));
 				mStudentSurname2 = c
 						.getString(c
 								.getColumnIndex(GradesContract.StudentsTable.COL_ST_SURNAME_2));
 				mStudentNia = c
 						.getString(c
 								.getColumnIndex(GradesContract.StudentsTable.COL_ST_NIA));
 				mStudentNif = c
 						.getString(c
 								.getColumnIndex(GradesContract.StudentsTable.COL_ST_NIF));
 				mStudentNip = c
 						.getString(c
 								.getColumnIndex(GradesContract.StudentsTable.COL_ST_NIP));
 				mStudentFullName = mStudentSurname1 + " " + mStudentSurname2
 						+ ", " + mStudentName;
 			}
 
 			// Process the course
 			int courseId = c.getInt(c
 					.getColumnIndex(GradesContract.CoursesTable._ID));
 			if (currentCourseId != courseId) {
 				if (currentRecord != null) {
 					currentRecord.addSubject(currentSubject);
 					addRecord(currentRecord);
 					
 				}
 				currentSubject = null;
 				currentCourseId = courseId;
 				currentRecord = new Record(
 						currentCourseId,
 						c.getString(c
 								.getColumnIndex(GradesContract.CoursesTable.COL_CO_NAME)),
 						c.getString(c
 								.getColumnIndex(GradesContract.CoursesTable.COL_CO_CENTER)),
 						c.getString(c
 								.getColumnIndex(GradesContract.CoursesTable.COL_CO_STUDIES)),
 						c.getString(c
 								.getColumnIndex(GradesContract.CoursesTable.COL_CO_TOTAL_CREDITS)),
 						c.getString(c
 								.getColumnIndex(GradesContract.CoursesTable.COL_CO_PASSED_CREDITS)),
 						c.getString(c
 								.getColumnIndex(GradesContract.CoursesTable.COL_CO_LANGUAGE)));
 			}
 
 			// Process the subject
 			String subjectName = c.getString(c
 					.getColumnIndex(GradesContract.SubjectsTable.COL_SU_NAME));
 			if (!currentSubjectName.equalsIgnoreCase(subjectName)) {
 				if(currentSubject != null){
 					currentRecord.addSubject(currentSubject);
 				}
 				currentSubjectName = subjectName;
 				currentSubject = new Subject(
 						c.getString(c
 								.getColumnIndex(GradesContract.SubjectsTable.COL_SU_NAME)),
 						c.getString(c
 								.getColumnIndex(GradesContract.SubjectsTable.COL_SU_TYPE)),
 						c.getString(c
 								.getColumnIndex(GradesContract.SubjectsTable.COL_SU_LANGUAGE)),
 						Integer.valueOf(
 								c.getString(c
 										.getColumnIndex(GradesContract.SubjectsTable.COL_SU_CO_CODE)))
 								.intValue(),
 						c.getString(c
 								.getColumnIndex(GradesContract.SubjectsTable.COL_SU_CREDITS)));
 			}
 
 			// Process the grade
 			long gradeTime = c.getLong(c
 					.getColumnIndex(GradesContract.GradesTable.COL_GR_TIME));
 			int gradeSubject = c.getInt(c
 					.getColumnIndex(GradesContract.GradesTable.COL_GR_SU_CODE));
 			if (currentGradeTime != gradeTime
 					|| currentGradeSubject != gradeSubject) {
 				currentGradeSubject = gradeSubject;
 				currentGradeTime = gradeTime;
 				currentGrade = new Grade(
 						currentSubject,
 						c.getString(c
 								.getColumnIndex(GradesContract.GradesTable.COL_GR_GRADE_NAME)),
 						c.getString(c
 								.getColumnIndex(GradesContract.GradesTable.COL_GR_GRADE)),
 						c.getString(c
 								.getColumnIndex(GradesContract.GradesTable.COL_GR_CALL)),
 						Integer.valueOf(
 								c.getString(c
 										.getColumnIndex(GradesContract.GradesTable.COL_GR_CALL_NUMBER)))
 								.intValue(),
 						c.getString(c
 								.getColumnIndex(GradesContract.GradesTable.COL_GR_CODE)),
 						c.getString(c
 								.getColumnIndex(GradesContract.GradesTable.COL_GR_PASSED)),
 						c.getString(c
 								.getColumnIndex(GradesContract.GradesTable.COL_GR_PROVISIONAL)),
 						Long.valueOf(
 								c.getString(c
 										.getColumnIndex(GradesContract.GradesTable.COL_GR_REVISION_TIME)))
 								.longValue(),
 						c.getString(c
 								.getColumnIndex(GradesContract.GradesTable.COL_GR_ASSISTED)),
 						Long.valueOf(
 								c.getString(c
 										.getColumnIndex(GradesContract.GradesTable.COL_GR_TIME)))
 								.longValue(),
 						c.getString(c
 								.getColumnIndex(GradesContract.GradesTable.COL_GR_YEAR)),
 						c.getString(c
 								.getColumnIndex(GradesContract.GradesTable.COL_GR_LANGUAGE)));
 				currentSubject.addGrade(currentGrade);
 			}
 		}
 		if(currentRecord != null){
 			currentRecord.addSubject(currentSubject);
 			addRecord(currentRecord);
 		}
 	}
 
 	public void addRecord(Record r) {
 		mRecordList.add(r);
 		mRecordCount++;
 	}
 }
