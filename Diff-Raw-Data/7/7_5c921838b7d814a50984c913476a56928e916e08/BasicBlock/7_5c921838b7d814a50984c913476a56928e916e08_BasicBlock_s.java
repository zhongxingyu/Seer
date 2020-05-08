 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 
 /**
  * Basic Block class. 
  * @author Faisal
  *
  */
 
 public class BasicBlock implements GraphComponent{
 	
 	private String name;
 	private String startAddress;
 	private String endAddress;
 	private HashSet<String> next;
 	private HashSet<String> prev;
 	private ArrayList<GraphComponent> instructions;
 	private HashMap<String, Instruction> instructionMap;
 	
 	
 	public BasicBlock(){
 		instructions = new ArrayList<GraphComponent>();
 		next = new HashSet<String>();
 		prev = new HashSet<String>();
 		instructionMap = new HashMap<String, Instruction>();
 	}
 
 
 	public String getName() {
 		return name;
 	}
 
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 
 	public String getStartAddress() {
 		return startAddress;
 	}
 
 
 	public void setStartAddress(String startAddress) {
 		this.startAddress = startAddress;
 	}
 
 
 	public String getEndAddress() {
 		return endAddress;
 	}
 
 
 	public void setEndAddress(String endAddress) {
 		this.endAddress = endAddress;
 	}
 
 
 	public HashSet<String> getNext() {
 		return next;
 	}
 	
 	public void addNext(String n){
		next.add(n);
 	}
 
 	public void revomeNext(String n){
 		next.remove(n);
 	}
 	
 	public HashSet<String> getPrev() {
 		return prev;
 	}
 	
 	public void addPrev(String p){
		prev.add(p);
 	}
 	
 	public void removePrev(String p){
 		prev.remove(p);
 	}
 	
 	public ArrayList<GraphComponent> getInstructions(){
 		return instructions;
 	}
 
 	@Override
 	public void addComponent(GraphComponent e) {
 		instructions.add(e);
 		
 		if(e instanceof Instruction)
 			this.instructionMap.put(((Instruction) e).getAddress(), (Instruction)e);
 	}
 
 
 	@Override
 	public void removeComponent(GraphComponent e) {
 		instructions.remove(e);
 	}
 
 
 	@Override
 	public void process() {
 		// TODO Auto-generated method stub
 		
 	}
 }
