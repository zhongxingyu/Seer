 class A {
     int a;
     String s;
     B b;
 
     A () {
 	a = 1;
 	s = "haha";
 	b = new B();
     }
 
     String getS() {
 	return s;
     }
     
     int getA() {
         return a;
     }
 }
 
 class B {
     B() {}
 }
 
 class C {
     A a;
 
     C() {
         a = new A();
     }
     
     A getA() {
         return a;
     }
 }
 
 public class Test2 {
     public static void main(String args[]) {
         C c = new C();
 	int f = c.getA().getA();
        f + c.getA().getA();
     }
 }
