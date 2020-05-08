 package uk.co.brotherlogic.mdb.vgadd;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.sql.SQLException;
 import java.util.Calendar;
 
 import uk.co.brotherlogic.mdb.Connect;
 import uk.co.brotherlogic.mdb.categories.GetCategories;
 import uk.co.brotherlogic.mdb.format.GetFormats;
 import uk.co.brotherlogic.mdb.label.GetLabels;
 import uk.co.brotherlogic.mdb.record.GetRecords;
 import uk.co.brotherlogic.mdb.record.Record;
 import uk.co.brotherlogic.mdb.record.Track;
 
 public class VGAdd
 {
    public void run(File f) throws IOException, SQLException
    {
       // For testing
     //	 Connect.setForDevMode();
 
       // Read the generic information
       BufferedReader reader = new BufferedReader(new FileReader(f));
       String header = reader.readLine();
       String[] elems = header.trim().split("~");
       String title = elems[0];
       int year = Integer.parseInt(elems[1]);
 
       for (String line = reader.readLine(); line != null; line = reader.readLine())
       {
    	  if (line.trim().length() > 0)
    	  {
          String[] lineElems = line.trim().split("~");
 
          String person = lineElems[0];
          int tracks = Integer.parseInt(lineElems[1]);
 
          if (GetRecords.create().getRecords(title + ": " + person).size() == 0)
          {
 
             // Build up the record
             Record r = new Record();
             r.setTitle(title + ": " + person);
             r.setLabel(GetLabels.create().getLabel("VinylVulture"));
             r.setReleaseType(2);
             r.setFormat(GetFormats.create().getFormat("CD"));
             r.setCatNo("VG+ XMAS " + year);
 
             Calendar cal = Calendar.getInstance();
             cal.set(Calendar.DAY_OF_MONTH, 25);
             cal.set(Calendar.MONTH, Calendar.DECEMBER);
             cal.set(Calendar.YEAR, year);
             r.setDate(cal.getTime());
 
             r.setYear(year);
             r.setReleaseMonth(12);
             r.setCategory(GetCategories.build().getCategory("VinylVulture"));
 
             for (int i = 1; i <= tracks; i++)
             {
                System.err.println(i + " => " + tracks);
                Track t = new Track();
                t.setFormTrackNumber(i);
                t.setTrackNumber(i);
                t.setTitle("");
 
                r.addTrack(t);
             }
 
             r.setOwner(1);
             r.setAuthor("Various");
 
             System.out.println("Adding: " + person);
 
             r.save();
          }
          else
             System.out.println("Skipping: " + person);
 
          Connect.getConnection().commitTrans();
          System.out.println(GetRecords.create().getRecords(title + ": " + person).get(0)
                .getFormTrackArtist(1));
          System.out.println(GetRecords.create().getRecords(title + ": " + person).get(0)
                .getFormTrackTitle(1));
       }
    	  }
 
       // Commit all the additions
 
    }
 }
