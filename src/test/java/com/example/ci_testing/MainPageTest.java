package com.example.ci_testing;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class MainPageTest {
    private static WebDriver driver;
    private static String email;
    private static String password;

    @BeforeAll
    public static void createUser() {
        createSessionAndOpenSite();

        driver.findElement(By.xpath("//a[@href = '/login']")).click();
        driver.findElement(By.xpath("//input[@value = 'Register']")).click();

        email = UUID.randomUUID() + "@mail.com";
        password = UUID.randomUUID().toString();

        driver.findElement(By.id("gender-male")).click();
        driver.findElement(By.id("FirstName")).sendKeys("Petras");
        driver.findElement(By.id("LastName")).sendKeys("Petraitis");
        driver.findElement(By.id("Email")).sendKeys(email);
        driver.findElement(By.id("Password")).sendKeys(password);
        driver.findElement(By.id("ConfirmPassword")).sendKeys(password);
        driver.findElement(By.xpath("//input[@value = 'Register']")).click();

        driver.quit();
    }

    @BeforeEach
    public void setUp() {
        createSessionAndOpenSite();
    }

    @AfterEach
    public void tearDown() {
        driver.quit();
    }

    @Test
    public void test1() {
        performBuyShoppingListFlow("data1.txt");
    }

    @Test
    public void test2() {
        performBuyShoppingListFlow("data2.txt");
    }

    private void performBuyShoppingListFlow(String dataFileName) {
        // LOGIN
        driver.findElement(By.xpath("//a[@href = '/login']")).click();
        driver.findElement(By.id("Email")).sendKeys(email);
        driver.findElement(By.id("Password")).sendKeys(password);
        driver.findElement(By.xpath("//input[@value = 'Log in']")).click();


        // NAVIGATE TO DIGITAL DOWNLOADS
        driver.findElement(By.xpath("//a[@href = '/digital-downloads']")).click();


        // ADD SHOPPING LIST ITEMS TO SHOPPING CART
        List<String> items = readShoppingList(dataFileName);
        for (String item: items) {
            addItemToShoppingCart(item);
        }


        // GO TO SHOPPING CART AND PRESS CHECKOUT
        driver.findElement(By.xpath("//a[@href='/cart']/span[text()='Shopping cart']")).click();
        driver.findElement(By.xpath("//input[@id='termsofservice']")).click();
        driver.findElement(By.xpath("//button[@id='checkout']")).click();


        // PERFORM CHECKOUT FLOW
        WebDriverWait wait = new WebDriverWait(driver, 5);


        // Billing address
        if (driver.findElements(By.xpath("//label[@for = 'billing-address-select']")).isEmpty()) {
            new Select(driver.findElement(By.xpath("//select[@id='BillingNewAddress_CountryId']"))).selectByVisibleText("Canada");
            driver.findElement(By.xpath("//input[@id='BillingNewAddress_City']")).sendKeys("City");
            driver.findElement(By.xpath("//input[@id='BillingNewAddress_Address1']")).sendKeys("Address1");
            driver.findElement(By.xpath("//input[@id='BillingNewAddress_ZipPostalCode']")).sendKeys("Zip");
            driver.findElement(By.xpath("//input[@id='BillingNewAddress_PhoneNumber']")).sendKeys("123456789");
        }
        driver.findElement(By.xpath("//div[@id='billing-buttons-container']/input")).click();
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.xpath("//div[@id='billing-buttons-container' and @disabled = 'disabled']")));

        // Payment method
        driver.findElement(By.xpath("//div[@id='payment-method-buttons-container']/input")).click();
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.xpath("//div[@id='payment-method-buttons-container' and @disabled = 'disabled']")));

        // Payment info
        driver.findElement(By.xpath("//div[@id='payment-info-buttons-container']/input")).click();
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.xpath("//div[@id='payment-info-buttons-container' and @disabled = 'disabled']")));

        // Confirm order
        driver.findElement(By.xpath("//div[@id='confirm-order-buttons-container']/input")).click();


        // ASSERT THAT ORDER IS SUCCESSFUL
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[text() = 'Your order has been successfully processed!']")));
        Assertions.assertNotNull(driver.findElement(By.xpath("//*[text() = 'Your order has been successfully processed!']")));
    }

    private void addItemToShoppingCart(String text) {
        int currentSize = Integer.parseInt(driver.findElement(By.xpath("//span[@class='cart-qty']")).getText().replace("(", "").replace(")", ""));

        String xpath = String.format("//a[text() = '%s']/../following-sibling::div[@class='add-info']/descendant::input[@value='Add to cart']", text);
        driver.findElement(By.xpath(xpath)).click();

        String expectedCartQuantityXpath = String.format("//span[@class='cart-qty' and text() = '(%s)']", currentSize + 1);
        WebDriverWait wait = new WebDriverWait(driver, 5);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(expectedCartQuantityXpath)));
    }

    private List<String> readShoppingList(String fileName) {
        List<String> lines;
        try {
            lines = Files.readAllLines(Path.of("./test_data/" + fileName));
        } catch (Exception e) {
            System.out.print(e.getMessage());
            e.printStackTrace();
            lines = null;
        }

        return lines;
    }

    private static void createSessionAndOpenSite() {
        System.setProperty("webdriver.chrome.driver", "./chromedriver");
        driver = new ChromeDriver();
//        driver.manage().window().maximize();
        driver.get("https://demowebshop.tricentis.com/");
    }
}
