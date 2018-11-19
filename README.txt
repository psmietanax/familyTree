This project is a family tree service implementation.

It uses Spring Boot and the gradle build tool to manage builds and dependencies.
A user can store and retrieve family members specifying a name, age, parents and children using REST API.

To run the service, please follow the following steps:
1) Build a project jar
  Run './gradlew bootJar' from the main project directory.
2) Copy generated jar into script directory:
  Run 'mv build/libs/familytree-0.0.1-SNAPSHOT.jar bin/jar' from the main project directory.
3) Run the main project script:
  Run 'bin/familytree_start.sh' from the main project directory.

The service is running on http://localhost:8080

REST API endpoints:
* GET http://localhost:8080/familyTree?order=<ORDER>
  Where <ORDER> can be: ASC or DESC
  Returns sorted family list JSON either in ASC or DESC order.

* GET http://localhost:8080/familyTree/<name>
  Returns a family member node JSON associated with the given name.

* POST http://localhost:8080/familyTree
  Creates a new family member.
  Request body fields:
  - name: String (required)
  - age: Integer (required)
  - parent1: String
  - parent2: String
  - children: Array[String]

The application was run and tested in IntelliJ IDEA Ultimate 2018.2 and Gradle 4.8.1.

Answers:

Q1:
The data structure the application uses is backed by a HashMap and a SortedList (first principle data structure).
The main project entity is Person; it contains name, age, references to its parents and children.

The HashMap provides fast name loopkup, whereas the SortedList holds nodes ordered by persons' age.

The SortedList internally holds buckets assigned to an age (an integer). Assuming the age must be within the range of [0, 100],
it holds 101 buckets - each assigned to a given age. Every bucket holds a singly-linked list of nodes with the same age.
Adding a new node to a bucket prepends the node to already existing list.

E.g. adding (3, test3) to the following list:

[0] -> null
[1] -> (1, test1) -> null
[2] -> null
[3] -> (3, test2) -> null
[4] -> null
...

produces the following structure:

[0] -> null
[1] -> (1, test1) -> null
[2] -> null
[3] -> (3, test3) -> (3, test2) -> null
[4] -> null
...

A sorted list can be easily returned by iterating over each bucket in linear time.
Adding a new node is fast and can be achieved in O(1).

The SortedList doesn't keep insertion order for the same bucket as the only requirement we have is to sort a family tree
in age ascending/descending order - the order among entries from the same bucket is irrelevant.
However, this could be easily fixed by introducing a doubly-linked list for each bucket.
Also, the SortedList just implements adding a new entry, as at the moment removing entries is not required.
This can also be easily added to the current implementation.

--
The FamilyTree#addPerson method is responsible for adding a new Person entry. Due to validation checks, it can be executed
in O(n) mostly because of cycle detection methods that use the Depth-First Search algorithm performed in O(n).

Q2:
Adding an entry to the SortedList can be performed in constant time O(1). Sorting the list means iterating over the SortedList
entries, which can be done in O(n).

My initial thought was to use a balanced binary tree (e.g. TreeMap) where adding a new node takes O(log n) and getting the
sorted list is performed in O(n). As with the first principles, the application uses simpler and faster collection - SortedList.

Q3:
Similarly to Q2, the SortedList can be traversed both in ASC and DESC orders. A user can define a specific order to get the sorted lists.
This operation can also be performed in O(n).

Q4:
The FamilyTree#printFamilyTree method method is responsible for printing a family tree in a given order.
The method uses SortedList to get all of the entries, which means it can be executed in O(n).

Q5:
The FamilyTree#printUpwards method is responsible for printing the reverse family tree from a node including both parents for each level.
It uses the Breadth-First Search algorithm and is executed in O(n).

Q6:
As already mentioned in Q1 - with the SortedList, we can insert a new element with constant time O(1).
All we have to do is to get the age value and prepend a newly created node to a bucket that holds the give age nodes.

Q7:
This is done by creating the DBService interface and injecting a mocked version whenever a new FamilyTree service is created.

Additional scope:
0. All of the unit tests can be found under src/test/java directory.
1. The application is running on Spring Boot with the REST API mentioned above.
2. Swagger v2 is used (please note the SwaggerConfig class).
3. The FamilyTree uses the ReadWrite locks for all of the operations that make it thread-safe.