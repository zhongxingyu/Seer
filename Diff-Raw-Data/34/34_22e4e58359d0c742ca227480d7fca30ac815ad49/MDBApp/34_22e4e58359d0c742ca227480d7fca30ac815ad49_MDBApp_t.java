 package uk.co.brotherlogic.mdb;
 
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.sql.SQLException;
 
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 import javax.swing.SwingUtilities;
 import javax.swing.UIManager;
 
 import uk.co.brotherlogic.mdb.artist.GetArtists;
 import uk.co.brotherlogic.mdb.categories.GetCategories;
 import uk.co.brotherlogic.mdb.cdbuilder.MakeCDFileOverseer;
 import uk.co.brotherlogic.mdb.format.GetFormats;
 import uk.co.brotherlogic.mdb.groop.GetGroops;
 import uk.co.brotherlogic.mdb.label.GetLabels;
 import uk.co.brotherlogic.mdb.record.AddRecordOverseer;
 import uk.co.brotherlogic.mdb.record.GetRecords;
 import uk.co.brotherlogic.mdb.record.Record;
 
 /**
  * Main entry point for the MDB App
  * 
  * @author sat
  * 
  */
 public class MDBApp extends JFrame
 {
 	/** The output string for windows */
 	private String fileString = "i:\\";
 
 	/**
 	 * Constructor
 	 */
 	public MDBApp()
 	{
 	}
 
 	private void addCD()
 	{
 		this.setVisible(false);
 		final MDBApp ref = this;
 		SwingUtilities.invokeLater(new Runnable()
 		{
 			public void run()
 			{
 				try
 				{
					AddRecordOverseer over = new AddRecordOverseer(ref, GetArtists.create().getArtists(), GetLabels.create().getLabels(), GetFormats
							.create().getFormats(), GetGroops.build().getGroopMap(), GetCategories.build().getCategories());
 					over.showGUI(ref);
 				}
 				catch (SQLException e)
 				{
 					e.printStackTrace();
 				}
 			}
 		});
 	}
 
 	public final void addDone(final Record done)
 	{
 		// Add record is done!
 
		try
		{
			System.err.println(done.getNumber());
			if (done.getNumber() <= 0)
				GetRecords.create().addRecord(done);
			else
				GetRecords.create().updateRecord(done);
		}
		catch (SQLException ex)
		{
			ex.printStackTrace();
		}
 
 		this.setVisible(true);
 	}
 
 	public final void cancel()
 	{
 		this.setVisible(true);
 	}
 
 	private void edit()
 	{
 		try
 		{
 			// Choose a file to examine
 			RecordSelector sel = new RecordSelector();
 			Record examine = sel.selectRecord(this);
 
 			if (examine != null)
 			{
 				// Prepare the viewer
 				this.setVisible(false);
 
				AddRecordOverseer over = new AddRecordOverseer(this, GetArtists.create().getArtists(), GetLabels.create().getLabels(), GetFormats
						.create().getFormats(), GetGroops.build().getGroopMap(), GetCategories.build().getCategories(), examine);
 				over.showGUI(this);
 			}
 		}
 		catch (Exception ex)
 		{
 			ex.printStackTrace();
 		}
 
 	}
 
 	private void jbInit() throws Exception
 	{
 		JButton buttonAdd = new JButton("Add Record");
 		buttonAdd.addActionListener(new java.awt.event.ActionListener()
 		{
 			public void actionPerformed(final ActionEvent e)
 			{
 				addCD();
 			}
 		});
 
 		this.getContentPane().setLayout(new GridLayout(2, 2));
 
 		JButton buttonEdit = new JButton("Edit Record");
 		buttonEdit.addActionListener(new java.awt.event.ActionListener()
 		{
 			public void actionPerformed(final ActionEvent e)
 			{
 				edit();
 			}
 		});
 
 		JButton buttonCD = new JButton("Make CD File");
 		buttonCD.addActionListener(new java.awt.event.ActionListener()
 		{
 			public void actionPerformed(final ActionEvent e)
 			{
 				makeCD();
 			}
 		});
 
 		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
 		this.setTitle("Music Database");
 
 		this.getContentPane().add(buttonAdd, null);
 		this.getContentPane().add(buttonCD, null);
 		this.getContentPane().add(buttonEdit, null);
 	}
 
 	private void makeCD()
 	{
 		try
 		{
 			// Run the button CD overseer
 			this.setVisible(false);
 			new MakeCDFileOverseer(GetRecords.create(), fileString);
 			this.setVisible(true);
 		}
 		catch (SQLException e2)
 		{
 			JOptionPane.showMessageDialog(this, e2.getMessage());
 		}
 
 	}
 
 	public final void runApp()
 	{
 
 		// Now construct the gui
 		try
 		{
 			jbInit();
 			final int appSize = 500;
 			this.setSize(appSize, appSize);
 
 			// Center the frame on screen
 			this.setLocationRelativeTo(null);
 
 			// Display the giu
 			this.setVisible(true);
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 		}
 
 	}
 
 	public final void setFileString(final String in)
 	{
 		fileString = in;
 	}
 
 	public static void main(final String[] args) throws Exception
 	{
 		try
 		{
 			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
 		}
 		catch (Exception ex)
 		{
 			ex.printStackTrace();
 		}
 
 		MDBApp c = new MDBApp();
 		if (System.getProperty("os.name").compareToIgnoreCase("Linux") == 0)
 			c.setFileString("/usr/share/hancock_multimedia/");
 		c.runApp();
 	}
 
 }
