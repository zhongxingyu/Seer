 package startrekj.hpbasic;
 
 import java.util.Vector;
 
 import javax.swing.JSpinner.NumberEditor;
 
 public class ArrayVariable {
 
 	private String name;
 
 	private int size1=0;
 	private int size2=0;
 	
 	private Vector<Number> array = new Vector<Number>();
 	
 	public ArrayVariable(String name) {
 		this.name = name;
 	}
 	public void setSize(int size1, int size2) {
 		this.size1 = size1 < 2 ? 1 : size1;
 		this.size2 = size2 < 2 ? 1 : size2;
 		array.setSize(size1*size2);
 	}
 	public String getName() {
 		return name;
 	}
 	public Number get(NumericVariable i, int j) {
 		return get(i.getValue().intValue(), j);
 	}
 	public Number get(int i, int j) {
 		return array.elementAt(index(i,j));
 	}
 	public ArrayPlace at(Number i) {
 		return at(new CONST(i), new CONST(1));
 	}
 	public ArrayPlace at(NumericExpression i) {
 		return at(i, new CONST(1));
 	}
 	public ArrayPlace at(Number i, Number j) {
 		return at(new CONST(i), new CONST(j));
 	}
 	public ArrayPlace at(NumericExpression i, Number j) {
 		return at(i, new CONST(j));
 	}
 	public ArrayPlace at(NumericExpression i, NumericExpression j) {
 		return new ArrayPlace(this, i, j);
 	}
 	public void setValue(int i, int j, Number value) {
 		array.set(index(i,j), value);
 	}
 	private int index(int i, int j) {
		return (j-1)*size2 + (i-1);
 	}
 	public void setAllTo(Number value) {
 		for(int i = 0; i < array.size(); ++i)
 			array.set(i, value);
 	}
 	@Override
 	public String toString() {
 		StringBuilder out = new StringBuilder();
 		out.append(name).append("=[");
 		if(array.size() > 0)
 			out.append(array.get(0));
 		for(int i=1; i<array.size(); ++i)
 			out.append(",").append(array.get(i));
 		out.append("]");
 		return out.toString();
 	}
 }
