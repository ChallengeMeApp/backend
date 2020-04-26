package de.challengeme.backend.challenge;

import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;

@Entity
@Table(name = "challenges", indexes = {@Index(name = "challengeCategoryIndex", columnList = "category", unique = false), @Index(name = "challengeCreatedByUserIdIndex", columnList = "createdByUserId", unique = false)})
public class Challenge extends ChallengePrototype {

}
