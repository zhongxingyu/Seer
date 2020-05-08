 package com.barchart.store.heap;
 
 import java.nio.ByteBuffer;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.atomic.AtomicBoolean;
 
 import rx.Observable;
 import rx.Observer;
 import rx.Subscription;
 import rx.util.functions.Func1;
 
 import com.barchart.store.api.ObservableIndexQueryBuilder;
 import com.barchart.store.api.StoreColumn;
 import com.barchart.store.api.StoreRow;
 
 public class HeapIndexQueryBuilder<T> extends QueryBuilderBase<T> implements
 		ObservableIndexQueryBuilder<T> {
 
 	protected final Collection<HeapRow<T>> rows;
 	final Map<T, Map<Object, Collection<HeapRow<T>>>> indexes;
 	final List<FieldCompare> filters;
 
 	public HeapIndexQueryBuilder(
 			final Map<T, Map<Object, Collection<HeapRow<T>>>> indexes_) {
 		indexes = indexes_;
 		filters = new ArrayList<FieldCompare>();
 		if (indexes == null) {
 			rows = Collections.<HeapRow<T>> emptySet();
 		} else {
 			rows = new HashSet<HeapRow<T>>();
 		}
 	}
 
 	@Override
 	public ObservableIndexQueryBuilder<T> where(final T column,
 			final Object value) {
 		return where(column, value, Operator.EQUAL);
 	}
 
 	@Override
 	public ObservableIndexQueryBuilder<T> where(final T column,
 			final Object value,
 			final com.barchart.store.api.ObservableIndexQueryBuilder.Operator op) {
 		filters.add(new FieldCompare(column, op, value));
 		return this;
 	}
 
 	@Override
 	public Observable<StoreRow<T>> build() {
 		return build(0);
 	}
 
 	@Override
 	public Observable<StoreRow<T>> build(final int limit) {
 
 		if (indexes != null) {
 
 			boolean validQuery = (filters.size() == 0);
 
 			for (final FieldCompare fc : filters) {
 				if (fc.operator == Operator.EQUAL) {
 					validQuery = true;
 					break;
 				}
 			}
 
 			if (!validQuery) {
 				throw new IllegalArgumentException(
 						"Secondary index queries must contain at least one EQUAL term");
 			}
 
 			// This shit is really inefficient, avoid LT/GT filters on large
 			// data sets for StoreHeap
 			boolean first = true;
 			for (final FieldCompare fc : filters) {
 				final Iterator<HeapRow<T>> iter;
 				switch (fc.operator) {
 					case GT:
 						iter = rows.iterator();
 						while (iter.hasNext()) {
 							final HeapRow<T> row = iter.next();
 							try {
 								if (compare(fc.value, row.get(fc.column)) <= 0) {
 									iter.remove();
 								}
 							} catch (final Exception e) {
 								iter.remove();
 							}
 						}
 						break;
 					case GTE:
 						iter = rows.iterator();
 						while (iter.hasNext()) {
 							final HeapRow<T> row = iter.next();
 							try {
 								if (compare(fc.value, row.get(fc.column)) < 0) {
 									iter.remove();
 								}
 							} catch (final Exception e) {
 								iter.remove();
 							}
 						}
 						break;
 					case LT:
 						iter = rows.iterator();
 						while (iter.hasNext()) {
 							final HeapRow<T> row = iter.next();
 							try {
 								if (compare(fc.value, row.get(fc.column)) >= 0) {
 									iter.remove();
 								}
 							} catch (final Exception e) {
 								iter.remove();
 							}
 						}
 						break;
 					case LTE:
 						iter = rows.iterator();
 						while (iter.hasNext()) {
 							final HeapRow<T> row = iter.next();
 							try {
 								if (compare(fc.value, row.get(fc.column)) > 0) {
 									iter.remove();
 								}
 							} catch (final Exception e) {
 								iter.remove();
 							}
 						}
 						break;
 					case EQUAL:
 					default:
 						final Collection<HeapRow<T>> matches =
 								indexes.get(fc.column).get(fc.value);
 						if (rows.size() == 0 && first) {
							rows.addAll(matches);
 						} else {
 							if (matches != null) {
 								rows.retainAll(matches);
 							} else {
 								rows.clear();
 							}
 						}
 				}
 				first = false;
 			}
 
 		}
 
 		return Observable
 				.create(new Func1<Observer<StoreRow<T>>, Subscription>() {
 
 					@Override
 					public Subscription call(final Observer<StoreRow<T>> o) {
 
 						final AtomicBoolean running = new AtomicBoolean(true);
 						int ct = 0;
 
 						try {
 
 							for (final HeapRow<T> row : rows) {
 								if (!running.get()
 										|| (limit > 0 && ct >= limit)) {
 									o.onCompleted();
 									break;
 								}
 								o.onNext(new RowFilter(row));
 								ct++;
 							}
 
 							o.onCompleted();
 
 						} catch (final Exception e) {
 							o.onError(e);
 						}
 
 						return new Subscription() {
 
 							@Override
 							public void unsubscribe() {
 								running.set(false);
 							}
 
 						};
 
 					}
 
 				});
 
 	}
 
 	@Override
 	public Observable<StoreRow<T>> build(final int limit, final int batchSize) {
 		return build(limit);
 	}
 
 	private int compare(final Object o1, final StoreColumn<T> o2)
 			throws Exception {
 
 		if (o1.getClass() == String.class) {
 			return ((String) o1).compareTo(o2.getString());
 		} else if (o1.getClass() == byte[].class) {
 			return ByteBuffer.wrap((byte[]) o1).compareTo(o2.getBlob());
 		} else if (o1.getClass() == Boolean.class) {
 			if (!o1.equals(o2.getBoolean())) {
 				return (Boolean) o1 ? 1 : -1;
 			}
 		} else if (o1.getClass() == ByteBuffer.class) {
 			return ((ByteBuffer) o1).compareTo(o2.getBlob());
 		} else if (o1.getClass() == Double.class) {
 			return ((Double) o1).compareTo(o2.getDouble());
 		} else if (o1.getClass() == Integer.class) {
 			return ((Integer) o1).compareTo(o2.getInt());
 		} else if (o1.getClass() == Long.class) {
 			return ((Long) o1).compareTo(o2.getLong());
 		} else if (o1.getClass() == Date.class) {
 			return ((Date) o1).compareTo(o2.getDate());
 		}
 
 		return 0;
 
 	}
 
 	private class FieldCompare {
 		public T column;
 		public Operator operator;
 		public Object value;
 
 		public FieldCompare(final T column_, final Operator operator_,
 				final Object value_) {
 			column = column_;
 			operator = operator_;
 			value = value_;
 		}
 	}
 
 }
