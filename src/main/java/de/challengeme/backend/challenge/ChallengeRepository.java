package de.challengeme.backend.challenge;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface ChallengeRepository extends JpaRepository<Challenge, Long> {

	@Query(value = "SELECT * FROM challenges WHERE deleted_at IS NULL AND kind = 'self' AND created_by_import = true AND category = :category AND id NOT IN(SELECT challenge_id FROM challengestatus WHERE user_id = :userId AND state != 0) ORDER BY rand() LIMIT 1", nativeQuery = true)
	public Challenge getRandomChallenge(String category, long userId);

	@Query(value = "SELECT * FROM challenges WHERE deleted_at IS NULL AND kind = 'self' AND created_by_import = true AND category = :category ORDER BY rand() LIMIT 1", nativeQuery = true)
	public Challenge getRandomChallenge(String category);

	@Query(value = "SELECT * FROM challenges WHERE deleted_at IS NULL AND kind = 'self' AND created_by_import = true ORDER BY rand() LIMIT 1", nativeQuery = true)
	public Challenge getRandomChallenge();

	@Query(value = "SELECT * FROM challenges WHERE created_by_import = true AND title = :title", nativeQuery = true)
	public Challenge getImportedChallengeFromTitle(String title);

	@Query(value = "SELECT * FROM challenges WHERE created_by_import = true", nativeQuery = true)
	public List<Challenge> getImportedChallenges();

	@Query(value = "SELECT * FROM challenges WHERE id = :id LIMIT 1", nativeQuery = true)
	public Challenge getChallengeFromId(long id);

	@Query(value = "SELECT * FROM challenges WHERE deleted_at IS NULL AND created_by_user_id=:userId ORDER BY created_at", nativeQuery = true)
	public List<Challenge> getChallengesCreatedByUser(long userId);

	@Query(value = "SELECT * FROM challenges WHERE deleted_at IS NULL AND created_by_user_id=:userId ORDER BY created_at", nativeQuery = true)
	public Slice<Challenge> getChallengesCreatedByUser(long userId, Pageable pageable);

	@Modifying
	@Transactional
	@Query(value = "DELETE FROM challenges WHERE id = :challengeId", nativeQuery = true)
	public void deleteChallenge(long challengeId);

	@Modifying
	@Transactional
	@Query(value = "DELETE FROM challenges WHERE created_by_import=true", nativeQuery = true)
	public void deleteImportedChallenges();

	// @formatter:off
	@Query(value = 
			"	SELECT *, (successes / (successes + failures)) as successFailureRatio FROM (" + 
			"	 SELECT c.*, SUM(case when cs.state = 1 then 1 else 0 end) as successes, SUM(case when cs.state = 2 then 1 else 0 end) as failures FROM challenges AS c" + 
			"	 LEFT JOIN challengestatus AS cs ON cs.challenge_id = c.id" + 
			"    WHERE c.deleted_at IS NULL AND c.kind = 'self' AND c.created_by_import AND (:category IS NULL OR c.category = :category) AND (c.repeatable_after_days IS NOT NULL OR c.id NOT IN (SELECT challenge_id FROM challengestatus WHERE user_id = :userId AND state != 0)" +
			"   )" + 
			"	GROUP by c.id) as i" + 
			"	ORDER BY successFailureRatio DESC", nativeQuery = true)
	public Slice<Challenge> getChallengesForStream(String category, long userId, Pageable pageable);
	// @formatter:on
}