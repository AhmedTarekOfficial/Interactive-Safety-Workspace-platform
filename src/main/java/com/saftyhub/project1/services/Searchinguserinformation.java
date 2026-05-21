package com.saftyhub.project1.services;
import com.saftyhub.project1.repository.Databasecomponents.*;
// import com.saftyhub.project1.services.ManagerLogic.Removing_employee;

import java.sql.*;


public class Searchinguserinformation {
    ResultSet rs ;
    PreparedStatement prd ;

    public Searchinguserinformation(String tablename , String [] Searching_columns ,
     String [] Searching_columncondition , String [] searchingcolumnconditionvalues){
        System.out.println(Searchingquery(tablename ,Searching_columns ,Searching_columncondition, searchingcolumnconditionvalues));
    }

    public String Searchingquery(String tablename , String [] Searching_columns ,
            String [] Searching_columncondition , String [] searchingcolumnconditionvalues){
                StringBuilder searching_query = new StringBuilder("select ");
                boolean first_input  = false ;
                int index = 0 ;

                for (String columns : Searching_columns){
                    if (first_input !=true){
                        searching_query.append(columns);
                        first_input = true ;
                    }else {
                        if (columns.length() !=0 || columns !=null){
                             searching_query.append(","+columns);
                        }else {
                         searching_query.append(" from " + tablename);
                        }
                    }
                }

                first_input = false ;
                if (Searching_columncondition.length !=0 || searchingcolumnconditionvalues.length!=0){
                    for (String conditioncolumns : Searching_columncondition){
                        if (first_input !=true){
                            searching_query.append(" where "+conditioncolumns +"="+searchingcolumnconditionvalues[index]);
                            ++index ;
                            first_input = true ;
                        }else {
                            searching_query.append(","+conditioncolumns +"="+searchingcolumnconditionvalues[index]);
                            ++index ;
                        }
                    }
                }

         return searching_query.toString();
    }

    public String FetchingData(String query) {
        Link ll = new Link() ;
        ll.Connection(); 
        try{
            

        }catch(Exception er){
            System.out.println(er.getMessage());
        }

        return "" ;
    }

}