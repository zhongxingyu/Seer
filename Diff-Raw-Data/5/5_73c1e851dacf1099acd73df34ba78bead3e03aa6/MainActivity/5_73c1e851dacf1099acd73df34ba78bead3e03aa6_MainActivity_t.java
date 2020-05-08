 package ru.redsolution.bst.ui;
 
 import org.apache.http.auth.AuthenticationException;
 
 import ru.redsolution.bst.R;
 import ru.redsolution.bst.data.BST;
 import ru.redsolution.bst.data.DocumentType;
 import ru.redsolution.bst.data.OperationListener;
 import ru.redsolution.bst.ui.dialog.AuthorizationDialog;
 import ru.redsolution.dialogs.ConfirmDialogBuilder;
 import ru.redsolution.dialogs.DialogBuilder;
 import ru.redsolution.dialogs.DialogListener;
 import android.app.Dialog;
 import android.app.ProgressDialog;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnCancelListener;
 import android.content.Intent;
 import android.os.Bundle;
 import android.preference.Preference;
 import android.preference.Preference.OnPreferenceClickListener;
 import android.preference.PreferenceActivity;
 import android.widget.Toast;
 
 /**
  * Главное окно приложения.
  * 
  * @author alexander.ivanov
  * 
  */
 public class MainActivity extends PreferenceActivity implements
 		OnPreferenceClickListener, OperationListener, DialogListener {
 
 	private static final String SAVED_INTENT = "ru.redsolution.bst.ui.MainActivity.SAVED_INTENT";
 
 	private static final int DIALOG_AUTH_ID = 1;
 	private static final int DIALOG_ANOTHER_CONFIRM_ID = 2;
 
 	private DocumentType type;
 
 	private ProgressDialog progressDialog;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		addPreferencesFromResource(R.xml.main);
 		findPreference(getString(R.string.continue_action))
 				.setOnPreferenceClickListener(this);
 		findPreference(getString(R.string.supply_action))
 				.setOnPreferenceClickListener(this);
 		findPreference(getString(R.string.inventory_action))
 				.setOnPreferenceClickListener(this);
 		findPreference(getString(R.string.import_action))
 				.setOnPreferenceClickListener(this);
 		findPreference(getString(R.string.settings_action))
 				.setOnPreferenceClickListener(this);
 
 		progressDialog = new ProgressDialog(this);
 		progressDialog.setIndeterminate(true);
 		progressDialog.setTitle(R.string.import_action);
 		progressDialog.setMessage(getString(R.string.wait));
 		progressDialog.setOnCancelListener(new OnCancelListener() {
 			@Override
 			public void onCancel(DialogInterface dialog) {
 				BST.getInstance().cancelImport();
 			}
 		});
 
 		type = null;
 		if (savedInstanceState != null) {
 			String value = savedInstanceState.getString(SAVED_INTENT);
 			if (value != null)
 				try {
 					type = DocumentType.valueOf(value);
 				} catch (IllegalArgumentException e) {
 				}
 		}
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		updateView();
 		BST.getInstance().setOperationListener(this);
 		if (BST.getInstance().isImporting())
 			onBegin();
 	}
 
 	@Override
 	protected void onSaveInstanceState(Bundle outState) {
 		super.onSaveInstanceState(outState);
 		if (type != null)
 			outState.putString(SAVED_INTENT, type.toString());
 	}
 
 	@Override
 	protected void onPause() {
 		super.onPause();
 		BST.getInstance().setOperationListener(null);
 		dismissProgressDialog();
 	}
 
 	@Override
 	public boolean onPreferenceClick(Preference preference) {
 		if (preference.getKey().equals(getString(R.string.continue_action))) {
 			startActivity(new Intent(this, DocumentActivity.class));
 		} else if (preference.getKey()
 				.equals(getString(R.string.supply_action))) {
 			checkAndCreateDocument(DocumentType.supply);
 		} else if (preference.getKey().equals(
 				getString(R.string.inventory_action))) {
 			checkAndCreateDocument(DocumentType.inventory);
 		} else if (preference.getKey()
 				.equals(getString(R.string.import_action))) {
 			if ("".equals(BST.getInstance().getLogin()))
 				showDialog(DIALOG_AUTH_ID);
 			else
 				BST.getInstance().importData();
 		} else if (preference.getKey().equals(
 				getString(R.string.settings_action))) {
 			startActivity(new Intent(this, SettingsActivity.class));
 		}
 		return true;
 	}
 
 	/**
 	 * Создать документ, при необходимости отобразить диалог подтверждения.
 	 * 
 	 * @param type
 	 */
 	private void checkAndCreateDocument(DocumentType type) {
 		this.type = type;
 		if (BST.getInstance().getDocumentType() == null)
 			createDocument();
 		else
 			showDialog(DIALOG_ANOTHER_CONFIRM_ID);
 	}
 
 	/**
 	 * Открыть окно создания документа.
 	 */
 	private void createDocument() {
 		Intent intent = new Intent(this, HeaderActivity.class);
 		intent.putExtra(HeaderActivity.EXTRA_TYPE, this.type.toString());
 		startActivity(intent);
 	}
 
 	@Override
 	protected Dialog onCreateDialog(int id) {
 		switch (id) {
 		case DIALOG_AUTH_ID:
 			return new AuthorizationDialog(this, id, this).create();
 		case DIALOG_ANOTHER_CONFIRM_ID:
 			return new ConfirmDialogBuilder(this, id, this)
 					.setTitle(R.string.another_title)
 					.setMessage(R.string.another_confirm).create();
 		default:
 			return super.onCreateDialog(id);
 		}
 	}
 
 	@Override
 	public void onAccept(DialogBuilder dialogBuilder) {
 		switch (dialogBuilder.getDialogId()) {
 		case DIALOG_AUTH_ID:
 			BST.getInstance().importData();
 			break;
 		case DIALOG_ANOTHER_CONFIRM_ID:
 			createDocument();
 			break;
 		default:
 			break;
 		}
 		updateView();
 	}
 
 	@Override
 	public void onDecline(DialogBuilder dialogBuilder) {
 	}
 
 	@Override
 	public void onCancel(DialogBuilder dialogBuilder) {
 	}
 
 	@Override
 	public void onBegin() {
 		updateView();
 		progressDialog.show();
 	}
 
 	@Override
 	public void onDone() {
 		dismissProgressDialog();
 	}
 
 	@Override
 	public void onCancelled() {
 		dismissProgressDialog();
 	}
 
 	@Override
 	public void onError(RuntimeException exception) {
 		dismissProgressDialog();
 		if (exception.getCause() instanceof AuthenticationException) {
 			showDialog(DIALOG_AUTH_ID);
 			Toast.makeText(this, R.string.auth_error, Toast.LENGTH_LONG).show();
 		} else {
 			Toast.makeText(this, R.string.connection_error, Toast.LENGTH_LONG)
 					.show();
 		}
 	}
 
 	private void dismissProgressDialog() {
 		updateView();
 		progressDialog.dismiss();
 	}
 
 	private void updateView() {
 		boolean isImported = BST.getInstance().isImported();
 		DocumentType documentType = BST.getInstance().getDocumentType();
 		findPreference(getString(R.string.continue_action)).setEnabled(
 				isImported && documentType != null);
		if (documentType == DocumentType.supply) {
			findPreference(getString(R.string.continue_action)).setSummary(
					R.string.continue_supply_summary);
		} else if (documentType == DocumentType.inventory) {
 			findPreference(getString(R.string.continue_action)).setSummary(
 					R.string.continue_inventory_summary);
 		} else {
 			findPreference(getString(R.string.continue_action)).setSummary(
 					R.string.continue_summary);
 		}
 		findPreference(getString(R.string.supply_action))
 				.setEnabled(isImported);
 		findPreference(getString(R.string.inventory_action)).setEnabled(
 				isImported);
 		findPreference(getString(R.string.settings_action)).setEnabled(
 				isImported);
 	}
 
 }
