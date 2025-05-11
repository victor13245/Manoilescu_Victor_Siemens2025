package com.siemens.internship;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class ItemService {
    @Autowired
    private ItemRepository itemRepository;
    private static final ExecutorService executor = Executors.newFixedThreadPool(10);
    //Folosim synchronizedList pentru a asigura lucrul pe aceeasi lista intre threaduri
    private final List<Item> processedItems = Collections.synchronizedList(new ArrayList<>());
    // Folosim AtomicInteger pentru contor pentru o mai mare siguranta in lucrul cu threaduri
    private final AtomicInteger processedCount = new AtomicInteger(0);

    public static final Logger log = LoggerFactory.getLogger(ItemService.class.getName());



    public List<Item> findAll() {
        return itemRepository.findAll();
    }

    public Optional<Item> findById(Long id) {
        return itemRepository.findById(id);
    }

    public Item save(Item item) {
        return itemRepository.save(item);
    }

    public void deleteById(Long id) {
        itemRepository.deleteById(id);
    }


    /**
     * Your Tasks
     * Identify all concurrency and asynchronous programming issues in the code
     * Fix the implementation to ensure:
     * All items are properly processed before the CompletableFuture completes
     * Thread safety for all shared state
     * Proper error handling and propagation
     * Efficient use of system resources
     * Correct use of Spring's @Async annotation
     * Add appropriate comments explaining your changes and why they fix the issues
     * Write a brief explanation of what was wrong with the original implementation
     *
     * Hints
     * Consider how CompletableFuture composition can help coordinate multiple async operations
     * Think about appropriate thread-safe collections
     * Examine how errors are handled and propagated
     * Consider the interaction between Spring's @Async and CompletableFuture
     */
    public List<Item> processItemsAsync() {
        processedItems.clear();
        processedCount.set(0);

        List<Long> itemIds = itemRepository.findAllIds();
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        //Procesam obiectele din lista separat intr-o functie
        for (Long id : itemIds) {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> processItem(id), executor);
            futures.add(future);
        }

        // Asteptam terminarea tuturor threadurilor pentru a avea lista complet procesata
        try {
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Item processing was interrupted", e);
        } catch (ExecutionException e) {
            throw new RuntimeException("Error occurred during item processing", e.getCause());
        }

        return processedItems;
    }


    // Functia de procesare
    private void processItem(Long id) {
        try {
            Thread.sleep(100);

            // Putem folosi Optional in loc sa verificam manual daca e null
            Optional<Item> optionalItem = itemRepository.findById(id);
            if (optionalItem.isEmpty())
                return;

            Item item = optionalItem.get();
            item.setStatus("PROCESSED");
            itemRepository.save(item);

            processedItems.add(item);
            processedCount.incrementAndGet();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Item processing interrupted for ID: " + id, e);
        } catch (Exception e) {
            throw new RuntimeException("Failed to process item ID: " + id, e);
        }
    }

}

