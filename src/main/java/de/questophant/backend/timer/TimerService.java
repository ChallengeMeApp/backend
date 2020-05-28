package de.questophant.backend.timer;

import java.time.Instant;

import javax.persistence.EntityNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TimerService {

	@Autowired
	private TimerRepository timerRepository;

	public Long getLinkedObjectOfTimer(TimerType timerType) {
		try {
			Timer timer = timerRepository.getActiveTimer(timerType.toString(), Instant.now());
			if (timer != null) {
				return timer.getLinkedId();
			}
		} catch (EntityNotFoundException e) {
			// ignore
		}
		return null;
	}

	public void setTimer(TimerType timerType, Instant validUntil, long linkedObjectId) {
		Timer timer = timerRepository.getActiveTimer(timerType.toString());
		if (timer == null) {
			timer = new Timer();
		}
		timer.setType(timerType);
		timer.setValidUntil(validUntil);
		timer.setLinkedId(linkedObjectId);
		timerRepository.saveAndFlush(timer);
	}
}
