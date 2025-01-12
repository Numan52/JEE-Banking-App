package net.froihofer.dsfinance.bank.client;

import net.froihofer.common.BankException;
import net.froihofer.common.BankService;
import net.froihofer.common.dtos.CustomerDto;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

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
                    // TODO: implement method to sell stock for customer
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
        System.out.println("First Name");
        String firstName = scanner.nextLine();
        System.out.println("Last Name");
        String lastName = scanner.nextLine();
        System.out.println("Address");
        String address = scanner.nextLine();
        System.out.println("Password");
        String password = scanner.nextLine();
        bankService.addCustomer(new CustomerDto(firstName, lastName, address, password));
    }
    public static void findCustomer(Scanner scanner, BankService bankService) {
        System.out.println("+-----------------+-----------------+-----------------+");
        System.out.println("Enter first name of the customer: ");
        String firstName = scanner.nextLine();
        System.out.println("Enter last name of the customer: ");
        String lastName = scanner.nextLine();
        List<CustomerDto> customers = new ArrayList<>();

        try {
            customers = bankService.findCustomerByName(firstName, lastName);
        } catch (BankException e) {
            System.out.println(e.getMessage());
        }
        for (CustomerDto customer : customers) {
            System.out.println(customer);
            System.out.println("+-----------------+-----------------+");
        }
    }
    public static void investibleBanVolume(Scanner scanner, BankService bankService) {
        try {
            System.out.println(bankService.getInvestableVolume());
        } catch (BankException e) {
            System.out.println(e.getMessage());
        }
    }
}
