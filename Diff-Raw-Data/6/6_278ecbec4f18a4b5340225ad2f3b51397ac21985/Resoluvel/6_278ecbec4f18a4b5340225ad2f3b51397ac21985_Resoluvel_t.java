 package blink.cli;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 import java.util.Scanner;
 import java.util.Vector;
 
 
 public class Resoluvel {
 
 	/*
 	string code;
 	int numV, numE, resoluvelEmGX;
 	int repre[MAXN];
 	vector<pair<int, int> > arestas[4], E2, E3, EGX[3], AGPMDMX;
 	int adj[MAXN][4];*/
 	static final int MAXN = 500;
 	
 	String code;
 	int numV, numE, resoluvelEmGX;
 	int repre[] = new int[MAXN];
 	Vector<pair> E2, E3, AGPMDMX;
 	ArrayList<pair> arestas[] = new ArrayList[4];
 	ArrayList<pair> EGX[] = new ArrayList[3];
 	int adj[][] = new int[MAXN][4];
 	
 	int criarAresta(int cor, int pos){
 	    int atual = 1, prox;
 	    while( !(pos == code.length() || code.charAt(pos) == ',') ){
 	        prox = code.charAt(pos)-'a'+1;
 	        if(prox <= 0) prox = code.charAt(pos)-'A'+27;
 	        arestas[cor].add(new pair(atual,2*prox));
 	        pos++;
 	        atual+=2;
 	    }
 	    return pos;
 	}
 	
 	void criaGrafo(){
 		arestas[0] = new ArrayList<>();
 	    arestas[1] = new ArrayList<>();
 	    arestas[2] = new ArrayList<>();
 	    arestas[3] = new ArrayList<>();
 	    int pos = 0;
 	    for(int i = 1;i<numV;i+=2) arestas[0].add(new pair(i,i+1));
 	    pos = criarAresta(1, pos)+1;
 	    pos = criarAresta(2, pos)+1;
 	    criarAresta(3, pos);
 	}
 	
 	void criarGrid(){
 	    int u, v;
 	    for(int i = 0;i<4;i++){
 	        for(int j = 0;j<arestas[i].size();j++){
 	            u = arestas[i].get(j).first;
 	            v = arestas[i].get(j).second;
 	            adj[u][i] = v;
 	            adj[v][i] = u;
 	        }
 	    }
 	}
 	
 	boolean isAB(int u, int v, int c1, int c2){
 	    int val02[] = new int[2];
 	    val02[0] = c1; val02[1] = c2;
 	    int prev = 1, now = 0, atual = u;
 	    do{
 	        atual = adj[atual][val02[now]];
 	        now = prev;
 	        prev = 1-now;
 	        if(atual == v) return true;
 	    }while(atual != u);
 	    return false;
 	}
 	
 	void createDM2(int tipo){
 	    E2 = new Vector<>();
 	    for(int i = 1;i<=numV;i++){
 	        for(int j = 1;j<=numV;j++){
 	            if(i == j) continue;
 	            if(tipo == 0 && isAB(i,j,0,2) && isAB(i,j,1,3) && !isAB(i,j,2,3)){
 	                E2.add(new pair(i,j));
 	            }
 	            if(tipo == 1 && isAB(i,j,0,3) && isAB(i,j,2,1) && !isAB(i,j,3,1)){
 	                E2.add(new pair(i,j));
 	            }
 	            if(tipo == 2 && isAB(i,j,0,1) && isAB(i,j,3,2) && !isAB(i,j,1,2)){
 	                E2.add(new pair(i,j));
 	            }
 	        }
 	    }
 	}
 	
 	void createDM3(int tipo){
 	    E3 = new Vector<>();
 	    for(int i = 1;i<=numV;i++){
 	        for(int j = 1;j<=numV;j++){
 	            if(i == j) continue;
 	            if(tipo == 0 && isAB(i,j,0,3) && isAB(i,j,1,2) && !isAB(i,j,3,2)){
 	                E3.add(new pair(i,j));
 	            }
 	            if(tipo == 1 && isAB(i,j,0,1) && isAB(i,j,2,3) && !isAB(i,j,1,3)){
 	                E3.add(new pair(i,j));
 	            }
 	            if(tipo == 2 && isAB(i,j,0,2) && isAB(i,j,3,1) && !isAB(i,j,2,1)){
 	                E3.add(new pair(i,j));
 	            }
 	        }
 	    }
 	}
 	
 	void criaEGX(int numGrafo, int a1, int a2){
 	    EGX[numGrafo] = new ArrayList<>();
 	    for(int i=0;i<arestas[a1].size();i++){
 	        EGX[numGrafo].add(arestas[a1].get(i));
 	    }
 	    for(int i=0;i<arestas[a2].size();i++){
 	        EGX[numGrafo].add(arestas[a2].get(i));
 	    }
 	    createDM2(numGrafo);
 	    createDM3(numGrafo);
 	}
 	
 	int find_set(int x){
 	    if(x == repre[x]) return x;
 	    return repre[x] = find_set(repre[x]);
 	}
 	
 	boolean isconexo(int numGrafo){
 	    int p1, p2, count = 0;
 	    AGPMDMX = new Vector<>();
 	    for(int i = 1;i<=numV;i++)repre[i] = i;
 	    for(int i = 0;i<EGX[numGrafo].size();i++){
 	        p1 = find_set(EGX[numGrafo].get(i).first);
 	        p2 = find_set(EGX[numGrafo].get(i).second);
 	        if(p1 != p2){
 	            count++;
 	            repre[p1] = p2;
 	        }
 	    }
 	    for(int i=0;i<E2.size();i++){
 	        p1 = find_set(E2.get(i).first);
 	        p2 = find_set(E2.get(i).second);
 	        if(p1 != p2){
 	            count++;
 	            repre[p1] = p2;
 	            AGPMDMX.add(E2.get(i));
 	        }
 	    }
 	    for(int i=0;i<E3.size();i++){
 	        p1 = find_set(E3.get(i).first);
 	        p2 = find_set(E3.get(i).second);
 	        if(p1 != p2){
 	            count++;
 	            repre[p1] = p2;
 	            AGPMDMX.add(E3.get(i));
 	        }
 	    }
 	    resoluvelEmGX = numGrafo;
 
 	    return (count+1 == numV);
 	}
 	
 	boolean rodar(String nome){
 	    code = nome;
 	    numV = ((code.length()-2)/3)*2;
 	    numE = 4*numV;
 	    criaGrafo();
 	    criarGrid();
 
 	    criaEGX(0,2,3);
 	    criaEGX(1,3,1);
 	    criaEGX(2,1,2);
 
 	    return (isconexo(0) || isconexo(1) || isconexo(2) );
 	}
 	
 	String find_AGPMDMX(String nome){
 		String result; 
 	    if(rodar(nome)){
 	    	result = "Resoluvel em G"+resoluvelEmGX;
 	    	result = "Conjunto de arestas dm2 e dm3";
 	        for(int i =0;i<AGPMDMX.size();i++){
 	        	result = AGPMDMX.get(i).first+" "+AGPMDMX.get(i).second;
 	        }
 	    }else{
	    	result = nome+" Nao e resoluvel";
 	    }
 		return result;
 	}
 	
 	public static void main(String[] args) throws IOException {
 		Scanner in = new Scanner(System.in);
 		
 //		Scanner in = new Scanner(new File("a.txt"));
 //		PrintWriter out = new PrintWriter(new FileWriter("c.txt",true));
 //		
 		String s;
 		Resoluvel objeto = new Resoluvel();
 		s = in.next();
 		while(true){
 			objeto.find_AGPMDMX(s);
 			if(!in.hasNext()) break;
 			s = in.next();
 		}
 		
 	}
 
 }
 
 class pair{
 	
 	int first;
 	int second;
 	
 	pair(int a, int b){
 		this.first = a;
 		this.second = b;
 	}
}
