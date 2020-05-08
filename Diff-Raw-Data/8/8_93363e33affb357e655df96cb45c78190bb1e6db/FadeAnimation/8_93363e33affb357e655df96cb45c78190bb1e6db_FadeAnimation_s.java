 /**
  * 
  */
 package org.cotrix.web.common.client.util;
 
 /**
  * @author "Federico De Faveri federico.defaveri@fao.org"
  *
  */
 import java.math.BigDecimal;
 
 import com.google.gwt.animation.client.Animation;
 import com.google.gwt.user.client.Element;
 
 /**
  * Adapted from http://map-notes.blogspot.it/2012/11/fade-animation.html
  * @author "Federico De Faveri federico.defaveri@fao.org"
  *
  */
 public class FadeAnimation extends Animation {
 	
 	public enum Speed {
 		IMMEDIATE(0),
 		VERY_FAST(500),
 		FAST(1000),
 		NORMAL(1500),
 		SLOW(2000);
 		
 		private int time;
 
 		/**
 		 * @param time
 		 */
 		private Speed(int time) {
 			this.time = time;
 		}
 
 		/**
 		 * @return the time
 		 */
 		public int getTime() {
 			return time;
 		}		
 	}
 	
 	public static final double INVISIBLE_OPACITY = 0.0;
 	public static final double VISIBLE_OPACITY = 1.0;
 	
 	private Element element;
 	private double opacityIncrement;
 	private double targetOpacity;
 	private double baseOpacity;
 	
 	private double visibleOpacity;
 	private double invisibleOpacity;
 
 	public FadeAnimation(Element element) {
 		this.element = element;
 		this.visibleOpacity = VISIBLE_OPACITY;
 		this.invisibleOpacity = INVISIBLE_OPACITY;
 	}
 	
 	public FadeAnimation(Element element, double visibleOpacity, double invisibleOpacity) {
 		this.element = element;
 		this.visibleOpacity = visibleOpacity;
 		this.invisibleOpacity = invisibleOpacity;
 	}
 
 	@Override
 	protected void onUpdate(double progress) {
 		element.getStyle().setOpacity(baseOpacity + progress * opacityIncrement);
 	}
 
 	@Override
 	protected void onComplete() {
 		super.onComplete();
 		element.getStyle().setOpacity(targetOpacity);
 	}
 	
 	public void setVisibility(boolean visible, Speed speed)
 	{
 		if (!(visible ^ isElementVisible())) return;
 		if (visible) fadeIn(speed);
 		else fadeOut(speed);
 	}
 	
 	public boolean isElementVisible() {
 		String opacityValue = element.getStyle().getOpacity();
 		if (opacityValue == null) return true;
 		try {
 			double opacity = new BigDecimal(opacityValue).doubleValue();
 			return opacity == visibleOpacity;
 		} catch(NumberFormatException e) {
 			return true;
 		}
 	}
 	
 	public void fadeOut(Speed speed)
 	{
 		cancel();
 		element.getStyle().setOpacity(1);
 		fade(speed.getTime(), invisibleOpacity);
 	}
 	
 	public void fadeIn(Speed speed)
 	{
 		fadeIn(invisibleOpacity, speed);
 	}
 	
 	public void fadeIn(double startingOpacity, Speed speed)
 	{
 		cancel();
 		element.getStyle().setOpacity(startingOpacity);
 		fade(speed.getTime(), 1);
 	}
 
 	public void fade(int duration, double targetOpacity) {
 		if(targetOpacity > 1.0) {
 			targetOpacity = 1.0;
 		}
 		if(targetOpacity < 0.0) {
 			targetOpacity = 0.0;
 		}
 		this.targetOpacity = targetOpacity;
 		String opacityStr = element.getStyle().getOpacity();
 		try {
 			baseOpacity = new BigDecimal(opacityStr).doubleValue();
 			opacityIncrement = targetOpacity - baseOpacity;
 			run(duration);
 		} catch(NumberFormatException e) {
 			// set opacity directly
 			onComplete();
 		}
 	}
 
 }
