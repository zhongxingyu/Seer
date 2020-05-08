 /*
  */
 package com.github.omwah.SDFEconomy;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import net.milkbowl.vault.economy.EconomyResponse;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.Rule;
 
 import org.junit.rules.TemporaryFolder;
 
 import org.junit.runner.RunWith;
 import org.junit.runners.JUnit4;
 
 import static org.junit.Assert.*;
 
 import org.bukkit.configuration.MemoryConfiguration;
 
 import net.milkbowl.vault.economy.EconomyResponse.ResponseType;
 
 /**
  * Tests the SDFEconomyAPI class
  */
 @RunWith(JUnit4.class)
 public class SDFEconomyAPITest {
     private SDFEconomyAPI api;
     
     @Rule
     public TemporaryFolder folder = new TemporaryFolder();
         
     @Before
     public void SetupAPI() {
         // Create an API object using a memory only configuration and a test
         // location translator that returns known values
         File storage_file = new File(folder.getRoot(), "api_test_accounts.yml");
         
         // Add test date using storage, not API
         EconomyYamlStorage storage = new EconomyYamlStorage(storage_file, false);
         
         // These will both match up, w/o specifying world name
         storage.createPlayerAccount("Player1", "World1", 10.0);
         storage.createPlayerAccount("Player2", "World2", 40.0);
         
         // This player is not in his default location per the test location translator
         storage.createPlayerAccount("Player3", "World1", 50.0);
         
         storage.createBankAccount("Bank1", "Player1", "World1", 101.00);
         BankAccount bank = storage.getBankAccount("Bank1");
         bank.addMember("Player2");
         bank.addMember("Player3");
         
         this.api = new SDFEconomyAPI(new MemoryConfiguration(), storage, new TestLocationTranslator());
     }
     
     @Test
     public void format() {
        assertEquals("1.00 simoleon", api.format(1.0));
        assertEquals("10.00 simoleons", api.format(10.0));
     }
     
     @Test
     public void getPlayerLocationName() {
         // This really just ends up testing that we can pass the right
         // string from the unit test location translator
         assertEquals("world1", api.getPlayerLocationName("Player1"));
         assertEquals("world1", api.getPlayerLocationName("player1"));
         assertEquals("world2", api.getPlayerLocationName("Player2"));
         assertEquals("world2", api.getPlayerLocationName("player2"));
         assertEquals(null, api.getPlayerLocationName("NullPlayer"));
     }
 
     @Test
     public void validLocationName() {
         assertTrue("World1 should be valid", api.validLocationName("World1"));
         assertTrue("WORLD1 should be valid", api.validLocationName("WORLD1"));
         assertTrue("world2 should be valid", api.validLocationName("world2"));
         assertFalse("null should be invalid", api.validLocationName(null));
         assertFalse("World4 should be invalid", api.validLocationName("World4"));
     }
      
     @Test
     public void playerNames() {
         // Player names returned as lower case only
         ArrayList<String> expected_names = new ArrayList<String>();
         expected_names.add("player1");
         expected_names.add("player3");
         assertEquals(expected_names, api.getPlayers("World1"));
 
         assertEquals(Collections.<String>emptyList(), api.getPlayers(null));
         assertEquals(Collections.<String>emptyList(), api.getPlayers("BadWorldName"));
     }
     
     @Test
     public void createPlayerAccount() {
         assertFalse("Player1 account should not be creatable in World1, already exists", api.createPlayerAccount("Player1"));
         assertFalse("Player2 account should not be creatable in World2, already exists", api.createPlayerAccount("Player2"));
         assertFalse("Player3 account should not be creatable in World1, already exists", api.createPlayerAccount("Player3", "World1"));
         
         assertFalse("Player1 account should not be creatable with null location", api.createPlayerAccount("Player1", null));
 
         // Since Player3 has no account in World3 yet
         assertTrue("Player1 account should be creatable in World3", api.createPlayerAccount("Player3"));
     }
 
     @Test
     public void deletePlayerAccount() {
         assertTrue("Player1 account be deletable in World1", api.deletePlayerAccount("Player1", "World1"));
         assertFalse("Player1 account should no longer exist in World1", api.hasAccount("Player1", "World1"));
         assertFalse("Player1 should have already been deleted", api.deletePlayerAccount("Player1", "World1"));
         
         assertFalse("Should fail when deleting null/null", api.deletePlayerAccount(null, null));
         assertFalse("Should fail when deleting Player2/null", api.deletePlayerAccount("Player2", null));
         assertFalse("Should fail when deleting Player2/null", api.deletePlayerAccount(null, "World2"));
     }
     
     @Test
     public void hasPlayerAccount() {
         assertTrue(api.hasAccount("Player1"));
         assertTrue(api.hasAccount("Player2"));
         
         assertFalse(api.hasAccount("Player3"));
         assertTrue(api.hasAccount("Player3", "World1"));
         
         assertFalse(api.hasAccount("NullPlayer"));
     }
     
     @Test
     public void getPlayerBalance() {
         assertEquals(10.0, api.getBalance("Player1"), 1e-6);
         assertEquals(10.0, api.getBalance("player1"), 1e-6);
         assertEquals(40.0, api.getBalance("Player2"), 1e-6);
         assertEquals(40.0, api.getBalance("player2"), 1e-6);
         assertEquals(50.0, api.getBalance("Player3", "World1"), 1e-6);
         assertEquals(50.0, api.getBalance("player3", "World1"), 1e-6);
         
         assertEquals(0.0, api.getBalance("NullPlayer"), 1e-6);
         assertEquals(0.0, api.getBalance("NullPlayer", "World1"), 1e-6);
 
     }
     
     @Test
     public void setPlayerBalance() {
         assertTrue("Player1's balance should have been set", api.setBalance("Player1", 20.0));
         assertEquals(20.0, api.getBalance("Player1"), 1e-6);
 
         assertTrue("Player3's balance should have been set", api.setBalance("Player3", "World1", 60.0));
         assertEquals(60.0, api.getBalance("Player3", "World1"), 1e-6);
         
         assertFalse("Player3's balance should not have been set", api.setBalance("Player3", 100.0));
     }
     
     @Test
     public void playerHasAmount() {
         assertTrue("Player1 should have 15.00", api.has("Player1", 9.0));
         assertFalse("Player1 should not have negative 15.00", api.has("Player1", -9.0));
         assertTrue("Player2 should have 45.00", api.has("Player2", 39.0));
         assertFalse("Player2 should not have negative 45.00", api.has("Player2", -39.0));
         
         assertTrue("NullPlayer should have 0.00", api.has("NullPlayer", 0.0));
         assertFalse("NullPlayer should not have 1.00", api.has("NullPlayer", 1.0));
     }
     
     @Test
     public void withdrawPlayer() {
         assertTrue("Withdraw of 10.0 from Player1 should succeeed", api.withdrawPlayer("Player1", 5).type == ResponseType.SUCCESS);
         assertEquals(5.0, api.getBalance("Player1"), 1e-6);
         assertTrue("Withdraw of 1.0 from Player1 should not succeeed", api.withdrawPlayer("Player1", 6).type == ResponseType.FAILURE);
  
         assertTrue("Withdraw of 40.0 from Player2 should succeeed", api.withdrawPlayer("Player2", 20).type == ResponseType.SUCCESS);
         assertEquals(20.0, api.getBalance("Player2"), 1e-6);
         assertTrue("Withdraw of 1.0 from Player2 should not succeeed", api.withdrawPlayer("Player2", 21).type == ResponseType.FAILURE);
  
         assertTrue("Withdraw of 1.0 from NullPlayer should not succeeed", api.withdrawPlayer("NullPlayer", 1).type == ResponseType.FAILURE);
     }
     
     @Test
     public void depositPlayer() {
         assertTrue("Deposit of 10.0 to Player1 should succeeed", api.depositPlayer("Player1", 10).type == ResponseType.SUCCESS);
         assertEquals(20.0, api.getBalance("Player1"), 1e-6);
         assertTrue("Deposit of 40.0 to Player2 should succeeed", api.depositPlayer("Player2", 40).type == ResponseType.SUCCESS);
         assertEquals(80.0, api.getBalance("Player2"), 1e-6);
 
         assertTrue("Deposit of 1.0 to NullPlayer should not succeeed", api.depositPlayer("NullPlayer", 1).type == ResponseType.FAILURE);
     }
     
     @Test
     public void bankNames() {
         ArrayList<String> expected_names = new ArrayList<String>();
         expected_names.add("bank1");
         
         assertEquals(expected_names, api.getBankNames());
     }
     
     @Test
     public void allBanks() {
         api.createBank("Bank2", "Player2");
         List<BankAccount> all_banks = api.getAllBanks();
         assertEquals("Should have 2 bank accounts", 2, all_banks.size());
         assertEquals("bank1", all_banks.get(0).getName());
         assertEquals("bank2", all_banks.get(1).getName());
     }
     
     @Test
     public void playerBanks() {
         api.createBank("Bank2", "Player2");
         List<BankAccount> player_banks = api.getPlayerBanks("Player1", "World1");
         assertEquals("Should have 1 bank account for Player1", 1, player_banks.size());
         assertEquals("bank1", player_banks.get(0).getName());
         
         player_banks = api.getPlayerBanks("Player2", "World2");
         assertEquals("Should have 1 bank account for Player2", 1, player_banks.size());
         assertEquals("bank2", player_banks.get(0).getName());
     }
     
     @Test
     public void getBank() {
         BankAccount bank = api.getBankAccount("bank1");
         assertEquals("bank1", bank.getName());
         assertEquals("player1", bank.getOwner());
     }
 
     @Test
     public void createBank () {
         assertEquals("Should have success creating Bank2 for Player2", ResponseType.SUCCESS, api.createBank("Bank2", "Player2").type);
         assertEquals("Should have failure creating Bank2 for Player2, again", ResponseType.FAILURE, api.createBank("Bank2", "Player2").type);
         assertEquals("Should have failure for creating Bank3 for null player", ResponseType.FAILURE, api.createBank("Bank3", null).type);
         assertEquals("Should have failure for creating Bank3 for null location", ResponseType.FAILURE, api.createBank("Bank3", "Player3", null).type);
     }
     
     @Test
     public void deleteBank() {
         assertEquals("Should have success when deleting Bank1", ResponseType.SUCCESS, api.deleteBank("bank1").type);
         assertEquals("Should have failure when deleting Bank1, which no longer exists", ResponseType.FAILURE, api.deleteBank("bank1").type);
     }
     
     @Test
     public void getBankBalance() {
         EconomyResponse bank1 = api.bankBalance("Bank1");
         assertEquals("Bank balance query should have been successful", ResponseType.SUCCESS, bank1.type);
         assertEquals("Bank balance should be 101.0", 101.0, bank1.balance, 1e-6);
         assertEquals("Query for balance of Bank2 should trigger a failure", ResponseType.FAILURE, api.bankBalance("Bank2").type);
         assertEquals("Query for balance of null should trigger a failure", ResponseType.FAILURE, api.bankBalance(null).type);
     }
     
     @Test
     public void hasBankAmount() {
         EconomyResponse bank1 = api.bankHas("Bank1", 100.0);
         assertEquals("Bank has query should have been a success", ResponseType.SUCCESS, bank1.type);
         assertEquals("Bank should have a balance of 101.0", 101.0, bank1.balance, 1.0e-6);
         assertEquals("Bank1 should have at least 101.00", ResponseType.SUCCESS, api.bankHas("Bank1", 101.0).type);
         assertEquals("Bank1 should not have at least 102.00", ResponseType.FAILURE, api.bankHas("Bank1", 102.0).type);
         assertEquals("Query on null bank should be a failure", ResponseType.FAILURE, api.bankHas(null, 0.0).type);
     }
 
     @Test
     public void bankWithdraw() {
         EconomyResponse bank1 = api.bankWithdraw("Bank1", 11.0);
         assertEquals("Bank withdraw should have been a success", ResponseType.SUCCESS, bank1.type);
         assertEquals("Bank should have a balance of 90.0", 90.0, bank1.balance, 1.0e-6);
         assertEquals("Transaction should have taken out 11.0", 11.0, bank1.amount, 1.0e-6);   
         assertEquals("Bank1 withdraw of 102.0 should have failed", ResponseType.FAILURE, api.bankWithdraw("Bank1", 102.0).type);
         assertEquals("Bank2 withdraw should have failed", ResponseType.FAILURE, api.bankWithdraw("Bank2", 1.0).type);
         assertEquals("null withdraw should have failed", ResponseType.FAILURE, api.bankWithdraw(null, 1.0).type);
     }
     
     @Test
     public void bankDeposit() {
         EconomyResponse bank1 = api.bankDeposit("Bank1", 9.0);
         assertEquals("Bank deposit should have been a success", ResponseType.SUCCESS, bank1.type);
         assertEquals("Bank should have a balance of 110.0", 110.0, bank1.balance, 1.0e-6);
         assertEquals("Transaction should have deposited out 11.0", 9.0, bank1.amount, 1.0e-6);
         assertEquals("Bank2 deposit should have failed", ResponseType.FAILURE, api.bankDeposit("Bank2", 1.0).type);
         assertEquals("null deposit should have failed", ResponseType.FAILURE, api.bankDeposit(null, 1.0).type);
     }
     
     @Test
     public void isBankOwner() {
         assertEquals("Player1 should be an owner of Bank1", ResponseType.SUCCESS, api.isBankOwner("Bank1", "Player1").type);
         assertEquals("Player1 should not be an owner of Bank1 @ World2", ResponseType.FAILURE, api.isBankOwner("Bank1", "Player1", "World2").type);
         assertEquals("Player2 should not be an owner of Bank1", ResponseType.FAILURE, api.isBankOwner("Bank1", "Player2").type);
         assertEquals("Player3 should not be an owner of Bank1", ResponseType.FAILURE, api.isBankOwner("Bank1", "Player3").type);
         assertEquals("null should not be an owner of Bank1", ResponseType.FAILURE, api.isBankOwner("Bank1", null).type);
         assertEquals("null should not be an owner of null", ResponseType.FAILURE, api.isBankOwner(null, null).type);          
     }
     
     @Test
     public void isBankMember() {
         assertEquals("Player1 should be a member of Bank1", ResponseType.SUCCESS, api.isBankMember("bank1", "Player1").type);
         assertEquals("Player2 should not be a member of Bank1 @ World1", ResponseType.FAILURE, api.isBankMember("Bank1", "Player2").type);
         assertEquals("Player3 should not be a member of Bank1 @ World3", ResponseType.FAILURE, api.isBankMember("Bank1", "Player3").type);
         assertEquals("Player3 should not be a member of Bank1 @ World1", ResponseType.SUCCESS, api.isBankMember("bANK1", "Player3", "WORLD1").type);
         assertEquals("null should not be an member of Bank1", ResponseType.FAILURE, api.isBankMember("Bank1", null).type);
         assertEquals("null should not be an member of null", ResponseType.FAILURE, api.isBankMember(null, null).type);          
     }
 }
