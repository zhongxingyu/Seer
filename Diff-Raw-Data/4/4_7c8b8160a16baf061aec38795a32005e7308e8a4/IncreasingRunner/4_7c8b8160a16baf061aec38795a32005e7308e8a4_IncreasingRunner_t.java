 import java.lang.reflect.Method;
 import java.util.Arrays;
 import java.util.Random;
 
 import org.nauxiancc.executor.Result;
 import org.nauxiancc.interfaces.Runner;
 
 public class IncreasingRunner extends Runner {
 
 	@Override
 	public Result[] getResults(Class<?> clazz) {
 		try {
 			Random ran = new Random();
 			Method method = clazz.getMethod("increasing", int[].class);
 			Result[] results = new Result[10];
 			for (int i = 0; i < 10; i++) {
 				int a = ran.nextInt(7), b = ran.nextInt(14), c = ran.nextInt(21);
				results[i] = new Result(
                        method.invoke(clazz.newInstance(), new int[]{a, b, c}),
                        increasing(new int[] { a, b, c }),
 						Arrays.toString(new int[] { a, b, c }));
 			}
 			return results;
 		} catch (Exception e) {
 			return new Result[] {};
 		}
 	}
 
 	private boolean increasing(int[] nums) {
 		int lowest = -2147483648;
 		for (final int num : nums) {
 			if (num < lowest) {
 				return false;
 			} else {
 				lowest = num;
 			}
 		}
 		return true;
 	}
 }
