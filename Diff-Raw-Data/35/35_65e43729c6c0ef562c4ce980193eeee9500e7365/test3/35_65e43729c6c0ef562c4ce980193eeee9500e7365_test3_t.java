//package examples;
 
public class B {
 	public Integer f = 1;
 	public String g = "as";
 }
 
public class A<T> with [Integer B.fname, String B.f2] {
 	B b = new B();
 
 	void test() {
 		Integer x = b.fname;
 		//Integer x = (Integer)JavaDBRuntime.getFieldValue(b, "fname");
 		//TODO b.fname = 1
 		System.out.println("test value should = 1 :" + x);
 		System.out.println(b.f2);
 	}
 
 	public static void main(String []args) {
 		// A a1 = new A();//this is wrong [done]
 		// A a2 = new A<B> with [C.f] (); //no such type [done]
 		//A a3 = new A<B> with [B.h] (); //no such field [done]
 		//A a4 = new A<B> with [B.g, B.f] ();
 		A a4 = new A<B> with [B.f, B.g] ();
 		//A a4 = new A(); JavaDBRuntime.registerField(a4, "B", "fname", "f");
 		a4.test();
 	}
 }
