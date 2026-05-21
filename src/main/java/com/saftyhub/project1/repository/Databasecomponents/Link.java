package com.saftyhub.project1.repository.Databasecomponents;

import java.sql.*;

public class Link {

    
     Connection con;
    private static final String URL = "jdbc:mysql://localhost:3306/safety_project";
    private static final String USER = "root";
    private static final String PASSWORD = "ahmed555@_";

    public Link(){
        Connection() ;
    }
    



    public  void Connection(){
          try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            con = DriverManager.getConnection(URL, USER, PASSWORD);
            if (con != null) {
                System.out.println("Connection Successful");
            } else {
                System.out.println("Connection Failed please check your internet connection !");
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    
    


    
}

