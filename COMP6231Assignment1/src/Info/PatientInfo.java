/**
 * @author Munj Bhavesh Nayak
 */
package Info;

public class PatientInfo {

	public static final String CLIENT_TYPE_ADMIN = "ADMIN";
    public static final String CLIENT_TYPE_PATIENT = "PATIENT";
    public static final String CLIENT_SERVER_SHERBROOK = "SHERBROOK";
    public static final String CLIENT_SERVER_QUEBEC = "QUEBEC";
    public static final String CLIENT_SERVER_MONTREAL = "MONTREAL";
    private String clientType;
    private String clientID;
    private String clientServer;

    public PatientInfo(String clientID) {
        this.clientID = clientID;
        this.clientType = detectClientType();
        this.clientServer = detectClientServer();
    }

    private String detectClientServer() {
        if (clientID.substring(0, 3).equalsIgnoreCase("MTL")) {
            return CLIENT_SERVER_MONTREAL;
        } else if (clientID.substring(0, 3).equalsIgnoreCase("QUE")) {
            return CLIENT_SERVER_QUEBEC;
        } else {
            return CLIENT_SERVER_SHERBROOK;
        }
    }

    private String detectClientType() {
        if (clientID.substring(3, 4).equalsIgnoreCase("A")) {
            return CLIENT_TYPE_ADMIN;
        } else {
            return CLIENT_TYPE_PATIENT;
        }
    }

    public String getClientType() {
        return clientType;
    }

    public void setClientType(String clientType) {
        this.clientType = clientType;
    }

    public String getClientID() {
        return clientID;
    }

    public void setClientID(String clientID) {
        this.clientID = clientID;
    }

    public String getClientServer() {
        return clientServer;
    }

    public void setClientServer(String clientServer) {
        this.clientServer = clientServer;
    }

    @Override
    public String toString() {
        return getClientType() + "(" + getClientID() + ") on " + getClientServer() + " Server.";
    }
}

