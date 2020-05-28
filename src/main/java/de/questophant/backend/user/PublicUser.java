package de.questophant.backend.user;

import javax.persistence.Entity;

import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.Subselect;

@Entity
@Immutable
@Subselect("SELECT 'ignoreme';")
public class PublicUser extends UserPrototype {

}
