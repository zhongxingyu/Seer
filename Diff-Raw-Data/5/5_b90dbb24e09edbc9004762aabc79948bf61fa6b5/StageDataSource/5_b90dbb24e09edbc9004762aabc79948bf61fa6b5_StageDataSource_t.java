 package by.bsu.courseproject.datasources;
 
 import android.content.ContentValues;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteQueryBuilder;
 import by.bsu.courseproject.model.Employee;
 import by.bsu.courseproject.model.Stage;
 import by.bsu.courseproject.stage.StageType;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Set;
 
 import static by.bsu.courseproject.db.DBConstants.Columns;
 import static by.bsu.courseproject.db.DBConstants.Tables;
 
 /**
  * User: Artyom Strok
  * Date: 24.03.13
  * Time: 12:38
  */
 public class StageDataSource {
 
   private final SQLiteDatabase db;
   private PersonDataSource personDataSource;
   private final HashMap<String, String> stageProjectionMap = new HashMap<String, String>();
   private final HashMap<String, String> stageEmployeeProjectionMap = new HashMap<String, String>();
 
   {
     stageProjectionMap.put(Columns._ID, Columns._ID);
     stageProjectionMap.put(Columns.STAGE_TYPE, Columns.STAGE_TYPE);
     stageProjectionMap.put(Columns.STAGE_PROJECT_ID, Columns.STAGE_PROJECT_ID);
     stageProjectionMap.put(Columns.STAGE_MANAGER, Columns.STAGE_MANAGER);
 
     stageEmployeeProjectionMap.put(Columns._ID, Columns._ID);
     stageEmployeeProjectionMap.put(Columns.STAGE_ID, Columns.STAGE_ID);
     stageEmployeeProjectionMap.put(Columns.EMPLOYEE_ID, Columns.EMPLOYEE_ID);
 
   }
 
 
   public StageDataSource(SQLiteDatabase db) {
     this.db = db;
   }
 
   public void persistStage(Stage stage) {
     ContentValues values = new ContentValues();
     values.put(Columns.STAGE_TYPE, stage.getType().ordinal());
     values.put(Columns.STAGE_PROJECT_ID, stage.getProject().getId());
     if (stage.getManager() != null) {
       values.put(Columns.STAGE_MANAGER, stage.getManager().getId());
     } else {
       values.put(Columns.STAGE_MANAGER, -1);
     }
     long insertId = db.insert(Tables.STAGE, null,
                               values);
     stage.setId(insertId);
   }
 
   public Stage load(Long projectId, int stageType, boolean isEmployeesNeedLoad) {
     SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
     queryBuilder.setProjectionMap(stageProjectionMap);
     queryBuilder.setTables(Tables.STAGE);
     queryBuilder.appendWhere(Columns.STAGE_PROJECT_ID + " = " + projectId + " and " + Columns.STAGE_TYPE + " = " + stageType);
     Cursor cursor = queryBuilder.query(db, null, null, null, null, null, null);
     cursor.moveToFirst();
     //TODO:add validation
     Stage stage = cursorToStage(cursor);
     if (isEmployeesNeedLoad) {
       initStageEmployees(stage);
     }
     return stage;
   }
 
   public Stage load(Long projectId, int stageType) {
     return load(projectId, stageType, false);
   }
 
 
   public void updateStage(Stage stage) {
     ContentValues values = new ContentValues();
    Employee manager = stage.getManager();
    if (manager != null) {
      values.put(Columns.STAGE_MANAGER, manager.getId());
    }
     long id = stage.getId();
     db.update(Tables.STAGE, values, Columns._ID + " = " + id, null);
     db.delete(Tables.STAGE_EMPLOYEE, Columns.STAGE_ID + " = " + id, null);
     for (Employee employee : stage.getEmployees()) {
       ContentValues contentValues = new ContentValues();
       contentValues.put(Columns.STAGE_ID, id);
       contentValues.put(Columns.EMPLOYEE_ID, employee.getId());
       db.insert(Tables.STAGE_EMPLOYEE, null, contentValues);
     }
   }
 
 
   public void deleteProjectStages(Long projectId) {
     db.delete(Tables.STAGE_EMPLOYEE,
               Columns.STAGE_ID + " IN ( SELECT " + Columns._ID + " FROM " + Tables.STAGE + " WHERE " + Columns.STAGE_PROJECT_ID + " = " + projectId + ")",
               null);
     db.delete(Tables.STAGE, Columns.STAGE_PROJECT_ID + " = " + projectId, null);
   }
 
   Stage cursorToStage(Cursor cursor) {
     Stage stage = new Stage();
     stage.setId(cursor.getLong(cursor.getColumnIndex(Columns._ID)));
     stage.setType(StageType.values()[cursor.getInt(cursor.getColumnIndex(Columns.STAGE_TYPE))]);
     Long managerID = cursor.getLong(cursor.getColumnIndex(Columns.STAGE_MANAGER));
     if (managerID != -1) {
       stage.setManager((Employee) personDataSource.load(managerID));
     }
     return stage;
   }
 
   public void setPersonDataSource(PersonDataSource personDataSource) {
     this.personDataSource = personDataSource;
   }
 
   private void initStageEmployees(Stage stage) {
     SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
     queryBuilder.setProjectionMap(stageEmployeeProjectionMap);
     queryBuilder.setTables(Tables.STAGE_EMPLOYEE);
     queryBuilder.appendWhere(Columns.STAGE_ID + " = " + stage.getId());
     Cursor cursor = queryBuilder.query(db, null, null, null, null, null, null);
     cursor.moveToFirst();
     Set<Employee> employees = new HashSet<Employee>();
     while (!cursor.isAfterLast()) {
       Long employeeId = cursor.getLong(cursor.getColumnIndex(Columns.EMPLOYEE_ID));
       employees.add((Employee) personDataSource.load(employeeId));
       cursor.moveToNext();
     }
     stage.setEmployees(employees);
   }
 }
