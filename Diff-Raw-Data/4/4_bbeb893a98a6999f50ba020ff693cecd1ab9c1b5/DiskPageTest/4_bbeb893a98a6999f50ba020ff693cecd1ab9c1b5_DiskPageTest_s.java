 package memoryManager;
 
 import org.junit.Assert;
 import org.junit.Test;
 
 /**
  * Created with IntelliJ IDEA.
  * User: nikita_kartashov
  * Date: 20/11/2013
  * Time: 17:49
  * To change this template use File | Settings | File Templates.
  */
 public class DiskPageTest
 {
 
 	@Test
 	public void InitialDataWritingTest()
 	{
 		byte[] rawData = new byte[DiskPage.MAX_PAGE_SIZE];
 
 		DiskPage newPage = new DiskPage(rawData, true);
 
		Assert.assertEquals(newPage.nextPageIndex(), DiskPage.NULL_PTR);
		Assert.assertEquals(newPage.prevPageIndex(), DiskPage.NULL_PTR);
 	}
 
 	@Test
 	public void CorrectDataWritingTest()
 	{
 		byte[] rawData = new byte[DiskPage.MAX_PAGE_SIZE];
 
 		DiskPage newPage = new DiskPage(rawData, true);
 
 		newPage.setNextPageIndex(Long.MAX_VALUE);
 		newPage.setPrevPageIndex(Long.MAX_VALUE / 2);
 
 		byte[] newData = newPage.rawPage();
 
 		newPage = new DiskPage(newData, false);
 		long nextPtr = newPage.nextPageIndex();
 		long prevPtr = newPage.prevPageIndex();
 
 		Assert.assertTrue(nextPtr == Long.MAX_VALUE && prevPtr == (Long.MAX_VALUE / 2));
 	}
 
 	@Test
 	public void ReadWriteTest()
 	{
 		byte[] rawData = new byte[DiskPage.MAX_PAGE_SIZE];
 
 		DiskPage newPage = new DiskPage(rawData, true);
 
 		byte[] dataPayload = {67, 24, 78, 90, 21, 77, 90, 5, 18};
 
 		newPage.write(56, dataPayload, 0, dataPayload.length);
 
 		byte[] result = newPage.read(56, dataPayload.length);
 
 		Assert.assertArrayEquals(dataPayload, result);
 	}
 
 }
