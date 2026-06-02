package com.Nexsta.Messaging.api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
@AllArgsConstructor
@Getter
public class ActivityMaps {

   private Map<String, List<String>> inChatMap;
   private  Map<String, List<String>> inInboxMap;

        public List<String> inChatIds()  { return inChatMap.values().stream().flatMap(List::stream).toList(); }
        public List<String> inInboxIds() { return inInboxMap.values().stream().flatMap(List::stream).toList(); }
        public List<String> allActiveIds() { return Stream.concat(inChatIds().stream(), inInboxIds().stream()).toList(); }
}
