 package be.ac.ua.comp.scarletnebula.gui;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.io.IOException;
 import java.util.Collection;
 import java.util.Random;
 
 import javax.swing.*;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import org.dasein.cloud.CloudException;
 import org.dasein.cloud.InternalException;
 import org.dasein.cloud.services.server.ServerState;
 
 import com.jgoodies.forms.builder.DefaultFormBuilder;
 import com.jgoodies.forms.layout.FormLayout;
 
 import be.ac.ua.comp.scarletnebula.core.CloudManager;
 import be.ac.ua.comp.scarletnebula.core.CloudProvider;
 import be.ac.ua.comp.scarletnebula.core.Server;
 import be.ac.ua.comp.scarletnebula.core.ServerChangedObserver;
 import be.ac.ua.comp.scarletnebula.core.ServerDisappearedException;
 
 public class GUI extends JFrame implements ListSelectionListener,
 		ServerChangedObserver
 {
 	private static Log log = LogFactory.getLog(GUI.class);
 
 	private static final long serialVersionUID = 1L;
 	private JList serverList;
 	private ServerListModel serverListModel;
 
 	private JPanel configurationTab;
 	private JPanel overviewTab;
 	private JPanel statisticsTab;
 	private JPanel communicationTab;
 
 	private JLabel statusLabel;
 	private JLabel dnsLabel;
 	private JLabel ipLabel;
 	private JLabel cloudLabel;
 	private JLabel unfriendlyNameLabel;
 	private JLabel sizeLabel;
 	private JLabel imageLabel;
 
 	public GUI()
 	{
 		try
 		{
 			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
 		}
 		catch (ClassNotFoundException e)
 		{
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		catch (InstantiationException e)
 		{
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		catch (IllegalAccessException e)
 		{
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		catch (UnsupportedLookAndFeelException e)
 		{
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		JPanel leftPartition = setupLeftPartition();
 		JPanel rightPartition = setupRightPartition();
 
 		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
 				leftPartition, rightPartition);
 		splitPane.setDividerSize(4);
 		splitPane.setDividerLocation(160);
 		add(splitPane);
 
 		setTitle("Scarlet Nebula");
 		setSize(600, 400);
 		setLocationRelativeTo(null);
 		setDefaultCloseOperation(EXIT_ON_CLOSE);
 		ImageIcon icon = new ImageIcon(getClass().getResource(
 				"/images/icon48.png"));
 		setIconImage(icon.getImage());
 
 		addInitialServers();
 		addMenubar();
 	}
 
 	private void addMenubar()
 	{
 		JMenuBar menuBar = new JMenuBar();
 		JMenu providerMenu = new JMenu("Providers");
 		providerMenu.setMnemonic(KeyEvent.VK_P);
 		providerMenu.getAccessibleContext().setAccessibleDescription(
 				"Managing cloud providers.");
 
 		JMenuItem manageProvidersItem = new JMenuItem("Manage Providers");
 		providerMenu.add(manageProvidersItem);
 
 		JMenuItem detectAllUnlinkedInstances = new JMenuItem(
 				"Detect all unlinked instances");
 		detectAllUnlinkedInstances.addActionListener(new ActionListener()
 		{
 			@Override
 			public void actionPerformed(ActionEvent e)
 			{
 				detectAllUnlinkedInstances();
 			}
 		});
 		providerMenu.add(detectAllUnlinkedInstances);
 		Collection<CloudProvider> providers = CloudManager.get()
 				.getLinkedCloudProviders();
 
 		for (final CloudProvider prov : providers)
 		{
 			JMenu providerSpecificSubMenu = new JMenu(prov.getName());
 			JMenuItem detectUnlinkedItem = new JMenuItem(
 					"Detect Unlinked Instances");
 			detectUnlinkedItem.addActionListener(new ActionListener()
 			{
 				@Override
 				public void actionPerformed(ActionEvent e)
 				{
 					System.out
 							.println("Detecting unlinked instances for provider"
 									+ prov.getName());
 				}
 			});
 			providerSpecificSubMenu.add(detectUnlinkedItem);
 
 			providerMenu.add(providerSpecificSubMenu);
 		}
 		menuBar.add(providerMenu);
 
 		JMenu helpMenu = new JMenu("Help");
 		helpMenu.setMnemonic(KeyEvent.VK_H);
 
 		// Pick a random message to display in the help menu
 		String messages[] = { "(You won't find any help here)",
 				"(Nobody can help you)", "(Keep on lookin' if you need help)",
 				"(Heeeeelp!)", "(You might want to try google for help)",
 				"(Try yelling loudly if you need help)" };
 
 		Random generator = new Random(System.currentTimeMillis());
 
 		JMenuItem noHelpItem = new JMenuItem(
 				messages[generator.nextInt(messages.length)]);
 		noHelpItem.setEnabled(false);
 		helpMenu.add(noHelpItem);
 
 		JMenuItem aboutItem = new JMenuItem("About...");
 		aboutItem.addActionListener(new ActionListener()
 		{
 			@Override
 			public void actionPerformed(ActionEvent e)
 			{
 				openAboutBox();
 			}
 		});
 
 		helpMenu.add(aboutItem);
 
 		menuBar.add(helpMenu);
 
 		setJMenuBar(menuBar);
 	}
 
 	protected void clearSelection()
 	{
 		serverList.clearSelection();
 		fillRightPartition();
 	}
 
 	/**
 	 * The method you should call when you want to keep refreshing until Server
 	 * "server" has state "state".
 	 * 
 	 * TODO Keep some kind of a map for each state, which can be checked whem
 	 * manually refreshing. Suppose a server is refreshed and it's state is
 	 * PAUSED. The user can then resume. Later, the server's timer that checks
 	 * server state will fire, and the state will show as RUNNING. The timer
 	 * will keep on firing until the count is high enough and it gives up, which
 	 * sucks. Therefore, when manually refreshing a server S, all timers for
 	 * that server should be checked. If there's a timer waiting for S's current
 	 * state, that timer should be cancelled.
 	 * 
 	 * @param server
 	 * @param state
 	 */
 	private void refreshUntilServerHasState(final Server server,
 			final ServerState state)
 	{
 		refreshUntilServerHasState(server, state, 1);
 	}
 
 	private void refreshUntilServerHasState(final Server server,
 			final ServerState state, final int attempt)
 	{
 		if (server.getStatus() == state || attempt > 20)
 			return;
 
 		try
 		{
 			server.refresh();
 		}
 		catch (InternalException e)
 		{
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		catch (CloudException e)
 		{
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		catch (ServerDisappearedException e)
 		{
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		if (server.getStatus() == state)
 			return;
 
 		// If the server's state still isn't the one we want it to be, try
 		// again, but only after waiting
 		// a logarithmic amount of time.
 		double wait = 15.0 * (Math.log10(attempt) + 1.0);
 
 		java.util.Timer timer = new java.util.Timer();
 		timer.schedule(new java.util.TimerTask()
 		{
 			@Override
 			public void run()
 			{
 				refreshUntilServerHasState(server, state, attempt + 1);
 				log.debug("Refreshing state for server "
 						+ server.getFriendlyName()
 						+ " because timer fired, waiting for state "
 						+ state.toString());
 				cancel();
 			}
 		}, (long) (wait * 1000));
 
 	}
 
 	protected void detectAllUnlinkedInstances()
 	{
 		Collection<CloudProvider> providers = CloudManager.get()
 				.getLinkedCloudProviders();
 
 		for (final CloudProvider prov : providers)
 		{
 			try
 			{
 				Collection<Server> unlinkedServers = prov.listUnlinkedServers();
 				log.debug("Provider " + prov.getName() + " has "
 						+ unlinkedServers.size() + " unlinked instances.");
 
 				for (Server server : unlinkedServers)
 				{
 					prov.registerUnlinkedServer(server);
 					addServer(server);
 				}
 			}
 			catch (Exception e)
 			{
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 
 		}
 	}
 
 	private void openAboutBox()
 	{
 		AboutWindow aboutWindow = new AboutWindow(this);
 		aboutWindow.setVisible(true);
 	}
 
 	private JPanel setupRightPartition()
 	{
 		JPanel total = new JPanel();
 		total.setLayout(new BorderLayout());
 
 		JTabbedPane tabbedPane = new JTabbedPane();
 
 		overviewTab = new JPanel();
 		configurationTab = new JPanel();
 		communicationTab = new JPanel();
 		statisticsTab = new JPanel();
 
 		ImageIcon stopIcon = new ImageIcon(getClass().getResource(
 				"/images/stop.png"));
 		JButton terminateButton = new JButton("Terminate Server", stopIcon);
 		terminateButton.addActionListener(new ActionListener()
 		{
 			@Override
 			public void actionPerformed(ActionEvent e)
 			{
 				terminateSelectedServers();
 			}
 		});
 		configurationTab.add(terminateButton);
 
 		overviewTab.setLayout(new BorderLayout());
 
 		FormLayout layout = new FormLayout(
 				"right:max(40dlu;p), 4dlu, min(50dlu;p):grow, 7dlu, "
 						+ "right:max(40dlu;p), 4dlu, min(50dlu;p):grow", "");
 		// add rows dynamically
 		DefaultFormBuilder builder = new DefaultFormBuilder(layout);
 		builder.setDefaultDialogBorder();
 		builder.appendSeparator("General Information");
 
 		statusLabel = new JLabel();
 		builder.append("Status", statusLabel);
 		cloudLabel = new JLabel();
 		builder.append("Provider", cloudLabel);
 		builder.nextLine();
 
 		dnsLabel = new JLabel();
 		builder.append("DNS Address", dnsLabel);
 		ipLabel = new JLabel();
 		builder.append("IP Address", ipLabel);
 		builder.nextLine();
 
 		builder.appendSeparator("Cloud Specific Information");
 		unfriendlyNameLabel = new JLabel();
 		builder.append("Name", unfriendlyNameLabel);
 		sizeLabel = new JLabel();
 		builder.append("Size", sizeLabel);
 		builder.nextLine();
 
 		imageLabel = new JLabel();
 		builder.append("Image", imageLabel);
 
 		overviewTab.add(builder.getPanel());
 
 		tabbedPane.addTab("Overview", overviewTab);
 		tabbedPane.addTab("Configuration", configurationTab);
 		tabbedPane.addTab("Communication", communicationTab);
 		tabbedPane.addTab("Statistics", statisticsTab);
 
 		total.add(tabbedPane);
 		return total;
 	}
 
 	protected void terminateSelectedServers()
 	{
 		Collection<Server> servers = getSelectedServers();
 
 		for (Server server : servers)
 		{
 			try
 			{
 				server.terminate();
 				refreshUntilServerHasState(server, ServerState.TERMINATED);
 			}
 			catch (CloudException e)
 			{
 				e.printStackTrace();
 			}
 			catch (InternalException e)
 			{
 				e.printStackTrace();
 			}
 		}
 	}
 
 	private JPanel setupLeftPartition()
 	{
 		// Create the list and put it in a scroll pane.
 		serverListModel = new ServerListModel();
 		serverList = new ServerList(serverListModel, this);
 		JScrollPane serverScrollPane = new JScrollPane(serverList);
 
 		ImageIcon addIcon = new ImageIcon(getClass().getResource(
 				"/images/add.png"));
 
 		JButton addButton = new JButton(addIcon);
 		addButton.addActionListener(new ActionListener()
 		{
 			@Override
 			public void actionPerformed(ActionEvent e)
 			{
 				startAddServerWizard();
 			}
 		});
 		addButton.setBounds(10, 10, addIcon.getIconWidth(),
 				addIcon.getIconHeight());
 
 		ImageIcon refreshIcon = new ImageIcon(getClass().getResource(
 				"/images/refresh.png"));
 		JButton refreshButton = new JButton(refreshIcon);
 		refreshButton.addActionListener(new ActionListener()
 		{
 			@Override
 			public void actionPerformed(ActionEvent e)
 			{
 				refreshSelectedServers();
 			}
 		});
 		refreshButton.setBounds(10, 10, refreshIcon.getIconWidth(),
 				refreshIcon.getIconHeight());
 
 		JTextField searchField = new JTextField(10);
 		SearchFieldListener searchFieldListener = new SearchFieldListener(
 				searchField, serverListModel);
 		searchField.addActionListener(searchFieldListener);
 		searchField.getDocument().addDocumentListener(searchFieldListener);
 
 		JPanel topLeftPane = new JPanel();
 		topLeftPane.setLayout(new BoxLayout(topLeftPane, BoxLayout.LINE_AXIS));
 		topLeftPane.add(searchField);
 
 		JPanel leftPanel = new JPanel();
 		leftPanel.setLayout(new BorderLayout());
 
 		leftPanel.add(topLeftPane, BorderLayout.PAGE_START);
 		leftPanel.add(serverScrollPane, BorderLayout.CENTER);
 
 		JPanel bottom = new JPanel();
 		bottom.setLayout(new BoxLayout(bottom, BoxLayout.X_AXIS));
 
 		// The button need to take up the full width of the bar on the left
 		// In a boxlayout, this is apparently computed from their maximum
 		// widths.
 		addButton.setMaximumSize(new Dimension(50000000, 500));
 		refreshButton.setMaximumSize(new Dimension(50000000, 500));
 
 		bottom.add(addButton, BorderLayout.WEST);
 		bottom.add(Box.createHorizontalGlue());
 		bottom.add(refreshButton, BorderLayout.EAST);
 		leftPanel.add(bottom, BorderLayout.PAGE_END);
 
 		return leftPanel;
 	}
 
 	protected void refreshSelectedServers()
 	{
 		int indices[] = serverList.getSelectedIndices();
 
 		Collection<Server> servers = serverListModel
 				.getVisibleServersAtIndices(indices);
 
 		fillRightPartition();
 
 		for (Server server : servers)
 		{
 			try
 			{
 				server.refresh();
 			}
 			catch (InternalException e)
 			{
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			catch (CloudException e)
 			{
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			catch (ServerDisappearedException e)
 			{
 				log.info(e.toString());
 				serverListModel.removeServer(server);
 			}
 		}
 	}
 
 	private void addInitialServers()
 	{
 		Collection<CloudProvider> providers = CloudManager.get()
 				.getLinkedCloudProviders();
 
 		for (CloudProvider prov : providers)
 		{
 			try
 			{
 				Collection<Server> servers = prov.loadLinkedServers();
 
 				if (servers == null)
 					return;
 
 				for (Server s : servers)
 				{
 					addServer(s);
 				}
 			}
 			catch (InternalException e1)
 			{
 				// TODO Auto-generated catch block
 				e1.printStackTrace();
 			}
 			catch (CloudException e1)
 			{
 				// TODO Auto-generated catch block
 				e1.printStackTrace();
 			}
 			catch (IOException e1)
 			{
 				// TODO Auto-generated catch block
 				e1.printStackTrace();
 			}
 		}
 	}
 
 	public void valueChanged(ListSelectionEvent e)
 	{
 		if (e.getValueIsAdjusting() == false)
 		{
 			fillRightPartition();
 		}
 	}
 
 	private void fillRightPartition()
 	{
 		int index = serverList.getSelectedIndex();
 
 		// When nothing is selected, return
 		if (index < 0)
 			return;
 
 		Server selectedServer = serverListModel.getVisibleServerAtIndex(index);
 
 		statusLabel.setText(selectedServer.getStatus().toString());
 		dnsLabel.setText(selectedServer.getPublicDnsAddress());
 
 		String ipString = new String();
 
 		for (String ip : selectedServer.getPublicIpAddresses())
 			ipString += ip + "\n";
 
 		ipLabel.setText(ipString);
 		cloudLabel.setText(selectedServer.getCloud().getName());
 		sizeLabel.setText(selectedServer.getSize());
 		unfriendlyNameLabel.setText(selectedServer.getUnfriendlyName());
 		imageLabel.setText(selectedServer.getImage());
 	}
 
 	void startAddServerWizard()
 	{
 		AddServerWizard wizard = new AddServerWizard(this, this);
 		wizard.setVisible(true);
 	}
 
 	public static void main(String[] args)
 	{
 		SwingUtilities.invokeLater(new Runnable()
 		{
 			public void run()
 			{
 				GUI ex = new GUI();
 				ex.setVisible(true);
 			}
 		});
 	}
	
	public void error(String errorMessage)
	{
		JOptionPane.showMessageDialog(this, errorMessage, "An unexpected error occured.", JOptionPane.ERROR_MESSAGE);
	}
 
	public void addServerWizardClosed(AddServerWizard wiz)
 	{
 		final String instancename = wiz.instancename;
 		final String instancesize = wiz.instancesize;
 		final CloudProvider provider = wiz.cloudProvider;
 
		if(Server.exists(instancename))
		{
			JOptionPane.showMessageDialog(this, "A server with this name already exists.", "Server already exists", JOptionPane.ERROR_MESSAGE);
			return;
		}
		
 		try
 		{
 			Server server = provider.startServer(instancename, instancesize);
 			addServer(server);
 			refreshUntilServerHasState(server, ServerState.RUNNING);
 		}
 		catch (InternalException e)
 		{
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		catch (CloudException e)
 		{
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	private void addServer(Server server)
 	{
 		serverListModel.addServer(server);
 		server.addServerChangedObserver(this);
 	}
 
 	@Override
 	public void serverChanged(Server server)
 	{
 		// Update the list on the left
 		serverListModel.refreshServer(server);
 		fillRightPartition();
 	}
 
 	public void pauseSelectedServers()
 	{
 		Collection<Server> selectedServers = getSelectedServers();
 
 		try
 		{
 			for (Server server : selectedServers)
 			{
 				server.pause();
 				refreshUntilServerHasState(server, ServerState.PAUSED);
 			}
 		}
 		catch (CloudException e)
 		{
			error(e.getMessage());
 		}
 		catch (InternalException e)
 		{
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	public void rebootSelectedServers()
 	{
 		Collection<Server> selectedServers = getSelectedServers();
 
 		try
 		{
 			for (Server server : selectedServers)
 				server.reboot();
 		}
 		catch (CloudException e)
 		{
 			e.printStackTrace();
 		}
 		catch (InternalException e)
 		{
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	private Collection<Server> getSelectedServers()
 	{
 		int indices[] = serverList.getSelectedIndices();
 		return serverListModel.getVisibleServersAtIndices(indices);
 	}
 
 	public void unlinkSelectedServers()
 	{
 		Collection<Server> selectedServers = getSelectedServers();
 
 		for (Server server : selectedServers)
 		{
 			serverListModel.removeServer(server);
 			server.unlink();
 		}
 	}
 
 }
