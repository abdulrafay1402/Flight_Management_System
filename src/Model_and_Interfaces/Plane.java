package Model_and_Interfaces;

public class Plane{

    private int id,admin_id,business_seats,economy_seats;
    private String plane_model,manufacturer;

    public Plane(int id, int admin_id, int business_seats, int economy_seats, String plane_model, String manufacturer) {
        this.id = id;
        this.admin_id = admin_id;
        this.business_seats = business_seats;
        this.economy_seats = economy_seats;
        this.plane_model = plane_model;
        this.manufacturer = manufacturer;
    }
}
