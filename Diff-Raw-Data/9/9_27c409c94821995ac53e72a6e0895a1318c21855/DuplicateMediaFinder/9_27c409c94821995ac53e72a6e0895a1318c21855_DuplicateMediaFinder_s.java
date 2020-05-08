 package fr.thumbnailsdb;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Comparator;
 import java.util.Iterator;
 import java.util.TreeSet;
 
 public class DuplicateMediaFinder {
 
 	protected ThumbStore thumbstore;
 
 	public DuplicateMediaFinder(ThumbStore c) {
 		this.thumbstore = c;
 	}
 
 	public ResultSet findDuplicateMedia() {
 		return thumbstore.getDuplicatesMD5();
 	}
 
 	private class DuplicateGroup {
 		long fileSize;
 		ArrayList<String> al = new ArrayList<String>();
 
 		public DuplicateGroup() {
 			super();
 			// this.individualSize = individualSize;
 		}
 
 		public void add(long size, String path) {
 			this.fileSize = size;
 			al.add(path);
 		}
 
 		public long getFileSize() {
 			return fileSize;
 		}
 
 		public int size() {
 			return al.size();
 		}
 
 		public String toString() {
 			StringBuilder sb = new StringBuilder();
 			for (Iterator iterator = al.iterator(); iterator.hasNext();) {
 				sb.append(iterator.next() + "\n");
 			}
 
 			return sb.toString();
 		}
 	}
 
 	public void prettyPrintDuplicate(ResultSet r) {
 		TreeSet<DuplicateGroup> list = computeDuplicateSets(r);
 
 		for (DuplicateGroup dg : list) {
 			System.out.println(dg.fileSize + " (" + dg.fileSize * (dg.size() - 1) + " to save) ");
 			System.out.println(dg);
 		}
 	}
 
 	protected TreeSet<DuplicateGroup> computeDuplicateSets(ResultSet r) {
 		TreeSet<DuplicateGroup> list = new TreeSet<DuplicateGroup>(new Comparator<DuplicateGroup>() {
 			// @Override
 			public int compare(DuplicateGroup o1, DuplicateGroup o2) {
 				return Double.compare(o2.getFileSize(), o1.getFileSize());
 			}
 		});
 		System.out.println("DuplicateMediaFinder.prettyPrintDuplicate() ");
 		// ArrayList<DuplicateGroup> al = new
 		// ArrayList<DuplicateMediaFinder.DuplicateGroup>();
 		DuplicateGroup dl = new DuplicateGroup();
 		String currentMd5 = "";
 		try {
 			while (r.next()) {
 				String md5 = r.getString("md5");
 				if (md5 != null) {
 					if (md5.equals(currentMd5)) {
 						// add to current group
 						dl.add(r.getLong("size"), r.getString("path"));
 					} else {
 						if (dl.size() > 1) {
 							list.add(dl);
 						}
 						dl = new DuplicateGroup();
 						dl.add(r.getLong("size"), r.getString("path"));
 						currentMd5 = md5;
 
 					}
 				}
 			}
 			if (dl.size() > 1) {
 				list.add(dl);
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return list;
 	}
 
 	// TODO : save result for subsequent requests
 	public String prettyHTMLDuplicate(ResultSet r, int max) {
 		TreeSet<DuplicateGroup> list = computeDuplicateSets(r);
 		System.out.println("DuplicateMediaFinder.prettyHTMLDuplicate() " + max);
 		String result = "";
 		int i = 0;
 		for (DuplicateGroup dg : list) {
 
 			result += dg.fileSize + " (" + dg.fileSize * (dg.size() - 1) + " to save)\n";
 			result += dg + "\n";
 			i++;
 			if (i > max) {
 				break;
 			}
 		}
		System.out.println("DuplicateMediaFinder.prettyHTMLDuplicate() " + result);
 		return result;
 	}
 
 }
