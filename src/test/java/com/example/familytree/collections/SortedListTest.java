package com.example.familytree.collections;

import com.example.familytree.enums.Order;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

public class SortedListTest {

	@Test(expected = IllegalArgumentException.class)
	public void testWrongMaxSize() {
		new SortedList<>(0);
	}

	@Test(expected = IndexOutOfBoundsException.class)
	public void testWrongIndex() {
		SortedList<Object> sortedList = new SortedList<>(1);
		sortedList.add(2, new Object());
	}

	@Test
	public void testIntegerAscOrder() {
		// GIVEN:
		SortedList<Integer> sortedList = new SortedList<>(5);

		// WHEN:
		sortedList.add(1, 1);
		sortedList.add(4, 4);
		sortedList.add(0, 0);
		sortedList.add(2, 2);
		sortedList.add(5, 5);
		sortedList.add(3, 3);

		// THEN:
		Assert.assertEquals(6, sortedList.size());
		Assert.assertEquals(Arrays.asList(0, 1, 2, 3, 4, 5), sortedList.toOrderedList(Order.ASC));
	}

	@Test
	public void testIntegerDescOrder() {
		// GIVEN:
		SortedList<Integer> sortedList = new SortedList<>(5);

		// WHEN:
		sortedList.add(1, 1);
		sortedList.add(4, 4);
		sortedList.add(0, 0);
		sortedList.add(2, 2);
		sortedList.add(5, 5);
		sortedList.add(3, 3);

		// THEN:
		Assert.assertEquals(6, sortedList.size());
		Assert.assertEquals(Arrays.asList(5, 4, 3, 2, 1, 0), sortedList.toOrderedList(Order.DESC));
	}



}
