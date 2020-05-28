package de.questophant.backend.user;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;

@Entity
@Table(name = "contacts", indexes = {@Index(name = "userIdIndex", columnList = "userId", unique = false)})
public class ContactListEntry {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	private long userId;

	private long contactId;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getUserId() {
		return userId;
	}

	public void setUserId(long userId) {
		this.userId = userId;
	}

	public long getContactId() {
		return contactId;
	}

	public void setContactId(long contactId) {
		this.contactId = contactId;
	}

}
