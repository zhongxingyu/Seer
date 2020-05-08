 package uk.co.brotherlogic.mdb.record;
 
 /**
  * Class to oversee the addition of a single record
  * 
  * @author Simon Tucker
  */
 
 import java.awt.Window;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.sql.SQLException;
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Collection;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.Map;
 import java.util.NoSuchElementException;
 import java.util.StringTokenizer;
 import java.util.TreeSet;
 import java.util.Vector;
 
 import javax.swing.JOptionPane;
 
 import uk.co.brotherlogic.mdb.Builder;
 import uk.co.brotherlogic.mdb.CategoryBuilderGUI;
 import uk.co.brotherlogic.mdb.LineUpSelectorGUI;
 import uk.co.brotherlogic.mdb.MDBApp;
 import uk.co.brotherlogic.mdb.RecordSelector;
 import uk.co.brotherlogic.mdb.SetBuilder;
 import uk.co.brotherlogic.mdb.artist.Artist;
 import uk.co.brotherlogic.mdb.categories.Category;
 import uk.co.brotherlogic.mdb.format.Format;
 import uk.co.brotherlogic.mdb.format.GetFormats;
 import uk.co.brotherlogic.mdb.format.NewFormatGUI;
 import uk.co.brotherlogic.mdb.groop.Groop;
 import uk.co.brotherlogic.mdb.groop.LineUp;
 import uk.co.brotherlogic.mdb.label.GetLabels;
 import uk.co.brotherlogic.mdb.label.Label;
 
 public class AddRecordOverseer implements ActionListener
 {
    Collection<Artist> artists;
 
    // THe control centre to call back to
    MDBApp call = null;
 
    Collection<Category> categories;
 
    Collection<String> baseFormats;
 
    // Flag indicating if we are done now
    boolean complete = false;
 
    // The record that we are creating
    Record curr;
 
    Map<String, Groop> groops;
 
    // The gui to be used
    AddRecordGUI gui;
 
    // Collections to be used for making
    Collection<Label> labels;
 
    public AddRecordOverseer(MDBApp c, Collection<Artist> artists, Collection<Label> labels,
          Collection<Format> formats, Map<String, Groop> groops, Collection<Category> categories)
         throws SQLException
    {
       // Set the callback object
       call = c;
 
       // Set the parameters used to build the record
       this.labels = labels;
       this.artists = artists;
       this.groops = groops;
       this.categories = categories;
 
       // No parameters mean that we are creating, not editing
       curr = new Record();
 
       // Create the gui
       gui = new AddRecordGUI(labels, formats, this);
       gui.setModel(curr);
 
       // Set the dates
       Date today = new Date();
       DateFormat myForm = new SimpleDateFormat("dd/MM/yy");
       gui.setDate(myForm.format(today));
       DateFormat yForm = new SimpleDateFormat("yyyy");
       gui.setYear(yForm.format(today));
    }
 
    public AddRecordOverseer(MDBApp c, Collection<Artist> artists, Collection<Label> labels,
          Collection<Format> formats, Map<String, Groop> groops, Collection<Category> categories,
          Record rec) throws SQLException
    {
       call = c;
       curr = rec;
 
       // Prepare the form
       this.labels = labels;
       this.artists = artists;
       this.groops = groops;
       this.categories = categories;
 
       // We are editing a record
       gui = new AddRecordGUI(labels, formats, this);
       gui.setModel(rec);
 
       // Prepare the gui
       gui.setRecordTitle(rec.getTitle());
       gui.setYear("" + rec.getReleaseYear());
       gui.setNotes(rec.getNotes());
       gui.displayLabels(rec.getLabels());
 
       // Set the format of the record
       if (rec.getFormat() != null)
          if (formats.contains(rec.getFormat()))
             gui.selectFormat(rec.getFormat());
          else
          {
             gui.addFormat(rec.getFormat());
             gui.selectFormat(rec.getFormat());
          }
 
       // Set the selected categories
       gui.selectCategory(rec.getCategory());
 
       if (rec.getParent() != null && rec.getParent() > 0)
          gui.setParent(GetRecords.create().getRecord(rec.getParent()));
 
       gui.displayCats(rec.getCatNos());
       DateFormat myForm = new SimpleDateFormat("dd/MM/yy");
       gui.setDate(myForm.format(rec.getDate().getTime()));
       gui.setNoTracks(rec.getTracks().size(), this);
       for (int i = 0; i < rec.getTracks().size(); i++)
       {
          Track currTrack = rec.getTrack(i + 1);
          addGroopsToGUI(currTrack.getLineUps(), i + 1);
          gui.setTrackTitle(currTrack.getTitle(), i + 1);
          setTrackTime(currTrack.getLengthInSeconds(), i + 1);
          gui.setTrackFormNumber(currTrack.getFormTrackNumber(), i + 1);
       }
 
       // Update the author field
       gui.setAuthor(rec.getAuthor());
       gui.setRecOwner(rec.getOwner());
 
       // Update the other stuff
       gui.setType(rec.getReleaseType());
       gui.setMonth(rec.getReleaseMonth());
 
       gui.setPrice(rec.getPrice());
 
       updateCompiler();
    }
 
    public void actionPerformed(ActionEvent e)
    {
 
       if (e.getActionCommand().equals("label"))
          try
          {
             doLabels();
          }
          catch (SQLException e2)
          {
             e2.printStackTrace();
          }
       else if (e.getActionCommand().equals("done"))
          try
          {
             // For now just parse the gui
             if (collectDataFromGUI())
             {
                gui.setVisible(false);
 
                // Destroy the gui!
                gui.dispose();
 
                // Add the record!
                complete = true;
 
                if (call != null)
                   call.addDone(curr);
             }
          }
          catch (SQLException e2)
          {
             e2.printStackTrace();
          }
       else if (e.getActionCommand().equals("cancel"))
       {
          // Set the current record to null and make the frame invisible
          gui.setVisible(false);
 
          // Destroy the gui!
          gui.dispose();
 
          call.cancel();
       }
       else if (e.getActionCommand().equals("newformat"))
          newFormat();
       else if (e.getActionCommand().equals("addcategory"))
          try
          {
             newCategory();
          }
          catch (SQLException ex)
          {
             ex.printStackTrace();
          }
       else if (e.getActionCommand().equals("format"))
       {
          if (gui != null)
             curr.setFormat(gui.getFormat());
       }
       else if (e.getActionCommand().equals("parent"))
          try
          {
             setParent();
          }
          catch (SQLException e2)
          {
             e2.printStackTrace();
          }
       else if (e.getActionCommand().equals("cat"))
          try
          {
             doCat();
          }
          catch (SQLException e2)
          {
             e2.printStackTrace();
          }
       else if (e.getActionCommand().equals("tracks"))
          try
          {
             newTracks();
          }
          catch (SQLException e2)
          {
             JOptionPane.showMessageDialog(null, e2.getLocalizedMessage());
          }
       else if (e.getActionCommand().equals("addtracks"))
          addTracks();
       else if (e.getActionCommand().equals("pers"))
          try
          {
             addPersonnel();
          }
          catch (SQLException e2)
          {
             JOptionPane.showMessageDialog(null, e2.getLocalizedMessage());
          }
       else if (e.getActionCommand().equals("groop"))
          try
          {
             addGroop();
          }
          catch (SQLException e2)
          {
             JOptionPane.showMessageDialog(null, e2.getLocalizedMessage());
          }
       else if (e.getActionCommand().startsWith("formtrack"))
       {
          int number = Integer.parseInt(e.getActionCommand().substring("formtrack".length()));
          gui.incrementLabFrom(number);
       }
       else if (e.getActionCommand().startsWith("tpers"))
       {
          // Get the track number
          int trackNumber = Integer.parseInt(e.getActionCommand().substring(5));
          try
          {
             addPersonnel(trackNumber);
          }
          catch (SQLException e2)
          {
             JOptionPane.showMessageDialog(null, e2.getLocalizedMessage());
          }
       }
       else if (e.getActionCommand().startsWith("tgroop"))
          try
          {
             // Get the track number
             int trackNumber = Integer.parseInt(e.getActionCommand().substring(6));
             addGroop(trackNumber);
             updateAuthor();
          }
          catch (SQLException e2)
          {
             JOptionPane.showMessageDialog(null, e2.getLocalizedMessage());
          }
       else if (e.getActionCommand().startsWith("comp"))
          try
          {
             // Deal with the compiler
             // Bring up the personnel selection screen
             SetBuilder<Artist> persBuild = new SetBuilder<Artist>("Select Compilers", gui,
                   new Builder<Artist>()
                   {
                      @Override
                      public Artist build(String name)
                      {
                         return new Artist(name);
                      }
 
                   });
             persBuild.setData(artists, curr.getCompilers());
             persBuild.setVisible(true);
 
             // Get the results
             Collection<Artist> tempComps = persBuild.getData();
             curr.setCompilers(tempComps);
             updateCompiler();
          }
          catch (SQLException ex)
          {
             ex.printStackTrace();
          }
    }
 
    public void addGroop() throws SQLException
    {
       // Bring up the group selection screen
       SetBuilder<Groop> grpBuild = new SetBuilder<Groop>("Select Groop", gui, new Builder<Groop>()
       {
          @Override
          public Groop build(String name)
          {
             return new Groop(name);
          }
       });
       grpBuild.setData(groops.values(), new Vector<Groop>());
       grpBuild.setVisible(true);
 
       // Get the results
       Collection<Groop> tempGrps = grpBuild.getData();
       Collection<LineUp> groupsToAdd = new Vector<LineUp>();
 
       // Check that some groops were selected
       if (tempGrps != null)
       {
          // Work through the selected groups
          Iterator<Groop> gIt = tempGrps.iterator();
          boolean cancel = false;
          while (gIt.hasNext() && !cancel)
          {
             // Get the current groop
             Groop currGroop = gIt.next();
 
             // Select thr groop line ups
             LineUpSelectorGUI lineup = new LineUpSelectorGUI(currGroop, gui);
             lineup.setData(artists);
             lineup.setVisible(true);
 
             // Get the group and add it to the group to be added
             LineUp toAdd = lineup.getData();
             if (toAdd != null)
                groupsToAdd.add(toAdd);
             else
                cancel = true;
          }
 
          if (!cancel)
          {
             // Now get the range to add to
             // Ask which tracks this should be added to
             String tracksToAdd = JOptionPane.showInputDialog(gui, "Enter Tracks", "Enter Tracks",
                   JOptionPane.QUESTION_MESSAGE);
             Collection<Integer> numbers = getRange(tracksToAdd, curr.getTracks().size());
 
             // Iterate each number and add the group
             Iterator<Integer> nIt = numbers.iterator();
             while (nIt.hasNext())
             {
                int trackNum = (nIt.next()).intValue();
                addGroopsToGUI(groupsToAdd, trackNum);
                curr.setGroops(trackNum, groupsToAdd);
             }
          }
       }
 
       // Now set the author
       updateAuthor();
 
       // Clear the group builder
       grpBuild.clean();
    }
 
    public void addGroop(int trackNumber) throws SQLException
    {
       Collection<Groop> chGrps = new LinkedList<Groop>();
       for (LineUp lineup : curr.getTrack(trackNumber).getLineUps())
          if (groops.containsKey(lineup.getGroop().getSortName()))
             chGrps.add(groops.get(lineup.getGroop().getSortName()));
 
       SetBuilder<Groop> grpBuild = new SetBuilder<Groop>("Select Groops", gui, new Builder<Groop>()
       {
          @Override
          public Groop build(String name)
          {
             return new Groop(name);
          }
       });
       grpBuild.setData(groops.values(), chGrps, Math.max(1, trackNumber - 1), curr.getTracks()
             .size());
       grpBuild.setVisible(true);
 
       // Get the results
       Collection<Groop> tempGrps = grpBuild.getData();
       Collection<LineUp> groupsToAdd = new LinkedList<LineUp>();
 
       if (grpBuild.getTrackNumber() > 0)
       {
          addGroopsToGUI(curr.getTrack(grpBuild.getTrackNumber()).getLineUps(), trackNumber);
          curr.setGroops(trackNumber, curr.getTrack(grpBuild.getTrackNumber()).getLineUps());
       }
       else if (tempGrps != null)
       {
          // Work through the selected groups
          Iterator<Groop> tIt = tempGrps.iterator();
          boolean cancel = false;
          while (tIt.hasNext() && !cancel)
          {
             // Get the current groop
             Groop currGroop = tIt.next();
 
             // Select thr groop line ups
             LineUpSelectorGUI lineup = new LineUpSelectorGUI(currGroop, gui);
             lineup.setData(artists);
             lineup.setVisible(true);
 
             // Get the group and add it to the group to be added
             LineUp toAdd = lineup.getData();
             if (toAdd != null)
                groupsToAdd.add(toAdd);
             else
                cancel = true;
 
             lineup.clean();
          }
          if (!cancel)
          {
             // Add the groups to the GUI and the record
             addGroopsToGUI(groupsToAdd, trackNumber);
             curr.setGroops(trackNumber, groupsToAdd);
          }
 
       }
 
       grpBuild.clean();
    }
 
    public void addGroopsToGUI(Collection<LineUp> in, int trackNumber)
    {
       String rep = "";
       if (in.size() > 1)
          // Multiple Groops
          rep = "MULTIPLE";
       else
       {
          Iterator<LineUp> cIt = in.iterator();
          while (cIt.hasNext())
          {
             LineUp tempGroop = cIt.next();
             rep = tempGroop.getGroop().getSortName();
          }
       }
 
       // Set the track in the gui
       gui.setGroop(rep, trackNumber);
    }
 
    public void addPersonnel() throws SQLException
    {
       // Bring up the personnel selection screen
       SetBuilder<Artist> persBuild = new SetBuilder<Artist>("Select Personnel", gui,
             new Builder<Artist>()
             {
 
                @Override
                public Artist build(String name)
                {
                   return new Artist(name);
                }
 
             });
       persBuild.setData(artists, new Vector<Artist>());
       persBuild.setVisible(true);
 
       // Get the results
       Collection<Artist> tempPers = persBuild.getData();
 
       // Check that something was returned - i.e. that cancel wasn't pressed
       if (tempPers != null)
       {
          // Decide whether to add or replace
          Object[] options =
          { "Add", "Replace" };
          int choice = JOptionPane.showOptionDialog(gui, "Should this set be added or replaced?",
                "Add or Replace?", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null,
                options, options[1]);
 
          // Set the replace flag accordingly
          boolean replace = true;
          if (choice == 0)
             replace = false;
 
          // Ask which tracks this should be added to
          String tracksToAdd = JOptionPane.showInputDialog(gui, "Enter Tracks", "Enter Tracks",
                JOptionPane.QUESTION_MESSAGE);
 
          // Get the track numbers
          Collection<Integer> trackNumbers = getRange(tracksToAdd, curr.getTracks().size());
 
          Iterator<Integer> tIt = trackNumbers.iterator();
          while (tIt.hasNext())
          {
             // Deal with each track number accordingly
             int trackNumber = (tIt.next()).intValue();
             if (replace)
                curr.setPersonnel(trackNumber, tempPers);
             else
                curr.addPersonnel(trackNumber, tempPers);
          }
       }
 
       persBuild.clean();
    }
 
    public void addPersonnel(int trackNumber) throws SQLException
    {
       // Bring up the personnel selection screen
       SetBuilder<Artist> persBuild = new SetBuilder<Artist>("Select Personnel", gui,
             new Builder<Artist>()
             {
                @Override
                public Artist build(String name)
                {
                   return new Artist(name);
                }
 
             });
       persBuild.setData(artists, curr.getTrack(trackNumber).getPersonnel(),
             Math.max(1, trackNumber - 1), curr.getTracks().size());
       persBuild.setVisible(true);
 
       // Get the results
       Collection<Artist> tempPers = persBuild.getData();
 
       // Add the data if cancel wasn't pressed
       if (persBuild.getTrackNumber() > 0)
          curr.setPersonnel(trackNumber, curr.getTrack(persBuild.getTrackNumber()).getPersonnel());
       else if (tempPers != null)
          curr.setPersonnel(trackNumber, tempPers);
 
       persBuild.clean();
 
    }
 
    public void addTracks()
    {
       try
       {
          // Ask for the number of tracks
          String toAdd = JOptionPane.showInputDialog(gui, "Enter Number of Tracks",
                "Number of Tracks", JOptionPane.QUESTION_MESSAGE);
          if (toAdd != null)
          {
             // Get the number of tracks to add
             int numberToAdd = Integer.parseInt(toAdd);
 
             // Bring up an entry box asking for the point of entry - less
             // one since we must add at the required point
             String point = JOptionPane.showInputDialog(gui, "Where Should The Tracks Be Added?",
                   "Addition Point", JOptionPane.QUESTION_MESSAGE);
             if (point != null)
             {
                // Get the point at which to add them
                int addPoint = Integer.parseInt(point);
 
                // Insert the tracks into the record
                curr.addTracks(addPoint, numberToAdd);
 
                // Now do the gui work - first add the required number of
                // tracks
                gui.addTracks(numberToAdd, addPoint, this);
             }
          }
       }
       catch (NumberFormatException ex)
       {
          JOptionPane.showMessageDialog(gui, "Enter a Proper Number!", "Warning",
                JOptionPane.WARNING_MESSAGE);
       }
    }
 
    public boolean collectDataFromGUI() throws SQLException
    {
       boolean done = true;
 
       // Read the title
       curr.setTitle(gui.getRecordTitle());
 
       // Set the author
       curr.setAuthor(gui.getAuthor());
       curr.setOwner(gui.getRecOwner());
 
       curr.setReleaseMonth(gui.getReleaseMonth());
       curr.setReleaseType(gui.getReleaseType());
 
       // Get the label if required
       if (curr.getLabels().size() < 2)
       {
          // Create a collection and add the current label to it
          Collection<Label> tempLabs = new Vector<Label>();
          String currLabelName = gui.getLabel();
 
          Label currLabel = GetLabels.create().getLabel(currLabelName);
          tempLabs.add(currLabel);
 
          // Set the labels
          curr.setLabels(tempLabs);
       }
 
       // Do the same for catalogue number
       if (curr.getCatNos().size() < 2)
       {
          // Create a collection and add the current label to it
          Collection<String> tempCats = new Vector<String>();
          tempCats.add(gui.getCatNo());
 
          curr.setCatNos(tempCats);
       }
 
       // Set the category
       curr.setCategory(gui.getCategory());
 
       curr.setPrice(gui.getPrice());
 
       // Get the date
       try
       {
          DateFormat df = new SimpleDateFormat("dd/MM/yy");
          curr.setDate(df.parse(gui.getDate()));
       }
       catch (ParseException e)
       {
          done = false;
          JOptionPane.showMessageDialog(gui, "Error in date!", "Error", JOptionPane.WARNING_MESSAGE);
       }
 
       // Get the year
       try
       {
          int yearVal = Integer.parseInt(gui.getYear());
          if ((yearVal > 3000 || yearVal < 1800) && yearVal != -1)
          {
             JOptionPane.showMessageDialog(gui, "Error in Year!", "Error",
                   JOptionPane.WARNING_MESSAGE);
             done = false;
          }
          curr.setYear(yearVal);
       }
       catch (NumberFormatException e)
       {
          JOptionPane.showMessageDialog(gui, "Error in Year!", "Error", JOptionPane.WARNING_MESSAGE);
          done = false;
       }
 
       // Get the notes
       curr.setNotes(gui.getNotes());
 
       // Set the track data too
       int currTrackNumber = -1;
       try
       {
          for (currTrackNumber = 1; currTrackNumber <= curr.getTracks().size(); currTrackNumber++)
          {
             curr.getTrack(currTrackNumber).setTitle(gui.getTrackTitle(currTrackNumber));
 
             // Set the format tracknumber
             curr.getTrack(currTrackNumber).setFormTrackNumber(
                   gui.getFormatTrackNumber(currTrackNumber));
 
             // Convert the time to seconds
             String timeString = gui.getTrackTime(currTrackNumber);
 
             // If no time specified store this as -1
             if (timeString.length() > 0)
             {
                // Split up around the ':'
                StringTokenizer tok = new StringTokenizer(timeString, ":");
                int time = 0;
                while (tok.hasMoreTokens())
                   time = time * 60 + Integer.parseInt(tok.nextToken());
                curr.getTrack(currTrackNumber).setLengthInSeconds(time);
             }
             else
                curr.getTrack(currTrackNumber).setLengthInSeconds(-1);
          }
       }
       catch (NumberFormatException e)
       {
          JOptionPane.showMessageDialog(gui, "Error in Track" + currTrackNumber, "Error",
                JOptionPane.WARNING_MESSAGE);
          done = false;
       }
 
       // Return the done flag
       return done;
    }
 
    public void doCat() throws SQLException
    {
       // Bring up a set selector using the catalogue list
       SetBuilder<String> catBuild = new SetBuilder<String>("Select Cat. Nos", gui,
             new Builder<String>()
             {
                @Override
                public String build(String name)
                {
                   return name;
                }
             });
       catBuild.setData(new Vector<String>(), curr.getCatNos());
       catBuild.setAddOnly(true);
       catBuild.setVisible(true);
 
       // Clear the cat numbers
       catBuild.dispose();
 
       Collection<String> newCats = catBuild.getData();
 
       // Check that some data was actually returned
       if (newCats != null)
       {
          curr.setCatNos(newCats);
 
          // Update the display
          gui.displayCats(newCats);
       }
    }
 
    public void doLabels() throws SQLException
    {
       // Bring up a set selector using the label list
       SetBuilder<Label> labBuild = new SetBuilder<Label>("Select Labels", gui, new Builder<Label>()
       {
          @Override
          public Label build(String name)
          {
             return new Label(name);
          }
 
       });
       labBuild.setData(labels, curr.getLabels());
       labBuild.setVisible(true);
 
       // Get the data on completion and add to the record
       // If cancel null will be returned
       // THis is a set of both Labels and String Wrappers
       Collection<Label> newLabels = labBuild.getData();
 
       // Clear the set builder
       labBuild.dispose();
 
       if (newLabels != null)
       {
          // Set the labels
          curr.setLabels(newLabels);
 
          // And update the display
          gui.displayLabels(newLabels);
       }
    }
 
    private void error(Exception e)
    {
       e.printStackTrace();
       JOptionPane.showMessageDialog(gui, "ERROR: " + e.getMessage());
    }
 
    public Collection<Integer> getRange(String ret, int max)
    {
       // Initialise the collection
       Vector<Integer> trckList = new Vector<Integer>();
 
       try
       {
          // If something was returned then parse the string
          if (ret != null)
             if (ret.trim().equalsIgnoreCase(""))
                for (int i = 1; i <= max; i++)
                   trckList.add(Integer.valueOf(i));
             else
             {
                StringTokenizer tok = new StringTokenizer(ret, ",");
 
                // Tokenize the string
                while (tok.hasMoreTokens())
                {
                   String currTok = tok.nextToken();
                   if (currTok.length() > 2)
                   {
                      // String is of the form XX - XX
                      StringTokenizer tok2 = new StringTokenizer(currTok, "-");
                      Integer start, end;
 
                      start = new Integer(tok2.nextToken());
                      end = new Integer(tok2.nextToken());
 
                      for (int i = start.intValue(); i <= end.intValue(); i++)
                         if (i <= max)
                            trckList.add(Integer.valueOf(i));
                   }
                   else // String is just one number
                   if (Integer.parseInt(currTok) <= max)
                      trckList.add(new Integer(currTok));
                }
             }
       }
       catch (NumberFormatException e)
       {
          // Wipe the returning collection
          JOptionPane.showMessageDialog(gui, "Invalid Track Range Entry!", "Error",
                JOptionPane.ERROR_MESSAGE);
 
          trckList.removeAllElements();
       }
       catch (NoSuchElementException e)
       {
          // Wipe the returning collection
          JOptionPane.showMessageDialog(gui, "Invalid Track Range Entry!", "Error",
                JOptionPane.ERROR_MESSAGE);
 
          trckList.removeAllElements();
       }
 
       return trckList;
    }
 
    public Record getRecord()
    {
       return curr;
    }
 
    public Record getRecordWhenDone()
    {
       return getRecord();
    }
 
    public boolean isComplete()
    {
       return complete;
    }
 
    public void newCategory() throws SQLException
    {
       // Put up the new category dialog
       CategoryBuilderGUI catgui = new CategoryBuilderGUI(gui, new TreeSet<Category>(),
             new TreeSet<Format>());
 
       // Get the new category
       Category cat = catgui.makeNewCategory();
 
       // Now add the relevant categories to the data box
       gui.setCategory(cat);
       curr.setCategory(cat);
 
    }
 
    public void newFormat()
    {
       // Ask for a new format
       NewFormatGUI formGUI = new NewFormatGUI(categories, baseFormats, gui);
       formGUI.setVisible(true);
       Format newFormat = formGUI.getFormat();
 
       // Add the new format and select it
       if (newFormat != null && !newFormat.getName().equals(""))
       {
          gui.addFormat(newFormat);
          gui.selectFormat(newFormat);
       }
    }
 
    public void newTracks() throws SQLException
    {
       if (curr.getTracks().size() > 0)
       {
          // Wipe the tracks
          gui.createTracks(this);
 
          // Update the record
          curr.setTracks(gui.getNoTracks());
 
          // Update the display
          for (int i = 1; i <= curr.getTracks().size(); i++)
          {
             Track currTrack = curr.getTrack(i);
 
             // Set the details
             gui.setTrackTitle(currTrack.getTitle(), currTrack.getTrackNumber());
             addGroopsToGUI(currTrack.getLineUps(), currTrack.getTrackNumber());
             setTrackTime(currTrack.getLengthInSeconds(), currTrack.getTrackNumber());
          }
 
          try
          {
             showGUI(call);
          }
          catch (SQLException e)
          {
             e.printStackTrace();
          }
       }
       else
       {
          // Tell the GUI to create the required number of tracks
          gui.createTracks(this);
 
          // Add the number of tracks to the record too
          curr.createTracks(gui.getNoTracks());
       }
 
       // Maximise the gui
       gui.maximise();
    }
 
    public void setParent() throws SQLException
    {
 
       // Get a listing of all records
       RecordSelector sel = new RecordSelector();
       Record parent = sel.selectRecord(gui);
       if (parent != null)
          curr.setParent(parent.getNumber());
 
       gui.setParent(parent);
 
    }
 
    public void setTrackTime(int trackLength, int trackNumber)
    {
       // Construct the length
       String tStr;
       if (trackLength < 0)
          tStr = "";
       else
       {
          int hours = trackLength / 3600;
          int left = trackLength % 3600;
          int mins = left / 60;
          int secs = left % 60;
          if (hours > 0)
             tStr = hours + ":" + mins + ":" + secs;
          else
             tStr = mins + ":" + secs;
       }
 
       gui.setLength(tStr, trackNumber);
    }
 
    public void showGUI(Window parent) throws SQLException
    {
       baseFormats = GetFormats.create().getBaseFormats();
 
       // Set the right size
       gui.setSize(700, 700);
 
       // Center the window
       gui.setLocationRelativeTo(parent);
 
       // DO THIS WHILST DEBUGGING
       gui.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
 
       gui.setVisible(true);
 
       // Ensure that the window is maximised if we're editing
       if (curr.getTitle() != null && curr.getTitle().length() > 0)
          gui.maximise();
    }
 
    public void updateAuthor()
    {
       // Get the new author and update the gui
       gui.setAuthor(curr.getGroopString());
    }
 
    public void updateCompiler()
    {
       try
       {
          if (curr.getCompilers().size() == 0)
             gui.setCompiler("");
          else if (curr.getCompilers().size() > 1)
             gui.setCompiler("Multiple Compilers");
          else
             gui.setCompiler(curr.getCompilers().iterator().next().getSortName());
       }
       catch (SQLException e)
       {
          error(e);
       }
    }
 }
