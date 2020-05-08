 import java.io.*;
 import java.util.*;
 import java.lang.Float;
 
 class GetReg{
     public StringBuffer signInt_toStr_nlen(int num, int len){ /* 相対jumpの時に使う */
 	StringBuffer regStr = new StringBuffer();
 	int mask = 1 << (len-2);
 	if(num >= (1 << (len-1)) || num < -(1 << (len-1))){
 	    System.out.println("out of range, jump to too far address");
 	    System.exit(1);
 	}
 	if(num < 0) { regStr.append('1'); num+=(1 << (len-1)); }
 	else regStr.append('0');
 
 	while(mask!=0){
 	    if(num/mask!=0){
 		regStr.append('1');
 		num-=mask;
 	    }
 	    else regStr.append('0');
 	    mask=mask >> 1;
 	}
 	return regStr;
     }
 
 
     public StringBuffer toStr_nlen(int num, int len){ /* int を 長さlenの2進数表示のstrigbufferに変換 */
 	StringBuffer regStr = new StringBuffer();
 	int mask = 1 << (len-1);
 	if(num >= 1 << len){
 	    System.out.println("out of range, jump to too far address");
 	    System.exit(1);
 	}
 	for(int i=0;i<len;i++){
 	    if((mask&num)!=0)
 		regStr.append('1');
 	    else
 		regStr.append('0');
 	    mask = mask >>> 1;   // changed
 	}
 	return regStr;
     }
     public StringBuffer strToBstr(String s, int len){ /* string を 長さlenの2進数表示のstringbufferに変換 */
 	return toStr_nlen(Integer.parseInt(s),len);
     }
     public StringBuffer getFnum(String s){
 	if (s.charAt(0)!='%'){
 	    System.out.println("called getRegnum, but argument s="+s+" : not a register");
 	    System.exit(1);
 	}
 	int tmp = Integer.parseInt(s.substring(2));
 	return toStr_nlen(tmp,5);
     }
 
     public StringBuffer getRegnum(String s){ /* rX を X を長さ5の2進数表示のstringbufferに変換 */
 	if (s.charAt(0)!='%'){
 	    System.out.println("called getRegnum, but argument s="+s+" : not a register");
 	    System.exit(1);
 	}
 
 	if((s.substring(1,3)).equals("sp"))
 	    return toStr_nlen(30,5);
 	else if((s.substring(1,3)).equals("ra"))
 	    return toStr_nlen(31,5);
 	else if((s.substring(1,3)).equals("hp"))
 	    return toStr_nlen(29,5);
 	else {
 	    int tmp = Integer.parseInt(s.substring(2));
 	    return toStr_nlen(tmp,5);
 	}
     }
 
     public StringBuffer getRelate(String s){
 	//	System.out.println(s.substring(1,s.length()-1));
 
 	String[] str = (s.substring(1,s.length()-1)).split(" \\+ ");
 	//	System.out.println(s);
 	//	System.out.println(str[0]);
 	//	System.out.println(str[1]);
 	
 	return getRegnum(str[0]).append(strToBstr(str[1],16));
 	    //	int len = s.length();
 	    //	if(s[0]!='[' || s[len-1]!=']'){
 	    //	    System.out.prinln("called getRelate, but argument s="+s+ ": net a format [%rx + num]");
 	    //	    System.exit(1);
 	    //	}
 	    //	getRegnum
     }
 }
 
 public class Assembler {
 	private static ArrayList<String> instMem = new ArrayList<String>();
 	
 	public static void main(String[] args) {
 		HashMap<String, Integer> tagmap = new HashMap();
 		int count=0;
 		try{
 		BufferedReader br = new BufferedReader(new FileReader(args[0]));
 		String str = new String();
 
 		/* タグと行の対応関係をマップするとともに、命令をarrayにロード */
 		while((str=br.readLine()) != null){
 			if(str.charAt(str.length()-1)==':'){
 			    tagmap.put(str.substring(0,str.length()-1), count);
 			}
 			else{
 			    instMem.add(str.substring(1));
 			    count++;
 			}
 		}
 		}
 		catch(Exception e){
 			System.out.println("Error: " + e);
 			System.exit(1);
 		}
 	
 		GetReg gr = new GetReg();
 
 
 		try{
 		    BufferedWriter bw = new BufferedWriter( new FileWriter(new File(args[1])));
 
 		    for(int k=0;k<instMem.size();k++){
 			StringBuffer code = new StringBuffer(32);
 			String str= instMem.get(k);
 			String inst[]= str.split("\t|, ");
 			System.out.println(inst[0]);
 			if(inst[0].equals("add")){
 			    code.append("100001");
 			    code.append(gr.getRegnum(inst[2]));
 			    code.append(gr.getRegnum(inst[3]));
 			    code.append(gr.getRegnum(inst[1]));
 			    code.append("00000000000");
 			}
 			else if(inst[0].equals("addi")){
 			    code.append("101001");
 			    code.append(gr.getRegnum(inst[2]));
 			    code.append(gr.getRegnum(inst[1]));
 			    //			    System.out.println(inst[3]);
 			    if(inst[3].matches("[1-9][0-9]*"))
 				code.append(gr.signInt_toStr_nlen(Integer.parseInt(inst[3]),16));
 			    else
 				code.append(gr.signInt_toStr_nlen(tagmap.get(inst[3]), 16));
 			}
 			else if(inst[0].equals("sub")){
 			    code.append("100010");
 			    code.append(gr.getRegnum(inst[2]));
 			    code.append(gr.getRegnum(inst[3]));
 			    code.append(gr.getRegnum(inst[1]));
 			    code.append("00000000000");
 			}
 			else if(inst[0].equals("subi")){
 			    code.append("101010");
 			    code.append(gr.getRegnum(inst[2]));
 			    code.append(gr.getRegnum(inst[1]));
 			    code.append(gr.signInt_toStr_nlen(Integer.parseInt(inst[3]),16));
 			}
 			else if(inst[0].equals("mul")){
 			    code.append("100011");
 			    code.append(gr.getRegnum(inst[2]));
 			    code.append(gr.getRegnum(inst[3]));
 			    code.append(gr.getRegnum(inst[1]));
 			    code.append("00000000000");
 			}
 			else if(inst[0].equals("muli")){
 			    code.append("101011");
 			    code.append(gr.getRegnum(inst[2]));
 			    code.append(gr.getRegnum(inst[1]));
 			    code.append(gr.signInt_toStr_nlen(Integer.parseInt(inst[3]),16));
 			}
 			else if(inst[0].equals("xor")){
 			    code.append("XXXXOR");
 			    code.append(gr.getRegnum(inst[2]));
 			    code.append(gr.getRegnum(inst[3]));
 			    code.append(gr.getRegnum(inst[1]));
 			    code.append("00000000000");
 			}
 			else if(inst[0].equals("xori")){
 			    code.append("XXXORI");
 			    code.append(gr.getRegnum(inst[2]));
 			    code.append(gr.getRegnum(inst[1]));
 			    code.append(gr.signInt_toStr_nlen(Integer.parseInt(inst[3]),16));
 			}
 			else if(inst[0].equals("sra")){
 			    code.append("XXXSRA");
 			    code.append(gr.getRegnum(inst[2]));
 			    code.append(gr.getRegnum(inst[1]));
 			    code.append(gr.signInt_toStr_nlen(Integer.parseInt(inst[3]),16));
 			}
 			else if(inst[0].equals("lli")){
 			    code.append("110010");
			    code.append(gr.getRegnum(inst[1]));
 			    code.append("00000");
 			    code.append(gr.toStr_nlen((Integer.parseInt(inst[2]) & 0x0000FFFF),16));
 				//			    code.append(gr.strToBstr(inst[2],16));
 			}
 			else if(inst[0].equals("lhi")){
 			    code.append("110011");
			    code.append(gr.getRegnum(inst[1]));
 			    code.append("00000");
 			    code.append(gr.toStr_nlen((Integer.parseInt(inst[2]) & 0xFFFF0000)>>>16,16));
 			}
 			else if(inst[0].equals("addf")){
 			    code.append("XXADDF");
 			    code.append(gr.getFnum(inst[2]));
 			    code.append(gr.getFnum(inst[3]));
 			    code.append(gr.getFnum(inst[1]));
 			    code.append("00000000000");
 			}
 			else if(inst[0].equals("mulf")){
 			    code.append("XXMULF");
 			    code.append(gr.getFnum(inst[2]));
 			    code.append(gr.getFnum(inst[3]));
 			    code.append(gr.getFnum(inst[1]));
 			    code.append("00000000000");
 			}
 			else if(inst[0].equals("subf")){
 			    code.append("XXSUBF");
 			    code.append(gr.getFnum(inst[2]));
 			    code.append(gr.getFnum(inst[3]));
 			    code.append(gr.getFnum(inst[1]));
 			    code.append("00000000000");
 			}
 			else if(inst[0].equals("divf")){
 			    code.append("XXDIVF");
 			    code.append(gr.getFnum(inst[2]));
 			    code.append(gr.getFnum(inst[3]));
 			    code.append(gr.getFnum(inst[1]));
 			    code.append("00000000000");
 			}
 			else if(inst[0].equals("movf")){
 			    code.append("XXMOVF");
 			    code.append(gr.getFnum(inst[2]));
 			    code.append(gr.getFnum(inst[1]));
 			    code.append("0000000000000000");
 			}
 			else if(inst[0].equals("lhif")){
 			    System.out.println("hoge");
 			    code.append("XXLHIF");
 			    code.append(gr.getFnum(inst[1]));
 			    code.append("00000");
 			    code.append(gr.toStr_nlen(Float.floatToIntBits(Float.parseFloat(inst[2])) >> 16, 16));
 			}
 			else if(inst[0].equals("llif")){
 			    System.out.println("fuga");
 			    code.append("XXLLIF");
 			    code.append(gr.getFnum(inst[1]));
 			    code.append("00000");
 			    code.append(gr.toStr_nlen(Float.floatToIntBits(Float.parseFloat(inst[2])) & 0x0000FFFF, 16));
 			}
 			else if(inst[0].equals("sw")){
 			    code.append("001111");
 			    code.append(gr.getRegnum(inst[1]));
 			    code.append(gr.getRelate(inst[2])); //will be changed
 			}
 			else if(inst[0].equals("sf")){
 			    code.append("XXXXSF");
 			    code.append(gr.getFnum(inst[1]));
 			    code.append(gr.getRelate(inst[2]));
 			}
 			else if(inst[0].equals("lw")){
 			    code.append("001110");
 			    ///			    code.append(gr.getRegnum(inst[1]));
 			    //			    code.append(gr.getRelate(inst[2]));
 			    StringBuffer tmpstr=gr.getRelate(inst[2]);
 			    code.append(tmpstr.substring(0,5));
 			    code.append(gr.getRegnum(inst[1]));
 			    code.append(tmpstr.substring(5));
 			}
 			else if(inst[0].equals("lf")){
 			    code.append("XXXXLF");
 			    StringBuffer tmpstr=gr.getRelate(inst[2]);
 			    code.append(tmpstr.substring(0,5));
 			    code.append(gr.getFnum(inst[1]));
 			    code.append(tmpstr.substring(5));
 			}
 			else if(inst[0].equals("jr")){
 			    code.append("010011");
 			    code.append(gr.getRegnum(inst[1]));
 			    code.append("000000000000000000000");
 			}
 			else if(inst[0].equals("jalr")){
 			    code.append("010100");
 			    code.append(gr.getRegnum(inst[1]));
 			    code.append("000000000000000000000");
 			}
 			else if(inst[0].equals("bgt")){
 			    code.append("001100");
 			    code.append(gr.getRegnum(inst[1]));
 			    code.append(gr.getRegnum(inst[2]));
 			    code.append(gr.signInt_toStr_nlen(tagmap.get(inst[3])-k, 16));
 			}
 			else if(inst[0].equals("beq")){
 			    code.append("001001");
 			    code.append(gr.getRegnum(inst[1]));
 			    code.append(gr.getRegnum(inst[2]));
 			    code.append(gr.signInt_toStr_nlen(tagmap.get(inst[3])-k, 16));
 			}
 			else if(inst[0].equals("bneq")){
 			    code.append("001010");
 			    code.append(gr.getRegnum(inst[1]));
 			    code.append(gr.getRegnum(inst[2]));
 			    code.append(gr.signInt_toStr_nlen(tagmap.get(inst[3])-k, 16));
 			}
 			else if(inst[0].equals("bgtf")){
 			    code.append("XXBGTF");
 			    code.append(gr.getFnum(inst[1]));
 			    code.append(gr.getFnum(inst[2]));
 			    code.append(gr.signInt_toStr_nlen(tagmap.get(inst[3])-k, 16));
 			}
 			else if(inst[0].equals("negf")){
 			    code.append("XXFNEG");
 			    code.append(gr.getFnum(inst[1]));
 			    code.append("000000000000000000000");
 			}
 			else if(inst[0].equals("absf")){
 			    code.append("XXFABS");
 			    code.append(gr.getFnum(inst[1]));
 			    code.append("000000000000000000000");
 			}
 			else if(inst[0].equals("jal")){
 			    code.append("010110");
 			    //			    System.out.println(inst[1]); // print tag
 			    code.append(gr.toStr_nlen(tagmap.get(inst[1]), 26));
 			}
 			else if(inst[0].equals("j")){
 			    code.append("010101");
 			    code.append(gr.toStr_nlen(tagmap.get(inst[1]), 26));
 			}
 			else if(inst[0].equals("sendw")){
 			    code.append("XSENDW");
 			    code.append(gr.getRegnum(inst[1]));
 			    code.append("000000000000000000000");
 			}
 			else if(inst[0].equals("sendc")){
 			    code.append("XSENDC");
 			    code.append(gr.getRegnum(inst[1]));
 			    code.append("000000000000000000000");
 			}
 			else if(inst[0].equals("movf2i")){
 			    code.append("MOVF2I");
 			    code.append(gr.getRegnum(inst[2]));
 			    code.append(gr.getFnum(inst[1]));
 			    code.append("0000000000000000");
 			}
 			else if(inst[0].equals("nop")){
 			    code.append("00000000000000000000000000000000");
 			}
 			else if(inst[0].equals("break")){
 			    code.append("XBREAK");
 			    code.append("00000000000000000000000000");
 			}
 			else if(inst[0].equals("halt")){
 			    code.append("110000");
 			    code.append("00000000000000000000000000");
 			}
 			//
 			// 疑似命令
 			//
 			else if(inst[0].equals("sqrt")){
 			    code.append("XXSQRT");
 			    code.append("00000000000000000000000000");
 			}
 			else if(inst[0].equals("sin")){
 			    code.append("XXXSIN");
 			    code.append("00000000000000000000000000");
 			}
 			else if(inst[0].equals("cos")){
 			    code.append("XXXCOS");
 			    code.append("00000000000000000000000000");
 			}
 			else if(inst[0].equals("atan")){
 			    code.append("XXATAN");
 			    code.append("00000000000000000000000000");
 			}
 			else if(inst[0].equals("floor")){
 			    code.append("XFLOOR");
 			    code.append("00000000000000000000000000");
 			}
 			else if(inst[0].equals("itof")){
 			    code.append("XXITOF");
 			    code.append("00000000000000000000000000");
 			}
 			else if(inst[0].equals("ftoi")){
 			    code.append("XXFTOI");
 			    code.append("00000000000000000000000000");
 			}
 			else if(inst[0].equals("print_float")){
 			    code.append("XPRFLT");
 			    code.append("00000000000000000000000000");
 			}
 			else if(inst[0].equals("read_int")){
 			    code.append("XRDINT");
 			    code.append("00000000000000000000000000");
 			}
 			else if(inst[0].equals("read_float")){
 			    code.append("XRDFLT");
 			    code.append("00000000000000000000000000");
 			}
 
 			//
 			// 要修正！！！！！！！！！！！！！！！！！！！！
 			//
 			//			else if(inst[0].equals("div")){
 			//			    code.append("XXXSRA");
 			//			    code.append(gr.getRegnum(inst[1]));
 			//			    code.append(gr.getRegnum(inst[2]));
 			//			    code.append(gr.signInt_toStr_nlen(2, 16));
 			//			}
 
 			//
 			// 疑似命令
 			//
 			else{
 			    System.out.println(inst[0]+" is not defined");
 			    System.exit(1);
 			}
 			//			System.out.println(code.toString());
 			bw.write(code.toString());
 			bw.newLine();
 		    }
 		    bw.close();
 		}
 		catch(Exception e){
 		    System.out.println("Write Error : " + e);
 		    System.exit(1);
 		}
 	}
 }
