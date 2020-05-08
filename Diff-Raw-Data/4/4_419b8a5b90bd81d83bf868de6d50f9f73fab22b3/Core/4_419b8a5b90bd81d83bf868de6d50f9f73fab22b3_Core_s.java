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
 import elw.web.VelocityUtils;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 public class Core {
 	protected final CourseDao courseDao;
 	protected final GroupDao groupDao;
 	protected final EnrollDao enrollDao;
 	protected final ScoreDao scoreDao;
 	protected final FileDao fileDao;
 
 	public Core(CourseDao courseDao, EnrollDao enrollDao, FileDao fileDao, GroupDao groupDao, ScoreDao scoreDao) {
 		this.courseDao = courseDao;
 		this.enrollDao = enrollDao;
 		this.fileDao = fileDao;
 		this.groupDao = groupDao;
 		this.scoreDao = scoreDao;
 	}
 
 	public List<Object[]> prepareLogEntries(
 			Ctx ctx, Format format, VelocityUtils u, LogFilter logFilter
 	) {
 		final List<Object[]> logData = new ArrayList<Object[]>();
 
 		if ("s".equalsIgnoreCase(logFilter.getScopePath()[0])) {
 			prepareLogEntriesStud(ctx, format, u, logFilter, logData);
 		} else if ("c".equalsIgnoreCase(logFilter.getScopePath()[0])) {
 			prepareLogEntriesAdm(ctx, format, u, logFilter, logData);
 		}
 
 		return logData;
 	}
 
 	private void prepareLogEntriesAdm(Ctx ctx, Format format, VelocityUtils u, LogFilter lf, List<Object[]> logData) {
 		for (int i = 0; i < ctx.getEnr().getIndex().size(); i++) {
 			final Ctx ctxAss = ctx.extendIndex(i);
 
 			final AssignmentType aType = ctxAss.getAssType();
 			final FileSlot[] slots = aType.getFileSlots();
 			for (FileSlot slot : slots) {
 				if (W.excluded(lf.getSlotId(), aType.getId(), slot.getId())) {
 					continue;
 				}
 
 				final Entry<FileMeta>[] uploadsAss = fileDao.findFilesFor(FileDao.SCOPE_ASS, ctxAss, slot.getId());
 				int total = uploadsAss.length;
 				if (lf.cScopeOne('a') && lf.cVer(ctxAss)) {
 					addRows(format, u, lf, logData, i, ctxAss, slot, uploadsAss, FileDao.SCOPE_ASS);
 				}
 
 				for (Version ver : ctxAss.getAss().getVersions()) {
 					final Ctx ctxVer = ctxAss.extendVer(ver);
 					final Entry<FileMeta>[] uploadsVer = fileDao.findFilesFor(FileDao.SCOPE_VER, ctxVer, slot.getId());
 					total += uploadsVer.length;
 					if (lf.cScopeOne('v') && lf.cVer(ctxVer)) {
 						addRows(format, u, lf, logData, i, ctxVer, slot, uploadsVer, FileDao.SCOPE_VER);
 					}
 				}
 
 				if (lf.cScopeOpen() && lf.cVer(ctxAss) && total == 0) {
 					logData.add(createRowLog(format, u, lf.getMode(), logData, i, ctxAss, slot, null, FileDao.SCOPE_ASS));
 				}
 			}
 		}
 	}
 
 	protected void prepareLogEntriesStud(Ctx ctx, Format f, VelocityUtils u, LogFilter lf, List<Object[]> logData) {
 		for (Student stud : ctx.getGroup().getStudents()) {
 			if (W.excluded(lf.getStudId(), stud.getId())) {
 				continue;
 			}
 
 			final Ctx ctxStud = ctx.extendStudent(stud);
 			for (int index = 0; index < ctx.getEnr().getIndex().size(); index++) {
 				final Ctx ctxVer = ctxStud.extendIndex(index);
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
 					if (!lf.cDue(ctxVer, slot, uploads)) {
 						continue;
 					}
 
 					addRows(f, u, lf, logData, index, ctxVer, slot, uploads, FileDao.SCOPE_STUD);
 					if (uploads.length == 0 && lf.cScopeStud(slot, null)) {
 						logData.add(createRowLog(f, u, lf.getMode(), logData, index, ctxVer, slot, null, FileDao.SCOPE_STUD));
 					}
 				}
 			}
 		}
 	}
 
 	protected int addRows(Format format, VelocityUtils u, LogFilter logFilter, List<Object[]> logData, int index, Ctx ctxVer, FileSlot slot, Entry<FileMeta>[] uploads, String scope) {
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
 			logData.add(createRowLog(format, u, logFilter.getMode(), logData, index, ctxVer, slot, e, scope));
 		}
 		return shown;
 	}
 
 	protected Object[] createRowLog(
 			Format f, VelocityUtils u, final String mode, List<Object[]> data,
 			int index, Ctx ctx, FileSlot slot, Entry<FileMeta> e, String scope
 	) {
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
 
 		final String txRef;
 		if (e != null) {
 			txRef = "<a href=\"../s/dl/" + nameNorm + q + "\">DL</a>";
 		} else {
 			//	FIXME proper UL URI here
 			txRef = "<a href=\"../a/ul" + q + "\">UL</a>";
 		}
 
 		final String authorName;
 		if (e == null) {
 			if (ctx.getStudent() != null) {
 				authorName = ctx.getStudent().getName();
 			} else {
 				authorName = "Admin";
 			}
 		} else {
 			authorName = e.getMeta().getAuthor();
 		}
 
 
 		final Map<String, String> status = u.status(f, mode, scope, ctx, slot, e);
 		final Object[] dataRow = {
 				/* 0 index */ data.size(),
 				/* 1 upload millis */ time,
 				/* 2 nice date - full */ e  == null ? "" : f.format(time, "EEE d HH:mm"),
 				/* 3 nice date - nice */ e  == null ? "" : f.format(time),
 				/* 4 author.id */ FileDao.SCOPE_STUD.equalsIgnoreCase(scope) ? ctx.getStudent().getId() : "admin",
 				/* 5 author.name */ authorName,
 				/* 6 class.index */ index,
 				/* 7 class.name */ ctx.getAss().getName(),
				/* 8 slot.id */ slot.getId(),
				/* 9 slot.name */ slot.getName(),
 				/* 10 comment */ e  == null ? "" : e.getMeta().getComment(),
 				/* 11 status text*/ status.get("text"),
 				/* 12 status classes */ status.get("classes"),
 				/* 13 approve */ "<a href=\"approve" + q + "\">A</a>",
 				/* 14 download */ txRef
 		};
 		return dataRow;
 	}
 }
