package io.perfana.client.domain;

import lombok.*;

@Value
@Builder
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class Init {
    String systemUnderTest;
    String testEnvironment;
    String workload;
}
