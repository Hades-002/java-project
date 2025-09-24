package app;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import db.DbConnection;

public class Function extends DbConnection {

    // ==========================
    // LOGIN
    // ==========================
    public String login(String usernameOrEmail, String password) {
        try {
            connect();

            String query = "SELECT u.first_name, u.last_name, u.role, a.password, a.user_id " +
                    "FROM users u " +
                    "JOIN accounts a ON u.user_id = a.user_id " +
                    "WHERE a.username = ? OR u.email = ?";
            prep = con.prepareStatement(query);
            prep.setString(1, usernameOrEmail);
            prep.setString(2, usernameOrEmail);

            ResultSet rs = prep.executeQuery();

            if (rs.next()) {
                String storedHash = rs.getString("password");
                int userId = rs.getInt("user_id");

                String inputHash = hashPassword(password);

                // Check if password matches SHA-256 hash
                if (storedHash.equals(inputHash)) {
                    String role = rs.getString("role");
                    System.out.println(" Welcome " + rs.getString("first_name") + " " +
                            rs.getString("last_name") + " (" + role + ")");
                    return role;
                }
                // Check if password matches old hashCode() or plain text
                else if (storedHash.equals(String.valueOf(password.hashCode())) || storedHash.equals(password)) {
                    // Upgrade password to SHA-256
                    String newHash = hashPassword(password);
                    String updateQuery = "UPDATE accounts SET password=? WHERE user_id=?";
                    PreparedStatement updatePrep = con.prepareStatement(updateQuery);
                    updatePrep.setString(1, newHash);
                    updatePrep.setInt(2, userId);
                    updatePrep.executeUpdate();

                    String role = rs.getString("role");
                    System.out.println(" Welcome " + rs.getString("first_name") + " " +
                            rs.getString("last_name") + " (" + role + ") [Password upgraded to SHA-256]");
                    return role;
                } else {
                    System.out.println(" Login failed. Invalid password.");
                    return null;
                }
            } else {
                System.out.println(" Login failed. Username or email not found.");
                return null;
            }
        } catch (SQLException e) {
            System.out.println(" Login error: " + e.getMessage());
            return null;
        }
    }

    // ==========================
    // LOGOUT
    // ==========================
    public void logout() {
        System.out.println("User logged out.");
    }

    // =====================================================
    // USER MANAGEMENT (Admin only)
    // =====================================================
    public void addUser(String firstName, String lastName, String email, String phoneNumber,
            String role, String username, String password) {
        try {
            connect();

            String checkQuery = "SELECT COUNT(*) AS count FROM users u " +
                    "JOIN accounts a ON u.user_id = a.user_id " +
                    "WHERE u.email = ? OR a.username = ?";
            prep = con.prepareStatement(checkQuery);
            prep.setString(1, email);
            prep.setString(2, username);
            ResultSet rsCheck = prep.executeQuery();
            if (rsCheck.next() && rsCheck.getInt("count") > 0) {
                System.out.println(" Email or username already exists!");
                con.close();
                return;
            }

            String queryUsers = "INSERT INTO users (first_name, last_name, email, phone_number, role) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement prepUsers = con.prepareStatement(queryUsers, PreparedStatement.RETURN_GENERATED_KEYS);
            prepUsers.setString(1, firstName);
            prepUsers.setString(2, lastName);
            prepUsers.setString(3, email);
            prepUsers.setString(4, phoneNumber);
            prepUsers.setString(5, role);
            prepUsers.executeUpdate();

            ResultSet rs = prepUsers.getGeneratedKeys();
            int userId = 0;
            if (rs.next()) {
                userId = rs.getInt(1);
            }

            String hashedPassword = hashPassword(password); // store SHA-256 hash

            String queryAccount = "INSERT INTO accounts (user_id, username, password, date_created) VALUES (?, ?, ?, NOW())";

            PreparedStatement prepAccount = con.prepareStatement(queryAccount);
            prepAccount.setInt(1, userId);
            prepAccount.setString(2, username);
            prepAccount.setString(3, hashedPassword);
            prepAccount.executeUpdate();

            System.out.println(" User added: " + firstName + " " + lastName + " | Username: " + username);

            con.close();
        } catch (SQLException e) {
            System.out.println("Add user error: " + e.getMessage());
        }
    }

    // ==========================
    // SHA-256 HASHING
    // ==========================
    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void displayUsers() {
        try {
            connect();
            String query = "SELECT * FROM users";
            prep = con.prepareStatement(query);
            ResultSet rs = prep.executeQuery();

            System.out.printf("%-3s | %-15s | %-15s | %-25s | %-10s | %-12s%n",
                    "ID", "First Name", "Last Name", "Email", "Role", "Phone");
            System.out.println("--------------------------------------------------------------------------------");

            while (rs.next()) {
                System.out.printf("%-3d | %-15s | %-15s | %-25s | %-10s | %-12s%n",
                        rs.getInt("user_id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("email"),
                        rs.getString("role"),
                        rs.getString("phone_number"));
            }
            con.close();
        } catch (SQLException e) {
            System.out.println(" Display users error: " + e.getMessage());
        }
    }

    public void updateUser(int userId, String newEmail, String newRole) {
        try {
            connect();
            String query = "UPDATE users SET email=?, role=? WHERE user_id=?";
            prep = con.prepareStatement(query);
            prep.setString(1, newEmail);
            prep.setString(2, newRole);
            prep.setInt(3, userId);

            int rows = prep.executeUpdate();
            if (rows > 0) {
                System.out.println(" Updated user ID " + userId);
            } else {
                System.out.println(" No user found with ID " + userId);
            }
            con.close();
        } catch (SQLException e) {
            System.out.println("Update user error: " + e.getMessage());
        }
    }

    public void deleteUser(int userId) {
        try {
            connect();
            String query = "DELETE FROM users WHERE user_id=?";
            prep = con.prepareStatement(query);
            prep.setInt(1, userId);

            int rows = prep.executeUpdate();
            if (rows > 0) {
                System.out.println(" Deleted user ID " + userId);
            } else {
                System.out.println(" No user found with ID " + userId);
            }
            con.close();
        } catch (SQLException e) {
            System.out.println(" Delete user error: " + e.getMessage());
        }
    }

    // =====================================================
    // COMPUTER PARTS MANAGEMENT
    // =====================================================
    public void addComputerParts(String name, String brand, int categoryId, int price, int stockQuantity) {
        try {
            connect();
            String query = "INSERT INTO computer_parts (name, brand, category_id, price, stock_quantity, archive) VALUES (?, ?, ?, ?, ?, 0)";
            prep = con.prepareStatement(query);
            prep.setString(1, name);
            prep.setString(2, brand);
            prep.setInt(3, categoryId);
            prep.setInt(4, price);
            prep.setInt(5, stockQuantity);

            prep.executeUpdate();
            System.out.println("Added computer part: " + name);
            con.close();
        } catch (SQLException e) {
            System.out.println(" Insert error: " + e.getMessage());
        }
    }

    public void displayComputerPartsRecord() {
        try {
            connect();
            String query = "SELECT * FROM computer_parts WHERE archive = 0";
            prep = con.prepareStatement(query);
            ResultSet rs = prep.executeQuery();

            // System.out.println(" Active Computer Parts:");
            System.out.printf("%-3s | %-25s | %-12s | %-12s | %-12s | %-12s%n",
                    "ID", "Name", "Brand", "Category", "Price", "Stock");
            System.out.println(
                    "--------------------------------------------------------------------------------------------");

            while (rs.next()) {
                int id = rs.getInt("part_id");
                String name = rs.getString("name");
                String brand = rs.getString("brand");
                int categoryId = rs.getInt("category_id");
                int price = rs.getInt("price");
                int stock = rs.getInt("stock_quantity");

                System.out.printf("%-3d | %-25s | %-12s | %-12d | %-12d | %-12d%n",
                        id, name, brand, categoryId, price, stock);
            }
            con.close();
        } catch (SQLException e) {
            System.out.println(" Read error: " + e.getMessage());
        }
    }

    public boolean partExists(int partId) {
        try {
            connect();
            String query = "SELECT part_id, name, brand, price, stock_quantity FROM computer_parts WHERE part_id = ? AND archive = 0";
            prep = con.prepareStatement(query);
            prep.setInt(1, partId);
            ResultSet rs = prep.executeQuery();

            if (rs.next()) {
                // Optionally, show the current details of the part
                System.out.println("\nCurrent Part Details:");
                System.out.printf("ID: %d | Name: %s | Brand: %s | Price: %d | Stock: %d%n",
                        rs.getInt("part_id"),
                        rs.getString("name"),
                        rs.getString("brand"),
                        rs.getInt("price"),
                        rs.getInt("stock_quantity"));
                con.close();
                return true;
            } else {
                System.out.println("\nWarning: No computer part found with ID " + partId);
                con.close();
                return false;
            }
        } catch (SQLException e) {
            System.out.println("Check part exists error: " + e.getMessage());
            return false;
        }
    }

    public void updateComputerPartsRecord(int partId, int newPrice, int newStock) {
        try {
            connect();
            String query = "UPDATE computer_parts SET price = ?, stock_quantity = ? WHERE part_id = ?";
            prep = con.prepareStatement(query);
            prep.setInt(1, newPrice);
            prep.setInt(2, newStock);
            prep.setInt(3, partId);

            int rows = prep.executeUpdate();
            if (rows > 0) {
                System.out.println(" Updated part ID " + partId);
            } else {
                System.out.println(" No part found with ID " + partId);
            }
            con.close();
        } catch (SQLException e) {
            System.out.println(" Update error: " + e.getMessage());
        }
    }

    public boolean archiveComputerPart(int partId, String deletedBy) {
        try {
            connect();

            // Mark as archived
            String query = "UPDATE computer_parts SET archive = 1, deleted_by = ? WHERE part_id = ?";
            prep = con.prepareStatement(query);
            prep.setString(1, deletedBy);
            prep.setInt(2, partId);

            int rows = prep.executeUpdate();
            con.close();

            if (rows > 0) {
                System.out.println("Part ID " + partId + " archived successfully by " + deletedBy + ".");
                return true;
            } else {
                return false;
            }
        } catch (SQLException e) {
            System.out.println("Archive error: " + e.getMessage());
            return false;
        }
    }

    public void deleteComputerPartsRecord(int partId) {
        try {
            connect();
            String query = "DELETE FROM computer_parts WHERE part_id = ?";
            prep = con.prepareStatement(query);
            prep.setInt(1, partId);

            int rows = prep.executeUpdate();
            if (rows > 0) {
                System.out.println(" Deleted part ID " + partId);
            } else {
                System.out.println(" No part found with ID " + partId);
            }
            con.close();
        } catch (SQLException e) {
            System.out.println(" Delete error: " + e.getMessage());
        }
    }

    // =====================================================
    // SEARCH PARTS
    // =====================================================
    public void searchComputerPartsRecord(String searchName) {
        try {
            connect();
            String query = "SELECT * FROM computer_parts WHERE name LIKE ? AND archive = 0";
            prep = con.prepareStatement(query);
            prep.setString(1, "%" + searchName + "%");
            ResultSet rs = prep.executeQuery();

            System.out.println("Search Results:");
            boolean found = false;
            while (rs.next()) {
                found = true;
                int id = rs.getInt("part_id");
                String name = rs.getString("name");
                String brand = rs.getString("brand");
                int categoryId = rs.getInt("category_id");
                int price = rs.getInt("price");
                int stock = rs.getInt("stock_quantity");

                System.out.println(id + " | " + name + " | " + brand + " | Category: "
                        + categoryId + " | Price: " + price + " | Stock: " + stock);
            }
            if (!found) {
                System.out.println(" No computer parts found with name like: " + searchName);
            }
            con.close();
        } catch (SQLException e) {
            System.out.println(" Search error: " + e.getMessage());
        }
    }

    // =====================================================
    // CATEGORY MANAGEMENT
    // =====================================================
    public void addCategory(String categoryName, String description) {
        try {
            connect();
            String query = "INSERT INTO categories (category_name, description) VALUES (?, ?)";
            prep = con.prepareStatement(query);
            prep.setString(1, categoryName);
            prep.setString(2, description);
            prep.executeUpdate();
            System.out.println("Category added: " + categoryName);
            con.close();
        } catch (SQLException e) {
            System.out.println("Category insert error: " + e.getMessage());
        }
    }

    public void updateCategory(int categoryId, String newName, String newDesc) {
        try {
            connect();
            String query = "UPDATE categories SET category_name=?, description=? WHERE category_id=?";
            prep = con.prepareStatement(query);
            prep.setString(1, newName);
            prep.setString(2, newDesc);
            prep.setInt(3, categoryId);
            int rows = prep.executeUpdate();
            if (rows > 0) {
                System.out.println("Updated category ID " + categoryId);
            } else {
                System.out.println("No category found with ID " + categoryId);
            }
            con.close();
        } catch (SQLException e) {
            System.out.println(" Update category error: " + e.getMessage());
        }
    }

    public void deleteCategory(int categoryId) {
        try {
            connect();
            String query = "DELETE FROM categories WHERE category_id=?";
            prep = con.prepareStatement(query);
            prep.setInt(1, categoryId);
            int rows = prep.executeUpdate();
            if (rows > 0) {
                System.out.println("Deleted category ID " + categoryId);
            } else {
                System.out.println(" No category found with ID " + categoryId);
            }
            con.close();
        } catch (SQLException e) {
            System.out.println(" Delete category error: " + e.getMessage());
        }
    }

    public void displayCategories() {
        try {
            connect();
            String query = "SELECT * FROM categories";
            prep = con.prepareStatement(query);
            ResultSet rs = prep.executeQuery();

            System.out.println(" Categories:");
            while (rs.next()) {
                int id = rs.getInt("category_id");
                String name = rs.getString("category_name");
                String desc = rs.getString("description");
                System.out.println(id + " | " + name + " - " + desc);
            }
            con.close();
        } catch (SQLException e) {
            System.out.println(" Category read error: " + e.getMessage());
        }
    }

    // ==========================
    // RESTORE OR DELETE ARCHIVED PARTS
    // ==========================
    public void displayArchivedPartsMenu(Scanner sc) {
        try {
            connect();
            String query = "SELECT * FROM computer_parts WHERE archive = 1";
            prep = con.prepareStatement(query);
            ResultSet rs = prep.executeQuery();

            System.out.println("🗄️ Archived Parts:");
            System.out.printf("%-3s | %-20s | %-10s | %-8s | %-7s | %-5s | %-10s%n",
                    "ID", "Name", "Brand", "Category", "Price", "Stock", "Deleted By");
            System.out.println("-------------------------------------------------------------");

            boolean hasRows = false;
            while (rs.next()) {
                hasRows = true;
                System.out.printf("%-3d | %-20s | %-10s | %-8d | %-7d | %-5d | %-10s%n",
                        rs.getInt("part_id"),
                        rs.getString("name"),
                        rs.getString("brand"),
                        rs.getInt("category_id"),
                        rs.getInt("price"),
                        rs.getInt("stock_quantity"),
                        rs.getString("deleted_by"));
            }

            if (!hasRows) {
                System.out.println("No archived parts found.");
                con.close();
                return;
            }

            con.close();

            System.out.print("\nEnter Part ID to manage (or 0 to exit): ");
            int partId = sc.nextInt();
            sc.nextLine(); // consume newline
            if (partId == 0)
                return;

            System.out.println("1. Restore part");
            System.out.println("2. Delete permanently");
            System.out.print("Choose an option: ");
            int choice = sc.nextInt();
            sc.nextLine();

            if (choice == 1) {
                restoreComputerPart(partId);
            } else if (choice == 2) {
                deleteArchivedComputerPart(partId);
            } else {
                System.out.println("Invalid choice.");
            }

        } catch (SQLException e) {
            System.out.println("Display archive error: " + e.getMessage());
        }
    }

    // Restore an archived part
    public void restoreComputerPart(int partId) {
        try {
            connect();
            String query = "UPDATE computer_parts SET archive = 0, deleted_by = NULL WHERE part_id = ? AND archive = 1";
            prep = con.prepareStatement(query);
            prep.setInt(1, partId);
            int rows = prep.executeUpdate();
            if (rows > 0) {
                System.out.println("Part ID " + partId + " has been restored.");
            } else {
                System.out.println("No archived part found with ID " + partId);
            }
            con.close();
        } catch (SQLException e) {
            System.out.println("Restore error: " + e.getMessage());
        }
    }

    // Delete an archived part permanently
    public void deleteArchivedComputerPart(int partId) {
        try {
            connect();
            String query = "DELETE FROM computer_parts WHERE part_id = ? AND archive = 1";
            prep = con.prepareStatement(query);
            prep.setInt(1, partId);
            int rows = prep.executeUpdate();
            if (rows > 0) {
                System.out.println("Archived part ID " + partId + " deleted permanently.");
            } else {
                System.out.println("No archived part found with ID " + partId);
            }
            con.close();
        } catch (SQLException e) {
            System.out.println("Delete archived part error: " + e.getMessage());
        }
    }

    public void displayPartDetails(int partId) {
        try {
            connect();
            String query = "SELECT part_id, name, brand, price, stock_quantity FROM computer_parts WHERE part_id = ? AND archive = 0";
            prep = con.prepareStatement(query);
            prep.setInt(1, partId);
            ResultSet rs = prep.executeQuery();

            if (rs.next()) {
                System.out.println("\nCurrent Part Details:");
                System.out.printf("ID: %d | Name: %s | Brand: %s | Price: %d | Stock: %d%n",
                        rs.getInt("part_id"),
                        rs.getString("name"),
                        rs.getString("brand"),
                        rs.getInt("price"),
                        rs.getInt("stock_quantity"));
            } else {
                System.out.println("\n⚠️ Warning: No computer part found with ID " + partId);
            }

            con.close();
        } catch (SQLException e) {
            System.out.println("Display part details error: " + e.getMessage());
        }
    }

}
