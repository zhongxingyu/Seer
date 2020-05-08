 package keyValueBaseInterfaces;
 
 import java.io.IOException;
 import java.nio.BufferOverflowException;
 import java.nio.channels.FileChannel;
 import java.nio.channels.FileChannel.MapMode;
 import java.util.Hashtable;
 import java.util.Iterator;
 
 public class MemoryMappedPinnable extends MemoryMappedFile {
 	public static int developmentVersion = 1;
 
 	private Hashtable<PinnedRegion,byte[]> pinned_regions;
 
 	public MemoryMappedPinnable(FileChannel channel, MapMode mode, int offset,
 			long totalSize) throws IndexOutOfBoundsException, IOException {
 
 		super(channel, mode, offset, totalSize);
 		
 		this.pinned_regions = new Hashtable<PinnedRegion,byte[]>();		
 	}
 
 	public void flush () {
 		
 		Iterator<PinnedRegion> it = this.pinned_regions.keySet().iterator();
 		while(it.hasNext()) {
 			PinnedRegion p = it.next();
 			super.put(this.pinned_regions.get(p), p.getStartPosition());
 		}
 		
 		this.pinned_regions.clear();
 		
 		for (int i = 0; i< super.buffers.size();i++)
 			super.buffers.get(i).force();
 	}
 
 	public void writePinned(byte[] src, long offset) {
 		
 		if (offset + src.length > this.totalSize)
 			throw new IndexOutOfBoundsException();
 		
 		PinnedRegion pR = new PinnedRegion(offset, src.length);
 		Iterator<PinnedRegion> it = this.pinned_regions.keySet().iterator();
 		while(it.hasNext()) {
 			if (pR.overlaps(it.next())) {
 				throw new IndexOutOfBoundsException("Overlapping with another itnerval");
 			}
 		}
 
 		this.pinned_regions.put(pR, src);
 	}
 
 	public void unpin (long offset, int length) {
 
 		PinnedRegion pR = new PinnedRegion(offset, length);
 		if (!this.pinned_regions.containsKey(pR))
 			throw new IndexOutOfBoundsException();
 		
 		byte[] data = this.pinned_regions.get(pR);
 		this.pinned_regions.remove(pR);
 		this.put(data, offset);
 	}
 	
 	@Override
 	public void get(byte[] dst, long offset) throws IndexOutOfBoundsException, BufferOverflowException{
 		
 		if (offset + dst.length > this.totalSize)
 			throw new IndexOutOfBoundsException();
 
 		PinnedRegion pR = new PinnedRegion(offset, dst.length);
 
 		if (!this.pinned_regions.containsKey(pR)){
 			super.get(dst, offset);
 		}
 		else{
 			byte[] value = this.pinned_regions.get(pR);
 			System.arraycopy(value, 0, dst, 0, value.length);
 		}
 
 	}
 
 
 	/**
 	 * @author PCSD-DIKU
 	 * 
 	 * This class defines a region in main memory that
 	 * can be pinned. It offers basic methods to get
 	 * its characteristics. It works with a long byte
 	 * array as a back up for the pinned data.
 	 */
 	public static class PinnedRegion implements Comparable<PinnedRegion>{
 
 		private Long startPosition;
 		private Integer size;
 
 		public PinnedRegion(long startPosition, int size) {
 			this.startPosition = startPosition;
 			this.size = size;
 		}
 
 		/**
 		 * Get the position where this region
 		 * starts.
 		 * 
 		 * @return the starting position
 		 */
 		public long getStartPosition() {
 			return this.startPosition;
 		}
 
 		/**
 		 * Get the length of this region.
 		 * 
 		 * @return the length of the region
 		 */
 		public long getSize() {
 			return this.size;
 		}
 
 		/**
 		 * Get the position where this region
 		 * ends.
 		 * 
 		 * @return the ending position
 		 */
 		public long getEndPosition() {
 			return this.startPosition + this.size;
 		}
 
 		/**
 		 * Compare this region's starting point with
 		 * another given region. It returns -1, 0 or
 		 * 1 depending on if this region starts before,
 		 * at the same address, or after the given region.
 		 * 
 		 * @param region the region with which to
 		 * Returns compare
 		 * this region
 		 * 
 		 * @return the position relative to the regions
 		 * starting position
 		 */
 		@Override
 		public int compareTo(PinnedRegion region) {
 			return startPosition.compareTo(region.startPosition);
 		}
 
 		/**
 		 * Check if this region overlaps at some point
 		 * with a given region
 		 * 
 		 * @param region the region with which to check
 		 * this region for overlap
 		 * 
 		 * @return whether the given region overlaps this
 		 * region at some address
 		 */
 		public boolean overlaps(PinnedRegion region) {
			return ((this.startPosition >= region.startPosition && this.startPosition < region.getEndPosition()) ||
					(region.startPosition >= this.startPosition && region.startPosition < this.getEndPosition()));
 		}
 		
 		@Override
 		public boolean equals(Object o) {
 			PinnedRegion p = (PinnedRegion) o;
			return (this.startPosition.equals(p.startPosition) && this.size == p.size);
 		}
 		
 		@Override
 		public int hashCode() {
 			return this.startPosition.hashCode();
 		}
 	}
 }
