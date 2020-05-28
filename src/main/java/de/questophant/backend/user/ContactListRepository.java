package de.questophant.backend.user;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ContactListRepository extends JpaRepository<ContactListEntry, Long> {

}
