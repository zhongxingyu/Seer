 package jbookreader.formatengine.impl;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.ListIterator;
 
 import jbookreader.formatengine.IAdjustableDrawable;
 import jbookreader.formatengine.ICompositor;
 import jbookreader.rendering.IDrawable;
 import jbookreader.rendering.IGraphicDriver;
 import jbookreader.rendering.Position;
 import jbookreader.style.Alignment;
 
 
 public class SimpleCompositor implements ICompositor {
 	public List<IDrawable> compose(List<IDrawable> particles, int width, Alignment alignment, IGraphicDriver driver) {
 		List<IDrawable> result = new ArrayList<IDrawable>();
 		List<IDrawable> line = new ArrayList<IDrawable>();
 		float currentWidth = 0;
 		for (IDrawable d: particles) {
 			if (line.isEmpty()) {
 				line.add(d);
 				currentWidth = d.getWidth(Position.START);
 			} else if (currentWidth + d.getWidth(Position.END) > width) {
 				// we can't add current element as last or middle
 				IDrawable last = line.get(line.size()-1);
 				currentWidth = currentWidth
 							- last.getWidth(Position.MIDDLE)
 							+ last.getWidth(Position.END);
 
 				// flush line
 				result.add(makeHBox(driver, line, width, alignment, false));
 				line.clear();
 
 				line.add(d);
 				currentWidth = d.getWidth(Position.START);
 			} else if (currentWidth + d.getWidth(Position.MIDDLE) > width) {
 				// can't add the element in the middle, but it could be last item
 				line.add(d);
 				currentWidth += d.getWidth(Position.END);
 
 				// flush line
 				result.add(makeHBox(driver, line, width, alignment, false));
 				line.clear();
 				currentWidth = 0;
 			} else {
 				line.add(d);
 				currentWidth += d.getWidth(Position.MIDDLE);
 			}
 		}
 		if (!line.isEmpty()) {
 			IDrawable last = line.get(line.size()-1);
 			currentWidth = currentWidth
 						- last.getWidth(Position.MIDDLE)
 						+ last.getWidth(Position.END);
 
 			// flush line
 			result.add(makeHBox(driver, line, width, alignment, true));
 			line.clear();
 		}
 		return result;
 	}
 
 	private IDrawable makeHBox(IGraphicDriver driver, List<IDrawable> line, float width, Alignment alignment, boolean last) {
 		HBox hbox;
 		switch (alignment) {
 		case JUSTIFY:
 		{
 			hbox = makeJustifiedHBox(line, width, last);
 			float boxWidth = hbox.getWidth(Position.MIDDLE);
 			float defect = width - boxWidth;
 			if (defect != 0) {
 				HBox wrapperBox = new HBox();
 				wrapperBox.add(hbox);
 				wrapperBox.add(new SimpleWhitespace(driver, defect));
 				hbox = wrapperBox;
 			}
 		}
 		break;
 		case LEFT:
 		{
 			hbox = new HBox();
 			hbox.addAll(line);
 			float boxWidth = hbox.getWidth(Position.MIDDLE);
 			float defect = width - boxWidth;
 			if (defect != 0) {
 				HBox wrapperBox = new HBox();
 				wrapperBox.add(hbox);
 				wrapperBox.add(new SimpleWhitespace(driver, defect));
 				hbox = wrapperBox;
 			}
 		}
 		break;
 		case RIGHT:
 		{
 			hbox = new HBox();
 			hbox.addAll(line);
 			float boxWidth = hbox.getWidth(Position.MIDDLE);
 			float defect = width - boxWidth;
 			if (defect != 0) {
 				HBox wrapperBox = new HBox();
 				wrapperBox.add(new SimpleWhitespace(driver, defect));
 				wrapperBox.add(hbox);
 				hbox = wrapperBox;
 			}
 		}
 		break;
 		case CENTER:
 		{
 			hbox = new HBox();
 			hbox.addAll(line);
 			float boxWidth = hbox.getWidth(Position.MIDDLE);
 			float defect = width - boxWidth;
 			if (defect != 0) {
 				HBox wrapperBox = new HBox();
 				wrapperBox.add(new SimpleWhitespace(driver, defect / 2));
 				wrapperBox.add(hbox);
 				wrapperBox.add(new SimpleWhitespace(driver, defect - (defect / 2)));
 				hbox = wrapperBox;
 			}
 		}
 		break;
 		default: throw new InternalError("Unhandled alignment type: " + alignment);
 		}
 		return hbox;
 	}
 
 	private HBox makeJustifiedHBox(List<IDrawable> line, float width, boolean last) {
 		if (last) {
 			HBox hbox = new HBox();
 			hbox.addAll(line);
 			return hbox;
 		}
		float currentWidth = 0;
		float currentStretch = 0;
 		for (ListIterator<IDrawable> iter = line.listIterator(); iter.hasNext();) {
 			IDrawable d = iter.next();
 			if (iter.previousIndex() == 0) {
 				currentWidth += d.getWidth(Position.START);
 			} else if (iter.hasNext()) {
 				currentWidth += d.getWidth(Position.MIDDLE);
 			} else {
 				currentWidth += d.getWidth(Position.END);
 			}
 			
 			if (d instanceof IAdjustableDrawable) {
 				IAdjustableDrawable ad = (IAdjustableDrawable)d;
 				if (iter.previousIndex() == 0) {
 					currentStretch += ad.getStretch(Position.START);
 				} else if (iter.hasNext()) {
 					currentStretch += ad.getStretch(Position.MIDDLE);
 				} else {
 					currentStretch += ad.getStretch(Position.END);
 				}
 			}
 		}
 			
 		if (currentStretch == 0) {
 			HBox hbox = new HBox();
 			hbox.addAll(line);
 			return hbox;
 		}
 		
 		float adjust = width - currentWidth;
 		if (adjust < 0) {
 			// FIXME: implement shrinking
 			throw new UnsupportedOperationException("Shrinking isn't supported yet");
 		}
 
 		HBox hbox = new HBox();
 		for (ListIterator<IDrawable> iter = line.listIterator(); iter.hasNext();) {
 			IDrawable drawable = iter.next();
 
 			if (drawable instanceof IAdjustableDrawable) {
 				IAdjustableDrawable d = (IAdjustableDrawable) drawable;
 				
 				float adj_i;
 				if (iter.previousIndex() == 0) {
 					adj_i = d.getStretch(Position.START);
 				} else if (iter.hasNext()) {
 					adj_i = d.getStretch(Position.MIDDLE);
 				} else {
 					adj_i = d.getStretch(Position.END);
 				}
 				if (adj_i != 0) {
 					float amount = adjust * adj_i / currentStretch;
 					adjust -= amount;
 					currentStretch -= adj_i;
 					d.adjust(amount);
 				}
 			}
 
 			hbox.add(drawable);
 		}
 		return hbox;
 	}
 
 }
