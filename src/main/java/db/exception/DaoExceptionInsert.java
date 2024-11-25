package db.exception;

public class DaoExceptionInsert extends RuntimeException {

    public DaoExceptionInsert(String message, Throwable cause) {
        super(message, cause);
    }
}
