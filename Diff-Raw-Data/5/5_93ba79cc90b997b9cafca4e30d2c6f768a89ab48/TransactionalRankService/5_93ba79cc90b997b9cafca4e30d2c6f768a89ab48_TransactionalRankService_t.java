 /**
  * Copyright (C) 2011  JTalks.org Team
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or (at your option) any later version.
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
  */
 package org.jtalks.poulpe.service.transactional;
 
 import java.util.List;
 import org.jtalks.common.service.transactional.AbstractTransactionalEntityService;
 import org.jtalks.poulpe.model.dao.RankDao;
 import org.jtalks.poulpe.model.entity.Rank;
 import org.jtalks.poulpe.service.RankService;
 import org.jtalks.poulpe.service.exceptions.NotUniqueException;
 
 /**
 * Transactional Rank Service implementation.
  * @author Pavel Vervenko
  */
 public class TransactionalRankService extends AbstractTransactionalEntityService<Rank, RankDao> implements RankService {
 
     /**
      * Create an instance of service.
      * @param rankDao rank DAO
      */
     public TransactionalRankService(RankDao rankDao) {
         this.dao = rankDao;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public List<Rank> getAll() {
         return dao.getAll();
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void deleteRank(Rank rank) {
         dao.delete(rank);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void saveRank(Rank rank) throws NotUniqueException {
         if (dao.isRankNameExists(rank.getRankName())) {
            throw new NotUniqueException("Name " + rank.getRankName() + " already exists");
         }
         dao.saveOrUpdate(rank);
     }
 }
