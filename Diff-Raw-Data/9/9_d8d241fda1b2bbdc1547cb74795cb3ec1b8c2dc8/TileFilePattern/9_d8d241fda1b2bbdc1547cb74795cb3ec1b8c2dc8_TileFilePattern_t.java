 package net.nabaal.majiir.realtimerender.rendering;
 
 import java.io.File;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.nabaal.majiir.realtimerender.Coordinate;
 import net.nabaal.majiir.realtimerender.image.FilePattern;
 
 //TODO: Rename
 public class TileFilePattern extends FilePattern {
 
 	private final String prefix;
 	private final Pattern pattern;
 	
 	public TileFilePattern(File parent, String prefix) {
 		super(parent);
 		this.prefix = prefix;
		this.pattern = Pattern.compile(Pattern.quote(prefix) + "\\.(\\-?\\d+)\\.(\\-?\\d+)\\.(\\-?\\d+)\\.png");
 	}
 	
 	@Override
 	public File getFile(Coordinate tile) {
 		return new File(getParent(), String.format("%s.%d.%d.%d.png", prefix, tile.getLevel(), tile.getX(), tile.getY()));
 	}
 	
 	@Override
 	public Coordinate getTile(File file) {
 		Matcher matcher = pattern.matcher(file.getName());
 		if (!matcher.matches()) {
 			return null;
 		}
 		return new Coordinate(Integer.valueOf(matcher.group(2)), Integer.valueOf(matcher.group(3)), Integer.valueOf(matcher.group(1)));
 	}
 	
 }
