package bg.svetozar.tastypizza.exception;

public final class ErrorCode {

    private ErrorCode() {}

    // IngredientType
    public static final String INGREDIENT_TYPE_NOT_FOUND = "INGREDIENT_TYPE_NOT_FOUND";
    public static final String INGREDIENT_TYPE_ALREADY_EXISTS = "INGREDIENT_TYPE_ALREADY_EXISTS";

    // Ingredient
    public static final String INGREDIENT_NOT_FOUND = "INGREDIENT_NOT_FOUND";

    // Drink
    public static final String DRINK_NOT_FOUND = "DRINK_NOT_FOUND";

    // Pizza / Product
    public static final String PIZZA_NOT_FOUND = "PIZZA_NOT_FOUND";
    public static final String PRODUCT_NOT_FOUND = "PRODUCT_NOT_FOUND";

    // Auth / User
    public static final String USERNAME_ALREADY_TAKEN = "USERNAME_ALREADY_TAKEN";
    public static final String USER_NOT_FOUND = "USER_NOT_FOUND";
    public static final String BAD_CREDENTIALS = "BAD_CREDENTIALS";

    // General
    public static final String BAD_REQUEST = "BAD_REQUEST";
    public static final String INTERNAL_ERROR = "INTERNAL_ERROR";
    public static final String UNAUTHORIZED = "UNAUTHORIZED";
    public static final String TYPE_IN_USE = "TYPE_IN_USE";
    public static final String NOT_FOUND = "NOT_FOUND";
    public static final String FORBIDDEN = "FORBIDDEN";
}
