// src/main/java/org/example/ChatGptClient.java
package org.example;

import java.util.List;

public interface ChatGptClient {


    List<Question> generate(String topic, int questionsCount);

}
