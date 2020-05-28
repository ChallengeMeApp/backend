package de.questophant.backend.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface UserRepository extends JpaRepository<MyUser, Long> {

	@Query(value = "SELECT * FROM users WHERE private_user_id = UUID_TO_BIN(:privateUserId)", nativeQuery = true)
	public MyUser getByPrivateUserId(String privateUserId);

	@Query(value = "SELECT * FROM users WHERE user_name = :userName", nativeQuery = true)
	public MyUser getByUserName(String userName);

	@Query(value = "SELECT COUNT(*) FROM users WHERE admin = true", nativeQuery = true)
	public int getCountOfAdminUsers();

	@Query(value = "SELECT * FROM users WHERE admin = true ORDER BY created_at DESC LIMIT 1", nativeQuery = true)
	public MyUser getRootUser();
}
