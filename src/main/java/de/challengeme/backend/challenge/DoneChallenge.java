package de.challengeme.backend.challenge;

import java.time.Instant;

import javax.persistence.Entity;

import io.swagger.annotations.ApiModelProperty;

@Entity
public class DoneChallenge extends Challenge {

	@ApiModelProperty(required = false, notes = "field is read-only and contains the time stamp when the challenge has been done")
	private Instant doneAt;

	public Instant getDoneAt() {
		return doneAt;
	}

	public void setDoneAt(Instant doneAt) {
		this.doneAt = doneAt;
	}

}
