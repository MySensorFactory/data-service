package com.factory.controller;

import com.factory.exception.ClientErrorException;
import com.factory.openapi.api.CountedWordsApi;
import com.factory.openapi.model.CountedWords;
import com.factory.openapi.model.Error;
import com.factory.service.WordCountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class WordCountController implements CountedWordsApi {

    private final WordCountService wordCountService;

    @Override
    public ResponseEntity<List<CountedWords>> getAllCountedWords() {
        List<CountedWords> countedWordsList = wordCountService.getAllCountedWords();
        return ResponseEntity.ok(countedWordsList);
    }

    @Override
    public ResponseEntity<CountedWords> getCountedWordsByProbeId(String probeId) {
        Optional<CountedWords> countedWordsOptional = wordCountService.getCountedWordsByProbeId(probeId);
        return countedWordsOptional.map(ResponseEntity::ok).orElseThrow(
                () -> new ClientErrorException(Error.CodeEnum.NOT_FOUND.toString(), String.format("Probe with given id %s not found", probeId)));
    }
}
