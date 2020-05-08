 package wicket.contrib.markup.html.tooltip;
 
 import wicket.markup.ComponentTag;
 import wicket.markup.MarkupStream;
 import wicket.markup.html.HtmlHeaderContainer;
import wicket.markup.html.PackageResourceReference;
 import wicket.markup.html.WebComponent;
import wicket.markup.html.WebMarkupContainer;
 import wicket.markup.html.panel.Panel;
 import wicket.markup.html.resources.JavaScriptReference;
 import wicket.model.IModel;
 import wicket.model.Model;
 import wicket.util.value.ValueMap;
 import wicket.AttributeModifier;
 import wicket.Component;
 
 /**
  * 
  * 
  * based on textsoft.it's multy-line html tooltip tutorial: 
  * <a href="http://www.texsoft.it/index.php?c=software&m=sw.js.htmltooltip&l=it">texsoft.it's Tooltip tutoroial</a><br/>
  * 
  * In short this is a fully customizable Javascript-HTML-Layout Wicket Tooltip.
  * What does it do? Well you make a MVOTooltip.java (My Very Own Tooltip)<br/>
  * which extends Tooltip.java, and write the corresponding MVOTooltip.html<br/> 
  * as if it were the HTML for a panel, you can make you're very own cusomized tooltip.<br/> 
  *
  * Note: Using setter methods afeter construction probably wont do much good, <br/>
  * because instance fields are used to render AttributeModifiers in the initTooltip() method.<br/>
  *
  *for usage examples see: <br/>
  *<a href="http://www.jroller.com/page/ruudmarco?entry=tooltip_tutioral_part_one">Tutorial 1: Static Tooltip</a><br/>
  *<a href="http://www.jroller.com/comments/ruudmarco/Weblog/tooltip_tutioral_part_2_dynamic">Tutorial 2: Dynamic Tooltip</a>
  * @author Marco & Ruud
  */
 
 public class Tooltip extends Panel
 {
 	private final Component target; //component which tooltip should react to
 	private final int offsetX; // X offset from target's upperleft corner
 	private final int offsetY; //Y offset from target's top
 	
 	//z-index for HTML comopnent. Default = 2. 
 	//Can be set to custom value if user's page requires tooltip to have higher z-index.
 	private final int zIndex = 2;
 
 	private final String id;
 	
 	/**
 	 * Constructor with default X and Y offset 
 	 * @param id component id
 	 * @param target target component bound to Tooltip
 	 */
 	public Tooltip(String id, Component target)
 	{
 		super(id);
 		this.id = id;
 		
 		//default offset. 
 		this.offsetX = 10;
 		this.offsetY = 20;
 		this.target = target;
 		
 		initTooltip();
 	}
 	
 	/**
 	 * Constructor with default X and Y offset and Model object 
 	 * @param id component id
 	 * @param target target component bound to Tooltip
 	 * @param model Model to set for the Panel
 	 */	
 	public Tooltip(String id, IModel model, Component target)
 	{
 		super(id, model);
 		this.id = id;
 		
 		//default offset. 
 		this.offsetX = 10;
 		this.offsetY = 20;
 		this.target = target;
 		
 		initTooltip();
 	}
 	
 	/**
 	 * constructor with custom x and y offsets
 	 * NOTE: IE seems to take y as the offset from target's top and FF from target's bottom
 	 * @param id Component id
 	 * @param target Target component bound to Tooltip
 	 * @param x X offset from target's upperleft corner
 	 * @param y Y offset from target's upperleft corner
 	 *
 	 */
 	public Tooltip(String id, Component target, int x, int y)
 	{
 		super(id);
 		this.id =id;
 		this.offsetX = x;
 		this.offsetY = y;
 		this.target = target;
 		
 		initTooltip();
 	}
 	
 	/**
 	 * constructor with custom x and y offsets and Model object
 	 * @param id Component id
 	 * @param target Target component bound to Tooltip
 	 * @param x X offset from target's upperleft corner
 	 * @param y Y offset from target's upperleft corner
 	 * 
 	 * NOTE: IE seems to take y as the offset from target's top and FF from target's bottom
 	 */
 	public Tooltip(String id, IModel model, Component target, int x, int y)
 	{
 		super(id, model);
 		this.id =id;
 		this.offsetX = x;
 		this.offsetY = y;
 		this.target = target;
 		
 		initTooltip();
 	}
 	
 	/**
 	 * innitializes the tooltip
 	 * to be called from constructors to avoid redundant code.
 	 */
 	private void initTooltip()
 	{
 		
 		String targetHTMLID = target.getPath();
 		String HTMLID = "id" + "_" + targetHTMLID;
 		
 		//add the javascript file
	
		
 		add(new JavaScriptReference("tooltipMain", Tooltip.class, "tooltip.js"));
 		
 		//add id attributes for javascript identification to both tooltip and parent
 		add(new AttributeModifier("id" , true, new Model(HTMLID)));
 		target.add(new AttributeModifier("id", true, new Model(targetHTMLID)));
 		
 		//add necessary style to component tag
 		add(new AppendAttributeModifier("style", true, new Model("visibility:hidden; z-index:2; position:absolute")));
 		
 		//add mousehandler functions to target
 		AppendAttributeModifier onMouseOverMod = new AppendAttributeModifier("onmouseover", true, new Model("xstooltip_show('" + HTMLID + "', '" + targetHTMLID + "', " + offsetX + ", " + offsetY + ");"));  
 		AppendAttributeModifier onMouseOutMod = new AppendAttributeModifier("onmouseout", true, new Model("xstooltip_hide('" + HTMLID + "');"));
 		target.add(onMouseOverMod);
 		target.add(onMouseOutMod);
 	}
 	
 	/**
 	 * @return x-offset
 	 */
 	public int getOffsetX()
 	{
 		return this.offsetX;
 	}
 
 	/**
 	 * @return y-offset
 	 */
 	public int getOffsetY()
 	{
 		return this.offsetY;
 	}
 
 	/**
 	 * @return z-index
 	 */
 	public int getZIndex()
 	{
 		return this.zIndex;
 	}
 
 	/**
 	 * @return target Component
 	 */
 	public Component getTarget()
 	{
 		return target;
 	}
 	
 	/**
 	 * AttributeModifier that appends the new value to the current value if an old value
 	 * exists. If it does not exist, it sets the new value.
 	 * @author Ruud Booltink
 	 * @author Marco van de Haar
 	 */
 	private final static class AppendAttributeModifier extends AttributeModifier
 	{
 		public AppendAttributeModifier(String attribute, boolean addAttributeIfNotPresent, IModel replaceModel) {
 			super(attribute, addAttributeIfNotPresent, replaceModel);
 		}
 
 		public AppendAttributeModifier(String attribute, IModel replaceModel) {
 			super(attribute, replaceModel);
 		}
 
 		public AppendAttributeModifier(String attribute, String pattern, boolean addAttributeIfNotPresent, IModel replaceModel) {
 			super(attribute, pattern, addAttributeIfNotPresent, replaceModel);
 		}
 
 		public AppendAttributeModifier(String attribute, String pattern, IModel replaceModel) {
 			super(attribute, pattern, replaceModel);
 		}
 
 		protected String newValue(String currentValue, String replacementValue) {
 			return (currentValue==null?"":currentValue) + replacementValue;
 		}
 	}
 
 }
