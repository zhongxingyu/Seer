 package ejemploSergio.ejemploSQLite;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.SQLException;
 import android.database.sqlite.SQLiteDatabase;
 
 // Clase intermediaria para acceder y manipular los datos de la base de datos.
 public class DbAdapter {
 	/** Propiedades a partir de aqu. */
 
 	// Para manipular el ayudante de conexin.
 	private SqLiteHelper dbHelper;
 	// Para manipular acciones sobre la base de datos.
 	private SQLiteDatabase db;
 
 	// Referencia a la actividad que usar esta clase.
 	private final Context contexto;
 
 	/** Constructores y mtodos heredados a partir de aqu. */
 
 	/**
 	 * Constructor de a clase DbAdapter.
 	 * 
 	 * @param contexto
 	 *            Referencia a la actividad que usar esta clase.
 	 */
 	public DbAdapter(Context contexto) {
 		this.contexto = contexto;
 	}
 
 	/** Mtodos adicionales a partir de aqu. */
 
 	/**
 	 * Para conectar y abrir la base de datos en modo escritura (lectura
 	 * permitida).
 	 * 
 	 * @return Devuelve un objeto de clase SQLiteDatabase como manipulador de la
 	 *         base de datos.
 	 * @throws SQLException
 	 */
 	public SQLiteDatabase open() throws SQLException {
 		// Crea un objeto asistente de base de datos de clase SqLiteHelper.
 		dbHelper = new SqLiteHelper(contexto);
 		// Abre la base de datos en modo escritura (lectura permitida).
 		db = dbHelper.getWritableDatabase();
 		// Devuelve el objeto de tipo SQLiteDatabase.
 		return db;
 	}
 
 	/**
 	 * Cierra la base de datos.
 	 */
 	public void close() {
 		dbHelper.close();
 	}
 
 	/**
 	 * Inserta un registro con los campos titulo y cuerpo en la base de datos.
 	 * 
 	 * @param titulo
 	 *            Campo ttulo de la nota.
 	 * @param cuerpo
 	 *            Campo cuerpo de la nota.
 	 * @return Devuelve el nmero de registro insertado o -1 en caso de error
 	 */
 	public long insertarNota(String titulo, String cuerpo) {
 		// Variable utilizada para enviar los datos al mtodo insert.
 		ContentValues registro = new ContentValues();
 
 		// Agrega los datos.
 		registro.put(SqLiteHelper.KEY_TITULO, titulo);
 		registro.put(SqLiteHelper.KEY_CUERPO, cuerpo);
 
 		// Inserta el registro y devuelve el resultado.
 		return db.insert(SqLiteHelper.DATABASE_TABLE, null, registro);
 	}
 
 	/**
 	 * Borra el registro que tiene el id especificado.
 	 * 
 	 * @param idRegistro
 	 *            Campo id del registro que se quiere borrar.
 	 * @return Devuelve cuntos registros han sido afectados.
 	 */
 	public int borrarNota(long idRegistro) {
 		return db.delete(SqLiteHelper.DATABASE_TABLE, SqLiteHelper.KEY_ID + "="
 				+ idRegistro, null);
 	}
 
 	/**
 	 * Obtiene todos los registros de la tabla notas.
 	 * 
 	 * @return Devuelve un cursor con los registros obtenidos.
 	 */
 	public Cursor obtenerNotas() {
 		return db.query(SqLiteHelper.DATABASE_TABLE, new String[] {
 				SqLiteHelper.KEY_ID, SqLiteHelper.KEY_TITULO,
 				SqLiteHelper.KEY_CUERPO }, null, null, null, null, null);
 	}
 
 	/**
 	 * Obtiene el registro que tiene el id especificado.
 	 * 
 	 * @param idRegistro
 	 *            Campo id del registro que se quiere obtener.
 	 * @return Devuelve un cursor con el registro obtenido.
 	 * @throws SQLException
 	 *             Lanza una excepcin SQLite en caso de error.
 	 */
 	public Cursor obtenerNota(long idRegistro) throws SQLException {
 		Cursor registro = db.query(true, SqLiteHelper.DATABASE_TABLE,
 				new String[] { SqLiteHelper.KEY_ID, SqLiteHelper.KEY_TITULO,
 						SqLiteHelper.KEY_CUERPO }, SqLiteHelper.KEY_ID + "="
 						+ idRegistro, null, null, null, null, null);
 
 		// Si lo ha encontrado, apunta al inicio del cursor.
 		if (registro != null) {
 			registro.moveToFirst();
 		}
 		return registro;
 	}
 
 	/**
 	 * Modifica los valores del registro que tiene el id especificado.
 	 * 
 	 * @param idRegistro
 	 *            Campo id del registro que se quiere modificar.
 	 * @param titulo
 	 *            Campo titulo del registro.
 	 * @param cuerpo
 	 *            Campo cuerpo del registro.
 	 * @return Devuelve cuntos registros han sido afectados.
 	 */
	public int actualizarNota(long idRegistro, String titulo, String cuerpo) {
 		// Variable utilizada para enviar los datos al mtodo update.
 		ContentValues registro = new ContentValues();
 
 		// Agrega los datos.
 		registro.put(SqLiteHelper.KEY_TITULO, titulo);
 		registro.put(SqLiteHelper.KEY_CUERPO, cuerpo);
 
 		// Inserta el registro y devuelve el resultado.
 		return db.update(SqLiteHelper.DATABASE_TABLE, registro,
				SqLiteHelper.KEY_ID + "=" + idRegistro, null);
 	}
 }
