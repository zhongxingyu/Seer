 package shu.journal;
 
 import android.database.Cursor;
 
 public abstract class TestDatabase{
 	DBAdapter databaseTest = new DBAdapter(null);
 	private String[][] userData = new String[0][10];
 	Cursor cursor;
 	private Long user_id;
 	void fillStringArray(){
 		userData[0][0]="tUser";
 		userData[0][1]="tPassword";
 		userData[0][2]="tlocation";
 		userData[0][3]="first";
 		userData[0][4]="last";
 		userData[0][5]="q1";
 		userData[0][6]="q2";
 		userData[0][7]= "q3";
 		userData[0][8]= "a1";
 		userData[0][9]="a2";
 		userData[0][10]="a3";
 		userData[1][0]="Username";
 		userData[1][1]="Password";
 		userData[1][2]="location";
 		userData[1][3]="F Name";
 		userData[1][4]="L Name";
 		userData[1][5]="Question 1";
 		userData[1][6]="Question 2";
 		userData[1][7]="Question3";
 		userData[1][8]="Answer1";
 		userData[1][9]="Answer2";
 		userData[1][10]="Answer3";
 	}
 	void testAddUser() {
 		System.out.println("Testing adding user to the database");
 		databaseTest.insertUser("tUser", "tPassword", "tlocation", "first", "last", "q1", "q2", "q3", "a1", "a2", "a3");
 		cursor = databaseTest.getFirstUser();
 		int n;
 		for (n=0;n>10;n++){
 			if(userData[0][n]==cursor.getString((int)n+1)){
 				System.out.println(userData[1][n]+"=Found");
 			}
 			else
 				System.out.println(userData[1][n]+"=Not Found");
 		}
 		
 	}
 	void testUserExists(){
 		System.out.println("Testing user exists check");
 		cursor = databaseTest.getFirstUser();
 		if(databaseTest.checkUserExists(cursor.getString(1))==true)
 			System.out.println("User exists");
 		else if(databaseTest.checkUserExists(cursor.getString(1))==false)
 			System.out.println("User does not exist");
 		else
 			System.out.println("Cant verify if user exists, check function");
 		
 	}
 	void testPasswordCheck(){
 		System.out.println("Testing password check");
 		cursor = databaseTest.getFirstUser();
 		Long passwordReturn;
 		passwordReturn = databaseTest.checkPassword(cursor.getString(1), "tPassword");
 		if(passwordReturn == -1)
 			System.out.println("Password check failed");
 		else
 			System.out.println("Password check successful");
 	}
 	void testAccountLockCheck()
 	{
 		System.out.println("Testing account lock check");
 		cursor = databaseTest.getFirstUser();
 		boolean accountLocked;
 		accountLocked=databaseTest.getLockStatus(cursor.getString(1));
		if(accountLocked = true)
 			System.out.println("Lock check successful, account is locked");
		else if(accountLocked = false)
 			System.out.println("Lock check successful, account is unlocked");
 		else
 			System.out.println("Lock check unsuccessful");
 	}
 	void testAddPage(){
 		System.out.println("Testing adding a journal page");
 		cursor = databaseTest.getFirstUser();
 		databaseTest.insertPage(cursor.getLong(1), "Journal Entry");
 		System.out.println("Page added successfully");
 	}
 	void testGetPage()
 	{
 		System.out.println("Testing retrieving a journal page");
 		cursor = databaseTest.getFirstUser();
 		Cursor c = databaseTest.getPageById(cursor.getLong(1));
 		if (c.getString(3)=="Journal Entry")
 			System.out.println("Page retrieved successfuly -- "+c.getString(3));
 		else
 			System.out.println("Could not retrieve page -- "+c.getString(3));
 	}
 	void testModifyPage(){
 		System.out.println("Testing modifying");
 		cursor = databaseTest.getFirstUser();
 		Cursor c = databaseTest.getPageById(cursor.getLong(1));
 		databaseTest.updatePage(c.getLong(0), "Modify Entry");
 		System.out.println("Page successfully modified");
 	}
 	void testDeletePage(){
 		System.out.println("Testing deleting a journal page");
 		cursor = databaseTest.getFirstUser();
 		Cursor c = databaseTest.getPageById(cursor.getLong(1));
 		databaseTest.deletePage(c.getPosition());
 	}
 }
