package io.sykq.wfplayground.model;

import lombok.*;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@With
@Builder
public class MiscResponse {

    private String id;
    private Map<String, Object> values;

}
