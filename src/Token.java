

/**
 * Created by arkins on 12/02/2017.
 * This Class will create the token objects used
 * by the parser to create tree nodes.
 * It takes arguments that are taken from the results
 * of the Regex matching process within the Tokeniser class.
 */
  class Token {
    final int token;
    final String value;
    final int errorTracker;

    Token(int token, String value, int errorTracker) {
        this.token = token;
        this.value = value;
        this.errorTracker = errorTracker;
    }
}
