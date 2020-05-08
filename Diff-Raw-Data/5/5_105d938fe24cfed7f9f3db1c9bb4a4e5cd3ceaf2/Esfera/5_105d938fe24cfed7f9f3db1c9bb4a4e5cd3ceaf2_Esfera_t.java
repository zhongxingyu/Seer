 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package guitarra;
 
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Rectangle;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javaPlay.GameEngine;
 import javaPlay.GameObject;
 import javaPlay.Keyboard;
 import javaPlay.Sprite;
 import javaPlayExtras.Imagem;
 import javaPlayExtras.Keys;
 import utilidades.Utilidades;
 
 /**
  *
  * @author fernando_mota
  * [[0,4],[1,2,3],[2,7]]
  */
 public abstract class Esfera extends GameObject {
 
     protected boolean especial;
     protected Sprite imagem;
     protected boolean pressionado;
     protected boolean bloqueado;
     protected Esfera anterior;
     protected Esfera proximo;
     protected float second;
     private int width = 40;
     private int margem = 15;
     private int height;
     private Rectangle rect;
     protected int tecla = 0;
     private Sprite explosao;
     Keyboard teclado = GameEngine.getInstance().getKeyboard();
     private int frame;
     private int framesElapsedInMiliseconds;
 
     public Esfera() {
         this.especial = Guitarra.getInstance().podeEspecial();
         this.second = Guitarra.getInstance().getPrecisionSecondsElapsed();
         try {
 
            this.explosao = new Sprite("img_cenario/explosaoo.png", 24, 60, 45);
 
         } catch (Exception ex) {
             Utilidades.alertar(ex.getMessage());
         }
     }
     public Esfera(float second) {
         this.especial = Guitarra.getInstance().podeEspecial();
         this.second = second;
         try {
            this.explosao = new Sprite("img_cenario/explosaoo.png", 24, 60, 48);
         } catch (Exception ex) {
             Utilidades.alertar(ex.getMessage());
         }
     }
     public abstract int getSerie();
 
     public boolean isEspecial() {
         return this.especial;
     }
 
     public int getPontos() {
         return 10 + (this.especial ? 5 : 0);
     }
 
     public abstract void setSerie(int serie);
 
     public void pressionar() {
         Guitarra.getInstance().adicionaPontos(this.getPontos());
         this.pressionado = true;
     }
     //public abstract void pressionar();
 
     public abstract void step(long timeElapsed);
     public void bloquearTecla(){
         this.bloqueado = true;
     }
     public boolean isBloqueado(){
         return this.bloqueado;
     }
     public boolean podePressionar() {
         return !this.bloqueado &&  this.getY() >= 390 && this.getY() <= 448;
     }
 
     public void preLocate(long timeElapsed) {
         this.y += 1;
         this.imagem.setCurrAnimFrame(this.getCurrentStep());
         //A linha abaixo captura o valor do atributo estatico 'serie' e 
         //multiplica pela largura de cada botao, somando com a margem
         //e posicionando a esfera corretamente
         this.x = 82 + this.getSerie() * this.width + this.margem;
         //Este metodo e sobreescrito e re-utilizado para permitir o alinhamento
         //Correto dos botoes
         Keyboard teclado = GameEngine.getInstance().getKeyboard();
         
         if (this.foiPressionado()) {
             this.framesElapsedInMiliseconds += 1;
             if (this.framesElapsedInMiliseconds > 2) {
                 ++this.frame;
                 this.explosao.setCurrAnimFrame(this.frame);
                 this.framesElapsedInMiliseconds = 0;
             }
         }
     }
 
     public int getCurrentStep() {
         return Math.round(this.y / 40)+1;
     }
 
     public void draw(Graphics g) {
         
         if (this.foiPressionado() && this.frame <= 24) {
             this.explosao.draw(g, this.x-10, this.y-28);
 
         }
         else if(!this.foiPressionado() && !this.isBloqueado()){
             this.imagem.draw(g, this.x, this.y);
         }
     }
 
     public Rectangle getRectangle() {
         if (this.rect == null) {
             this.rect = new Rectangle(this.width, this.height);
         }
         this.rect.setLocation(this.x, this.y);
         return this.rect;
     }
     public int getTecla(){
         return this.tecla;
     }
     public String getCor() {
         return this.getClass().getName();
     }
 
     public abstract Esfera getNewInstance(float second);
 
     boolean foiPressionado() {
 
         return this.pressionado;
     }
 
     public float getSecond() {
         return this.second;
     }
 }
