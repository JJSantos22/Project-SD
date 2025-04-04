package pt.tecnico.distledger.userclient.exceptions;

public class UserException extends Exception {
    public final class ErrorMessages {
        private ErrorMessages() {}
        
        public static final String IO_ERROR = "Error: Couldn't connect to the server";

        public static final String INVALID_COMMAND = "Error: Invalid Command";
        public static final String INVALID_NUM_ARGS = "Error: Wrong number of arguments for command";
        public static final String UNKNOWN_COMMAND = "Error: Unknown command";
        public static final String INVALID_ARG_FORMAT = "Error: Invalid argument type";
        public static final String SERVER_NOT_FOUND = "Error: Server not found";
    }
    private final String errorMessage;

    public UserException(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}