/*
 * Group members: YOUR NAMES HERE
 * Instructions: For Project 2, implement all methods in this class, and test to confirm they behave as expected when the program is run.
 */

package database;

import java.sql.*;
import java.util.*;

import dataClasses.*;
import dataClasses.Driver;

public class DatabaseMethods {
  private Connection conn;

  public DatabaseMethods(Connection conn) {
    this.conn = conn;
  }

  /*
   * Accepts: Nothing
   * Behaviour: Retrieves information about all accounts
   * Returns: List of account objects 
   */
  public ArrayList<Account> getAllAccounts() throws SQLException {
    ArrayList<Account> accounts = new ArrayList<Account>();

      String sql = "SELECT a.FIRST_NAME, a.LAST_NAME, ad.STREET, ad.CITY, ad.PROVINCE, ad.POSTAL_CODE, " +
              "a.PHONE_NUMBER, a.EMAIL, a.BIRTHDATE, " +
              "CASE WHEN d.ID IS NOT NULL THEN true ELSE false END AS IsDriver, " +
              "CASE WHEN p.ID IS NOT NULL THEN true ELSE false END AS IsPassenger " +
              "FROM accounts a " +
              "INNER JOIN addresses ad ON a.ADDRESS_ID = ad.ID " +
              "LEFT JOIN drivers d ON a.ID = d.ID " +
              "LEFT JOIN passengers p ON a.ID = p.ID";

      PreparedStatement statement = conn.prepareStatement(sql);
      ResultSet resultSet = statement.executeQuery();
      while (resultSet.next()) {
          Account account = new Account(
                  resultSet.getString(1),
                  resultSet.getString(2),
                  resultSet.getString(3),
                  resultSet.getString(4),
                  resultSet.getString(5),
                  resultSet.getString(6),
                  resultSet.getString(7),
                  resultSet.getString(8),
                  resultSet.getString(9),
                  resultSet.getBoolean(10),
                  resultSet.getBoolean(11)
          );
          accounts.add(account);
      }

    return accounts;
  }

  /*
   * Accepts: Email address of driver
   * Behaviour: Calculates the average rating over all rides performed by the driver specified by the email address
   * Returns: The average rating value 
   */
  public double getAverageRatingForDriver(String driverEmail) throws SQLException {
    double averageRating = 0.0;

      String sql = "select avg(r.RATING_FROM_PASSENGER) as ratingFromPassenger from accounts a INNER join rides r on a.ID = r.DRIVER_ID where a.email = ?";
      PreparedStatement statement = conn.prepareStatement(sql);
      statement.setString(1, driverEmail);
      ResultSet resultSet = statement.executeQuery();
      if(resultSet.next()){
        averageRating = resultSet.getDouble(1);
      }

    return averageRating;
  }

  /*
   * Accepts: Account details, and passenger and driver specific details. Passenger or driver details could be
   * null if account is only intended for one type of use.
   * Behaviour:
   *  - Insert new account using information provided in Account object
   *  - For non-null passenger/driver details, insert the associated data into the relevant tables
   * Returns: Nothing 
   */
  public void createAccount(Account account, Passenger passenger, Driver driver) throws SQLException {
    // TODO: Implement
    // Hint: Use the available insertAccount, insertPassenger, and insertDriver methods

      insertPassenger(passenger,insertAccount(account));
      insertDriver(driver, insertAccount(account));    
  }

  /*
   * Accepts: Account details (which includes address information)
   * Behaviour: Inserts the new account, as well as the account's address if it doesn't already exist. The new/existing address should
   * be linked to the account
   * Returns: Id of the new account 
   */
  public int insertAccount(Account account) throws SQLException {
    int accountId = -1;

    // TODO: Implement
    // Hint: Use the insertAddressIfNotExists method

    String sql = "insert into accounts(FIRST_NAME,LAST_NAME,BIRTHDATE,ADDRESS_ID,PHONE_NUMBER,EMAIL) values(?, ?, ?, ?, ?, ?)";
    PreparedStatement statement = conn.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS);
    statement.setString(1, account.getFirstName());
    statement.setString(2, account.getLastName());
    statement.setString(3, account.getBirthdate());
    statement.setInt(4, insertAddressIfNotExists(account.getAddress()));
    statement.setString(5, account.getPhoneNumber()); 
    statement.setString(6,account.getEmail());
    // Set other account properties for insertion

    int rowsAffected = statement.executeUpdate();

    // If insertion was successful, get the ID of the new account
    if (rowsAffected > 0) {
      ResultSet generatedKeys = statement.getGeneratedKeys();
      if (generatedKeys.next()) {
        accountId = generatedKeys.getInt(1);
      }
    }

    return accountId;
  }

  /*
   * Accepts: Passenger details (should not be null), and account id for the passenger
   * Behaviour: Inserts the new passenger record, correctly linked to the account id
   * Returns: Id of the new passenger 
   */
  public int insertPassenger(Passenger passenger, int accountId) throws SQLException {
    // TODO: Implement
    int passengerId = -1;

    String sql = "insert into passenger(CREDIT_CARD_NUMBER) values(?);";
    PreparedStatement statement = conn.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS);
    statement.setString(1, passenger.getCreditCardNumber());
    // Set other passengers properties for insertion

    int rowsAffected = statement.executeUpdate();

    // If insertion was successful, get the ID of the new address
    if (rowsAffected > 0) {
      ResultSet generatedKeys = statement.getGeneratedKeys();
      if (generatedKeys.next()) {
        passengerId = generatedKeys.getInt(1);
      }
    } 
    return passengerId;
  }

  /*
   * Accepts: Driver details (should not be null), and account id for the driver
   * Behaviour: Inserts the new driver and driver's license record, correctly linked to the account id
   * Returns: Id of the new driver 
   */
  public int insertDriver(Driver driver, int accountId) throws SQLException {
    // TODO: Implement
    // Hint: Use the insertLicense method
    String sql = "insert into drivers(LICENSE_ID) values(?)";
    PreparedStatement statement = conn.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS);
    statement.setInt(1, insertLicense(driver.getLicenseNumber(), driver.getLicenseExpiryDate()));
    // Set other driver properties for insertion

    int rowsAffected = statement.executeUpdate();

    // If insertion was successful, get the ID of the new driver
    if (rowsAffected > 0) {
      ResultSet generatedKeys = statement.getGeneratedKeys();
      if (generatedKeys.next()) {
        accountId = generatedKeys.getInt(1);
      }
    }
    return accountId;
  }

  /*
   * Accepts: Driver's license number and license expiry
   * Behaviour: Inserts the new driver's license record
   * Returns: Id of the new driver's license
   */
  public int insertLicense(String licenseNumber, String licenseExpiry) throws SQLException {
    int licenseId = -1;
    // TODO: Implement
    String sql = "insert into licences(NUMBER,EXPIRY_DATE) values(?,?)";
    PreparedStatement statement = conn.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS);
    statement.setString(1, licenseNumber);
    statement.setString(2, licenseExpiry);
    // Set other licenses properties for insertion

    int rowsAffected = statement.executeUpdate();

    // If insertion was successful, get the ID of the new license
    if (rowsAffected > 0) {
      ResultSet generatedKeys = statement.getGeneratedKeys();
      if (generatedKeys.next()) {
        licenseId = generatedKeys.getInt(1);
      }
    }
    return licenseId;
  }

  /*
   * Accepts: Address details
   * Behaviour: 
   *  - Checks if an address with these properties already exists.
   *  - If it does, gets the id of the existing address.
   *  - If it does not exist, creates the address in the database, and gets the id of the new address
   * Returns: Id of the address
   */
  public int insertAddressIfNotExists(Address address) throws SQLException {
    int addressId = -1;

    // TODO: Implement
      String sql = "select id from addresses where POSTAL_CODE = ?";
      PreparedStatement statement = conn.prepareStatement(sql);
      statement.setString(1, address.getPostalCode());
      ResultSet resultSet = statement.executeQuery();
      addressId = resultSet.getInt(0);

    // If the address exists, get its ID
    if (resultSet.next()) {

      addressId = resultSet.getInt("id");
    } else {

      // If the address does not exist, create it in the database
      sql = "INSERT INTO addresses(STREET,CITY,PROVINCE,POSTAL_CODE) VALUES (?, ?, ?, ?)";
      statement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
      statement.setString(1, address.getStreet());
      statement.setString(2, address.getCity());
      statement.setString(3, address.getProvince());
      statement.setString(4, address.getPostalCode());
      // Set other address properties for insertion

      int rowsAffected = statement.executeUpdate();

      // If insertion was successful, get the ID of the new address
      if (rowsAffected > 0) {
        ResultSet generatedKeys = statement.getGeneratedKeys();
        if (generatedKeys.next()) {
          addressId = generatedKeys.getInt(1);
        }
      }
    }

    return addressId;
  }

  /*
   * Accepts: Name of new favourite destination, email address of the passenger, and the id of the address being favourited
   * Behaviour: Finds the id of the passenger with the email address, then inserts the new favourite destination record
   * Returns: Nothing
   */
  public void insertFavouriteDestination(String favouriteName, String passengerEmail, int addressId)
      throws SQLException {
    // TODO: Implement
    String email = "";
    String sql = "select ID from accounts where EMAIL = ?;";
    PreparedStatement statement = conn.prepareStatement(sql);
    statement.setString(1, passengerEmail);
    ResultSet resultSet = statement.executeQuery();

    sql = "insert into favourite_locations(PASSENGER_ID,LOCATION_ID,NAME) values(?, ?, ?)";
    PreparedStatement statement2 = conn.prepareStatement(sql);
    statement2.setInt(1, resultSet.getInt(1));
    statement2.setInt(2, addressId);
    statement2.setString(3, favouriteName);
    statement2.executeUpdate();
  }

  /*
   * Accepts: Email address
   * Behaviour: Determines if a driver exists with the provided email address
   * Returns: True if exists, false if not
   */
  public boolean checkDriverExists(String email) throws SQLException {
    // TODO: Implement
    String sql = "select * from accounts a inner join drivers d on a.ID =  d.ID where a.EMAIL = ?";
    PreparedStatement statement = conn.prepareStatement(sql);
    statement.setString(1, email);
    ResultSet resultSet = statement.executeQuery();
    return resultSet.next();
  }

  /*
   * Accepts: Email address
   * Behaviour: Determines if a passenger exists with the provided email address
   * Returns: True if exists, false if not
   */
  public boolean checkPassengerExists(String email) throws SQLException {
    // TODO: Implement

    String sql = "select * from accounts a inner join passengers p on a.ID =  p.ID where a.EMAIL = ?";
    PreparedStatement statement = conn.prepareStatement(sql);
    statement.setString(1, email);
    ResultSet resultSet = statement.executeQuery();
    return resultSet.next();
  }

  /*
   * Accepts: Email address of passenger making request, id of dropoff address, requested date/time of ride, and number of passengers
   * Behaviour: Inserts a new ride request, using the provided properties
   * Returns: Nothing
   */
  public void insertRideRequest(String passengerEmail, int dropoffLocationId, String date, String time,
      int numberOfPassengers) throws SQLException {
      int passengerId = this.getPassengerIdFromEmail(passengerEmail);
      int pickupAddressId = this.getAccountAddressIdFromEmail(passengerEmail);

    // TODO: Implement
      String sql = "insert into ride_requests(PASSENGER_ID,PICKUP_LOCATION_ID,PICKUP_DATE,PICKUP_TIME,NUMBER_OF_RIDERS,DROPOFF_LOCATION_ID) VALUES(?,?,?,?,?,?)";
      PreparedStatement statement = conn.prepareStatement(sql);
      statement.setInt(1, passengerId);
      statement.setInt(2, pickupAddressId);
      statement.setString(3, date);
      statement.setString(4, time);
      statement.setInt(5, numberOfPassengers);
      statement.setInt(6,dropoffLocationId);
      statement.executeUpdate();
  }

  /*
   * Accepts: Email address
   * Behaviour: Gets id of passenger with specified email (assumes passenger exists)
   * Returns: Id
   */
  public int getPassengerIdFromEmail(String passengerEmail) throws SQLException {
    int passengerId = -1;
    // TODO: Implement
    String sql = "select p.ID from accounts a inner join passengers p on a.ID =  p.ID where a.EMAIL = ?";
    PreparedStatement statement = conn.prepareStatement(sql);
    statement.setString(1, passengerEmail);
    ResultSet resultSet = statement.executeQuery();
    passengerId = resultSet.getInt(1);
    return passengerId;
  }

  /*
   * Accepts: Email address
   * Behaviour: Gets id of driver with specified email (assumes driver exists)
   * Returns: Id
   */
  public int getDriverIdFromEmail(String driverEmail) throws SQLException {
    int driverId = -1;
    // TODO: Implement
    String sql = "select d.ID from accounts a inner join drivers d on a.ID =  d.ID where a.EMAIL = ?";
    PreparedStatement statement = conn.prepareStatement(sql);
    statement.setString(1, driverEmail);
    ResultSet resultSet = statement.executeQuery();
    driverId = resultSet.getInt(1);
    return driverId;
  }

  /*
   * Accepts: Email address
   * Behaviour: Gets the id of the address tied to the account with the provided email address
   * Returns: Address id
   */
  public int getAccountAddressIdFromEmail(String email) throws SQLException {
    int addressId = -1;
    // TODO: Implement
    String sql = "select ad.ID from accounts a inner join addresses ad on a.ID = ad.ID where a.EMAIL = ?";
    PreparedStatement statement = conn.prepareStatement(sql);
    statement.setString(1, email);
    ResultSet resultSet = statement.executeQuery();
    addressId = resultSet.getInt(1);
    return addressId;
  }

  /*
   * Accepts: Email address of passenger
   * Behaviour: Gets a list of all the specified passenger's favourite destinations
   * Returns: List of favourite destinations
   */
  public ArrayList<FavouriteDestination> getFavouriteDestinationsForPassenger(String passengerEmail)
      throws SQLException {
    ArrayList<FavouriteDestination> favouriteDestinations = new ArrayList<FavouriteDestination>();
    
    // TODO: Implement
    String sql = "select fl.name,fl.LOCATION_ID,ad.STREET,ad.CITY,ad.PROVINCE,ad.POSTAL_CODE from accounts a inner join passengers p on a.ID = p.ID inner join favourite_locations fl on fl.PASSENGER_ID = p.ID inner join addresses ad on ad.ID = fl.LOCATION_ID where a.EMAIL = ?;";
    PreparedStatement statement = conn.prepareStatement(sql);
    statement.setString(1, passengerEmail);
    ResultSet resultSet =  statement.executeQuery();
    while (resultSet.next()) {
      FavouriteDestination favouriteDestination = new FavouriteDestination(resultSet.getString(1), resultSet.getInt(2), resultSet.getString(3), resultSet.getString(4), resultSet.getString(5), resultSet.getString(6));
      favouriteDestinations.add(favouriteDestination);
    }
    return favouriteDestinations;
  }

  /*
   * Accepts: Nothing
   * Behaviour: Gets a list of all uncompleted ride requests (i.e. requests without an associated ride record)
   * Returns: List of all uncompleted rides
   */
  public ArrayList<RideRequest> getUncompletedRideRequests() throws SQLException {
    ArrayList<RideRequest> uncompletedRideRequests = new ArrayList<RideRequest>();

    // TODO: Implement
    
    return uncompletedRideRequests;
  }

  /*
   * Accepts: Ride details
   * Behaviour: Inserts a new ride record
   * Returns: Nothing
   */
  public void insertRide(Ride ride) throws SQLException {
    // TODO: Implement
    // Hint: Use getDriverIdFromEmail
  }

}
