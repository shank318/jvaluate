package src;

import java.util.List;

public class TokenStream {
    List<ExpressionToken> tokens;
    int index;
    int tokenLength;

    public TokenStream(List<ExpressionToken> tokens) {
        this.tokens = tokens;
        this.tokenLength = tokens.size();
    }

    public void rewind() {
        this.index -= 1;
    }

    public ExpressionToken next() {

        ExpressionToken

                token = this.tokens.get(this.index);

        this.index += 1;
        return token;
    }

    public boolean hasNext() {
        return this.index < this.tokenLength;
    }
}
