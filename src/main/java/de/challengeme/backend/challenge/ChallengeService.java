package de.challengeme.backend.challenge;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

import com.google.common.base.Preconditions;

import de.challengeme.backend.timer.TimerService;
import de.challengeme.backend.timer.TimerType;
import de.challengeme.backend.user.User;

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

	public void deleteChallenge(long challengeId) {
		challengeRepository.deleteChallenge(challengeId);
	}

	public void deleteImportedChallenges() {
		challengeRepository.deleteImportedChallenges();
	}

	public List<Challenge> getChallengesCreatedByUser(User user) {
		return challengeRepository.getChallengesCreatedByUser(user.getId());
	}

	public Slice<Challenge> getChallengesCreatedByUser(User user, Pageable pageable) {
		return challengeRepository.getChallengesCreatedByUser(user.getId(), pageable);
	}

	public Slice<Challenge> getChallengesIgnoredByUser(User user, Pageable pageable) {
		return challengeRepository.getChallengesIgnoredByUser(user.getId(), pageable);
	}

	public Slice<Challenge> getChallengesMarkedByUser(User user, Pageable pageable) {
		return challengeRepository.getChallengesMarkedByUser(user.getId(), pageable);
	}

	public Slice<OnGoingChallenge> getChallengesOngoingByUser(User user, Pageable pageable) {
		return challengeRepository.getChallengesOngoingByUser(user.getId(), pageable);
	}

	public Slice<DoneChallenge> getChallengesDoneByUser(User user, Pageable pageable) {
		return challengeRepository.getChallengesDoneByUser(user.getId(), pageable);
	}

	public Challenge getRandomChallenge(Category category, User user) {
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

	public Slice<Challenge> getChallengesForStream(Category category, User user, Pageable pageable) {
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

	public void createChallenge(User user, Challenge challenge) {
		challenge.setCreatedAt(Instant.now());
		challenge.setCreatedByUserId(user.getId());
		challengeRepository.saveAndFlush(challenge);
	}

	public Challenge markChallengeAsDeleted(User user, long challengeId) {
		Challenge challenge = challengeRepository.getOne(challengeId);
		Preconditions.checkNotNull(challenge, "No challenge found with that id.");
		if (challenge.getCreatedByUserId() == user.getId()) {
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

	public void save(Challenge challenge) {
		challengeRepository.saveAndFlush(challenge);
	}

	public void save(ChallengeStatus challengeResult) {
		challengeResultRepository.saveAndFlush(challengeResult);
	}

	public void setChallengeIgnoredByUser(User user, Challenge challenge, boolean ignored) {
		ignoredChallengesRepository.delete(user.getId(), challenge.getId());

		if (ignored) {
			IgnoredChallenge ignoredChallenge = new IgnoredChallenge();
			ignoredChallenge.setUserId(user.getId());
			ignoredChallenge.setChallengeId(challenge.getId());
			ignoredChallenge.setTimeStamp(Instant.now());
			ignoredChallengesRepository.saveAndFlush(ignoredChallenge);
		}
	}

	public void setChallengeMarkedByUser(User user, Challenge challenge, boolean marked) {
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
