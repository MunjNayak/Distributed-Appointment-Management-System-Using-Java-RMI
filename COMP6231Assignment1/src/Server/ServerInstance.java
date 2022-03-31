/** 
 * @author Munj Bhavesh Nayak
 */
package Server;

import Client.Client; 
import Info.AppointmentInfo;
import Logger.Logger;
import AppointmentManagement.AppointmentManagement;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class ServerInstance {

	private String serverID;
    private String serverName;
    private int serverRegistryPort;
    private int serverUdpPort;

    public ServerInstance(String serverID) throws Exception {
        this.serverID = serverID;
        switch (serverID) {
            case "MTL":
                serverName = AppointmentManagement.APPOINTMENT_SERVER_MONTREAL;
                serverRegistryPort = Client.SERVER_MONTREAL;
                serverUdpPort = AppointmentManagement.Montreal_Server_Port;
                break;
            case "QUE":
                serverName = AppointmentManagement.APPOINTMENT_SERVER_QUEBEC;
                serverRegistryPort = Client.SERVER_QUEBEC;
                serverUdpPort = AppointmentManagement.Quebec_Server_Port;
                break;
            case "SHE":
                serverName = AppointmentManagement.APPOINTMENT_SERVER_SHERBROOK;
                serverRegistryPort = Client.SERVER_SHERBROOKE;
                serverUdpPort = AppointmentManagement.Sherbrooke_Server_Port;
                break;
        }

        AppointmentManagement remoteObject = new AppointmentManagement(serverID, serverName);
        Registry registry = LocateRegistry.createRegistry(serverRegistryPort);
        registry.bind(Client.APPOINTMENT_MANAGEMENT_REGISTERED_NAME, remoteObject);

        System.out.println(serverName + " Server is Up & Running");
        Logger.serverLog(serverID, " Server is Up & Running");
  //      addTestData(remoteObject);
        Runnable task = () -> {
            listenForRequest(remoteObject, serverUdpPort, serverName, serverID);
        };
        Thread thread = new Thread(task);
        thread.start();
    }
/*
    private void addTestData(AppointmentManagement remoteObject) {
        switch (serverID) {
            case "MTL":
                remoteObject.addNewAppointment("MTLA090620", AppointmentInfo.PHYSICIAN, 2);
                remoteObject.addNewAppointment("MTLA080620", AppointmentInfo.SURGEON, 2);
                remoteObject.addNewAppointment("MTLE230620", AppointmentInfo.SURGEON, 1);
                remoteObject.addNewAppointment("MTLA150620", AppointmentInfo.DENTAL, 12);
                break;
            case "QUE":
                remoteObject.addNewClientToClients("QUEA1234");
                remoteObject.addNewClientToClients("QUEP4114");
                break;
            case "SHE":
                remoteObject.addNewAppointment("SHEE110620", AppointmentInfo.PHYSICIAN, 1);
                remoteObject.addNewAppointment("SHEE080620", AppointmentInfo.PHYSICIAN, 1);
                break;
        }
    }
*/
    private static void listenForRequest(AppointmentManagement obj, int serverUdpPort, String serverName, String serverID) {
        DatagramSocket aSocket = null;
        String sendingResult = "";
        try {
            aSocket = new DatagramSocket(serverUdpPort);
            byte[] buffer = new byte[1000];
            System.out.println(serverName + " UDP Server Started at port " + aSocket.getLocalPort() + " ............");
            Logger.serverLog(serverID, " UDP Server Started at port " + aSocket.getLocalPort());
            while (true) {
                DatagramPacket request = new DatagramPacket(buffer, buffer.length);
                aSocket.receive(request);
                String sentence = new String(request.getData(), 0,
                        request.getLength());
                String[] parts = sentence.split(";");
                String method = parts[0];
                String patientID = parts[1];
                String appointmentType = parts[2];
                String appointmentID = parts[3];
                if (method.equalsIgnoreCase("removeAppointment")) {
                    Logger.serverLog(serverID, patientID, " UDP request received " + method + " ", " appointmentID: " + appointmentID + " eventType: " + appointmentType + " ", " ...");
                    String result = obj.removeAppointmentUDP(appointmentID, appointmentType, patientID);
                    sendingResult = result + ";";
                } else if (method.equalsIgnoreCase("listAppointmentAvailability")) {
                    Logger.serverLog(serverID, patientID, " UDP request received " + method + " ", " appointmentType: " + appointmentType + " ", " ...");
                    String result = obj.listAppointmentAvailabilityUDP(appointmentType);
                    sendingResult = result + ";";
                } else if (method.equalsIgnoreCase("bookAppointment")) {
                    Logger.serverLog(serverID, patientID, " UDP request received " + method + " ", " appointmentID: " + appointmentID + " appointmentType: " + appointmentType + " ", " ...");
                    String result = obj.bookAppointment(patientID, appointmentID, appointmentType);
                    sendingResult = result + ";";
                } else if (method.equalsIgnoreCase("cancelAppointment")) {
                    Logger.serverLog(serverID, patientID, " UDP request received " + method + " ", " appointmentID: " + appointmentID + " appointmentType: " + appointmentType + " ", " ...");
                    String result = obj.cancelAppointment(patientID, appointmentID, appointmentType);
                    sendingResult = result + ";";
                }
                byte[] sendData = sendingResult.getBytes();
                DatagramPacket reply = new DatagramPacket(sendData, sendingResult.length(), request.getAddress(),
                        request.getPort());
                aSocket.send(reply);
                Logger.serverLog(serverID, patientID, " UDP reply sent " + method + " ", " appointmentID: " + appointmentID + " appointmentType: " + appointmentType + " ", sendingResult);
            }
        } catch (SocketException e) {
            System.out.println("SocketException: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
        } finally {
            if (aSocket != null)
                aSocket.close();
        }
    }
}
