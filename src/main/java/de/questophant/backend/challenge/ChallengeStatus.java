package de.questophant.backend.challenge;

import java.time.Instant;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;

@Entity
@Table(name = "challenge_status", indexes = {@Index(name = "userIdIndex", columnList = "userId", unique = false), @Index(name = "challengeIdIndex", columnList = "challengeId", unique = false)})
public class ChallengeStatus {

	public enum State {
		ongoing, finished_success, finished_failure
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	private long userId;
	private long challengeId;

	private State state;
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

	public State getState() {
		return state;
	}
	public void setState(State state) {
		this.state = state;
	}
	public Instant getTimeStamp() {
		return timeStamp;
	}
	public void setTimeStamp(Instant timeStamp) {
		this.timeStamp = timeStamp;
	}

}
