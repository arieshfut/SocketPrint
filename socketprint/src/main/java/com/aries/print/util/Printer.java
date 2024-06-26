package com.aries.print.util;

public class Printer {

    private String address, name, serial, location;
    private int K1, K2, cyan, magenta, yellow, black;
    private boolean offline = false, colour = false, kprinter = false, labelprinter = false;

    public Printer(String address) {
        this.address = address;
        name = "";
        serial = "";
        location = "";
        K1 = 0;
        K2 = 0;
        cyan = 0;
        magenta = 0;
        yellow = 0;
        black = 0;
    }

    public String getIP() {
        return address;
    }

    public void setName(String nam) {
        name = nam;
    }

    public String getName() {
        return name;
    }

    public void setSerial(String ser) {
        serial = ser;
    }

    public String getSerial() {
        return serial;
    }

    public void setBlack(int ton) {
        black = ton;
    }

    public int getBlack() {
        return black;
    }

    public void setLocation(String loc) {
        location = loc;
    }

    public String getLocation() {
        return location;
    }

    public void setK1(int ton) {
        K1 = ton;
    }

    public int getK1() {
        return K1;
    }

    public void setK2(int ton) {
        K2 = ton;
    }

    public int getK2() {
        return K2;
    }

    public void setCyan(int ton) {
        cyan = ton;
    }

    public int getCyan() {
        return cyan;
    }

    public void setMagenta(int ton) {
        magenta = ton;
    }

    public int getMagenta() {
        return magenta;
    }

    public void setYellow(int ton) {
        yellow = ton;
    }

    public int getYellow() {
        return yellow;
    }

    public void setOffline() {
        offline = true;
    }

    public boolean isOffline() {
        return offline;
    }

    public void setColour() {
        colour = true;
    }

    public boolean isColour() {
        return colour;
    }

    public boolean isNotColour() {
        return !colour;
    }
    public void setkprinter() {
        kprinter = true;
    }

    public boolean iskprinter() {
        return kprinter;
    }

    public void setLabelPrinter() {
        labelprinter = true;
    }

    public boolean isLabelPrinter() {
        return labelprinter;
    }

    @Override
    public String toString() {
        String out = "";
        if(this.isOffline()) {
            out = String.format("%-30s %-14s", "Printer Offline", this.getIP());
        }
        else if(address.equals("10.214.192.215") || address.equals("10.214.192.250")) {
            out = String.format("%-30s %-16s %-30s %-20s",this.getLocation(), this.getIP(), this.getName(), this.getSerial());
        }
        else if(this.iskprinter()) {
            out = String.format("%-30s %-16s %-30s %-20s %3d%% %3d%% %3d%% %3d%% %4d%% %3d%%", this.getLocation(), this.getIP(), this.getName(), this.getSerial(), this.getBlack(), this.getYellow(), this.getMagenta(), this.getCyan(), this.getK1(), this.getK2());
        }
        else if(this.isColour())
        {
            out = String.format("%-30s %-16s %-30s %-20s %3d%% %3d%% %3d%% %3d%%", this.getLocation(), this.getIP(), this.getName(), this.getSerial(), this.getBlack(), this.getYellow(), this.getMagenta(), this.getCyan());
        }
        else {
            out = String.format("%-30s %-16s %-30s %-20s %3d%%", this.getLocation(), this.getIP(), this.getName(), this.getSerial(), this.getBlack());
        }
        return out;
    }

}
