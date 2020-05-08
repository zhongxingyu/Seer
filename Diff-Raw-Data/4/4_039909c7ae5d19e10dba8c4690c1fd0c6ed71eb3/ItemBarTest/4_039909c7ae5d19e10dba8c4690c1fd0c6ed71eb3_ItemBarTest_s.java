 package ultraextreme.model;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import junit.framework.TestCase;
 import ultraextreme.model.item.AbstractWeapon;
 import ultraextreme.model.item.BasicWeapon;
 import ultraextreme.model.item.BulletManager;
 import ultraextreme.model.item.ItemBar;
 import ultraextreme.model.util.PlayerID;
 import ultraextreme.model.util.Position;
 
 /**
  * 
  * @author Daniel Jonsson
  * 
  */
 public class ItemBarTest extends TestCase {
 
 	private PlayerID playerId;
 	private BulletManager bulletManager;
 	private ItemBar itemBar;
 
 	@Override
 	protected void setUp() throws Exception {
 		super.setUp();
 		this.resetInstanceVariables(10);
 	}
 
 	private void resetInstanceVariables(int slots) {
 		bulletManager = new BulletManager();
 		playerId = PlayerID.PLAYER1;
		itemBar = new ItemBar(playerId, bulletManager, slots);
 	}
 
 	/**
 	 * Add a lot of items to the item bar, get them from the item bar and
 	 * finally check if the size is correct.
 	 */
 	public void testAddItemsAndCheckSize() {
 		for (int i = 0; i < 1000; i += 100) {
 			resetInstanceVariables(1000);
 			addItemSizeTester(i);
 		}
 	}
 
 	private void addItemSizeTester(int numberOfItems) {
 		for (int i = 0; i < numberOfItems; i++) {
 			itemBar.addItem(new BasicWeapon(bulletManager));
 		}
 		assertEquals(itemBar.getItems().size(), numberOfItems);
 	}
 
 	/**
 	 * Add 15 items to an item bar that can only take 10 items, and then check
 	 * that the size of items is correct in the item bar.
 	 */
 	public void testAddTooManyItems() {
 		resetInstanceVariables(10);
 		for (int i = 0; i < 15; i++) {
 			itemBar.addItem(getNewItem());
 		}
 		assertEquals(itemBar.getItems().size(), 10);
 	}
 
 	/**
 	 * Add 7 items to an item bar that can only hold 5 items. Then check so it's
 	 * the last 5 added items that are in the item bar. Also check so they are
 	 * in the right order.
 	 */
 	public void testWhetherItemsAreWrittenOverCorrectly() {
 		resetInstanceVariables(5);
 		List<AbstractWeapon> items = new ArrayList<AbstractWeapon>();
 		for (int i = 0; i < 7; i++) {
 			AbstractWeapon item = getNewItem();
 			items.add(item);
 			itemBar.addItem(item);
 		}
 		assertFalse(itemBar.getItems().contains(items.get(0)));
 		assertFalse(itemBar.getItems().contains(items.get(1)));
 		assertSame(itemBar.getItems().get(0), items.get(5));
 		assertSame(itemBar.getItems().get(1), items.get(6));
 		assertSame(itemBar.getItems().get(2), items.get(2));
 		assertSame(itemBar.getItems().get(3), items.get(3));
 		assertSame(itemBar.getItems().get(4), items.get(4));
 	}
 
 	private AbstractWeapon getNewItem() {
 		return new BasicWeapon(bulletManager);
 	}
 
 	private AbstractWeapon getNewWeapon() {
 		return new BasicWeapon(bulletManager);
 	}
 
 	/**
 	 * Fire a weapon a couple of times and check if the bullets are added to the
 	 * bullet manager.
 	 */
 	public void testFireWeapon() {
 		itemBar.fireWeapons(new Position());
 		assertTrue(bulletManager.getBullets().size() == 0);
 
 		itemBar.addItem(getNewWeapon());
 		itemBar.fireWeapons(new Position());
 		int bulletsShot = bulletManager.getBullets().size();
 		assertTrue(bulletsShot > 0);
 
 		itemBar.fireWeapons(new Position());
 		assertTrue(bulletManager.getBullets().size() > bulletsShot);
 	}
 }
