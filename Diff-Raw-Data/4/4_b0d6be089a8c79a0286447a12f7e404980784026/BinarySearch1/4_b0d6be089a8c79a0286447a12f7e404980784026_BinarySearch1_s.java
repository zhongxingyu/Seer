 package cn.sunjiachao.s7common.arithmetic.search;
 
 /**
 * 二分查找算法
  * 
  * @author jiachao.sun
  * 
  */
 public class BinarySearch1 {
 
 	public int[] array = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
 
 	public BinarySearch1(int[] array) {
 		this.array = array;
 	}
 
 	public BinarySearch1() {
 
 	}
 
 	public int search(int value) {
 		// 定义初始状态的边界
 		int start = 0;
 		int end = array.length - 1;
 
 		// 开始查找
 		while (true) {
 			int current = (start + end) / 2;
 			if (array[current] == value)
 				// 找到
 				return current;
 			if (start > end)
 				// 左边大于右边 错误
 				return -1;
 
 			// 当前值小于查找值，说明查找值在current右边（大于中点）
 			if (array[current] < value)
 				start = current + 1;
 			else
 				end = current - 1;
 
 		}
 	}
 
 	public static void main(String[] args) {
 
 		BinarySearch1 app = new BinarySearch1();
 		int result = app.search(7);
 		System.out.println(result);
 
 	}
 
 }
