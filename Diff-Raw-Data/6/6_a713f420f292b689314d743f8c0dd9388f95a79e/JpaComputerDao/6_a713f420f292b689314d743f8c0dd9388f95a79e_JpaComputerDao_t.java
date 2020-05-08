 package com.formation.project.computerDatabase.dao;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.data.domain.Page;
 import org.springframework.data.domain.PageRequest;
 import org.springframework.data.domain.Pageable;
 import org.springframework.data.domain.Sort;
 import org.springframework.stereotype.Repository;
 
 import com.formation.project.computerDatabase.base.Computer;
 import com.formation.project.computerDatabase.base.TableSort;
 import com.formation.project.computerDatabase.repositories.ComputerRepository;
 
 @Repository
 public class JpaComputerDao implements IComputerDao {
 		
 	@Autowired
 	private ComputerRepository repo;
 
 	private Sort getSort(TableSort sortBy) {
 		return new Sort(sortBy.isAsc() 
 				? Sort.Direction.ASC
 				: Sort.Direction.DESC, 
 				sortBy.getSortString());
 	}
 
 	@Override
 	public Computer getComputer(Long id) {
 		return repo.findOne(id);
 	}
 
 	@Override
 	public Page<Computer> getComputers(Integer currentPage, Integer resultsPerPage, TableSort sortBy, String name) {
 		Pageable pageable = new PageRequest(currentPage-1, resultsPerPage, getSort(sortBy));
 		return repo.findAllByNameLikeIgnoreCase(String.format("%%%s%%", name), pageable);
 	}
 
 	@Override
 	public boolean saveOrUpdate(Computer computer) {
 		repo.save(computer);
		if(computer.getId() == null)
			return false;
		return true;
 	}
 
 	@Override
 	public void delete(Long id) {
 		Computer c = getComputer(id);
 		repo.delete(c);
 	}
 
 }
