package edu.rit.ibd.a1;

import com.devskiller.jfairy.Fairy;
import com.devskiller.jfairy.producer.BaseProducer;
import com.devskiller.jfairy.producer.DateProducer;
import com.devskiller.jfairy.producer.person.Person;
import com.devskiller.jfairy.producer.text.TextProducer;

import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
//import io.codearte.jfairy.Fairy;
//import org.jfairy.*;

public class Application implements Runnable{


    private static final String NULL_STR = "\\N";
    static String jdbcURL;
    static String jdbcUser;
    static String jdbcPwd;
    static Set<AtomicInteger> operations =new HashSet<AtomicInteger>();

    static List<Integer> prod =new ArrayList<Integer>();


    AtomicInteger at=new AtomicInteger(0);


    public static Connection createConnection() throws Exception{
        Connection con = DriverManager.getConnection(jdbcURL, jdbcUser, jdbcPwd);
        con.setAutoCommit(false);
        con.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
        return con;
    }

    public static void main(String[] args) throws Exception {
        jdbcURL =  "jdbc:mysql://localhost:3306/ITEM";
        jdbcUser = "root";
        jdbcPwd = "2302";
        Connection con = createConnection();

        String drop1 ="DROP TABLE IF EXISTS Reviews";
        String drop2 ="DROP TABLE IF EXISTS Orders";
        String drop3 ="DROP TABLE IF EXISTS Products";
        String drop4 ="DROP TABLE IF EXISTS Users";

        con.createStatement().execute(drop1);
        con.createStatement().execute(drop2);
        con.createStatement().execute(drop3);
        con.createStatement().execute(drop4);

        PreparedStatement st = con.prepareStatement("CREATE TABLE `Users` (\n" +
                "  `username` VARCHAR(250) NOT NULL,\n" +
                "  `password` VARCHAR(250) NOT NULL,\n" +
                "  `firstName` VARCHAR(250) NOT NULL,\n" +
                "  `lastName` VARCHAR(250) NOT NULL,\n" +
                "  PRIMARY KEY (`username`))");
        st.execute();
        st = con.prepareStatement("CREATE TABLE `Products` (\n" +
                "  `id` INT(4) NOT NULL AUTO_INCREMENT,\n" +
                "  `name` VARCHAR(250) NOT NULL,\n" +
                "  `description` VARCHAR(250) NOT NULL,\n" +
                "  `price` INT(11) NOT NULL,\n" +
                "  `quantity` INT(11) NOT NULL,\n" +
                "  PRIMARY KEY (`id`,`name`))");
        st.execute();
        st = con.prepareStatement("CREATE TABLE `Reviews` (\n" +
                "  `username` VARCHAR(250) NOT NULL,\n" +
                "  `productID` INT(4) NOT NULL,\n" +
                "  `reviewText` VARCHAR(250) NOT NULL,\n" +
                "  `rating` FLOAT(4,2) NOT NULL,\n" +
                "  `date` DATE NOT NULL,\n" +
                "  PRIMARY KEY (`username`,`productID`),\n"+
                "  FOREIGN KEY (`username`) REFERENCES Users (`username`),\n"+
                "  FOREIGN KEY (`productID`) REFERENCES Products (`id`))");
        st.execute();
        st = con.prepareStatement("CREATE TABLE `Orders` (\n" +
                "  `id` INT(11) NOT NULL AUTO_INCREMENT,\n" +
                "  `username` VARCHAR(250) NOT NULL,\n" +
                "  `date` DATE NOT NULL,\n" +
                "  `productID` INT(4) NOT NULL,\n" +
                "  `quantity` INT(4) NOT NULL,\n" +
                "  PRIMARY KEY (`id`,`productID`),\n"+
                "  FOREIGN KEY (`username`) REFERENCES Users (`username`),\n"+
                "  FOREIGN KEY (`productID`) REFERENCES Products (`id`))");
        st.execute();

        con.commit();
        st.close();
        //con.close();

        Fairy fairy = Fairy.create();

        int i=0;
        while(i<1000){
            Person person =fairy.person();
            String username = person.getUsername();
            String password = person.getPassword();
            String firstname =person.getFirstName();
            String lastname = person.getLastName();
            boolean val = createAccount(con,username,password,firstname,lastname);
            if(val){
                i++;
            }

        }
        int productid=1;
        while(productid<1001){

            TextProducer text = fairy.textProducer();
            String name = text.word(1);
            String description = text.sentence();
            BaseProducer cost = fairy.baseProducer();
            int price = cost.randomBetween(1,100);
            int initialStock = cost.randomBetween(1,50);
            boolean val = addProduct(con,name, description,price,initialStock);
            if(val){
                productid++;
            }
        }

        int j=0;
        while(j<20000){
            String selectusername = "SELECT * FROM Users ORDER BY RAND() LIMIT 1";
            PreparedStatement uname = con.prepareStatement(selectusername);
            ResultSet rs = uname.executeQuery();
            String username = "";
            String password = "";
            if (rs.next()) {
                username = rs.getString("username");
                password = rs.getString("password");
            }
            BaseProducer cost1 = fairy.baseProducer();
            int prodid = cost1.randomBetween(1,1000);
            float rating = cost1.randomBetween(0,10);
            TextProducer text1 = fairy.textProducer();
            String reviewText = text1.sentence();
            boolean val = postReview(con,username,password, prodid, rating, reviewText);
            if(val){
                j++;
            }
        }
        int k=0;
        while(k<10000){
            String selectusername = "SELECT * FROM Users ORDER BY RAND() LIMIT 1";
            PreparedStatement uname = con.prepareStatement(selectusername);
            ResultSet rs = uname.executeQuery();
            String username = "";
            String password = "";
            if (rs.next()) {
                username = rs.getString("username");
                password = rs.getString("password");
            }
            BaseProducer cost1 = fairy.baseProducer();
            Map<Integer,Integer> listOfProductsAndQuantities = new HashMap<>();
            //int m=0;
            while(listOfProductsAndQuantities.size()<10){
                int prodid = cost1.randomBetween(1,1000);
                int quant = cost1.randomBetween(1,50);
                listOfProductsAndQuantities.put(prodid,quant);
            }
            DateProducer d = fairy.dateProducer();
            LocalDateTime date = d.randomDateInThePast(50);
            boolean val = submitOrder(true,con,date.toLocalDate(),username,password, listOfProductsAndQuantities);
            if(val){
                k++;
            }
        }
        con.close();


        Runnable runnable1 = new Application();
        Thread t1 = new Thread(runnable1);
        t1.start();
        t1.join();
        Connection con2 = createConnection();
        String x = "Select count(id) as count from Products where quantity<=0;";
        PreparedStatement uname = null;
        int count=0;
        try {
            uname = con2.prepareStatement(x);
            ResultSet rs = uname.executeQuery();
            if (rs.next()) {
                count = rs.getInt("count");
            }
            prod.add(count/10);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        con2.close();

        Runnable runnable2 = new Application();
        Thread t2 = new Thread(runnable2);
        Thread t3 = new Thread(runnable2);
        t2.start();
        t3.start();
        t2.join();
        t3.join();
        con2 = createConnection();
        count=0;
        try {
            uname = con2.prepareStatement(x);
            ResultSet rs = uname.executeQuery();
            if (rs.next()) {
                count = rs.getInt("count");
            }
            prod.add(count/10);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        con2.close();

        Runnable runnable3 = new Application();
        Thread t4 = new Thread(runnable3);
        Thread t5 = new Thread(runnable3);
        Thread t6 = new Thread(runnable3);
        t4.start();
        t5.start();
        t6.start();
        t4.join();
        t5.join();
        t6.join();
        con2 = createConnection();
        count=0;
        try {
            uname = con2.prepareStatement(x);
            ResultSet rs = uname.executeQuery();
            if (rs.next()) {
                count = rs.getInt("count");
            }
            prod.add(count/10);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        con2.close();


        Runnable runnable4 = new Application();
        Thread t7 = new Thread(runnable4);
        Thread t8 = new Thread(runnable4);
        Thread t9 = new Thread(runnable4);
        Thread t10 = new Thread(runnable4);
        t7.start();
        t8.start();
        t9.start();
        t10.start();
        t7.join();
        t8.join();
        t9.join();
        t10.join();
        con2 = createConnection();
        count=0;
        try {
            uname = con2.prepareStatement(x);
            ResultSet rs = uname.executeQuery();
            if (rs.next()) {
                count = rs.getInt("count");
            }
            prod.add(count/10);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        con2.close();


        Runnable runnable5 = new Application();
        Thread t11 = new Thread(runnable5);
        Thread t12 = new Thread(runnable5);
        Thread t13 = new Thread(runnable5);
        Thread t14= new Thread(runnable5);
        Thread t15 = new Thread(runnable5);
        t11.start();
        t12.start();
        t13.start();
        t14.start();
        t15.start();
        t11.join();
        t12.join();
        t13.join();
        t14.join();
        t15.join();
        con2 = createConnection();
        count=0;
        try {
            uname = con2.prepareStatement(x);
            ResultSet rs = uname.executeQuery();
            if (rs.next()) {
                count = rs.getInt("count");
            }
            prod.add(count/10);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        con2.close();


        Runnable runnable6 = new Application();
        Thread t16 = new Thread(runnable6);
        Thread t17 = new Thread(runnable6);
        Thread t18 = new Thread(runnable6);
        Thread t19 = new Thread(runnable6);
        Thread t20 = new Thread(runnable6);
        Thread t21 = new Thread(runnable6);
        t16.start();
        t17.start();
        t18.start();
        t19.start();
        t20.start();
        t21.start();
        t16.join();
        t17.join();
        t18.join();
        t19.join();
        t20.join();
        t21.join();
        con2 = createConnection();
        count=0;
        try {
            uname = con2.prepareStatement(x);
            ResultSet rs = uname.executeQuery();
            if (rs.next()) {
                count = rs.getInt("count");
            }
            prod.add(count/10);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        con2.close();


        Runnable runnable7 = new Application();
        Thread t22 = new Thread(runnable7);
        Thread t23 = new Thread(runnable7);
        Thread t24 = new Thread(runnable7);
        Thread t25 = new Thread(runnable7);
        Thread t26 = new Thread(runnable7);
        Thread t27 = new Thread(runnable7);
        Thread t28 = new Thread(runnable7);
        t22.start();
        t23.start();
        t24.start();
        t25.start();
        t26.start();
        t27.start();
        t28.start();
        t22.join();
        t23.join();
        t24.join();
        t25.join();
        t26.join();
        t27.join();
        t28.join();
        con2 = createConnection();
        count=0;
        try {
            uname = con2.prepareStatement(x);
            ResultSet rs = uname.executeQuery();
            if (rs.next()) {
                count = rs.getInt("count");
            }
            prod.add(count/10);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        con2.close();


        Runnable runnable8 = new Application();
        Thread t29 = new Thread(runnable8);
        Thread t30 = new Thread(runnable8);
        Thread t31 = new Thread(runnable8);
        Thread t32 = new Thread(runnable8);
        Thread t33 = new Thread(runnable8);
        Thread t34 = new Thread(runnable8);
        Thread t35 = new Thread(runnable8);
        Thread t36 = new Thread(runnable8);
        t29.start();
        t30.start();
        t31.start();
        t32.start();
        t33.start();
        t34.start();
        t35.start();
        t36.start();
        t29.join();
        t30.join();
        t31.join();
        t32.join();
        t33.join();
        t34.join();
        t35.join();
        t36.join();
        con2 = createConnection();
        count=0;
        try {
            uname = con2.prepareStatement(x);
            ResultSet rs = uname.executeQuery();
            if (rs.next()) {
                count = rs.getInt("count");
            }
            prod.add(count/10);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        con2.close();


        Runnable runnable9 = new Application();
        Thread t37 = new Thread(runnable9);
        Thread t38 = new Thread(runnable9);
        Thread t39 = new Thread(runnable9);
        Thread t40 = new Thread(runnable9);
        Thread t41 = new Thread(runnable9);
        Thread t42 = new Thread(runnable9);
        Thread t43 = new Thread(runnable9);
        Thread t44 = new Thread(runnable9);
        Thread t45 = new Thread(runnable9);
        t37.start();
        t38.start();
        t39.start();
        t40.start();
        t41.start();
        t42.start();
        t43.start();
        t44.start();
        t45.start();
        t37.join();
        t38.join();
        t39.join();
        t40.join();
        t41.join();
        t42.join();
        t43.join();
        t44.join();
        t45.join();
        con2 = createConnection();
        count=0;
        try {
            uname = con2.prepareStatement(x);
            ResultSet rs = uname.executeQuery();
            if (rs.next()) {
                count = rs.getInt("count");
            }
            prod.add(count/10);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        con2.close();


        Runnable runnable10 = new Application();
        Thread t46 = new Thread(runnable10);
        Thread t47 = new Thread(runnable10);
        Thread t48 = new Thread(runnable10);
        Thread t49 = new Thread(runnable10);
        Thread t50 = new Thread(runnable10);
        Thread t51 = new Thread(runnable10);
        Thread t52 = new Thread(runnable10);
        Thread t53 = new Thread(runnable10);
        Thread t54 = new Thread(runnable10);
        Thread t55 = new Thread(runnable10);
        t46.start();
        t47.start();
        t48.start();
        t49.start();
        t50.start();
        t51.start();
        t52.start();
        t53.start();
        t54.start();
        t55.start();
        t46.join();
        t47.join();
        t48.join();
        t49.join();
        t50.join();
        t51.join();
        t52.join();
        t53.join();
        t54.join();
        t55.join();
        con2 = createConnection();
        count=0;
        try {
            uname = con2.prepareStatement(x);
            ResultSet rs = uname.executeQuery();
            if (rs.next()) {
                count = rs.getInt("count");
            }
            prod.add(count/10);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        con2.close();


        System.out.println(operations);
        System.out.println(prod);


    }

    @Override
    public void run() {
        Connection con3 = null;
        try {
            con3 = createConnection();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Random r = new Random();
        int low = 0;
        int high = 101;
        long before = System.nanoTime();

        while(((System.nanoTime()-before)/ (1e9 * 60))<=5){
            int result = r.nextInt(high-low) + low;
            try {
                selectmethod(result,con3);
            } catch (Exception e) {
                e.printStackTrace();
            }
            at.getAndIncrement();
        }
        operations.add(at);
        try {
            con3.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**CreateAccount 0-3
     AddProduct 4-5
     UpdateStockLevel 6-15
     GetProductAndReviews 16-80
     GetAverageUserRating 81-85
     SubmitOrder 86-95
     PostReview 96-100
     **/
    public void selectmethod(int prob,Connection con) throws Exception {
        Fairy fairy = Fairy.create();
        if(prob<=3){
            Person person =fairy.person();
            String username = person.getUsername();
            String password = person.getPassword();
            String firstname =person.getFirstName();
            String lastname = person.getLastName();
            createAccount(con,username,password,firstname,lastname);
        }else if(prob>3 && prob<=5){
            TextProducer text = fairy.textProducer();
            String name = text.word(1);
            String description = text.sentence();
            BaseProducer cost = fairy.baseProducer();
            int price = cost.randomBetween(1,100);
            int initialStock = cost.randomBetween(1,50);
            addProduct(con,name, description,price,initialStock);

        }else if(prob>5 && prob<=15){
            BaseProducer cost = fairy.baseProducer();
            int itemCountToAdd = cost.randomBetween(1,50);
            int productID = cost.randomBetween(1,1000);
            updateStockLevel(con, productID, itemCountToAdd);
        }
        else if(prob>15 && prob<=80){
            BaseProducer cost = fairy.baseProducer();
            int productID = cost.randomBetween(1,1000);
            getProductAndReviews(con, productID);
        }
        else if(prob>80 && prob<=85){
            String selectusername = "SELECT * FROM Reviews ORDER BY RAND() LIMIT 1";
            PreparedStatement uname = con.prepareStatement(selectusername);
            ResultSet rs = uname.executeQuery();
            String username = "";
            if (rs.next()) {
                username = rs.getString("username");
            }
            getAverageUserRating(con,username);
        }
        else if(prob>85 && prob<=95){
            boolean val=false;
            while(!val){
                String selectusername = "SELECT * FROM Users ORDER BY RAND() LIMIT 1";
                PreparedStatement uname = con.prepareStatement(selectusername);
                ResultSet rs = uname.executeQuery();
                String username = "";
                String password = "";
                if (rs.next()) {
                    username = rs.getString("username");
                    password = rs.getString("password");
                }
                BaseProducer cost1 = fairy.baseProducer();
                Map<Integer,Integer> listOfProductsAndQuantities = new HashMap<>();
                while(listOfProductsAndQuantities.size()<10){
                    int prodid = cost1.randomBetween(1,1000);
                    int quant = cost1.randomBetween(1,50);
                    listOfProductsAndQuantities.put(prodid,quant);
                }
                DateProducer d = fairy.dateProducer();
                LocalDateTime date = d.randomDateInThePast(50);
                val = submitOrder(false,con,date.toLocalDate(),username,password, listOfProductsAndQuantities);
            }


        }else{
            boolean val=false;
            while(!val){
                String selectusername = "SELECT * FROM Users ORDER BY RAND() LIMIT 1";
                PreparedStatement uname = con.prepareStatement(selectusername);
                ResultSet rs = uname.executeQuery();
                String username = "";
                String password = "";
                if (rs.next()) {
                    username = rs.getString("username");
                    password = rs.getString("password");
                }
                BaseProducer cost1 = fairy.baseProducer();
                int prodid = cost1.randomBetween(1,1000);
                float rating = cost1.randomBetween(0,10);
                TextProducer text1 = fairy.textProducer();
                String reviewText = text1.sentence();
                val = postReview(con,username,password, prodid, rating, reviewText);
            }

        }
    }


    private static boolean createAccount(Connection con,String username,String password,String firstName,String lastName) throws Exception {
        boolean success=true;
        PreparedStatement stnew = null;
        try{
            String que = "INSERT INTO Users(username,password,firstName,lastName) VALUES(?,?,?,?)";
            stnew = con.prepareStatement(que);
            stnew.setString(1,username);
            stnew.setString(2,password);
            stnew.setString(3,firstName);
            stnew.setString(4,lastName);
            stnew.addBatch();
            stnew.executeUpdate();
            con.commit();
        }catch (SQLException e) {
            e.printStackTrace();
            success= false;
        } finally {
            try {
                // Close connection
                if (stnew != null) {
                    stnew.close();
                }
                return success;
            } catch (Exception e) {
                e.printStackTrace();
                con.rollback();
                return false;
            }
        }
    }

    private static boolean submitOrder(boolean init, Connection con, LocalDate date, String username, String password, Map<Integer,Integer> listOfProductsAndQuantities) throws Exception{
        boolean success=true;
        PreparedStatement stnew = null;
        PreparedStatement update = null;
        try{
            String authent = "SELECT COUNT(1) FROM Users WHERE username ='"+ username+ "' AND password ='"+ password+"'";
            PreparedStatement sql = con.prepareStatement(authent);
            ResultSet rs = sql.executeQuery();
            if (rs.next() == false) {
                System.out.println("username or password is invalid");
            } else {
                int productid = 0;
                int quantity = 0;
                PreparedStatement checkprod = null;
                String que = "INSERT INTO Orders(username,date,productID,quantity) VALUES(?,?,?,?)";
                for (Map.Entry<Integer,Integer> entry : listOfProductsAndQuantities.entrySet()){
                    productid = entry.getKey();
                    quantity = entry.getValue();
                    boolean placeorder=true;
                    if(!init){
                        String quant = "SELECT quantity FROM Products WHERE id ="+ productid;
                        checkprod = con.prepareStatement(quant);
                        ResultSet rs1=checkprod.executeQuery(quant);
                        int availablequantity=0;
                        if(rs1.next()){
                            availablequantity = rs1.getInt("quantity");
                        }
                        if (availablequantity < quantity) {
                            placeorder=false;
                            //System.out.println("either product does not exist or quantity available is less");
                        } else {
                            String updatequant = "UPDATE Products SET quantity = ? WHERE id = ?";
                            availablequantity = availablequantity - quantity;
                            update = con.prepareStatement(updatequant);
                            update.setInt(1, availablequantity);
                            update.setInt(2, productid);
                            update.executeUpdate();
                        }
                    }
                    if(placeorder){
                        stnew = con.prepareStatement(que);
                        //stnew.setInt(1,orderid);
                        stnew.setString(1,username);
                        stnew.setDate(2, Date.valueOf(date));
                        stnew.setInt(3,productid);
                        stnew.setInt(4,quantity);
                        stnew.executeUpdate();
                    }
                     con.commit();
                    }
                }
        }catch (SQLException e) {
            e.printStackTrace();
            con.rollback();
            success=false;
        } finally {
            try {
                // Close connection
                if (update != null) {
                    update.close();
                }
                if (stnew != null) {
                    stnew.close();
                }
                return success;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
    }

    //First authorizes the user (as above) and then submits a review. Each user should also
    //    only be able to post a single review for a given product.
    private static boolean postReview(Connection con, String username,String password, int productID, float rating, String reviewText)throws Exception{
        boolean success=true;
        PreparedStatement stnew = null;
        PreparedStatement sql =null;
        try{
            String authent = "SELECT COUNT(1) FROM Users WHERE username ='"+ username+ "' AND password ='"+ password+"'";

            sql = con.prepareStatement(authent);
            ResultSet rs = sql.executeQuery();
            if (rs.next() == false) {
                System.out.println("username or password is invalid");
                success=false;
            } else {
                String que = "INSERT INTO Reviews(username,productID,reviewText,rating,date) VALUES(?,?,?,?,?)";
                stnew = con.prepareStatement(que);
                stnew.setString(1,username);
                stnew.setInt(2,productID);
                stnew.setString(3,reviewText);
                stnew.setFloat(4,rating);
                java.util.Date date=new java.util.Date();
                java.sql.Date sqlDate=new java.sql.Date(date.getTime());
                stnew.setDate(5,sqlDate);
                stnew.addBatch();
                stnew.executeUpdate();
                con.commit();
            }
        }catch (SQLException e) {
            e.printStackTrace();
            con.rollback();
            success=false;
        } finally {
            try {
                // Close connection
                if (sql != null) {
                    sql.close();
                }
                if (stnew != null) {
                    stnew.close();
                }
                return success;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
    }

    /**
     * A new product is added to the database with the given name and description and the
     * given initial stock value. This operation should provide an ID for the product which can
     * be used in future operations.
     */
    private static boolean addProduct(Connection con, String name, String description, int price,int initialStock) throws Exception{
        boolean success=true;
        PreparedStatement stnew = null;
        try{
            String que = "INSERT INTO Products(name, description, price, quantity) VALUES(?,?,?,?)";
            stnew = con.prepareStatement(que);
            stnew.setString(1,name);
            stnew.setString(2,description);
            stnew.setInt(3,price);
            stnew.setInt(4,initialStock);
            stnew.addBatch();
            stnew.executeUpdate();
            con.commit();
        }catch (SQLException e) {
            e.printStackTrace();
            success=false;
        } finally {
            try {
                if (stnew != null) {
                    stnew.close();
                }
                return success;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
    }

    //Adds new inventory associated with the product, adding to the current stock level.
    private static void updateStockLevel(Connection con, int productID, int itemCountToAdd) throws Exception{
        PreparedStatement update = null;
        try{
//            String quant = "SELECT quantity FROM Products WHERE id ="+ productID;
//            checkprod = con.prepareStatement(quant);
//            ResultSet rs1=checkprod.executeQuery(quant);
//            int availablequantity=0;
//            if(rs1.next()){
//                availablequantity = rs1.getInt("quantity");
//            }
            String updatequant = "UPDATE Products SET quantity = quantity + "+itemCountToAdd+ " WHERE id ="+productID;
            update = con.prepareStatement(updatequant);
//            update.setInt(1, availablequantity+itemCountToAdd);
//            update.setInt(2, productID);
            update.executeUpdate();
            con.commit();
        }catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (update != null) {
                    update.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    //Return the product information and all the reviews for the given product including the
    //    username of the reviewing user, the rating, and the text of the review.
    private static List<String> getProductAndReviews(Connection con, int productID)throws Exception{
        String st="SELECT * FROM Products WHERE id="+productID;

        PreparedStatement p = con.prepareStatement(st);
        ResultSet rs = p.executeQuery();
        List<String> ll=new ArrayList<>();
        if (rs.next()) {
            ll.add(rs.getString("name"));
            ll.add(rs.getString("description"));
        }

        String st2="SELECT * FROM Reviews WHERE productID="+productID;
        PreparedStatement p2 = con.prepareStatement(st2);
        ResultSet rs2 = p2.executeQuery();
        if (rs2.next()) {
            ll.add(rs2.getString("username"));
            ll.add(rs2.getString("reviewText"));
            ll.add(String.valueOf(rs2.getFloat("rating")));
        }
        return ll;
    }

    //Get the average rating value for a given user by adding the ratings for all products and
    //    dividing by the total number of reviews the user has provided.
    private static float getAverageUserRating(Connection con,String username)throws Exception{
        String avgrating = "select round(avg(rating),2) as avg_rating from Reviews where username = '"+username+"'";
        PreparedStatement pp = con.prepareStatement(avgrating);
        ResultSet rs = pp.executeQuery();
        float avg=0;
        if (rs.next()) {
            avg = rs.getFloat("avg_rating");
        }
        return avg;
    }




}

