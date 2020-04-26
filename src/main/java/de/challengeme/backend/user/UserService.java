package de.challengeme.backend.user;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private EntityManager em;

	public Points getUserPoints(User user) {

		// @formatter:off
		Query query = em.createNativeQuery( " SELECT 0 id, IFNULL(SUM(u.points),0) points FROM" + 
											" (" + 
											"	SELECT SUM(points_win) points FROM challenges WHERE id in (" + 
											"		SELECT challenge_id FROM challenge_status WHERE user_id = :userId AND state = 1" + 
											"	)" + 
											"	UNION ALL" + 
											"	SELECT SUM(points_loose) points FROM challenges WHERE id in (" + 
											"		SELECT challenge_id FROM challenge_status WHERE user_id = :userId AND state = 2" + 
											"	)" + 
											" ) AS u;", Points.class);
		// @formatter:on

		query.setParameter("userId", user.getId());

		for (Object object : query.getResultList()) {
			return (Points) object;
		}

		return null;
	}

	public int getCountOfAdminUsers() {
		return userRepository.getCountOfAdminUsers();
	}

	public User createUser() {
		while (true) {
			UUID userId = UUID.randomUUID();
			User user = userRepository.getByUserId(userId.toString());
			if (user == null) {
				user = new User();
				user.setUserId(userId);
				user.setCreatedAt(Instant.now());
				userRepository.saveAndFlush(user);
				return user;
			}
		}
	}

	public User getUserByUserName(String userName) {
		return userRepository.getByUserName(userName);
	}

	public User getUserByUserId(UUID userId) {
		return getUserByUserId(userId.toString());
	}

	public User getUserByUserId(String userId) {
		User user = userRepository.getByUserId(userId);
		if (user != null) {
			user.setLastRequestAt(Instant.now());
			userRepository.save(user);
		}
		return user;
	}

	public User getRootUser() {
		return userRepository.getRootUser();
	}

	public void save(User user) {
		userRepository.saveAndFlush(user);
	}

	public String getUserNameFromId(long id) {
		Optional<User> o = userRepository.findById(id);
		if (o.isPresent()) {
			return o.get().getUserName();
		}
		return null;
	}
}
