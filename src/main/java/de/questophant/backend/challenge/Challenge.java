package de.questophant.backend.challenge;

import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;

@Entity
@Table(name = "challenges", indexes = {@Index(name = "categoryIndex", columnList = "category", unique = false), @Index(name = "createdByPublicUserIdIndex", columnList = "createdByPublicUserId", unique = false)})
public class Challenge extends ChallengePrototype {

}
