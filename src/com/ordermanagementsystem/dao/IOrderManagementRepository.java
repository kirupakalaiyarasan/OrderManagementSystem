package com.ordermanagementsystem.dao;

import com.ordermanagementsystem.exception.OrderNotFoundException;
import com.ordermanagementsystem.exception.UserNotFoundExeption;
import com.ordermanagementsystem.model.Product;
import com.ordermanagementsystem.model.User;

import java.util.List;

public interface IOrderManagementRepository {

    void createOrder(User user, List<Product> products) throws Exception;

    void cancelOrder(int userId, int orderId) throws OrderNotFoundException, UserNotFoundExeption, Exception;

    void createProduct(User user, Product product) throws Exception;

    void createUser(User user) throws Exception;

    List<Product> getAllProducts() throws Exception;

    List<Product> getOrderByUser(User user) throws UserNotFoundExeption, Exception;
}
