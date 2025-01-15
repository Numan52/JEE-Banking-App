package net.froihofer.dsfinance.bank.client;

import net.froihofer.common.BankException;
import net.froihofer.common.BankService;
import net.froihofer.common.dtos.StockDto;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

import static net.froihofer.dsfinance.bank.client.EmployeeMenu.findCustomer;

public class Helper {

    public static void buyStock(Scanner scanner, BankService bankService) {
        long customerId = getCustomerId(scanner, bankService);
        if (customerId == -1) {
            System.out.println("Customer not found");
            return;
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


    public static void sellStock(Scanner scanner, BankService bankService) {
       long customerId = getCustomerId(scanner, bankService);
       if (customerId == -1) {
           System.out.println("Customer not found");
           return;
       }

        System.out.println("Enter the symbol of the stock you wish to sell:");
        String symbol = scanner.nextLine();

        System.out.println("Enter the amount of shares you wish to sell: ");
        int quantity = Integer.parseInt(scanner.nextLine());


        String result;
        try {
            result = bankService.sellStock(customerId, symbol, quantity);
        } catch (BankException e) {
            System.out.println(e.getMessage());
            return;
        }
        System.out.println(result);
    }


    public static long getCustomerId(Scanner scanner, BankService bankService) {
        System.out.println("+-----------------+-----------------+-----------------+");
        long customerId = 0;
        String userRole = bankService.getUserRole();
        if (userRole.equalsIgnoreCase("employee")) {
            System.out.println("Do you want to search for customers?: Yes (y) | No (n)");
            String choice = scanner.nextLine().toLowerCase();

            if (choice.equals("y") || choice.equals("yes")) {
                findCustomer(scanner, bankService);
            }

            System.out.println("Enter customer id: ");
            customerId = Long.parseLong(scanner.nextLine());

        } else if (userRole.equalsIgnoreCase("customer")) {
            customerId = bankService.getCurrentUserId();
        }
        return customerId;
    }



    //Search for stock quotes based on a part of the company name.
    public static void findStock(Scanner scanner, BankService bankService){
        System.out.println("+-----------------+-----------------+-----------------+");
        System.out.println("+--------------- Find Stock ---------------");
        System.out.println("Part of Company Name: ");
        String companyName = scanner.nextLine();

        List<StockDto> stocks = null;
        try {
            stocks = bankService.findStock(companyName);
        } catch (BankException e) {
            System.out.println(e.getMessage());
            return;
        }
        System.out.println("Found stocks: ");
        System.out.println("+-----------------+-----------------+-----------------+");

        for (StockDto stock : stocks) {
            System.out.println("Company Name: " + stock.getCompanyName());
            System.out.println("Stock Symbol: " + stock.getStockSymbol());
            System.out.println("Price per share: " + stock.getPricePerShare().setScale(2, RoundingMode.HALF_EVEN));
            System.out.println("+-----------------+-----------------+");
        }
    }



    public static void viewDepo(Scanner scanner, BankService bankService){
        System.out.println("+-----------------+-----------------+-----------------+");
        System.out.println("+--------------- Get Portfolio ---------------");
        long customerId = 0;
        String userRole = bankService.getUserRole();
        try {
            if (userRole.equalsIgnoreCase("employee")) {
                System.out.println("Do you want to search for customers?: Yes (y) | No (n)");
                String choice = scanner.nextLine().toLowerCase();

                if (choice.equals("y") || choice.equals("yes")) {
                    findCustomer(scanner, bankService);
                }

                System.out.println("Enter customer id: ");
                customerId = Long.parseLong(scanner.nextLine());

            } else if (userRole.equalsIgnoreCase("customer")) {
                customerId = bankService.getCurrentUserId();
                if (customerId == -1) {
                    System.out.println("Customer not found");
                    return;
                }
            }
        } catch (NumberFormatException e) {
            System.out.println("The Input is invalid, only numbers!");
        }
        try {
            List<StockDto> stocks = bankService.getCustomerPortfolio(customerId);
            BigDecimal total = BigDecimal.ZERO;
            System.out.println("\n---Portfolio Overview---");
            if (!stocks.isEmpty()) {
                for (StockDto stock : stocks) {
                    BigDecimal currentValue = BigDecimal.valueOf(stock.getQuantity()) // Menge als BigDecimal
                            .multiply(stock.getPricePerShare()); // Multipliziere mit Preis pro Aktie
                    System.out.println("Company: " + stock.getCompanyName() + ", Symbol: " + stock.getStockSymbol() + ", Quantity: " + stock.getQuantity() + ", Current Value: " + currentValue + ", Value per Share: " + stock.getPricePerShare());
                    total = total.add(currentValue);
                }
            } else {
                System.out.println("Currently holding no Stocks");
            }
            System.out.println("Total Portfolio Value: " + total);
        }catch (BankException e)
        {
            System.out.println(e.getMessage());
        }
    }


}
