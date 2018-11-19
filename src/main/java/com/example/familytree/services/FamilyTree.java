package com.example.familytree.services;

import com.example.familytree.collections.SortedList;
import com.example.familytree.entities.Person;
import com.example.familytree.enums.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

/**
 * A service that is responsible for managing a family tree.
 */
@Service
public class FamilyTree {
	/**
	 * A database persistence layer service.
	 */
	private DBService dbService;

	/**
	 * Name to person node map.
	 * Guarantees fast lookups for names.
	 */
	private HashMap<String, Person> nameToPerson;

	/**
	 * Contains a list of Person entities ordered by age.
	 */
	private SortedList<Person> sortedList;

	/**
	 * A read lock.
	 */
	private final Lock readLock;

	/**
	 * A write lock.
	 */
	private final Lock writeLock;

	/**
	 * Max age limit.
	 */
	private final int maxAge;

	@Autowired
	public FamilyTree(DBService dbService, @Value("${familyTree.maxAge}") int maxAge) {
		this.dbService = dbService;
		this.nameToPerson = new HashMap<>();
		this.maxAge = maxAge;
		this.sortedList = new SortedList<>(maxAge);

		ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
		this.readLock = readWriteLock.readLock();
		this.writeLock = readWriteLock.writeLock();
	}

	/**
	 * This function adds a new person node to a family tree.
	 * Performance: O(n); this is because of checking if a cycle exists.
	 */
	public void addPerson(String name, Integer age, String parent1Name, String parent2Name, List<String> childNames) {
		writeLock.lock();
		try {
			validate(name, age, parent1Name, parent2Name, childNames);

			Person parent1 = nameToPerson.get(parent1Name);
			Person parent2 = nameToPerson.get(parent2Name);

			List<Person> children = null;
			if (childNames != null && !childNames.isEmpty()) {
				children = childNames.stream()
						.map(c -> nameToPerson.get(c))
						.collect(Collectors.toList());
			}

			Person person = new Person.Builder(name, age)
					.parent1(parent1)
					.parent2(parent2)
					.children(children)
					.build();

			// update name mapping
			nameToPerson.put(name, person);
			// add to the sorted list
			sortedList.add(age, person);
			// persist the data
			dbService.savePerson(person);

			if (parent1 != null) {
				parent1.addChild(person);
				dbService.updatePerson(parent1);
			}
			if (parent2 != null) {
				parent2.addChild(person);
				dbService.updatePerson(parent2);
			}

			if (children != null) {
				for (Person child : children) {
					if (child.getParent1() == null) {
						child.setParent1(person);
					} else {
						child.setParent2(person);
					}
					dbService.updatePerson(child);
				}
			}
		} finally {
			writeLock.unlock();
		}
	}

	/**
	 * This function returns a person node for a given name.
	 * Performance: O(1)
	 */
	public Person getPerson(String name) {
		readLock.lock();
		try {
			return nameToPerson.get(name);
		} finally {
			readLock.unlock();
		}
	}

	/**
	 * This function prints the reverse family tree (upwards) from a node including both parents for each level.
	 * The function returns a list of names in the upwards order.
	 * The implementation is based on the Breadth-First Search (BFS) algorithm.
	 * Performance: O(N); this is because of the BFS algorithm complexity.
	 */
	public List<String> printUpwards(String name) {
		readLock.lock();
		try {
			Person person = nameToPerson.get(name);
			if (person == null) {
				throw new IllegalArgumentException("Name " + name + " doesn't exist");
			}

			List<String> names = new ArrayList<>();

			Deque<Person> queue = new ArrayDeque<>();
			queue.add(person);
			int depth = 0;
			int nodesToNextDepth = 1;

			while (!queue.isEmpty()) {
				person = queue.poll();
				if (depth == 0) {
					System.out.println("person: " + person.getName());
					names.add(person.getName());
				} else {
					for (int i=1; i<depth; i++) {
						System.out.print("grand ");
					}
					System.out.println("parent: " + person.getName());
					names.add(person.getName());
				}
				if (person.getParent1() != null) {
					queue.add(person.getParent1());
				}
				if (person.getParent2() != null) {
					queue.add(person.getParent2());
				}
				if (nodesToNextDepth == 1) {
					nodesToNextDepth = queue.size();
					depth++;
				} else {
					nodesToNextDepth--;
				}
			}
			return names;
		} finally {
			readLock.unlock();
		}
	}

	/**
	 * This function returns a sorted family list.
	 * The element order must be defined.
	 * Performance: O(N); this is because of the SortedList's toOrderedList() complexity.
	 */
	public List<Person> getSortedFamilyList(Order order) {
		readLock.lock();
		try {
			return sortedList.toOrderedList(order);
		} finally {
			readLock.unlock();
		}
	}

	/**
	 * Pretty print function.
	 * Performance: O(N); this is because of the getSortedFamilyList() complexity.
	 */
	public void printFamilyTree(Order order) {
		readLock.lock();
		try {
			for (Person person : getSortedFamilyList(order)) {
				StringBuilder str = new StringBuilder();

				str.append("Person: ");
				str.append(person.getName());

				str.append(", age: ");
				str.append(person.getAge());

				if (person.getParent1() != null && person.getParent2() != null) {
					str.append(", parents: ");
					str.append(person.getParent1().getName());
					str.append(", ");
					str.append(person.getParent2().getName());
				} else if (person.getParent1() != null) {
					str.append(", parent: ");
					str.append(person.getParent1().getName());
				} else if (person.getParent2() != null) {
					str.append(", parent: ");
					str.append(person.getParent2().getName());
				}

				List<Person> children = person.getChildren();
				if (children != null && !children.isEmpty()) {
					String childrenNames = children.stream()
							.map(Person::getName)
							.collect(Collectors.joining(", "));
					str.append(", children: ");
					str.append(childrenNames);
				}

				System.out.println(str.toString());
			}
		} finally {
			readLock.unlock();
		}
	}

	/**
	 * Check for a cycle when adding a child node.
	 * The implementation is based on the Depth-First Search (DFS) algorithm.
	 * Performance: O(N); this is because of the DFS algorithm complexity.
	 */
	private boolean isCycleUpwards(Person parent1, Person parent2) {
		Deque<Person> stack = new ArrayDeque<>();
		stack.add(parent1);
		stack.add(parent2);

		Set<Person> visited = new HashSet<>();

		while (!stack.isEmpty()) {
			Person person = stack.pollFirst();

			if (visited.contains(person)) {
				return true;
			}
			if (person.getParent1() != null) {
				stack.addFirst(person.getParent1());
			}
			if (person.getParent2() != null) {
				stack.addFirst(person.getParent2());
			}

			visited.add(person);
		}
		return false;
	}

	/**
	 * Check for a cycle when adding a parent node.
	 * The implementation is based on the Depth-First Search (DFS) algorithm.
	 * Performance: O(N); this is because of the DFS algorithm complexity.
	 */
	private boolean isCycleDownwards(List<Person> children) {
		Deque<Person> stack = new ArrayDeque<>();
		for (Person child : children) {
			stack.add(child);
		}

		Set<Person> visited = new HashSet<>();

		while (!stack.isEmpty()) {
			Person person = stack.pollFirst();

			if (visited.contains(person)) {
				return true;
			}
			for (Person child : person.getChildren()) {
				stack.addFirst(child);
			}

			visited.add(person);
		}
		return false;
	}

	private void validate(String name, Integer age, String parent1Name, String parent2Name, List<String> childNames) {
		// name & age
		if (nameToPerson.containsKey(name)) {
			throw new IllegalArgumentException("Name " + name + " is already defined");
		}

		if (age == null || age < 0 || age > maxAge) {
			throw new IllegalArgumentException("Age must be within the range [0, " + maxAge + "]");
		}

		// parents
		if (parent1Name != null) {
			validateParent(parent1Name, age);
		}

		if (parent2Name != null) {
			validateParent(parent2Name, age);
		}

		// check whether adding a node introduces a cycle
		if (parent1Name != null
				&& parent2Name != null
				&& isCycleUpwards(nameToPerson.get(parent1Name), nameToPerson.get(parent2Name))) {
			throw new IllegalArgumentException("Cycle detected when adding a child node");
		}

		// children
		if (childNames != null) {
			for (String childName : childNames) {
				validateChild(childName, age);
			}

			List<Person> children = childNames.stream()
					.map(c -> nameToPerson.get(c))
					.collect(Collectors.toList());

			if (isCycleDownwards(children)) {
				throw new IllegalArgumentException("Cycle detected when adding a parent node");
			}
		}
	}

	private void validateParent(String parentName, int age) {
		Person parent = nameToPerson.get(parentName);
		if (parent == null) {
			throw new IllegalArgumentException("Name " + parentName + " doesn't exist");
		}
		if (age >= parent.getAge()) {
			throw new IllegalArgumentException("Given age cannot be greater than or equal to a parent age");
		}
	}

	private void validateChild(String childName, int age) {
		Person child = nameToPerson.get(childName);
		if (child == null) {
			throw new IllegalArgumentException("Name " + childName + " doesn't exist");
		}
		if (age <= child.getAge()) {
			throw new IllegalArgumentException("Given age cannot be less than or equal to a child age");
		}
		if (child.getParent1() != null && child.getParent2() != null) {
			throw new IllegalArgumentException("Person " + childName + " already has both parents");
		}
	}
}
