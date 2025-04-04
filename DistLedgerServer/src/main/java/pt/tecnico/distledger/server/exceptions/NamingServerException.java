package pt.tecnico.distledger.server.exceptions;

public class NamingServerException extends Exception {

    public final class ErrorMessages {
        private ErrorMessages() {
        }

        public static final String FAIL_REGISTER = "Error: Registration failed: ";
        public static final String FAIL_LOOKUP = "Error: Lookup operation failed: ";
        public static final String FAIL_DELETE = "Error: Server deletion failed: ";

        public static final String DUPLICATE_SERVER = "Not possible to register the server";
        public static final String NO_ACCOUNT = "Account does not exist: %s";
        public static final String INVALID_BALANCE = "Invalid balance: %s doesn't have %s coins";
        public static final String SERVER_UNAVAILABLE = "Server is unavailable";
    }

    private final String errorMessage;

    public NamingServerException(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
