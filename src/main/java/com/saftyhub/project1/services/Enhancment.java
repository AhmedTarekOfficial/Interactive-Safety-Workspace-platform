package com.saftyhub.project1.services;
import  com.saftyhub.project1.repository.Databasecomponents.* ;
import java.sql.* ;


public class Enhancment {
    
    Statement  st ;

    public  Enhancment(){

    }


    public Enhancment(String tablename , String [] updated_columns ,
     String []  updated_columns_values , String [] conditions  , String [] columns_conditions_value){

    Updateinformation(Querybuilder(tablename ,  updated_columns , updated_columns_values , conditions , columns_conditions_value )) ;


     }
public String Querybuilder(String tablename ,
String [] updated_columns , String [] updated_columns_values , String [] conditions , 
String [] columns_conditions_value
){
    StringBuilder Query = new StringBuilder("update ") ;
    boolean first_input = false ; 
    int index=0 ;

    for (String update : updated_columns) {
        if (first_input == false){
            Query.append(tablename+" "+update+"="+updated_columns_values[index]) ;
            ++index ;
            first_input = true ;
        }else {
             Query.append(","+" "+update+"="+updated_columns_values[index]) ;
             ++index ;
        }

    }
    first_input = false ; 
    index = 0 ;

    if ( conditions.length != 0 && columns_conditions_value.length != 0)  {
        for (String columns :conditions  )
        if (first_input != true ){
            Query.append("where "+columns+"="+columns_conditions_value[index]);
            ++index ;
            first_input = true ;
        }else {
             Query.append(","+columns+"="+columns_conditions_value[index]);
             ++index ;
        }
        
    }


   
    return Query.toString() ;
}

    public void Updateinformation(
String Query  )
{
     Link lm = new Link();
        lm.Connection();
        try{
            if(st.executeUpdate(Query) != 0 ) {
                System.out.println("Updating data successfully ");

            }else {
                System.out.println("Faild to update the data  please recheck your information and try again");
            }
        }catch(Exception ex){

            System.out.println(ex.getMessage());
        }
        

    }
    


}


