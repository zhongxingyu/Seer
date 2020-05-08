 /*
  	org.manalith.ircbot.plugin.distopkgfinder/DebianPkgFinderRunner.java
  	ManalithBot - An open source IRC bot based on the PircBot Framework.
  	Copyright (C) 2011, 2012  Seong-ho, Cho <darkcircle.0426@gmail.com>
  	Copyright (C) 2012  Changwoo Ryu <cwryu@debian.org>
 
     This program is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     This program is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package org.manalith.ircbot.plugin.distropkgfinder;
 
import org.jsoup.Connection;
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 import org.jsoup.select.Elements;
 
 public class DebianPkgFinderRunner {
 	private String keyword;
 
 	public DebianPkgFinderRunner() {
 		this.setKeyword("");
 	}
 
 	public DebianPkgFinderRunner(String newKeyword) {
 		this.setKeyword(newKeyword);
 	}
 
 	public void setKeyword(String newKeyword) {
 		this.keyword = newKeyword;
 	}
 
 	public String getKeyword() {
 		return this.keyword;
 	}
 
 	public String parseVersionInfo(Document doc) {
 		Elements exactHits = doc.select("#psearchres").select("ul")
 			.get(0).select("li");
 		String result = "";
 
 		for (Element e : exactHits) {
 			String dist = e.select("a").text().split("[\\(\\)]")[1];
 			String version = "UNKNOWN";
 			String[] versionLines =
 				e.toString().split("\\<br\\s\\/>");
 
 			for (String line : versionLines) {
 				String v = line.split(":")[0];
 				if (v.split("\\s").length > 1)
 					continue;
 				else {
 					version = v;
 					break;
 				}
 			}
 
 			result += version + " (" + dist + ") ";
 		}
 
 		return result;
 	}
 
 	public String run() {
 		String result = "";
 		String url = "http://packages.debian.org/search?keywords="
 				+ this.getKeyword() + "&searchon=names&suite=all&section=all";
 
 		boolean hasExacthits = false;
 
 		try {
			
			Connection conn = Jsoup.connect(url);
			conn.timeout(10000);
			Document doc = conn.get();
 
 			if (doc.select("#psearchres").size() == 0) {
 				result = "There is no result";
 				return result;
 			}
 
 			Elements hits = doc.select("#psearchres").select("h2");
 			int hsize = hits.size();
 
 			if (hsize == 0)
 				result = "There is no result";
 			for (int i = 0; i < hsize; i++) {
 				if (hits.get(i).text().equals("Exact hits")) {
 					hasExacthits = true;
 					break;
 				}
 
 			}
 			if (!hasExacthits) {
 				result = "There is no result";
 				return result;
 			}
 
 			String pkgname = doc.select("#psearchres")
 			    .select("h3").get(0).text().split("\\s")[1];
 
 			Elements exactHits = doc.select("#psearchres").select("ul").get(0)
 					.select("li");
 			int elemCnt = exactHits.size();
 			Element latestElement = exactHits.get(elemCnt - 1);
 			String description = latestElement.toString().split("\\<br\\s\\/>")[0]
 					.split("\\:")[1].trim();
 
 			result = pkgname + " - " + description + "\n";
 			result += parseVersionInfo(doc);
 		} catch (Exception e) {
 			result = e.getMessage();
 			return result;
 		}
 
 		return result;
 	}
 }
