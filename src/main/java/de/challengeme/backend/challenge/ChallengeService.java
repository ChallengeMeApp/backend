package de.challengeme.backend.challenge;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
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
	private ChallengeResultRepository challengeResultRepository;

	@Autowired
	private TimerService timerService;

	public void deleteImportedChallenges() {
		challengeRepository.deleteImportedChallenges();
	}

	public List<Challenge> getChallengesCreatedByUser(User user) {
		return challengeRepository.getChallengesCreatedByUser(user.getId());
	}

	public Challenge getRandomChallenge(Category category, User user) {
		return challengeRepository.getRandomChallenge(category.toString(), user.getId());
	}

	public Challenge getRandomChallenge(Category category) {
		return challengeRepository.getRandomChallenge(category.toString());
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
}
