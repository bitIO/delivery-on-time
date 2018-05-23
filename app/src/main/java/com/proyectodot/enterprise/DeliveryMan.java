package com.proyectodot.enterprise;

import com.google.firebase.database.DataSnapshot;

public class DeliveryMan {

    private String id;
    private String name;
    private String surname;
    private String phone;
    private String email;
    private String comments;

    public DeliveryMan() {
    }

    public DeliveryMan(String name, String surname, String phone, String email, String comments) {
        this.name = name;
        this.surname = surname;
        this.phone = phone;
        this.email = email;
        this.comments = comments;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    @Override
    public String toString() {
        return name + ' ' + surname + " ("+ phone + ")";
    }

    public static DeliveryMan parse(DataSnapshot snapshot) {
        DeliveryMan instance = new DeliveryMan();
        instance.setId(snapshot.getKey());

        Iterable<DataSnapshot> it = snapshot.getChildren();
        for (DataSnapshot child: it) {
            switch (child.getKey()) {
                case "name":
                    instance.setName((String) child.getValue());
                    break;
                case "surname":
                    instance.setSurname((String) child.getValue());
                    break;
                case "phone":
                    instance.setPhone((String)child.getValue());
                    break;
                case "email":
                    instance.setEmail((String)child.getValue());
                    break;
                case "comments":
                    instance.setComments((String)child.getValue());
                    break;
            }
        }
        return instance;
    }
}
