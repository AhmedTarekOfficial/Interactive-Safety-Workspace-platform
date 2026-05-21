package com.saftyhub.project1.services;

import com.saftyhub.project1.repository.Databasecomponents.*;
import com.saftyhub.project1.repository.Databasecomponents.*;
 import java.sql.*;
public class Insert  {
    
    Link Databaseconnection ;
    Statement st ;
   
    


Checktablevalidation chk ;

public Insert(String tablename , String [] insertion_values , 
String [] column_conditions){

if (tablename !=null){
     Databaseconnection = new Link() ;
    try{
       st.executeUpdate(querybuilder(tablename, insertion_values, column_conditions));
       System.out.println("Done Inserted Data succsessfully !");

    }catch(Exception er ){
        System.out.println("Inserted Data failled please check all input information and try again ");
        er.getMessage() ;
    }
    
   
    
    
    


}else {
    System.out.println("sorry but we can't find the table");
}

}

public String querybuilder(String tablename , String [] values 
, String [] columnsconditions
){
    StringBuilder insertionquery = new StringBuilder("insert into " + tablename+" ");
    boolean first_input = false ;
    int index = 0 ; 

    if(columnsconditions.length !=0){
      for (String conditions : columnsconditions)  {
        if (first_input!=true){
            insertionquery.append("("+conditions);
            first_input = true ;
        }else{
            if (conditions.length() !=0  && conditions != null ){
                insertionquery.append(","+conditions);
            }
            
        }
      }
    }

    first_input = false ;

    for (String value : values){
        if (first_input != true ){
            insertionquery.append("values("+values);
            first_input = true ;
        }else {
            if (value !=null || value.length() !=0) {
                 insertionquery.append(","+values+"");

            }else {
                insertionquery.append(")");
            }
    

        }


    }

    


    


    return insertionquery.toString() ;
}

// @Overload 
// public validate_value(String value) {

//     return "" ;
// }


// public static void main(String[] args) {

//     Insert ahmed =  new Insert("department", 
//     new  String[] {"2" ,"Technichal" , "1"}, new String[] {""});

//     System.out.println(ahmed.querybuilder("department", new String [] {"2" , "technichal" , "1"} , new String [] {})) ;
// }    


}
