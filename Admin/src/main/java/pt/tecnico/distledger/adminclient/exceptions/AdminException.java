package pt.tecnico.distledger.adminclient.exceptions;

public class AdminException extends Exception {

    public final class ErrorMessages {
        private ErrorMessages() {}

        public static final String IO_ERROR = "Error: Couldn't connect to the server";

        public static final String INVALID_COMMAND = "Error: Invalid Command";
        public static final String INVALID_NUM_ARGS = "Error: Wrong number of arguments for command";
        public static final String UNKNOWN_COMMAND = "Error: Unknown command";
        public static final String SERVER_NOT_FOUND = "Error: Server not found";
    }
    private final String errorMessage;

    public AdminException(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}