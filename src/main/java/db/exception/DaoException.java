package db.exception;

public class DaoException extends RuntimeException {

    public DaoException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
