package Main;

import GUI.*;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
                System.out.println("Launching GUI interface...");
                // Run GUI version on the Event Dispatch Thread
                javax.swing.SwingUtilities.invokeLater(() -> {
                    new LoginTry(); // GUI version
                });
        scanner.close();
    }
}