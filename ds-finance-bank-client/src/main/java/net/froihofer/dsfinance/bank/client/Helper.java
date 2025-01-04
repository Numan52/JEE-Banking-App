package net.froihofer.dsfinance.bank.client;

import net.froihofer.common.BankException;
import net.froihofer.common.BankService;
import net.froihofer.common.dtos.CustomerDto;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Helper {

    public static void buyStock(Scanner scanner, BankService bankService) {
        // test
        System.out.println("+-----------------+-----------------+-----------------+");
        long customerId = 0;

        if (bankService.getUserRole().equals("employee")) {
            System.out.println("Do you know the customers id?: Yes (y) | No (n)");
            String choice = scanner.nextLine().toLowerCase();

            if (choice.equals("n") || choice.equals("no")) {
                findCustomer(scanner, bankService);
            }

            System.out.println("Enter customer id: ");
            customerId = Long.parseLong(scanner.nextLine());

        } else {
            // TODO: customer
        }


        System.out.println("Enter the symbol of the stock you wish to buy:");
        String symbol = scanner.nextLine();

        System.out.println("Enter the amount of shares you wish to buy: ");
        int quantity = Integer.parseInt(scanner.nextLine());


        String result;
        try {
            result = bankService.buyStock(customerId, symbol, quantity);
        } catch (BankException e) {
            System.out.println(e.getMessage());
            return;
        }
        System.out.println(result);
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
}
