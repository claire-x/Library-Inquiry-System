
import java.io.*;
import java.util.*;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.sql.Date;


public class LibrarySystem {
    
    public static String dbAddress = "jdbc:mysql://projgw.cse.cuhk.edu.hk:2633/db20";

    public static String dbUsername = "Group20";

    public static String dbPassword = "CSCI3170";


  
    
    public static Connection connectToMYSQL(){
        Connection conn = null;
        try{
            Class.forName("com.mysql.jdbc.Driver"); 
            conn = DriverManager.getConnection(dbAddress, dbUsername, dbPassword);
        } catch (ClassNotFoundException e){
            System.out.println("[Error]: Java MYSQL DB server not connected.");
            System.exit(0);
        } catch (SQLException e){
            System.out.println(e);
        }
        return conn;
    }

    public static void mainMenu(Connection conn) throws ParseException 
    {
        Scanner scanner = new Scanner(System.in);
        while(true)
        {
            try {
                // print main menu
                System.out.println("-----Main menu-----\n" +
                        "What kinds of operations would you like to perform?\n" +
                        "1. Operations for Administrator\n" +
                        "2. Operations for Library User\n" +
                        "3. Operations for Librarian\n" +
                        "4. Exit this program");

                System.out.print("Enter your Choice: ");

                int mainMenu = scanner.nextInt();

                if(mainMenu == 1) {
                    adminMenu(scanner, conn);
                } else if(mainMenu == 2) {
//                    userMenu(scanner, conn);     
                    libUser(scanner, conn);
                } else if(mainMenu == 3) {
//                    librarianMenu(scanner, conn);
                    Librarian(scanner, conn);
                } else if(mainMenu == 4) {
                    System.exit(0);;
                } else {
                    System.out.println("[Error]: Please type again. You should input 1-4.");
                }
            } catch (SQLException e){
                System.out.println(e);
            }
        }
    }
        
    public static void createTable(Connection conn) throws SQLException 
    {

    // create table schemas in database         
        
        String ucSQL = "CREATE TABLE IF NOT EXISTS user_category" +
                   "(ucid    INTEGER(1) NOT NULL," +
                   " max     INTEGER(2) NOT NULL, " + 
                   " period  INTEGER(2) NOT NULL, " + 
                   " PRIMARY KEY (ucid))"; 

        String libuserSQL = "CREATE TABLE IF NOT EXISTS libuser" +
                   "(libuid    CHAR(10) PRIMARY KEY," +
                   " name      CHAR(25) NOT NULL," + 
                   " age       INTEGER(3) NOT NULL," + 
                   " address   CHAR(100) NOT NULL," +
                   " ucid      INTEGER(1) NOT NULL," +
                   " FOREIGN KEY (ucid) REFERENCES user_category (ucid))"; 

        String bcSQL = "CREATE TABLE IF NOT EXISTS book_category" +
                   "(bcid    INTEGER(1) PRIMARY KEY," +
                   " bcname  CHAR(30) NOT NULL)";

        String bookSQL = "CREATE TABLE IF NOT EXISTS book" +
                   "(callnum    CHAR(8)," +
                   " title      CHAR(30) NOT NULL," + 
                   " publish    DATE NOT NULL," + 
                   " rating     FLOAT," + 
                   " tborrowed  INTEGER(2) NOT NULL," + 
                   " bcid       INTEGER(1) NOT NULL," +
                   " PRIMARY KEY (callnum)," +
                   " FOREIGN KEY (bcid) REFERENCES book_category (bcid))"; 

        String copySQL = "CREATE TABLE IF NOT EXISTS copy" +
                   "(callnum    CHAR(8)," +
                   " copynum    INTEGER(1)," + 
                   " PRIMARY KEY (callnum, copynum)," + 
                   " FOREIGN KEY (callnum) REFERENCES book (callnum))";  

        String borrowSQL = "CREATE TABLE IF NOT EXISTS borrow" +
                   "(libuid CHAR(10)," +
                   " callnum CHAR(8)," + 
                   " copynum INTEGER(1)," + 
                   " checkout  DATE," +
                   " `return` DATE DEFAULT NULL," +
                   " PRIMARY KEY (libuid, callnum, copynum, checkout)," +
                   " FOREIGN KEY (libuid) REFERENCES libuser (libuid)," +
                   " FOREIGN KEY (callnum, copynum) REFERENCES copy (callnum, copynum))"; 

        String authorship = "CREATE TABLE IF NOT EXISTS authorship" +
                   "(aname CHAR(25)," +
                   " callnum  CHAR(8)," + 
                   " PRIMARY KEY (aname, callnum)," + 
                   " FOREIGN KEY (callnum) REFERENCES book (callnum))";       

        Statement stmt  = conn.createStatement();
        System.out.println("Processing...");
        
        stmt.addBatch(ucSQL);
        stmt.addBatch(libuserSQL);
        stmt.addBatch(bcSQL);
        stmt.addBatch(bookSQL);
        stmt.addBatch(copySQL);
        stmt.addBatch(borrowSQL);
        stmt.addBatch(authorship);
        stmt.executeBatch();
       
        
        System.out.print("Done. Database is initialized."); 
        stmt.close();
    }


    public static void deleteTable(Connection conn) throws SQLException
    {
        // drop all the table schemas in database
        String settingSQL = "SET FOREIGN_KEY_CHECKS=0";
        String ucSQL = "DROP TABLE IF EXISTS user_category";
        String libuserSQL = "DROP TABLE IF EXISTS libuser";
        String bcSQL = "DROP TABLE IF EXISTS book_category";
        String bookSQL = "DROP TABLE IF EXISTS book";
        String copySQL = "DROP TABLE IF EXISTS copy";
        String borrowSQL = "DROP TABLE IF EXISTS borrow";
        String authorship = "DROP TABLE IF EXISTS authorship";

        Statement stmt  = conn.createStatement();
        System.out.println("Processing...");

        stmt.addBatch(settingSQL);
        stmt.addBatch(ucSQL);
        stmt.addBatch(libuserSQL);
        stmt.addBatch(bcSQL);
        stmt.addBatch(bookSQL);
        stmt.addBatch(copySQL);
        stmt.addBatch(borrowSQL);
        stmt.addBatch(authorship);
        stmt.executeBatch();


        System.out.print("Done. Database is removed."); 
        stmt.close();
    }
        
         
    public static void loadData(Scanner scanner, Connection con) throws SQLException {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        SimpleDateFormat sdfToString = new SimpleDateFormat("yyyy-MM-dd");     
        String filePath = "";
        
        // input file path
        while(true)
        {
            System.out.println("");
            System.out.print("Type in the Source Data Folder Path: "); 
            filePath = scanner.next();
            if((new File(filePath)).isDirectory()) 
                break;  
            else
                System.out.println("[Error]: You should input the correct folder path.");
        }
        System.out.println("Processing...");
        String psql;
        PreparedStatement pstmt;
        
        // read user_category.txt
        try
        {
           
            File file;
            Scanner data;
            file = new File(filePath + "/user_category.txt");
            data = new Scanner(file);
            while (data.hasNext()){
                String[] uc = data.nextLine().split("\t");
                int ucid = Integer.parseInt(uc[0]);
                int max = Integer.parseInt(uc[1]);
                int period = Integer.parseInt(uc[2]);
                psql = "INSERT IGNORE INTO user_category VALUES (?, ?, ?)";
                pstmt = con.prepareStatement(psql);
                pstmt.setInt(1, ucid);
                pstmt.setInt(2, max);
                pstmt.setInt(3, period);
                pstmt.executeUpdate();
            }
            data.close();

        } catch(Exception e) {
            System.out.println("[Error]: You should create table user_category first.");
        }
        
        // read user.txt
        try 
        {
            
            File file;
            Scanner data;
            file = new File(filePath + "/user.txt");
            data = new Scanner(file);
            while (data.hasNext()){
                String[] user = data.nextLine().split("\t");
                String uid = user[0];
                String name = user[1];
                int age = Integer.parseInt(user[2]);
                String address = user[3];
                int ucid = Integer.parseInt(user[4]);
                psql = "INSERT IGNORE INTO libuser VALUES (?, ?, ?, ?, ?)";
                pstmt = con.prepareStatement(psql);
                pstmt.setString(1, uid);
                pstmt.setString(2, name);
                pstmt.setInt(3, age);
                pstmt.setString(4, address);
                pstmt.setInt(5, ucid);
                pstmt.executeUpdate();
            }
            data.close();

        } catch(Exception e) {
            System.out.println("[Error]: You should create table libuser first.");
        }
        
        // read book_category.txt
        try 
        {
            
            File file;
            Scanner data;
            file = new File(filePath + "/book_category.txt");
            data = new Scanner(file);
           while (data.hasNext()){
                String[] bc = data.nextLine().split("\t");
                int bcid = Integer.parseInt(bc[0]);
                String bcname = bc[1];
                psql = "INSERT IGNORE INTO book_category VALUES (?, ?)";
                pstmt = con.prepareStatement(psql);
                pstmt.setInt(1, bcid);
                pstmt.setString(2, bcname);
                pstmt.executeUpdate();
            }
            data.close();

        } catch(Exception e) {
            System.out.println("[Error]: You should create table book_category first.");
        }
        
        // read book.txt
        try 
        {
            File file;
            Scanner data;
            file = new File(filePath + "/book.txt");
            data = new Scanner(file);
            while (data.hasNext()) {
                String[] book = data.nextLine().split("\t");
                String callnum = book[0];
                int copyNum = Integer.parseInt(book[1]);
                String title = book[2];
                String[] authors = book[3].split(",");
                
                java.sql.Date publish = java.sql.Date.valueOf(sdfToString.format(sdf.parse(book[4])));

                
                Double rating;
                if (book[5].compareTo("null") != 0) {
                    rating = Double.parseDouble(book[5]);
                }
                else
                    rating = -1.0;
                int tborrowed = Integer.parseInt(book[6]);
                int bcid = Integer.parseInt(book[7]);
                
                // insert values into book
                psql = "INSERT IGNORE INTO book VALUES (?, ?, ?, ?, ?, ?)";
                pstmt = con.prepareStatement(psql);
                pstmt.setString(1, callnum);
                pstmt.setString(2, title);
                pstmt.setDate(3, publish);
                if (rating >= 0)
                    pstmt.setDouble(4, rating);
                else
                    pstmt.setNull(4, Types.FLOAT);
                pstmt.setInt(5, tborrowed);
                pstmt.setInt(6, bcid);
                pstmt.executeUpdate();
                
                // insert values into authorship
                psql = "INSERT IGNORE INTO authorship VALUES (?, ?)";
                pstmt = con.prepareStatement(psql);
                
                for (int i=0; i < authors.length; i++){
                    pstmt.setString(1, authors[i]);
                    pstmt.setString(2, callnum);
                    pstmt.executeUpdate();
                }
                
                
                // insert values into copy
                psql = "INSERT IGNORE INTO copy VALUES (?, ?)";
                pstmt = con.prepareStatement(psql);
                
                for (int j = 1; j <= copyNum; j++){
                pstmt.setString(1, callnum);
                pstmt.setInt(2, j);
                pstmt.executeUpdate();
                }
                
            }
            data.close();

        } catch(FileNotFoundException | NumberFormatException | SQLException | ParseException e) {
            System.out.println("[Error]: You should create table book, authorship, copy first.");
        }
        
                // read check_out.txt
        try
        {
            
            File file;
            Scanner data;
            file = new File(filePath + "/check_out.txt");
            data = new Scanner(file);
            while (data.hasNext()){
                String[] check = data.nextLine().split("\t");
                String callNum = check[0];
                int copyNum = Integer.parseInt(check[1]);
                String uid = check[2];
                String tmp = sdfToString.format(sdf.parse(check[3]));
                Date checkOut = Date.valueOf(tmp);
                Date returnDate = null;
                if (check[4].compareTo("null") != 0){
                    tmp = sdfToString.format(sdf.parse(check[4]));
                    returnDate = Date.valueOf(tmp);
                }
                psql = "INSERT IGNORE INTO borrow VALUES (?, ?, ?, ?, ?)";
                pstmt = con.prepareStatement(psql);
                pstmt.setString(1, uid);
                pstmt.setString(2, callNum);
                pstmt.setInt(3, copyNum);
                pstmt.setDate(4, checkOut);
                pstmt.setDate(5, returnDate);
                pstmt.executeUpdate();
            }
            data.close();

        } catch(FileNotFoundException e) {
            System.out.println("[Error]: You should create table borrow first.");
        } catch (ParseException ex) {
            Logger.getLogger(LibrarySystem.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
            
    public static void showRecord(Scanner scanner, Connection conn) 
    {
        // table names of all the tables in database
        String[] tableName = {"user_category", "libuser", "book_category", "book", "copy", "borrow", "authorship"};

        System.out.println("Number of records in each table:\n");
        // retrieve numbers of records and print
        for (int i = 0; i < 7; i++){
            try {
                Statement stmt  = conn.createStatement();
                ResultSet rs = stmt.executeQuery("select count(*) from "+tableName[i]);

                rs.next();
                System.out.println(tableName[i]+": "+rs.getString(1));
                rs.close();
                stmt.close();
            } catch (SQLException ex) {
                System.out.println("[Error]: You should creat " + tableName[i] + " first.");
            }
        }
        
    }
    
    public static void adminMenu(Scanner scanner, Connection conn) throws SQLException, ParseException
    {
        while(true) 
        {
            System.out.println(); 
            System.out.println("-----Operations for administrator menu-----\n"+
                    "What kinds of operations would you like to perform?\n" +
                    "1. Create all tables\n" +
                    "2. Delete all tables\n" +
                    "3. Load from datafile\n" +
                    "4. Show numbers of records in each table\n" +
                    "5. Return to the main menu");
            System.out.print("Enter your Choice: ");

            int operation = scanner.nextInt();
            
            switch(operation) {
                    case 1:
                        createTable(conn);
                        break;
                    case 2:
                        deleteTable(conn);
                        break;
                    case 3:
//                        loadData(scanner, conn);
                        loadData(scanner, conn);
                        break;
                    case 4:
                        showRecord(scanner, conn);
                        break;
                    case 5:
                        mainMenu(conn);
                    default:
                        System.out.println("[Error]: Type again. You should input 1-5.");
            }
        }
    }
    
    public static void libUser(Scanner scanner, Connection con) throws ParseException{
        while(true){
            System.out.println("-----Operations for library user menu-----");
            System.out.println("What kind of operation would you like to perform?");
            System.out.println("1. Search for books");
            System.out.println("2. Show loan record of a user");
            System.out.println("3. Return to the main menu");
            System.out.print("Enter your choice: ");

            int inputInt;
            do {
                inputInt = scanner.nextInt();
                scanner.nextLine();
                String psql;
                PreparedStatement pstmt;
                ResultSet rs;
                String callnum;
                switch (inputInt) {
                    case 1:
                        System.out.println("Choose the search criterion:");
                        System.out.println("1. Call number");
                        System.out.println("2. Title");
                        System.out.println("3. Author");
                        System.out.print("Choose the search criterion: ");
                        int criterion;
                        do {
                            criterion = scanner.nextInt();
                            scanner.nextLine();
                            String searchKey;
                            switch(criterion){
                                case 1:
                                    System.out.print("Type in the Search Keyword: ");
                                    searchKey = scanner.nextLine();
                                    bookSearch(con, searchKey);
                                    System.out.println("End of Query");
                                    break;
                                case 2:
                                    System.out.print("Type in the Search Keyword: ");
                                    searchKey = scanner.nextLine();
                                    try{
                                        psql = "SELECT callnum\n"+
                                               "FROM book\n"+
                                               "WHERE title LIKE ?;";
                                        pstmt = con.prepareStatement(psql);
                                        pstmt.setString(1, "%" + searchKey + "%");
                                        rs = pstmt.executeQuery();
                                        if(!rs.isBeforeFirst())
                                            System.out.println("[Error]: No records found.");
                                        else {
                                            System.out.println("|Call Num|Title|Book Category|Author|Rating|Availabel No. of Copy|");
                                            while (rs.next()) {
                                                callnum = rs.getString("callnum");
                                                bookSearch(con, callnum);
                                            }
                                        }
                                    }catch (SQLException e){
                                        System.out.println(e);
                                    }
                                    System.out.println("End of Query");
                                    break;
                                case 3:
                                    System.out.print("Type in the Search Keyword: ");
                                    searchKey = scanner.nextLine();
                                    try{
                                        psql = "SELECT callnum\n"+
                                               "FROM authorship\n"+
                                               "WHERE aname LIKE ?;";
                                        pstmt = con.prepareStatement(psql);
                                        pstmt.setString(1, "%" + searchKey + "%");
                                        rs = pstmt.executeQuery();
                                        if(!rs.isBeforeFirst())
                                            System.out.println("[Error]: No records found.");
                                        else{
                                            System.out.println("|Call Num|Title|Book Category|Author|Rating|Availabel No. of Copy|");
                                            while (rs.next()) {
                                                callnum = rs.getString("callnum");
                                                bookSearch(con, callnum);
                                            }
                                        }
                                    }catch (SQLException e){
                                        System.out.println(e);
                                    }
                                    System.out.println("End of Query");
                                    break;
                                default:
                                    System.out.println("Invalid input! Please try again.");
                                    System.out.print("Choose the search criterion: ");
                            }
                        } while (criterion < 1 || criterion > 3);
                        break;
                    case 2:
                        System.out.print("Enter the User ID: ");
                        String uid = scanner.nextLine();

                        try{
                            psql = "SELECT borrow.callnum, copynum, book.title, checkout, `return`\n"+
                                   "FROM borrow, book\n"+
                                   "WHERE borrow.callnum=book.callnum AND libuid=?;";
                            pstmt = con.prepareStatement(psql);
                            pstmt.setString(1, uid);
                            rs = pstmt.executeQuery();
                            if(!rs.isBeforeFirst())
                                System.out.println("[Error]: No records found.");
                            else {
                                System.out.println("Loan Record:");
                                System.out.println("|Call Num|Copy Num|Title|Author|Check-out|Returned?|");
                                while (rs.next()) {
                                    System.out.print("|" + rs.getString("callnum") + "|" + rs.getString("copynum") + "|" +
                                    rs.getString("title") + "|" + getAuthors(con, rs.getString("callnum")) + "|" + rs.getDate("checkout") + "|");
                                    String res;
                                    if (rs.getDate("return") == null)
                                        res = "No";
                                    else
                                        res = "Yes";
                                    System.out.println(res + "|");
                                }
                            }
                        }catch (SQLException e){
                            System.out.println(e);
                        }

                        System.out.println("End of Query");
                        break;
                    case 3:
                        System.out.println();
                        mainMenu(con);
                        break;
                    default:
                        System.out.println("Invalid input! Please try again.");
                        System.out.print("Enter your choice: ");
                }
            } while (inputInt > 3 || inputInt < 1);
        }
    }
    
    public static void bookSearch(Connection con, String callnum) {
        String psql;
        PreparedStatement pstmt;
        ResultSet rs;
        int copyNum = 0;
        int tNotReturn = 0;
        try{
            psql = "SELECT B.callnum, title, bcname, rating\n"+
                   "FROM book B, book_category BC\n"+
                   "WHERE B.bcid=BC.bcid AND B.callnum=?";
            pstmt = con.prepareStatement(psql);
            pstmt.setString(1, callnum);
            rs = pstmt.executeQuery();
            if(!rs.isBeforeFirst())
                System.out.println("[Error]: No records found.");
            else{
                rs.next();
                System.out.print("|" + rs.getString("B.callnum") + "|" + rs.getString("title") + "|"
                        + rs.getString("bcname") + "|" + getAuthors(con, callnum) + "|" + rs.getString("rating") + "|");
                
                psql = "SELECT COUNT(*) AS cnt\n" +
                       "FROM copy\n" +
                       "WHERE callnum=?;";
                pstmt = con.prepareStatement(psql);
                pstmt.setString(1, callnum);
                rs = pstmt.executeQuery();
                if(!rs.isBeforeFirst())
                    System.out.println("[Error]: No records found.");
                else{
                    rs.next();
                    copyNum = rs.getInt("cnt");
                }

                psql = "SELECT COUNT(*) AS cnt\n" +
                    "FROM borrow\n" +
                    "WHERE callnum=? AND `return` IS NULL";
                pstmt = con.prepareStatement(psql);
                pstmt.setString(1, callnum);
                rs = pstmt.executeQuery();
                if (!rs.isBeforeFirst())
                    System.out.println("[Error]: No records found.");
                else{
                    rs.next();
                    tNotReturn = rs.getInt("cnt");
                }

                System.out.print(copyNum-tNotReturn);
                System.out.println("|");
            }
        }catch (SQLException e){
            System.out.println(e);
        }
    }

    public static String getAuthors(Connection con, String callnum) {
        String psql;
        PreparedStatement pstmt;
        ResultSet rs;
        String result = "";
        try{
            psql = "SELECT aname\n"+
                    "FROM authorship\n"+
                    "WHERE callnum=?;";
            pstmt = con.prepareStatement(psql);
            pstmt.setString(1, callnum);
            rs = pstmt.executeQuery();
            if(!rs.isBeforeFirst())
                System.out.println("[Error]: No records found.");
            else{
                while(rs.next()){
                    result += rs.getString("aname") + ", ";
                }
                result = result.substring(0, result.length()-2);
            }
        }catch (SQLException e){
            System.out.println(e);
        }    
        return result;  
    }
    
    public static void Librarian(Scanner scanner, Connection con) throws ParseException{

        int inputInt;
        do {
            System.out.println("-----Operations for librarian menu-----");
            System.out.println("What kind of operation would you like to perform?");
            System.out.println("1. Book Borrowing");
            System.out.println("2. Book Returning");
            System.out.println("3. List all un-returned book copies which are checked-out within a period");
            System.out.println("4. Return to the main menu");
            System.out.print("Enter your choice: ");

            inputInt = scanner.nextInt();
            scanner.nextLine();
            switch (inputInt) {
                case 1:
                    System.out.print("Enter The User ID: ");
                    String id = scanner.nextLine();
                    System.out.print("Enter The Call Number:");
                    String call = scanner.nextLine();
                    System.out.print("Enter The Copy Number:");
                    int copy = Integer.parseInt(scanner.nextLine());
                    Borrow(con,id,call,copy);
                    break;
                case 2:
                    System.out.print("Enter the User ID: ");
                    String id2 = scanner.nextLine();
                    System.out.print("Enter the Call Number: ");
                    String call2 = scanner.nextLine();
                    System.out.print("Enter The Copy Number:");
                    int copy2 = Integer.parseInt(scanner.nextLine());
                    System.out.print("Enter Your Rating of the Book:");
                    Double rating = Double.parseDouble(scanner.nextLine());
                    ReturnBook(con,id2,call2,copy2,rating);
                    break;
                case 3:
                    System.out.print("Type in the starting date [dd/mm/yyyy]: ");
                    SimpleDateFormat sdf=new SimpleDateFormat("dd/MM/yyyy");
                    String tmp = scanner.nextLine();
                    java.util.Date date1 = sdf.parse(tmp);
                    java.sql.Date d1=new java.sql.Date(date1.getTime());
                    System.out.print("Type in the ending date [dd/mm/yyyy]: ");
                    tmp = scanner.nextLine();
                    java.util.Date date2 = sdf.parse(tmp);
                    java.sql.Date d2=new java.sql.Date(date2.getTime());
                    Unreturned(con,d1,d2);
                    break;
                case 4:
                    System.out.println();
                    mainMenu(con); 
                    break;
                default:
                    System.out.println("Invalid input! Please try again.");
                    System.out.print("Enter your choice: ");
            }
        } while (true);
    }
    
    public static void Borrow(Connection con, String id,String call,int copy) {
        String query;
        PreparedStatement pstmt;
        ResultSet rs;
        Calendar calendar = Calendar.getInstance();
        java.sql.Date date = new java.sql.Date(calendar.getTime().getTime());
        int max, ct,ucid;
        try{
            query = "SELECT ucid\n"+
            "FROM libuser\n"+
            "WHERE libuid=?";
            pstmt = con.prepareStatement(query);
            pstmt.setString(1, id);
            rs = pstmt.executeQuery();
            boolean p=rs.next();
            if (!p){
                System.out.println("[Error]: Invalid id input.");
                return;
            }
            ucid=rs.getInt(1);
            query = "SELECT max\n"+
            "FROM user_category\n"+
            "WHERE ucid=?";
            pstmt = con.prepareStatement(query);
            pstmt.setInt(1, ucid);
            rs = pstmt.executeQuery();
            p=rs.next();
            if (!p){
                System.out.println("[Error]: Invalid ucid record for the user.");
                return;
            }
            max=rs.getInt(1);
            query = "SELECT COUNT(*) FROM borrow WHERE libuid=? AND (`return` IS NULL)";
            pstmt = con.prepareStatement(query);
            pstmt.setString(1, id);
            rs = pstmt.executeQuery();
            rs.next();
            ct=rs.getInt(1);
            if (ct >= max){System.out.println("You have reached the max number of borrowing.");}
            else{
                query = "SELECT *\n"+
                        "FROM borrow\n"+
                        "WHERE callnum=? AND copynum=? AND (`return` IS NULL)";
                pstmt = con.prepareStatement(query);
                pstmt.setString(1, call);
                pstmt.setInt(2, copy);
                rs = pstmt.executeQuery();
                if(!rs.next()) {
                    query = "INSERT INTO borrow (libuid, callnum, copynum, checkout)\n"+
                        "VALUES (?, ?, ?, ?)";
                    pstmt = con.prepareStatement(query);
                    pstmt.setString(1, id);
                    pstmt.setString(2, call);
                    pstmt.setInt(3, copy);
                    pstmt.setDate(4, date);
                    pstmt.executeUpdate();
                    System.out.println("Book borrowing performed successfully.");}
                else{
                    System.out.println("This book is not available now.");
                }
            }
        }catch (SQLException e){
            System.out.println(e);
        }
    }
    
    public static void ReturnBook(Connection con, String id,String call, int copy, Double r) {
        String psql;
        PreparedStatement pstmt;
        ResultSet rs;
        int t;
        Double newr;
        try{
            psql = "SELECT ucid\n"+
            "FROM libuser\n"+
            "WHERE libuid=?";
            pstmt = con.prepareStatement(psql);
            pstmt.setString(1, id);
            rs = pstmt.executeQuery();
            boolean p=rs.next();
            if (!p){
                System.out.println("[Error]: Invalid id input for the user.");
                return;
            }
            int ucid=rs.getInt(1);
            psql = "SELECT period\n"+
            "FROM user_category\n"+
            "WHERE ucid=?";
            pstmt = con.prepareStatement(psql);
            pstmt.setInt(1, ucid);
            rs = pstmt.executeQuery();
            p=rs.next();
            if (!p){
                System.out.println("[Error]: Invalid ucid record for the user.");
                return;
            }
            int period=rs.getInt(1);
            psql = "SELECT checkout\n"+
                    "FROM borrow\n"+
                    "WHERE libuid=? AND callnum=? AND copynum=? AND (`return` IS NULL)";
            pstmt = con.prepareStatement(psql);
            pstmt.setString(1, id);
            pstmt.setString(2, call);
            pstmt.setInt(3, copy);
            rs = pstmt.executeQuery();
            if(!rs.isBeforeFirst())
                System.out.println("[Error]: No matching borrowing records found. This book has not been borrowed yet.");
            else{
                Calendar calendar = Calendar.getInstance();
                java.sql.Date date = new java.sql.Date(calendar.getTime().getTime());
                psql = "SELECT checkout\n"+
                       "FROM borrow\n"+
                       "WHERE libuid=? AND callnum=? AND copynum=? AND (`return` IS NULL)";
                    pstmt = con.prepareStatement(psql);
                    pstmt.setString(1, id);
                    pstmt.setString(2, call);
                    pstmt.setInt(3, copy);
                    rs = pstmt.executeQuery();
                    rs.next();
                    java.sql.Date bDate=rs.getDate("checkout");
                    long nod=(date.getTime()-bDate.getTime())/(24*60*60*1000);
                if (nod>period){System.out.println("This returning is overdue. Please manually operate and adjust user category.");}
                else{
                    psql = "UPDATE borrow \n"+
                        "SET `return`=?\n"+
                        "WHERE libuid=? AND callnum=? AND copynum=? AND (`return` IS NULL)";
                        pstmt = con.prepareStatement(psql);
                        pstmt.setDate(1, date);
                        pstmt.setString(2, id);
                        pstmt.setString(3, call);
                        pstmt.setInt(4, copy);
                        pstmt.executeUpdate();
                    psql = "SELECT rating, tborrowed\n"+
                        "FROM book\n"+
                        "WHERE callnum=?";
                        pstmt = con.prepareStatement(psql);
                        pstmt.setString(1, call);
                        rs = pstmt.executeQuery();
                        if (rs.next()){
                            Double or=rs.getDouble("rating");//or:original rating
                            t=rs.getInt("tborrowed");
                            newr = (or* t+r)/(t+1);
                            t+=1;
                        }
                        else{
                            newr=r;
                            t =1;
                        }
                    psql = "UPDATE book\n"+
                        "SET tborrowed=?\n"+
                        "WHERE callnum=?";
                        pstmt = con.prepareStatement(psql);
                        pstmt.setInt(1, t);
                        pstmt.setString(2, call);
                        pstmt.executeUpdate();
                    psql = "UPDATE book\n"+
                        "SET rating=?\n"+
                        "WHERE callnum=?";
                        pstmt = con.prepareStatement(psql);
                        pstmt.setDouble(1,newr);
                        pstmt.setString(2, call);
                        pstmt.executeUpdate();
                    System.out.println("Book Returning performed successfully.");
                }
            }
        }catch (SQLException e){
            System.out.println(e);
        }     
    }
    
    public static void Unreturned(Connection con, Date date1, Date date2) {
        String psql;
        PreparedStatement pstmt;
        ResultSet rs;
        try{
            psql = "SELECT libuid, callnum, copynum, checkout\n"+
                   "FROM borrow\n"+
                   "WHERE checkout>? AND checkout<? AND (`return` IS NULL);";
            pstmt = con.prepareStatement(psql);  
            pstmt.setDate(1,date1);
            pstmt.setDate(2,date2);
            rs = pstmt.executeQuery();
            if(!rs.isBeforeFirst())
               System.out.println("[Error]: No borrowing records in this period.\n");
            else{
                System.out.println("List of UnReturned Book:");
                System.out.println("|LibUID|CallNum|CopyNum|Checkout|");
                while (rs.next()) {
                      System.out.println("|" + rs.getString("libuid") + "|" + rs.getString("callnum") + "|" +
                      rs.getInt("copynum") + "|" + rs.getDate("checkout") + "|");
                }
             }
        }
        catch (SQLException e){
            System.out.println(e);
        }
    }
            
    
    
    public static void main(String[] args) throws SQLException, ParseException {
        
        System.out.println("Welcome to Library Inquiry System!");
        System.out.println();
        Scanner scanner = new Scanner(System.in);
        Connection conn = connectToMYSQL();
        mainMenu(conn);
        scanner.close();
        System.exit(0);
        
    }
    
}
