package com.example.familytree;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = FamilyTreeApplication.class)
@AutoConfigureMockMvc
public class FamilyTreeApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	@Test
	public void testPersonDoesnExist() throws Exception {
		mockMvc.perform(get("/familyTree/test"))
				.andExpect(status().isNotFound());
	}

	@Test
	public void testPersonExists() throws Exception {
		// WHEN
		ObjectMapper objectMapper = new ObjectMapper();

		HashMap<String, Object> jsonMap = new HashMap<>();
		jsonMap.put("name", "Person1");
		jsonMap.put("age", 50);

		String json = objectMapper.writeValueAsString(jsonMap);

		mockMvc.perform(post("/familyTree")
				.contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
				.content(json))
				.andExpect(status().isOk());

		// THEN
		mockMvc.perform(get("/familyTree/Person1"))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE));
	}

	@Test
	public void testOrderedList() throws Exception {
		// WHEN
		// Person1
		ObjectMapper objectMapper = new ObjectMapper();

		HashMap<String, Object> jsonMap = new HashMap<>();
		jsonMap.put("name", "Person2");
		jsonMap.put("age", 50);

		String json = objectMapper.writeValueAsString(jsonMap);

		mockMvc.perform(post("/familyTree")
				.contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
				.content(json))
				.andExpect(status().isOk());

		// Person2
		jsonMap = new HashMap<>();
		jsonMap.put("name", "Person3");
		jsonMap.put("age", 30);

		json = objectMapper.writeValueAsString(jsonMap);

		mockMvc.perform(post("/familyTree")
				.contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
				.content(json))
				.andExpect(status().isOk());

		// THEN
		mockMvc.perform(get("/familyTree?order=ASC"))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE));

		mockMvc.perform(get("/familyTree?order=DESC"))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE));
	}
}