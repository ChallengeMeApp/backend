package de.questophant.backend.challenge;

import java.time.Instant;

import javax.persistence.Entity;

import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.Subselect;

import io.swagger.annotations.ApiModelProperty;

@Entity
@Immutable
@Subselect("SELECT 'ignoreme';")
public class OnGoingChallenge extends ChallengePrototype {

	@ApiModelProperty(required = false, notes = "field is read-only and contains the time stamp when the challenge has been started")
	private Instant startedAt;

	public Instant getStartedAt() {
		return startedAt;
	}

}
