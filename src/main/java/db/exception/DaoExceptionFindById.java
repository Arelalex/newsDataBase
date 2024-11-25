package db.exception;

public class DaoExceptionFindById extends RuntimeException {
    public DaoExceptionFindById(String message, Throwable cause) {
        super(message, cause);
    }
}
