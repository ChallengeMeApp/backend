package de.questophant.backend.challenge;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface IgnoredChallengesRepository extends JpaRepository<IgnoredChallenge, Long> {

	@Modifying
	@Transactional
	@Query(value = "DELETE FROM ignored_challenges WHERE user_id = :userId AND challenge_id = :challengeId", nativeQuery = true)
	public void delete(long userId, long challengeId);
}
