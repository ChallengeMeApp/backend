package de.challengeme.backend.challenge;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface ChallengeRepository extends JpaRepository<Challenge, Long> {

	@Query(value = "SELECT * FROM challenges WHERE deleted_at IS NULL AND kind = 'self' AND created_by_import = true ORDER BY rand() LIMIT 1", nativeQuery = true)
	public Challenge getRandomChallenge();

	@Query(value = "SELECT * FROM challenges WHERE created_by_import = true AND title = :title", nativeQuery = true)
	public Challenge getImportedChallengeFromTitle(String title);

	@Query(value = "SELECT * FROM challenges WHERE created_by_import = true", nativeQuery = true)
	public List<Challenge> getImportedChallenges();

	@Query(value = "SELECT * FROM challenges WHERE id = :id", nativeQuery = true)
	public Challenge getChallengeFromId(long id);

	@Query(value = "SELECT * FROM challenges WHERE deleted_at IS NULL AND created_by_public_user_id=UUID_TO_BIN(:publicUserId) ORDER BY created_at", nativeQuery = true)
	public Slice<Challenge> getChallengesCreatedByUser(String publicUserId, Pageable pageable);

	// @formatter:off
	@Query(value = " SELECT c.* FROM ignored_challenges as ic" + 
	               " LEFT JOIN challenges AS c ON ic.challenge_id = c.id" +
	               " WHERE c.deleted_at IS NULL AND ic.user_id = :userId" + 
	               " ORDER BY time_stamp", nativeQuery = true)
	public Slice<Challenge> getChallengesIgnoredByUser(long userId, Pageable pageable);
	// @formatter:on

	// @formatter:off
	@Query(value = " SELECT c.* FROM marked_challenges as mc" + 
	               " LEFT JOIN challenges AS c ON mc.challenge_id = c.id" +
	               " WHERE c.deleted_at IS NULL AND mc.user_id = :userId" + 
	               " ORDER BY time_stamp", nativeQuery = true)
	public Slice<Challenge> getChallengesMarkedByUser(long userId, Pageable pageable);
	// @formatter:on

	@Modifying
	@Transactional
	@Query(value = "DELETE FROM challenges WHERE id = :challengeId", nativeQuery = true)
	public void deleteChallenge(long challengeId);

	// @formatter:off
	@Query(value = 
			"	SELECT *, (successes / (successes + failures)) as successFailureRatio FROM (" + 
			"	 SELECT c.*, SUM(case when cs.state = 1 then 1 else 0 end) as successes, SUM(case when cs.state = 2 then 1 else 0 end) as failures FROM challenges AS c" + 
			"	 LEFT JOIN challenge_status AS cs ON cs.challenge_id = c.id" + 
			"    WHERE c.deleted_at IS NULL AND c.kind = 'self' AND c.in_distribution AND (:category IS NULL OR c.category = :category) AND" +
			"          (c.repeatable_after_days IS NOT NULL OR c.id NOT IN (SELECT challenge_id FROM challenge_status WHERE user_id = :userId AND state != 0)) AND" +
			"          c.id NOT IN (SELECT challenge_id FROM ignored_challenges WHERE user_id = :userId)" +
			"    GROUP by c.id"+
			"  ) as i" + 
			"  ORDER BY successFailureRatio DESC", nativeQuery = true)
	public Slice<Challenge> getChallengesForStream(String category, long userId, Pageable pageable);
	// @formatter:on
}