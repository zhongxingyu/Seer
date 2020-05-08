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
 // BEDViewer.java
 // Since: 2009/05/19
 //
 // $URL$ 
 // $Author$
 //--------------------------------------
 package org.utgenome.gwt.utgb.server.app;
 
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.Serializable;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.utgenome.format.bed.BED2SilkReader;
 import org.utgenome.format.bed.BEDGene;
 import org.utgenome.format.bed.BEDQuery;
 import org.utgenome.format.bed.BEDTrack;
 import org.utgenome.graphics.GeneCanvas;
 import org.utgenome.graphics.GenomeWindow;
 import org.utgenome.gwt.utgb.client.bio.ChrLoc;
 import org.utgenome.gwt.utgb.client.bio.Gene;
 import org.utgenome.gwt.utgb.client.bio.OnGenome;
 import org.utgenome.gwt.utgb.server.WebTrackBase;
 import org.xerial.db.sql.ResultSetHandler;
 import org.xerial.db.sql.sqlite.SQLiteAccess;
 import org.xerial.lens.Lens;
 import org.xerial.util.log.Logger;
 
 /**
  * BED viewer
  * 
  * @author leo
  * 
  */
 public class BEDViewer extends WebTrackBase implements Serializable {
 
 	private static final long serialVersionUID = 1L;
 
 	private static Logger _logger = Logger.getLogger(BEDViewer.class);
 
 	public String species = "human";
 	public String revision = "hg18";
 	public String name = "chr22";
 	public int start = 1;
 	public int end = 1000000;
 	public int width = 700;
 	public String fileName;
 
 	@Override
 	public void handle(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 
 		List<OnGenome> geneList = query(fileName, new ChrLoc(name, start, end));
 
 		String suffix = getActionSuffix(request);
 
 		if (suffix != null && suffix.equals("silk")) {
 			response.setContentType("text/plain");
 			response.getWriter().print(Lens.toSilk(geneList));
 		}
 		else {
 			GeneCanvas geneCanvas = new GeneCanvas(width, 300, new GenomeWindow(start, end));
 			geneCanvas.draw(geneList);
 
 			response.setContentType("image/png");
 			geneCanvas.toPNG(response.getOutputStream());
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	public static List<OnGenome> query(String bedPath, final ChrLoc location) {
 
 		final ArrayList<OnGenome> geneList = new ArrayList<OnGenome>();
 		int sqlStart = location.end >= location.start ? location.start : location.end;
 		int sqlEnd = location.end >= location.start ? location.end : location.start;
 
 		try {
 			File input = new File(getProjectRootPath(), bedPath);
 			File dbInput = new File(input.getAbsolutePath() + ".sqlite");
 			if (dbInput.exists()) {
 				// use db
 				SQLiteAccess dbAccess = new SQLiteAccess(dbInput.getAbsolutePath());
 
 				// correct 0-based BED data into 1-origin 
				String sql = createSQLStatement("select start, end, name, score, strand, cds, exon, color from gene "
 						+ "where coordinate = '$1' and ((start between $2 and $3) or (start <= $2 and end >= $3))", location.chr, sqlEnd, sqlStart);
 
 				if (_logger.isDebugEnabled())
 					_logger.debug(sql);
 
 				dbAccess.query(sql, new ResultSetHandler() {
 					@Override
 					public Object handle(ResultSet rs) throws SQLException {
 						geneList.add(new Gene(BEDGene.createFromResultSet(location.chr, rs)));
 						return null;
 					}
 				});
 			}
 			else {
 				// use raw text
 				BED2SilkReader in = null;
 				try {
 					in = new BED2SilkReader(new FileReader(input));
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
 			_logger.error(e);
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
 
 		public void addGene(BEDGene gene) {
 			// correct 0-based BED data into 1-origin 
 
			if (coordinate.equals(gene.coordinate) && (start <= gene.getEnd()) && (end >= gene.getStart())) {
 				geneList.add(new Gene(gene));
 			}
 		}
 
 		public void addTrack(BEDTrack track) {
 
 		}
 
 		public void reportError(Exception e) {
 			_logger.error(e);
 		}
 	}
 
 }
