package pt.tecnico.distledger.namingserver.exceptions;

public class NamingServerException extends Exception { 

    public final class ErrorMessages{
        
        private ErrorMessages(){}
        
        public static final String FAIL_REGISTER = "Error: Service register failed: ";
        public static final String FAIL_LOOKUP = "Error: Service lookup failed: ";
        public static final String FAIL_DELETE = "Error: Service deletion failed: ";

        public static final String DUPLICATE_NAME = "Service name already registered: %s";
        public static final String NO_SERVICE = "Service does not exist: %s";

    }
    private final String errorMessage;

    public NamingServerException(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
    
}
