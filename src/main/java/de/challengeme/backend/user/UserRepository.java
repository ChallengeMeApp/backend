package de.challengeme.backend.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface UserRepository extends JpaRepository<User, Long> {

	@Query(value = "SELECT * FROM users WHERE user_id = UUID_TO_BIN(:userId)", nativeQuery = true)
	public User getByUserId(String userId);

	@Query(value = "SELECT * FROM users WHERE user_name = :userName", nativeQuery = true)
	public User getByUserName(String userName);

	@Query(value = "SELECT COUNT(*) FROM users WHERE admin = true", nativeQuery = true)
	public int getCountOfAdminUsers();

	@Query(value = "SELECT * FROM users WHERE admin = true ORDER BY created_at DESC LIMIT 1", nativeQuery = true)
	public User getRootUser();
}
