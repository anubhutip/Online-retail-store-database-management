package edu.rit;

import com.devskiller.jfairy.Fairy;
import com.devskiller.jfairy.producer.BaseProducer;
import com.devskiller.jfairy.producer.DateProducer;
import com.devskiller.jfairy.producer.person.Person;
import com.devskiller.jfairy.producer.text.TextProducer;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import com.mongodb.client.model.IndexOptions;
import org.bson.conversions.Bson;

import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.lte;

public class NosqlMongo implements Runnable{
    static MongoCollection<Document> col1;
    static MongoCollection<Document> col2;
    static int productid=1;
    static int userid =1;
    static int orderid=1;
    AtomicInteger atomicint=new AtomicInteger(0);
    //Stores number of operations for a thread
    static List<AtomicInteger> operations =new ArrayList<AtomicInteger>();
    //Stores percentage of products whose stock is less than 0
    static List<Integer> stocklt0 =new ArrayList<Integer>();
    //Stores number of operations in each test
    static List<Integer> operationpertest =new ArrayList<Integer>();


    public static void createConnection(String mongoDBURL, String mongoDBName) throws Exception{
        MongoClient client = getClient(mongoDBURL);
        MongoDatabase db = client.getDatabase(mongoDBName);
        col1 = db.getCollection("Users");
        col2 = db.getCollection("Products");
    }

    public static void main(String[] args) throws Exception {
        final String mongoDBURL = "None";
        final String mongoDBName = "ITEM";

        createConnection(mongoDBURL,mongoDBName);

        Fairy fairy = Fairy.create();

        //int i=1;
        while(userid<1001){
            Person person =fairy.person();
            //BaseProducer name = fairy.baseProducer();
            String username = "user"+userid;
            String password = "password"+userid;
            String firstname =person.getFirstName();
            String lastname = person.getLastName();
            boolean val = createAccount(username,password,firstname,lastname);
            if(val){
                userid++;
            }

        }


        while(productid<1001){

            TextProducer text = fairy.textProducer();
            String name = "prod"+productid;
            String description = text.sentence();
            BaseProducer cost = fairy.baseProducer();
            int price = cost.randomBetween(1,100);
            int initialStock = cost.randomBetween(1,1000);
            boolean val = addProduct(productid, name, description,price,initialStock);
            if(val){
                productid++;
            }
        }


        int k=0;
        while(k<20){
            int j=1;
            while(j<1001){
                //int p=j;
                BaseProducer bint = fairy.baseProducer();
                int id = bint.randomBetween(1,1000);
                String username1 = "user"+id;
                String password1 = "password"+id;
                BaseProducer intval = fairy.baseProducer();
                int prodid = j;
                float rating = intval.randomBetween(0,10);
                TextProducer text1 = fairy.textProducer();
                String reviewText = text1.sentence();
                boolean val = postReview(username1,password1, prodid, rating, reviewText);
                if(val){
                    j++;
                }
            }
            k++;
        }


        while(orderid<10001){
            BaseProducer bord = fairy.baseProducer();
            int id = bord.randomBetween(1,1000);
            String username2 = "user"+id;
            String password2 = "password"+id;

            Map<Integer,Integer> listOfProductsAndQuantities = new HashMap<>();
            while(listOfProductsAndQuantities.size()<10){
                int prodid = bord.randomBetween(1,1000);
                int quant = bord.randomBetween(1,50);
                listOfProductsAndQuantities.put(prodid,quant);
            }
            DateProducer d = fairy.dateProducer();
            LocalDateTime date = d.randomDateInThePast(50);
            boolean val = submitOrder(orderid,true,date.toLocalDate(),username2,password2, listOfProductsAndQuantities);
            if(val){
                orderid++;
            }
        }

        //test1
        Runnable runnable1 = new NosqlMongo();
        Thread t1 = new Thread(runnable1);
        t1.start();
        t1.join();
        Bson query= lte("initialStock", 0);
            long estimatedCount = col2.estimatedDocumentCount();
            long matchingCount = col2.countDocuments(query);
        stocklt0.add((int) ((matchingCount*100)/estimatedCount));
        int sum=0;
        for(AtomicInteger ati:operations){
            sum =sum+ati.intValue();
        }
        operationpertest.add(sum);
        operations.clear();

        //test2
        Runnable runnable2 = new NosqlMongo();
        Thread t2 = new Thread(runnable2);
        Thread t3 = new Thread(runnable2);
        t2.start();
        t3.start();
        t2.join();
        t3.join();
        estimatedCount = col2.estimatedDocumentCount();
        matchingCount = col2.countDocuments(query);
        stocklt0.add((int) ((matchingCount*100)/estimatedCount));
        sum=0;
        for(AtomicInteger ati:operations){
            sum =sum+ati.intValue();
        }
        operationpertest.add(sum);
        operations.clear();

        //test3
        Runnable runnable3 = new NosqlMongo();
        Thread t4 = new Thread(runnable3);
        Thread t5 = new Thread(runnable3);
        Thread t6 = new Thread(runnable3);
        t4.start();
        t5.start();
        t6.start();
        t4.join();
        t5.join();
        t6.join();
        estimatedCount = col2.estimatedDocumentCount();
        matchingCount = col2.countDocuments(query);
        stocklt0.add((int) ((matchingCount*100)/estimatedCount));
        sum=0;
        for(AtomicInteger ati:operations){
            sum =sum+ati.intValue();
        }
        operationpertest.add(sum);
        operations.clear();

        //test4
        Runnable runnable4 = new NosqlMongo();
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
        estimatedCount = col2.estimatedDocumentCount();
        matchingCount = col2.countDocuments(query);
        stocklt0.add((int) ((matchingCount*100)/estimatedCount));
        sum=0;
        for(AtomicInteger ati:operations){
            sum =sum+ati.intValue();
        }
        operationpertest.add(sum);
        operations.clear();

        //test5
        Runnable runnable5 = new NosqlMongo();
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
        estimatedCount = col2.estimatedDocumentCount();
        matchingCount = col2.countDocuments(query);
        stocklt0.add((int) ((matchingCount*100)/estimatedCount));
        sum=0;
        for(AtomicInteger ati:operations){
            sum =sum+ati.intValue();
        }
        operationpertest.add(sum);
        operations.clear();

        //test6
        Runnable runnable6 = new NosqlMongo();
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
        estimatedCount = col2.estimatedDocumentCount();
        matchingCount = col2.countDocuments(query);
        stocklt0.add((int) ((matchingCount*100)/estimatedCount));
        sum=0;
        for(AtomicInteger ati:operations){
            sum =sum+ati.intValue();
        }
        operationpertest.add(sum);
        operations.clear();

        //test7
        Runnable runnable7 = new NosqlMongo();
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
        estimatedCount = col2.estimatedDocumentCount();
        matchingCount = col2.countDocuments(query);
        stocklt0.add((int) ((matchingCount*100)/estimatedCount));
        sum=0;
        for(AtomicInteger ati:operations){
            sum =sum+ati.intValue();
        }
        operationpertest.add(sum);
        operations.clear();

        //test8
        Runnable runnable8 = new NosqlMongo();
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
        estimatedCount = col2.estimatedDocumentCount();
        matchingCount = col2.countDocuments(query);
        stocklt0.add((int) ((matchingCount*100)/estimatedCount));
        sum=0;
        for(AtomicInteger ati:operations){
            sum =sum+ati.intValue();
        }
        operationpertest.add(sum);
        operations.clear();

        //test9
        Runnable runnable9 = new NosqlMongo();
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
        estimatedCount = col2.estimatedDocumentCount();
        matchingCount = col2.countDocuments(query);
        stocklt0.add((int) ((matchingCount*100)/estimatedCount));
        sum=0;
        for(AtomicInteger ati:operations){
            sum =sum+ati.intValue();
        }
        operationpertest.add(sum);
        operations.clear();

        //test10
        Runnable runnable10 = new NosqlMongo();
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
        estimatedCount = col2.estimatedDocumentCount();
        matchingCount = col2.countDocuments(query);
        stocklt0.add((int) ((matchingCount*100)/estimatedCount));
        sum=0;
        for(AtomicInteger ati:operations){
            sum =sum+ati.intValue();
        }
        operationpertest.add(sum);
        operations.clear();

//        System.out.println(stocklt0);
//        System.out.println(operationpertest);


    }

    @Override
    public void run() {
        Random r = new Random();
        int low = 0;
        int high = 101;
        long before = System.nanoTime();

        while(((System.nanoTime()-before)/ (1e9 * 60))<=5){
            int result = r.nextInt(high-low) + low;
            boolean success = selectmethod(result);
            if(success)
                atomicint.getAndIncrement();
        }
        operations.add(atomicint);
    }


    /**CreateAccount 0-3
     AddProduct 4-5
     UpdateStockLevel 6-15
     GetProductAndReviews 16-80
     GetAverageUserRating 81-85
     SubmitOrder 86-95
     PostReview 96-100
     **/
    public boolean selectmethod(int prob){
        Fairy fairy = Fairy.create();
        Person person =fairy.person();
        BaseProducer base = fairy.baseProducer();
        TextProducer text = fairy.textProducer();
        boolean retval=true;
        if(prob<=3){
            String username = "user"+userid;
            String password = "password"+userid;
            String firstname =person.getFirstName();
            String lastname = person.getLastName();
            retval = createAccount(username,password,firstname,lastname);
            if(retval)
                userid++;
        }else if(prob>3 && prob<=5){
            String name = "prod"+productid;
            String description = text.sentence();
            int price = base.randomBetween(1,100);
            int initialStock = base.randomBetween(1,1000);
            retval = addProduct(productid, name, description,price,initialStock);
            if(retval)
                productid++;
        }else if(prob>5 && prob<=15){
            int itemCountToAdd = base.randomBetween(1,100);
            int productID = base.randomBetween(1,1000);
            updateStockLevel(productID, itemCountToAdd);
        }else if(prob>15 && prob<=80){
            int productID = base.randomBetween(1,1000);
            getProductAndReviews(productID);
        }else if(prob>80 && prob<=85){
            int i = base.randomBetween(1,1000);
            String usernameav = "user"+i;
            getAverageUserRating(usernameav);
        }else if(prob>85 && prob<=95){
            boolean val=false;
            int c=10;
            while(!val && c>0) {
                c--;
                int i = base.randomBetween(1, 1000);
                String username = "user" + i;
                String password = "password" + i;
                Map<Integer, Integer> listOfProductsAndQuantities = new HashMap<>();
                while (listOfProductsAndQuantities.size() < 10) {
                    int prodid = base.randomBetween(1, 1000);
                    int quant = base.randomBetween(1, 50);
                    listOfProductsAndQuantities.put(prodid, quant);
                }
                DateProducer d = fairy.dateProducer();
                LocalDateTime date = d.randomDateInThePast(50);
                val = submitOrder(orderid, false, date.toLocalDate(), username, password, listOfProductsAndQuantities);
            }
            if(c!=0){
                orderid++;
                retval=true;
            }else{
                retval=false;
            }

        }else{
            int id = base.randomBetween(1,1000);
            String username = "user"+id;
            String password = "password"+id;
            int prodid = base.randomBetween(1,1000);
            float rating = base.randomBetween(0,10);
            String reviewText = text.sentence();
            retval = postReview(username,password, prodid, rating, reviewText);
        }
        return retval;
    }

    //create account of user and store in collection 1
    private static boolean createAccount(String username,String password,String firstName,String lastName){

        Document d = new Document();
        d.append("_id", username);
        d.append("password", password);
        d.append("firstName", firstName);
        d.append("lastName", lastName);
        List<Document> order=new ArrayList<>();
        d.append("orders",order);
        List<Document> prate=new ArrayList<>();
        d.append("productrating",prate);
        try{
            col1.insertOne(d);
            return true;
        }catch(Exception e){
            return false;
        }
    }

    //Add products and their description in collection 2
    private static boolean addProduct(int productid, String name, String description, int price,int initialStock) {
        Document d = new Document();
        d.append("_id",productid);
        d.append("name", name);
        d.append("description", description);
        d.append("price", price);
        d.append("initialStock", initialStock);
        List<Document> ldoc=new ArrayList<>();
        d.append("review",ldoc);

        try {
            col2.insertOne(d);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    //Authenticate the user,and Submit the order, add order details in collection 1. If it is not initialisation state, then
    //decrease the stock level and dont place order if quantity available is less than required.
    private static boolean submitOrder(int oid, boolean init, LocalDate date, String username, String password, Map<Integer,Integer> listOfProductsAndQuantities){
        AggregateIterable<Document> ot = col1.aggregate(Arrays.asList(new Document("$match",
                        new Document("_id", username)
                                .append("password", password)),
                new Document("$count", "count")));
        Iterator iter = ot.iterator();
        if (iter.hasNext()) {
            int productid = 0;
            int quantity = 0;
            List<Document> pq = new ArrayList<>();

            for (Map.Entry<Integer, Integer> entry : listOfProductsAndQuantities.entrySet()) {
                productid = entry.getKey();
                quantity = entry.getValue();

                if (!init) {
                    AggregateIterable<Document> ot1 = col2.aggregate(Arrays.asList(new Document("$match",
                            new Document("_id", productid))));
                    Iterator iter1 = ot1.iterator();

                    Document doc2 = (Document) iter1.next();
                    int availablestock = doc2.getInteger("initialStock");
                    if (availablestock <= 0) {
                        return false;
                    }

                    Bson search = Filters.eq("_id", productid);
                    int dec = -quantity;
                    Bson update = Updates.inc("initialStock", dec);
                    col2.updateOne(search, update);
                    if(availablestock-quantity<0){
                        quantity=availablestock;
                    }
                }

                Document docpq = new Document();
                docpq.append("product", productid);
                docpq.append("quantity", quantity);
                pq.add(docpq);
            }
            Document order = new Document("orderid", oid)
                    .append("date", Date.valueOf(date))
                    .append("cart", pq);
            Bson search = Filters.eq("_id", username);
            Bson update = Updates.push("orders", order);
            col1.updateOne(search, update);
        } else {
            return false;
        }
        return true;
    }

    //post review of product, add details in product collection (2). First authenticate user, then check if
    //a user has posted review for a product, if not then post review.
    private static boolean postReview(String username,String password, int productID, float rating, String reviewText){

        AggregateIterable<Document> ot = col1.aggregate(Arrays.asList(new Document("$match",
                        new Document("_id", username)
                                .append("password", password)),
                new Document("$count", "count")));
        Iterator iter = ot.iterator();
        if (iter.hasNext()) {
            AggregateIterable<Document> ot1 = col2.aggregate(Arrays.asList(new Document("$match",
                            new Document("_id", productID))));
            Iterator iter1 = ot1.iterator();

            Document doc2 = (Document)iter1.next();


            List<Document> ll = doc2.get("review", List.class);
            if (ll.isEmpty()) {
                //update
                java.util.Date date = new java.util.Date();
                java.sql.Date sqlDate = new java.sql.Date(date.getTime());

                Document newreview = new Document("user", username)
                        .append("text", reviewText)
                        .append("rating", rating)
                        .append("Date", sqlDate);

                Bson search = Filters.eq("_id", productID);
                Bson update = Updates.push("review", newreview);
                col2.updateOne(search, update);

                Document pr = new Document("product", productID)
                        .append("rating", rating);
                Bson search1 = Filters.eq("_id", username);
                Bson update1 = Updates.push("productrating", pr);
                col1.updateOne(search1, update1);


            } else {
                for (Document doc : ll) {
                    if (doc.get("user").equals(username)) {
                        return false;
                    }
                }
                //update
                java.util.Date date = new java.util.Date();
                java.sql.Date sqlDate = new java.sql.Date(date.getTime());

                Document newreview = new Document("user", username)
                        .append("text", reviewText)
                        .append("rating", rating)
                        .append("Date", sqlDate);

                Bson search = Filters.eq("_id", productID);
                Bson update = Updates.push("review", newreview);
                col2.updateOne(search, update);

                Document pr = new Document("product", productID)
                        .append("rating", rating);
                Bson search1 = Filters.eq("_id", username);
                Bson update1 = Updates.push("productrating", pr);
                col1.updateOne(search1, update1);

            }
        } else {
            return false;
        }
        return true;
    }


    //Adds new inventory associated with the product, adding to the current stock level.
    private static void updateStockLevel(int productID, int itemCountToAdd){

        Bson search = Filters.eq("_id", productID);
        Bson update = Updates.inc("initialStock",itemCountToAdd);
        col2.updateOne(search, update);

    }

    //return the details about a product from collection 2
    private static List<List<String>> getProductAndReviews(int productID){
        List<List<String>> ll=new ArrayList<>();
        List<String> l1=new ArrayList<>();
        AggregateIterable<Document> ot = col2.aggregate(Arrays.asList(new Document("$match",
                        new Document("_id", productID))));

        Iterator iter = ot.iterator();
        Document doc = (Document) iter.next();
        String name = doc.getString("name");
        String desc = doc.getString("description");
        int price = doc.getInteger("price");
        l1.add(name);
        l1.add(desc);
        l1.add(String.valueOf(price));
        ll.add(l1);

        List<Document> reviews = (List<Document>) doc.get("review");
        for(Document r:reviews){
            List<String> l2=new ArrayList<>();
            String u =r.getString("user");
            String t =r.getString("text");
            float rate = r.getDouble("rating").floatValue();
            l2.add(u);
            l2.add(t);
            l2.add(String.valueOf(rate));
            ll.add(l2);
        }

        return ll;
    }

    //Finds average of rating of all the products a user has rated from collection 1
    private static float getAverageUserRating(String username){
        AggregateIterable<Document> ot = col1.aggregate(Arrays.asList(new Document("$match",
                        new Document("_id", username)),
                new Document("$project",
                        new Document("avgrate",
                                new Document("$avg", "$productrating.rating")))));
        Iterator iter = ot.iterator();

        Document doc = (Document) iter.next();
        float avgrating = doc.getDouble("avgrate").floatValue();
        return avgrating;
    }

    private static MongoClient getClient(String mongoDBURL) {
        MongoClient client = null;
        if (mongoDBURL.equals("None"))
            client = new MongoClient();
        else
            client = new MongoClient(new MongoClientURI(mongoDBURL));
        return client;
    }
}
