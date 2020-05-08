 import java.util.*;
 
 public class MyLinkedList {
 	//parham
 	//Sudi add something
 	//test
 	//printMe method
 	private static void printMe(List<String> l){
 		for(String s:l){
 			System.out.printf("%s ", s);
 		}
 		System.out.println();
 	}
 
 	//removeStuff method
 	private static void removeStuff(List<String> l, int from, int to){
 		l.subList(from, to).clear();
 	}
 
 	private static void reverseMe(List<String> list1) {
 		ListIterator<String> li = list1.listIterator(list1.size());
 		while(li.hasPrevious())
 			System.out.printf("%s ", li.previous());
 
 	}
 
 
 
 
 
 	public static void main(String[] args) {
 		String[] things = {"test1", "test2", "test3", "test4", ""};
 		List<String> list1 = new LinkedList<String>();
 
 		for(String x:things){
 			list1.add(x);
 		}
 
 		String[] things2 = {"sausage", "bacon", "goats", "harry potter"};
 
 		List<String> list2 = new LinkedList<String>();
 		for(String y: things2)
 			list2.add(y);
 
 		list1.addAll(list2);
 		list2 = null;
 
 		printMe(list1);
 		removeStuff(list1, 2, 5);
 		printMe(list1);
 		reverseMe(list1);
 
 	}
 
 }
 
 
