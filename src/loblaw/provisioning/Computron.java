package loblaw.provisioning;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

/**
 *
 * @author Michael Watkins
 */
public class Computron {

    String[] phoneToStrip = {"-", ".", "(", ")"};//to remove valid non-numbers
    String[] postToStrip = {"-", " "};//to remove valid non-alphanums

    static String[] configurationTags;
    static String[] connectionTypeTags;
    static String[] inBuildingLocationTags;
    static String[] inBuildingLocationHostnames;
    static String[] interactiveTags;
    static String[] manufacturerTags;
    static String[] orientationTags;
    static String[] provinceTags;

    LoblawStore store;
    Technician tech;


// <editor-fold defaultstate="collapsed" desc=" Initializing Tag Strings ">
    //tags 
    String configurationTag = "";//from a dropdown - no checking needed
    String connectionTag = "";//from a dropdown - no checking needed
    String iblocationTag;//from a dropdown - no checking needed
    String ibHostname = "";
    String interactiveTag = "";//from a dropdown - no checking needed
    String manufacturerTag = "";//from a dropdown - no checking needed
    String modelTag = "";//needs checked
    String orientationTag = "";//from a dropdown - no checking needed
    String ipAddressTag = "";//needs checked
    String serialTag = "";//needs checked
    String hostname = "";

// </editor-fold>
// <editor-fold defaultstate="collapsed" desc=" Initializing Status Booleans ">
    //booleans for status:
    boolean imported = false;
    boolean connection = false;
    boolean channel = false;
    boolean rs = false;
    boolean timeZone = false;
    boolean ntp = false;
    boolean update = false;

// </editor-fold>
    public Computron(LoblawStore location, Technician installer,
            String[] configurationT,
            String[] connectionTypeT,
            String[] inBuildingLocationT, String[] inBuildingLocationH,
            String[] interactiveT,
            String[] manufacturerT,
            String[] orientationT,
            String[] provinceT) {

        configurationTags = configurationT;
        connectionTypeTags = connectionTypeT;
        inBuildingLocationTags = inBuildingLocationT;
        inBuildingLocationHostnames = inBuildingLocationH;
        interactiveTags = interactiveT;
        manufacturerTags = manufacturerT;
        orientationTags = orientationT;
        provinceTags = provinceT;
        store = location;
        tech = installer;


    }



    public ArrayList<String> setTags(int configurationSelection, int connectionTypeSelection, int inBuildingLocationSelection, int interactiveSelection, int manufacturerSelection, String model, int orientationSelection, String ip) {

        try {
            configurationTag = configurationTags[configurationSelection];
            connectionTag = connectionTypeTags[connectionTypeSelection];
            iblocationTag = inBuildingLocationTags[inBuildingLocationSelection];
            ibHostname = inBuildingLocationHostnames[inBuildingLocationSelection];
            interactiveTag = interactiveTags[interactiveSelection];//from a dropdown - no checking needed
            manufacturerTag = manufacturerTags[manufacturerSelection];//from a dropdown - no checking needed
            orientationTag = orientationTags[orientationSelection];//from a dropdown - no checking needed
        } catch (Exception e) {
            System.out.println("Runtime Error during device tag setting: " + e.getMessage());
            printTagsDebug();
            System.exit(0);
        }

        

        ipAddressTag = ip;//needs checked
        modelTag = model;//needs checked
        
        ArrayList<String> errors = new ArrayList();
        errors = testIP(ip);
        errors.addAll(testModelTag(model));

        //printTagsDebug();
        
        return errors;
    }

    public void setHostname(String deviceNumber) {
        hostname = getHostnamePrefix() + deviceNumber;
    }

    private String stripPhone(String phone) {
        String temp = phone;

        for (int j = 0; j < phoneToStrip.length; j++) {
            temp = temp.replace(phoneToStrip[j], "");
        }
        return temp;
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

    private ArrayList<String> testIP(String ip) {
        ArrayList<String> errors = new ArrayList();
        //making sure there are exactly 3 periods, and that none of them are in a row:
        boolean prevWasPeriod = false;
        int numPeriods = 0;
        for (int i = 0; i < ip.length(); i++) {//look at IP one char at a time
            if (("" + ip.charAt(i)).compareToIgnoreCase(".") == 0) {//if its a .
                numPeriods++;//increment how many . we have seen
                if (prevWasPeriod) {//if previous char was also a .
                    errors.add("two \".\" in a row at slot " + (i + 1));//error!
                } else {//if previos char was not also a .
                    prevWasPeriod = true;//the current . now is considered the prev
                }
            } else {//if character was not a .
                prevWasPeriod = false;//current lack of . is now prev lack of
            }
        }
        if (numPeriods != 3) {//if we dont have exactly 3 periods, error!
            errors.add("Ip has " + numPeriods + " periods, it needs exactly 3");
        }
        if (errors.size() > 0) {//if ip format is bad
            return errors;//we want to break before attempting to split octets
        }

        //breaking down into octets
        String firstOctet = ip.substring(0, ip.indexOf("."));
        String rest = ip.substring(ip.indexOf(".") + 1);
        boolean goodFirst, goodSecond, goodThird, goodFourth = false;
        try {
            int first = Integer.parseInt(firstOctet);
            if (first < 0 || first > 255) {//unnecessary for now, but might need later
                errors.add(first + " is out of octet range (0-255)");
            } else if (first != 172) {
                errors.add("Loblaw IPs should start with 172, yours starts with:" + first);
            }
        } catch (Exception e) {
            errors.add("first octet \"" + firstOctet + "\" is not a valid int");
        }

        String secondOctet = rest.substring(0, rest.indexOf("."));
        rest = rest.substring(rest.indexOf(".") + 1);
        try {
            int second = Integer.parseInt(secondOctet);
            if (second != 23) {
                errors.add("Second octet of IP should be 23, you entered:" + second);
            } else if (second < 0 || second > 255) {
                errors.add("second octet \"" + secondOctet + "\" is not a valid int");
            }
        } catch (Exception e) {
            errors.add("second Octet \"" + secondOctet + "\" is not a valid int");
        }

        String thirdOctet = rest.substring(0, rest.indexOf("."));
        rest = rest.substring(rest.indexOf(".") + 1);
        try {
            int third = Integer.parseInt(thirdOctet);
            if (third < 0 || third > 255) {
                errors.add("third octet \"" + thirdOctet + "\" is not a valid int");
            }
        } catch (Exception e) {
            errors.add("third Octet \"" + thirdOctet + "\" is not a valid int");
        }

        String fourthOctet = rest;
        try {
            int fourth = Integer.parseInt(fourthOctet);
            if (fourth < 0 || fourth > 255) {
                errors.add("fourth octet \"" + fourthOctet + "\" is not a valid int");
            }
        } catch (Exception e) {
            errors.add("fourth Octet \"" + fourthOctet + "\" is not a valid int");
        }

        return errors;
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

    private ArrayList<String> testModelTag(String model) {
        ArrayList<String> errors = new ArrayList();
        //right now, we except pretty much anything as the model
        return errors;
    }

    public String getCSVString() {
        //oh god the horra
        String tagNames = "properties.system.hostname,resolve,"
                + "properties.system.ipAddress,properties.system.mac,"
                + "proxy,outproxy,properties.system.opstate,device.serial,"
                + "type.type,type.brand,type.series,type.model,"
                + "system.module.version,login,password,location.level1,"
                + "location.level2,location.level3,location.level4,"
                + "location.level5,location.zip,latitude,longitude,ocreceiver,"
                + "ocsender,properties.category,properties.system.sec.hostname,"
                + "properties.system.sec.ipAddress,properties.system.sec.mac,"
                + "Banner,Configuration,Connection Type,In-Building Location,"
                + "Interactive,Language,Line of Business,Location ID,"
                + "Location Type,MP_Manufacturer,MP_Manufacturer_Model,"
                + "NOC Monitor,Orientation,Provisioned_Date";
        String nl = "\n";
        String c = ",";
        //bohica
        String values = hostname + c + "false" + c + ipAddressTag + c
                + c + c + c + "Running" + c + serialTag + c + "Digital Signage Network Player" + c
                + "Stratacache" + c + "Spectra 200" + c + "S-200-W7" + c + c + c + c
                + "North America" + c + "Canada" + c + store.getProvince() + c + store.getCity() + c
                + store.getStreet() + c + store.getPostal() + c + "0.0" + c + "0.0" + c + "true" + c + "false" + c
                + c + c + c + c + store.getBanner() + c + configurationTag + c + connectionTag + c
                + iblocationTag + c + interactiveTag + c + store.getLanguage() + c + store.getLineOfBusiness() + c
                + store.getStoreNumber() + c + store.getLocationType() + c + manufacturerTag + c + modelTag + c
                + "Yes" + c + orientationTag + c + getDateTag();

        return tagNames + nl + values;
    }

    public String getHostnamePrefix() {
        String leadingZeros = "";
        int numZeros = 5 - store.getStoreNumber().length();
        for (int i = 0; i < numZeros; i++) {
            leadingZeros = leadingZeros + "0";
        }
        return leadingZeros + store.getStoreNumber() + "_" + "DS" + "_" + iblocationTag + "_";
    }

    public String getHostname() {
        return hostname;
    }





    public String getTagInfo() {
        String sInfo = "\n";
        sInfo = sInfo + "-Banner: " + store.getBanner() + "\n";
        sInfo = sInfo + "-Configuration: " + configurationTag + "\n";
        sInfo = sInfo + "-Connection Type: " + connectionTag + "\n";
        sInfo = sInfo + "-In Building Location: " + iblocationTag + "\n";
        sInfo = sInfo + "-Interactive: " + interactiveTag + "\n";
        sInfo = sInfo + "-Language: " + store.getLanguage() + "\n";
        sInfo = sInfo + "-Line of Business: " + store.getLineOfBusiness() + "\n";
        sInfo = sInfo + "-Location ID: " + store.getStoreNumber() + "\n";
        sInfo = sInfo + "-Location Type: " + store.getLocationType() + "\n";
        sInfo = sInfo + "-Media Player Manufacturer: " + manufacturerTag + "\n";
        sInfo = sInfo + "-Media Player Model: " + modelTag + "\n";
        sInfo = sInfo + "-NOC Monitor = Yes\n";
        sInfo = sInfo + "-Orientation: " + orientationTag + "\n";
        sInfo = sInfo + "-Phone Number: " + store.getPhone() + "\n";
        sInfo = sInfo + "-Provisioned_Date: " + getDateTag() + "\n";

        return sInfo;
    }

    public String getTicketDescription() {
        String tInfo = tech.getTechInfo()
                + "\n"
                + "Provisioning Task: CHANGE ME \n"
                + "\n"
                + store.getStoreInfo()
                + "\n"
                + "2. Number of players being provisioned?: CHANGE ME\n"
                + "\n"
                + "3. Verify Tags With Installer:\n"
                + getTagInfo()
                + " \n"
                + " 4. Added device to CM?: " + transBool(imported) + " \n"
                + "        -Used correct naming convention for Hostname? ([Location ID]_DS_[In Building Location]_[Device #]): y \n"
                + "        -Player Was Provisioned in Production CM?: " + transBool(imported) + " \n"
                + "        -Site Address Updated?:y \n"
                + "        -Tags in Step 3 added?:y \n"
                + "        -Connection to the device?: " + transBool(connection) + " \n"
                + "        -Device is in a channel?: " + transBool(channel) + " \n"
                + "  \n";
        if (orientationTag.compareToIgnoreCase("portrait") == 0) {
            tInfo = tInfo
                    + "5. (Portrait Devices only) Is the desktop resolution 1080x1920?: CHANGE ME\n"
                    + "        -If desktop resolution is Landscape 1920x1080, applied Desktop Rotation Script? (Located in Loblaw Folder on the Shared Drive): CHANGE ME\n"
                    + "        -Changed 'Desktop Orientation: Portrait Display (Top on Right)' in Player Configuration?: CHANGE ME\n"
                    + "        -Changed 'Display Orientation: Portrait (Top on Right)' in Player Configuration?: CHANGE ME\n"
                    + "  \n";
        }
        tInfo = tInfo
                + "6. Is RS232 working?: " + transBool(rs) + "\n"
                + "\n"
                + "7. Set Timezone on the device?: " + transBool(timeZone) + "\n"
                + "\n"
                + "8. Changed the NTP Server to the Gateway IP?: " + transBool(ntp) + "\n"
                + "\n"
                + "9. Pushed a channel update?: " + transBool(update) + "\n"
                + " \n"
                + " \n"
                + "If this call was for Content Push Only please set player back to provisioned after the push is successful.\n"
                + "\n"
                + "Include any issues as well as site open date in body above the ticket info.";

        return tInfo;
    }

    public String getDateTag() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd");
        Calendar cal = Calendar.getInstance();
        String dateTag = dateFormat.format(cal.getTime());
        return dateTag;
    }

    public LoblawStore getStore(){
        return store;
    }
    public Technician getTech(){
        return tech;
    }


    public void setStatus(boolean[] checks) {
        imported = checks[0];
        connection = checks[1];
        channel = checks[2];
        rs = checks[3];
        timeZone = checks[4];
        ntp = checks[5];
        update = checks[6];
    }

    public String transBool(boolean toTranslate) {
        if (toTranslate) {
            return "Yes";
        } else {
            return "No";
        }
    }

    private void printTagsDebug(){
        try{
        System.out.println("Banner Tag: " + store.getBanner());
        System.out.println("Configuration Tag: " + configurationTag);
        System.out.println("In-Building-Location Tag: " + iblocationTag);
        System.out.println("Hostname Tag: " + ibHostname);
        System.out.println("Interactive Tag: " + interactiveTag);
        System.out.println("Language Tag: " + store.getLanguage());
        System.out.println("Line of Business Tag: " + store.getLineOfBusiness());
        System.out.println("location Type Tag: " + store.getLocationType());
        System.out.println("Manufacturer Tag: " + manufacturerTag);
        System.out.println("Orientation Tag: " + orientationTag);
        System.out.println("IP Address Tag: " + ipAddressTag);
        System.out.println("Store ID Tag: " + store.getStoreNumber());
        System.out.println("Model Tag: " + modelTag);
        } catch(Exception e){
            System.out.println("Error printing tags during debug: " +e.getMessage());
        }
    }
}
