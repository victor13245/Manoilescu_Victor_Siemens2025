package com.siemens.internship;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static org.mockito.Mockito.*;

@SpringBootTest
class InternshipApplicationTests {

	@Mock
	private ItemRepository itemRepository;

	@InjectMocks
	private ItemService itemService;

	@BeforeEach
	void setup() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	void testSave() {
		Item item = new Item();
		item.setStatus("TEST");
		when(itemRepository.save(item)).thenReturn(item);

		Item result = itemService.save(item);
		assertEquals("TEST", result.getStatus());
	}

	@Test
	void testFindAll() {
		List<Item> mockItems = List.of(new Item(), new Item());
		when(itemRepository.findAll()).thenReturn(mockItems);

		List<Item> result = itemService.findAll();
		assertEquals(2, result.size());
		verify(itemRepository).findAll();
	}

	@Test
	void testFindById() {
		Item item = new Item();
		item.setId(1L);
		when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

		Optional<Item> result = itemService.findById(1L);
		assertTrue(result.isPresent());
		assertEquals(1L, result.get().getId());
	}

	@Test
	void testDeleteById() {
		Long id = 1L;
		itemService.deleteById(id);
		verify(itemRepository).deleteById(id);
	}

	@Test
	void testProcessItemsAsync() {
		List<Long> ids = List.of(1L, 2L);
		when(itemRepository.findAllIds()).thenReturn(ids);

		Item item1 = new Item();
		item1.setId(1L);
		Item item2 = new Item();
		item2.setId(2L);

		when(itemRepository.findById(1L)).thenReturn(Optional.of(item1));
		when(itemRepository.findById(2L)).thenReturn(Optional.of(item2));
		when(itemRepository.save(any())).thenAnswer(i -> i.getArgument(0));

		List<Item> processed = itemService.processItemsAsync();

		assertEquals(2, processed.size());
		for (Item item : processed) {
			assertEquals("PROCESSED", item.getStatus());
		}

		verify(itemRepository, times(2)).save(any());
	}

	@Test
	void contextLoads() {
	}

}
