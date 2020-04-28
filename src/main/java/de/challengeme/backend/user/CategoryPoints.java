package de.challengeme.backend.user;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;

import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.Subselect;

import com.fasterxml.jackson.annotation.JsonIgnore;

import de.challengeme.backend.challenge.Category;

@Entity
@Immutable
@Subselect("SELECT 'ignoreme';")
public class CategoryPoints {

	@Id
	@JsonIgnore
	protected long id;

	@Enumerated(EnumType.STRING)
	private Category category;

	private long points;

	public long getPoints() {
		return points;
	}

	public Category getCategory() {
		return category;
	}

}
