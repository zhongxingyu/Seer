 package com.lincbio.lincxmap.android.app;
 
 import java.io.File;
 import java.util.List;
 
 import com.lincbio.lincxmap.R;
 import com.lincbio.lincxmap.android.Constants;
 import com.lincbio.lincxmap.android.database.DatabaseHelper;
 import com.lincbio.lincxmap.android.utils.FileUtils;
 import com.lincbio.lincxmap.android.utils.Toasts;
 import com.lincbio.lincxmap.android.view.FlowLayout;
 import com.lincbio.lincxmap.pojo.ImageSource;
 import com.lincbio.lincxmap.pojo.Template;
 import com.lincbio.lincxmap.pojo.TemplateItem;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.ContentResolver;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.Bundle;
 import android.provider.MediaStore;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnLongClickListener;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 /**
  * Template list UI
  * 
  * @author Johnson Lee
  * 
  */
 public class TemplateListActivity extends Activity implements Constants {
 	private final DatabaseHelper dbHelper = new DatabaseHelper(this);
 	private final ImageSourceListener imgSrcListener = new ImageSourceListener();
 
 	private FlowLayout desktop;
 	private Template selectedTemplate;
 	private String selectedImage;
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		switch (resultCode) {
 		case RESULT_OK:
 			switch (requestCode) {
 			case ImageSource.IMAGE_SOURCE_CAPTURE:
 				break;
 			case ImageSource.IMAGE_SOURCE_GALLERY:
 				Uri uri = data.getData();
 				String scheme = uri.getScheme();
 
 				if ("file".equalsIgnoreCase(scheme)) {
 					this.selectedImage = uri.getPath();
 				} else if ("content".equalsIgnoreCase(scheme)) {
 					ContentResolver cr = getContentResolver();
 					Cursor c = cr.query(uri, null, null, null, null);
 
 					if (null == c)
 						break;
 
 					try {
 						if (c.moveToNext()) {
 							this.selectedImage = c.getString(1);
 						}
 					} finally {
 						c.close();
 					}
 				}
 				break;
 			default:
 				return;
 			}
 
 			if (null == this.selectedImage)
 				return;
 
 			long id = this.selectedTemplate.getId();
 			List<TemplateItem> items = this.dbHelper.getTemplateItems(id);
 			this.selectedTemplate.getItems().clear();
 			this.selectedTemplate.getItems().addAll(items);
 
 			Intent intent = new Intent(this, ProfileChooser.class);
 			intent.putExtra(PARAM_IMAGE_SOURCE, this.selectedImage);
 			intent.putExtra(PARAM_TEMPLATE_OBJECT, this.selectedTemplate);
 			startActivity(intent);
 			break;
 		default:
 			this.finish();
 			break;
 		}
 	}
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		this.setContentView(R.layout.template);
 		this.desktop = (FlowLayout) findViewById(R.id.desktop);
 	}
 
 	@Override
 	protected void onStart() {
 		super.onStart();
 
 		View item;
 		ImageView del;
 		ImageView icon;
 		TextView label;
 
 		this.desktop.removeAllViews();
 
 		// query database to fetch detection templates
 		String text = getString(R.string.label_new);
 		List<Template> templates = this.dbHelper.getTemplates();
 		templates.add(new Template(-1, text, 0, 0));
 
 		for (final Template tpl : templates) {
 			item = this.getLayoutInflater().inflate(R.layout.desktop_item,
 					this.desktop, false);
 			del = (ImageView) item.findViewById(R.id.desktop_item_del);
 			icon = (ImageView) item.findViewById(R.id.desktop_item_icon);
 			label = (TextView) item.findViewById(R.id.desktop_item_label);
 			label.setText(tpl.getName());
 
 			if (-1 == tpl.getId()) {
 				icon.setImageResource(R.drawable.ic_desktop_add);
 				item.setOnClickListener(new NewTemplateListener());
 			} else {
 				del.setOnClickListener(new DeleteTemplateListener(tpl, item));
 				icon.setImageResource(R.drawable.ic_desktop);
 				item.setOnLongClickListener(new DesktopListener());
 				item.setOnClickListener(new DetectionWizardListener(tpl));
 			}
 
 			this.desktop.addView(item);
 		}
 	}
 
 	private final class DeleteTemplateListener implements OnClickListener {
 		private final Template template;
 		private final View item;
 
 		public DeleteTemplateListener(Template template, View item) {
 			this.template = template;
 			this.item = item;
 		}
 
 		@Override
 		public void onClick(View v) {
 			try {
 				dbHelper.deleteTemplate(this.template.getId());
 				desktop.removeView(this.item);
 			} catch (Throwable t) {
 				Toasts.show(TemplateListActivity.this, t);
 			}
 		}
 	}
 
 	private final class DesktopListener implements OnLongClickListener {
 
 		@Override
 		public boolean onLongClick(View v) {
 			View del = v.findViewById(R.id.desktop_item_del);
 			del.setVisibility(View.VISIBLE);
 			return true;
 		}
 
 	}
 
 	private final class DetectionWizardListener implements OnClickListener {
 		private final Template template;
 
 		public DetectionWizardListener(Template template) {
 			this.template = template;
 		}
 
 		@Override
 		public void onClick(View v) {
 			View del = v.findViewById(R.id.desktop_item_del);
 
 			if (null != del && View.VISIBLE == del.getVisibility()) {
 				del.setVisibility(View.GONE);
 				return;
 			}
 
 			CharSequence[] choices = getResources().getTextArray(
 					R.array.array_image_sources);
 			selectedTemplate = this.template;
 			new AlertDialog.Builder(TemplateListActivity.this)
 					.setIcon(android.R.drawable.ic_dialog_alert)
 					.setTitle(R.string.title_choose_image)
 					.setItems(choices, imgSrcListener).show();
 		}
 
 	}
 
 	private final class NewTemplateListener implements OnClickListener {
 
 		@Override
 		public void onClick(View v) {
 			Class<?> cls = TemplateSpecActivity.class;
 			startActivity(new Intent(TemplateListActivity.this, cls));
 		}
 
 	}
 
 	private final class ImageSourceListener implements
 			android.content.DialogInterface.OnClickListener {
 
 		@Override
 		public void onClick(DialogInterface dialog, int which) {
 			Intent intent = null;
 
 			switch (which) {
 			case ImageSource.IMAGE_SOURCE_CAPTURE:
				File image = FileUtils.newTempFile(".jpg");
 				intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
 				intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(image));
 				selectedImage = image.toString();
 				break;
 			case ImageSource.IMAGE_SOURCE_GALLERY:
 				intent = new Intent(Intent.ACTION_GET_CONTENT);
 				intent.setType("image/*");
 				break;
 			}
 
 			if (null == intent)
 				return;
 
 			startActivityForResult(intent, which);
 		}
 
 	}
 
 }
