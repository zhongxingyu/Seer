 /*
 The MIT License (MIT)
 
 Copyright (c) 2013 Symantec Corporation.
 
 Permission is hereby granted, free of charge, to any person obtaining a copy of
 this software and associated documentation files (the "Software"), to deal in
 the Software without restriction, including without limitation the rights to
 use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 the Software, and to permit persons to whom the Software is furnished to do so,
 subject to the following conditions:
 
 The above copyright notice and this permission notice shall be included in all
 copies or substantial portions of the Software.
 
 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
 
 package com.symantec.gwt.flot.client;
 
 import java.util.ArrayList;
 
 import com.google.gwt.core.client.JavaScriptObject;
 import com.google.gwt.event.logical.shared.AttachEvent;
 import com.google.gwt.event.logical.shared.AttachEvent.Handler;
 import com.google.gwt.event.shared.SimpleEventBus;
 import com.google.gwt.json.client.JSONArray;
 import com.google.gwt.json.client.JSONBoolean;
 import com.google.gwt.json.client.JSONNumber;
 import com.google.gwt.json.client.JSONObject;
 import com.google.gwt.json.client.JSONString;
 import com.google.gwt.json.client.JSONValue;
 import com.google.gwt.user.client.DOM;
 import com.google.gwt.user.client.ui.DecoratedPopupPanel;
 import com.google.gwt.user.client.ui.Widget;
 import com.symantec.gwt.flot.client.bar.BarSeries;
 import com.symantec.gwt.flot.client.bar.Column;
 import com.symantec.gwt.flot.client.event.tooltip.TooltipProvider;
 import com.symantec.gwt.flot.client.line.LineSeries;
 import com.symantec.gwt.flot.client.line.LineSeriesOptions;
 import com.symantec.gwt.flot.client.line.Point;
 import com.symantec.gwt.flot.client.pie.PieSeries;
 import com.symantec.gwt.flot.client.pie.Slice;
 
 /**
  * Parent class for all types of Flot charts
  * @param <D> {@link ChartFragment}
  * @param <D>
  */
 public abstract class Flot<D extends ChartFragment> implements Plotable
 {
 	private FlotLegend legend;
 	private Widget element;
 	private String divId;
 	private ArrayList<Series<D>> series;
 	protected SeriesOptions<D> seriesOptions;
 	private DecoratedPopupPanel tooltip;
 	private SimpleEventBus eventBus;
 	protected JavaScriptObject graph;
 
 	public <C extends SeriesOptions<D>> Flot(C options)
 	{
 		this.seriesOptions = options;
 		legend = new FlotLegend();
 		tooltip = new DecoratedPopupPanel();
 		series = new ArrayList<Series<D>>();
 		eventBus = new SimpleEventBus();
 	}
 	
 	public <C extends SeriesOptions<D>> Flot(Widget element, C options)
 	{
 		this(options);
 		setParentElement(element);
 	}
 
 	protected void destroy()
 	{
 		unbindEvents();
 	}
 
 	public FlotLegend getLegend()
 	{
 		return legend;
 	}
 
 	/**
 	 * Configure Flot legend
 	 * @param legend
 	 */
 	public void setLegend(FlotLegend legend)
 	{
 		this.legend = legend;
 	}
 
 	/**
 	 * Hide/show Flot legend. This method should be called before the Flot chart renders
 	 * 
 	 * @param show 	If <code>true</code>, legend is rendered; otherwise it is not
 	 */
 	public void setShowLegend(boolean show)
 	{
 		if(legend != null)
 		{
 			legend.setVisible(show);
 		}
 	}
 
 	/**
 	 * 	Gets DOM id of the parent DOM element. Returns <code>null</code> if chart has not been plotted yet
 	 * 	@return The DOM id of the element in which the chart is rendered
 	 * 	@see #plot(Widget)
 	 */
 	public String getDivId()
 	{
 		return divId;
 	}
 	
 	
 	/**
 	 * Returns list of underlying data series for this chart <br/>
 	 * See section titled <tt>Data Format</tt> in <a href="http://flot.googlecode.com/svn/trunk/API.txt">Flot API</a>
 	 * @return list of underlying data series
 	 */
 	public ArrayList<Series<D>> getSeries()
 	{
 		return series;
 	}
 	
 	/**
 	 * Sets the underlying data series for this flot chart <br/>
 	 * See section titled <tt>Data Format</tt> in <a href="http://flot.googlecode.com/svn/trunk/API.txt">Flot API</a>
 	 * @param series List of underlying data series for this chart
 	 */
 	public void setSeries(ArrayList<? extends Series<D>> series)
 	{
 		if(this.series == null)
 		{
 			this.series = new ArrayList<Series<D>>();
 		}
 		this.series.addAll(series);
 	}
 	
 	/**
 	 * 	Adds a data series to the list of data series for this chart <br/>
 	 * 	@param s series A data series to be added
 	 * 	@see #setSeries(ArrayList)
 	 */
 	public void addSeries(Series<D> s)
 	{
 		if(series == null)
 		{
 			series = new ArrayList<Series<D>>();
 		}
 		
 		series.add(s);
 	}
 	
 	@Override
 	public void cleanup()
 	{
 		destroy();
 	}
 
 	protected JavaScriptObject getOptionsAsJson()
 	{
 		JSONObject obj = new JSONObject();
 		obj.put("color", new JSONNumber(1));
 		obj.put("series", seriesOptions.asJson());
 		obj.put("shadowSize", new JSONNumber(0));
 		obj.put("borderWidth", new JSONNumber(1));
 		obj.put("borderColor",new JSONString("#DDDDDD"));
 		if(legend != null)
 		{
 			obj.put("legend",legend.asJson());
 		}
 		obj.put("grid", getGridOptionsAsJson());
 		obj.put("font", getFontOptions());
 		
 		if(seriesOptions instanceof LineSeriesOptions)
 		{
 			LineSeriesOptions opt = (LineSeriesOptions)seriesOptions;
 			obj.put("markings", opt.getMarkingsConfig());
 		}
 		
 		JSONObject otherOptions = getOptions();
 		
 		if(otherOptions != null)
 		{
 			for(String key: otherOptions.keySet())
 			{
 				obj.put(key, otherOptions.get(key));
 			}
 		}
 		
 		return obj.getJavaScriptObject();
 	}
 	
 	protected abstract JSONObject getOptions();
 	
 	private JavaScriptObject getDataAsJson()
 	{
 		JSONArray dataArray = new JSONArray();
 		int i= 0;
 		for(Series<D> s: series)
 		{
 			dataArray.set(i++, s.asJson());
 		}
 		
 		return dataArray.getJavaScriptObject();
 	}
 
 	private JSONValue getFontOptions()
 	{
 		JSONObject fontOpts = new JSONObject();
 		fontOpts.put("size", new JSONNumber(11));
 		fontOpts.put("style", new JSONString("normal"));
 		fontOpts.put("weight", new JSONString("normal"));
 		fontOpts.put("family",new JSONString("Tahoma"));
 		return fontOpts;
 	}
 	
 	private JSONValue getGridOptionsAsJson()
 	{
 		JSONObject gridOpts = new JSONObject();
 
 		JSONObject otherOptions = seriesOptions.getGridOptions();
 		
 		if(otherOptions != null)
 		{
 			for(String key: otherOptions.keySet())
 			{
 				gridOpts.put(key, otherOptions.get(key));
 			}
 		}
 	
 		gridOpts.put("hoverable", JSONBoolean.getInstance(true));
 		gridOpts.put("clickable", JSONBoolean.getInstance(true));
 		return gridOpts;
 	}
 
 	private Widget getHoverText(int seriesIndex, JavaScriptObject object)
 	{
 		if(series!=null && series.size() > seriesIndex)
 		{
 			Series<D> s = series.get(seriesIndex);
 			
 			if(s instanceof LineSeries)
 			{
 				JSONObject lineDataObject = new JSONObject(object);
 				double x = lineDataObject.get("x").isNumber().doubleValue();
 				double y = lineDataObject.get("y").isNumber().doubleValue();
 				LineSeries l = (LineSeries)s;
 				TooltipProvider<Point> provider = l.getTooltipProvider();
 				
 				if(provider != null)
 				{
 					Point item = new Point(x, y);
 					return provider.getTooltip(item);
 				}
 			}
 			else if(s instanceof PieSeries)
 			{
 				JSONObject pieDataObject = new JSONObject(object);
 				JSONValue percentageVal = pieDataObject.get("percentage");
 
 				double percentage = percentageVal.isNumber().doubleValue();
 				PieSeries p = (PieSeries)s;
 				TooltipProvider<Slice> observer = p.getTooltipProvider();
 				if(observer != null)
 				{
 					Slice item = new Slice((float)percentage, p.getAbsoluteValue());
 					return observer.getTooltip(item);
 				}
 			}
 			else if(s instanceof BarSeries)
 			{
 				JSONObject barDataObject = new JSONObject(object);
 				JSONValue heightVal = barDataObject.get("height");
 				JSONValue colVal = barDataObject.get("position");
 
 				double height = heightVal.isNumber().doubleValue();
 				double col = colVal.isNumber().doubleValue();
 				BarSeries b = (BarSeries)s;
 				TooltipProvider<Column> observer = b.getTooltipProvider();
 				if(observer != null)
 				{
 					Column item = new Column(height,(int)col);
 					return observer.getTooltip(item);
 				}
 			}
 			
 			
 		}
 		return null;
 	}
 	
 	protected void afterPlot(){}
 	protected void onCleanup(){}
 	
 	private void fireClickEvent(int seriesIndex, JavaScriptObject object)
 	{
 		if(series!= null && series.size() > seriesIndex)
 		{
 			Series<D> s = series.get(seriesIndex);
 			
 			if(s instanceof LineSeries)
 			{
 				JSONObject lineDataObject = new JSONObject(object);
 				double x = lineDataObject.get("x").isNumber().doubleValue();
 				double y = lineDataObject.get("y").isNumber().doubleValue();
 				LineSeries l = (LineSeries)s;
 				
 				Point item = new Point(x, y);
 				l.fireClickEvent(item);
 			}
 			else if(s instanceof PieSeries)
 			{
 				JSONObject pieDataObject = new JSONObject(object);
 				JSONValue percentageVal = pieDataObject.get("percentage");
 
 				double percentage = percentageVal.isNumber().doubleValue();
 				PieSeries p = (PieSeries)s;
 				Slice item = new Slice((float)percentage, p.getAbsoluteValue());
 				p.fireClickEvent(item);
 			}
 			else if(s instanceof BarSeries)
 			{
 				JSONObject barDataObject = new JSONObject(object);
 				JSONValue heightVal = barDataObject.get("height");
 				JSONValue colVal = barDataObject.get("position");
 				JSONValue xVal = barDataObject.get("x");
 
 				double height = heightVal.isNumber().doubleValue();
 				double col = colVal.isNumber().doubleValue();
 				double x = xVal.isNumber().doubleValue();
 				BarSeries b = (BarSeries)s;
 				Column item = new Column(height, x);
 				item.setPosition((int)col);
 				b.fireClickEvent(item);
 			}
 		}
 	}
 	
 	public final void plot(Widget element)
 	{
 		setParentElement(element);
 		__plot__();
 	}
 
 	private final void setParentElement(Widget element)
 	{
 		this.element = element;
 		if(element.getElement().getId() == null || element.getElement().getId().trim().length() == 0)
 		{
 			element.getElement().setId(DOM.createUniqueId());
 		}
 		this.divId = element.getElement().getId();
 		element.addAttachHandler(new Handler() {
 			
 			@Override
 			public void onAttachOrDetach(AttachEvent event) {
 				
 				if(!event.isAttached())
 				{
 					destroy();
 				}
 			}
 		});
 	}
 	
 	private final native void __plot__() /*-{
 		
 		var self = this;
 		var selfDiv = $wnd.$('#'+self.@com.symantec.gwt.flot.client.Flot::divId);
 		var previousPoint = null;
 		
 		var tooltipHandler = function (event, pos, item) {
 			
             if (item) {
             	
                 if (true || previousPoint != item.dataIndex) {
                     previousPoint = item.dataIndex;
                     
                     if(self.@com.symantec.gwt.flot.client.Flot::tooltip.@com.google.gwt.user.client.ui.DecoratedPopupPanel::isShowing()())
                     {
                     	self.@com.symantec.gwt.flot.client.Flot::tooltip.@com.google.gwt.user.client.ui.DecoratedPopupPanel::hide()();
                     }
                     var x = 0
                     ,	y = 0
                     ,	col = 0
                     ,	percentage = 0
                     ,	tooltipWidget
                     ,	seriesIndex = item.seriesIndex;
                     
                     if(item.series.percent)
                     {
                     	percentage = parseFloat(item.series.percent);
                     }
                     else if(item.datapoint)
                     {
                     	x = item.datapoint[0];
                 		y = item.datapoint[1];
                 		col = item.dataIndex;
                     }
                     
                     var valunit = '';
                     eventData = {'x':x,'y':y,'percentage':percentage,'height':y, 'position':col};
                     tooltipWidget = self.@com.symantec.gwt.flot.client.Flot::getHoverText(ILcom/google/gwt/core/client/JavaScriptObject;)(seriesIndex,eventData);
                   	
                    
             	   if(tooltipWidget != null && tooltipWidget != undefined)
             	   {
                    		self.@com.symantec.gwt.flot.client.Flot::tooltip.@com.google.gwt.user.client.ui.DecoratedPopupPanel::setWidget(Lcom/google/gwt/user/client/ui/Widget;)(tooltipWidget); 
                    		self.@com.symantec.gwt.flot.client.Flot::tooltip.@com.google.gwt.user.client.ui.DecoratedPopupPanel::show()();
                    		self.@com.symantec.gwt.flot.client.Flot::tooltip.@com.google.gwt.user.client.ui.DecoratedPopupPanel::setPopupPosition(II)(pos.pageX+7, pos.pageY+7);
             	   }
                 }
             }
             else {
                 self.@com.symantec.gwt.flot.client.Flot::tooltip.@com.google.gwt.user.client.ui.DecoratedPopupPanel::hide()();
                 previousPoint = null;            
             }
     	};
     	
     	var clickHandler = function (event, pos, item) {
 			
             if (item) 
             {
                 var x = 0
                 ,	y = 0
                 ,	col = 0
                 ,	percentage = 0
                 ,	eventData
                 ,	seriesIndex = item.seriesIndex;
                 
                 if(item.series.percent)
                 {
                 	percentage = parseFloat(item.series.percent);
                 }
                 else if(item.datapoint)
                 {
                 	x = item.datapoint[0];
                 	y = item.datapoint[1];
                 	col = item.dataIndex;
                 }
                 
                 
                 eventData = {'x':x,'y':y,'percentage':percentage,'height':y, 'position':x, 'position':col};
                 self.@com.symantec.gwt.flot.client.Flot::fireClickEvent(ILcom/google/gwt/core/client/JavaScriptObject;)(seriesIndex, eventData);
               	
             }
     	};
     	
     	var flotOptions = self.@com.symantec.gwt.flot.client.Flot::getOptionsAsJson()();
     	var flotData = self.@com.symantec.gwt.flot.client.Flot::getDataAsJson()();
     	var graph = $wnd.$.plot(selfDiv, flotData, flotOptions);
     	self.@com.symantec.gwt.flot.client.Flot::graph = graph;
     	
     	selfDiv.on("plothover", tooltipHandler);
     	selfDiv.on("plotclick", clickHandler);
     	
 		self.@com.symantec.gwt.flot.client.Flot::afterPlot()();
     	
 		
 	}-*/;
 	
 	private native void unbindEvents()/*-{
 	
 		var self = this;
 		var selfDiv = $wnd.$('#'+self.@com.symantec.gwt.flot.client.Flot::divId);
 		selfDiv.off("plothover");
 		selfDiv.off("plotclick");
     	
     	self.@com.symantec.gwt.flot.client.Flot::onCleanup()();
     	
 	}-*/;
 	
 	/**
 	 * 	Returns the current plot options for this chart <br/>
 	 * 	See section titled <tt>Plot Options</tt> in <a href="http://flot.googlecode.com/svn/trunk/API.txt">Flot API</a>
 	 * 	@return plot options
 	 */
 	public SeriesOptions<D> getSeriesOptions() {
 		return seriesOptions;
 	}
 
 	/**
 	 * 	Sets the plot options for this chart <br/>
 	 * 	See section titled <tt>Plot Options</tt> in <a href="http://flot.googlecode.com/svn/trunk/API.txt">Flot API</a>
 	 * 	@param seriesOptions plot options
 	 */
 	public void setSeriesOptions(SeriesOptions<D> seriesOptions) {
 		this.seriesOptions = seriesOptions;
 	}
 
 	/**
 	 * Renders the specified html inside the 
 	 * @param emptyHtml
 	 */
 	public void showEmptyHtml(String emptyHtml)
 	{
 		cleanup();
 		if(divId != null)
 		{
 			DOM.getElementById(divId).setInnerHTML(emptyHtml);
 		}
 	}
 
 }
