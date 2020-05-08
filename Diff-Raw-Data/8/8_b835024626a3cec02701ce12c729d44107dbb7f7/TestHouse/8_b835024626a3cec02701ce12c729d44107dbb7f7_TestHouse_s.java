 package se.chalmers.project14.test;
 
 import se.chalmers.project14.model.House;
 import junit.framework.TestCase;
 
 /*
  * A simple test class testing the getters and setter for class House
  */
 
 public class TestHouse extends TestCase {
 
 	private House house = new House();
 
	public void TestGetId() {
 		house.setId(0);
 		int getId = house.getId();
 		int expectedResult = 0;
 		assertEquals(expectedResult, getId);
 	}
 
	public void TestGetFloor() {
 		house.SetFloor("5");
 		String getFloor = house.getFloor();
 		String expectedResult = "5";
 		assertEquals(expectedResult, getFloor);
 	}
 
	public void TestGetLectureRoom() {
 		house.setLectureRoom("hubben");
 		String getLectureRoom = house.getLectureRoom();
 		String expectedResult = "hubben";
 		assertEquals(expectedResult, getLectureRoom);
 	}
 }
