 package uk.co.brotherlogic.mdb.record;
 
 /**
  * GUI Class for adding records
  * @author Simon Tucker
  */
 
 import java.awt.Dimension;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import java.awt.event.ActionListener;
 import java.sql.SQLException;
 import java.text.NumberFormat;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Vector;
 
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextField;
 import javax.swing.SwingConstants;
 import javax.swing.border.TitledBorder;
 
 import uk.co.brotherlogic.mdb.FilledTextArea;
 import uk.co.brotherlogic.mdb.categories.Category;
 import uk.co.brotherlogic.mdb.categories.GetCategories;
 import uk.co.brotherlogic.mdb.format.Format;
 import uk.co.brotherlogic.mdb.label.Label;
 
 public class AddRecordGUI extends JFrame
 {
    // Create objects
    GridBagLayout gridBagLayout1 = new GridBagLayout();
    JLabel jLabel1 = new JLabel();
    JTextField textTitle = new JTextField();
    JLabel jLabel2 = new JLabel();
    JLabel jLabel3 = new JLabel();
    JLabel jLabel4 = new JLabel();
    JLabel jLabel5 = new JLabel();
    JLabel jLabel6 = new JLabel();
    JTextField textLabel = new JTextField();
    JTextField textCatNo = new JTextField();
    JTextField textDate = new JTextField();
    JTextField textNoTracks = new JTextField();
    JButton butMultiLabel = new JButton();
    JButton butNew = new JButton();
    JButton butMultiCat = new JButton();
    JLabel jLabel7 = new JLabel();
    JTextField textYear = new JTextField();
    JButton butGroup = new JButton();
    JScrollPane scrollTracks = new JScrollPane();
    JButton butPersonnel = new JButton();
    JButton butCancel = new JButton();
    JButton butDone = new JButton();
    JComboBox comboFormat = new JComboBox();
    JButton butTracks = new JButton();
 
    JLabel labelPrice = new JLabel("Price: ");
    JTextField textPrice = new JTextField("0.00");
 
    // Author and Mixer stuff
    JLabel labelAuth = new JLabel();
    JTextField textAuth = new JTextField();
    JLabel labelMix = new JLabel();
    JTextField textMix = new JTextField();
    JButton butMix = new JButton();
 
    // Release Type / Release Month stuff
    JComboBox comboType = new JComboBox();
    JComboBox comboMonth = new JComboBox();
 
    // Panel which holds all the track information
    JPanel trackPan = new JPanel();
 
    // Vector of Groups and Personnel buttons
    Vector<JTextField> trackTitles = new Vector<JTextField>();
    Vector<JTextField> trackLengths = new Vector<JTextField>();
    Vector<JButton> trackGroops = new Vector<JButton>();
    List<JTextField> trackFormNumbers = new LinkedList<JTextField>();
 
    // Stuff for ownership
    JLabel labelOwner = new JLabel();
    JComboBox comboOwner = new JComboBox();
 
    JButton butParent = new JButton("Parent");
 
    int noTracks;
    JTextField textNotes = new JTextField();
    JLabel jLabel8 = new JLabel();
    TitledBorder titledBorder1;
    JLabel jLabel9 = new JLabel();
    JComboBox comboCategory = new JComboBox();
    JButton butNewCat = new JButton();
 
    Record model;
 
    public AddRecordGUI(Collection<Label> labels, Collection<Format> formats, ActionListener list)
    {
       // Create the filled Text Area
       textLabel = new FilledTextArea<Label>(labels);
       comboFormat = new JComboBox(new Vector<Format>(formats));
 
       Vector<String> months = new Vector<String>();
       months.add("Unknown");
       months.add("January");
       months.add("February");
       months.add("March");
       months.add("April");
       months.add("May");
       months.add("June");
       months.add("July");
       months.add("August");
       months.add("Sepetember");
       months.add("October");
       months.add("November");
       months.add("December");
       comboMonth = new JComboBox(months);
 
       Vector<String> owner = new Vector<String>();
       owner.add("Simon");
       owner.add("Jeanette");
       comboOwner = new JComboBox(owner);
 
       Vector<String> types = new Vector<String>();
       types.add("Album");
       types.add("Single");
       comboType = new JComboBox(types);
 
       try
       {
          jbInit(list);
       }
       catch (Exception e)
       {
          e.printStackTrace();
       }
 
       // Only set the categories if there are categories to set!
       try
       {
          setCategories(GetCategories.build().getCategories());
       }
       catch (SQLException e)
       {
          e.printStackTrace();
       }
    }
 
    public void addFormat(Format formIn)
    {
       // Add the format to the combo box
       comboFormat.addItem(formIn);
 
       // And set the category selector button on!
       butNewCat.setEnabled(true);
    }
 
    public void addTracks(int noTracks, int addPoint, ActionListener list)
    {
       int currNoTracks = trackTitles.size();
 
       for (int i = currNoTracks; i < currNoTracks + noTracks; i++)
       {
          // Create the necessary object
          JLabel labTrack = new JLabel("" + (i + 1));
          JTextField formTrackNumber = new JTextField((i + 1) + "");
          formTrackNumber.setActionCommand("formtrack" + (i + 1));
          formTrackNumber.addActionListener(list);
          JButton butGroop = new JButton("");
          JTextField textTrack = new JTextField("");
          JButton butPers = new JButton("Personnel");
          JTextField textTime = new JTextField("");
          butGroop.setPreferredSize(new Dimension(79, 30));
          butGroop.setActionCommand("tgroop" + (i + 1));
          butGroop.addActionListener(list);
          butPers.setPreferredSize(new Dimension(92, 30));
          butPers.setActionCommand("tpers" + (i + 1));
          butPers.addActionListener(list);
          textTime.setPreferredSize(new Dimension(60, 30));
          textTrack.setPreferredSize(new Dimension(60, 30));
 
          trackTitles.add(i, textTrack);
          trackLengths.add(i, textTime);
          trackGroops.add(i, butGroop);
          trackFormNumbers.add(i, formTrackNumber);
 
          // Add them using the required constraints
          trackPan.add(labTrack, new GridBagConstraints(0, i, 1, 1, 0, 0, GridBagConstraints.CENTER,
                GridBagConstraints.NONE, new Insets(1, 1, 1, 1), 0, 0));
          trackPan.add(formTrackNumber, new GridBagConstraints(GridBagConstraints.RELATIVE, i, 1, 1,
                0, 0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(1, 1, 1, 1), 0,
                0));
          trackPan.add(butGroop, new GridBagConstraints(GridBagConstraints.RELATIVE, i, 1, 1, 0, 0,
                GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(1, 1, 1, 1), 0, 0));
          trackPan.add(textTrack, new GridBagConstraints(GridBagConstraints.RELATIVE, i, 1, 1, 1, 0,
                GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(1, 1, 1, 1), 0,
                0));
          trackPan.add(butPers, new GridBagConstraints(GridBagConstraints.RELATIVE, i, 1, 1, 0, 0,
                GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(1, 1, 1, 1), 0, 0));
          trackPan.add(textTime, new GridBagConstraints(GridBagConstraints.RELATIVE, i, 1, 1, 0, 0,
                GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(1, 1, 1, 1), 0, 0));
       }
 
       // scrollTracks.getViewport().removeAll();
       // scrollTracks.getViewport().add(trackPan);
 
       // Now move the titles around - start at the back!
       for (int i = currNoTracks + noTracks - 1; i > addPoint + noTracks - 1; i--)
       {
          // Move the titles and times
          (trackTitles.get(i)).setText((trackTitles.get(i - noTracks)).getText());
          (trackLengths.get(i)).setText((trackLengths.get(i - noTracks)).getText());
       }
 
       // And blank the new fields
       for (int i = addPoint; i < addPoint + noTracks; i++)
       {
          (trackTitles.get(i)).setText("");
          (trackLengths.get(i)).setText("");
       }
 
       trackPan.invalidate();
       this.invalidate();
    }
 
    public void createTracks(ActionListener list)
    {
       // Enable the relevant buttons
       butTracks.setEnabled(true);
       butGroup.setEnabled(true);
       butPersonnel.setEnabled(true);
       butDone.setEnabled(true);
 
       // Get the number of tracks
       noTracks = Integer.parseInt(textNoTracks.getText().trim());
 
       trackPan = new JPanel(new GridBagLayout());
       scrollTracks.getViewport().add(trackPan);
       addTracks(noTracks, 0, list);
       /*
        * // Create the panel to store the track stuff trackPan = new JPanel(new
        * GridBagLayout());
        * 
        * // Add the tracks scrollTracks.getViewport().add(trackPan); // ,new
        * 
        * for (int i = 0; i < noTracks; i++) { // Create the necessary objects
        * JLabel labTrack = new JLabel("" + (i + 1)); JButton butGroop = new
        * JButton(""); JTextField textTrack = new JTextField(""); JButton butPers
        * = new JButton("Personnel"); JTextField textTime = new JTextField("");
        * butGroop.setPreferredSize(new Dimension(79, 30));
        * butGroop.setActionCommand("tgroop" + (i + 1));
        * butGroop.addActionListener(list); butPers.setPreferredSize(new
        * Dimension(92, 30)); butPers.setActionCommand("tpers" + (i + 1));
        * butPers.addActionListener(list); textTime.setPreferredSize(new
        * Dimension(60, 30)); textTrack.setPreferredSize(new Dimension(60, 30));
        * 
        * trackTitles.add(textTrack); trackLengths.add(textTime);
        * trackGroops.add(butGroop);
        * 
        * // Add them using the required constraints trackPan.add(labTrack, new
        * GridBagConstraints(0, i, 1, 1, 0, 0, GridBagConstraints.CENTER,
        * GridBagConstraints.NONE, new Insets(1, 1, 1, 1), 0, 0));
        * trackPan.add(butGroop, new GridBagConstraints(
        * GridBagConstraints.RELATIVE, i, 2, 1, 0, 0, GridBagConstraints.CENTER,
        * GridBagConstraints.NONE, new Insets(1, 1, 1, 1), 0, 0));
        * trackPan.add(textTrack, new GridBagConstraints(
        * GridBagConstraints.RELATIVE, i, 1, 1, 1, 0, GridBagConstraints.CENTER,
        * GridBagConstraints.HORIZONTAL, new Insets(1, 1, 1, 1), 0, 0));
        * trackPan.add(butPers, new GridBagConstraints(
        * GridBagConstraints.RELATIVE, i, 1, 1, 0, 0, GridBagConstraints.CENTER,
        * GridBagConstraints.NONE, new Insets(1, 1, 1, 1), 0, 0));
        * trackPan.add(textTime, new GridBagConstraints(
        * GridBagConstraints.RELATIVE, i, 1, 1, 0, 0, GridBagConstraints.CENTER,
        * GridBagConstraints.NONE, new Insets(1, 1, 1, 1), 0, 0)); }
        * 
        * trackTitles.trimToSize(); trackLengths.trimToSize();
        */
    }
 
    public void displayCats(Collection<String> in)
    {
       if (in.size() > 1)
       {
          textCatNo.setText("Multiple Numbers");
          textCatNo.setEnabled(false);
       }
       else
       {
          for (String catno : in)
             textCatNo.setText(catno);
          textCatNo.setEnabled(true);
       }
    }
 
    public void displayLabels(Collection<Label> in)
    {
       if (in.size() > 1)
       {
          textLabel.setText("Multiple Labels");
          textLabel.setEnabled(false);
          butMultiCat.setEnabled(true);
       }
       else if (in.size() > 0)
       {
          Iterator<Label> lIt = in.iterator();
          textLabel.setText(lIt.next().toString());
          textLabel.setEnabled(true);
          // butMultiCat.setEnabled(false);
       }
       else
       {
          textLabel.setText("");
          textLabel.setEnabled(true);
          // butMultiCat.setEnabled(false);
       }
    }
 
    public String getAuthor()
    {
       return textAuth.getText();
    }
 
    public Category getCategory()
    {
       return (Category) comboCategory.getSelectedItem();
    }
 
    public String getCatNo()
    {
       return textCatNo.getText();
    }
 
    public String getDate()
    {
       return textDate.getText();
    }
 
    public Format getFormat()
    {
       return (Format) comboFormat.getSelectedItem();
    }
 
    public int getFormatTrackNumber(int track)
    {
       return Integer.parseInt(trackFormNumbers.get(track - 1).getText());
    }
 
    public String getLabel()
    {
       return textLabel.getText();
    }
 
    public String getNotes()
    {
       return textNotes.getText();
    }
 
    public int getNoTracks()
    {
       return Integer.parseInt(textNoTracks.getText());
    }
 
    public double getPrice()
    {
       return Double.parseDouble(textPrice.getText());
    }
 
    public String getRecordTitle()
    {
       return textTitle.getText();
    }
 
    public int getRecOwner()
    {
       return comboOwner.getSelectedIndex() + 1;
    }
 
    public int getReleaseMonth()
    {
       return comboMonth.getSelectedIndex();
    }
 
    public int getReleaseType()
    {
       return comboType.getSelectedIndex() + 1;
    }
 
    public String getTrackTime(int i)
    {
       return (trackLengths.get(i - 1)).getText();
    }
 
    public String getTrackTitle(int i)
    {
       // +1 since track i = array entry i-1
       return (trackTitles.get(i - 1)).getText();
    }
 
    public String getYear()
    {
       return textYear.getText();
    }
 
    public void incrementLabFrom(int val)
    {
       for (JTextField tForm : trackFormNumbers.subList(val, trackFormNumbers.size()))
       {
          int num = Integer.parseInt(tForm.getText());
          tForm.setText(Integer.toString(num - 1));
       }
    }
 
    private void jbInit(ActionListener list) throws Exception
    {
       titledBorder1 = new TitledBorder("");
       jLabel1.setHorizontalAlignment(SwingConstants.RIGHT);
       jLabel1.setHorizontalTextPosition(SwingConstants.RIGHT);
       jLabel1.setText("Title :");
       labelAuth.setHorizontalAlignment(SwingConstants.RIGHT);
       labelAuth.setHorizontalTextPosition(SwingConstants.RIGHT);
       labelAuth.setText("Author :");
       this.getContentPane().setLayout(gridBagLayout1);
       jLabel2.setHorizontalAlignment(SwingConstants.RIGHT);
       jLabel2.setHorizontalTextPosition(SwingConstants.RIGHT);
       jLabel2.setText("Label : ");
       labelMix.setHorizontalAlignment(SwingConstants.RIGHT);
       labelMix.setHorizontalTextPosition(SwingConstants.RIGHT);
       labelMix.setText("Compiler :");
       jLabel3.setHorizontalAlignment(SwingConstants.RIGHT);
       jLabel3.setHorizontalTextPosition(SwingConstants.RIGHT);
       jLabel3.setText("Format : ");
       jLabel4.setHorizontalAlignment(SwingConstants.RIGHT);
       jLabel4.setHorizontalTextPosition(SwingConstants.RIGHT);
       jLabel4.setText("Cat. No. :");
       jLabel5.setHorizontalAlignment(SwingConstants.RIGHT);
       jLabel5.setHorizontalTextPosition(SwingConstants.RIGHT);
       jLabel5.setText("Date : ");
       jLabel6.setMaximumSize(new Dimension(35, 17));
       jLabel6.setMinimumSize(new Dimension(35, 17));
       jLabel6.setPreferredSize(new Dimension(55, 17));
       jLabel6.setHorizontalAlignment(SwingConstants.RIGHT);
       jLabel6.setText("Tracks : ");
       textLabel.setMinimumSize(new Dimension(4, 30));
       textLabel.setPreferredSize(new Dimension(63, 30));
       textMix.setMinimumSize(new Dimension(4, 30));
       textMix.setPreferredSize(new Dimension(63, 30));
       textMix.setEditable(false);
       textCatNo.setMinimumSize(new Dimension(4, 30));
       textCatNo.setPreferredSize(new Dimension(63, 30));
       textDate.setMaximumSize(new Dimension(50, 30));
       textDate.setMinimumSize(new Dimension(50, 30));
       textDate.setPreferredSize(new Dimension(50, 30));
       textNoTracks.setMaximumSize(new Dimension(58, 30));
       textNoTracks.setMinimumSize(new Dimension(58, 30));
       textNoTracks.setPreferredSize(new Dimension(58, 30));
       textNoTracks.setActionCommand("tracks");
       textNoTracks.addActionListener(list);
       butMultiLabel.setMaximumSize(new Dimension(79, 30));
       butMultiLabel.setMinimumSize(new Dimension(79, 30));
       butMultiLabel.setPreferredSize(new Dimension(79, 30));
       butMultiLabel.setText("Multiple");
       butMultiLabel.setActionCommand("label");
       butMultiLabel.addActionListener(list);
       butMix.setMaximumSize(new Dimension(79, 30));
       butMix.setMinimumSize(new Dimension(79, 30));
       butMix.setPreferredSize(new Dimension(79, 30));
       butMix.setText("Compiler");
       butMix.setActionCommand("comp");
       butMix.addActionListener(list);
       butNew.setMaximumSize(new Dimension(76, 30));
       butNew.setMinimumSize(new Dimension(79, 30));
       butNew.setPreferredSize(new Dimension(79, 30));
       butNew.setText("New");
       butNew.setActionCommand("newformat");
       butNew.addActionListener(list);
       butMultiCat.setMaximumSize(new Dimension(79, 30));
       butMultiCat.setMinimumSize(new Dimension(79, 30));
       butMultiCat.setPreferredSize(new Dimension(79, 30));
       butMultiCat.setText("Multiple");
       butMultiCat.setEnabled(true);
       butMultiCat.setActionCommand("cat");
       butMultiCat.addActionListener(list);
       jLabel7.setMaximumSize(new Dimension(40, 17));
       jLabel7.setMinimumSize(new Dimension(40, 17));
       jLabel7.setPreferredSize(new Dimension(40, 17));
       jLabel7.setHorizontalAlignment(SwingConstants.RIGHT);
       jLabel7.setHorizontalTextPosition(SwingConstants.RIGHT);
       jLabel7.setText("Year : ");
       textYear.setMinimumSize(new Dimension(70, 30));
       textYear.setPreferredSize(new Dimension(63, 30));
       butGroup.setMinimumSize(new Dimension(79, 30));
       butGroup.setPreferredSize(new Dimension(79, 30));
       butGroup.setText("Group");
       butGroup.setEnabled(false);
       butGroup.setMaximumSize(new Dimension(79, 30));
       butGroup.setActionCommand("groop");
       butGroup.addActionListener(list);
       butPersonnel.setMaximumSize(new Dimension(79, 30));
       butPersonnel.setMinimumSize(new Dimension(79, 30));
       butPersonnel.setPreferredSize(new Dimension(79, 30));
       butPersonnel.setText("Pers.");
       butPersonnel.setEnabled(false);
       butPersonnel.setActionCommand("pers");
       butPersonnel.addActionListener(list);
       butCancel.setMaximumSize(new Dimension(79, 30));
       butCancel.setMinimumSize(new Dimension(79, 30));
       butCancel.setPreferredSize(new Dimension(79, 30));
       butCancel.setText("Cancel");
       butCancel.setActionCommand("cancel");
       butCancel.addActionListener(list);
       butDone.setMaximumSize(new Dimension(79, 30));
       butDone.setMinimumSize(new Dimension(79, 30));
       butDone.setPreferredSize(new Dimension(79, 30));
       butDone.setText("Done");
       butDone.setActionCommand("done");
       butDone.addActionListener(list);
       butDone.setEnabled(false);
       textTitle.setMinimumSize(new Dimension(4, 30));
       textTitle.setPreferredSize(new Dimension(4, 30));
       textAuth.setMinimumSize(new Dimension(4, 30));
       textAuth.setPreferredSize(new Dimension(4, 30));
       butTracks.setMaximumSize(new Dimension(79, 30));
       butTracks.setMinimumSize(new Dimension(79, 30));
       butTracks.setPreferredSize(new Dimension(79, 30));
       butTracks.setText("Tracks");
       butTracks.setEnabled(false);
       butTracks.setActionCommand("addtracks");
       butTracks.addActionListener(list);
 
       textPrice.setMinimumSize(new Dimension(4, 30));
       labelOwner.setText("Owner: ");
 
       textNotes.setBorder(textYear.getBorder());
       textNotes.setMinimumSize(new Dimension(4, 30));
       textNotes.setPreferredSize(new Dimension(4, 30));
       textNotes.setText("");
       jLabel8.setHorizontalAlignment(SwingConstants.RIGHT);
       jLabel8.setText("Notes: ");
       jLabel9.setText("Category: ");
       comboCategory.setMinimumSize(new Dimension(31, 30));
       comboCategory.setPreferredSize(new Dimension(31, 30));
       comboMonth.setMinimumSize(new Dimension(31, 30));
       comboMonth.setPreferredSize(new Dimension(31, 30));
       comboType.setMinimumSize(new Dimension(31, 30));
       comboType.setPreferredSize(new Dimension(31, 30));
       comboFormat.setMinimumSize(new Dimension(31, 30));
       comboFormat.setPreferredSize(new Dimension(31, 30));
       comboFormat.setActionCommand("format");
       comboFormat.addActionListener(list);
       butNewCat.setMaximumSize(new Dimension(79, 30));
       butNewCat.setMinimumSize(new Dimension(79, 30));
       butNewCat.setPreferredSize(new Dimension(79, 30));
       butNewCat.setText("New Cat");
       butNewCat.setActionCommand("addcategory");
       butNewCat.addActionListener(list);
       butNewCat.setEnabled(true);
 
       butParent.setActionCommand("parent");
       butParent.addActionListener(list);
 
       this.getContentPane().add(
             jLabel1,
             new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER,
                   GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
       this.getContentPane().add(
             textTitle,
             new GridBagConstraints(1, 0, 8, 1, 1.0, 0.0, GridBagConstraints.CENTER,
                   GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
 
       this.getContentPane().add(
             jLabel2,
             new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER,
                   GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
       this.getContentPane().add(
             textLabel,
             new GridBagConstraints(1, 1, 7, 1, 1.0, 0.0, GridBagConstraints.CENTER,
                   GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
       this.getContentPane().add(
             butMultiLabel,
             new GridBagConstraints(8, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER,
                   GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
 
       JLabel tLabel = new JLabel("Type: ");
       tLabel.setHorizontalAlignment(SwingConstants.RIGHT);
       this.getContentPane().add(
             tLabel,
             new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER,
                   GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
       this.getContentPane().add(
             comboType,
             new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER,
                   GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
       this.getContentPane().add(
             jLabel3,
             new GridBagConstraints(2, 2, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER,
                   GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
       this.getContentPane().add(
             comboFormat,
             new GridBagConstraints(3, 2, 5, 1, 1.0, 0.0, GridBagConstraints.CENTER,
                   GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
       this.getContentPane().add(
             butNew,
             new GridBagConstraints(8, 2, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER,
                   GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
 
       this.getContentPane().add(
             jLabel4,
             new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER,
                   GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
       this.getContentPane().add(
             textCatNo,
             new GridBagConstraints(1, 3, 7, 1, 1.0, 0.0, GridBagConstraints.CENTER,
                   GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
       this.getContentPane().add(
             butMultiCat,
             new GridBagConstraints(8, 3, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER,
                   GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
 
       this.getContentPane().add(
             jLabel5,
             new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER,
                   GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
       this.getContentPane().add(
             textDate,
             new GridBagConstraints(1, 4, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER,
                   GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
       this.getContentPane().add(
             jLabel7,
             new GridBagConstraints(2, 4, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER,
                   GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
       this.getContentPane().add(
             textYear,
             new GridBagConstraints(3, 4, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER,
                   GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
       this.getContentPane().add(
             new JLabel("Month: "),
             new GridBagConstraints(4, 4, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER,
                   GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
       this.getContentPane().add(
             comboMonth,
             new GridBagConstraints(5, 4, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER,
                   GridBagConstraints.HORIZONTAL, new Insets(0, 5, 0, 5), 0, 0));
       this.getContentPane().add(
             jLabel9,
             new GridBagConstraints(6, 4, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER,
                   GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
       this.getContentPane().add(
             comboCategory,
             new GridBagConstraints(7, 4, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER,
                   GridBagConstraints.HORIZONTAL, new Insets(0, 5, 0, 5), 0, 0));
       this.getContentPane().add(
             butNewCat,
             new GridBagConstraints(8, 4, 1, 2, 0.0, 0.0, GridBagConstraints.NORTH,
                   GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
 
       this.getContentPane().add(
             new JLabel("Owner: "),
             new GridBagConstraints(4, 7, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER,
                   GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
       this.getContentPane().add(
             comboOwner,
             new GridBagConstraints(5, 7, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER,
                   GridBagConstraints.HORIZONTAL, new Insets(0, 5, 0, 5), 0, 0));
 
       this.getContentPane().add(
             jLabel8,
             new GridBagConstraints(0, 5, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER,
                   GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
       this.getContentPane().add(
             textNotes,
             new GridBagConstraints(1, 5, 8, 2, 1.0, 0.1, GridBagConstraints.CENTER,
                   GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 0, 0));
 
       this.getContentPane().add(
             jLabel6,
             new GridBagConstraints(0, 7, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER,
                   GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
       this.getContentPane().add(
             textNoTracks,
             new GridBagConstraints(1, 7, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER,
                   GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
       this.getContentPane().add(
             butTracks,
             new GridBagConstraints(2, 7, 2, 1, 0.0, 0.0, GridBagConstraints.WEST,
                   GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
       this.getContentPane().add(
             butPersonnel,
             new GridBagConstraints(7, 7, 1, 1, 0.0, 0.0, GridBagConstraints.EAST,
                   GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
       this.getContentPane().add(
             butGroup,
             new GridBagConstraints(8, 7, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER,
                   GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
 
       this.getContentPane().add(
             labelMix,
             new GridBagConstraints(0, 8, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER,
                   GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
       this.getContentPane().add(
             textMix,
             new GridBagConstraints(1, 8, 3, 1, 1.0, 0.0, GridBagConstraints.CENTER,
                   GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
       this.getContentPane().add(
             butMix,
             new GridBagConstraints(4, 8, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER,
                   GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
 
       this.getContentPane().add(
             labelPrice,
             new GridBagConstraints(7, 8, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER,
                   GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
 
       this.getContentPane().add(
             labelPrice,
             new GridBagConstraints(6, 8, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER,
                   GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
 
       this.getContentPane().add(
             textPrice,
             new GridBagConstraints(7, 8, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER,
                   GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
 
       this.getContentPane().add(
             scrollTracks,
             new GridBagConstraints(0, 9, 9, 1, 1.0, 0.9, GridBagConstraints.CENTER,
                   GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 0, 0));
 
       this.getContentPane().add(
             labelAuth,
             new GridBagConstraints(0, 10, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER,
                   GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
       this.getContentPane().add(
             textAuth,
             new GridBagConstraints(1, 10, 8, 1, 1.0, 0.0, GridBagConstraints.CENTER,
                   GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
 
       this.getContentPane().add(
             butParent,
             new GridBagConstraints(0, 11, 5, 1, 0.0, 0.0, GridBagConstraints.WEST,
                   GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
 
       this.getContentPane().add(
             butCancel,
             new GridBagConstraints(7, 11, 1, 1, 0.0, 0.0, GridBagConstraints.EAST,
                   GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
       this.getContentPane().add(
             butDone,
             new GridBagConstraints(8, 11, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER,
                   GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
 
    }
 
    public void maximise()
    {
       System.err.println("Maximising");
       this.setExtendedState(JFrame.MAXIMIZED_BOTH);
    }
 
    public void selectCategory(Category catIn)
    {
       comboCategory.setSelectedItem(catIn);
    }
 
    public void selectFormat(Format formIn)
    {
       comboFormat.setSelectedItem(formIn);
    }
 
    public void setAuthor(String in)
    {
       textAuth.setText(in);
    }
 
    public void setCategories(Collection<Category> in)
    {
       // Fill the categories combo box accordingly
       comboCategory.removeAllItems();
 
       for (Category category : in)
          comboCategory.addItem(category);
    }
 
    public void setCategory(Category in)
    {
       // Add this category
       comboCategory.addItem(in);
 
       // And select it
       comboCategory.setSelectedItem(in);
    }
 
    public void setCompiler(String name)
    {
       textMix.setText(name);
    }
 
    public void setDate(String in)
    {
       textDate.setText(in);
    }
 
    public void setGroop(String name, int trackNo)
    {
       // Get the button
       JButton tempBut = trackGroops.get(trackNo - 1);
       tempBut.setText(name);
    }
 
    public void setLength(String title, int no)
    {
       (trackLengths.get(no - 1)).setText(title);
    }
 
   public void setModel(Record rec) throws SQLException
    {
       model = rec;
 
       // Set the format
       if (model.getFormat() != null)
          comboFormat.setSelectedItem(model.getFormat());
       else
          comboFormat.setSelectedIndex(0);
    }
 
    public void setMonth(int in)
    {
       comboMonth.setSelectedIndex(in);
    }
 
    public void setNotes(String in)
    {
       textNotes.setText(in);
    }
 
    public void setNoTracks(int no, ActionListener list)
    {
       textNoTracks.setText("" + no);
       createTracks(list);
    }
 
    public void setParent(Record in)
    {
       butParent.setText(in.getAuthor() + " - " + in.getTitle());
    }
 
    public void setPrice(double in)
    {
       NumberFormat form = NumberFormat.getInstance();
       form.setMaximumFractionDigits(2);
       form.setMinimumFractionDigits(2);
       textPrice.setText(form.format(in));
    }
 
    public void setRecordTitle(String in)
    {
       textTitle.setText(in);
    }
 
    public void setRecOwner(int in)
    {
       comboOwner.setSelectedIndex(in - 1);
    }
 
    public void setTrackFormNumber(int number, int track)
    {
       (trackFormNumbers.get(track - 1)).setText("" + number);
    }
 
    public void setTrackTitle(String title, int no)
    {
       (trackTitles.get(no - 1)).setText(title);
    }
 
    public void setType(int in)
    {
       comboType.setSelectedIndex(in - 1);
    }
 
    public void setYear(String in)
    {
       textYear.setText(in);
    }
 }
