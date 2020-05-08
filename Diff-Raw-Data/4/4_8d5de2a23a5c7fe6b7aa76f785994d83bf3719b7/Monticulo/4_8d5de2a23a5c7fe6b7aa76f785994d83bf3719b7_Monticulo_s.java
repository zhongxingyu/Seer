 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package interfazgraficamonticulos;
 
 /**
  *
  * @author chaqui
  */
 public class Monticulo {
     private Usuario[] usuario = new Usuario[31];
     private int size=0;
     public boolean b=true;
 
     public int getSize() {
         return size;
     }
     
     public int getClaveUsuario(int a)
     {
         return this.usuario[a].getClave();
     }
     public String getNombreUsuario(int a)
     {
         return this.usuario[a].getNombre();
     }
     public String getTelefonoUsuario(int a)
     {
         return this.usuario[a].getElefono();
     }
     public void ingresar(Usuario usuario)
     {
         System.out.println("Ingresar 1");
         System.out.println(String.valueOf(size));
         int cPadre;
         if (size<31) {
             for (int i = 0; i < size; i++) {
                 if (this.usuario[i].getClave()== usuario.getClave()) {
                 b =false;
             }
         }
         if (b) {
             System.out.println("Ingresar");
             this.usuario[size]=usuario;
             cPadre= Padre(size);
             if (cPadre>=0) {
                 if (this.usuario[cPadre].getClave()>this.usuario[size].getClave()) {
                     this.upHeap(cPadre, size);
                 }
             }
         }
         }
        size++;
         
         
     }
     public Usuario eliminar()
     {
         if (size>0) {
            size--;
         }
         Usuario us = usuario[0];
         System.out.println(us.getNombre());
         int p=0;
         usuario[p] = usuario[size];
         usuario[size]= null;
         int cIzq=hijoIzq(p);
         int cDer=hijoDer(p);
         if (cIzq<this.size || usuario[cIzq]!= null) {
             if (cDer<this.size || usuario[cDer]!= null) {
                 if (usuario[cIzq]!= null){
                     System.out.println(String.valueOf(usuario[p].getClave()));
                  if (usuario[p].getClave()>usuario[cIzq].getClave()) {
                     if (usuario[p].getClave()>usuario[cDer].getClave()) {
                         if (usuario[cIzq].getClave()>usuario[cDer].getClave()) {
                             this.downHeap(p, cIzq);
                         }
                         else
                         {
                              this.downHeap(p, cDer);
                         }
                 }
                     else
                     {
                          this.downHeap(p, cIzq);
                     }
                 }   
                 }
                 
             }
             else
             {
                 if (usuario[p].getClave()>usuario[cIzq].getClave()) {
                     this.downHeap(p, cIzq);
                 }
             }
         }
 
         System.out.println(String.valueOf(size));
         return us;
         
     }
     public int hijoIzq(int padre)
     {
         return padre*2+1;
     }
     public int hijoDer(int padre)
     {
         return padre*2+2;
     }
     public void downHeap(int padre, int hijo)
     {
     Usuario vpadre = this.usuario[padre];
     this.usuario[padre]= this.usuario[hijo];
     this.usuario[hijo]=vpadre;
     padre=hijo;
     int cIzq=hijoIzq(padre);
         int cDer=hijoDer(padre);
         if (cIzq<this.size || usuario[cIzq]!= null) {
             if (cDer<this.size || usuario[cDer]!= null) {
                 if (usuario[padre].getClave()>usuario[cIzq].getClave()) {
                     if (usuario[padre].getClave()>usuario[cDer].getClave()) {
                         if (usuario[cIzq].getClave()>usuario[cDer].getClave()) {
                             this.downHeap(padre, cIzq);
                         }
                         else
                         {
                              this.downHeap(padre, cDer);
                         }
                 }
                     else
                     {
                          this.downHeap(padre, cIzq);
                     }
                 }
             }
             else
             {
                 if (usuario[padre].getClave()>usuario[cIzq].getClave()) {
                     this.downHeap(padre, cIzq);
                 }
             }
         }
     }
     public void upHeap(int padre, int hijo)
     {
     Usuario vpadre = this.usuario[padre];
     this.usuario[padre]= this.usuario[hijo];
     this.usuario[hijo]=vpadre;
         int gpadre = this.Padre(padre);
         if (gpadre>=0) {
                 if (this.usuario[gpadre].getClave()>this.usuario[padre].getClave()) {
                     this.upHeap(gpadre, padre);
                 }
             }
     }
     public int Padre(int hijo)
     {
         if (hijo%2 == 0) {
             return (hijo-2)/2;
         }
         else
         {
             return (hijo-1)/2;
         }
     }
 }
