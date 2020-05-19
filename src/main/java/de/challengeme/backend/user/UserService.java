package de.challengeme.backend.user;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import de.challengeme.backend.Helper;
import de.challengeme.backend.challenge.Category;
import de.challengeme.backend.user.Achievements.Achievement;

@Service
public class UserService {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private ContactListRepository contactListRepository;

	@PersistenceContext
	private EntityManager em;

	public Points getUserPoints(UserPrototype user) {

		// @formatter:off
		Query query = em.createNativeQuery( " SELECT 0 id, IFNULL(SUM(u.points),0) points FROM" + 
											" (" + 
											"	SELECT SUM(c.points_win) points FROM challenges AS c" + 
											"	RIGHT JOIN challenge_status AS cs ON c.id = cs.challenge_id" + 
											"	WHERE cs.user_id = :userId AND cs.state = 1" + 
											"	UNION ALL" + 
											"	SELECT SUM(c.points_loose) points FROM challenges AS c" + 
											"	RIGHT JOIN challenge_status AS cs ON c.id = cs.challenge_id" + 
											"	WHERE cs.user_id = :userId AND cs.state = 2" + 
											" ) AS u;", Points.class);
		// @formatter:on

		query.setParameter("userId", user.getId());

		for (Object object : query.getResultList()) {
			return (Points) object;
		}

		return null;
	}

	@SuppressWarnings("unchecked")
	public List<CategoryPoints> getUserPointsPerCategory(UserPrototype user) {

		// @formatter:off
		Query query = em.createNativeQuery( " SELECT 0 id, IFNULL(SUM(points),0) points, category FROM" + 
											" (" + 
											"	SELECT SUM(c.points_win) points, c.category FROM challenges AS c" + 
											"	RIGHT JOIN challenge_status AS cs ON c.id = cs.challenge_id" + 
											"	WHERE cs.user_id = :userId AND cs.state = 1" + 
											"	GROUP BY c.category" + 
											"	UNION ALL" + 
											"	SELECT SUM(c.points_loose) points, c.category FROM challenges AS c" + 
											"	RIGHT JOIN challenge_status AS cs ON c.id = cs.challenge_id" + 
											"	WHERE cs.user_id = :userId AND cs.state = 2" +
											"	GROUP BY c.category" +
											" ) AS u"+
											" GROUP BY category", CategoryPoints.class);
		// @formatter:on

		query.setParameter("userId", user.getId());

		return query.getResultList();
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	public Achievements getUserAchievements(UserPrototype user, String imageUrlPrefix) throws IOException {

		String NOT_ACHIEVED_IMAGE_NAME = imageUrlPrefix + "Fragezeichen";

		Map<String, Object> predefinedAchievements;
		ObjectMapper mapper = new ObjectMapper();
		try (InputStream is = getClass().getResourceAsStream("/achievements.json")) {
			predefinedAchievements = mapper.readValue(is, Map.class);
		}

		Map<String, Map> pAByCategory = Helper.getAsMap(predefinedAchievements.get("byCategory"));

		Map<Category, List<Achievement>> achievementsByCategory = Maps.newHashMap();
		for (CategoryPoints cp : getUserPointsPerCategory(user)) {
			Map<String, Object> pAList = pAByCategory.get(cp.getCategory().toString());
			pAList.put("points", cp.getPoints());
		}

		Map<Integer, Integer> totalLevels = Maps.newHashMap();
		for (Entry<String, Map> entry : pAByCategory.entrySet()) {

			Category category = Category.valueOf(entry.getKey());
			List<Achievement> achievements = Lists.newArrayList();

			Map<String, Object> pAList = entry.getValue();
			Long points = (Long) pAList.get("points");
			if (points == null) {
				points = 0l;
			}

			int level = 0;
			boolean lastWasAchieved = true;
			for (Entry<String, Object> entryPA : pAList.entrySet()) {
				if (entryPA.getValue() instanceof Map) {
					Map paMap = Helper.getAsMap(entryPA.getValue());
					String name = Helper.getAsString(paMap.get("name"), "");
					String imageUrl = Helper.getAsString(paMap.get("image"), "");

					if (imageUrl.length() > 0) {
						imageUrl = imageUrlPrefix + imageUrl;
					} else {
						imageUrl = NOT_ACHIEVED_IMAGE_NAME; // no image yet
					}

					if (Long.parseLong(entryPA.getKey()) <= points) {
						achievements.add(new Achievement(name, imageUrl, true));
						level++;
					} else {
						achievements.add(new Achievement(lastWasAchieved ? name : "", lastWasAchieved ? imageUrl : NOT_ACHIEVED_IMAGE_NAME, false));
						lastWasAchieved = false;
					}
				}
			}

			totalLevels.put(level, totalLevels.getOrDefault(level, 0) + 1);
			achievementsByCategory.put(category, achievements);
		}

		List<Achievement> overalLevel = Lists.newArrayList();
		List<Map> pAOverall = Helper.getAsList(predefinedAchievements.get("overall"));
		boolean lastWasAchieved = true;
		for (Map overallMap : pAOverall) {
			List<Map> minLevels = Helper.getAsList(overallMap.get("minLevels"));
			boolean conditionsMatch = true;
			for (Map minLevelMap : minLevels) {
				Integer level = Helper.getAsInteger(minLevelMap.get("level"), 0);
				Integer requiredAmount = Helper.getAsInteger(minLevelMap.get("amount"), Category.values().length);

				if (totalLevels.getOrDefault(level, 0) < requiredAmount) {
					conditionsMatch = false;
					break;
				}
			}

			String name = Helper.getAsString(overallMap.get("name"), "");
			String imageUrl = Helper.getAsString(overallMap.get("image"), "");

			if (imageUrl.length() > 0) {
				imageUrl = imageUrlPrefix + imageUrl;
			} else {
				imageUrl = NOT_ACHIEVED_IMAGE_NAME; // no image yet
			}

			if (conditionsMatch) {
				overalLevel.add(new Achievement(name, imageUrl, true));
			} else {
				overalLevel.add(new Achievement(lastWasAchieved ? name : "", lastWasAchieved ? imageUrl : NOT_ACHIEVED_IMAGE_NAME, false));
				lastWasAchieved = false;
			}
		}

		Achievements achievements = new Achievements();
		achievements.setAchievementsByCategory(achievementsByCategory);
		achievements.setOveralLevel(overalLevel);
		return achievements;
	}

	public int getCountOfAdminUsers() {
		return userRepository.getCountOfAdminUsers();
	}

	public synchronized MyUser createUser() {
		MyUser user = new MyUser();
		user.setPrivateUserId(UUID.randomUUID());
		user.setPublicUserId(UUID.randomUUID());
		user.setCreatedAt(Instant.now());
		userRepository.saveAndFlush(user);
		return user;
	}

	public MyUser getUserByUserName(String userName) {
		return userRepository.getByUserName(userName);
	}

	public MyUser getUserByPrivateUserId(UUID userId) {
		return getUserByPrivateUserId(userId == null ? null : userId.toString());
	}

	public MyUser getUserByPrivateUserId(String privateUserId) {
		MyUser user = privateUserId == null || privateUserId.length() != 36 ? null : userRepository.getByPrivateUserId(privateUserId);
		if (user != null) {
			user.setLastRequestAt(Instant.now());
			userRepository.save(user);
		}
		return user;
	}

	public MyUser getRootUser() {
		return userRepository.getRootUser();
	}

	public void save(MyUser user) {
		userRepository.saveAndFlush(user);
	}

	public String getUserNameFromId(long id) {
		Optional<MyUser> o = userRepository.findById(id);
		if (o.isPresent()) {
			return o.get().getUserName();
		}
		return null;
	}

	public PublicUser getPublicUserByUserId(UUID publicUserId) {
		return getPublicUserByUserId(publicUserId == null ? null : publicUserId.toString());
	}

	public PublicUser getPublicUserByUserId(String publicUserId) {

		if (publicUserId == null || publicUserId.length() != 36) {
			return null;
		}

		// @formatter:off
		Query query = em.createNativeQuery( "SELECT * FROM users WHERE public_user_id = UUID_TO_BIN(:publicUserId)", PublicUser.class);
		// @formatter:on

		query.setParameter("publicUserId", publicUserId);

		for (Object o : query.getResultList()) {
			return (PublicUser) o;
		}
		return null;
	}

	public PublicUser getPublicUserByUserName(String publicUserName) {

		if (Strings.isNullOrEmpty(publicUserName)) {
			return null;
		}

		// @formatter:off
		Query query = em.createNativeQuery( "SELECT * FROM users WHERE user_name = :userName", PublicUser.class);
		// @formatter:on

		query.setParameter("userName", publicUserName);

		for (Object o : query.getResultList()) {
			return (PublicUser) o;
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public Slice<PublicUser> getContactList(MyUser user, Pageable pageable) {

		// @formatter:off
		Query query = em.createNativeQuery( "SELECT * FROM users WHERE id IN (SELECT contact_id FROM contacts WHERE user_id = :userId)", PublicUser.class);
		// @formatter:on

		query.setParameter("userId", user.getId());

		return new SliceImpl<>(query.getResultList(), pageable, true);
	}

	public boolean isOnContactList(MyUser myUser, PublicUser publicUser) {

		// @formatter:off
		Query query = em.createNativeQuery( "SELECT COUNT(contact_id) FROM contacts WHERE user_id = :userId AND contact_id = :contactId");
		// @formatter:on

		query.setParameter("userId", myUser.getId());
		query.setParameter("contactId", publicUser.getId());

		return ((Number) query.getSingleResult()).intValue() > 0;
	}

	public void addToContactList(MyUser myUser, PublicUser publicUser) {
		ContactListEntry contactListEntry = new ContactListEntry();
		contactListEntry.setUserId(myUser.getId());
		contactListEntry.setContactId(publicUser.getId());
		contactListRepository.saveAndFlush(contactListEntry);
	}

	@Transactional
	@Modifying
	public void removeFromContactList(MyUser myUser, PublicUser publicUser) {
		// @formatter:off
		Query query = em.createNativeQuery( "DELETE FROM contacts WHERE user_id = :userId AND contact_id = :contactId");
		// @formatter:on

		query.setParameter("userId", myUser.getId());
		query.setParameter("contactId", publicUser.getId());

		query.executeUpdate();
	}

}
