 package models;
 
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import javax.persistence.Entity;
 import javax.persistence.Id;
 import javax.persistence.Transient;
 
 import org.springframework.util.CollectionUtils;
 
 import play.data.validation.Constraints.Required;
 import play.db.ebean.Model;
 
 @Entity
 public class Block extends Model {
 	private static final long serialVersionUID = 1L;
 
 	@Id
 	public Long id;
 	@Required
 	public String cong;
 	@Required
 	public String block;
 	@Required
 	public Integer number;
 	@Required
 	public String coord;
 	public String markerCoord;
 	public String printCoord;
 	public String printZoomLevel;
 	public Integer recommendedWorkerNum = 4; //Default card is for 4 people
 	
 	
 	@Transient
 	public Date lastWorkedDate;
 
 	public static Finder<Long, Block> find = new Finder<Long, Block>(Long.class, Block.class);
 
 	public static List<Block> all() {
 		return find.all();
 	}
 
 	public static List<Block> find(String cong) {
 		List<Block> blockList = find.where().ieq("cong", cong).orderBy("block, number asc").findList();
 		for (Block b : blockList) {
 			List<Record> recordList = Record.findAll(cong, b.block, b.number.toString());
 			if (!CollectionUtils.isEmpty(recordList)) {
 				b.lastWorkedDate = recordList.get(0).workDate;
 			}
 		}
 
 		return blockList;
 	}
 
 	public static void create(Block point) {
 		point.save();
 	}
 	
 	public static void update(Block point) {
 		point.update();
 	}
 
 	public static void delete(Long id) {
 		find.ref(id).delete();
 	}
 
 	public static void delete(String cong, String blockName, String blockNumber) {
 		find.where().ieq("cong", cong).ieq("block", blockName).ieq("number", blockNumber).findUnique().delete();
 	}
	
	public static Block findById(Long id) {
		return find.byId(id);
	}
 
 	public static Block find(String cong, String blockName, String blockNumber) {
 		Block block = find.where().ieq("cong", cong).ieq("block", blockName).ieq("number", blockNumber).findUnique();
 
 		List<Record> recordList = Record.findAll(cong, blockName, blockNumber);
		if (block != null && !CollectionUtils.isEmpty(recordList)) {
 			block.lastWorkedDate = recordList.get(0).workDate;
 		}
 
 		return block;
 	}
 
 	/**
 	 * TODO : Study ebean and refactor this "horrible" query code
 	 * 
 	 * @param cong
 	 * @param blockArray
 	 * @return
 	 */
 	public static List<Block> find(String cong, String[] blockArray) {
 		List<Block> blockList = new ArrayList<Block>();
 
 		for (String block : blockArray) {
 			String blockName = block.split("-")[0];
 			String blockNumber = block.split("-")[1];
 			blockList.add(find.where().ieq("cong", cong).ieq("block", blockName).ieq("number", blockNumber).findUnique());
 		}
 
 		return blockList;
 	}
 
 	public static List<Block> find(String cong, String block) {
 		List<Block> blockList = find.where().ieq("cong", cong).ieq("block", block).orderBy("block, number asc").findList();
 
 		for (Block b : blockList) {
 			List<Record> recordList = Record.findAll(cong, b.block, b.number.toString());
 			if (!CollectionUtils.isEmpty(recordList)) {
 				b.lastWorkedDate = recordList.get(0).workDate;
 			}
 		}
 
 		return blockList;
 	}
 	
 	public boolean isEqualCongBlockAndNumber(Block block) {
 		return this.cong.equals(block.cong) && this.block.equals(block.block) && this.number.equals(block.number);
 	}
 	
 
 	@Override
 	public String toString() {
 		return block + "-" + number;
 		// return ReflectionToStringBuilder.toString(this, ToStringStyle.);
 	}
 
 	public String getLastWorkedDateString() {
 		DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
 		return this.lastWorkedDate == null ? "" : format.format(this.lastWorkedDate);
 	}
 }
