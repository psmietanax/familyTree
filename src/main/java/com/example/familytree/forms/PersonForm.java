package com.example.familytree.forms;

import java.io.Serializable;
import java.util.List;

/**
 * Simple REST API form that represents a person object.
 * This objects is being converted from JSON representation.
 */
public class PersonForm implements Serializable {
	private String name;
	private Integer age;
	private String parent1;
	private String parent2;
	private List<String> children;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getAge() {
		return age;
	}

	public void setAge(Integer age) {
		this.age = age;
	}

	public String getParent1() {
		return parent1;
	}

	public void setParent1(String parent1) {
		this.parent1 = parent1;
	}

	public String getParent2() {
		return parent2;
	}

	public void setParent2(String parent2) {
		this.parent2 = parent2;
	}

	public List<String> getChildren() {
		return children;
	}

	public void setChildren(List<String> children) {
		this.children = children;
	}
}
