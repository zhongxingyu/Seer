 package de.enwida.web.utils;
 
 import java.nio.ByteBuffer;
 import java.nio.charset.Charset;
 import java.nio.file.Files;
 import java.nio.file.Paths;
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.Collections;
 import java.util.List;
 import java.util.Locale;
 
 import org.springframework.context.support.ReloadableResourceBundleMessageSource;
 
 import com.fasterxml.jackson.databind.ObjectMapper;
 import com.fasterxml.jackson.databind.SerializationFeature;
 
 import de.enwida.transport.Aspect;
 import de.enwida.transport.DataResolution;
 import de.enwida.web.dao.implementation.NavigationDaoImpl;
 import de.enwida.web.model.ChartNavigationData;
 import de.enwida.web.model.ProductTree;
 
 public class NavigationJsonGenerator {
 	
 	private static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
 	private static ObjectMapper objectMapper;
 	
 	static {
 		final ObjectMapperFactory objectMapperFactory = new ObjectMapperFactory(dateFormat);
 		objectMapper = objectMapperFactory.create();
 		objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
 	}
 	
     public static void main(String[] args) throws Exception {
     	ChartNavigationData navigationData;
     	
     	if (args.length == 0) {
     		navigationData = getExampleNavigation();
     	} else {
     		final String templatePath = args[0];
     		final byte[] bytes = Files.readAllBytes(Paths.get(templatePath));
     		final String template = Charset.forName("UTF-8").decode(ByteBuffer.wrap(bytes)).toString();
 
     		navigationData = getNavigationFromTemplate(template);
     	}
 	    
     	System.out.println(objectMapper.writeValueAsString(navigationData));
     }
     
     private static ChartNavigationData getNavigationFromTemplate(String templateJson) throws Exception {
     	final Template template = objectMapper.readValue(templateJson, Template.class);
     	final ChartNavigationData result = new ChartNavigationData();
     	
     	// Set flags
     	result.setIsDateScale(template.isDateScale != null ? template.isDateScale : true);
     	result.setHasLineSelection(template.hasLineSelection != null ? template.hasLineSelection : true);
     	result.setHasProductSelection(template.hasProductSelection != null ? template.hasProductSelection : true);
     	result.setHasTimeSelection(template.hasTimeSelection != null ? template.hasTimeSelection : true);
     	
     	// Set defaults
     	if (template.timeRanges == null) {
     		template.timeRanges = Arrays.asList(new String[] { "Day", "Week", "Month", "Year" });
     	}
     	if (template.aspects == null) {
     		template.aspects = Arrays.asList(Aspect.values());
     	}
     	if (template.tsos == null) {
     		template.tsos = Collections.singletonList(99);
     	}
     	if (template.products == null) {
     		template.products = Arrays.asList(new String[] { "2**", "3**" });
     	}
     	if (template.resolutions == null) {
     		template.resolutions = Arrays.asList(DataResolution.values());
     	}
     	if (template.dateFrom == null) {
     		template.dateFrom = Calendar.getInstance();
     		template.dateFrom.setTimeInMillis(0);
     	}
     	if (template.dateTo == null) {
     		template.dateTo = Calendar.getInstance();
     		template.dateTo.setTimeInMillis(Long.MAX_VALUE);
     	}
     	
     	// Set time ranges
     	for (final String timeRange : template.timeRanges) {
     		result.getTimeRanges().put(timeRange, timeRange);
     	}
     	
     	// Set aspects
     	result.getAspects().addAll(template.aspects);
     	
     	// Set defaults
     	final NavigationDefaults defaults = new NavigationDefaults();
     	defaults.setTsoId(template.tsos.get(0));
     	defaults.setResolution(template.resolutions.get(0));
     	final Calendar defaultFrom = Calendar.getInstance();
     	final Calendar defaultTo = Calendar.getInstance();
     	defaultFrom.set(2010, 0, 1);
     	defaultTo.set(2011, 0, 1);
     	defaults.setTimeRange(new CalendarRange(defaultFrom, defaultTo));
     	defaults.setProduct(Integer.parseInt(template.products.get(0).replaceAll("\\*", "1")));
    	defaults.setDisabledLines(new ArrayList<Integer>(0));
     	result.setDefaults(defaults);
     	
     	// Create product trees
     	for (final int tso : template.tsos) {
     		result.addProductTree(new ProductTree(tso));
     	}
     	
     	// Fill product trees
     	for (final String product : template.products) {
     		for (final ProductTree tree : result.getProductTrees()) {
     			ProductNode node = tree.getRoot();
     			if (product.equals("200")) {
     				final ProductNode scr = new ProductNode(2, "SCR");
     				node.addChild(scr);
    				appendZeroParts(scr, template);
     			} else if (product.equals("300")) {
     				final ProductNode tcr = new ProductNode(3, "TCR");
     				node.addChild(tcr);
    				appendZeroParts(tcr, template);
     			} else if (product.equals("2**")) {
     				final ProductNode scr = new ProductNode(2, "SCR");
     				node.addChild(scr);
     				
     				final ProductNode pt = new ProductNode(1, "PT");
     				final ProductNode opt = new ProductNode(2, "OPT");
 
     				appendLeafs(pt, template);
     				appendLeafs(opt, template);
     				scr.addChild(pt);
     				scr.addChild(opt);
     			} else if (product.equals("3**")) {
     				final ProductNode tcr = new ProductNode(3, "TCR");
     				node.addChild(tcr);
     				
     				final ProductNode t1 = new ProductNode(1, "0-4");
     				final ProductNode t2 = new ProductNode(2, "4-8");
     				final ProductNode t3 = new ProductNode(3, "8-12");
     				final ProductNode t4 = new ProductNode(4, "12-16");
     				final ProductNode t5 = new ProductNode(5, "16-20");
     				final ProductNode t6 = new ProductNode(6, "20-24");
     				
     				appendLeafs(t1, template);
     				appendLeafs(t2, template);
     				appendLeafs(t3, template);
     				appendLeafs(t4, template);
     				appendLeafs(t5, template);
     				appendLeafs(t6, template);
     				tcr.addChild(t1);
     				tcr.addChild(t2);
     				tcr.addChild(t3);
     				tcr.addChild(t4);
     				tcr.addChild(t5);
     				tcr.addChild(t6);
     			}
     		}
     	}
     	
     	return result;
     }
     
     private static void appendLeafs(ProductNode node, Template template) {
     	final ProductLeaf pos = new ProductLeaf(1, "pos");
     	final ProductLeaf neg = new ProductLeaf(2, "neg");
     	pos.setResolution(template.resolutions);
     	neg.setResolution(template.resolutions);
     	pos.setTimeRange(new CalendarRange(template.dateFrom, template.dateTo));
     	neg.setTimeRange(new CalendarRange(template.dateFrom, template.dateTo));
     	
     	node.addChild(pos);
     	node.addChild(neg);
     }
     
     private static void appendZeroParts(ProductNode node, Template template) {
 		final ProductNode wholeDay = new ProductNode(0, "AllDay");
 		node.addChild(wholeDay);
 		
 		final ProductLeaf posneg = new ProductLeaf(0, "PosNeg");
 		posneg.setResolution(template.resolutions);
 		posneg.setTimeRange(new CalendarRange(template.dateFrom, template.dateTo));
 		wholeDay.addChild(posneg);
     }
     
     private static ChartNavigationData getExampleNavigation() {
         // Manually construct the needed beans
         final ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
         messageSource.setBasename("/resources/messages/messages");
         
         final NavigationDaoImpl navigationDao = new NavigationDaoImpl();
         navigationDao.setMessageSource(messageSource);
 
         final ChartNavigationData navigationData = navigationDao.getDefaultNavigation(0, Locale.ENGLISH);
         navigationData.setIsDateScale(true);
         navigationData.getAspects().add(Aspect.CR_DEGREE_OF_ACTIVATION);
 
         navigationData.getTimeRanges().put("Day", "Day");
         navigationData.getTimeRanges().put("Week", "Week");
         navigationData.getTimeRanges().put("Month", "Month");
         navigationData.getTimeRanges().put("Year", "Year");
 
 	    try {
             navigationData.setDefaults(new NavigationDefaults(99, DataResolution.MONTHLY, 211, new CalendarRange(dateFormat.parse("2010-11-01"), dateFormat.parse("2010-12-01"))));
         } catch (ParseException e) {
             e.printStackTrace();
         }
 	    return navigationData;
     }
     
     static class Template {
     	public List<Aspect> aspects; 
     	public List<String> timeRanges;
     	public List<Integer> tsos;
     	public List<DataResolution> resolutions;
     	public List<String> products;
     	public Calendar dateFrom;
     	public Calendar dateTo;
     	
     	public Boolean isDateScale;
     	public Boolean hasLineSelection;
     	public Boolean hasProductSelection;
     	public Boolean hasTimeSelection;
     }
 
 }
