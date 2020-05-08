 package se.chalmers.datastrukt.lab1;
 
 public class Uppgift1 {
 
 	private String[] stringArr;
 	private String[] tmpStringArr;
 	private int numberOfElements = 0;
 	private int pointer;
 	private int capacity;
 
 	public Uppgift1() {
 		this(10);
 	}
 
 	public Uppgift1(int length) {
 		this.stringArr = new String[length];
 		capacity = length;
 
 	}
 
 	public void addFirst(String element) {
 		int tmpCount = 1;
 		if (numberOfElements >= capacity) {
 			reallocate();
 		}
 		tmpStringArr = new String[capacity];
 		tmpStringArr[0] = element;
 		for (int i = 0; i < numberOfElements; i++) {
 				tmpStringArr[tmpCount] = stringArr[i];
 				tmpCount++;
 		}
 		stringArr = tmpStringArr;
 		numberOfElements++;
 	}
 
 	// private int numberOfElements(String[] strArray) {
 	// int number = 0;
 	// for (int i = 0; i < strArray.length; i++) {
 	// if (strArray[i] != null) {
 	// number++;
 	// }
 	// }
 	// return number;
 	// }
 
 	public boolean empty() {
 		for (int i = 0; i < stringArr.length; i++) {
 			if (stringArr[i] != null) {
 				return false;
 			}
 		}
 		return true;
 	}
 
 	public String getFirst() {
 		return stringArr[0];
 	}
 
 	public void removeFirst() {
 		if(empty()){
 			throw new ArrayIndexOutOfBoundsException("Remove failed, array already empty");
 		}
 		int nbrOfElem = numberOfElements - 1;
 		tmpStringArr = new String[nbrOfElem];
 		System.arraycopy(stringArr, 1, tmpStringArr, 0, nbrOfElem);
 		stringArr = tmpStringArr;
 		if (stringArr.length > 0) {
 			numberOfElements--;
 		}
 	}
 
 	public boolean existP(String elem) {
 		for (int i = 0; i < stringArr.length; i++) {
 			if (elem.equals(stringArr[i])) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	public String toString() {
 		StringBuilder stringBuilder = new StringBuilder();
 		for (int i = 0; i < numberOfElements; i++) {
 			stringBuilder.append(stringArr[i]).toString();
 			if (i < numberOfElements - 1) {
 				stringBuilder.append(", ");
 			}
 		}
 		String str = "[ " + stringBuilder.toString();
 		str += " ] ";
 		return str;
 	}
 
 	public void setP(int p) {
 		if (p < 0 || p > numberOfElements) {
 			throw new IndexOutOfBoundsException();
 		}
 		pointer = p;
 	}
 
 	public boolean hasNext() { // rtt metodsignatur?
		return (pointer + 1 <= numberOfElements);
		// return (p >= 0 || p <= numberOfElements); // kastar exceptions p
		// andra
		// stllen
 	}
 
 	public void addAfterP(int index, String element) {
 		if (index < 0 || index > numberOfElements) {
 			throw new IndexOutOfBoundsException();
 		} else {
 			if (numberOfElements == capacity) {
 				reallocate();
 			}
 			// tmpStringArr = new String[numberOfElements + 1];
 			for (int i = numberOfElements; i > index; i--) {
 				stringArr[i] = stringArr[i - 1];
 			}
 			stringArr[index] = element; // capacity vad r
 			// det!?!?!?!??!?!?!?!?!
 			// tmpStringArr = new String[numberOfElements + 1];
 			// for (int i = 0; i < index; i++) {
 			// tmpStringArr[i] = stringArr[i];
 			// }
 			// tmpStringArr[index] = element;
 			// for (int i = index; i < numberOfElements; i++) {
 			// tmpStringArr[i + 1] = stringArr[i];
 			// }
 			// stringArr = tmpStringArr;
 			numberOfElements++;
 
 		}
 	}
 
 	private void reallocate() {
 		capacity = capacity * 2;
 		tmpStringArr = new String[capacity];
 		for (int i = 0; i < numberOfElements; i++) {
 			tmpStringArr[i] = stringArr[i];
 		}
 		stringArr = tmpStringArr;
 	}
 
 	public String get(int p) {
 		if (p >= 0 || p <= numberOfElements) {
 			throw new IndexOutOfBoundsException();
 		}
 		return stringArr[p]; // + 1?
 	}
 
 	public void moveP(int val) {
 		if (pointer + val > 0 || pointer + val <= numberOfElements) {
 			throw new IndexOutOfBoundsException();
 		} else {
 			pointer += val;
 		}
 	}
 
 	public void setPtoStringPos(String elem) {
 		for (int i = 0; i < stringArr.length; i++) {
 			if (elem.equals(stringArr[i])) {
 				pointer = i;
 			}
 		}
 	}
 
 	public static void main(String[] args) {
 		int stringLength = 10;
 		Uppgift1 uppgA = new Uppgift1(stringLength);
 
 		//a)
 		// First test for method empty
 		System.out.println("*" + uppgA.empty() + " # br vara true");
 
 		// Test for method addFirst
 		uppgA.addFirst("Erland");
 		System.out.println("*" + uppgA + " # Br skriva ut: [ Erland ]");
 		uppgA.addFirst("Tomas");
 		uppgA.addFirst("Anton");
 		uppgA.addFirst("Henrik");
 		System.out.println("*" + uppgA + " # br vara Henrik frst");
 
 		// Second test for method empty
 		System.out.println("*" + uppgA.empty() + " # br vara false");
 
 		// Test for method getFirst
 		System.out.println("*" + uppgA.getFirst() + " # br vara Henrik");
 
 		// Test for method removeFirst
 		uppgA.removeFirst();
 		System.out.println("*" + uppgA + " # br vara Anton frst");
 
 		// Test for method existP
 		System.out.println("*" + uppgA.existP("Anton") + " # br vara true");
 		System.out.println("*" + uppgA.existP("Adam") + " # br vara false");
 		
 		// Test for toString
 		System.out.println("*" + uppgA + " # br vara [ Anton, Tomas, Erland ]");
 
 		// Test for reallocate
 		System.out.println("*" + uppgA.capacity + " # br vara 10");
 		uppgA.reallocate();
 		System.out.println("*" + uppgA.capacity + " # br vara 20");
 		
 		
 		//b)
 		Uppgift1 uppgB = new Uppgift1();
 		uppgB.addFirst("Julius");
 		uppgB.addFirst("Ingvar");
 		uppgB.addFirst("Henrik");
 		uppgB.addFirst("Gustav");
 		uppgB.addFirst("Felix");
 		uppgB.addFirst("Erik");
 		uppgB.addFirst("David");
 		uppgB.addFirst("Cecilia");
 		uppgB.addFirst("Bertil");
 		uppgB.addFirst("Anton");
 		System.out.println("*" + uppgB + " # br skriva ut 10 namn i alfabetisk ordning");
 		
 		// Test for setP
 		uppgB.setP(4);
 		System.out.println("*" + uppgB.pointer + " # br skriva ut 4");
 		
 	}
 }
