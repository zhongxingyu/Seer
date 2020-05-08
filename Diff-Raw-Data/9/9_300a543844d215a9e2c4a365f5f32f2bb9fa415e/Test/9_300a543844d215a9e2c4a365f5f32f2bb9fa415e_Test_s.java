 package ptanks.jython;
 import org.python.util.PythonInterpreter;
 import org.python.core.*;
 public class Test {
 	public static void main(String[] args) throws PyException	{
 		PythonInterpreter py = new PythonInterpreter();
 		System.out.println("Python?");
 		py.exec("a = 2");
 		py.exec("b=a*a");
 		py.exec("for i in range(b):\n\tprint i");
 		PyObject a = py.get("a");
 		System.out.println("Python a:" + a);
 		PyObject b = py.get("b");
 		System.out.println("Python b:" + b);
 		
 		String s = "z = 100\nprint z";
 		PyCode code = py.compile(s);
 		py.eval(code);
 	}
 }
