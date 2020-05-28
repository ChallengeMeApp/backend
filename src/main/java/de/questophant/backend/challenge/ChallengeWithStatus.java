package de.questophant.backend.challenge;

import javax.persistence.Entity;

import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.Subselect;

import io.swagger.annotations.ApiModelProperty;

@Entity
@Immutable
@Subselect("SELECT 'ignoreme';")
public class ChallengeWithStatus extends ChallengePrototype {

	@ApiModelProperty(required = false, notes = "field is read-only and tells if the challenge is currently being done by the user")
	private boolean ongoing;

	@ApiModelProperty(required = false, notes = "field is read-only and tells if the challenge is currently being marked by the user")
	private boolean marked;

	public boolean isOngoing() {
		return ongoing;
	}

	public boolean isMarked() {
		return marked;
	}

}
