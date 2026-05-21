package com.saftyhub.project1.services;
import org.springframework.stereotype.Service;
import  com.saftyhub.project1.repository.*;


@Service
public class UserAutho {

public  Searchinguserinformation se ;
public  Insert pusshing ;
public Enhancment ens ;
UserRepository user ;



    
public boolean Login(String user_name , String password) {
   // se = new Searchinguserinformation("users",new String [] {"user_name" , "password"}, 
   // new String[] {"user_name" , "password"}, new String [] {user_name , password});

   // if (se == null ) {
   //  System.out.println("Please check you'r user_name and password and try again");;
   //  return false ;
   // }else {
   //  System.out.println("Information found !");
   //  return true ;
   // }

   return true ;

} 


public void Register(String user_name , String password , 
String department  , String job_title ) {


   



}

    
}
