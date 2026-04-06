package com.channyanh.channyanhweb.common.responses;

import com.google.gson.JsonElement;
import lombok.Data;

@Data
public class GenericApiResponse {
    int code;
    String type;
    String status;
    String message;
    JsonElement data;
}
