package util;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;

import org.json.simple.parser.ParseException;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Parameters;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentHtmlReporter;


public class TestBase {

    public static ExtentReports reports;
    public static ExtentHtmlReporter htmlReporter;
    private static ThreadLocal<ExtentTest> parentTest = new ThreadLocal<ExtentTest>();
    public static ThreadLocal<ExtentTest> testInfo = new ThreadLocal<ExtentTest>();
    public static String baseUrl;



    @BeforeSuite
    @Parameters({"testReport", "environment"})
    public void setUp(String testReport, String environment) throws IOException, ParseException {
        baseUrl = "https://reqres.in";
        htmlReporter = new ExtentHtmlReporter(new File(System.getProperty("user.dir") + testReport));
        reports = new ExtentReports();
        reports.attachReporter(htmlReporter);
    }

    @BeforeClass
    @Parameters({"url"})
    public synchronized void beforeClass(String url, ITestContext testContext) {
        ExtentTest parent = reports.createTest(getClass().getName());
        parentTest.set(parent);
        baseUrl = System.getProperty("instance-url", url);
        reports.setSystemInfo(testContext.getName()+" Test Environment", baseUrl);

    }
    @BeforeMethod
    public synchronized void beforeMethod(Method method) {
        ExtentTest child = ((ExtentTest) parentTest.get()).createNode(method.getName());
        testInfo.set(child);
    }

    @AfterMethod
    public synchronized void afterMethod(ITestResult result) {

        for (String group : result.getMethod().getGroups())
            testInfo.get().assignCategory(group);

        if (result.getStatus() == ITestResult.FAILURE)
            testInfo.get().fail(result.getThrowable());
        else if (result.getStatus() == ITestResult.SKIP)
            testInfo.get().skip(result.getThrowable());
        else
            testInfo.get().pass("Test Completed");

        reports.flush();
    }

}
