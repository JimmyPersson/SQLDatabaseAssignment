package com.company;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;

public class Initiation {
    String userName, password;
    Scanner input = new Scanner(System.in);
    Properties prop = new Properties();
    List<Shoes> cart = new ArrayList<>();
    int id;
    int orderNr;

    public Initiation() {
        try {
            prop.load(new FileInputStream("C:\\Users\\jimmy\\IdeaProjects\\ShoeDatabase\\src\\com\\company\\connection.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void Welcome() {
        String choice1;
        System.out.println("\n\n**** Welcome to the digital shoeshop! ****\n\n" +
                "Existing customer? Enter 1.\n" +
                "New customer? Enter 2. (Alpha-version)");
        choice1 = input.nextLine();

        if (choice1.equals("1")) LoginUser();
        else if (choice1.equals("2")) System.out.println("Fiskfest!");
        else {
            System.out.println("Easy instructions might not be your forté, try again!");
            try {
                Thread.sleep(2000);
                Welcome();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    public void LoginUser() {
        System.out.println("Please enter your name: ");
        userName = input.nextLine();
        System.out.println("Please enter your password: ");
        password = input.nextLine();


        try (Connection con = DriverManager.getConnection(prop.getProperty("connection"),
                prop.getProperty("name"),
                prop.getProperty("password"));
             Statement statement = con.createStatement()) {

            ResultSet rs;

            rs = statement.executeQuery("SELECT * FROM customer WHERE firstname ='" + userName + "' AND password ='" + password + "'");
            if (rs.next()) {
                Customer customer = new Customer();
                customer.setFirstname(rs.getString("firstname"));
                customer.setPassword(rs.getString("password"));
                customer.setId(rs.getInt("id"));
                id = rs.getInt("id");
                landingPage(customer.getId());
            } else {
                System.out.println("Wrong username or password! Please try again.");
                Thread.sleep(2000);
                LoginUser();
            }
        } catch (SQLException a) {
            System.out.println(a.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void landingPage(int id) {
        String choice2;

        System.out.println("\nWelcome back! Please choose an option below: \n\n"
                + "Enter 1 to order more shoes.\n"
                + "Enter 2 to add products to previous orders.\n");
        choice2 = input.nextLine();
        while (true) {
            if (choice2.equals("1")) {
                chooseShoe();
                break;
            } else if (choice2.equals("2")) {
                System.out.println("Enter previous order number: ");
                orderNr = Integer.parseInt(input.nextLine());
                chooseShoe();
                break;
            } else System.out.println("Please enter a valid option.");
        }
    }

    public void chooseShoe() {
        List<Shoes> shoesList = new ArrayList();
        String category;
        String choice3;
        int shoe;
        int count = 1;
        int count2 = 1;

        System.out.println("\nStart by choosing category: \n");
        try (Connection con = DriverManager.getConnection(prop.getProperty("connection"),
                prop.getProperty("name"),
                prop.getProperty("password"));
             Statement statement = con.createStatement();) {

            ResultSet rs;
            rs = statement.executeQuery("SELECT name, id FROM category");
            while (rs.next()) {
                System.out.println(count + ". " + rs.getString("name"));
                count++;
            }

            category = input.nextLine();
            if (Integer.parseInt(category) < count) {
                rs = statement.executeQuery("SELECT brand.name, shoes.id, color, price, size, stock" +
                        " FROM belongs INNER JOIN shoes" +
                        " ON shoeid = shoes.id" +
                        " INNER JOIN brand ON brand.id = shoes.brandid WHERE shoes.categoryid ='" + category + "'");

                while (rs.next()) {
                    Shoes tempShoe = new Shoes();
                    tempShoe.setBrand(rs.getString("brand.name"));
                    tempShoe.setId(rs.getInt("id"));
                    tempShoe.setColor(rs.getString("color"));
                    tempShoe.setPrice(rs.getInt("price"));
                    tempShoe.setSize(rs.getInt("size"));
                    tempShoe.setStock(rs.getInt("stock"));
                    shoesList.add(tempShoe);
                }

            } else {
                System.out.println("Not a valid choice, try again!");
                Thread.sleep(2000);
                chooseShoe();

            }
            System.out.println("\n\nAvailable shoes: \n");
            for (Shoes s : shoesList) {
                System.out.println(count2 + ". " + s.getBrand() +
                        " | " + "Color: " + s.getColor() +
                        " | " + "Size: " + s.getSize() +
                        " | " + "Price: " + s.getPrice() +
                        " | " + "Stock: " + s.getStock());
                count2++;
            }
            System.out.println("Please pick a shoe: ");
            shoe = Integer.parseInt(input.nextLine());
            if (shoe <= shoesList.size()) {
                if (shoesList.get(shoe - 1).getStock() != 0) {
                    cart.add(shoesList.get(shoe - 1));
                    System.out.println("Do you want to purchase more shoes or checkout?\n" +
                            "1. Continue shopping.\n" +
                            "2. Checkout.");
                    choice3 = input.nextLine();
                    if (choice3.equals("1")) chooseShoe();
                    else if (choice3.equals("2")) viewShoppingCart(cart);
                    else {
                        System.out.println("Invalid input - sending you to shopping cart!");
                        Thread.sleep(2000);
                        viewShoppingCart(cart);
                    }
                } else {
                    System.out.println("Shoe out of stock. Please choose another shoe.");
                    Thread.sleep(2000);
                    chooseShoe();
                }
            } else {
                System.out.println("Not a valid choice, try again!");
                Thread.sleep(2000);
                chooseShoe();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void viewShoppingCart(List<Shoes> cart) {
        String choice4;

        System.out.println("Order summary: \n");
        for (Shoes s : cart) {
            System.out.println("Brand: " + s.getBrand() +
                    " | Color: " + s.getColor() +
                    " | Size: " + s.getSize() +
                    " | Price: " + s.getPrice());
        }
        System.out.println("\nDo you want to proceed with your order\n" +
                "1. Confirm purchase.\n" +
                "2. Exit.");
        choice4 = input.nextLine();

        if (choice4.equals("1")) pushOrder();
        else if (choice4.equals("2")) {
            System.out.println("We could've been friends :((");
        } else {
            System.out.println("Not a valid choice! Try again!");
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            viewShoppingCart(cart);
        }
    }

    public void pushOrder() {
        for (int i = 0; i < cart.size(); i++) {
            try (Connection con = DriverManager.getConnection(prop.getProperty("connection"),
                    prop.getProperty("name"),
                    prop.getProperty("password"));
                 CallableStatement statement = con.prepareCall("CALL AddToCart(?,?,?,?)");) {
                if (i == 0) {
                    statement.setInt(1, id);
                    statement.setInt(2, orderNr);
                    statement.setInt(3, cart.get(i).getId());
                    statement.registerOutParameter(4, Types.INTEGER);
                    statement.executeUpdate();
                    orderNr = statement.getInt(4);
                } else {
                    statement.setInt(1, id);
                    statement.setInt(2, orderNr);
                    statement.setInt(3, cart.get(i).getId());
                    statement.registerOutParameter(4, Types.INTEGER);
                    statement.executeUpdate();
                }
                System.out.println("Thank you for your purchase! Please come back - it's lonely here!");
            } catch (SQLException e) {
                System.out.println(e);
            }
        }
    }
}



