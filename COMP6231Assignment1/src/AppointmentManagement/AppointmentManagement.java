/** 
 * @author Munj Bhavesh Nayak
 */
package AppointmentManagement;
import Interface.AppointmentManagementInterface;
import Logger.Logger;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import Info.PatientInfo;
import Info.AppointmentInfo;

public class AppointmentManagement extends UnicastRemoteObject implements AppointmentManagementInterface{ 
	public static final int Montreal_Server_Port = 8888;
    public static final int Quebec_Server_Port = 7777;
    public static final int Sherbrooke_Server_Port = 6666;
    public static final String APPOINTMENT_SERVER_SHERBROOK = "SHERBROOK";
    public static final String APPOINTMENT_SERVER_QUEBEC = "QUEBEC";
    public static final String APPOINTMENT_SERVER_MONTREAL = "MONTREAL";
    private String serverID;
    private String serverName;
    // HashMap<AppointmentType, HashMap <AppointmentID, Appointment>>
    private Map<String, Map<String, AppointmentInfo>> allAppointments;
    // HashMap<PatientID, HashMap <AppointmentType, List<AppointmentID>>>
    private Map<String, Map<String, List<String>>> patientAppointments;
    // HashMap<PatientID, PatientInfo>
    private Map<String, PatientInfo> serverPatients;

    public AppointmentManagement(String serverID, String serverName) throws RemoteException {
        super();
        this.serverID = serverID;
        this.serverName = serverName;
        allAppointments = new ConcurrentHashMap<>();
        allAppointments.put(AppointmentInfo.PHYSICIAN, new ConcurrentHashMap<>());
        allAppointments.put(AppointmentInfo.SURGEON, new ConcurrentHashMap<>());
        allAppointments.put(AppointmentInfo.DENTAL, new ConcurrentHashMap<>());
        patientAppointments = new ConcurrentHashMap<>();
        serverPatients = new ConcurrentHashMap<>();

    }

    private static int getServerPort(String hos) {
        if (hos.equalsIgnoreCase("MTL")) {
            return Montreal_Server_Port;
        } else if (hos.equalsIgnoreCase("SHE")) {
            return Sherbrooke_Server_Port;
        } else if (hos.equalsIgnoreCase("QUE")) {
            return Quebec_Server_Port;
        }
        return 1;
    }

    @Override
    public String addAppointment(String appointmentID, String appointmentType, int bookingCapacity) throws RemoteException {
        String response;
        if (allAppointments.get(appointmentType).containsKey(appointmentID)) {
            if (allAppointments.get(appointmentType).get(appointmentID).getAppointmentCapacity() <= bookingCapacity) {
                allAppointments.get(appointmentType).get(appointmentID).setAppointmentCapacity(bookingCapacity);
                response = "Success: Appointment " + appointmentID + " Capacity increased to " + bookingCapacity;
                try {
                    Logger.serverLog(serverID, "null", " RMI addAppointment ", " appointmentID: " + appointmentID + " appointmentType: " + appointmentType + " bookingCapacity " + bookingCapacity + " ", response);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return response;
            } else {
                response = "Failed: Appointment Already Exists, Cannot Decrease Booking Capacity";
                try {
                    Logger.serverLog(serverID, "null", " RMI addAppointment ", " appointmentID: " + appointmentID + " appointmentType: " + appointmentType + " bookingCapacity " + bookingCapacity + " ", response);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return response;
            }
        }
        if (AppointmentInfo.detectAppointmentServer(appointmentID).equals(serverName)) {
            AppointmentInfo appointment = new AppointmentInfo(appointmentType, appointmentID, bookingCapacity);
            Map<String, AppointmentInfo> appointmentHashMap = allAppointments.get(appointmentType);
            appointmentHashMap.put(appointmentID, appointment);
            allAppointments.put(appointmentType, appointmentHashMap);
            response = "Success: Appointment " + appointmentID + " added successfully";
            try {
                Logger.serverLog(serverID, "null", " RMI addAppointment ", " appointmentID: " + appointmentID + " appointmentType: " + appointmentType + " bookingCapacity " + bookingCapacity + " ", response);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return response;
        } else {
            response = "Failed: Cannot Add Appointment to servers other than " + serverName;
            try {
                Logger.serverLog(serverID, "null", " RMI addAppointment ", " appointmentID: " + appointmentID + " appointmentType: " + appointmentType + " bookingCapacity " + bookingCapacity + " ", response);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return response;
        }
    }

    @Override
    public String removeAppointment(String appointmentID, String appointmentType) throws RemoteException {
        String response;
        if (AppointmentInfo.detectAppointmentServer(appointmentID).equals(serverName)) {
            if (allAppointments.get(appointmentType).containsKey(appointmentID)) {
                List<String> registeredPatients = allAppointments.get(appointmentType).get(appointmentID).getRegisteredPatientIDs();
                allAppointments.get(appointmentType).remove(appointmentID);
                addPatientsToNextSameAppointment(appointmentID, appointmentType, registeredPatients);
                response = "Success: Appointment Removed Successfully";
                try {
                    Logger.serverLog(serverID, "null", " RMI removeAppointment ", " appointmentID: " + appointmentID + " appointmentType: " + appointmentType + " ", response);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return response;
            } else {
                response = "Failed: Appointment " + appointmentID + " Does Not Exist";
                try {
                    Logger.serverLog(serverID, "null", " RMI removeAppointment ", " appointmentID: " + appointmentID + " appointmentType: " + appointmentType + " ", response);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return response;
            }
        } else {
            response = "Failed: Cannot Remove Appointment from servers other than " + serverName;
            try {
                Logger.serverLog(serverID, "null", " RMI removeAppointment ", " appointmentID: " + appointmentID + " appointmentType: " + appointmentType + " ", response);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return response;
        }
    }

    @Override
    public String listAppointmentAvailability(String appointmentType) throws RemoteException {
        String response;
        Map<String, AppointmentInfo> appointments = allAppointments.get(appointmentType);
        StringBuilder builder = new StringBuilder();
        builder.append(serverName + " Server " + appointmentType + ":\n");
        if (appointments.size() == 0) {
            builder.append("No Appointments of Type " + appointmentType);
        } else {
            for (AppointmentInfo appointment :
                    appointments.values()) {
                builder.append(appointment.toString() + " || ");
            }
            builder.append("\n=====================================\n");
        }
        String otherServer1, otherServer2;
        if (serverID.equals("MTL")) {
            otherServer1 = sendUDPMessage(Sherbrooke_Server_Port, "listAppointmentAvailability", "null", appointmentType, "null");
            otherServer2 = sendUDPMessage(Quebec_Server_Port, "listAppointmentAvailability", "null", appointmentType, "null");
        } else if (serverID.equals("SHE")) {
            otherServer1 = sendUDPMessage(Quebec_Server_Port, "listAppointmentAvailability", "null", appointmentType, "null");
            otherServer2 = sendUDPMessage(Montreal_Server_Port, "listAppointmentAvailability", "null", appointmentType, "null");
        } else {
            otherServer1 = sendUDPMessage(Montreal_Server_Port, "listAppointmentAvailability", "null", appointmentType, "null");
            otherServer2 = sendUDPMessage(Sherbrooke_Server_Port, "listAppointmentAvailability", "null", appointmentType, "null");
        }
        builder.append(otherServer1).append(otherServer2);
        response = builder.toString();
        try {
            Logger.serverLog(serverID, "null", " RMI listAppointmentAvailability ", " appointmentType: " + appointmentType + " ", response);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }

    @Override
    public String bookAppointment(String patientID, String appointmentID, String appointmentType) throws RemoteException {
        String response;
        if (!serverPatients.containsKey(patientID)) {
            addNewPatientToPatients(patientID);
        }
        if (AppointmentInfo.detectAppointmentServer(appointmentID).equals(serverName)) {
            AppointmentInfo bookedAppointment = allAppointments.get(appointmentType).get(appointmentID);
            if (!bookedAppointment.isFull()) {
                if (patientAppointments.containsKey(patientID)) {
                    if (patientAppointments.get(patientID).containsKey(appointmentType)) {
                        if (!patientAppointments.get(patientID).get(appointmentType).contains(appointmentID)) {
                            patientAppointments.get(patientID).get(appointmentType).add(appointmentID);
                        } else {
                            response = "Failed: Appointment " + appointmentID + " Already Booked";
                            try {
                                Logger.serverLog(serverID, patientID, " RMI bookAppointment ", " appointmentID: " + appointmentID + " appointmentType: " + appointmentType + " ", response);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            return response;
                        }
                    } else {
                        List<String> temp = new ArrayList<>();
                        temp.add(appointmentID);
                        patientAppointments.get(patientID).put(appointmentType, temp);
                    }
                } else {
                    Map<String, List<String>> temp = new ConcurrentHashMap<>();
                    List<String> temp2 = new ArrayList<>();
                    temp2.add(appointmentID);
                    temp.put(appointmentType, temp2);
                    patientAppointments.put(patientID, temp);
                }
                if (allAppointments.get(appointmentType).get(appointmentID).addRegisteredPatientID(patientID) == AppointmentInfo.ADD_SUCCESS) {
                    response = "Success: Appointment " + appointmentID + " Booked Successfully";
                } else if (allAppointments.get(appointmentType).get(appointmentID).addRegisteredPatientID(patientID) == AppointmentInfo.APPOINTMENT_FULL) {
                    response = "Failed: Appointment " + appointmentID + " is Full";
                } else {
                    response = "Failed: Cannot Add You To Appointment " + appointmentID;
                }
                try {
                    Logger.serverLog(serverID, patientID, " RMI bookAppointment ", " appointmentID: " + appointmentID + " appointmentType: " + appointmentType + " ", response);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return response;
            } else {
                response = "Failed: Appointment " + appointmentID + " is Full";
                try {
                    Logger.serverLog(serverID, patientID, " RMI bookAppointment ", " appointmentID: " + appointmentID + " appointmentType: " + appointmentType + " ", response);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return response;
            }
        } else {
            if (!exceedWeeklyLimit(patientID, appointmentID.substring(4))) {
                String serverResponse = sendUDPMessage(getServerPort(appointmentID.substring(0, 3)), "bookAppointment", patientID, appointmentType, appointmentID);
                if (serverResponse.startsWith("Success:")) {
                    if (patientAppointments.get(patientID).containsKey(appointmentType)) {
                        patientAppointments.get(patientID).get(appointmentType).add(appointmentID);
                    } else {
                        List<String> temp = new ArrayList<>();
                        temp.add(appointmentID);
                        patientAppointments.get(patientID).put(appointmentType, temp);
                    }
                }
                try {
                    Logger.serverLog(serverID, patientID, " RMI bookAppointment ", " appointmentID: " + appointmentID + " appointmentType: " + appointmentType + " ", serverResponse);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return serverResponse;
            } else {
                response = "Failed: You Cannot Book Appointment in Other Servers For This Week(Max Weekly Limit = 3)";
                try {
                    Logger.serverLog(serverID, patientID, " RMI bookAppointment ", " appointmentID: " + appointmentID + " appointmentType: " + appointmentType + " ", response);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return response;
            }
        }
    }

    @Override
    public String getAppointmentSchedule(String patientID) throws RemoteException {
        String response;
        if (!serverPatients.containsKey(patientID)) {
            addNewPatientToPatients(patientID);
            response = "Appointment Schedule Empty For " + patientID;
            try {
                Logger.serverLog(serverID, patientID, " RMI getAppointmentSchedule ", "null", response);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return response;
        }
        Map<String, List<String>> appointments = patientAppointments.get(patientID);
        if (appointments.size() == 0) {
            response = "Booking Schedule Empty For " + patientID;
            try {
                Logger.serverLog(serverID, patientID, " RMI getAppointmentSchedule ", "null", response);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return response;
        }
        StringBuilder builder = new StringBuilder();
        for (String appointmentType :
                appointments.keySet()) {
            builder.append(appointmentType + ":\n");
            for (String appointmentID :
                    appointments.get(appointmentType)) {
                builder.append(appointmentID + " ||");
            }
            builder.append("\n=====================================\n");
        }
        response = builder.toString();
        try {
            Logger.serverLog(serverID, patientID, " RMI getAppointmentSchedule ", "null", response);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }

    @Override
    public String cancelAppointment(String patientID, String appointmentID, String appointmentType) throws RemoteException {
        String response;
        if (AppointmentInfo.detectAppointmentServer(appointmentID).equals(serverName)) {
            if (patientID.substring(0, 3).equals(serverID)) {
                if (!serverPatients.containsKey(patientID)) {
                    addNewPatientToPatients(patientID);
                    response = "Failed: You " + patientID + " Are Not Registered in " + appointmentID;
                    try {
                        Logger.serverLog(serverID, patientID, " RMI cancelAppointment ", " appointmentID: " + appointmentID + " appointmentType: " + appointmentType + " ", response);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return response;
                } else {
                    if (patientAppointments.get(patientID).get(appointmentType).remove(appointmentID)) {
                        allAppointments.get(appointmentType).get(appointmentID).removeRegisteredPatientID(patientID);
                        response = "Success: Appointment " + appointmentID + " Canceled for " + patientID;
                        try {
                            Logger.serverLog(serverID, patientID, " RMI cancelAppointment ", " appointmentID: " + appointmentID + " appointmentType: " + appointmentType + " ", response);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return response;
                    } else {
                        response = "Failed: You " + patientID + " Are Not Registered in " + appointmentID;
                        try {
                            Logger.serverLog(serverID, patientID, " RMI cancelAppointment ", " appointmentID: " + appointmentID + " appointmentType: " + appointmentType + " ", response);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return response;
                    }
                }
            } else {
                if (allAppointments.get(appointmentType).get(appointmentID).removeRegisteredPatientID(patientID)) {
                    response = "Success: Appointment " + appointmentID + " Canceled for " + patientID;
                    try {
                        Logger.serverLog(serverID, patientID, " RMI cancelAppointment ", " appointmentID: " + appointmentID + " appointmentType: " + appointmentType + " ", response);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return response;
                } else {
                    response = "Failed: You " + patientID + " Are Not Registered in " + appointmentID;
                    try {
                        Logger.serverLog(serverID, patientID, " RMI cancelAppointment ", " appointmentID: " + appointmentID + " appointmentType: " + appointmentType + " ", response);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return response;
                }
            }
        } else {
            if (patientID.substring(0, 3).equals(serverID)) {
                if (!serverPatients.containsKey(patientID)) {
                    addNewPatientToPatients(patientID);
                } else {
                    if (patientAppointments.get(patientID).get(appointmentType).remove(appointmentID)) {
                        return sendUDPMessage(getServerPort(appointmentID.substring(0, 3)), "cancelAppointment", patientID, appointmentType, appointmentID);
                    }
                }
            }
            return "Failed: You " + patientID + " Are Not Registered in " + appointmentID;
        }
    }

    /**
     * for udp calls only
     *
     * @param oldAppointmentID
     * @param appointmentType
     * @param patientID
     * @return
     * @throws RemoteException
     */
    public String removeAppointmentUDP(String oldAppointmentID, String appointmentType, String patientID) throws RemoteException {
        if (!serverPatients.containsKey(patientID)) {
            addNewPatientToPatients(patientID);
            return "Failed: You " + patientID + " Are Not Registered in " + oldAppointmentID;
        } else {
            if (patientAppointments.get(patientID).get(appointmentType).remove(oldAppointmentID)) {
                return "Success: Appointment " + oldAppointmentID + " Was Removed from " + patientID + " Schedule";
            } else {
                return "Failed: You " + patientID + " Are Not Registered in " + oldAppointmentID;
            }
        }
    }

    /**
     * for UDP calls only
     *
     * @param appointmentType
     * @return
     * @throws RemoteException
     */
    public String listAppointmentAvailabilityUDP(String appointmentType) throws RemoteException {
        Map<String, AppointmentInfo> appointments = allAppointments.get(appointmentType);
        StringBuilder builder = new StringBuilder();
        builder.append(serverName + " Server " + appointmentType + ":\n");
        if (appointments.size() == 0) {
            builder.append("No Appointments of Type " + appointmentType);
        } else {
            for (AppointmentInfo appointment :
                    appointments.values()) {
                builder.append(appointment.toString() + " || ");
            }
        }
        builder.append("\n=====================================\n");
        return builder.toString();
    }

    private String sendUDPMessage(int serverPort, String method, String patientID, String appointmentType, String appointmentId) {
        DatagramSocket aSocket = null;
        String result = "";
        String dataFromPatient = method + ";" + patientID + ";" + appointmentType + ";" + appointmentId;
        try {
            Logger.serverLog(serverID, patientID, " UDP request sent " + method + " ", " appointmentID: " + appointmentId + " appointmentType: " + appointmentType + " ", " ... ");
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            aSocket = new DatagramSocket();
            byte[] message = dataFromPatient.getBytes();
            InetAddress aHost = InetAddress.getByName("localhost");
            DatagramPacket request = new DatagramPacket(message, dataFromPatient.length(), aHost, serverPort);
            aSocket.send(request);

            byte[] buffer = new byte[1000];
            DatagramPacket reply = new DatagramPacket(buffer, buffer.length);

            aSocket.receive(reply);
            result = new String(reply.getData());
            String[] parts = result.split(";");
            result = parts[0];
        } catch (SocketException e) {
            System.out.println("Socket: " + e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("IO: " + e.getMessage());
        } finally {
            if (aSocket != null)
                aSocket.close();
        }
        try {
            Logger.serverLog(serverID, patientID, " UDP reply received" + method + " ", " appointmentID: " + appointmentId + " appointmentType: " + appointmentType + " ", result);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;

    }

    private String getNextSameAppointment(Set<String> keySet, String appointmentType, String oldAppointmentID) {
        List<String> sortedIDs = new ArrayList<String>(keySet);
        sortedIDs.add(oldAppointmentID);
        Collections.sort(sortedIDs, new Comparator<String>() {
            @Override
            public int compare(String ID1, String ID2) {
                Integer timeSlot1 = 0;
                switch (ID1.substring(3, 4).toUpperCase()) {
                    case "M":
                        timeSlot1 = 1;
                        break;
                    case "A":
                        timeSlot1 = 2;
                        break;
                    case "E":
                        timeSlot1 = 3;
                        break;
                }
                Integer timeSlot2 = 0;
                switch (ID2.substring(3, 4).toUpperCase()) {
                    case "M":
                        timeSlot2 = 1;
                        break;
                    case "A":
                        timeSlot2 = 2;
                        break;
                    case "E":
                        timeSlot2 = 3;
                        break;
                }
                Integer date1 = Integer.parseInt(ID1.substring(8, 10) + ID1.substring(6, 8) + ID1.substring(4, 6));
                Integer date2 = Integer.parseInt(ID2.substring(8, 10) + ID2.substring(6, 8) + ID2.substring(4, 6));
                int dateCompare = date1.compareTo(date2);
                int timeSlotCompare = timeSlot1.compareTo(timeSlot2);
                if (dateCompare == 0) {
                    return ((timeSlotCompare == 0) ? dateCompare : timeSlotCompare);
                } else {
                    return dateCompare;
                }
            }
        });
        int index = sortedIDs.indexOf(oldAppointmentID) + 1;
        for (int i = index; i < sortedIDs.size(); i++) {
            if (!allAppointments.get(appointmentType).get(sortedIDs.get(i)).isFull()) {
                return sortedIDs.get(i);
            }
        }
        return "Failed";
    }

    private boolean exceedWeeklyLimit(String patientID, String appointmentDate) {
        int limit = 0;
        for (int i = 0; i < 3; i++) {
            List<String> registeredIDs = new ArrayList<>();
            switch (i) {
                case 0:
                    if (patientAppointments.get(patientID).containsKey(AppointmentInfo.PHYSICIAN)) {
                        registeredIDs = patientAppointments.get(patientID).get(AppointmentInfo.PHYSICIAN);
                    }
                    break;
                case 1:
                    if (patientAppointments.get(patientID).containsKey(AppointmentInfo.SURGEON)) {
                        registeredIDs = patientAppointments.get(patientID).get(AppointmentInfo.SURGEON);
                    }
                    break;
                case 2:
                    if (patientAppointments.get(patientID).containsKey(AppointmentInfo.DENTAL)) {
                        registeredIDs = patientAppointments.get(patientID).get(AppointmentInfo.DENTAL);
                    }
                    break;
            }
            for (String appointmentID :
                    registeredIDs) {
                if (appointmentID.substring(6, 8).equals(appointmentDate.substring(2, 4)) && appointmentID.substring(8, 10).equals(appointmentDate.substring(4, 6))) {
                    int week1 = Integer.parseInt(appointmentID.substring(4, 6)) / 7;
                    int week2 = Integer.parseInt(appointmentDate.substring(0, 2)) / 7;
                    if (week1 == week2) {
                        limit++;
                    }
                }
                if (limit == 3)
                    return true;
            }
        }
        return false;
    }

    private void addPatientsToNextSameAppointment(String oldAppointmentID, String appointmentType, List<String> registeredPatients) throws RemoteException {
        for (String patientID :
                registeredPatients) {
            if (patientID.substring(0, 3).equals(serverID)) {
                patientAppointments.get(patientID).get(appointmentType).remove(oldAppointmentID);
                String nextSameAppointmentResult = getNextSameAppointment(allAppointments.get(appointmentType).keySet(), appointmentType, oldAppointmentID);
                if (nextSameAppointmentResult.equals("Failed")) {
                    return;
                } else {
                    bookAppointment(patientID, nextSameAppointmentResult, appointmentType);
                }
            } else {
                sendUDPMessage(getServerPort(patientID.substring(0, 3)), "removeAppointment", patientID, appointmentType, oldAppointmentID);
            }
        }
    }

    public Map<String, Map<String, AppointmentInfo>> getAllAppointments() {
        return allAppointments;
    }

    public Map<String, Map<String, List<String>>> getPatientAppointments() {
        return patientAppointments;
    }

    public Map<String, PatientInfo> getServerPatients() {
        return serverPatients;
    }

    public void addNewAppointment(String appointmentID, String appointmentType, int capacity) {
        AppointmentInfo sample = new AppointmentInfo(appointmentType, appointmentID, capacity);
        allAppointments.get(appointmentType).put(appointmentID, sample);
    }

    public void addNewPatientToPatients(String patientID) {
        PatientInfo newPatient = new PatientInfo(patientID);
        serverPatients.put(newPatient.getClientID(), newPatient);
        patientAppointments.put(newPatient.getClientID(), new ConcurrentHashMap<>());
    }

}
