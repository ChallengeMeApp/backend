package de.challengeme.backend.controller.prototype;

public class CreateChallengeBody {
	private String title;
	private String description;
	private String category;
	private long durationSeconds;
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
	public String getCategory() {
		return category;
	}
	public void setCategory(String category) {
		this.category = category;
	}
	public long getDurationSeconds() {
		return durationSeconds;
	}
	public void setDurationSecondss(long durationSeconds) {
		this.durationSeconds = durationSeconds;
	}

}
