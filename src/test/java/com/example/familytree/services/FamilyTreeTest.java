package com.example.familytree.services;

import com.example.familytree.entities.Person;
import com.example.familytree.enums.Order;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FamilyTreeTest {

	private final DBService dbService = Mockito.mock(DBService.class);
	private final int maxAge = 100;

	@Test(expected = IllegalArgumentException.class)
	public void testWrongAge1() {
		FamilyTree familyTree = new FamilyTree(dbService, maxAge);
		familyTree.addPerson("Person1", 120, null, null, null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testWrongAge2() {
		FamilyTree familyTree = new FamilyTree(dbService, maxAge);
		familyTree.addPerson("Person1", 30, null, null, null);
		familyTree.addPerson("Person2", 20, null, null, Arrays.asList("Person1"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testWrongAge3() {
		FamilyTree familyTree = new FamilyTree(dbService, maxAge);
		familyTree.addPerson("Person1", 30, null, null, null);
		familyTree.addPerson("Person2", 50, "Person1", null, null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testNameAlreadyExists() {
		FamilyTree familyTree = new FamilyTree(dbService, maxAge);
		familyTree.addPerson("Person1", 20, null, null, null);
		familyTree.addPerson("Person1", 30, null, null, null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testParentDoesntExist() {
		FamilyTree familyTree = new FamilyTree(dbService, maxAge);
		familyTree.addPerson("Person1", 50, "Person2", null, null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testChildDoesntExist() {
		FamilyTree familyTree = new FamilyTree(dbService, maxAge);
		familyTree.addPerson("Person1", 50, null, null, Arrays.asList("Person2"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testChildAlreadyHasBothParents() {
		FamilyTree familyTree = new FamilyTree(dbService, maxAge);
		familyTree.addPerson("Person1", 50, null, null, null);
		familyTree.addPerson("Person2", 70, null, null, null);
		familyTree.addPerson("Person3", 30, "Person1", "Person2", null);
		familyTree.addPerson("Person4", 50, null, null, Arrays.asList("Person3"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testCycleDetectionWhenAddingParent() {
		FamilyTree familyTree = new FamilyTree(dbService, maxAge);
		familyTree.addPerson("Person1", 30, null, null, null);
		familyTree.addPerson("Person2", 40, null, null, null);
		familyTree.addPerson("Person3", 10, "Person1", "Person2", null);
		familyTree.addPerson("Person4", 60, null, null, Arrays.asList("Person1", "Person2"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testCycleDetectionWhenAddingChild() {
		FamilyTree familyTree = new FamilyTree(dbService, maxAge);
		familyTree.addPerson("Person1", 50, null, null, null);
		familyTree.addPerson("Person2", 70, null, null, null);
		familyTree.addPerson("Person3", 30, "Person1", "Person2", null);
		familyTree.addPerson("Person4", 10, "Person1", "Person3", null);
	}

	@Test
	public void testPrintUpwards() {
		// GIVEN:
		FamilyTree familyTree = buildFamilyTree();

		// WHEN:
		List<String> personNames = familyTree.printUpwards("Person26");

		// THEN:
		Assert.assertEquals(Arrays.asList("Person26", "Person19", "Person20", "Person12", "Person13", "Person14", "Person05", "Person06", "Person07"), personNames);
	}

	@Test
	public void testSortByAgeAsc() {
		// GIVEN:
		FamilyTree familyTree = buildFamilyTree();

		// WHEN:
		List<Person> persons = familyTree.getSortedFamilyList(Order.ASC);

		// THEN:
		for (int i=1; i<persons.size(); i++) {
			Assert.assertTrue(persons.get(i-1).getAge() <= persons.get(i).getAge());
		}
	}

	@Test
	public void testSortByAgeDesc() {
		// GIVEN:
		FamilyTree familyTree = buildFamilyTree();

		// WHEN:
		List<Person> persons = familyTree.getSortedFamilyList(Order.DESC);

		// THEN:
		for (int i=1; i<persons.size(); i++) {
			Assert.assertTrue(persons.get(i-1).getAge() >= persons.get(i).getAge());
		}
	}

	@Test
	public void testPersistenceLayerAddPerson() {
		// GIVEN:
		FamilyTree familyTree = new FamilyTree(dbService, maxAge);
		Person person = new Person.Builder("Person1", 50).build();

		// WHEN:
		familyTree.addPerson("Person1", 50, null, null, null);

		// THEN:
		Mockito.verify(dbService).savePerson(Mockito.refEq(person));
	}

	@Test
	public void testPersistenceLayerAddPersonWithParents() {
		// GIVEN:
		FamilyTree familyTree = new FamilyTree(dbService, maxAge);
		Person person1 = new Person.Builder("Person1", 50).build();
		Person person2 = new Person.Builder("Person2", 60).build();
		Person person3 = new Person.Builder("Person3", 20).build();
		Person person1Updated = new Person.Builder("Person1", 50).children(Arrays.asList(person3)).build();
		Person person2Updated = new Person.Builder("Person2", 60).children(Arrays.asList(person3)).build();
		person3.setParent1(person1Updated);
		person3.setParent2(person2Updated);

		// WHEN:
		familyTree.addPerson("Person1", 50, null, null, null);
		// THEN:
		Mockito.verify(dbService).savePerson(Mockito.refEq(person1));

		// WHEN:
		familyTree.addPerson("Person2", 60, null, null, null);
		// THEN:
		Mockito.verify(dbService).savePerson(Mockito.refEq(person2));

		// WHEN:
		familyTree.addPerson("Person3", 20, "Person1", "Person2", null);
		// THEN:
		Mockito.verify(dbService).savePerson(Mockito.refEq(person3));
		Mockito.verify(dbService).updatePerson(Mockito.refEq(person1Updated));
		Mockito.verify(dbService).updatePerson(Mockito.refEq(person2Updated));
	}

	@Test
	public void testPersistenceLayerAddPersonWithChildren() {
		// GIVEN:
		FamilyTree familyTree = new FamilyTree(dbService, maxAge);
		Person person1 = new Person.Builder("Person1", 50).build();
		Person person2 = new Person.Builder("Person2", 10).build();
		Person person3 = new Person.Builder("Person3", 20).build();
		Person person2Updated = new Person.Builder("Person2", 10).parent1(person1).build();
		Person person3Updated = new Person.Builder("Person3", 20).parent1(person1).build();
		person1.addChild(person2Updated);
		person1.addChild(person3Updated);

		// WHEN:
		familyTree.addPerson("Person2", 10, null, null, null);
		// THEN:
		Mockito.verify(dbService).savePerson(Mockito.refEq(person2));

		// WHEN:
		familyTree.addPerson("Person3", 20, null, null, null);
		// THEN:
		Mockito.verify(dbService).savePerson(Mockito.refEq(person3));

		// WHEN:
		familyTree.addPerson("Person1", 50, null, null, Arrays.asList("Person2", "Person3"));
		// THEN:
		Mockito.verify(dbService).savePerson(Mockito.refEq(person1));
		Mockito.verify(dbService).updatePerson(Mockito.refEq(person2Updated));
		Mockito.verify(dbService).updatePerson(Mockito.refEq(person3Updated));
	}

	@Test
	public void testAddPersonsConcurrently() throws InterruptedException {
		// GIVEN:
		FamilyTree familyTree = new FamilyTree(dbService, maxAge);

		// WHEN

		// number of running threads
		int T = 4;
		// number of elements each thread will add
		int M = 100;

		CountDownLatch startLatch = new CountDownLatch(T);
		CountDownLatch endLatch = new CountDownLatch(T);

		// Run two threads
		ExecutorService executorService = Executors.newFixedThreadPool(T);
		for (int i = 0; i < T; i++) {
			int idx = i;
			executorService.submit(() -> {
				startLatch.await();
				for (int j=0; j<M; j++) {
					familyTree.addPerson("Person" + (idx * M + j), 50, null, null, null);
				}
				endLatch.countDown();
				return null;
			});
			startLatch.countDown();
		}

		endLatch.await();

		// THEN
		Assert.assertEquals(T * M, familyTree.getSortedFamilyList(Order.ASC).size());
		for (int i=0; i < T * M; i++) {
			Assert.assertNotNull(familyTree.getPerson("Person" + i));
		}
	}

	private FamilyTree buildFamilyTree() {
		FamilyTree familyTree = new FamilyTree(dbService, maxAge);
		// level 1
		familyTree.addPerson("Person01", 90, null, null, null);
		familyTree.addPerson("Person02", 91, null, null, null);
		familyTree.addPerson("Person03", 92, null, null, null);
		familyTree.addPerson("Person04", 93, null, null, null);
		familyTree.addPerson("Person05", 91, null, null, null);
		familyTree.addPerson("Person06", 90, null, null, null);
		familyTree.addPerson("Person07", 95, null, null, null);
		// level 2
		familyTree.addPerson("Person08", 70, "Person01", "Person02", null);
		familyTree.addPerson("Person09", 71, null, null, null);
		familyTree.addPerson("Person10", 72, "Person03", "Person04", null);
		familyTree.addPerson("Person11", 73, null, null, null);
		familyTree.addPerson("Person12", 72, "Person05", null, null);
		familyTree.addPerson("Person13", 72, null, null, null);
		familyTree.addPerson("Person14", 76, "Person06", "Person07", null);
		familyTree.addPerson("Person15", 77, null, null, null);
		familyTree.addPerson("Person16", 75, null, null, null);
		// level 3
		familyTree.addPerson("Person17", 51, "Person08", "Person09", null);
		familyTree.addPerson("Person18", 54, "Person10", "Person11", null);
		familyTree.addPerson("Person19", 53, "Person12", null, null);
		familyTree.addPerson("Person20", 54, "Person13", "Person14", null);
		familyTree.addPerson("Person21", 55, "Person15", "Person16", null);
		familyTree.addPerson("Person22", 51, null, null, null);
		familyTree.addPerson("Person23", 57, null, null, null);
		// level 4
		familyTree.addPerson("Person24", 30, "Person17", "Person18", null);
		familyTree.addPerson("Person25", 31, "Person19", null, null);
		familyTree.addPerson("Person26", 30, "Person19", "Person20", null);
		familyTree.addPerson("Person27", 33, "Person19", "Person21", null);
		familyTree.addPerson("Person28", 35, "Person22", "Person23", null);
		// level 5
		familyTree.addPerson("Person29", 10, "Person24", "Person25", null);
		familyTree.addPerson("Person30", 11, "Person27", "Person28", null);
		familyTree.addPerson("Person31", 10, "Person27", "Person28", null);
		familyTree.addPerson("Person32", 13, "Person27", "Person28", null);

		return familyTree;
	}

}
