 /*--------------------------------------------------------------------------
  *  Copyright 2009 utgenome.org
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
 // utgb-core Project
 //
 // KeywordDBGenerator.java
 // Since: May 20, 2010
 //
 // $URL$ 
 // $Author$
 //--------------------------------------
 package org.utgenome.format.keyword;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.IOException;
 import java.io.Reader;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.utgenome.UTGBErrorCode;
 import org.utgenome.UTGBException;
 import org.utgenome.format.bed.BED2SilkReader;
 import org.utgenome.format.bed.BEDGene;
 import org.utgenome.format.bed.BEDQuery;
 import org.utgenome.format.bed.BEDTrack;
 import org.utgenome.format.fasta.CompactFASTAIndex;
 import org.utgenome.format.keyword.GenomeKeywordEntry.KeywordAlias;
 import org.utgenome.gwt.utgb.client.bio.KeywordSearchResult;
 import org.xerial.db.DBException;
 import org.xerial.db.sql.ResultSetHandler;
 import org.xerial.db.sql.SQLExpression;
 import org.xerial.db.sql.SQLUtil;
 import org.xerial.db.sql.sqlite.SQLiteAccess;
 import org.xerial.db.sql.sqlite.SQLiteCatalog;
 import org.xerial.util.StringUtil;
 import org.xerial.util.log.Logger;
 
 /**
  * Keyword database generator/query interface
  * 
  * @author leo
  * 
  */
 public class KeywordDB {
 
 	private static Logger _logger = Logger.getLogger(KeywordDB.class);
 
 	private SQLiteAccess db;
 
 	public KeywordDB(File dbPath) throws DBException {
 		this(new SQLiteAccess(dbPath.getAbsolutePath()));
 	}
 
 	public KeywordDB(String dbPath) throws DBException, UTGBException {
 		this(new SQLiteAccess(dbPath));
 	}
 
 	public KeywordDB(SQLiteAccess db) throws DBException {
 		this.db = db;
 	}
 
 	public void initDB() throws DBException {
 
 		SQLiteCatalog catalog = db.getCatalog();
 		if (!catalog.getTableNameSet().contains("entry")) {
 			db.update("create table if not exists entry(chr text, start integer, end integer, original_keyword text)");
 			db.update("create virtual table alias_table using fts3(keyword text, alias text)");
 			db.update("create virtual table keyword_index using fts3(ref text, keyword text)");
 		}
 	}
 
 	public KeywordSearchResult query(String ref, String keyword, int page, int pageSize) throws Exception {
 
 		if (pageSize > 100)
 			pageSize = 100;
 		if (pageSize <= 0)
 			pageSize = 10;
 
 		final KeywordSearchResult r = new KeywordSearchResult();
 		r.page = page;
 		String sKeyword = sanitize(keyword);
 		String keywordSegmentsWithStar = splitAndAddStar(keyword);
 		if (keywordSegmentsWithStar == null)
 			return r;
 
 		// search alias
 		String aliasSearchKeyword = null;
 		String aliasQuery = SQLExpression.fillTemplate("select distinct(keyword), alias from alias_table where alias match \"$1\"", keywordSegmentsWithStar);
 		List<KeywordAlias> aliases = db.query(aliasQuery, GenomeKeywordEntry.KeywordAlias.class);
 		ArrayList<String> keywords = new ArrayList<String>();
 		//keywords.add(sKeyword);
 		for (KeywordAlias each : aliases) {
 			keywords.add(sanitize(each.keyword));
 		}
 		aliasSearchKeyword = StringUtil.join(keywords, " OR ");
 
 		String refCondition = (ref == null) ? "" : SQLExpression.fillTemplate("ref=\"$1\" and ", ref);
 
 		String perfectMatchQuery = SQLExpression.fillTemplate(
 				"select rowid as id, 1 as priority, offsets(keyword_index) as offsets, * from keyword_index where $1 keyword match $2", refCondition, SQLUtil
 						.doubleQuote(sKeyword));
 
 		String aliasPerfectMatchQuery = SQLExpression.fillTemplate(
 				"select rowid as id, 2 as priority, offsets(keyword_index) as offsets, * from keyword_index where $1 keyword match $2", refCondition, SQLUtil
 						.doubleQuote(aliasSearchKeyword));
 
 		String forwardMatchQuery = SQLExpression.fillTemplate(
 				"select rowid as id, 3 as priority, offsets(keyword_index) as offsets, * from keyword_index where $1 keyword match $2", refCondition, SQLUtil
 						.doubleQuote(keywordSegmentsWithStar + " -" + sKeyword));
 
 		String unionSQL = SQLExpression.fillTemplate("select * from ($1 union all $2 union all $3)", perfectMatchQuery, aliasPerfectMatchQuery,
 				forwardMatchQuery);
 
 		// count the search results
 		String countSQL = SQLExpression.fillTemplate("select count(*) as count from (select distinct(id) from ($1))", unionSQL);
 
 		db.query(countSQL, new ResultSetHandler<Void>() {
 			@Override
 			public Void handle(ResultSet rs) throws SQLException {
 				r.count = rs.getInt(1);
 				return null;
 			}
 		});
 
 		r.maxPage = r.count / pageSize + (r.count % pageSize == 0 ? 0 : 1);
 
 		String searchSQLTemplate = "select distinct(t.id) as id, original_keyword as name, offsets, ref, chr, start, end "
 				+ "from ($1) t, entry where t.id = entry.rowid order by priority, chr, start limit $2 offset $3";
 		String keywordSearchSQL = SQLExpression.fillTemplate(searchSQLTemplate, unionSQL, pageSize, pageSize * (page - 1));
 
 		r.result = db.query(keywordSearchSQL, KeywordSearchResult.Entry.class);
 
 		return r;
 	}
 
 	public static String splitAndAddStar(String keyword) {
 		if (keyword != null) {
 			String sunitizedKeyword = sanitize(keyword);
 			String[] segment = sunitizedKeyword.split("\\s+");
 			if (segment == null)
 				return keyword;
 
 			ArrayList<String> keywordList = new ArrayList<String>();
 			for (String s : segment) {
 				keywordList.add(s + "*");
 			}
 			return StringUtil.join(keywordList, " AND ");
 		}
 		else
 			return null;
 	}
 
 	public static String sanitize(String text) {
 		if (text == null)
 			return null;
 
 		return text.replaceAll("[\\p{Punct}]", "");
 	}
 
 	public void add(GenomeKeywordEntry entry) throws DBException {
 
 		String sKeyword = sanitize(entry.text);
 
 		String newEntrySQL = SQLExpression.fillTemplate("insert into entry values('$1', $2, $3, '$4')", entry.chr, entry.start, entry.end, entry.text);
 		String newIndexSQL = SQLExpression.fillTemplate("insert into keyword_index values('$1', '$2')", entry.ref, sKeyword);
 
 		db.update(newEntrySQL);
 		db.update(newIndexSQL);
 	}
 
 	public void add(KeywordAlias alias) throws DBException {
 		String sql = SQLExpression.fillTemplate("insert into alias_table values('$1', '$2')", alias.keyword, sanitize(alias.alias));
 		db.update(sql);
 	}
 
 	public void importKeywordAliasFile(Reader keywordAliasFile) throws UTGBException {
 		try {
 			initDB();
 
 			KeywordAliasReader r = new KeywordAliasReader(keywordAliasFile);
 			KeywordAlias alias;
 			db.update("pragma synchronous = off");
 			db.setAutoCommit(false);
 			while ((alias = r.next()) != null) {
 				add(alias);
 			}
 			db.update("commit");
 		}
 		catch (Exception e) {
 			throw UTGBException.convert(e);
 		}
 	}
 
 	public void importFromFASTAIndex(String ref, Reader fastaIndexFile) throws UTGBException {
 		try {
 			initDB();
 
 			db.update("pragma synchronous = off");
 			db.setAutoCommit(false);
 
 			List<CompactFASTAIndex> index = CompactFASTAIndex.load(fastaIndexFile);
 			for (CompactFASTAIndex each : index) {
 				GenomeKeywordEntry e = new GenomeKeywordEntry(ref, each.name, each.name, 1, 1);
 				add(e);
 			}
 			db.update("commit");
 		}
 		catch (Exception e) {
 			throw UTGBException.convert(e);
 		}
 	}
 
 	public void importFromTAB(String ref, Reader tabFileReader) throws UTGBException {
 
 		int entryCount = 0;
 		int lineCount = 1;
 		try {
 			initDB();
 			db.update("pragma synchronous = off");
 			db.setAutoCommit(false);
 
 			BufferedReader input = new BufferedReader(tabFileReader);
 			for (String line; (line = input.readLine()) != null; lineCount++) {
 				// skip the comment line
 				if (line.startsWith("#"))
 					continue;
 
 				try {
 					String[] column = line.split("\t");
 					if (column.length < 4) {
 						throw new IllegalArgumentException();
 					}
 					// chr, start, end, text ....
 					String chr = column[0];
 					int start = Integer.parseInt(column[1]);
 					int end = Integer.parseInt(column[2]);
 					StringBuilder buf = new StringBuilder();
 					for (int i = 3; i < column.length; ++i) {
 						if (i != 3)
 							buf.append(" ");
 						buf.append(column[i]);
 					}
 					GenomeKeywordEntry entry = new GenomeKeywordEntry(ref, chr, buf.toString(), start, end);
 
 					if (entryCount > 0 && (entryCount % 10000 == 0))
 						_logger.info("num entries: " + entryCount);
 					entryCount++;
 
 					add(entry);
 				}
 				catch (IllegalArgumentException e) {
					_logger.warn(String.format("line %d has invalid format: %s", lineCount, line));
 				}
 				catch (DBException e) {
					_logger.error(String.format("line %d: insertion error %s", e.getMessage()));
 					e.printStackTrace(System.err);
 				}
 			}
 
 			db.update("commit");
 		}
 		catch (DBException e) {
 			_logger.error("error at line " + lineCount);
 			throw new UTGBException(UTGBErrorCode.DatabaseError, e);
 		}
 		catch (IOException e) {
 			throw new UTGBException(UTGBErrorCode.IO_ERROR, e);
 		}
 	}
 
 	public void importFromBED(final String ref, Reader bed) throws UTGBException {
 
 		try {
 			initDB();
 
 			db.update("pragma synchronous = off");
 			db.setAutoCommit(false);
 
 			BED2SilkReader.scan(bed, new BEDQuery() {
 
 				private int entryCount = 0;
 
 				public void addGene(BEDGene gene) {
 					entryCount++;
 					GenomeKeywordEntry e = new GenomeKeywordEntry(ref, gene.coordinate, gene.getName(), gene.getStart(), gene.getEnd());
 					try {
 						if (entryCount > 0 && (entryCount % 10000 == 0))
 							_logger.info("num entries: " + entryCount);
 
 						KeywordDB.this.add(e);
 					}
 					catch (DBException e1) {
 						reportError(e1);
 					}
 				}
 
 				public void addTrack(BEDTrack track) {
 					// simply ignore
 				}
 
 				public void reportError(Exception e) {
 					_logger.error(e);
 				}
 			});
 
 			db.update("commit");
 		}
 		catch (DBException e) {
 			throw new UTGBException(UTGBErrorCode.DatabaseError, e);
 		}
 
 	}
 
 	public void close() throws DBException {
 		db.dispose();
 	}
 
 }
