 package model.common;
 
 import common.Result;
 import model.item.Item;
 import model.item.ItemVault;
 import model.product.Product;
 import model.product.ProductVault;
 import model.productcontainer.ProductGroup;
 import model.productcontainer.ProductGroupVault;
 import model.productcontainer.StorageUnit;
 import model.productcontainer.StorageUnitVault;
 import org.joda.time.DateTime;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 
 /**
  * Created with IntelliJ IDEA.
  * User: nethier
  * Date: 9/25/12
  * Time: 10:37 PM
  * To change this template use File | Settings | File Templates.
  */
 public class BaseModelTest {
     StorageUnit su1,su2,su3,su4,su5;
     ProductGroup pg1,pg2,pg3;
     Product p1,p2,p3,p4;
     Item i1,i2,i3,i4,i5,i6;
 
     @Before
     public void setUp() throws Exception {
         su1 = new StorageUnit();
         su2 = new StorageUnit();
         su3 = new StorageUnit();
         su4 = new StorageUnit();
         su5 = new StorageUnit();
         su1.setName("A");
         su2.setName("B");
         su3.setName("C");
         su4.setName("D");
         su5.setName("E");
         su1.validate();
         su2.validate();
         su3.validate();
         su4.validate();
         su5.validate();
         su1.save();
         su2.save();
         su3.save();
         su4.save();
         su5.save();
 
         pg1 = new ProductGroup();
         pg2 = new ProductGroup();
         pg3 = new ProductGroup();
         pg1.set3MonthSupply(new Size(1, Size.Unit.count));
         pg1.setRootParentId(su1.getId());
         pg1.setName("Group 1");
         pg1.validate();
         pg1.save();
 
         pg2.set3MonthSupply(new Size(1, Size.Unit.count));
         pg2.setName("Group B");
         pg2.setRootParentId(su1.getId());
         pg2.setParentId(pg1.getId());
         pg2.validate();
         pg2.save();
 
         pg3.setName("Group C");
         pg3.set3MonthSupply(new Size(1, Size.Unit.count));
         pg3.setRootParentId(su2.getId());
         pg3.setParentId(-1);
         pg3.validate();
         pg3.save();
 
         p1 = new Product();
         p2 = new Product();
         p3 = new Product();
         p4 = new Product();
         p1.setContainerId(pg1.getId());
         p1.set3MonthSupply(1);
         p1.setBarcode(new Barcode("0002"));
         p1.setCreationDate(new DateTime());
         p1.setDescription("Item A");
         p1.setShelfLife(3);
         p1.setSize(new Size(1, Size.Unit.count));
         p1.setStorageUnitId(su1.getId());
         assert p1.validate().getStatus();
         assert p1.save().getStatus();
 
         p2.setContainerId(pg2.getId());
         p2.set3MonthSupply(2);
         p2.setBarcode(new Barcode("0202"));
         p2.setCreationDate(new DateTime());
         p2.setDescription("Item B");
         p2.setShelfLife(4);
         p2.setSize(new Size(1, Size.Unit.count));
         p2.setStorageUnitId(su1.getId());
         p2.validate();
         p2.save();
 
         p3.setContainerId(pg3.getId());
         p3.set3MonthSupply(9);
         p3.setBarcode(new Barcode("0003"));
         p3.setCreationDate(new DateTime());
         p3.setDescription("Item C");
         p3.setShelfLife(9);
         p3.setSize(new Size(1, Size.Unit.count));
         p3.setStorageUnitId(su2.getId());
         p3.validate();
         p3.save();
 
         p4.setContainerId(-1);
         p4.set3MonthSupply(5);
         p4.setBarcode(new Barcode("0008"));
         p4.setCreationDate(new DateTime());
         p4.setDescription("Item X");
         p4.setShelfLife(4);
         p4.setSize(new Size(1, Size.Unit.count));
         p4.setStorageUnitId(su4.getId());
         p4.validate();
         p4.save();
 
         i1 = new Item();
         i2 = new Item();
         i3 = new Item();
         i4 = new Item();
         i5 = new Item();
         i6 = new Item();
 
         i1.setProductId(p1.getId());
         //i1.setBarcode(new Barcode("0110"));
         i1.setEntryDate(DateTime.now());
         i1.setExpirationDate(DateTime.now().plusMonths(2));
         i1.validate();
         i1.save();
 
         i2.setProductId(p1.getId());
         //i2.setBarcode(new Barcode("0111"));
         i2.setEntryDate(DateTime.now());
         i2.setExpirationDate(DateTime.now().plusMonths(2));
         i2.validate();
         i2.save();
 
         i3.setProductId(p2.getId());
         //i3.setBarcode(new Barcode("0210"));
         i3.setEntryDate(DateTime.now());
         i3.setExpirationDate(DateTime.now().plusMonths(2));
         i3.validate();
         i3.save();
 
         i4.setProductId(p2.getId());
         //i4.setBarcode(new Barcode("0310"));
         i4.setEntryDate(DateTime.now());
         i4.setExpirationDate(DateTime.now().plusMonths(2));
         i4.validate();
         i4.save();
 
         i5.setProductId(p3.getId());
         //i5.setBarcode(new Barcode("0111222"));
         i5.setEntryDate(DateTime.now());
         i5.setExpirationDate(DateTime.now().plusMonths(2));
         i5.validate();
         i5.save();
 
         i6.setProductId(p3.getId());
         //i6.setBarcode(new Barcode("01199"));
         i6.setEntryDate(DateTime.now());
         i6.setExpirationDate(DateTime.now().plusMonths(2));
         i6.validate();
         i6.save();
 
 
     }
 
     @After
     public void tearDown() throws Exception {
         ItemVault.getInstance().clear();
         ProductVault.getInstance().clear();
         ProductGroupVault.getInstance().clear();
         StorageUnitVault.getInstance().clear();
     }
 
     @Test
     public void testAddItem() throws Exception {
         BaseModel c = new BaseModel();
         Item i = new Item();
         //i.setBarcode(new Barcode("2345"));
         i.setEntryDate(DateTime.now());
         i.setExpirationDate(DateTime.now().plusYears(1));
         i.setProductId(p4.getId());
 
        Result r = c.AddItem(su1, pg1, i);
         assertTrue(r.getStatus());
         assertEquals(su1.getId(), i.getProduct().getStorageUnitId());
         assertEquals(pg1.getId(),i.getProduct().getContainerId());
 
     }
 
     @Test
     public void testMoveItem() throws Exception {
         BaseModel c = new BaseModel();
 
        Result r = c.MoveItem(su1, pg1, i6);
         assertTrue(r.getStatus());
         assertEquals(su1.getId(),i6.getProduct().getStorageUnitId());
         assertEquals(pg1.getId(),i6.getProduct().getContainerId());
     }
 
     @Test
     public void testRemoveItem() throws Exception {
 
     }
 
     @Test
     public void testMoveProduct() throws Exception {
         BaseModel c = new BaseModel();
 
         Result r = c.MoveProduct(su1, pg1, p3);
         assertTrue(r.getStatus());
         assertEquals(su1.getId(),i6.getProduct().getStorageUnitId());
         assertEquals(pg1.getId(),i6.getProduct().getContainerId());
     }
 
     @Test
     public void testDeleteProduct() throws Exception {
         BaseModel c = new BaseModel();
 
         Result r = c.DeleteProduct(p3);
         assertFalse(r.getStatus());
 
         assertTrue(c.DeleteProduct(p4).getStatus());
 
     }
 }
