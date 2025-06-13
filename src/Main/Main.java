package Main;

import GUI.*;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("   \\\\\\\\\\\\\\");
        System.out.println("  | ^   ^ |");
        System.out.println(" (|  O O  |)  Where would you like to start?");
        System.out.println("  |   ∆   |   [1] Graphical Interface (GUI)");
        System.out.println("   \\_____/    [2] Console Interface");
        System.out.println("    |   |");
        System.out.print("Your choice (1/2): ");

        int choice = 0;
        try {
            choice = Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Please enter 1 or 2.");
            System.exit(1);
        }

        switch(choice) {
            case 1:
                System.out.println("Launching GUI interface...");
                // Run GUI version on the Event Dispatch Thread
                javax.swing.SwingUtilities.invokeLater(() -> {
                    new LoginTry(); // GUI version
                });
                break;
            case 2:
                System.out.println("Launching console interface...");
                EntryPoint entryPoint = new EntryPoint();
                try {
                    entryPoint.startApplication(); // Console version
                } catch (Exception e) {
                    System.err.println("Failed to start console application: " + e.getMessage());
                }
                break;
            default:
                System.out.println("Invalid choice. Exiting...");
                System.exit(1);
        }

        scanner.close();
    }
}