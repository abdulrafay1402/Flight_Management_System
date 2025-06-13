package Model_and_Interfaces;

public class CBooking {
    private int id;
    private int flightId;
    private int clientId;
    private boolean isPaid;
    private boolean isReserved;
    private double fees;
    private String seatType;

    // Default constructor
    public CBooking() {
        this.isPaid = false;
        this.isReserved = true;
        this.fees = 0.00;
        this.seatType = "economy";
    }

    // Parameterized constructor
    public CBooking(int id, int flightId, int clientId, boolean isPaid, boolean isReserved, double fees, String seatType) {
        this.id = id;
        this.flightId = flightId;
        this.clientId = clientId;
        this.isPaid = isPaid;
        this.isReserved = isReserved;
        this.fees = fees;
        this.seatType = seatType;
    }

    // Getters and setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getFlightId() {
        return flightId;
    }

    public void setFlightId(int flightId) {
        this.flightId = flightId;
    }

    public int getClientId() {
        return clientId;
    }

    public void setClientId(int clientId) {
        this.clientId = clientId;
    }

    public boolean isPaid() {
        return isPaid;
    }

    public void setPaid(boolean isPaid) {
        this.isPaid = isPaid;
    }

    public boolean isReserved() {
        return isReserved;
    }

    public void setReserved(boolean isReserved) {
        this.isReserved = isReserved;
    }

    public double getFees() {
        return fees;
    }

    public void setFees(double fees) {
        this.fees = fees;
    }

    public String getSeatType() {
        return seatType;
    }

    public void setSeatType(String seatType) {
        this.seatType = seatType;
    }

    @Override
    public String toString() {
        return "Booking{" +
                "id=" + id +
                ", flightId=" + flightId +
                ", clientId=" + clientId +
                ", isPaid=" + isPaid +
                ", isReserved=" + isReserved +
                ", fees=" + fees +
                ", seatType='" + seatType + '\'' +
                '}';
    }
}
