 package edu.ucsf.rbvi.cddApp.internal.ui;
 
 import java.awt.BorderLayout;
 import java.awt.Component;
 import java.awt.Desktop;
 import java.io.IOException;
 import java.net.URISyntaxException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 
 import javax.swing.Icon;
 import javax.swing.JEditorPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.event.HyperlinkEvent;
 import javax.swing.event.HyperlinkListener;
 
 import org.cytoscape.application.CyApplicationManager;
 import org.cytoscape.application.swing.CytoPanelComponent;
 import org.cytoscape.application.swing.CytoPanelName;
 import org.cytoscape.model.CyNetwork;
 import org.cytoscape.model.CyTable;
 import org.cytoscape.model.events.RowSetRecord;
 import org.cytoscape.model.events.RowsSetEvent;
 import org.cytoscape.model.events.RowsSetListener;
 import org.cytoscape.util.swing.OpenBrowser;
 
 /**
  * Displays information on the domains of a protein from the CDD in the Results panel.
  * @author Allan Wu
  *
  */
 public class DomainsPanel extends JPanel implements CytoPanelComponent,
 		RowsSetListener {
 	
 	private JEditorPane textArea;
 	private JScrollPane scrollPane;
 	private HashMap<Long, Boolean> selectedNodes;
 	private CyApplicationManager manager;
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 4255348824636450908L;
 
 	/**
 	 * 
 	 * @param manager CyApplication manager of this instance of Cytoscape
 	 * @param openBrowser class that opens the default browser from Cytoscape
 	 */
 	public DomainsPanel(CyApplicationManager manager, OpenBrowser openBrowser) {
 		final OpenBrowser ob = openBrowser;
 		setLayout(new BorderLayout());
 		textArea = new JEditorPane("text/html", null);
 		textArea.setEditable(false);
 		textArea.addHyperlinkListener(new HyperlinkListener() {
 		    public void hyperlinkUpdate(HyperlinkEvent e) {
 		        if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
 		        	ob.openURL(e.getURL().toString());
 		        }
 		    }
 		});
 		scrollPane = new JScrollPane(textArea);
 		add(BorderLayout.CENTER, scrollPane);
 		selectedNodes = new HashMap<Long, Boolean>();
 		this.manager = manager;
 	}
 	
 	public void handleEvent(RowsSetEvent arg0) {
 		try {
 		CyTable table;
		// If we're not getting selection, we're not interested
		if (manager.getCurrentNetwork() == null || !arg0.containsColumn(CyNetwork.SELECTED)) return;
 		table = manager.getCurrentNetwork().getDefaultNodeTable();
		// Oops, not relevant to us...
		if (table.getColumn("PDB-Chain") == null) return;
 		String message = "";
 		Collection<RowSetRecord> record = arg0.getPayloadCollection();
 		for (RowSetRecord r: record) {
 			Long suid = r.getRow().get(CyNetwork.SUID, Long.class);
 			if (suid != null && table.getRow(suid) != null &&
 					table.getRow(suid).getList("PDB-Chain", String.class) != null &&
 					table.getRow(suid).getList("PDB-Chain", String.class).size() > 0 &&
 					table.getRow(suid).getList("PDB-Chain-Features", String.class) != null &&
 					table.getRow(suid).getList("PDB-Chain-Features", String.class).size() > 0)
 				selectedNodes.put(suid,r.getRow().get(CyNetwork.SELECTED, Boolean.class));
 		}
 		for (long node: selectedNodes.keySet()) {
 			if (table.getRow(node) != null && selectedNodes.get(node) != null && selectedNodes.get(node)) {
 				List<String> pdbChains = table.getRow(node).getList("PDB-Chain", String.class),
 						pdbChainFeatures = table.getRow(node).getList("PDB-Chain-Features", String.class),
 						cddAccession = table.getRow(node).getList("CDD-Accession", String.class),
 						domainType = table.getRow(node).getList("CDD-Hit-Type", String.class),
 						cddFeature = table.getRow(node).getList("CDD-Feature", String.class),
 						cddFeatureType = table.getRow(node).getList("CDD-Feature-Type", String.class),
 						cddFeatureSite = table.getRow(node).getList("CDD-Feature-Site", String.class);
 				List<Long> cddFrom = table.getRow(node).getList("CDD-From", Long.class),
 						cddTo = table.getRow(node).getList("CDD-To", Long.class);
 				if ((pdbChains == null || pdbChains.size() == 0 || pdbChains.get(0).equals("")) &&
 						(cddFeature == null || cddFeature.size() == 0 || cddFeature.get(0).equals(""))) continue;
 				HashSet<String> chains = new HashSet<String>();
 				for (String s: pdbChains) if (!s.equals("")) chains.add(s);
 				for (String s: pdbChainFeatures) if (!s.equals("")) chains.add(s);
 				
 				HashMap<String, List<Integer>> pdbChainPos = new HashMap<String, List<Integer>>(),
 						pdbChainFeaturePos = new HashMap<String, List<Integer>>();
 				for (int i = 0; i < pdbChains.size(); i++) {
 					String n = pdbChains.get(i);
 					if (!pdbChainPos.containsKey(n))
 						pdbChainPos.put(n, new ArrayList<Integer>());
 					pdbChainPos.get(n).add(i);
 				}
 				for (int i = 0; i < pdbChainFeatures.size(); i++) {
 					String n = pdbChainFeatures.get(i);
 					if (!pdbChainFeaturePos.containsKey(n))
 						pdbChainFeaturePos.put(n, new ArrayList<Integer>());
 					pdbChainFeaturePos.get(n).add(i);
 				}
 				message = message + "<div>";
 				message = message + "<p><b>Node: \n" + 
 						table.getRow(node).get(CyNetwork.NAME, String.class) + "</b></p>\n\n";
 				for (String chain: chains) {
 					message = message + "<p>Protein: " + chain + "</p>\n";
 					message = message + "<table border=\"1\">\n<tr><th>Domain Name</th><th>Domain Type</th><th>Domain Range</th></tr>\n";
 					if (pdbChains != null && pdbChains.size() != 0 && ! pdbChains.get(0).equals("") && pdbChainPos.get(chain) != null)
 						for (int i: pdbChainPos.get(chain)) {
 							message = message + "<tr><td><a href=\"http://www.ncbi.nlm.nih.gov/cdd/?term=" + cddAccession.get(i) + "\">" + cddAccession.get(i) + "</a></td>\n" +
 									"<td>" + domainType.get(i) + "</td>\n" +
 									"<td>" + cddFrom.get(i) + "-" + cddTo.get(i) + "</td></tr>\n\n";
 						}
 					if (cddFeature != null && cddFeature.size() != 0 && ! cddFeature.get(0).equals("") && pdbChainFeaturePos.get(chain) != null)
 						for (int i: pdbChainFeaturePos.get(chain)) {
 							message = message + "<tr><td>" + cddFeature.get(i) + "</td>\n" +
 									"<td>" + cddFeatureType.get(i) + "</td>\n" +
 									"<td>" + cddFeatureSite.get(i) + "</td></tr>\n\n";
 						}
 					message = message + "</table>";
 				}
 				message = message + "</div>";
 			}
 		}
 		textArea.setText(message);
 		}
 		catch (Exception e){e.printStackTrace();}
 	}
 
 	public Component getComponent() {
 		// TODO Auto-generated method stub
 		return this;
 	}
 
 	public CytoPanelName getCytoPanelName() {
 		// TODO Auto-generated method stub
 		return CytoPanelName.EAST;
 	}
 
 	public Icon getIcon() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public String getTitle() {
 		// TODO Auto-generated method stub
 		return "CDD Domains";
 	}
 
 }
