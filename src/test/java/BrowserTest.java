
import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.ie.InternetExplorerOptions;
import org.openqa.selenium.remote.BrowserType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.safari.SafariOptions;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

import static org.junit.Assert.assertTrue;


public class BrowserTest {
    final private static String ADVERTISER_URL = "https://p64ya2x0wl.execute-api.us-east-2.amazonaws.com/";
    final private static String PUBLISHER_URL = "https://1svkujd1fk.execute-api.us-east-2.amazonaws.com/";
    final private static String REPORT_URL = PUBLISHER_URL + "report";
    final private static String SAUCE_LABS_APPLICATION_NAME = "3rdPartyCookieTest";
    final private static String ITEM_ID = "test item id";

    private static Capabilities getSauceOptions() {
        final String sauceUserName = System.getenv("SAUCE_USERNAME");
        final String sauceAccessKey = System.getenv("SAUCE_ACCESS_KEY");

        final MutableCapabilities sauceOptions = new MutableCapabilities();
        sauceOptions.setCapability("username", sauceUserName);
        sauceOptions.setCapability("accessKey", sauceAccessKey);
        sauceOptions.setCapability("name", SAUCE_LABS_APPLICATION_NAME);
        return sauceOptions;
    }

    private static Capabilities getLatestChromeCapabilities() {
        final ChromeOptions browserOptions = new ChromeOptions();
        browserOptions.setExperimentalOption("w3c", true);
        browserOptions.setCapability("platformName", "Windows 10");
        browserOptions.setCapability("browserVersion", "latest");
        browserOptions.setCapability("sauce:options", getSauceOptions());
        return browserOptions;
    }

    private static Capabilities getOldestChromeCapabilities() {
        final DesiredCapabilities browserOptions = new DesiredCapabilities(BrowserType.CHROME, "50.0", Platform.WIN10);
        browserOptions.setCapability("sauce:options", getSauceOptions());
        return browserOptions;
    }

    private static Capabilities getLatestEdgeCapabilities() {
        final EdgeOptions browserOptions = new EdgeOptions();
        browserOptions.setCapability("platformName", "Windows 10");
        browserOptions.setCapability("browserVersion", "latest");
        browserOptions.setCapability("sauce:options", getSauceOptions());
        return browserOptions;
    }

    private static Capabilities getOldestEdgeCapabilities() {
        final EdgeOptions browserOptions = new EdgeOptions();
        browserOptions.setCapability("platformName", "Windows 10");
        browserOptions.setCapability("browserVersion", "15.15063");
        browserOptions.setCapability("sauce:options", getSauceOptions());
        return browserOptions;
    }

    private static Capabilities getLatestFirefoxCapabilities() {
        final FirefoxOptions browserOptions = new FirefoxOptions();
        browserOptions.setCapability("platformName", "Windows 10");
        browserOptions.setCapability("browserVersion", "latest");
        browserOptions.setCapability("sauce:options", getSauceOptions());
        return browserOptions;
    }

    private static Capabilities getOldestFirefoxCapabilities() {
        final FirefoxOptions browserOptions = new FirefoxOptions();
        browserOptions.setCapability("platformName", "Windows 10");
        browserOptions.setCapability("browserVersion", "60.0");
        browserOptions.setCapability("sauce:options", getSauceOptions());
        return browserOptions;
    }

    private static Capabilities getLatestInternetExplorerCapabilities() {
        final InternetExplorerOptions browserOptions = new InternetExplorerOptions();
        browserOptions.setCapability("platformName", "Windows 10");
        browserOptions.setCapability("browserVersion", "latest");
        browserOptions.setCapability("sauce:options", getSauceOptions());
        return browserOptions;
    }

    private static Capabilities getOldestInternetExplorerCapabilities() {
        final DesiredCapabilities browserOptions = new DesiredCapabilities(BrowserType.IE, "9.0", Platform.VISTA);
        browserOptions.setCapability("sauce:options", getSauceOptions());
        return browserOptions;
    }

    private static Capabilities getLatestSafariCapabilities() {
        final SafariOptions browserOptions = new SafariOptions();
        browserOptions.setCapability("platformName", "macOS 11.00");
        browserOptions.setCapability("browserVersion", "latest");
        browserOptions.setCapability("sauce:options", getSauceOptions());
        return browserOptions;
    }

    // safari < 12 failed to launch VM probably because of the incompatible Selenium version
    private static Capabilities getOldestSafariCapabilities() {
        final SafariOptions browserOptions = new SafariOptions();
        browserOptions.setCapability("platformName", "macOS 10.14");
        browserOptions.setCapability("browserVersion", "12.0");
        browserOptions.setCapability("sauce:options", getSauceOptions());
        return browserOptions;
    }

    private static WebDriver setUpWebDriver() throws MalformedURLException {
        final String sauceURL = "https://ondemand.saucelabs.com/wd/hub";
        WebDriver driver = new RemoteWebDriver(new URL(sauceURL), getLatestSafariCapabilities());
        return driver;
    };

    @Test
    public void test() throws IOException, InterruptedException, JSONException {
        WebDriver driver = setUpWebDriver();

        try {
            synchronized (driver) {
                final String uid = visitPublisherAndGetUid(driver);
                visitAdvertiserAndMakeAnConversion(driver, ITEM_ID);

                // Let Sauce Labs know that the selenium testing finished successfully
                ((JavascriptExecutor) driver).executeScript("sauce:job-result=passed");

                assertTrue(verifyConversionInReportPage(uid, ITEM_ID));
            }
        } finally {
            driver.quit();
        }
    }

    private static String visitPublisherAndGetUid(final WebDriver driver) throws InterruptedException {
        driver.navigate().to(PUBLISHER_URL);
        driver.wait(1000);
        final Optional<Cookie> maybeCtk = getCookies(driver).stream()
                .filter(cookie -> cookie.getName().equals("uid")).findFirst();
        return maybeCtk.get().getValue();
    }

    private static void visitAdvertiserAndMakeAnConversion(final WebDriver driver, final String itemId) throws InterruptedException {
        driver.navigate().to(ADVERTISER_URL);
        ((JavascriptExecutor) driver).executeScript("document.querySelector('#conversion-item').value='" + itemId + "';");
        ((JavascriptExecutor) driver).executeScript("document.querySelector('#conversion-button').click();");
        driver.wait(1000);
    }

    private boolean verifyConversionInReportPage(final String uid, final String itemId) throws IOException, JSONException {
        JSONObject json = JsonReader.readJsonFromUrl(REPORT_URL);
        System.out.println(json.toString());
        final JSONArray conversions = json.getJSONArray("conversions");
        for (int i = 0; i < conversions.length(); i++) {
            final JSONObject conversion = conversions.getJSONObject(i);
            if (conversion.getString("uid").equals(uid) && conversion.getString("item").equals(itemId)) {
                System.out.println("Found corresponding conversion: " + conversion.toString());
                return true;
            }
        }
        return false;
    }

    private static Set<Cookie> getCookies(final WebDriver driver) {
        // Cookie.equals only check key and value. Therefore we can use Set to dedup
        final Set<Cookie> cookies = new HashSet<>();
        cookies.addAll(getCookiesFromDriver(driver));
        cookies.addAll(getCookiesFromJs(driver));
        return cookies;
    }

    private static Set<Cookie> getCookiesFromDriver(final WebDriver driver) {
        return driver.manage().getCookies();
    }

    // In IE, driver.manage().getCookies() returns empty result.
    // To avoid that, we can execute JS to get cookies. Note that it can't get http-only cookies.
    private static Set<Cookie> getCookiesFromJs(final WebDriver driver) {
        final String cookies = (String) ((JavascriptExecutor) driver).executeScript("return document.cookie");
        return parseBrowserCookies(cookies);
    }

    private static Set<Cookie> parseBrowserCookies(final String cookiesString) {
        final Set<Cookie> cookies = new HashSet<>();
        if (StringUtils.isBlank(cookiesString)) {
            return cookies;
        }
        Arrays.asList(cookiesString.split("; ")).forEach(cookie -> {
            final String[] splitCookie = cookie.split("=", 2);
            cookies.add(new Cookie(splitCookie[0], splitCookie[1], "/"));
        });
        return cookies;
    }
}
