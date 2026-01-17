package bg.svetozar.tastypizza.exception;

import java.awt.*;

public final class ErrorMessage {

    private ErrorMessage() {}

    public static final String INVALID_USER_ID = "Invalid user ID ";
    public static final String ORDER_NOT_FOUND = "Order not found ";
    public static final String USER_NOT_FOUND = "User not found ";
    public static final String PRODUCT_NOT_FOUND =  "Product not found ";
    public static final String PRODUCT_NOT_FOUND_FOR_PIZZA = "Product not found for pizza";
    public static final String DRINK_NOT_FOUND = "Drink not found ";
    public static final String PIZZA_NOT_FOUND = "Pizza not found";
    public static final String ALLOWED_INGREDIENT_NOT_FOUND = "Allowed Ingredient not found";
    public static final String ALLOWED_INGREDIENT_NOT_FOUND_WITH_ID = "PizzaAllowedIngredient not found: ";
    public static final String PIZZA_NOT_FOUND_WITH_ID = "Pizza not found with id: ";
    public static final String INVALID_ORDER_ID = "Invalid order ID";
    public static final String INVALID_STATUS_FILTER = "Invalid status filter";
    public static final String ORDER_ALREADY_DELIVERED = "Cannot cancel a delivered order";
    public static final String ORDER_ALREADY_CANCELLED =  "Order is already cancelled";
    public static final String ORDER_IS_CART = "Cannot cancel a cart";
    public static final String INVALID_STATUS_TRANSITION =  "Invalid status transition";
    public static final String ADMIN_CANNOT_CHANGE_OWN_ROLE = "You cannot change your own role.";
    public static final String ADMIN_CANNOT_DELETE_SELF = "You cannot delete your own account.";
    public static final String USER_IS_DELETED_CANNOT_MODIFIED = "User is deleted and cannot be modified.";
    public static final String REQUIRED_ROLE =  "Role is required.";
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
    public static final String INGREDIENT_ALREADY_ALLOWED = "Ingredient is already allowed for this pizza";
    public static final String INGREDIENTS_DO_NOT_EXIST = "Some ingredients do not exist";
    public static final String INGREDIENT_CANNOT_BE_BOTH_ADDED_AND_REMOVED = "Ingredient cannot be both added and removed";
    public static final String PIZZA_ENTITY_NOT_FOUND_PRODUCT = "Pizza entity not found for product";
    public static final String PIZZA_VARIANT_NOT_FOUND = "Pizza variant not found";
    public static final String PIZZA_VARIANT_NOT_FOUND_FOR_PIZZA = "Variant does not belong to given pizza";
    public static final String ORDER_IS_NOT_CART = "Order is not a cart";
    public static final String REQUIRED_PHONE = "Phone is required";
    public static final String REQUIRED_ADDRESS = "Address is required";
    public static final String REQUIRED_AUTHENTICATION = "Authentication is required";
    public static final String CANNOT_CHECKOUT_EMPTY_CART = "Cannot checkout empty cart";
    public static final String ONLY_PIZZA_CHANGE_VARIANT = "Only pizza items can change variant";
    public static final String ONLY_PIZZA_CUSTOMIZATION = "Only pizza items support customizations";
    public static final String INGREDIENT_NOT_FOUND = "Ingredient not found ";
    public static final String INGREDIENT_NOT_FOUND_WITH_ID = "Ingredient not found with id: ";
    public static final String INGREDIENT_NOT_FOUND_OR_DELETE =  "Ingredient not found or deleted ";
    public static final String INGREDIENT_TYPE_NOT_FOUND = "Ingredient type not found ";
    public static final String INGREDIENT_TYPE_ALREADY_EXISTS = "Ingredient type already exists ";
    public static final String INGREDIENT_TYPE_IN_USE = "Ingredient type is already in use ";
    public static final String INGREDIENT_TYPE_NAME_CANNOT_BE_EMPTY = "Ingredient type name cannot be empty";
    public static final String INVALID_AUTHENTICATION_PRINCIPAL = "Invalid authentication principal";
    public static final String ORDER_NOT_ALLOWED_ACCESS = "Not allowed to access this order";
    public static final String CANNOT_REORDER_CART = "Cannot reorder a cart";
    public static final String REQUIRED_EXTRA_PRICE = "extraPrice is required";
    public static final String INVALID_EXTRA_PRICE = "Invalid extra price ";
    public static final String PIZZA_INGREDIENT_ALREADY_EXISTS = "Ingredient already exists in pizza base recipe";
    public static final String PIZZA_INGREDIENT_NOT_FOUND = "Ingredient not found";
    public static final String PIZZA_INGREDIENT_NOT_FOUND_WITH_ID = "Ingredient not found  with id: ";
    public static final String PIZZA_ALREADY_DELETED = "Pizza is already deleted";
    public static final String PIZZA_NOT_DELETED = "Pizza is not deleted";
    public static final String INVALID_PRICE = "Invalid price";
    public static final String INVALID_PRICE_MUST_BE_POSITIVE = "Price must be >= 0";
    public static final String INVALID_PRICE_FORMAT = "Invalid price format";
    public static final String INVALID_ENUM_VALUE = "Invalid enum value";
    public static final String INVALID_ENUM_VALUE_WITH_VALUE = "Invalid enum value: ";
    public static final String DUPLICATE_PIZZA_VARIANT = "Duplicate pizza variant (size+dough)";
    public static final String DUPLICATE_PIZZA_INGREDIENT = "Duplicate ingredient in base ingredients list";
    public static final String DUPLICATE_ALLOWED_INGREDIENT = "Duplicate ingredient in allowed ingredients list";
    public static final String INGREDIENT_CANNOT_BOTH_ALLOWED_AND_BASE = "Ingredient cannot be both base and allowed extra";
    public static final String BASE_PRICE_NOT_FOUND_FOR_PRODUCT = "Base price not found for product";
    public static final String PRODUCT_TYPE_NOT_FOUND = "Product type not found for product";
    public static final String REQUIRED_IMAGE = "Image is required";
    public static final String PRODUCT_ALREADY_DELETED = "Product is already deleted";
    public static final String PRODUCT_NOT_DELETED = "Product is not deleted";
    public static final String REQUIRED_PRICE =  "Price is required";
    public static final String NOT_AUTHENTICATED = "Not authenticated";
    public static final String INVALID_CURRENT_PASSWORD = "Invalid current password";
    public static final String USER_NOT_FOUND_OR_DELETED = "User not found or deleted. Username: ";


    public static final String REQUIRED_USERNAME = "Username is required";
    public static final String INVALID_USERNAME_BETWEEN_3_50_CHARS = "Username must be between 3 and 50 characters";
    public static final String INVALID_PASSWORD_BETWEEN_6_255_CHARS = "Password must be between 6 and 255 characters";
    public static final String REQUIRED_PASSWORD = "Password is required";
    public static final String REQUIRED_FULL_NAME = "Full name is required";
    public static final String INVALID_FULL_NAME_BETWEEN_2_100_CHARS = "Full  name must be between 2 and 100 characters";
    public static final String REQUIRED_NAME = "Name is required";
    public static final String INVALID_DRINK_NAME_BETWEEN_2_80_CHARS = "Name must be between 2 and 80 characters";
    public static final String INVALID_DRINK_DESCRIPTION_MAX_500_CHARS = "Description must be at most 500 characters";
    public static final String REQUIRED_DRINK_BASE_PRICE = "Base price is required";
    public static final String INVALID_DRINK_BASE_PRICE_2_DECIMALS = "Base price must be a valid number with up to 2 decimals";
    public static final String INVALID_INGREDIENT_NAME_BETWEEN_2_100_CHARS = "Ingredient name is required";
    public static final String REQUIRED_TYPE_ID = "Type id is required";
    public static final String INVALID_TYPE_ID_POSITIVE = "Type id must be positive";
    public static final String INVALID_TYPE_NAME_BETWEEN_2_50_CHARS = "Type name must be between 2 and 50 characters";

    public static final String REQUIRED_PRODUCT_ID = "Product id is required";
    public static final String INVALID_PRODUCT_ID_POSITIVE = "Product id must be positive";
    public static final String REQUIRED_QUANTITY = "Quantity is required";
    public static final String INVALID_QUANTITY_POSITIVE = "Quantity must be positive";
    public static final String INVALID_NOTE_MAX_300_CHARS = "Note max 300 characters";
    public static final String REQUIRED_VARIANT_ID = "Variant id is required";
    public static final String INVALID_VARIANT_ID_POSITIVE = "Variant id must be positive";
    public static final String INVALID_REMOVE_INGREDIENT_IDS_POSITIVE = "Remove ingredient id must be positive";
    public static final String INVALID_ADD_INGREDIENT_IDS_POSITIVE =  "Adding ingredient id must be positive";
    public static final String INVALID_PHONE_NUMBER_MAX_30_CHARS = "Phone number max 30 characters";
    public static final String INVALID_ADDRESS_MAX_300_CHARS = "Address max 300 characters";
    public static final String INVALID_PIZZA_DESCRIPTION_MAX_1000_CHARS = "Description must be at most 1000 characters";
    public static final String INVALID_PIZZA_NAME_BETWEEN_5_100_CHARS = "Pizza name must be between 5 and 100 characters";
    public static final String REQUIRED_SPICY_LEVEL = "Spicy level is required";
    public static final String INVALID_SPICY_LEVEL_MAX_20 = "Spicy level must be at most 20 characters";
    public static final String REQUIRED_VARIANTS =  "Variants are required";
    public static final String REQUIRED_PIZZA_INGREDIENTS = "Pizza ingredients are required";
    public static final String REQUIRED_ALLOWED_INGREDIENTS = "Allowed ingredients are required";
    public static final String REQUIRED_PIZZA_BASE_PRICE = "Base price is required";
    public static final String INVALID_PIZZA_BASE_PRICE_2_DECIMALS = "Base price must be a valid number with up to 2 decimals";

    public static final String REQUIRED_PIZZA_SIZE = "Pizza size is required";
    public static final String INVALID_PIZZA_SIZE_MAX_20_CHARS = "Pizza size must be at most 20 characters";
    public static final String REQUIRED_PIZZA_DOUGH =  "Pizza dough is required";
    public static final String INVALID_PIZZA_DOUGH_MAX_20_CHARS = "Pizza dough must be at most 20 characters";
    public static final String INVALID_EXTRA_PRICE_2_DECIMALS = "Extra price must be a valid number with up to 2 decimals";

    public static final String REQUIRED_INGREDIENT_ID = "Ingredient id is required";
    public static final String INVALID_INGREDIENT_ID_POSITIVE = "Ingredient id must be positive";

    public static final String REQUIRED_CURRENT_PASSWORD = "Current password is required";
    public static final String REQUIRED_NEW_PASSWORD = "New password is required";
    public static final String INVALID_NEW_PASSWORD_BETWEEN_6_255_CHARS = "New password must be between 6 and 255 characters";

    public static final String INVALID_ITEM_ID_POSITIVE = "Item id must be positive";
    public static final String INVALID_ID_POSITIVE = "Id must be positive";
    public static final String INVALID_PIZZA_ID_POSITIVE = "Pizza id must be positive";



}