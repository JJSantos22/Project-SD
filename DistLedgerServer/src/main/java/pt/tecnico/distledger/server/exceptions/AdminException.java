package pt.tecnico.distledger.server.exceptions;

public class AdminException extends Exception {
    public final class ErrorMessages {
        private ErrorMessages() {
        }

        public static final String FAIL_ACTIVATE = "Error: Server activation failed: Server is already active";
        public static final String FAIL_DEACTIVATE = "Error: Server deactivation failed: Server is already inactive";
    }

    private final String errorMessage;

    public AdminException(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
