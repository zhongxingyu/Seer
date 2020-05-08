 package lotto;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.concurrent.CopyOnWriteArrayList;
 
 public class Lotto {
 
 	static class LottoNumber {
 		public static int MAX_NUMBER_OF_LOTTO = 46;
 		public static int MAX_COUNT_OF_LOTTO  = 6;
 		static List<Integer> digits = new ArrayList<Integer>(MAX_NUMBER_OF_LOTTO);
 		static { for (int i = 1; i <= MAX_NUMBER_OF_LOTTO; i++) digits.add(i); }
 
 		private List<Integer> lotto;
 		
 		public LottoNumber() {
 			Collections.shuffle(digits);
 			lotto = new ArrayList<Integer>(digits.subList(0, MAX_COUNT_OF_LOTTO));
 			Collections.sort(lotto);
 		}
 		
 		public boolean equalNumber(LottoNumber other) {
 			for (int i = 0; i < MAX_COUNT_OF_LOTTO; i++)
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
 //				try {
 //					Thread.sleep(5);
 //				} catch (InterruptedException e) {
 //					e.printStackTrace();
 //				}
 			}
 		}
 	}
 	
 	static class LottoHouse implements Runnable {
 		List<LottoNumber> lottos = new ArrayList<LottoNumber>();
 		void add(LottoNumber lotto) {
 //			lotto.print();
 			lottos.add(lotto);
 		}
 		private int getHitCountBy(LottoNumber thisLotto) {
 			int hitCount = 0;
 			int sizeOfLottos = lottos.size();
			System.out.print("(" + sizeOfLottos + ":");
 			for (int i = 0; i < sizeOfLottos; i++ )
 				if (thisLotto.equalNumber(lottos.get(i)))
 					hitCount++;
			System.out.println(lottos.size() + ") ");

 //			for (LottoNumber lotto : lottos) {
 ////				System.out.print("<< ");
 ////				thisLotto.print();
 ////				System.out.print(">> ");
 ////				lotto.print();
 //				if (thisLotto.equalNumber(lotto))
 //					hitCount++;
 //			}
 			return hitCount;
 		}
 		
 		@Override
 		public void run() {
 			for (;;) {
 //				System.out.println("House--------");
 				try {
 					Thread.sleep(6000);
 				} catch (InterruptedException e) {
 					e.printStackTrace();
 				}
 				LottoNumber thisLotto = new LottoNumber();
 
 //				System.out.print("...");
 //				thisLotto.print();
 				int thisHitCount = getHitCountBy(thisLotto);
 				print(thisHitCount, thisLotto);
 				lottos.clear();
 			}
 		}
 		
 		private void print(int thisHitCount, LottoNumber thisLotto) {
 			System.out.println("------------");
 			System.out.println("HIT : " + thisHitCount + "/" + lottos.size());
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
