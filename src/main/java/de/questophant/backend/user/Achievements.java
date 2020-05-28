package de.questophant.backend.user;

import java.util.List;
import java.util.Map;

import de.questophant.backend.challenge.Category;

public class Achievements {

	public static class Achievement {

		private String name;
		private String imageUrl;
		private boolean achieved;
		public Achievement(String name, String imageUrl, boolean achieved) {
			super();
			this.name = name;
			this.imageUrl = imageUrl;
			this.achieved = achieved;
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
		@Override
		public String toString() {
			return "Achievement [name=" + name + ", imageUrl=" + imageUrl + ", achieved=" + achieved + "]";
		}

	}

	private Map<Category, List<Achievement>> achievementsByCategory;
	private List<Achievement> overalLevel;

	public Map<Category, List<Achievement>> getAchievementsByCategory() {
		return achievementsByCategory;
	}
	public void setAchievementsByCategory(Map<Category, List<Achievement>> achievementsByCategory) {
		this.achievementsByCategory = achievementsByCategory;
	}
	public List<Achievement> getOveralLevel() {
		return overalLevel;
	}
	public void setOveralLevel(List<Achievement> overalLevel) {
		this.overalLevel = overalLevel;
	}

}
