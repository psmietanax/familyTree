package com.example.familytree.controllers;

import com.example.familytree.converters.OrderConverter;
import com.example.familytree.entities.Person;
import com.example.familytree.enums.Order;
import com.example.familytree.forms.PersonForm;
import com.example.familytree.services.FamilyTree;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

/**
 * Main REST API controller.
 */
@RestController
public class FamilyTreeController {

	@Autowired
	private FamilyTree familyTree;

	@InitBinder
	public void initBinder(WebDataBinder webdataBinder) {
		webdataBinder.registerCustomEditor(Order.class, new OrderConverter());
	}

	@GetMapping("/familyTree")
	public List<String> getOrderedFamilyList(@RequestParam Order order) {
		return familyTree.getSortedFamilyList(order).stream()
				.map(Person::getName)
				.collect(Collectors.toList());
	}

	@GetMapping(value = "/familyTree/{name}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public Person getPerson(@PathVariable String name) {
		Person person = familyTree.getPerson(name);
		if (person == null) {
			throw new NoSuchElementException();
		}
		return person;
	}

	@PostMapping(value = "/familyTree", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public void addPerson(@RequestBody PersonForm personForm) {
		familyTree.addPerson(personForm.getName(),
				personForm.getAge(),
				personForm.getParent1(),
				personForm.getParent2(),
				personForm.getChildren()
		);
	}

	@ExceptionHandler(NoSuchElementException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public void handleNoSuchElementException() {
		// returns 404 status
	}

	@ExceptionHandler(IllegalArgumentException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public void handleIllegalArgumentException() {
		// returns 400 status
	}

}
