package net.froihofer.dsfinance.bank.client;

import net.froihofer.common.BankService;

import java.util.Scanner;

public class CustomerMenu {

    public void customerMenu(Scanner scanner, BankService bankService){

        int input = scanner.nextInt();

        System.out.print("\n---TRADING SERVICE---\n");

        System.out.println("(1) Search Stock");
        System.out.println("(2) Buy Stock");
        System.out.println("(3) Sell Stock");
        System.out.println("(4) List Customer's Deposit");
        System.out.print("Enter a number: ");

        switch (input) {
            case 1:
                // TODO: implement method to search for stock
                break;
            case 2:
                // TODO: implement method to buy stock
                break;
            case 3:
                // TODO: implement method to sell stock
                break;
            case 4:
                // TODO: implement method to look up investible bank volume
                break;
            default:
                System.out.println("Invalid input. Please try again.");
                break;
        }

    }

}
