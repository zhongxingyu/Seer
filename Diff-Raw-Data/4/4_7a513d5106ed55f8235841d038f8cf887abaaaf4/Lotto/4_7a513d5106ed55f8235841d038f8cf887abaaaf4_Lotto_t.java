 package lotto;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
 
 public class Lotto {
 
 	static class LottoNumber {
 		static List<Integer> digit45 = new ArrayList<Integer>(45);
 		static { for (int i = 1; i < 46; i++) digit45.add(i); }
 
 		private List<Integer> lotto;
 		
 		public LottoNumber() {
 			Collections.shuffle(digit45);
 			lotto = digit45.subList(0, 6);
 			Collections.sort(lotto);
 		}
 		
 		public boolean equalNumber(LottoNumber other) {
 			for (int i = 0; i < 6; i++)
 				if (lotto.get(i) != other.lotto.get(i))
 					return false;
 			return true;
 		}
 		
 		public void print() {
 			for (Integer i : lotto)
 				System.out.print(i + " ");
 			System.out.println();
 		}
 	}
 	
 	static class LottoBuyer implements Runnable {
 		private final LottoHouse lottoHouse;
 
 		public LottoBuyer(LottoHouse lottoHouse) {
 			this.lottoHouse = lottoHouse;
 		}
 
 		@Override
 		public void run() {
 			for (;;) {
 				lottoHouse.add(new LottoNumber());
 				try {
 					Thread.sleep(1000);
 				} catch (InterruptedException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 	}
 	
 	static class LottoHouse implements Runnable {
		CopyOnWriteArrayList<LottoNumber> lottos = new CopyOnWriteArrayList<LottoNumber>();
 		void add(LottoNumber lotto) {
 			lotto.print();
 			lottos.add(lotto);
 		}
 		private int getHitCountBy(LottoNumber thisLotto) {
 			int hitCount = 0;
 			for (LottoNumber lotto : lottos) {
 				System.out.print("<< ");
 				thisLotto.print();
 				System.out.print(">> ");
 				lotto.print();
 				if (thisLotto.equalNumber(lotto))
 					hitCount++;
 			}
 			return hitCount;
 		}
 		
 		@Override
 		public void run() {
 			for (;;) {
 				System.out.println("House--------");
 				try {
 					Thread.sleep(4000);
 				} catch (InterruptedException e) {
 					e.printStackTrace();
 				}
 
 				LottoNumber thisLotto = new LottoNumber();
 				System.out.print("...");
 				thisLotto.print();
 				int thisHitCount = getHitCountBy(thisLotto);
 				print(thisHitCount, thisLotto);
 			}
 		}
 		
 		private void print(int thisHitCount, LottoNumber thisLotto) {
 			System.out.println("------------");
 			System.out.println("HIT : " + thisHitCount);
 			thisLotto.print();
 			System.out.println("------------");
 		}
 
 	}
 	
 	public static void main(String[] args) {
 //		for (int i = 0; i < 10; i++) {
 //			new LottoNumber().print();
 //		}
 		LottoHouse lottoHouse = new LottoHouse();
 		LottoBuyer lottoBuyer = new LottoBuyer(lottoHouse);
 		new Thread(lottoBuyer).start();
 		new Thread(lottoHouse).start();
 	}
 }
