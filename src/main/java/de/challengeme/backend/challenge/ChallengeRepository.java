package de.challengeme.backend.challenge;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ChallengeRepository extends JpaRepository<Challenge, Long> {

	@Query(value = "SELECT * FROM challenges WHERE deleted = false AND kind = 'self' AND created_by_import = true AND category = :category AND id NOT IN(SELECT challenge_id FROM challengeresults WHERE user_id=:userId) ORDER BY rand() LIMIT 1", nativeQuery = true)
	public Challenge getRandomChallenge(String category, long userId);

	@Query(value = "SELECT * FROM challenges WHERE deleted = false AND kind = 'self' AND created_by_import = true AND category = :category ORDER BY rand() LIMIT 1", nativeQuery = true)
	public Challenge getRandomChallenge(String category);

	@Query(value = "SELECT * FROM challenges WHERE deleted = false AND kind = 'self' AND created_by_import = true ORDER BY rand() LIMIT 1", nativeQuery = true)
	public Challenge getRandomChallenge();

	@Query(value = "SELECT * FROM challenges WHERE deleted = false AND created_by_user_id=:userId ORDER BY created_at", nativeQuery = true)
	public List<Challenge> getChallengesCreatedByUser(long userId);

	@Query(value = "DELETE FROM challenges WHERE created_by_user_id=:userId AND id = :challengeId", nativeQuery = true)
	public void deleteChallenge(long userId, long challengeId);

	@Query(value = "DELETE FROM challenges WHERE created_by_import=true", nativeQuery = true)
	public void deleteImportedChallenges();

}