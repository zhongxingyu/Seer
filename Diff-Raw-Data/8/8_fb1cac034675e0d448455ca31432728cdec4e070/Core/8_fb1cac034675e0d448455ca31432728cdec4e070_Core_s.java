 /*
  * ELW : e-learning workspace
  * Copyright (C) 2010  Anton Kraievoy
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package elw.web.core;
 
 import elw.dao.*;
 import elw.vo.*;
 import elw.vo.Class;
 import elw.web.ElwUri;
 import elw.web.VelocityTemplates;
 import elw.web.VtTuple;
 
 import java.util.*;
 
 public class Core {
 	protected final CourseDao courseDao;
 	protected final GroupDao groupDao;
 	protected final EnrollDao enrollDao;
 	protected final ScoreDao scoreDao;
 	protected final FileDao fileDao;
 
 	protected final VelocityTemplates vt = VelocityTemplates.INSTANCE;
 	protected final ElwUri uri = new ElwUri();
 	protected static final String CTX_TO_SCORE_TOTAL = "--total";
 
 	public Core(CourseDao courseDao, EnrollDao enrollDao, FileDao fileDao, GroupDao groupDao, ScoreDao scoreDao) {
 		this.courseDao = courseDao;
 		this.enrollDao = enrollDao;
 		this.fileDao = fileDao;
 		this.groupDao = groupDao;
 		this.scoreDao = scoreDao;
 	}
 
 	public VelocityTemplates getTemplates() {
 		return vt;
 	}
 
 	public ElwUri getUri() {
 		return uri;
 	}
 
 	public FileDao getFileDao() {
 		return fileDao;
 	}
 
 	public List<Object[]> log(
 			Ctx ctx, Format format, LogFilter logFilter, final boolean adm
 	) {
 		final List<Object[]> logData = new ArrayList<Object[]>();
 
 		if ("s".equalsIgnoreCase(logFilter.getScopePath()[0])) {
 			logStud(ctx, format, logFilter, logData, adm);
 		} else if ("c".equalsIgnoreCase(logFilter.getScopePath()[0])) {
 			logCourse(ctx, format, logFilter, logData, adm);
 		}
 
 		return logData;
 	}
 
 	protected void logCourse(
 			Ctx ctx, Format format, LogFilter lf,
 			List<Object[]> logData, final boolean adm
 	) {
 		for (int i = 0; i < ctx.getEnr().getIndex().size(); i++) {
 			final Ctx ctxAss = ctx.extendIndex(i);
 
 			final AssignmentType aType = ctxAss.getAssType();
 			final FileSlot[] slots = aType.getFileSlots();
 			for (FileSlot slot : slots) {
 				if (W.excluded(lf.getSlotId(), aType.getId(), slot.getId())) {
 					continue;
 				}
 				if (!lf.cDue(ctxAss, slot)) {
 					continue;
 				}
 				if (!adm && !ctxAss.cFrom().isStarted()) {
 					continue;
 				}
 
 				final Entry<FileMeta>[] uploadsAss = fileDao.findFilesFor(FileDao.SCOPE_ASS, ctxAss, slot.getId());
 				int total = uploadsAss.length;
 				if (lf.cScopeOne('a') && lf.cVer(ctxAss)) {
 					logRows(format, lf, logData, i, ctxAss, slot, uploadsAss, FileDao.SCOPE_ASS, adm);
 				}
 
 				final Version[] versions = adm ? ctxAss.getAss().getVersions() : new Version[] {ctxAss.getVer()};
 				for (Version ver : versions) {
 					final Ctx ctxVer = ctxAss.extendVer(ver);
 					final Entry<FileMeta>[] uploadsVer = fileDao.findFilesFor(FileDao.SCOPE_VER, ctxVer, slot.getId());
 					total += uploadsVer.length;
 					if (lf.cScopeOne('v') && lf.cVer(ctxVer)) {
 						logRows(format, lf, logData, i, ctxVer, slot, uploadsVer, FileDao.SCOPE_VER, adm);
 					}
 				}
 
 				if (lf.cScopeOpen() && lf.cVer(ctxAss) && total == 0) {
 					logData.add(logRow(format, lf.getMode(), logData, i, ctxAss, slot, null, FileDao.SCOPE_ASS, adm));
 				}
 			}
 		}
 	}
 
 	protected void logStud(Ctx ctx, Format f, LogFilter lf, List<Object[]> logData, boolean adm) {
 		if (adm) {
 			for (Student stud : ctx.getGroup().getStudents()) {
 				if (W.excluded(lf.getStudId(), stud.getId())) {
 					continue;
 				}
 
 				final Ctx ctxStud = ctx.extendStudent(stud);
 				logStudForStud(ctx, f, lf, logData, ctxStud, adm);
 			}
 		} else {
 			if (ctx.getStudent() != null) {
 				logStudForStud(ctx, f, lf, logData, ctx, adm);
 			}
 		}
 	}
 
 	protected void logStudForStud(
 			Ctx ctx, Format f,
 			LogFilter lf, List<Object[]> logData, Ctx ctxStud,
 			boolean adm) {
 		for (int index = 0; index < ctx.getEnr().getIndex().size(); index++) {
 			final Ctx ctxVer = ctxStud.extendIndex(index);
 			if (!adm && !ctxVer.cFrom().isStarted()) {
 				continue;
 			}
 			if (!lf.cVer(ctxVer)) {
 				continue;
 			}
 			final AssignmentType aType = ctxVer.getAssType();
 			final FileSlot[] slots = aType.getFileSlots();
 
 			for (FileSlot slot : slots) {
 				if (W.excluded(lf.getSlotId(), aType.getId(), slot.getId())) {
 					continue;
 				}
 
 				final Entry<FileMeta>[] uploads = fileDao.findFilesFor(FileDao.SCOPE_STUD, ctxVer, slot.getId());
 				if (!lf.cDue(ctxVer, slot)) {
 					continue;
 				}
 
 				logRows(f, lf, logData, index, ctxVer, slot, uploads, FileDao.SCOPE_STUD, adm);
 				if (uploads.length == 0 && lf.cScopeStud(slot, null)) {
 					logData.add(logRow(f, lf.getMode(), logData, index, ctxVer, slot, null, FileDao.SCOPE_STUD, adm));
 				}
 			}
 		}
 	}
 
 	protected int logRows(Format format, LogFilter logFilter, List<Object[]> logData, int index, Ctx ctxVer, FileSlot slot, Entry<FileMeta>[] uploads, String scope, boolean adm) {
 		int shown = 0;
 		for (int i = 0, uploadsLength = uploads.length; i < uploadsLength; i++) {
 			final boolean last = i + 1 == uploadsLength;
 			if (!logFilter.cLatest(last)) {
 				continue;
 			}
 			final Entry<FileMeta> e = uploads[i];
 			if (FileDao.SCOPE_STUD.equals(scope) && !logFilter.cScopeStud(slot, e)) {
 				continue;
 			}
 			shown += 1;
 			logData.add(logRow(format, logFilter.getMode(), logData, index, ctxVer, slot, e, scope, adm));
 		}
 		return shown;
 	}
 
 	protected Object[] logRow(
 			Format f, final String mode, List<Object[]> data,
 			int index, Ctx ctx, FileSlot slot, Entry<FileMeta> e, String scope,
 			boolean adm) {
 		final long time = e == null ? System.currentTimeMillis() : e.getMeta().getCreateStamp().getTime();
 
 		final IndexEntry iEntry = ctx.getIndexEntry();
 		final String nameNorm;
 		if (e == null) {
 			nameNorm = "";
 		} else {
 			if (FileDao.SCOPE_STUD.equalsIgnoreCase(scope)) {
 				nameNorm = iEntry.normName(
 						ctx.getEnr(), ctx.getStudent(), ctx.getAss(),
 						ctx.getVer(), slot, e.getMeta(), f
 				);
 			} else {
 				nameNorm = e.getMeta().getName();
 			}
 		}
 		final StringBuilder q = new StringBuilder(
 				"?elw_ctx=" + ctx.toString() + "&s=" + scope + "&sId=" + slot.getId()
 		);
 		if (e != null) {
 			q.append("&fId=").append(e.getMeta().getId());
 		}
 
 		final String dlRef;
 		if (e != null) {
 			dlRef = (adm ? "../s/" : "") + "dl/" + nameNorm + q;
 		} else {
 			dlRef = null;
 		}
 
 		//	FIXME proper UL ElwUri here
 		final String ulRef;
 		if (adm) {
 			if (!FileDao.SCOPE_STUD.equals(scope)) {
 				ulRef = "ul" + q;
 			} else {
 				ulRef = null;
 			}
 		} else {
 			if (FileDao.SCOPE_STUD.equals(scope) && ctx.getVer().checkWrite(ctx.getAssType(), ctx.getAss(), slot.getId(), fileDao.loadFilesStud(ctx))) {
 				ulRef = "ul" + q;
 			} else {
 				ulRef = null;
 			}
 		}
 
 		final String authorName;
 		if (e == null) {
 			if (FileDao.SCOPE_STUD.equalsIgnoreCase(scope)) {
 				authorName = ctx.getStudent().getName();
 			} else {
 				authorName = "Admin";
 			}
 		} else {
 			authorName = e.getMeta().getAuthor();
 		}
 
 
 		final VtTuple status = vt.status(f, mode, scope, ctx, slot, e);
 
 		final String nameComment;
 		if (e == null) {
 			nameComment = "";
 		} else {
 			if (e.getMeta().getComment() != null && e.getMeta().getComment().trim().length() > 0) {
 				nameComment = e.getMeta().getName() + " / " + e.getMeta().getComment();
 			} else {
 				nameComment = e.getMeta().getName();
 			}
 		}
 
 		final Object[] dataRow = {
 				/* 0 index */ data.size(),
 				/* 1 upload millis */ time,
 				/* 2 nice date - full */ e  == null ? "" : f.format(time, "EEE d HH:mm"),
 				/* 3 nice date - nice */ e  == null ? "" : f.format(time),
 				/* 4 author.id */ FileDao.SCOPE_STUD.equalsIgnoreCase(scope) ? ctx.getStudent().getId() : "admin",
 				/* 5 author.name */ authorName,
 				/* 6 class.index */ index,
 				/* 7 class.name */ ctx.getAss().getName(),
 				/* 8 slot.id */ ctx.getAssType().getId() + "--" + slot.getId(),
 				/* 9 slot.name */ ctx.getVer() != null ? ctx.getVer().getName() + " / " + slot.getName() : slot.getName(),
 				/* 10 comment */ nameComment,
 				/* 11 status sort*/ status.getSort(),
 				/* 12 status text*/ status.getText(),
 				/* 13 status classes */ status.getClasses(),
 				/* 14 source ip */ e  == null ? "" : e.getMeta().getSourceAddress(),
 				/* 15 size bytes */ e  == null ? "" : e.computeSize(),
 				/* 16 size */ e  == null ? "" : f.formatSize(e.computeSize()),
 				/* 17 approve ref */ "approve" + q,
 				/* 18 dl ref */ dlRef,
 				/* 19 ul ref */ ulRef,
 				/* 20 comment ref */ adm ? null : "#"	//	TODO comment edit url/page/method
 		};
 		return dataRow;
 	}
 
 	public List<Object[]> index(Enrollment[] enrolls) {
 		final List<Object[]> indexData = new ArrayList<Object[]>();
 		for (Enrollment enr : enrolls) {
 			indexData.add(indexRow(indexData, enr));
 		}
 		return indexData;
 	}
 
 	protected Object[] indexRow(List<Object[]> indexData, Enrollment enr) {
 		final Group group = groupDao.findGroup(enr.getGroupId());
 		final Course course = courseDao.findCourse(enr.getCourseId());
 
 		final Object[] arr = {
 				/* 0 index - */ indexData.size(),
 				/* 1 enr.id - */ enr.getId(),
 				/* 2 group.id - */ group.getId(),
 				/* 3 group.name 0*/ group.getName(),
 				/* 4 course.id - */ course.getId(),
 				/* 5 course.name 1 */ course.getName(),
 				/* 6 summary ref 2 */ uri.summary(enr.getId()),
 				/* 7 students ref 3 */ "#",
 				/* 8 tasks ref 4 */ uri.tasks(enr.getId()),
 				/* 9 classes ref 5 */ "#",
 				/* 10 uploads ref 6 */ uri.logPending(enr.getId()),
 				/* 11 uploads-open ref 7 */ uri.logOpen(enr.getId()),
 				/* 12 uploads-course ref 8 */ uri.logCourse(enr.getId()),
 		};
 		return arr;
 	}
 
 	public List<Object[]> logScore(
 			SortedMap<Stamp, Entry<Score>> allScores, Ctx ctx, FileSlot slot, Entry<FileMeta> file,
 			Format f, String mode, Stamp stamp
 	) {
 		final List<Object[]> logData = new ArrayList<Object[]>();
 
 		final Score scoreBest = chooseBestScore(allScores, ctx, slot);
 
 		for (Stamp s : allScores.keySet()) {
 			final Entry<Score> scoreEntry = allScores.get(s);
 			final Score score = scoreEntry.getMeta();
 
 			final Stamp createStamp = score.getCreateStamp();
 			final boolean selected = stamp == null ? createStamp == null : stamp.equals(createStamp);
 
 			final long time;
 			String approveUri = "approve?elw_ctx=" + ctx.toString() + "&sId=" + slot.getId() + "&fId=" + file.getMeta().getId();
 			if (createStamp == null) {
 				time = System.currentTimeMillis();
 			} else {
 				time = createStamp.getTime();
 				approveUri += "&stamp="+createStamp.toString();
 			}
 
 			final VtTuple status = vt.status(f, mode, FileDao.SCOPE_STUD, ctx, slot, file, score);
 			final VtTuple statusScoring = vt.status(f, "s", FileDao.SCOPE_STUD, ctx, slot, file, score);
 
 			final Object[] logRow = new Object[] {
 				/* 0 index - */ logData.size(),
 				/* 1 selected 0 */ selected ? "&gt;" : "",
 				/* 2 best 1 */ scoreBest == score ? "*" : "",
 				/* 3 score date millis - */ time,
 				/* 4 score date full - */ f.format(time, "EEE d HH:mm"),
 				/* 5 score date nice 2 */ f.format(time),
 				/* 6 status classes - */ status.getClasses(),
 				/* 7 status text 3 */ status.getText(),
 				/* 8 scoring 4 */ statusScoring.getText(),
 				/* 9 comment 5 */ score.getComment(),
 				/* 10 edit score 6 */ approveUri
 			};
 			logData.add(logRow);
 		}
 
 		return logData;
 	}
 
 	protected Score chooseBestScore(SortedMap<Stamp, Entry<Score>> allScores, Ctx ctx, FileSlot slot) {
 		Score scoreBest = null;
 		double pointsBest = 0;
 		for (Stamp s : allScores.keySet()) {
 			final Score scoreCur = allScores.get(s).getMeta();
 			final double pointsCur = ctx.getIndexEntry().computePoints(scoreCur, slot);
 			if (scoreBest == null || pointsBest < pointsCur) {
 				scoreBest = scoreCur;
 				pointsBest = pointsCur;
 			}
 		}
 		return scoreBest;
 	}
 
 	public List<Object[]> tasks(Ctx ctx, final LogFilter filter, Format f, boolean adm) {
 		final List<Object[]> indexData = new ArrayList<Object[]>();
 
 		final TreeMap<String, Map<String, List<Entry<FileMeta>>>> ctxVerToSlotToFiles =
 				new TreeMap<String, Map<String, List<Entry<FileMeta>>>>();
 		final TreeMap<String, Double> ctxEsToScore = new TreeMap<String, Double>();
 		final TreeMap<String, Summary> ctxEsToSummary = new TreeMap<String, Summary>();
 
 		final int studCount = tasksData(ctx, filter, adm, ctxVerToSlotToFiles, ctxEsToScore, ctxEsToSummary);
 
 		int totalBudget = 0;
 		for (int i = 0; i < ctx.getEnr().getIndex().size(); i++) {
 			final Ctx ctxAss = ctx.extendIndex(i);
 			indexData.add(tasksRow(f, indexData, ctxAss, adm, ctxEsToScore, ctxEsToSummary, studCount));
 			totalBudget += ctxAss.getIndexEntry().getScoreBudget();
 		}
 
 		final Double totalScore = ctxEsToScore.get(CTX_TO_SCORE_TOTAL);
 		if (totalBudget > 0 && studCount > 0 && totalScore != null) {
 			final double avgScore = totalScore / studCount;
 			final String score = f.format2(avgScore) + " of " + totalBudget + ": " + vt.niceRatio(f, avgScore / totalBudget, "");
 			indexData.add(new Object[]{
 					/* 0 index - */ indexData.size(),
 					/* 1 date millis - */ 0,
 					/* 2 date full - */ "",
 					/* 3 date nice 0*/ "",
 					/* 4 tType.id - */ "",
 					/* 5 tType.name 1 */ "",
 					/* 6 task.id ref - */ "",
 					/* 7 task.name ref 2 */ "",
 					/* 8 summary status sort - */ 0,
 					/* 9 summary status text - */ "",
 					/* 10 summary status text 3 */ "",
 					/* 11 summary due millis - */ 0,
 					/* 12 summary due full - */ "",
 					/* 13 summary due nice 4 */ "<b>Total:</b>",
 					/* 14 score sort */ 0,
 					/* 15 score nice 5 */ score,
 					/* 16 uploads ref 6 */ null,
 					/* 17 uploads-open ref 7 */ null,
 					/* 18 uploads-course ref 8 */ null,
 					/* 19 task-total sort - */ 1
 			});
 		}
 
 		return indexData;
 	}
 
 	protected Object[] tasksRow(
 			Format f, List<Object[]> indexData, Ctx ctxAss, boolean adm,
 			TreeMap<String, Double> ctxEsToScore, TreeMap<String, Summary> ctxEsToSummary, int studCount
 	) {
 		final Class classFrom = ctxAss.cFrom();
 
 		final VtTuple summary = vt.summary(ctxAss, ctxEsToSummary, studCount);
 		final Summary summ = ctxEsToSummary.get(ctxAss.ei());
 
 		final String dueNice;
 		final String dueFull;
 		final long dueSort;
 		if (classFrom.isStarted()) {
 			if (summ.getEarliestDue() == null) {
 				dueNice = "None";
 				dueFull = "";
 				dueSort = System.currentTimeMillis();
 			} else {
 				dueNice = "Due " + f.format(summ.getEarliestDue());
 				dueFull = f.format(summ.getEarliestDue(), "EEE d HH:mm");
 				dueSort = summ.getEarliestDue();
 			}
 		} else {
 			dueNice = "Opens " + f.format(classFrom.getFromDateTime().getMillis());
 			dueFull = f.format(classFrom.getFromDateTime().getMillis(), "EEE d HH:mm");
 			dueSort = classFrom.getFromDateTime().getMillis();
 		}
 
 		final String scoreNice;
 		final double scoreSort;
 		if (ctxEsToScore.get(ctxAss.ei()) != null) {
 			final int budget = ctxAss.getIndexEntry().getScoreBudget();
 			if (budget > 0) {
 				final double score = ctxEsToScore.get(ctxAss.ei()) / studCount;
 				scoreNice = f.format2(score) + " of " + budget + ": " + vt.niceRatio(f, score / budget, "");
 				scoreSort = score;
 			} else {
 				scoreSort = -1;
 				scoreNice = "";
 			}
 		} else {
 			scoreNice = "?";
 			scoreSort = -2;
 		}
 
 		final Object[] arr = {
 				/* 0 index - */ indexData.size(),
 				/* 1 date millis - */ classFrom.getFromDateTime().getMillis(),
 				/* 2 date full - */ f.format(classFrom.getFromDateTime().getMillis(), "EEE d HH:mm"),
 				/* 3 date nice 0*/ f.format(classFrom.getFromDateTime().getMillis()),
 				/* 4 tType.id - */ ctxAss.getAssType().getId(),
 				/* 5 tType.name 1 */ ctxAss.getAssType().getName(),
 				/* 6 task.id ref - */ ctxAss.getAss().getId(),
 				/* 7 task.name ref 2 */ ctxAss.getAss().getName(),
 				/* 8 summary status sort - */ summary.getSort(),
 				/* 9 summary status text - */ summary.getClasses(),
 				/* 10 summary status text 3 */ summary.getText(),
 				/* 11 summary due millis - */ dueSort,
 				/* 12 summary due full - */ dueFull,
 				/* 13 summary due nice 4 */ dueNice,
 				/* 14 score sort */ scoreSort,
 				/* 15 score nice 5 */ scoreNice,
 				/* 16 uploads ref 6 */ adm || classFrom.isStarted() ? uri.logPending(ctxAss.getEnr().getId(), ctxAss.getAss().getId()) : null,
 				/* 17 uploads-open ref 7 */ adm || classFrom.isStarted() ? uri.logOpen(ctxAss.getEnr().getId(), ctxAss.getAss().getId()) : null,
 				/* 18 uploads-course ref 8 */ adm || classFrom.isStarted() ? uri.logCourse(ctxAss.getEnr().getId(), ctxAss.getAss().getId()) : null,
 				/* 19 task-total sort - */ 0
 		};
 
 		return arr;
 	}
 
 	/**
 	 * Generate per-task data score summaries
 	 *
 	 * @param ctxEnr context with a student set (non-adm) or only enrollment set (adm)
 	 * @param filter to filter tasks and/or students
 	 * @param adm whether this is an admin report or not
 	 * @param fileMetas to store per slot file meta listings
 	 * @param ctxToScore to store totals per task
 	 * @param ctxToSummary to handle open/pending/approved stats
 	 *
 	 * @return number of students processed in this report
 	 */
 	public int tasksData(
 			Ctx ctxEnr, LogFilter filter, boolean adm,
 			Map<String, Map<String, List<Entry<FileMeta>>>> fileMetas,
 			Map<String, Double> ctxToScore,
 			Map<String, Summary> ctxToSummary
 	) {
 		int students = 0;
 		if (adm) {
 			for (Student stud : ctxEnr.getGroup().getStudents()) {
 				if (W.excluded(filter.getStudId(), stud.getId())) {
 					continue;
 				}
 				students += 1;
 				storeTasksData(ctxEnr.extendStudent(stud), filter, fileMetas, ctxToScore, ctxToSummary);
 			}
 		} else {
 			//	let's hope that student id is already present here...
 			students += 1;
 			storeTasksData(ctxEnr, filter, fileMetas, ctxToScore, ctxToSummary);
 		}
 
 		return students;
 	}
 
 	protected void storeTasksData(
 			Ctx ctxStud, LogFilter filter,
 			Map<String, Map<String, List<Entry<FileMeta>>>> fileMetas,
 			Map<String, Double> ctxToScore,
 			Map<String, Summary> ctxToSummary
 	) {
 		for (int i = 0; i < ctxStud.getEnr().getIndex().size(); i++) {
 			storeTaskData(ctxStud.extendIndex(i), filter, fileMetas, ctxToScore, ctxToSummary);
 		}
 	}
 
 	protected void storeTaskData(
 			Ctx ctxVer, LogFilter filter,
 			Map<String, Map<String, List<Entry<FileMeta>>>> fileMetas,
 			Map<String, Double> ctxToScore,
 			Map<String, Summary> ctxToSummary
 	) {
 		final String assPath = ctxVer.toString();
 		final AssignmentType assType = ctxVer.getAssType();
 
 		final SortedMap<String, List<Entry<FileMeta>>> slotIdToFiles = fileDao.loadFilesStud(ctxVer);
 		if (fileMetas != null) {
 			fileMetas.put(assPath, slotIdToFiles);
 		}
 
 		for (FileSlot slot : assType.getFileSlots()) {
 			if (W.excluded(filter.getSlotId(), assType.getId(), slot.getId())) {
 				continue;
 			}
 
 			final Class classDue = ctxVer.cDue(slot.getId());
 			final Long classDueStamp = classDue != null ? classDue.getFromDateTime().getMillis() : null;
 			final List<Entry<FileMeta>> filesForSlot = slotIdToFiles.get(slot.getId());
 			final Entry<FileMeta> bestFile = selectBestFile(ctxVer, slot.getId(), filesForSlot, slot);
 
 			final double scoreForIdx;
 			final Summary sum;
 			if (bestFile != null) {
 				final Score score = bestFile.getMeta().getScore();
 
 				if (score != null && Boolean.TRUE.equals(score.getApproved())) {
 					scoreForIdx = ctxVer.getIndexEntry().computePoints(score, slot);
 				} else {
 					scoreForIdx = 0;
 				}
 				sum = Summary.forScore(classDueStamp, score == null ? null : score.getApproved());
 			} else {
 				scoreForIdx = 0;
 				if (classDue == null) {
 					sum = new Summary(0,0,0,0, null);
 				} else if (filesForSlot == null || filesForSlot.isEmpty()) {
 					sum = new Summary(0,0,1,0, classDueStamp);
 				} else {
 					final Entry<FileMeta> lastFile = filesForSlot.get(filesForSlot.size() - 1);
 					final Score score = lastFile.getMeta().getScore();
 
 					sum = Summary.forScore(classDueStamp, score == null ? null : score.getApproved());
 				}
 			}
 
 			ctxToScore.put(ctxVer.toString(), scoreForIdx);
 			Summary.increment(ctxToScore, ctxVer.ei(), scoreForIdx);
 			Summary.increment(ctxToScore, ctxVer.es(), scoreForIdx);
 			Summary.increment(ctxToScore, CTX_TO_SCORE_TOTAL, scoreForIdx);
 			Summary.increment(ctxToSummary, ctxVer.ei(), sum);
 		}
 	}
 
 	protected Entry<FileMeta> selectBestFile(Ctx ctxVer, String slotId, List<Entry<FileMeta>> filesForSlot, final FileSlot slot) {
 		if (filesForSlot == null || filesForSlot.isEmpty()) {
 			return null;
 		}
 
 		for (Entry<FileMeta> e : filesForSlot) {
 			if (e.getMeta().getScore() != null && e.getMeta().getScore().getApproved() != null) {
 				continue;	//	don't alter any scores once they're approved
 			}
 			final Score autoScore = ScoreDao.updateAutos(
 					ctxVer, slotId, e, e.getMeta().getScore()
 			);
 			e.getMeta().setScore(autoScore);
 		}
 
 		Entry<FileMeta> usedEntry = null;
 		double maxScore = 0;
 		for (Entry<FileMeta> e : filesForSlot) {
 			final Score score = e.getMeta().getScore();
 			final double eScore = ctxVer.getIndexEntry().computePoints(score, slot);
 
 			if (!Boolean.FALSE.equals(score.getApproved()) && (usedEntry == null || maxScore < eScore)) {
 				maxScore = eScore;
 				usedEntry = e;
 			}
 		}
 
 		if (usedEntry != null) {
 			final Score s = usedEntry.getMeta().getScore();
 			if (s != null) {
 				s.setBest(true);
 			}
 		}
 
 		return usedEntry;
 	}
 	
 	public String cmpForwardToEarliestPendingSince(Ctx ctx, FileSlot slot, Stamp since) {
 		FileMeta epF = null;	//	earliest pending
 		Ctx epCtx = null;
 
 		final Ctx ctxEnr = Ctx.forEnr(ctx.getEnr()).resolve(enrollDao, groupDao,  courseDao);
 		//	LATER oh this pretty obviously looks like we REALLY need some rdbms from now on... :D
 		for (Student stud : ctx.getGroup().getStudents()) {
 			final Ctx ctxStud = ctxEnr.extendStudent(stud);
 			for (int index = 0; index < ctx.getEnr().getIndex().size(); index++) {
 				final Ctx ctxVer = ctxStud.extendIndex(index);
 				if (!ctxVer.getAssType().getId().equals(ctx.getAssType().getId())) {
 					continue;	//	other ass types out of scope
 				}
 				for (FileSlot s : ctxVer.getAssType().getFileSlots()) {
 					if (!s.getId().equals(slot.getId())) {
 						continue;	//	other slots out of scope
 					}
 					final Entry<FileMeta>[] uploads = fileDao.findFilesFor(FileDao.SCOPE_STUD, ctxVer, slot.getId());
 					if (uploads != null && uploads.length > 0) {
 						for (int i = uploads.length - 1; i >= 0; i--) {
 							final FileMeta f = uploads[i].getMeta();
							if (since != null && f.getCreateStamp().compareTo(since) < 0) {
 								break;	//	oh this is overly stale
 							}
 
							if (f.getScore() == null) {
 								if ((epF == null || epF.getCreateStamp().getTime() > f.getCreateStamp().getTime())) {
 									epF = f;
 									epCtx = ctxVer;
 								}
 								break;	//	don't look into earlier pending versions before this one is approved
 							} else if (Boolean.TRUE.equals(f.getScore().getApproved())) {
 								break;	//	don't look into earlier pending versions after this one is approved
 							}
 						}
 					}
 				}
 			}
 		}
 
 		final String forward;
 		if (epCtx != null) {
 			forward = "approve?elw_ctx=" + epCtx.toString() + "&sId=" + slot.getId() + "&fId=" + epF.getId();
 		} else {
			forward = "log?elw_ctx=" + ctxEnr.toString() + "&f_slot=" + slot.getId();
 		}
 		return forward;
 	}
 }
