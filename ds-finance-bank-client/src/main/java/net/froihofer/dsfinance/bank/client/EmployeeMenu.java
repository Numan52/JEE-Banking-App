package net.froihofer.dsfinance.bank.client;

import net.froihofer.common.BankException;
import net.froihofer.common.BankService;
import net.froihofer.common.dtos.CustomerDto;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static org.apache.commons.lang3.StringUtils.isNumeric;

public class EmployeeMenu {

    public void employeeMenu(Scanner scanner, BankService bankService){

        int input = 0;
        while (input != 5) {
            System.out.print("\n---TRADING SERVICE---\n");
            System.out.println("(1) Add Customer");
            System.out.println("(2) Search Customer");
            System.out.println("(3) Search Stock");
            System.out.println("(4) Buy Stock");
            System.out.println("(5) Sell Stock");
            System.out.println("(6) List Customer's Portfolio (incl. Stock Holdings and Total Value)");
            System.out.println("(7) Investible Bank Volume");
            System.out.println("(8) End");
            System.out.print("Enter a number: ");
            input = scanner.nextInt();
            scanner.nextLine(); // consuming the line break of nextint
            switch (input) {
                case 1:
                    addCustomer(scanner, bankService);
                    break;
                case 2:
                    findCustomer(scanner, bankService);
                    break;
                case 3:
                    Helper.findStock(scanner, bankService);
                    break;
                case 4:
                    Helper.buyStock(scanner, bankService);
                    break;
                case 5:
                    Helper.sellStock(scanner, bankService);
                    break;
                case 6:
                    Helper.viewDepo(scanner, bankService);
                    break;
                case 7:
                    investibleBanVolume(scanner, bankService);
                    break;
                case 8:
                    System.out.println("Thanks for using our Bank");
                    break;
                default:
                    System.out.println("Invalid input. Please try again.");
                    break;
            }
        }
    }

    public static void addCustomer(Scanner scanner, BankService bankService){
        System.out.println("+-----------------+-----------------+-----------------+");
        System.out.println("+--------------- Create a new customer ---------------");

        System.out.println("Username: ");
        String username = scanner.nextLine();
        System.out.println("First Name:");
        String firstName = scanner.nextLine();
        System.out.println("Last Name:");
        String lastName = scanner.nextLine();
        System.out.println("Address:");
        String address = scanner.nextLine();
        System.out.println("Password:");
        String password = scanner.nextLine();

        try {
            bankService.addCustomer(username, firstName, lastName, address, password);
        } catch (BankException e) {
            System.out.println("Could not add customer: " + e.getMessage());
            return;
        }
        System.out.println("Customer added successfully.");
    }


    public static void findCustomer(Scanner scanner, BankService bankService) {
        System.out.println("Would you like to search by customer ID or customer name?");
        System.out.println("1: Search by Customer ID");
        System.out.println("2: Search by Customer Name");
        System.out.print("Enter your choice (1 or 2): ");
        String choice = scanner.nextLine().toLowerCase();
        switch (choice) {
            case "1": //customerID
                System.out.print("Please enter the customer ID: ");
                String customerId = scanner.nextLine();
                System.out.println("Searching for customer with ID: " + customerId);
                try {
                    if (isNumeric(customerId)) {
                        long customerIdAsLong = Long.parseLong(customerId);
                        CustomerDto customer = bankService.findCustomer(customerIdAsLong);
                        System.out.println("+-----------------+-----------------+");
                        System.out.println("CustomerID: " + customer.getCustomerId() + " | First Name: " + customer.getFirstName() + " | Last Name: " + customer.getLastName() + " | Address: " + customer.getAddress());
                        System.out.println("+-----------------+-----------------+");
                    } else {
                        System.out.println("The Input is invalid, only numbers!");
                        return;
                    }
                } catch (BankException e) {
                    System.out.println(e.getMessage());
                }
                break;
            case "2": //first and lastname
                System.out.println("+-----------------+-----------------+-----------------+");
                System.out.println("Enter first name of the customer: ");
                String firstName = scanner.nextLine().toLowerCase();
                System.out.println("Enter last name of the customer: ");
                String lastName = scanner.nextLine().toLowerCase();
                List<CustomerDto> customers = new ArrayList<>();
                try {
                    customers = bankService.findCustomerByName(firstName, lastName);
                    for (CustomerDto customer : customers) {
                        System.out.println("+-----------------+-----------------+");
                        System.out.println("CustomerID: " + customer.getCustomerId() + " | First Name: " + customer.getFirstName() + " | Last Name: " + customer.getLastName() + " | Address: " + customer.getAddress());
                        System.out.println("+-----------------+-----------------+");
                    }
                } catch (BankException e) {
                    System.out.println(e.getMessage());
                }
                break;
            default:
                System.out.println("Invalid input. Please choose 1 or 2.");
        }

    }
    public static void investibleBanVolume(Scanner scanner, BankService bankService) {
        try {
            System.out.println("The current investible Volume of the Bank is $" + bankService.getInvestableVolume());
        } catch (BankException e) {
            System.out.println(e.getMessage());
        }
    }
}
