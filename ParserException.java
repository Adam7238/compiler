package CE305_assignment2;

/**
 * Created by arkins on 08/02/2017.
 * The ParserException class throws an exception and
 * prints out the appropriate message for the error that
 * has occurred.
 */

class ParserException extends RuntimeException {
    ParserException(String msg) {
        super(msg);
    }
}