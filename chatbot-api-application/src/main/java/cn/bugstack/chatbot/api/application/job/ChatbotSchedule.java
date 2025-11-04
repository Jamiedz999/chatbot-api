package cn.bugstack.chatbot.api.application.job;


import cn.bugstack.chatbot.api.domain.ai.IOpenAI;
import cn.bugstack.chatbot.api.domain.zsxq.IZsxqApi;
import cn.bugstack.chatbot.api.domain.zsxq.model.aggregates.UnAnsweredQuestionsAggregates;
import cn.bugstack.chatbot.api.domain.zsxq.model.vo.Topics;
import com.alibaba.fastjson.JSON;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import javax.annotation.Resource;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Random;

@EnableScheduling
@Configuration
public class ChatbotSchedule {

    private Logger logger = LoggerFactory.getLogger(ChatbotSchedule.class);

    @Value("${chatbot-api.groupId}")
    private String groupId;
    @Value("${chatbot-api.cookie}")
    private String cookie;

    @Resource
    private IZsxqApi zsxqApi;
    @Resource
    private IOpenAI openAI;

    @Scheduled(cron = "0/5 * * * * ?")
    public void run() {
        try {
            if (new Random().nextBoolean()) {
                logger.info("random closing service");
                return;
            }

            GregorianCalendar calendar = new GregorianCalendar();
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            if(hour> 22|| hour < 7) {
                logger.info("I am off work");
                return;
            }

            //1 get questions
            UnAnsweredQuestionsAggregates unAnsweredQuestionsAggregates = zsxqApi.queryUnAnsweredQuestionsTopicId(groupId, cookie);
            logger.info("test result: {}", JSON.toJSONString(unAnsweredQuestionsAggregates));

            List<Topics> topics = unAnsweredQuestionsAggregates.getResp_data().getTopics();

            if(null == topics || topics.isEmpty()){
                logger.info("no unanswered questions this time");
                return;
            }
            // 2 ai answer
            Topics topic = topics.get(0);
            String answer =  openAI.doChatGPT(topic.getTalk().getText().trim());

            //3, reply to the talk
            Boolean status =  zsxqApi.answer(groupId, cookie, topic.getTopic_id(), answer);

            logger.info("id: {}, question: {}, answer: {}, status: {}", topic.getTopic_id(),  topic.getTalk().getText(), answer, status);


        } catch ( Exception e) {
            logger.info("auto answered failed", e);
        }
    }
}
