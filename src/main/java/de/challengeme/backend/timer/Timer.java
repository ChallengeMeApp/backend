package de.challengeme.backend.timer;

import java.time.Instant;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;

@Entity
@Table(name = "timer", indexes = {@Index(name = "timerTypeIndex", columnList = "type", unique = true)})
public class Timer {

	@Id
	private long id;

	@Enumerated(EnumType.STRING)
	@Column(columnDefinition = "VARCHAR(16)")
	private TimerType type;

	private Instant validUntil;

	private long linkedId;

	public TimerType getType() {
		return type;
	}

	public void setType(TimerType type) {
		this.type = type;
	}

	public Instant getValidUntil() {
		return validUntil;
	}

	public void setValidUntil(Instant validUntil) {
		this.validUntil = validUntil;
	}

	public long getLinkedId() {
		return linkedId;
	}

	public void setLinkedId(long linkedId) {
		this.linkedId = linkedId;
	}

}
