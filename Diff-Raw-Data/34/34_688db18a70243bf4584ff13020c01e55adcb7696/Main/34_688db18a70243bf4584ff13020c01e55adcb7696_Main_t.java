 package id10022;
 
 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
 
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
			//br = new BufferedReader(new InputStreamReader(System.in));
 			while(!(str=br.readLine()).equals("END_OF_TEXT")){
				list.add(str);				//1行ごとにlistに追加
 			}
 		}catch(IOException e){
 			e.printStackTrace();
 		}
 
		for(int i=0; i<list.size(); i++){	//すべての単語を小文字にしてwordsに追加
 			w = list.get(i).split(" ");
 			for(String s : w){
				words.add("<" + s.toLowerCase() + ">");
 			}
 		}
 		//判別
		String sample	= words.get(0);		//wordsの1番目(=W)をsampleに代入
		String reg		= "<" + sample + ">";		//正規表現
		Pattern p = Pattern.compile(reg);
		Matcher m;
 		for(String s : words){
			m = p.matcher(s);
			if(m.find()){					//sampleと一致した回数をカウント
 				count++;
 			}
 		}
		System.out.println(count-1);		//1番目(=W)も一致してしまうので1引く
		//test
		for(String s : words){
			System.out.println(s);
		}
 	}
 
 	public static void main(String[] args){
 		new Main().start();
 	}
 }
