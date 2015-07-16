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
public class LoblawStore {

    String[] phoneToStrip = {"-", ".", "(", ")"};//to remove valid non-numbers
    String[] postToStrip = {"-", " "};//to remove valid non-alphanums

    static String[] languageTags;
    static String[] lineOfBusinessTags;
    static String[] locationTypeTags;
    static String[] bannerTags;

    String numberOfPlayers = "";
    String phone = "";//needs checked
    String street = "";//can be almost anything
    String city = "";//needs checked
    String province = "";//needs checked
    String postal = "";//needs checked

    String banner = "";//from a dropdown - no checking needed
    String language = "";//from a dropdown - no checking needed
    String lineOfBusiness = "";//needs checked
    String storeNumber = "";//needs checked
    String locationType = "";//from a dropdown - no checking needed

    public LoblawStore(String[] langTags, String[] lobTags, String[] loctypeTags, String[] bannerA) {
        languageTags = langTags;
        lineOfBusinessTags = lobTags;
        locationTypeTags = loctypeTags;
        bannerTags = bannerA;
    }

    public ArrayList<String> setInfo(String storeID, String sPhone, String sStreet,
            String sCity, String sProvince, String sPostal, String numPlayers,
            int locationTypeSelection, int lineOfBusinessSelection, int languageSelection, int bannerSelection) {

        ArrayList<String> errors = new ArrayList();

        //handle numPlayers
        try {
            int test = Integer.parseInt(numPlayers);
        } catch (Exception e) {
            numberOfPlayers = numPlayers;
            errors.add("Non-Integer entered for number of players");
        }
        /////////end numplayers
        /////////end numplayers

        //handle phone
        String tempPhone = stripPhone(sPhone);
        boolean goodPhone = testPhone(tempPhone);
        if (!goodPhone) {
            phone = sPhone;
            errors.add("Phone number is not valid");
        } else {
            phone = formatPhone(tempPhone);
        }
        ////////end of phone

        //handle city
        boolean goodCity = testName(sCity);
        if (!goodCity) {
            errors.add("Non-alpha, non-space character detected in City.");
        }
        city = sCity;
        ////////end of city

        province = sProvince;//province comes from dropdown box, so I
        //dont have to test, amiriiiiiiight?

        street = sStreet;//we accept all non-evil values
        ////////handle postal
        String tempPost = stripPostal(sPostal);
        String postError = testPostal(tempPost);
        if (postError.compareToIgnoreCase("~") != 0) {

            postal = sPostal;
            errors.add(postError);
        } else {
            postal = formatPostal(tempPost);
        }
        ////////////////end of postal
        locationType = locationTypeTags[locationTypeSelection];//from a dropdown - no checking needed
        lineOfBusiness = lineOfBusinessTags[lineOfBusinessSelection];//from a dropdown - no checking needed
        language = languageTags[languageSelection];//from a dropdown - no checking needed
        banner = bannerTags[bannerSelection];

        storeNumber = storeID;//needs checked
        errors.addAll(testStoreIDTag(storeNumber));

        return errors;
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

    private String stripPostal(String spostal) {
        String temp = spostal;

        for (int j = 0; j < postToStrip.length; j++) {
            temp = temp.replace(postToStrip[j], "");
        }
        return temp;
    }

    private String testPostal(String tempPost) {
        if (tempPost.length() != 6) {
            return "Postal wrong amount of digits";
        }
        for (int i = 0; i < tempPost.length(); i++) {
            int asciiValue = (int) tempPost.charAt(i);
            if (i % 2 == 0) {//every other digit starting with first should be a letter
                if (asciiValue < 65 || asciiValue > 122 || (asciiValue > 90 && asciiValue < 97)) {//ascii values for chars
                    System.out.println(">9 <65");
                    return "Postal digit " + (i + 1) + " (" + tempPost.charAt(i) + ") " + " is not a valid letter";
                }

            } else {//every other character start with second should be number
                if (!(asciiValue >= 48 && asciiValue <= 57)) {//we want to permit 0-9 numbers
                    return "Postal digit " + (i + 1) + " (" + tempPost.charAt(i) + ") " + " is not a valid number";
                }
            }
        }

        return "~";
    }

    private String formatPostal(String tempPost) {
        tempPost = tempPost.substring(0, 3) + " " + tempPost.substring(3);
        System.out.println(tempPost);
        return tempPost;
    }

    private ArrayList<String> testStoreIDTag(String storeIDTag) {
        ArrayList<String> errors = new ArrayList();

        for (int i = 0; i < storeIDTag.length(); i++) {
            int asciiValue = (int) storeIDTag.charAt(i);
            //is letter                                   is number                                 is -
            if (!((asciiValue >= 65 && asciiValue <= 122) || (asciiValue >= 48 && asciiValue <= 57) || asciiValue == 45 || asciiValue == 32)) {
                errors.add("Store ID " + (i + 1) + " (" + storeIDTag.charAt(i) + ") " + " is not a valid letter, number,-, or space");
            }

        }

        return errors;
    }

    public String getStoreInfo() {
        String sInfo = "";
        sInfo = "1. What is the Site Address?:\n";
        sInfo = sInfo + street + "\n";
        sInfo = sInfo + city + ", " + province + "\n";
        sInfo = sInfo + postal;
        return sInfo;
    }

    public String getProvince(){
        return province;
    }
    public String getBanner() {
        return banner;
    }

    public String getLineOfBusiness() {
        return lineOfBusiness;
    }
    public String getCity(){
        return city;
    }
    public String getPostal(){
        return postal;
    }
    public String getStoreNumber() {
        return storeNumber;
    }
    public String getStreet(){
        return street;
    }
    public String getLanguage(){
        return language;
    }
    public String getLocationType(){
        return locationType;
    }
    public String getPhone(){
        return phone;
    }
}
