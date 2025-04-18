package com.ordermanagementsystem.main;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import com.ordermanagementsystem.dao.IOrderManagementRepository;
import com.ordermanagementsystem.dao.OrderProcessor;
import com.ordermanagementsystem.exception.OrderNotFoundException;
import com.ordermanagementsystem.exception.UserNotFoundExeption;
import com.ordermanagementsystem.model.Clothing;
import com.ordermanagementsystem.model.Electronics;
import com.ordermanagementsystem.model.Product;
import com.ordermanagementsystem.model.User;

public class MainModule {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        IOrderManagementRepository service = new OrderProcessor();

        while (true) {
            System.out.println("\n=== ORDER MANAGEMENT SYSTEM ===");
            System.out.println("1. Create User");
            System.out.println("2. Create Product (Admin only)");
            System.out.println("3. Create Order");
            System.out.println("4. Cancel Order");
            System.out.println("5. View All Products");
            System.out.println("6. View Orders by User");
            System.out.println("7. Exit");
            System.out.print("Enter your choice: ");
            int choice = sc.nextInt();
            sc.nextLine(); // Consume newline

            try {
                switch (choice) {
                    case 1: {
                        System.out.print("Enter User ID: ");
                        int id = sc.nextInt();
                        sc.nextLine();
                        System.out.print("Enter Username: ");
                        String uname = sc.nextLine();
                        System.out.print("Enter Password: ");
                        String pass = sc.nextLine();
                        System.out.print("Enter Role (admin/user): ");
                        String role = sc.nextLine().toLowerCase();

                        if (!role.equals("admin") && !role.equals("user")) {
                            System.out.println("Invalid role. Must be 'admin' or 'user'.");
                            break;
                        }

                        User user = new User(id, uname, pass, role);
                        service.createUser(user);
                        System.out.println("User created successfully.");
                        break;
                    }

                    case 2: {
                        System.out.print("Enter Admin User ID: ");
                        int adminId = sc.nextInt();
                        sc.nextLine();
                        System.out.print("Enter Admin Username: ");
                        String uname = sc.nextLine();
                        System.out.print("Enter Password: ");
                        String pass = sc.nextLine();

                        User admin = new User(adminId, uname, pass, "admin");

                        System.out.print("Enter Product ID: ");
                        int pid = sc.nextInt();
                        sc.nextLine();
                        System.out.print("Enter Product Name: ");
                        String pname = sc.nextLine();
                        System.out.print("Enter Description: ");
                        String desc = sc.nextLine();
                        System.out.print("Enter Price: ");
                        double price = sc.nextDouble();
                        System.out.print("Enter Quantity in Stock: ");
                        int qty = sc.nextInt();
                        sc.nextLine();
                        System.out.print("Enter Type (electronics/clothing): ");
                        String type = sc.nextLine();

                        Product product;
                        if (type.equalsIgnoreCase("electronics")) {
                            System.out.print("Enter Brand: ");
                            String brand = sc.nextLine();
                            System.out.print("Enter Warranty Period (months): ");
                            int warranty = sc.nextInt();
                            sc.nextLine();
                            product = new Electronics(pid, pname, desc, price, qty, type, brand, warranty);
                        } else if (type.equalsIgnoreCase("clothing")) {
                            System.out.print("Enter Size: ");
                            String size = sc.nextLine();
                            System.out.print("Enter Color: ");
                            String color = sc.nextLine();
                            product = new Clothing(pid, pname, desc, price, qty, type, size, color);
                        } else {
                            System.out.println("Invalid product type.");
                            break;
                        }

                        service.createProduct(admin, product);
                        System.out.println("Product added successfully.");
                        break;
                    }

                    case 3: {
                        System.out.print("Enter User ID: ");
                        int id = sc.nextInt();
                        sc.nextLine();
                        System.out.print("Enter Username: ");
                        String uname = sc.nextLine();
                        System.out.print("Enter Password: ");
                        String pass = sc.nextLine();
                        User user = new User(id, uname, pass, "user");

                        List<Product> allProducts = service.getAllProducts();
                        if (allProducts.isEmpty()) {
                            System.out.println("No products available to order.");
                            break;
                        }

                        System.out.println("Available Products:");
                        for (Product p : allProducts) {
                            System.out.println(p);
                        }

                        List<Product> orderList = new ArrayList<>();
                        String choiceMore;
                        do {
                            System.out.print("Enter Product ID to add to order: ");
                            int pid = sc.nextInt();
                            boolean found = false;
                            for (Product p : allProducts) {
                                if (p.getProductId() == pid) {
                                    orderList.add(p);
                                    found = true;
                                    break;
                                }
                            }
                            if (!found) {
                                System.out.println("Invalid product ID.");
                            }
                            System.out.print("Add more products? (yes/no): ");
                            sc.nextLine();
                            choiceMore = sc.nextLine();
                        } while (choiceMore.equalsIgnoreCase("yes"));

                        if (orderList.isEmpty()) {
                            System.out.println("No products added to the order.");
                        } else {
                            service.createOrder(user, orderList);
                            System.out.println("Order placed successfully.");
                        }
                        break;
                    }

                   

                    case 4: {
                        List<Product> products = service.getAllProducts();
                        System.out.println("=== All Products ===");
                        if (products.isEmpty()) {
                            System.out.println("No products available.");
                        } else {
                            for (Product p : products) {
                                System.out.println(p);
                            }
                        }
                        break;
                    }

                    

                    case 6:
                        System.out.println("Exiting system. Thank you!");
                        sc.close();
                        System.exit(0);
                        break;

                    default:
                        System.out.println("Invalid choice. Try again.");
                }

            } catch (UserNotFoundExeption | OrderNotFoundException e) {
                System.err.println("ERROR: " + e.getMessage());
            } catch (Exception e) {
                System.err.println("Unexpected error occurred:");
                e.printStackTrace();
            }
        }
    }
}
