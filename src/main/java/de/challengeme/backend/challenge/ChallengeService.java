package de.challengeme.backend.challenge;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Service;

import com.google.common.base.Preconditions;

import de.challengeme.backend.timer.TimerService;
import de.challengeme.backend.timer.TimerType;
import de.challengeme.backend.user.MyUser;

@Service
public class ChallengeService {

	@Autowired
	private ChallengeRepository challengeRepository;

	@Autowired
	private ChallengeStatusRepository challengeResultRepository;

	@Autowired
	private IgnoredChallengesRepository ignoredChallengesRepository;

	@Autowired
	private MarkedChallengesRepository markedChallengesRepository;

	@Autowired
	private TimerService timerService;

	@PersistenceContext
	private EntityManager em;

	public void deleteChallenge(long challengeId) {
		challengeRepository.deleteChallenge(challengeId);
	}

	public void deleteImportedChallenges() {
		challengeRepository.deleteImportedChallenges();
	}

	public List<Challenge> getChallengesCreatedByUser(MyUser user) {
		return challengeRepository.getChallengesCreatedByUser(user.getId());
	}

	public Slice<Challenge> getChallengesCreatedByUser(MyUser user, Pageable pageable) {
		return challengeRepository.getChallengesCreatedByUser(user.getId(), pageable);
	}

	public Slice<Challenge> getChallengesIgnoredByUser(MyUser user, Pageable pageable) {
		return challengeRepository.getChallengesIgnoredByUser(user.getId(), pageable);
	}

	public Slice<Challenge> getChallengesMarkedByUser(MyUser user, Pageable pageable) {
		return challengeRepository.getChallengesMarkedByUser(user.getId(), pageable);
	}

	@SuppressWarnings("unchecked")
	public Slice<OnGoingChallenge> getChallengesOngoingByUser(MyUser user, Pageable pageable) {

		// @formatter:off
		Query query = em.createNativeQuery( 
				" SELECT c.*, cs.time_stamp started_at FROM challenges AS c" + 
				" RIGHT JOIN (" +
				"              SELECT challenge_id, time_stamp FROM (" + 
				"	                 SELECT challenge_id, state, row_number() OVER (PARTITION BY challenge_id ORDER BY time_stamp DESC) as row_num, time_stamp " +
				"                     FROM challenge_status" + 
				"                     WHERE user_id = :userId" +
				"              ) as i" + 
				"              WHERE row_num = 1 AND state = 0 " +
				"       ) AS cs" +
				" ON cs.challenge_id = c.id" +
				" WHERE c.deleted_at IS NULL" +
				" ORDER BY cs.time_stamp", OnGoingChallenge.class);
		// @formatter:on

		query.setParameter("userId", user.getId());

		return new SliceImpl<>(query.getResultList(), pageable, true);
	}

	@SuppressWarnings("unchecked")
	public Slice<DoneChallenge> getChallengesDoneByUser(MyUser user, Pageable pageable) {

		// @formatter:off
		Query query = em.createNativeQuery( 
				" SELECT c.*, cs.time_stamp done_at FROM challenges AS c" + 
				" RIGHT JOIN (" + 
				"	                 SELECT challenge_id, time_stamp " +
				"                    FROM challenge_status" + 
				"                    WHERE user_id = :userId AND state != 0" +
				"            ) AS cs" +
				" ON cs.challenge_id = c.id" +
				" WHERE c.deleted_at IS NULL" +
				" ORDER BY cs.time_stamp", DoneChallenge.class);
		// @formatter:on

		query.setParameter("userId", user.getId());

		return new SliceImpl<>(query.getResultList(), pageable, true);
	}

	public Challenge getRandomChallenge(Category category, MyUser user) {
		return challengeRepository.getRandomChallenge(category.toString(), user.getId());
	}

	public Challenge getRandomChallenge(Category category) {
		return challengeRepository.getRandomChallenge(category.toString());
	}

	public List<Challenge> getImportedChallenges() {
		return challengeRepository.getImportedChallenges();
	}

	public Challenge getImportedChallengeFromTitle(String title) {
		return challengeRepository.getImportedChallengeFromTitle(title);
	}

	public Slice<Challenge> getChallengesForStream(Category category, MyUser user, Pageable pageable) {
		return challengeRepository.getChallengesForStream(category == null ? null : category.toString(), user.getId(), pageable);
	}

	/**
	 * Needs database locking if more than one backend is in use.
	 * 
	 * @return
	 */
	public Challenge getDailyChallenge() {
		Challenge result = null;
		Long challengeId = timerService.getLinkedObjectOfTimer(TimerType.CHALLENGE);

		if (challengeId == null) {
			result = challengeRepository.getRandomChallenge();
			Instant validUntil = LocalDate.now().atStartOfDay().plusDays(1).toInstant(ZoneOffset.UTC);
			timerService.setTimer(TimerType.CHALLENGE, validUntil, result.getId());
		} else {
			result = challengeRepository.getChallengeFromId(challengeId);
		}
		return result;
	}

	public List<Challenge> list() {
		return challengeRepository.findAll();
	}

	public void createChallenge(MyUser user, Challenge challenge) {
		challenge.setCreatedAt(Instant.now());
		challenge.setCreatedByPublicUserId(user.getPublicUserId());
		challengeRepository.saveAndFlush(challenge);
	}

	public Challenge markChallengeAsDeleted(MyUser user, long challengeId) {
		Challenge challenge = challengeRepository.getOne(challengeId);
		Preconditions.checkNotNull(challenge, "No challenge found with that id.");
		if (user.getPublicUserId().equals(challenge.getCreatedByPublicUserId())) {
			challenge.setDeletedAt(Instant.now());
			challengeRepository.saveAndFlush(challenge);
			return challenge;
		} else {
			throw new IllegalArgumentException("Challenge was not created by this user.");
		}
	}

	public Challenge getChallengeFromId(long challengeId) {
		return challengeRepository.getChallengeFromId(challengeId);
	}

	public ChallengeWithStatus getChallengeWithStatusFromId(MyUser user, long challengeId) {

		// @formatter:off
		Query query = em.createNativeQuery( " SELECT *, " +
				       "        (SELECT COUNT(*) ongoing FROM marked_challenges WHERE user_id = :userId AND challenge_id = :challengeId) marked," +
				       "        (CASE WHEN (SELECT state FROM challenge_status WHERE user_id = :userId AND challenge_id = :challengeId ORDER BY time_stamp DESC LIMIT 1) = 0 THEN 1 ELSE 0 END) ongoing" +
				       " FROM challenges WHERE id = :challengeId", ChallengeWithStatus.class);
		// @formatter:on

		query.setParameter("userId", user.getId());
		query.setParameter("challengeId", challengeId);

		for (Object object : query.getResultList()) {
			return (ChallengeWithStatus) object;
		}

		return null;
	}

	public void save(Challenge challenge) {
		challengeRepository.saveAndFlush(challenge);
	}

	public void save(ChallengeStatus challengeResult) {
		challengeResultRepository.saveAndFlush(challengeResult);
	}

	public void setChallengeIgnoredByUser(MyUser user, Challenge challenge, boolean ignored) {
		ignoredChallengesRepository.delete(user.getId(), challenge.getId());

		if (ignored) {
			IgnoredChallenge ignoredChallenge = new IgnoredChallenge();
			ignoredChallenge.setUserId(user.getId());
			ignoredChallenge.setChallengeId(challenge.getId());
			ignoredChallenge.setTimeStamp(Instant.now());
			ignoredChallengesRepository.saveAndFlush(ignoredChallenge);
		}
	}

	public void setChallengeMarkedByUser(MyUser user, Challenge challenge, boolean marked) {
		markedChallengesRepository.delete(user.getId(), challenge.getId());

		if (marked) {
			MarkedChallenge markedChallenge = new MarkedChallenge();
			markedChallenge.setUserId(user.getId());
			markedChallenge.setChallengeId(challenge.getId());
			markedChallenge.setTimeStamp(Instant.now());
			markedChallengesRepository.saveAndFlush(markedChallenge);
		}
	}
}
