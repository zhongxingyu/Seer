 package btwmods.util;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import net.minecraft.src.ChunkCoordIntPair;
 import btwmods.util.intervals.IntervalTree;
 
 public class Zones<Type> extends HashSet<Area<Type>> {
 
 	private Map<ChunkCoordIntPair, IntervalTree<Area<Type>>> intervalsByRegion = new HashMap<ChunkCoordIntPair, IntervalTree<Area<Type>>>();
 	
 	@Override
 	public boolean add(Area<Type> area) {
		if (add(area)) {
 			addIntervals(area);
 			return true;
 		}
 		
 		return false;
 	}
 	
 	private void addIntervals(Area<Type> area) {
 		if (area != null) {
 			// Add the area to it's respective regions.
 			for (int regionX = area.x1 >> 9; regionX <= area.x2 >> 9; regionX++) {
 				for (int regionZ = area.z1 >> 9; regionZ <= area.z2 >> 9; regionZ++) {
 					
 					ChunkCoordIntPair coords = new ChunkCoordIntPair(regionX, regionZ);
 					
 					IntervalTree<Area<Type>> intervals = intervalsByRegion.get(coords);
 					if (intervals == null)
 						intervalsByRegion.put(coords, intervals = new IntervalTree<Area<Type>>());
 					
 					intervals.addInterval(area.x1 - 1, area.x2 + 1, area);
 				}
 			}
 		}
 	}
 
 	@Override
 	public boolean remove(Object o) {
 		return o instanceof Area && remove((Area)o);
 	}
 	
	public boolean remove(Area area) {
		if (remove(area)) {
 			removeIntervals(area);
 			return true;
 		}
 		
 		return false;
 	}
 	
 	private void removeIntervals(Area area) {
 		if (area != null) {
 			for (int regionX = area.x1 >> 9; regionX <= area.x2 >> 9; regionX++) {
 				for (int regionZ = area.z1 >> 9; regionZ <= area.z2 >> 9; regionZ++) {
 
 					ChunkCoordIntPair coords = new ChunkCoordIntPair(regionX, regionZ);
 					
 					IntervalTree<Area<Type>> intervals = intervalsByRegion.get(coords);
 					if (intervals != null)
 						intervals.removeByData(area);
 					
 					if (intervals.listSize() == 0)
 						intervalsByRegion.remove(coords);
 				}
 			}
 		}
 	}
 	
 	public List<Area<Type>> get(int x, int z) {
 		return get(x, 0, z, false);
 	}
 	
 	public List<Area<Type>> get(int x, int y, int z) {
 		return get(x, y, z, true);
 	}
 	
 	private List<Area<Type>> get(int x, int y, int z, boolean checkY) {
 		ArrayList<Area<Type>> areas = new ArrayList<Area<Type>>();
 		
 		if (!isEmpty()) {
 			// Get areas for the region the X and Z are in.
 			IntervalTree tree = intervalsByRegion.get(new ChunkCoordIntPair(x >> 9, z >> 9));
 			
 			if (tree != null) {
 				List<Area<Type>> intervalAreas = tree.get(x);
 				int size = intervalAreas.size();
 				
 				// Check all the areas that matched X.
 				for (int i = 0; i < size; i++) {
 					Area<Type> area = intervalAreas.get(i);
 					if ((checkY && area.isWithin(x, y, z)) || (!checkY && area.isWithin(x, z))) {
 						areas.add(area);
 					}
 				}
 			}
 		}
 		
 		return areas;
 	}
 
 	@Override
 	public void clear() {
 		intervalsByRegion.clear();
 		super.clear();
 	}
 
 	@Override
 	public Iterator<Area<Type>> iterator() {
 		return new AreaIterator(super.iterator());
 	}
 	
 	private class AreaIterator implements Iterator<Area<Type>> {
 		
 		private final Iterator<Area<Type>> iterator;
 		private Area<Type> current = null;
 		
 		public AreaIterator(Iterator<Area<Type>> iterator) {
 			this.iterator = iterator;
 		}
 
 		@Override
 		public boolean hasNext() {
 			return iterator.hasNext();
 		}
 
 		@Override
 		public Area<Type> next() {
 			return current = iterator.next();
 		}
 
 		@Override
 		public void remove() {
 			iterator.remove();
 			removeIntervals(current);
 		}
 	}
 }
