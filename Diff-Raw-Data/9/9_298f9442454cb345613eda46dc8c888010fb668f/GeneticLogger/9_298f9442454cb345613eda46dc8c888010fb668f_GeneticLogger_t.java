 package util.genetic;
 
 import java.io.DataInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.nio.ByteBuffer;
 import java.nio.channels.FileChannel;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import eval.expEvalV3.EvalParameters;
 
 /** logs and loads the logs for genetic simuations*/
 public final class GeneticLogger {
 	private final static int typeIterationRecord = 0;
 	private final static int typeEntityRecord = 1;
 	
 	private final ByteBuffer b = ByteBuffer.allocate(1<<15);
 	private final FileChannel f;
 
 	@SuppressWarnings("resource")
 	public GeneticLogger(File f) throws IOException{
 		this.f = new FileOutputStream(f).getChannel();
 	}
 	
 	public void recordGEntity(final GEntity e) throws IOException{
 		b.clear();
		b.put((byte)typeEntityRecord);
 		b.putInt(e.id);
 		e.p.write(b);
 		
 		b.limit(b.position());
 		b.rewind();
 		f.write(b);
 	}
 	
 	public void recordIteration(final int iteration, final GEntity[] p) throws IOException{
 		b.clear();
 		b.put((byte)typeIterationRecord);
 		b.putInt(iteration);
 		b.putInt(p.length);
 		for(int a = 0; a < p.length; a++){
 			b.putInt(p[a].id);
 			b.putInt(p[a].wins.get());
 			b.putInt(p[a].losses.get());
 			b.putInt(p[a].draws.get());
 		}
 		
 		b.limit(b.position());
 		b.rewind();
 		f.write(b);
 	}
 	
 	public final static class IterationResult{
 		public int iteration;
 		/** stores results, each entry formatted [id,wins,losses,draws]*/
 		public final List<int[]> results = new ArrayList<int[]>();
 	}
 	/** representation for genetic lods*/
 	public final static class GLog{
 		/** stores iteration result offsets, indexed [iteration]:offset-index*/
 		public final Map<Integer, IterationResult> iterationResults = new HashMap<Integer, IterationResult>();
 		public final Map<Integer, EvalParameters> params = new HashMap<Integer, EvalParameters>();
 	}
 	/** probes passed file for list of game iterations*/
 	public static GLog loadLog(final File file) throws IOException{
 		final GLog offsets = new GLog();
 		final byte[] buff = new byte[1<<15];
 		final DataInputStream dis = new DataInputStream(new FileInputStream(file));
 		while(dis.available() > 0){
 			final int type = dis.read();
 			if(type == typeEntityRecord){
 				final int id = dis.readInt();
 				dis.read(buff);
 				ByteBuffer b = ByteBuffer.wrap(buff);
 				final EvalParameters temp = new EvalParameters();
 				temp.read(b);
 				offsets.params.put(id, temp);
 			} else if(type == typeIterationRecord){
 				final IterationResult temp = new IterationResult();
 				final int iteration = dis.readInt();
 				temp.iteration = iteration;
 				final int len = dis.readInt();
 				for(int a = 0; a < len; a++){
 					final int[] r = new int[4];
 					for(int q = 0; q < 4; q++){
 						r[q] = dis.readInt();
 					}
 					temp.results.add(r);
 				}
 				offsets.iterationResults.put(iteration, temp);
 			}
 		}
 		dis.close();
 		return offsets;
 	}
 }
