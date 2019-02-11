package edu.studyup.serviceImpl;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import edu.studyup.entity.Event;
import edu.studyup.entity.Location;
import edu.studyup.entity.Student;
import edu.studyup.util.DataStorage;
import edu.studyup.util.StudyUpException;

class EventServiceImplTest {

	EventServiceImpl eventServiceImpl;

	@BeforeAll
	static void setUpBeforeClass() throws Exception {
	}

	@AfterAll
	static void tearDownAfterClass() throws Exception {
	}

	@BeforeEach
	void setUp() throws Exception {
		eventServiceImpl = new EventServiceImpl();
		//Create Student
		Student student = new Student();
		student.setFirstName("John");
		student.setLastName("Doe");
		student.setEmail("JohnDoe@email.com");
		student.setId(1);
		
		//Create Event1
		Event event = new Event();
		event.setEventID(1);
		event.setDate(new Date());
		event.setName("Event 1");
		Location location = new Location(-122, 37);
		event.setLocation(location);
		List<Student> eventStudents = new ArrayList<>();
		eventStudents.add(student);
		event.setStudents(eventStudents);
		
		DataStorage.eventData.put(event.getEventID(), event);
	}

	@AfterEach
	void tearDown() throws Exception {
		DataStorage.eventData.clear();
	}

	@Test
	void testUpdateEventName_GoodCase() throws StudyUpException {
		int eventID = 1;
		eventServiceImpl.updateEventName(eventID, "Renamed Event 1");
		assertEquals("Renamed Event 1", DataStorage.eventData.get(eventID).getName());
	}
	
	@Test
	void testUpdateEvent_WrongEventID_badCase() {
		int eventID = 3;
		Assertions.assertThrows(StudyUpException.class, () -> {
			eventServiceImpl.updateEventName(eventID, "Renamed Event 3");
		  });
	}
	
	@Test
	void testBadCase() {
		assertEquals(DataStorage.eventData.size(), 1);
	}

	@Test
	void testUpdateEventName_NameTooLong_badCase() {
		int eventID = 1;
		Assertions.assertThrows(StudyUpException.class, () -> {
			eventServiceImpl.updateEventName(eventID, "A really really long event test name... This is greater than 20 characters"); // way more than 20
		});
	}
	
	@Test
	void testUpdateEventName_NameEqual20_GoodCase() throws StudyUpException {
		int eventID = 1;
		String name = "ATwentyCharacterName"; // 20 characters long
		eventServiceImpl.updateEventName(eventID, name);
		assertEquals(name, DataStorage.eventData.get(eventID).getName());
	}

	@Test
	void testUpdateEventName_NameLessThan20_GoodCase() throws StudyUpException {
		int eventID = 1;
		String name = "ShortName1"; // less than 20
		eventServiceImpl.updateEventName(eventID, name);
		assertEquals(name, DataStorage.eventData.get(eventID).getName());
	}

	@Test
	void testUpdateEventName_NoCharacters_GoodCase() throws StudyUpException {
		int eventID = 1;
		String name = ""; // 0 characters
		eventServiceImpl.updateEventName(eventID, name);
		assertEquals(name, DataStorage.eventData.get(eventID).getName());
	}

	@Test
	void testGetActiveEvents_OneActiveEvent_GoodCase() {
		List<Event> activeEvents = eventServiceImpl.getActiveEvents();
		assertEquals(activeEvents.size(), 1);
	}

	@Test
	void testGetActiveEvents_MultipleActiveEvent_GoodCase() {

		int numEvents = 5;

		for (int i = 2; i < numEvents; i++) {
			Event event = new Event();
			event.setEventID(i);
			event.setDate(new Date());
			event.setName("none");

			DataStorage.eventData.put(event.getEventID(), event);
		}

		List<Event> activeEvents = eventServiceImpl.getActiveEvents();
		assertEquals(activeEvents.size(), numEvents - 1);
	}

	@Test
	void testGetActiveEvents_MultipleActiveAndInactiveEvent_GoodCase() {
		int numEvents = 5;

		for (int i = 2; i < numEvents; i++) {
			Event event = new Event();
			event.setEventID(i);
			event.setDate(new Date());
			event.setName("none");

			DataStorage.eventData.put(event.getEventID(), event);
		}

		Event pastEvent = new Event();
		pastEvent.setEventID(numEvents + 1);
		pastEvent.setDate(new Date(System.currentTimeMillis() - 1000)); // 1000 ms from current time
		pastEvent.setName("Event 3");

		DataStorage.eventData.put(pastEvent.getEventID(), pastEvent);

		List<Event> activeEvents = eventServiceImpl.getActiveEvents();
		assertEquals(activeEvents.size(), numEvents - 1); 
	}

	@Test
	void testAddStudentToEvent_GoodCase() throws StudyUpException {
		int eventID = 2;
		Event event = new Event();
		event.setEventID(eventID);
		event.setDate(new Date());
		event.setName("none");

		DataStorage.eventData.put(event.getEventID(), event);

		Student student = new Student();
		student.setFirstName("John");
		student.setLastName("Doe");
		student.setEmail("JohnDoe@email.com");
		student.setId(1);

		eventServiceImpl.addStudentToEvent(student, eventID);

		Student addedStudent = DataStorage.eventData.get(eventID).getStudents().get(0);
		assertEquals(1, addedStudent.getId()); // asserts student with ID 1 is the first student in event 1's list of
												// students
	}

	@Test
	void testAddStudentToEvent_TooManyStudents_badCase() throws StudyUpException {
		int eventID = 1;
		Student student = new Student();
		student.setFirstName("John");
		student.setLastName("Doe");
		student.setEmail("JohnDoe@email.com");
		student.setId(1);

		eventServiceImpl.addStudentToEvent(student, eventID);
		eventServiceImpl.addStudentToEvent(student, eventID);
		// 2 added correctly

		Assertions.assertThrows(StudyUpException.class, () -> {
			eventServiceImpl.addStudentToEvent(student, eventID);
		});
		// this should be an exception because there cannot be more than 2 students in
		// an event, but it isnt --> BUG
	}

	@Test
	void testAddStudentToEvent_NotGoodEventID_GoodCase() throws StudyUpException {
		Student student = new Student();
		student.setFirstName("John");
		student.setLastName("Doe");
		student.setEmail("JohnDoe@email.com");
		student.setId(1);

		Assertions.assertThrows(StudyUpException.class, () -> {
			eventServiceImpl.addStudentToEvent(student, 0);
		});
	}

	@Test
	void testGetPastEvents_OneActiveAndPastEvent_GoodCase() throws StudyUpException {
		Event event = new Event();
		event.setEventID(1);
		event.setDate(new Date());
		event.setName("Event 1");

		DataStorage.eventData.put(event.getEventID(), event);
		

		Event pastEvent = new Event();
		pastEvent.setEventID(2);
		pastEvent.setDate(new Date(System.currentTimeMillis() - 1000)); // 1000 ms from current time
		pastEvent.setName("Event 2");

		DataStorage.eventData.put(pastEvent.getEventID(), pastEvent);

		List<Event> pastEvents = eventServiceImpl.getPastEvents();
		assertEquals(pastEvents.size(), 1);
	}

	@Test
	void testDeleteEvent_DeleteOneEvent_GoodCase() throws StudyUpException {
		Event event = new Event();
		event.setEventID(2);
		event.setDate(new Date());
		event.setName("Event 2");

		DataStorage.eventData.put(event.getEventID(), event);
	
		assertEquals(eventServiceImpl.deleteEvent(event.getEventID()), event);
	}
	
}
