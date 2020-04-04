package de.challengeme.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import de.challengeme.backend.challenge.ChallengeService;
import de.challengeme.backend.user.UserService;
import io.swagger.annotations.Api;

@RestController
@RequestMapping("/api/admin/v1")
@Api(value = "API for administrative tasks.", description = "API for administrative tasks.", tags = {"Admin Backend V1"})
public class AdminController {

	@Autowired
	private ChallengeService challengeService;

	@Autowired
	private UserService userService;

}
