 package com.eagerlogic.cubee.client.components;
 
 import com.eagerlogic.cubee.client.events.IEventListener;
 import com.eagerlogic.cubee.client.events.MouseDownEventArgs;
 import com.eagerlogic.cubee.client.events.MouseEventTypes;
 import com.eagerlogic.cubee.client.events.MouseUpEventArgs;
 import com.eagerlogic.cubee.client.properties.AExpression;
 import com.eagerlogic.cubee.client.properties.BackgroundProperty;
 import com.eagerlogic.cubee.client.properties.BooleanProperty;
 import com.eagerlogic.cubee.client.properties.BorderProperty;
 import com.eagerlogic.cubee.client.properties.ColorProperty;
 import com.eagerlogic.cubee.client.properties.IntegerProperty;
 import com.eagerlogic.cubee.client.properties.PaddingProperty;
 import com.eagerlogic.cubee.client.properties.Property;
 import com.eagerlogic.cubee.client.properties.StringProperty;
 import com.eagerlogic.cubee.client.styles.Border;
 import com.eagerlogic.cubee.client.styles.Color;
 import com.eagerlogic.cubee.client.styles.ColorStop;
 import com.eagerlogic.cubee.client.styles.ECursor;
 import com.eagerlogic.cubee.client.styles.ETextAlign;
 import com.eagerlogic.cubee.client.styles.ETextOverflow;
 import com.eagerlogic.cubee.client.styles.FontFamily;
 import com.eagerlogic.cubee.client.styles.LinearGradient;
 import com.eagerlogic.cubee.client.styles.Padding;
 
 /**
  *
  * @author dipacs
  */
 public final class Button extends AUserControl {
 
 	private final Label label;
 
 	public Button() {
 		this.paddingProperty().set(new Padding(5));
 		this.cursorProperty().set(ECursor.POINTER);
 		this.borderProperty().set(new Border(1, Color.getRgbColor(0x808080), 5));
 		LinearGradient lg = new LinearGradient(0.0, new ColorStop(0.0, Color.getRgbColor(0xe0e0e0)),
 				new ColorStop(1.0, Color.getRgbColor(0xc0c0c0)));
 		this.backgroundProperty().set(lg);
 
 		label = new Label();
 		label.textOverflowProperty().set(ETextOverflow.ELLIPSIS);
 		label.widthProperty().bind(new AExpression<Integer>() {
 			@Override
 			public Integer calculate() {
 				if (widthProperty().get() == null) {
 					return null;
 				} else {
 					return widthProperty().get();
 				}
 			}
 		});
 		label.textAlignProperty().set(ETextAlign.CENTER);
 		label.pointerTransparentProperty().set(Boolean.TRUE);
 		label.selectableProperty().set(Boolean.FALSE);
 		this.getChildren().add(label);
 
 		this.onMouseDownEvent().addListener(new IEventListener<MouseDownEventArgs>() {
 			@Override
 			public void onFired(MouseDownEventArgs args) {
 				alphaProperty().set(0.5);
 			}
 		});
 		this.onMouseUpEvent().addListener(new IEventListener<MouseUpEventArgs>() {
 			@Override
 			public void onFired(MouseUpEventArgs args) {
 				alphaProperty().set(1.0);
 			}
 		});
 	}
 
 	@Override
 	public IntegerProperty widthProperty() {
 		return super.widthProperty();
 	}
 
 	@Override
 	public IntegerProperty heightProperty() {
 		return super.heightProperty();
 	}
 
 	@Override
 	public BackgroundProperty backgroundProperty() {
 		return super.backgroundProperty();
 	}
 
 	@Override
 	public PaddingProperty paddingProperty() {
 		return super.paddingProperty();
 	}
 
 	@Override
 	public BorderProperty borderProperty() {
 		return super.borderProperty();
 	}
 
 	public final StringProperty textProperty() {
 		return label.textProperty();
 	}
 
 	public final Property<ETextOverflow> labelTextOverflowProperty() {
 		return label.textOverflowProperty();
 	}
 
 	public ColorProperty labelForeColorProperty() {
 		return label.foreColorProperty();
 	}
 
 	public BooleanProperty labelBoldProperty() {
 		return label.boldProperty();
 	}
 
 	public BooleanProperty labelItalicProperty() {
 		return label.italicProperty();
 	}
 
 	public BooleanProperty labelUnderlineProperty() {
 		return label.underlineProperty();
 	}
 
 	public Property<ETextAlign> labelTextAlignProperty() {
 		return label.textAlignProperty();
 	}
 
 	public IntegerProperty labelFontSizeProperty() {
 		return label.fontSizeProperty();
 	}
 
 	public Property<FontFamily> labelFontFamilyProperty() {
 		return label.fontFamilyProperty();
 	}
 }
