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
import javax.persistence.Query;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import de.challengeme.backend.Helper;
import de.challengeme.backend.challenge.Category;
import de.challengeme.backend.user.Achievments.Achievment;

@Service
public class UserService {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private EntityManager em;

	public Points getUserPoints(UserPrototype user) {

		// @formatter:off
		Query query = em.createNativeQuery( " SELECT 0 id, IFNULL(SUM(u.points),0) points FROM" + 
											" (" + 
											"	SELECT SUM(points_win) points FROM challenges WHERE id in (" + 
											"		SELECT challenge_id FROM challenge_status WHERE user_id = :userId AND state = 1" + 
											"	)" + 
											"	UNION ALL" + 
											"	SELECT SUM(points_loose) points FROM challenges WHERE id in (" + 
											"		SELECT challenge_id FROM challenge_status WHERE user_id = :userId AND state = 2" + 
											"	)" + 
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
		Query query = em.createNativeQuery( " SELECT 0 id, IFNULL(SUM(u.points),0) points, category FROM" + 
											" (" + 
											"	SELECT SUM(points_win) points, category FROM challenges WHERE id in (" + 
											"		SELECT challenge_id FROM challenge_status WHERE user_id = :userId AND state = 1" + 
											"	) GROUP BY category" + 
											"	UNION ALL" + 
											"	SELECT SUM(points_loose) points, category FROM challenges WHERE id in (" + 
											"		SELECT challenge_id FROM challenge_status WHERE user_id = :userId AND state = 2" + 
											"	) GROUP BY category" + 
											" ) AS u"+
											" GROUP BY category", CategoryPoints.class);
		// @formatter:on

		query.setParameter("userId", user.getId());

		return query.getResultList();
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	public Achievments getUserAchievments(UserPrototype user, String imageUrlPrefix) throws IOException {

		Map<String, Object> predefinedAchievments;
		ObjectMapper mapper = new ObjectMapper();
		try (InputStream is = getClass().getResourceAsStream("/achievments.json")) {
			predefinedAchievments = mapper.readValue(is, Map.class);
		}

		Map<String, Map> pAByCategory = Helper.getAsMap(predefinedAchievments.get("byCategory"));
		Map<Category, List<Achievment>> achievmentsByCategory = Maps.newHashMap();
		for (CategoryPoints cp : getUserPointsPerCategory(user)) {
			Map<String, Object> pAList = pAByCategory.get(cp.getCategory().toString());
			pAList.put("points", cp.getPoints());
		}

		Map<Integer, Integer> totalLevels = Maps.newHashMap();
		for (Entry<String, Map> entry : pAByCategory.entrySet()) {

			Category category = Category.valueOf(entry.getKey());
			List<Achievment> achievments = Lists.newArrayList();

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
					String imageUrl = Helper.getAsString(paMap.get("imageUrl"), "");

					if (imageUrl.length() > 0) {
						imageUrl = imageUrlPrefix + imageUrl;
					}

					if (Long.parseLong(entryPA.getKey()) <= points) {
						achievments.add(new Achievment(name, imageUrl, true));
						level++;
					} else {
						achievments.add(new Achievment(lastWasAchieved ? name : "", lastWasAchieved ? imageUrl : "", false));
						lastWasAchieved = false;
					}
				}
			}

			totalLevels.put(level, totalLevels.getOrDefault(level, 0) + 1);
			achievmentsByCategory.put(category, achievments);
		}

		List<Achievment> overalLevel = Lists.newArrayList();
		List<Map> pAOverall = Helper.getAsList(predefinedAchievments.get("overall"));
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
			String imageUrl = Helper.getAsString(overallMap.get("imageUrl"), "");

			if (imageUrl.length() > 0) {
				imageUrl = imageUrlPrefix + imageUrl;
			}

			if (conditionsMatch) {
				overalLevel.add(new Achievment(name, imageUrl, true));
			} else {
				overalLevel.add(new Achievment(lastWasAchieved ? name : "", lastWasAchieved ? imageUrl : "", false));
				lastWasAchieved = false;
			}
		}

		Achievments achievments = new Achievments();
		achievments.setAchievmentsByCategory(achievmentsByCategory);
		achievments.setOveralLevel(overalLevel);
		return achievments;
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
		return getUserByPrivateUserId(userId.toString());
	}

	public MyUser getUserByPrivateUserId(String privateUserId) {
		MyUser user = userRepository.getByPrivateUserId(privateUserId);
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
		return getPublicUserByUserId(publicUserId.toString());
	}

	public PublicUser getPublicUserByUserId(String publicUserId) {

		// @formatter:off
		Query query = em.createNativeQuery( "SELECT * FROM users WHERE public_user_id = UUID_TO_BIN(:publicUserId)", PublicUser.class);
		// @formatter:on

		query.setParameter("publicUserId", publicUserId);

		for (Object object : query.getResultList()) {
			return (PublicUser) object;
		}

		return null;
	}
}
