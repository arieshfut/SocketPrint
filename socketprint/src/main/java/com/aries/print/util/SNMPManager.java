package com.aries.print.util;

import android.util.Log;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;

import org.snmp4j.*;
import org.snmp4j.event.*;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.*;
import org.snmp4j.transport.DefaultUdpTransportMapping;

public class SNMPManager implements Runnable {
    private final static String TAG = "SNMPManager";

    private Snmp snmp;
    private ArrayList<String> addressList;
    private ArrayList<Printer> printers;


    /**
     * Constructor
     */
    public SNMPManager() {
        addressList = new ArrayList<>();
        printers = new ArrayList<>();
        snmp = null;
    }

    public void run() {
        begin();
    }

    private void begin() {
        /* Port 161 is used for Read and Other operations
         * Port 162 is used for the trap generation */
        Log.i(TAG, "Gaethring IP Addresses...");
        inputAddresses();
        Log.i(TAG, printers.size()+" printers found.");
        start();
        Log.i(TAG, "Sending SNMP Messages Done");
        String progressBar = "|                                   |";
        for(int i = 0;i < printers.size();i++) {
            getAsString(printers.get(i));
            progressBar = progressBar.substring(0, i+1) +'=' + progressBar.substring(i+2);
        }
        try {
            snmp.close();
        } catch (IOException e) {
            // Error
            Log.i(TAG, "Error trying to close() snmp");
        }
        //Sorts ascending order by black ink levels
        //"%-30s %-16s %-30s %-20s %2d%% %2d%% %2d%% %2d%% %2d%% %2d%%
        printers.sort(Comparator.comparing(Printer::isOffline)
                .thenComparing(Printer::isLabelPrinter)
                .thenComparing(Printer::iskprinter)
                .thenComparing(Printer::isNotColour)
                .thenComparingInt(Printer::getBlack));
        Log.i(TAG, "- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - ");
        Log.i(TAG, String.format("%-30s %-16s %-30s %-22s %3s","Location","IP","Model","Serial","B    Y    M    C      K1     K2"));
        Log.i(TAG, "- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - ");
        //Make a print string instead?
        //print
        for(int i = 0; i < printers.size(); i ++) {
            Log.i(TAG, printers.get(i).toString());
        }
        // System.exit(0);
    }

    private void inputAddresses(){
        //input addresses from text file
        String[] ipList = {
                "172.18.2.51",
                "192.168.10.130",
                "172.18.67.247",
                "172.18.1.10",
                "172.18.3.92",
                "172.18.4.108",
                "172.18.4.241",
                "172.18.33.156",
                "172.18.4.168"
        };

        for (String ipStr : ipList) {
            Printer newPrinter = new Printer(ipStr);
            addressList.add(ipStr);
            printers.add(newPrinter);
        }
    }

    /**
     * Start the Snmp session. If you forget the listen() method you will not
     * get any answers because the communication is asynchronous
     * and the listen() method listens for answers.
     */
    private void start() {
        TransportMapping<?> transport;
        try {
            transport = new DefaultUdpTransportMapping();
            snmp = new Snmp(transport);
            snmp.listen();
        } catch (IOException e) {
            // Error
            Log.e(TAG, "Error trying to listen()");
            // System.exit(1);
        }
    }

    /**
     * Method which takes a single OID and returns the response from the agent as a String.
     * @param obj printer
     */
    private void getAsString(Printer obj) {
        //These care custom IP settings for the printers on my network
        ResponseEvent<?> event;
        event = getV2C(new OID[] {new OID(".1.3.6.1.2.1.1.5.0"),new OID(".1.3.6.1.2.1.43.11.1.1.9.1.1"),new OID(".1.3.6.1.2.1.43.11.1.1.8.1.1"),
                new OID(".1.3.6.1.2.1.43.11.1.1.9.1.2"),new OID(".1.3.6.1.2.1.43.11.1.1.8.1.2"),new OID(".1.3.6.1.2.1.43.11.1.1.9.1.3"),new OID(".1.3.6.1.2.1.43.11.1.1.8.1.3"),
                new OID(".1.3.6.1.2.1.43.11.1.1.9.1.4"),new OID(".1.3.6.1.2.1.43.11.1.1.8.1.4"),new OID(".1.3.6.1.2.1.43.11.1.1.9.1.30"),new OID(".1.3.6.1.2.1.43.11.1.1.8.1.30"),
                new OID(".1.3.6.1.2.1.43.11.1.1.9.1.31"),new OID(".1.3.6.1.2.1.43.11.1.1.8.1.31"),new OID(".1.3.6.1.2.1.1.6.0"),new OID(".1.3.6.1.2.1.43.5.1.1.17.1"), new OID(".1.3.6.1.2.1.43.12.1.1.4.1.2")}, obj);
        // 0 name, 1 black curr, 2 black max, 3 yellow curr, 4 yellow max, 5 magenta curr, 6 magenta max, 7 cyan curr, 8 cyan max, 9 k1 curr, 10 k1 max, 11 k2 curr , 12 k2 max, 13 location, 14 serial, 15 "yellow"
        PDU response = event.getResponse();
        if(response != null) {
            obj.setName(response.get(0).getVariable().toString());
            if(!response.get(1).getVariable().isException() && !response.get(2).getVariable().isException()){
                obj.setBlack(Math.round(Float.parseFloat(response.get(1).getVariable().toString())/Float.parseFloat(response.get(2).getVariable().toString())*100));
            }

            if(response.get(15).getVariable().toString().equals("yellow")) {
                //if colour printer
                obj.setColour();
                obj.setYellow(Math.round(Float.parseFloat(response.get(3).getVariable().toString())/Float.parseFloat(response.get(4).getVariable().toString())*100));
                obj.setMagenta(Math.round(Float.parseFloat(response.get(5).getVariable().toString())/Float.parseFloat(response.get(6).getVariable().toString())*100));
                obj.setCyan(Math.round(Float.parseFloat(response.get(7).getVariable().toString())/Float.parseFloat(response.get(8).getVariable().toString())*100));
            }

            if(!response.get(9).getVariable().isException() && !response.get(10).getVariable().isException()){
                float tonerK1 = Float.parseFloat(response.get(9).getVariable().toString())/Float.parseFloat(response.get(10).getVariable().toString());
                obj.setK1(Math.round(tonerK1*100));
                obj.setkprinter();
            }

            if(!response.get(11).getVariable().isException() && !response.get(12).getVariable().isException()){
                float tonerK2 = Float.parseFloat(response.get(11).getVariable().toString())/Float.parseFloat(response.get(12).getVariable().toString());
                obj.setK2(Math.round(tonerK2*100));
            }

            String serial = response.get(14).getVariable().toString();
            obj.setSerial(serial.substring(serial.length() - 6));
            obj.setLocation(response.get(13).getVariable().toString());
        } else {
            //can't contact printer
            obj.setOffline();
        }
    }

    /**
     * This method is capable of handling multiple OIDs
     * @param oids
     */
    private ResponseEvent<?> getV2C(OID oids[], Printer obj){
        PDU pdu = new PDU();
        for (OID oid : oids) {
            pdu.add(new VariableBinding(oid));
        }
        pdu.setType(PDU.GET);
        ResponseEvent<?> event = null;
        try {
            event = snmp.send(pdu, getTarget(obj, 2), null);
        } catch (IOException e) {
            // Error on V2
        }
        return event;
    }

    /**
     * This method returns a Target, which contains information about
     * where the data should be fetched and how.
     */
    private CommunityTarget<Address> getTarget(Printer obj, int ver) {
        Address targetAddress = GenericAddress.parse("udp:"+obj.getIP()+"/161");
        CommunityTarget<Address> target = new CommunityTarget<Address>();
        target.setCommunity(new OctetString("public"));
        target.setAddress(targetAddress);
        target.setRetries(2);
        target.setTimeout(1500);
        //SNMP Version 1, 2c, or 3
        if(ver==2) {
            target.setVersion(SnmpConstants.version2c);
        }
        else if(ver==1) {
            target.setVersion(SnmpConstants.version1);
        }
        return target;
    }
}
