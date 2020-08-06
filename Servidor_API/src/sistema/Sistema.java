/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sistema;

import interfaces.AlmacenamientoInterface;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import modelos.Almacenamiento;

/**
 *
 * @author kachu
 */
public class Sistema {

    public static void main(String[] args) throws RemoteException, MalformedURLException, NotBoundException, NoSuchAlgorithmException {
        /*
            ¿Que argumentos recibo?
             * 1er argumento mi ip 
             * 2do argumento mi puerto
             * siguientes argumentos ip:puerto ip:puerto etc... (esto es para reealizar la conexion a los otros nodos) 
        
             Que voy a hacer?
             Primero me creo a mi mismo
             
         */
        
        
        /*
            si no recibo argumentos 
                //10.0.0.20:15000/

            si recibo argumentos
                //10.0.0.20:+args[x]+/
         */

        if(args.length >= 3){
            String ip = args[0];
            int puerto = 0;

            try {
                puerto = Integer.parseInt(args[1]);
            } catch (Exception e) {
                System.out.println("Error en el puerto");
                System.exit(0);
            }
            String[] nodos = new String[args.length-2] ;
            int j = 0;
            for (int i = 2; i < args.length; i++) {
                System.out.println(args[i]);
                nodos[j] = args[i].toString() ;
                j++;
            }
            
//            System.setProperty("java.rmi.server.hostname","10.0.0.20"); 
            //creamos la interfaz
            
            Almacenamiento almacenamiento = new Almacenamiento(ip , Integer.toString(puerto) ,nodos);

            //Registramos el puerto
            LocateRegistry.createRegistry(puerto);
            //Indicamos como se accede a nuestra clase
            Naming.rebind("//"+ip+":"+puerto+"/almacenamiento", almacenamiento);
        }else{
            System.out.println("Error en los argumentos, debe contener al menos 3 argumentos (ip puerto ip:puerto(nodo))");
        }
         
        
        
        
//        
//        else if (args.length == 2) {
//
//            String ip = args[0];
//            int puerto = 0;
//
//            try {
//                puerto = Integer.parseInt(args[1]);
//            } catch (Exception e) {
//                System.out.println("Error en el puerto");
//                System.exit(0);
//            }
//            System.setProperty("java.rmi.server.hostname",Integer.toString(puerto));            
//            //Indicamos como se accede a nuestra clase
//            try {
//                //Registramos el puerto
//                LocateRegistry.createRegistry(puerto);
//                while (true){
//                    AlmacenamientoInterface servidorPrincipal = (AlmacenamientoInterface) 
//                    Naming.lookup("//10.0.0.20:15000"+"/almacenamiento");
//                     //Indicamos como se accede a nuestra clase
//                    Naming.rebind("//"+ip+":"+puerto+"/almacenamiento", servidorPrincipal);
//                }
//                
//            } catch (Exception e) {
//                System.out.println("3 - instancia principal caída.");
//                System.exit(0);
//            }
//            
//            
//        } else {
//            
//            System.out.println("Error en los argumentos");
//            System.exit(0);
//        }
//        

    }
}
