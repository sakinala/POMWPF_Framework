package com.automationtesting.basetest;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;

import com.automationtesting.util.ExcelApiTest;
import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.markuputils.ExtentColor;
import com.aventstack.extentreports.markuputils.MarkupHelper;
import com.aventstack.extentreports.reporter.ExtentHtmlReporter;
import com.aventstack.extentreports.reporter.configuration.ChartLocation;
import com.aventstack.extentreports.reporter.configuration.Theme;

public class BaseTest {

	public WebDriver driver;
	public ExtentHtmlReporter htmlReporter;
	public ExtentReports extent;
	public ExtentTest test;

	public ExcelApiTest eat = null;

	public FileInputStream configFis = null;
	public Properties configProp = null;

	public Properties envProp = null;
	public FileInputStream envFis = null;

	@BeforeSuite
	public void init() throws Exception {

		eat = new ExcelApiTest(
				System.getProperty("user.dir") + "/src/main/java/com/automationtesting/repo/TestData.xlsx");

		configFis = new FileInputStream(
				"/Users/krishnasakinala/hubiC/workspace/POMWPF_Framework/src/main/java/com/automationtesting/repo/config.properties");

		configProp = new Properties();
		configProp.load(configFis);

		if (System.getProperty("pomBrowser") != null && System.getProperty("pomEnvironment") != null) {
			System.out.println("POM Browser is: " + System.getProperty("pomBrowser"));
			System.out.println("POM Environment is: " + System.getProperty("pomEnvironment"));

			configProp.setProperty("browser", System.getProperty("pomBrowser"));
			configProp.setProperty("environment", System.getProperty("pomEnvironment"));
		}

		if (configProp.getProperty("environment").equals("dev")) {
			envFis = new FileInputStream(
					"/Users/krishnasakinala/hubiC/workspace/POMWPF_Framework/src/main/java/com/automationtesting/repo/devconfig.properties");
			envProp = new Properties();
			envProp.load(envFis);
		}

		if (configProp.getProperty("environment").equals("qa")) {
			envFis = new FileInputStream(
					"/Users/krishnasakinala/hubiC/workspace/POMWPF_Framework/src/main/java/com/automationtesting/repo/qaconfig.properties");
			envProp = new Properties();
			envProp.load(envFis);
		}

		if (extent == null) {
			htmlReporter = new ExtentHtmlReporter(
					System.getProperty("user.dir") + "/test-output/AutomationTestingSiteReport.html");
			extent = new ExtentReports();
			extent.attachReporter(htmlReporter);

			extent.setSystemInfo("OS", "Mac Sierra");
			extent.setSystemInfo("Host Name", "Krishna");
			extent.setSystemInfo("Environment", "QA");
			extent.setSystemInfo("User Name", "Krishna Sakinala");

			htmlReporter.config().setChartVisibilityOnOpen(true);
			htmlReporter.config().setDocumentTitle("AutomationTesting.in Demo Report");
			htmlReporter.config().setReportName("My Own Report");
			htmlReporter.config().setTestViewChartLocation(ChartLocation.TOP);
			htmlReporter.config().setTheme(Theme.DARK);
		}

		if (driver == null) {

			if (configProp.get("browser").equals("chrome")) {
				System.setProperty("webdriver.chrome.driver", "/KRISHNA VOLUME/drivers/chromedriver");
				driver = new ChromeDriver();
			} else if (configProp.get("browser").equals("firefox")) {
				System.setProperty("webdriver.gecko.driver", "/KRISHNA VOLUME/drivers/geckodriver");
				driver = new FirefoxDriver();
			}
			driver.get(envProp.getProperty("siteUrl"));
			driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
		}
	}

	public void logIntoReport(String logInfo) {
		test.info(MarkupHelper.createLabel(logInfo, ExtentColor.BLUE));
	}

	@AfterMethod
	public void getResult(ITestResult result) throws IOException {
		if (result.getStatus() == ITestResult.FAILURE) {
			String screenShotPath = capture(driver, "screenShotName");
			test.fail(MarkupHelper.createLabel(result.getName() + " Test case FAILED due to below issues:",
					ExtentColor.RED));
			test.fail(result.getThrowable());
			test.fail("Snapshot below: " + test.addScreenCaptureFromPath(screenShotPath));
		} else if (result.getStatus() == ITestResult.SUCCESS) {
			test.pass(MarkupHelper.createLabel(result.getName() + " Test Case PASSED", ExtentColor.GREEN));
		} else {
			test.skip(MarkupHelper.createLabel(result.getName() + " Test Case SKIPPED", ExtentColor.ORANGE));
			test.skip(result.getThrowable());
		}
	}

	public void verifyTitle(String title) {
		Assert.assertEquals(driver.getTitle(), title);
	}

	public static String capture(WebDriver driver, String screenShotName) throws IOException {
		TakesScreenshot ts = (TakesScreenshot) driver;
		File source = ts.getScreenshotAs(OutputType.FILE);
		String dest = System.getProperty("user.dir") + "/ErrorScreenshots/" + screenShotName + getCurrentDateTime()
				+ ".png";
		File destination = new File(dest);
		FileUtils.copyFile(source, destination);

		return dest;
	}

	public static String getCurrentDateTime() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		Date date = new Date();

		return sdf.format(date);
	}

	@AfterSuite
	public void tearDown() {
		extent.flush();
		driver.quit();
	}
}
