package cn.bugstack.chatbot.api.test;


import cn.bugstack.chatbot.api.domain.ai.IOpenAI;
import cn.bugstack.chatbot.api.domain.zsxq.IZsxqApi;
import cn.bugstack.chatbot.api.domain.zsxq.model.aggregates.UnAnsweredQuestionsAggregates;
import cn.bugstack.chatbot.api.domain.zsxq.model.vo.Topics;
import com.alibaba.fastjson.JSON;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;


@SpringBootTest
@RunWith(SpringRunner.class)
public class SpringbootRunTest {

    private Logger logger = LoggerFactory.getLogger(SpringbootRunTest.class);

    @Value("${chatbot-api.groupId}")
    private String groupId;
    @Value("${chatbot-api.cookie}")
    private String cookie;

    @Resource
    private IZsxqApi zsxqApi;
    @Resource
    private IOpenAI openAI;

    @Test
    public void test_zsxqApi() throws IOException {
        UnAnsweredQuestionsAggregates unAnsweredQuestionsAggregates = zsxqApi.queryUnAnsweredQuestionsTopicId(groupId, cookie);
        logger.info("test result: {}", JSON.toJSONString(unAnsweredQuestionsAggregates));

        List<Topics > topics = unAnsweredQuestionsAggregates.getResp_data().getTopics();

        for (Topics topic : topics){
            String topicId = topic.getTopic_id();
            String text = topic.getTalk().getText();

            logger.info("topicId: {}, text: {}", topicId, text);

            zsxqApi.answer(groupId, cookie, topicId, text);
        }

    }

    @Test
    public void test_openAI() throws IOException {
        String question = "为什么爱尔兰总是下雨";
        String answer = openAI.doChatGPT(question);
        logger.info("test result: {}", answer);
    }
}
