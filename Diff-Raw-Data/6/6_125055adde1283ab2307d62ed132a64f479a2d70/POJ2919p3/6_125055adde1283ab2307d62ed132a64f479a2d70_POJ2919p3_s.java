 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Scanner;
 
 /**
  * POJ2919 Traveling Queen Problem
  *
  * @author (TAT)chaN
  * @since 2014.6.30
  * @date 2014.8.18 (Accepted: 76)
  *
  * yTvz
 * ܂C`FXC[WD`FX̔ՖʂƃR}͓̈D
 * ł́C1̃NC[(Q)Ֆʂړ邱ƂlD
  * NC[ՖʂɔzuĂ邻ꂼ̃iCg(N, 2`14)K₷KvC
  * KƂ́CiCg̗אڃ}X({IɎ8}X)̉ꂩɃNC[Œ1x~܂邱ƂwD
  * NC[ՖʂɔzuĂSẴiCgK₵̂CrVbv(B)K₷oHlD
  * ́ČoHɂāCNC[̈ړ񐔂ŏC
  * ܂̒ŌoH(R}̈ړ)ő(At@xbg̐擪)Ȃ̂߂D
  * CoHȂꍇ́uimpossiblevƏo͂D
  *
  * y@z
  * DT(BFS)DNC[(X^[gn_)1ł}X􂢏oĂƓɁC
  * 􂢏oꂽ}XɂāCiCgK₷邱ƂɂȂ𔻒肷D
  * }X̍W(64}X)Ƃǂ̃NC[K₵(őł2^14p^[)ԂƂāCێD
  * Tɂ邱ƂŁCŏɌoH𓚂Ƃ邱ƂłD
  * Ȃ̂ŁC1ňړł}Xɂă\[gĂD
  * }Xɖ߂Ăꍇ́CԐC[v̂ŁCȍԂ疳D
  * (ł̏ԂC}XłCiCg̖K󋵂قȂ΁CԂ͕ʂ)
  * NG@ƂāuIv@vCuċA(cf. \[X p2)([DT DFS)vɂg񂾁D
  * ɁCċA̐[DTł́CŒZXebv킩ȂԂŁC
  * oH̎ӎȂƂȂ̂ŁCSTKvƂȂC}ĂsԂǂtȂD
  *
  * y|Cgz
  * o͂̓ViIƂɋsށD
  * }XɖK₵ہC̃iCgK₷邱ƂɂȂ\lKvD
  * ܂C̃}XɃNC[鎞_ŁCiCgK₷邱Ƃ΁CKς݂ɂĂKvD
  *
  * yz
  * POJTime Limit5bɐݒ肳Ă邪CN=14̃f[^11.3bœĂC
  * TLEƂȂĂDŏIIɂ0.3b܂ōAcceptꂽD
  * 1. }X1łǂ̃}Xɍs邩́CǂȏԂłӂɒ܂D
  * @}X64Ȃ߁CxvZۑCvZKvɂȂ΁Cp邱ƂōD
  * @(moveFasterMap)
  * 2. ǂ̃}XɃNC[΁Cǂ̃iCgK₷邱ƂɂȂ邩COɌvZD
  * @(neighboorKnightSerial)
  * 3. oȏԃ`FbN̂߁C^UzpӁDԂ8*8*(2^14)=100xȂ̂
  * @񎟌z[x*8+y][1<<14]Ƃ΁CO(1)ŊǗ\
  * @(publishedSuper)
  * 4. iCgK˂𖈉iCgK󋵔z(acKnight)𒲂ׂĂ͎Ԃ̂ŁC
  * @goBishopŃiCgK␔ǗCiCgogoBishopr邱ƂŁCƂ̓rVbv
  * 5. L3ɂāCO(1)Ŏ擾ł悤CiCgK󋵔z̃p^[ޔԍɓ̂
  * @ێĂƂ悢(visitKnightNumber) p^[2^14=2サȂD
  *
  * eXg POJ2919test.txt, POJ2919ans.txt QƁD
  *
  */
 public class POJ2919 {
 
     /**
      * neighboorKnightSerial̂߂̃NX
      * KłiCg̊ǗԍێD
      */
     class Nei{
 	ArrayList<Integer> knightNo;
 
 	public Nei() {
 	    this.knightNo = new ArrayList<Integer>();
 	}
 
 	/**
 	 * KłiCgǉ
 	 * @param n ǉiCg̊Ǘԍ
 	 */
 	public void add(int n){
 	    this.knightNo.add(n);
 	}
     }
 
     /**
      * W܂Ԃ̃NX
      */
     class Point implements Comparable<Point>{
 	int id;	         	        // ԂID
 	int x, y;
 	int parent;	        	// ̏Ԃ̐e(ID)
 	boolean[] acKnight;		// iCg̖K󋵔z
 	int goBishop;			// iCg̖K␔
 	int visitKnightNumber;		// iCg̖K󋵃p^[ԍ
 
 	/**
 	 * W̐RXgN^
 	 * @param _x
 	 * @param _y
 	 */
 	Point(int _x, int _y){
 	    this.x = _x;
 	    this.y = _y;
 	}
 
 	/**
 	 * WƏԂ̐RXgN^
 	 * @param _x
 	 * @param _y
 	 * @param _parent
 	 * @param _acKnight
 	 * @param _goBishop
 	 * @param _visitKnightNumber
 	 */
 	Point(int _x, int _y, int _parent, boolean[] _acKnight, int _goBishop, int _visitKnightNumber){
 	    this.id = -100;	// KIDUȂ
 	    this.x = _x;
 	    this.y = _y;
 	    this.parent = _parent;
 	    this.goBishop = _goBishop;
 	    this.visitKnightNumber = _visitKnightNumber;
 	    this.acKnight = new boolean[knightNum];
 	    if(_acKnight!=null){
 		for(int i=0;i<knightNum;i++){
 		    this.acKnight[i] = _acKnight[i];
 		}
 	    }
 	}
 
 	/**
 	 * w肵iCgKɂ(KɂĂĂ)
 	 * ƓɁCiCg̖K␔ƃiCg̖K󋵃p^[XVD
 	 * @param checkKnight K₷iCg̊Ǘԍ
 	 */
 	public void checkKnight(int checkKnight){
 	    if(!this.acKnight[checkKnight]){
 		this.acKnight[checkKnight] = true;
 		this.goBishop++;
 		this.visitKnightNumber += Math.pow(2, checkKnight);
 	    }
 	}
 
 	/**
 	 * VK̏ԂƂĐKID𔭍sD
 	 * ܂CoH̋tł悤Ԃ̕ۑCԂoƂĒǉD
 	 */
 	public void doPublish(){
 	    this.id = createID++;
 	    published.put(this.id, this);
 	    publishedSuper[this.x*MAT+this.y][this.visitKnightNumber] = true;
 	}
 
 	//	public String toString() {
 	//	    String[] c = {"a","b","c","d","e","f","g","h"};
 	//	    return "("+c[y]+(8-x)+")";
 	//	}
 
 	//	public String accessKnightString(){
 	//		StringBuilder sb = new StringBuilder();
 	//		for(int i=0;i<knightNum;i++){
 	//			if(this.acKnight[i])
 	//				sb.append("o");
 	//			else
 	//				sb.append("x");
 	//		}
 	//		return sb.toString();
 	//	}
 
 	/**
 	 * compareToD}X̎\[gp
 	 */
 	@Override
 	    public int compareTo(Point o) {
 	    return (((8-this.x)+this.y*8)-((8-o.x)+o.y*8));
 	}
 
 	/**
 	 * }X̖̂𕶎Ŏ擾
 	 * @return }X̖̕
 	 */
 	public String getStr() {
 	    String[] c = {"a","b","c","d","e","f","g","h"};
 	    return c[this.y]+(8-this.x);
 	}
     }
 
     static int knightNum;	        		// iCg̐
     static final int MAT = 8;
     static int[][] field;				// Ֆʂ̏
     static HashMap<Integer, Nei> neighboorKnightSerial;	// e}XɂẴiCgKXg
     static List<Point> neighboorBishop;                 // rVbv̗אڃ}XXg
     static Point start;					// NC[(X^[gn_)
     static final int PAWN = -40;
     static final int QUEEN = -10;
     static final int KNIGHT = -20;
     static final int BISHOP = -30;
     static int createID;				// Ԃ̃VAID
     static Map<Integer, Point> published;	        // oHtp̏ԕۑ
     static boolean[][] publishedSuper;			// oԔp
     static HashMap<Integer, List<Point>> moveFasterMap;	// 1ړ\}X̕ۑp
 
     public static void main(String[] args)  {
 	// System.setOut(new PrintStream(new File("koike.txt")));
 	new POJ2919().run();
     }
 
     /**
      * ͂ƎsĂяo̐e\bh
      */
     private void run() {
 	Scanner stdIn = new Scanner(System.in);
 	int d = Integer.valueOf(stdIn.nextLine());
 	for(int z=0;z<d;z++){
 	    if(z!=0) stdIn.nextLine();
 	    field = new int[MAT][MAT];
 	    knightNum = 0;
 	    for(int i=0;i<MAT;i++){
 		char[] line = stdIn.nextLine().toCharArray();
 		for(int j=0;j<MAT;j++){
 		    if(line[j]=='Q'){
 			field[i][j] = QUEEN;
 			start = new Point(i, j);
 		    }
 		    else if(line[j]=='P') field[i][j] = PAWN;
 		    else if(line[j]=='N'){
 			knightNum++;
 			field[i][j] = KNIGHT;
 		    }
 		    else if(line[j]=='B') field[i][j] = BISHOP;
 		}
 	    }
 	    start = new Point(start.x, start.y, -1, null, 0, 0);
 	    standby();
 	    System.out.println("Scenario #"+(z+1)+":");
 	    // long start = System.currentTimeMillis();
 	    solve();
 	    // System.out.println((System.currentTimeMillis()-start)*1.0/1000+" sec");
 	    System.out.println();
 	}
     }
 
     /**
      * vZ
      */
     private void solve(){
 	// 
 	createID = 0;
 	published = new HashMap<Integer, Point>();
 	publishedSuper = new boolean[MAT*MAT][1<<14];
 	moveFasterMap =	new HashMap<Integer,List<Point>>();
 	for(int i=0;i<MAT*MAT;i++){
 	    publishedSuper[i] = new boolean[1<<14];
 	}
 	List<Point> nextList = new ArrayList<Point>();
 
 	// NC[Ԃł̃iCg(rVbv)KɊւ鏈
 	Nei nowAccessKnight2 = neighboorKnightSerial.get(start.x*MAT+start.y);
 	Point check2 = new Point(start.x, start.y, start.id,
 				 start.acKnight, start.goBishop, start.visitKnightNumber);
 	for(Integer nei: nowAccessKnight2.knightNo){
 	    check2.checkKnight(nei);
 	}
 	if(check2.goBishop==knightNum){
 	    for(int k=0;k<neighboorBishop.size();k++){
 		Point nei = neighboorBishop.get(k);
 		if(nei.x==check2.x&&nei.y==check2.y){
 		    check2.doPublish();
 		    route(check2.id);
 		    return;
 		}
 	    }
 	}
 	nextList.add(check2);
 	check2.doPublish();
 
 	// NC[(X^[g)n_珇ɏ
 	// int cnt = 0;
 	while(true){
 	    //	cnt++;
 	    //	System.out.println("===== "+cnt+" ("+nextList.size()+") =====");
 	    // ԂȂȂ΁CoHȂ̖ړIBs
 	    if(nextList.size()==0){
 		System.out.println("impossible");
 		return;
 	    }
 	    // Xg̏
 	    List<Point> list = null;
 	    // Xg𑖍XgփRs[
 	    list = new ArrayList<Point>(nextList);
 	    // Xg̏
 	    nextList = null;
 	    nextList = new ArrayList<Point>();
 	    // Xg珇Ƀ}XԂ擾
 	    for(Point nowPoint : list){
 		// }X̎擾
 		List<Point> possiblePoint = possibleMove(nowPoint);
 		// }Xɏ
 		for(Point nextPoint : possiblePoint){
 		    // }XŃiCgK
 		    Nei nowAccessKnight = neighboorKnightSerial.get(nextPoint.x*MAT+nextPoint.y);
 		    Point check = new Point(nextPoint.x, nextPoint.y, nowPoint.id,
 					    nowPoint.acKnight, nowPoint.goBishop, nowPoint.visitKnightNumber);
 		    for(Integer nei: nowAccessKnight.knightNo){
 			check.checkKnight(nei);
 		    }
 		    // SẴiCgK₪CrVbvB肪KvmF
 		    if(check.goBishop==knightNum){
 			for(int k=0;k<neighboorBishop.size();k++){
 			    Point nei = neighboorBishop.get(k);
 			    if(nei.x==check.x&&nei.y==check.y){
 				// ړIB̔
 				check.doPublish();
 				route(check.id);
 				return;
 			    }
 			}
 		    }
 		    // oԂmF
 		    if(!alreadyPublished(check)){
 			nextList.add(check);
 			check.doPublish();
 		    }
 		}
 	    }
 	}
     }
 
     /**
      * rVbv(S[}X)̗אڃ}X(ړIBŏI}X)X^[g(NC[)
      * kȂCړ}X(oH)̕񐶐o͂D
      * @param id rVbv̗אڃ}X
      */
     private static void route(int id) {
 	String s = "";
 	while(id!=0){
 	    s = published.get(id).getStr()+s;
 	    id = published.get(id).parent;
 	}
 	s = published.get(id).getStr()+s;
 	System.out.println(s);
     }
 
     /**
      * ̏ԂłɊoł邩ǂ肷
      * @param target 肷PointNX
      * @return oł
      */
     private static boolean alreadyPublished(Point target){
 	if(publishedSuper[target.x*MAT+target.y][target.visitKnightNumber]) return true;
 	return false;
     }
 
     /**
      * }X1ňړł}XXgŕԂ
      * @param locS ̃}X
      * @return ړł}X̃Xg
      */
     private List<Point> possibleMove(Point locS) {
 	// ɌvZmF
 	List<Point> temp = moveFasterMap.get(locS.x*MAT+locS.y);
 	if(temp!=null&&temp.size()!=0){
 	    // vZς݌ʂԂ
 	    return temp;
 	}
 	// VKvZD㉺EC΂߂̕Ōv8
 	List<Point> result = new ArrayList<Point>();
 	for(int i=locS.x-1;i>=0;i--)
 	    if(field[i][locS.y]>-15) result.add(new Point(i, locS.y));
 	    else break;
 	for(int i=locS.x+1;i<MAT;i++)
 	    if(field[i][locS.y]>-15) result.add(new Point(i, locS.y));
 	    else break;
 	for(int i=locS.y-1;i>=0;i--)
 	    if(field[locS.x][i]>-15) result.add(new Point(locS.x, i));
 	    else break;
 	for(int i=locS.y+1;i<MAT;i++)
 	    if(field[locS.x][i]>-15) result.add(new Point(locS.x, i));
 	    else break;
 	for(int i=1;i<MAT;i++)
 	    if(inMat(locS.x+i, locS.y+i)&&field[locS.x+i][locS.y+i]>-15) result.add(new Point(locS.x+i, locS.y+i));
 	    else break;
 	for(int i=1;i<MAT;i++)
 	    if(inMat(locS.x+i, locS.y-i)&&field[locS.x+i][locS.y-i]>-15) result.add(new Point(locS.x+i, locS.y-i));
 	    else break;
 	for(int i=1;i<MAT;i++)
 	    if(inMat(locS.x-i, locS.y+i)&&field[locS.x-i][locS.y+i]>-15) result.add(new Point(locS.x-i, locS.y+i));
 	    else break;
 	for(int i=1;i<MAT;i++)
 	    if(inMat(locS.x-i, locS.y-i)&&field[locS.x-i][locS.y-i]>-15) result.add(new Point(locS.x-i, locS.y-i));
 	    else break;
 
 	// ړ\}X̃\[g
 	Collections.sort(result);
 	// vZʂ̕ۑ
 	moveFasterMap.put(locS.x*MAT+locS.y, result);
 	return result;
     }
 
     /**
      * rVbv̗אڃ}XXg@Ɓ@
      * @e}XɂẴiCgKXg
      */
     private void standby() {
 	// 
 	neighboorBishop = new ArrayList<Point>();
 	neighboorKnightSerial = new HashMap<Integer, Nei>();
 	for(int i=0;i<MAT;i++){
 	    for(int j=0;j<MAT;j++){
 		Nei nei = new Nei();
 		neighboorKnightSerial.put(i*MAT+j, nei);
 	    }
 	}
 
 	int cnt = -1;
 	for(int i=0;i<MAT;i++){
 	    for(int j=0;j<MAT;j++){
 		if(field[i][j]==KNIGHT){
 		    cnt++;	// iCg̊Ǘԍ
 		    for(int x=-1;x<2;x++)
 			for(int y=-1;y<2;y++)
 			    if(inMat(i+x, j+y)&&!(x==0&&y==0)&&field[i+x][j+y]!=PAWN){
 				Nei nei = neighboorKnightSerial.get((i+x)*MAT+(j+y));
 				nei.add(cnt);
 				neighboorKnightSerial.put((i+x)*MAT+(j+y), nei);
 			    }
 		}
 		else if(field[i][j]==BISHOP){
 		    for(int x=-1;x<2;x++)
 			for(int y=-1;y<2;y++)
 			    if(inMat(i+x, j+y)&&!(x==0&&y==0))
 				neighboorBishop.add(new Point(i+x, j+y));
 		}
 	    }
 	}
     }
 
     /**
      * WՖʓł邩
      * @param i xW
      * @param j yW
      * @return Ֆʓł
      */
     private static boolean inMat(int i, int j) {
 	if(i>=0&&i<MAT&&j>=0&&j<MAT) return true;
 	return false;
     }
 }
