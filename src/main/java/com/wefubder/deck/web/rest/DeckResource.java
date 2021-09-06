package com.wefubder.deck.web.rest;


import com.wefubder.deck.service.DeckService;
import com.wefubder.deck.service.dto.DeckDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@CrossOrigin("*")
public class DeckResource {

    private final Logger log = LoggerFactory.getLogger(DeckResource.class);

    private final DeckService deckService;

    public DeckResource(DeckService deckService) {
        this.deckService = deckService;
    }


    @PostMapping("/decks")
    public ResponseEntity<DeckDTO> createDeck(@ModelAttribute DeckDTO deckDTO) throws Exception {
        log.debug("REST request to save Deck : {}", deckDTO);
        DeckDTO result = deckService.createNewDeck(deckDTO);
        return ResponseEntity.ok().body(result);
    }

    @GetMapping("/decks/{id}")
    public ResponseEntity<DeckDTO> getDeck(@PathVariable UUID id) throws IOException {
        log.debug("REST request to get Deck by id: ");
        DeckDTO result = deckService.getDeck(id);
        return ResponseEntity.ok().body(result);
    }

    @GetMapping("/decks")
    public ResponseEntity<List<DeckDTO>> getAllDecks(){
        log.debug("REST request to get Deck by id: ");
        return ResponseEntity.ok().body(deckService.getAllDecks());
    }
}
