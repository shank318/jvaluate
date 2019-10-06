package src;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;


public class Parsing {


    public static List<ExpressionToken> parseTokens(String expression) throws MyException {
        List<ExpressionToken> tokens = new ArrayList<>();
        ExpressionToken token = new ExpressionToken();
        LexerStream lexerStream = new LexerStream(expression);
        LexerState lexerState = new LexerState();
        boolean found;
        lexerState = lexerState.getKinds().get(0);

        for (; lexerStream.canRead(); ) {
            ReadTokenResult result = readToken(lexerStream, lexerState);

            if (!result.completed) {
                break;
            }

            lexerState = LexerState.getLexerStateForToken(result.expressionToken.kind);
            // append this valid token
            tokens.add(result.expressionToken);
        }

        checkBalance(tokens);
        return tokens;
    }

    private static class ReadTokenResult {
        ExpressionToken expressionToken;
        boolean completed;
    }


    private static ReadTokenResult readToken(LexerStream stream, LexerState state) throws MyException {

        ExpressionToken ret = new ExpressionToken();
        Object tokenValue = null;
        ParseTimeResult tokenTime;
        String tokenString;
        TokenKind kind = TokenKind.UNKNOWN;

        char character;
        ReadUntilFalseResult readUntilFalseResult;
        ReadTokenResult result = new ReadTokenResult();

        // numeric is 0-9, or . or 0x followed by digits
        // string starts with '
        // variable is alphanumeric, always starts with a letter
        // bracket always means variable
        // symbols are anything non-alphanumeric
        // all others read into a buffer until they reach the end of the stream
        for (; stream.canRead(); ) {

            character = stream.readCharacter();
            if (character == ' ') {
                continue;
            }

            kind = TokenKind.UNKNOWN;

            // numeric constant
            if (isNumeric(character)) {
                if (stream.canRead() && character == '0') {

                    character = stream.readCharacter();
                    if (stream.canRead() && character == 'x') {
                        readUntilFalseResult = readUntilFalse(stream, false, true, true, "h");
                        int tokenValueInt = Integer.parseInt(readUntilFalseResult.buffer);

                        kind = TokenKind.NUMERIC;
                        tokenValue = tokenValueInt;
                        break;
                    } else {
                        stream.rewind(1);
                    }
                }
                tokenString = readTokenUntilFalse(stream, "n");
                try {
                    tokenValue = Float.parseFloat(tokenString);
                } catch (NumberFormatException e) {
                    throw new MyException("Unable to parse numeric value to float", "JValuateException");
                }

                kind = TokenKind.NUMERIC;
                break;
            }

            // comma, separator
            if (character == ',') {

                tokenValue = ",";
                kind = TokenKind.SEPARATOR;
                break;
            }

            // escaped variable
            if (character == '[') {

                readUntilFalseResult = readUntilFalse(stream, true, false, true, "ncb");
                kind = TokenKind.VARIABLE;

                if (!readUntilFalseResult.isCorrect) {
                    throw new MyException("Unclosed parameter bracket", "JValuateException");
                }

                // above method normally rewinds us to the closing bracket, which we want to skip.
                stream.rewind(-1);
                break;
            }

            // regular variable
            if (Character.isLetter(character)) {
                tokenString = readTokenUntilFalse(stream, "v");

                tokenValue = tokenString;
                kind = TokenKind.VARIABLE;

                // textual operator?
                if (tokenValue == "in" || tokenValue == "IN") {

                    // force lower case for consistency
                    tokenValue = "in";
                    kind = TokenKind.COMPARATOR;
                }

                break;
            }

            if (!isNotQuote(character)) {
                readUntilFalseResult = readUntilFalse(stream, true, false, true, "nq");
                tokenValue = readUntilFalseResult.buffer;
                if (!readUntilFalseResult.isCorrect) {
                    throw new MyException("Unclosed string literal", "JValuateException");
                }

                // advance the stream one position, since reading until false assumes the terminator is a real token
                stream.rewind(-1);

                // check to see if this can be parsed as a time.
                tokenTime = tryParseTime((String) tokenValue);
                if (tokenTime.isCorrect) {
                    kind = TokenKind.TIME;
                    tokenValue = tokenTime.time;
                } else {
                    kind = TokenKind.STRING;
                }
                break;
            }

            if (character == '(') {
                tokenValue = character;
                kind = TokenKind.CLAUSE;
                break;
            }

            if (character == ')') {
                tokenValue = character;
                kind = TokenKind.CLAUSE_CLOSE;
                break;
            }

            // must be a known symbol
            tokenString = readTokenUntilFalse(stream, "nln");
            tokenValue = tokenString;
            System.out.println(tokenString);
            OperatorSymbol symbol = OperatorSymbol.logicalSymbols().get(tokenString);

            if (symbol != null) {

                kind = TokenKind.LOGICALOP;
                break;
            }

            symbol = OperatorSymbol.comparatorSymbols().get(tokenString);
            if (symbol != null) {

                kind = TokenKind.COMPARATOR;
                break;
            }

            throw new MyException("Invalid token " + tokenString, "JValuateException");
        }

        ret.kind = kind;
        ret.value = tokenValue;
        result.expressionToken = ret;
        result.completed = (kind != TokenKind.UNKNOWN);
        return result;
    }


    private static boolean isDigit(char rune) {
        return Character.isDigit(rune);
    }

    private static boolean isHexDigit(char rune) {

        char character = Character.toLowerCase(rune);

        return Character.isDigit(character) ||
                character == 'a' ||
                character == 'b' ||
                character == 'c' ||
                character == 'd' ||
                character == 'e' ||
                character == 'f';
    }

    private static boolean isNumeric(char rune) {

        return Character.isDigit(rune) || rune == '.';
    }

    private static boolean isNotQuote(char rune) {

        return rune != '\'' && rune != '"';
    }

    private static boolean isNotAlphanumeric(char rune) {

        return !(Character.isDigit(rune) ||
                Character.isLetter(rune) ||
                rune == '(' ||
                rune == ')' ||
                rune == '[' ||
                rune == ']' || // starting to feel like there needs to be an `isOperation` func (#59)
                !isNotQuote(rune));
    }

    private static boolean isVariableName(char rune) {

        return Character.isLetter(rune) ||
                Character.isDigit(rune) ||
                rune == '_' ||
                rune == '.';
    }

    private static boolean isNotClosingBracket(char rune) {

        return rune != ']';
    }


    /*
	Checks the balance of tokens which have multiple parts, such as parenthesis.
*/
    public static void checkBalance(List<ExpressionToken> tokens) throws MyException {

        ExpressionToken token;
        int parens = 0;

        TokenStream stream = new TokenStream(tokens);

        for (; stream.hasNext(); ) {

            token = stream.next();
            if (token.kind.name() == TokenKind.CLAUSE.name()) {
                parens++;
                continue;
            }
            if (token.kind.name() == TokenKind.CLAUSE_CLOSE.name()) {
                parens--;
                continue;
            }
        }

        if (parens != 0) {
            throw new MyException("Unbalanced parenthesis", "JValuateException");
        }
    }

    /*
	Attempts to parse the [candidate] as a Time.
	Tries a series of standardized date formats, returns the Time if one applies,
	otherwise returns false through the second return.
*/
    private static ParseTimeResult tryParseTime(String candidate) {

        ParseTimeResult parseTimeResult = new ParseTimeResult();
        parseTimeResult.time = new Date();
        parseTimeResult.isCorrect = false;

        List<String> formats = Arrays.asList("yyyy-MM-dd",                         // RFC 3339
                "yyyy-MM-dd hh:mm",                   // RFC 3339 with minutes
                "yyyy-MM-dd hh:mm aaa"               // RFC 3339 with seconds
               );


        for (String format : formats) {

            ParseTimeResult result = tryParseExactTime(candidate, format);
            if (result.isCorrect) {
                return result;
            }
        }

        return parseTimeResult;
    }

    private static ParseTimeResult tryParseExactTime(String candidate, String format) {

        ParseTimeResult parseTimeResult = new ParseTimeResult();

        try {
            Date date1 = new SimpleDateFormat(format).parse(candidate);
            parseTimeResult.time = date1;
            parseTimeResult.isCorrect = true;
        } catch (ParseException e) {
            parseTimeResult.time = new Date();
            parseTimeResult.isCorrect = false;
        }

        return parseTimeResult;
    }

    private static class ParseTimeResult {
        Date time;
        boolean isCorrect;
    }

    private static class ReadUntilFalseResult {
        String buffer;
        boolean isCorrect;
    }

    private static int getFirstRune(String candidate) {

        for (char c : candidate.toCharArray()) {
            return (int) c;
        }

        return 0;
    }

    private static String readTokenUntilFalse(LexerStream stream, String type) {

        stream.rewind(1);
        ReadUntilFalseResult result = readUntilFalse(stream, false, true, true, type);
        return result.buffer;
    }

    /*
	Returns the string that was read until the given [condition] was false, or whitespace was broken.
	Returns false if the stream ended before whitespace was broken or condition was met.
*/
    private static ReadUntilFalseResult readUntilFalse(LexerStream stream, boolean includeWhitespace, boolean breakWhitespace, boolean allowEscaping, String type) {

        ReadUntilFalseResult result = new ReadUntilFalseResult();
        StringBuffer tokenBuffer = new StringBuffer();
        char character;
        boolean conditioned = false;


        for (; stream.canRead(); ) {

            character = stream.readCharacter();

            // Use backslashes to escape anything
            if (allowEscaping && character == '\\') {

                character = stream.readCharacter();
                tokenBuffer.append(character);
                continue;
            }

            if (Character.isSpaceChar(character)) {

                if (breakWhitespace && tokenBuffer.length() > 0) {
                    conditioned = true;
                    break;
                }
                if (!includeWhitespace) {
                    continue;
                }
            }

            if (type.equals("n") && isNumeric(character)) {
                tokenBuffer.append(character);
            } else if (type.equals("d") && isDigit(character)) {
                tokenBuffer.append(character);
            } else if (type.equals("h") && isHexDigit(character)) {
                tokenBuffer.append(character);
            } else if (type.equals("nln") && isNotAlphanumeric(character)) {
                tokenBuffer.append(character);
            } else if (type.equals("ncb") && isNotClosingBracket(character)) {
                tokenBuffer.append(character);
            } else if (type.equals("nq") && isNotQuote(character)) {
                tokenBuffer.append(character);
            } else if (type.equals("v") && isVariableName(character)) {
                tokenBuffer.append(character);
            } else {
                conditioned = true;
                stream.rewind(1);
                break;
            }

        }
        result.buffer = tokenBuffer.toString();
        result.isCorrect = conditioned;
        return result;
    }


}
