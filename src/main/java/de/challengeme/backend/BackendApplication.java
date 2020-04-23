package de.challengeme.backend;

import java.util.Arrays;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.google.common.base.Joiner;

import de.challengeme.backend.user.User;
import de.challengeme.backend.user.UserService;

@SpringBootApplication
public class BackendApplication {

	private static List<String> arguments;
	private static Logger logger = LogManager.getLogger();

	@Autowired
	private UserService userService;

	@Autowired
	private GoogleDocImporter googleDocImporter;

	public static void main(String[] args) {
		arguments = Arrays.asList(args);
		SpringApplication.run(BackendApplication.class, args);
	}

	@PostConstruct
	public void initializeDatabase() {
		if (arguments != null) { // is null when tests are run
			logger.info("Started with arguments: " + Joiner.on(',').join(arguments));

			// create root admin user
			if (userService.getCountOfAdminUsers() == 0) {
				User user = userService.createUser();
				user.setAdmin(true);
				user.setFirstName("root");
				user.setLastName("root");
				user.setUserName("CM Team");
				userService.save(user);
				logger.info("Created root user: " + user.getUserId());
			}

			// create one guest user, usable for testing
			if (userService.getUserByUserName("Guest") == null) {
				User user = userService.createUser();
				user.setAdmin(false);
				user.setFirstName("Guest");
				user.setLastName("Guest");
				user.setUserName("Guest");
				userService.save(user);
				logger.info("Created guest user: " + user.getUserId());
			}

			for (String argument : arguments) {
				if (argument.startsWith("import=")) {
					googleDocImporter.importChallenges(argument.substring(7));
				}
			}
		}
	}
}
