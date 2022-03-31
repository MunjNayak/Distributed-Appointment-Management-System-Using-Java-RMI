/** 
 * @author Munj Bhavesh Nayak
 */
package Logger;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Logger {

	 public static final int LOG_TYPE_SERVER = 1;
	    public static final int LOG_TYPE_CLIENT = 0;

	    public static void patientLog(String patientID, String action, String requestParams, String response) throws IOException {
	        FileWriter fileWriter = new FileWriter(getFileName(patientID, LOG_TYPE_CLIENT), true);
	        PrintWriter printWriter = new PrintWriter(fileWriter);
	        printWriter.println("DATE: " + getFormattedDate() + " Patient Action: " + action + " | RequestParameters: " + requestParams + " | Server Response: " + response);

	        printWriter.close();
	    }

	    public static void patientLog(String patientID, String msg) throws IOException {
	        FileWriter fileWriter = new FileWriter(getFileName(patientID, LOG_TYPE_CLIENT), true);
	        PrintWriter printWriter = new PrintWriter(fileWriter);
	        printWriter.println("DATE: " + getFormattedDate() + " " + msg);

	        printWriter.close();
	    }

	    public static void serverLog(String serverID, String patientID, String requestType, String requestParams, String serverResponse) throws IOException {

	        if (patientID.equals("null")) {
	            patientID = "Admin";
	        }
	        FileWriter fileWriter = new FileWriter(getFileName(serverID, LOG_TYPE_SERVER), true);
	        PrintWriter printWriter = new PrintWriter(fileWriter);
	        printWriter.println("DATE: " + getFormattedDate() + " PatientID: " + patientID + " | RequestType: " + requestType + " | RequestParameters: " + requestParams + " | ServerResponse: " + serverResponse);

	        printWriter.close();
	    }

	    public static void serverLog(String serverID, String msg) throws IOException {

	        FileWriter fileWriter = new FileWriter(getFileName(serverID, LOG_TYPE_SERVER), true);
	        PrintWriter printWriter = new PrintWriter(fileWriter);
	        printWriter.println("DATE: " + getFormattedDate() + " " + msg);

	        printWriter.close();
	    }

	    public static void deleteALogFile(String ID) throws IOException {

	        String fileName = getFileName(ID, LOG_TYPE_CLIENT);
	        File file = new File(fileName);
	        file.delete();
	    }

	    private static String getFileName(String ID, int logType) {
	        final String dir = System.getProperty("user.dir");
	        String fileName = dir;
	        if (logType == LOG_TYPE_SERVER) {
	            if (ID.equalsIgnoreCase("MTL")) {
	                fileName = dir + "\\src\\Logs\\Server\\Montreal.txt";
	            } else if (ID.equalsIgnoreCase("SHE")) {
	                fileName = dir + "\\src\\Logs\\Server\\Sherbrooke.txt";
	            } else if (ID.equalsIgnoreCase("QUE")) {
	                fileName = dir + "\\src\\Logs\\Server\\Quebec.txt";
	            }
	        } else {
	            fileName = dir + "\\src\\Logs\\Client\\" + ID + ".txt";
	        }
	        return fileName;
	    }

	    private static String getFormattedDate() {
	        Date date = new Date();

	        String strDateFormat = "yyyy-MM-dd hh:mm:ss a";

	        DateFormat dateFormat = new SimpleDateFormat(strDateFormat);

	        return dateFormat.format(date);
	    }
}
