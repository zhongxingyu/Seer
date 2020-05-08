 package edu.rpi.rocs.client.ui.scheduler;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.event.dom.client.MouseOutEvent;
 import com.google.gwt.event.dom.client.MouseOutHandler;
 import com.google.gwt.event.dom.client.MouseOverEvent;
 import com.google.gwt.event.dom.client.MouseOverHandler;
 
 import edu.rpi.rocs.client.objectmodel.Course;
 import edu.rpi.rocs.client.objectmodel.Period;
 import edu.rpi.rocs.client.objectmodel.Schedule;
 import edu.rpi.rocs.client.objectmodel.Section;
 import edu.rpi.rocs.client.objectmodel.Time;
 import edu.rpi.rocs.client.ui.filters.ScheduleTimeBlockFilterWidget;
 import edu.rpi.rocs.client.ui.svg.SVGCanvasWidget;
 import edu.rpi.rocs.client.ui.svg.SVGGroupWidget;
 import edu.rpi.rocs.client.ui.svg.SVGPathComponent;
 import edu.rpi.rocs.client.ui.svg.SVGPathWidget;
 import edu.rpi.rocs.client.ui.svg.SVGRectWidget;
 import edu.rpi.rocs.client.ui.svg.SVGTextWidget;
 import edu.rpi.rocs.client.ui.svg.IsSVGText.TextAnchor;
 
 public class ScheduleViewWidget extends SVGCanvasWidget {
 
 	Schedule m_schedule;
 	ScheduleBackgroundWidget m_background;
 	private static final int MAJOR_Y_DISTANCE=26;
 	private static final int MAJOR_X_DISTANCE=75;
 	private static final int MINOR_Y_DISTANCE=13;
 	RandomColorGenerator m_generator;
 
 	private class ScheduleBackgroundWidget extends SVGGroupWidget {
 		SVGPathWidget m_solid_lines;
 		SVGPathWidget m_dashed_lines;
 		SVGRectWidget m_background;
 		SVGGroupWidget m_x_labels;
 		SVGGroupWidget m_y_labels;
 		SVGPathWidget m_blocked;
 
 		private void createBackground() {
 			m_background = new SVGRectWidget();
 			m_background.setX("0");
 			m_background.setY("0");
 			m_background.setWidth("600");
 			m_background.setHeight("390");
 			m_background.setFillColor("#ffffff");
 			m_background.setStrokeColor("#000000");
 			m_background.setStrokeWidth("1");
 		}
 
 		private void createBlocked(Schedule s) {
 			m_blocked = new SVGPathWidget();
 			m_blocked.setFillColor("#b8b8b8");
 			ArrayList<SVGPathComponent> temp = new ArrayList<SVGPathComponent>();
 			int start,end,x,y;
 			for(int day=0;day<7;day++) {
 				for(int i=8;i<22;i++) {
 					start=end=x=y=-1;
 					if(s.isBlocked(day, i, 0)) {
 						start = i*2;
 						end = start+1;
 						while(end<22*2) {
 							if(s.isBlocked(day, i, 30)) {
 								end++;
 								i++;
 							}
 							else {
 								break;
 							}
 							if(i==22) break;
 							if(s.isBlocked(day, i, 0)) {
 								end++;
 							}
 							else {
 								break;
 							}
 						}
 					}
 					if(s.isBlocked(day, i, 30)) {
 						start = i*2+1;
 						end = start+1;
 						i++;
 						while(end<22*2) {
 							if(s.isBlocked(day, i, 0))
 								end++;
 							else break;
 							if(s.isBlocked(day, i, 30)) {
 								end++;
 								i++;
 							}
 							else break;
 						}
 					}
 					if(start>0) {
 						x = day * MAJOR_X_DISTANCE;
 						y = (start/2-8) * MAJOR_Y_DISTANCE;
 						if(start%2==1) y += MINOR_Y_DISTANCE;
 
 						// Adjust for headers
 						x += MAJOR_X_DISTANCE;
 						y += MAJOR_Y_DISTANCE;
 
 						SVGPathComponent c = SVGPathComponent.createMovePathComponent(Integer.toString(x), Integer.toString(y), false);
 						temp.add(c);
 						c = SVGPathComponent.createLinePathComponent(Integer.toString(MAJOR_X_DISTANCE), "0", true);
 						temp.add(c);
 						c = SVGPathComponent.createLinePathComponent("0", Integer.toString((end-start)*MINOR_Y_DISTANCE), true);
 						temp.add(c);
 						c = SVGPathComponent.createLinePathComponent(Integer.toString(-MAJOR_X_DISTANCE), "0", true);
 						temp.add(c);
 						c = SVGPathComponent.createClosePathComponent(false);
 						temp.add(c);
 					}
 				}
 			}
 			m_blocked.addAll(temp);
 		}
 
 		private void createSolidLines() {
 			m_solid_lines = new SVGPathWidget();
 			ArrayList<SVGPathComponent> temp = new ArrayList<SVGPathComponent>();
 			for(int i=0;i<14;i++) {
 				temp.add(SVGPathComponent.createMovePathComponent("0", Integer.toString((i+1)*MAJOR_Y_DISTANCE), false));
 				temp.add(SVGPathComponent.createLinePathComponent("600", "0", true));
 			}
 			for(int i=0;i<7;i++) {
 				temp.add(SVGPathComponent.createMovePathComponent(Integer.toString((i+1)*MAJOR_X_DISTANCE), "0", false));
 				temp.add(SVGPathComponent.createLinePathComponent("0", "390", true));
 			}
 			m_solid_lines.addAll(temp);
 			m_solid_lines.setStrokeColor("#000000");
 			m_solid_lines.setStrokeWidth("1");
 		}
 
 		private void createDashedLines() {
 			m_dashed_lines = new SVGPathWidget();
 			ArrayList<SVGPathComponent> temp = new ArrayList<SVGPathComponent>();
 			for(int i=0;i<14;i++) {
 				temp.add(SVGPathComponent.createMovePathComponent(Integer.toString(MAJOR_X_DISTANCE),
 						Integer.toString((i+1)*MAJOR_Y_DISTANCE+MINOR_Y_DISTANCE), false));
 				temp.add(SVGPathComponent.createLinePathComponent(Integer.toString(600-MAJOR_X_DISTANCE), "0", true));
 			}
 			m_dashed_lines.addAll(temp);
 			m_dashed_lines.setStrokeColor("#888888");
 			m_dashed_lines.setStrokeWidth("1");
 			ArrayList<String> dash = new ArrayList<String>();
 			dash.add("4");
 			dash.add("4");
 			m_dashed_lines.setStrokeDashArray(dash);
 		}
 
 		private void createLabels() {
 			m_y_labels = new SVGGroupWidget();
 			for(int i=0;i<7;i++) {
 				SVGTextWidget text = new SVGTextWidget();
 				text.setFillColor("#000000");
 				text.setTextAnchor(TextAnchor.Middle);
 				text.setText(ScheduleTimeBlockFilterWidget.dayOfWeek(i));
 				text.setX(Integer.toString((i+1)*75+75/2));
 				text.setY("20");
 				text.setFontSize("16");
 				m_y_labels.addSVGElement(text);
 			}
 			m_x_labels = new SVGGroupWidget();
 			for(int i=0;i<14;i++) {
 				SVGTextWidget text = new SVGTextWidget();
 				text.setFillColor("#000000");
 				text.setTextAnchor(TextAnchor.End);
 				text.setText(Integer.toString(i+8));
 				text.setX("72");
 				text.setY(Integer.toString((i+1)*26+20));
 				text.setFontSize("16");
 				m_x_labels.addSVGElement(text);
 			}
 		}
 
 		public ScheduleBackgroundWidget() {
 			createBackground();
 			addSVGElement(m_background);
 			m_blocked = new SVGPathWidget();
 			m_blocked.setFillColor("#b8b8b8");
 			ArrayList<SVGPathComponent> temp = new ArrayList<SVGPathComponent>();
 			temp.add(SVGPathComponent.createMovePathComponent("225","91",false));
 			temp.add(SVGPathComponent.createLinePathComponent("75", "0", true));
 			temp.add(SVGPathComponent.createLinePathComponent("0", "117", true));
 			temp.add(SVGPathComponent.createLinePathComponent("-75", "0", true));
 			temp.add(SVGPathComponent.createClosePathComponent(false));
 			m_blocked.addAll(temp);
 			addSVGElement(m_blocked);
 			createSolidLines();
 			addSVGElement(m_solid_lines);
 			createDashedLines();
 			addSVGElement(m_dashed_lines);
 			createLabels();
 			addSVGElement(m_y_labels);
 			addSVGElement(m_x_labels);
 		}
 
 		public ScheduleBackgroundWidget(Schedule mSchedule) {
 			createBackground();
 			addSVGElement(m_background);
 			createBlocked(mSchedule);
 			addSVGElement(m_blocked);
 			createSolidLines();
 			addSVGElement(m_solid_lines);
 			createDashedLines();
 			addSVGElement(m_dashed_lines);
 			createLabels();
 			addSVGElement(m_y_labels);
 			addSVGElement(m_x_labels);
 		}
 	}
 
 	private class SectionGroupWidget extends SVGGroupWidget {
 		private String m_identifier;
 		private ArrayList<SVGRectWidget> m_rects;
 		private ArrayList<SVGTextWidget> m_text;
 		private String m_color;
 		private Section m_section;
 
 		abstract class SectionMouseOverHandler implements MouseOverHandler {
 			SectionGroupWidget self;
 
 			SectionMouseOverHandler(SectionGroupWidget w) {
 				self = w;
 			}
 		}
 
 		abstract class SectionMouseOutHandler implements MouseOutHandler {
 			SectionGroupWidget self;
 
 			SectionMouseOutHandler(SectionGroupWidget w) {
 				self = w;
 			}
 		}
 		
 		abstract class SectionClickHandler implements ClickHandler {
 			SectionGroupWidget self;
 			
 			SectionClickHandler(SectionGroupWidget w) {
 				self = w;
 			}
 		}
 
 		SectionMouseOverHandler overHandle = new SectionMouseOverHandler(this) {
 
 			public void onMouseOver(MouseOverEvent arg0) {
 				for(SVGRectWidget w : self.m_rects) {
 					w.setStrokeColor("#800000");
 					w.setStrokeWidth("2");
 				}
 			}
 			
 		};
 
 		SectionMouseOutHandler outHandle = new SectionMouseOutHandler(this) {
 
 			public void onMouseOut(MouseOutEvent arg0) {
 				for(SVGRectWidget w : self.m_rects) {
 					w.setStrokeColor("#000000");
 					w.setStrokeWidth("1");
 				}
 			}
 			
 		};
 		
 		SectionClickHandler clickHandle = new SectionClickHandler(this) {
 			
 			public void onClick(ClickEvent arg0) {
 				for(SVGRectWidget w : self.m_rects) {
 					w.setStrokeColor("#000000");
 					w.setStrokeWidth("3");
 				}
 				SchedulerDisplayPanel.getInstance().getInfoPanel().displayInfoForSection(m_section);
 			}
 		};
 
 		public SectionGroupWidget(Section s) {
 			m_section = s;
 			Course temp = s.getParent();
 			m_identifier = temp.getDept() + " " + temp.getNum();
 			m_color = "#ffffff";
 			m_rects = new ArrayList<SVGRectWidget>();
 			m_text = new ArrayList<SVGTextWidget>();
 			List<Period> times = s.getPeriods();
			this.addStyleName("with-cursor");
 			for(Period p : times) {
 				Time start = p.getStart();
 				Time end = p.getEnd();
 				int startPix = (int)((double)MAJOR_Y_DISTANCE*(double)((double)start.getMinute()/60.0));
 				startPix += (start.getHour()-8)*MAJOR_Y_DISTANCE;
 				int endPix = (int)((double)MAJOR_Y_DISTANCE*(double)((double)end.getMinute()/60.0));
 				endPix += (end.getHour()-8)*MAJOR_Y_DISTANCE;
 
 				// Adjust for header row
 				startPix += MAJOR_Y_DISTANCE;
 				endPix += MAJOR_Y_DISTANCE;
 
 				for(Integer day : p.getDays()) {
 					int d = day.intValue();
 					int left = MAJOR_X_DISTANCE*d;
 					int right = MAJOR_X_DISTANCE*(d+1);
 
 					// Adjust for header column
 					left += MAJOR_X_DISTANCE;
 					right += MAJOR_X_DISTANCE;
 
 					SVGRectWidget rect = new SVGRectWidget();
 					rect.setX(Integer.toString(left));
 					rect.setY(Integer.toString(startPix));
 					rect.setWidth(Integer.toString(right-left));
 					rect.setHeight(Integer.toString(endPix-startPix));
 					rect.setFillColor(m_color);
 					rect.setStrokeColor("#000000");
 					rect.setStrokeWidth("1");
 					rect.addMouseOutHandler(outHandle);
 					rect.addMouseOverHandler(overHandle);
 					rect.addClickHandler(clickHandle);
 					m_rects.add(rect);
 
 					SVGTextWidget text = new SVGTextWidget();
 					text.setX(Integer.toString((left+right)/2));
 					text.setY(Integer.toString(startPix+MINOR_Y_DISTANCE-2));
 					text.setFontSize("12");
 					text.setTextAnchor(TextAnchor.Middle);
 					text.setText(m_identifier);
 					text.addMouseOutHandler(outHandle);
 					text.addMouseOverHandler(overHandle);
 					text.addClickHandler(clickHandle);
 					m_text.add(text);
 				}
 			}
 			for(SVGRectWidget w : m_rects)
 				addSVGElement(w);
 			for(SVGTextWidget w : m_text)
 				addSVGElement(w);
 		}
 
 		@SuppressWarnings("unused")
 		public Section getSection() {
 			return m_section;
 		}
 
 		public SectionGroupWidget(boolean twohr) {
 			if(twohr) {
 				m_identifier = "CSCI 4210";
 				m_color="#ffffff";
 				m_rects = new ArrayList<SVGRectWidget>();
 				m_text = new ArrayList<SVGTextWidget>();
 				SVGRectWidget rect = new SVGRectWidget();
 				rect.setX("150");
 				rect.setY("78");
 				rect.setWidth("75");
 				rect.setHeight("48");
 				rect.setFillColor(m_color);
 				rect.setStrokeColor("#000000");
 				rect.setStrokeWidth("1");
 				rect.addMouseOverHandler(overHandle);
 				rect.addMouseOutHandler(outHandle);
 				m_rects.add(rect);
 				rect = new SVGRectWidget();
 				rect.setX("375");
 				rect.setY("78");
 				rect.setWidth("75");
 				rect.setHeight("48");
 				rect.setFillColor(m_color);
 				rect.setStrokeColor("#000000");
 				rect.setStrokeWidth("1");
 				rect.addMouseOutHandler(outHandle);
 				rect.addMouseOverHandler(overHandle);
 				m_rects.add(rect);
 				SVGTextWidget text = new SVGTextWidget();
 				text.setX(Integer.toString(150+76/2));
 				text.setY(Integer.toString(78+35/2));
 				text.setFontSize("14");
 				text.setTextAnchor(TextAnchor.Middle);
 				text.setText(m_identifier);
 				text.addMouseOutHandler(outHandle);
 				text.addMouseOverHandler(overHandle);
 				m_text.add(text);
 				text = new SVGTextWidget();
 				text.setX(Integer.toString(375+76/2));
 				text.setY(Integer.toString(78+35/2));
 				text.setFontSize("14");
 				text.setTextAnchor(TextAnchor.Middle);
 				text.setText(m_identifier);
 				text.addMouseOutHandler(outHandle);
 				text.addMouseOverHandler(overHandle);
 				m_text.add(text);
 				for(SVGRectWidget w : m_rects)
 					addSVGElement(w);
 				for(SVGTextWidget w : m_text)
 					addSVGElement(w);
 			}
 			else {
 				m_identifier = "CSCI 4320";
 				m_color="#ffffff";
 				m_rects = new ArrayList<SVGRectWidget>();
 				m_text = new ArrayList<SVGTextWidget>();
 				SVGRectWidget rect = new SVGRectWidget();
 				rect.setX("150");
 				rect.setY("130");
 				rect.setWidth("75");
 				rect.setHeight("35");
 				rect.setFillColor(m_color);
 				rect.setStrokeColor("#000000");
 				rect.setStrokeWidth("1");
 				rect.addMouseOutHandler(outHandle);
 				rect.addMouseOverHandler(overHandle);
 				m_rects.add(rect);
 				rect = new SVGRectWidget();
 				rect.setX("375");
 				rect.setY("130");
 				rect.setWidth("75");
 				rect.setHeight("35");
 				rect.setFillColor(m_color);
 				rect.setStrokeColor("#000000");
 				rect.setStrokeWidth("1");
 				rect.addMouseOutHandler(outHandle);
 				rect.addMouseOverHandler(overHandle);
 				m_rects.add(rect);
 				SVGTextWidget text = new SVGTextWidget();
 				text.setX(Integer.toString(150+76/2));
 				text.setY(Integer.toString(130+35/2));
 				text.setFontSize("14");
 				text.setTextAnchor(TextAnchor.Middle);
 				text.setText(m_identifier);
 				text.addMouseOutHandler(outHandle);
 				text.addMouseOverHandler(overHandle);
 				m_text.add(text);
 				text = new SVGTextWidget();
 				text.setX(Integer.toString(375+76/2));
 				text.setY(Integer.toString(130+35/2));
 				text.setFontSize("14");
 				text.setTextAnchor(TextAnchor.Middle);
 				text.setText(m_identifier);
 				text.addMouseOutHandler(outHandle);
 				text.addMouseOverHandler(overHandle);
 				m_text.add(text);
 				for(SVGRectWidget w : m_rects)
 					addSVGElement(w);
 				for(SVGTextWidget w : m_text)
 					addSVGElement(w);
 			}
 		}
 
 		public void setFillColor(String color) {
 			super.setFillColor(color);
 			m_color = color;
 			for(SVGRectWidget w : m_rects)
 				w.setFillColor(color);
 		}
 	}
 
 	public ScheduleViewWidget(Schedule s, RandomColorGenerator g) {
 		super("schedule_viewer", "600", "400");
 		m_schedule = s;
 		m_generator = g;
 		//ArrayList<Section> sections = s.getSections();
 		if(s==null) {
 			m_background = new ScheduleBackgroundWidget();
 			addSVGElement(m_background);
 			SectionGroupWidget w = new SectionGroupWidget(true);
 			w.setFillColor(m_generator.randomlySelectColor());
 			addSVGElement(w);
 			w = new SectionGroupWidget(false);
 			w.setFillColor(m_generator.randomlySelectColor());
 			addSVGElement(w);
 		}
 		else {
 			m_background = new ScheduleBackgroundWidget(m_schedule);
 			addSVGElement(m_background);
 			ArrayList<Section> sections = s.getSections();
 			for(Section section : sections) {
 				SectionGroupWidget w = new SectionGroupWidget(section);
 				w.setFillColor(m_generator.randomlySelectColor(section.getParent()));
 				addSVGElement(w);
 			}
 		}
 	}
 }
