package com.factory.service;

import com.factory.exception.ClientErrorException;
import com.factory.openapi.model.CountedWords;
import com.factory.openapi.model.Error;
import com.factory.persistence.repository.CountedWordsRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WordCountService {

    private final CountedWordsRepository countedWordsRepository;
    private final ModelMapper modelMapper;

    public List<CountedWords> getAllCountedWords() {
        List<com.factory.persistence.entity.CountedWords> result =
                (List<com.factory.persistence.entity.CountedWords>) countedWordsRepository.findAll();
        return result.stream()
                .map(entity -> modelMapper.map(entity, CountedWords.class))
                .toList();
    }

    public Optional<CountedWords> getCountedWordsByProbeId(String probeId) {
        UUID uuid;
        try {
            uuid = UUID.fromString(probeId);
        } catch (IllegalArgumentException ex) {
            throw new ClientErrorException(Error.CodeEnum.INVALID_INPUT.toString(), "Probe ID is not formatted like UUID.");
        }
        var result = countedWordsRepository.findById(uuid);
        return result.map(entity -> modelMapper.map(entity, CountedWords.class));
    }
}
