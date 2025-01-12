package net.froihofer.dsfinance.bank.client;

import net.froihofer.common.BankException;
import net.froihofer.common.BankService;
import net.froihofer.common.dtos.CustomerDto;
import net.froihofer.common.dtos.StockDto;
import org.glassfish.jaxb.core.v2.TODO;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
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
                Helper.findCustomer(scanner, bankService);
                break;
            case 3:
                findStock(scanner, bankService);
                break;
            case 4:
                // TODO: implement method to buy stock for customer
                Helper.buyStock(scanner, bankService);
                break;
            case 5:
                // TODO: implement method to sell stock for customer
                break;
            case 6:
                depo(scanner, bankService);
                // TODO: implement method to list customer's deposit
                break;
            case 7:
                try {
                    bankService.getInvestableVolume();
                }catch (BankException e){
                    System.out.println(e.getMessage());
                }
                // TODO: implement method to look up investible bank volume
                break;
            default:
                System.out.println("Invalid input. Please try again.");
                break;
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

    public static void depo(Scanner scanner, BankService bankService){
        System.out.println("+-----------------+-----------------+-----------------+");
        System.out.println("+--------------- Get DEPO ---------------");
        System.out.println("Customerid");
        int customerid = Integer.parseInt(scanner.nextLine());
        List<StockDto> stocks = bankService.getCustomerPortfolio(customerid);
        BigDecimal total = BigDecimal.ZERO;
        for (StockDto stock : stocks) {
            BigDecimal currentValue = BigDecimal.valueOf(stock.getQuantity()) // Menge als BigDecimal
                    .multiply(stock.getPricePerShare()); // Multipliziere mit Preis pro Aktie
            System.out.println("Company: " + stock.getCompanyName() + ", Symbol: " + stock.getStockSymbol() + ", Quantity: " + stock.getQuantity() + ", Current Value: " + currentValue + ", Value per Share: " + stock.getPricePerShare());
            total = total.add(currentValue);
        }
        System.out.println("Total: " + total);
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

}
