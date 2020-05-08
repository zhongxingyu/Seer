 package se.chalmers.project14.test;
 
 import se.chalmers.project14.model.House;
 import junit.framework.TestCase;
 
 /*
  * A simple test class testing the getters and setter for class House
  */
 
 public class TestHouse extends TestCase {
 
 	private House house = new House();
 
	public void testGetId() {
 		house.setId(0);
 		int getId = house.getId();
 		int expectedResult = 0;
 		assertEquals(expectedResult, getId);
 	}
 
	public void testGetFloor() {
 		house.SetFloor("5");
 		String getFloor = house.getFloor();
 		String expectedResult = "5";
 		assertEquals(expectedResult, getFloor);
 	}
 
	public void testGetLectureRoom() {
 		house.setLectureRoom("hubben");
 		String getLectureRoom = house.getLectureRoom();
 		String expectedResult = "hubben";
 		assertEquals(expectedResult, getLectureRoom);
 	}
 }
