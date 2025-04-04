package pt.tecnico.distledger.server.exceptions;

public class UserException extends Exception {

    public final class ErrorMessages {
        private ErrorMessages() {
        }

        public static final String FAIL_CREATE_ACCOUNT = "Error: Account creation failed: ";
        public static final String FAIL_BALANCE = "Error: Balance access failed: ";
        public static final String FAIL_TRANSFER_TO = "Error: Transfer failed: ";

        public static final String DUPLICATE_ACCOUNT = "Account already exists: %s";
        public static final String DUPLICATE_OPERATION = "Operation was already added";
        public static final String NO_ACCOUNT = "Account does not exist: %s";
        public static final String NON_ZERO_BALANCE = "Account %s has non-zero balance";
        public static final String INVALID_BALANCE = "Invalid balance: %s doesn't have %s coins";
        public static final String SERVER_UNAVAILABLE = "Server is unavailable";
        public static final String INVALID_AMOUNT = "Transfer amount is not valid";
        public static final String TRANSFER_TO_SELF = "Origin and destination accounts are the same";
        public static final String DELETE_BROKER = "Broker account cannot be deleted";
    }

    private final String errorMessage;

    public UserException(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}