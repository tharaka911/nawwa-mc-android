package lk.macna.nawwa_mc.network;

import lk.macna.nawwa_mc.BuildConfig;

/**
 * ApiConfig centralizes all API-related constants for the application.
 * Values are primarily driven by BuildConfig (populated from gradle.properties).
 */
public class ApiConfig {

    // Reads from gradle.properties (like a .env file)
    public static final String BASE_URL = BuildConfig.BASE_URL;
    
    public static final String API_BASE_URL = BASE_URL + "/api";

    // Endpoint paths
    public static final String LOGIN_URL = API_BASE_URL + "/users/login";
    public static final String REGISTER_URL = API_BASE_URL + "/users";
    public static final String FORGOT_PASSWORD_URL = API_BASE_URL + "/users/forgot-password";
    public static final String PROFILE_URL = API_BASE_URL + "/users/";
    
    public static final String CATEGORIES_URL = API_BASE_URL + "/categories";
    public static final String PRODUCTS_URL = API_BASE_URL + "/products";
    public static final String CARTS_URL = API_BASE_URL + "/carts";
    public static final String ORDERS_URL = API_BASE_URL + "/orders";

    // Media Types
    public static final String JSON_MEDIA_TYPE = "application/json; charset=utf-8";
}