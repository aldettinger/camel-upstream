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

import static org.apache.camel.component.langchain4j.extract.LangChain4jExtract.SCHEME;

import java.util.Map;

import org.apache.camel.Category;
import org.apache.camel.Consumer;
import org.apache.camel.Processor;
import org.apache.camel.Producer;
import org.apache.camel.spi.Metadata;
import org.apache.camel.spi.UriEndpoint;
import org.apache.camel.spi.UriParam;
import org.apache.camel.spi.UriPath;
import org.apache.camel.support.DefaultEndpoint;

@UriEndpoint(firstVersion = "4.5.0", scheme = SCHEME,
             title = "langChain4j Extract",
             syntax = "langchain4j-extract:chatId", producerOnly = true,
             category = { Category.AI }, headersClass = LangChain4jExtract.Headers.class)
public class LangChain4jExtractEndpoint extends DefaultEndpoint {

    @Metadata(required = true)
    @UriPath(description = "The id")
    private final String chatId;

    @UriParam
    private LangChain4jExtractConfiguration configuration;

    @Metadata(label = "consumer")
    @UriParam(description = "Tool description")
    private String description;

    @Metadata(label = "consumer")
    @UriParam(description = "List of Tool parameters in the form of parameter.<name>=<type>", prefix = "parameter.",
              multiValue = true, enums = "string,integer,number,object,array,boolean,null")
    private Map<String, String> parameters;

    public LangChain4jExtractEndpoint(String uri, LangChain4jExtractComponent component, String chatId,
                                      LangChain4jExtractConfiguration configuration) {
        super(uri, component);
        this.chatId = chatId;
        this.configuration = configuration;
    }

    @Override
    public Producer createProducer() throws Exception {
        return new LangChain4jExtractProducer(this);
    }

    @Override
    public Consumer createConsumer(Processor processor) throws Exception {
        // TODO: align with other component
        throw new RuntimeException("The langchain4j-extract component does not support consumer");
    }

    /**
     * Chat ID
     *
     * @return
     */
    public String getChatId() {
        return chatId;
    }

    public LangChain4jExtractConfiguration getConfiguration() {
        return configuration;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }
}
