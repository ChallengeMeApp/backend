package de.challengeme.backend.challenge;

import java.time.Instant;

import javax.persistence.Entity;

import io.swagger.annotations.ApiModelProperty;

@Entity
public class OnGoingChallenge extends Challenge {

	@ApiModelProperty(required = false, notes = "field is read-only and contains the time stamp when the challenge has been started")
	private Instant startedAt;

	public Instant getStartedAt() {
		return startedAt;
	}

	public void setStartedAt(Instant startedAt) {
		this.startedAt = startedAt;
	}

}
