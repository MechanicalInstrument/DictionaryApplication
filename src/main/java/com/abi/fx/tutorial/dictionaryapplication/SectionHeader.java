package com.abi.fx.tutorial.dictionaryapplication;

/**
 * Represents a section header in the word list (e.g., "A", "B", "C")
 * Used to divide words alphabetically in the ListView
 */
public record SectionHeader(String letter) {
    @Override
    public String toString() {
        return letter;
    }
}

