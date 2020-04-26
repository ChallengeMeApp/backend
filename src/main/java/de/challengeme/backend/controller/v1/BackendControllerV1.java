package de.challengeme.backend.controller.v1;

import java.time.Instant;
import java.util.List;

import javax.validation.Valid;

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
import com.google.common.base.Strings;

import de.challengeme.backend.DefaultResponse;
import de.challengeme.backend.challenge.Category;
import de.challengeme.backend.challenge.Challenge;
import de.challengeme.backend.challenge.ChallengePrototype;
import de.challengeme.backend.challenge.ChallengeService;
import de.challengeme.backend.challenge.ChallengeStatus;
import de.challengeme.backend.challenge.ChallengeStatus.State;
import de.challengeme.backend.challenge.ChallengeWithStatus;
import de.challengeme.backend.challenge.DoneChallenge;
import de.challengeme.backend.challenge.OnGoingChallenge;
import de.challengeme.backend.user.User;
import de.challengeme.backend.user.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RestController
@RequestMapping("/api/v1")
@Api(value = "API for the app.", description = "API for the app.", tags = {"Backend Version 1"})
@CrossOrigin
public class BackendControllerV1 {

	@Autowired
	private ChallengeService challengeService;

	@Autowired
	private UserService userService;

	@PostMapping("/users")
	@ApiOperation(value = "Creates or edits a user depending on if a valid user-object with a valid userId is submitted. Returns the newly created or edited user.", response = User.class)
	@ApiResponses(value = {@ApiResponse(code = 400, response = Void.class, message = "Validation failed."), @ApiResponse(code = 226, response = Void.class, message = "User name already in use."), @ApiResponse(code = 404, message = "User not found by given user id.")})
	public Object createOrEditUser(@RequestBody @Valid User user) {

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

			if ("Guest".equals(userToSave.getUserName())) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Nice try.");
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

	@GetMapping("/guest")
	@ApiOperation(value = "Fetches a test-user for testing the API on this webseite.", response = User.class)
	public Object getGuest() {
		return userService.getUserByUserName("Guest");
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

	@GetMapping("/users/{userId}/challenge/{challengeId}")
	@ApiOperation(value = "Returns a challenge for the corresponding challengeId.", response = Challenge.class)
	public Object getChallengeById(@PathVariable String userId, @PathVariable long challengeId) {
		User user = userService.getUserByUserId(userId);
		if (user == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
		}
		ChallengeWithStatus result = challengeService.getChallengeWithStatusFromId(user, challengeId);
		if (result == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Challenge not found.");
		}
		enrichChallenge(result);
		return result;
	}

	@GetMapping("/daily_challenge")
	@ApiOperation(value = "Returns the daily challenge.", response = Challenge.class)
	public Challenge getDailyChallenge() {
		Challenge result = challengeService.getDailyChallenge();
		enrichChallenge(result);
		return result;
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
		enrichChallenge(result);
		return result;
	}

	/**
	 * Workaround function for @Formula not working in Hibernate for 7 years. It is advertised to work in the next
	 * version.
	 */
	private void enrichChallenge(List<? extends ChallengePrototype> challenges) {
		for (ChallengePrototype challenge : challenges) {
			enrichChallenge(challenge);
		}
	}

	/**
	 * Workaround function for @Formula not working in Hibernate for 7 years. It is advertised to work in the next
	 * version.
	 */
	private void enrichChallenge(ChallengePrototype challenge) {
		if (!Strings.isNullOrEmpty(challenge.getImageUrl())) {
			challenge.setImageUrl("/images/" + challenge.getImageUrl());
		}
		challenge.setCreatedByUserName(userService.getUserNameFromId(challenge.getCreatedByUserId()));
	}

	@PostMapping("/users/{userId}/challenge_status/{challengeId}")
	@ApiOperation(value = "This interface is called whenever a user is starting or finishing a challenge.", response = DefaultResponse.class)
	public Object setChallengeResult(@PathVariable String userId, @PathVariable Long challengeId, @RequestParam State state) {

		User user = userService.getUserByUserId(userId);
		if (user == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
		}

		Challenge challenge = challengeService.getChallengeFromId(challengeId);
		if (challenge == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Challenge not found.");
		}

		ChallengeStatus challengeResult = new ChallengeStatus();
		challengeResult.setUserId(user.getId());
		challengeResult.setChallengeId(challenge.getId());
		challengeResult.setTimeStamp(Instant.now());
		challengeResult.setState(state);

		challengeService.save(challengeResult);

		return DefaultResponse.SUCCESS;
	}

	@GetMapping("/users/{userId}/ongoing_challenges")
	@ApiOperation(value = "Gets all challenges, currently being done by the user.", response = Challenge.class, responseContainer = "List")
	public Object getOngoingChallenges(@PathVariable String userId, @RequestParam(defaultValue = "0") Integer pageIndex, @RequestParam(defaultValue = "10") Integer pageSize) {
		User user = userService.getUserByUserId(userId);
		if (user == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
		}
		Slice<OnGoingChallenge> resultSlice = challengeService.getChallengesOngoingByUser(user, PageRequest.of(pageIndex, pageSize));
		List<OnGoingChallenge> result = resultSlice.getContent();
		enrichChallenge(result);
		return result;
	}

	@GetMapping("/users/{userId}/done_challenges")
	@ApiOperation(value = "Gets all challenges done by the user (successfuly or not).", response = DoneChallenge.class, responseContainer = "List")
	public Object getDoneChallenges(@PathVariable String userId, @RequestParam(defaultValue = "0") Integer pageIndex, @RequestParam(defaultValue = "10") Integer pageSize) {
		User user = userService.getUserByUserId(userId);
		if (user == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
		}
		Slice<DoneChallenge> resultSlice = challengeService.getChallengesDoneByUser(user, PageRequest.of(pageIndex, pageSize));
		List<DoneChallenge> result = resultSlice.getContent();
		enrichChallenge(result);
		return result;
	}

	@GetMapping("/users/{userId}/ignored_challenges")
	@ApiOperation(value = "Gets all challenges, ignored by the user.", response = Challenge.class, responseContainer = "List")
	public Object getIgnoredChallenges(@PathVariable String userId, @RequestParam(defaultValue = "0") Integer pageIndex, @RequestParam(defaultValue = "10") Integer pageSize) {
		User user = userService.getUserByUserId(userId);
		if (user == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
		}
		Slice<Challenge> resultSlice = challengeService.getChallengesIgnoredByUser(user, PageRequest.of(pageIndex, pageSize));
		List<Challenge> result = resultSlice.getContent();
		enrichChallenge(result);
		return result;
	}

	@PostMapping("/users/{userId}/ignored_challenges/{challengeId}")
	@ApiOperation(value = "Challenges set on ignore will no longer be displayed in the proposition streams.", response = DefaultResponse.class)
	public Object setChallengeIgnoredByUser(@PathVariable String userId, @PathVariable Long challengeId, @RequestParam boolean ignored) {

		User user = userService.getUserByUserId(userId);
		if (user == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
		}

		Challenge challenge = challengeService.getChallengeFromId(challengeId);
		if (challenge == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Challenge not found.");
		}

		challengeService.setChallengeIgnoredByUser(user, challenge, ignored);

		return DefaultResponse.SUCCESS;
	}

	@GetMapping("/users/{userId}/marked_challenges")
	@ApiOperation(value = "Gets all challenges, marked by the user (<3).", response = Challenge.class, responseContainer = "List")
	public Object getMarkedChallenges(@PathVariable String userId, @RequestParam(defaultValue = "0") Integer pageIndex, @RequestParam(defaultValue = "10") Integer pageSize) {
		User user = userService.getUserByUserId(userId);
		if (user == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
		}
		Slice<Challenge> resultSlice = challengeService.getChallengesMarkedByUser(user, PageRequest.of(pageIndex, pageSize));
		List<Challenge> result = resultSlice.getContent();
		enrichChallenge(result);
		return result;
	}

	@PostMapping("/users/{userId}/marked_challenges/{challengeId}")
	@ApiOperation(value = "Adds or removes a challenge from the marked-challenges-list of a user.", response = DefaultResponse.class)
	public Object setChallengeMarkedByUser(@PathVariable String userId, @PathVariable Long challengeId, @RequestParam boolean marked) {

		User user = userService.getUserByUserId(userId);
		if (user == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
		}

		Challenge challenge = challengeService.getChallengeFromId(challengeId);
		if (challenge == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Challenge not found.");
		}

		challengeService.setChallengeMarkedByUser(user, challenge, marked);

		return DefaultResponse.SUCCESS;
	}

	@GetMapping("/users/{userId}/created_challenges")
	@ApiOperation(value = "Gets all challenges, created by the user.", response = Challenge.class, responseContainer = "List")
	public Object getCreatedChallenges(@PathVariable String userId, @RequestParam(defaultValue = "0") Integer pageIndex, @RequestParam(defaultValue = "10") Integer pageSize) {
		User user = userService.getUserByUserId(userId);
		if (user == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
		}
		Slice<Challenge> resultSlice = challengeService.getChallengesCreatedByUser(user, PageRequest.of(pageIndex, pageSize));
		List<Challenge> result = resultSlice.getContent();
		enrichChallenge(result);
		return result;
	}

	@PostMapping("/users/{userId}/created_challenges")
	@ApiOperation(value = "Creates a new challenge.", response = Challenge.class)
	@ApiResponses(value = {@ApiResponse(code = 400, response = Void.class, message = "Validation failed."), @ApiResponse(code = 404, message = "User not found by given user id.")})
	public Object createChallenge(@PathVariable String userId, @RequestBody @Valid Challenge challenge) {
		User user = userService.getUserByUserId(userId);
		if (user == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
		}

		challenge.setId(0);
		challenge.setDeletedAt(null);
		challenge.setCreatedByImport(false);
		// TODO: filter invalid challenges

		challengeService.createChallenge(user, challenge);
		enrichChallenge(challenge);
		return challenge;
	}

	@DeleteMapping("/users/{userId}/created_challenges/{challengeId}")
	@ApiOperation(value = "Removes the challenge with the corresponding id.", response = String.class)
	public Object deleteChallenge(@PathVariable String userId, @PathVariable Long challengeId) {
		User user = userService.getUserByUserId(userId);
		if (user == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
		}

		Challenge result = challengeService.markChallengeAsDeleted(user, challengeId);
		enrichChallenge(result);
		return result;
	}
}
