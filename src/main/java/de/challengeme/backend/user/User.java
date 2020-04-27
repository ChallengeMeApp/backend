package de.challengeme.backend.user;

import java.time.Instant;
import java.util.UUID;

import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonIgnore;

import de.challengeme.backend.validation.NoHtml;
import de.challengeme.backend.validation.UserName;
import io.swagger.annotations.ApiModelProperty;

@Entity
@Table(name = "users", indexes = {@Index(name = "userNameIndex", columnList = "userName", unique = true), @Index(name = "userUserIdIndex", columnList = "userId", unique = true)})
public class User {

	@JsonIgnore
	private long id;

	@ApiModelProperty(required = false, notes = "field is read-only")
	private boolean admin;

	@Nullable
	@ApiModelProperty(required = false, notes = "field is read-only")
	private UUID userId;

	@Nullable
	@NoHtml
	@UserName(message = "A valid userName consists of words, containing letters and characters.")
	@Size(min = 3, max = 30, message = "User names must be between 3 and 30 characters.")
	@Column(columnDefinition = "VARCHAR(30)")
	@ApiModelProperty(allowableValues = "3-30 alphanumeric characters (a-z,A-Z,0-9) and spaces inbetween", example = "Anonymous Bat")
	private String userName;

	@Nullable
	@NoHtml
	@Size(min = 2, max = 40, message = "userName must be between 2 and 40 characters")
	@ApiModelProperty(allowableValues = "2-40 characters and no HTML", example = "Peter")
	private String firstName;

	@Nullable
	@NoHtml
	@Size(min = 2, max = 40, message = "userName must be between 2 and 40 characters")
	@ApiModelProperty(allowableValues = "2-40 characters and no HTML", example = "Mues")
	private String lastName;

	@Column(columnDefinition = "VARCHAR(255)")
	@ApiModelProperty(required = false, notes = "field is read-only, it contains the URL of the image to be displayed")
	protected String imageUrl;

	@Nullable
	@ApiModelProperty(required = false, notes = "field is read-only")
	private Instant createdAt;

	@Nullable
	@ApiModelProperty(required = false, notes = "field is read-only")
	private Instant lastRequestAt;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}

	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(columnDefinition = "BINARY(16)")
	public UUID getUserId() {
		return userId;
	}
	public void setUserId(UUID userId) {
		this.userId = userId;
	}
	public String getFirstName() {
		return firstName;
	}
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	public String getLastName() {
		return lastName;
	}
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	public boolean isAdmin() {
		return admin;
	}
	public void setAdmin(boolean admin) {
		this.admin = admin;
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
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getImageUrl() {
		return imageUrl;
	}
	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}

}
