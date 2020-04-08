package de.challengeme.backend.user;

import java.time.Instant;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

	@Autowired
	private UserRepository userRepository;

	public int getCountOfAdminUsers() {
		return userRepository.getCountOfAdminUsers();
	}

	public User createUser() {
		while (true) {
			UUID userId = UUID.randomUUID();
			User user = userRepository.getByUUID(userId.toString());
			if (user == null) {
				user = new User();
				user.setUserId(userId);
				user.setCreatedAt(Instant.now());
				userRepository.saveAndFlush(user);
				return user;
			}
		}
	}

	public User getUser(String userId) {
		User user = userRepository.getByUUID(userId);
		if (user != null) {
			user.setLastRequestAt(Instant.now());
			userRepository.save(user);
		}
		return user;
	}

	public User getRootUser() {
		return userRepository.getRootUser();
	}

	public void save(User user) {
		userRepository.saveAndFlush(user);
	}
}
