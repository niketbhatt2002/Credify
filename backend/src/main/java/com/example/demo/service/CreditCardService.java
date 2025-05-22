package com.example.demo.service;

import com.example.demo.model.CreditCard;
import com.example.demo.util.ExcelReader;
import com.example.demo.util.SpellChecking;
import com.example.demo.util.Trie;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class handling business logic for credit card operations.
 * Provides methods for retrieving, filtering, and searching credit cards,
 * as well as supporting autocomplete and spell checking functionality.
 */
@Service
public class CreditCardService {

    // =========================================
    // Fields and Dependencies
    // =========================================

    private final List<CreditCard> creditCards;
    private final Trie trie;
    private final SpellChecking spellChecker;

    @Autowired
    private SearchHistoryService searchHistoryService;

    // =========================================
    // Constructor and Initialization
    // =========================================

    /**
     * Initializes the service by:
     * 1. Loading credit card data from Excel
     * 2. Building the Trie for autocomplete
     * 3. Initializing spell checker
     */
    public CreditCardService() {
        this.creditCards = ExcelReader.readCreditCardsFromExcel(
            "demo\\src\\main\\resources\\Credit Card Details - Sheet1 (1).xlsx");
        this.trie = new Trie();
        this.spellChecker = new SpellChecking(creditCards);
        buildTrie();
    }

    // =========================================
    // Core Card Retrieval Methods
    // =========================================

    /**
     * Returns all credit cards in the system.
     * @return List of all credit cards
     */
    public List<CreditCard> getAllCards() {
        return creditCards;
    }

    // =========================================
    // Filtering Methods
    // =========================================

    /**
     * Filters cards by bank name (case-insensitive).
     * @param cards List of cards to filter
     * @param bankName Bank name to filter by
     * @return Filtered list of cards
     */
    public List<CreditCard> getCardsByBank(List<CreditCard> cards, String bankName) {
        return cards.stream()
                .filter(card -> card.getBankName().trim().equalsIgnoreCase(bankName.trim()))
                .collect(Collectors.toList());
    }

    /**
     * Filters cards by annual fee range.
     * @param cards List of cards to filter
     * @param minFee Minimum fee (inclusive)
     * @param maxFee Maximum fee (inclusive)
     * @return Filtered list of cards
     */
    public List<CreditCard> getCardsByAnnualFees(List<CreditCard> cards, Double minFee, Double maxFee) {
        return cards.stream()
                .filter(card -> {
                    try {
                        double fee = Double.parseDouble(card.getAnnualFees().replace("$", "").trim());
                        return fee >= minFee && fee <= maxFee;
                    } catch (NumberFormatException e) {
                        System.err.println("Invalid annual fee format: " + card.getAnnualFees());
                        return false;
                    }
                })
                .collect(Collectors.toList());
    }

    /**
     * Filters cards by purchase interest rate range.
     * @param cards List of cards to filter
     * @param minInterest Minimum interest rate (inclusive)
     * @param maxInterest Maximum interest rate (inclusive)
     * @return Filtered list of cards
     */
    public List<CreditCard> getCardsByPurchaseInterestRate(List<CreditCard> cards, 
                                                         Double minInterest, Double maxInterest) {
        return cards.stream()
                .filter(card -> {
                    try {
                        double interestRate = Double.parseDouble(card.getPurchaseInterestRate()) * 100;
                        return interestRate >= minInterest && interestRate <= maxInterest;
                    } catch (NumberFormatException e) {
                        System.err.println("Invalid purchase interest rate format: " + card.getPurchaseInterestRate());
                        return false;
                    }
                })
                .collect(Collectors.toList());
    }

    /**
     * Filters cards by search term (case-insensitive).
     * Records the search term in search history.
     * @param cards List of cards to filter
     * @param searchTerm Term to search for
     * @return Filtered list of cards
     */
    public List<CreditCard> getCardsBySearchTerm(List<CreditCard> cards, String searchTerm) {
        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            searchHistoryService.recordSearch(searchTerm);
        }
        String lowerSearch = searchTerm.toLowerCase();
        return cards.stream()
                .filter(card -> {
                    String title = card.getCardTitle() != null ? card.getCardTitle().toLowerCase() : "";
                    return title.contains(lowerSearch);
                })
                .collect(Collectors.toList());
    }

    // =========================================
    // Search Support Methods
    // =========================================

    /**
     * Builds the Trie data structure for autocomplete functionality.
     * Indexes card titles, value propositions, and benefits.
     */
    private void buildTrie() {
        for (CreditCard card : creditCards) {
            String[] tokens = (card.getCardTitle() + " " + card.getProductValueProp() + " " + card.getProductBenefits())
                    .toLowerCase().split("\\s+");
            for (String token : tokens) {
                trie.insert(token);
            }
        }
    }

    /**
     * Provides autocomplete suggestions for a given prefix.
     * @param prefix The prefix to search for
     * @return List of matching suggestions
     */
    public List<String> getAutocompleteSuggestions(String prefix) {
        return trie.searchPrefix(prefix.toLowerCase());
    }

    /**
     * Provides spelling suggestions for a potentially misspelled word.
     * @param word The word to check
     * @return List of spelling suggestions
     */
    public List<String> getSpellingSuggestions(String word) {
        return spellChecker.getSuggestions(word, 2, 3); // Max 2 edits, return 3 suggestions
    }

    /**
     * Returns the frequency count of a word in the card database.
     * @param word The word to count
     * @return Frequency count of the word
     */
    public int getWordFrequency(String word) {
        return spellChecker.getWordFrequency(word);
    }
}