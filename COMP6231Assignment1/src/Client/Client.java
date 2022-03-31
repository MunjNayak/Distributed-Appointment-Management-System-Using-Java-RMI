/** 
 * @author Munj Bhavesh Nayak
 */
package Client;


import Info.AppointmentInfo;
import Interface.AppointmentManagementInterface;
import Logger.Logger;

import java.io.IOException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;
public class Client {

	public static final int USER_TYPE_PATIENT = 1;
    public static final int USER_TYPE_ADMIN = 2;
    public static final int PATIENT_BOOK_APPOINTMENT = 1;
    public static final int PATIENT_GET_APPOINTMENT_SCHEDULE = 2;
    public static final int PATIENT_CANCEL_APPOINTMENT = 3;
    public static final int PATIENT_LOGOUT = 4;
    public static final int ADMIN_ADD_APPOINTMENT = 1;
    public static final int ADMIN_REMOVE_APPOINTMENT = 2;
    public static final int ADMIN_LIST_APPOINTMENT_AVAILABILITY = 3;
    public static final int ADMIN_BOOK_APPOINTMENT = 4;
    public static final int ADMIN_GET_APPOINTMENT_SCHEDULE = 5;
    public static final int ADMIN_CANCEL_APPOINTMENT = 6;
    public static final int ADMIN_LOGOUT = 7;
    public static final int SERVER_MONTREAL = 2964;
    public static final int SERVER_SHERBROOKE = 2965;
    public static final int SERVER_QUEBEC = 2966;
    public static final String APPOINTMENT_MANAGEMENT_REGISTERED_NAME = "APPOINTMENT_MANAGEMENT";

    static Scanner input;

    public static void main(String[] args) throws Exception {
        init();
    }

    public static void init() throws IOException {
        input = new Scanner(System.in);
        String userID;
        System.out.println("Please Enter your UserID:");
        userID = input.next().trim().toUpperCase();
        Logger.patientLog(userID, " login attempt");
        switch (checkUserType(userID)) {
            case USER_TYPE_PATIENT:
                try {
                    System.out.println("Patient Login successful (" + userID + ")");
                    Logger.patientLog(userID, " Patient Login successful");
                    patient(userID, getServerPort(userID.substring(0, 3)));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case USER_TYPE_ADMIN:
                try {
                    System.out.println("Admin Login successful (" + userID + ")");
                    Logger.patientLog(userID, " Admin Login successful");
                    admin(userID, getServerPort(userID.substring(0, 3)));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            default:
                System.out.println("!!UserID is not in correct format");
                Logger.patientLog(userID, " UserID is not in correct format");
                Logger.deleteALogFile(userID);
                init();
        }
    }

    private static int getServerPort(String hos) {
        if (hos.equalsIgnoreCase("MTL")) {
            return SERVER_MONTREAL;
        } else if (hos.equalsIgnoreCase("SHE")) {
            return SERVER_SHERBROOKE;
        } else if (hos.equalsIgnoreCase("QUE")) {
            return SERVER_QUEBEC;
        }
        return 1;
    }

    private static int checkUserType(String userID) {
        if (userID.length() == 8) {
            if (userID.substring(0, 3).equalsIgnoreCase("MTL") ||
                    userID.substring(0, 3).equalsIgnoreCase("QUE") ||
                    userID.substring(0, 3).equalsIgnoreCase("SHE")) {
                if (userID.substring(3, 4).equalsIgnoreCase("P")) {
                    return USER_TYPE_PATIENT;
                } else if (userID.substring(3, 4).equalsIgnoreCase("A")) {
                    return USER_TYPE_ADMIN;
                }
            }
        }
        return 0;
    }

    private static void patient(String patientID, int serverPort) throws Exception {
        if (serverPort == 1) {
            return;
        }
        Registry registry = LocateRegistry.getRegistry(serverPort);
        AppointmentManagementInterface remoteObject = (AppointmentManagementInterface) registry.lookup(APPOINTMENT_MANAGEMENT_REGISTERED_NAME);
        boolean repeat = true;
        printMenu(USER_TYPE_PATIENT);
        int menuSelection = input.nextInt();
        String appointmentType;
        String appointmentID;
        String serverResponse;
        switch (menuSelection) {
            case PATIENT_BOOK_APPOINTMENT:
                appointmentType = promptForAppointmentType();
                appointmentID = promptForAppointmentID();
                Logger.patientLog(patientID, " attempting to bookAppointment");
                serverResponse = remoteObject.bookAppointment(patientID, appointmentID, appointmentType);
                System.out.println(serverResponse);
                Logger.patientLog(patientID, " bookAppointment", " appointmentID: " + appointmentID + " appointmentType: " + appointmentType + " ", serverResponse);
                break;
            case PATIENT_GET_APPOINTMENT_SCHEDULE:
                Logger.patientLog(patientID, " attempting to getAppointmentSchedule");
                serverResponse = remoteObject.getAppointmentSchedule(patientID);
                System.out.println(serverResponse);
                Logger.patientLog(patientID, " bookAppointment", " null ", serverResponse);
                break;
            case PATIENT_CANCEL_APPOINTMENT:
                appointmentType = promptForAppointmentType();
                appointmentID = promptForAppointmentID();
                Logger.patientLog(patientID, " attempting to cancelAppointment");
                serverResponse = remoteObject.cancelAppointment(patientID, appointmentID, appointmentType);
                System.out.println(serverResponse);
                Logger.patientLog(patientID, " bookAppointment", " appointmentID: " + appointmentID + " appointmentType: " + appointmentType + " ", serverResponse);
                break;
            case PATIENT_LOGOUT:
                repeat = false;
                Logger.patientLog(patientID, " attempting to Logout");
                init();
                break;
        }
        if (repeat) {
            patient(patientID, serverPort);
        }
    }

    private static void admin(String adminID, int serverPort) throws Exception {
        if (serverPort == 1) {
            return;
        }
        Registry registry = LocateRegistry.getRegistry(serverPort);
        AppointmentManagementInterface remoteObject = (AppointmentManagementInterface) registry.lookup(APPOINTMENT_MANAGEMENT_REGISTERED_NAME);
        boolean repeat = true;
        printMenu(USER_TYPE_ADMIN);
        String patientID;
        String appointmentType;
        String appointmentID;
        String serverResponse;
        int capacity;
        int menuSelection = input.nextInt();
        switch (menuSelection) {
            case ADMIN_ADD_APPOINTMENT:
                appointmentType = promptForAppointmentType();
                appointmentID = promptForAppointmentID();
                capacity = promptForCapacity();
                Logger.patientLog(adminID, " attempting to addAppointment");
                serverResponse = remoteObject.addAppointment(appointmentID, appointmentType, capacity);
                System.out.println(serverResponse);
                Logger.patientLog(adminID, " addAppointment", " appointmentID: " + appointmentID + " appointmentType: " + appointmentType + " appointmentCapacity: " + capacity + " ", serverResponse);
                break;
            case ADMIN_REMOVE_APPOINTMENT:
                appointmentType = promptForAppointmentType();
                appointmentID = promptForAppointmentID();
                Logger.patientLog(adminID, " attempting to removeEvent");
                serverResponse = remoteObject.removeAppointment(appointmentID, appointmentType);
                System.out.println(serverResponse);
                Logger.patientLog(adminID, " removeAppointment", " appointmentID: " + appointmentID + " appointmentType: " + appointmentType + " ", serverResponse);
                break;
            case ADMIN_LIST_APPOINTMENT_AVAILABILITY:
                appointmentType = promptForAppointmentType();
                Logger.patientLog(adminID, " attempting to listAppointmentAvailability");
                serverResponse = remoteObject.listAppointmentAvailability(appointmentType);
                System.out.println(serverResponse);
                Logger.patientLog(adminID, " listAppointmentAvailability", " appointmentType: " + appointmentType + " ", serverResponse);
                break;
            case ADMIN_BOOK_APPOINTMENT:
                patientID = askForPatientIDFromAdmin(adminID.substring(0, 3));
                appointmentType = promptForAppointmentType();
                appointmentID = promptForAppointmentID();
                Logger.patientLog(adminID, " attempting to bookAppointment");
                serverResponse = remoteObject.bookAppointment(patientID, appointmentID, appointmentType);
                System.out.println(serverResponse);
                Logger.patientLog(adminID, " bookAppointment", " patientID: " + patientID + " appointmentID: " + appointmentID + " appointmentType: " + appointmentType + " ", serverResponse);
                break;
            case ADMIN_GET_APPOINTMENT_SCHEDULE:
                patientID = askForPatientIDFromAdmin(adminID.substring(0, 3));
                Logger.patientLog(adminID, " attempting to getAppointmentSchedule");
                serverResponse = remoteObject.getAppointmentSchedule(patientID);
                System.out.println(serverResponse);
                Logger.patientLog(adminID, " getAppointmentSchedule", " patientID: " + patientID + " ", serverResponse);
                break;
            case ADMIN_CANCEL_APPOINTMENT:
                patientID = askForPatientIDFromAdmin(adminID.substring(0, 3));
                appointmentType = promptForAppointmentType();
                appointmentID = promptForAppointmentID();
                Logger.patientLog(adminID, " attempting to cancelAppointment");
                serverResponse = remoteObject.cancelAppointment(patientID, appointmentID, appointmentType);
                System.out.println(serverResponse);
                Logger.patientLog(adminID, " cancelAppointment", " patientID: " + patientID + " appointmentID: " + appointmentID + " appointmentType: " + appointmentType + " ", serverResponse);
                break;
            case ADMIN_LOGOUT:
                repeat = false;
                Logger.patientLog(adminID, "attempting to Logout");
                init();
                break;
        }
        if (repeat) {
            admin(adminID, serverPort);
        }
    }

    private static String askForPatientIDFromAdmin(String hos) {
        System.out.println("Please enter a patientID(Within " + hos + " Server):");
        String userID = input.next().trim().toUpperCase();
        if (checkUserType(userID) != USER_TYPE_PATIENT || !userID.substring(0, 3).equals(hos)) {
            return askForPatientIDFromAdmin(hos);
        } else {
            return userID;
        }
    }

    private static void printMenu(int userType) {
        System.out.println("*************************************");
        System.out.println("Please choose an option below:");
        if (userType == USER_TYPE_PATIENT) {
            System.out.println("1.Book Appointment");
            System.out.println("2.Get Appointment Schedule");
            System.out.println("3.Cancel Appointment");
            System.out.println("4.Logout");
        } else if (userType == USER_TYPE_ADMIN) {
            System.out.println("1.Add Appointment");
            System.out.println("2.Remove Appointment");
            System.out.println("3.List Appointment Availability");
            System.out.println("4.Book Appointment");
            System.out.println("5.Get Appointment Schedule");
            System.out.println("6.Cancel Appointment");
            System.out.println("7.Logout");
        }
    }

    private static String promptForAppointmentType() {
        System.out.println("*************************************");
        System.out.println("Please choose an appointmentType below:");
        System.out.println("1.Physician");
        System.out.println("2.Surgeon");
        System.out.println("3.Dental");
        switch (input.nextInt()) {
            case 1:
                return AppointmentInfo.PHYSICIAN;
            case 2:
                return AppointmentInfo.SURGEON;
            case 3:
                return AppointmentInfo.DENTAL;
        }
        return promptForAppointmentType();
    }

    private static String promptForAppointmentID() {
        System.out.println("*************************************");
        System.out.println("Please enter the AppointmentID (e.g MTLM190120)");
        String appointmentID = input.next().trim().toUpperCase();
        if (appointmentID.length() == 10) {
            if (appointmentID.substring(0, 3).equalsIgnoreCase("MTL") ||
                    appointmentID.substring(0, 3).equalsIgnoreCase("SHE") ||
                    appointmentID.substring(0, 3).equalsIgnoreCase("QUE")) {
                if (appointmentID.substring(3, 4).equalsIgnoreCase("M") ||
                        appointmentID.substring(3, 4).equalsIgnoreCase("A") ||
                        appointmentID.substring(3, 4).equalsIgnoreCase("E")) {
                    return appointmentID;
                }
            }
        }
        return promptForAppointmentID();
    }

    private static int promptForCapacity() {
        System.out.println("*************************************");
        System.out.println("Please enter the booking capacity:");
        return input.nextInt();
    }
}

