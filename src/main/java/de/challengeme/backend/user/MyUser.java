package de.challengeme.backend.user;

import java.time.Instant;
import java.util.UUID;

import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Index;
import javax.persistence.Table;

import io.swagger.annotations.ApiModelProperty;

@Entity
@Table(name = "users", indexes = {@Index(name = "userNameIndex", columnList = "userName", unique = true), @Index(name = "privateUserIdIndex", columnList = "privateUserId", unique = true), @Index(name = "publicUserIdIndex", columnList = "publicUserId", unique = true)})
public class MyUser extends UserPrototype {

	@Nullable
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(columnDefinition = "BINARY(16)")
	@ApiModelProperty(required = false, notes = "private user id, field is read-only")
	private UUID privateUserId;

	@Nullable
	@ApiModelProperty(required = false, notes = "field is read-only")
	private Instant createdAt;

	@Nullable
	@ApiModelProperty(required = false, notes = "field is read-only")
	private Instant lastRequestAt;

	public UUID getPrivateUserId() {
		return privateUserId;
	}

	public void setPrivateUserId(UUID privateUserId) {
		this.privateUserId = privateUserId;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Instant createdAt) {
		this.createdAt = createdAt;
	}

	public Instant getLastRequestAt() {
		return lastRequestAt;
	}

	public void setLastRequestAt(Instant lastRequestAt) {
		this.lastRequestAt = lastRequestAt;
	}
}
