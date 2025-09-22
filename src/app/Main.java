package app;

import java.util.Scanner;

public class Main {

    // Clear the terminal screen
    public static void clearScreen() {
        try {
            if (System.getProperty("os.name").toLowerCase().contains("windows")) {
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } else {
                // Linux / macOS
                System.out.print("\033[H\033[2J\033[3J");
                System.out.flush();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Pause until user presses Enter
    public static void pause(Scanner sc) {
        System.out.println("\nPress Enter to continue...");
        sc.nextLine();
        clearScreen();
    }

    public static void main(String[] args) {
        Function f = new Function();
        Scanner sc = new Scanner(System.in);

        // Login
        System.out.print("Enter email: ");
        String username = sc.nextLine();
        System.out.print("Enter password: ");
        String password = sc.nextLine();

        // clear after login inputs
        clearScreen();

        // login now returns role if successful, null otherwise
        String role = f.login(username, password);

        if (role != null) {
            boolean running = true;

            while (running) {
                if (role.equalsIgnoreCase("Admin")) {
                    clearScreen();
                    System.out.println("===== ADMIN MENU =====");
                    System.out.println("1. Manage Computer Parts");
                    System.out.println("2. Manage Categories");
                    System.out.println("3. Manage Users");
                    System.out.println("4. View Archive");
                    System.out.println("5. Logout");
                    System.out.print("Choose an option: ");
                    int choice = sc.nextInt();
                    sc.nextLine();

                    switch (choice) {
                        case 1: // Computer Parts
                            boolean cpRunning = true;
                            while (cpRunning) {
                                clearScreen();
                                System.out.println("=== Computer Parts ===");
                                System.out.println("1. Add Part");
                                System.out.println("2. Update Part");
                                System.out.println("3. Delete Part");
                                System.out.println("4. Archive Part");
                                System.out.println("5. View Parts");
                                System.out.println("6. Back to Admin Menu");
                                System.out.print("Choose: ");

                                int cp = sc.nextInt();
                                sc.nextLine();

                                switch (cp) {
                                    case 1:
                                        clearScreen();
                                        f.displayCategories();
                                        System.out.print("Enter part name: ");
                                        String name = sc.nextLine();
                                        System.out.print("Enter brand: ");
                                        String brand = sc.nextLine();
                                        System.out.print("Enter category id: ");
                                        int categoryId = sc.nextInt();
                                        System.out.print("Enter price: ");
                                        int price = sc.nextInt();
                                        System.out.print("Enter stock quantity: ");
                                        int stock = sc.nextInt();
                                        f.addComputerParts(name, brand, categoryId, price, stock);
                                        pause(sc);
                                        break;

                                    case 2:
                                        clearScreen();
                                        System.out.print("Enter part ID to update: ");
                                        int updateId = sc.nextInt();
                                        System.out.print("Enter new price: ");
                                        int newPrice = sc.nextInt();
                                        System.out.print("Enter new stock: ");
                                        int newStock = sc.nextInt();
                                        f.updateComputerPartsRecord(updateId, newPrice, newStock);
                                        pause(sc);
                                        break;

                                    case 3:
                                        clearScreen();
                                        System.out.print("Enter part ID to delete: ");
                                        int deleteId = sc.nextInt();
                                        f.deleteComputerPartsRecord(deleteId);
                                        pause(sc);
                                        break;

                                    case 4: // Archive Part
                                        clearScreen();
                                        System.out.println("=== Archive Computer Part ===");

                                        // Show active computer parts so user can see IDs
                                        f.displayComputerPartsRecord();

                                        System.out.print("\nEnter part ID to archive: ");
                                        int archiveId = sc.nextInt();
                                        sc.nextLine();

                                        System.out.print("Enter your name to record who archives this part: ");
                                        String deletedBy = sc.nextLine(); // record actual user/admin name

                                        boolean archived = f.archiveComputerPart(archiveId, deletedBy); // update method
                                                                                                        // to return
                                                                                                        // true/false

                                        if (archived) {
                                            System.out.println("\n✅ Part archived successfully.");
                                        } else {
                                            System.out.println("\n❌ Part not found or could not be archived.");
                                        }

                                        pause(sc);
                                        break;

                                    case 5:
                                        clearScreen();
                                        f.displayComputerPartsRecord();
                                        pause(sc);
                                        break;

                                    case 6:
                                        cpRunning = false; // back to admin
                                        break;

                                    default:
                                        System.out.println("Invalid option.");
                                        pause(sc);
                                }
                            }
                            break;

                        case 2: // Categories
                            clearScreen();
                            System.out.println("=== Categories ===");
                            System.out.println("1. Add Category");
                            System.out.println("2. Update Category");
                            System.out.println("3. Delete Category");
                            System.out.println("4. View Categories");
                            System.out.print("Choose: ");
                            int cat = sc.nextInt();
                            sc.nextLine();

                            switch (cat) {
                                case 1:
                                    clearScreen();
                                    System.out.print("Enter category name: ");
                                    String cname = sc.nextLine();
                                    System.out.print("Enter description: ");
                                    String desc = sc.nextLine();
                                    f.addCategory(cname, desc);
                                    break;
                                case 2:
                                    clearScreen();
                                    System.out.print("Enter category ID: ");
                                    int cid = sc.nextInt();
                                    sc.nextLine();
                                    System.out.print("Enter new name: ");
                                    String newName = sc.nextLine();
                                    System.out.print("Enter new description: ");
                                    String newDesc = sc.nextLine();
                                    f.updateCategory(cid, newName, newDesc);
                                    break;
                                case 3:
                                    clearScreen();
                                    System.out.print("Enter category ID to delete: ");
                                    int delCid = sc.nextInt();
                                    f.deleteCategory(delCid);
                                    break;
                                case 4:
                                    clearScreen();
                                    f.displayCategories();
                                    break;
                                default:
                                    System.out.println("Invalid option.");
                            }
                            pause(sc);
                            break;

                        case 3: // Users
                            clearScreen();
                            System.out.println("=== Users ===");
                            System.out.println("1. Add User");
                            System.out.println("2. Update User");
                            System.out.println("3. Delete User");
                            System.out.println("4. View Users");
                            System.out.print("Choose: ");
                            int us = sc.nextInt();
                            sc.nextLine();

                            switch (us) {
                                case 1:
                                    clearScreen();
                                    System.out.print("Enter first name: ");
                                    String fname = sc.nextLine();
                                    System.out.print("Enter last name: ");
                                    String lname = sc.nextLine();
                                    System.out.print("Enter email: ");
                                    String uemail = sc.nextLine();
                                    System.out.print("Enter role (Admin/User): ");
                                    String urole = sc.nextLine();
                                    System.out.print("Enter username: ");
                                    String uname = sc.nextLine();
                                    System.out.print("Enter password: ");
                                    String upass = sc.nextLine();
                                    System.out.print("Enter phone number: ");
                                    String phone = sc.nextLine();

                                    f.addUser(fname, lname, uemail, phone, urole, uname, upass);

                                    break;

                                case 2:
                                    clearScreen();
                                    System.out.print("Enter user ID: ");
                                    int uid = sc.nextInt();
                                    sc.nextLine();
                                    System.out.print("Enter new email: ");
                                    String nemail = sc.nextLine();
                                    System.out.print("Enter new role: ");
                                    String nrole = sc.nextLine();
                                    f.updateUser(uid, nemail, nrole);
                                    break;
                                case 3:
                                    clearScreen();
                                    System.out.print("Enter user ID to delete: ");
                                    int duid = sc.nextInt();
                                    f.deleteUser(duid);
                                    break;
                                case 4:
                                    clearScreen();
                                    f.displayUsers();
                                    break;
                                default:
                                    System.out.println("Invalid option.");
                            }
                            pause(sc);
                            break;

                        case 4: // View Archive
                            clearScreen();
                            f.displayArchivedParts();
                            pause(sc);
                            break;

                        case 5: // Logout
                            f.logout();
                            running = false;
                            break;

                        default:
                            System.out.println("Invalid option.");
                            pause(sc);
                    }

                } else { // USER MENU
                    clearScreen();
                    System.out.println("===== USER MENU =====");
                    System.out.println("1. View Computer Parts");
                    System.out.println("2. Search Computer Part");
                    System.out.println("3. Update Computer Part");
                    System.out.println("4. Logout");
                    System.out.print("Choose an option: ");
                    int choice = sc.nextInt();
                    sc.nextLine();

                    switch (choice) {
                        case 1:
                            clearScreen();
                            f.displayComputerPartsRecord();
                            break;
                        case 2:
                            clearScreen();
                            System.out.print("Enter part name to search: ");
                            String searchName = sc.nextLine();
                            f.searchComputerPartsRecord(searchName);
                            break;
                        case 3:
                            clearScreen();
                            System.out.print("Enter part ID to update: ");
                            int updateId = sc.nextInt();
                            System.out.print("Enter new price: ");
                            int newPrice = sc.nextInt();
                            System.out.print("Enter new stock: ");
                            int newStock = sc.nextInt();
                            f.updateComputerPartsRecord(updateId, newPrice, newStock);
                            break;
                        case 4:
                            f.logout();
                            running = false;
                            break;
                        default:
                            System.out.println("Invalid option.");
                    }
                    if (running)
                        pause(sc);
                }
            }
        } else {
            System.out.println("Exiting program...");
        }

        sc.close();
    }
}
