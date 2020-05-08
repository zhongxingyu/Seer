 package be.ac.ua.comp.scarletnebula.gui;
 
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.Collection;
 import java.util.List;
 
 import javax.swing.JDialog;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JComboBox;
 import javax.swing.JTextField;
 
 
 import org.ciscavate.cjwizard.*;
 import org.ciscavate.cjwizard.pagetemplates.DefaultPageTemplate;
 
 import be.ac.ua.comp.scarletnebula.core.CloudManager;
 import be.ac.ua.comp.scarletnebula.core.CloudProvider;
 
 
 public class AddServerWizard extends JDialog
 {
 
 	private static final long serialVersionUID = 4523932653698865313L;
 
 	String instancename;
 	String providername;
 	String instancesize;
 
 	CloudProvider cloudProvider;
 
 	public AddServerWizard(JFrame frame, final GUI gui)
 	{
 		super(frame);
 
 		// first, build the wizard. The TestFactory defines the
 		// wizard content and behavior.
 		final WizardContainer wc = new WizardContainer(
 				new AddServerPageFactory(), new DefaultPageTemplate());
 
 		wc.addWizardListener(new WizardListener()
 		{
 			@Override
 			public void onCanceled(List<WizardPage> path,
 					WizardSettings settings)
 			{
 				AddServerWizard.this.dispose();
 			}
 
 			@Override
 			public void onFinished(List<WizardPage> path,
 					WizardSettings settings)
 			{
 				instancename = (String) settings.get("instancename");
 				providername = (String) settings.get("provider");
 				instancesize = (String) settings.get("instancesize");
 
				gui.addServerWizardClosed(AddServerWizard.this);
 				AddServerWizard.this.dispose();
 			}
 
 			@Override
 			public void onPageChanged(WizardPage newPage, List<WizardPage> path)
 			{
 				// Set the dialog title to match the description of the new
 				// page:
 				AddServerWizard.this.setTitle(newPage.getDescription());
 			}
 		});
 
 		// Set up the standard bookkeeping stuff for a dialog, and
 		// add the wizard to the JDialog:
 		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
 		this.getContentPane().add(wc);
 		this.setPreferredSize(new Dimension(400, 300));
 		this.setLocation(0, 0);
 		this.setLocationRelativeTo(null);
 		this.pack();
 	}
 
 	private class AddServerPageFactory implements PageFactory
 	{
 
 		// To keep things simple, we'll just create an array of wizard pages:
 		private final WizardPage[] pages = {
 				new WizardPage("Cloud Type", "Cloud Type")
 				{
 					private static final long serialVersionUID = 1L;
 
 					{
 						String[] providers = { "Amazon Web Services" };
 
 						// Create the combo box, select item at index 4.
 						// Indices start at 0, so 4 specifies the pig.
 						final JComboBox providerList = new JComboBox(providers);
 						providerList.setName("provider");
 						providerList.addActionListener(new ActionListener()
 						{
 							@Override
 							public void actionPerformed(ActionEvent e)
 							{
 								providername = (String) providerList
 										.getSelectedItem();
 								cloudProvider = CloudManager.get()
 										.getCloudProviderByName(providername);
 							}
 						});
 						providerList.setSelectedIndex(0);
 
 						add(new JLabel("Choose a Cloud Provider"));
 						add(providerList);
 					}
 				}, new WizardPage("Instance Information", "Instance Information")
 				{
 					/**
 					 * 
 					 */
 					private static final long serialVersionUID = 1L;
 
 					{
 						JTextField field = new JTextField();
 						field.setName("instancename");
 						field.setPreferredSize(new Dimension(100, 25));
 						add(new JLabel("Instance name:"));
 						add(field);
 
 						add(new JLabel("Instance size:"));
 
 						final JComboBox sizeList = new JComboBox();
 						sizeList.setName("instancesize");
 
 
 						Collection<String> sizes = cloudProvider
 								.getPossibleInstanceSizes();
 						for (String size : sizes)
 							sizeList.addItem(size);
 
 						sizeList.setSelectedIndex(0);
 						add(sizeList);
 					}
 				}, new WizardPage("Last", "Last")
 				{
 					/**
 					 * 
 					 */
 					private static final long serialVersionUID = 1L;
 
 					{
 
 						/**/
 					}
 
 					/**
 					 * This is the last page in the wizard, so we will enable
 					 * the finish button and disable the "Next >" button just
 					 * before the page is displayed:
 					 */
 					public void rendering(List<WizardPage> path,
 							WizardSettings settings)
 					{
 						super.rendering(path, settings);
 						setFinishEnabled(true);
 						setNextEnabled(false);
 					}
 
 				} };
 
 		/*
 		 * (non-Javadoc)
 		 * 
 		 * @see org.ciscavate.cjwizard.PageFactory#createPage(java.util.List,
 		 * org.ciscavate.cjwizard.WizardSettings)
 		 */
 		@Override
 		public WizardPage createPage(List<WizardPage> path,
 				WizardSettings settings)
 		{
 
 			// Get the next page to display. The path is the list of all wizard
 			// pages that the user has proceeded through from the start of the
 			// wizard, so we can easily see which step the user is on by taking
 			// the length of the path. This makes it trivial to return the next
 			// WizardPage:
 			WizardPage page = pages[path.size()];
 
 			// if we wanted to, we could use the WizardSettings object like a
 			// Map<String, Object> to change the flow of the wizard pages.
 			// In fact, we can do arbitrarily complex computation to determine
 			// the next wizard page.
 
 			return page;
 		}
 
 	}
 
 }
