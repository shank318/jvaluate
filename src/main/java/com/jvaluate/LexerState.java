package com.jvaluate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LexerState {
    boolean isEOF;
    boolean isNullable;
    TokenKind kind;
    List<TokenKind> validNextKinds = new ArrayList();


    public static List<LexerState> getKinds() {

        List<LexerState> states = new ArrayList();

        LexerState unknown = new LexerState();
        unknown.kind = TokenKind.UNKNOWN;
        unknown.isEOF = false;
        unknown.isNullable = false;
        unknown.validNextKinds = Arrays.asList(TokenKind.NUMERIC, TokenKind.VARIABLE, TokenKind.STRING, TokenKind.TIME, TokenKind.CLAUSE);
        states.add(unknown);

        LexerState clause = new LexerState();
        clause.kind = TokenKind.CLAUSE;
        clause.isEOF = false;
        clause.isNullable = true;
        clause.validNextKinds = Arrays.asList(TokenKind.NUMERIC, TokenKind.VARIABLE, TokenKind.STRING, TokenKind.TIME, TokenKind.CLAUSE, TokenKind.CLAUSE_CLOSE);
        states.add(clause);

        LexerState clauseClose = new LexerState();
        clauseClose.kind = TokenKind.CLAUSE_CLOSE;
        clauseClose.isEOF = true;
        clauseClose.isNullable = true;
        clauseClose.validNextKinds = Arrays.asList(TokenKind.COMPARATOR, TokenKind.NUMERIC, TokenKind.VARIABLE, TokenKind.STRING, TokenKind.TIME, TokenKind.CLAUSE, TokenKind.CLAUSE_CLOSE, TokenKind.LOGICALOP, TokenKind.SEPARATOR);
        states.add(clauseClose);

        LexerState numeric = new LexerState();
        numeric.kind = TokenKind.NUMERIC;
        numeric.isEOF = true;
        numeric.isNullable = false;
        numeric.validNextKinds = Arrays.asList(TokenKind.COMPARATOR, TokenKind.LOGICALOP, TokenKind.CLAUSE_CLOSE, TokenKind.SEPARATOR);
        states.add(numeric);

        LexerState string = new LexerState();
        string.kind = TokenKind.STRING;
        string.isEOF = true;
        string.isNullable = false;
        string.validNextKinds = Arrays.asList(TokenKind.COMPARATOR, TokenKind.LOGICALOP, TokenKind.CLAUSE_CLOSE, TokenKind.SEPARATOR);
        states.add(string);

        LexerState time = new LexerState();
        time.kind = TokenKind.TIME;
        time.isEOF = true;
        time.isNullable = false;
        time.validNextKinds = Arrays.asList(TokenKind.COMPARATOR, TokenKind.LOGICALOP, TokenKind.CLAUSE_CLOSE, TokenKind.SEPARATOR);
        states.add(time);

        LexerState variable = new LexerState();
        variable.kind = TokenKind.VARIABLE;
        variable.isEOF = true;
        variable.isNullable = false;
        variable.validNextKinds = Arrays.asList(TokenKind.COMPARATOR, TokenKind.LOGICALOP, TokenKind.CLAUSE_CLOSE, TokenKind.SEPARATOR);
        states.add(variable);


        LexerState comparator = new LexerState();
        comparator.kind = TokenKind.COMPARATOR;
        comparator.isEOF = false;
        comparator.isNullable = false;
        comparator.validNextKinds = Arrays.asList(TokenKind.NUMERIC, TokenKind.VARIABLE, TokenKind.STRING, TokenKind.TIME, TokenKind.CLAUSE, TokenKind.CLAUSE_CLOSE);
        states.add(comparator);

        LexerState logical = new LexerState();
        logical.kind = TokenKind.LOGICALOP;
        logical.isEOF = false;
        logical.isNullable = false;
        logical.validNextKinds = Arrays.asList(TokenKind.NUMERIC, TokenKind.VARIABLE, TokenKind.STRING, TokenKind.TIME, TokenKind.CLAUSE, TokenKind.CLAUSE_CLOSE);
        states.add(logical);

        LexerState separator = new LexerState();
        separator.kind = TokenKind.LOGICALOP;
        separator.isEOF = false;
        separator.isNullable = true;
        separator.validNextKinds = Arrays.asList(TokenKind.NUMERIC, TokenKind.STRING, TokenKind.TIME, TokenKind.VARIABLE, TokenKind.CLAUSE);
        states.add(separator);

        return states;
    }

    private static boolean canTransitionTo(LexerState state, TokenKind kind) {


        for (TokenKind k : state.validNextKinds) {
            if (k.name() == kind.name()) {
                return true;
            }
        }

        return false;
    }

    public static void checkExpressionSyntax(List<ExpressionToken> tokens) throws JValuateException {


        ExpressionToken lastToken = null;
        LexerState state = getKinds().get(0);

        for (ExpressionToken token : tokens) {

            if (!canTransitionTo(state, token.kind)) {

                // call out a specific error for tokens looking like they want to be functions.
                if (lastToken.kind.name() == TokenKind.VARIABLE.name() && token.kind.name() == TokenKind.CLAUSE_CLOSE.name()) {
                    throw new JValuateException("Undefined function " + (String) lastToken.value, "JValuateException");
                }

                throw new JValuateException("Cannot transition token types from", "JValuateException");
            }

            state = getLexerStateForToken(token.kind);

            if (!state.isNullable && token.value == null) {
                throw new JValuateException("Token kind " + token.kind.name() + "cannot have a nil value", "JValuateException");
            }

            lastToken = token;
        }


        if (!state.isEOF) {
            throw new JValuateException("Unexpected end of expression", "JValuateException");
        }

    }

    public static LexerState getLexerStateForToken(TokenKind kind) throws JValuateException {

        for (LexerState state : getKinds()) {
            if (state.kind.name() == kind.name()) {
                return state;
            }
        }

        throw new JValuateException("No lexer state found for token kind" + kind.name(), "JValuateException");
    }


}
