package com.hotel.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class HotelApplicationRequest {
    @NotBlank @Size(max = 150)
    private String hotelName;
    @NotBlank @Size(max = 100)
    private String city;
    @NotBlank @Size(max = 100)
    private String state;
    @NotBlank @Size(max = 255)
    private String address;
    @NotBlank @Size(max = 150)
    private String contactName;
    @NotBlank @Email @Size(max = 150)
    private String email;
    @NotBlank @Size(max = 30)
    private String phone;
    @Min(1)
    private int roomCount;
    @Size(max = 255)
    private String website;
    @Size(max = 2000)
    private String message;
    @Size(min = 6, max = 100)
    private String password;

    public String getHotelName() { return hotelName; }
    public void setHotelName(String hotelName) { this.hotelName = hotelName; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getContactName() { return contactName; }
    public void setContactName(String contactName) { this.contactName = contactName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public int getRoomCount() { return roomCount; }
    public void setRoomCount(int roomCount) { this.roomCount = roomCount; }
    public String getWebsite() { return website; }
    public void setWebsite(String website) { this.website = website; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
