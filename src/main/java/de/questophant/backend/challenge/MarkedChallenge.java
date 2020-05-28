package de.questophant.backend.challenge;

import java.time.Instant;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;

@Entity
@Table(name = "marked_challenges", indexes = {@Index(name = "userIndex", columnList = "userId", unique = false), @Index(name = "challengeIndex", columnList = "challengeId", unique = false)})
public class MarkedChallenge {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	private long userId;
	private long challengeId;
	private Instant timeStamp;
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public long getUserId() {
		return userId;
	}
	public void setUserId(long userId) {
		this.userId = userId;
	}
	public long getChallengeId() {
		return challengeId;
	}
	public void setChallengeId(long challengeId) {
		this.challengeId = challengeId;
	}
	public Instant getTimeStamp() {
		return timeStamp;
	}
	public void setTimeStamp(Instant timeStamp) {
		this.timeStamp = timeStamp;
	}
}
