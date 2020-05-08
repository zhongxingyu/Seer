 /**
  * 
  */
 package config;
 
 import java.util.LinkedList;
 import java.util.Queue;
 
 import org.newdawn.slick.Image;
 
 /**
  * @author Mullings
  *
  */
 public enum Level {
 	FOREST(Section.FOREST_1,
 			Section.FOREST_2,
 			Section.FOREST_2A,
 			Section.FOREST_2B,
 			Section.FOREST_3,
 			Section.FOREST_4,
 			Section.FOREST_5,
 			Section.FOREST_6,
 			Section.FOREST_7,
 			Section.FOREST_8),
 	LAKE(Section.LAKE_1,
 		 Section.LAKE_2),
 	
 	DESERT(Section.DESERT_1),
 	
	CANYON(Section.CANYON_1),
 	HELL(Section.HELL_1,
 		 Section.HELL_2,
 		 Section.HELL_3);
 //	FOREST_PLAINS(Section.FOREST_PLAINS);
 
 	private static final Queue<Section> sectionQueue = new LinkedList<Section>();
 	private Section[] sections;
 	
 	private Level(Section...sections) {
 		this.sections = sections;
 	}
 	
 	private Level(Image instruction, int x, int y, Section...sections) {
 		this.sections = sections;
 
 	}
 	
 	public static void addToQueue(Section section) {
 		sectionQueue.add(section);
 	}
 	
 	public static void addToQueue(Level level, int index) {
 		sectionQueue.add(level.getSection(index));
 	}
 
 	public static void addAllToQueue(Level level) {
 		for (int i = 0; i < level.getNumSections(); i++) {
 			sectionQueue.add(level.getSection(i));
 		}
 	}
 	
 	
 	public static void clearQueue() {
 		System.out.println("Clearing queue");
 		sectionQueue.clear();
 	}
 	
 	public static Section getNextSection() {
 		return sectionQueue.poll();
 	}
 	
 	public static boolean isQueueEmpty() {
 		return sectionQueue.isEmpty();
 	}
 
 	public Section[] getSections() {
 		return sections;
 	}
 
 	public Section getSection(int index) {
 		return sections[index];
 	}
 	
 	public int getNumSections() {
 		return sections.length;
 	}
 }
