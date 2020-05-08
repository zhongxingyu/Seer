 package nl.tue.fingerpaint.client.gui;
 
 import nl.tue.fingerpaint.client.gui.animation.SizeAnimation;
 
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.VerticalPanel;
 
 /**
  * <p>This button is used to show and hide the menu panel in the application.</p>
  * 
  * @author Group Fingerpaint
  */
 public class MenuToggleButton extends Button implements ClickHandler {
 
 	/** The ID of the button element. */
 	public static final String ELEMENT_ID = "menuToggleButton";
 	
 	/** Text that is shown in the button when the menu is visible and can be hidden. */
 	public static final String HIDE_TEXT = "-";
 	/** Text that is shown in the button when the menu is not visible and can be shown. */
 	public static final String SHOW_TEXT = "+";
 	
 	/** How long the sliding animation takes. */
 	public static final int DURATION = 1000;
 	
 	/** Indicate if the menu is shown or not. */
 	protected boolean shown = true;
 	/** The MenuPanel that is shown/hidden. */
	protected VerticalPanel menuPanel;
 	/** Animation to hide menu panel. */
 	protected SizeAnimation menuAnimation;
 	
 	/**
 	 * Construct a new {@link MenuToggleButton}.
 	 * 
	 * @param menuPanel The element to be animated.
 	 */
	public MenuToggleButton(VerticalPanel menuPanel) {
 		super();
 		
		this.menuPanel = menuPanel;
 		addClickHandler(this);
 		setText(HIDE_TEXT);
 		getElement().setId(ELEMENT_ID);
		menuAnimation = new SizeAnimation(menuPanel.getElement(), SizeAnimation.ANIMATE_WIDTH);
 	}
 
 	@Override
 	public void onClick(ClickEvent event) {
 		if (shown) {
 			setText(SHOW_TEXT);
 			menuAnimation.doHide(DURATION);
 		} else {
 			setText(HIDE_TEXT);
 			menuAnimation.doShow(DURATION);
 		}
 		
 		// Actually toggle state
 		shown = !shown;
 	}
 	
 }
