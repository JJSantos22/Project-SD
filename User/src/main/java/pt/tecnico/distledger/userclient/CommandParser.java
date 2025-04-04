package pt.tecnico.distledger.userclient;

import pt.tecnico.distledger.userclient.exceptions.UserException;
import pt.tecnico.distledger.userclient.service.UserNamingServerService;
import pt.tecnico.distledger.userclient.service.UserService;
import pt.ulisboa.tecnico.distledger.contract.DistLedgerCommonDefinitions.Server;
import static pt.tecnico.distledger.userclient.exceptions.UserException.ErrorMessages.*;

import io.grpc.Status;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;


public class CommandParser {

    private static final String SPACE = " ";
    private static final String CREATE_ACCOUNT = "createAccount";
    private static final String TRANSFER_TO = "transferTo";
    private static final String BALANCE = "balance";
    private static final String HELP = "help";
    private static final String EXIT = "exit";

    private static final String SERVICE_NAME = "DistLedger";

    /** Map to save the qualifier to the user service associated*/
    private final Map<String, UserService> userServices;

    /** Naming server service for the User client */
    private final UserNamingServerService namingServerService;

    /** Map to save the qualifier to the timeStamp associated*/
    private Map<String, Integer> timeStamps;
    
    public CommandParser(UserNamingServerService namingServerService) {
        this.namingServerService = namingServerService;
        this.userServices = new HashMap<String, UserService>();
        this.timeStamps = new HashMap<>();
    }

    void parseInput() {

        Scanner scanner = new Scanner(System.in);
        boolean exit = false;

        while (!exit) {
            System.out.print("> ");
            String line = scanner.nextLine().trim();
            String cmd = line.split(SPACE)[0];

            try{
                switch (cmd) {
                    case CREATE_ACCOUNT:
                        this.createAccount(line);
                        break;

                    case TRANSFER_TO:
                        this.transferTo(line);
                        break;

                    case BALANCE:
                        this.balance(line);
                        break;

                    case HELP:
                        this.printUsage();
                        break;

                    case EXIT:
                        userServices.values().forEach(service -> service.shutdown());
                        exit = true;
                        break;

                    default:
                        this.printUsage();
                        throw new UserException(UNKNOWN_COMMAND);
                }
            }
            catch (UserException e){
                System.err.println(e.getErrorMessage());
            }
        }
        // Scanner and UserService should be closed before end of process
        scanner.close();
        userServices.values().forEach(service -> service.shutdown());
        namingServerService.shutdown();
    }

    /**
     * Creates a new user account on the specified server by parsing the input line
     * 
     * @param line the input line containing the server and username for the account to be created
     * @throws UserException if the number of arguments is not correct or if the createAccount process fails
     */
    private void createAccount(String line) throws UserException{
        String[] split = line.split(SPACE);
        UserService userService;

        if (split.length != 3) {
            this.printUsage();
            throw new UserException(INVALID_NUM_ARGS);
        }

        String server = split[1];
        String username = split[2];
        if ((userService = (userServices.get(server))) == null){
            List<Server> serverList = namingServerService.lookup(SERVICE_NAME, server);
            if (serverList.size() == 0)
                throw new UserException(SERVER_NOT_FOUND);
            if (!timeStamps.keySet().contains(server))
                timeStamps.put(server, 0);
            userService = new UserService(serverList.get(0).getQualifier(), serverList.get(0).getAddress());
            userServices.put(userService.getQualifier(), userService);
        }
        timeStamps.put(server, userService.createAccount(username, timeStamps).get(server));
        System.out.println(Status.OK.getCode());
    }
    
    /**
     * Returs the balance of an account specified by an account id by parsing the input line
     * 
     * @param line the input line containing the server and the username of the account to be consulted
     * @throws UserException if the number of arguments is not correct or if the transferTo process fails
     */
    private void balance(String line) throws UserException{
        String[] split = line.split(SPACE);
        UserService userService;

        if (split.length != 3){
            this.printUsage();
            throw new UserException(INVALID_NUM_ARGS);
        }
        String server = split[1];
        String username = split[2];
        if ((userService=(userServices.get(server)))==null){
            List<Server> serverList = namingServerService.lookup(SERVICE_NAME, server);
            serverList = namingServerService.lookup(SERVICE_NAME, server);            
            if (serverList.size() == 0)
                throw new UserException(SERVER_NOT_FOUND);
            userService = new UserService(serverList.get(0).getQualifier(), serverList.get(0).getAddress());
            userServices.put(server, userService);
        }
        int balance = userService.balance(username, timeStamps);
        System.out.println(Status.OK.getCode());
        System.out.println(balance);
    }

    
    /**
     * Transfers a given amount of money from one user account to another by parsing the input line
     * 
     * @param line the input line containing the server, the username of the sender, the username of the receiver and the amount to be transferred
     * @throws UserException if the number of arguments is not correct or if the transferTo process fails
     */
    private void transferTo(String line) throws UserException{
        String[] split = line.split(SPACE);
        UserService userService;

        if (split.length != 5){
            this.printUsage();
            throw new UserException(INVALID_NUM_ARGS);
        }
        String server = split[1];
        if ((userService=(userServices.get(server)))==null){
            List<Server> serverList = namingServerService.lookup(SERVICE_NAME, server);
            if (serverList.size() == 0)
                throw new UserException(SERVER_NOT_FOUND);
            userService = new UserService(serverList.get(0).getQualifier(), serverList.get(0).getAddress());
            userServices.put(server, userService);
        }
        String from = split[2];
        String dest = split[3];
        Integer amount = 0;
        try {
            amount = Integer.valueOf(split[4]);
        }catch (NumberFormatException e) {
            throw new UserException(INVALID_ARG_FORMAT);
        }

        timeStamps.put(server, userService.transferTo(from, dest, amount).get(server));
        System.out.println(Status.OK.getCode());
    }

    /**
     * Prints the usage of the user commands
     */
    private void printUsage() {
        System.out.println("Usage:\n" +
                        "- createAccount <server> <username>\n" +
                        "- balance <server> <username>\n" +
                        "- transferTo <server> <username_from> <username_to> <amount>\n" +
                        "- exit");
    }
}
