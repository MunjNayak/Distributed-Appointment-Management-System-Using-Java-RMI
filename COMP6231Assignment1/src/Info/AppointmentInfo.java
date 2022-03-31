/** 
 * @author Munj Bhavesh Nayak
 */
package Info;


import java.util.ArrayList;
import java.util.List; 
import static AppointmentManagement.AppointmentManagement.*;
public class AppointmentInfo {

	public static final String APPOINTMENT_TIME_MORNING = "Morning";
    public static final String APPOINTMENT_TIME_AFTERNOON = "Afternoon";
    public static final String APPOINTMENT_TIME_EVENING = "Evening";
    public static final String PHYSICIAN = "Physician";
    public static final String SURGEON = "Surgeon";
    public static final String DENTAL = "Dental";
    public static final int APPOINTMENT_FULL = -1;
    public static final int ALREADY_REGISTERED = 0;
    public static final int ADD_SUCCESS = 1;
    private String appointmentType;
    private String appointmentID;
    private String appointmentServer;
    private int appointmentCapacity;
    private String appointmentDate;
    private String appointmentTimeSlot;
    private List<String> registeredPatients;

    public AppointmentInfo(String appointmentType, String appointmentID, int appointmentCapacity) {
        this.appointmentID = appointmentID;
        this.appointmentType = appointmentType;
        this.appointmentCapacity = appointmentCapacity;
        this.appointmentTimeSlot = detectAppointmentTimeSlot(appointmentID);
        this.appointmentServer = detectAppointmentServer(appointmentID);
        this.appointmentDate = detectAppointmentDate(appointmentID);
        registeredPatients = new ArrayList<>();
    }

    public static String detectAppointmentServer(String appointmentID) {
        if (appointmentID.substring(0, 3).equalsIgnoreCase("MTL")) {
            return APPOINTMENT_SERVER_MONTREAL;
        } else if (appointmentID.substring(0, 3).equalsIgnoreCase("QUE")) {
            return APPOINTMENT_SERVER_QUEBEC;
        } else {
            return APPOINTMENT_SERVER_SHERBROOK;
        }
    }

    public static String detectAppointmentTimeSlot(String appointmentID) {
        if (appointmentID.substring(3, 4).equalsIgnoreCase("M")) {
            return APPOINTMENT_TIME_MORNING;
        } else if (appointmentID.substring(3, 4).equalsIgnoreCase("A")) {
            return APPOINTMENT_TIME_AFTERNOON;
        } else {
            return APPOINTMENT_TIME_EVENING;
        }
    }

    public static String detectAppointmentDate(String appointmentID) {
        return appointmentID.substring(4, 6) + "/" + appointmentID.substring(6, 8) + "/20" + appointmentID.substring(8, 10);
    }

    public String getAppointmentType() {
        return appointmentType;
    }

    public void setAppointmentType(String appointmentType) {
        this.appointmentType = appointmentType;
    }

    public String getAppointmentID() {
        return appointmentID;
    }

    public void setAppointmentID(String appointmentID) {
        this.appointmentID = appointmentID;
    }

    public String getAppointmentServer() {
        return appointmentServer;
    }

    public void setAppointmentServer(String appointmentServer) {
        this.appointmentServer = appointmentServer;
    }

    public int getAppointmentCapacity() {
        return appointmentCapacity;
    }

    public void setAppointmentCapacity(int appointmentCapacity) {
        this.appointmentCapacity = appointmentCapacity;
    }

    public int getAppointmentRemainCapacity() {
        return appointmentCapacity - registeredPatients.size();
    }


    public String getAppointmentDate() {
        return appointmentDate;
    }

    public void setAppointmentDate(String appointmentDate) {
        this.appointmentDate = appointmentDate;
    }

    public String getAppointmentTimeSlot() {
        return appointmentTimeSlot;
    }

    public void setAppointmentTimeSlot(String appointmentTimeSlot) {
        this.appointmentTimeSlot = appointmentTimeSlot;
    }

    public boolean isFull() {
        return getAppointmentCapacity() == registeredPatients.size();
    }

    public List<String> getRegisteredPatientIDs() {
        return registeredPatients;
    }

    public void setRegisteredPatientsIDs(List<String> registeredPatientIDs) {
        this.registeredPatients = registeredPatientIDs;
    }

    public int addRegisteredPatientID(String registeredPatientID) {
        if (!isFull()) {
            if (registeredPatients.contains(registeredPatientID)) {
                return ALREADY_REGISTERED;
            } else {
                registeredPatients.add(registeredPatientID);
                return ADD_SUCCESS;
            }
        } else {
            return APPOINTMENT_FULL;
        }
    }

    public boolean removeRegisteredPatientID(String registeredPatientID) {
        return registeredPatients.remove(registeredPatientID);
    }

    @Override
    public String toString() {
        return " (" + getAppointmentID() + ") in the " + getAppointmentTimeSlot() + " of " + getAppointmentDate() + " Total[Remaining] Capacity: " + getAppointmentCapacity() + "[" + getAppointmentRemainCapacity() + "]";
    }
}
