 package algorithm.common;
 
 public class MaxSum {
 	
 	/**
 	 * 最直观的的方法O(n^2)
 	 * @param list
 	 * @return
 	 */
 	public static int maxSum(int[] list) {
 		int maxSum = 0;
 		for (int i = 0; i < list.length; i++) {
 			int thisSum = 0;
 			for (int j = i; j < list.length; j++) {
 				thisSum += list[j];
 				if(thisSum > maxSum)
 					maxSum = thisSum;
 			}
 		}
 		return maxSum;
 	}
 	
 	/**
 	 * 若前面序列的和已经小于等于0，则可以抛弃前面的序列O(n)
 	 * @param list
 	 * @return
 	 */
 	public static int maxSum2(int[] list) {
		int maxSum = -10000; //需要定义全是负数时返回什么-INF/0
 		int tempSum = 0;
 		for (int i = 0; i < list.length; i++) {
 			tempSum += list[i];
 			if(tempSum > maxSum)
 				maxSum = tempSum;
			if(tempSum <= 0)
 				tempSum = 0;
 		}
 		return maxSum;
 	}
 	/**
 	 * 递归，只可能出现三种情况 O(nlogn)
 	 * @param list
 	 * @param left
 	 * @param right
 	 * @return
 	 */
 	public static int maxSumRec(int[] list, int left, int right) {
 		//递归终止条件
 		if(left == right) {
 			if(list[left] > 0)
 				return list[left];
 			else
 				return 0;
 		}
 		
 		int center = (left + right) / 2;
 		//计算左侧最大子序列
 		int maxLeftSum = maxSumRec(list, left, center);
 		//计算右侧最大子序列
 		int maxRightSum = maxSumRec(list, center+1, right);
 		//计算中间最大子序列
 		int maxLeftBoundSum = 0;
 		int maxLeftTempCount = 0;
 		for (int i = center; i >= left; i--) {
 			maxLeftTempCount += list[i];
 			if(maxLeftTempCount > maxLeftBoundSum)
 				maxLeftBoundSum = maxLeftTempCount;
 		}
 		int maxRightBoundSum = 0;
 		int maxRightTempCount = 0;
 		for (int i = center+1; i <= right; i++) {
 			maxRightTempCount += list[i];
 			if(maxRightTempCount > maxRightBoundSum)
 				maxRightBoundSum = maxRightTempCount;
 		}
 		
 		int maxMidSum = maxLeftBoundSum + maxRightBoundSum;
 		return maxInThree(maxLeftSum, maxRightSum, maxMidSum);
 	}
 	
 	private static int maxInThree(int num1, int num2, int num3) {
 		int maxNum = num1;
 		if(num2 > maxNum)
 			maxNum = num2;
 		if(num3 > maxNum)
 			maxNum = num3;
 		return maxNum;
 	}
 	
 	public static void main(String[] args) {
		int[] list = new int[]{-9,-2,-3,-1,-4};
 		System.out.println(MaxSum.maxSum(list));
 		System.out.println(MaxSum.maxSum2(list));
 		System.out.println(MaxSum.maxSumRec(list, 0, list.length-1));
 	}
 }
