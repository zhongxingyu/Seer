 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.BufferedReader;
 public class RPN{
     public static void main(String[] args) {
 	// スタック
 	IntStack is = new IntStack(100);
 	int len = 0;
 	int space = 0;
 	String input[];
 	BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
 	try{
 	    String line = br.readLine();
 	    input = line.split("\\s");
 	    len = input.length;
 	    for (int i = 0; i < len; i++) {
 		// 文字列が数字かどうかを判定
 		if(isDigit(input[i])){
 		    // 数字なのでスタックにプッシュ
 		    is.push(Integer.parseInt(input[i]));
 		    System.out.println(i+": push to stack:"+Integer.parseInt(input[i]));
 		}else{
 		    // 数字でないということは演算子なので計算する
 		    if(input[i].equals("+")){
 			int m = is.pop();
 			int n = is.pop();
 			is.push(m+n);
 			System.out.println(i+": push to stack:"+(m+n));
 		    }else if(input[i].equals("-")){
 			int m = is.pop();
 			int n = is.pop();
 			// LIFOなので引く順番が逆
 			System.out.println(m+":"+n);
 			is.push(n-m);
 		    }
 		    else if(input[i].equals("*")){
 			int m = is.pop();
 			int n = is.pop();
 			System.out.println(m+":"+n);
 			is.push(m*n);
 		    }
 		}		
 	    }
 	// 結果を示す(最上段をポップ)
 	System.out.println(is.pop());
 
 	}catch(IOException e){
 	    e.printStackTrace();
	}
	
     }
     //--- 文字列が数字かどうかを判定するメソッド ---//
     public static boolean isDigit(String s){
 	try{
 	    Integer.parseInt(s);
 	    return true;
 	}catch(NumberFormatException e){
 	    return false;
 	}
     }
 }
 // 簡単なスタックを実現
 class IntStack {
     private int max;
     private int ptr;
     private int[] stk;
     public IntStack(int capacity) {
 	max = capacity;
 	ptr = 0;
 	try {
 	    stk = new int[max];
 	} catch (OutOfMemoryError e)  {
 	    e.printStackTrace();
 	    max = 0;
 	}
     }
 	public int push(int x){
 	    if(ptr >= max)
 		System.out.println("Running push error");
 	    return stk[ptr++] = x;
 	}
     public int pop(){
 	if(ptr <= 0)
 	    System.out.println("Running pop error");
 	return stk[--ptr];
     }
 }

