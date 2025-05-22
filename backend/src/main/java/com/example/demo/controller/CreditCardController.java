package com.example.demo.controller;

import com.example.demo.model.CreditCard;
import com.example.demo.service.CreditCardService;
import com.example.demo.service.SearchHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

/**
 * REST controller for handling credit card-related operations.
 * Provides endpoints for retrieving, filtering, and searching credit cards,
 * as well as managing search history.
 */
@RestController
@RequestMapping("/api/creditcards") // Base URL for all endpoints in this controller
public class CreditCardController {

    // Dependency injections
    @Autowired
    private CreditCardService creditCardService;

    @Autowired
    private SearchHistoryService searchHistoryService;

    // =============================================
    // Credit Card Retrieval and Filtering Endpoints
    // =============================================

    /**
     * Retrieves all credit cards with optional filtering parameters.
     * 
     * @param bankName    Filter by bank name
     * @param minFee      Minimum annual fee
     * @param maxFee      Maximum annual fee
     * @param minInterest Minimum purchase interest rate
     * @param maxInterest Maximum purchase interest rate
     * @param search      Search term for filtering
     * @return List of filtered credit cards
     */
    @CrossOrigin(origins = "http://localhost:3000")
    @GetMapping
    public List<CreditCard> getAllCards(
            @RequestParam(required = false) String bankName,
            @RequestParam(required = false) Double minFee,
            @RequestParam(required = false) Double maxFee,
            @RequestParam(required = false) Double minInterest,
            @RequestParam(required = false) Double maxInterest,
            @RequestParam(required = false) String search) {

        List<CreditCard> filteredCards = creditCardService.getAllCards();

        // Apply all filters sequentially
        if (bankName != null) {
            filteredCards = creditCardService.getCardsByBank(filteredCards, bankName);
        }
        if (minFee != null && maxFee != null) {
            filteredCards = creditCardService.getCardsByAnnualFees(filteredCards, minFee, maxFee);
        }
        if (minInterest != null && maxInterest != null) {
            filteredCards = creditCardService.getCardsByPurchaseInterestRate(filteredCards, minInterest, maxInterest);
        }
        if (search != null && !search.trim().isEmpty()) {
            filteredCards = creditCardService.getCardsBySearchTerm(filteredCards, search.trim().toLowerCase());
        }

        return filteredCards;
    }

    // ======================
    // Search Support Endpoints
    // ======================

    /**
     * Provides autocomplete suggestions based on a prefix.
     * 
     * @param prefix The prefix to search for
     * @return List of matching suggestions
     */
    @CrossOrigin(origins = "http://localhost:3000")
    @GetMapping("/autocomplete")
    public List<String> getAutocompleteSuggestions(@RequestParam String prefix) {
        return creditCardService.getAutocompleteSuggestions(prefix);
    }

    /**
     * Provides spelling suggestions for a potentially misspelled word.
     * 
     * @param word The word to check
     * @return List of spelling suggestions
     */
    @CrossOrigin(origins = "http://localhost:3000")
    @GetMapping("/spelling-suggestions")
    public List<String> getSpellingSuggestions(@RequestParam String word) {
        return creditCardService.getSpellingSuggestions(word);
    }

    /**
     * Returns the frequency count of a specific word in the credit card database.
     * 
     * @param word The word to count
     * @return Response containing the word and its count
     */
    @CrossOrigin(origins = "http://localhost:3000")
    @GetMapping("/word-frequency")
    public ResponseEntity<?> getWordFrequency(@RequestParam String word) {
        int count = creditCardService.getWordFrequency(word);
        return ResponseEntity.ok(Map.of("word", word, "count", count));
    }

    // ======================
    // Search History Endpoints
    // ======================

    /**
     * Retrieves popular search terms.
     * 
     * @param limit Maximum number of popular searches to return
     * @return Map of search terms and their counts
     */
    @CrossOrigin(origins = "http://localhost:3000")
    @GetMapping("/search-history")
    public ResponseEntity<Map<String, Integer>> getSearchHistory(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(searchHistoryService.getPopularSearches(limit));
    }

    /**
     * Clears all search history.
     * 
     * @return No content response
     */
    @CrossOrigin(origins = "http://localhost:3000")
    @DeleteMapping("/search-history")
    public ResponseEntity<Void> clearSearchHistory() {
        searchHistoryService.clearHistory();
        return ResponseEntity.noContent().build();
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @PostMapping("/search-history")
    public ResponseEntity<Void> recordSearch(@RequestBody Map<String, String> request) {
        String term = request.get("term");
        if (term != null && !term.trim().isEmpty()) {
            searchHistoryService.recordSearch(term);
        }
        return ResponseEntity.ok().build();
    }
}