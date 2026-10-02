package com.practica2.practica2pom;

import java.util.List;
import java.util.Map;

public record ApiResponse(int statusCode, List<?> data) {
    public static ApiResponse ok(List<?> data) {
        return new ApiResponse(200, data);
    }
}