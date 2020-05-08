 import java.util.Random;
 
 public class Algoritmos {
 	static final int numCols = 18; //final para valores não variáveis//
 	static final int numLinhas = 14;
 
 	//Só enxe de *//
 	public static char [][] preencheMatriz(){
 
 		char matriz[][] = new char [numLinhas][numCols];
 		int i,j;
 		for(i=0; i<numLinhas; i++){
 			for(j=0; j<numCols; j++){
 				matriz[i][j] = '*';
 			}
 		}
 		return matriz;
 	}
 
 	//--------------------------------------------------------------------------------------------
 	public static char[][] recebePalavras(){
 		int acertos = 1;
 		int erros = 1;
 
 		//1 = Horizontal //2 = Vertical : !!10 letras Max ou explode a matriz na vertical!!
 		char matriz[][] = Algoritmos.preencheMatriz(); //Recebo uma matriz cheia de *
 
 		char p1[] = {'L','u','x','e','m','b','u','r','g','o'};
 		char p2[] = {'P','a','q','u','i','s','t','a','o'};
 		char p3[] = {'I','t','a','l','i','a'};
 		char p4[] = {'B','r','a','s','i','l'};
 		char p5[] = {'H','o','l','a','n','d','a'};
 		char p6[] = {'J','a','p','a','o'};	
 
 		Algoritmos.inserePalavras(p1, matriz);
 		Algoritmos.inserePalavras(p2, matriz);
 		Algoritmos.inserePalavras(p3, matriz);
 		Algoritmos.inserePalavras(p4, matriz);
 		Algoritmos.inserePalavras(p5, matriz);
 		Algoritmos.inserePalavras(p6, matriz);
 
 		Algoritmos.mostraMatriz(matriz);
 
 		do{
 			System.out.println();
 			int orientacao = Console.readInt("A palavra está na: 1-Horizontal ou 2-Vertical ?");
 
 			System.out.println();
 			int linha = Console.readInt("Em qual linha está a palavra?");
 
 			System.out.println();
 			int coluna = Console.readInt("Em qual coluna está a palavra?");
 
 			System.out.println();
 			int comprimento = Console.readInt("Quantas letras tem a palavra?");
 
 			System.out.println();
 			if (Algoritmos.verificadorDeAcerto(orientacao, linha, coluna, comprimento, matriz)){
 				System.out.println("Acertou! " + acertos + " de 6");
 				System.out.println();
 				Algoritmos.mostraMatriz(matriz);
 				acertos++;
 				//jogadas++;
 			}else{
 				System.out.println("Errou! " + erros + " de 7");
 				erros++;
 				System.out.println();
 				Algoritmos.mostraMatriz(matriz);
 				//jogadas++;
 			}
 
 			if(acertos == 7){ //Começa em 1
 				System.out.println("!Luser, ganhou!");
 				break;
 			}
 
 			if(erros == 8){
				System.out.println("!Winner, perdeu!");
 				break;
 			}
 
 		}while(acertos != 7); //Só termina quando acertar as 6(começa em 1).
 		return matriz;
 	}
 
 	//--------------------------------------------------------------------------------------------
 	public static boolean verificadorDeAcerto(int orientacao, int linha, int coluna, int comprimento, char matriz[][]){
 
 		int i;
 		int cont = 0;
 
 		if(orientacao == Posicao.ORIENTACAO_HORIZONTAL){
 
 			for(i = coluna; i < numCols; i++) {
 				if (matriz[linha][i] == '*') {
 					break;
 				}
 				cont++;
 			}
 			System.out.println("Cont vale: " + cont);
 
 			if(cont == comprimento){	
 				for(i = coluna; i < coluna + cont; i++){
 					matriz[linha][i] = ' ';
 				}
 				Algoritmos.mostraMatriz(matriz);
 				return true;
 			}
 			return false;
 
 		}else{ //vertical
 			for (i = linha; i < numLinhas; i++){
 				if (matriz[i][coluna] == '*') {
 					break;
 				}
 				cont++;
 			}
 
 			if(cont == comprimento){
 				for(i = linha; i < linha + cont; i++){
 					matriz[i][coluna] = ' ';
 				}
 				Algoritmos.mostraMatriz(matriz);
 				return true;
 			}
 		}
 		System.out.println();
 		return false;
 	}
 
 	//--------------------------------------------------------------------------------------------	
 	public static char[] inversorDePalavras(char palavra[]){
 
 		int len = palavra.length;
 		char[] temporario = new char[len];  
 		char[] resultado = new char[len];
 		int i,j;
 
 		for (i = 0; i < len; i++) {  
 			temporario[i] = palavra[i];
 		}  
 
 		for (j = 0; j < len; j++) {  
 			resultado[j] = temporario[len - 1 - j];  
 		}  
 		return resultado;
 	}
 
 	//--------------------------------------------------------------------------------------------	
 	public static Posicao geradorDePosicaoValida(char[] palavra, char matriz[][]){
 
 		int k;
 		Posicao pos = new Posicao();
 		Random randomGenerator = new Random();
 
 		//String p = new String(palavra); //TESTE
 		//System.out.println("palavra = " + p); //TESTE Palavra sendo adicionada
 
 		boolean fazDeNovo;
 
 		do {
 			fazDeNovo = false;
 			pos.orientacao = Principal.orientacao();
 
 			if(pos.orientacao == Posicao.ORIENTACAO_HORIZONTAL){
 				pos.linha = randomGenerator.nextInt(numLinhas); //Alguma das linhas até numlinhas
 				pos.coluna = randomGenerator.nextInt(numCols + 1 - palavra.length); //Comprimento da linha+1 -tam da palavra
 
 				//System.out.println("h linha=" + pos.linha); //TESTE
 				//System.out.println("h coluna=" + pos.coluna); //TESTE
 				for (k = 0; k < palavra.length; k++){
 					if((matriz[pos.linha][pos.coluna + k]) != '*'){ 
 						fazDeNovo = true; //Se acima achar letra, fazDeNovo
 						break;
 					}
 				}
 
 			} else { //Vertical
 				pos.linha = randomGenerator.nextInt(numLinhas + 1 - palavra.length); //(linha +1)
 				pos.coluna = randomGenerator.nextInt(numCols); //Alguma coluna até numCols
 
 				//System.out.println("v linha=" + pos.linha); //TESTE
 				//System.out.println("v coluna=" + pos.coluna); //TESTE
 
 				for (k = 0; k < palavra.length; k++){
 					if((matriz[pos.linha + k][pos.coluna]) != '*'){
 						fazDeNovo = true;
 						break;
 					}
 				}
 			}
 
 			//Console.readLine(">>");
 		} while (fazDeNovo);
 
 		return pos; //retorna o objeto preenchido, vai ser pego por validPosition em inserePalavras//
 	}
 
 	//--------------------------------------------------------------------------------------------
 	public static void inserePalavras(char palavra[], char [][]matriz){
 
 		Posicao validPosition = geradorDePosicaoValida(palavra, matriz); //preenche valid position
 
 		int i = validPosition.linha; //validposition.linha vai receber a linha valida pro i
 		int j = validPosition.coluna; //validposition.linha vai receber a coluna valida pro j
 		int k;
 		
 		//1 = Horizontal 0 = Vertical
 		int orientacao =  validPosition.orientacao;
 
 		//Bloco de inversão//////
 		if(Principal.inverte() == 1){ //Inverte?
 			palavra = Algoritmos.inversorDePalavras(palavra);
 		}
 		///Fim do bloco de inversao///
 
 		if(orientacao == 1){
 			for(k = 0; k < palavra.length; k++){
 				matriz[i][j++] = palavra[k];
 			}
 		}else{
 			for(k = 0; k < palavra.length; k++){
 				matriz[i++][j] = palavra[k];
 			}
 		}
 	}
 
 	//--------------------------------------------------------------------------------------------
 	public static void mostraMatriz(char matriz[][]){
 		int i,j;
 		Random r = new Random();
 		char c; //Para organizar o print da matriz
 
 		System.out.print("     "); //Numera colunas 
 		
 		for(j = 0; j < numCols; j++){
 			System.out.printf("%2d ", j); //printf FTW :)
 		}
 		System.out.print("\n    +");
 		
 		//Separador dos numeros das colunas --
 		
 		for(j = 0; j < numCols; j++){ //Separador colunas
 			System.out.print("---"); 
 		}
 
 		System.out.println();
 		for(i = 0; i < numLinhas; i++){
 			System.out.printf("%3d |", i); //Numero da linhas com pipe(|)
 			for(j = 0; j < numCols; j++){
 				if (matriz[i][j] == '*'){
 					c = (char)(97 + r.nextInt(25));
 				}else{
 					c = matriz[i][j];
 				}
 				System.out.printf(" %c ", c); //Printa randomchar no lugar de *
 			}
 			System.out.println();
 		}
 	}
}
