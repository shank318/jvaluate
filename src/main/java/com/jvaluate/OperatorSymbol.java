package com.jvaluate;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum OperatorSymbol {

    VALUE,
    LITERAL,
    NOOP,
    EQ,
    NEQ,
    GT,
    LT,
    GTE,
    LTE,
    IN,

    AND,
    OR,
    SEPARATE;

    public static Map<String, OperatorSymbol> comparatorSymbols() {
        Map<String, OperatorSymbol> map = new HashMap();
        map.put("==", EQ);
        map.put("!=", NEQ);
        map.put(">", GT);
        map.put("<", LT);
        map.put(">=", GTE);
        map.put("<=", LTE);
        map.put("in", IN);
        return Collections.unmodifiableMap(map);
    }

    public static Map<String, OperatorSymbol> logicalSymbols() {
        Map<String, OperatorSymbol> map = new HashMap();
        map.put("&&", AND);
        map.put("||", OR);
        return Collections.unmodifiableMap(map);
    }

    public static Map<String, OperatorSymbol> separatorSymbols() {
        Map<String, OperatorSymbol> map = new HashMap();
        map.put(",", SEPARATE);
        return Collections.unmodifiableMap(map);
    }

    public boolean isModifierType(List<OperatorSymbol> candidate) {

        for (OperatorSymbol symbol : candidate) {
            if (this == symbol) {
                return true;
            }
        }

        return false;
    }

    public String String() {

        switch (this) {
            case NOOP:
                return "NOOP";
            case VALUE:
                return "VALUE";
            case EQ:
                return "=";
            case NEQ:
                return "!=";
            case GT:
                return ">";
            case LT:
                return "<";
            case GTE:
                return ">=";
            case LTE:
                return "<=";
            case AND:
                return "&&";
            case OR:
                return "||";
            case IN:
                return "in";
        }
        return "";
    }

}
