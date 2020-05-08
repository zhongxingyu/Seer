 /**
  *   Copyright 2013 Nekorp
  *
  *Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License
  */
 
 package org.nekorp.workflow.desktop.modelo.alerta;
 
 import org.joda.time.DateTime;
 
 /**
  *
  */
 public class RangoAlertaVerificacion {
 
     private static final RangoAlertaVerificacion amarillo = new RangoAlertaVerificacion(new int[]{1,2}, "enero y febrero");
     private static final RangoAlertaVerificacion rosa = new RangoAlertaVerificacion(new int[]{2,3}, "febrero y marzo");
     private static final RangoAlertaVerificacion rojo = new RangoAlertaVerificacion(new int[]{3,4}, "marzo y abril");
     private static final RangoAlertaVerificacion verde = new RangoAlertaVerificacion(new int[]{4,5}, "abril y mayo");
     private static final RangoAlertaVerificacion azul = new RangoAlertaVerificacion(new int[]{5,6}, "mayo y junio");
     
     private int[] meses;
     private String nombrePerdiodo;
     private int diasAntesDelMes = 2;
     
     private RangoAlertaVerificacion(int[] meses, String nombre) {
         this.meses = meses;
         nombrePerdiodo = nombre;
     }
     
     public boolean dentroRango(DateTime dt) {
         boolean anioActual = cumpleLimiteIzquierdo(dt, 0) && cumpleLimiteDerecho(dt, 0);
         boolean proximoAnio = cumpleLimiteIzquierdo(dt, 1) && cumpleLimiteDerecho(dt, 1);
         return anioActual || proximoAnio;
     }
     
     private boolean cumpleLimiteIzquierdo(DateTime dt, int yearSwift) {
         DateTime fechaRango;
         for (int x: meses) {
             fechaRango = armaFechaRangoIzquierdo(x, yearSwift);
             if (dt.isAfter(fechaRango)) {
                 return true;
             }
         }
         return false;
     }
     
     private boolean cumpleLimiteDerecho(DateTime dt, int yearSwift) {
         DateTime fechaRango;
         for (int x: meses) {
             fechaRango = armaFechaRangoDerecho(x, yearSwift);
             if (dt.isBefore(fechaRango)) {
                 return true;
             }
         }
         return false;
     }
     
     private DateTime armaFechaRangoIzquierdo(int mes, int yearSwift) {
         DateTime r = new DateTime();
         r = new DateTime(r.getYear() + yearSwift, mes, r.dayOfMonth().getMinimumValue(),
             r.hourOfDay().getMinimumValue(), r.minuteOfHour().getMinimumValue(), 
             r.secondOfMinute().getMinimumValue(), r.millisOfSecond().getMinimumValue(),
             r.getZone());
         r = r.minusDays(diasAntesDelMes);
         return r;
     }
     
     private DateTime armaFechaRangoDerecho(int mes, int yearSwift) {
         DateTime r = new DateTime();
        r = new DateTime(r.getYear() + yearSwift, mes, r.dayOfMonth().getMinimumValue(),
            r.hourOfDay().getMinimumValue(), r.minuteOfHour().getMinimumValue(), 
            r.secondOfMinute().getMinimumValue(), r.millisOfSecond().getMinimumValue(),
            r.getZone());
        r = new DateTime(r.getYear(), mes, r.dayOfMonth().getMaximumValue(),
             r.hourOfDay().getMaximumValue(), r.minuteOfHour().getMaximumValue(), 
             r.secondOfMinute().getMaximumValue(), r.millisOfSecond().getMaximumValue(),
             r.getZone());
         return r;
     }
     
     public static RangoAlertaVerificacion getRango(int ultimoNumeroPlaca) {
         switch (ultimoNumeroPlaca) {
             case 1: return verde;
             case 2: return verde;
             case 3: return rojo;
             case 4: return rojo;
             case 5: return amarillo;
             case 6: return amarillo;
             case 7: return rosa;
             case 8: return rosa;
             case 9: return azul;
             case 0: return azul;
             default: return null;
         }
     }
 
     @Override
     public String toString() {
         return nombrePerdiodo;
     }
 }
