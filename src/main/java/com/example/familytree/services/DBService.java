package com.example.familytree.services;

import com.example.familytree.entities.Person;

/**
 * A database persistence layer service.
 */
public interface DBService {
	void savePerson(Person person);
	void updatePerson(Person person);
}
