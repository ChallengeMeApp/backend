package de.challengeme.backend.controller.v1;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import de.challengeme.backend.challenge.Category;
import de.challengeme.backend.challenge.Challenge;
import de.challengeme.backend.challenge.ChallengeService;
import de.challengeme.backend.user.User;
import de.challengeme.backend.user.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping("/api/v1")
@Api(value = "API for the prototype App.", description = "API for the app.", tags = {"Backend Version 1"})
@CrossOrigin
public class BackendControllerV1 {

	@Autowired
	private ChallengeService challengeService;

	@Autowired
	private UserService userService;

	@PostMapping("/users")
	@ApiOperation(value = "Creates or edits a user depending on if a valid user-object with a valid userId is submitted. Returns the newly created or edited user.", response = User.class)
	public Object createOrEditUser(@RequestBody User user) {

		if (user == null) {
			return userService.createUser();
		}

		User userToSave;
		if (user.getUserId() == null) {
			userToSave = userService.createUser();
		} else {
			userToSave = userService.getUser(user.getUserId());
			if (userToSave == null) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
			}
		}

		if (user.getUserName() != null) {
			userToSave.setUserName(user.getUserName());
		}

		if (user.getFirstName() != null) {
			userToSave.setFirstName(user.getFirstName());
		}

		if (user.getLastName() != null) {
			userToSave.setLastName(user.getLastName());
		}

		userService.save(userToSave);
		return userToSave;
	}

	@GetMapping("/users/{userId}")
	@ApiOperation(value = "Gets a user object for the given userId.", response = User.class)
	public User getUser(@PathVariable(value = "userId") String userId) {
		return userService.getUser(userId);
	}

	@GetMapping("/daily_challenge")
	public Challenge getDailyChallenge() {
		return challengeService.getDailyChallenge();
	}

	@GetMapping("/random_challenge")
	public Challenge getRandomChallenge(@RequestParam(value = "category", defaultValue = "household") Category category) {
		return challengeService.getRandomChallenge(category);
	}

	@GetMapping("/users/{userId}/challenges")
	@ApiOperation(value = "Gets all challenges, created by the user.", response = Challenge.class, responseContainer = "List")
	public Object getCreatedChallenges(@PathVariable(value = "userId") String userId) {
		User user = userService.getUser(userId);
		if (user == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
		}
		return challengeService.getChallengesCreatedByUser(user);
	}

	@PostMapping("/users/{userId}/challenges")
	@ApiOperation(value = "Creates a new challenge.", response = Challenge.class)
	public Object createChallenge(@PathVariable(value = "userId") String userId, @RequestBody Challenge challenge) {
		User user = userService.getUser(userId);
		if (user == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
		}

		challenge.setId(0);
		challenge.setDeletedAt(null);
		challenge.setCreatedByImport(false);
		// TODO: filter invalid challenges

		challengeService.createChallenge(user, challenge);
		return challenge;
	}

	@DeleteMapping("/users/{userId}/challenges/{challengeId}")
	@ApiOperation(value = "Removes the challenge with the corresponding id.", response = String.class)
	public Object deleteChallenge(@PathVariable(value = "userId") String userId, @PathVariable(value = "challengeId") Long challengeId) {
		User user = userService.getUser(userId);
		if (user == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
		}

		return challengeService.markChallengeAsDeleted(user, challengeId);
	}
}
