 /*
  * Created on Jan 21, 2006
  */
 package net.nansore.cedalion.figures;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import net.nansore.cedalion.eclipse.Activator;
 import net.nansore.cedalion.eclipse.TermContext;
 import net.nansore.cedalion.eclipse.TermVisualizationException;
 import net.nansore.cedalion.execution.ExecutionContext;
 import net.nansore.cedalion.execution.ExecutionContextException;
 import net.nansore.cedalion.execution.Notifier;
 import net.nansore.cedalion.execution.TermInstantiationException;
 import net.nansore.cedalion.execution.TermInstantiator;
 import net.nansore.prolog.Compound;
 import net.nansore.prolog.PrologException;
 import net.nansore.prolog.PrologProxy;
 import net.nansore.prolog.Variable;
 
 import org.eclipse.core.runtime.Status;
 import org.eclipse.draw2d.FlowLayout;
 import org.eclipse.draw2d.FocusBorder;
 import org.eclipse.draw2d.IFigure;
 import org.eclipse.draw2d.Label;
 import org.eclipse.draw2d.MouseEvent;
 import org.eclipse.draw2d.MouseListener;
 import org.eclipse.draw2d.Panel;
 import org.eclipse.draw2d.geometry.Point;
 import org.eclipse.jface.dialogs.ErrorDialog;
 import org.eclipse.jface.fieldassist.IContentProposal;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.FocusListener;
 import org.eclipse.swt.events.KeyEvent;
 import org.eclipse.swt.events.KeyListener;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.Font;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Menu;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.ui.IWorkbenchPart;
 
 /**
  * @author boaz
  */
 public class VisualTerm extends Panel implements TermFigure, TermContext, MouseListener, FocusListener, KeyListener {
 
 //    private static final String TEXT_CONTENT_FILENAME = ".tmpContent";
     private TermContext context;
     private IFigure contentFigure;
     private List<TermFigure> disposables = new ArrayList<TermFigure>();
 	private Object path;
 	private Compound descriptor;
 	private Compound projType;
 	private Runnable unreg;
 
     /**
      * @param term
      * @param context
      * @throws TermVisualizationException
      * @throws TermInstantiationException 
      */
     public VisualTerm(Compound term, TermContext parent) throws TermVisualizationException, TermInstantiationException {
     	context = parent;
         // The first argument is the descriptor, containing the path and additional information
    	descriptor = (Compound)term.arg(1); 
         path = ((Compound)descriptor.arg(1)).arg(1);
         if(term.arity() > 1) {
         	projType = (Compound)term.arg(2);
         } else {
         	projType = term.getProlog().createCompound("cpi#default");
         }
         
         try {
             // Set up the GUI
             setLayoutManager(new FlowLayout());
             setRequestFocusEnabled(true);
             // Create the child figures
             contentFigure = createContentFigure(descriptor);
             add(contentFigure);
             
             // Register this object with the content
             context.registerTermFigure(path, this);
             context.registerDispose(this);
             PrologProxy p = term.getProlog();
 			unreg = Notifier.instance().register(p.createCompound("::", path, p.createCompound("cpi#path")), new Runnable() {
 				
 				@Override
 				public void run() {
 					try {
 						updateFigure();
 					} catch (TermVisualizationException e) {
 						e.printStackTrace();
 					} catch (TermInstantiationException e) {
 						e.printStackTrace();
 					}
 				}
 			});
         } catch (TermVisualizationException e) {
             e.printStackTrace();
             Label label = new Label("<<<" + e.getMessage() + ">>>");
             label.setForegroundColor(new Color(context.getTextEditor().getDisplay(), 255, 0, 0));
             add(label);
         }
     }
 
     /**
      * @param content
      * @return
      * @throws TermVisualizationException
      * @throws TermInstantiationException 
      */
     private IFigure createContentFigure(Object path) throws TermVisualizationException, TermInstantiationException {
         // Query for the annotated term's visualization
 	    Variable vis = new Variable();
 	    PrologProxy prolog = Activator.getProlog();
 		Compound q = prolog.createCompound("cpi#visualizeDescriptor", path, projType, vis);
 	    // If successful, build the GUI
         try {
 			Map<Variable, Object> s = prolog.getSolution(q);
 		    return (IFigure) TermInstantiator.instance().instantiate((Compound)s.get(vis), this);
 		} catch (PrologException e) {
 			throw new TermVisualizationException(e);
 		}
     }
 
     /* (non-Javadoc)
      * @see net.nansore.visualterm.TermContext#getTextEditor()
      */
     public Text getTextEditor() {
         return context.getTextEditor();
     }
 
     /* (non-Javadoc)
      * @see net.nansore.visualterm.TermContext#bindFigure(net.nansore.visualterm.figures.TermFigure)
      */
     public void bindFigure(TermFigure figure) {
         figure.addMouseListener(this);
     }
 
     /* (non-Javadoc)
      * @see org.eclipse.draw2d.MouseListener#mousePressed(org.eclipse.draw2d.MouseEvent)
      */
     public void mousePressed(MouseEvent me) {
         if(canFocus()) {
             requestFocus();
             context.getTextEditor().setEnabled(true);
             context.getTextEditor().addFocusListener(this);
             context.getTextEditor().setFocus();
         } else {
             context.handleClick(me);
         }
         /////////// Test /////////////
         if(me.button == 3) {
             try {
 				createContextMenu(me);
 			} catch (TermInstantiationException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}                   
         }
     }
 
 	private void createContextMenu(MouseEvent me) throws TermInstantiationException {
 		System.out.println("Right button click");
 		Display display = context.getCanvas().getDisplay();
 		Menu menu = new Menu(context.getCanvas().getShell(), SWT.POP_UP);
 		try {
 			Variable varAction = new Variable("Action");
 			PrologProxy prolog = Activator.getProlog();
 			Iterator<Map<Variable, Object>> results = prolog.getSolutions(prolog.createCompound("cpi#contextMenuEntry", path, varAction));
 			while(results.hasNext()) {
 				Map<Variable, Object> result = (Map<Variable, Object>)results.next();
 				Compound action = (Compound)result.get(varAction);
 				TermInstantiator.instance().instantiate(action, menu, context);
 			}
 		} catch (PrologException e1) {
 			e1.printStackTrace();
 		}
 		Point absLocation = me.getLocation().getCopy();
 		translateToAbsolute(absLocation);
 		org.eclipse.swt.graphics.Point point = display.map(context.getCanvas(), null, new org.eclipse.swt.graphics.Point(absLocation.x, absLocation.y));
 		menu.setLocation(point);
 		menu.setVisible(true);
 		while (!menu.isDisposed() && menu.isVisible()) {
 		    if (!display.readAndDispatch())
 		        display.sleep();
 		}
 	}
 
     private boolean canFocus() {
     	return canModify();
     }
 
     /* (non-Javadoc)
      * @see org.eclipse.draw2d.MouseListener#mouseReleased(org.eclipse.draw2d.MouseEvent)
      */
     public void mouseReleased(MouseEvent me) {
     }
 
     /* (non-Javadoc)
      * @see org.eclipse.draw2d.MouseListener#mouseDoubleClicked(org.eclipse.draw2d.MouseEvent)
      */
     public void mouseDoubleClicked(MouseEvent me) {
         performDefaultAction();
     }
 
     /**
      * 
      */
     public void performDefaultAction() {
 /*        try {
             Variable varCmd = new Variable("Cmd");
             Map solution = PrologClient.getSolution(new Compound("vtbiDefaultAction", getResource(), content, type, varCmd));
             if(solution == null) {
                 context.performDefaultAction();
             } else {
                 Command cmd = CommandFactory.createCommand((Compound)solution.get(varCmd));
                 cmd.run(this);
                 figureUpdated();
             }
         } catch (PrologException e) {
             e.printStackTrace();
         } catch (CommandException e) {
             e.printStackTrace();
         }*/
     }
 
 
     private String termToText() throws IOException, PrologException, TermInstantiationException, ExecutionContextException {
     	PrologProxy prolog = Activator.getProlog();
 		ExecutionContext exe = new ExecutionContext(prolog);
     	return (String) exe.evaluate(prolog.createCompound("cpi#termAsString", path, prolog.createCompound("cpi#constExpr", 5)), new Variable());
     }
 
     public String getPackage() {
 		return context.getPackage();
 	}
 
 	/* (non-Javadoc)
      * @see net.nansore.visualterm.TermContext#focusChanged(net.nansore.visualterm.figures.TermFigure)
      */
     public void selectionChanged(TermFigure figure) {
         context.selectionChanged(figure);
     }
 
     /* (non-Javadoc)
      * @see net.nansore.visualterm.TermContext#registerTermFigure(long, net.nansore.visualterm.figures.TermFigure)
      */
     public void registerTermFigure(Object termID, TermFigure figure) {
         context.registerTermFigure(termID, figure);
     }
 
     /* (non-Javadoc)
      * @see net.nansore.visualterm.figures.TermFigure#updateFigure()
      */
     public void updateFigure() throws TermVisualizationException, TermInstantiationException {
 		disposeChildFigures();
 		if(contentFigure != null) {
 			contentFigure.erase();
 		    remove(contentFigure);			
 		}
         contentFigure = createContentFigure(descriptor);
         add(contentFigure);
         requestFocus();
         setFocus();
         context.figureUpdated();
     }
 
 	public void setFocus() {
 		context.setFocus();
 	}
 
     /* (non-Javadoc)
      * @see net.nansore.visualterm.TermContext#getColor()
      */
     public Color getColor() {
         return context.getColor();
     }
     
     public Font getFont(int fontType) {
         return context.getFont(fontType);
     }
 
     /* (non-Javadoc)
      * @see net.nansore.visualterm.TermContext#registerDispose(net.nansore.visualterm.Disposable)
      */
     public void registerDispose(TermFigure disp) {
         disposables.add(disp);
     }
 
     /* (non-Javadoc)
      * @see net.nansore.visualterm.Disposable#dispose()
      */
     public void dispose() {
 		unregisterTermFigure(path, this);
 		unreg.run();
         disposeChildFigures();
     }
 
 	private void disposeChildFigures() {
 		for(Iterator<TermFigure> i = disposables.iterator(); i.hasNext(); )
 		    i.next().dispose();
 		disposables.clear();
 	}
 
     /**
      * 
      */
     public void figureUpdated() {
         context.figureUpdated();
     }
 
     /* (non-Javadoc)
      * @see net.nansore.visualterm.TermContext#unregisterTermFigure(int, net.nansore.visualterm.figures.TermFigure)
      */
     public void unregisterTermFigure(Object termID, TermFigure figure) {
         context.unregisterTermFigure(termID, figure);
     }
 
 	public String getResource() {
 		return context.getResource();
 	}
 
     public void focusGained(org.eclipse.swt.events.FocusEvent arg0) {
         setBorder(new FocusBorder());
         context.selectionChanged(this);
         if(canModify()) {
             try {
                 String text = termToText();
 				context.getTextEditor().setText(text);
                 context.getTextEditor().setEnabled(true);
                 context.getTextEditor().setSelection(0, text.length());
                 context.getTextEditor().addKeyListener(this);
             } catch (IOException e) {
                 e.printStackTrace();
             } catch (PrologException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (TermInstantiationException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (ExecutionContextException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
         }
     }
 
     private boolean canModify() {
         try {
         	PrologProxy prolog = Activator.getProlog();
 			ExecutionContext exe = new ExecutionContext(prolog);
         	return exe.isProcDefined(prolog.createCompound("cpi#edit", path, new Variable(), new Variable()));
         } catch (PrologException e) {
             e.printStackTrace();
             return false;
         }
     }
 
     public void focusLost(org.eclipse.swt.events.FocusEvent arg0) {
         setBorder(null);
         context.getTextEditor().setText("");
         context.getTextEditor().setEnabled(false);
         context.getTextEditor().removeFocusListener(this);
         context.getTextEditor().removeKeyListener(this);
     }
 
     public void keyPressed(KeyEvent event) {
         if(event.keyCode == 13 && event.stateMask == 0) {
             try {
                 setContentFromString(context.getTextEditor().getText());
             } catch (TermVisualizationException e) {
                 ErrorDialog.openError(context.getTextEditor().getShell(), "Error Updating Element", "The following error has occured: " + e.getMessage(), Status.OK_STATUS);
             } catch (PrologException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (TermInstantiationException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (ExecutionContextException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
         }
     }
 
     private void setContentFromString(String text) throws TermVisualizationException, PrologException, TermInstantiationException, ExecutionContextException {
     	PrologProxy prolog = Activator.getProlog();
 		ExecutionContext exe = new ExecutionContext(prolog);
 		exe.runProcedure(prolog.createCompound("cpi#editFromString", path, prolog.createCompound("cpi#constExpr", text)));
         figureUpdated();
     }
 
     public void keyReleased(KeyEvent arg0) {
     }
 
     public void handleClick(MouseEvent me) {
         mousePressed(me);
     }
 
     public Control getCanvas() {
         return context.getCanvas();
     }
 
 	public IWorkbenchPart getWorkbenchPart() {
 		return context.getWorkbenchPart();
 	}
 
 	public IContentProposal[] getProposals(String substring, int pos) {
 	    List<IContentProposal> proposals = new ArrayList<IContentProposal>();
 	    try {
 			Variable varCompletion = new Variable();
 			PrologProxy prolog = Activator.getProlog();
 			Iterator<Map<Variable, Object>> solutions = prolog.getSolutions(prolog.createCompound("cpi#autocomplete", descriptor, substring, varCompletion));
 			while(solutions.hasNext()) {
 				Map<Variable, Object> solution = solutions.next();
 				final String completion = (String)solution.get(varCompletion);
 				proposals.add(new IContentProposal() {
 	
 					public String getContent() {
 						return completion;
 					}
 	
 					public int getCursorPosition() {
 						int pos;
 						for(pos = 0; pos < completion.length(); pos++) {
 							if(completion.charAt(pos) == '(')
 								return pos + 1;
 						}
 						return pos;
 					}
 	
 					public String getDescription() {
 						return null;
 					}
 	
 					public String getLabel() {
 						return completion;
 					}});
 			}
 			
 		} catch (PrologException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return proposals.toArray(new IContentProposal[] {}); 
 	}
 
 	private String toLocalString(String string) {
 		String name = string;
 		String args = "";
 		if(string.contains("(")) {
 			name = string.substring(0, string.indexOf("("));
 			args = string.substring(string.indexOf("("));
 		}
 		if(name.contains("#")) {
 			name = "'" + name.substring(0, name.indexOf("#")) + "':'" + name.substring(name.indexOf("#") + 1) + "'";
 		}
 		return name + args;
 	}
 }
