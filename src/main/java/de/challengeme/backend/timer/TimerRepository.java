package de.challengeme.backend.timer;

import java.time.Instant;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TimerRepository extends JpaRepository<Timer, Long> {

	@Query(value = "SELECT * FROM timer WHERE type = :type AND valid_until > :stillValidAt LIMIT 1", nativeQuery = true)
	public Timer getActiveTimer(String type, Instant stillValidAt);

	@Query(value = "SELECT * FROM timer WHERE type = :type LIMIT 1", nativeQuery = true)
	public Timer getActiveTimer(String type);

}