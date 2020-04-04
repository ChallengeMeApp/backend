package de.challengeme.backend.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface UserRepository extends JpaRepository<User, Long> {

	@Query(value = "SELECT * FROM users WHERE user_id = UUID_TO_BIN(:userId)", nativeQuery = true)
	public User getByUUID(String userId);
}
