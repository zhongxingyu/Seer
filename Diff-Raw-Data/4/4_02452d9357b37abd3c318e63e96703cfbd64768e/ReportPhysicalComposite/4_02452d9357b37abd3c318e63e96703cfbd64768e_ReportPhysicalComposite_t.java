 package de.hswt.hrm.inspection.ui.part;
 
 import java.util.Collection;
 
 import javax.annotation.PostConstruct;
 import javax.inject.Inject;
 
 import org.eclipse.e4.core.contexts.IEclipseContext;
 import org.eclipse.jface.window.IShellProvider;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.ScrolledComposite;
 import org.eclipse.swt.events.FocusAdapter;
 import org.eclipse.swt.events.FocusEvent;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.List;
 import org.eclipse.ui.forms.widgets.FormToolkit;
 import org.eclipse.ui.forms.widgets.Section;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import de.hswt.hrm.common.database.exception.DatabaseException;
 import de.hswt.hrm.common.database.exception.SaveException;
 import de.hswt.hrm.common.observer.Observable;
 import de.hswt.hrm.common.observer.Observer;
 import de.hswt.hrm.common.ui.swt.forms.FormUtil;
 import de.hswt.hrm.common.ui.swt.layouts.LayoutUtil;
 import de.hswt.hrm.common.ui.swt.utils.ContentProposalUtil;
 import de.hswt.hrm.i18n.I18n;
 import de.hswt.hrm.i18n.I18nFactory;
 import de.hswt.hrm.inspection.model.Inspection;
 import de.hswt.hrm.inspection.model.PhysicalRating;
 import de.hswt.hrm.inspection.model.SamplingPointType;
 import de.hswt.hrm.inspection.service.InspectionService;
 import de.hswt.hrm.misc.comment.model.Comment;
 import de.hswt.hrm.misc.comment.service.CommentService;
 import de.hswt.hrm.plant.model.Plant;
 import de.hswt.hrm.scheme.model.SchemeComponent;
 
 public class ReportPhysicalComposite extends AbstractComponentRatingComposite {
 
 	@Inject
 	private InspectionService inspectionService;
 
 	@Inject
 	private IEclipseContext context;
 
 	@Inject
 	private CommentService commentService;
 
 	@Inject
 	private IShellProvider shellProvider;
 	
 	private Collection<PhysicalRating> ratings;
 
 	private FormToolkit formToolkit = new FormToolkit(Display.getDefault());
 
 	private Button nothingRadioButton;
 	private Button climateParameterRadioButton;
 	private Button photoRadioButton;
 	private Button dustRadioButton;
 
 	private Combo commentCombo;
 
 	private List gradeList;
 	private List weightList;
 	private List list;
 
 	private static final Logger LOG = LoggerFactory
 			.getLogger(ReportPhysicalComposite.class);
 	
 	private static final I18n I18N = I18nFactory.getI18n(ReportPhysicalComposite.class);
 
 	private final Observable<Integer> grade = new Observable<>();
 	private final Observable<SamplingPointType> samplePointType = new Observable<>();
 
 	private SchemeComponent currentSchemeComponent;
 
 	private Inspection inspection;
 
 	/**
 	 * Create the composite.
 	 * 
 	 * Do not use this constructor when instantiate this composite! It is only
 	 * included to make the WindowsBuilder working.
 	 * 
 	 * @param parent
 	 * @param style
 	 */
 	private ReportPhysicalComposite(Composite parent, int style) {
 		super(parent, SWT.NONE);
 		createControls();
 	}
 
 	/**
 	 * Create the composite.
 	 * 
 	 * @param parent
 	 * @param style
 	 */
 	public ReportPhysicalComposite(Composite parent) {
 		super(parent, SWT.NONE);
 		formToolkit.dispose();
 		formToolkit = FormUtil.createToolkit();
 	}
 
 	@PostConstruct
 	public void createControls() {
 		GridLayout gl = new GridLayout(1, false);
 		gl.marginBottom = 5;
 		gl.marginLeft = 5;
 		gl.marginWidth = 0;
 		gl.marginHeight = 0;
 		setLayout(gl);
 		setBackground(getDisplay().getSystemColor(SWT.COLOR_WHITE));
 
 		Composite c = new Composite(this, SWT.NONE);
 		c.setLayoutData(LayoutUtil.createFillData());
 		c.setLayout(new FillLayout());
 
 		ScrolledComposite sc = new ScrolledComposite(c, SWT.H_SCROLL
 				| SWT.V_SCROLL);
 		sc.setExpandVertical(true);
 		sc.setExpandHorizontal(true);
 
 		Composite composite = new Composite(sc, SWT.None);
 		composite.setBackgroundMode(SWT.INHERIT_FORCE);
 		gl = new GridLayout(2, true);
 		gl.marginWidth = 0;
 		gl.marginHeight = 0;
 		composite.setLayout(gl);
 		sc.setContent(composite);
 
 		Section physicalSection = formToolkit.createSection(composite,
 				Section.TITLE_BAR);
 		formToolkit.paintBordersFor(physicalSection);
 		physicalSection.setText(I18N.tr("Physical rating"));
 		physicalSection.setLayoutData(LayoutUtil.createHorzFillData());
 		FormUtil.initSectionColors(physicalSection);
 
 		Section imageSection = formToolkit.createSection(composite,
 				Section.TITLE_BAR);
 		formToolkit.paintBordersFor(imageSection);
 		imageSection.setText(I18N.tr("Photos"));
 		imageSection.setLayoutData(LayoutUtil.createHorzFillData());
 		FormUtil.initSectionColors(imageSection);
 
 		Section tagSection = formToolkit.createSection(composite,
 				Section.TITLE_BAR);
 		formToolkit.paintBordersFor(tagSection);
 		tagSection.setText(I18N.tr("Scheme tags"));
 		tagSection.setLayoutData(LayoutUtil.createHorzFillData(2));
 		FormUtil.initSectionColors(tagSection);
 
 		/******************************
 		 * physical rating components
 		 *****************************/
 		Composite physicalComposite = new Composite(physicalSection, SWT.NONE);
 		physicalComposite.setBackgroundMode(SWT.INHERIT_DEFAULT);
 		formToolkit.adapt(physicalComposite);
 		formToolkit.paintBordersFor(physicalComposite);
 		gl = new GridLayout(4, false);
 		gl.marginWidth = 0;
 		physicalComposite.setLayout(gl);
 		physicalSection.setClient(physicalComposite);
 
 		Label gradeLabel = new Label(physicalComposite, SWT.NONE);
 		gradeLabel.setLayoutData(LayoutUtil.createHorzFillData(2));
 		formToolkit.adapt(gradeLabel, true, true);
 		gradeLabel.setText(I18N.tr("Grade"));
 
 		Label weightLabel = new Label(physicalComposite, SWT.NONE);
 		weightLabel.setLayoutData(LayoutUtil.createHorzFillData(2));
 		formToolkit.adapt(weightLabel, true, true);
 		weightLabel.setText(I18N.tr("Weight"));
 
 		gradeList = new List(physicalComposite, SWT.BORDER);
 		gradeList.setLayoutData(LayoutUtil.createHorzFillData(2));
 		formToolkit.adapt(gradeList, true, true);
 		// 0 = not rated
 		for (int i = 0; i < 6; i++) {
 			gradeList.add(Integer.toString(i));
 		}
 		gradeList.select(0);
 		gradeList.addSelectionListener(new SelectionListener() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				int selection = gradeList.getSelectionIndex();
 				if (selection == -1) {
 					selection = 0;
 				}
 				grade.set(selection);
 				PhysicalRating rating = 
 						getRatingForComponent(currentSchemeComponent);
 				rating.setRating(selection);
 			}
 
 			@Override
 			public void widgetDefaultSelected(SelectionEvent e) {
 			}
 		});
 
 		weightList = new List(physicalComposite, SWT.BORDER);
 		weightList.setLayoutData(LayoutUtil.createHorzFillData(2));
 		formToolkit.adapt(weightList, true, true);
 		for (int i = 1; i <= 6; i++) {
 			weightList.add(Integer.toString(i));
 		}
 		weightList.select(0);
 		weightList.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				int selection = weightList.getSelectionIndex();
 				if (selection == -1) {
 					selection = 0;
 				}
 				PhysicalRating rating = 
 						getRatingForComponent(currentSchemeComponent);
 				if (rating == null) {
 					return;
 				}
 				rating.setQuantifier(selection);
 			}
 		});
 
 		Label commentLabel = new Label(physicalComposite, SWT.NONE);
 		commentLabel.setLayoutData(LayoutUtil.createLeftGridData());
 		formToolkit.adapt(commentLabel, true, true);
 		commentLabel.setText(I18N.tr("Comment"));
 
 		commentCombo = new Combo(physicalComposite, SWT.MULTI);
 		commentCombo.setLayoutData(LayoutUtil.createHorzFillData(3));
 		commentCombo.addModifyListener(new ModifyListener() {
 			@Override
 			public void modifyText(ModifyEvent e) {
 				PhysicalRating rating = 
 						getRatingForComponent(currentSchemeComponent);
 				if (rating == null) {
 					return;
 				}
 				rating.setNote(commentCombo.getText());
 			}
 		});
 		formToolkit.adapt(commentCombo);
 		formToolkit.paintBordersFor(commentCombo);
 
 		initCommentAutoCompletion(commentCombo);
 		
 
 		/***************************************
 		 * Photo/image section
 		 ***************************************/
 		Composite imageComposite = new Composite(imageSection, SWT.NONE);
 		imageComposite.setBackgroundMode(SWT.INHERIT_DEFAULT);
 		formToolkit.adapt(imageComposite);
 		formToolkit.paintBordersFor(imageComposite);
 		gl = new GridLayout(2, false);
 		gl.marginWidth = 0;
 		imageComposite.setLayout(gl);
 		imageSection.setClient(imageComposite);
 
 		list = new List(imageComposite, SWT.BORDER);
 		list.setLayoutData(LayoutUtil.createFillData(1, 6));
 		formToolkit.adapt(list, true, true);
 
 		Button addPhotoButton = new Button(imageComposite, SWT.NONE);
 		formToolkit.adapt(addPhotoButton, true, true);
 		addPhotoButton.setText(I18N.tr("Add photo"));
 		addPhotoButton.setLayoutData(LayoutUtil.createRightGridData());
 		addPhotoButton.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				addPhoto();
 			}
 		});
 
 		Button removeImageButton = new Button(imageComposite, SWT.NONE);
 		formToolkit.adapt(removeImageButton, true, true);
 		removeImageButton.setText(I18N.tr("Remove"));
 		removeImageButton.setLayoutData(LayoutUtil.createRightGridData());
 		new Label(imageComposite, SWT.NONE);
 		new Label(imageComposite, SWT.NONE);
 		new Label(imageComposite, SWT.NONE);
 
 		/********************************
 		 * tags composite
 		 *******************************/
 		Composite tagsComposite = new Composite(tagSection, SWT.NONE);
 		gl = new GridLayout(2, true);
 		gl.marginHeight = 0;
 		gl.marginWidth = 0;
 		tagsComposite.setLayout(gl);
 		tagsComposite.setLayoutData(LayoutUtil.createHorzFillData(2));
 		tagsComposite.setBackground(getDisplay()
 				.getSystemColor(SWT.COLOR_WHITE));
 		tagSection.setClient(tagsComposite);
 
 		nothingRadioButton = new Button(tagsComposite, SWT.RADIO);
 		formToolkit.adapt(nothingRadioButton, true, true);
 		nothingRadioButton.setLayoutData(LayoutUtil.createHorzFillData());
 		nothingRadioButton.setText(I18N.tr("Nothing"));
 		nothingRadioButton.setSelection(true);
 		nothingRadioButton.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				samplePointType.set(SamplingPointType.none);
 			}
 		});
 
 		climateParameterRadioButton = new Button(tagsComposite, SWT.RADIO);
 		formToolkit.adapt(climateParameterRadioButton, true, true);
 		climateParameterRadioButton.setLayoutData(LayoutUtil
 				.createHorzFillData());
 		climateParameterRadioButton.setText(I18N.tr("Climate parameter"));
 		climateParameterRadioButton.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				samplePointType.set(SamplingPointType.climateParameter);
 			}
 		});
 
 		photoRadioButton = new Button(tagsComposite, SWT.RADIO);
 		formToolkit.adapt(photoRadioButton, true, true);
 		photoRadioButton.setLayoutData(LayoutUtil.createHorzFillData());
 		photoRadioButton.setText(I18N.tr("Photo"));
 		photoRadioButton.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				samplePointType.set(SamplingPointType.photo);
 			}
 		});
 
 		dustRadioButton = new Button(tagsComposite, SWT.RADIO);
 		formToolkit.adapt(dustRadioButton, true, true);
 		dustRadioButton.setLayoutData(LayoutUtil.createHorzFillData());
 		dustRadioButton.setText(I18N.tr("Dust concentration determination"));
 		dustRadioButton.addSelectionListener(new SelectionAdapter() {
 
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				samplePointType.set(SamplingPointType.dustConcentration);
 			}
 		});
 
 		sc.setMinSize(composite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
 	}
 
 	public void addGradeSelectionObserver(Observer<Integer> o) {
 		grade.addObserver(o);
 	}
 	
 	public void addSamplePointObserver(Observer<SamplingPointType> o) {
 		samplePointType.addObserver(o);
 	}
 
 	private void initCommentAutoCompletion(Combo combo) {
 
 		Collection<Comment> comments;
 		try {
 			comments = commentService.findAll();
 			String[] s = new String[comments.size()];
 			int i = 0;
 
 			for (Comment c : comments) {
 				s[i] = c.getText();
 				i++;
 			}
 			combo.setItems(s);
 			ContentProposalUtil.enableContentProposal(combo);
 		} catch (DatabaseException e) {
 			LOG.debug("An error occured", e);
 		}
 
 	}
 
 	private void addPhoto() {
 		// TODO request for one or more photos (wizard, dialog?)
 	}
 
 	@Override
 	public void inspectionChanged(Inspection inspection) {
 		if (inspection == null) {
 			return;
 		}
 		
 		this.inspection = inspection;
 		
 	    updateInspectionRatings();
 	}
 	
 	
     @Override
     public void inspectionComponentSelectionChanged(SchemeComponent component) {
     	if (component == null) {
     		return;
     	}
     	
     	currentSchemeComponent = component;
 
     	PhysicalRating rating = getRatingForComponent(component);
 
         if (rating != null) {
             updateRatingValues(rating);
         }
         else {
             gradeList.select(0);
             weightList.select(0);
             commentCombo.setText("");
         }
     }
 	
     private void updateRatingValues(PhysicalRating rating) {
         
         gradeList.select(rating.getRating());
         grade.set(gradeList.getSelectionIndex());
         weightList.select(rating.getQuantifier());
         if (rating.getNote() !=null) {
             commentCombo.setText(rating.getNote().get());
         }
     }
     
     private PhysicalRating getRatingForComponent(SchemeComponent component) {
         for (PhysicalRating rating : ratings) {
             if (rating.getComponent().equals(component)) {
                 return rating;
             }
         }
         PhysicalRating rating = new PhysicalRating(inspection, currentSchemeComponent);
         if (component.getComponent().getQuantifier().isPresent())  {
             rating.setQuantifier(component.getComponent().getQuantifier().get());
         }
         ratings.add(rating);
         return rating;
     }
 
     @Override
 	public void plantChanged(Plant plant) {
     	updateInspectionRatings();
     }
 	
 	private void updateInspectionRatings() {
 		try {
             ratings = inspectionService.findPhysicalRating(inspection);
         }
         catch (DatabaseException e) {
             LOG.error("An error occured");
         }
     }
 
 	@Override
 	protected void saveValues() {
 	    try {
	    	if (ratings != null && ratings.size() > 0) {
	    		inspectionService.insertPhysicalRatings(ratings);
	    	}
         }
         catch (SaveException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
         catch (DatabaseException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
 	}
 
 	@Override
 	public void dispose() {
 		formToolkit.dispose();
 		super.dispose();
 	}
 	
 	@Override
 	protected void checkSubclass() {
 		// Disable the check that prevents subclassing of SWT components
 	}
 
 }
