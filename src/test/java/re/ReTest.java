package re;

import org.junit.jupiter.api.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReTest {
    @Test
    void testRE() {
        String re = "(https?:/|\\.)?(/([\\w-]+(\\.)?)+)+(\\?(([\\w-]+(\\.)?)+=((/?([\\w-]+|[\\u4e00-\\u9fa5]+)+(\\.)?)+)?(&)?)+)?";
        String testString = "http://www.caai.cn/index.php?title=科学abc&s=/home/article/search.html&p=1";
        Pattern pattern = Pattern.compile(re);
        Matcher matcher = pattern.matcher(testString);
        System.out.println(matcher.lookingAt());
        if (matcher.lookingAt()) {
            int end = matcher.end();
            System.out.println(testString.substring(0, end));
        }
    }
}
