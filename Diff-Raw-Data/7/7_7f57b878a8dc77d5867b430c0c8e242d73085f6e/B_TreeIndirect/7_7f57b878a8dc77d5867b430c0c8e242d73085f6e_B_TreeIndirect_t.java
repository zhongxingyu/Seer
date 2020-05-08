 package suite.immutable;
 
 import java.io.Closeable;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.nio.ByteBuffer;
 import java.util.ArrayDeque;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Deque;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import suite.file.SerializedPageFile;
 import suite.util.FunUtil.Source;
 import suite.util.SerializeUtil;
 import suite.util.SerializeUtil.Serializer;
 import suite.util.Util;
 
 public class B_TreeIndirect<T> implements Closeable {
 
 	private int maxSize = 16;
 	private int halfSize = maxSize / 2;
 
 	private Comparator<T> comparator;
 	private Serializer<T> serializer;
 
 	private SerializedPageFile<Page> pageFile;
 	private B_TreeIndirect<Pointer> allocationB_tree;
 
 	public static class Pointer {
 		public static Comparator<Pointer> comparator = new Comparator<Pointer>() {
 			public int compare(Pointer p0, Pointer p1) {
 				return p0.number - p1.number;
 			}
 		};
 
 		public static Serializer<Pointer> serializer = SerializeUtil.nullable(new Serializer<Pointer>() {
 			public Pointer read(ByteBuffer buffer) {
 				Pointer pointer = new Pointer();
 				pointer.number = SerializeUtil.intSerializer.read(buffer);
 				return pointer;
 			}
 
 			public void write(ByteBuffer buffer, Pointer pointer) {
 				SerializeUtil.intSerializer.write(buffer, pointer.number);
 			}
 		});
 
 		private int number;
 	}
 
 	private class Page {
 		private List<Slot> slots;
 	}
 
 	/**
 	 * Pointer would be null in leaves. Pivot stores the leaf value.
 	 * 
 	 * Pivot would be null at the maximum side of a tree. It represents the
 	 * guarding key.
 	 */
 	private class Slot {
 		private Pointer pointer;
 		private T pivot;
 
 		private Slot(Pointer pointer, T pivot) {
 			this.pointer = pointer;
 			this.pivot = pivot;
 		}
 
 		private List<Slot> slots() {
 			return pointer != null ? read(pointer).slots : null;
 		}
 	}
 
 	private interface Allocator {
 		public Pointer allocate();
 
 		public void discard(Pointer pointer);
 
 		public List<Integer> commit();
 	}
 
 	public class SwappingAllocator implements Allocator {
 		private List<Pointer> pointers = Arrays.asList(new Pointer(), new Pointer());
 		private int using = 0;
 
 		private SwappingAllocator(int using) {
 			this.using = using;
 		}
 
 		public Pointer allocate() {
 			return pointers.get(using);
 		}
 
 		public void discard(Pointer pointer) {
 		}
 
 		public List<Integer> commit() {
 			List<Integer> pointer = Arrays.asList(using);
 			using = 1 - using;
 			return pointer;
 		}
 	}
 
 	public class B_TreeAllocator implements Allocator {
 		private B_TreeIndirect<Pointer> b_tree;
 		private B_TreeIndirect<Pointer>.Transaction transaction;
 
 		public B_TreeAllocator(B_TreeIndirect<Pointer> b_tree, List<Integer> chain) {
 			this.b_tree = b_tree;
 			this.transaction = b_tree.transaction(chain);
 		}
 
 		public Pointer allocate() {
 			Pointer pointer = b_tree.source(transaction).source();
 			if (pointer != null) {
 				transaction.remove(pointer);
 				return pointer;
 			} else
 				throw new RuntimeException("Pages exhausted");
 		}
 
 		public void discard(Pointer pointer) {
 			transaction.add(pointer);
 		}
 
 		public List<Integer> commit() {
 			return transaction.commit();
 		}
 	}
 
 	public class Transaction {
 		private Pointer root;
 		private Allocator allocator;
 
 		private Transaction(Allocator allocator) {
 			this.allocator = allocator;
 			root = persist(Arrays.asList(new Slot(null, null)));
 		}
 
 		private Transaction(Allocator allocator, Pointer root) {
 			this.root = root;
 			this.allocator = allocator;
 		}
 
 		public void add(T t) {
 			add(t, false);
 		}
 
 		/**
 		 * Replaces a value with another. Mainly for dictionary cases to replace
 		 * stored value for the same key.
 		 * 
 		 * Asserts comparator.compare(<original-value>, t) == 0.
 		 */
 		public void replace(T t) {
 			add(t, true);
 		}
 
 		public void remove(T t) {
 			allocator.discard(root);
 			root = createRootPage(remove(read(root).slots, t));
 		}
 
 		private void add(T t, boolean isReplace) {
 			allocator.discard(root);
 			root = createRootPage(add(read(root).slots, t, isReplace));
 		}
 
 		public List<Integer> commit() {
 			List<Integer> result = new ArrayList<>();
 			result.add(root.number);
 			result.addAll(allocator.commit());
 			return result;
 		}
 
 		private List<Slot> add(List<Slot> slots0, T t, boolean isReplace) {
 			Slot slot = null;
 			int i = 0, c = 1;
 
 			// Finds appropriate slot
 			while ((c = compare((slot = slots0.get(i)).pivot, t)) < 0)
 				i++;
 
 			// Adds the node into it
 			List<Slot> replaceSlots;
 
 			if (slot.pointer != null) {
 				allocator.discard(slot.pointer);
 				replaceSlots = add(slot.slots(), t, isReplace);
 			} else if (c != 0)
 				replaceSlots = Arrays.asList(new Slot(null, t), slot);
 			else if (isReplace)
 				replaceSlots = Arrays.asList(new Slot(null, t));
 			else
 				throw new RuntimeException("Duplicate node " + t);
 
 			List<Slot> slots1 = Util.add(Util.left(slots0, i), replaceSlots, Util.right(slots0, i + 1));
 
 			List<Slot> slots2;
 
 			// Checks if need to split
 			if (slots1.size() < maxSize)
 				slots2 = Arrays.asList(slot(slots1));
 			else { // Splits into two if reached maximum number of nodes
 				List<Slot> leftSlots = Util.left(slots1, halfSize);
 				List<Slot> rightSlots = Util.right(slots1, halfSize);
 				slots2 = Arrays.asList(slot(leftSlots), slot(rightSlots));
 			}
 
 			return slots2;
 		}
 
 		private List<Slot> remove(List<Slot> slots0, T t) {
 			int size = slots0.size();
 			Slot slot = null;
 			int i = 0, c = 1;
 
 			// Finds appropriate slot
 			while ((c = compare((slot = slots0.get(i)).pivot, t)) < 0)
 				i++;
 
 			// Removes the node from it
 			int s0 = i, s1 = i + 1;
 			List<Slot> replaceSlots;
 
 			if (c >= 0)
 				if (slot.pointer != null) {
 					List<Slot> slots1 = remove(slot.slots(), t);
 
 					// Merges with a neighbor if reached minimum number of nodes
 					if (slots1.size() < halfSize)
 						if (s0 > 0)
 							replaceSlots = merge(slots0.get(--s0).slots(), slots1);
 						else if (s1 < size)
 							replaceSlots = merge(slots1, slots0.get(s1++).slots());
 						else
 							replaceSlots = Arrays.asList(slot(slots1));
 					else
 						replaceSlots = Arrays.asList(slot(slots1));

					for (int s = s0; s < s1; s++)
						allocator.discard(slots0.get(s).pointer);
 				} else if (c == 0)
 					replaceSlots = Collections.emptyList();
 				else
 					throw new RuntimeException("Node not found " + t);
 			else
 				throw new RuntimeException("Node not found " + t);
 
 			return Util.add(Util.left(slots0, s0), replaceSlots, Util.right(slots0, s1));
 		}
 
 		private List<Slot> merge(List<Slot> slots0, List<Slot> slots1) {
 			List<Slot> merged;
 
 			if (slots0.size() + slots1.size() >= maxSize) {
 				List<Slot> leftSlots, rightSlots;
 
 				if (slots0.size() > halfSize) {
 					leftSlots = Util.left(slots0, -1);
 					rightSlots = Util.add(Arrays.asList(Util.last(slots0)), slots1);
 				} else {
 					leftSlots = Util.add(slots0, Arrays.asList(Util.first(slots1)));
 					rightSlots = Util.right(slots1, 1);
 				}
 
 				merged = Arrays.asList(slot(leftSlots), slot(rightSlots));
 			} else
 				merged = Arrays.asList(slot(Util.add(slots0, slots1)));
 
 			return merged;
 		}
 
 		private Pointer createRootPage(List<Slot> slots) {
 			Pointer pointer, pointer1;
 			if (slots.size() == 1 && (pointer1 = slots.get(0).pointer) != null)
 				pointer = pointer1;
 			else
 				pointer = persist(slots);
 			return pointer;
 		}
 
 		private Slot slot(List<Slot> slots) {
 			return new Slot(persist(slots), Util.last(slots).pivot);
 		}
 
 		private Pointer persist(List<Slot> slots) {
 			Page page = new Page();
 			page.slots = slots;
 
 			Pointer pointer = allocator.allocate();
 			write(pointer, page);
 			return pointer;
 		}
 	}
 
 	/**
 	 * Constructor for a small tree that would not span more than 1 page, i.e.
 	 * no extra "page allocation tree" is required.
 	 */
 	public B_TreeIndirect(String filename, Comparator<T> comparator, Serializer<T> serializer) throws FileNotFoundException {
 		this(filename, comparator, serializer, null);
 	}
 
 	/**
 	 * Constructor for larger trees that require another tree for page
 	 * allocation management.
 	 */
 	public B_TreeIndirect(String filename, Comparator<T> comparator, Serializer<T> serializer,
 			B_TreeIndirect<Pointer> allocationB_tree) throws FileNotFoundException {
 		this.comparator = comparator;
 		this.serializer = SerializeUtil.nullable(serializer);
 		this.allocationB_tree = allocationB_tree;
 		pageFile = new SerializedPageFile<>(filename, createPageSerializer());
 	}
 
 	@Override
 	public void close() throws IOException {
 		pageFile.close();
 	}
 
 	public Source<T> source(Transaction transaction) {
 		return source(transaction.root, null, null);
 	}
 
 	private Source<T> source(final Pointer pointer, final T start, final T end) {
 		return new Source<T>() {
 			private Deque<List<Slot>> stack = new ArrayDeque<>();
 
 			{
 				List<Slot> slots = read(pointer).slots;
 
 				while (true) {
 					int size = slots.size();
 					Slot slot = null;
 					int i = 0;
 
 					while (i < size && start != null && compare((slot = slots.get(i)).pivot, start) < 0)
 						i++;
 
 					if (slot != null) {
 						stack.push(Util.right(slots, i + 1));
 						slots = slot.slots();
 					} else {
 						stack.push(Util.right(slots, i));
 						break;
 					}
 				}
 			}
 
 			public T source() {
 				T t = null;
 				while (!stack.isEmpty() && (t = push(stack.pop())) == null)
 					;
 				return compare(t, end) < 0 ? t : null;
 			}
 
 			private T push(List<Slot> slots) {
 				while (!slots.isEmpty()) {
 					Slot slot0 = slots.get(0);
 					stack.push(Util.right(slots, 1));
 
 					if (slot0.pointer != null)
 						slots = slot0.slots();
 					else
 						return slot0.pivot;
 				}
 
 				return null;
 			}
 		};
 	}
 
 	public T find(Pointer pointer, T t) {
 		Slot slot = null;
 		int c = 1;
 
 		while (pointer != null) {
 			List<Slot> slots = read(pointer).slots;
 			int size = slots.size();
 			int i = 0;
 
 			while (i < size && (c = compare((slot = slots.get(i)).pivot, t)) < 0)
 				i++;
 
 			pointer = slot.pointer;
 		}
 
 		return c == 0 ? slot.pivot : null;
 	}
 
 	public static List<Integer> initializeAllocator(B_TreeIndirect<Pointer> b_tree, List<Integer> chain, int nPages) {
 		B_TreeIndirect<Pointer>.Transaction transaction = b_tree.init(chain);
 		for (int p = 0; p < nPages; p++) {
 			Pointer pointer = new Pointer();
 			pointer.number = p;
 			transaction.add(pointer);
 		}
 		return transaction.commit();
 	}
 
 	public List<Integer> initialize(List<Integer> chain) {
 		return init(chain).commit();
 	}
 
 	public Transaction init(List<Integer> chain) {
 		return new Transaction(allocator(chain));
 	}
 
 	public Transaction transaction(List<Integer> chain) {
 		Pointer root = new Pointer();
 		root.number = chain.get(0);
 		return new Transaction(allocator(Util.right(chain, 1)), root);
 	}
 
 	private Allocator allocator(List<Integer> chain) {
 		boolean isBta = allocationB_tree != null;
 		return isBta ? new B_TreeAllocator(allocationB_tree, chain) : new SwappingAllocator(chain.get(0));
 	}
 
 	private int compare(T t0, T t1) {
 		boolean b0 = t0 != null;
 		boolean b1 = t1 != null;
 
 		if (b0 && b1)
 			return comparator.compare(t0, t1);
 		else
 			return b0 ? -1 : b1 ? 1 : 0;
 	}
 
 	private Map<Integer, Page> disk = new HashMap<>();
 
 	private Page read(Pointer pointer) {
 		return disk.get(pointer.number);
 		// return pageFile.load(pointer.number);
 	}
 
 	private void write(Pointer pointer, Page page) {
 		disk.put(pointer.number, page);
 		// pageFile.save(pointer.number, page);
 	}
 
 	private Serializer<Page> createPageSerializer() {
 		final Serializer<List<Slot>> slotsSerializer = SerializeUtil.list(new Serializer<Slot>() {
 			public Slot read(ByteBuffer buffer) {
 				return new Slot(Pointer.serializer.read(buffer), serializer.read(buffer));
 			}
 
 			public void write(ByteBuffer buffer, Slot slot) {
 				Pointer.serializer.write(buffer, slot.pointer);
 				serializer.write(buffer, slot.pivot);
 			}
 		});
 
 		return new Serializer<Page>() {
 			public Page read(ByteBuffer buffer) {
 				Page page = new Page();
 				page.slots = slotsSerializer.read(buffer);
 				return page;
 			}
 
 			public void write(ByteBuffer buffer, Page page) {
 				slotsSerializer.write(buffer, page.slots);
 			}
 		};
 	}
 
 }
