package de.challengeme.backend.challenge;

import java.time.Instant;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;

@Entity
@Table(name = "challenges", indexes = {@Index(name = "challengeCategoryIndex", columnList = "category", unique = false), @Index(name = "challengeCreatedByUserIdIndex", columnList = "createdByUserId", unique = false)})
public class Challenge {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	private long createdByUserId;
	private String title;

	@Column(columnDefinition = "VARCHAR(2048)")
	private String description;
	private Long durationSeconds;

	@Enumerated(EnumType.STRING)
	@Column(columnDefinition = "VARCHAR(16)")
	private Category category;

	@Enumerated(EnumType.STRING)
	@Column(columnDefinition = "VARCHAR(16)")
	private ChallengeKind kind;

	private Instant createdAt;
	private Instant deletedAt;
	private boolean createdByImport;
	private Integer repeatableAfterDays;
	private String material;

	private int pointsWin;
	private int pointsLoose;
	private int pointsParticipation;

	private boolean addToTreasureChest;

	public int getPointsWin() {
		return pointsWin;
	}
	public void setPointsWin(int pointsWin) {
		this.pointsWin = pointsWin;
	}
	public int getPointsLoose() {
		return pointsLoose;
	}
	public void setPointsLoose(int pointsLoose) {
		this.pointsLoose = pointsLoose;
	}
	public int getPointsParticipation() {
		return pointsParticipation;
	}
	public void setPointsParticipation(int pointsParticipation) {
		this.pointsParticipation = pointsParticipation;
	}
	public boolean isAddToTreasureChest() {
		return addToTreasureChest;
	}
	public void setAddToTreasureChest(boolean addToTreasureChest) {
		this.addToTreasureChest = addToTreasureChest;
	}
	public String getMaterial() {
		return material;
	}
	public void setMaterial(String material) {
		this.material = material;
	}

	public Integer getRepeatableAfterDays() {
		return repeatableAfterDays;
	}
	public void setRepeatableAfterDays(Integer repeatableAfterDays) {
		this.repeatableAfterDays = repeatableAfterDays;
	}
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public ChallengeKind getKind() {
		return kind;
	}
	public void setKind(ChallengeKind kind) {
		this.kind = kind;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}

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

	public Long getDurationSeconds() {
		return durationSeconds;
	}
	public void setDurationSeconds(Long durationSeconds) {
		this.durationSeconds = durationSeconds;
	}
	public Category getCategory() {
		return category;
	}
	public void setCategory(Category category) {
		this.category = category;
	}
	public Instant getCreatedAt() {
		return createdAt;
	}
	public void setCreatedAt(Instant createdAt) {
		this.createdAt = createdAt;
	}

	public Instant getDeletedAt() {
		return deletedAt;
	}
	public void setDeletedAt(Instant deletedAt) {
		this.deletedAt = deletedAt;
	}
	public boolean isCreatedByImport() {
		return createdByImport;
	}
	public void setCreatedByImport(boolean createdByImport) {
		this.createdByImport = createdByImport;
	}

}
