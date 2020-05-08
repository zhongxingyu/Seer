 package ClassAdminBackEnd;
 
 import java.util.LinkedList;
 
 public class LeafMarkEntity extends MarkEntity{
 
 
 	public LeafMarkEntity(EntityType type, SuperEntity parentEntity, double mark) {
 		super(type, parentEntity);
 		// TODO Auto-generated constructor stub
 	}
 	
 	public LeafMarkEntity(SuperEntity replacedEntity, double mark) {
 		super(replacedEntity);
 		this.setMark(mark);
 		if(this.getSubEntity().size() != 0){
 			this.unLeaf();
 		}
 		// TODO Auto-generated constructor stub
 	}
 
 	public Double doMarkMath() throws AbsentException{
 		return this.getMark();
 	}
 	
 	public SuperEntity unLeaf(){
 		return new MarkEntity(this);
 	}
 	public String createTreeFromHead(LinkedList<SuperEntity> studentLinkedList)
 	{
 		String s;
 		if(this.getClass().toString().contains("Absent"))
 		{
			s = "*ABSENT*";
 		}
 		else
 		{
 			s = this.getValue();
 		}
 		studentLinkedList.add((SuperEntity) this);
 		String str = "";
 		str += "<leaf>" +
 				"<attribute name = \"name\" value= \"" + this.getType().getName() + ": " + s + "\" />";
 		str +="</leaf>";
 		return str;
 	}
 
 }
