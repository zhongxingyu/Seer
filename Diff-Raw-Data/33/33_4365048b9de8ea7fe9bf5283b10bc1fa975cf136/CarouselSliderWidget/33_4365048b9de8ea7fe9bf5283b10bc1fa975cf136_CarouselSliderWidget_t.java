 package com.pronoiahealth.olhie.client.pages.bulletinboard.widgets;
 
 import static com.google.gwt.query.client.GQuery.$;
 
 import javax.annotation.PostConstruct;
 
 import org.jboss.errai.ui.shared.api.annotations.DataField;
 import org.jboss.errai.ui.shared.api.annotations.Templated;
 
 import com.google.gwt.query.client.Function;
 import com.google.gwt.query.client.GQuery;
 import com.google.gwt.user.client.DOM;
 import com.google.gwt.user.client.Element;
 import com.google.gwt.user.client.Timer;
 import com.google.gwt.user.client.ui.Composite;
 
 @Templated("#root")
 public class CarouselSliderWidget extends Composite {
 	final int transition_time = 1000; // 1 second
 	final int time_between_slides = 4000; // 4 seconds
 
 	@DataField("carouselContainer")
 	private Element carouselContainer = DOM.createDiv();
 
 	private Timer effectTimer;
 
 	public CarouselSliderWidget() {
 	}
 
 	@PostConstruct
 	private void postConstruct() {
		
		
 		attachedGQuery();
 	}
 
 	private void attachedGQuery() {
 		// settings
 		final GQuery slider = $(".ph-carousel-slider", carouselContainer);
 		final String slide = "li"; // could also use 'img' if you're not using a
 
 		// Get slides and fade all of them out immediately
 		final GQuery slides = slider.find(slide);
 		slides.fadeOut(0, (Function[]) null);
 
 		// Set the first slide active
 		GQuery firstSlide = slides.first();
 		firstSlide.addClass("active");
 		firstSlide.fadeIn(transition_time, (Function[]) null);
 
 		// Start a timer to fade the slides in and out
 		effectTimer = new Timer() {
 			@Override
 			public void run() {
 				// slides.stop();
 				int i = slides
 						.index(slider.find(slide + ".active").elements()[0]);
 				slides.eq(i).removeClass("active");
 				slides.eq(i).fadeOut(transition_time);
 				if (slides.length() == (i + 1)) {
 					i = -1; // loop to start
 				}
 				slides.eq(i + 1).fadeIn(transition_time);
 				slides.eq(i + 1).addClass("active");
 			}
 		};
 	}
 
 	/**
 	 * Start the timer when the widget loads
 	 * 
 	 * @see com.google.gwt.user.client.ui.Widget#onLoad()
 	 */
 	@Override
 	protected void onLoad() {
 		super.onLoad();
 		effectTimer.scheduleRepeating(time_between_slides + transition_time);
 	}
 
 	/**
 	 * Cancel the timer
 	 * 
 	 * @see com.google.gwt.user.client.ui.Widget#onUnload()
 	 */
 	@Override
 	protected void onUnload() {
 		super.onUnload();
 
 		// Cancel timer
 		effectTimer.cancel();
 	}
 }
