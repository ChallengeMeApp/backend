package de.challengeme.backend.challenge;

import javax.persistence.Entity;

import org.hibernate.annotations.Subselect;

import io.swagger.annotations.ApiModelProperty;

@Entity
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

	public void setOngoing(boolean ongoing) {
		this.ongoing = ongoing;
	}

	public void setMarked(boolean marked) {
		this.marked = marked;
	}

}
