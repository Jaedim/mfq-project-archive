import java.io.*;
import java.util.Scanner;

public class Main {
	
	public static void main(String[] args) throws InterruptedException {       
		PrintWriter pw = null;
		Scanner fileInput = null;
		MFQ vm = null;
		
		try {
			pw = new PrintWriter(new FileWriter("output.txt"));
			fileInput = new Scanner(new File("input.txt"));
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
