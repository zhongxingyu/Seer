 package ClassAdminBackEnd;
 
 import java.awt.Color;
 import java.util.Date;
 import java.util.LinkedList;
 
 import javax.activity.InvalidActivityException;
 
 import org.tmatesoft.sqljet.core.SqlJetException;
 import org.tmatesoft.sqljet.core.SqlJetTransactionMode;
 import org.tmatesoft.sqljet.core.table.ISqlJetTable;
 import org.tmatesoft.sqljet.core.table.SqlJetDb;
 
 public class SuperEntity {
 
 	private SuperEntity parentEntity;
 	private LinkedList<SuperEntity> subEntity = new LinkedList<SuperEntity>();
 	private double mark;
 	private int rowFollowCount = 0;
 	private EntityType type;
 	private String field = "";
 	private SuperEntityPointer thisPointer;
 	private boolean hasMark = false;
 
 	/**
 	 * @return the type
 	 */
 	public EntityType getType() {
 		return type;
 	}
 
 	/**
 	 * @param type
 	 *            the type to set
 	 */
 	public void setType(EntityType type) {
 		if (this.type != null) {
 			this.type.getEntityList().remove(this);
 		}
 		this.type = type;
 		this.type.getEntityList().add(this);
 	}
 
 	/**
 	 * @return the fields
 	 */
 	public String getField() {
 		return field;
 	}
 
 	public void setField(String field) {
 		this.field = field;
 	}
 
 	/**
 	 * @return the picture
 	 */
 	public String getPicture() {
 		return Picture;
 	}
 
 	/**
 	 * @param picture
 	 *            the picture to set
 	 */
 	public void setPicture(String picture) {
 		Picture = picture;
 	}
 
 	/**
 	 * @return the highlightColor
 	 */
 	public Color getHighlightColor() {
 		return highlightColor;
 	}
 
 	/**
 	 * @param highlightColor
 	 *            the highlightColor to set
 	 */
 	public void setHighlightColor(Color highlightColor) {
 		this.highlightColor = highlightColor;
 	}
 
 	/**
 	 * @return the textColor
 	 */
 	public Color getTextColor() {
 		return textColor;
 	}
 
 	/**
 	 * @param textColor
 	 *            the textColor to set
 	 */
 	public void setTextColor(Color textColor) {
 		this.textColor = textColor;
 	}
 
 	private String Picture;
 	private Color highlightColor;
 	private Color textColor;
 
 	/**
 	 * @param parentEntity
 	 * @param subEntity
 	 * @param subEntityWeight
 	 * @param details
 	 * @param mark
 	 */
 
 	public SuperEntity(EntityType type, SuperEntity parentEntity, double mark) {
 		this.parentEntity = parentEntity.unLeaf();
 		this.setType(type);
 		this.mark = mark;
 		this.parentEntity.getSubEntity().add(this);
 
 	}
 
 	public SuperEntity(SuperEntity replacedEntity) {
 
 		this.setType(replacedEntity.getType());
 		replacedEntity.getType().getEntityList().remove(replacedEntity);
 		this.setThisPointer(replacedEntity.getThisPointer());
 
 		if (this.getThisPointer() != null)
 			this.getThisPointer().setTarget(this);
 
 		this.parentEntity = replacedEntity.getParentEntity();
 		try {
 			this.mark = replacedEntity.getMark();
 		} catch (AbsentException e) {
 			this.hasMark = false;
 		}
 		this.field = replacedEntity.getField();
 		this.subEntity = replacedEntity.getSubEntity();
 		for (int x = 0; x < subEntity.size(); ++x) {
 			this.subEntity.get(x).setParentEntity(this);
 		}
 		int index = replacedEntity.getParentEntity().getSubEntity()
 				.indexOf(replacedEntity);
 
 		replacedEntity.getParentEntity().getSubEntity().set(index, this);
 	}
 
 	public SuperEntity(EntityType type, double mark) {
 		this.setType(type);
 		this.mark = mark;
 
 	}
 
 	/**
 	 * @return the rowFollowCount
 	 */
 	public int getRowFollowCount() {
 		return rowFollowCount;
 	}
 
 	/**
 	 * increases the rowFollowCount
 	 */
 	public void increaseRowFollowCount() {
 		this.rowFollowCount++;
 	}
 
 	/**
 	 * @return the parentEntity
 	 */
 	public SuperEntity getParentEntity() {
 		return parentEntity;
 	}
 
 	/**
 	 * @param parentEntity
 	 *            the parentEntity to set
 	 */
 	public void setParentEntity(SuperEntity parentEntity) {
 		this.parentEntity = parentEntity;
 	}
 
 	/**
 	 * @return the subEntity
 	 */
 	public LinkedList<SuperEntity> getSubEntity() {
 		if (subEntity == null)
 			this.subEntity = new LinkedList<SuperEntity>();
 		return subEntity;
 	}
 
 	/**
 	 * @return the subEntityWeight
 	 */
 	public double getWeight() {
 
 		return this.getType().getDefaultWeight();
 	}
 
 	public SuperEntityPointer getThisPointer() {
 		return thisPointer;
 	}
 
 	public void setThisPointer(SuperEntityPointer thisPointer) {
 		this.thisPointer = thisPointer;
 	}
 
 	/**
 	 * @return the mark
 	 */
 	public double getMark() throws AbsentException{
 		if(!this.hasMark)
 			throw new AbsentException();
 		return mark;
 	}
 
 	/**
 	 * @param mark
 	 *            the mark to set
 	 */
 	public void setMark(double mark) {
 		this.mark = mark;
 		this.updateMark();
 		
 	}
 	
 	public void updateMark(){
 		try {
 			this.calcMark();
 		} catch (Exception e) {
 			this.hasMark = false;
 		}
		try{
			this.parentEntity.updateMark();
		}
		catch (NullPointerException e){
			
		}
 	}
 
 	/**
 	 * @return the details
 	 */
 	public SuperEntity getDetails() {
 		return this;
 	}
 
 	/**
 	 * @param details
 	 *            the details to set
 	 */
 
 	public Boolean isAbsent() {
 		return this.getType().getDate() != null
 				&& this.getType().getDate().after(new Date());
 	}
 
 	public SuperEntity unLeaf() {
 		return this;
 	}
 
 	public Double doMarkMath() throws AbsentException {
 		double mTotal = 0;
 		double wTotal = 0;
 		Boolean hasval = false;
 		for (int i = 0; i < subEntity.size(); ++i) {
 			try {
 				mTotal += subEntity.get(i).getMark()
 						* subEntity.get(i).getWeight();
 				wTotal += subEntity.get(i).getWeight();
 
 				hasval = true;
 			} catch (Exception e) {
 			}
 
 		}
 
 		if (!hasval) {
 			throw new AbsentException();
 		}
 
 		if (wTotal != 0)
 			return mTotal / wTotal;
 		else
 			return mTotal;
 	}
 
 	/**
 	 * @return
 	 * @throws AbsentException
 	 */
 
 	public void calcMark() throws Exception {
 		if (this.isAbsent()) {
 			throw new AbsentException();
 		} else {
 			this.mark = doMarkMath();
 		}
 
 	}
 
 	public String[] getHeaders() {
 		String heads = subEntity.get(0).getHeadersString();
 
 		String[] s = heads
 				.split("bn f3hjjm3734n  5f6 34h 35g635 346n34f f g46345f");
 		return s;
 
 	}
 
 	private String getHeadersString() {
 		String str = this.getDetails().getType().getName();
 
 		for (int x = 0; x < this.subEntity.size(); x++) {
 			str = str + "bn f3hjjm3734n  5f6 34h 35g635 346n34f f g46345f"
 					+ this.subEntity.get(x).getHeadersString();
 		}
 
 		return str;
 	}
 
 	public LinkedList<SuperEntity> getHeadersLinkedList() {
 		LinkedList<SuperEntity> lEntity = new LinkedList<SuperEntity>();
 
 		LinkedList<LinkedList<SuperEntity>> temp = this.getDataLinkedList();
 
 		for (int x = 0; x < temp.get(0).size(); x++) {
 			lEntity.add(temp.get(0).get(x));
 		}
 
 		return lEntity;
 	}
 
 	public LinkedList<LinkedList<SuperEntity>> getDataLinkedList() {
 		LinkedList<LinkedList<SuperEntity>> linkLinkEntity = new LinkedList<LinkedList<SuperEntity>>();
 
 		for (int x = 0; x < this.getSubEntity().size(); x++) {
 			linkLinkEntity.add(new LinkedList<SuperEntity>());
 			this.getSubEntity().get(x)
 					.addDataToLinkedList(linkLinkEntity.get(x));
 		}
 
 		return linkLinkEntity;
 	}
 
 	private void addDataToLinkedList(LinkedList<SuperEntity> linkLinkEntity) {
 		linkLinkEntity.add(this);
 		for (int x = 0; x < this.getSubEntity().size(); x++) {
 			this.getSubEntity().get(x).addDataToLinkedList(linkLinkEntity);
 		}
 	}
 
 	public String getValue() {
 		// TODO
 		return "";
 	}
 
 	public void setValue(String str) {
 		this.setMark(Double.parseDouble(str));
 	}
 
 	public String[][] getData() {
 		String[][] sData = new String[subEntity.size()][];
 
 		for (int x = 0; x < subEntity.size(); x++) {
 			String heads = subEntity.get(x).getDataString();
 			// System.out.println(heads);
 
 			String[] s = heads.split("qwerpoiu");
 
 			sData[x] = s;
 		}
 
 		return sData;
 
 	}
 
 	private String getDataString() {
 		String str;
 
 		str = this.getValue();
 
 		for (int x = 0; x < this.subEntity.size(); x++) {
 			str = str + "qwerpoiu" + this.subEntity.get(x).getDataString();
 		}
 
 		return str;
 	}
 
 	public long saveToDB(SqlJetDb db, long parentID, PDatIDGenerator idgen)
 			throws SqlJetException {
 		db.beginTransaction(SqlJetTransactionMode.WRITE);
 		long id = idgen.getID();
 
 		ISqlJetTable table = db.getTable(PDatExport.ENTITY_TABLE);
 		// insert statements
 
 		table.insert(id, parentID, this.getType().getID());
 
 		for (int x = 0; x < this.getSubEntity().size(); ++x) {
 			this.getSubEntity().get(x).saveToDB(db, id, idgen);
 		}
 		return id;
 	}
 
 	public LinkedList<SuperEntity> getColumn(
 			LinkedList<LinkedList<SuperEntity>> data, int kolumn) {
 		return (data.get(kolumn));
 	}
 
 	public String[] getNumberHeaders() {
 		LinkedList<SuperEntity> list = this.getHeadersLinkedList();
 		LinkedList<String> strlst = new LinkedList<String>();
 
 		for (int x = 0; x < list.size(); x++) {
 			if (!list.get(x).getType().getIsTextField()) {
 				strlst.add(list.get(x).getType().getName());
 			}
 		}
 
 		String[] str = new String[strlst.size()];
 
 		for (int x = 0; x < strlst.size(); x++) {
 			str[x] = strlst.get(x);
 		}
 
 		return (str);
 	}
 
 	public String createTreeFromHead() {
 		String str = "";
 		str += "<branch>" + "<attribute name = \"name\" value= \""
 				+ this.getValue() + "\" />";
 		for (int i = 0; i < this.getSubEntity().size(); i++) {
 			str += this.getSubEntity().get(i).createTreeFromHead();
 		}
 		str += "</branch>";
 		return str;
 	}
 
 	public SuperEntity findEntityOfType_Down(EntityType type) {
 		if (type.getEntityList().contains(this)){
 			return this;
 		}
 
 		else {
 			for (int x = 0; x < this.getSubEntity().size(); ++x) {
 
 				SuperEntity temp = this.getSubEntity().get(x)
 						.findEntityOfType_Down(type);
 				if (temp != null)
 					return temp;
 			}
 			return null;
 		}
 
 	}
 
 	public SuperEntity findEntityOfTypeUpDown(EntityType type) {
 		SuperEntity temp = this.findEntityOfType_Down(type);
 		if (temp == null)
 			temp = this.findEntityOfType_Up(type, this);
 
 		return temp;
 	}
 
 	public SuperEntity findEntityOfType_Up(EntityType type,
 			SuperEntity originator) {
 		if (type.getEntityList().contains(this))
 			return this;
 
 		else {
 			SuperEntity newParent = null;
 
 			for (int x = 0; x < this.getSubEntity().size(); ++x){
 				if (this.getSubEntity().get(x) != originator)
 					newParent = this.getSubEntity().get(x)
 							.findEntityOfType_Down(type);
 				if(newParent != null)
 					break;
 			}
 
 			if (newParent == null)
 				newParent = this.getParentEntity().findEntityOfType_Up(type,
 						this);
 
 			return newParent;
 		}
 	}
 
 	public void changeParentTotype(EntityType newParentType)
 			throws InvalidActivityException {
 		SuperEntity oldParent = this.getParentEntity();
 		SuperEntity newParent = null;
 
 		newParent = this.findEntityOfType_Up(newParentType, this);
 
 		if (newParent == null)
 			throw new InvalidActivityException();
 
 		SuperEntityPointer sPointer = new SuperEntityPointer(newParent);
 		LeafMarkEntity temp2 = new LeafMarkEntity(this.getType(),
 				sPointer.getTarget(), 0);
 		newParentType.getEntityList().remove(temp2);
 		sPointer.getTarget().getSubEntity().remove(temp2);
 		temp2.getType().getEntityList().remove(temp2);
 
 		oldParent.getSubEntity().remove(this);
 		sPointer.getTarget().getSubEntity().add(this);
 		this.setParentEntity(sPointer.getTarget());
 
 	}
 
 	public int getDepth() {
 
 		int max = 0;
 		for (int x = 0; x < getSubEntity().size(); ++x) {
 			int tmp = this.getSubEntity().get(x).getDepth();
 			if (tmp > max) {
 				max = tmp;
 			}
 		}
 		return ++max;
 	}
 
 	public IMGEntity IterativeDeepeningfindPortrait() {
 		int depth = 1;
 		int maxDepth = this.getDepth() - 1;
 		IMGEntity result = null;
 		while (result == null && depth <= maxDepth) {
 			result = findPortrait(depth++);
 		}
 		return result;
 	}
 
 	public IMGEntity findPortrait(int i) {
 		if (i == 0)
 			return null;
 
 		IMGEntity result = null;
 		while (result == null) {
 			result = findPortrait(i - 1);
 		}
 		return result;
 	}
 }
