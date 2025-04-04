package pt.tecnico.distledger.userclient;

import pt.tecnico.distledger.userclient.service.UserNamingServerService;

public class UserClientMain {
    private final static String LOCALHOST = "localhost";
    private final static int PORT = 5001;

    public static void main(String[] args) {

        System.out.println(UserClientMain.class.getSimpleName());

        final String address = LOCALHOST + ":" + PORT;

        CommandParser parser = new CommandParser(new UserNamingServerService(address));
        
        /** Input handling and service's methods calls*/
        parser.parseInput();
    }
}
