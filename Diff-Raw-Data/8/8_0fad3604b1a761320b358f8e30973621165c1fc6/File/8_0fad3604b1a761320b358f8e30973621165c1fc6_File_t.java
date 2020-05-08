 package base.file;
 
 import java.io.IOException;
 import java.io.RandomAccessFile;
 
 import base.data.Bay;
 import base.data.Data;
 import base.exception.DiskException;
 import base.process.Mistake;
 import base.size.Stripe;
 import base.size.StripePattern;
 import base.state.Close;
 
 /** An open file on the disk with access to its data. */
 public class File extends Close {
 
 	// Make
 
 	/** Make and open the file at the given path. */
 	public File(Open open) {
 		try {
 			
 			// Get and save the path
 			path = open.path;
 			
 			// Enforce and check how we are told to open the file
 			if (open.how == Open.overwrite) path.delete(); // Delete a file or empty folder at path, or throw a DiskException
 			if ((open.how == Open.overwrite || open.how == Open.make) && path.exists()) throw new DiskException("exists"); // Make requires available path
 			if ((open.how == Open.read || open.how == Open.write) && !path.existsFile()) throw new DiskException("not found"); // Open requires file at path
 			
 			// Make or open the file
 			String access = "rw"; // For the make, overwrite, and write commands, get read and write access
 			if (open.how == Open.read) access = "r"; // For the read command, get read access
 			file = new RandomAccessFile(path.file, access);
 			
 			// Get or create and save the pattern
 			StripePattern pattern = open.pattern;
 			if (pattern == null) { // If no pattern given in path, make one
 				pattern = new StripePattern(); // If file is empty, pattern is ready
 				long size = file.getChannel().size(); // If the file has gaps, size will be as if they are full
 				if (size > 0) pattern = pattern.add(new Stripe(0, size)); // Mark the whole file as full
 			}
 			this.pattern = pattern;
 			
		} catch (IOException e) {
			close(this);
			throw new DiskException(e);
		} catch (RuntimeException e) {
			close(this);
			throw e;
		}
 	}
 
 	// Look
 	
 	/** The Path to our open file on the disk. */
 	public final Path path;
 	/** The Java RandomAccessFile object that gives us access to the data in our file. */
 	public final RandomAccessFile file;
 
 	// Close
 
 	/** Close our open connection to this file on the disk. */
 	public void close() {
 		if (already()) return;
 		try { file.close(); } catch (IOException e) { Mistake.log(e); } // Also closes file's FileChannel
 	}
 
 	/** Close and delete this file on the disk. */
 	public void delete() {
 		close(this);
 		path.delete(); // Delete it at its Path
 	}
 
 	// Size
 	
 	/** The size of this file, as though any gaps in it are full. */
 	public long size() { return pattern.size(); } // Ask our StripePattern
 	/** True if this File has a size of 0 bytes. */
 	public boolean isEmpty() { return size() == 0; }
 	/** True if this File has 1 or more bytes of data inside. */
 	public boolean hasData() { return size() > 0; }
 	
 	/** A StripePattern that shows what parts of this File have data, and which parts are gaps. */
 	public StripePattern pattern() { return pattern; }
 	private StripePattern pattern;
 	
 	/** Tell this File that you wrote stripe of data to it. */
 	public void add(Stripe stripe) {
 		pattern = pattern.add(stripe);
 	}
 
 	// Transfer
 
 	/** Read the contents of this File into memory. */
 	public Data read() { Bay bay = new Bay(); read(bay); return bay.data(); }
 	/** Read the part of the File stripe identifies into memory. */
 	public Data read(Stripe stripe) { Bay bay = new Bay(); read(bay, stripe); return bay.data(); }
 
 	/** Read the contents of this File into bay. */
 	public void read(Bay bay) {
 		if (size() == 0) return; // This File is empty
 		read(bay, new Stripe(0, size())); // Call the next method with a Stripe that clips out this whole file
 	}
 
 	/** Read the part of this File stripe identifies into bay. */
 	public void read(Bay bay, Stripe stripe) {
 		if (!pattern.is(true, stripe)) throw new DiskException("hole"); // Make sure we have data where stripe is
 		bay.read(this, stripe); // Add stripe.size bytes from stripe.i in our file to bay
 	}
 
 	/** Write d a distance i bytes into this File. */
 	public void write(long i, Data data) {
 		try {
 			if (data.isEmpty()) return; // Nothing to write
 			int did = file.getChannel().write(data.toByteBuffer(), i);
 			if (did != data.size()) throw new DiskException("did " + did); // Make sure write() wrote everything
 			add(new Stripe(i, data.size())); // Update pattern
 		} catch (IOException e) { throw new DiskException(e); }
 	}
 
 	// Small
 	
 	/** Open the file at path, copy its contents into memory, and close it. */
 	public static Data data(Path path) {
 		File f = new File(new Open(path, null, Open.read));
 		Data d = f.read(); // Copy the file's contents into memory
 		close(f);
 		return d;
 	}
 
 	/** Save d to a file at path, overwriting one already there. */
 	public static void save(Path path, Data d) {
 		try {
 			File f = new File(new Open(path, null, Open.overwrite));
 			f.write(0, d);
 			f.file.getChannel().truncate(d.size()); // Chop the file off after that
 			close(f);
 		} catch (IOException e) { throw new DiskException(e); }
 	}
 }
