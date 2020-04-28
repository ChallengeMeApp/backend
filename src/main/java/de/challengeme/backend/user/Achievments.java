package de.challengeme.backend.user;

import java.util.List;
import java.util.Map;

import de.challengeme.backend.challenge.Category;

public class Achievments {

	public static class Achievment {

		private String name;
		private String imageUrl;
		private boolean achieved;
		public Achievment(String name, String imageUrl, boolean achieved) {
			super();
			this.name = name;
			this.imageUrl = imageUrl;
		}
		public String getName() {
			return name;
		}
		public String getImageUrl() {
			return imageUrl;
		}
		public boolean isAchieved() {
			return achieved;
		}

	}

	private Map<Category, List<Achievment>> achievmentsByCategory;
	private List<Achievment> overalLevel;

	public Map<Category, List<Achievment>> getAchievmentsByCategory() {
		return achievmentsByCategory;
	}
	public void setAchievmentsByCategory(Map<Category, List<Achievment>> achievmentsByCategory) {
		this.achievmentsByCategory = achievmentsByCategory;
	}
	public List<Achievment> getOveralLevel() {
		return overalLevel;
	}
	public void setOveralLevel(List<Achievment> overalLevel) {
		this.overalLevel = overalLevel;
	}

}
