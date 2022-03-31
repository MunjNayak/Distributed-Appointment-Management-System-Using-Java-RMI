/** 
 * @author Munj Bhavesh Nayak
 */
package Interface;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface AppointmentManagementInterface extends Remote {

	/**
     * Only Admin
     */
    String addAppointment(String appointmentID, String appointmentType, int bookingCapacity) throws RemoteException;

    String removeAppointment(String appointmentID, String appointmentType) throws RemoteException;

    String listAppointmentAvailability(String appointmentType) throws RemoteException;

    /**
     * Both Admin and Patient
     */
    String bookAppointment(String patientID, String appointmentID, String appointmentType) throws RemoteException;

    String getAppointmentSchedule(String patientID) throws RemoteException;

    String cancelAppointment(String patientID, String appointmentID, String appointmentType) throws RemoteException;
}
