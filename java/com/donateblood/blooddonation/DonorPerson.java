package com.donateblood.blooddonation;

public class DonorPerson{

    public String ID,name,email,number,image,age;


    public DonorPerson(String name, String email, String number, String image,String age,String ID) {
        this.name=name;
        this.email=email;
        this.number=number;
        this.image=image;
        this.age = age;
        this.ID = ID;
    }

    public String getName() {
        return name;
    }
    public  String getID(){
        return ID;
    }

    public String getEmail() {
        return email;
    }

    public String getNumber() {
        return number;
    }

    public String getImage() {
        return image;
    }
    public String getAge(){
        return age;
    }

}
