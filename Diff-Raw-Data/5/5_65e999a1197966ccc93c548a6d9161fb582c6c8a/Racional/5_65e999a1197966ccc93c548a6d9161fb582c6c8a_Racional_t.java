 /*
  * Racional.java criado em 03/08/2011
  * 
  * Propriedade de Objectos Fábrica de Software LTDA.
  * Reprodução parcial ou total proibida.
  */
 package br.com.objectos.dojo.hescarate.tdd.ex1;
 
import com.google.common.base.Preconditions;

 /**
  * @author hellen.escarate@objectos.com.br (Hellen Escarate)
  */
 public class Racional {
 
   Integer numerador;
   Integer denominador;
 
   public Racional(int numerador, int denominador) {
    Preconditions.checkArgument(denominador != 0);

     int g = mdc(Math.abs(numerador), Math.abs(denominador));
 
     this.numerador = numerador / g;
     this.denominador = denominador / g;
   }
 
   // exercicio que retona o equivalente na forma reduzida >>
   private int mdc(int a, int b) {
 
     return b == 0 ? a : mdc(b, a % b);
 
   }
   // exercício que retorna o equivalente na forma reduzida <<
 
   public Racional(int i) {
 
     this(i, 1);
 
   }
 
   @Override
   public String toString() {
     return numerador.toString() + "/" + denominador.toString();
   }
 
   public Racional soma(Racional numero) {
 
     int numerador = numero.numerador;
     int denominador = numero.denominador;
 
     int somaNumerador = this.numerador * denominador + this.denominador * numerador;
     int somaDenominador = this.denominador * denominador;
 
     return new Racional(somaNumerador, somaDenominador);
 
   }
 
   public Racional multiplica(Racional outro) {
     return new Racional(numerador * outro.numerador, denominador * outro.denominador);
   }
 
 }
