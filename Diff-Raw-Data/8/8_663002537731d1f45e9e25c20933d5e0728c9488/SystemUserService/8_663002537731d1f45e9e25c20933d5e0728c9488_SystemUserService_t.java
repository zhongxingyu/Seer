 package cz.muni.fi.pa165.pujcovnastroju.service;
 
 import java.util.List;
 
 import cz.muni.fi.pa165.pujcovnastroju.dto.SystemUserDTO;
 import cz.muni.fi.pa165.pujcovnastroju.dto.UserTypeEnumDTO;
 import org.springframework.security.access.prepost.PostAuthorize;
 import org.springframework.security.access.prepost.PostFilter;
 import org.springframework.security.access.prepost.PreAuthorize;
 
 /**
  * 
  * @author Vojtech Schlemmer
  */
 public interface SystemUserService {
 	/**
 	 * Persist SystemUserDTO into database
 	 * 
 	 * @param userDTO
 	 *            to be persisted
 	 * @return persisted user
 	 * @throws DataAccessException
 	 *             if the userDTO is null
 	 */
 	@PreAuthorize("hasRole('ADMINISTRATOR') OR "
 			+ "((hasRole('EMPLOYEE') OR isAnonymous()) AND "
 			+ "(#userDTO.type.typeLabel == 'CUSTOMERLEGAL' OR #userDTO.type.typeLabel == 'CUSTOMERINDIVIDUAL'))")
 	public SystemUserDTO create(SystemUserDTO userDTO);
 
 	/**
 	 * returns SystemUserDTO with given id
 	 * 
 	 * @param id
 	 *            id of the user
 	 * @return SystemUserDTO with the given id
 	 * @throws DataAccessException
 	 *             if the id is null
 	 */
 	@PostAuthorize("hasRole('ADMINISTRATOR') OR "
 			+ "(hasRole('EMPLOYEE') AND "
 			+ "(returnObject.type.typeLabel == 'CUSTOMERLEGAL' OR "
 			+ "returnObject.type.typeLabel == 'CUSTOMERINDIVIDUAL')) OR "
 			+ "(hasRole('REVISIONER') AND returnObject.type.typeLabel == 'REVISIONER') OR "
 			+ "returnObject.username == principal.username")
 	public SystemUserDTO read(Long id);
 
 	/**
 	 * Updates given SystemUserDTO
 	 * 
 	 * @param userDTO
 	 *            to be updated
 	 * @return updated SystemUserDTO
 	 * @throws DataAccessException
 	 *             if the user is null
 	 */
 	@PreAuthorize("hasRole('ADMINISTRATOR') OR "
 			+ "((hasRole('EMPLOYEE') OR #userDTO.username == principal.username) AND " 
 			+ "(#userDTO.type.typeLabel == 'CUSTOMERLEGAL' OR "
 			+ "#userDTO.type.typeLabel == 'CUSTOMERINDIVIDUAL'))")
 	public SystemUserDTO update(SystemUserDTO userDTO);
 
 	/**
 	 * 
 	 * @param userDTO
 	 *            to be deleted
 	 * @return deleted SystemUserDTO
 	 * @throws DataAccessException
 	 *             if the user is null
 	 */
 	@PreAuthorize("hasRole('ADMINISTRATOR') OR "
 			+ "(hasRole('EMPLOYEE') AND " 
 			+ "(#userDTO.type.typeLabel == 'CUSTOMERLEGAL' OR "
 			+ "#userDTO.type.typeLabel == 'CUSTOMERINDIVIDUAL'))")
 	public SystemUserDTO delete(SystemUserDTO userDTO);
 
 	/**
 	 * Returns all users
 	 * 
 	 * @return list of all users
 	 */
 	@PostFilter("hasRole('ADMINISTRATOR') OR "
 			+ "(hasRole('EMPLOYEE') AND "
 			+ "(filterObject.type.typeLabel == 'CUSTOMERLEGAL' OR "
 			+ "filterObject.type.typeLabel == 'CUSTOMERINDIVIDUAL')) OR "
 			+ "filterObject.username == principal.username")
 	public List<SystemUserDTO> findAllSystemUsers();
 
 	/**
 	 * Retrieves system users that match given parameters. If any parameter is
 	 * null, it won't be involved in lookup
 	 * 
 	 * @param firstName
 	 *            first name of users
 	 * @param lastName
 	 *            last name of users
 	 * @param type
 	 *            type of users
 	 * @return list of users that match the given parameters
 	 */
 	@PostFilter("hasRole('ADMINISTRATOR') OR "
 			+ "(hasRole('EMPLOYEE') AND "
 			+ "(filterObject.type.typeLabel == 'CUSTOMERLEGAL' OR "
 			+ "filterObject.type.typeLabel == 'CUSTOMERINDIVIDUAL')) OR "
 			+ "((hasRole('CUSTOMERLEGAL') OR hasRole('CUSTOMERINDIVIDUAL')) AND "
 			+ "(filterObject.username == principal.username)) OR "
 			+ "(hasRole('REVISIONER') AND filterObject.type.typeLabel == 'REVISIONER')")
 	public List<SystemUserDTO> getSystemUsersByParams(String firstName,
			String lastName, UserTypeEnumDTO type, String userName);
 
 	/**
 	 * Retrieves users of given types
 	 * 
 	 * @param types list of types
 	 * @return list of users of given types
 	 */
 	@PostFilter("hasRole('ADMINISTRATOR') OR "
 			+ "(hasRole('EMPLOYEE') AND "
 			+ "(filterObject.type.typeLabel == 'CUSTOMERLEGAL' OR "
 			+ "filterObject.type.typeLabel == 'CUSTOMERINDIVIDUAL')) OR "
 			+ "((hasRole('CUSTOMERLEGAL') OR hasRole('CUSTOMERINDIVIDUAL')) AND "
 			+ "(filterObject.username == principal.username)) OR "
 			+ "(hasRole('REVISIONER') AND filterObject.type.typeLabel == 'REVISIONER')")
 	public List<SystemUserDTO> getSystemUsersByTypeList(List<UserTypeEnumDTO> types);
 
 	/**
 	 * Retrieves user of given username
 	 * 
 	 * @param username
 	 * @return user with given username
 	 */
 	@PostAuthorize("hasRole('ADMINISTRATOR') OR "
 			+ "(hasRole('EMPLOYEE') AND "
 			+ "(returnObject.type.typeLabel == 'CUSTOMERLEGAL' OR "
 			+ "returnObject.type.typeLabel == 'CUSTOMERINDIVIDUAL')) OR "
 			+ "isAnonymous() OR returnObject.username == principal.username")
 	public SystemUserDTO getSystemUserByUsername(String username);
 }
