package com.factory.controller;

import com.factory.openapi.model.CountedWords;
import com.factory.service.WordCountService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class WordCountControllerTest {

    @Mock
    private WordCountService wordCountService;

    private WordCountController wordCountController;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        wordCountController = new WordCountController(wordCountService);
    }

    @Test
    void testGetAllCountedWords() {
        CountedWords countedWords = new CountedWords();
        when(wordCountService.getAllCountedWords()).thenReturn(Collections.singletonList(countedWords));

        ResponseEntity<List<CountedWords>> response = wordCountController.getAllCountedWords();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void testGetCountedWordsByProbeIdValidId() {
        UUID probeId = UUID.randomUUID();
        CountedWords countedWords = new CountedWords();
        when(wordCountService.getCountedWordsByProbeId(probeId.toString())).thenReturn(Optional.of(countedWords));

        ResponseEntity<CountedWords> response = wordCountController.getCountedWordsByProbeId(probeId.toString());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(countedWords, response.getBody());
    }

    @Test
    void testGetCountedWordsByProbeIdInvalidId() {
        String invalidProbeId = "invalid-id";
        when(wordCountService.getCountedWordsByProbeId(invalidProbeId)).thenReturn(Optional.empty());

        ResponseEntity<CountedWords> response = wordCountController.getCountedWordsByProbeId(invalidProbeId);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
}
