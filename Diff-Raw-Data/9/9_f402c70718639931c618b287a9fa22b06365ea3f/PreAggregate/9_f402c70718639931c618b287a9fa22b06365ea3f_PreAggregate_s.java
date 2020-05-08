 package nl.utwente.db.neogeo.preaggregate;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
import java.sql.Statement;
 import java.sql.Timestamp;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Vector;
 
import com.vividsolutions.jts.geom.Polygon;

 import nl.utwente.db.neogeo.preaggregate.AggregateAxis.DoubleAxisIndexer;
 import nl.utwente.db.neogeo.preaggregate.AggregateAxis.IntegerAxisIndexer;
 import nl.utwente.db.neogeo.preaggregate.AggregateAxis.LongAxisIndexer;
 import nl.utwente.db.neogeo.preaggregate.AggregateAxis.TimestampAxisIndexer;
 
 public class PreAggregate {
 
 	/*
 	 * Experiment setup variables
 	 * 
 	 */
 	public static final boolean showAxisAndKey		= true;
 	public static final boolean	doResultCorrection	= true;
 	public static final boolean	serversideStairwalk	= true;
 	public static final char	DEFAULT_KD			= AggrKeyDescriptor.KD_CROSSPRODUCT_LONG;
 
 	private	static final int	AGGR_BASE			= 0x01;
 	public static final	int		AGGR_COUNT			= AGGR_BASE;
 	public static final	int		AGGR_SUM			= AGGR_BASE << 1;
 	public static final	int		AGGR_MIN			= AGGR_BASE << 2;
 	public static final	int		AGGR_MAX			= AGGR_BASE << 3;
 	public static final int		AGGR_ALL			= (AGGR_COUNT|AGGR_SUM|AGGR_MIN|AGGR_MAX);
 
 	private int	aggregateMask = 0; // the 'allowed' aggregates for this instance
 
 	private static final String  aggregateRepositoryName	= "pre_aggregate";
 	private static final String  aggregateRepositoryDimName	= "pre_aggregate_axis";
 
 	public static final int	RMIN	= 0;
 	public static final int	RMAX	= 1;
 
 	private static final boolean useDirect = true; // do direct texting in postgis without aggregate
 
 	public static final boolean do_assert		= true;
 	public static final long    indexMask		= 0x00FFFFFFFFFFFFFFL; // warning, should match with levStart
 	public static final int		levStart 		= 56; // the first 8 bits
 
 	public static final long li_key(short l, int i) {
 		long key = (((long)l)<<levStart) | (long)i;
 
 		if ( do_assert ) {
 			if ( l != li_l(key) )
 				throw new RuntimeException("li_l(): " + l + "<>" + li_l(key));
 			if ( i != li_i(key) )
 				throw new RuntimeException("li_i(l="+l+", i="+i+"): i=" + i + "<> li()=" + li_i(key) + ", bits="+AggregateAxis.log2(i));
 		}
 		return key;
 	}
 
 	public static final short li_l(long key) {
 		return (short)(key >>levStart);
 	}
 
 	public static final int li_i(long key) {
 		return (int)(key & indexMask);
 	}
 
 	public static final String li_toString(long key) {
 		return "li_key[l="+li_l(key)+", i="+li_i(key) + "]";
 	}
 
 	public static final String PA_EXTENSION = "_pa";
 	// TODO other type mappings have to be added
 	private static final HashMap<String, String> className = new HashMap<String, String>()
 							{{put(LongAxisIndexer.TYPE_EXPRESSION,Long.class.getCanonicalName());
 							  put(IntegerAxisIndexer.TYPE_EXPRESSION,Integer.class.getCanonicalName());
 							  put(DoubleAxisIndexer.TYPE_EXPRESSION,Double.class.getCanonicalName());
 							  put(TimestampAxisIndexer.TYPE_EXPRESSION, Timestamp.class.getCanonicalName());}};
 
 	protected Connection c;
 	protected String schema;
 	protected String table;
 	protected String label;
 
 	AggrKeyDescriptor kd = null;
 	String indexPrefix = "_ipfx_"; // incomplete, should be unique in database
 
 	private HashMap<Long, AggrRec> internalHash = null;
 
 	protected AggregateAxis axis[];
 	private String aggregateColumn;
 	private String aggregateType;
 
 	public PreAggregate() {
 	}
 
 	public PreAggregate(Connection c, String schema, String table, String label) 
 	throws SQLException {
 		_init(c,schema,table,label);
 	}
 
 	// Is this still used?
 	public PreAggregate(Connection c, String schema, String table, String label, HashMap<Long, AggrRec> internalHash)
 	throws SQLException {
 		_init(c,schema,table,label);
 		this.internalHash = internalHash;
 	}
 
 	public PreAggregate(Connection c, String schema, String table, String label, AggregateAxis axis[], String aggregateColumn, String aggregateType, int aggregateMask, int axisToSplit, long chunkSize, Object[][] newRange) 
 	throws SQLException {
 		createPreAggregate(c,schema,table,label,axis,aggregateColumn,aggregateType,aggregateMask,axisToSplit,chunkSize,newRange);
 	}
 
 	private void _init(Connection c, String schema, String table, String label)
 	throws SQLException {
 		this.c = c;
 		this.schema = schema;
 		this.table = table;
 		this.label = label;
 		if (!read_repository(c,schema,table,label) )
 			throw new SQLException("No PreAggregate " + label + " for table " + schema + "."
 					+ table);
 	}
 
 	public Object[][] getRangeValues(Connection c) throws SQLException {
 		return getRangeValues(c, schema, table, axis);
 	}
 
 	public Object[][] getRangeValues(Connection c, String schema, String table, AggregateAxis axis[]) throws SQLException {
 		int i;
 		Object res[][] = new Object[axis.length][2];
 
 		StringBuilder aggrs = new StringBuilder();
 		for(i=0; i<axis.length; i++ ) {
 			if ( i>0 )
 				aggrs.append(',');
 			aggrs.append("min(");
 			aggrs.append(axis[i].columnExpression());
 			aggrs.append("),max(");
 			aggrs.append(axis[i].columnExpression());
 			aggrs.append(")");
 		}
 		// incomplete, do nothing with the count yet
 		ResultSet rs = SqlUtils.execute(c,"SELECT "+ aggrs + ",count(*) FROM " + schema + "." + table + ";"); 
 		if ( !rs.next() )
 			throw new SQLException("PreAggregate: unable to get range values");
 		for(i=0; i<axis.length; i++ ) {
 			res[i][RMIN] = rs.getObject(1+i*2); // min
 			res[i][RMAX] = rs.getObject(2+i*2); // max
 		}
 		return res;
 	}
 
 	private String rangeFunName(int dim) {
 		return indexPrefix + "d"+dim+"rf";
 	}
 
 	private String create_dimTable(String schema, int dim, int N, int levels, SqlScriptBuilder sql_build) throws SQLException {
 		String tableName = indexPrefix + "dim"+dim;
 
 		sql_build.add("DROP TABLE IF EXISTS " + schema + "." + tableName + ";\n");
 		sql_build.add("CREATE TABLE " + schema + "." + tableName + " (" + "level int," + "factor int" + ");\n");
 		int factor = 0;
 		for (int i=0; i<levels; i++) {
 			factor = (i==0) ? 1 : (factor*N);
 			sql_build.add("INSERT INTO " + schema + "." + tableName + "  (level,factor) VALUES("+i+","+factor+");\n");
 		}
 		sql_build.addPost("DROP TABLE " + schema + "." + tableName + ";\n");
 		return tableName;
 	}
 
 	protected void createPreAggregate(Connection c, String schema,
 			String table, String label, AggregateAxis axis[],
 			String aggregateColumn, String aggregateType, int aggregateMask, 
 			int axisToSplit, long chunkSize, Object[][] newRange) throws SQLException {
 		int i;
 		String dimTable[]	 = new String[axis.length];
 
 		long create_time_ms = new Date().getTime();
 
 		/*
 		 * First initialize and compute the aggregation axis
 		 */
 		if ( showAxisAndKey )
 			System.out.println("#! Aggregate Axis:");
 		Object obj_ranges[][] = getRangeValues(c,schema,table,axis);
 		short maxLevel = 0;
 		for (i = 0; i < axis.length; i++) {
 			if ( !axis[i].hasRangeValues() )
 				axis[i].setRangeValues(obj_ranges[i][RMIN], obj_ranges[i][RMAX]);
 			if ( axis[i].maxLevels() > maxLevel )
 				maxLevel = axis[i].maxLevels();
 			if (showAxisAndKey)
 				System.out.println("AXIS["+i+"]="+axis[i]);
 		}
 
 		kd = new AggrKeyDescriptor(DEFAULT_KD, axis);
 		if (showAxisAndKey)
 			System.out.println("KEY="+kd);
 
 		/*
 		 * Start generating the SQL commands for the pre aggregate index creation. 
 		 * The main code is generated in pre_script. The cleanup code is generated 
 		 * in post_script.
 		 */
 		SqlScriptBuilder sql_build = new SqlScriptBuilder(c);
 
 		for(i=0; i<axis.length; i++) {
 			// generate the range conversion function for the dimension
 			sql_build.add(axis[i].sqlRangeFunction(c, rangeFunName(i)));
 			sql_build.newLine();
 
 			sql_build.addPost(SqlUtils.gen_DROP_FUNCTION(c, rangeFunName(i),axis[i].sqlType()));
 
 			// generate the dimension level/factor value table
 			dimTable[i] = create_dimTable(schema,i,axis[i].N(),axis[i].maxLevels(), sql_build);
 			sql_build.newLine();
 		}
 
 		// generate the function which converts all dimension levels/range indices into one value
 		String genKey = indexPrefix+"genKey";
 		sql_build.add(kd.crossproductLongKeyFunction(c, genKey));
 		sql_build.newLine();
 
 		/*
 		 * Generate the table which contains the final index
 		 */
 		String table_pa = schema + "." + table + PA_EXTENSION;
 		sql_build.add("-- create the table containg the pre aggregate index\n");
 		sql_build.add("DROP TABLE IF EXISTS " + table_pa + ";\n");
 		sql_build.add(
 				"CREATE TABLE " + table_pa + " (\n" +
 				"\tckey bigint NOT NULL PRIMARY KEY,\n" + 
 				((aggregateMask&AGGR_COUNT)!=0 ? "\tcountAggr bigint,\n" : "") + 
 				((aggregateMask&AGGR_SUM) !=0 ? "\tsumAggr "  +aggregateType+",\n" : "") +
 				((aggregateMask&AGGR_MIN)!=0 ? "\tminAggr "+aggregateType+",\n" : "") +
 				((aggregateMask&AGGR_MAX)!=0 ? "\tmaxAggr "+aggregateType+"\n" : "") +
 				");\n"
 		);
 		sql_build.newLine();
 
 		int nChunks = 1;
 		Object ro[][] = null;
 		if ( axisToSplit >= 0 ) {
 			long nTuples = SqlUtils.count(c,schema,table,"*");
 			nChunks = (int) (nTuples / chunkSize) +1;
 			ro = axis[axisToSplit].split(nChunks);
 		}
 		for (i = 0; i < nChunks; i++) {
 			/*
 			 * Generate the level 0 table
 			 */
 			String where = null;
 			if ( nChunks > 1 ) {
 				where = "("+axis[axisToSplit].columnExpression()+">="+ro[i][0]+" AND "+axis[axisToSplit].columnExpression()+"<"+ro[i][1]+")";
 				sql_build.add("-- adding increment "+i+" to pa index\n");
 			} else {
 				sql_build.add("-- computing pa_index in one step\n");
 			}
 			String level0_table = schema + "." + indexPrefix + "level0";
 			sql_build.add("DROP TABLE IF EXISTS " + level0_table + ";\n");
 			//
 			sql_build.add(generate_level0(c, level0_table,
 					schema + "." + table, where, axis, aggregateColumn,
 					aggregateMask));
 			sql_build.newLine();
 			if ( i == 0 ) // drop this table only once
 				sql_build.addPost("DROP TABLE " + level0_table + ";\n");
 			String level0 = level0_table;
 
 			/*
 			 * Now generate the index levels 0 + n-1
 			 */
 			String level0_n_table = schema + "." + indexPrefix + "0_n";
 			sql_build.add("DROP TABLE IF EXISTS " + level0_n_table + ";\n");
 			String level0_n = generate_level0_n(c, level0_n_table, level0,
 					axis, dimTable, genKey, aggregateMask);
 			sql_build.add(level0_n);
 			sql_build.newLine();
 			if ( i == 0 ) // drop this table only once 
 				sql_build.addPost("DROP TABLE " + level0_n_table + ";\n");
 			//
 			if ( nChunks == 1 ) {
 				sql_build.add("INSERT INTO " + table_pa
 						+ " (\n\tSELECT * FROM " + level0_n_table + "\n);");
 				sql_build.newLine();
 			} else {
 				StringBuilder add_aggr = new StringBuilder();
 
 				if ((aggregateMask & AGGR_COUNT) != 0)
 					add_aggr.append(",countAggr = pa_table.countAggr + pa_delta.countAggr");
 				if ((aggregateMask & AGGR_SUM) != 0)
 					add_aggr.append(",sumAggr = pa_table.sumAggr + pa_delta.sumAggr");
 				if ((aggregateMask & AGGR_MIN) != 0)
 					add_aggr.append(",minAggr = LEAST(pa_table.minAggr,pa_delta.minAggr)");
 				if ((aggregateMask & AGGR_MAX) != 0)
 					add_aggr.append(",maxAggr = GREATEST(pa_table.maxAggr,pa_delta.maxAggr)");
 				sql_build
 				.add("UPDATE "
 						+ table_pa
 						+ " AS pa_table \n\tSET "
 						+ add_aggr.substring(1) // remove leading ','
 						+ " \n\tFROM "
 						+ level0_n_table
 						+ " AS pa_delta WHERE pa_delta.ckey = pa_table.ckey;\n\n");
 
 				sql_build
 				.add("INSERT INTO "
 						+ table_pa
 						+ " (\n\tSELECT * FROM "
 						+ level0_n_table
 						+ " AS pa_delta \n\tWHERE NOT EXISTS (SELECT * FROM "
 						+ table_pa
 						+ " AS pa_table WHERE pa_delta.ckey = pa_table.ckey));\n");
 				sql_build.newLine();
 			}
 		}
 
 		if ( true ) {
 			System.out.println("\n#! SCRIPT:\n"+sql_build.getScript());
 			System.out.flush(); // flush before possible crash
 		}
 		sql_build.executeBatch();
 		create_time_ms = new Date().getTime() - create_time_ms;
 
 		if (showAxisAndKey) {
 			long input_tuples = SqlUtils.count(c, schema, table, "*");
 			System.out.print("# input tuples=" + input_tuples);
 			long cells = SqlUtils.count(c, schema, table + PA_EXTENSION, "*");
 			int perc = (int)(((double)cells/(double)input_tuples)*100);
 			System.out.println(", aggregate index cells=" + cells + "["+perc+"%]");
 			System.out.println("# aggregate index creation time = "
 					+ create_time_ms + "ms");
 			System.out.println("");
 		}
 
 		// add the index to the repository
 		update_repository(c, schema, table, label, aggregateColumn, aggregateType, kd, axis, aggregateMask);
 
 		/*
 		 * run the constructor initialization code for the PreAggregate Object
 		 */
 		_init(c, schema, table, label);
 	}
 
 	public String generate_level0(Connection c, String level0_table, String from, String where, AggregateAxis axis[], String aggregateColumn, int aggregateMask) 
 	throws SQLException {
 		/*
 		 * Generate the level 0 table
 		 */
 		int i;
 		StringBuilder select = new StringBuilder();
 		StringBuilder gb	 = new StringBuilder();
 		for(i=0; i<axis.length; i++) {
 			if (i>0) {
 				select.append(",\n\t");
 				gb.append(',');
 			}
 			select.append(rangeFunName(i)+"("+axis[i].columnExpression()+") AS i"+i);
 			gb.append("i"+i);
 		}
 		//
 		if ( where != null )
 			where = "\nWHERE " + where;
 		else
 			where = "";
 		String level0 = SqlUtils.gen_Select_INTO(c, 
 				level0_table,
 				"SELECT\t" + select +
 				((aggregateMask&AGGR_COUNT)!=0 ? ",\n\tCOUNT(" + aggregateColumn + ") AS countAggr" : "") +
 				((aggregateMask&AGGR_SUM) !=0 ? ",\n\tSUM(" + aggregateColumn + ") AS sumAggr" : "") +
 				((aggregateMask&AGGR_MIN) !=0 ? ",\n\tMIN(" + aggregateColumn + ") AS minAggr" : "") +
 				((aggregateMask&AGGR_MAX) !=0 ? ",\n\tMAX(" + aggregateColumn + ") AS maxAggr" : "")
 				, 
 				"FROM " + from + where + "\nGROUP BY "+gb);
 		return level0;
 	}
 
 	public String generate_level0_n(Connection c, String delta_table,
 			String level0, AggregateAxis axis[], String dimTable[],
 			String genKey, int aggregateMask) throws SQLException {
 		int i;
 		StringBuilder select = new StringBuilder();
 		StringBuilder from = new StringBuilder();
 		StringBuilder where = new StringBuilder();
 		StringBuilder gb = new StringBuilder();
 		StringBuilder gk = new StringBuilder();
 		gk.append(genKey + "(");
 		for (i = 0; i < axis.length; i++) {
 			if (i > 0) {
 				select.append(',');
 				from.append(',');
 				gb.append(',');
 				gk.append(',');
 			}
 			if (i > 1)
 				where.append(" AND ");
 			select.append("\n\t\tdim" + i + ".level" + " AS l" + i);
 			select.append(",\n\t\t"
 					+ SqlUtils
 					.gen_DIV(c, "level0.i" + i, "dim" + i + ".factor")
 					+ " AS v" + i);
 			from.append("\n\t\t" + dimTable[i] + " AS dim" + i);
 			gk.append("l" + i + "," + "v" + i);
 			if (i > 0)
 				where.append("dim0.level=dim" + i + ".level");
 			gb.append("l" + i + ",v" + i);
 		}
 		gk.append(")");
 		if ((aggregateMask & AGGR_COUNT) != 0)
 			select.append(",\n\t\tSUM(level0.countAggr) AS countAggr");
 		if ((aggregateMask & AGGR_SUM) != 0)
 			select.append(",\n\t\tSUM(level0.sumAggr) AS sumAggr");
 		if ((aggregateMask & AGGR_MIN) != 0)
 			select.append(",\n\t\tMIN(level0.minAggr) AS minAggr");
 		if ((aggregateMask & AGGR_MAX) != 0)
 			select.append(",\n\t\tMAX(level0.maxAggr) AS maxAggr");
 		String DATA = level0 + " AS level0";
 		from.append(",\n\t\t" + DATA);
 
 		String subindexQ = "SELECT " + select + "\n\t FROM\t" + from
 		+ "\n\tGROUP BY " + gb;
 
 		String aggrAttr = ((aggregateMask & AGGR_COUNT) != 0 ? ",\n\tcountAggr"
 				: "")
 				+ ((aggregateMask & AGGR_SUM) != 0 ? ",\n\tsumAggr" : "")
 				+ ((aggregateMask & AGGR_MIN) != 0 ? ",\n\tminAggr" : "")
 				+ ((aggregateMask & AGGR_MAX) != 0 ? ",\n\tmaxAggr" : "");
 
 		// first update, then insert to prevent problems
 		String delta = SqlUtils.gen_Select_INTO(c, delta_table, "SELECT \t"
 				+ gk + " as ckey" + aggrAttr, "FROM\t(" + subindexQ
 				+ ") AS siq"); // incomplete
 		return delta;
 	}
 
 	/*
 	 * 
 	 * The Query section
 	 * 
 	 */
 	public ResultSet SQLquery_interval(int queryAggregateMask, Object obj_range_interval[][][]) throws SQLException {
 		ResultSet result = null;
 
 		StringBuilder qb = new StringBuilder();
 
 		for(int i=0; i<obj_range_interval.length; i++) {
 			Object obj_range[][] = obj_range_interval[i];
 
 			if ( i > 0 )
 				qb.append("UNION\n");
 			qb.append("(");
 			qb.append(SQLquery_string(queryAggregateMask,obj_range,i + " AS i, "));
 			qb.append(")\n");
 		}
 		String sql = qb + ";";
 		System.out.println("# interval query=\n" + sql);
 		result = SqlUtils.execute(c, sql);
 		return result;
 	}
 
 	public ResultSet SQLquery_grid(int queryAggregateMask, Object iv_first_obj[][], int iv_count[]) throws SQLException {
 		ResultSet result = null;
 		int i;
 
 		if ( iv_first_obj.length != axis.length )
 			throw new SQLException("SQLquery_grid: dimension of first interval does not meet axis");
 		if ( iv_count.length != axis.length )
 			throw new SQLException("SQLquery_grid: dimension of interval count does not meet axis");
 		int iv_size[] = new int[axis.length];
 		int iv_first[] = new int[axis.length];
 		for(i=0; i<axis.length; i++) {
 			AggregateAxis ax = axis[i];
 
 			if (false) {
 				if (!ax.exactIndex(iv_first_obj[i][RMIN]))
 					throw new SQLException(
 							"SQLquery_grid: start of first interval in dim "
 							+ i + " not on boundary: "
 							+ iv_first_obj[i][RMIN]);
 				if (!ax.exactIndex(iv_first_obj[i][RMAX]))
 					throw new SQLException(
 							"SQLquery_grid: end of first interval in dim " + i
 							+ " not on boundary: "
 							+ iv_first_obj[i][RMAX]);
 			}
 			iv_first[i] = ax.getIndex(iv_first_obj[i][RMIN]);
 			iv_size[i] = ax.getIndex(iv_first_obj[i][RMAX]) - iv_first[i];
 		}
 		// now have all the info we need
 		// what do do in the sql case ?
 		if ( serversideStairwalk ) {
 			int swgc[][] = new int[axis.length][3]; // start/width/gridcells
 
 			for(i=0; i<axis.length; i++) {
 				swgc[i][0] = iv_first[i]; // start
 				swgc[i][1] = iv_size[i]; // width
 				swgc[i][2] = iv_count[i]; // gridcells
 			}
 			StringBuilder sqlaggr = new StringBuilder();
 			if ((queryAggregateMask & aggregateMask & AGGR_COUNT) != 0)
 				sqlaggr.append(",sum(countAggr) AS countAggr");
 			if ((queryAggregateMask & aggregateMask & AGGR_SUM) != 0)
 				sqlaggr.append(",sum(sumAggr) AS sumAggr");
 			if ((queryAggregateMask & aggregateMask & AGGR_MIN) != 0)
 				sqlaggr.append(",min(minAggr) AS minAggr");
 			if ((queryAggregateMask & aggregateMask & AGGR_MAX) != 0)
 				sqlaggr.append(",max(maxAggr) AS maxAggr");
 			String gcells = "pa_grid(\'" + grid_paGridQuery(swgc) + "\')";
 			String sql = "SELECT gkey"+sqlaggr+" FROM "+schema+"."+table+PA_EXTENSION+", "+gcells+ " WHERE ckey=pakey GROUP BY gkey order by gkey;";
 			System.out.println("XXX="+sql);			
 			result = SqlUtils.execute(c,sql);
 		} else {
 			// explode it
 			PermutationGenerator p = new PermutationGenerator(axis.length);
 			for(i=0; i<axis.length; i++) {
 				p.setRange(i,0,iv_count[i]);
 			}
 			int		ranges[][] = new int[axis.length][2];
 			StringBuilder qb = new StringBuilder();
 			p.start();
 			while ( p.next() ) {
 				StringBuilder ib = new StringBuilder();
 
 				for(i=0; i<axis.length; i++) {
 					ranges[i][RMIN] = iv_first[i] + iv_size[i] * (p.permutation[i] + 0);
 					ranges[i][RMAX] = iv_first[i] + iv_size[i] * (p.permutation[i] + 1);
 
 					ib.append( p.permutation[i] + " AS d"+i+", ");
 				}
 				if ( qb.length() > 0 )
 					qb.append("UNION\n");
 				qb.append( SQLquery_string(queryAggregateMask,ib.toString(),ranges) + "\n");
 				// incomplete
 			}
 			qb.append(';');
 			System.out.println("#! executing Grid Query:\n"+qb);
 			result = SqlUtils.execute(c, qb.toString());
 
 		}
 		return result;
 	}
 
 	public ResultSet SQLquery(int queryAggregateMask, Object obj_range[][]) throws SQLException {
 		ResultSet result = null;
 
 		String sql = SQLquery_string(queryAggregateMask,obj_range,"") + ";";
 		System.out.println("# main query=\n" + sql);
 		result = SqlUtils.execute(c, sql);
 		return result;
 	}
 
 	private String SQLquery_string(int queryAggregateMask, Object obj_range[][], String extra) throws SQLException {
 		int		i;
 		int		ranges[][] = new int[axis.length][2];
 
 		if ( obj_range.length != axis.length )
 			throw new SQLException("PreAggregate.query(): dimension index and query do not match");
 		boolean needsCorrection = false;
 		for(i=0; i<axis.length; i++) {
 			ranges[i][RMIN] = axis[i].getIndex(obj_range[i][RMIN]);
 			ranges[i][RMAX] = axis[i].getIndex(obj_range[i][RMAX]);
 			if (	doResultCorrection && (
 					!axis[i].exactIndex(obj_range[i][RMIN]) ||
 					!axis[i].exactIndex(obj_range[i][RMAX])
 			)) {
 				needsCorrection = true;
 			}
 		}
 		return SQLquery_string(queryAggregateMask,extra,ranges);
 	}
 
 	private String SQLquery_string(int queryAggregateMask, String extra_select, int ranges[][]) throws SQLException {
 		if ( queryAggregateMask == 0)
 			throw new SQLException("pre aggregate query without aggregates");
 		else {
 			// incomplete, check if the query_aggregates are in the pre_aggregate cells
 		}
 		System.out.println("#!AGGR_MASK="+aggregateMask+", QueryMask="+queryAggregateMask);
 		StringBuilder b_sqlaggr = new StringBuilder();
 		if ((queryAggregateMask&AGGR_COUNT)!=0) b_sqlaggr.append(",SUM(countAggr) AS countAggr");
 		if ((queryAggregateMask&AGGR_SUM)!=0) b_sqlaggr.append(",SUM(sumAggr) AS sumAggr");
 		if ((queryAggregateMask&AGGR_MIN)!=0) b_sqlaggr.append(",MIN(minAggr) AS minAggr");
 		if ((queryAggregateMask&AGGR_MAX)!=0) b_sqlaggr.append(",MAX(maxAggr) AS maxAggr");
 		String sqlaggr = b_sqlaggr.substring(1); // rome heading ,
 
 		StringBuffer qb = new StringBuffer("SELECT " + extra_select + sqlaggr + " FROM "+schema+"."+table+PA_EXTENSION);
 		System.out.println("$ pa-command = "+range_paGridQuery(ranges));
 		qb.append(cellCondition(ranges));
 		return qb.toString();
 	}
 
 	private String cellCondition(int ranges[][]) {
 		if ( serversideStairwalk ) {
 			// use Postgres internal pacells2d function
 			String pa_grid_str;
 
 			if ( false )
 				pa_grid_str = "pa_grid('"+range_paGridQuery(ranges)+"')";
 			else
 				pa_grid_str = "pa_grid_cell('"+range_paGridQuery(ranges)+"') AS pakey "; // 2 times faster
 			return ", " + pa_grid_str + " WHERE ckey=pakey";
 		} else {
 			StringBuilder qb = new StringBuilder();
 
 			Vector<AggrKey> resKeys = computePaCells(kd,ranges,axis);
 			System.out.println("#!create WHERE {ckey=v} query: #keys="+resKeys.size());
 			qb.append(" WHERE ");
 			for (int i = 0; i < resKeys.size(); i++) {
 				qb.append(((i > 0) ? " OR " : "") + "ckey="
 						+ resKeys.elementAt(i).toKey());
 			}
 			return qb.toString();
 		}
 	}
 
 	public long query(String aggr, Object obj_range[][]) throws SQLException {
 		// only for legacy and code examples
 		int		i;
 		int		ranges[][] = new int[axis.length][2];
 
 		if ( obj_range.length != axis.length )
 			throw new SQLException("PreAggregate.query(): dimension index and query do not match");
 		boolean needsCorrection = false;
 		for(i=0; i<axis.length; i++) {
 			ranges[i][RMIN] = axis[i].getIndex(obj_range[i][RMIN]);
 			ranges[i][RMAX] = axis[i].getIndex(obj_range[i][RMAX]);
 			if (	doResultCorrection && (
 					!axis[i].exactIndex(obj_range[i][RMIN]) ||
 					!axis[i].exactIndex(obj_range[i][RMAX])
 			)) {
 				needsCorrection = true;
 			}
 		}
 		long direct_result = -1;
 		long pg_direct_time_ms = -1;
 		if ( false && useDirect ) {
 			String daggr = "x"; 
 			if ( aggr.equals("count") )
 				daggr = "count(*)";
 			else if ( aggr.equals("sumAggr") )
 				daggr = "sum(sumAggr)";
 			else
 				throw new SQLException("unexpected aggr: "+aggr);
 			String cond = "";
 			for(i=0; i<axis.length; i++) {
 				if ( i > 0 )
 					cond += " AND ";
 				cond += "(" + axis[i].columnExpression() + ">= ? ) AND (" + axis[i].columnExpression() + "<= ?)";
 			}
 			pg_direct_time_ms = new Date().getTime();
 			PreparedStatement ps = c.prepareStatement("SELECT "+daggr+" from "+schema+"."+table + " WHERE " + cond + ";");
 			for(i=0; i<axis.length; i++) {
 				ps.setObject(1 + i*2, obj_range[i][RMIN]);
 				ps.setObject(2 + i*2, obj_range[i][RMAX]);
 			}
 			// System.out.println("#! Direct query="+ps.toString());
 			ResultSet rs = ps.executeQuery();
 			pg_direct_time_ms = new Date().getTime() - pg_direct_time_ms;
 			if ( !rs.next() )
 				throw new RuntimeException("NO RESULT");
 			direct_result = rs.getLong(1);
 			System.out.println("# direct result = "+direct_result+", time = "+pg_direct_time_ms+"ms");
 		}
 		//
 		PreparedStatement correction_stat = null;
 		if ( needsCorrection && doResultCorrection )
 			correction_stat = correctionStatement(ranges,obj_range);
 
 		// kd.switchSubindexOff();
 
 		Vector<AggrKey> resKeys;
 		resKeys = computePaCells(kd,ranges,axis);
 
 		String sqlaggr;
 		if ( aggr.equals("count") )
 			sqlaggr = "sum(countAggr)";
 		else if ( aggr.equals("sum") )
 			sqlaggr = "sum(sumAggr)";
 		else if ( aggr.equals("min") )
 			sqlaggr = "min(minAggr)";
 		else if ( aggr.equals("max") )
 			sqlaggr = "max(maxAggr)";
 		else
 			throw new SQLException("unexpected aggr: "+aggr);
 		StringBuffer qb = new StringBuffer("SELECT "+sqlaggr+" FROM "+schema+"."+table+PA_EXTENSION);
 		System.out.println("$ pa-command = "+range_paGridQuery(ranges));
 		long internalCount = 0;
 		if ( serversideStairwalk ) {
 			// use Postgres internal pacells2d function
 			String pa_grid_str;
 
 			if ( false )
 				pa_grid_str = "pa_grid('"+range_paGridQuery(ranges)+"')";
 			else
 				pa_grid_str = "pa_grid_cell('"+range_paGridQuery(ranges)+"') AS pakey "; // 2 times faster
 			qb.append(", ");
 			qb.append(pa_grid_str);
 			qb.append(" WHERE ckey=pakey");
 
 		} else {
 			System.out.println("#!create WHERE {ckey=v} query: #keys="+resKeys.size());
 			qb.append(" WHERE ");
 			for (i = 0; i < resKeys.size(); i++) {
 				qb.append(((i > 0) ? " OR " : "") + "ckey="
 						+ resKeys.elementAt(i).toKey());
 				if (internalHash != null) {
 					AggrRec r = internalHash.get(resKeys.elementAt(i));
 					if (r != null) {
 						internalCount += r.getCount();
 						// System.out.println("#cell: "+lxy_toString(resKeys.elementAt(i).longValue())+"="+r.getCount());
 					}
 				}
 			}
 		}
 		qb.append(";");
 		long pg_time_ms = -1;
 		long result = -1, correction = -1;
 		if ( true ) {
 			System.out.println("# main query= " + qb);
 			pg_time_ms = new Date().getTime();
 			result = SqlUtils.execute_1long(c, qb.toString());
 			pg_time_ms = new Date().getTime() - pg_time_ms;
 			System.out.println("# answer = "+result+", time = "+pg_time_ms+"ms");
 			//
 			if ( correction_stat != null ) {
 				// System.out.println("# corr query= " + correction_stat);
 				pg_time_ms = new Date().getTime();
 				ResultSet rs = correction_stat.executeQuery();
 				if ( !rs.next() )
 					throw new SQLException("correction result empty");
 				correction = - rs.getLong(1)	;
 				pg_time_ms = new Date().getTime() - pg_time_ms;
 				System.out.println("# correction="+correction+", time = "+pg_time_ms+"ms");
 			}
 		}
 		if (true) {
 			System.out.println("# schema="+schema+", table="+table+", Dqres="+resKeys.size());
 			System.out.print("# main answer="+result);
 			if ( correction_stat != null )
 				System.out.print(", corrected answer="+(result + correction));
 			if ( useDirect )
 				System.out.print(", direct answer="+direct_result+".");
 			System.out.println("");
 		}
 		return result;
 	}
 
 	private PreparedStatement correctionStatement(int ranges[][], Object obj_range[][])
 	throws SQLException {
 		int i;
 		Object rangesREV[][] = new Object[axis.length][2];
 
 		for (i = 0; i < axis.length; i++) {
 			rangesREV[i][RMIN] = axis[i].reverseValue(ranges[i][RMIN]);
 			rangesREV[i][RMAX] = axis[i].reverseValue(ranges[i][RMAX] + 1);
 		}
 		StringBuilder sb = new StringBuilder();
 		Vector<Object> all = new Vector<Object>();
 		Object outer[][] = rangesREV;
 		Object inner[][] = obj_range;
 
 		for (i = 0; i < axis.length; i++) {
 			StringBuilder lsb = new StringBuilder();
 			Vector<Object> lv = new Vector<Object>();
 			StringBuilder hsb = new StringBuilder();
 			Vector<Object> hv = new Vector<Object>();
 
 			String ci = axis[i].columnExpression();
 			lsb.append(ci + ">=" + '?' + " AND " + ci + "<" + '?');
 			lv.add(outer[i][RMIN]);
 			lv.add(inner[i][RMIN]);
 			hsb.append(ci + ">" + '?' + " AND " + ci + "<=" + '?');
 			hv.add(inner[i][RMAX]);
 			hv.add(outer[i][RMAX]);
 			for (int j = 0; j < axis.length; j++)
 				if (i != j) {
 					String cj = axis[j].columnExpression();
 					String di;
 					Vector<Object> dv = new Vector<Object>();
 					if (i < j) {
 						di = cj + ">=" + '?' + " AND " + cj + "<=" + '?';
 						dv.add(outer[j][RMIN]);
 						dv.add(outer[j][RMAX]);
 					} else {
 						di = cj + ">" + '?' + " AND " + cj + "<" + '?';
 						dv.add(inner[j][RMIN]);
 						dv.add(inner[j][RMAX]);
 					}
 					lsb.append(" AND " + di);
 					lv.addAll(dv);
 					hsb.append(" AND " + di);
 					hv.addAll(dv);
 				}
 			if (i > 0)
 				sb.append(" OR ");
 			sb.append("(" + lsb + ")\n");
 			all.addAll(lv);
 			sb.append(" OR (" + hsb + ")\n");
 			all.addAll(hv);
 		}
 		PreparedStatement ps = c.prepareStatement("SELECT count(*) from " + schema + "." + table
 				+ " WHERE " + sb + ";");
 		for(i=0; i<all.size(); i++)
 			ps.setObject(i+1,all.get(i));
 		return ps;
 	}
 
 	private static boolean qverbose = false;
 
 	private static final Vector<Long> stairwalk(int from, int to, int N) {
 		Vector<Long> res = new Vector<Long>();
 		to++; // last step must be to 1 beyond upper bound
 		short level = 0;
 		int step  = 1;
 		int nowAt = from;
 		// first walk up the stairs
 		while (nowAt <= to) {
 			// System.out.println("* Check up: nowAt%(step*N)="+(nowAt % (step*N))+", (nowAt+step*N)="+(nowAt+step*N)+", level="+level+", step="+step+", to="+to);
 			if ( (nowAt % (step*N) == 0) && ((nowAt+step*N)<=to) ) {
 				// I can make a step up
 				level++;
 				step *= N;
 				// System.out.println("* Stepping down: nowAt="+nowAt+", level="+level+", step="+step);
 			} else {
 				// mark this one for selection
 				if ( (nowAt+step) <= to ) {
 					// System.out.println("+ Adding: nowAt="+li_toString(li_key(level,nowAt/step)));
 					res.add(li_key(level,nowAt/step));
 				}
 				nowAt += step;
 			}
 		}
 		nowAt -= step;	// do step back you're too far
 		// System.out.println("* Stepping down: nowAt="+nowAt+", level="+level);
 		while (nowAt < to) {
 			// System.out.println("L["+level+"]: nowAt="+nowAt+", step="+step);
 			if ((nowAt + step) > to) {
 				// step down;
 				level--;
 				step /= N;
 			} else {
 				// make a step
 				if ( (nowAt+step) <= to ) {
 					res.add(li_key(level,nowAt/step));
 					// System.out.println("+ Adding: nowAt="+li_toString(li_key(level,nowAt/step)));
 					nowAt += step;
 				}
 			}
 		}
 		if ( qverbose ) {
 			System.out.println("# do stairwalk(from="+from+",to="+to+",N="+N+") = {");
 			for(int i=0; i<res.size(); i++)
 				System.out.println("\t"+li_toString(res.elementAt(i).longValue()));
 			System.out.println("}");
 		}
 		return res;
 	}
 
 	protected String empty_paGridQuery() { // just for key manipulations
 		int swgc[][] = new int[axis.length][3]; // start/width/gridcells
 
 		for(int i=0; i<axis.length; i++) {
 			swgc[i][0] = 0; // start
 			swgc[i][1] = 0; // width
 			swgc[i][2] = 0; // gridcells
 		}
 		return grid_paGridQuery(swgc);
 	}
 
 	protected String range_paGridQuery(int range[][]) {
 		int swgc[][] = new int[range.length][3]; // start/width/gridcells
 
 		for(int i=0; i<range.length; i++) {
 			swgc[i][0] = range[i][RMIN]; // start
 			swgc[i][1] = range[i][RMAX] - range[i][RMIN]; // width
 			swgc[i][2] = 1; // gridcells
 		}
 		return grid_paGridQuery(swgc);
 	}
 
 	protected String grid_paGridQuery(int swgc[][]) {
 		if ( (swgc.length != axis.length) || (swgc[0].length != 3) )
 			throw new RuntimeException("Dimensions for grid_paQuery wrong");
 		StringBuffer sb = new StringBuffer();
 		sb.append("#G|"+kd.kind()+"|");
 		// sb.append((kd.isSubindexed()?"T":"F")+"|");
 		sb.append(kd.levelBits+"|");
 		sb.append(kd.dimensions()+"|");
 		for(int i=0; i<swgc.length; i++) {
 			sb.append(axis[i].N()+",");
 			sb.append(kd.dimBits[i]+",");
 			sb.append(swgc[i][0]+","+swgc[i][1]+","+swgc[i][2]+"|");
 		}
 		sb.append(schema+"."+table+"|"+schema+"."+table+"_btree"+"|");
 		return sb.toString();
 	}
 
 	private static Vector<AggrKey> computePaCells(AggrKeyDescriptor kd, int range[][], AggregateAxis axis[]) {
 		Vector<Vector<Long>> stairs = new Vector<Vector<Long>>();
 
 		PermutationGenerator p = new PermutationGenerator(axis.length);
 		for(int i=0; i<axis.length; i++) {
 			Vector<Long> sw_res = stairwalk(range[i][RMIN], range[i][RMAX], axis[i].N());
 			stairs.add(sw_res);
 			p.setRange(i,0,sw_res.size());
 		}
 		AggrKey K = new AggrKey(kd);
 		Vector<AggrKey> res = new Vector<AggrKey>();
 		p.start();
 		while ( p.next() ) {
 			// incomplete, analyze permutation levels in no subindexing case
 			K.reset();
 			for(short i=0; i<axis.length; i++) {
 				long lk = stairs.elementAt(i).elementAt(p.permutation(i));
 				K.setIndex(i, li_i(lk));
 				K.setLevel(i, li_l(lk));
 			}
 			res.add( K.copy() );
 		}
 		if (qverbose) {
 			System.out.println("- RESULT(" + res.size() + ") = {");
 			for (int i = 0; i < res.size(); i++)
 				System.out.println("\t" + res.elementAt(i));
 			System.out.println("}");
 		}
 		return res;
 	}
 
 	/*
 	 * The repository management section.
 	 */
 	private static void checkRepository(Connection c, String schema) throws SQLException{
 		// create one repository per schema
 		if (!SqlUtils.existsTable(c, schema, aggregateRepositoryName)) {
 			SqlUtils.executeNORES(c,
 					"CREATE TABLE " + schema + "." + aggregateRepositoryName + " (" +
 					"tableName TEXT," +
 					"label TEXT," +
 					"aggregateColumn TEXT," +
 					"aggregateType TEXT," +
 					"dimensions int," +
 					"keyflag char," +
 					"aggregateMask int" +
 					");"
 			);
 		}
 
 		if (!SqlUtils.existsTable(c, schema, aggregateRepositoryDimName)) {
 			SqlUtils.executeNORES(c, 
 					"CREATE TABLE " + schema + "." + aggregateRepositoryDimName + " (" +
 					"tableName TEXT," + 
 					"label TEXT," +
 					"dimension int," +
 					"columnExpression TEXT," + 
 					"type TEXT," +
 					"low TEXT," + 
 					"high TEXT," + 
 					"BASEBLOCKSIZE TEXT," +
 					"N int," +
 					"Nmax int," +
 					"bits int" +
 			");");
 		}
 	}
 
 	private boolean read_repository(Connection c, String schema, String tableName, String label) throws SQLException {
 		if(!SqlUtils.existsTable(c, schema, tableName)) return false; // CHECK Andreas Change
 		ResultSet rs = SqlUtils.execute(c,
 				"SELECT " + "*" +
 				" FROM " + schema + "." + aggregateRepositoryName +
 				" WHERE tableName=\'"+tableName+"\' AND label=\'"+label+"\';"
 		);
 		if (!rs.next())
 			return false;
 		int dimensions = rs.getInt(3);
 		char keyFlag = rs.getString(4).charAt(0);
 		aggregateMask = rs.getInt(5);
 		aggregateColumn = rs.getString(7);
 		aggregateType = rs.getString(8);
 		AggregateAxis read_axis[] = new AggregateAxis[dimensions];
 
 		ResultSet rsi = SqlUtils.execute(c,
 				"SELECT "+"dimension,columnExpression,type,low,high,BASEBLOCKSIZE,N"+" FROM " + schema + "." + aggregateRepositoryDimName +
 				" WHERE tableName=\'"+tableName+"\' AND label=\'"+label+"\';"
 		);
 		while( rsi.next() ) {
 			read_axis[rsi.getInt(1)] = new AggregateAxis(
 					rsi.getString(2),
 					rsi.getString(3),
 					rsi.getString(4),
 					rsi.getString(5),
 					rsi.getString(6),
 					(short)rsi.getInt(7)
 			);
 		}
 		for (int i=0; i<dimensions; i++) {
 			if ( read_axis[i] == null )
 				throw new NullPointerException();
 		}
 		axis = read_axis;
 		//
 		kd = new AggrKeyDescriptor(keyFlag,axis); // INCOMPLETE, put in flag type
 		//
 		return true;
 	}
 
 	private static void update_repository(Connection c, String schema, String tableName, String label, String aggregateColumn, String aggregateType, AggrKeyDescriptor kd, AggregateAxis axis[], int aggregateMask)
 	throws SQLException {
 		checkRepository(c,schema);
 		SqlUtils.executeNORES(c,
 				"DELETE FROM " + schema + "." + aggregateRepositoryName +
 				" WHERE tableName=\'"+tableName+"\' AND label=\'"+label+"\';"
 		);
 		SqlUtils.executeNORES(c,
 				"DELETE FROM " + schema + "." + aggregateRepositoryDimName +
 				" WHERE tableName=\'"+tableName+"\' AND label=\'"+label+"\';"
 		);
 		long cnt = SqlUtils.count(c, schema, tableName, "*");
 		PreparedStatement ps = c.prepareStatement("INSERT INTO " + schema + "." + aggregateRepositoryName + "  (" +
 				"tableName," +
 				"label," +
 				"aggregateColumn,"+
 				"aggregateType,"+
 				"dimensions," +
 				"keyflag," +
 				"aggregateMask," +
 				"count"+
 		") VALUES(?,?,?,?,?,?,?,?);");
 		PreparedStatement psi = c.prepareStatement("INSERT INTO " + schema + "." + aggregateRepositoryDimName + "  (" +
 				"tableName," +
 				"label," +
 				"dimension," +
 				"columnExpression," + 
 				"type," +
 				"low," + 
 				"high," + 
 				"BASEBLOCKSIZE," +
 				"N," +
 				"Nmax," +
 				"bits"  +
 		") VALUES(?,?,?,?,?,?,?,?,?,?,?);");
 		ps.setString(1,tableName);
 		ps.setString(2,label);
 		ps.setString(3,aggregateColumn);
 		ps.setString(4,aggregateType);
 		ps.setInt(5,axis.length);
 		ps.setString(6,String.valueOf(kd.kind())); // INCOMPLETE
 		ps.setInt(7,aggregateMask);
 		ps.setInt(8, (int)cnt);
 		for(int i=0; i<axis.length; i++) {
 			psi.setString(1,tableName);
 			psi.setString(2,label);
 			psi.setInt(   3,i);
 			psi.setString(4, axis[i].columnExpression());
 			psi.setString(5, axis[i].type());
 			psi.setString(6, axis[i].storageFormat(axis[i].low()));
 			psi.setString(7, axis[i].storageFormat(axis[i].high()));
 			psi.setString(8, axis[i].BASEBLOCKSIZE().toString());
 			psi.setInt(   9, axis[i].N());
 			psi.setInt(  10, axis[i].maxLevels());
 			psi.setInt(  11, axis[i].bits());
 			psi.execute();
 		}
 		ps.execute();
 	}
 
 	public AggregateAxis[] getAxis(){
 		return axis;
 	}
 
 	public int getAggregateMask() {
 		return aggregateMask;
 	}
 
 	public String getSchema() {
 		return schema;
 	}
 
 	public String getTable() {
 		return table;
 	}
 
 	public String getLabel() {
 		return label;
 	}
 
 	static public List<String> availablePreAggregates(Connection c, String schema) throws SQLException{
 		if (!SqlUtils.existsTable(c, schema, aggregateRepositoryName)) return null;
 		String query = "select tablename,label from "+aggregateRepositoryName+";";
 		ResultSet rs = SqlUtils.execute(c,query);
 		Vector<String> ret = new Vector<String>();
 		while(rs.next()){
 			ret.add(getTypeName(rs.getString("tablename"),rs.getString("label")));
 		}
 		return ret;
 	}
 
 	public static String getTypeName(String tablename, String label) {
 		return tablename+"___"+label;
 	}
 	
 	public static String getTablenameFromTypeName(String typename) {
 		if(typename.contains("___"))
 			return typename.split("___")[0];
 		else return typename;
 	}
 	
 	public static String getLabelFromTypeName(String typename) {
 		if(typename.contains("___"))
 			return typename.split("___")[1];
 		else return "";
 	}
 	
 	public Map<String,Class> getColumnTypes(int mask) throws ClassNotFoundException{
 		Class cl = Class.forName(className.get(aggregateType));
 		LinkedHashMap<String,Class> ret = new LinkedHashMap<String,Class>();
 		if((mask & this.aggregateMask & PreAggregate.AGGR_COUNT) != 0)
 			ret.put("countaggr", Long.class);
 		if((mask & this.aggregateMask & PreAggregate.AGGR_SUM) != 0)
 			ret.put("sumaggr", cl);
 		if((mask & this.aggregateMask & PreAggregate.AGGR_MIN) != 0)
 			ret.put("min", cl);
 		if((mask & this.aggregateMask & PreAggregate.AGGR_MAX) != 0)
 			ret.put("max", cl);
 		// check whether time is contained in the axis
 		for(AggregateAxis a : axis){
 			if(TimestampAxisIndexer.TYPE_EXPRESSION.equals(a.sqlType())){
 				// we have a time axis
 				ret.put("starttime", Timestamp.class);
 				ret.put("endtime", Timestamp.class);
 			} 
 		}
 		return ret;
 	}
 	
 	//	public static void main(String[] argv) {
 	//		qverbose = true;
 	//		AggrKeyDescriptor kd = new AggrKeyDescriptor();
 	//		short axisN[] = {2,2};
 	//
 	//		int range[][] = {
 	//				{61, 214 },
 	//				{154, 261 }
 	//		};
 	//		// pacells_2d(kd,ranges[0][RMIN],ranges[0][RMAX],axisN[0],ranges[1][RMIN],ranges[1][RMAX],axisN[1],null);
 	//		// pacellsN(kd,range,axisN);
 	//	}
 
 }
