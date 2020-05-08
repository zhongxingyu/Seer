 package de.komoot.hackathon;
 
 import com.fasterxml.jackson.databind.ObjectMapper;
 import com.vividsolutions.jts.geom.Geometry;
 import de.komoot.hackathon.openstreetmap.*;
 import org.openstreetmap.osmosis.pbf2.v0_6.PbfReader;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.Writer;
 import java.util.Collection;
 import java.util.List;
 
 import static com.fasterxml.jackson.core.JsonGenerator.Feature;
 
 /** @author jan */
 public class AreaMatcher {
 	/** automatically generated Logger statement. */
 	private final static org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(AreaMatcher.class);
 
 	public static void main(String[] args) throws IOException {
 		PbfReader in = new PbfReader(new File(args[0]), 4);
 		OsmToKmtSink out = new OsmToKmtSink(10000);
 		in.setSink(out);
 		in.run();
 
 		File directory = new File(args[1]);
 		//writeMatches(out.getNodes(), out.getWays(), out.getAreas(), directory);
 
 		writeGeometries(out.getNodes(), out.getWays(), out.getAreas(), directory);
 	}
 
 	private static void writeMatches(List<OsmNode> nodes, List<OsmWay> ways, List<OsmArea> areas, File directory) throws IOException {
 		try(Writer writer = new FileWriter(new File(directory, "nodes-with-areas.csv"))) {
 			match(nodes, areas, writer);
 		}
 		try(Writer writer = new FileWriter(new File(directory, "ways-with-areas.csv"))) {
 			match(ways, areas, writer);
 		}
 	}
 
 	/**
 	 * Matches (spatially intersects) two Collections of OsmEntities against each other.
 	 * Writes a csv line to the given targetFile containing all items of the first Collection with a list of all intersecting elements from the second iterable
 	 *
 	 * @param entities1
 	 * @param entities2
 	 * @param targetFile
 	 * @throws IOException
 	 */
 	private static void match(Collection<? extends OsmEntity<?>> entities1, Collection<? extends OsmEntity<?>> entities2, Writer targetFile) throws IOException {
 		LOGGER.info("Matching {} elements with {} elements", entities1.size(), entities2.size());
 		for(OsmEntity<?> e1 : entities1) {
 			Geometry g1 = e1.getGeometry();
 			targetFile.write(Long.toString(e1.getOsmId()));
 			targetFile.write(',');
 			String name = e1.getTags().get("name");
 			if(name != null) {
 				targetFile.write('"');
 				targetFile.write(quote(name));
 				targetFile.write('"');
 			}
 			for(OsmEntity<?> e2 : entities2) {
 				Geometry g2 = e2.getGeometry();
 				if(g1.intersects(g2)) {
 					targetFile.write(',');
 					targetFile.write(Long.toString(e2.getOsmId()));
 				}
 			}
 			targetFile.write('\n');
 		}
 		LOGGER.info("done");
 	}
 
 	private static String quote(String name) {
 		return name.replace("\"", "\\\\\"");
 	}
 
 	private static void writeGeometries(List<OsmNode> nodes, List<OsmWay> ways, List<OsmArea> areas, File directory) throws IOException {
 
 		ObjectMapper mapper = new ObjectMapper();
 		mapper.registerModule(new GeometryModule());
 		mapper.configure(Feature.AUTO_CLOSE_TARGET, false);
 
 		try(Writer writer = new FileWriter(new File(directory, "nodes-raw.csv"))) {
 			write(nodes, writer, mapper);
 		}
 
 		try(Writer writer = new FileWriter(new File(directory, "ways-raw.csv"))) {
			write(nodes, writer, mapper);
 		}
 
 		try(Writer writer = new FileWriter(new File(directory, "areas-raw.csv"))) {
			write(nodes, writer, mapper);
 		}
 	}
 
 	private static void write(Collection<? extends OsmEntity<?>> entities, Writer writer, ObjectMapper mapper) throws IOException {
 		for(OsmEntity<?> e : entities) {
 			JsonGeometryEntity<Geometry> g = new JsonGeometryEntity<Geometry>(e.getId(), e.getGeometry(), e.getTags());
 			mapper.writeValue(writer, g);
 			writer.write('\n');
 		}
 	}
 }
