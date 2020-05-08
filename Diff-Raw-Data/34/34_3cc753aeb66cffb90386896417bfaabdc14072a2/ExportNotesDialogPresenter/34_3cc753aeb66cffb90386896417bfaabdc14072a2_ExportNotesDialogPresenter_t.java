 package net.todd.biblestudy.rcp;
 
 import net.todd.biblestudy.common.IListener;
 
 public class ExportNotesDialogPresenter {
 	public static void create(final IExportNotesDialogView view, final IExportNotesDialogModel model,
 			final IFileDialogLauncher fileDialogLauncher) {
 		view.addListener(new IListener() {
 			@Override
 			public void handleEvent() {
 				model.doExport();
 			}
 		}, IExportNotesDialogView.OK_BUTTON_PRESSED);
 
 		view.addListener(new IListener() {
 			@Override
 			public void handleEvent() {
 				model.setSelectedNotes(view.getSelectedNotes());
 			}
 		}, IExportNotesDialogView.SELECTION);
 
 		model.addListener(new IListener() {
 			@Override
 			public void handleEvent() {
 				view.setSelectedNotes(model.getSelectedNotes());
				view.setExportButtonEnabled(!model.getSelectedNotes().isEmpty());
 			}
 		}, IExportNotesDialogModel.SELECTION);
 
 		view.addListener(new IListener() {
 			@Override
 			public void handleEvent() {
 				model.selectAll();
 			}
 		}, IExportNotesDialogView.SELECTION_ALL);
 
 		view.addListener(new IListener() {
 			@Override
 			public void handleEvent() {
 				model.selectNone();
 			}
 		}, IExportNotesDialogView.SELECTION_NONE);
 
 		view.addListener(new IListener() {
 			@Override
 			public void handleEvent() {
 				model.setExportFileLocation(fileDialogLauncher.launchFileDialog());
 			}
 		}, IExportNotesDialogView.FILE_BROWSE);
 
 		model.addListener(new IListener() {
 			@Override
 			public void handleEvent() {
 				view.setExportFileLocation(model.getExportFileLocation());
 			}
 		}, IExportNotesDialogModel.EXPORT_FILE_LOCATION);
 
 		view.addListener(new IListener() {
 			@Override
 			public void handleEvent() {
 				model.setExportFileLocation(view.getExportFileLocation());
 			}
 		}, IExportNotesDialogView.EXPORT_FILE_LOCATION);
 
 		view.populateAllNotes(model.getAllNotes());
 		view.setExportButtonEnabled(!model.getSelectedNotes().isEmpty());
 		view.setSelectedNotes(model.getSelectedNotes());
 	}
 }
