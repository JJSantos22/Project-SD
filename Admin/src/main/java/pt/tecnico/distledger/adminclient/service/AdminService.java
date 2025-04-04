package pt.tecnico.distledger.adminclient.service;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;

import pt.tecnico.distledger.adminclient.exceptions.AdminException;
import static pt.tecnico.distledger.adminclient.exceptions.AdminException.ErrorMessages.*;

import pt.ulisboa.tecnico.distledger.contract.admin.AdminServiceGrpc;
import pt.ulisboa.tecnico.distledger.contract.admin.AdminDistLedger.*;

public class AdminService {

    private static final String ADMIN_SERVICE_STARTED = "AdminService created";
    private static final String ADMIN_SERVICE_SHUT_DOWN = "AdminService shut down";

    private static final String ACTIVATE_REQUEST = "Activate server request sent";
    private static final String ACTIVATE_RESPONSE = "Activate server response received";

    private static final String DEACTIVATE_REQUEST = "Deactivate server request sent";
    private static final String DEACTIVATE_RESPONSE = "Deactivate server response received";

    private static final String GOSSIP_REQUEST = "Gossip server request sent";
    private static final String GOSSIP_RESPONSE = "Gossip server response received";


    private static final String GET_SERVER_STATE_REQUEST = "Get server state request sent";
    private static final String GET_SERVERS_STATE_RESPONSE = "Get server state response received";

    /**
     * Set flag to true to print debug messages.
     * The flag can be set using the -Ddebug command line option.
     */
	private static final boolean DEBUG_FLAG = (System.getProperty("debug") != null);

    /** Helper method to print debug messages. */
    private static void debug(String debugMessage) {
        if (DEBUG_FLAG)
            System.err.println(debugMessage);
    }

    private final ManagedChannel channel;
    private AdminServiceGrpc.AdminServiceBlockingStub stub;
    private String address;
    private String qualifier;


    public AdminService(String qualifier, String address) {
        this.qualifier = qualifier;
        this.address = address;
        /** Creating the channel to the target server */
        this.channel = ManagedChannelBuilder.forTarget(address).usePlaintext().build();
        /** Creating a blocking stub for the channel */        
        this.stub = AdminServiceGrpc.newBlockingStub(this.channel);
        debug(ADMIN_SERVICE_STARTED);
    }

    /**
     * Send request from Admin to activate the server
     * 
     * @throws AdminException if an exception occurs while sending the request
     */
    public void activate() throws AdminException{
        try {
            debug(ACTIVATE_REQUEST);
            stub.activate(ActivateRequest.getDefaultInstance());
        } catch (StatusRuntimeException exception) {
            if (exception.getStatus().getCode().equals(Status.UNAVAILABLE.getCode())){
                throw new AdminException(IO_ERROR);
            }
            throw new AdminException(exception.getStatus().getDescription());
        }
        debug(ACTIVATE_RESPONSE);
    }

     /**
     * Send request from Admin to deactivate the server
     * 
     * @throws AdminException if an exception occurs while sending the request
     */
    public void deactivate() throws AdminException{
        try {
            debug(DEACTIVATE_REQUEST);
            stub.deactivate(DeactivateRequest.getDefaultInstance());
        } catch (StatusRuntimeException exception) {
            if (exception.getStatus().getCode().equals(Status.UNAVAILABLE.getCode())){
                throw new AdminException(IO_ERROR);
            }
            throw new AdminException(exception.getStatus().getDescription());
        }
        debug(DEACTIVATE_RESPONSE);
    }

    /**
     * Send gossip request to server
     * 
     * @throws AdminException if an exception occurs while sending the request
     */
    public void gossip() throws AdminException{
        try {
            debug(GOSSIP_REQUEST);
            stub.gossip(GossipRequest.getDefaultInstance());
        } catch (StatusRuntimeException exception) {
            if (exception.getStatus().getCode().equals(Status.UNAVAILABLE.getCode())){
                throw new AdminException(IO_ERROR);
            }
            throw new AdminException(exception.getStatus().getDescription());
        }
        debug(GOSSIP_RESPONSE);
    }
    
    
    /**
     * Send request from Admin to get the state of the server
     * 
     * @return the server Ledger State
     */
    public String getLedgerState() {
        debug(GET_SERVER_STATE_REQUEST);
        getLedgerStateResponse response = stub.getLedgerState(getLedgerStateRequest.getDefaultInstance());
        debug(GET_SERVERS_STATE_RESPONSE);
        return response.getLedgerState().toString();
    }

    /** Shut down the channel and the service */
    public void shutdown() {
        channel.shutdownNow();
		debug(ADMIN_SERVICE_SHUT_DOWN);
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setQualifier(String qualifier) {
        this.qualifier = qualifier;
    }

    public String getQualifier() {
        return qualifier;
    }

    public String getAddress() {
        return address;
    }
}
