/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.component.langchain4j.extract;

import java.io.IOException;
import java.time.LocalDate;

import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import kotlin.text.Charsets;
import org.apache.camel.builder.RouteBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

import static org.apache.commons.io.IOUtils.resourceToString;

@DisabledIfSystemProperty(named = "ci.env.name", matches = ".*", disabledReason = "Requires too much network resources")
public class LangChain4jExtractIT extends OllamaTestSupport {

    /**
     * The customer birthday date format need to be forced to comply with what langchain4j gson parser need.
     */
    static final String CUSTOM_POJO_EXTRACT_PROMPT
            = "Extract information about a customer from the text delimited by triple backticks: ```{{text}}```."
              + "The customerBirthday field should be formatted as YYYY-MM-DD."
              + "The summary field should concisely relate the customer main ask.";

    static class CustomPojo {
        private boolean customerSatisfied;
        private String customerName;
        private LocalDate customerBirthday;
        private String summary;
    }

    interface CamelCustomPojoExtractor {
        @UserMessage(CUSTOM_POJO_EXTRACT_PROMPT)
        CustomPojo extractFromText(@V("text") String text);
    }

    @Override
    protected RouteBuilder createRouteBuilder() {
        this.context.getRegistry().bind("chatModel", chatLanguageModel);

        CamelCustomPojoExtractor extractionService = AiServices.create(CamelCustomPojoExtractor.class, chatLanguageModel);
        this.context.getRegistry().bind("extractionService", extractionService);

        return new RouteBuilder() {
            public void configure() {
                from("direct:send-simple-message")
                        .bean(extractionService)
                        .to("mock:response");
            }
        };
    }

    @Test
    void testSendMessage() throws InterruptedException, IOException {

        String[] conversationResourceNames = {
                "01_sarah-london-10-07-1986-satisfied.txt", "02_john-doe-01-11-2001-unsatisfied.txt",
                "03_kate-boss-13-08-1999-satisfied.txt" };

        //String[] conversationResourceNames = { "01_sarah-london-10-07-1986-satisfied.txt" };

        for (String conversationResourceName : conversationResourceNames) {
            String conversation = resourceToString(String.format("/texts/%s", conversationResourceName), Charsets.UTF_8);

            long begin = System.currentTimeMillis();
            CustomPojo answer = template.requestBody("direct:send-simple-message", conversation, CustomPojo.class);
            long duration = System.currentTimeMillis() - begin;

            System.out.println(toPrettyFormat(answer));
            System.out.println(String.format("----- Inference lasted %.1fs ------------------------------", duration / 1000.0));
        }

        /*
        MockEndpoint mockEndpoint = this.context.getEndpoint("mock:response", MockEndpoint.class);
        mockEndpoint.expectedMessageCount(1);
        */

        //mockEndpoint.assertIsSatisfied();
    }

    private final static String FORMAT = "****************************************\n"
                                         + "customerSatisfied: %s\n"
                                         + "customerName: %s\n"
                                         + "customerBirthday: %td %tB %tY\n"
                                         + "summary: %s\n"
                                         + "****************************************\n";

    public static String toPrettyFormat(CustomPojo extract) {
        return String.format(FORMAT, extract.customerSatisfied, extract.customerName, extract.customerBirthday,
                extract.customerBirthday, extract.customerBirthday, extract.summary);
    }

}
