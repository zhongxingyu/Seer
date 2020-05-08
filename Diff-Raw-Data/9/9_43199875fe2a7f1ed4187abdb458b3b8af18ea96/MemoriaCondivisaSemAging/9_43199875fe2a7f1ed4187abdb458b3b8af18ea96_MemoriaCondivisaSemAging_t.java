 /*
 
 Si implementi, estendendo la classe MemoriaCondivisa,
 una soluzione con i semafori del problema dei lettori scrittori
 del terzo tipo, utilizzando la tecnica dell'aging.
 
 */
 
 import java.util.concurrent.Semaphore;
 import java.util.Map;
 import java.util.HashMap;
 
 public class MemoriaCondivisaSemAging extends MemoriaCondivisa {
 
 	Semaphore mutex = new Semaphore(1);
	Semaphore mutex2 = new Semaphore(1);
 	Semaphore scrittura = new Semaphore(1);
 	Semaphore waitLettori = new Semaphore(0);
 
 	public static final int INITIAL_PRIORITY = 10;
 	public static final int MIN_PRIORITY = 1;
 	private int numLettori = 0, lettoriInAttesa = 0, ageLettori = INITIAL_PRIORITY;
 	private boolean scrittoreInAttesa = false;
 
 	public void inizioScrittura() throws InterruptedException {
 		mutex.acquire();
 		scrittoreInAttesa = true;
 		mutex.release();
 		scrittura.acquire();
 		System.out.println("Thread " + Thread.currentThread().getId() + " inizia a scrivere");
 		System.out.println("\tNumero lettori: " + numLettori);
 	}
 
 	public void fineScrittura() throws InterruptedException {
 		System.out.println("\tNumero lettori: " + numLettori);
 		System.out.println("Thread " + Thread.currentThread().getId() + " finisce di scrivere");
		mutex2.acquire();
 		ageLettori = INITIAL_PRIORITY;
 		waitLettori.release(lettoriInAttesa);
 		lettoriInAttesa = 0;
		mutex2.release();
 		scrittura.release();
 	}
 
 	public void inizioLettura() throws InterruptedException {
 		mutex.acquire();
 		if (ageLettori < MIN_PRIORITY) {
			mutex2.acquire();
 			lettoriInAttesa++;
			mutex2.release();
 			mutex.release();
 			waitLettori.acquire();
 			mutex.acquire();
 		}
 		if (numLettori == 0) {
 			scrittura.acquire();
 			scrittoreInAttesa = false;
 		}
 		numLettori++;
 		if (scrittoreInAttesa) ageLettori--;
 		System.out.println("Thread " + Thread.currentThread().getId() + " inizia a leggere");
 		mutex.release();
 	}
 
 	public void fineLettura() throws InterruptedException {
 		mutex.acquire();
 		numLettori--;
 		System.out.println("Thread " + Thread.currentThread().getId() + " finisce di leggere");
 		if (numLettori == 0) scrittura.release();
 		mutex.release();
 	}
 
 	public static void main(String[]args) {
 		MemoriaCondivisa m = new MemoriaCondivisaSemAging();
 		m.test(20, 5);
 	}
 }
