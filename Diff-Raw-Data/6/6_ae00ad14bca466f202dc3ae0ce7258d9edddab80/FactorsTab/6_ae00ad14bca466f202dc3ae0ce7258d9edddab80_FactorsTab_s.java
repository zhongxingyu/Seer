 package edu.nrao.dss.client;
 
 import com.extjs.gxt.ui.client.Style;
 import com.extjs.gxt.ui.client.Style.Scroll;
 import com.extjs.gxt.ui.client.widget.ContentPanel;
 import com.extjs.gxt.ui.client.widget.TabPanel;
 import com.extjs.gxt.ui.client.widget.TabItem;
 import com.extjs.gxt.ui.client.widget.layout.FitLayout;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.user.client.ui.ScrollPanel;
 
 public class FactorsTab implements FactorsDisplay {
 	
 	private TabPanel panel;
 	
 	public FactorsTab(TabPanel tp) {
 		panel = tp;
 	}
 	
 	@Override
 	public void show(String title, String banner, String[] headers, String[][] factors) {
 		FactorGrid grid = new FactorGrid(factors.length, headers.length, headers, factors);
         TabItem item = new TabItem(title);
         ContentPanel cp = createContentPanel(banner, grid);
         item.add(cp);
         item.getHeader().setToolTip(title);
         item.setLayout(new FitLayout());
         item.setClosable(true);
         panel.add(item);
 	}
 	
 	public ContentPanel createContentPanel(String banner, FactorGrid grid) {
 		ContentPanel panel = new ContentPanel();
 		panel.setHeading(banner);
 		panel.setLayout(new FitLayout());
 		
 		ScrollPanel sp = new ScrollPanel(grid);
 		panel.add(sp);
		panel.setScrollMode(Scroll.AUTO);
 		
 		return panel;
 	}
 
 }
