 package sstTest;
 
 import java.util.HashMap;
 
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Test;
 
 import sst.ShieldControl;
 import sst.Ship;
 import sst.SubSystem;
 
 public class ShipTests {
 	HashMap<String, SubSystem> mapOfSubSystems;
 
 	@Before
 	public void initializeSubSystems() {
 		mapOfSubSystems = new HashMap<String, SubSystem>();
 		mapOfSubSystems.put("Weapons", new SubSystem(2000, 200));
 		mapOfSubSystems.put("Engine", new SubSystem(1500, 100));
 		mapOfSubSystems.put("LifeSupport", new SubSystem(1000, 100));
 		mapOfSubSystems.put("ShieldControl", new ShieldControl(0));
 	}
 
 	@Test
 	public void ShipConstruction() {
 		String registration = "NCC-1701";
 		Ship ship = new Ship(registration, mapOfSubSystems);
 		Assert.assertEquals(registration, ship.getRegistration());
 	}
 	
 
 	
 
 	@Test
 	public void ShieldsAtStart() {
 		Ship ship = new Ship("NCC-1701", mapOfSubSystems);
 		Assert.assertEquals(0, ship.getShieldLevel());
 	}
 
 	@Test
 	public void TransferEnergyToShields() {
 		Ship ship = new Ship("NCC-1701", mapOfSubSystems);
 		ship.transferEnergyToShields(1000);
 		Assert.assertEquals(1000, ship.getShieldLevel());
 	}
 
 	@Test
 	public void ShipEnergyAtStart() {
 		Ship ship = new Ship("NCC-1701", mapOfSubSystems);
 		Assert.assertEquals(50000, ship.getEnergyLevel());
 	}
 
 	@Test
 	public void TransferEnergyFromShipToShields() {
 		Ship ship = new Ship("NCC-1701", 10000, mapOfSubSystems);
 		ship.transferEnergyToShields(1000);
 		Assert.assertEquals(1000, ship.getShieldLevel());
 		Assert.assertEquals(9000, ship.getEnergyLevel());
 	}
 
 	@Test
 	public void TransferMoreEnergyThanShieldsCanTake() {
 		Ship ship = new Ship("NCC-1701", 20000, mapOfSubSystems);
 		ship.transferEnergyToShields(9500);
 		ship.transferEnergyToShields(1000);
 		Assert.assertEquals(10000, ship.getShieldLevel());
 		Assert.assertEquals(10000, ship.getEnergyLevel());
 	}
 
 	@Test
 	public void TakeDamage() {
 		Ship ship = new Ship("NCC-1701", mapOfSubSystems);
 		ship.transferEnergyToShields(10000);
 		int damage = 1000;
 		ship.takeDamage(damage);
 		Assert.assertEquals(9000, ship.getShieldLevel());
 	}
 
 	@Test
 	public void TransferEnergyYouDoNotHave() {
 		Ship ship = new Ship("NCC-1701", 1000, mapOfSubSystems);
 		int deficitEnergy = ship.transferEnergyToShields(2000);
 		Assert.assertEquals(1000, ship.getShieldLevel());
 		Assert.assertEquals(-1000, deficitEnergy);
 	}
 
 	@Test
 	public void TransferExtraEnergy() {
 		Ship ship = new Ship("NCC-1701", 20000, mapOfSubSystems);
 		int extraEnergy = ship.transferEnergyToShields(20000);
 		Assert.assertEquals(10000, ship.getShieldLevel());
 		Assert.assertEquals(10000, extraEnergy);
 	}
 
 	@Test
 	public void CreateShipWithInitialEnergy() {
 		Ship ship = new Ship("NCC-1701", 2000, mapOfSubSystems);
 		int shipEnergy = ship.getEnergyLevel();
 		Assert.assertEquals(2000, shipEnergy);
 	}
 	
 	@Test
 	public void pickRandomSubSystemWithOneSubSystem() {
 		HashMap<String, SubSystem> mapOfSubSystems = new HashMap<String, SubSystem>();
 		SubSystem engine = new SubSystem(1500, 100);
 		Ship ship = new Ship("NCC-1701", 2000, mapOfSubSystems);
 		mapOfSubSystems.put("Engine", engine);
 		
 		Assert.assertSame("Engine should be returned", engine, ship.pickRandomSubSystem());
 	}
 	
 	public void pickRandomSubSystemWithMultipleSubSystems() {
 		Ship ship = new Ship("NCC-1701", 2000, mapOfSubSystems);
 		SubSystem engine = mapOfSubSystems.get("Engine");
 		Ship.generator = new MockRandom();
 		
 		Assert.assertSame("Engine should be returned", engine, ship.pickRandomSubSystem());
 	}
 
 	@Test
 	public void DamageSubSystem() {
 		Ship ship = new Ship("NCC-1701", 2000, mapOfSubSystems);
 		ship.takeDamage(200);
 		boolean subSystemIsDamaged = ship.isSubSystemDamaged();
 		Assert.assertEquals(true, subSystemIsDamaged);
 	}
 	
 	@Test
 	public void RestShipToRepairCompletely() {
 		Ship ship = new Ship("NCC-1701", 2000, mapOfSubSystems);
 		ship.takeDamage(1000);
 		ship.rest(50);
 		Assert.assertEquals(false, ship.isSubSystemDamaged());
 	}
 	
 	@Test
 	public void RestShipToRepairPartially() {
 		Ship ship = new Ship("NCC-1701", 2000, mapOfSubSystems);
 		ship.takeDamage(10000);
 		ship.rest(10);
 		Assert.assertEquals(true, ship.isSubSystemDamaged());
 
 	}
 	
 	@Test
 	public void DockingWithNoStarbaseNearby() {
 		Ship ship = new Ship("NCC-1701", 2000, mapOfSubSystems);
 		boolean docked = ship.dock(false);
 		Assert.assertEquals(false, docked);
 	}
 
 	@Test
 	public void DockingWithStarbase() {
 		Ship ship = new Ship("NCC-1701", 2000, mapOfSubSystems);
 		boolean docked = ship.dock(true);
 		Assert.assertEquals(true, docked);
 	}
 	
 	@Test
 	public void RepairWhileDocked() {
 		mapOfSubSystems = new HashMap<String, SubSystem>();
		mapOfSubSystems.put("ShieldControl", new ShieldControl(0));
 
 		Ship ship = new Ship("NCC-1701", 2000, mapOfSubSystems);
		ship.takeDamage(1000);
 		ship.dock(true);
		ship.rest(20);
 		Assert.assertEquals(false, ship.isSubSystemDamaged());
 		
 	}
 }
