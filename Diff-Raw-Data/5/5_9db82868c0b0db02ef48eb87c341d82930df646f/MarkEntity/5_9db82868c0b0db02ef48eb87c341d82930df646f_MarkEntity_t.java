 package ClassAdminBackEnd;
 
 import java.util.Date;
 import java.util.LinkedList;
 
 public class MarkEntity {
 
 
 	private MarkEntity parentEntity;
 	private LinkedList<MarkEntity> subEntity = new LinkedList<MarkEntity>();
 	private LinkedList<Double> subEntityWeight = new LinkedList<Double>();
 	private EntityDetails details;
 	private double mark;
 	private int rowFollowCount;
 
 	/**
 	 * @param parentEntity
 	 * @param subEntity
 	 * @param subEntityWeight
 	 * @param details
 	 * @param mark
 	 */
 	public MarkEntity(MarkEntity parentEntity,
 			EntityDetails details, double mark) {
 		this.parentEntity = parentEntity;
 		this.details = details;
 		this.mark = mark;
 		rowFollowCount = 0;
 	}
 	public MarkEntity(EntityDetails details, double mark){
 		this.details = details;
 		this.mark = mark;
 		rowFollowCount = 0;
 	}
 
 	/* (non-Javadoc)
 	 * @see java.lang.Object#toString()
 	 */
 	@Override
 	public String toString() {
 		StringBuilder builder = new StringBuilder();
 		
 		
 		builder.append("MarkEntity [mark=");
 		builder.append(mark);
 		builder.append(", details=");
 		builder.append(details);
 		builder.append(", subEntityWeight=");
 		builder.append(subEntityWeight);
 		builder.append(", subEntity=");
 		builder.append(subEntity);
 		builder.append("]");
 		
 		return builder.toString();
 	}
 
 	/**
 	 * @return the rowFollowCount
 	 */
 	public int getRowFollowCount() {
 		return rowFollowCount;
 	}
 
 	/**
 	 * @param rowFollowCount
 	 */
 	public void setRowFollowCount(int rowFollowCount) {
 		this.rowFollowCount = rowFollowCount;
 	}
 	
 	/**
 	 * increases the rowFollowCount
 	 */
 	public void increaseRowFollowCount(){
 		this.rowFollowCount++;
 	}
 
 	/**
 	 * @return the parentEntity
 	 */
 	public MarkEntity getParentEntity() {
 		return parentEntity;
 	}
 
 	/**
 	 * @param parentEntity
 	 *            the parentEntity to set
 	 */
 	public void setParentEntity(MarkEntity parentEntity) {
 		this.parentEntity = parentEntity;
 	}
 
 	/**
 	 * @return the subEntity
 	 */
 	public LinkedList<MarkEntity> getSubEntity() {
 		return subEntity;
 	}
 
 	/**
 	 * @param subEntity
 	 *            the subEntity to set
 	 */
 	public void setSubEntity(LinkedList<MarkEntity> subEntity) {
 		this.subEntity = subEntity;
 	}
 
 	/**
 	 * @return the subEntityWeight
 	 */
 	public LinkedList<Double> getSubEntityWeight() {
 		return subEntityWeight;
 	}
 
 	/**
 	 * @param subEntityWeight
 	 *            the subEntityWeight to set
 	 */
 	public void setSubEntityWeight(LinkedList<Double> subEntityWeight) {
 		this.subEntityWeight = subEntityWeight;
 	}
 
 	/**
 	 * @return the mark
 	 */
 	public double getMark() {
 		return mark;
 	}
 
 	/**
 	 * @param mark
 	 *            the mark to set
 	 */
 	public void setMark(double mark) {
 		this.mark = mark;
 	}
 
 	/**
 	 * @return the details
 	 */
 	public EntityDetails getDetails() {
 		return details;
 	}
 
 	/**
 	 * @param details
 	 *            the details to set
 	 */
 	public void setDetails(EntityDetails details) {
 		this.details = details;
 	}
 
 	/**
 	 * @return
 	 * @throws AbsentException
 	 */
 	public double calcMark() throws Exception {
 		if ((this.details.getAbsentExcuse() == true)
 				|| (this.details.getType().getDate() != null && this.details.getType().getDate().after(new Date()))) {
 			throw new AbsentException();
 		} else {
 			double mTotal = 0;
 			double wTotal = 0;
 			for (int i = 0; i < subEntity.size(); ++i) {
 				try {
 					mTotal += subEntity.get(i).calcMark()
 							* subEntityWeight.get(i);
 					wTotal += subEntityWeight.get(i);
 				} catch (Exception e) {
 				}
 
 			}
 
 			if (wTotal != 0)
 				this.mark = mTotal / wTotal;
 			else
 				this.mark = mTotal;
 			return this.mark;
 		}
 
 	}
 	
 	public String[] getHeaders(){
 		/*int max = -1;
 		int maxe =-1;
 		for(int x =0; x < subEntity.size();x++){
 			if(subEntity.get(x).getRowFollowCount() > max){
 				max = subEntity.get(x).getRowFollowCount();
 				maxe = x;
 			}
 		}*/
 		String heads = subEntity.get(0).getHeadersString();		
 		//System.out.println(heads);
 		
 		//System.out.print(heads);
 		String[] s = heads.split("bn f3hjjm3734n  5f6 34h 35g635 346n34f f g46345f");
 		return s; 
 			
 	}
 	
 	private String getHeadersString(){
 		String str = this.getDetails().getType().getName();
 		
 		for(int x = 0; x < this.subEntity.size();x++){
 			str = str + "bn f3hjjm3734n  5f6 34h 35g635 346n34f f g46345f" + this.subEntity.get(x).getHeadersString();
 		}
 		
 		return str;
 	}
 	
 	
 	public String[][] getData(){
 		/*int max = -1;
 		int maxe =-1;
 		for(int x =0; x < subEntity.size();x++){
 			if(subEntity.get(x).getRowFollowCount() > max){
 				max = subEntity.get(x).getRowFollowCount();
 				maxe = x;
 			}
 		}*/
 				
 		//System.out.println(heads);
 		
 		//System.out.print(heads);
 		
 		String[][] sData = new String[subEntity.size()][]; 
 		
 		for(int x = 0; x < subEntity.size();x++){
			String heads = subEntity.get(x).getDataString();
 			System.out.println(heads);
 			
 			String[] s = heads.split("qwerpoiu");
 			
 			sData[x] = s;
 		}
 		
 		
 		return sData; 
 			
 	}
 	
 	private String getDataString(){
 		String str;
 		
 		if(this.getDetails().getType().getIsTextField() == true){
			str = this.getDetails().getFields().get(0);	
 		}
 		else
 			str = Double.toString(this.getMark());	
 		
 		for(int x = 0; x < this.subEntity.size();x++){
 			str = str + "qwerpoiu" + this.subEntity.get(x).getDataString();
 		}
 		
 		return str;
 	}
 }
