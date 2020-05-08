 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import javax.swing.JFrame;
 import java.awt.Canvas;
 import java.util.Random;
 
 
 public class AutomataGui extends Canvas{
 
 	private Dimension dimension;
     private int largo,ancho;
     private Automata automata;
     private JFrame frame;
     private int[] generacionAnterior;
     private int[] generacionNueva;
     private int[] coeficientes = new int[4];
     private int rand,contador = 0;
 
     public AutomataGui(int regla,int rand){
         automata = new Automata(regla);
         this.rand = rand;
     }
 
     @Override
     public void paint(Graphics g){
         super.paint(g);
         int i,j;
         dimension = getSize();
         largo = dimension.width;
         ancho = dimension.height;
         generacionAnterior = new int[largo];
         generacionNueva = new int[largo];
         contador++;
         if(rand == 1)
             primerGeneracionGeneral();
         else
             primerGeneracionRandom();
         for(i = 0;i < ancho;i++){
             for(j = 0;j < largo;j++){
                 //Si en la generacion hay 1,pintar
                 if(generacionAnterior[j] == 1){
                    g.setColor(Color.green);
                     g.drawLine(j,i,j,i);
                 }
                 else{
                    g.setColor(Color.black);
                     g.drawLine(j,i,j,i);
                 }
             }
             nuevaGeneracion();
         }
     }
 
     private void primerGeneracionGeneral(){
         int i;
         for(i = 0;i < largo;i++){
             //Si es la celda central,pon 1[celda encendida]...modificarlo para que encuentra le celda media
            // System.out.println("["+i+"]");
             if(i == (largo /2)){
                 //System.out.println("["+i+"]");
                 generacionAnterior[i] = 1;
                 //System.out.println("Valor "+generacionAnterior[i]);
             }
             //Si es cualquier otra,apagala
             else{
                 generacionAnterior[i] = 0;
             }
         }
     }
 
     private void primerGeneracionRandom(){
         int i,decision;
         Random randoms = new Random();
         for(i = 0;i < largo;i++){
             //Si es la celda central,pon 1[celda encendida]...modificarlo para que encuentra le celda media
            // System.out.println("["+i+"]");
             decision = randoms.nextInt(1000) % 2;
             if(decision == 1){
                 generacionAnterior[i] = 1;
             }
             //Si es cualquier otra,apagala
             else{
                 generacionAnterior[i] = 0;
             }
         }
     }
 
     private void nuevaGeneracion(){
         setCoeficientes();
         int iteraciones = 0;
         int[] numeroBinario = new int[3];
         numeroBinario[0] = generacionAnterior[coeficientes[0]];
         numeroBinario[1] = generacionAnterior[coeficientes[1]];
         numeroBinario[2] = generacionAnterior[coeficientes[2]];
         do{
             generacionNueva[coeficientes[3]] = automata.busquedaEnHashMapRegla(numeroBinario);
             aumentarCoeficientes();
             avanzarCeldas(numeroBinario);
             iteraciones++;
         }while(iteraciones < largo);
         System.arraycopy(generacionNueva,0,generacionAnterior,0,generacionNueva.length);
     }
 
     public void avanzarCeldas(int[] avanzarNumeroBinario){
         avanzarNumeroBinario[0] = generacionAnterior[coeficientes[0]];
         avanzarNumeroBinario[1] = generacionAnterior[coeficientes[1]];
         avanzarNumeroBinario[2] = generacionAnterior[coeficientes[2]];
     }
 
     public void aumentarCoeficientes(){
         int i;
         for(i = 0;i < 4;i++){
             coeficientes[i]++;
             //Si se llego al limite de las celdas
             if(coeficientes[i] == largo)
             coeficientes[i] = 0;    
         }
     }
 
     public void setCoeficientes(){
         //Coeficientes de las celdas que se iran recorriendo
         coeficientes[0] = 0;
         coeficientes[1] = 1;
         coeficientes[2] = 2;
         //Coeficiente de la celda del resultado de la nueva generacion
         coeficientes[3] = 1;
     }
 
     public void mostrar(){
         frame = new JFrame("Automata Celular");
         dimension = super.getToolkit().getScreenSize(); 
         frame.setSize(dimension);
         frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         frame.setLocationRelativeTo(null);
         frame.add(this);
         frame.setVisible(true);
     }
 
 }
