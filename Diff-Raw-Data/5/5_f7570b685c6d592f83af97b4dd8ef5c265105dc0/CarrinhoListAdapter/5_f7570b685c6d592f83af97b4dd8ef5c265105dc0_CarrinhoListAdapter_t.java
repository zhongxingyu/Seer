 package com.example.xcsbooks;
 
 import java.util.HashMap;
 import java.util.List;
 
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.example.xcsbooks.control.JSONParser;
 import com.example.xcsbooks.model.Dinheiro;
 import com.example.xcsbooks.model.ItemPedido;
 import com.example.xcsbooks.model.LivroNovo;
 
 //TODO: Corrigir bug de no mostrar corretamente a lista
 public class CarrinhoListAdapter extends ExtendedSimpleAdapter {
 	LayoutInflater inflater = null;
 	Context context = null;
 	List<HashMap<String, Object>> data;
 	
 	public CarrinhoListAdapter(Context context,
 			List<HashMap<String, Object>> data, int resource, String[] from,
 			int[] to) {
 		super(context, (List<HashMap<String, Object>>) data, resource, from, to);
 		inflater = LayoutInflater.from(context);
 		this.context = context;
 		this.data = data;
 	}
 
 	@Override
 	public View getView(final int position, View convertView, ViewGroup parent)
 	{
 		final View view = super.getView(position, convertView, parent);
 	    ViewHolder holder = (ViewHolder) view.getTag();
 	    if(holder == null){
 	    	holder = new ViewHolder();
 	    	holder.titulo = (TextView) view.findViewById(R.id.itemCarrinho_txtTituloLivro);
 	    	holder.autor = (TextView) view.findViewById(R.id.itemCarrinho_txtAutorLivro);
 	    	holder.editora = (TextView) view.findViewById(R.id.itemCarrinho_txtEditoraLivro);
 	    	holder.preco = (TextView) view.findViewById(R.id.itemCarrinho_txtPrecoLivro);
 	    	holder.quant = (TextView) view.findViewById(R.id.itemCarrinho_txtQuantidadeLivro);
 	    	holder.dec =(Button) view.findViewById(R.id.itemCarrinho_btnDecremento);
 	    	holder.inc = (Button) view.findViewById(R.id.itemCarrinho_btnIncremento);
 	    	holder.remove = (Button) view.findViewById(R.id.itemCarrinho_remover);
 	    	holder.thumbnail = (ImageView) view.findViewById(R.id.itemCarrinho_thumbLivro);
 	    	view.setTag(holder);
 	    	final Context context = view.getContext();
 	    	
 	    	holder.dec.setOnClickListener(new OnClickListener() {
 				
 				@Override
 				public void onClick(View v) {
 					updateQuantPreco(-1, position);
 				}
 			});
 	    	
 	    	holder.inc.setOnClickListener(new OnClickListener() {
 				
 				@Override
 				public void onClick(View v) {
 					updateQuantPreco(1, position);
 				}
 			});
 	    	
 	    	holder.remove.setOnClickListener(new OnClickListener() {
 				
 				@Override
 				public void onClick(View v) {
 					data.remove(position);
 					
 					// Efetivamente removendo o livro do carrinho
 					SharedPreferences prefs = context.getSharedPreferences("CARRINHO", context.MODE_PRIVATE);
 					String strItensPedido = prefs.getString("ITENSCARRINHO", JSONParser.DEFAULT_PRODUTOS);
 					
 					// Obtem a lista de livros que est guardada como um JSON
 					List<ItemPedido> itens = JSONParser.ItemPedidoFromJSON(strItensPedido);
 
 					itens.remove(position);
 					// Transforma a nova lista em um JSON
 					String carrinho = JSONParser.ItemPedidoToJSON(itens);
 					//Guarda o novo JSON
 					SharedPreferences.Editor editor = prefs.edit();
 					editor.clear();
 					editor.putString("ITENSCARRINHO", carrinho);
 					editor.commit();
 
 					//Notifica o usuario, atualiza a lista
 					Toast.makeText(context, "Item removido", Toast.LENGTH_SHORT).show();
 					notifyDataSetChanged();
 				}
 			});
 	    }
 	    
 	    return view;
 		
 	}
 	
 	static class ViewHolder {
 		ImageView thumbnail;
 		TextView titulo;
 		TextView autor;
 		TextView editora;
 		TextView preco;
 		TextView quant;
 		Button inc;
 		Button dec;
 		Button remove;
 	}
 	
 	private void updateQuantPreco(int q, int position){
 		int quant = (Integer) data.get(position).get("itemCarrinho_quantidadeItem");
 		Dinheiro prevPreco = new Dinheiro((String) data.get(position).get("itemCarrinho_totalItem"));
 		Log.d("PRECO", "PrevPreco = " + prevPreco.toString());
 		Dinheiro precoOriginal = new Dinheiro(prevPreco.div(quant));
 		Log.d("PRECO", "PrecoOriginal = " + precoOriginal.toString());
 		
 		//Ler carrinho do sharedpref
 		SharedPreferences prefs = context.getSharedPreferences("CARRINHO", context.MODE_PRIVATE);
 		String strItensPedido = prefs.getString("ITENSCARRINHO", JSONParser.DEFAULT_PRODUTOS);
 		
 		// Obtem a lista de livros que est guardada como um JSON
 		List<ItemPedido> itens = JSONParser.ItemPedidoFromJSON(strItensPedido);
 
 		int quantidadeDisponivel = itens.get(position).getProduto().getQuantidade();
 		
 		if(q < 0){
			if(quant > 1)
 				quant += q;
 			else 
 				return;
 		} else {
			if(quant >= 1 && quant < quantidadeDisponivel)
 				quant += q;
 		}
 		
 		Dinheiro novoPreco = new Dinheiro(precoOriginal.mult(quant));
 		data.get(position).put("itemCarrinho_quantidadeItem", quant);
 		data.get(position).put("itemCarrinho_totalItem", novoPreco.toString());
 		
 		itens.get(position).setQuantidade(quant);
 		// Transforma a nova lista em um JSON
 		String carrinho = JSONParser.ItemPedidoToJSON(itens);
 		//Guarda o novo JSON
 		SharedPreferences.Editor editor = prefs.edit();
 		editor.clear();
 		editor.putString("ITENSCARRINHO", carrinho);
 		editor.commit();
 		
 		notifyDataSetChanged();
 	}
 	
 }
