package de.challengeme.backend.controller.v1;

import java.time.Instant;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
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

import com.google.common.base.Objects;

import de.challengeme.backend.DefaultResponse;
import de.challengeme.backend.challenge.Category;
import de.challengeme.backend.challenge.Challenge;
import de.challengeme.backend.challenge.ChallengeResult;
import de.challengeme.backend.challenge.ChallengeService;
import de.challengeme.backend.user.User;
import de.challengeme.backend.user.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

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
	@ApiResponses(value = {@ApiResponse(code = 226, response = Void.class, message = "User name already in use."), @ApiResponse(code = 404, message = "User not found by given user id.")})
	public Object createOrEditUser(@RequestBody User user) {

		if (user == null) {
			return userService.createUser();
		}

		User userToSave;
		if (user.getUserId() == null) {

			// check if user name is already in use
			if (user.getUserName() != null) {
				if (userService.getUserByUserName(user.getUserName()) != null) {
					return ResponseEntity.status(HttpStatus.IM_USED).body("User name already in use.");
				}
			}

			userToSave = userService.createUser();
		} else {
			userToSave = userService.getUserByUserId(user.getUserId());
			if (userToSave == null) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
			}

			// check if user name is already in use
			if (user.getUserName() != null) {
				User check = userService.getUserByUserName(user.getUserName());
				if (check != null && !Objects.equal(user.getUserId(), check.getUserId())) {
					return ResponseEntity.status(HttpStatus.IM_USED).body("User name already in use.");
				}
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
	public Object getUser(@PathVariable String userId) {
		User result = userService.getUserByUserId(userId);
		if (result == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
		}
		return result;
	}

	@GetMapping("/daily_challenge")
	public Challenge getDailyChallenge() {
		return challengeService.getDailyChallenge();
	}

	@GetMapping("/users/{userId}/challenge_stream")
	@ApiOperation(value = "Returns the stream of challenges for the different categories. If no category is given, it returns all of them.", response = Challenge.class, responseContainer = "List")
	public Object getChallengeStream(@PathVariable String userId, @RequestParam(required = false) Category category, @RequestParam(defaultValue = "0") Integer pageIndex, @RequestParam(defaultValue = "10") Integer pageSize) {
		User user = userService.getUserByUserId(userId);
		if (user == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
		}

		Slice<Challenge> resultSlice = challengeService.getChallengesForStream(category, user, PageRequest.of(pageIndex, pageSize));
		List<Challenge> result = resultSlice.getContent();
		enrichCreatedByUserName(result);
		return result;
	}

	/**
	 * Workaround function for @Formula not working in Hibernate for 7 years. It is advertised to work in the next
	 * version.
	 */
	private void enrichCreatedByUserName(List<Challenge> challenges) {
		for (Challenge challenge : challenges) {
			enrichCreatedByUserName(challenge);
		}
	}

	/**
	 * Workaround function for @Formula not working in Hibernate for 7 years. It is advertised to work in the next
	 * version.
	 */
	private void enrichCreatedByUserName(Challenge challenge) {
		challenge.setCreatedByUserName(userService.getUserNameFromId(challenge.getCreatedByUserId()));
	}

	@PostMapping("/users/{userId}/challenge_result/{challengeId}")
	@ApiOperation(value = "Stores a challenge result (success/failure).", response = DefaultResponse.class)
	public Object setChallengeResult(@PathVariable String userId, @PathVariable Long challengeId, @RequestParam boolean success) {

		User user = userService.getUserByUserId(userId);
		if (user == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
		}

		Challenge challenge = challengeService.getChallengeFromId(challengeId);
		if (challenge == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Challenge not found.");
		}

		ChallengeResult challengeResult = new ChallengeResult();
		challengeResult.setUserId(user.getId());
		challengeResult.setChallengeId(challenge.getId());
		challengeResult.setTimeStamp(Instant.now());
		challengeResult.setSuccess(success);

		challengeService.save(challengeResult);

		return DefaultResponse.SUCCESS;
	}

	@GetMapping("/users/{userId}/own_challenges")
	@ApiOperation(value = "Gets all challenges, created by the user.", response = Challenge.class, responseContainer = "List")
	public Object getCreatedChallenges(@PathVariable String userId, @RequestParam(defaultValue = "0") Integer pageIndex, @RequestParam(defaultValue = "10") Integer pageSize) {
		User user = userService.getUserByUserId(userId);
		if (user == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
		}
		Slice<Challenge> resultSlice = challengeService.getChallengesCreatedByUser(user, PageRequest.of(pageIndex, pageSize));
		List<Challenge> result = resultSlice.getContent();
		enrichCreatedByUserName(result);
		return result;
	}

	@PostMapping("/users/{userId}/own_challenges")
	@ApiOperation(value = "Creates a new challenge.", response = Challenge.class)
	public Object createChallenge(@PathVariable String userId, @RequestBody Challenge challenge) {
		User user = userService.getUserByUserId(userId);
		if (user == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
		}

		challenge.setId(0);
		challenge.setDeletedAt(null);
		challenge.setCreatedByImport(false);
		// TODO: filter invalid challenges

		challengeService.createChallenge(user, challenge);
		enrichCreatedByUserName(challenge);
		return challenge;
	}

	@DeleteMapping("/users/{userId}/own_challenges/{challengeId}")
	@ApiOperation(value = "Removes the challenge with the corresponding id.", response = String.class)
	public Object deleteChallenge(@PathVariable String userId, @PathVariable Long challengeId) {
		User user = userService.getUserByUserId(userId);
		if (user == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
		}

		Challenge result = challengeService.markChallengeAsDeleted(user, challengeId);
		enrichCreatedByUserName(result);
		return result;
	}
}
