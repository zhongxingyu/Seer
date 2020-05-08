 package uk.co.brotherlogic.mdb;
 
 import java.sql.SQLException;
 import java.util.Date;
 import java.util.List;
 
 import junit.framework.TestCase;
 import uk.co.brotherlogic.mdb.categories.Category;
 import uk.co.brotherlogic.mdb.format.Format;
 import uk.co.brotherlogic.mdb.label.Label;
 import uk.co.brotherlogic.mdb.record.GetRecords;
 import uk.co.brotherlogic.mdb.record.Record;
 
 public class RecordTest extends TestCase {
 	private static boolean built = false;
 
 	private void buildRecord() {
 		if (!built) {
 			// Create
 			Record r = new Record();
 			r.setAuthor("fake-author");
 			r.setCategory(new Category("fake-cat", 12));
 			r.setCatNo("fake-cat-no");
 			r.setDate(new Date());
 			r.setDiscogsNum(12);
 			r.setFormat(new Format("fake-format", "12"));
 			r.setLabel(new Label("fake-label"));
 			r.setNotes("fake-notes");
 			r.setOwner(1);
 			r.setPrice(12.65);
 			r.setReleaseMonth(12);
 			r.setReleaseType(1);
 			r.setTitle("fake-title");
 			r.setYear(2000);
 
 			try {
 				// Persist
 				r.save();
 				built = true;

				// Set some scores
				r.addScore(User.getUser("Simon"), 5);
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	public void testCatNoSize() {
 		try {
 			buildRecord();
 			Record nrec = GetRecords.create().getRecords("fake-title").get(0);
 			assert (nrec.getCatNos().size() == 1);
 			assert (nrec.getCatNoString().equals("fake-cat-no"));
 		} catch (SQLException e) {
 			e.printStackTrace();
 			assert (false);
 		}
 	}
 
 	public void testLabels() {
 		try {
 			buildRecord();
 			Record nrec = GetRecords.create().getRecords("fake-title").get(0);
 			assert (nrec.getLabels().size() == 1);
 			assert (nrec.getLabels().iterator().next().getName()
 					.equals("fake-label"));
 		} catch (SQLException e) {
 			e.printStackTrace();
 			assert (false);
 		}
 	}
 
 	public void testNewAuthor() {
 		try {
 			buildRecord();
 			Record r = GetRecords.create().getRecords("fake-title").get(0);
 			r.setAuthor("New Fake Author");
 			r.save();
 			Record nrec = GetRecords.create().getRecords("fake-title").get(0);
 			assert (nrec.getAuthor().equals("New Fake Author"));
 
 		} catch (SQLException e) {
 			e.printStackTrace();
 			assert (false);
 		}
 	}
 
 	public void testRetrieveSize() {
 		try {
 			buildRecord();
 			// Retrieve
 			List<Record> recs = GetRecords.create().getRecords("fake-title");
 			assert (recs.size() == 1);
 		} catch (SQLException e) {
 			e.printStackTrace();
 			assert (false);
 		}
 	}
 
 	public void testRiploc() {
 		try {
 			buildRecord();
 			// Retrieve
 			Record rec = GetRecords.create().getRecords("fake-title").get(0);
 			rec.setRiploc("testing");
 			rec.save();
 
 			Record rec2 = GetRecords.create().getRecords("fake-title").get(0);
 			assert (rec2.getRiploc().equals("testing"));
 		} catch (SQLException e) {
 			e.printStackTrace();
 			assert (false);
 		}
 	}
 
 	public void testScore() {
 		try {
 			buildRecord();
 			Record nrec = GetRecords.create().getRecords("fake-title").get(0);
 			double score = nrec.getScore();
 			double sscore = nrec.getScore(User.getUser("Simon"));
			assert (score >= 0);
			assert (sscore >= 0);
 		} catch (SQLException e) {
 			e.printStackTrace();
 			assert (false);
 		}
 	}
 
 }
