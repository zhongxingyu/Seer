 package by.bsu.courseproject.ui;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.TableLayout;
 import android.widget.TableRow;
 import android.widget.TextView;
 import by.bsu.courseproject.PMApplication;
 import by.bsu.courseproject.R;
 import by.bsu.courseproject.db.DBConstants;
 import by.bsu.courseproject.db.ProjectManagerProvider;
 import by.bsu.courseproject.model.Employee;
 import by.bsu.courseproject.stage.StageType;
 
 import java.util.HashSet;
 import java.util.Set;
 
 /**
  * User: Artyom Strok
  * Date: 06.04.13
  * Time: 23:01
  */
 public class Stage extends Activity implements View.OnClickListener {
   public static final int REQUEST_MANAGER = 301;
   public static final int REQUEST_EMPLOYEE = 302;
   private by.bsu.courseproject.model.Stage stage;
   private Set<Long> stageEmployeesIds = new HashSet<Long>();
 
   @Override
   protected void onCreate(Bundle savedInstanceState) {
     super.onCreate(savedInstanceState);
     setContentView(R.layout.stage);
     addListeners();
     fillForm(getIntent());
   }
 
   private void fillForm(Intent intent) {
     this.setTitle("Стадия");
     Long projectId = intent.getLongExtra(DBConstants.Columns.STAGE_PROJECT_ID, -1L);
     int stageType = intent.getIntExtra(DBConstants.Columns.STAGE_TYPE, -1);
     if (!projectId.equals(-1L) && stageType != -1) {
       ((TextView) findViewById(R.id.stageName)).setText(StageType.values()[stageType].toString());
       ((EditText) findViewById(R.id.editTextProjectName)).setText(intent.getStringExtra(DBConstants.Columns.PROJECT_PROJECTNAME));
       stage = PMApplication.getPMDB().getStageDataSource().load(projectId, stageType, true);
       Employee manager = stage.getManager();
       if (manager != null) {
         ((EditText) findViewById(R.id.stageManagerFN)).setText(manager.getFirstName());
         ((EditText) findViewById(R.id.stageManagerP)).setText(manager.getMiddleName());
         ((EditText) findViewById(R.id.stageManagerLN)).setText(manager.getLastName());
       }
     }
     TableLayout tableLayout = (TableLayout) findViewById(R.id.employeeTable);
     for (Employee employee : stage.getEmployees()) {
       addEmployee(tableLayout, employee);
     }
 
   }
 
   private void addEmployee(TableLayout tableLayout, Employee employee) {
     LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
     TableRow tableRow = (TableRow) inflater.inflate(R.layout.stage_employees,
                                                     null);
     ((TextView) tableRow.findViewById(R.id.empFN)).setText(employee.getFirstName());
     ((TextView) tableRow.findViewById(R.id.empLN)).setText(employee.getLastName());
     ((TextView) tableRow.findViewById(R.id.empMN)).setText(employee.getMiddleName());
     tableLayout.addView(tableRow);
   }
 
   private void addListeners() {
     findViewById(R.id.stageButtonSave).setOnClickListener(this);
     findViewById(R.id.stageButtonCancel).setOnClickListener(this);
     findViewById(R.id.addEmloyee).setOnClickListener(this);
 
     findViewById(R.id.stageManagerFN).setOnClickListener(this);
     findViewById(R.id.stageManagerLN).setOnClickListener(this);
     findViewById(R.id.stageManagerP).setOnClickListener(this);
   }
 
   @Override
   protected void onResume() {
     super.onResume();
   }
 
  @Override
   public void onClick(View view) {
     switch (view.getId()) {
     case R.id.stageButtonCancel:
       finish();
       break;
     case R.id.stageButtonSave:
       PMApplication.getPMDB().getStageDataSource().updateStage(stage);
       finish();
       break;
     case R.id.stageManagerFN:
     case R.id.stageManagerLN:
     case R.id.stageManagerP:
       callList(true);
       break;
     case R.id.addEmloyee:
       callList(false);
       break;
     }
   }
 
   private void callList(boolean isManager) {
     Intent intent = new Intent();
     intent.setClass(Stage.this, CatalogueFragment.class);
 
     String[] projection = new String[]{
         DBConstants.Columns._ID,
         DBConstants.Columns.PERSON_DISCRIMINATOR,
         DBConstants.Columns.PERSON_FIRSTNAME,
         DBConstants.Columns.PERSON_MIDDLENAME,
         DBConstants.Columns.PERSON_LASTNAME,
         DBConstants.Columns.PERSON_EMAIL,
         DBConstants.Columns.PERSON_SKYPE,
         DBConstants.Columns.PERSON_PHONE
     };
 
     intent.setData(ProjectManagerProvider.PERSON_URI);
     intent.putExtra(CatalogueListFragment.ARG_FILTER_COLUMNS,
                     new String[]{DBConstants.Columns.PERSON_FIRSTNAME, DBConstants.Columns.PERSON_MIDDLENAME,
                                  DBConstants.Columns.PERSON_LASTNAME,
                                  DBConstants.Columns.PERSON_EMAIL,
                                  DBConstants.Columns.PERSON_SKYPE,
                                  DBConstants.Columns.PERSON_PHONE});
     intent.putExtra(CatalogueListFragment.ARG_ADAPTER_FROM,
                     new String[]{DBConstants.Columns.PERSON_LASTNAME, DBConstants.Columns.PERSON_FIRSTNAME, DBConstants.Columns.PERSON_MIDDLENAME,
 
                                  DBConstants.Columns.PERSON_PHONE});
     intent.putExtra(CatalogueListFragment.ARG_ADAPTER_TO,
                     new int[]{R.id.fName, R.id.mName, R.id.lName, R.id.phone});
 
     intent.setAction(Intent.ACTION_PICK);
 
     intent.putExtra(CatalogueFragment.FROM_LIST, CatalogueList.EMPLOYEE);
     intent.putExtra(CatalogueListFragment.ARG_PROJECTION, projection);
     if (isManager) {
       startActivityForResult(intent, REQUEST_MANAGER);
     } else {
       startActivityForResult(intent, REQUEST_EMPLOYEE);
     }
 
   }
 
   @Override
   public void onActivityResult(int requestCode, int resultCode, Intent data) {
     super.onActivityResult(requestCode, resultCode, data);
     if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_MANAGER) {
       Cursor cursor = managedQuery(data.getData(), new String[]{DBConstants.Columns._ID,
                                                                 DBConstants.Columns.PERSON_FIRSTNAME,
                                                                 DBConstants.Columns.PERSON_MIDDLENAME,
                                                                 DBConstants.Columns.PERSON_LASTNAME}, null, null, null);
 
       if (cursor != null && cursor.moveToFirst()) {
         ((EditText) findViewById(R.id.stageManagerFN)).setText(cursor.getString(cursor.getColumnIndex(DBConstants.Columns.PERSON_FIRSTNAME)));
         ((EditText) findViewById(R.id.stageManagerP)).setText(cursor.getString(cursor.getColumnIndex(DBConstants.Columns.PERSON_MIDDLENAME)));
         ((EditText) findViewById(R.id.stageManagerLN)).setText(cursor.getString(cursor.getColumnIndex(DBConstants.Columns.PERSON_LASTNAME)));
         Long managerID = cursor.getLong(cursor.getColumnIndex(DBConstants.Columns._ID));
         Employee manager = new Employee();
         manager.setId(managerID);
         stage.setManager(manager);
       }
     } else if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_EMPLOYEE) {
       Cursor cursor = managedQuery(data.getData(), new String[]{DBConstants.Columns._ID,
                                                                 DBConstants.Columns.PERSON_FIRSTNAME,
                                                                 DBConstants.Columns.PERSON_MIDDLENAME,
                                                                 DBConstants.Columns.PERSON_LASTNAME}, null, null, null);
 
       if (cursor != null && cursor.moveToFirst()) {
         Long employeeId = cursor.getLong(cursor.getColumnIndex(DBConstants.Columns._ID));
         if (stageEmployeesIds.add(employeeId)) {
           Employee employee = new Employee();
           employee.setId(employeeId);
           employee.setLastName(cursor.getString(cursor.getColumnIndex(DBConstants.Columns.PERSON_LASTNAME)));
           employee.setFirstName(cursor.getString(cursor.getColumnIndex(DBConstants.Columns.PERSON_FIRSTNAME)));
           employee.setMiddleName(cursor.getString(cursor.getColumnIndex(DBConstants.Columns.PERSON_MIDDLENAME)));
           stage.getEmployees().add(employee);
           TableLayout tableLayout = (TableLayout) findViewById(R.id.employeeTable);
           addEmployee(tableLayout, employee);
         }
       }
     }
   }
 
 }
