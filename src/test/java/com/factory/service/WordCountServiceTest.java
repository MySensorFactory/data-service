package com.factory.service;

import com.factory.openapi.model.CountedWords;
import com.factory.persistence.repository.CountedWordsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

class WordCountServiceTest {

    @Mock
    private CountedWordsRepository countedWordsRepository;

    private final ModelMapper modelMapper = new ModelMapper();
    private WordCountService wordCountService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        wordCountService = new WordCountService(countedWordsRepository, modelMapper);
    }

    @Test
    void testGetCountedWordsByProbeIdValidId() {
        UUID probeId = UUID.randomUUID();
        com.factory.persistence.entity.CountedWords countedWordsEntity = new com.factory.persistence.entity.CountedWords();
        countedWordsEntity.setId(probeId);
        when(countedWordsRepository.findById(probeId)).thenReturn(Optional.of(countedWordsEntity));

        Optional<CountedWords> result = wordCountService.getCountedWordsByProbeId(probeId.toString());

        assertEquals(probeId, result.get().getId());
    }

    @Test
    void testGetCountedWordsByProbeIdInvalidId() {
        String invalidProbeId = "invalid-id";
        Optional<CountedWords> result = wordCountService.getCountedWordsByProbeId(invalidProbeId);

        assertEquals(Optional.empty(), result);
    }

    @Test
    void testGetAllCountedWords() {
        com.factory.persistence.entity.CountedWords countedWordsEntity1 = new com.factory.persistence.entity.CountedWords();
        countedWordsEntity1.setId(UUID.randomUUID());

        com.factory.persistence.entity.CountedWords countedWordsEntity2 = new com.factory.persistence.entity.CountedWords();
        countedWordsEntity2.setId(UUID.randomUUID());

        when(countedWordsRepository.findAll()).thenReturn(List.of(countedWordsEntity1, countedWordsEntity2));

        var result = wordCountService.getAllCountedWords();

        assertEquals(2, result.size());
    }

    @Test
    void testGetAllCountedWordsNoData() {
        when(countedWordsRepository.findAll()).thenReturn(List.of());

        var result = wordCountService.getAllCountedWords();

        assertEquals(0, result.size());
    }
}
