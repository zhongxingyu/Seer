 package nl.giantit.minecraft.database.drivers.h2;
 
 import nl.giantit.minecraft.database.query.Column;
 
 /**
  *
  * @author Giant
  */
 public class H2Column implements Column {
 
 	private String name;
 
 	private boolean P_KEY = false;
 	private boolean A_INCR = false;
 	private boolean n = false;
 	private Integer length = null;
 
 	private DataType t;
 	private String data;
 
 	private boolean rawDef = false;
 	private String def;
 
 	private boolean prepared = false;
 	private String field;
 
 	@Override
 	public Column setName(String name) {
 		this.name = name;
 		return this;
 	}
 	
 	@Override
 	public String getName() {
 		return this.name;
 	}
 
 	@Override
 	public Column setDataType(DataType t) {
 		if(t == DataType.DATETIME) {
 			t = DataType.TIMESTAMP;
 		}
 		
 		this.t = t;
 
 		return this;
 	}
 
 	@Override
 	public Column setRawDataType(String data) {
 		t = DataType.RAW;
 		this.data = data;
 		return this;
 	}
 
 	@Override
 	public Column setLength(int length) {
 		this.length = length;
 		return this;
 	}
 
 	@Override
 	public boolean hasPrimaryKey() {
 		return this.P_KEY;
 	}
 
 	@Override
 	public Column setPrimaryKey() {
 		this.P_KEY = true;
 
 		return this;
 	}
 
 	@Override
 	public Column setAutoIncr() {
 		this.A_INCR = true;
 
 		return this;
 	}
 
 	@Override
 	public Column setNull() {
 		this.n = true;
 
 		return this;
 	}
 
 	@Override
 	public Column setDefault(String def) {
 		this.def = def;
 
 		return this;
 	}
 
 	@Override
 	public Column setRawDefault(String def) {
 		this.rawDef = true;
 		this.def = def;
 
 		return this;
 	}
 
 	@Override
 	public Column parse() {
 		if(!this.prepared) {
 			this.prepared = true;
 
 			StringBuilder sB = new StringBuilder();
 
 			sB.append(this.name);
 
 			String type = this.t.getTextual();
 
 			if(this.t == DataType.RAW) {
 				sB.append(type.replace("%1", this.data));
 			}else if(null != this.length) {
 				sB.append(type.replace("%1", String.valueOf(this.length.intValue())));
 			}else{
 				sB.append(type);
 			}
 
 			if(this.n) {
 				sB.append(" DEFAULT NULL ");
 			}else{
 				sB.append(" NOT NULL ");
 				if(this.rawDef) {
 					sB.append(" DEFAULT ");
 					sB.append(this.def);
 					sB.append(" ");
 				}else{
 					if(null != this.def) {
 						sB.append(" DEFAULT '");
 						sB.append(this.def);
 						sB.append("' ");
 					}
 				}
 			}
 
 			if(this.A_INCR) {
				sB.append(" AUTOINCREMENT ");
 			}
 
 			this.field = sB.toString();
 		}
 
 		return this;
 	}
 
 	@Override
 	public boolean isParsed() {
 		return this.prepared;
 	}
 
 	@Override
 	public String getParsedColumn() {
 		if(!this.prepared) {
 			return "";
 		}
 
 		return this.field;
 	}
 
 }
