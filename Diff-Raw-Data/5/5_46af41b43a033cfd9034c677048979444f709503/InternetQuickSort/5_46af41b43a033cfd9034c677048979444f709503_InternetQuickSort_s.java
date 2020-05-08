package se751.team13.quicksort;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.concurrent.BrokenBarrierException;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
 class QuickSortTask<T extends Comparable<? super T>> implements Runnable {
 	List<T> data;
 	int base;
 	int n;
 	QuickSort<T> manager;
 
 	public QuickSortTask(List<T> data, int base, int n, QuickSort<T> manager) {
 		this.data = data;
 		this.base = base;
 		this.n = n;
 		this.manager = manager;
 	}
 
 	static final int SORT_DIRECT = 200;
 
 	public void run() {
 		int i, j;
 		if (n <= SORT_DIRECT) {
 			for (j = 1; j < n; j++) {
 				T key = data.get(base + j);
 				for (i = j - 1; i >= 0 && data.get(base + i).compareTo(key) < 0; i--)
 					data.set(base + i + 1, data.get(base + i));
 				data.set(base + i + 1, key);
 			}
 			manager.task_done();
 			return;
 		}
 		i = 0;
 		j = n - 1;
 		while (true) {
 			while (data.get(base + i).compareTo(data.get(base + j)) < 0)
 				j--;
 			if (i >= j)
 				break;
 			{
 				T t = data.get(base + i);
 				data.set(base + i, data.get(base + j));
 				data.set(base + j, t);
 			} /* swap */
 			i++;
 			while (data.get(base + i).compareTo(data.get(base + j)) < 0)
 				i++;
 			if (i >= j) {
 				i = j;
 				break;
 			}
 			{
 				T t = data.get(base + i);
 				data.set(base + i, data.get(base + j));
 				data.set(base + j, t);
 			} /* swap */
 			j--;
 		}
 		manager.add_task(data, base, i);
 		manager.add_task(data, base + i + 1, n - i - 1);
 		manager.task_done();
 	}
 }
 
 class QuickSort<T extends Comparable<? super T>> {
 	int task_count;
 	ExecutorService exec;
 
 	public QuickSort(int n_threads) {
 		task_count = 0;
 		exec = Executors.newFixedThreadPool(n_threads);
 	}
 
 	public synchronized void add_task(List<T> data, int base, int n) {
 		task_count++;
 		Runnable task = new QuickSortTask<T>(data, base, n, this);
 		exec.execute(task);
 	}
 
 	public synchronized void task_done() {
 		task_count--;
 		if (task_count <= 0)
 			notify();
 	}
 
 	public synchronized void work_wait() throws java.lang.InterruptedException {
 		while (task_count > 0) {
 			wait();
 		}
 		exec.shutdown();
 	}
 }
 
 
 class InPlaceQuickSort<T extends Comparable<? super T>> implements Sorter<T> {
 	@Override
 	public List<T> sort(List<T> unsorted) throws InterruptedException,
 			BrokenBarrierException {
 		ArrayList<T> data = new ArrayList<T>(unsorted);
 
 		QuickSort<T> qs = new QuickSort<T>(Runtime.getRuntime()
 				.availableProcessors());
 		qs.add_task(data, 0, unsorted.size());
 		qs.work_wait();
 
 		return data;
 	}
 }
