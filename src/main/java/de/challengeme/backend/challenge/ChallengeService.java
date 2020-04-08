package de.challengeme.backend.challenge;

import java.time.Instant;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.base.Preconditions;

import de.challengeme.backend.user.User;

@Service
public class ChallengeService {

	@Autowired
	private ChallengeRepository challengeRepository;

	@Autowired
	private ChallengeResultRepository challengeResultRepository;

	Challenge dailyChallenge;

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

	public Challenge getDailyChallenge() {
		if (dailyChallenge == null) {
			dailyChallenge = challengeRepository.getRandomChallenge();
		}
		return dailyChallenge;
	}

	public List<Challenge> list() {
		return challengeRepository.findAll();
	}

	public void createChallenge(User user, Challenge challenge) {
		challenge.setCreatedAt(Instant.now());
		challenge.setCreatedByUserId(user.getId());
		challengeRepository.saveAndFlush(challenge);
	}

	public void markChallengeAsDeleted(User user, long challengeId) {
		Challenge challenge = challengeRepository.getOne(challengeId);
		Preconditions.checkNotNull(challenge, "No challenge found with that id.");
		if (challenge.getCreatedByUserId() == user.getId()) {
			challenge.setDeleted(true);
			challengeRepository.saveAndFlush(challenge);
		} else {
			throw new IllegalArgumentException("Challenge was not created by this user.");
		}
	}
}
