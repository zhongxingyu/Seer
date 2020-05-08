 /**
  * This file is part of the Paxle project.
  * Visit http://www.paxle.net for more information.
  * Copyright 2007-2009 the original author or authors.
  *
  * Licensed under the terms of the Common Public License 1.0 ("CPL 1.0").
  * Any use, reproduction or distribution of this program constitutes the recipient's acceptance of this agreement.
  * The full license text is available under http://www.opensource.org/licenses/cpl1.0.txt
  * or in the file LICENSE.txt in the root directory of the Paxle distribution.
  *
  * Unless required by applicable law or agreed to in writing, this software is distributed
  * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  */
 package org.paxle.desktop.impl.dialogues.stats;
 
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.lang.reflect.Method;
 import java.util.Collection;
import java.util.Map;
 
 import javax.swing.BorderFactory;
 import javax.swing.JButton;
 import javax.swing.JLabel;
 
 import org.osgi.framework.InvalidSyntaxException;
 import org.paxle.core.IMWComponent;
 import org.paxle.desktop.Utilities;
 import org.paxle.desktop.impl.DesktopServices;
 import org.paxle.desktop.impl.Messages;
 import org.paxle.desktop.impl.ServiceManager;
 import org.paxle.desktop.impl.DesktopServices.MWComponents;
 import org.paxle.desktop.impl.dialogues.stats.StatisticsPanel.Stats;
 
 class ActivityPanel extends Stats implements ActionListener {
 	
 	private static final long serialVersionUID = 1L;
 	
 	private final JLabel[] lblsCount;
 	private final JButton[] lblsStatus;
 	private final JLabel[] lblsComp;
 	private final JLabel[] lblsPPM;
 	private final JLabel[] lblsActive;
 	private final JLabel[] lblsEnqueued;
 	
 	private final DesktopServices services;
 	
 	public ActivityPanel(final DesktopServices services) {
 		this.services = services;
 		
 		final int cnt = MWComponents.values().length;
 		lblsCount = new JLabel[cnt];
 		lblsStatus = new JButton[cnt];
 		lblsComp = new JLabel[cnt];
 		lblsPPM = new JLabel[cnt];
 		lblsActive = new JLabel[cnt];
 		lblsEnqueued = new JLabel[cnt];
 		
 		init();
 	}
 	
 	private void init() {
 		super.setLayout(new GridBagLayout());
 		super.setBorder(BorderFactory.createTitledBorder(
 				BorderFactory.createEtchedBorder(),
 				Messages.getString("statisticsPanel.activity"))); //$NON-NLS-1$
 		
 		final GridBagConstraints gbcChart = new GridBagConstraints();
 		final GridBagConstraints gbcNums = new GridBagConstraints();
 		final GridBagConstraints gbcBtns = new GridBagConstraints();
 		final GridBagConstraints gbcDesc = new GridBagConstraints();
 		final GridBagConstraints gbcVals = new GridBagConstraints();
 		
 		gbcChart.gridx = 0; gbcChart.gridy = 0;
 		gbcChart.gridwidth = 6;
 		gbcChart.insets = Utilities.INSETS_DEFAULT;
 		gbcChart.anchor = GridBagConstraints.CENTER;
 		
 		gbcNums.gridx = 0; gbcNums.gridy = 1;
 		gbcNums.insets = Utilities.INSETS_DEFAULT;
 		gbcNums.anchor = GridBagConstraints.CENTER;
 		
 		gbcBtns.gridx = 1; gbcBtns.gridy = 1;
 		gbcBtns.insets = Utilities.INSETS_DEFAULT;
 		gbcBtns.anchor = GridBagConstraints.CENTER;
 		
 		gbcDesc.gridx = 2; gbcDesc.gridy = 1;
 		gbcDesc.insets = Utilities.INSETS_DEFAULT;
 		gbcDesc.anchor = GridBagConstraints.WEST;
 		
 		gbcVals.gridx = 3; gbcVals.gridy = 1;
 		gbcVals.insets = Utilities.INSETS_DEFAULT;
 		gbcVals.anchor = GridBagConstraints.WEST;
 		
 		// add chart
 		super.add(chart, gbcChart);
 		
 		// set title
 		super.add(new JLabel(Messages.getString("statisticsPanel.activity.num")), 		gbcNums); gbcNums.gridy++; //$NON-NLS-1$
 		super.add(new JLabel(Messages.getString("statisticsPanel.activity.status")), 	gbcBtns); gbcBtns.gridy++; //$NON-NLS-1$
 		super.add(new JLabel(Messages.getString("statisticsPanel.activity.component")), gbcDesc); gbcDesc.gridy++; //$NON-NLS-1$
 		super.add(new JLabel(Messages.getString("statisticsPanel.activity.ppm")), 		gbcVals); gbcVals.gridx++; //$NON-NLS-1$
 		super.add(new JLabel(Messages.getString("statisticsPanel.activity.active")), 	gbcVals); gbcVals.gridx++; //$NON-NLS-1$
 		super.add(new JLabel(Messages.getString("statisticsPanel.activity.enqueued")), 	gbcVals); gbcVals.gridx = 3; //$NON-NLS-1$
 		gbcVals.gridy++;
 		
 		// set values
 		final MWComponents[] comps = MWComponents.values();
 		for (int i=0; i<comps.length; i++) {
 			super.add(lblsCount[i] = new JLabel(), 		gbcNums); gbcNums.gridy++;
 			lblsStatus[i] = Utilities.setButtonProps(new JButton(), null, this, comps[i].name(), -1, null);
 			super.add(lblsStatus[i], 					gbcBtns); gbcBtns.gridy++;
 			super.add(lblsComp[i] = new JLabel(), 		gbcDesc); gbcDesc.gridy++;
 			super.add(lblsPPM[i] = new JLabel(), 		gbcVals); gbcVals.gridx++;
 			super.add(lblsActive[i] = new JLabel(), 	gbcVals); gbcVals.gridx++;
 			super.add(lblsEnqueued[i] = new JLabel(), 	gbcVals); gbcVals.gridx = 3;
 			gbcVals.gridy++;
 		}
 	}
 	
 	@Override
 	public boolean isStatsDataSupported() {
 		return true;
 	}
 	
 	@Override
 	public void initChart() {
 		if (sds != null) {
 			sds.init(
 					Messages.getString("statisticsPanel.activity.chart.title"),
 					Messages.getString("statisticsPanel.activity.chart.yDesc"),
 					Messages.getString("statisticsPanel.activity.chart.crawler"),
 					Messages.getString("statisticsPanel.activity.chart.parser"),
 					Messages.getString("statisticsPanel.activity.chart.indexer"));
 		}
 	}
 	
 	public void actionPerformed(ActionEvent e) {
 		final MWComponents mwComp = MWComponents.valueOf(e.getActionCommand());
 		final IMWComponent<?> comp = services.getMWComponent(mwComp);
 		if (comp != null) {
 			if (comp.isPaused()) {
 				comp.resume();
 			} else {
 				comp.pause();
 			}
 		}
 	}
 	
 	@Override
 	public boolean update() {
 		final ServiceManager manager = services.getServiceManager();
 		
 		final MWComponents[] comps = MWComponents.values();
 		boolean anyOk = false;
 		final Number[] ppms = new Number[comps.length];
 		for (int i=0; i<comps.length; i++) {
 			lblsComp[i].setText(comps[i].toString());
 			
 			boolean ok = false;
 			try {
 				final IMWComponent<?>[] mwComps = manager.getServices(IMWComponent.class, comps[i].toQuery());
 				if (mwComps != null && mwComps.length > 0) {
 					int cnt;
 					if (comps[i] == MWComponents.INDEXER) {
 						cnt = 1;
 					} else {
 						final Object subManager = manager.getService(comps[i].getID() + ".ISub" + comps[i].toString() + "Manager");
 						if (subManager == null) {
 							cnt = 0;
 						} else try {
 							final Method getSub_List = subManager.getClass().getMethod("getSub" + comps[i].toString() + "s");
							final Map<?,?> list = (Map<?,?>)getSub_List.invoke(subManager);
 							cnt = list.size();
 						} catch (Exception e) { e.printStackTrace(); cnt = -1; }
 					}
 					
 					lblsCount[i].setText(Integer.toString(cnt));
 					lblsStatus[i].setText((mwComps[0].isPaused())
 							? Messages.getString("statisticsPanel.activity.start")		//$NON-NLS-1$ 
 							: Messages.getString("statisticsPanel.activity.pause"));	//$NON-NLS-1$
 					final int ppm = mwComps[0].getPPM();
 					lblsPPM[i].setText(Integer.toString(ppm));
 					ppms[i] = Integer.valueOf(ppm);
 					lblsActive[i].setText(Integer.toString(mwComps[0].getActiveJobCount()));
 					lblsEnqueued[i].setText(Integer.toString(mwComps[0].getEnqueuedJobCount()));
 					ok = true;
 				}
 			} catch (InvalidSyntaxException e) { e.printStackTrace(); }
 			
 			if (!ok) {
 				lblsCount[i].setText(StatisticsPanel.NA);
 				lblsStatus[i].setText(StatisticsPanel.NA);
 				lblsPPM[i].setText(StatisticsPanel.NA);
 				lblsActive[i].setText(StatisticsPanel.NA);
 				lblsEnqueued[i].setText(StatisticsPanel.NA);
 			}
 			anyOk |= ok;
 		}
 		
 		if (sds != null)
 			sds.addOrUpdate(ppms);
 		
 		return anyOk;
 	}
 }
