package de.challengeme.backend.challenge;

import java.time.Instant;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;

@Entity
@Table(name = "challenges", indexes = {@Index(name = "challengeCategoryIndex", columnList = "category", unique = false), @Index(name = "challengeCreatedByUserIdIndex", columnList = "createdByUserId", unique = false)})
public class Challenge {

	private long id;
	private long createdByUserId;
	private String title;
	private String description;
	private long durationSeconds;
	private String category;
	private Instant createdAt;
	private boolean deleted;
	private boolean createdByImport;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}

	@Column(columnDefinition = "VARCHAR(2048)")
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public long getCreatedByUserId() {
		return createdByUserId;
	}
	public void setCreatedByUserId(long createdByUserId) {
		this.createdByUserId = createdByUserId;
	}
	public long getDurationSeconds() {
		return durationSeconds;
	}
	public void setDurationSeconds(long durationSeconds) {
		this.durationSeconds = durationSeconds;
	}

	public String getCategory() {
		return category;
	}
	public void setCategory(String category) {
		this.category = category;
	}
	public Instant getCreatedAt() {
		return createdAt;
	}
	public void setCreatedAt(Instant createdAt) {
		this.createdAt = createdAt;
	}
	public boolean isDeleted() {
		return deleted;
	}
	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}
	public boolean isCreatedByImport() {
		return createdByImport;
	}
	public void setCreatedByImport(boolean createdByImport) {
		this.createdByImport = createdByImport;
	}

}
