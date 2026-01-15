package bg.svetozar.tastypizza.exception;

public final class ErrorMessage {

    private ErrorMessage() {}

    public static final String INVALID_USER_ID = "Invalid user ID";
    public static final String ORDER_NOT_FOUND = "Order not found";
    public static final String USER_NOT_FOUND = "User not found";
    public static final String PRODUCT_NOT_FOUND =  "Product not found";
    public static final String INVALID_ORDER_ID = "Invalid order ID";
    public static final String INVALID_STATUS_FILTER = "Invalid status filter";
    public static final String ORDER_ALREADY_DELIVERED = "Cannot cancel a delivered order";
    public static final String ORDER_ALREADY_CANCELLED =  "Order is already cancelled";
    public static final String ORDER_IS_CART = "Cannot cancel a cart";
    public static final String INVALID_STATUS_TRANSITION =  "Invalid status transition";
    public static final String ADMIN_CANNOT_CHANGE_OWN_ROLE = "You cannot change your own role.";
    public static final String ADMIN_CANNOT_DELETE_SELF = "You cannot delete your own account.";
    public static final String USER_IS_DELETED_CANNOT_MODIFIED = "User is deleted and cannot be modified.";
    public static final String ROLE_REQUIRED =  "Role is required.";
    public static final String INVALID_SHOW_FILTER = "Invalid show filter. Allowed: active, deleted, all.";
    public static final String USERNAME_ALREADY_TAKEN =  "Username is already taken";
    public static final String USER_DELETED =   "User is deleted";
    public static final String INVALID_REFRESH_TOKEN = "Invalid refresh token";
    public static final String INVALID_USERNAME_PASSWORD = "Invalid username or password";
    public static final String REQUIRED_REFRESH_TOKEN = "Refresh token is required";
    public static final String INVALID_PRODUCT_TYPE = "Invalid product type";
    public static final String PRODUCT_DELETED = "Product is deleted";
    public static final String ORDER_ITEM_NOT_FOUND = "Order item not found";
    public static final String GUEST_TOKEN_REQUIRED = "Guest token is required";

    public static final String POSITIVE_QUANTITY = "Quantity must be positive";
    public static final String CANNOT_MODIFY_ANOTHER_USER_CART = "You cannot modify another user's cart";
    public static final String CANNOT_MODIFY_USER_CART = "You cannot modify a user cart as guest";
    public static final String CANNOT_MODIFY_ANOTHER_GUEST_CART = "You cannot modify another guest cart";

    public static final String INGREDIENT_NOT_ALLOWED = "Ingredient is not allowed for this pizza";
    public static final String INGREDIENT_NOT_REMOVABLE = "Ingredient cannot be removed";
    public static final String INGREDIENT_IS_NOT_IN_RECIPE = "Ingredient is not in base recipe";
    public static final String INGREDIENT_DELETED = "Ingredient is deleted";
    public static final String INGREDIENTS_DO_NOT_EXIST = "Some ingredients do not exist";
public static final String INGREDIENT_CANNOT_BE_BOTH_ADDED_AND_REMOVED = "Ingredient cannot be both added and removed";


}