 package org.openmrs.module.mirebalais.smoke;
 
 import static org.junit.Assert.assertTrue;
 
 import org.junit.Test;
 import org.openmrs.module.mirebalais.smoke.pageobjects.AppDashboard;
 import org.openmrs.module.mirebalais.smoke.pageobjects.LoginPage;
 import org.openmrs.module.mirebalais.smoke.pageobjects.UserAdmin;
 
 public class UserAdminTest extends BasicMirebalaisSmokeTest {
 
     private LoginPage loginPage;
     private AppDashboard appDashboard;
     private UserAdmin userAdmin;
     
     @Override
     public void specificSetUp() {
 		loginPage = new LoginPage(driver);
 		appDashboard = new AppDashboard(driver);
 		userAdmin = new UserAdmin(driver);
     }
 
     @Test
     public void registerPatientdPrintingCard() {
 		loginPage.logIn("admin", "Admin123");
     	appDashboard.openSysAdminApp();
    	userAdmin.createAccount("xxx", "yyy", createUser(), "Teste123");
     	
     	assertTrue(userAdmin.isAccountCreatedSuccesfully());
 	}
    
    //TODO: atualizar para apagar o usuário criado
    private String createUser() {
    	return new String("user" + System.currentTimeMillis());
    }
 
 }
