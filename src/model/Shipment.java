package model;

import java.time.LocalDate;

public class Shipment {
    private int shipmentId;
    private String batchName;
    private String departureCountry;
    private String arrivalCountry;
    private double shipmentCost;
    private LocalDate departureDate;
    private LocalDate arrivalDate;
    private String status;
    private double transportationCost;
    private double otherCosts;

    public int getShipmentId() { return shipmentId; }
    public void setShipmentId(int shipmentId) { this.shipmentId = shipmentId; }

    public String getBatchName() { return batchName; }
    public void setBatchName(String batchName) { this.batchName = batchName; }

    public String getDepartureCountry() { return departureCountry; }
    public void setDepartureCountry(String departureCountry) { this.departureCountry = departureCountry; }

    public String getArrivalCountry() { return arrivalCountry; }
    public void setArrivalCountry(String arrivalCountry) { this.arrivalCountry = arrivalCountry; }

    public double getShipmentCost() { return shipmentCost; }
    public void setShipmentCost(double shipmentCost) { this.shipmentCost = shipmentCost; }

    public LocalDate getDepartureDate() { return departureDate; }
    public void setDepartureDate(LocalDate departureDate) { this.departureDate = departureDate; }

    public LocalDate getArrivalDate() { return arrivalDate; }
    public void setArrivalDate(LocalDate arrivalDate) { this.arrivalDate = arrivalDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public double getTransportationCost() { return transportationCost; }
    public void setTransportationCost(double transportationCost) { this.transportationCost = transportationCost; }

    public double getOtherCosts() { return otherCosts; }
    public void setOtherCosts(double otherCosts) { this.otherCosts = otherCosts; }

    @Override
    public String toString() {
        return getDisplayText();
    }

    public String getDisplayText() {
        String dateStr = arrivalDate != null ? arrivalDate.toString() : "TBD";
        return batchName + " (Arrival: " + dateStr + ")";
    }
}
