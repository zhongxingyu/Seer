 import java.lang.reflect.Method;
 import java.util.Random;
 
 import org.nauxiancc.executor.Result;
 import org.nauxiancc.interfaces.Runner;
 
 public class CanReachRunner extends Runner {
 
 	@Override
 	public Result[] getResults(Class<?> clazz) {
 		try {
 			final Method method = clazz.getMethod("canReach", int.class, int.class, int.class);
 			final Random ran = new Random();
 			final Result[] results = new Result[10];
 			for (int i = 0; i < 10; i++) {
				final int small = ran.nextInt(15) + 5;
				final int large = ran.nextInt(5) + 5;
				final int goal = (small + large * 5) + 2 + ran.nextInt(6);
				results[i] = new Result(method.invoke(clazz.newInstance(), small, large, goal), (goal > small + large * 5) && goal % 5 <= small, small, large, goal);
 			}
 			return results;
 		} catch (Exception e) {
 			return new Result[] {};
 		}
 	}
 }
