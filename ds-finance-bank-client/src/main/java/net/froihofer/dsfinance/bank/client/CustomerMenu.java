package net.froihofer.dsfinance.bank.client;

import net.froihofer.common.BankException;
import net.froihofer.common.BankService;
import java.util.Scanner;

public class CustomerMenu {

    public void customerMenu(Scanner scanner, BankService bankService) {

        int input = 0;
        while (input != 5) {
            System.out.print("\n---TRADING SERVICE---\n");
            System.out.println("(1) Search Stock");
            System.out.println("(2) Buy Stock");
            System.out.println("(3) Sell Stock");
            System.out.println("(4) View Portfolio (incl. Stock Holdings and Total Value)");
            System.out.println("(5) End");
            System.out.print("Enter a number: ");
            input = scanner.nextInt();
            scanner.nextLine(); // consuming the line break of nextint
            switch (input) {
                case 1:
                    Helper.findStock(scanner, bankService);
                    break;
                case 2:
                    Helper.buyStock(scanner, bankService);
                    break;
                case 3:
                    Helper.sellStock(scanner, bankService);
                    break;
                case 4:
                    Helper.viewDepo(scanner, bankService);
                    break;
                case 5:
                    System.out.println("Thanks for using our Bank");
                    break;
                case 6:
                    System.out.println("Hidden Test unlocked");
                    buyStockforOtherCustomerTest(scanner, bankService);
                    break;
                default:
                    System.out.println("Invalid input. Please try again.");
                    break;
            }
        }
    }
    public static void buyStockforOtherCustomerTest(Scanner scanner, BankService bankService) {
        // test
        System.out.println("+-----------------+-----------------+-----------------+");
        long customerId = 0;
        System.out.println("Enter customer id: ");
        customerId = Long.parseLong(scanner.nextLine());
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
}
