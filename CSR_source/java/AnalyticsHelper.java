package helpers;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class AnalyticsHelper {
 
  public static List<String> listCustomersAlpha() {
    Connection conn = null;
    Statement st = null;
    ResultSet res = null;

    List<String> customers = new ArrayList<String>();
    
    // establish connection
    try {
      try {
        conn = HelperUtils.connect();
      }catch(Exception e) {
        System.err.println("Internal Server Error. This shouldn't happen.");
        return customers;
      }
        st = conn.createStatement();
        String query = "SELECT name FROM users WHERE role = 'customer' ORDER BY name";
        res = st.executeQuery(query);
        while (res.next()) {
          customers.add(res.getString(1));
        }
        return customers;
    } catch (Exception e) {
      System.err.println("Some error happened!<br/>" + e.getLocalizedMessage());
      return customers;
    } finally {
        try {
          st.close();
          conn.close();
        } catch(SQLException e) {
          e.printStackTrace();
        }
    }
  }

  public static List<ProductWithCategoryName> listProductsAlpha(String selectedCategory) {
    Connection conn = null;
    Statement st = null;
    ResultSet res = null;
    String categoryFilter = selectedCategory;
    String query = "";
    List<ProductWithCategoryName> products = new ArrayList<ProductWithCategoryName>();
    try {
      try {
        conn = HelperUtils.connect();
      }  catch (Exception e) {
        System.err.println("Internal Server Error. This shouldn't happen.");
        return products;
      }
        st = conn.createStatement();

        // if the category filter is set to All
        if (categoryFilter.equals("All")) {
          query = "SELECT p.id, c.name, p.name, p.sku, p.price FROM products p, categories c WHERE p.cid = c.id ORDER BY p.name";
        }
        // if the category filter is set to any other category
        else {
          query = "SELECT p.id, c.name, p.name, p.sku, p.price FROM products p, categories c WHERE p.cid = c.id AND c.name = '" + categoryFilter + "' ORDER BY p.name";
        }

        res = st.executeQuery(query);
        while (res.next()) {
          Integer id = res.getInt(1);
          String cname = res.getString(2);
          String name = res.getString(3);
          String sku = res.getString(4);
          Integer price = res.getInt(5);

          products.add(new ProductWithCategoryName(id, cname, name, sku, price));
        }

        return products;
      } catch (Exception e) {
        System.err.println("Some error happened!<br/>" + e.getLocalizedMessage());
        return products;
    } finally {
        try {
            st.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
  }

  public static List<String> listStatesAlpha() {
    Connection conn = null;
    Statement st = null;
    ResultSet res = null;

    List<String> states = new ArrayList<String>();
    try {
      try {
        conn = HelperUtils.connect();
      }  catch (Exception e) {
        System.err.println("Internal Server Error. This shouldn't happen.");
        return states;
      }
        st = conn.createStatement();
        String query = "SELECT name FROM states";
        
        res = st.executeQuery(query);
        while (res.next()) {
          states.add(res.getString(1));
        }
        
        return states;
      } catch (Exception e) {
        System.err.println("Some error happened!<br/>" + e.getLocalizedMessage());
        return states;
    } finally {
        try {
            st.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
  }
  
  public static String[][] analystCustomersAlpha(String custState, String category, String order, String rowNum, String colNum)
  {
    boolean skip = false;
    Connection conn = null;
    Statement st = null;
    ResultSet res = null;
    ResultSet cols = null;
    ResultSet cols2 = null;
    ResultSet rows = null;
    String query = "SELECT * FROM users LIMIT 0;";
    String cust = "SELECT name FROM users ORDER BY name LIMIT 20;";
    String pro = "SELECT products.name, sales.price FROM sales, products WHERE " +
                 "sales.pid = products.id GROUP BY sales.price, products.name " +
                 "ORDER BY name LIMIT 10 OFFSET (0 + (10 * " + rowNum + "));";
    String cat = "";
    String topCat = "";
    String[][] result = new String[21][11];
    String[] totalSales = new String[21];
    String[] prodSales = new String[11];

    try {
      try {
        conn = HelperUtils.connect();
      }  catch (Exception e) {
        System.err.println("Internal Server Error. This shouldn't happen.");
        return result;
      }
        st = conn.createStatement();

        if(category.equals("All"))
        {
          category = "";

          if( order.equals("Top-K"))
          {
            pro = "SELECT products.name, sales.price, sales.price, SUM(sales.quantity * sales.price) AS sold" +
                  " FROM sales, products WHERE sales.pid = products.id GROUP BY sales.price, " +
                  "products.name, sales.price ORDER BY sold DESC, products.name LIMIT 10 OFFSET (0 + (10 * " +
                  rowNum + "));";
          }
        }
        else
        {
          if( order.equals("Top-K"))
          {
            topCat = "AND products.cid = '" + category + "'";
            pro = "SELECT products.name, sales.price, SUM(sales.price * sales.quantity) AS sold " +
                  "FROM sales, products WHERE products.id = sales.pid " + topCat +
                  " GROUP BY sales.price, products.name ORDER BY sold DESC, products.name LIMIT 10 OFFSET (0 + " +
                  "(10 * " + rowNum + "));";
            cat = "WHERE products.cid = '" + category + "'";
          }
          else
          {
            pro = "SELECT products.name, sales.price FROM products, sales WHERE " +
                  "sales.pid = products.id AND products.cid = " + category +
                  "GROUP BY sales.price, products.name ORDER BY name LIMIT 10 " +
                  "OFFSET (0 + (10 * " + rowNum + "));";
            cat = "WHERE products.cid = '" + category + "'";
            topCat = "AND products.cid = '" + category + "'";
          }
        }

        if(order.equals("Alphabetical"))
        {
          if(custState.equals("Customers"))
          {
            cust = "SELECT users.name, SUM(sales.quantity * sales.price) AS sold " +
            "FROM users, sales, products WHERE sales.uid = users.id AND sales.pid = products.id " +
            topCat + " GROUP BY users.name, users.id ORDER BY users.name LIMIT 20 OFFSET (0 + (20 * " + colNum + "));";

            query = "SELECT userSale.name, userSale.id, prodSale.name, prodSale.id, " +
                    "SUM(sales.quantity * sales.price) AS total FROM (SELECT users.id, users.name FROM sales, products, users " +
                    "WHERE sales.pid = products.id AND sales.uid = users.id " + 
                    topCat + " GROUP BY users.name, users.id ORDER BY name " +
                    "LIMIT 20 OFFSET (0 + (20 * " + colNum + "))) " +
                    "userSale, (SELECT id, name FROM products " + cat + " ORDER " +
                    "BY name LIMIT 10 OFFSET (0 + (10 * " + rowNum + "))) " +
                    "prodSale, sales WHERE sales.pid = prodSale.id AND sales.uid" +
                    " = userSale.id GROUP BY userSale.name, userSale.id, " +
                    "prodSale.name, prodSale.id ORDER BY userSale.name, prodSale.name;";
          }
          else if(custState.equals("States"))
          {
            cust = "SELECT states.name, states.id, SUM(sales.quantity * " +
                   "sales.price) AS sold FROM states, users, sales, " +
                   "products WHERE sales.pid = products.id AND (sales.uid = " +
                   "users.id AND users.state = states.id) " + topCat +" GROUP " +
                   "BY states.name, states.id ORDER BY states.name LIMIT 20 OFFSET " +
                   "(0 + (20 * " + colNum + "));";

            query = "SELECT stateSale.name, stateSale.id, prodSale.name, users.state," + 
                    " SUM(sales.quantity * sales.price) FROM (SELECT id, name FROM products " +
                    cat + " ORDER BY name LIMIT 10 OFFSET (0 + (10 * " + rowNum + 
                    "))) prodSale, (SELECT id, name FROM states ORDER BY name " +
                    "LIMIT 20 OFFSET (0 + (20 * " + colNum + "))) stateSale, " +
                    "users, sales WHERE sales.pid = prodSale.id AND (sales.uid " +
                    "= users.id AND users.state = stateSale.id) GROUP BY stateSale.name, " +
                    "stateSale.id, users.state, prodSale.name ORDER BY stateSale.name, prodSale.name;";
          }
        }
        else if(order.equals("Top-K"))
        {
          if(custState.equals("Customers"))
          {
            cust = "SELECT users.name, SUM(sales.quantity * sales.price) " +
                   "AS sold FROM users, sales, products WHERE sales.uid = " +
                   "users.id AND sales.pid = products.id " + topCat + " GROUP BY " +
                   "users.name, users.id ORDER BY sold DESC, users.name LIMIT 20 OFFSET (" +
                   "0 +(20 * " + colNum + "));";
            query = "SELECT userSale.name, userSale.id, prodSale.name, " +
                    "prodSale.id, SUM(sales.quantity * sales.price) FROM (SELECT users.name," +
                    " users.id, SUM(sales.quantity * sales.price) AS sold " +
                    "FROM users, sales, products WHERE sales.uid = users.id " +
                    "AND sales.pid = products.id " + topCat + " GROUP BY " +
                    "users.name, users.id ORDER BY sold DESC, users.name LIMIT 20 OFFSET " +
                    "(0 + (20 * + " + colNum + "))) userSale, (SELECT products.id, products.name, " +
                    "SUM(sales.price * sales.quantity) AS sold FROM sales, " +
                    "products WHERE products.id = sales.pid " + topCat + " GROUP" +
                    " BY products.id, products.name ORDER BY sold DESC, products.name LIMIT 10 OFFSET " +
                    "(0 + (10 * " + rowNum + "))) prodSale, sales WHERE sales.pid" +
                    " = prodSale.id AND sales.uid = userSale.id GROUP BY " +
                    "userSale.sold, userSale.name, userSale.id, prodSale.name, prodSale.sold," +
                    " prodSale.id ORDER BY userSale.sold DESC, userSale.name, prodSale.sold DESC, prodSale.name;";
          }
          else if (custState.equals("States"))
          {
            cust = "SELECT states.name, states.id, SUM(sales.quantity * " +
                   "sales.price) AS sold FROM states, users, sales, " +
                   "products WHERE sales.pid = products.id AND (sales.uid = " +
                   "users.id AND users.state = states.id) " + topCat + "GROUP " +
                   "BY states.name, states.id ORDER BY sold DESC, states.name LIMIT 20 OFFSET" +
                   " (0 + (20 * " + colNum + "));";
            query = "SELECT stateSale.name, stateSale.id, prodSale.name, " +
                    "prodSale.id, SUM(sales.quantity * sales.price) FROM (SELECT states.name," +
                    " states.id, SUM(sales.quantity * sales.price) AS sold" +
                    " FROM states, users, sales, products WHERE sales.pid = " +
                    "products.id AND (sales.uid = users.id AND users.state = " +
                    "states.id) " + topCat + " GROUP BY states.name, states.id " +
                    "ORDER BY sold DESC LIMIT 20 OFFSET (0 + (20 * " + colNum +
                    "))) stateSale, (SELECT products.id, products.name, SUM(sales.price * " +
                    "sales.quantity) AS sold FROM sales, products WHERE " +
                    "products.id = sales.pid " + topCat + " GROUP BY " +
                    "products.id, products.name ORDER BY sold DESC LIMIT 10 OFFSET " +
                    "(0 + (10 * " + rowNum + "))) prodSale, users, sales WHERE sales.pid = prodSale.id " +
                    "AND (sales.uid = users.id AND users.state = stateSale.id) " +
                    "GROUP BY stateSale.sold, stateSale.name, stateSale.id, prodSale.sold," +
                    "prodSale.name, prodSale.id ORDER BY stateSale.sold DESC, stateSale.name, prodSale.sold DESC, prodSale.name;";
          }
        }

        cols = st.executeQuery(cust);

        for(int i = 1; i < 21; i++)
        {
          if(cols.isLast() || cols.isAfterLast())
          {
            break;
          }

        cols.next();
        result[i][0] = cols.getString(1);
        totalSales[i] = cols.getString("sold");
        }

        rows = st.executeQuery(pro);

        for(int j = 1; j < 11; j++)
        {
          if(rows.isLast() || rows.isAfterLast())
          {
            break;
          }

          rows.next();
          result[0][j] = rows.getString(1);
          prodSales[j] = rows.getString(2);
        }

        res = st.executeQuery(query);
        if(!res.next())
        {
          skip = true;
        }

        for(int i = 1; i < 21; i++)
        {
          for(int j = 1; j < 11; j++)
          {
            if(!skip)
            {
              if(res.getString(1).equals(result[i][0]) && res.getString(3).equals(result[0][j]))
              {
                result[i][j] = "$" + res.getString(5);

                if(!res.isLast())
                {
                  res.next();
                }
              }
              else
              {
                result[i][j] = "$0";
              }
            }
            else
            {
              result[i][j] = "$0";
            }
          }
        }

        for(int i = 1; i < 21; i++)
        {
        if (totalSales[i] == null) {
          continue;
        }
        else {
          result[i][0] = result[i][0] + "</br> ($" + totalSales[i] + ")";
        }
        }
        
        for(int i = 1; i < 11; i++)
        {
          if(prodSales[i] == null)
          {
            continue;
          }
          else
          {
            result[0][i] = result[0][i] + "</br> ($" + prodSales[i] + ")";
          }
        }

        return result;
      } catch (Exception e) {
        System.err.println("Some error happened!<br/>" + e.getLocalizedMessage());
        return result;
    } finally {
        try {
            st.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
  }
}
