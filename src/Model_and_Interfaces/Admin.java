package Model_and_Interfaces;

import Exceptions.*;
import Model_and_Interfaces.*;
import com.sun.security.jgss.GSSUtil;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Scanner;


public class Admin extends User<Admin> implements PlaneOperations, FlightOperations,ClientOperations, BookingOperations {
    private String companyname;
    private float profit;
    private final Connection connection;
    private final Scanner scanner;
    public Admin(int id, String name, String password, String role, Connection connection, Scanner scanner, float profit, String companyname) {
        super(id, name, password,companyname+"@ats.com", role);
        this.connection = connection;
        this.scanner = scanner;
        this.profit = profit;
        this.companyname = companyname;
    }

    public String getCompanyname() {
        return companyname;
    }

    public float getProfit() {
        return profit;
    }

    public void setProfit(float profit) {
        this.profit = profit;
    }
    public void setCompanyname(String companyname) {
        this.companyname = companyname;
    }

    @Override
    public void menu(Admin admin) throws Exception
    {
        System.out.println("Welcome ADMIN:-> " + admin.name);
        while (true) {
            System.out.println("1.  View Clients");//
            System.out.println("2.  View Planes");//
            System.out.println("3.  View All Bookings");//
            System.out.println("4.  View All Flights");//
            System.out.println("5.  Add Plane");//
            System.out.println("6.  Add Flight");//
            System.out.println("7.  Update Flight");
            System.out.println("8. Log out");
            System.out.print("Enter: ");

            int option = scanner.nextInt();

            switch (option) {
                case 1:
                    viewClients(connection);//view Client
                    break;
                case 2:
                    viewPlanes(connection);//view Planes
                    break;
                case 3:
                    viewAllBookings(connection);// view Bookings
                    break;
                case 4:
                    viewFlights(connection);//view Flights
                    break;
                case 5:
                    //Add Plane
                    scanner.nextLine();
                    System.out.print("Enter Plane Model: ");
                    String planeModel = scanner.nextLine();

                    System.out.print("Enter Manufacturer: ");
                    String manufacturer = scanner.nextLine();
                    if(planeExists(connection,planeModel,manufacturer))
                    {
                        System.out.println("Plane already exists...");
                        break;
                    }
                    System.out.print("Enter number of Business Seats: ");
                    int businessSeats = scanner.nextInt();

                    scanner.nextLine();
                    if(businessSeats<0){
                        throw new ValueLessThanZeroException("Business seats cant be negative");
                    }
                    System.out.print("Enter number of Economy Seats: ");
                    int economySeats = scanner.nextInt();

                    scanner.nextLine();
                    if(economySeats<0){
                        throw new ValueLessThanZeroException("Economy seats cant be negative");
                    }
                    addPlane(connection,id,planeModel, manufacturer, businessSeats, economySeats);
                    break;
                case 6:
                    //addFlight
                    System.out.println("Adding a New Flight...");
                    System.out.print("Enter plane ID: ");
                    int plane_id = scanner.nextInt();
                    scanner.nextLine(); // consume newline
                    if(planeBelongsToAdmin(connection,plane_id,this.id)){
                    try {
                        //int plane_id = scanner.nextInt();
                        scanner.nextLine(); // consume newline
                        if(!planeExists(connection,plane_id)){
                            throw new PlaneNotFoundException("Plane not available");
                        }
                        if(plane_id<0)
                        {
                            throw new ValueLessThanZeroException("Id cant be negative");
                        }
                        System.out.print("Enter source: ");
                        String source = scanner.nextLine();

                        System.out.print("Enter destination: ");
                        String destination = scanner.nextLine();

                        // Arrival time
                        System.out.println("Enter arrival time:");
                        System.out.print("  Hour (0-23): ");
                        int arrHour = scanner.nextInt();
                        System.out.print("  Minute (0-59): ");
                        int arrMinute = scanner.nextInt();
                        System.out.print("  Second (0-59): ");
                        int arrSecond = scanner.nextInt();
                        scanner.nextLine(); // consume newline
                        if (arrHour < 0 || arrHour > 23 || arrMinute < 0 || arrMinute > 59 || arrSecond < 0 || arrSecond > 59) {
                            throw new InvalidTimeException("Invalid Time");
                        }
                        LocalDate today = LocalDate.now();
                        LocalTime arrivalLocalTime = LocalTime.of(arrHour, arrMinute, arrSecond);
                        Timestamp arrival_time = Timestamp.valueOf(today.atTime(arrivalLocalTime));

                        // Reporting time
                        System.out.println("Enter reporting time:");
                        System.out.print("  Hour (0-23): ");
                        int repHour = scanner.nextInt();
                        System.out.print("  Minute (0-59): ");
                        int repMinute = scanner.nextInt();
                        System.out.print("  Second (0-59): ");
                        int repSecond = scanner.nextInt();
                        scanner.nextLine(); // consume newline
                        if (repHour < 0 || repHour > 23 || repMinute < 0 || repMinute > 59 || repSecond < 0 || repSecond > 59) {
                            throw new InvalidTimeException("Invalid Time");
                        }
                        LocalTime reportingLocalTime = LocalTime.of(repHour, repMinute, repSecond);
                        Timestamp reporting_time = Timestamp.valueOf(today.atTime(reportingLocalTime));

                        System.out.print("Enter expense: ");
                        float expense = scanner.nextFloat();
                        scanner.nextLine(); // optional, for safety
                        if(expense<0)
                        {
                            throw  new ValueLessThanZeroException("Expense cant be negative");
                        }
                        if(flightExists(connection,plane_id,source,destination,arrival_time,reporting_time,expense)){
                            throw new FlightAlreadyExistsException("Flight already exists");
                        }
                        // Pass to your method
                        admin.addFlight(connection,plane_id, source, destination, arrival_time, reporting_time, expense);

                    } catch (Exception e) {
                        System.out.println("Invalid input: " + e.getMessage());
                    }
                    }else System.out.println("The plane Doesn't belongs to you");
                    break;
                case 7:
                    //Update Flight
                    System.out.println("Updating flight");
                    System.out.print("Enter Flight ID: ");
                    int flightId = scanner.nextInt();
                    scanner.nextLine(); // Consume newline
                    if(!flightExists(connection,flightId))
                    {
                        throw new FlightDoesntExistsExeption("Flight does not exists");
                    }
                    if(flightId<0)
                    {
                        throw new ValueLessThanZeroException("Flight id cant be negative");
                    }
                    if(flightBelongsToAdmin(connection,flightId,this.id)){
                    System.out.print("Enter field to update (source, destination, arrival_time, reporting_time, expense): ");
                    String field = scanner.nextLine();
                    try{
                        if (!field.equals("source") &&
                                !field.equals("destination") &&
                                !field.equals("arrival_time") &&
                                !field.equals("reporting_time") &&
                                !field.equals("expense")) {
                            throw new IllegalArgumentException("Invalid field entered.");
                        }
                    }catch (IllegalArgumentException e)
                    {
                        System.out.println(e.getMessage());
                    }

                    try{
                        System.out.println(admin.updateFlight(connection,flightId, field, scanner));
                    }catch (Exception e)
                    {
                        System.out.println(e.getMessage());
                    }
                    }else System.out.println("Flight doesn't belongs to you");
                    break;
                case 8://logout
                    System.out.println("Logging out...");
                    return;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }

}
