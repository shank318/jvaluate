package com.jvaluate;

import java.util.ArrayList;
import java.util.List;

public class LexerStream {
    int length;
    int position;
    List<Integer> source;

    public LexerStream(String sourceString) {

        this.source = new ArrayList<Integer>();
        for (char c : sourceString.toCharArray()) {
            this.source.add((int) c);
        }

        this.length = this.source.size();
    }

    public char readCharacter() {
        char character = (char) ((int) this.source.get(this.position));
        this.position += 1;
        return character;

    }

    public void rewind(int amount) {
        this.position -= amount;
    }

    public boolean canRead() {
        return this.position < this.length;
    }
}
