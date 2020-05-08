 package org.acaro.graffiti.query;
 
 import java.io.DataInput;
 import java.io.DataOutput;
 import java.io.IOException;
 import java.util.ArrayList;
 
 import org.apache.hadoop.io.Writable;
 
 import com.google.common.base.Joiner;
 
 public class LocationStep implements Writable {
 	private ArrayList<Condition> conditions = new ArrayList<Condition>();
 	private String edge = " ";
 	private int repeat = -1;
 	private boolean isSP = false;
 	
 	public void setEdge(String edge) {
 		this.edge = edge;
 	}
 
 	public void addCondition(Condition condition) {
 		conditions.add(condition);
 	}
 
 	public void setRepeat(Integer repeat) {
 		this.repeat = repeat;
 	}
 
 	public void setSP(boolean value) {
 		this.isSP = value;
 	}
 	
 	public String toString() {
 		StringBuffer string = new StringBuffer();
 		
 		if (!edge.equals(" "))
 			string.append(edge);
 		else
 			string.append("<no edge>");
 		
 		string.append(" ");
 		string.append(Joiner.on(" | ").skipNulls().join(conditions));
 		
 		if (repeat != -1) {
 			string.append(" (");
 			if (isSP)
 				string.append("*");
			string.append(repeat);
 			string.append(")");
 		}
 		
 		return string.toString();
 	}
 
 	@Override
 	public void readFields(DataInput input) throws IOException {
 		setEdge(input.readUTF());
 		setRepeat(input.readInt());
 		setSP(input.readBoolean());
 		
 		int n = input.readInt();
 		for (int i = 0; i < n; i++) {
 			Condition.TYPE type = Condition.TYPE.values()[input.readInt()];
 			Condition c;
 			
 			switch (type) {
 			case FILTER:
 			{
 				c = new Filter();
 				break;
 			}	
 			case SUBQUERY:
 			{
 				c = new Subquery();
 				break;
 			}
 			default:
 				throw new RuntimeException("Unknown Condition type at readFields!");
 			}

			c.readFields(input);
 			conditions.add(c);
 		}
 	}
 
 	@Override
 	public void write(DataOutput output) throws IOException {
 		output.writeUTF(edge);
 		output.writeInt(repeat);
 		output.writeBoolean(isSP);
 		
 		output.writeInt(conditions.size());
 		for (Condition c: conditions)
 			c.write(output);
 	}
 }
