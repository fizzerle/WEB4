package at.ac.tuwien.big.we16.ue4.controller;

import at.ac.tuwien.big.we16.ue4.error.FormError;
import at.ac.tuwien.big.we16.ue4.model.User;
import at.ac.tuwien.big.we16.ue4.service.AuthService;
import at.ac.tuwien.big.we16.ue4.service.UserService;
import twitter4j.JSONException;
import twitter4j.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

public class UserController {
    private final UserService userService;
    private final AuthService authService;
    private final Date date = new Date(811807200000l);
    public UserController(UserService userService, AuthService authService) {
        this.userService = userService;
        this.authService = authService;
    }

    public void getRegister(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	//TODO: Try to register and login if successful, send back login information
        if (this.authService.isLoggedIn(request.getSession())) {
            response.sendRedirect("/");
            return;
        }
        request.setAttribute("error", new FormError());
        request.getRequestDispatcher("/views/registration.jsp").forward(request, response);
    }

    public void postRegister(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        FormError formError = new FormError();
        /*Date date = userService.parseDate(request.getParameter("dateofbirth"));
        if (date == null) {
            formError.setDateFormatError(true);
        }*/
        User user = new User(
                request.getParameter("salutation"),
                request.getParameter("firstname"),
                request.getParameter("lastname"),
                request.getParameter("email"),
                request.getParameter("password"),
                date
        );
        //TODO: Instead of selecting a view, just send back login data
        formError = this.userService.createUser(user, formError);
        if (formError.isAnyError()) {
            response.setContentType("application/json");
            PrintWriter out = response.getWriter();
            JSONObject obj = new JSONObject();
            try {
                obj.put("success",false);
            } catch (JSONException e1) {
                e1.printStackTrace();
            }
            out.write(obj.toString());
        } else {
            this.authService.login(request.getSession(), user);
            if (this.authService.isLoggedIn(request.getSession())) {
                try{
                    HttpSession session = request.getSession();
                    user.getConvertedBalance();
                    response.setContentType("application/json");
                    response.setCharacterEncoding("UTF-8");
                    PrintWriter out = response.getWriter();
                    //JSONObject jsonObj = (JSONObject) JSONValue.parse(request.getParameter("para"));
                    //System.out.println(jsonObj.get("message"));
                    JSONObject obj = new JSONObject();
                    obj.put("success",true);
                    obj.put("name",user.getFullName());
                    obj.put("balance",user.getConvertedBalance());
                    obj.put("running",user.getRunningAuctionsCount());
                    obj.put("lost",user.getLostAuctionsCount());
                    obj.put("won",user.getWonAuctionsCount());
                    out.write(obj.toString());
                }catch (JSONException e){
                    e.printStackTrace();
                    throw new ServletException(e.getMessage());
                }
                return;
            }
        }
    }


}
