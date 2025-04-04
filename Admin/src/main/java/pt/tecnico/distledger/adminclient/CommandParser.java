package pt.tecnico.distledger.adminclient;

import pt.tecnico.distledger.adminclient.exceptions.AdminException;
import pt.tecnico.distledger.adminclient.service.AdminNamingServerService;
import pt.tecnico.distledger.adminclient.service.AdminService;
import pt.ulisboa.tecnico.distledger.contract.DistLedgerCommonDefinitions.Server;

import static pt.tecnico.distledger.adminclient.exceptions.AdminException.ErrorMessages.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.List;

import io.grpc.Status;

public class CommandParser {

    private static final String SPACE = " ";
    private static final String ACTIVATE = "activate";
    private static final String DEACTIVATE = "deactivate";
    private static final String GET_LEDGER_STATE = "getLedgerState";
    private static final String GOSSIP = "gossip";
    private static final String HELP = "help";
    private static final String EXIT = "exit";

    private static final String SERVICE_NAME = "DistLedger";

    /** Map to associate the server qualifier to the corresponding admin service */
    private final Map<String, AdminService> adminServices;

    /** Naming server service */
    private final AdminNamingServerService namingServerService;

    public CommandParser(AdminNamingServerService namingServerService) {
        this.namingServerService = namingServerService;
        this.adminServices = new HashMap<String, AdminService>();
    }

    void parseInput() {

        Scanner scanner = new Scanner(System.in);
        boolean exit = false;

        while (!exit) {
            System.out.print("> ");
            String line = scanner.nextLine().trim();
            String cmd = line.split(SPACE)[0];

            try {
                switch (cmd) {
                    case ACTIVATE:
                        this.activate(line);
                        break;

                    case DEACTIVATE:
                        this.deactivate(line);
                        break;

                    case GET_LEDGER_STATE:
                        this.dump(line);
                        break;

                    case GOSSIP:
                        this.gossip(line);
                        break;

                    case HELP:
                        this.printUsage();
                        break;

                    case EXIT:
                        exit = true;
                        break;

                    default:
                        this.printUsage();
                        throw new AdminException(UNKNOWN_COMMAND);
                }
            } catch (AdminException e) {
                System.err.println(e.getErrorMessage());
            }
        }
        /** Scanner and UserService should be closed before end of process */
        scanner.close();
        adminServices.values().forEach(service -> service.shutdown());
        namingServerService.shutdown();
    }

    /**
     * Activate the server with the given qualifier
     * 
     * @param line the command line to be executed
     * @throws AdminException if the command has the wrong number of arguments or the activation process failed
     */
    private void activate(String line) throws AdminException {
        String[] split = line.split(SPACE);

        if (split.length != 2) {
            this.printUsage();
            throw new AdminException(INVALID_NUM_ARGS);
        }
        String server = split[1];

        lookupServices(server).activate();

        System.out.println(Status.OK.getCode());
    }

    /**
     * Deactivate the given server
     * 
     * @param line is the command line received
     * @throws AdminException
     */
    private void deactivate(String line) throws AdminException {
        String[] split = line.split(SPACE);

        if (split.length != 2) {
            this.printUsage();
            throw new AdminException(INVALID_NUM_ARGS);
        }
        String server = split[1];

        lookupServices(server).deactivate();
        System.out.println(Status.OK.getCode());
    }

    /**
     * Dumps the current state of the ledger in the specified server.
     * 
     * @param line the command line input
     * @throws AdminException if the command line input is invalid or there is an issue with the admin service
     */
    private void dump(String line) throws AdminException {
        String[] split = line.split(SPACE);

        if (split.length != 2) {
            this.printUsage();
            throw new AdminException(INVALID_NUM_ARGS);
        }
        String server = split[1];

        String ledgerState = lookupServices(server).getLedgerState();

        System.out.println(Status.OK.getCode());
        System.out.println(ledgerState);
    }

    private void gossip(String line) throws AdminException {
        String[] split = line.split(SPACE);

        if (split.length != 2) {
            this.printUsage();
            throw new AdminException(INVALID_NUM_ARGS);
        }
        String server = split[1];
        lookupServices(server).gossip();

        System.out.println(Status.OK.getCode());
    }

    private AdminService lookupServices(String server) throws AdminException{
        AdminService adminService;

        // Look up admin service associated with server
        if ((adminService = (adminServices.get(server))) == null) {
            List<Server> serverList = namingServerService.lookup(SERVICE_NAME, server);
            if (serverList.size() == 0)
                throw new AdminException(SERVER_NOT_FOUND);
            adminService = new AdminService(serverList.get(0).getQualifier(), serverList.get(0).getAddress());
            adminServices.put(server, adminService);
        }
        return adminService;
    }

    /**
     * Print the usage of the AdminClient
     */
    private void printUsage() {
        System.out.println("Usage:\n" +
                "- activate <server>\n" +
                "- deactivate <server>\n" +
                "- getLedgerState <server>\n" +
                "- gossip <server>\n" +
                "- exit\n");
    }

}
