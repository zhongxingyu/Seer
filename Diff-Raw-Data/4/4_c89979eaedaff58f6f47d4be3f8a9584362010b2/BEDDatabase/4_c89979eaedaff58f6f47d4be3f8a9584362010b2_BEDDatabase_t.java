 /*--------------------------------------------------------------------------
  *  Copyright 2008 utgenome.org
  *
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  *--------------------------------------------------------------------------*/
 //--------------------------------------
 // utgb-shell Project
 //
 // BEDDatabaseGenerator.java
 // Since: May 26, 2009
 //
 // $URL$ 
 // $Author$
 //--------------------------------------
 package org.utgenome.format.bed;
 
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.ObjectOutputStream;
 import java.io.Reader;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.utgenome.UTGBException;
 import org.utgenome.gwt.utgb.client.bio.BEDGene;
 import org.utgenome.gwt.utgb.client.bio.ChrLoc;
 import org.utgenome.gwt.utgb.client.bio.OnGenome;
 import org.xerial.core.XerialException;
 import org.xerial.db.sql.ResultSetHandler;
 import org.xerial.db.sql.SQLExpression;
 import org.xerial.db.sql.sqlite.SQLiteAccess;
 import org.xerial.lens.Lens;
 import org.xerial.util.log.Logger;
 
 /**
  * Query/Update interface for BED database stored in SQLite
  * 
  * @author leo
  * 
  */
 public class BEDDatabase {
 
 	private static Logger _logger = Logger.getLogger(BEDDatabase.class);
 
 	public static void toSQLiteDB(Reader bedInput, String dbName) throws IOException, XerialException {
 		BED2SilkReader reader = new BED2SilkReader(bedInput);
 
 		DBBuilder query;
 		try {
 
 			Class.forName("org.sqlite.JDBC");
 			Connection conn = DriverManager.getConnection("jdbc:sqlite:" + dbName);
 
 			query = new DBBuilder(conn);
 			Lens.loadSilk(query, reader);
 
 			query.dispose();
 		}
 		catch (Exception e) {
 			_logger.error(e);
 		}
 	}
 
 	public static class DBBuilder {
 
 		int geneCount = 0;
 
 		Connection conn;
 		PreparedStatement p1;
 		PreparedStatement p2;
 		Statement stat;
 
 		public DBBuilder(Connection conn) throws SQLException {
 			this.conn = conn;
 			this.stat = conn.createStatement();
 
 			stat.executeUpdate("pragma synchronous = off");
 
 			conn.setAutoCommit(false);
 
 			stat.executeUpdate("drop table if exists track");
 			stat.executeUpdate("drop table if exists gene");
 			stat.executeUpdate("create table track (object blob)");
 
 			stat.executeUpdate("create table gene (coordinate text, start integer, end integer, " + "name text, score integer, strand text, cds text, "
 					+ "exon text, color text)");
 
 			p1 = conn.prepareStatement("insert into track values(?)");
 			p2 = conn.prepareStatement("insert into gene values(?, ?, ?, ?, ?, ?, ?, ?, ?)");
 
 		}
 
 		public void dispose() throws SQLException {
 
 			_logger.info("creating indexes...");
 			stat.executeUpdate("create index gene_index on gene (coordinate, start)");
 
 			conn.commit();
 
 			p1.close();
 			p2.close();
 			stat.close();
 			conn.close();
 		}
 
 		public void addTrack(BEDTrack track) {
 			// store track data to db
 			try {
 				ByteArrayOutputStream buf = new ByteArrayOutputStream();
 				ObjectOutputStream out = new ObjectOutputStream(buf);
 				out.writeObject(track);
 				out.flush();
 
 				p1.setBytes(1, buf.toByteArray());
 				p1.execute();
 			}
 			catch (Exception e) {
 				_logger.error(e);
 			}
 		}
 
 		public void addGene(BEDEntry gene) {
 			// store gene data to db
 			try {
 				p2.setString(1, gene.coordinate);
 				p2.setLong(2, gene.getStart());
 				p2.setLong(3, gene.getEnd());
 				p2.setString(4, gene.getName());
 				p2.setInt(5, gene.score);
 				p2.setString(6, Character.toString(gene.getStrand()));
 				p2.setString(7, gene.getCDS().toString());
 				p2.setString(8, gene.getExon().toString());
 				p2.setString(9, gene.getColor());
 
 				p2.execute();
 
 				geneCount++;
 				if ((geneCount % 10000) == 0)
 					_logger.info(String.format("added %d entries.", geneCount));
 
 			}
 			catch (Exception e) {
 				_logger.error(e);
 			}
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	public static List<OnGenome> overlapQuery(File bedPath, final ChrLoc location) throws UTGBException {
 
 		final ArrayList<OnGenome> geneList = new ArrayList<OnGenome>();
 		int sqlStart = location.end >= location.start ? location.start : location.end;
 		int sqlEnd = location.end >= location.start ? location.end : location.start;
 
 		try {
 			File dbInput = new File(bedPath.getAbsolutePath() + ".sqlite");
 			if (dbInput.exists()) {
 				// use db
 				SQLiteAccess dbAccess = new SQLiteAccess(dbInput.getAbsolutePath());
 
 				String sql = SQLExpression.fillTemplate("select start, end, name, score, strand, cds, exon, color from gene "
 						+ "where coordinate = '$1' and ((start between $2 and $3) or (start <= $2 and end >= $3))", location.chr, sqlEnd, sqlStart);
 
 				if (_logger.isDebugEnabled())
 					_logger.debug(sql);
 
 				dbAccess.query(sql, new ResultSetHandler() {
 					@Override
 					public Object handle(ResultSet rs) throws SQLException {
 						geneList.add(new BEDGene(BEDEntry.createFromResultSet(location.chr, rs)));
 						return null;
 					}
 				});
 			}
 			else {
 				// use raw text
 				BED2SilkReader in = null;
 				try {
 					in = new BED2SilkReader(new FileReader(bedPath));
 					BEDRangeQuery query = new BEDRangeQuery(geneList, location.chr, sqlStart, sqlEnd);
 					Lens.loadSilk(query, in);
 				}
 				finally {
 					if (in != null)
 						in.close();
 				}
 			}
 		}
 		catch (Exception e) {
 			throw UTGBException.convert(e);
 		}
 
 		return geneList;
 	}
 
 	public static class BEDRangeQuery implements BEDQuery {
 		private String coordinate;
 		private int start;
 		private int end;
 		public List<OnGenome> geneList;
 
 		public BEDRangeQuery(List<OnGenome> geneList, String coordinate, int start, int end) {
 			this.geneList = geneList;
 			this.coordinate = coordinate;
 			this.start = end >= start ? start : end;
 			this.end = end >= start ? end : start;
 		}
 
 		public BEDTrack track;
 
 		public void addGene(BEDEntry gene) {
 			if (coordinate.equals(gene.coordinate) && (start <= gene.getEnd()) && (end >= gene.getStart())) {
				geneList.add(new BEDGene(gene));
 			}
 		}
 
 		public void addTrack(BEDTrack track) {
 
 		}
 
 		public void reportError(Exception e) {
 			_logger.error(e);
 		}
 	}
 
 }
