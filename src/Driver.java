import java.io.*;
import java.util.Scanner;

/**
 * Entry point for the program. The control of flow starts here and is
 * ended here. Contains the interaction of main objects of the program.
 *
 */
public class Driver {
	/**
	 * Main entry for program. It handles all visible exceptions by
	 * outputting one or more strings and closing gracefully.
	 * @param args
	 * @throws InterruptedException 
	 */
	public static void main(String[] args) throws InterruptedException {       
		PrintWriter pw = null;
		Scanner fileInput = null;
		MFQ vm = null;
		
		try {
			pw = new PrintWriter(new FileWriter("csis.txt"));
			fileInput = new Scanner(new File("mfq.txt"));
			vm = new MFQ(fileInput, pw); // "virtual machine"
			
		} catch (IOException e) {
			System.out.println("Input file could not be found." +
					"Program will now exit.");
			pw.close();
			System.exit(1);
		}
		vm.toString(); // Added just to suppress warning
		pw.close();
		fileInput.close();
	}
}
