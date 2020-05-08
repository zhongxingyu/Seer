 package ui.marker;
 
 import java.util.List;
 
 import processing.core.PConstants;
 import processing.core.PGraphics;
 import util.StringCouple;
 import de.fhpotsdam.unfolding.UnfoldingMap;
 import de.fhpotsdam.unfolding.geo.Location;
 import de.fhpotsdam.unfolding.marker.SimpleLinesMarker;
 import de.fhpotsdam.unfolding.utils.MapPosition;
 import de.fhpotsdam.unfolding.utils.ScreenPosition;
 import de.looksgood.ani.Ani;
 import de.looksgood.ani.easing.Easing;
 
 public class EdgeMarker<E extends NamedMarker> extends SimpleLinesMarker {
 	
 	private static final float ANI_DURATION = .5f;
 	private static final int DEFAULT_STROKE_WEIGHT = 1;
 	
	private static final int COLOR = 0x505050FF;
	private static final int HIGHLIGHT_COLOR = 0xFFFF0000;
 	
 	private E m1, m2;
 	
 	private StringCouple id;
 	
 	private float strokeWeight;
 	private float strokeWeightTarget;
 	
 	private float weightPercentage;
 	
 	private Ani currentAnimation;
 	private Ani currentHideAnimation;
 	
 	public EdgeMarker(E m1, E m2){
 		super(m1.getLocation(), m2.getLocation());
 		this.m1 = m1;
 		this.m2 = m2;
 		
 		setColor(COLOR);
 		setHighlightColor(HIGHLIGHT_COLOR);
 		
 		weightPercentage = 1;
 		currentHideAnimation = null;
 		
 		id = new StringCouple(m1.getName(), m2.getName());
 		
 		strokeWeight = strokeWeightTarget = DEFAULT_STROKE_WEIGHT;
 		currentAnimation = null;
 	}
 	
 	public E getM1(){
 		return m1;
 	}
 	
 	public E getM2(){
 		return m2;
 	}
 	
 	public StringCouple getIds() {
 		return id;
 	}
 	
 	@Override
 	public void setHidden(boolean hidden) {
 		if (hidden == isHidden()) {
 			super.setHidden(hidden);
 			return;
 		}
 		
 		super.setHidden(hidden);
 		
 		if (hidden)
 			currentHideAnimation = Ani.to(this, ANI_DURATION, "weightPercentage", 0, Easing.QUAD_OUT);
 		else
 			// delay
 			currentHideAnimation = Ani.to(this, ANI_DURATION, ANI_DURATION, "weightPercentage", 1, Easing.QUAD_IN);
 		
 		currentHideAnimation.start();
 	}
 	
 	public void setHiddenUnanimated(boolean hidden) {
 		if (hidden == isHidden() && (currentHideAnimation == null || currentHideAnimation.isEnded()))
 			return;
 		
 		super.setHidden(hidden);
 		
 		if (!hidden) {
 			// show directly
 			if (currentHideAnimation == null || currentHideAnimation.isEnded())
 				weightPercentage = 1;
 			else {
 				currentHideAnimation.setEnd(1);
 				currentHideAnimation.end();
 				currentHideAnimation = null;
 			}
 		} else {
 			// hide with a delay, but do it instantaneously
 			if (currentHideAnimation != null && !currentHideAnimation.isEnded()) {
 				currentHideAnimation.setEnd(weightPercentage);
 				currentHideAnimation.end();
 			}
 			
 			currentHideAnimation = Ani.to(this, 0, ANI_DURATION, "weightPercentage", 0, Easing.LINEAR);
 			currentHideAnimation.start();
 		}
 	}
 	
 	@Override
 	public void setStrokeWeight(int weight) {
 		if (currentAnimation != null) {
 			currentAnimation.setEnd(strokeWeightTarget = weight);
 			currentAnimation.end();
 			return;
 		}
 		
 		strokeWeight = weight;
 		strokeWeightTarget = weight;
 	}
 	
 	public void setWidth(int width) {
 		setStrokeWeight(width);
 	}
 	
 	public void setWidthAnimated(int width) {
 		if (strokeWeightTarget == width)
 			return;
 		strokeWeightTarget = width;
 		
 		currentAnimation = Ani.to(this, ANI_DURATION, "strokeWeight", width, Easing.LINEAR, "onEnd:callback");
 		currentAnimation.start();
 	}
 	
 	@SuppressWarnings("unused")
 	private void callback() {
 		currentAnimation = null;
 	}
 	
 	@Override
 	public void setSelected(boolean selected){
 		if (selected == this.selected)
 			return;
 		
 		this.selected = selected;
 		
 		if (selected) {
 			m1.addSelectedLine();
 			m2.addSelectedLine();
 		} else {
 			m1.removeSelectedLine();
 			m2.removeSelectedLine();
 		}
 	}
 	
 	@Override
 	public boolean isInside(UnfoldingMap map, float checkX, float checkY) {
 		if (weightPercentage == 0)
 			return false;
 		
 		Location l1 = m1.getLocation();
 		Location l2 = m2.getLocation();
 		ScreenPosition  sposa = map.getScreenPosition(l1),
 						sposb = map.getScreenPosition(l2);
 		float 	xa = sposa.x,
 				xb = sposb.x,
 				ya = sposa.y,
 				yb = sposb.y;
 		if(checkX > Math.max(xa, xb)
 				|| checkX < Math.min(xa, xb)
 				|| checkY > Math.max(ya, yb)
 				|| checkY < Math.min(ya, yb)){
 			return false;
 		}
 		float	m = (ya - yb) / (xa - xb),
 				b = ya - m * xa,
 				d = (float) (Math.abs(checkY - m * checkX - b) / Math.sqrt(m*m + 1));
 		return d < strokeWeight * weightPercentage;
 	}
 	
 	@Override
 	public void draw(PGraphics pg, List<MapPosition> mapPositions) {
 		if (mapPositions.isEmpty())
 			return;
 		if (weightPercentage == 0)
 			return;
 
 		pg.pushStyle();
 		pg.noFill();
 		if (isSelected()) {
 			pg.stroke(highlightColor);
 		} else {
 			pg.stroke(color);
 		}
 		pg.strokeWeight(strokeWeight * weightPercentage);
 		pg.smooth();
 
 		pg.beginShape(PConstants.LINES);
 		MapPosition last = mapPositions.get(0);
 		for (int i = 1; i < mapPositions.size(); ++i) {
 			MapPosition mp = mapPositions.get(i);
 			pg.vertex(last.x, last.y);
 			pg.vertex(mp.x, mp.y);
 
 			last = mp;
 		}
 		pg.endShape();
 		pg.popStyle();
 	}
 
 }
