package loblaw.provisioning;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

// <editor-fold defaultstate="collapsed" desc=" Author Notes ">
/**
 * @author Michael Watkins
 */
// </editor-fold>
public class LoblawProvisioning {

// <editor-fold defaultstate="collapsed" desc=" Globals! ">
    //for input checking:
    static String rootPath = "\\\\10.0.104.25\\SupportShare\\zMichael_Watkins\\LoblawProvisioning\\Resources\\";
    //for failed or succeeded ticket:
    static boolean[] checks = {false, false, false, false, false, false, false};
    // 0 - imported to CM
    // 1 - good connection in CM
    // 2 - device got a channel
    // 3 - RS232 is working (this is optional)
    // 4 - Timezone was set
    // 5 - ran NTP software upgrade
    // 6 - pushed a channel update

    //for input checking:
    static String[] bannedChars = {"\"", "\\", ";", ":", "\'", "/", "`", ","};

    //combobox options:
    static String[] bannerOptions;
    static String[] bannerTags;
    static String[] configurationOptions;
    static String[] configurationTags;
    static String[] connectionTypeOptions;
    static String[] connectionTypeTags;
    static String[] inBuildingLocationOptions;
    static String[] inBuildingLocationTags;
    static String[] inBuildingLocationHostnames;
    static String[] interactiveOptions;
    static String[] interactiveTags;
    static String[] languageOptions;
    static String[] languageTags;
    static String[] lineOfBusinessOptions;
    static String[] lineOfBusinessTags;
    static String[] locationTypeOptions;
    static String[] locationTypeTags;
    static String[] manufacturerOptions;
    static String[] manufacturerTags;
    static String[] orientationOptions;
    static String[] orientationTags;
    static String[] provinceOptions;
    static String[] provinceTags;

    //checkboxes
    static JCheckBox importBox;
    static JCheckBox connectionBox;
    static JCheckBox channelBox;
    static JCheckBox rsBox;
    static JCheckBox timeZoneBox;
    static JCheckBox ntpBox;
    static JCheckBox updateBox;

    ///default choices for tag dropdowns:
    static int tagBanner = 0;
    static int configurationSelection = 0;
    static int connectionTypeSelection = 0;
    static int inBuildingLocationSelection = 0;
    static int interactiveSelection = 0;
    static int tagLanguage = 0;
    static int tagLineOfBusiness = 0;
    static int tagLocationType = 0;
    static int manufacturerSelection = 0;
    static int orientationSelection = 0;

    //tech infos
    static String tName = "";
    static String tPhone = "";
    static String tCompany = "BFG";
    static String poNum = "N/A";

    //store infos
    static String tagNumPlayers = "";
    static String tagStorePhone = "";
    static String tagStoreStreet = "";
    static String tagStoreCity = "";
    static String sProvince = "";
    static String tagStorePostal = "";
    static int selectedProvince = 0;

    //tag infos
    static String tagStoreID = "";
    static String tagMPModel = "TinyPC";
    static String tagIPAddress = "172.23.";

    static JTextField poNumBox;//this one is very special.

    //global containers:
    static JPanel canvas;
    static JFrame frame;

    //configuration settings:
    static int panelWidth = 500;
    static int panelHeight = 500;
    static final String loblawTicketTemplateCode = "7501";

    static ArrayList<Computron> players;
    static Computron device;//device to create
    static Technician tech;
    static LoblawStore store;

    static String currentCanvas = "tech";//state manager

    static ArrayList<JComponent> entries;
    static int bgRedstat = 238;
    static int bgGreenstat = 238;
    static int bgBluestat = 238;

// </editor-fold>
// <editor-fold defaultstate="collapsed" desc="Canvas State Handlers and Helpers ">
    public static void main(String[] args) {
        populateArrays();

        init();
    }

    public static void init() {
        System.out.println("Initializing Frame");
        frame = new JFrame("Loblaw Provisioning Tool");
        frame.setIconImage(Toolkit.getDefaultToolkit().getImage("your_image.gif"));
        players = new ArrayList();
        //need to populate arrays in store
        store = new LoblawStore(languageTags, lineOfBusinessTags, locationTypeTags, bannerTags);
        tech = new Technician();
        buildCanvas();
    }

    private static void buildCanvas() {
        killCanvas();
        canvas = new JPanel();
        entries = new ArrayList();
        makePanel(currentCanvas);
        frame.setLayout(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(panelWidth, panelHeight);
        frame.setResizable(false);//lock window size
        frame.setContentPane(canvas);
        frame.setVisible(true);
        canvas.requestFocusInWindow();
    }

    private static void makePanel(String key) {
        if (key.compareToIgnoreCase("tech") == 0) {
            makeTechPanel();
        } else if (key.compareToIgnoreCase("store") == 0) {
            makeStorePanel();
        } else if (key.compareToIgnoreCase("multitags") == 0) {
            makeMultitagPanel();
        } else if (key.compareToIgnoreCase("host") == 0) {
            makeHostnamePanel();
        } else if (key.compareToIgnoreCase("conf") == 0) {
            makeConfirmationPanel();
        } else if (key.compareToIgnoreCase("check") == 0) {
            makeCheckboxPanel();
        }
    }

    private static void killCanvas() {
        if (canvas != null) {
            canvas.removeAll();
            frame.dispose();
        }
    }

    private static void setWindowSize(int rowSize, int colSize) {
        panelWidth = rowSize;
        panelHeight = colSize;
    }

    private static void displayErrors(ArrayList<String> errors) {
        //to console
        String fullErrors = "";
        for (int i = 0; i < errors.size(); i++) {
            fullErrors = fullErrors + errors.get(i) + "\n";
        }
        //to a joptionpane
        //JOptionPane.showMessageDialog(null, "message", "title", JOptionPane.ERROR_MESSAGE);
        JOptionPane.showMessageDialog(null, fullErrors, "Input Error", JOptionPane.ERROR_MESSAGE);
    }

// </editor-fold>
// <editor-fold defaultstate="collapsed" desc=" Panel Builders ">
    private static void makeTechPanel() {
        panelWidth = 500;
        panelHeight = 150;
        canvas.setLayout(new GridLayout(5, 1));
        JPanel nameLine = new JPanel(new GridLayout(1, 2));
        JPanel techPhoneLine = new JPanel(new GridLayout(1, 2));
        JPanel techCompanyLine = new JPanel(new GridLayout(1, 2));
        JPanel purchaseOrderLine = new JPanel(new GridLayout(1, 2));
        JPanel buttonLine = new JPanel(new GridLayout(1, 2));

        JLabel namelbl = new JLabel("Tech Name:");
        JLabel phonelbl = new JLabel("Tech Phone:");
        JLabel techCompanylbl = new JLabel("Tech Company:");
        JLabel purhcaseOrderlbl = new JLabel("Tech Purchase Order #:");

        JTextField nameBox = new JTextField(tName);
        nameBox.setColumns(10);
        entries.add(nameBox);

        JTextField phoneBox = new JTextField(tPhone);
        phoneBox.setColumns(10);
        entries.add(phoneBox);

        JTextField companyBox = new JTextField(tCompany);
        // companyBox.setColumns(10);
        //companyBox.setForeground(defaultText);
        entries.add(companyBox);

        poNumBox = new JTextField(poNum);
        poNumBox.setColumns(10);
        //entries.add(poNumBox); //we don't actually validate this input
        //so there is no need to test it in entries
        //just use the poNum variable.

        JButton clearButton = new JButton("Reset");

        clearButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                resetButtonPressed();
            }

        });

        JButton continueButton = new JButton("Continue");
        continueButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                continueButtonPressed();
            }

        });
        //pack that shit up
        nameLine.add(namelbl);
        nameLine.add(nameBox);

        techPhoneLine.add(phonelbl);
        techPhoneLine.add(phoneBox);

        techCompanyLine.add(techCompanylbl);
        techCompanyLine.add(companyBox);

        purchaseOrderLine.add(purhcaseOrderlbl);
        purchaseOrderLine.add(poNumBox);

        buttonLine.add(clearButton);
        buttonLine.add(continueButton);
        //add it to our canvas:
        canvas.add(nameLine);
        canvas.add(techPhoneLine);
        canvas.add(techCompanyLine);
        canvas.add(purchaseOrderLine);
        canvas.add(buttonLine);

    }

    private static void makeStorePanel() {
        panelWidth = 830;
        panelHeight = 200;
        canvas.setLayout(new BorderLayout());

        JPanel titlePanel = new JPanel(new GridLayout(1, 1));
        JLabel titleLine = new JLabel("Store Information", JLabel.CENTER);
        titlePanel.add(titleLine);
        canvas.add(titlePanel, BorderLayout.NORTH);

        JPanel storeInfoLeft = new JPanel(new GridLayout(6, 2)); //left side of store info page        
        //building left lines:
        JPanel idLine = getTextBoxPanel("Store ID #:", tagStoreID);                              //entry 0
        JPanel storePhoneLine = getTextBoxPanel("Phone #:", tagStorePhone);                     //entry 1
        JPanel storeStreetLine = getTextBoxPanel("Street Address:", tagStoreStreet);            //entry 2
        JPanel storeCityLine = getTextBoxPanel("City:", tagStoreCity);                          //entry 3
        JPanel storeProvinceLine = getComboBoxPanel("Province:", provinceOptions, selectedProvince); //entry 4
        JPanel storePostalLine = getTextBoxPanel("Postal Code:", tagStorePostal);               //entry 5
        //adding lines to left side:
        storeInfoLeft.add(idLine);
        storeInfoLeft.add(storePhoneLine);
        storeInfoLeft.add(storeStreetLine);
        storeInfoLeft.add(storeCityLine);
        storeInfoLeft.add(storeProvinceLine);
        storeInfoLeft.add(storePostalLine);
        //adding left side to canvas:
        canvas.add(storeInfoLeft, BorderLayout.WEST);

        JPanel storeInfoRight = new JPanel(new GridLayout(5, 2));
        //building right lines:
        JPanel numPlayersLine = getTextBoxPanel("Number of Players:", tagNumPlayers);                       //entry 6
        JPanel locTypeLine = getComboBoxPanel("Location Type:", locationTypeOptions, tagLocationType);   //entry 7
        JPanel lOBLine = getComboBoxPanel("Line of Business:", lineOfBusinessOptions, tagLineOfBusiness);//entry 8
        JPanel languageLine = getComboBoxPanel("Language:", languageOptions, tagLanguage);               //entry 9
        JPanel BannerLine = getComboBoxPanel("Banner:", bannerOptions, tagBanner);                       //entry 10
        //adding lines to right side:
        storeInfoRight.add(numPlayersLine);
        storeInfoRight.add(locTypeLine);
        storeInfoRight.add(lOBLine);
        storeInfoRight.add(languageLine);
        storeInfoRight.add(BannerLine);
        //adding right side to canvas:
        canvas.add(storeInfoRight, BorderLayout.EAST);

        canvas.add(getButtonBar(), BorderLayout.SOUTH);

    }

    private static void makeMultitagPanel() {
        panelWidth = 500;
        panelHeight = 600;
        int numPlayers = Integer.parseInt(tagNumPlayers);
        JPanel tagsPanel = new JPanel(new BorderLayout());
        JLabel panelTitle = new JLabel("Player Tags", JLabel.CENTER);

        JPanel devicesSection = new JPanel(new GridLayout(1, numPlayers));

        for (int i = 0; i < numPlayers; i++) {
            JPanel devicePanel = new JPanel(new GridLayout(9, 1));

            JLabel playerNo = new JLabel("Player: " + (i + 1));
//////////////////////////////////////////////////////////////////////////////         Building tag lines
            JPanel configLine = getComboBoxPanel("Configuration:", configurationOptions, configurationSelection);                            //entry 0
            JPanel wiredLine = getComboBoxPanel("Connection Type:", connectionTypeOptions, connectionTypeSelection);                  //entry 1
            JPanel locationLine = getComboBoxPanel("In-Building Location:", inBuildingLocationOptions, inBuildingLocationSelection);  //entry 2
            JPanel interactiveLine = getComboBoxPanel("Interactive:", interactiveOptions, interactiveSelection);                      //entry 3
            JPanel manufacturerLine = getComboBoxPanel("MP Manufacturer:", manufacturerOptions, manufacturerSelection);                //entry 4
            JPanel modelLine = getTextBoxPanel("MP Model:", tagMPModel);                                                        //entry 5
            JPanel orientationLine = getComboBoxPanel("Orientation:", orientationOptions, orientationSelection);                       //entry 6
            JPanel ipLine = getTextBoxPanel("IP Address:", tagIPAddress);                                                       //entry 7
////////////////////////////////////////////////////////////////////////////        Done building tag lines

//adding lines to canvas
            devicePanel.add(playerNo);
            devicePanel.add(configLine);
            devicePanel.add(wiredLine);
            devicePanel.add(locationLine);
            devicePanel.add(interactiveLine);
            devicePanel.add(manufacturerLine);
            devicePanel.add(modelLine);
            devicePanel.add(orientationLine);
            devicePanel.add(ipLine);
            devicesSection.add(devicePanel);
        }

        JScrollPane multiTagPanel = new JScrollPane(devicesSection, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        multiTagPanel.setPreferredSize(new Dimension(500, 500));
        tagsPanel.add(panelTitle, BorderLayout.NORTH);
        tagsPanel.add(multiTagPanel, BorderLayout.CENTER);
        tagsPanel.add(getButtonBar(), BorderLayout.SOUTH);//adding button bar
        canvas.add(tagsPanel);

    }

    private static void makeHostnamePanel() {
        canvas.setLayout(new BorderLayout());

        JTextField deviceNumber = new JTextField("01");
        entries.add(deviceNumber);
        JPanel deviceBar = new JPanel(new GridLayout(1, 2));
        JLabel deviceNumlbl = new JLabel("Device #:");
        deviceBar.add(deviceNumlbl);
        deviceBar.add(deviceNumber);

        JTextField hostNameBox = new JTextField(device.getHostnamePrefix());
        JPanel hostNameLine = new JPanel(new GridLayout(1, 3));
        JLabel hostnamelbl = new JLabel("Hostname Prefix:");
        hostNameBox.setEditable(false);

        hostNameLine.add(hostnamelbl);
        hostNameLine.add(hostNameBox);
        hostNameLine.add(deviceBar);

        JTextArea display = new JTextArea(1, 10);
        display.setText("Search CM for hostname and set the device number to the smallest non-taken integer");
        display.setEditable(false);
        Color beige = new Color(bgRedstat, bgGreenstat, bgBluestat);
        display.setBackground(beige);

        JButton continueButton = new JButton("Continue");

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(continueButton);
        continueButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                continueButtonPressed();
            }
        });

        canvas.add(hostNameLine, BorderLayout.NORTH);
        canvas.add(display, BorderLayout.CENTER);
        canvas.add(buttonPanel, BorderLayout.SOUTH);

    }

    private static void makeConfirmationPanel() {
        canvas.setLayout(new BorderLayout());
        panelWidth = 600;
        panelHeight = 500;

        int newlineLocation;
        String tagline = device.getCSVString();
        String tags = tagline.substring(0, tagline.indexOf("\n"));
        String values = tagline.substring(tagline.indexOf("\n") + 1);
        values = values.replaceAll(",", " ,");
        String[] tagArray = tags.split(",");
        String[] valuesArray = values.split(",");
        String tagsPreview = "";
        System.out.println("");

        for (int i = 0; i < tagArray.length; i++) {
            tagsPreview = tagsPreview + tagArray[i] + ": " + valuesArray[i] + " \n";
        }//tags preview shows what each tag is being set as, would rather it show pastable ticket info for the momentt

        JTextArea display = new JTextArea(10, 40);
        display.setText(getTicketDump());//tagsPreview is a good alternative
        display.setEditable(false);
        Color beige = new Color(bgRedstat, bgGreenstat, bgBluestat);
        display.setBackground(beige);
        JScrollPane displayPane = new JScrollPane(display);

        JPanel buttonBar = new JPanel(new GridLayout(1, 2));

        JButton continueButton = new JButton("Continue");
        continueButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                continueButtonPressed();
            }

        });

        JButton ticketDump = new JButton("Continue");
        ticketDump.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                continueButtonPressed();
            }
        });

        JButton exportButton = new JButton("Export CSV to Desktop");
        exportButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                writeToCSV();
            }
        });

        buttonBar.add(ticketDump);
        buttonBar.add(exportButton);

        //canvas.add(display, BorderLayout.CENTER);
        canvas.add(displayPane);
        canvas.add(buttonBar, BorderLayout.SOUTH);
        System.out.println("MADE IT TO CONFIRM PANEL END");

    }

    private static void makeCheckboxPanel() {
        //layout:
        canvas.setLayout(new GridLayout(8, 1));
        //panels
        JPanel importPane = new JPanel(new BorderLayout());
        JPanel connectionPane = new JPanel(new BorderLayout());
        JPanel channelPane = new JPanel(new BorderLayout());
        JPanel rsPane = new JPanel(new BorderLayout());
        JPanel timeZonePane = new JPanel(new BorderLayout());
        JPanel ntpPane = new JPanel(new BorderLayout());
        JPanel updatePane = new JPanel(new BorderLayout());
        JPanel buttonPane = new JPanel(new GridLayout(1, 2));

        //check boxes
        importBox = new JCheckBox();
        connectionBox = new JCheckBox();
        channelBox = new JCheckBox();
        rsBox = new JCheckBox();
        timeZoneBox = new JCheckBox();
        ntpBox = new JCheckBox();
        updateBox = new JCheckBox();

        //labels
        JLabel importLabel = new JLabel("Imported device(s) into CM?");
        JLabel connectionLabel = new JLabel("Good connection to device(s) in CM?");
        JLabel channelLabel = new JLabel("Device received a channel?");
        JLabel rsLabel = new JLabel("RS232 is working? (optional)");
        JLabel timeZoneLabel = new JLabel("Timezone correct?");
        JLabel ntpLabel = new JLabel("Ran NTP software updgrade?");
        JLabel updateLabel = new JLabel("Pushed channel update?");

        //tooltips
        JButton importQuestion = new JButton("?");
        importQuestion.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                questionButtonPressed("import");
            }

        });
        JButton connectionQuestion = new JButton("?");
        connectionQuestion.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                questionButtonPressed("connection");
            }

        });
        JButton channelQuestion = new JButton("?");
        channelQuestion.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                questionButtonPressed("channel");
            }

        });
        JButton rsQuestion = new JButton("?");
        rsQuestion.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                questionButtonPressed("rs");
            }

        });
        JButton timeZoneQuestion = new JButton("?");
        timeZoneQuestion.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                questionButtonPressed("timeZone");
            }

        });
        JButton ntpQuestion = new JButton("?");
        ntpQuestion.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                questionButtonPressed("NTP");
            }

        });
        JButton updateQuestion = new JButton("?");
        updateQuestion.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                questionButtonPressed("update");
            }

        });
        // end of tootip buttons

        //sizing
        panelWidth = 500;
        panelHeight = 150;
        //setting up continue button
        JButton continueButton = new JButton("Continue");
        continueButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                continueButtonPressed();
            }

        });
        JButton backButton = new JButton("Back");
        backButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                backButtonPressed();
            }

        });
        /**
         * JPanel importPane = new JPanel(new BorderLayout()); JPanel
         * connectionPane = new JPanel(new BorderLayout()); JPanel channelPane =
         * new JPanel(new BorderLayout()); JPanel rsPane = new JPanel(new
         * BorderLayout()); JPanel timeZonePane = new JPanel(new
         * BorderLayout()); JPanel ntpPane = new JPanel(new BorderLayout());
         * JPanel updatePane = new JPanel(new BorderLayout());
         */

        importPane.add(importBox, BorderLayout.WEST);
        importPane.add(importLabel, BorderLayout.CENTER);
        importPane.add(importQuestion, BorderLayout.EAST);

        connectionPane.add(connectionBox, BorderLayout.WEST);
        connectionPane.add(connectionLabel, BorderLayout.CENTER);
        connectionPane.add(connectionQuestion, BorderLayout.EAST);

        channelPane.add(channelBox, BorderLayout.WEST);
        channelPane.add(channelLabel, BorderLayout.CENTER);
        channelPane.add(channelQuestion, BorderLayout.EAST);

        rsPane.add(rsBox, BorderLayout.WEST);
        rsPane.add(rsLabel, BorderLayout.CENTER);
        rsPane.add(rsQuestion, BorderLayout.EAST);

        timeZonePane.add(timeZoneBox, BorderLayout.WEST);
        timeZonePane.add(timeZoneLabel, BorderLayout.CENTER);
        timeZonePane.add(timeZoneQuestion, BorderLayout.EAST);

        ntpPane.add(ntpBox, BorderLayout.WEST);
        ntpPane.add(ntpLabel, BorderLayout.CENTER);
        ntpPane.add(ntpQuestion, BorderLayout.EAST);

        updatePane.add(updateBox, BorderLayout.WEST);
        updatePane.add(updateLabel, BorderLayout.CENTER);
        updatePane.add(updateQuestion, BorderLayout.EAST);

        buttonPane.add(continueButton);

        canvas.add(importPane);
        canvas.add(connectionPane);
        canvas.add(channelPane);
        canvas.add(rsPane);
        canvas.add(timeZonePane);
        canvas.add(ntpPane);
        canvas.add(updatePane);
        canvas.add(buttonPane);

    }

// </editor-fold>
// <editor-fold defaultstate="collapsed" desc=" Form Continue Handlers ">
    private static void handleTechContinue() {
        System.out.println("Handling Continue from Tech Info Panel");
        ArrayList<String> errors = checkInvalidChars();
        boolean badChars = false;
        if (errors.size() > 0) {
            displayErrors(errors);
            badChars = true;
        }

        tName = ((JTextField) entries.get(0)).getText();
        tPhone = ((JTextField) entries.get(1)).getText();
        tCompany = ((JTextField) entries.get(2)).getText();
        poNum = poNumBox.getText();
        if (badChars) {
            return;
        }

        errors = tech.setInfo(tName, tPhone, tCompany, poNum);
        if (errors.size() > 0) {
            displayErrors(errors);
            return;
        }
        currentCanvas = "store";

    }

    private static void handleStoreContinue() {
        ArrayList<String> errors = checkInvalidChars();
        boolean badChars = false;//if their input is flagged by standard check
        if (errors.size() > 0) {//was flagged
            displayErrors(errors);
            badChars = true;
        }
        tagStoreID = ((JTextField) entries.get(0)).getText();
        tagStorePhone = ((JTextField) entries.get(1)).getText();
        tagStoreStreet = ((JTextField) entries.get(2)).getText();
        tagStoreCity = ((JTextField) entries.get(3)).getText();
        sProvince = provinceTags[((JComboBox) entries.get(4)).getSelectedIndex()];
        selectedProvince = ((JComboBox) entries.get(4)).getSelectedIndex();
        tagStorePostal = ((JTextField) entries.get(5)).getText();
        tagNumPlayers = ((JTextField) entries.get(6)).getText();
        String sLocationType = locationTypeOptions[((JComboBox) entries.get(7)).getSelectedIndex()];
        tagLocationType = ((JComboBox) entries.get(7)).getSelectedIndex();
        String lineOfBusiness = lineOfBusinessOptions[((JComboBox) entries.get(8)).getSelectedIndex()];
        tagLineOfBusiness = ((JComboBox) entries.get(8)).getSelectedIndex();
        String language = languageOptions[((JComboBox) entries.get(9)).getSelectedIndex()];
        tagLanguage = ((JComboBox) entries.get(9)).getSelectedIndex();
        String banner = bannerOptions[((JComboBox) entries.get(10)).getSelectedIndex()];
        tagBanner = ((JComboBox) entries.get(10)).getSelectedIndex();

        if (badChars) {//need to check if it failed basic input check
            return;//if so, break out to rebuild form.
        }
        //if the generic test passed, we are ready to push the info
        //to the specific input tests
        errors = store.setInfo(tagStoreID, tagStorePhone, tagStoreStreet, tagStoreCity, sProvince, tagStorePostal, tagNumPlayers, tagLocationType, tagLineOfBusiness, tagLanguage, tagBanner);
        if (errors.size() > 0) {//if it failed a specific input check
            displayErrors(errors);
            return;
        }
        currentCanvas = "multitags";
    }

    private static void handleTagsContinue() {
        System.out.println("ONLY 1 PLAYER: HANDLE TAGS CONTINUE");
        ArrayList<String> errors = checkInvalidChars();
        boolean badChars = false;
        if (errors.size() > 0) {
            displayErrors(errors);
            badChars = true;
        }
        //handle dropdowns
        ////////////////////////////////////////////////////////////////banner
        String banner = bannerOptions[((JComboBox) entries.get(0)).getSelectedIndex()];
        tagBanner = ((JComboBox) entries.get(0)).getSelectedIndex();
        ///////////////////////////////////////////////////////////configuration
        String configuration = configurationOptions[((JComboBox) entries.get(1)).getSelectedIndex()];
        configurationSelection = ((JComboBox) entries.get(1)).getSelectedIndex();
        /////////////////////////////////////////////////////////connection type
        String connectionType = connectionTypeOptions[((JComboBox) entries.get(2)).getSelectedIndex()];
        connectionTypeSelection = ((JComboBox) entries.get(2)).getSelectedIndex();
        ///////////////////////////////////////////////////in building location
        String inBuildingLocation = inBuildingLocationTags[((JComboBox) entries.get(3)).getSelectedIndex()];
        inBuildingLocationSelection = ((JComboBox) entries.get(3)).getSelectedIndex();
        /////////////////////////////////////////////////////////////interactive
        String interactive = interactiveOptions[((JComboBox) entries.get(4)).getSelectedIndex()];
        interactiveSelection = ((JComboBox) entries.get(4)).getSelectedIndex();
        ////////////////////////////////////////////////////////////////language
        String language = languageOptions[((JComboBox) entries.get(5)).getSelectedIndex()];
        tagLanguage = ((JComboBox) entries.get(5)).getSelectedIndex();
        ////////////////////////////////////////////////////////line of business
        String lineOfBusiness = lineOfBusinessOptions[((JComboBox) entries.get(6)).getSelectedIndex()];
        tagLineOfBusiness = ((JComboBox) entries.get(6)).getSelectedIndex();
        ////////////////////////////////////////////////////////////store number
        tagStoreID = ((JTextField) entries.get(7)).getText();
        ///////////////////////////////////////////////////////////location type
        String locationType = locationTypeOptions[((JComboBox) entries.get(8)).getSelectedIndex()];
        tagLocationType = ((JComboBox) entries.get(8)).getSelectedIndex();
        ////////////////////////////////////////////////////////////manufacturer
        String manufacturer = manufacturerOptions[((JComboBox) entries.get(9)).getSelectedIndex()];
        manufacturerSelection = ((JComboBox) entries.get(9)).getSelectedIndex();
        //////////////////////////////////////////////////////manufacturer model
        tagMPModel = ((JTextField) entries.get(10)).getText();
        /////////////////////////////////////////////////////////////orientation
        String orientation = orientationOptions[((JComboBox) entries.get(11)).getSelectedIndex()];
        orientationSelection = ((JComboBox) entries.get(11)).getSelectedIndex();
        //////////////////////////////////////////////////////////////ip address
        tagIPAddress = ((JTextField) entries.get(12)).getText();

        //now that we've saved their selections, we can check if we need to 
        //rebuild the form because of a bad input
        if (badChars) {// if an error had been detected 
            return;//break to rebuild the form with inputted values
        }
        //attempting to add this info onto the device, will receive an ArrayList
        //of Strings which contain the detailed error message
        int offset = -1;//we added a null value to the beginning of the arraylists after importing them.
        //so we need to correct that when passing it through or else it will get out of bounds.
        //errors = device.setTags(tagBanner + offset, tagConfig + offset, tagConnectionType + offset,
        //       tagInBuildingLocation + offset, tagInteractive + offset, tagLanguage + offset, tagLineOfBusiness + offset, tagStoreID,
        //       tagLocationType + offset, tagManufacturer + offset, tagMPModel, tagOrientation + offset, tagIPAddress);

        if (errors.size() > 0) {//if the array list isnt empty, there's a problem
            displayErrors(errors);//this method prints them all on a new window
            return;//break to rebuild form with saved values
        }
        //if we got here, we did not have any errors, so this was a success
        currentCanvas = "host";
    }

    private static void handleMultiTagContinue() {
        ArrayList<String> errors = checkInvalidChars();//checks all entries for banned characters
        ArrayList<String> playerErrors = new ArrayList();//checks MP input for errors
        ArrayList<Computron> tempPlayers = new ArrayList();
        boolean badChars = false;//flag for if there was bad input

        if (errors.size() > 0) {//if there was at least 1 error
            displayErrors(errors); //print the error
            badChars = true;//set flag so that later we know to break out
        }
        int numberOfPlayers = Integer.parseInt(tagNumPlayers);

        //for each device:
        for (int currentPlayerNumber = 0; currentPlayerNumber < numberOfPlayers; currentPlayerNumber++) {
            //lets grab their inputs and store them in some variables:
            String configuration = configurationOptions[((JComboBox) entries.get(0)).getSelectedIndex()];
            configurationSelection = ((JComboBox) entries.get(0)).getSelectedIndex();

            String connectionType = connectionTypeOptions[((JComboBox) entries.get(1)).getSelectedIndex()];
            connectionTypeSelection = ((JComboBox) entries.get(1)).getSelectedIndex();

            String inBuildingLocation = inBuildingLocationTags[((JComboBox) entries.get(2)).getSelectedIndex()];
            inBuildingLocationSelection = ((JComboBox) entries.get(2)).getSelectedIndex();

            String interactive = interactiveOptions[((JComboBox) entries.get(3)).getSelectedIndex()];
            interactiveSelection = ((JComboBox) entries.get(3)).getSelectedIndex();

            String manufacturer = manufacturerOptions[((JComboBox) entries.get(4)).getSelectedIndex()];
            manufacturerSelection = ((JComboBox) entries.get(4)).getSelectedIndex();

            tagMPModel = ((JTextField) entries.get(5)).getText();

            String orientation = orientationOptions[((JComboBox) entries.get(6)).getSelectedIndex()];
            orientationSelection = ((JComboBox) entries.get(6)).getSelectedIndex();

            tagIPAddress = ((JTextField) entries.get(7)).getText();
            //done pulling inputs 
            //now that we've stored their choices - if there was an invalid input, we can rebuild

            if (badChars) {// if an error had been detected 
                return;//break to rebuild the form with inputted values
            }
            //attempting to create player, will receive arraylist of 
            //of Strings which contain all applicable error messages:
            int offset = -1;//we added a null value to the beginning of the arraylists after importing them.
            //this offset corrects for that.

            Computron newDevice = new Computron(store, tech, configurationTags,
                    connectionTypeTags, inBuildingLocationTags, inBuildingLocationHostnames,
                    interactiveTags, manufacturerTags, orientationTags, provinceTags);
            errors = newDevice.setTags(configurationSelection, connectionTypeSelection,
                    inBuildingLocationSelection, interactiveSelection,
                    manufacturerSelection, tagMPModel, orientationSelection, tagIPAddress);

            for (int e = 0; e < errors.size(); e++) {
                playerErrors.add(errors.get(e));
            }
            tempPlayers.add(newDevice);
        }///end device creation loop
        boolean brake = false;
        if (errors.size() > 0) {//if the array list isnt empty, there's a problem
            displayErrors(errors);//this method prints them all on a new window
            brake = true;
        }
        if (playerErrors.size() > 0) {
            displayErrors(playerErrors);
            brake = true;
        }
        if (brake) {
            return;
        }
        //if we got here, we did not have any errors, so this was a success
        //lets now add the devices to our players array

        for (int i = 0; i < tempPlayers.size(); i++) {
            players.add(tempPlayers.get(i));
        }
        currentCanvas = "host";
        System.out.println("should go to host panel now.");

    }

    private static void handleHostContinue() {
        ArrayList<String> errors = new ArrayList();

        String numEntryString = ((JTextField) entries.get(0)).getText();
        boolean addLeadingZero = false;
        try {
            int numEntered = Integer.parseInt(numEntryString);
            if (numEntered < 10 && numEntryString.length() == 1) {
                addLeadingZero = true;
            }
        } catch (Exception e) {
            errors.add("Device number entry not a valid number");
            displayErrors(errors);
            return;
        }
        if (addLeadingZero) {
            numEntryString = "0" + numEntryString;
        }
        device.setHostname(numEntryString);

        currentCanvas = "conf";
    }

    private static void handleConfContinue() {
        currentCanvas = "check";
    }

    private static void handleCheckContinue() {

        checks[0] = importBox.isSelected();
        checks[1] = connectionBox.isSelected();
        checks[2] = channelBox.isSelected();
        checks[3] = rsBox.isSelected();
        checks[4] = timeZoneBox.isSelected();
        checks[5] = ntpBox.isSelected();
        checks[6] = updateBox.isSelected();

        device.setStatus(checks);
        //look at checkboxes and make sure they are appropriate
        //if rs232 box is not checked, display "set to not managed" popup
        //create ticket when done
        createTicket();

    }

    private static void clearStateFields() {
        System.out.println("Reset");
        entries = new ArrayList();
        buildCanvas();
    }

// </editor-fold>
// <editor-fold defaultstate="collapsed" desc=" Ticket Generation ">
    private static void createTicket() {
        TicketCreator loblawMaker = new TicketCreator(
                loblawTicketTemplateCode,//template ID for ticket system
                "Loblaw",//name of customer
                getTicketDump(),//ticket description text string
                device.getStore().getStoreNumber(),//store number
                "Loblaw New Site Provisioning - SiteID: " + device.getStore().getBanner() + " " + device.getStore().getStoreNumber() + " " + device.getStore().getLineOfBusiness()//ticket subject line
        );

    }

    private static String getTicketDumpOld() {
        String techInfo;
        techInfo = device.getTech().getTechInfo();
        String storeInfo = device.getStore().getStoreInfo();
        String tagInfo = device.getTagInfo();

        return techInfo + "\n\n\n" + storeInfo + "\n\n\n" + tagInfo;
    }

    private static String getTicketDump() {
        return device.getTicketDescription();
    }

// </editor-fold>
// <editor-fold defaultstate="collapsed" desc=" Global Button Handlers ">
    private static void questionButtonPressed(String code) {
        if (code.compareToIgnoreCase("import") == 0) {
            JOptionPane.showMessageDialog(frame, "Import tooltip message");
        } else if (code.compareToIgnoreCase("connection") == 0) {
            JOptionPane.showMessageDialog(frame, "Connection tooltip message");
        } else if (code.compareToIgnoreCase("channel") == 0) {
            JOptionPane.showMessageDialog(frame, "Channel tooltip message");
        } else if (code.compareToIgnoreCase("rs") == 0) {
            JOptionPane.showMessageDialog(frame, "RS232 tooltip message");
        } else if (code.compareToIgnoreCase("timeZone") == 0) {
            JOptionPane.showMessageDialog(frame, "Time Zone tooltip message");
        } else if (code.compareToIgnoreCase("NTP") == 0) {
            JOptionPane.showMessageDialog(frame, "NTP tooltip message");
        } else if (code.compareToIgnoreCase("update") == 0) {
            JOptionPane.showMessageDialog(frame, "Update tooltip message");
        }
    }

    private static void backButtonPressed() {
        System.out.println(currentCanvas);
    }

    private static void resetButtonPressed() {
        tagBanner = 0;
        configurationSelection = 0;
        connectionTypeSelection = 0;
        inBuildingLocationSelection = 0;
        interactiveSelection = 0;
        tagLanguage = 0;
        tagLineOfBusiness = 0;
        tagLocationType = 0;
        manufacturerSelection = 0;
        orientationSelection = 0;

        //tech infos
        tName = "";
        tPhone = "";
        tCompany = "BFG";
        poNum = "N/A";

        //store infos
        tagStorePhone = "";
        tagStoreStreet = "";
        tagStoreCity = "";
        sProvince = "";
        tagStorePostal = "";
        selectedProvince = 0;
        clearStateFields();
    }

    private static void continueButtonPressed() {
        if (currentCanvas.compareToIgnoreCase("tech") == 0) {
            System.out.println("TECH");
            handleTechContinue();

        } else if (currentCanvas.compareToIgnoreCase("store") == 0) {
            System.out.println("STORE info finished");
            handleStoreContinue();

        } else if (currentCanvas.compareToIgnoreCase("tags") == 0) {
            System.out.println("TAGS info finished");
            handleTagsContinue();

        } else if (currentCanvas.compareToIgnoreCase("multitags") == 0) {
            System.out.println("MULTITAGS info finished");
            handleMultiTagContinue();

        } else if (currentCanvas.compareToIgnoreCase("host") == 0) {
            System.out.println("HOST info finished");
            handleHostContinue();
            //do nothin
        } else if (currentCanvas.compareToIgnoreCase("conf") == 0) {
            System.out.println("CONF info finished");
            handleConfContinue();
            //do nothin
        } else if (currentCanvas.compareToIgnoreCase("check") == 0) {
            System.out.println("CHECK info finished");
            handleCheckContinue();
        } else if (currentCanvas.compareToIgnoreCase("done") == 0) {
            System.out.println("FIN");
        } else {
            System.out.println("ERROR! BAD STATE CODE,"
                    + " BAD! BAD STATE CODE, BAD!");
            killCanvas();
            return;
        }
        buildCanvas();
    }

// </editor-fold>
// <editor-fold defaultstate="collapsed" desc=" I/O Methods ">
    private static ArrayList<String> getFileText(String filePath) {
        ArrayList<String> toReturn = new ArrayList();
        BufferedReader br = null;
        try {
            String sCurrentLine;
            br = new BufferedReader(new FileReader(filePath));

            while ((sCurrentLine = br.readLine()) != null) {
                toReturn.add(sCurrentLine);
            }
            br.close();
        } catch (IOException e) {
            System.out.println("Error in " + filePath + ":\n" + e);
        }
        return toReturn;
    }

    private static void writeToCSV() {
        FileWriter tofile;
        try {
            tofile = new FileWriter("C:/Users/" + System.getProperty("user.name") + "/Desktop/" + device.getHostname() + ".csv");
            tofile.write(device.getCSVString());
            tofile.close();
        } catch (Exception e) {
            System.out.println("Write error!");
        }

    }

// </editor-fold>
// <editor-fold defaultstate="collapsed" desc="Methods for populating form fields from shared drive ">
    private static void populateArrays() {
        System.out.println("Populating Tag Arrays");
        try {
            populateBanner();
            populateConfiguration();
            populateConnectionType();
            populateInBuildingLocation();
            populateInteractive();
            populateLanguage();
            populateLineOfBusiness();
            populateLocationType();
            populateManufacturer();
            populateOrientation();
            populateProvinces();
            populateBannedChars();
        } catch (Exception e) {
            System.out.println("Crashed while reading arrays from files:\n" + e.getLocalizedMessage());
        }
    }

    private static void populateBanner() {
        ////////////////////first we do the array for the JComboBox
        ArrayList<String> fromFile = getFileText(rootPath + "Banner\\" + "displayOptions.txt");
        if (fromFile.size() < 1) {
            System.out.println("failed to load displayable Banner list");
            System.exit(0);
        } else {
            bannerOptions = new String[fromFile.size() + 1];
            bannerOptions[0] = "";
            for (int i = 0; i < fromFile.size(); i++) {
                bannerOptions[i + 1] = fromFile.get(i);
            }
        }
        ///////////////////////////////now for the tags
        fromFile = getFileText(rootPath + "Banner\\" + "tagOptions.txt");
        if (fromFile.size() < 1) {
            System.out.println("failed to load taggable Banner list");
            System.exit(0);
        } else {
            bannerTags = new String[fromFile.size()];
            for (int i = 0; i < fromFile.size(); i++) {
                bannerTags[i] = fromFile.get(i);
            }
        }

    }

    private static void populateConfiguration() {
        ////////////////////first we do the array for the JComboBox
        ArrayList<String> fromFile = getFileText(rootPath + "Configuration\\" + "displayOptions.txt");
        if (fromFile.size() < 1) {
            System.out.println("failed to load displayable Configuration list");
            System.exit(0);
        } else {
            configurationOptions = new String[fromFile.size() + 1];
            configurationOptions[0] = "";
            for (int i = 0; i < fromFile.size(); i++) {
                configurationOptions[i + 1] = fromFile.get(i);
            }
        }
        ///////////////////////////////now for the tags
        fromFile = getFileText(rootPath + "Configuration\\" + "tagOptions.txt");
        if (fromFile.size() < 1) {
            System.out.println("failed to load taggable Configuration list");
            System.exit(0);
        } else {
            configurationTags = new String[fromFile.size()];
            for (int i = 0; i < fromFile.size(); i++) {
                configurationTags[i] = fromFile.get(i);
            }
        }

    }

    private static void populateConnectionType() {
        ////////////////////first we do the array for the JComboBox
        ArrayList<String> fromFile = getFileText(rootPath + "ConnectionType\\" + "displayOptions.txt");
        if (fromFile.size() < 1) {
            System.out.println("failed to load displayable ConnectionType list");
            System.exit(0);
        } else {
            connectionTypeOptions = new String[fromFile.size() + 1];
            connectionTypeOptions[0] = "";
            for (int i = 0; i < fromFile.size(); i++) {
                connectionTypeOptions[i + 1] = fromFile.get(i);
            }
        }
        ///////////////////////////////now for the tags
        fromFile = getFileText(rootPath + "ConnectionType\\" + "tagOptions.txt");
        if (fromFile.size() < 1) {
            System.out.println("failed to load taggable ConnectionType list");
            System.exit(0);
        } else {
            connectionTypeTags = new String[fromFile.size()];
            for (int i = 0; i < fromFile.size(); i++) {
                connectionTypeTags[i] = fromFile.get(i);
            }
        }

    }

    private static void populateInBuildingLocation() {
        ////////////////////first we do the array for the JComboBox
        ArrayList<String> fromFile = getFileText(rootPath + "InBuildingLocation\\" + "displayOptions.txt");
        if (fromFile.size() < 1) {
            System.out.println("failed to load displayable InBuildingLocation list");
            System.exit(0);
        } else {
            inBuildingLocationOptions = new String[fromFile.size() + 1];
            inBuildingLocationOptions[0] = "";
            for (int i = 0; i < fromFile.size(); i++) {
                inBuildingLocationOptions[i + 1] = fromFile.get(i);
            }
        }
        ///////////////////////////////now for the tags
        fromFile = getFileText(rootPath + "InBuildingLocation\\" + "tagOptions.txt");
        if (fromFile.size() < 1) {
            System.out.println("failed to load taggable InBuildingLocation list");
            System.exit(0);
        } else {
            inBuildingLocationTags = new String[fromFile.size()];
            for (int i = 0; i < fromFile.size(); i++) {
                inBuildingLocationTags[i] = fromFile.get(i);
            }
        }
        ///////////////////////////////now for the tags
        fromFile = getFileText(rootPath + "InBuildingLocation\\" + "hostnameOptions.txt");
        if (fromFile.size() < 1) {
            System.out.println("failed to load hostname InBuildingLocation list");
            System.exit(0);
        } else {
            inBuildingLocationHostnames = new String[fromFile.size() + 1];
            inBuildingLocationHostnames[0] = "";
            for (int i = 0; i < fromFile.size(); i++) {
                inBuildingLocationHostnames[i + 1] = fromFile.get(i);
            }
        }

    }

    private static void populateInteractive() {
        ////////////////////first we do the array for the JComboBox
        ArrayList<String> fromFile = getFileText(rootPath + "Interactive\\" + "displayOptions.txt");
        if (fromFile.size() < 1) {
            System.out.println("failed to load displayable Interactive list");
            System.exit(0);
        } else {
            interactiveOptions = new String[fromFile.size() + 1];
            interactiveOptions[0] = "";
            for (int i = 0; i < fromFile.size(); i++) {
                interactiveOptions[i + 1] = fromFile.get(i);
            }
        }
        ///////////////////////////////now for the tags
        fromFile = getFileText(rootPath + "Interactive\\" + "tagOptions.txt");
        if (fromFile.size() < 1) {
            System.out.println("failed to load taggable Interactive list");
            System.exit(0);
        } else {
            interactiveTags = new String[fromFile.size()];
            for (int i = 0; i < fromFile.size(); i++) {
                interactiveTags[i] = fromFile.get(i);
            }
        }

    }

    private static void populateLanguage() {
        ////////////////////first we do the array for the JComboBox
        ArrayList<String> fromFile = getFileText(rootPath + "Language\\" + "displayOptions.txt");
        if (fromFile.size() < 1) {
            System.out.println("failed to load displayable Language list");
            System.exit(0);
        } else {
            languageOptions = new String[fromFile.size() + 1];
            languageOptions[0] = "";
            for (int i = 0; i < fromFile.size(); i++) {
                languageOptions[i + 1] = fromFile.get(i);
            }
        }
        ///////////////////////////////now for the tags
        fromFile = getFileText(rootPath + "Language\\" + "tagOptions.txt");
        if (fromFile.size() < 1) {
            System.out.println("failed to load taggable Language list");
            System.exit(0);
        } else {
            languageTags = new String[fromFile.size()];
            for (int i = 0; i < fromFile.size(); i++) {
                languageTags[i] = fromFile.get(i);
            }
        }

    }

    private static void populateLineOfBusiness() {
        ////////////////////first we do the array for the JComboBox
        ArrayList<String> fromFile = getFileText(rootPath + "LineOfBusiness\\" + "displayOptions.txt");
        if (fromFile.size() < 1) {
            System.out.println("failed to load displayable LineOfBusiness list");
            System.exit(0);
        } else {
            lineOfBusinessOptions = new String[fromFile.size() + 1];
            lineOfBusinessOptions[0] = "";
            for (int i = 0; i < fromFile.size(); i++) {
                lineOfBusinessOptions[i + 1] = fromFile.get(i);
            }
        }
        ///////////////////////////////now for the tags
        fromFile = getFileText(rootPath + "LineOfBusiness\\" + "tagOptions.txt");
        if (fromFile.size() < 1) {
            System.out.println("failed to load taggable LineOfBusiness list");
            System.exit(0);
        } else {
            lineOfBusinessTags = new String[fromFile.size()];
            for (int i = 0; i < fromFile.size(); i++) {
                lineOfBusinessTags[i] = fromFile.get(i);
            }
        }

    }

    private static void populateLocationType() {
        ////////////////////first we do the array for the JComboBox
        ArrayList<String> fromFile = getFileText(rootPath + "LocationType\\" + "displayOptions.txt");
        if (fromFile.size() < 1) {
            System.out.println("failed to load displayable LocationType list");
            System.exit(0);
        } else {
            locationTypeOptions = new String[fromFile.size() + 1];
            locationTypeOptions[0] = "";
            for (int i = 0; i < fromFile.size(); i++) {
                locationTypeOptions[i + 1] = fromFile.get(i);
            }
        }
        ///////////////////////////////now for the tags
        fromFile = getFileText(rootPath + "LocationType\\" + "tagOptions.txt");
        if (fromFile.size() < 1) {
            System.out.println("failed to load taggable LocationType list");
            System.exit(0);
        } else {
            locationTypeTags = new String[fromFile.size()];
            for (int i = 0; i < fromFile.size(); i++) {
                locationTypeTags[i] = fromFile.get(i);
            }
        }

    }

    private static void populateManufacturer() {
        ////////////////////first we do the array for the JComboBox
        ArrayList<String> fromFile = getFileText(rootPath + "Manufacturer\\" + "displayOptions.txt");
        if (fromFile.size() < 1) {
            System.out.println("failed to load displayable Manufacturer list");
            System.exit(0);
        } else {
            manufacturerOptions = new String[fromFile.size() + 1];
            manufacturerOptions[0] = "";
            for (int i = 0; i < fromFile.size(); i++) {
                manufacturerOptions[i + 1] = fromFile.get(i);
            }
        }
        ///////////////////////////////now for the tags
        fromFile = getFileText(rootPath + "Manufacturer\\" + "tagOptions.txt");
        if (fromFile.size() < 1) {
            System.out.println("failed to load taggable Manufacturer list");
            System.exit(0);
        } else {
            manufacturerTags = new String[fromFile.size()];
            for (int i = 0; i < fromFile.size(); i++) {
                manufacturerTags[i] = fromFile.get(i);
            }
        }

    }

    private static void populateOrientation() {
        ////////////////////first we do the array for the JComboBox
        ArrayList<String> fromFile = getFileText(rootPath + "Orientation\\" + "displayOptions.txt");
        if (fromFile.size() < 1) {
            System.out.println("failed to load displayable Orientation list");
            System.exit(0);
        } else {
            orientationOptions = new String[fromFile.size() + 1];
            orientationOptions[0] = "";
            for (int i = 0; i < fromFile.size(); i++) {
                orientationOptions[i + 1] = fromFile.get(i);
            }
        }
        ///////////////////////////////now for the tags
        fromFile = getFileText(rootPath + "Orientation\\" + "tagOptions.txt");
        if (fromFile.size() < 1) {
            System.out.println("failed to load taggable Orientation list");
            System.exit(0);
        } else {
            orientationTags = new String[fromFile.size()];
            for (int i = 0; i < fromFile.size(); i++) {
                orientationTags[i] = fromFile.get(i);
            }
        }

    }

    private static void populateProvinces() {
        ////////////////////first we do the array for the JComboBox
        ArrayList<String> fromFile = getFileText(rootPath + "Provinces\\" + "displayOptions.txt");
        if (fromFile.size() < 1) {
            System.out.println("failed to load displayable province list");
            System.exit(0);
        } else {
            provinceOptions = new String[fromFile.size() + 1];
            provinceOptions[0] = "";
            for (int i = 0; i < fromFile.size(); i++) {
                provinceOptions[i + 1] = fromFile.get(i);
            }
        }
        ///////////////////////////////now for the tags
        fromFile = getFileText(rootPath + "Provinces\\" + "tagOptions.txt");
        if (fromFile.size() < 1) {
            System.out.println("failed to load taggable province list");
            System.exit(0);
        } else {
            provinceTags = new String[fromFile.size()];
            for (int i = 0; i < fromFile.size(); i++) {
                provinceTags[i] = fromFile.get(i);
            }
        }
    }

    private static void populateBannedChars() {
        ArrayList<String> fromFile = getFileText(rootPath + "bannedInputCharacters.txt");
        if (fromFile.size() < 1) {
            System.out.println("failed to load banned characters list");
            System.exit(0);
        } else {
            bannedChars = new String[fromFile.size()];
            for (int i = 0; i < fromFile.size(); i++) {
                bannedChars[i] = fromFile.get(i);
            }
        }
    }

// </editor-fold>
    private static void checkForUpdates() {

    }

    private static ArrayList<String> checkInvalidChars() {
        ArrayList<String> errors = new ArrayList();
        for (int i = 0; i < entries.size(); i++) {//i through entries
            try {
                JTextField temp = (JTextField) entries.get(i);
                //System.out.println("Working with " + temp.getText());
                if (temp.getText().compareToIgnoreCase("") == 0) {
                    errors.add("Blank Value for input line " + (i + 1));
                }
                for (int j = 0; j < temp.getText().length(); j++) {//j through letters
                    for (int k = 0; k < bannedChars.length; k++) {
                        if ((temp.getText().charAt(j) + "").compareToIgnoreCase(bannedChars[k]) == 0) {
                            errors.add("invalid character \""
                                    + temp.getText().charAt(j) + "\" found in \""
                                    + temp.getText() + "\" on line " + (i + 1)
                                    + " slot " + (j + 1));
                        }
                    }
                }
            } catch (Exception e) {
                try {
                    JComboBox temp = (JComboBox) entries.get(i);
                    if (temp.getSelectedIndex() == 0) {
                        errors.add("Line " + (i + 1) + " dropdown is empty");
                    }
                } catch (Exception f) {
                    System.out.println("Invalid JComponent Analyzed:");
                    System.out.println(e.getMessage());
                    System.out.println(f.getMessage());
                }

            }
        }
        return errors;
    }

    private static JPanel getComboBoxPanel(String label, String[] options, int selection) {
        JPanel line = new JPanel(new GridLayout(1, 2));//Banner tag
        JLabel labelText = new JLabel(label);
        JComboBox box = new JComboBox(options);
        box.setSelectedIndex(selection);
        line.add(labelText);
        line.add(box);
        entries.add(box);
        return line;
    }

    private static JPanel getTextBoxPanel(String label, String entry) {
        JPanel line = new JPanel(new GridLayout(1, 2));
        JLabel labeText = new JLabel(label);
        JTextField entryBox = new JTextField(entry);
        line.add(labeText);
        line.add(entryBox);
        entries.add(entryBox);
        return line;
    }

    private static JPanel getButtonBar() {
        JButton clearButton = new JButton("Reset");
        clearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                resetButtonPressed();
            }
        });
        JButton continueButton = new JButton("Continue");
        continueButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                continueButtonPressed();
            }
        });

        JPanel buttonLine = new JPanel(new GridLayout(1, 2));
        buttonLine.add(clearButton);
        buttonLine.add(continueButton);
        return buttonLine;
    }
}
