package pt.tecnico.distledger.server.exceptions;

public class CrossServerException extends Exception {

    public final class ErrorMessages {
        private ErrorMessages() {}

        public static final String FAIL_PROPAGATE_STATE = "State propagation failed: Secundary server is not running/is inactive";        
    }

    private final String errorMessage;
    private String qualifier = "";


    public CrossServerException(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public CrossServerException(String qualifier, String errorMessage) {
        this.qualifier = qualifier;
        this.errorMessage = errorMessage;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public String getQualifier() {
        return qualifier;
    }
}