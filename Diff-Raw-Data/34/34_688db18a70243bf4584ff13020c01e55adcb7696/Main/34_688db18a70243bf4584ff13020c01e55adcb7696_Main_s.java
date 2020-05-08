 package id10022;
 
/**
 * 大文字小文字を区別しない！
 */
 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
 
 public class Main {
 
 	ArrayList<String> list,words;
 	BufferedReader br;
 	int count = 0;
 
 	public Main(){
 		list = new ArrayList<String>();
 		words = new ArrayList<String>();
 	}
 
 	public void start(){
 		String str;
 		String[] w;
 		try {
 			br = new BufferedReader(new FileReader("sampleData/id10022D.txt"));
 			while(!(str=br.readLine()).equals("END_OF_TEXT")){
				list.add(str);		//1行ごとにlistに追加
 			}
 		}catch(IOException e){
 			e.printStackTrace();
 		}
 
		for(int i=0; i<list.size(); i++){	//すべての単語をwordsに追加
 			w = list.get(i).split(" ");
 			for(String s : w){
				words.add(s);
 			}
 		}
 		//判別
		String sample = words.get(0);	//wordsの1番目(=W)をsampleに代入
 		for(String s : words){
			if(sample.equals(s)){		//sampleと一致した回数をカウント
 				count++;
 			}
 		}
		System.out.println(count-1);	//1番目(=W)も一致してしまうので1引く
 	}
 
 	public static void main(String[] args){
 		new Main().start();
 	}
 }
