package services;

import com.aventstack.extentreports.markuputils.MarkupHelper;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.json.simple.parser.ParseException;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import util.TestBase;
import util.TestUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static io.restassured.RestAssured.given;

public class login extends TestBase {

    private String loginResourceUrl;
    private JsonObject validLoginBody;
    private JsonObject emptyPasswordBody;
    private JsonObject emptyEmailBody;
    private JsonObject emptyFieldsBody;
    private JsonObject invalidEmailBody;
    private JsonObject invalidPasswordBody;
    private String token;

    @Parameters({"environment"})
    @BeforeMethod
    public void init(String environment) throws JsonSyntaxException, IOException, ParseException {
        RestAssured.baseURI = baseUrl;
        String path = "src/test/resources/" + environment + "/login.json";

        JsonObject config = new Gson().fromJson(new String(Files.readAllBytes(Paths.get(path))), JsonObject.class);
        loginResourceUrl = (String) config.get("endpoint").getAsString();
        validLoginBody = config.get("Valid_Login").getAsJsonObject();
        emptyEmailBody = config.get("Empty_Email").getAsJsonObject();
        emptyPasswordBody = config.get("Empty_Password").getAsJsonObject();
        emptyFieldsBody = config.get("Empty_Fields").getAsJsonObject();
        invalidEmailBody = config.get("Invalid_Email").getAsJsonObject();
        invalidPasswordBody = config.get("Invalid_Password").getAsJsonObject();
        token = (String) config.get("token").getAsString();
    }

    @Parameters({"environment"})
    public void checkLogin(int statusCode, String description,
                           String testReason, String requestBody) throws IOException, ParseException {

        TestUtils.testTitle("ENDPOINT");
        testInfo.get().info(baseUrl + loginResourceUrl);

        TestUtils.testTitle("Test");
        testInfo.get().info("<b>Login Request For: </b>" + testReason);

        Response res = given()
                .header("Content-Type", "application/json")
                .header("Connection", "keep-alive")
                .body(requestBody).when().post(loginResourceUrl).then().extract().response();

        TestUtils.testTitle("Request Body");
        String request = requestBody.toString();
        testInfo.get().info(MarkupHelper.createCodeBlock(request));

        TestUtils.testTitle("Response Body");
        String response = res.asString();
        testInfo.get().info(MarkupHelper.createCodeBlock(response));

        int sCode = res.getStatusCode();
        Assert.assertEquals(sCode, statusCode);
        JsonPath jsonRes = new JsonPath(response);

        if (sCode != 200) {
            Assert.assertTrue(response.contains(description));
        } else {
            String statusBody = jsonRes.getString("token");
            Assert.assertEquals(statusBody, token);
        }


    }

    //Login Tests
    @Test
    public void successfulLogin() throws IOException, ParseException {
        checkLogin(200,token, "Valid Credentials", validLoginBody.toString());
    }

    @Test
    public void invalidEmailLogin() throws IOException, ParseException {
        checkLogin(400,"user not found", "Invalid Email", invalidEmailBody.toString());
    }

    @Test
    public void invalidPasswordLogin() throws IOException, ParseException {
        checkLogin(400,"Missing password", "Invalid Password", invalidPasswordBody.toString());
    }

    @Test
    public void noEmailLogin() throws IOException, ParseException {
        checkLogin(400,"Missing email or username", "No Email", emptyEmailBody.toString());
    }

    @Test
    public void noPasswordLogin() throws IOException, ParseException {
        checkLogin(400,"Missing password", "No Password", emptyPasswordBody.toString());
    }

    @Test
    public void noFieldsLogin() throws IOException, ParseException {
        checkLogin(400,"Missing email or username", "Empty Fields", emptyFieldsBody.toString());
    }
}