# DistLedger

Distributed Systems Project 2022/2023

## Authors

**Group A54**

### Code Identification

In all source files (namely in the *groupId*s of the POMs), replace __GXX__ with your group identifier. The group
identifier consists of either A or T followed by the group number - always two digits. This change is important for 
code dependency management, to ensure your code runs using the correct components and not someone else's.

### Team Members

| Number | Name              | User                              | Email                                           |
|--------|-------------------|-----------------------------------|-------------------------------------------------|
| 99258  | Jorge Santos      | <https://github.com/JJSantos22>   | <mailto: jorge.m.santos@tecnico.ulisboa.pt>     |
| 99276  | Marta Félix       | <https://github.com/martafelix13> | <mailto: marta.felix@tecnico.ulisboa.pt>        |
| 99290  | Miguel Fonseca    | <https://github.com/mdgf1>        | <mailto: miguel.d.g.fonseca@tecnico.ulisboa.pt> |

## Getting Started

The overall system is made up of several modules. The main server is the _DistLedgerServer_. The clients are the _User_ 
and the _Admin_. The definition of messages and services is in the _Contract_. The future naming server
is the _NamingServer_.

See the [Project Statement](https://github.com/tecnico-distsys/DistLedger) for a complete domain and system description.

### Prerequisites

The Project is configured with Java 17 (which is only compatible with Maven >= 3.8), but if you want to use Java 11 you
can too -- just downgrade the version in the POMs.

To confirm that you have them installed and which versions they are, run in the terminal:

```s
javac -version
mvn -version
```

### Installation

To compile and install all modules:

```s
mvn clean install
```

### Compile and Execution 

The execution is going to require at least new terminal for each module and two for the servers (primary and backup), running in simultaneosly.

1. Compile and execute the naming server (Mandatory):

```s
cd NamingServer
mvn compile exec:java
```

2. Compile the server (Mandatory):

```s
cd DistLedgerServer
mvn compile
```

3. Execute the primary and the secondary servers simultaneosly in separated terminals (Mandatory):

Primary (qualifier must be 'A'):
```s
mvn exec:java -Dexec.args="2001 A" 
```

Secondary (qualifier must be 'B'): 
```s
mvn exec:java -Dexec.args="2002 B" 
``` 


4. Compile and execute the User:

```s
cd ../User
mvn compile exec:java
```

5. Compile and execute the Admin:

```s
cd ../Admin
mvn compile exec:java
```

After all the modules are running, you can insert the command directly in the user terminal or in the admin terminal.
All the interactions with the server must be done through the user or admin terminal.

## Built With

* [Maven](https://maven.apache.org/) - Build and dependency management tool;
* [gRPC](https://grpc.io/) - RPC framework.
