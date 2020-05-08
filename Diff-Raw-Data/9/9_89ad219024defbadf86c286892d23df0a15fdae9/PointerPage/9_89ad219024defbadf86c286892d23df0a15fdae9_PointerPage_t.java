 package memoryManager;
 
 import utils.BitArray;
 import utils.ByteConverter;
 
 /**
  * Created with IntelliJ IDEA.
  * User: nikita_kartashov
  * Date: 06/11/2013
  * Time: 00:38
  * To change this template use File | Settings | File Templates.
  */
 public class PointerPage extends DiskPage
 {
 	public PointerPage(byte[] rawPage, boolean blankPage)
 	{
 		super(rawPage, blankPage);
 		if (blankPage)
 		{
 			initHeader(_rawPage);
 			_isOccupied = new BitArray(MAX_POINTERS_COUNT);
 		}
 		else
 		{
 			_isOccupied = BitArray.readBitArray(rawPage, COUNT_OFFSET, MAX_POINTERS_COUNT);
 		}
 	}
 
 	public long getPointer(int index)
 	{
 		return ByteConverter.longFromByte(_rawPage, DATA_OFFSET + index * ByteConverter.LONG_LENGTH_IN_BYTES);
 	}
 
 	public long firstGreenPageIndex()
 	{
 		return getPointer(_isOccupied.firstSetBit());
 	}
 
 	public void setPointer(int index, long pointer) throws IndexOutOfBoundsException
 	{
 		if (index > MAX_POINTERS_COUNT)
 			throw new IndexOutOfBoundsException("Cannot set pointer with " + index + ": index is out of range");
 		byte[] newValues = ByteConverter.longToByte(pointer);
 		System.arraycopy(newValues, 0, _rawPage,
 			DATA_OFFSET + index * ByteConverter.LONG_LENGTH_IN_BYTES, ByteConverter.LONG_LENGTH_IN_BYTES);
 	}
 
 	public void addPointer(long pointer)
 	{
 		setPointer(_isOccupied.firstClearBit(), pointer);
 		_isOccupied.set(_isOccupied.firstClearBit());
 		updateOccupiedSet();
 	}
 
 	public void removePointer(int index)
 	{
 		_isOccupied.clear(index);
 		updateOccupiedSet();
 	}
 
 	public void removePointer(long pointer)
 	{
 		int i = -1;
 		while(true)
 		{
 			i = _isOccupied.nextSetBit(i + 1);
 			if (i == -1)
 				break;
 			if (getPointer(i) == pointer)
 			{
 				removePointer(i);
 				_isOccupied.clear(i);
 				updateOccupiedSet();
 				return;
 			}
 		}
 	}
 
 	public boolean isFull() {return _isOccupied.cardinality() == MAX_POINTERS_COUNT;}
 
 	public boolean isEmpty() {return _isOccupied.cardinality() == 0;}
 
 	public int pointersCount() {return _isOccupied.cardinality();}
 
 	public long[] allPointers()
 	{
 		long[] result = new long[pointersCount()];
 		int j = 0;
 		int i = -1;
 		while(true)
 		{
 			i = _isOccupied.nextSetBit(i + 1);
 			if (i == -1)
 				break;
 			result[j++] = getPointer(i);
 		}
 
 		return result;
 	}
 
 	public static void initHeader(byte[] rawData)
 	{
 		DiskPage.initHeader(rawData);
 		updateOccupiedSet(rawData, new BitArray(MAX_POINTERS_COUNT));
 	}
 
 	private void updateOccupiedSet()
 	{
 		updateOccupiedSet(_rawPage, _isOccupied);
 	}
 
 	private static void updateOccupiedSet(byte[] rawData, BitArray array)
 	{
 		array.writeBitArray(rawData, COUNT_OFFSET);
 	}
 
 	private BitArray _isOccupied;
 
 	private static int POINTER_SIZE_IN_BYTES = 8;
	private static int MAX_POINTERS_COUNT = DATA_PAGE_SIZE_IN_BYTES / POINTER_SIZE_IN_BYTES;
 	private static int COUNT_OFFSET = 16;
 }
