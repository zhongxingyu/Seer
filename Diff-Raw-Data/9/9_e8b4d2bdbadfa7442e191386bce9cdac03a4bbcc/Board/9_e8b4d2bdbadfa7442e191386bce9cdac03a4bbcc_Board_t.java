 package uk.ac.ox.cs.sokobanexam.model;
 
 import java.util.ArrayList;
 import java.util.Collections;
import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import uk.ac.ox.cs.sokobanexam.model.sprites.Floor;
 import uk.ac.ox.cs.sokobanexam.model.sprites.Sprite;
 import uk.ac.ox.cs.sokobanexam.model.sprites.Sprite.SemanticType;
 
 public class Board implements IBoard {
 	
 	// Class Invariant:
 	//		mMap.length >= 1
 	//		mMap[y].length >= 1
 	//		mMap[y][x].size() >= 1
 	private List<Sprite>[][] mMap;
 	
 	// Class Invariant: mOccupiedPoints[type] = {point in mMap | mMap[point].type() = type}
 	private Map<SemanticType,Set<Point>> mOccupiedPoints;
 	
 	@SuppressWarnings("unchecked")
 	public Board(int width, int height) {
 		if (width <= 0 || height <= 0)
 			throw new IllegalArgumentException("Board size: "+width+"x"+height);
 		mMap = new List[height][width];
		mOccupiedPoints = new HashMap<SemanticType,Set<Point>>();
 		
 		// Initialize class invariant:
 		for (SemanticType type : SemanticType.values())
 			mOccupiedPoints.put(type, new HashSet<Point>());
 		for (int y = 0; y < height; y++)
 			for (int x = 0; x < width; x++) {
 				mMap[y][x] = new ArrayList<Sprite>();
 				insertSpriteAt(Point.at(x,y), new Floor());
 			}
 	}
 	
 	@Override
 	public int getHeight() {
 		return mMap.length;
 	}
 
 	@Override
 	public int getWidth() {
 		return mMap[0].length;
 	}
 	
	protected List<Sprite> getSpritesAt(Point point) {
 		if (!(0 <= point.x && point.x < getWidth())
 				|| !(0 <= point.y && point.y < getHeight()))
 			throw new IndexOutOfBoundsException();
		return mMap[point.y][point.x];
 	}
 	
 	@Override
 	public Sprite getTopSpriteAt(Point point) {
 		List<Sprite> sprites = getSpritesAt(point);
 		return sprites.get(sprites.size()-1);
 	}
 
 	@Override
 	public Sprite deleteTopSpriteAt(Point point) {
 		List<Sprite> sprites = getSpritesAt(point);
 		if (sprites.size() == 0)
 			return sprites.get(0);
 		Sprite sprite = sprites.get(sprites.size()-1);
 		mOccupiedPoints.get(sprite.type()).remove(point);
 		return sprites.remove(sprites.size()-1);
 	}
 
 	@Override
 	public Board insertSpriteAt(Point point, Sprite sprite) {
 		getSpritesAt(point).add(sprite);
 		mOccupiedPoints.get(sprite.type()).add(point);
 		return this;
 	}
 	
 	@Override
 	public Set<Point> getOccupiedPoints(SemanticType type) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 }
