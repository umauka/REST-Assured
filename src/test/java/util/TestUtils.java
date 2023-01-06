package util;

import com.aventstack.extentreports.markuputils.ExtentColor;
import com.aventstack.extentreports.markuputils.Markup;
import com.aventstack.extentreports.markuputils.MarkupHelper;

public class TestUtils extends TestBase{

    public static void testTitle(String phrase) {
        String word = "<b>"+phrase+"</b>";
        Markup w = MarkupHelper.createLabel(word, ExtentColor.BLUE);
        testInfo.get().info(w);
    }

}
