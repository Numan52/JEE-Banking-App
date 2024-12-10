package net.froihofer.dsfinance.bank.client;

import net.froihofer.common.BankService;
import net.froihofer.common.dtos.CustomerDto;

import java.util.Scanner;

public class EmployeeMenu {

    public void employeeMenu(Scanner scanner, BankService bankService){

        System.out.print("\n---TRADING SERVICE---\n");
        System.out.println("(1) Add Customer");
        System.out.println("(2) Search Customer");
        System.out.println("(3) Search Stock");
        System.out.println("(4) Buy Stock");
        System.out.println("(5) Sell Stock");
        System.out.println("(6) List Customer's Deposit");
        System.out.println("(7) Investible Bank Volume");
        System.out.print("Enter a number: ");
        int input = scanner.nextInt();
        scanner.nextLine(); // consuming the line break of nextint
        switch (input) {
            case 1:
                addCustomer(scanner, bankService);
                break;
            case 2:
                // TODO: implement method to find customer
                break;
            case 3:
                // TODO: implement method to find stock
                break;
            case 4:
                // TODO: implement method to buy stock for customer
                break;
            case 5:
                // TODO: implement method to sell stock for customer
                break;
            case 6:
                // TODO: implement method to list customer's deposit
                break;
            case 7:
                // TODO: implement method to look up investible bank volume
                break;
            default:
                System.out.println("Invalid input. Please try again.");
                break;
        }
    }

    public void addCustomer(Scanner scanner, BankService bankService){
        System.out.println(bankService.getUserRole());
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

}
