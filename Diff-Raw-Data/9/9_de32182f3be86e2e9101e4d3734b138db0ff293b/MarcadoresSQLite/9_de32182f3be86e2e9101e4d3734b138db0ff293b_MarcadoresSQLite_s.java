 package com.cuepoint.datos;
 
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

 import com.cuepoint.clases.Punto;
 
 public class MarcadoresSQLite
 {
 	public void nuevoMarcador(Context contexto, Punto punto, int idPlano)
 	{
 		//Abrimos la base de datos 'CuePoint'
 		ConexionSQLite pdb = new ConexionSQLite(contexto, "CuePoint", null, 1);
          
         // Insertar datos en la base de datos
         SQLiteDatabase db = pdb.getWritableDatabase();
         
         StringBuilder sb = new StringBuilder();
         sb.append("INSERT INTO Marcadores (x, y, idPlano) values (");
         sb.append(punto.getX() + ",");
         sb.append(punto.getY() + ",");
         sb.append(idPlano);
        sb.append(";");
         
         //Si hemos abierto correctamente la base de datos
         if(db != null)
         {
        	Log.d("Insert", sb.toString());
             db.execSQL(sb.toString());
             //Cerramos la base de datos
             db.close();
         }
 	}
 	
 	public Punto getMarcadorPorIdPlano(Context contexto, int idPlano)
 	{
 		Punto p = new Punto();
     	
     	//Abrimos la base de datos 'CuePoint'
 		ConexionSQLite pdb = new ConexionSQLite(contexto, "CuePoint", null, 1);
         SQLiteDatabase db = pdb.getReadableDatabase();
         
         //Leer datos de la base de datos
         Cursor c = db.rawQuery("SELECT x,y " +
         		"FROM Marcadores " +
         		"WHERE idPlano=" + idPlano + ";", null);
         
         //Nos aseguramos de que existe al menos un registro
         if (c.moveToFirst())
         {
         	p.setX(c.getFloat(0));
         	p.setY(c.getFloat(1));
        }
        db.close();
        return p;
 	}
 	
 	public void guardarMarcador(Context contexto, Punto punto, int idPlano)
 	{
 		//Abrimos la base de datos 'CuePoint'
 		ConexionSQLite pdb = new ConexionSQLite(contexto, "CuePoint", null, 1);
          
         // Insertar datos en la base de datos
         SQLiteDatabase db = pdb.getWritableDatabase();
         
         StringBuilder sb = new StringBuilder();
         sb.append("UPDATE Marcadores set x = ");
         sb.append(punto.getX() + ",");
         sb.append("y = ");
         sb.append(punto.getY());
         sb.append(" WHERE idPlano = ");
         sb.append(idPlano);
         sb.append(";");
         
         //Si hemos abierto correctamente la base de datos
         if(db != null)
         {
        	Log.d("UPDATE", sb.toString());
             db.execSQL(sb.toString());
             //Cerramos la base de datos
             db.close();
         }
 	}
 }
