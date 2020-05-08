 package com.robonobo.gui.panels;
 
 import static com.robonobo.gui.GUIUtils.*;
 import info.clearthought.layout.TableLayout;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.util.List;
 import java.util.Map;
 
 import javax.swing.*;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import com.robonobo.common.concurrent.CatchingRunnable;
 import com.robonobo.common.util.FileUtil;
 import com.robonobo.common.util.TextUtil;
 import com.robonobo.core.RobonoboController;
 import com.robonobo.core.api.*;
 import com.robonobo.gui.GUIUtils;
 import com.robonobo.gui.RoboFont;
 import com.robonobo.gui.components.BalanceLabel;
 import com.robonobo.gui.components.base.*;
 import com.robonobo.gui.frames.RobonoboFrame;
 import com.robonobo.mina.external.ConnectedNode;
 
 @SuppressWarnings("serial")
 public class StatusPanel extends JPanel implements RobonoboStatusListener, TransferSpeedListener {
 	Log log = LogFactory.getLog(getClass());
 
 	RobonoboController control;
 	private ImageIcon connFailImg;
 	private ImageIcon connOkImg;
 	private RLabel networkStatusIcon;
 	private RLabel numConnsLbl;
 
 	private RLabel bandwidthLbl;
 
 	private BalanceLabel balanceLbl;
 	
 	public StatusPanel(RobonoboFrame frame) {
 		this.control = frame.getController();
 		setPreferredSize(new Dimension(200, 85));
 		setMaximumSize(new Dimension(200, 85));
 		double[][] cellSizen = { { 1, 9, 32, 5, 110, TableLayout.FILL, 5 }, { 10, 30, 5, 15, 15, 5, TableLayout.FILL} };
 		setLayout(new TableLayout(cellSizen));
 		setName("robonobo.status.panel");
 		setOpaque(true);
 		
 		balanceLbl = new BalanceLabel(frame);
 		add(balanceLbl, "1,1,6,1,CENTER,CENTER");
 
 		connOkImg = createImageIcon("/icon/connection_ok.png", null);
 		connFailImg = createImageIcon("/icon/connection_fail.png", null);
 		networkStatusIcon = new RIconLabel(connFailImg);
 		add(networkStatusIcon, "2,3,2,5");
 		
 		numConnsLbl = new RLabel9("Starting...");
 		numConnsLbl.setFont(RoboFont.getFont(9, false));
 		numConnsLbl.setForeground(Color.WHITE);
 		add(numConnsLbl, "4,3,5,3,LEFT,BOTTOM");
 
 		bandwidthLbl = new RLabel9("");
 		bandwidthLbl.setFont(RoboFont.getFont(9, false));
 		bandwidthLbl.setForeground(Color.WHITE);
 		add(bandwidthLbl, "4,4,5,4,LEFT,BOTTOM");
 
 		control.addRobonoboStatusListener(this);
 		updateConnStatus();		
 	}
 
 	public BalanceLabel getBalanceLbl() {
 		return balanceLbl;
 	}
 	
 	@Override
 	public void roboStatusChanged() {
 		updateConnStatus();
 	}
 	
 	@Override
 	public void connectionAdded(ConnectedNode node) {
 		updateConnStatus();
 	}
 	
 	@Override
 	public void connectionLost(ConnectedNode node) {
 		updateConnStatus();
 	}
 
 	private void updateConnStatus() {
 		SwingUtilities.invokeLater(new CatchingRunnable() {
 			public void doRun() throws Exception {
 				synchronized (StatusPanel.this) {
 					RobonoboStatus status = control.getStatus();
 					switch(status) {
 					case Starting:
 						numConnsLbl.setText("Starting...");
 						networkStatusIcon.setIcon(connFailImg);
 						break;
 					case NotConnected:
						numConnsLbl.setText("No connections");
 						networkStatusIcon.setIcon(connFailImg);
 						break;
 					case Connected:
 						List<ConnectedNode> nodes = control.getConnectedNodes();
 						if(nodes.size() > 0)
 							networkStatusIcon.setIcon(connOkImg);
 						else
 							networkStatusIcon.setIcon(connFailImg);
						numConnsLbl.setText(TextUtil.numItems(nodes, "connection"));
 						break;
 					case Stopping:
 						numConnsLbl.setText("Stopping...");
 					}
 				}
 			}
 		});
 	}
 	
 	@Override
 	public void newTransferSpeeds(Map<String, TransferSpeed> speedsByStream, Map<String, TransferSpeed> speedsByNode) {
 		int totalDown = 0;
 		int totalUp = 0;
 		for (TransferSpeed ts : speedsByNode.values()) {
 			totalDown += ts.download;
 			totalUp += ts.upload;
 		}
 		updateSpeeds(totalDown, totalUp);
 	}
 	
 	private void updateSpeeds(final int downloadBps, final int uploadBps) {
 		SwingUtilities.invokeLater(new CatchingRunnable() {
 			public void doRun() throws Exception {
 				StringBuffer sb = new StringBuffer();
 				sb.append(FileUtil.humanReadableSize(uploadBps)).append("/s");
 				sb.append(" up - ");
 				sb.append(FileUtil.humanReadableSize(downloadBps)).append("/s");
 				sb.append(" down");
 				synchronized (StatusPanel.this) {
 					bandwidthLbl.setText(sb.toString());
 				}
 			}
 		});
 	}
 }
