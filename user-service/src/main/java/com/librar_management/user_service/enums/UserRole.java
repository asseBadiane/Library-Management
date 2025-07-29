package com.librar_management.user_service.enums;

/**
 * User roles with specific permissions
 */
public enum UserRole {
    READER("Can search books, borrow/return items, view history"),
    LIBRARY_MANAGER("Can manage books, inventory, approve requests"),
    ADMIN("Full system access, user management, audit logs");
    
    private final String description;
    
    UserRole(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}