package com.ordermanagementsystem.dao;

import com.ordermanagementsystem.exception.OrderNotFoundException;
import com.ordermanagementsystem.exception.UserNotFoundExeption;
import com.ordermanagementsystem.model.Clothing;
import com.ordermanagementsystem.model.Electronics;
import com.ordermanagementsystem.model.Product;
import com.ordermanagementsystem.model.User;
import com.ordermanagementsystem.util.DbConnectionUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrderProcessor implements IOrderManagementRepository {

    @Override
    public void createOrder(User user, List<Product> products) throws Exception {
        Connection conn = DbConnectionUtil.getConnection();

        String checkUser = "SELECT * FROM users WHERE userid = ?";
        PreparedStatement ps = conn.prepareStatement(checkUser);
        ps.setInt(1, user.getUserId());
        ResultSet rs = ps.executeQuery();

        if (!rs.next()) {
            createUser(user); 
        }

        String insertOrder = "INSERT INTO orders(userid) VALUES(?)";
        ps = conn.prepareStatement(insertOrder, Statement.RETURN_GENERATED_KEYS);
        ps.setInt(1, user.getUserId());
        ps.executeUpdate();

        rs = ps.getGeneratedKeys();
        int orderId = 0;
        if (rs.next()) {
            orderId = rs.getInt(1);
        }

        String insertDetails = "INSERT INTO order_details(orderid, productid) VALUES(?, ?)";
        for (Product product : products) {
            ps = conn.prepareStatement(insertDetails);
            ps.setInt(1, orderId);
            ps.setInt(2, product.getProductId());
            ps.executeUpdate();
        }

        conn.close();
    }

    @Override
    public void cancelOrder(int userId, int orderId) throws OrderNotFoundException, UserNotFoundExeption, Exception {
        Connection conn = DbConnectionUtil.getConnection();

        PreparedStatement ps = conn.prepareStatement("SELECT * FROM users WHERE userid = ?");
        ps.setInt(1, userId);
        ResultSet rs = ps.executeQuery();
        if (!rs.next()) {
            throw new UserNotFoundExeption("User ID not found: " + userId);
        }

        ps = conn.prepareStatement("SELECT * FROM orders WHERE orderid = ?");
        ps.setInt(1, orderId);
        rs = ps.executeQuery();
        if (!rs.next()) {
            throw new OrderNotFoundException("Order ID not found: " + orderId);
        }

        ps = conn.prepareStatement("DELETE FROM order_details WHERE orderid = ?");
        ps.setInt(1, orderId);
        ps.executeUpdate();

        ps = conn.prepareStatement("DELETE FROM orders WHERE orderid = ?");
        ps.setInt(1, orderId);
        ps.executeUpdate();

        conn.close();
    }

    @Override
    public void createProduct(User user, Product product) throws Exception {
        Connection conn = DbConnectionUtil.getConnection();

        PreparedStatement ps = conn.prepareStatement("SELECT * FROM users WHERE userid = ?");
        ps.setInt(1, user.getUserId());
        ResultSet rs = ps.executeQuery();
        if (!rs.next() || !user.getRole().equalsIgnoreCase("admin")) {
            throw new Exception("Only admin users can add products.");
        }

        ps = conn.prepareStatement("INSERT INTO products(productid, productname, description, price, quantityinstock, type, brand, warranty, size, color) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
        ps.setInt(1, product.getProductId());
        ps.setString(2, product.getProductName());
        ps.setString(3, product.getDescription());
        ps.setDouble(4, product.getPrice());
        ps.setInt(5, product.getQuantityInStock());
        ps.setString(6, product.getType());

        if (product instanceof Electronics) {
            Electronics e = (Electronics) product;
            ps.setString(7, e.getBrand());
            ps.setInt(8, e.getWarrantyPeriod());
            ps.setNull(9, Types.VARCHAR);
            ps.setNull(10, Types.VARCHAR);
        } else if (product instanceof Clothing) {
            Clothing c = (Clothing) product;
            ps.setNull(7, Types.VARCHAR);
            ps.setNull(8, Types.INTEGER);
            ps.setString(9, c.getSize());
            ps.setString(10, c.getColor());
        } else {
            ps.setNull(7, Types.VARCHAR);
            ps.setNull(8, Types.INTEGER);
            ps.setNull(9, Types.VARCHAR);
            ps.setNull(10, Types.VARCHAR);
        }

        ps.executeUpdate();
        conn.close();
    }

    @Override
    public void createUser(User user) throws Exception {
        Connection conn = DbConnectionUtil.getConnection();
        PreparedStatement ps = conn.prepareStatement("INSERT INTO users(userid, username, password, role) VALUES (?, ?, ?, ?)");
        ps.setInt(1, user.getUserId());
        ps.setString(2, user.getUsername());
        ps.setString(3, user.getPassword());
        ps.setString(4, user.getRole());
        ps.executeUpdate();
        conn.close();
    }

    @Override
    public List<Product> getAllProducts() throws Exception {
        List<Product> products = new ArrayList<>();

        Connection conn = DbConnectionUtil.getConnection();
        PreparedStatement stmt = conn.prepareStatement("SELECT * FROM products");
        ResultSet rs = stmt.executeQuery();

        while (rs.next()) {
            int id = rs.getInt("productid");
            String name = rs.getString("productname");
            String desc = rs.getString("description");
            double price = rs.getDouble("price");
            int qty = rs.getInt("quantityinstock");
            String type = rs.getString("type");

            if ("electronics".equalsIgnoreCase(type)) {
                String brand = rs.getString("brand");
                int warranty = rs.getInt("warranty");
                products.add(new Electronics(id, name, desc, price, qty, type, brand, warranty));
            } else if ("clothing".equalsIgnoreCase(type)) {
                String size = rs.getString("size");
                String color = rs.getString("color");
                products.add(new Clothing(id, name, desc, price, qty, type, size, color));
            } else {
                products.add(new Product(id, name, desc, price, qty, type));
            }
        }

        conn.close();
        return products;
    }

    @Override
    public List<Product> getOrderByUser(User user) throws UserNotFoundExeption, Exception {
        List<Product> orderedProducts = new ArrayList<>();
        Connection conn = DbConnectionUtil.getConnection();

        String query = "SELECT p.* FROM products p " +
                       "JOIN order_details od ON p.productid = od.productid " +
                       "JOIN orders o ON od.orderid = o.orderid " +
                       "WHERE o.userid = ?";
        PreparedStatement ps = conn.prepareStatement(query);
        ps.setInt(1, user.getUserId());
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            int id = rs.getInt("productid");
            String name = rs.getString("productname");
            String desc = rs.getString("description");
            double price = rs.getDouble("price");
            int qty = rs.getInt("quantityinstock");
            String type = rs.getString("type");

            if ("electronics".equalsIgnoreCase(type)) {
                String brand = rs.getString("brand");
                int warranty = rs.getInt("warranty");
                orderedProducts.add(new Electronics(id, name, desc, price, qty, type, brand, warranty));
            } else if ("clothing".equalsIgnoreCase(type)) {
                String size = rs.getString("size");
                String color = rs.getString("color");
                orderedProducts.add(new Clothing(id, name, desc, price, qty, type, size, color));
            } else {
                orderedProducts.add(new Product(id, name, desc, price, qty, type));
            }
        }

        conn.close();
        return orderedProducts;
    }
}
