package com.jvaluate;

public enum TokenKind {
    UNKNOWN,
    NUMERIC,
    STRING,
    TIME,
    VARIABLE,
    SEPARATOR,
    COMPARATOR,
    LOGICALOP,
    CLAUSE,
    CLAUSE_CLOSE,
}
