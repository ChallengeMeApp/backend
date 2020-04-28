package de.challengeme.backend.controller.v1;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.validation.Valid;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
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
import org.springframework.web.multipart.MultipartFile;

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
import de.challengeme.backend.user.Achievments;
import de.challengeme.backend.user.MyUser;
import de.challengeme.backend.user.Points;
import de.challengeme.backend.user.PublicUser;
import de.challengeme.backend.user.UserPrototype;
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

	private static final Logger logger = LogManager.getLogger();

	@Autowired
	private ChallengeService challengeService;

	@Autowired
	private UserService userService;

	@Autowired
	private Environment env;

	private Path imageFolderPath;
	private String imageUrlPrefix;

	@PostConstruct
	public void initialize() {
		imageFolderPath = Paths.get(env.getProperty("questophant.image.directory"));
		imageUrlPrefix = env.getProperty("questophant.image.url");
	}

	@PostMapping("/myUser")
	@ApiOperation(value = "Creates or edits a user depending on if a valid user-object with a valid privateUserId is submitted. Returns the newly created or edited user.", response = MyUser.class)
	@ApiResponses(value = {@ApiResponse(code = 400, response = Void.class, message = "Validation failed."), @ApiResponse(code = 226, response = Void.class, message = "User name already in use."), @ApiResponse(code = 404, message = "User not found by given user id.")})
	public Object createOrEditUser(@RequestBody @Valid MyUser user) {

		if (user == null) {
			return userService.createUser();
		}

		MyUser userToSave;
		if (user.getPrivateUserId() == null) {

			// check if user name is already in use
			if (user.getUserName() != null) {
				if (userService.getUserByUserName(user.getUserName()) != null) {
					return ResponseEntity.status(HttpStatus.IM_USED).body("User name already in use.");
				}
			}

			userToSave = userService.createUser();
		} else {
			userToSave = userService.getUserByPrivateUserId(user.getPrivateUserId());
			if (userToSave == null) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
			}

			if ("Guest".equals(userToSave.getUserName())) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Nice try.");
			}

			// check if user name is already in use
			if (user.getUserName() != null) {
				MyUser check = userService.getUserByUserName(user.getUserName());
				if (check != null && !Objects.equal(user.getPrivateUserId(), check.getPrivateUserId())) {
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

		enrichUser(userToSave);
		return userToSave;
	}

	@PostMapping("/myUser/{privateUserId}/image")
	@ApiOperation(value = "Sets the image of a user.", response = MyUser.class)
	public Object setUserImage(@PathVariable String privateUserId, @RequestBody @Valid MultipartFile file) {
		MyUser user = userService.getUserByPrivateUserId(privateUserId);
		if (user == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
		}

		String fileName;

		do {
			fileName = UUID.randomUUID().toString();
		} while (Files.exists(imageFolderPath.resolve(fileName)));

		try (InputStream from = file.getInputStream()) {
			Files.createDirectories(imageFolderPath);
			Files.copy(from, imageFolderPath.resolve(fileName));
		} catch (IOException e) {
			logger.error("Could not save image.", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Could not save image.");
		}

		user.setImageUrl(fileName);
		userService.save(user);

		enrichUser(user);
		return user;
	}

	@GetMapping("/guest")
	@ApiOperation(value = "Fetches a test-user for testing the API on this webseite.", response = MyUser.class)
	public Object getGuest() {
		MyUser user = userService.getUserByUserName("Guest");
		enrichUser(user);
		return user;
	}

	@GetMapping("/myUser/{privateUserId}")
	@ApiOperation(value = "Gets a user object for the given privateUserId.", response = MyUser.class)
	public Object getPrivateUser(@PathVariable String privateUserId) {
		MyUser result = userService.getUserByPrivateUserId(privateUserId);
		if (result == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
		}
		enrichUser(result);
		return result;
	}

	@GetMapping("/publicUser/{publicUserId}")
	@ApiOperation(value = "Gets a user object for the given publicUserId.", response = PublicUser.class)
	public Object getPublicUser(@PathVariable String publicUserId) {
		PublicUser result = userService.getPublicUserByUserId(publicUserId);
		if (result == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
		}
		enrichUser(result);
		return result;
	}

	@GetMapping("/myUser/{privateUserId}/achievments")
	@ApiOperation(value = "Gets the achievments of a user.", response = Achievments.class)
	public Object getPrivateUserAchievments(@PathVariable String privateUserId) {
		MyUser user = userService.getUserByPrivateUserId(privateUserId);
		if (user == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
		}
		try {
			return userService.getUserAchievments(user, imageUrlPrefix);
		} catch (IOException e) {
			logger.error("Error retrieving achievments.", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error retrieving achievments.");
		}
	}

	@GetMapping("/publicUser/{publicUserId}/achievments")
	@ApiOperation(value = "Gets the achievments of a user.", response = Achievments.class)
	public Object getPublicUserAchievments(@PathVariable String publicUserId) {
		PublicUser user = userService.getPublicUserByUserId(publicUserId);
		if (user == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
		}
		try {
			return userService.getUserAchievments(user, imageUrlPrefix);
		} catch (IOException e) {
			logger.error("Error retrieving achievments.", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error retrieving achievments.");
		}
	}

	@GetMapping("/myUser/{privateUserId}/points")
	@ApiOperation(value = "Gets the points of a user.", response = Points.class)
	public Object getPrivateUserPoints(@PathVariable String privateUserId) {
		MyUser user = userService.getUserByPrivateUserId(privateUserId);
		if (user == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
		}
		return userService.getUserPoints(user);
	}

	@GetMapping("/publicUser/{publicUserId}/points")
	@ApiOperation(value = "Gets the points of a user.", response = Points.class)
	public Object getPublicUserPoints(@PathVariable String publicUserId) {
		PublicUser user = userService.getPublicUserByUserId(publicUserId);
		if (user == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
		}
		return userService.getUserPoints(user);
	}

	@GetMapping("/myUser/{privateUserId}/challenge/{challengeId}")
	@ApiOperation(value = "Returns a challenge for the corresponding challengeId.", response = ChallengeWithStatus.class)
	public Object getChallengeWithStatusById(@PathVariable String privateUserId, @PathVariable long challengeId) {
		MyUser user = userService.getUserByPrivateUserId(privateUserId);
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

	@GetMapping("/myUser/{privateUserId}/challenge_stream")
	@ApiOperation(value = "Returns the stream of challenges for the different categories. If no category is given, it returns all of them.", response = Challenge.class, responseContainer = "List")
	public Object getChallengeStream(@PathVariable String privateUserId, @RequestParam(required = false) Category category, @RequestParam(defaultValue = "0") Integer pageIndex, @RequestParam(defaultValue = "10") Integer pageSize) {
		MyUser user = userService.getUserByPrivateUserId(privateUserId);
		if (user == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
		}

		Slice<Challenge> resultSlice = challengeService.getChallengesForStream(category, user, PageRequest.of(pageIndex, pageSize));
		List<Challenge> result = resultSlice.getContent();
		enrichChallenge(result);
		return result;
	}

	private void enrichChallenge(List<? extends ChallengePrototype> challenges) {
		for (ChallengePrototype challenge : challenges) {
			enrichChallenge(challenge);
		}
	}

	private void enrichChallenge(ChallengePrototype challenge) {
		if (!Strings.isNullOrEmpty(challenge.getImageUrl())) {
			challenge.setImageUrl(imageUrlPrefix + challenge.getImageUrl());
		}
		PublicUser publicUser = userService.getPublicUserByUserId(challenge.getCreatedByPublicUserId());
		challenge.setCreatedByUserName(publicUser == null ? null : publicUser.getUserName());
	}

	private void enrichUser(UserPrototype user) {
		if (!Strings.isNullOrEmpty(user.getImageUrl())) {
			user.setImageUrl(imageUrlPrefix + user.getImageUrl());
		}
	}

	@PostMapping("/myUser/{privateUserId}/challenge_status/{challengeId}")
	@ApiOperation(value = "This interface is called whenever a user is starting or finishing a challenge.", response = DefaultResponse.class)
	public Object setChallengeResult(@PathVariable String privateUserId, @PathVariable Long challengeId, @RequestParam State state) {

		MyUser user = userService.getUserByPrivateUserId(privateUserId);
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

	@GetMapping("/myUser/{privateUserId}/ongoing_challenges")
	@ApiOperation(value = "Gets all challenges, currently being done by the user.", response = Challenge.class, responseContainer = "List")
	public Object getOngoingChallenges(@PathVariable String privateUserId, @RequestParam(defaultValue = "0") Integer pageIndex, @RequestParam(defaultValue = "10") Integer pageSize) {
		MyUser user = userService.getUserByPrivateUserId(privateUserId);
		if (user == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
		}
		Slice<OnGoingChallenge> resultSlice = challengeService.getChallengesOngoingByUser(user, PageRequest.of(pageIndex, pageSize));
		List<OnGoingChallenge> result = resultSlice.getContent();
		enrichChallenge(result);
		return result;
	}

	@GetMapping("/myUser/{privateUserId}/done_challenges")
	@ApiOperation(value = "Gets all challenges done by the user (successfuly or not).", response = DoneChallenge.class, responseContainer = "List")
	public Object getDoneChallenges(@PathVariable String privateUserId, @RequestParam(defaultValue = "0") Integer pageIndex, @RequestParam(defaultValue = "10") Integer pageSize) {
		MyUser user = userService.getUserByPrivateUserId(privateUserId);
		if (user == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
		}
		Slice<DoneChallenge> resultSlice = challengeService.getChallengesDoneByUser(user, PageRequest.of(pageIndex, pageSize));
		List<DoneChallenge> result = resultSlice.getContent();
		enrichChallenge(result);
		return result;
	}

	@GetMapping("/myUser/{privateUserId}/ignored_challenges")
	@ApiOperation(value = "Gets all challenges, ignored by the user.", response = Challenge.class, responseContainer = "List")
	public Object getIgnoredChallenges(@PathVariable String privateUserId, @RequestParam(defaultValue = "0") Integer pageIndex, @RequestParam(defaultValue = "10") Integer pageSize) {
		MyUser user = userService.getUserByPrivateUserId(privateUserId);
		if (user == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
		}
		Slice<Challenge> resultSlice = challengeService.getChallengesIgnoredByUser(user, PageRequest.of(pageIndex, pageSize));
		List<Challenge> result = resultSlice.getContent();
		enrichChallenge(result);
		return result;
	}

	@PostMapping("/myUser/{privateUserId}/ignored_challenges/{challengeId}")
	@ApiOperation(value = "Challenges set on ignore will no longer be displayed in the proposition streams.", response = DefaultResponse.class)
	public Object setChallengeIgnoredByUser(@PathVariable String privateUserId, @PathVariable Long challengeId, @RequestParam boolean ignored) {

		MyUser user = userService.getUserByPrivateUserId(privateUserId);
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

	@GetMapping("/myUser/{privateUserId}/marked_challenges")
	@ApiOperation(value = "Gets all challenges, marked by the user (<3).", response = Challenge.class, responseContainer = "List")
	public Object getMarkedChallenges(@PathVariable String privateUserId, @RequestParam(defaultValue = "0") Integer pageIndex, @RequestParam(defaultValue = "10") Integer pageSize) {
		MyUser user = userService.getUserByPrivateUserId(privateUserId);
		if (user == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
		}
		Slice<Challenge> resultSlice = challengeService.getChallengesMarkedByUser(user, PageRequest.of(pageIndex, pageSize));
		List<Challenge> result = resultSlice.getContent();
		enrichChallenge(result);
		return result;
	}

	@PostMapping("/myUser/{privateUserId}/marked_challenges/{challengeId}")
	@ApiOperation(value = "Adds or removes a challenge from the marked-challenges-list of a user.", response = DefaultResponse.class)
	public Object setChallengeMarkedByUser(@PathVariable String privateUserId, @PathVariable Long challengeId, @RequestParam boolean marked) {

		MyUser user = userService.getUserByPrivateUserId(privateUserId);
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

	@GetMapping("/myUser/{privateUserId}/created_challenges")
	@ApiOperation(value = "Gets all challenges, created by the user.", response = Challenge.class, responseContainer = "List")
	public Object getCreatedChallenges(@PathVariable String privateUserId, @RequestParam(defaultValue = "0") Integer pageIndex, @RequestParam(defaultValue = "10") Integer pageSize) {
		MyUser user = userService.getUserByPrivateUserId(privateUserId);
		if (user == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
		}
		Slice<Challenge> resultSlice = challengeService.getChallengesCreatedByUser(user, PageRequest.of(pageIndex, pageSize));
		List<Challenge> result = resultSlice.getContent();
		enrichChallenge(result);
		return result;
	}

	@PostMapping("/myUser/{privateUserId}/created_challenges")
	@ApiOperation(value = "Creates a new challenge.", response = Challenge.class)
	@ApiResponses(value = {@ApiResponse(code = 400, response = Void.class, message = "Validation failed."), @ApiResponse(code = 404, message = "User not found by given user id.")})
	public Object createChallenge(@PathVariable String privateUserId, @RequestBody @Valid Challenge challenge) {
		MyUser user = userService.getUserByPrivateUserId(privateUserId);
		if (user == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
		}

		challenge.setId(0);
		challenge.setDeletedAt(null);
		challenge.setCreatedByImport(false);
		challenge.setPointsLoose(0);
		challenge.setPointsWin(10);

		challengeService.createChallenge(user, challenge);
		enrichChallenge(challenge);
		return challenge;
	}

	@DeleteMapping("/myUser/{privateUserId}/created_challenges/{challengeId}")
	@ApiOperation(value = "Removes the challenge with the corresponding id.", response = Challenge.class)
	public Object deleteChallenge(@PathVariable String privateUserId, @PathVariable Long challengeId) {
		MyUser user = userService.getUserByPrivateUserId(privateUserId);
		if (user == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
		}

		Challenge result = challengeService.markChallengeAsDeleted(user, challengeId);
		enrichChallenge(result);
		return result;
	}

	@PostMapping("/myUser/{privateUserId}/created_challenges/{challengeId}/image")
	@ApiOperation(value = "Sets the image of a created challenge.", response = Challenge.class)
	public Object setChallengeImage(@PathVariable String privateUserId, @PathVariable Long challengeId, @RequestBody @Valid MultipartFile file) {
		MyUser user = userService.getUserByPrivateUserId(privateUserId);
		if (user == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
		}

		Challenge challenge = challengeService.getChallengeFromId(challengeId);
		if (!user.getPublicUserId().equals(challenge.getCreatedByPublicUserId())) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Challenge was not created by this user.");
		}

		String fileName;

		do {
			fileName = UUID.randomUUID().toString();
		} while (Files.exists(imageFolderPath.resolve(fileName)));

		try (InputStream from = file.getInputStream()) {
			Files.createDirectories(imageFolderPath);
			Files.copy(from, imageFolderPath.resolve(fileName));
		} catch (IOException e) {
			logger.error("Could not save image.", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Could not save image.");
		}

		challenge.setImageUrl(fileName);
		challengeService.save(challenge);

		enrichChallenge(challenge);
		return challenge;
	}
}
