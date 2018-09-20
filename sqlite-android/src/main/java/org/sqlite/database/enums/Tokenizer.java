package org.sqlite.database.enums;

public enum Tokenizer {

    HTML_TOKENIZER("HTMLTokenizer");

    private String name;

    Tokenizer(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}