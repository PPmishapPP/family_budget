package ru.mishapp.api;

import java.net.URI;

public final class ApiPaths {

    public static final String V1 = "/api/v1";

    private ApiPaths() {
    }

    public static URI paymentRule(Long accountId, Long ruleId) {
        return URI.create(V1 + "/accounts/" + accountId + "/payment-rules/" + ruleId);
    }

    public static URI media(Long accountId, Long mediaId) {
        return URI.create(V1 + "/accounts/" + accountId + "/media/" + mediaId);
    }
}
