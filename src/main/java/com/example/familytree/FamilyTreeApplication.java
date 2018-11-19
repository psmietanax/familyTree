package com.example.familytree;

import com.example.familytree.entities.Person;
import com.example.familytree.services.DBService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class FamilyTreeApplication {

	public static void main(String[] args) {
		SpringApplication.run(FamilyTreeApplication.class, args);
	}

	@Bean
	public DBService generateDummyDBService() {
		return new DBService() {
			@Override
			public void savePerson(Person person) {
				System.out.println("Dummy database service stored person " + person.getName());
			}

			@Override
			public void updatePerson(Person person) {
				System.out.println("Dummy database service updated person " + person.getName());
			}
		};
	}
}
