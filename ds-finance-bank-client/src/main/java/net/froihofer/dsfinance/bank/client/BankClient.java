package net.froihofer.dsfinance.bank.client;

import java.util.Properties;
import java.util.Scanner;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import net.froihofer.common.BankService;
import net.froihofer.util.AuthCallbackHandler;
import net.froihofer.util.WildflyJndiLookupHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main class for starting the bank client.
 *
 */
public class BankClient {
  private static Logger log = LoggerFactory.getLogger(BankClient.class);
  private BankService bankService;

  /**
   * Skeleton method for performing an RMI lookup
   */
  private String getRmiProxy(String username, String password) {

    AuthCallbackHandler.setUsername(username);
    AuthCallbackHandler.setPassword(password);

    Properties props = new Properties();
    props.put(Context.SECURITY_PRINCIPAL, AuthCallbackHandler.getUsername());
    props.put(Context.SECURITY_CREDENTIALS, AuthCallbackHandler.getPassword());

    try {
      WildflyJndiLookupHelper jndiHelper = new WildflyJndiLookupHelper(new InitialContext(props),
              "ds-finance-bank-ear",
              "ds-finance-bank-ejb",
              "");
      // Proxy für BankService abrufen
      bankService = jndiHelper.lookup("BankService", BankService.class);
      return bankService.getUserRole();
    } catch (NamingException e) {
      log.error("Failed to initialize InitialContext.", e);
    }
    return "Unknown";
  }

  private void run() {
    Scanner scanner = new Scanner(System.in);

    /* ---------------- input username ---------------- */
    System.out.println("Enter username: ");
    String username = scanner.nextLine();

    /* ---------------- input password ---------------- */
    System.out.println("Enter password: ");
    String password = scanner.nextLine();

    String role = getRmiProxy(username, password);

    if (role.equalsIgnoreCase("employee")) {
      System.out.println("Welcome Employee!");
      EmployeeMenu employeeMenu = new EmployeeMenu();
      employeeMenu.employeeMenu(scanner, bankService);
    } else if (role.equalsIgnoreCase("customer")) {
      System.out.println("Welcome Customer!");
      CustomerMenu customerMenu = new CustomerMenu();
      customerMenu.customerMenu(scanner, bankService);
    } else {
      System.out.println("Invalid credentials or unknown user.");
    }

    scanner.close();

  }

  public static void main(String[] args) {
    BankClient client = new BankClient();
    client.run();
  }
}
