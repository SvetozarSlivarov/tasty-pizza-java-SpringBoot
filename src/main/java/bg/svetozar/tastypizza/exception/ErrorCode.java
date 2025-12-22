package bg.svetozar.tastypizza.exception;

public final class ErrorCode {

    public static final String INVALID_SHOW_FILTER = "INVALID_SHOW_FILTER";
    public static final String CONFLICT = "CONFLICT";
    public static final String INVALID_ENUM_VALUE = "INVALID_ENUM_VALUE";


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
    public static final String BAD_CREDENTIALS = "BAD_CREDENTIALS";

    // General
    public static final String BAD_REQUEST = "BAD_REQUEST";
    public static final String INTERNAL_ERROR = "INTERNAL_ERROR";
    public static final String UNAUTHORIZED = "UNAUTHORIZED";
    public static final String TYPE_IN_USE = "TYPE_IN_USE";
    public static final String NOT_FOUND = "NOT_FOUND";
    public static final String FORBIDDEN = "FORBIDDEN";

    public static final String USER_NOT_FOUND = "USER_NOT_FOUND";
    public static final String ADMIN_CANNOT_CHANGE_OWN_ROLE = "ADMIN_CANNOT_CHANGE_OWN_ROLE";
    public static final String ADMIN_CANNOT_DELETE_SELF = "ADMIN_CANNOT_DELETE_SELF";

    public static final String INVALID_USER_ID = "INVALID_USER_ID";
    public static final String ROLE_REQUIRED = "ROLE_REQUIRED";

    public static final String USER_DELETED = "USER_DELETED";

    public static final String CONCURRENT_MODIFICATION = "CONCURRENT_MODIFICATION";

    public static final String CART_EMPTY = "CART_EMPTY";
    public static final String CART_FORBIDDEN = "CART_FORBIDDEN";

    // Order item
    public static final String ORDER_ITEM_NOT_FOUND = "ORDER_ITEM_NOT_FOUND";
    public static final String INVALID_ORDER_ITEM_ID = "INVALID_ORDER_ITEM_ID";

    // Product / Variant
    public static final String INVALID_PRODUCT_ID = "INVALID_PRODUCT_ID";
    public static final String PRODUCT_DELETED = "PRODUCT_DELETED";
    public static final String INVALID_PRODUCT_TYPE = "INVALID_PRODUCT_TYPE";

    public static final String VARIANT_REQUIRED = "VARIANT_REQUIRED";
    public static final String VARIANT_NOT_FOUND = "VARIANT_NOT_FOUND";
    public static final String VARIANT_NOT_BELONG_TO_PIZZA = "VARIANT_NOT_BELONG_TO_PIZZA";

    // Quantity / customization
    public static final String INVALID_QUANTITY = "INVALID_QUANTITY";
    public static final String INVALID_CUSTOMIZATION = "INVALID_CUSTOMIZATION";
    public static final String INGREDIENT_NOT_REMOVABLE = "INGREDIENT_NOT_REMOVABLE";

    // Guest
    public static final String GUEST_TOKEN_REQUIRED = "GUEST_TOKEN_REQUIRED";

    // Operation
    public static final String INVALID_OPERATION = "INVALID_OPERATION";

    public static final String ORDER_NOT_FOUND = "ORDER_NOT_FOUND";
    public static final String INVALID_ORDER_ID = "INVALID_ORDER_ID";
    public static final String ORDER_FORBIDDEN = "ORDER_FORBIDDEN";

    // Order status / transitions
    public static final String INVALID_STATUS_FILTER = "INVALID_STATUS_FILTER";
    public static final String INVALID_STATUS_TRANSITION = "INVALID_STATUS_TRANSITION";
    public static final String ORDER_ALREADY_CANCELLED = "ORDER_ALREADY_CANCELLED";
    public static final String ORDER_ALREADY_DELIVERED = "ORDER_ALREADY_DELIVERED";
    public static final String ORDER_IS_CART = "ORDER_IS_CART";

    // Reorder
    public static final String REORDER_NOT_ALLOWED = "REORDER_NOT_ALLOWED";
    public static final String REORDER_SOURCE_IS_CART = "REORDER_SOURCE_IS_CART";

    // PizzaAllowedIngredient
    public static final String PIZZA_ALLOWED_INGREDIENT_NOT_FOUND = "PIZZA_ALLOWED_INGREDIENT_NOT_FOUND";
    public static final String PIZZA_ALLOWED_INGREDIENT_ALREADY_EXISTS = "PIZZA_ALLOWED_INGREDIENT_ALREADY_EXISTS";
    public static final String INVALID_EXTRA_PRICE = "INVALID_EXTRA_PRICE";

    public static final String PIZZA_INGREDIENT_NOT_FOUND = "PIZZA_INGREDIENT_NOT_FOUND";
    public static final String PIZZA_INGREDIENT_ALREADY_EXISTS = "PIZZA_INGREDIENT_ALREADY_EXISTS";



}
