# Online-retail-store-database-management
Evaluated and compared the performance of MySQL and MongoDB under heavy load.

Database is be capable of storing information about users, Products, Reviews and Orders.
Operations performed are: 
CreateAccount
SubmitOrder
PostReview
AddProduct
UpdateStockLevel
GetProductAndReviews
GetAverageUserRating


Tested the performance of application with a number of concurrent threads ranging from 1-10. (That is, test once with one thread, once with two threads, and so on.) For a period of five minutes, all threads will loop and repeatedly choose an operation and execute it. Which operation to perform during each iteration of the loop was selected at random.
