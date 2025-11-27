package model;

public class DeliveryOption {
    private int deliveryOptionId;
    private String name;
    private String description;
    private String contactInfo;

    public int getDeliveryOptionId() { return deliveryOptionId; }
    public void setDeliveryOptionId(int deliveryOptionId) { this.deliveryOptionId = deliveryOptionId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getContactInfo() { return contactInfo; }
    public void setContactInfo(String contactInfo) { this.contactInfo = contactInfo; }
}
