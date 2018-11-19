package com.example.familytree.collections;

import com.example.familytree.enums.Order;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A simplified sorted list data structure.
 * The implementation is based on the HashMap where hashing function is replaced by an index.
 * This implementation provides constant-time performance for adding a new entry.
 * It stores a given number of buckets ordered by the integer index.
 * Buckets hold a singly linked list of nodes referencing to next elements.
 * When a new entry is added, a given bucket is amended by prepending the entry to a given list.
 * Example: adding the following pairs (3, test1), (1, test2), (2, test3), (1, test4), (2, test5),
 * creates the following ordered list: [(1, test4), (1, test2), (2, test5), (2, test3), (3, test1)].
 * The collection is not threads safe, so must be used within a thread-safe environment.
 */
public class SortedList<E> implements Iterable<E> {
	private final Node[] nodes;
	private final int maxSize;
	private int size;

	public SortedList(int maxSize) {
		if (maxSize < 1) {
			throw new IllegalArgumentException("Size must be greater than 0");
		}
		this.maxSize = maxSize;
		this.nodes = new Node[maxSize + 1];
	}

	/**
	 * Adds a new element to the sorted list.
	 * Performance: O(1)
	 */
	@SuppressWarnings("unchecked")
	public void add(int index, E elem) {
		if (index < 0 || index > maxSize) {
			throw new IndexOutOfBoundsException("Index must be with the range [0, " + maxSize + "]");
		}
		Node<E> nextNode = nodes[index];
		Node<E> node = new Node<>(elem, nextNode);
		nodes[index] = node;
		size++;
	}

	/**
	 * Returns an ordered java.util.List instance.
	 * Performance: O(N)
	 */
	public List<E> toOrderedList(Order order) {
		List<E> list = new ArrayList<>(size);
		Iterator<E> it = order == Order.ASC ? iterator() : reverseIterator();
		while (it.hasNext()) {
			list.add(it.next());
		}
		return list;
	}

	/**
	 * Returns the collection size.
	 * Performance: O(1)
	 */
	public int size() {
		return size;
	}

	/**
	 * Returns an ascending iterator.
	 */
	@Override
	public Iterator<E> iterator() {
		return new SortedListIterator();
	}

	/**
	 * Returns a descending iterator.
	 */
	public Iterator<E> reverseIterator() {
		return new SortedListReverseIterator();
	}

	/**
	 * A node class that represents a singly linked list element.
	 */
	private static class Node<E> {
		E item;
		Node<E> next;

		Node(E item, Node<E> next) {
			this.item = item;
			this.next = next;
		}
	}

	/**
	 * An ascending iterator.
	 */
	@SuppressWarnings("unchecked")
	private class SortedListIterator implements Iterator<E> {
		Node<E> next;
		Node<E> current;
		int index;

		SortedListIterator() {
			current = null;
			next = null;
			index = 0;
			// find first non-empty entry
			while (index < nodes.length && (next = nodes[index++]) == null) { }
		}

		@Override
		public boolean hasNext() {
			return next != null;
		}

		@Override
		public E next() {
			Node<E> e = next;
			if ((next = e.next) == null) {
				while (index < nodes.length && (next = nodes[index++]) == null) { }
			}
			return e.item;
		}
	}

	/**
	 * A descending iterator.
	 */
	@SuppressWarnings("unchecked")
	private class SortedListReverseIterator implements Iterator<E> {
		Node<E> next;
		Node<E> current;
		int index;

		SortedListReverseIterator() {
			current = null;
			next = null;
			index = nodes.length - 1;
			// find first non-empty entry
			while (index >= 0 && (next = nodes[index--]) == null) { }
		}

		@Override
		public boolean hasNext() {
			return next != null;
		}

		@Override
		public E next() {
			Node<E> e = next;
			if ((next = e.next) == null) {
				while (index >= 0 && (next = nodes[index--]) == null) { }
			}
			return e.item;
		}
	}

}

