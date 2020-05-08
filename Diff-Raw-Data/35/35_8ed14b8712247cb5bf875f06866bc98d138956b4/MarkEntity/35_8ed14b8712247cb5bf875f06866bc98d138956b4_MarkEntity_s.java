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
 
 	/* (non-Javadoc)
 	 * @see java.lang.Object#toString()
 	 */
 	@Override
 	public String toString() {
 		StringBuilder builder = new StringBuilder();
 		builder.append("MarkEntity [subEntity=");
 		builder.append(subEntity);
 		builder.append(", subEntityWeight=");
 		builder.append(subEntityWeight);
 		builder.append(", details=");
 		builder.append(details);
 		builder.append(", mark=");
 		builder.append(mark);
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
		String heads = this.getHeadersString();
		
		LinkedList<String> str = new LinkedList<String>();
		
		while(heads.contains("*/*/*")){
			str.add(heads.substring(0, heads.indexOf("*/*/*")-1));

			heads = heads.substring(heads.indexOf("*/*/*")+5);
		}
		
		String[] headers = new String[str.size()];
		
		for(int x = 0; x < str.size();x++){
				headers[x] = str.get(x);
		}
 		
		return headers;
 			
 	}
 	
 	public String getHeadersString(){
 		String str = this.getDetails().getType().getName();
 		
 		for(int x = 0; x < this.subEntity.size();x++){
			str = str + "*/*/*" + this.subEntity.get(x).getHeaders();
 		}
 		
		
 		return str;
 	}
 	
 }
