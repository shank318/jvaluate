package src;

public enum OperatorPrecedence {
    noopPrecedence,
    valuePrecedence,
    comparatorPrecedence,
    logicalAndPrecedence,
    logicalOrPrecedence,
    separatePrecedence;

    public static OperatorPrecedence findOperatorPrecedenceForSymbol(OperatorSymbol symbol) {
        switch (symbol) {
            case NOOP:
                return noopPrecedence;
            case VALUE:
                return valuePrecedence;
            case EQ:
            case NEQ:
            case GT:
            case LT:
            case GTE:
            case LTE:
            case IN:
                return comparatorPrecedence;
            case AND:
                return logicalAndPrecedence;
            case OR:
                return logicalOrPrecedence;
            case SEPARATE:
                return separatePrecedence;
        }

        return valuePrecedence;
    }
}
