 package suite.immutable;
 
 import java.util.ArrayDeque;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Deque;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import suite.util.FunUtil.Source;
 import suite.util.Util;
 
 public class B_TreeIndirect<T> {
 
 	private int maxSize = 4;
 	private int halfSize = maxSize / 2;
 
 	private Comparator<T> comparator;
 	private B_TreeIndirect<Pointer> allocationB_tree;
 
 	private Storage storage = new Storage();
 
 	private class Storage {
 		private Map<Pointer, Page> disk = new HashMap<>();
 
 		private Page read(Pointer pointer) {
 			return disk.get(pointer);
 		}
 
 		private void write(Pointer pointer, Page page) {
 			disk.put(pointer, page);
 		}
 	}
 
 	private static class Pointer {
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
 			return pointer != null ? storage.read(pointer).slots : null;
 		}
 	}
 
 	private interface Allocator {
 		public Pointer allocate();
 
 		public void discard(Pointer pointer);
 
 		public List<Object> commit();
 	}
 
 	public class SwappingAllocator implements Allocator {
 		private List<Pointer> pointers = Arrays.asList(new Pointer(), new Pointer());
 		private int using = 0;
 
 		public Pointer allocate() {
 			return pointers.get(using);
 		}
 
 		public void discard(Pointer pointer) {
 		}
 
 		public List<Object> commit() {
			List<Object> pointer = Arrays.asList((Object) using);
			using = 1 - using;
			return pointer;
 		}
 	}
 
 	public class B_TreeAllocator implements Allocator {
 		private B_TreeIndirect<Pointer> b_tree;
 		private B_TreeIndirect<Pointer>.Transaction transaction;
 
 		public B_TreeAllocator(B_TreeIndirect<Pointer> b_tree) {
 			this.b_tree = b_tree;
 			this.transaction = b_tree.transaction();
 		}
 
 		public Pointer allocate() {
 			Pointer pointer = b_tree.source(transaction.root).source();
 			transaction.remove(pointer);
 			return pointer;
 		}
 
 		public void discard(Pointer pointer) {
 			transaction.add(pointer);
 		}
 
 		public List<Object> commit() {
 			return transaction.commit();
 		}
 	}
 
 	public class Transaction {
 		private Pointer root;
 		private Allocator allocator;
 
 		public Transaction(Allocator allocator) {
 			this.allocator = allocator;
 		}
 
 		public void create() {
 			root = persist(Arrays.asList(new Slot(null, null)));
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
 			root = createRootPage(remove(storage.read(root).slots, t));
 		}
 
 		private void add(T t, boolean isReplace) {
 			allocator.discard(root);
 			root = createRootPage(add(storage.read(root).slots, t, isReplace));
 		}
 
 		public List<Object> commit() {
 			List<Object> result = new ArrayList<>();
 			result.add(root);
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
 
 			if (slot.pointer != null)
 				replaceSlots = add(slot.slots(), t, isReplace);
 			else if (c != 0)
 				replaceSlots = Arrays.asList(new Slot(null, t), slot);
 			else if (isReplace)
 				replaceSlots = Arrays.asList(new Slot(null, t));
 			else
 				throw new RuntimeException("Duplicate node " + t);
 
 			List<Slot> slots1 = Util.add(Util.left(slots0, i), replaceSlots, Util.right(slots0, i + 1));
 			difference(slots0.subList(i, i + 1), replaceSlots);
 
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
 			while (i < size && (c = compare((slot = slots0.get(i)).pivot, t)) < 0)
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
 				} else
 					replaceSlots = Collections.emptyList();
 			else
 				throw new RuntimeException("Node not found " + t);
 
 			difference(slots0.subList(s0, s1), replaceSlots);
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
 
 		private void difference(List<Slot> slots0, List<Slot> slots1) {
 			Set<Pointer> pointers0 = new HashSet<>();
 			Set<Pointer> pointers1 = new HashSet<>();
 
 			for (Slot slot : slots0)
 				pointers0.add(slot.pointer);
 			for (Slot slot : slots1)
 				pointers1.add(slot.pointer);
 
 			for (Pointer pointer : Util.subtract(pointers0, pointers1))
 				allocator.discard(pointer);
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
 			storage.write(pointer, page);
 			return pointer;
 		}
 	}
 
 	/**
 	 * Constructor for a small tree that would not span more than 1 page, i.e.
 	 * no extra "page allocation tree" is required.
 	 */
 	public B_TreeIndirect(Comparator<T> comparator) {
 		this(comparator, null);
 	}
 
 	public B_TreeIndirect(Comparator<T> comparator, B_TreeIndirect<Pointer> allocationB_tree) {
 		this.comparator = comparator;
 		this.allocationB_tree = allocationB_tree;
 	}
 
 	public Source<T> source(Pointer pointer) {
 		return source(pointer, null, null);
 	}
 
 	private Source<T> source(final Pointer pointer, final T start, final T end) {
 		return new Source<T>() {
 			private Deque<List<Slot>> stack = new ArrayDeque<>();
 
 			{
 				List<Slot> slots = storage.read(pointer).slots;
 
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
 			List<Slot> slots = storage.read(pointer).slots;
 			int size = slots.size();
 			int i = 0;
 
 			while (i < size && (c = compare((slot = slots.get(i)).pivot, t)) < 0)
 				i++;
 
 			pointer = slot.pointer;
 		}
 
 		return c == 0 ? slot.pivot : null;
 	}
 
 	public Transaction transaction() {
 		Allocator allocator = allocationB_tree != null ? new B_TreeAllocator(allocationB_tree) : new SwappingAllocator();
 		return new Transaction(allocator);
 	}
 
 	private int compare(T t0, T t1) {
 		boolean b0 = t0 != null;
 		boolean b1 = t1 != null;
 
 		if (b0 && b1)
 			return comparator.compare(t0, t1);
 		else
 			return b0 ? -1 : b1 ? 1 : 0;
 	}
 
 }
