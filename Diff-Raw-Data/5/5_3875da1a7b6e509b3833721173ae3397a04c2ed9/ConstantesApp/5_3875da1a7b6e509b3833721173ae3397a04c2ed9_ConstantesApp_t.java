 package ve.com.fsjv.devsicodetv.utilitarios.otros;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 
 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 /**
  *
  * @author franklin
  */
 public class ConstantesApp {
     public static final String ACRONIMO_MODULO_FICHA_DETENIDO = "DET";
     public static final String ACRONIMO_MODULO_SALIDA = "SAL";
     public static final String ACRONIMO_MODULO_TRASLADO = "TRA";
     public static final String ACRONIMO_MODULO_REGISTRAR_CAUSA = "CAU";
     public static final String ACRONIMO_MODULO_NIVEL_CALABOZO = "CAL";
     public static final String ACRONIMO_MODULO_CELDA = "CEL";
     public static final String ACRONIMO_MODULO_TIPO_SALIDA = "TSA";
     public static final String ACRONIMO_MODULO_SUB_TIPO_SALIDA = "SSA";
     public static final String ACRONIMO_MODULO_TIPO_TRASLADO = "TTR";
     public static final String ACRONIMO_MODULO_SUB_TIPO_TRASLADO = "STR";
     public static final String ACRONIMO_MODULO_ASIGNACION_CELDA = "ACC";
     public static final String ACRONIMO_MODULO_ORGANISMO = "ORG";
     public static final String ACRONIMO_MODULO_EVIDENCIAS = "EVI";
     
     public static final String APLICACION = "Sicodet v2.0";
     
     public static final String MODULO_FICHA_DETENIDO = "Ficha de Detenido";
     public static final String MODULO_SALIDA = "Salidas";
     public static final String MODULO_TRASLADOS = "Traslados";
     public static final String MODULO_REGISTRAR_CAUSA = "Causas de Detencion";
     public static final String MODULO_NIVEL_CALABOZO = "Calabozos";
     public static final String MODULO_CELDA = "Celdas";
     public static final String MODULO_TIPO_SALIDA = "Tipo de Salidas";
     public static final String MODULO_SUB_TIPO_SALIDA = "Sub Tipo de Salida";
     public static final String MODULO_TIPO_TRASLADO = "Tipo de Traslados";
     public static final String MODULO_SUB_TIPO_TRASLADOS = "Sub Tipo de Traslados";
     public static final String MODULO_ASIGNACION_CELDA = "Asignacion/Cambio de Celda";
     public static final String MODULO_ORGANISMO = "Organismos";
     public static final String MODULO_EVIDENCIAS = "Evidencias";
     public static final String MODULO_HISTORIALES_RELACIONADOS = "Historiales Relacionados";
     public static final String MODULO_ULTIMOS_AGREGADOS = "Ultimos Registros Agregados";
     public static final String MODULO_BUSQUEDAS_DETENIDOS = "Busqueda de Detenido";
     
     public static final boolean NO_EDITABLE = false;
     public static final boolean EDITABLE = true;
     public static final String EN_BLANCO = "";
     public static final String INICIALIZAR_STRING = null;
     
     public static final String FORMATO_FECHA = "dd/MM/yyyy";
     public static final String FORMATO_FECHA_COMPLETA = "dd/MM/yyyy HH:mm:ss";
     public static final String FORMATO_HORA = "HH:mm:ss a";
     
     public static final int MINLENGTH = 1;
     public static final int MAXLENGTH_TITULO = 100;
     public static final int MAXLENGTH_CEDULA = 10;
     public static final int TIPO_VALIDACION_VACIO = 1;
     public static final int TIPO_VALIDACION_MAXLENGTH_CEDULA = 2;
     public static final int TIPO_VALIDACION_MAXLENGTH_TITULO = 3;
     public static final int TIPO_VALIDACION_FECHA = 4;
     public static final int TIPO_VALIDACION_FECHA_HORA = 5;
     public static final int TIPO_VALIDACION_HORA = 6;
     public static final int TIPO_VALIDACION_TELEFONO = 7;
     public static final int TIPO_VALIDACION_DECIMAL = 8;
     
     public static final int BACKGROUND_ERROR_R = 255;
     public static final int BACKGROUND_ERROR_G = 213;
     public static final int BACKGROUND_ERROR_B = 213;
     
     public static final int REQUIRED_R = 215;
     public static final int REQUIRED_G = 238;
     public static final int REQUIRED_B = 244;
     
     public static final int BORDER_COLOR_R = 180;
     public static final int BORDER_COLOR_G = 180;
     public static final int BORDER_COLOR_B = 180;
     
     public static final int BANDERA_TRUE = 1;
     public static final int BANDERA_FALSE = 0;
     
     public static final int READONLY_DEFAULT = 1;
     public static final int READONLY_DATA = 2;
     
     public static final int SIN_TITULO = 0;
     public static final int TITULO_SIMPLE = 1;
     public static final int TITULO_DOBLE = 2;
     public static final int TITULO_COMPLETO = 3;
     
     public static final int CAMPO_REQUERIDO = 1;
     public static final int CAMPO_NORMAL = 0;
     
     public static final String VALIDACION_EXITOSA = "No se consiguio ningun tipo de error";
     public static final String VALIDACION_ERROR = "Errores por validacion encontrados:";
     public static final String TITULO_VALIDACION = "Validacion Terminada";
     
     public static final String CONCATENADOR = "---";
     
     public static final int ANIO_INICIAL = 1940;
     
    public static final String ESTATUS_LIBRE = "LIBRE";
    public static final String ESTATUS_SIN_ASIGNAR = "SIN ASIGNAR";
    
    
    
     public static final String MENSAJE_CAMPO_VACIO = "Codigo de Error 001: Campo vacio, componente referenciado: ";
     public static final String MENSAJE_CAMPO_EXCEDE_MAXLENGTH_TITULO = "Codigo de Error 002: El length del campo excede la cantidad de " + MAXLENGTH_TITULO + " Caracteres, componente referenciado: ";
     public static final String MENSAJE_CAMPO_EXCEDE_MAXLENGTH_CEDULA = "Codigo de Error 003: El length del campo excede la cantidad de " + MAXLENGTH_CEDULA + " Caracteres, componente referenciado: ";
     public static final String MENSAJE_CAMPO_MENOR_MINLENGTH = "Codigo de Error 004: El length del campo es menor de " + MINLENGTH + " Caracteres, componente referenciado: ";
     public static final String MENSAJE_CAMPO_FECHA_INVALIDO = "Codigo de Error 005: Formato Fecha Simple Invalido, componente referenciado: ";
     public static final String MENSAJE_CAMPO_FECHA_COMPLETA_INVALIDO = "Codigo de Error 006: Formato de Fecha larga invalido, componente referenciado: ";
     public static final String MENSAJE_CAMPO_HORA_INVALIDO = "Codigo de Error 007: Formato de Hora invalido, componente referenciado: ";
     public static final String MENSAJE_COMPONENTE_NULO = "Codigo de Error 008: Componente nulo.";
     public static final String MENSAJE_CHECKBOX_NO_SELECCIONADO = "Codigo de Error 009: Item No Seleccionado, componente referenciado: ";
     public static final String MENSAJE_PASSWORD_DIFERENTES = "Codigo de Error 010: Contraseñas no coinciden.";
     public static final String MENSAJE_CLASE_PROCESOS_NULA = "Codigo de Error 011: La clase procesos no se instancio o esta nula";
     public static final String MENSAJE_ACRONIMO_NULO = "Codigo de Error 012: El parametro acronimo esta pasando nulo o vacio.";
     public static final String MENSAJE_CAMPO_TELEFONO_ERRONEO = "Codigo de Error 013: El campo telefono esta incompleto, componente referenciado: ";
     public static final String MENSAJE_CAMPO_DECIMAL_ERRONEO = "Codigo de Error 014: El campo decimal esta mal escrito o incompleto, componente referenciado: ";
     
 }
