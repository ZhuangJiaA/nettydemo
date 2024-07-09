package com.sixeco.nettydemo.dto;

public class Student {

    String name;
    int age;

    Student() {
        name = "Tom";
        age = 16;
    }

    Student(String name1) {
        name = name1;
        age = 16;
    }

    Student(int age1) {
        name = "Tome";
        age = age1;
    }

    Student(String name1, int age1) {
        name = name1;
        age = age1;
    }

    public static void main(String[] args) {
        int x = 3;
        while(x < 9) {
            x += 2;
            x++;
            System.out.println("x ======= " + x );
        }
    }

}
