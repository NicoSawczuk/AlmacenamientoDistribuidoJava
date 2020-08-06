/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package modelos;

import java.rmi.server.UnicastRemoteObject;
import interfaces.AlmacenamientoInterface;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.nio.charset.Charset;
import java.rmi.Naming;
import java.rmi.NotBoundException;

import java.rmi.RemoteException;

import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMIServerSocketFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author kachu
 */
public class Almacenamiento extends UnicastRemoteObject implements AlmacenamientoInterface {

    // private ArrayList<ArrayList> almacenamiento; 
    //Hashtable<String, String> almacenamiento = new Hashtable<String, String>();
    ConcurrentHashMap<String, String> almacenamiento = new ConcurrentHashMap<>();
    private String nodo;
    private ConcurrentHashMap<String, BigInteger> nodos = new ConcurrentHashMap<>();
    int[] nodosHash = new int[256];

    public Almacenamiento() throws RemoteException {
    }

    public Almacenamiento(String ip, String puerto, String[] nodos) throws RemoteException, NoSuchAlgorithmException {
        this.nodo = ip + ":" + puerto;
        System.out.println("yo soy " + this.nodo);
        this.convertirHash(nodos);

    }

    public Almacenamiento(int port) throws RemoteException {
        super(port);
    }

    public Almacenamiento(int port, RMIClientSocketFactory csf, RMIServerSocketFactory ssf) throws RemoteException {
        super(port, csf, ssf);
    }

    public int validarClave(String clave) {
        //Controlamos que sea alfanumerico y pueda contener _
        if (!(clave.matches("[a-zA-Z0-9_]"))) {
            if ((clave.length() >= 1) && (clave.length() <= 50)) {
                return 1;
            } else {
                return 0;
            }
        } else {
            return 0;
        }
    }

    public int validarValor(String valor) {
        //Casteamos el string a un array de bytes
        byte[] b = valor.getBytes();
        //Controlamos que ese array pese 1.5MB
        if (b.length <= 1536) {
            return 1;
        } else {
            return 0;
        }
    }

    private void convertirHash(String[] nodos) throws NoSuchAlgorithmException {
        BigInteger posicion = new BigInteger("0");
        for (int i = 0; i < nodos.length; i++) {
            posicion = this.obtenerPosicion(nodos[i]);
            this.nodos.put(nodos[i], posicion);
            System.out.println("nodo " + nodos[i] + "  posicion: " + posicion);
        }
    }

    private BigInteger obtenerPosicion(String clave) throws NoSuchAlgorithmException {
        BigInteger posicion = new BigInteger("0");
        MessageDigest md;
        md = MessageDigest.getInstance("SHA-512");
        byte[] arreglo = md.digest(clave.getBytes());
        posicion = new BigInteger(1, arreglo);
        return posicion;
    }

    private String obtenerResponsable(BigInteger posicion) {
        ArrayList<BigInteger> listado = new ArrayList<>(this.nodos.values());
        BigInteger responsable = null;
        String nodoResponsable = null;
        Collections.sort(listado);
        for (int i = 0; i < listado.size(); i++) {
            if (listado.get(i).compareTo(posicion) >= 0) {
                responsable = listado.get(i);
                break;
            }

        }
        if (responsable == null) {
            responsable = listado.get(0);
        }

        Set<Map.Entry<String, BigInteger>> valores = this.nodos.entrySet();
        for (Map.Entry<String, BigInteger> v : valores) {
            if (v.getValue().equals(responsable)) {
                nodoResponsable = v.getKey();
            }

        }
        return nodoResponsable;
    }

    @Override
    //0 para modificar, 1 para guardar uno nuevo y 2 error
    public String guardar(String clave, String valor) throws RemoteException {
        String retorno;

        try {
            String responsable = this.obtenerResponsable(this.obtenerPosicion(clave));
            if (responsable.equals(this.nodo)) {
                if ((validarClave(clave) == 1) && (validarValor(valor) == 1)) {
                    try {
                        if (almacenamiento.containsKey(clave)) {
                            almacenamiento.replace(clave, valor);
                            System.out.println("despues de guar" + this.almacenamiento.entrySet());
                            retorno = "1";
                        } else {
                            almacenamiento.put(clave, valor);
                            System.out.println("despues de guar" + this.almacenamiento.entrySet());
                            retorno = "0";
                        }
                    } catch (Exception e) {
                        retorno = "2";
                    }
                } else {
                    retorno = "0 - error en formato.";
                }
                return retorno;
            } else {
                //Estamos accediendo remotamente a almacenamiento que esta del lado del servidor
                AlmacenamientoInterface servidor = (AlmacenamientoInterface) Naming.lookup("//" + responsable + "/almacenamiento");
                servidor.guardar(clave, valor);
            }

        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(Almacenamiento.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NotBoundException ex) {
            Logger.getLogger(Almacenamiento.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MalformedURLException ex) {
            Logger.getLogger(Almacenamiento.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;

    }

    @Override
    //retornamos el valor de la clave si existe else error(2)
    public String obtener(String clave) throws RemoteException {
        try {
            String responsable = this.obtenerResponsable(this.obtenerPosicion(clave));
            if (responsable.equals(this.nodo)) {
                try {
                    if (almacenamiento.containsKey(clave)) {
                        return (String) almacenamiento.get(clave);
                    } else {
                        return "1 - clave no existe.";
                    }
                } catch (Exception e) {
                    return e.getMessage();
                }
            } else {
                //Estamos accediendo remotamente a almacenamiento que esta del lado del servidor
                AlmacenamientoInterface servidor = (AlmacenamientoInterface) Naming.lookup("//" + responsable + "/almacenamiento");
                servidor.obtener(clave);
            }

        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(Almacenamiento.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NotBoundException ex) {
            Logger.getLogger(Almacenamiento.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MalformedURLException ex) {
            Logger.getLogger(Almacenamiento.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "fin obt";

    }

    @Override
    //si elimina retorna valor else retorna error(2)
    public String eliminar(String clave) throws RemoteException {
        try {
            String aux;
            String responsable = this.obtenerResponsable(this.obtenerPosicion(clave));
            if (responsable.equals(this.nodo)) {
                try {
                    if (almacenamiento.containsKey(clave)) {
                        aux = (String) almacenamiento.get(clave);
                        almacenamiento.remove(clave);
                        System.out.println("despues de elm" + this.almacenamiento.entrySet());
                        return aux;
                    } else {
                        return "2 - clave no existe";
                    }

                } catch (Exception e) {
                    return e.getMessage();
                }

            } else {
                AlmacenamientoInterface servidor = (AlmacenamientoInterface) Naming.lookup("//" + responsable + "/almacenamiento");
                servidor.eliminar(clave);
            }

        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(Almacenamiento.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NotBoundException ex) {
            Logger.getLogger(Almacenamiento.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MalformedURLException ex) {
            Logger.getLogger(Almacenamiento.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "fin elm";
    }

    @Override
    public void agregarNodo(String ip, int puerto) throws RemoteException {

    }

    @Override
    public void eliminarNodo(String ip, int puerto) throws RemoteException {
        String idNodo = ip + ":" + puerto;
        if (this.nodo.equals(idNodo)) {
            this.nodos.remove(idNodo);
            Set<Map.Entry<String, BigInteger>> listadoNodos = this.nodos.entrySet();
            for (Map.Entry<String, BigInteger> n : listadoNodos) {
                try {
                    AlmacenamientoInterface servidor = (AlmacenamientoInterface) Naming.lookup("//" + n.getKey() + "/almacenamiento");
                } catch (NotBoundException ex) {
                    Logger.getLogger(Almacenamiento.class.getName()).log(Level.SEVERE, null, ex);
                } catch (MalformedURLException ex) {
                    Logger.getLogger(Almacenamiento.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            Set<Map.Entry<String, String>> datos = this.almacenamiento.entrySet();
            for (Map.Entry<String, String> d : datos) {
                this.guardar(d.getKey(), d.getValue());
            }
        } else {
            this.nodos.remove(idNodo);
        }
    }

}
