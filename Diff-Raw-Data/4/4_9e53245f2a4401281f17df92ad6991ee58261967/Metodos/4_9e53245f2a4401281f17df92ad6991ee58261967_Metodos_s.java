 package com.example.clasificados3.Controladores;
 
 import android.graphics.drawable.Drawable;
 import android.util.Log;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.CheckBox;
 import android.widget.ListAdapter;
 import android.widget.ListView;
 
 import com.example.clasificados3.Clases.Categoria;
 import com.example.clasificados3.Clases.Clasificado;
 import com.example.clasificados3.Clases.Imagen;
 import com.example.clasificados3.Clases.Usuario;
 import com.example.clasificados3.MainActivity;
 
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.ResponseHandler;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.BasicResponseHandler;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 
 /**
  * Created by martincho on 09/11/13.
  */
 public class Metodos
 {
     String ip;
 
     public Metodos(String ip)
     {
         this.ip = ip;
     }
 
     public String httpGetData(String mURL)
     {
         String response="";
         mURL=mURL.replace(" ", "%20");
 //        Log.i("LocAndroid Response HTTP Threas", "Ejecutando get 0: " + mURL);
         HttpClient httpclient = new DefaultHttpClient();
 
 //        Log.i("LocAndroid Response HTTP Thread","Ejecutando get 1");
         HttpGet httppost = new HttpGet(mURL);
         Log.i("LocAndroid Response HTTP Thread","Ejecutando get 2");
         try
         {
 
 //            Log.i("LocAndroid Response HTTP","Ejecutando get");
             // Execute HTTP Post Request
             ResponseHandler<String> responseHandler=new BasicResponseHandler();
             response = httpclient.execute(httppost,responseHandler);
 //            Log.i("LocAndroid Response HTTP",response);
         }
         catch (ClientProtocolException e)
         {
             Log.i("LocAndroid Response HTTP ERROR 1",e.getMessage());
             // TODO Auto-generated catch block
         }
         catch (IOException e) {
 
             Log.i("LocAndroid Response HTTP ERROR 2", e.getMessage());
             // TODO Auto-generated catch block
         }
         return response;
     }
 
 
     //--------------------- Metodos de Usuario
 
     //si devuelve 0 es porque el nombre de usuario no existe y vice
     public int validarNombreUsuario(String usuario)
     {
         int x = 0;
         //guarda la respuyesta de la consulta
         String jsonResult = httpGetData("http://" + ip + "/prueba/Clasificados_ValidarNombreUsuario.php?usuario=" + usuario);
 
         try
         {
             JSONObject jsonResponse = new JSONObject(jsonResult);
             JSONArray jsonMainNode = jsonResponse.optJSONArray("lista");
 
             x = jsonMainNode.getInt(0);
         }
         catch (JSONException e)
         {
             e.printStackTrace();
         }
         return x;
     }
 
     //si devuelve 0 es porque el usuario no existe y vice
     public int validarUsuario(Usuario usuario)
     {
         int x = 0;
         String jsonResult = httpGetData("http://" + ip + "/prueba/Clasificados_ValidarUsuario.php?usuario=" + usuario.getUsuario() + "&password=" + usuario.getPassword());
 
         try
         {
             JSONObject jsonResponse = new JSONObject(jsonResult);
             JSONArray jsonMainNode = jsonResponse.optJSONArray("lista");
 
             x = jsonMainNode.getInt(0);
         }
         catch (JSONException e)
         {
             e.printStackTrace();
         }
         return x;
     }
 
     public int insertarUsuario(Usuario x)
     {
         int id = 0;
 
         String jsonResult = httpGetData("http://" + ip + "/prueba/Clasificados_RegistrarUsuario.php?usuario=" + x.getUsuario() + "&password=" + x.getPassword() + "&correo=" + x.getCorreo());
 
         try
         {
             JSONObject jsonResponse = new JSONObject(jsonResult);
             JSONArray jsonMainNode = jsonResponse.optJSONArray("lista");
 
             id = jsonMainNode.getInt(0);
         }
         catch (JSONException e)
         {
             e.printStackTrace();
         }
         return id;
     }
 
     //basado en el nombre devuelve el objeto entero
     public Usuario getUsuario(String nombreUsuario)
     {
         String jsonResult = httpGetData("http://" + ip + "/prueba/Clasificados_GetUsuario.php?usuario=" + nombreUsuario);
         Usuario x = new Usuario();
 
         try
         {
             JSONObject jsonResponse = new JSONObject(jsonResult);
             JSONArray jsonMainNode = jsonResponse.optJSONArray("lista");
 
             JSONObject jsonChildNode = jsonMainNode.getJSONObject(0);
             int id = jsonChildNode.optInt("id");
             String usuario = jsonChildNode.optString("usuario");
             String password = jsonChildNode.optString("password");
             int admin = jsonChildNode.optInt("admin");
 
             x.setId(id);
             x.setUsuario(usuario);
             x.setPassword(password);
             x.setAdmin(admin);
         }
         catch (JSONException e)
         {
             e.printStackTrace();
         }
         return x;
     }
     //--------------------------------------------------------------------------------
 
     //--------------------- Metodos Clasificado
 
     public int insertarClasificado(Clasificado x)
     {
         int id = 0;
 
         String jsonResult = httpGetData("http://" + ip + "/prueba/Clasificados_InsertarClasificado.php?id_usuario="+ MainActivity.usuario.getId() +"&titulo=" + x.getTitulo() + "&descripcion=" + x.getDescripcion() + "&precio=" + x.getPrecio() + "&id_categoria=" + x.getCategoria().getId());
 
         try
         {
             JSONObject jsonResponse = new JSONObject(jsonResult);
             JSONArray jsonMainNode = jsonResponse.optJSONArray("lista");
 
             id = jsonMainNode.getInt(0);
         }
         catch (JSONException e)
         {
             e.printStackTrace();
         }
         return id;
     }
 
     public void modificarClasificado(Clasificado x)
     {
         httpGetData("http://" + ip + "/prueba/Clasificados_ModificarClasificado.php?id=" + x.getId() + "&titulo=" + x.getTitulo() + "&descripcion=" + x.getDescripcion() + "&precio=" + x.getPrecio() + "&id_categoria=" + x.getCategoria().getId());
     }
 
     public void eliminarClasificado(Clasificado x)
     {
         httpGetData("http://" + ip + "/prueba/Clasificados_EliminarClasificado.php?id=" + x.getId());
     }
 
     public ArrayList<Clasificado> getClasificados()
     {
         String jsonResult = httpGetData("http://" + ip + "/prueba/Clasificados_GetClasificados.php");
         ArrayList<Clasificado> lista = new ArrayList<Clasificado>();
         try
         {
             JSONObject jsonResponse = new JSONObject(jsonResult);
             JSONArray jsonMainNode = jsonResponse.optJSONArray("lista");
 
 
             for(int i = 0; i < jsonMainNode.length(); i++)
             {
                 JSONObject jsonChildNode = jsonMainNode.getJSONObject(i);
                 int id = jsonChildNode.optInt("id");
                 int idUsuario = jsonChildNode.optInt("id_usuario");
                 String titulo = jsonChildNode.optString("titulo");
                 String descripcion = jsonChildNode.optString("descripcion");
                 Double precio = jsonChildNode.optDouble("precio");
                 int idCategoria = jsonChildNode.optInt("id_categoria");
 
                 Clasificado x = new Clasificado();
 
                 x.setId(id);
 
                 Usuario usuario = new Usuario();
                 usuario.setId(idUsuario);
                 x.setUsuario(usuario);
 
                 x.setTitulo(titulo);
 
                 x.setDescripcion(descripcion);
 
                 x.setPrecio(precio);
 
                 ArrayList<Imagen> imagenes = getImagenesPorClasificado(x);
                 x.setImagenes(imagenes);
 
 
                 Categoria categoria = new Categoria();
                 categoria.setId(idCategoria);
                 //quitado para mejorar performance
 //                categoria = getCategoria(categoria);
                 x.setCategoria(categoria);
 
                 lista.add(x);
             }
         }
         catch (JSONException e)
         {
             e.printStackTrace();
         }
         return lista;
     }
 
     public ArrayList<Clasificado> getClasificadosPorCategoria(Categoria c)
     {
         String jsonResult = httpGetData("http://" + ip + "/prueba/Clasificados_GetClasificadosPorCategoria.php?id_categoria=" + c.getId());
         ArrayList<Clasificado> lista = new ArrayList<Clasificado>();
         try
         {
             JSONObject jsonResponse = new JSONObject(jsonResult);
             JSONArray jsonMainNode = jsonResponse.optJSONArray("lista");
 
 
             for(int i = 0; i < jsonMainNode.length(); i++)
             {
                 JSONObject jsonChildNode = jsonMainNode.getJSONObject(i);
                 int id = jsonChildNode.optInt("id");
                 int idUsuario = jsonChildNode.optInt("id_usuario");
                 String titulo = jsonChildNode.optString("titulo");
                 String descripcion = jsonChildNode.optString("descripcion");
                 Double precio = jsonChildNode.optDouble("precio");
                 int idCategoria = jsonChildNode.optInt("id_categoria");
 
                 Clasificado x = new Clasificado();
 
                 x.setId(id);
 
                 Usuario usuario = new Usuario();
                 usuario.setId(idUsuario);
                 x.setUsuario(usuario);
 
                 x.setTitulo(titulo);
 
                 x.setDescripcion(descripcion);
 
                 x.setPrecio(precio);
 
                 ArrayList<Imagen> imagenes = getImagenesPorClasificado(x);
                 x.setImagenes(imagenes);
 
                 Categoria categoria = new Categoria();
                 categoria.setId(idCategoria);
                 //quitado para mejorar performance
 //                categoria = getCategoria(categoria);
                 x.setCategoria(categoria);
 
                 lista.add(x);
             }
         }
         catch (JSONException e)
         {
             e.printStackTrace();
         }
         return lista;
     }
 
     public ArrayList<Clasificado> getClasificadosPorUsuario(Usuario usuarioId)
     {
         String jsonResult = httpGetData("http://" + ip + "/prueba/Clasificados_GetClasificadosPorUsuario.php?id=" + usuarioId.getId());
         ArrayList<Clasificado> lista = new ArrayList<Clasificado>();
         try
         {
             JSONObject jsonResponse = new JSONObject(jsonResult);
             JSONArray jsonMainNode = jsonResponse.optJSONArray("lista");
 
 
             for(int i = 0; i < jsonMainNode.length(); i++)
             {
                 JSONObject jsonChildNode = jsonMainNode.getJSONObject(i);
                 int id = jsonChildNode.optInt("id");
                 int idUsuario = jsonChildNode.optInt("id_usuario");
                 String titulo = jsonChildNode.optString("titulo");
                 String descripcion = jsonChildNode.optString("descripcion");
                 Double precio = jsonChildNode.optDouble("precio");
 
                 Clasificado x = new Clasificado();
 
                 x.setId(id);
 
                 Usuario usuario = new Usuario();
                 usuario.setId(idUsuario);
                 x.setUsuario(usuario);
 
                 x.setTitulo(titulo);
 
                 x.setDescripcion(descripcion);
 
                 x.setPrecio(precio);
 
                 ArrayList<Imagen> imagenes = getImagenesPorClasificado(x);
                 x.setImagenes(imagenes);
 
                 lista.add(x);
             }
         }
         catch (JSONException e)
         {
             e.printStackTrace();
         }
         return lista;
     }
 
     public Clasificado getClasificado(Clasificado clasificado)
     {
         String jsonResult = httpGetData("http://" + ip + "/prueba/Clasificados_GetClasificado.php?id=" + clasificado.getId());
         Clasificado x = new Clasificado();
 
         try
         {
             JSONObject jsonResponse = new JSONObject(jsonResult);
             JSONArray jsonMainNode = jsonResponse.optJSONArray("lista");
 
             JSONObject jsonChildNode = jsonMainNode.getJSONObject(0);
             int id = jsonChildNode.optInt("id");
             int idUsuario = jsonChildNode.optInt("id_usuario");
             String titulo = jsonChildNode.optString("titulo");
             String descripcion = jsonChildNode.optString("descripcion");
             Double precio = jsonChildNode.optDouble("precio");
             int idCategoria = jsonChildNode.optInt("id_categoria");
 
             x.setId(id);
 
             Usuario usuario = new Usuario();
             usuario.setId(idUsuario);
             x.setUsuario(usuario);
 
             x.setTitulo(titulo);
 
             x.setDescripcion(descripcion);
 
             x.setPrecio(precio);
 
             Categoria categoria = new Categoria();
             categoria.setId(idCategoria);
             x.setCategoria(categoria);
 
             ArrayList<Imagen> imagenes = getImagenesPorClasificado(x);
             x.setImagenes(imagenes);
 
         }
         catch (JSONException e)
         {
             e.printStackTrace();
         }
         return x;
     }
     //-------------------------------------------------------------------------
 
 
 
     //--------------------- Metodos Imagen
     public int insertarImagen(Imagen x)
     {
         int id = 0;
 
         String jsonResult = httpGetData("http://" + ip + "/prueba/Clasificados_InsertarImagen.php?id_clasificado=" + x.getClasificado().getId() + "&nombre=" + x.getNombre());
 
         try
         {
             JSONObject jsonResponse = new JSONObject(jsonResult);
             JSONArray jsonMainNode = jsonResponse.optJSONArray("lista");
 
             id = jsonMainNode.getInt(0);
         }
         catch (JSONException e)
         {
             e.printStackTrace();
         }
         return id;
     }
 
 
     //se le pasa el path de la imagen y la sube al servidor
     public void uploadFile(String filename)
     {
         try
         {
             FileInputStream fis = new FileInputStream(filename);
             HttpFileUploader htfu = new HttpFileUploader("http://10.0.0.3/prueba/Clasificados_SubirImagen.php","noparamshere", filename);
             htfu.doStart(fis);
         }
         catch (FileNotFoundException e)
         {
             e.printStackTrace();
         }
     }
 
 
     public ArrayList<Imagen> getImagenesPorClasificado(Clasificado clasificado)
     {
         String jsonResult = httpGetData("http://" + ip + "/prueba/Clasificados_GetImagenesPorClasificado.php?id_clasificado=" + clasificado.getId());
         ArrayList<Imagen> lista = new ArrayList<Imagen>();
         try
         {
             JSONObject jsonResponse = new JSONObject(jsonResult);
             JSONArray jsonMainNode = jsonResponse.optJSONArray("lista");
 
 
             for(int i = 0; i<jsonMainNode.length();i++)
             {
                 JSONObject jsonChildNode = jsonMainNode.getJSONObject(i);
                 int id = jsonChildNode.optInt("id");
 //                int idClasificado = jsonChildNode.optInt("id_clasificado");
                 String nombre = jsonChildNode.optString("nombre");
 
                 Imagen x = new Imagen();
                 x.setId(id);
 
 //                quitado para mejorar perdormance
 //                Clasificado clasificado2 = new Clasificado();
 //                clasificado2.setId(idClasificado);
 //                x.setClasificado(clasificado2);
 
                 x.setNombre(nombre);
                 lista.add(x);
             }
         }
         catch (JSONException e)
         {
             e.printStackTrace();
         }
         return lista;
     }
 
     public void eliminarImagen(Imagen x)
     {
         httpGetData("http://" + ip + "/prueba/Clasificados_EliminarImagen.php?id=" + x.getId());
     }
 
     public void eliminarImagenPorClasificado(Clasificado x)
     {
         httpGetData("http://" + ip + "/prueba/Clasificados_EliminarImagenesPorClasificado.php?id_clasificado=" + x.getId());
     }
 
     //----------------------------------------------------------------
 
 
     //--------------------- Metodos Categoria
 
     public ArrayList<Categoria> getCategorias()
     {
         String jsonResult = httpGetData("http://" + ip + "/prueba/Clasificados_GetCategorias.php");
         ArrayList<Categoria> lista = new ArrayList<Categoria>();
         try
         {
             JSONObject jsonResponse = new JSONObject(jsonResult);
             JSONArray jsonMainNode = jsonResponse.optJSONArray("lista");
 
 
             for(int i = 0; i<jsonMainNode.length();i++)
             {
                 JSONObject jsonChildNode = jsonMainNode.getJSONObject(i);
                 int id = jsonChildNode.optInt("id");
                 String nombre = jsonChildNode.optString("nombre");
 
                 Categoria x = new Categoria();
                 x.setId(id);
                 x.setNombre(nombre);
                 lista.add(x);
             }
         }
         catch (JSONException e)
         {
             e.printStackTrace();
         }
         return lista;
     }
 
     public void insertarCategoria(Categoria x)
     {
         httpGetData("http://" + ip + "/prueba/Clasificados_InsertarCategoria.php?nombre=" + x.getNombre());
     }
 
     public void modificarCategoria(Categoria x)
     {
         httpGetData("http://" + ip + "/prueba/Clasificados_ModificarCategoria.php?id=" + x.getId() + "&nombre=" + x.getNombre());
     }
 
     public void eliminarCategoria(Categoria x)
     {
         httpGetData("http://" + ip + "/prueba/Clasificados_EliminarCategoria.php?id=" + x.getId());
     }
     //-------------------------------------------------------------------------
 
 
     //------------------- Metodos para Cargar Una imagen desde una URL
     public Drawable imageOperations(String url)
     {
         try {
             InputStream is = (InputStream) this.fetch(url);
             Drawable d = Drawable.createFromStream(is, "src");
             return d;
         } catch (MalformedURLException e) {
             return null;
         } catch (IOException e) {
             return null;
         }
     }
 
     public Object fetch(String address) throws MalformedURLException,IOException {
         URL url = new URL(address);
         Object content = url.getContent();
         return content;
     }
     //----------------------------------------------------------------
 
 
 
 
     //---------------------------- Metodos de List View
 
     //devuelve la lista de las posiciones de los checkbox seleccionados
     public ArrayList<Integer> checkBoxSeleccionados(ListView listView, CheckBox cbx2)
     {
         ArrayList<Integer> lista = new ArrayList<Integer>();
        CheckBox cbx = (CheckBox)listView.findViewById(cbx2.getId());
 
         int firstPosition = listView.getFirstVisiblePosition();
         for(int i=firstPosition; i < listView.getCount(); i++)
         {
             View v1 = listView.getChildAt(i);
            cbx = (CheckBox)v1.findViewById(cbx2.getId());
             if(cbx.isChecked())
             {
                 lista.add(i);
             }
         }
         return lista;
     }
 
 
     //setea el largo en funcion de los items que tenga
     public void setListViewHeightBasedOnChildren(ListView listView)
     {
         ListAdapter listAdapter = listView.getAdapter();
         if (listAdapter == null)
         {
             // pre-condition
             return;
         }
 
         int totalHeight = 0;
         for (int i = 0; i < listAdapter.getCount(); i++)
         {
             View listItem = listAdapter.getView(i, null, listView);
             listItem.measure(0, 0);
             totalHeight += listItem.getMeasuredHeight();
         }
 
         ViewGroup.LayoutParams params = listView.getLayoutParams();
         params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
         listView.setLayoutParams(params);
         listView.requestLayout();
     }
     //------------------------------------------------------------------------
 
 }
