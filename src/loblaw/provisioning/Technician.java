/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package loblaw.provisioning;

import java.util.ArrayList;

/**
 *
 * @author MWatkins
 */
public class Technician {

    String[] phoneToStrip = {"-", ".", "(", ")"};//to remove valid non-numbers

    String name = "";//needs checked
    String techPhone = "";//needs checked
    String company = "";//needs checked
    String purchaseOrder = "";//can be anything

    public Technician() {

    }

    public ArrayList<String> setInfo(String tname, String tphone, String tcompany, String tpurchaseOrder) {
        ArrayList<String> errors = new ArrayList();
        ////////////////handle name
        boolean goodName = testName(tname);
        if (!goodName) {
            errors.add("Non-alpha, non-space character detected in name.");
        }
        name = tname;
        /////////////////end of name
        ////////handle phone
        String tempPhone = stripPhone(tphone);
        boolean goodPhone = testPhone(tempPhone);
        if (!goodPhone) {
            techPhone = tphone;
            errors.add("Phone number is not valid");
        } else {
            techPhone = formatPhone(tempPhone);
        }
        ////////////////end of phone
        ///////////////handle company
        boolean goodCompany = testName(tcompany);
        if (!goodCompany) {
            errors.add("Non-alpha, non-space character detected in Company.");
        }
        company = tcompany;
        //////////////end of company

        purchaseOrder = tpurchaseOrder;//we accept all non-evil values for purchase order.

        return errors;
    }

    private boolean testName(String name) {
        for (int i = 0; i < name.length(); i++) {
            int asciiValue = (int) name.charAt(i);
            if (asciiValue != 32) {//we want to permit spaces
                if (asciiValue < 65) {//lower bound for capital alphabet
                    return false;
                }
                if (asciiValue > 122) {//upper bound for lowercase
                    return false;
                }
                if (asciiValue > 90 && asciiValue < 97) {//gap between lowercase and uppercase
                    return false;
                }
            }
        }
        return true;
    }

    private String stripPhone(String phone) {
        String temp = phone;

        for (int j = 0; j < phoneToStrip.length; j++) {
            temp = temp.replace(phoneToStrip[j], "");
        }
        return temp;
    }

    private boolean testPhone(String tempPhone) {
        if (tempPhone.length() != 10) {
            System.out.println("Phone Number wrong amount of digits");
            return false;
        }
        String t1 = tempPhone.substring(0, 5);
        String t2 = tempPhone.substring(5);

        try {
            int test = Integer.parseInt(t1);
            if (test < 0) {
                return false;
            }
            test = Integer.parseInt(t2);
            if (test < 0) {
                return false;
            }

        } catch (NumberFormatException e) {
            System.out.println(e.getMessage());
            return false;
        }
        return true;
    }

    private String formatPhone(String phone) {
        String temp = phone;
        temp = temp.substring(0, 3) + "-" + temp.substring(3, 6) + "-" + temp.substring(6);
        return temp;
    }

    public String getTechInfo() {
        String tname = "Technician Name (first name will do): " + name + "\n";
        String tnumbah = "Technician Mobile Number: " + techPhone + "\n";
        String tCompany = "Technician Company: " + company + "\n";
        String poNumber = "Purchase Order #: " + purchaseOrder + "\n";

        return tname + tnumbah + tCompany + poNumber;
    }
}
