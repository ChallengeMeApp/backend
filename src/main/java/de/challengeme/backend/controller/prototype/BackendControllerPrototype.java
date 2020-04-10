package de.challengeme.backend.controller.prototype;

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
@RequestMapping("/api/prototype")
@Api(value = "API for the prototype App.", description = "API for the prototype App.", tags = {"Backend Prototype"})
@CrossOrigin
public class BackendControllerPrototype {

	@Autowired
	private ChallengeService challengeService;

	@Autowired
	private UserService userService;

	@PostMapping("/users")
	public User createUser() {
		return userService.createUser();
	}

	@GetMapping(value = "/daily_tip", produces = "application/json")
	public Tipp getDailyTip() {
		Tipp result = new Tipp();
		result.setId(1);
		result.setTitle("Eating");
		result.setDescription("An apple a day keeps the doctor away - a challenge a day keeps the clouds away :)");
		return result;
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
	public Object createChallenge(@PathVariable(value = "userId") String userId, @RequestBody CreateChallengeBody challengeBody) {
		User user = userService.getUser(userId);
		if (user == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
		}

		Challenge challenge = new Challenge();
		challenge.setDescription(challengeBody.getDescription());
		challenge.setTitle(challengeBody.getTitle());
		challenge.setCategory(Category.valueOf(challengeBody.getCategory()));
		challenge.setDurationSeconds(challengeBody.getDurationSeconds());

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

		challengeService.markChallengeAsDeleted(user, challengeId);
		return "ok";
	}

}