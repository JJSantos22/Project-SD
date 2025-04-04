package pt.tecnico.distledger.namingserver.domain;

import pt.ulisboa.tecnico.distledger.contract.DistLedgerCommonDefinitions.Server;

public class ServerEntry {

    private String qualifier; // Saves the server qualifier
    private String address; // Saves the server address

    public ServerEntry(String qualifier, String address){
        this.qualifier = qualifier;
        this.address = address;
    }

    public String getQualifier(){
        return qualifier;
    }

    public String getAddress(){
        return address;
    }

    public void setQualifier(String qualifier){
        this.qualifier = qualifier;
    }

    public void setHostport(String address){
        this.address = address;
    }

    public Server proto() {
        return Server.newBuilder().setAddress(address).setQualifier(qualifier).build();
    }

}
