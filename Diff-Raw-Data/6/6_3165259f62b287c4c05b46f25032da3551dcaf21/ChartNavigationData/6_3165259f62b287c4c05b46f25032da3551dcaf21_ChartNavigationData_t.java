 package de.enwida.web.model;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import de.enwida.transport.Aspect;
 import de.enwida.transport.DataResolution;
 import de.enwida.web.model.ProductTree.ProductAttributes;
 import de.enwida.web.utils.CalendarRange;
 import de.enwida.web.utils.NavigationDefaults;
 
 
 public class ChartNavigationData implements Cloneable {
 
 	private String chartTitle;
 	private String xAxisLabel;
 	private String yAxisLabel;
 	private NavigationDefaults defaults;
 	private List<ProductTree> productTrees;
 	private Map<String, String> timeRanges;
 	private List<Aspect> aspects;
 	private boolean isDateScale;
 	private boolean hasTimeSelection;
 	private boolean hasProductSelection;
 	private boolean hasLineSelection;
 
 	public ChartNavigationData() {
 		this.productTrees = new ArrayList<>();
 		this.timeRanges = new HashMap<>();
 		this.aspects = new ArrayList<>();
 	}
 	
 	public ChartNavigationData(String chartTitle, String xAxisLabel, String yAxisLabel) {
 		this(chartTitle, xAxisLabel, yAxisLabel, true, new ArrayList<ProductTree>(), null);
 	}
 
 	public ChartNavigationData(String chartTitle, String xAxisLabel, String yAxisLabel,
 	       boolean dateScale, List<ProductTree> productTrees, NavigationDefaults defaults) {
 	    
 		this.chartTitle = chartTitle;
 		this.xAxisLabel = xAxisLabel;
 		this.yAxisLabel = yAxisLabel;
 		this.isDateScale = dateScale;
 		this.productTrees = productTrees;
 		this.defaults = defaults;
 		this.aspects = new ArrayList<>();
 		this.timeRanges = new HashMap<>();
 		
 		// Show all selections by default
 		this.hasLineSelection = true;
 		this.hasProductSelection = true;
 		this.hasTimeSelection = true;
 	}
 
 	public ChartNavigationData clone() {
 	    final ChartNavigationData result = new ChartNavigationData(chartTitle, xAxisLabel, yAxisLabel);
 	    result.setDefaults(defaults.clone());
 	    result.aspects = new ArrayList<Aspect>(aspects);
 	    result.timeRanges = new HashMap<>(timeRanges);
 
 	    result.setIsDateScale(isDateScale);
 	    result.setHasLineSelection(hasLineSelection);
 	    result.setHasProductSelection(hasProductSelection);
 	    result.setHasTimeSelection(hasTimeSelection);
 
 	    for (final ProductTree tree : productTrees) {
 	        result.addProductTree(tree.clone());
 	    }
 	    return result;
 	}
 
 	public NavigationDefaults getDefaults() {
 		return this.defaults;
 	}
 
 	public List<ProductTree> getProductTrees() {
 		return this.productTrees;
 	}
 	
 	public void addProductTree(ProductTree productTree) {
 	    this.productTrees.add(productTree);
 	}
 
 	public List<DataResolution> getAllResolutions() {
 	    final List<DataResolution> result = new ArrayList<DataResolution>();
 	    final Set<DataResolution> set = new HashSet<DataResolution>();
 
 	    for (final ProductTree productTree : productTrees) {
     	    for (final ProductAttributes product : productTree.flatten()) {
     	        set.addAll(product.resolutions);
     	    }
 	    }
 	    result.addAll(set);
 	    return result;
 	}
 
 	public CalendarRange getTimeRangeMax() {
 	    final List<CalendarRange> ranges = new ArrayList<CalendarRange>();
 
 	    for (final ProductTree productTree : productTrees) {
     	    final List<ProductAttributes> products = productTree.flatten();
     	    
     	    for (final ProductAttributes product : products) {
     	        ranges.add(product.timeRange);
     	    }
 	    }
 	    return CalendarRange.getMaximum(ranges);
 	}
 
 	public CalendarRange getTimeRangeMin() {
 	    final List<CalendarRange> ranges = new ArrayList<CalendarRange>();
 
 	    for (final ProductTree productTree : productTrees) {
     	    final List<ProductAttributes> products = productTree.flatten();
     	    
     	    for (final ProductAttributes product : products) {
     	        ranges.add(product.timeRange);
     	    }
 	    }
 	    return CalendarRange.getMinimum(ranges);
 	}
 	
 	public Map<Integer, String> getTsos() {
 		final Map<Integer, String> result = new HashMap<>();
 		
 		for (ProductTree tree : productTrees) {
 			result.put(tree.getTso(), tree.getTsoName());
 		}
 		return result;
 	}
 
 	public String getTitle() {
 		return this.chartTitle;
 	}
 
 	public String getxAxisLabel() {
 		return this.xAxisLabel;
 	}
 
 	public String getyAxisLabel() {
 		return this.yAxisLabel;
 	}
 
 	public void setChartTitle(String chartTitle) {
 		this.chartTitle = chartTitle;
 	}
 
 	public void setDefaults(NavigationDefaults defaults) {
 		this.defaults = defaults;
 	}
 
 	public void setTitle(String title) {
 		this.chartTitle = title;
 	}
 
 	public void setxAxisLabel(String xAxisLabel) {
 		this.xAxisLabel = xAxisLabel;
 	}
 
 	public void setyAxisLabel(String yAxisLabel) {
 		this.yAxisLabel = yAxisLabel;
 	}
 
     public boolean getIsDateScale() {
         return isDateScale;
     }
 
     public void setIsDateScale(boolean dateScale) {
         this.isDateScale = dateScale;
     }
 
     public List<Aspect> getAspects() {
         return aspects;
     }
 
     public Map<String, String> getTimeRanges() {
         return timeRanges;
     }
 
 	public boolean getHasTimeSelection() {
 		return hasTimeSelection;
 	}
 
 	public void setHasTimeSelection(boolean hasTimeSelection) {
 		this.hasTimeSelection = hasTimeSelection;
 	}
 
 	public boolean getHasProductSelection() {
 		return hasProductSelection;
 	}
 
 	public void setHasProductSelection(boolean hasProductSelection) {
 		this.hasProductSelection = hasProductSelection;
 	}
 
 	public boolean getHasLineSelection() {
 		return hasLineSelection;
 	}
 
 	public void setHasLineSelection(boolean hasLineSelection) {
 		this.hasLineSelection = hasLineSelection;
 	}
 	
 }
