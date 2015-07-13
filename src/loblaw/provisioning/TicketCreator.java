/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package loblaw.provisioning;

import javax.swing.JOptionPane;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 *
 * @author MWatkins
 */
public class TicketCreator {

    WebDriver activeBrowser;
    String chromeDriverPath = "\\\\10.0.104.25\\SupportShare\\zMichael_Watkins\\ChromeDriver\\chromedriver.exe";
    String newTicket = "http://support.stratacache.com/WorkOrder.do?reqTemplate=";
    String ticketTemplate = "";
    String storeNumberText = "TESTSTORENUMBER";
    String ticketTitleText = "TEST TICKET PLEASE DELETE ME ASAP";
    String ticketDumpText = "TEST TICKET DESCRIPTION; DELETE PLOX";

    public TicketCreator(String templateID, String contactName, String descriptionText, String storeNumber, String titleText) {
        ticketTemplate = templateID;
        ticketTemplate = "7501";//hardcoding for loblaw
        storeNumberText = storeNumber;
        ticketTitleText = titleText;
        ticketDumpText = descriptionText;

        init();
        boolean escapeLogin = false;
        while (!escapeLogin) {
            escapeLogin = true;
            if (isLoginPage()) {
                login();
                escapeLogin = false;
            } else {
                createTicket();
            }
        }

        try {

             activeBrowser.quit();//OR ELSE DRIVER PROCESS STAYS OPEN AN HOGS MEMORY.
            Thread.sleep(1000);
            // System.exit(0);
        } catch (Exception e) {
            System.out.println("there was an issue closing the browser, please close it manually.");
            System.exit(0);
        }
    }

    private void init() {
        System.setProperty("webdriver.chrome.driver", chromeDriverPath);
        ChromeOptions opt = new ChromeOptions();
        opt.addArguments("--user-data-dir=C:/ChromeProfile");//custom profile so that logins save
        activeBrowser = new ChromeDriver(opt);
        activeBrowser.get(newTicket + ticketTemplate);
    }

    private boolean isLoginPage() {
        try {
            WebElement findElement = activeBrowser.findElement(By.xpath("//*[@id=\"signedInCB\"]"));
            findElement.click();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void login() {
        JOptionPane.showMessageDialog(null, "Please Log in and then press OK to continue");
    }

    private void createTicket() {
        try {

         //   WebElement templateDropdown = (new WebDriverWait(activeBrowser, 10)).until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id=\"reqTemplateListh\"]/p/span")));
            //  templateDropdown.click();
            //WebElement loblawLine = (new WebDriverWait(activeBrowser, 10)).until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id=\"reqTemplateList\"]/div/table/tbody/tr/td/ul/li[45]/a")));
            //loblawLine.click();
            Thread.sleep(300);
            WebElement contactName = (new WebDriverWait(activeBrowser, 10)) //added this line
                    .until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id=\"reqSearch\"]")));
            contactName.sendKeys("Loblaw");

            WebElement storeNumberArea = (new WebDriverWait(activeBrowser, 10).until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id=\"UDF_CHAR2\"]"))));
            storeNumberArea.clear();
            storeNumberArea.sendKeys(storeNumberText);

            WebElement titleArea = (new WebDriverWait(activeBrowser, 10)).until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id=\"title\"]")));
            //title format: Loblaw New Site Provisioning - SiteID: [Banner] [Location ID] [Line of Business]
            titleArea.clear();
            titleArea.sendKeys(ticketTitleText);

            WebElement descriptionArea = (new WebDriverWait(activeBrowser, 10)).until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id=\"zEditor\"]/div/iframe")));
            descriptionArea.sendKeys(Keys.chord(Keys.CONTROL, "a"));
            descriptionArea.sendKeys(ticketDumpText);

            WebElement addRequestButton = (new WebDriverWait(activeBrowser, 10)).until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id=\"buttonType\"]")));
            addRequestButton.click();
            Thread.sleep(2000);

        } catch (Exception e) {

            System.out.println("Trying to create ticket, got error:");
            System.out.println(e.getMessage());
        }
    }

}
