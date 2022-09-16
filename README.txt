Database Systems Project – Library Inquiry System - Group 20



Group Members:
JIN Zhao	1155141435
XU Yuhan        1155124360
YANG Shichen	1155124352



Compilation and deployment of the system:

1. How to run:		
	1. Connect to CSE VPN

	2. Connect to Linux Servers
	type:								ssh username@linux5.cse.cuhk.edu.hk	
		
	3. Connect to MySQL (In Java)
	type:								javac LibrarySystem.java
									java -cp .:mysql-connector-java-5.1.47.jar LibrarySystem



2. Structure of LibrarySystem.java

2.1. Connect to MySQL:	

	In LibrarySystem.java, we have method 
	to connect to SQL server:					connectToMYSQL()  	

	and have
	set database address as 					"jdbc:mysql://projgw.cse.cuhk.edu.hk:2633/db20"
	set user name as 						"Group20"
    	set password as 						"CSCI3170"


2.2. Main menu:								mainMenu(Connection conn)    
	

2.3. Administrator operations:
   
	administrator menu:						adminMenu(Scanner scanner, Connection conn) 

   	Create table schemas in the database:  				createTable(Connection conn) 

   	Delete table schemas in the database:				deleteTable(Connection conn)    
 
   	Load data from a dataset:  					loadData(Scanner scanner, Connection con)   
   
   	Show the number of records in each table:              		showRecord(Scanner scanner, Connection conn) 
   	

2.4. Library User operations:
 
	library user menu:						libUser(Scanner scanner, Connection con) 
    
	Search for books:						bookSearch(Connection con, String callnum) 

	Get the authors:						getAuthors(Connection con, String callnum) 

  
2.5. Librarian operations:
  
 	librarian menu:							Librarian(Scanner scanner, Connection con)     

    	Borrow a book copy:						Borrow(Connection con, String id,String call,int copy)    

    	Return a book copy:						ReturnBook(Connection con, String id,String call, int copy, Double r)  
   
    	List all un-returned books copies 
	which are checked-out within a period:				Unreturned(Connection con, Date date1, Date date2)            
    
    
2.6. Main method  



3. Input and result explanation:

3.1. Main menu:

	Input 1 turns to the menu of Administrator operations.
	Input 2 turns to the menu of  Library User operations.
	Input 3 turns to the menu of Librarian operations.
	Input 4 exits the program.
	Input other integers, the system will send a reminder that the user should input 1-4.

3.2. Administrator menu:

	Input 1, the system will create tables schemas in the database.
	Input 2, the system will delete all existing table schemas in the database.
	Input 3, the system will load data from data files from a user-specified folder.
	Input 4, the system will show the number of records in each table.
	Input 5, the system will return to the main menu.
	Input other integers, the system will send a reminder that the user should input 1-5.

	When loading data from dataset:
	If the input folder path is not available, the system will send a reminder that the user should input the correct folder path.
	If the table does not exist when the system is required to load the data, the system will send a reminder that the user should create the corresponding table first.
	
	When show the number of records in each table:  
	If the table does not exist when the system is required to show the records, the system will send a reminder that the user should create the corresponding table first.

3.3. Library user menu: 
	
	Input 1 for book searching. The books in the library can be searched in three different ways:
		1. By call number (exact matching)
		2. By title (partial matching)
		3. By author (partial matching)
	Input 2 to show all check-out records of a library user. A valid user id should be inputed.
	If the result set is returned as an empty set, a “No result found” error will be raised.

3.4. Librarian menu:

	Input 1 for book borrowing. User id, call number and copy number are required.
        Input 2 for book returning. User id, call number and copy number are required. Users are also required to rate the book.
        Input 3 for borrowing records within a period. Input of starting date and ending date are required.

	When borrowing a book copy:
	If the user id and the ucid are invalid, the system will generate a reminder and back to the librarian menu to let user reinput.  
	If the corresponding copy of the book has been already borrowed. The system will say  “This book is not available now.      	
	If the user has already reached his/her max number of borrowing, then the borrowing won’t be processed and a reminder will be generated.
	If the borrowing is processed successfully, the system will say “Book borrowing performanced successfully.	

	When returning a book copy:
	If the user id and the ucid are invalid, the system will generate a reminder and back to the librarian menu to let user reinput.  
	If the borrowing records can not be found. The system will generate a reminder.
	If the returning exceeds the user’s period requirement in the corresponding user category, this returning will not be processed, and the system will send a reminder. Then the librarian may need to manually deal with it and give some penalty or adjustment to the user.
	If the returning is processed successfully, the system will send a reminder. And the rating and tborrowed of this book will be updated.

	When listing all un-returned books copies which are checked-out within a period:
	If there are no records in the certain period, the system will send a reminder.
	Listing will follow a format of “|LibUID|CallNum|CopyNum|Checkout|”.




























