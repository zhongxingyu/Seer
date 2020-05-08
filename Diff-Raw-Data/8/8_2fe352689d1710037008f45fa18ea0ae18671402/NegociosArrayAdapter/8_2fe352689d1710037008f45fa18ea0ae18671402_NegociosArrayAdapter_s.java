 package com.mawape.aimant.widget;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.content.Context;
 import android.content.Intent;
import android.net.Uri;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.Filter;
 import android.widget.Filterable;
 import android.widget.ImageView;
 import android.widget.Toast;
 import android.widget.ImageView.ScaleType;
import android.widget.TextView;
 
 import com.mawape.aimant.R;
 import com.mawape.aimant.activities.NegocioMapActivity;
 import com.mawape.aimant.constants.AppConstants;
 import com.mawape.aimant.entities.Categoria;
 import com.mawape.aimant.entities.Negocio;
 import com.mawape.aimant.utilities.AndroidServicesUtil;
 import com.mawape.aimant.utilities.ApacheStringUtils;
 
 public class NegociosArrayAdapter extends ArrayAdapter<Negocio> implements
 		AdapterView.OnItemClickListener, Filterable {
 
 	private final Object mLock = new Object();
 	private List<Negocio> filteredValues;
 	private List<Negocio> originalValues;
 	private Filter filter;
 	private Categoria currentCategoria;
 
 	static class NegociosViewHolder {
 		TextView txtNombre;
 		TextView txtDireccion;
 		ImageView imgIcon;
 		ImageView imgCall;
 		ImageView imgGo;
 	}
 
 	public NegociosArrayAdapter(Context context, List<Negocio> values,
 			Categoria currentCategoria) {
 		super(context, R.layout.negocios_row, values);
 		this.filteredValues = values;
 		this.currentCategoria = currentCategoria;
 	}
 
 	@Override
 	public View getView(int position, View rowView, ViewGroup parent) {
 
 		final Negocio negocioSeleccionado = getItem(position);
 		// Keeps reference to avoid future findViewById()
 		NegociosViewHolder viewHolder;
 		if (rowView == null) {
 			LayoutInflater inflater = (LayoutInflater) getContext()
 					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 			rowView = inflater.inflate(R.layout.negocios_row, parent, false);
 			viewHolder = new NegociosViewHolder();
 			viewHolder.imgCall = (ImageView) rowView
 					.findViewById(R.id.negRowBtnCall);
 			viewHolder.imgGo = (ImageView) rowView
 					.findViewById(R.id.negRowBtnMap);
 			viewHolder.imgIcon = (ImageView) rowView
 					.findViewById(R.id.negRowImg);
 			viewHolder.txtDireccion = (TextView) rowView
 					.findViewById(R.id.negRowDir);
 			viewHolder.txtNombre = (TextView) rowView
 					.findViewById(R.id.negRowNombre);
 
 			rowView.setTag(viewHolder);
 		} else {
 			viewHolder = (NegociosViewHolder) rowView.getTag();
 		}
 		viewHolder.txtNombre.setText(negocioSeleccionado.getNombre());
 		viewHolder.txtDireccion.setText(negocioSeleccionado.getDireccion());
 		String imgPath = negocioSeleccionado.getImgPath();
 		if (!ApacheStringUtils.isEmpty(imgPath)) {
 			String mDrawableName = imgPath.substring(0,
 					imgPath.lastIndexOf("."));// no-extension
 			Integer imgResourceId = getContext().getResources()
 					.getIdentifier(mDrawableName, "drawable",
 							getContext().getPackageName());
 			if (imgResourceId > 0) {
 				viewHolder.imgIcon.setImageResource(imgResourceId);
				viewHolder.imgIcon.setScaleType(ScaleType.FIT_XY);
 			}
 		} else {
 			viewHolder.imgIcon.setImageResource(R.drawable.image_default);
 		}
 
 		viewHolder.imgCall.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				makePhoneCall(negocioSeleccionado.getTelefonoPrimario());
 			}
 		});
 
 		viewHolder.imgGo.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				showMap(negocioSeleccionado);
 			}
 		});
 
 		return rowView;
 	}
 
 	public Negocio getItem(int position) {
 		return filteredValues.get(position);
 	}
 
 	private void makePhoneCall(String phoneNumber) {
 		AndroidServicesUtil.makePhoneCall(getContext(), phoneNumber);
 	}
 
 	private void showMap(Negocio negocioSeleccionado) {
 		Toast.makeText(getContext(), getContext().getResources().getString(R.string.loading_map),
 				Toast.LENGTH_SHORT).show();
 		Bundle bundle = new Bundle();
 		bundle.putSerializable(AppConstants.CATEGORIA_SELECCIONADA_KEY,
 				currentCategoria);
 		bundle.putSerializable(AppConstants.NEGOCIO_SELECCIONADO_KEY,
 				negocioSeleccionado);
 		Intent intent = new Intent(getContext(), NegocioMapActivity.class);
 		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 		intent.putExtras(bundle);
 		getContext().startActivity(intent);
 	}
 
 	public List<Negocio> getFilteredValues() {
 		return filteredValues;
 	}
 
 	public Filter getFilter() {
 		if (filter == null) {
 			filter = new NegociosFilter();
 		}
 		return filter;
 	}
 
 	private class NegociosFilter extends Filter {
 
 		@Override
 		protected FilterResults performFiltering(CharSequence constraint) {
 			FilterResults results = new FilterResults();
 			String prefix = constraint.toString().toLowerCase();
 			List<Negocio> resultValues = new ArrayList<Negocio>();
 
 			if (originalValues == null) {
 				synchronized (mLock) {
 					// first time
 					originalValues = new ArrayList<Negocio>(filteredValues);
 				}
 			}
 			if (prefix != null && prefix.length() > 0) {
 				for (Negocio negocio : originalValues) {
 					if (negocio.getNombre().toLowerCase().contains(prefix) || negocio.containsInTags(prefix)) {
 						resultValues.add(negocio);
 					}
 				}
 			} else {// if nothing input then all
 				resultValues.addAll(originalValues);
 			}
 			results.values = resultValues;
 			results.count = resultValues.size();
 			return results;
 		}
 
 		@Override
 		protected void publishResults(CharSequence constraint,
 				FilterResults results) {
 			filteredValues = (List<Negocio>) results.values;
 			notifyDataSetChanged();
 			clear();
 			for (Negocio n : filteredValues) {
 				add(n);
 			}
 			notifyDataSetInvalidated();
 
 		}
 
 	}
 
 	@Override
 	public void onItemClick(AdapterView<?> parent, View itemClicked,
 			int position, long id) {
 		final Negocio negocioSeleccionado = getItem(position);
 		showMap(negocioSeleccionado);
 	}
 
 }
