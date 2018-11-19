package com.example.familytree.entities;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This class represents a family member node - the main application's entity.
 */
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "name")
public class Person {
	private String name;
	private int age;
	@JsonIgnore
	private Person parent1;
	@JsonIgnore
	private Person parent2;
	@JsonIgnore
	private List<Person> children;

	private Person(Builder builder) {
		this.name = builder.name;
		this.age = builder.age;
		this.parent1 = builder.parent1;
		this.parent2 = builder.parent2;
		this.children = builder.children;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}

	public Person getParent1() {
		return parent1;
	}

	public void setParent1(Person parent1) {
		this.parent1 = parent1;
	}

	public Person getParent2() {
		return parent2;
	}

	public void setParent2(Person parent2) {
		this.parent2 = parent2;
	}

	public List<Person> getChildren() {
		return children;
	}

	public void addChild(Person child) {
		children.add(child);
	}

	@JsonGetter("parent1")
	public String getParent1Name() {
		if (parent1 != null) {
			return parent1.getName();
		}
		return null;
	}

	@JsonGetter("parent2")
	public String getParent2Name() {
		if (parent2 != null) {
			return parent2.getName();
		}
		return null;
	}

	@JsonGetter("children")
	public List<String> getChildrenNames() {
		if (children != null) {
			return children.stream()
					.map(Person::getName)
					.collect(Collectors.toList());
		}
		return null;
	}

	public static class Builder {
		private final String name;
		private final int age;
		private Person parent1;
		private Person parent2;
		private List<Person> children;

		public Builder(String name, int age) {
			this.name = name;
			this.age = age;
		}

		public Builder parent1(Person parent1) {
			this.parent1 = parent1;
			return this;
		}

		public Builder parent2(Person parent1) {
			this.parent2 = parent1;
			return this;
		}

		public Builder children(List<Person> children) {
			this.children = children;
			return this;
		}

		public Person build() {
			if (children == null) {
				children = new ArrayList<>();
			}
			return new Person(this);
		}
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Person person = (Person) o;

		if (age != person.age) return false;
		if (name != null ? !name.equals(person.name) : person.name != null) return false;
		if (parent1 != null ? !parent1.getName().equals(person.parent1.getName()) : person.parent1 != null) return false;
		if (parent2 != null ? !parent2.getName().equals(person.parent2.getName()) : person.parent2 != null) return false;
		return children != null ? children.equals(person.children) : person.children == null;

	}

	@Override
	public int hashCode() {
		int result = name != null ? name.hashCode() : 0;
		result = 31 * result + age;
		result = 31 * result + (parent1 != null ? parent1.getName().hashCode() : 0);
		result = 31 * result + (parent2 != null ? parent2.getName().hashCode() : 0);
		result = 31 * result + (children != null ? children.hashCode() : 0);
		return result;
	}

	@Override
	public String toString() {
		return "Person{" +
				"name='" + name + '\'' +
				", age=" + age +
				'}';
	}
}