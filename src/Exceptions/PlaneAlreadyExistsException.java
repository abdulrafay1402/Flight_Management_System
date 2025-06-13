package Exceptions;

public class PlaneAlreadyExistsException extends RuntimeException {
    public PlaneAlreadyExistsException(String message) {
        super(message);
    }
}
