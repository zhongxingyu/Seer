 package org.iucn.sis.client.api.caches;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.iucn.sis.client.api.caches.AssessmentCache.FetchMode;
 import org.iucn.sis.client.api.container.SISClientBase;
 import org.iucn.sis.client.api.container.StateManager;
 import org.iucn.sis.client.api.utils.UriBase;
 import org.iucn.sis.shared.api.acl.base.AuthorizableObject;
 import org.iucn.sis.shared.api.assessments.AssessmentFetchRequest;
 import org.iucn.sis.shared.api.data.WorkingSetParser;
 import org.iucn.sis.shared.api.debug.Debug;
 import org.iucn.sis.shared.api.models.Assessment;
 import org.iucn.sis.shared.api.models.AssessmentFilter;
 import org.iucn.sis.shared.api.models.Taxon;
 import org.iucn.sis.shared.api.models.WorkingSet;
 
 import com.extjs.gxt.ui.client.event.ButtonEvent;
 import com.extjs.gxt.ui.client.event.SelectionListener;
 import com.extjs.gxt.ui.client.widget.Html;
 import com.extjs.gxt.ui.client.widget.Window;
 import com.extjs.gxt.ui.client.widget.button.Button;
 import com.solertium.lwxml.shared.GenericCallback;
 import com.solertium.lwxml.shared.NativeDocument;
 import com.solertium.lwxml.shared.NativeElement;
 import com.solertium.lwxml.shared.NativeNodeList;
 import com.solertium.util.events.SimpleListener;
 import com.solertium.util.extjs.client.WindowUtils;
 import com.solertium.util.portable.PortableAlphanumericComparator;
 
 /**
  * Class that represents the working set cache. Main member is the static
  * representation of the class, to be used to represent the current working set.
  * 
  * @author liz.schwartz
  * 
  */
 public class WorkingSetCache {
 
 	public static class WorkingSetComparator extends PortableAlphanumericComparator {
 		private static final long serialVersionUID = 1L;
 		public WorkingSetComparator() {
 			super();
 		}
 
 		@Override
 		public int compare(Object ol, Object or) {
 			WorkingSet ws1 = (WorkingSet) ol;
 			WorkingSet ws2 = (WorkingSet) or;
 
 			return super.compare(ws1.getName(), ws2.getName());
 		}
 	}
 
 	public static final WorkingSetCache impl = new WorkingSetCache();
 	
 	private Map<Integer, WorkingSet> workingSets;
 	private Map<Integer, Map<Integer, List<Integer>>> assessmentRelations;
 
 	private WorkingSetCache() {
 		workingSets = new HashMap<Integer, WorkingSet>();
 		assessmentRelations = new HashMap<Integer, Map<Integer,List<Integer>>>();
 	}
 
 	/**
 	 * adds the given working set data object to the public working set pool.
 	 * Assumes that the id of the working set is either null or "", will send to
 	 * edit if the id matches another working set, and fails otherwise.
 	 * 
 	 * @param ws
 	 * @param wayBack
 	 */
 	public void createWorkingSet(final WorkingSet ws, final GenericCallback<String> wayBack) {
 
 		if (ws.getId() == 0) {
 
 			final NativeDocument ndoc = SISClientBase.getHttpBasicNativeDocument();
 			ndoc.putAsText(UriBase.getInstance() + "/workingSet/public/" + SISClientBase.currentUser.getUsername(), ws
 					.toXML(), new GenericCallback<String>() {
 				public void onFailure(Throwable caught) {
 					wayBack.onFailure(caught);
 				}
 
 				public void onSuccess(String arg0) {
 					ws.setId(Integer.valueOf(ndoc.getText()));
 					addToWorkingSetCache(ws);
 					wayBack.onSuccess(arg0);
 				}
 			});
 		} else
 			wayBack.onFailure(new Throwable("Don't know what to do because user dosen't have rights to specify"
 					+ " the working set id."));
 	}
 
 	private void addToWorkingSetCache(Collection<WorkingSet> ws) {
 		for (WorkingSet curWS : ws)
 			addToWorkingSetCache(curWS);
 	}
 
 	private void addToWorkingSetCache(WorkingSet ws) {
 		workingSets.put(ws.getId(), ws);
 	}
 
 	public void createTaxaList(Integer id, final GenericCallback<String> wayback) {
 		if (getWorkingSet(id) != null) {
 			final NativeDocument ndoc = SISClientBase.getHttpBasicNativeDocument();
 			ndoc.getAsText(UriBase.getInstance().getSISBase() + "/workingSet/taxaList/"
 					+ SISClientBase.currentUser.getUsername() + "/" + id, new GenericCallback<String>() {
 
 				public void onFailure(Throwable caught) {
 					// TODO Auto-generated method stub
 					wayback.onFailure(caught);
 				}
 
 				public void onSuccess(String arg0) {
 					// NDOC.TEXT SHOULD CONTAIN URL TO LET THE USER
 					// DOWNLOAD THE FILE
 					wayback.onSuccess(ndoc.getText());
 				}
 
 			});
 		} else
 			wayback.onFailure(new Throwable("Not a valid working set"));
 	}
 
 	public void createTaxaListWithCats(Integer id, AssessmentFilter filter, final GenericCallback<String> wayback) {
 		if (getWorkingSet(id) != null) {
 			final NativeDocument ndoc = SISClientBase.getHttpBasicNativeDocument();
 			ndoc.postAsText(UriBase.getInstance().getSISBase() + "/workingSet/taxaList/"
 					+ SISClientBase.currentUser.getUsername() + "/" + id, "<root>" + filter.toXML() + "</root>",
 					new GenericCallback<String>() {
 
 						public void onFailure(Throwable caught) {
 							// TODO Auto-generated method stub
 							wayback.onFailure(caught);
 						}
 
 						public void onSuccess(String arg0) {
 							// NDOC.TEXT SHOULD CONTAIN URL TO LET THE USER
 							// DOWNLOAD THE FILE
 							wayback.onSuccess(ndoc.getText());
 						}
 
 					});
 		} else
 			wayback.onFailure(new Throwable("Not a valid working set"));
 	}
 
 	public void unsubscribeToWorkingSet(final WorkingSet ws, final GenericCallback<String> wayBack) {
 		NativeDocument ndoc = SISClientBase.getHttpBasicNativeDocument();
 		ndoc.delete(UriBase.getInstance().getSISBase() + "/workingSet/unsubscribe/"
 				+ SISClientBase.currentUser.getUsername() + "/" + ws.getId(), new GenericCallback<String>() {
 			public void onFailure(Throwable caught) {
 				wayBack.onFailure(caught);
 			}
 
 			public void onSuccess(String arg0) {
 				workingSets.remove(ws.getId());
 				uncacheAssessmentsForWorkingSet(ws);
 				wayBack.onSuccess(ws.getName());
 			}
 		});
 
 	}
 
 	public void deleteWorkingSet(final WorkingSet ws, final GenericCallback<String> wayBack) {
 		NativeDocument ndoc = SISClientBase.getHttpBasicNativeDocument();
 		ndoc.delete(UriBase.getInstance().getSISBase() + "/workingSet/public/"
 				+ SISClientBase.currentUser.getUsername() + "/" + ws.getId(), new GenericCallback<String>() {
 			public void onFailure(Throwable caught) {
 				wayBack.onFailure(caught);
 			}
 
 			public void onSuccess(String arg0) {
 				workingSets.remove(ws.getId());
 				uncacheAssessmentsForWorkingSet(ws);
				wayBack.onSuccess(ws.getName());
 			}
 		});
 
 	}
 
 	public void doLogout() {
 		workingSets.clear();
 	}
 
 	public void editWorkingSet(final WorkingSet ws, final GenericCallback<String> wayback) {
 		if (workingSets.containsKey(ws.getId())) {
 			NativeDocument ndoc = SISClientBase.getHttpBasicNativeDocument();
 			ndoc.post(UriBase.getInstance().getSISBase() + "/workingSet/public/"
 					+ SISClientBase.currentUser.getUsername() + "/" + ws.getId(), ws.toXML(), new GenericCallback<String>() {
 				public void onFailure(Throwable caught) {
 					wayback.onFailure(caught);
 				}
 
 				public void onSuccess(String arg0) {
 					workingSets.put(ws.getId(), ws);
 					uncacheAssessmentsForWorkingSet(ws);
 					wayback.onSuccess(arg0);
 				}
 			});
 
 		} else {
 			wayback.onFailure(new Throwable("Not currently in the workingSet"));
 		}
 	}
 
 	public void editTaxaInWorkingSet(final WorkingSet ws, final Collection<Taxon> taxonToAdd,
 			final Collection<Taxon> taxonToRemove, final GenericCallback<String> callback) {
 		NativeDocument ndoc = SISClientBase.getHttpBasicNativeDocument();
 		StringBuilder posting = new StringBuilder("<taxa>");
 		if (taxonToAdd != null) {
 			for (Taxon taxon : taxonToAdd)
 				posting.append("<add>" + taxon.getId() + "</add>");
 		}
 		if (taxonToRemove != null) {
 			for (Taxon taxon : taxonToRemove)
 				posting.append("<remove>" + taxon.getId() + "</remove>");
 		}
 		posting.append("</taxa>");
 		ndoc.post(UriBase.getInstance().getSISBase() + "/workingSet/editTaxa/"
 				+ SISClientBase.currentUser.getUsername() + "/" + ws.getId(), posting.toString(),
 				new GenericCallback<String>() {
 
 					@Override
 					public void onSuccess(String result) {
 						if (taxonToRemove != null)
 							ws.getTaxon().removeAll(taxonToRemove);
 						if (taxonToAdd != null)
 							ws.getTaxon().addAll(taxonToAdd);
 						callback.onSuccess(result);
 					}
 
 					@Override
 					public void onFailure(Throwable caught) {
 						callback.onFailure(caught);
 
 					}
 				});
 
 	}
 
 	public void exportWorkingSet(final Integer workingSetID, final boolean lock, final GenericCallback<String> wayBack) {
 		if (workingSets.containsKey(workingSetID)) {
 			String mode = "public";
 
 			if (workingSets.containsKey(workingSetID)) {
 				final NativeDocument ndoc = SISClientBase.getHttpBasicNativeDocument();
 				ndoc.getAsText(UriBase.getInstance().getSISBase() + "/workingSetExporter/" + mode + "/"
 						+ SISClientBase.currentUser.getUsername() + "/" + workingSetID + "?lock=" + lock,
 						new GenericCallback<String>() {
 
 							public void onFailure(Throwable caught) {
 								WindowUtils.errorAlert("Server error while exporting working set."
 										+ "<br>Error as follows: " + ndoc.getText());
 								wayBack.onFailure(caught);
 							}
 
 							public void onSuccess(String arg0) {
 								String body = ndoc.getText();
 								int firstLineBreak = body.indexOf("\r\n", 0);
 								final String path = body.substring(0, firstLineBreak);
 
 								if (lock && body.contains("<div>")) {
 									final Window w = WindowUtils.newWindow("Assessments Locked", null, false, true);
 									w.getButtonBar().add(new Button("Close", new SelectionListener<ButtonEvent>() {
 										public void componentSelected(ButtonEvent ce) {
 											wayBack.onSuccess(path);
 											w.hide();
 										};
 									}));
 									w.add(new Html(body.substring(firstLineBreak)));
 									w.show();
 									w.setSize(500, 400);
 									w.center();
 								} else
 									wayBack.onSuccess(path);
 							}
 						});
 			} else {
 				wayBack.onFailure(new Throwable("Not a valid working set."));
 			}
 
 		} else
 			wayBack.onFailure(new Throwable("oh no, how did you get here, "
 					+ "the working set is not in your working sets"));
 	}
 
 	public void fetchTaxaForWorkingSet(final WorkingSet ws, final GenericCallback<List<Taxon>> wayback) {
 		TaxonomyCache.impl.fetchList(ws.getSpeciesIDs(), new GenericCallback<String>() {
 			public void onFailure(Throwable caught) {
 				wayback.onFailure(caught);
 			}
 
 			public void onSuccess(String arg0) {
 				List<Integer> toRemove = new ArrayList<Integer>();
 				List<Taxon> result = new ArrayList<Taxon>();
 				for (Integer cur : ws.getSpeciesIDs()) {
 					if (!TaxonomyCache.impl.contains(cur))
 						toRemove.add(cur);
 					else
 						result.add(TaxonomyCache.impl.getTaxon(cur));
 				}
 
 				if (toRemove.size() > 0)
 					ws.getSpeciesIDs().removeAll(toRemove);
 
 				wayback.onSuccess(result);
 			}
 		});
 	}
 	
 	public void uncacheAssessmentsForWorkingSet(final WorkingSet ws) {
 		assessmentRelations.remove(ws.getId());
 	}
 	
 	/**
 	 * Get all assessments for a working set for the given taxon 
 	 * (or all assessments if taxon is null), THEN pre-fetch and 
 	 * cache all resulting assessments, and return the cached 
 	 * assessment objects.
 	 * 
 	 * @param ws
 	 * @param taxon
 	 * @param callback
 	 */
 	public void getAssessmentsForWorkingSet(final WorkingSet ws, final Taxon taxon, final GenericCallback<List<Assessment>> callback) {
 		final SimpleListener handler = new SimpleListener() {
 			public void handleEvent() {
 				Map<Integer, List<Integer>> assessmentsForTaxa = assessmentRelations.get(ws.getId());
 				
 				final AssessmentFetchRequest request = new AssessmentFetchRequest();
 				if (taxon == null) 
 					for (List<Integer> id : assessmentsForTaxa.values())
 						request.addAssessments(id);
 				else if (assessmentsForTaxa.get(taxon.getId()) != null)
 					request.addAssessments(assessmentsForTaxa.get(taxon.getId()));
 				
 				final Collection<Integer> assessments = new ArrayList<Integer>(request.getAssessmentUIDs());
 				
 				if (assessments.isEmpty())
 					callback.onSuccess(new ArrayList<Assessment>());
 				else
 					AssessmentCache.impl.fetchAssessments(assessments, FetchMode.PARTIAL, new GenericCallback<Collection<Assessment>>() {
 						public void onSuccess(Collection<Assessment> result) {
 							List<Assessment> list = new ArrayList<Assessment>(result);
 							/*for (Integer id : assessments)
 								list.add(AssessmentCache.impl.getAssessment(id));*/
 							
 							callback.onSuccess(list);
 						}
 						public void onFailure(Throwable caught) {
 							callback.onFailure(caught);
 						}
 					});
 			}
 		};
 		
 		fetchAssessmentsForWorkingSet(ws, taxon, handler);
 	}
 	
 	/**
 	 * List the IDs of the assessments in this working set for the given 
 	 * taxon (or null for all assessments).  Does not actually fetch or 
 	 * cache these items, leaving that exercise to the caller.
 	 * @param ws
 	 * @param taxon
 	 * @param callback
 	 */
 	public void listAssessmentsForWorkingSet(final WorkingSet ws, final Taxon taxon, final GenericCallback<List<Integer>> callback) {
 		final SimpleListener handler = new SimpleListener() {
 			public void handleEvent() {
 				Map<Integer, List<Integer>> assessmentsForTaxa = assessmentRelations.get(ws.getId());
 				
 				Set<Integer> set = new HashSet<Integer>();
 				if (taxon == null) 
 					for (List<Integer> id : assessmentsForTaxa.values())
 						set.addAll(id);
 				else if (assessmentsForTaxa.get(taxon.getId()) != null)
 					set.addAll(assessmentsForTaxa.get(taxon.getId()));
 				
 				callback.onSuccess(new ArrayList<Integer>(set));
 			}
 		};
 		
 		fetchAssessmentsForWorkingSet(ws, taxon, handler);
 	}
 	
 	private void fetchAssessmentsForWorkingSet(final WorkingSet ws, final Taxon taxon, final SimpleListener handler) {
 		if (assessmentRelations.get(ws.getId()) != null) {
 			handler.handleEvent();
 		}
 		else {
 			final NativeDocument document = SISClientBase.getHttpBasicNativeDocument();
 			document.get(UriBase.getInstance().getSISBase() + "/workingSet/assessments/" + 
 					SISClientBase.currentUser.getUsername() + "/" + 
 					ws.getId(), new GenericCallback<String>() {
 				public void onSuccess(String result) {
 					Map<Integer, List<Integer>> mapping = new HashMap<Integer, List<Integer>>();
 					
 					final NativeNodeList nodes = document.getDocumentElement().getElementsByTagName("assessment");
 					for (int i = 0; i < nodes.getLength(); i++) {
 						NativeElement node = nodes.elementAt(i);
 						Integer assessmentID = Integer.valueOf(node.getAttribute("id"));
 						Integer taxonID = Integer.valueOf(node.getAttribute("taxon"));
 						
 						List<Integer> l = mapping.get(taxonID);
 						if (l == null) {
 							l = new ArrayList<Integer>();
 							mapping.put(taxonID, l);
 						}
 						
 						l.add(assessmentID);
 					}
 					
 					assessmentRelations.put(ws.getId(), mapping);
 					
 					handler.handleEvent();
 				}
 				public void onFailure(Throwable caught) {
 					WindowUtils.errorAlert("Error loading assessments.");
 				}
 			});
 		}
 	}
 
 	/**
 	 * gets all public working sets that the user can subscribe to
 	 * 
 	 * @param wayBack
 	 */
 	public void getAllSubscribableWorkingSets(final GenericCallback<List<WorkingSet>> wayBack) {
 		final NativeDocument ndoc = SISClientBase.getHttpBasicNativeDocument();
 		ndoc.get(UriBase.getInstance().getSISBase() + "/workingSet/subscribe/"
 				+ SISClientBase.currentUser.getUsername(), new GenericCallback<String>() {
 			public void onFailure(Throwable caught) {
 				wayBack.onFailure(caught);
 			}
 			public void onSuccess(String arg0) {
 				List<WorkingSet> subscribableWorkingSets = new ArrayList<WorkingSet>();
 				NativeNodeList list = ndoc.getDocumentElement().getElementsByTagName(WorkingSet.ROOT_TAG);
 				for (int i = 0; i < list.getLength(); i++) {
 					NativeElement element = list.elementAt(i);
 					WorkingSet ws = WorkingSet.fromXMLMinimal(element);
 
 					//TODO: server should not even return working sets that you can't read...
 					if (AuthorizationCache.impl.hasRight(SISClientBase.currentUser, AuthorizableObject.READ, ws)) {
 						subscribableWorkingSets.add(ws);
 					}
 				}
 				wayBack.onSuccess(subscribableWorkingSets);
 			}
 		});
 	}
 
 	public int getNumberOfWorkingSets() {
 		return workingSets.size();
 	}
 
 	/**
 	 * given the working set id, returns the working set associated with it,
 	 * null if it doesn't exist
 	 * 
 	 * @param id
 	 */
 	public WorkingSet getWorkingSet(Integer id) {
 		return workingSets.get(id);
 	}
 
 	public Map<Integer, WorkingSet> getWorkingSets() {
 		return workingSets;
 	}
 	
 	public WorkingSet getCurrentWorkingSet() {
 		return StateManager.impl.getWorkingSet();
 	}
 
 	/**
 	 * Resetting the current working set will presume that 
 	 * property data integrity measures have already been 
 	 * taken.
 	 */
 	/*public void resetCurrentWorkingSet() {
 		setCurrentWorkingSet((WorkingSet) null, false);
 	}
 
 	public void setCurrentWorkingSet(Integer id) {
 		setCurrentWorkingSet(id, true);
 	}
 	
 	public void setCurrentWorkingSet(Integer id, boolean saveIfNecessary) {
 		setCurrentWorkingSet(id, saveIfNecessary, null);
 	}
 	
 	public void setCurrentWorkingSet(Integer id, boolean saveIfNecessary, SimpleListener afterChange) {
 		setCurrentWorkingSet(workingSets.get(id), saveIfNecessary, afterChange);
 	}
 	
 	public void setCurrentWorkingSet(final WorkingSet ws, boolean saveIfNecessary) {
 		setCurrentWorkingSet(ws, saveIfNecessary, null);
 	}
 
 	public void setCurrentWorkingSet(final WorkingSet ws, boolean saveIfNecessary, final SimpleListener afterChange) {
 		WorkingSet currentWorkingSet = StateManager.impl.getWorkingSet();
 		
 		boolean changeNeeded = ((currentWorkingSet == null && ws != null) || 
 				(currentWorkingSet != null && ws == null) || 
 				(currentWorkingSet != null && ws != null && !currentWorkingSet.equals(ws)));
 		//Debug.println("Set current ws; Changed needed? {0}: cur {1} & new {2}", changeNeeded, currentWorkingSet, ws);
 		if (changeNeeded) {
 			final SimpleListener callback = new SimpleListener() {
 				public void handleEvent() {
 					Debug.println("Setting {0} as current", ws);
 					StateManager.impl.setWorkingSet(ws);
 					
 					SISClientBase.getInstance().onWorkingSetChanged();
 					if (afterChange != null)
 						afterChange.handleEvent();
 				}
 			};
 			if (saveIfNecessary)
 				AssessmentClientSaveUtils.saveIfNecessary(callback);
 			else
 				callback.handleEvent();
 		}
 	}*/
 
 	/**
 	 * given the workingSetID, adds the id to your list of working sets that you
 	 * are subscribed to
 	 * 
 	 * @param workingSetID
 	 * @param wayBack
 	 */
 	public void subscribeToWorkingSet(final String workingSetID, final GenericCallback<String> wayBack) {
 		final NativeDocument ndoc = SISClientBase.getHttpBasicNativeDocument();
 		ndoc.put(UriBase.getInstance().getSISBase() + "/workingSet/subscribe/"
 				+ SISClientBase.currentUser.getUsername() + "/" + workingSetID, "", new GenericCallback<String>() {
 
 			public void onFailure(Throwable caught) {
 				wayBack.onFailure(caught);
 			}
 
 			public void onSuccess(String arg0) {
 				update(wayBack);
 			}
 		});
 	}
 
 	public void update(final GenericCallback<String> backInTheDay) {
 		final NativeDocument ndoc = SISClientBase.getHttpBasicNativeDocument();
 		ndoc.get(UriBase.getInstance().getSISBase() + "/workingSet/list/" + SISClientBase.currentUser.getUsername(),
 				new GenericCallback<String>() {
 			public void onFailure(Throwable caught) {
 				backInTheDay.onFailure(caught);
 			}
 			public void onSuccess(String arg0) {
 				workingSets.clear();
 				assessmentRelations.clear();
 				try {
 					WorkingSetParser parser = new WorkingSetParser();
 					parser.parseWorkingSetXML(ndoc);
 					
 					addToWorkingSetCache(parser.getWorkingSets());
 					
 					backInTheDay.onSuccess(arg0);
 				} catch (Exception e) {
 					Debug.println("Working Set Parse Failure\n{0}", e);
 				}
 			}
 		});
 	}
 
 }
