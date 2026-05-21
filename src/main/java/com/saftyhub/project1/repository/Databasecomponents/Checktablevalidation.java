package com.saftyhub.project1.repository.Databasecomponents;

import org.springframework.data.jpa.repository.Query;

public class Checktablevalidation {


@Query(value = "SELECT COUNT(*) FROM information_schema.tables WHERE table_name = ?1", 
       nativeQuery = true)
public boolean checktable(String table_name){
    if (table_name !=null){
        System.out.println("table found successfully");
        return true ;
    }else {
        System.out.println("table not found please recheck the table name and try again");
        return false ;
    }
}

    
}
