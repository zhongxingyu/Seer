 package com.eagerlogic.cubee.client.components;
 
 import com.eagerlogic.cubee.client.events.ClickEventArgs;
 import com.eagerlogic.cubee.client.events.IEventListener;
 import com.eagerlogic.cubee.client.properties.AExpression;
 import com.eagerlogic.cubee.client.properties.BooleanProperty;
 import com.eagerlogic.cubee.client.properties.IChangeListener;
 import com.eagerlogic.cubee.client.properties.IntegerProperty;
 import com.eagerlogic.cubee.client.styles.Color;
 import com.eagerlogic.cubee.client.styles.ColorBackground;
 
 /**
  *
  * @author dipacs
  */
 public abstract class APopup {
 	
 	private final boolean modal;
 	private final boolean autoClose;
 	private final Color glassColor;
 	
 	private final IntegerProperty translateX = new IntegerProperty(0, false, false);
 	private final IntegerProperty translateY = new IntegerProperty(0, false, false);
 	private final BooleanProperty center = new BooleanProperty(false, false, false);
 	
 	private Panel popupRoot;
 	private Panel rootComponentContainer;
 	private AComponent rootComponent;
 	
 	private boolean visible = false;
 	
 	public APopup() {
 		this(true, true, Color.getArgbColor(0x00000000));
 	}
 	
 	public APopup(boolean modal, boolean autoClose) {
 		this(modal, autoClose, Color.getArgbColor(0x00000000));
 	}
 
 	public APopup(boolean modal, boolean autoClose, Color glassColor) {
 		this.modal = modal;
 		this.autoClose = autoClose;
 		this.glassColor = glassColor;
 		this.popupRoot = new Panel();
 		if (glassColor != null) {
 			this.popupRoot.backgroundProperty().set(new ColorBackground(glassColor));
 		}
 		if (modal || autoClose) {
 			this.popupRoot.getElement().getStyle().setProperty("pointerEvents", "all");
 		} else {
 			this.popupRoot.getElement().getStyle().setProperty("pointerEvents", "none");
 		}
 		this.popupRoot.widthProperty().bind(CubeePanel.getInstance().boundsWidthProperty());
 		this.popupRoot.heightProperty().bind(CubeePanel.getInstance().boundsHeightProperty());
 		
 		this.rootComponentContainer = new Panel();
 		this.rootComponentContainer.translateXProperty().bind(new AExpression<Integer>() {
 			
 			{
 				this.bind(center, popupRoot.boundsWidthProperty(), translateX, 
 						rootComponentContainer.boundsWidthProperty());
 			}
 
 			@Override
 			public Integer calculate() {
 				int baseX = 0;
 				if (center.get()) {
 					baseX = (popupRoot.clientWidthProperty().get() - rootComponentContainer.boundsWidthProperty().get()) / 2;
 				}
 				return baseX + translateX.get();
 			}
 		});
 		this.rootComponentContainer.translateYProperty().bind(new AExpression<Integer>() {
 			
 			{
 				this.bind(center, popupRoot.boundsHeightProperty(), translateY, 
 						rootComponentContainer.boundsHeightProperty());
 			}
 
 			@Override
 			public Integer calculate() {
 				int baseY = 0;
 				if (center.get()) {
 					baseY = (popupRoot.boundsHeightProperty().get() - rootComponentContainer.boundsHeightProperty().get()) / 2;
 				}
 				return baseY + translateY.get();
 			}
 		});
 		this.popupRoot.getChildren().add(rootComponentContainer);
 		
 		if (autoClose) {
 			this.popupRoot.onClickEvent().addListener(new IEventListener<ClickEventArgs>() {
 
 				@Override
 				public void onFired(ClickEventArgs args) {
 					close();
 				}
 			});
 		}
 	}
 
 	final Panel getPopupRoot() {
 		return popupRoot;
 	}
 
 	protected AComponent getRootComponent() {
 		return rootComponent;
 	}
 
 	protected void setRootComponent(AComponent rootComponent) {
 		this.rootComponentContainer.getChildren().clear();
 		this.rootComponent = null;
 		if (rootComponent != null) {
 			this.rootComponentContainer.getChildren().add(rootComponent);
 		}
 		this.rootComponent = rootComponent;
 	}
 	
 	protected void show() {
 		if (visible) {
 			throw new IllegalStateException("This popup is already shown.");
 		}
 		CubeePanel.getInstance().showPopup(this);
 		this.visible = true;
 	}
 	
 	protected void close() {
 		if (!visible) {
 			throw new IllegalStateException("This popup isn't shown.");
 		}
 		CubeePanel.getInstance().closePopup(this);
 		this.visible = false;
 		onClosed();
 	}
 	
 	protected void onClosed() {
 		
 	}
 
 	public final boolean isModal() {
 		return modal;
 	}
 
 	public final boolean isAutoClose() {
 		return autoClose;
 	}
 
 	public final Color getGlassColor() {
 		return glassColor;
 	}
 
	protected IntegerProperty translateXProperty() {
 		return translateX;
 	}
 
	protected IntegerProperty translateYProperty() {
 		return translateY;
 	}
 
 	protected BooleanProperty centerProperty() {
 		return center;
 	}
 	
 }
