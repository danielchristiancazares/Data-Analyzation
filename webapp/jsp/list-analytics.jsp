<%@page
    import="java.util.List"
    import="helpers.*"
    import="java.sql.ResultSet"%>

<%@ page contentType="text/html; charset=utf-8" language="java"%>

<!-- Set query options form --> 
    <form action="/jsp/analytics.jsp" method="GET">
            Rows:   <select name="rows">
                      <option value="Customers">Customers</option>
                      <option value="States">States</option>
                    </select> </br>
            Order:  <select name="order">
                      <option value="Alphabetical">Alphabetical</option>
                      <option value="Top-K">Top-K</option>
                    </select> </br>
            <% List<CategoryWithCount> categories = CategoriesHelper.listCategories(); %>
            Category: <select name="category">
                        <option value="All">All</option>
                        <%  for (CategoryWithCount c: categories) { %>
                           <option value="<%= c.getId() %>"> <%= c.getName() %></option>
                        <% } %>
                	  </select>
            <input type="submit" value="Run Query">
  </form>

<!-- Display the table -->
<%
    boolean finished = false;
    int noProd = 12;
    /*
       If “Customers” is chosen, then each displayed row corresponds to a customer.
       If “States” is chosen then each row corresponds to a state.
     */
    String rows = request.getParameter("rows");
    if (rows == null) {
      rows = "Customers";
    }

    /*
       If "Alphabetic" is chosen, then the rows are alphabetically ordered.
       If "Top-K" is chosen, then we order them by top sales.
     */
    String order = request.getParameter("order");
    if (order == null) {
      order = "Alphabetical";
    }

    /* Category filters for a specific product category. */
    String cat = request.getParameter("category");
    if (cat == null) {
      cat = "All";
    }

    String[][] result; // Stores the returned results.

    result = AnalyticsHelper.analystCustomersAlpha(rows, cat, order, "0", "0"); // Returns a list of alphabetically sorted customers.

    String currName = null;
%>
    <table class="table table-striped" align="center">
      <%for(int i = 0; i < 21; i++)
        {%>
          <tr>
        <%
          for(int j = 0; j < 11; j++)
          {
            if(j == 0 && i == 0)
            {
          %>  
              <td><%= rows%>\Products</td>
          <%  continue;
            }
            else if (result[i][0] == null && i > 0)
            {
              finished = true;
              break;
            }
            else if(j == noProd)
            {
              break;
            }
            else if(result[0][j] == null && j > 0)
            {
              noProd = j;
              break;
            }
          %>
            <td><%=result[i][j]%></td>
        <%}%>
        </tr>
      <%  if(finished)
          {
            break;
          }
        }%>
    </table>
    <form action="/jsp/new-analytics.jsp" method="GET">
      <input type="hidden" name="nextRow" value="1">
      <input type="hidden" name="nextCol" value="0">
      <input type="hidden" name="rows" value="<%= rows%>">
      <input type="hidden" name="order" value="<%= order%>">
      <input type="hidden" name="category" value="<%= cat%>">
      <input type="submit" value="Next 10 Products >>>">
    </form>
    <form action="/jsp/new-analytics.jsp" method="GET">
      <input type="hidden" name="nextCol" value="1">
      <input type="hidden" name="nextRow" value="0">
      <input type="hidden" name="rows" value="<%= rows%>">
      <input type="hidden" name="order" value="<%= order%>">
      <input type="hidden" name="category" value="<%= cat%>">
      <input type="submit" value="Next 20 <%= rows%> >>>">
    </form>
