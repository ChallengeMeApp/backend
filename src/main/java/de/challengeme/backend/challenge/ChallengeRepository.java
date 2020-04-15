package de.challengeme.backend.challenge;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ChallengeRepository extends JpaRepository<Challenge, Long> {

	@Query(value = "SELECT * FROM challenges WHERE deleted_at IS NULL AND kind = 'self' AND created_by_import = true AND category = :category AND id NOT IN(SELECT challenge_id FROM challengeresults WHERE user_id=:userId) ORDER BY rand() LIMIT 1", nativeQuery = true)
	public Challenge getRandomChallenge(String category, long userId);

	@Query(value = "SELECT * FROM challenges WHERE deleted_at IS NULL AND kind = 'self' AND created_by_import = true AND category = :category ORDER BY rand() LIMIT 1", nativeQuery = true)
	public Challenge getRandomChallenge(String category);

	@Query(value = "SELECT * FROM challenges WHERE deleted_at IS NULL AND kind = 'self' AND created_by_import = true ORDER BY rand() LIMIT 1", nativeQuery = true)
	public Challenge getRandomChallenge();

	@Query(value = "SELECT * FROM challenges WHERE id = :id LIMIT 1", nativeQuery = true)
	public Challenge getChallengeFromId(long id);

	@Query(value = "SELECT * FROM challenges WHERE deleted_at IS NULL AND created_by_user_id=:userId ORDER BY created_at", nativeQuery = true)
	public List<Challenge> getChallengesCreatedByUser(long userId);

	@Query(value = "SELECT * FROM challenges WHERE deleted_at IS NULL AND created_by_user_id=:userId ORDER BY created_at", nativeQuery = true)
	public Slice<Challenge> getChallengesCreatedByUser(long userId, Pageable pageable);

	@Query(value = "DELETE FROM challenges WHERE created_by_user_id=:userId AND id = :challengeId", nativeQuery = true)
	public void deleteChallenge(long userId, long challengeId);

	@Query(value = "DELETE FROM challenges WHERE created_by_import=true", nativeQuery = true)
	public void deleteImportedChallenges();

	// @formatter:off
	@Query(value = 
			"	SELECT *, (successes / (successes + failures)) as successFailureRatio FROM (" + 
			"	 SELECT c.*, SUM(case when cr.success = 1 then 1 else 0 end) as successes, SUM(case when cr.success = 0 then 1 else 0 end) as failures FROM challenges AS c" + 
			"	 LEFT JOIN challengeresults AS cr ON cr.challenge_id = c.id" + 
			"    WHERE c.deleted_at IS NULL AND c.kind = 'self' AND c.created_by_import AND c.category = :category AND (c.repeatable OR c.id NOT IN (SELECT challenge_id FROM challengeresults WHERE user_id=:userId)" +
			"   )" + 
			"	GROUP by c.id) as i" + 
			"	ORDER BY successFailureRatio DESC", nativeQuery = true)
	public Slice<Challenge> getChallengesForStream(String category, long userId, Pageable pageable);
	// @formatter:on

	// @formatter:off
	@Query(value = 
			"	SELECT *, (successes / (successes + failures)) as successFailureRatio FROM (" + 
			"	 SELECT c.*, SUM(case when cr.success = 1 then 1 else 0 end) as successes, SUM(case when cr.success = 0 then 1 else 0 end) as failures FROM challenges AS c" + 
			"	 LEFT JOIN challengeresults AS cr ON cr.challenge_id = c.id" + 
			"    WHERE c.deleted_at IS NULL AND c.kind = 'self' AND c.created_by_import AND (c.repeatable OR c.id NOT IN (SELECT challenge_id FROM challengeresults WHERE user_id=:userId)" +
			"   )" + 
			"	GROUP by c.id) as i" + 
			"	ORDER BY successFailureRatio DESC", nativeQuery = true)
	public Slice<Challenge> getChallengesForStream(long userId, Pageable pageable);
	// @formatter:on
}