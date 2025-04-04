package pt.tecnico.distledger.server.domain;

import pt.tecnico.distledger.server.domain.operation.*;
import static pt.tecnico.distledger.server.exceptions.UserException.ErrorMessages.*;
import static pt.tecnico.distledger.server.exceptions.AdminException.ErrorMessages.*;
import static pt.tecnico.distledger.server.exceptions.CrossServerException.ErrorMessages.*;
import pt.tecnico.distledger.server.exceptions.UserException;
import pt.tecnico.distledger.server.service.CrossServerDistLedgerService;
import pt.tecnico.distledger.server.service.NamingServerDistLedgerService;
import pt.ulisboa.tecnico.distledger.contract.DistLedgerCommonDefinitions.Server;
import pt.tecnico.distledger.server.exceptions.AdminException;
import pt.tecnico.distledger.server.exceptions.CrossServerException;
import pt.tecnico.distledger.server.exceptions.NamingServerException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.HashMap;

public class ServerState {
    private static final String DEFAULT_ACCOUNT = "broker";
    private static final String DEFAULT_SERVICE = "DistLedger";

    private static final String ACCOUNT_CREATED = "Account created: %s";
    private static final String OPERATION_REGISTERED = "New operation was registered on ledger";

    private static final String SERVER_ACTIVATED = "Server activated";
    private static final String SERVER_DEACTIVATED = "Server deactivated";

    private static final String LEDGE_ACCESSED = "Ledge accessed";

    private static final String TRANSFER_TO = "Transfer Operation: \n\tFrom: %s\n\tTo: %s\n\tAmount: %d";

    private static final String BALANCE_ACCESSED = "Account Balance Accessed: \n\tUserId: %s\n\tAmount: %s";


    /** Server Configurations */
    private boolean activated = true;

    /** List that saves all the operations executed */
    private List<Operation> ledger;

    /** Map to save the accounts by the user_id */
    private Map<String, Integer> accounts;

    /** Map to save the crossServices by the qualifier */
    private Map<String, CrossServerDistLedgerService> crossServerServices;

    private NamingServerDistLedgerService namingService;

    private String qualifier;

    /** Map to save the value timeStaps by the qualifier */
    private Map<String, Integer> timeStamps;

    /** Map to save the replica timeStaps by the qualifier */
    private Map<String, Integer> replicaTimeStamps;

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

    public ServerState(String qualifier, NamingServerDistLedgerService namingService) {
        this.ledger = new ArrayList<>();
        this.accounts = new HashMap<>();
        this.timeStamps = new HashMap<>();
        this.replicaTimeStamps = new HashMap<>();
        this.qualifier = qualifier;
        this.namingService = namingService;
        this.crossServerServices = new HashMap<>();

        timeStamps.put(qualifier, 0);
        replicaTimeStamps.put(qualifier, 0);
        lookupAddService(crossServerServices);

        try {
            createAccount(DEFAULT_ACCOUNT, timeStamps);
        } catch (UserException exception) {
            System.err.println(exception.getErrorMessage());
        }
        accounts.put(DEFAULT_ACCOUNT, 1000);
        debug(String.format(ACCOUNT_CREATED, DEFAULT_ACCOUNT));
    }

    /**
     * Creates a new account for the given user ID
     * 
     * @param userId the ID of the user for whom the account will be created
     * @throws UserException if the user service fails
    */
    public synchronized Map<String, Integer> createAccount(String userId, Map<String, Integer> timeStamps) throws UserException {
        Map<String, Integer> prevTimeStamps = createNewTimeStamps(timeStamps);
        Map<String, Integer> newTimeStamps = new HashMap<String, Integer>(prevTimeStamps);
        newTimeStamps.put(this.qualifier, this.replicaTimeStamps.get(this.qualifier) + 1);
        Operation newOp = new CreateOp(userId, prevTimeStamps, newTimeStamps);
        createAccountLogic(userId, newOp);
        if (biggerEqualTs(this.timeStamps, prevTimeStamps)) {
            accounts.put(userId, 0);
            newOp.setStable(true);
            this.timeStamps = merge(this.timeStamps, newTimeStamps);
            debug(String.format(ACCOUNT_CREATED, userId));
        } 
        this.replicaTimeStamps.put(this.qualifier, this.replicaTimeStamps.get(this.qualifier) + 1);
        return merge(this.replicaTimeStamps, newTimeStamps);
    }

    private Map<String, Integer> createNewTimeStamps(Map<String, Integer> timeStamps) {
        Map<String, Integer> ts1Copy = sameSize(this.replicaTimeStamps, timeStamps);
        ts1Copy.put(this.qualifier, this.replicaTimeStamps.get(this.qualifier));
        return ts1Copy;
    }

    private Map<String, Integer> sameSize(Map<String, Integer> ts1, Map<String, Integer> ts2) {
        Map<String, Integer> ts2Copy = new HashMap<String, Integer>(ts2);
        for (String key : ts1.keySet()) {
            if (!ts2Copy.containsKey(key))
                ts2Copy.put(key, 0);
        }
        return ts2Copy;
        
    }

    /** Auxiliar function to create account */
    private synchronized void createAccountLogic(String userId, Operation newOp) throws UserException {
        // check server status, primary server status, and account validity
        if (!activated)
            throw new UserException(FAIL_CREATE_ACCOUNT + SERVER_UNAVAILABLE);
        if (this.accounts.keySet().contains(userId)) 
            throw new UserException(FAIL_CREATE_ACCOUNT +
                String.format(DUPLICATE_ACCOUNT, userId));
        if (this.ledger.contains(newOp))
            throw new UserException(FAIL_CREATE_ACCOUNT + DUPLICATE_OPERATION);
        
        ledger.add(newOp);
        debug(OPERATION_REGISTERED);
    }

    /**
     * Transfer a certain amount from one account to another
     * 
     * @param accountFrom the ID of the account to transfer funds from
     * @param accountTo the ID of the account to transfer funds to
     * @param amount the amount to transfer
     * @throws UserException if the user service fails
     * @throws CrossServerException if the cross-server service fails to propagate the updated ledger state
     */
    public synchronized Map<String, Integer> transferTo(String accountFrom, String accountTo, int amount, Map<String, Integer> timeStamps) throws UserException{
        Map<String, Integer> prevTimeStamps = createNewTimeStamps(timeStamps);
        Map<String, Integer> newTimeStamps = new HashMap<String, Integer>(prevTimeStamps);
        newTimeStamps.put(this.qualifier, this.replicaTimeStamps.get(this.qualifier) + 1);
        Operation newOp = new TransferOp(accountFrom, accountTo, amount, prevTimeStamps, newTimeStamps);
        transferToLogic(accountFrom, accountTo, amount, newOp);
        if (biggerEqualTs(this.timeStamps, prevTimeStamps)) {
            accounts.put(accountFrom, accounts.get(accountFrom) - amount);
            accounts.put(accountTo, accounts.get(accountTo) + amount);
            newOp.setStable(true);
            this.timeStamps = merge(this.timeStamps, newTimeStamps);
            debug(String.format(TRANSFER_TO, accountFrom, accountTo, amount));
        } 
        this.replicaTimeStamps.put(this.qualifier, this.replicaTimeStamps.get(this.qualifier) + 1);
        return merge(this.replicaTimeStamps, newTimeStamps);
    }

    /** Auxiliar function to transfer to */
    private synchronized void transferToLogic(String accountFrom, String accountTo, int amount, Operation newOp) throws UserException{
        // check server status, primary server status, and account validity        
        if (!activated) 
            throw new UserException(FAIL_TRANSFER_TO + SERVER_UNAVAILABLE);
        if (!this.accounts.keySet().contains(accountFrom)) 
            throw new UserException(FAIL_TRANSFER_TO 
                + String.format(NO_ACCOUNT, accountFrom));
        if (!this.accounts.keySet().contains(accountTo)) 
            throw new UserException(FAIL_TRANSFER_TO 
                + String.format(NO_ACCOUNT, accountTo));
        if (accountFrom.equals(accountTo)) 
            throw new UserException(FAIL_TRANSFER_TO + TRANSFER_TO_SELF);
        if (amount <= 0)
            throw new UserException(FAIL_TRANSFER_TO + INVALID_AMOUNT);
        if (accounts.get(accountFrom) < amount) 
            throw new UserException(FAIL_TRANSFER_TO 
                + String.format(INVALID_BALANCE, accountFrom, amount));
        if (this.ledger.contains(newOp))
            throw new UserException(DUPLICATE_OPERATION);
        ledger.add(newOp);
    }

    private Map<String, Integer> merge(Map<String, Integer> ts1, Map<String, Integer> ts2) {

        Map<String, Integer> newTimeStamps = new HashMap<String, Integer>(ts2);
        for (String key : ts1.keySet()) {
            if (!newTimeStamps.containsKey(key))
                newTimeStamps.put(key, ts1.get(key));
        }
        for (String key : ts1.keySet()) {
            if (newTimeStamps.get(key) < ts1.get(key))
                newTimeStamps.put(key, ts1.get(key));   
        }
        return newTimeStamps;
    }

    /**
     * Returns the balance of the account corresponding to the provided user ID.
     *
     * @param userId the user ID whose balance is requested.
     * @return the balance of the account.
     * @throws UserException if the server is not activated, or if the account with the given user ID does not exist.
     */
    public synchronized int balance(String userId, Map<String, Integer> userTS) throws UserException {
        // check server status, primary server status, and account validity
        if (!activated)
            throw new UserException(FAIL_BALANCE + SERVER_UNAVAILABLE);
        while (!biggerEqualTs(this.replicaTimeStamps, userTS)) {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (!this.accounts.keySet().contains(userId))
            throw new UserException(FAIL_BALANCE + String.format(NO_ACCOUNT, userId));

        // balance logic
        debug(String.format(BALANCE_ACCESSED, userId, accounts.get(userId)));
        return accounts.get(userId);
    }

    /**
     * Activate the server
     * 
     * @throws AdminException if the server is already activated
     */
    public synchronized void activate() throws AdminException {
        if (activated == true)
            throw new AdminException(FAIL_ACTIVATE);
        activated = true;
        debug(SERVER_ACTIVATED);
    }

    /**
     * Deactivate the server
     * 
     * @throws AdminException if the server is already deactivated
     */
    public synchronized void deactivate() throws AdminException {
        if (activated == false)
            throw new AdminException(FAIL_DEACTIVATE);
        activated = false;
        debug(SERVER_DEACTIVATED);
    }

    public boolean isActive() {
        return activated;
    }

    /**
     * Get Ledger State
     * 
     * @return list of all the operations executed in the server
     */
    public List<Operation> getLedgerState() {
        debug(LEDGE_ACCESSED);
        return cloneLedgerState();
    }

    /**
     * Returns a synchronized copy of the current ledger state
     * 
     * @return a new ArrayList containing cloned copies of the operations in the original ledger
     */
    private synchronized List<Operation> cloneLedgerState() {
        ArrayList<Operation> clone = new ArrayList<>();
        ledger.stream().forEach(op -> clone.add(op.clone()));
        return clone;
    }

    /**
     * Receives the Ledger State from the primary server and process the operations 
     * 
     * @param newOps list of operations to be processed
     * @throws UserException if the process fails
     */
    public synchronized void receiveGossip(List<Operation> newOps, Map<String, Integer> timeStamps) {
        for (Operation op : newOps) {
            if (op instanceof CreateOp) {
                try {
                    if (!biggerEqualTs(this.replicaTimeStamps, op.getTS())) {
                        createAccount(op.getAccount(), op.getPrevTS());
                        this.timeStamps = merge(this.timeStamps, op.getTS());
                    }
                } catch (UserException exception) {
                    debug(exception.getErrorMessage());
                    this.timeStamps = merge(this.timeStamps, op.getTS());
                }
            } else if (op instanceof TransferOp){
                try {
                    if (!biggerEqualTs(this.replicaTimeStamps, op.getTS())) {
                        TransferOp tOp = (TransferOp) op;
                        transferTo(tOp.getAccount(), tOp.getDestAccount(), tOp.getAmount(), op.getPrevTS());
                        this.timeStamps = merge(this.timeStamps, op.getTS());
                    }
                } catch (UserException exception) {
                    this.timeStamps = merge(this.timeStamps, op.getTS());
                    debug(exception.getErrorMessage());
                }
            }
        }
        this.replicaTimeStamps = merge(this.replicaTimeStamps, timeStamps);
        for (Operation op : this.ledger) {
            if (biggerEqualTs(this.timeStamps, op.getPrevTS()) && !op.getStable()) {
                op.setStable(true);
                if (op instanceof CreateOp) {
                    accounts.put(op.getAccount(), 0);
                    this.timeStamps = merge(this.timeStamps, op.getTS());
                } else {
                    TransferOp tOp = (TransferOp) op;
                    accounts.put(op.getAccount(), accounts.get(op.getAccount()) - tOp.getAmount());
                    accounts.put(tOp.getDestAccount(), accounts.get(tOp.getDestAccount()) + tOp.getAmount());
                    this.timeStamps = merge(this.timeStamps, op.getTS());
                }
            }
        }
        notifyAll();
    }

    private boolean biggerEqualTs(Map<String, Integer> ts1, Map<String, Integer> ts2) {
        Map<String, Integer> ts1Copy = sameSize(ts2, ts1);
        Map<String, Integer> ts2Copy = sameSize(ts1, ts2);
        for (String key : ts1Copy.keySet()) {
            if (ts1Copy.get(key) < ts2Copy.get(key))
                return false;
        }
        return true;
    }

    /**
     * Propagate the Ledger State to all the known servers
     * 
     * @param newOps list of operations to be propagated
     * @throws CrossServerException if the propagation fails
     */
    public synchronized void sendGossip() throws CrossServerException {
        // if there are no known servers lookup
        if (crossServerServices.isEmpty())
            lookupAddService(this.crossServerServices);
        // if no servers are found in namingServer throw exception
        if (crossServerServices.isEmpty())
            throw new CrossServerException(FAIL_PROPAGATE_STATE);

        List<String> propagatedQualifiers = new ArrayList<>();
        // try to propagato to all known servers
        for (var entry : crossServerServices.entrySet()) {
            try {
                entry.getValue().propagateState(ledger.stream().filter(op -> op.getStable()).collect(Collectors.toList()),  new HashMap<>(timeStamps));
                propagatedQualifiers.add(entry.getKey());
            } catch (CrossServerException exception) {
                continue;
            }
        }
        // search new adress for servers that failed propagation
        List<Server> notPropagatedServers;
        try {
            notPropagatedServers = namingService
                .lookup(DEFAULT_SERVICE)
                .stream()
                .filter(server -> !propagatedQualifiers.contains(server.getQualifier()) && !server.getQualifier().equals(this.qualifier))
                .collect(Collectors.toList());
        } catch (NamingServerException exception) {
            throw new CrossServerException(exception.getErrorMessage());
        }

        if (notPropagatedServers.size() == 0) 
            return ;
        for (Server server : notPropagatedServers) {
            CrossServerDistLedgerService crossServerService = new CrossServerDistLedgerService(server.getQualifier(), server.getAddress(), this);
            crossServerServices.put(server.getQualifier(), crossServerService);
            try{
                crossServerService.propagateState(ledger.stream().filter(op -> op.getStable()).collect(Collectors.toList()), new HashMap<>(timeStamps));
            } catch (CrossServerException exception) {
                continue;
            }
        }
    }

    /**
     * Finds all the servers with the default service name and stores them in a map by the qualifier
     * 
     * @param crossServerServices map where the the all the services are stored
     */
    private synchronized void lookupAddService(Map<String, CrossServerDistLedgerService> crossServerServices) {
        try {
            namingService
            .lookup(DEFAULT_SERVICE)
            .stream()
            .filter(server -> !server.getQualifier().equals(qualifier)
                && !crossServerServices.containsKey(server.getQualifier()))
            .forEach(server -> {
                crossServerServices.put(server.getQualifier(),
                    new CrossServerDistLedgerService(server.getQualifier(), server.getAddress(), this));
                this.timeStamps.put(server.getQualifier(), 0);
                this.replicaTimeStamps.put(server.getQualifier(), 0);
            });
        } catch (NamingServerException exception) {
            debug(exception.getErrorMessage());
        }
    }

}
