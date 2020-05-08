 package org.aoeu.jgames.breakout;
 
 /**
  * @author Luis Martín Canaval Sánchez
  */
 public class Breakout {
 
     // Definición de la barrita
     int barLeft;
     int barTop;
     int barWidth;
     int barHeight = 10;
 
     // definicion esfera
     int sphereLeft;
     int sphereTop;
     int sphereDiam = 30;
 
     // parametros de movimiento de esfera
     int deltaX;
     int deltaY;
 
     // datos tablero
     int width;
     int height;
 
     // estado de juego
     boolean gameOver;
 
     Breakout(int width, int height) {
         this.width = width;
         this.height = height;
         barWidth = width / 7;
         barTop = height - barHeight * 2;
         barLeft = barWidth * 3;
         sphereLeft = width / 2 - sphereDiam / 2;
         sphereTop = height - barHeight * 2 - sphereDiam * 2;
         deltaX = 0;
         deltaY = -5;
         gameOver = false;
     }
 
    void moverEsfera() {
         if (sphereTop + sphereDiam > barTop + barHeight) {
             gameOver = true;
         } else if (sphereTop + sphereDiam > barTop) {
             // verificar si esta sobre barrita
             int sphereMid = sphereLeft + sphereDiam / 2;
             if (sphereMid > barLeft && sphereMid < barLeft + barWidth) {
                 deltaY = -5;
                 if (sphereMid < barLeft + barWidth / 2) {
                     deltaX = -5;
                 } else if (sphereMid > barLeft + barWidth / 2) {
                     deltaX = 5;
                 } else {
                     deltaX = 0;
                 }
             } else {
                 gameOver = true;
             }
         }
         if (sphereLeft <= 1) {
             deltaX = 5;
         } else if (sphereLeft >= width - sphereDiam - 1) {
             deltaX = -5;
         }
         if (sphereTop <= 1) {
             deltaY = 5;
         }
         sphereLeft += deltaX;
         sphereTop += deltaY;
     }
 }
