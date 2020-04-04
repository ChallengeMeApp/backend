package de.challengeme.backend.challenge;

import java.time.Instant;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;

@Entity
@Table(name = "challengeresults", indexes = {@Index(name = "userIndex", columnList = "userId", unique = false), @Index(name = "challengeIndex", columnList = "challengeId", unique = false)})
public class ChallengeResult {
	private long id;
	private long userId;
	private long challengeId;
	private boolean success;
	private Instant timeStamp;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
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
	public boolean isSuccess() {
		return success;
	}
	public void setSuccess(boolean success) {
		this.success = success;
	}
	public Instant getTimeStamp() {
		return timeStamp;
	}
	public void setTimeStamp(Instant timeStamp) {
		this.timeStamp = timeStamp;
	}

}
