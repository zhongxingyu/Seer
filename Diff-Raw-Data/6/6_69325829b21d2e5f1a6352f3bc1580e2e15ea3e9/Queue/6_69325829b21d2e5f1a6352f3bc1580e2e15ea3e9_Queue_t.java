 public class Queue<V> {
 
 	private V[] queue;
 	private int head = 0;
 	private int tail = 0;
 	private int length = 0;
 	private final V EXIT_MESSAGE;
 
 	@SuppressWarnings("unchecked")
 	public Queue(int cap, V EXIT_MESSAGE) {
 		this.EXIT_MESSAGE = EXIT_MESSAGE;
 		queue = (V[]) new V[cap];
 	}
 
 	public synchronized void enqueue(V item) {
 		while (full()) {
 			try {
 				wait();
 			} catch (InterruptedException e) {
 				throw new RuntimeException(e);
 			}
 		}
 		queue[tail] = item;
 		if (++tail == queue.length) {
 			tail = 0;
 		}
 		length++;
 		notifyAll();
 	}
 
 	public synchronized V dequeue() {
 		while (empty()) {
 			try {
 				wait();
 			} catch (InterruptedException e) {
 				throw new RuntimeException(e);
 			}
 		}
 		V val = queue[head];
                queue[head] = null;
 		if (++head == queue.length) {
 			head = 0;
 		}
 		length--;
 		notifyAll();
 		return val;
 	}
 
 	public boolean full() {
 		return length == queue.length;
 	}
 
 	public boolean empty() {
 		return length == 0;
 	}
 
 	public V getExitMessage() {
 		return EXIT_MESSAGE;
 	}
}
