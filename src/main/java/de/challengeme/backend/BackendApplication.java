package de.challengeme.backend;

import java.util.Arrays;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;

import com.google.common.base.Joiner;

import de.challengeme.backend.user.MyUser;
import de.challengeme.backend.user.UserService;

@ServletComponentScan
@SpringBootApplication
public class BackendApplication {

	private static List<String> arguments;
	private static Logger logger = LogManager.getLogger();

	@Autowired
	private UserService userService;

	@Autowired
	private GoogleDocImporter googleDocImporter;

	@Autowired
	AutowireCapableBeanFactory beanFactory;

	public static void main(String[] args) {
		arguments = Arrays.asList(args);
		if (arguments != null) {
			logger.info("Started with arguments: " + Joiner.on(',').join(arguments));
		}
		SpringApplication.run(BackendApplication.class, args);
	}

	protected boolean isInTestMode() {
		return arguments == null; // is null when tests are run
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	@PostConstruct
	@Bean
	public ServletRegistrationBean initializeImageServlet() {
		ServletRegistrationBean srb = new ServletRegistrationBean();
		final ImageServlet servlet = new ImageServlet();
		beanFactory.autowireBean(servlet); // <--- The most important part
		servlet.initialize();
		srb.setServlet(servlet);
		srb.setUrlMappings(Arrays.asList("/api/v1/img/*"));
		srb.setLoadOnStartup(1);
		return srb;
	}

	@PostConstruct
	public void initializeDatabase() {
		if (!isInTestMode()) {

			// create root admin user
			if (userService.getCountOfAdminUsers() == 0) {
				MyUser user = userService.createUser();
				user.setAdmin(true);
				user.setFirstName("root");
				user.setLastName("root");
				user.setUserName("CM Team");
				userService.save(user);
				logger.info("Created root user: " + user.getPrivateUserId());
			}

			// create one guest user, usable for testing
			if (userService.getUserByUserName("Guest") == null) {
				MyUser user = userService.createUser();
				user.setAdmin(false);
				user.setFirstName("Guest");
				user.setLastName("Guest");
				user.setUserName("Guest");
				userService.save(user);
				logger.info("Created guest user: {}", user.getPrivateUserId());
			}

			for (String argument : arguments) {
				if (argument.startsWith("import=")) {
					googleDocImporter.importChallenges(argument.substring(7));
				}
			}
		}
	}
}
