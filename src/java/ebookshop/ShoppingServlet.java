package ebookshop;

import java.io.IOException;
import java.util.ArrayList;
import javax.servlet.ServletException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.RequestDispatcher;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServletResponse;

@WebServlet(name = "ShoppingServlet", urlPatterns = {"/ctrl"})
public class ShoppingServlet extends HttpServlet {

  ArrayList<String> bookList = new ArrayList();
  
  @Override
  public void init(ServletConfig conf) throws ServletException {
    bookList.add("Learn HTML5 and JavaScript for iOS. Scott Preston $39.99");
    bookList.add("Java 7 for Absolute Beginners. Jay Bryant $39.99");
    bookList.add("Beginning Android 4. Livingston $39.99");
    bookList.add("Pro Spatial with SQL Server 2012. Alastair Aitchison $59.99");
    bookList.add("Beginning Database Design. Clare Churcher $34.99");
    super.init(conf);
  }

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse res)
          throws ServletException, IOException {
    doPost(req, res);
  }

  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse res)
          throws ServletException, IOException {
    HttpSession session = req.getSession(true);
    ArrayList<Book> shoplist = (ArrayList<Book>) session.getAttribute("ebookshop.cart");
    String do_this = req.getParameter("do_this");
    if (do_this == null) {
      session.setAttribute("ebookshop.list", bookList);
      forward(req, res, "/index.jsp");
      return;
    }

    switch (do_this) {

      case "buy":
        session.invalidate();
        res.sendRedirect("ctrl");
        break;

      case "checkout":
        float dollars = 0;
        int books = 0;
        for (Book aBook : shoplist) {
          float price = aBook.getPrice();
          int qty = aBook.getQuantity();
          dollars += price * qty;
          books += qty;
        }
        req.setAttribute("dollars", Float.toString(dollars));
        req.setAttribute("books", Integer.toString(books));
        forward(req, res, "/Checkout.jsp");
        break;

      case "remove":
        String pos = req.getParameter("position");
        shoplist.remove((new Integer(pos)).intValue());
        session.setAttribute("ebookshop.cart", shoplist);
        forward(req, res, "/index.jsp");
        break;

      case "add":
        Book aBook = getBook(req);
        if (aBook != null) {
          if (shoplist == null) {  // the shopping cart is empty
            shoplist = new ArrayList<>();
            shoplist.add(aBook);
          } else {  // update the #copies if the book is already there
            boolean found = false;
            for (int i = 0; i < shoplist.size() && !found; i++) {
              Book b = (Book) shoplist.get(i);
              if (b.getTitle().equals(aBook.getTitle())) {
                b.setQuantity(b.getQuantity() + aBook.getQuantity());
                shoplist.add(i, b);
                found = true;
              }
            }
            if (!found) {  // if it is a new book => Add it to the shoplist
              shoplist.add(aBook);
            }
          }
        }
        session.setAttribute("ebookshop.cart", shoplist);
        forward(req, res, "/index.jsp");
        break;
    }

  }

  private void forward(HttpServletRequest req, HttpServletResponse res, String path) throws IOException, ServletException {
    ServletContext sc = getServletContext();
    RequestDispatcher rd = sc.getRequestDispatcher(path);
    rd.forward(req, res);
  }

  private Book getBook(HttpServletRequest req) {
    String myBook = req.getParameter("book");
    if (myBook == null) {
      return null;
    }
    int n = myBook.indexOf('$');
    String title = myBook.substring(0, n);
    String price = myBook.substring(n + 1);
    String qty = req.getParameter("qty");
    return new Book(title, Float.parseFloat(price), Integer.parseInt(qty));
  }
}
