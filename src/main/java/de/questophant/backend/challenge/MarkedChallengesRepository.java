package de.questophant.backend.challenge;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface MarkedChallengesRepository extends JpaRepository<MarkedChallenge, Long> {

	@Modifying
	@Transactional
	@Query(value = "DELETE FROM marked_challenges WHERE user_id = :userId AND challenge_id = :challengeId", nativeQuery = true)
	public void delete(long userId, long challengeId);
}