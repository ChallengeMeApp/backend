package de.challengeme.backend.user;

import javax.persistence.Entity;
import javax.persistence.Id;

import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.Subselect;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Immutable
@Subselect("SELECT 'ignoreme';")
public class Points {

	@Id
	@JsonIgnore
	protected long id;

	private long points;

	public long getPoints() {
		return points;
	}

	public void setPoints(long points) {
		this.points = points;
	}

}
